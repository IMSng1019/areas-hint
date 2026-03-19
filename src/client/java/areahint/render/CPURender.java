package areahint.render;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CPU娓叉煋瀹炵幇绫?
 * 浣跨敤CPU杩涜杞欢娓叉煋锛屼笉浣跨敤GPU
 */
public class CPURender implements RenderManager.IRender {
    // Minecraft瀹㈡埛绔疄渚?
    private final MinecraftClient client;

    // 绾跨▼姹狅紝鐢ㄤ簬寮傛娓叉煋
    private final ExecutorService renderThreadPool;

    // 褰撳墠鍔ㄧ敾鐘舵€?
    private AnimationState animationState = AnimationState.NONE;

    // 褰撳墠鏄剧ず鐨勬枃鏈?
    private String currentText = null;

    // 褰撳墠棰滆壊
    private String currentColor = "#FFFFFF";

    // 鍔ㄧ敾寮€濮嬫椂闂?
    private long animationStartTime = 0;

    // 鍔ㄧ敾鎸佺画鏃堕棿锛堟绉掞級
    private static final long ANIMATION_IN_DURATION = 500; // 杩涘叆鍔ㄧ敾鎸佺画鏃堕棿
    private static final long ANIMATION_STAY_DURATION = 3000; // 鏄剧ず鎸佺画鏃堕棿
    private static final long ANIMATION_OUT_DURATION = 300; // 閫€鍑哄姩鐢绘寔缁椂闂?
    
    // 涓婁竴甯х殑Y鍋忕Щ鍜岄€忔槑搴︼紝鐢ㄤ簬骞虫粦鎻掑€?
    private float lastYOffset = 0;
    private float lastAlpha = 0;
    
    // 鎻掑€肩郴鏁帮紙0-1涔嬮棿锛岃秺灏忚秺骞虫粦锛屼絾寤惰繜瓒婂ぇ锛?
    private static final float INTERPOLATION_FACTOR = 0.15f;
    
    // 棰勬覆鏌撶殑鏂囨湰鍥惧儚
    private final AtomicReference<BufferedImage> renderedTextImage = new AtomicReference<>();
    
    // 瀛椾綋璁剧疆
    private static final Font FONT = new Font("Arial", Font.BOLD, 32); // 澧炲ぇ瀛椾綋澶у皬锛屼粠24鍒?2
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 128);
    
    /**
     * 鏋勯€犳柟娉?
     * @param client Minecraft瀹㈡埛绔疄渚?
     */
    public CPURender(MinecraftClient client) {
        this.client = client;
        this.renderThreadPool = Executors.newFixedThreadPool(2); // 鍒涘缓鍥哄畾澶у皬鐨勭嚎绋嬫睜
        
        // 娉ㄥ唽Tick浜嬩欢鐢ㄤ簬鏇存柊鍔ㄧ敾鐘舵€?
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        // 娉ㄥ唽HUD娓叉煋浜嬩欢
        HudRenderCallback.EVENT.register(this::onHudRender);
    }
    
    /**
     * HUD娓叉煋浜嬩欢澶勭悊
     * @param drawContext 缁樺埗涓婁笅鏂?
     * @param tickDelta tick闂撮殧鏃堕棿
     */
    private void onHudRender(MatrixStack matrices, float tickDelta) {
        if (animationState == AnimationState.NONE || currentText == null || client.player == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // 璁＄畻鏂囨湰鍦ㄥ睆骞曚笂鐨勪綅缃?
        int x = screenWidth / 2;
        int y = screenHeight / 4; // 灞忓箷1/4浣嶇疆
        
        // 鏍规嵁鍔ㄧ敾鐘舵€佽绠梇鍋忕Щ鍜岄€忔槑搴?
        float alpha = 1.0f;
        float yOffset = 0.0f;
        
        long elapsedTime = System.currentTimeMillis() - animationStartTime;
        float progress = 0;
        
        switch (animationState) {
            case IN:
                // 纭繚娓愬叆鍔ㄧ敾鐨勮繘搴﹁绠楁洿鍔犲钩婊?
                progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_IN_DURATION);
                progress = easeOutCubic(progress); // 浣跨敤缂撳姩鍑芥暟
                alpha = progress;
                // 娓愬叆鍔ㄧ敾鏃朵笉浣跨敤Y鍋忕Щ锛屼繚鎸佸浐瀹氫綅缃?
                yOffset = 0.0f;
                break;
            case STAY:
                // 淇濇寔鍘熶綅锛屽畬鍏ㄤ笉閫忔槑
                alpha = 1.0f;
                yOffset = 0.0f;
                break;
            case OUT:
                progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_OUT_DURATION);
                progress = easeInCubic(progress); // 浣跨敤缂撳姩鍑芥暟
                yOffset = -15.0f * progress; // 浣跨敤娴偣鏁拌绠楋紝閬垮厤鏁存暟鎴柇瀵艰嚧鐨勯棯鐑?
                alpha = 1.0f - progress;
                break;
            case NONE:
                return; // 涓嶆覆鏌?
        }
        
        // 搴旂敤骞虫粦鎻掑€硷紝鍑忓皯闂儊
        // 瀵逛簬娓愬叆鍔ㄧ敾锛岀洿鎺ヤ娇鐢ㄨ绠楀€硷紝涓嶈繘琛屾彃鍊煎鐞?
        if (animationState == AnimationState.IN && elapsedTime < 100) {
            // 娓愬叆鍔ㄧ敾寮€濮嬮樁娈碉紝鐩存帴浣跨敤璁＄畻鍊?
            lastYOffset = yOffset;
            lastAlpha = alpha;
        } else {
            // 鍏朵粬鎯呭喌浣跨敤鎻掑€?
            yOffset = lastYOffset * (1.0f - INTERPOLATION_FACTOR) + yOffset * INTERPOLATION_FACTOR;
            alpha = lastAlpha * (1.0f - INTERPOLATION_FACTOR) + alpha * INTERPOLATION_FACTOR;
            
            // 淇濆瓨褰撳墠鍊肩敤浜庝笅涓€甯ф彃鍊?
            lastYOffset = yOffset;
            lastAlpha = alpha;
        }
        
        // 娓叉煋鏂囨湰
        Text text = areahint.util.TextCompat.of(currentText);
        TextRenderer textRenderer = client.textRenderer;
        
        // 搴旂敤缂╂斁鏉ュ澶ф枃鏈昂瀵?
        matrices.push();
        // 浣跨敤娴偣鏁扮洿鎺ヤ紶閫掔粰鐭╅樀鍙樻崲锛岄伩鍏嶆暣鏁拌浆鎹?
        matrices.translate(x, y + yOffset, 0);
        // 鏍规嵁閰嶇疆鑾峰彇瀛楀箷澶у皬
        float textScale = getTextScale();
        matrices.scale(textScale, textScale, 1.0f);
        
        // 鑾峰彇鏈缉鏀剧殑鏂囨湰瀹藉害
        int textWidth = textRenderer.getWidth(text);
        
        // 璁＄畻鏈€缁堜綅缃?(姝ｇ‘璁＄畻灞呬腑浣嶇疆)
        int finalX = -textWidth / 2;
        int finalY = 0;
        
        // CPU娓叉煋鐗规晥锛氭坊鍔犱竴涓畝鍗曠殑鑳屾櫙妗?
        int bgColor = getAlphaColor(0x000000, alpha * 0.5f);
        DrawableHelper.fill(matrices, finalX - 4, finalY - 2, finalX + textWidth + 4, finalY + textRenderer.fontHeight + 2, bgColor);

        // 缁樺埗甯︽湁闃村奖鐨勬枃鏈紙鏀寔闂儊棰滆壊锛?
        long now = System.currentTimeMillis();
        if (FlashColorHelper.isFlashMode(currentColor)) {
            if (FlashColorHelper.isPerCharMode(currentColor)) {
                int xOff = finalX;
                for (int i = 0; i < currentText.length(); i++) {
                    String ch = String.valueOf(currentText.charAt(i));
                    int charRgb = FlashColorHelper.getCharColor(currentColor, now, i);
                    textRenderer.drawWithShadow(matrices, areahint.util.TextCompat.of(ch), xOff, finalY, getAlphaColor(charRgb, alpha));
                    xOff += textRenderer.getWidth(ch);
                }
            } else {
                int rgb = FlashColorHelper.getWholeColor(currentColor, now);
                textRenderer.drawWithShadow(matrices, text, finalX, finalY, getAlphaColor(rgb, alpha));
            }
        } else {
            int rgb = parseHexColor(currentColor);
            textRenderer.drawWithShadow(matrices, text, finalX, finalY, getAlphaColor(rgb, alpha));
        }

        // 鎭㈠鐭╅樀鐘舵€?
        matrices.pop();
        
        // 杈撳嚭璋冭瘯淇℃伅
        if (animationState == AnimationState.IN) {
            AreashintClient.LOGGER.debug("CPURender: 娓愬叆鍔ㄧ敾 - 鍖哄煙鏍囬: {}, 杩涘害: {}, 閫忔槑搴? {}", 
                currentText, progress, alpha);
        }
    }
    
    /**
     * 瀹㈡埛绔疶ick浜嬩欢澶勭悊锛岀敤浜庢洿鏂板姩鐢荤姸鎬?
     * @param minecraftClient Minecraft瀹㈡埛绔疄渚?
     */
    private void onClientTick(MinecraftClient minecraftClient) {
        if (animationState == AnimationState.NONE || currentText == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;
        
        // 鏇存柊鍔ㄧ敾鐘舵€?
        if (animationState == AnimationState.IN && elapsedTime >= ANIMATION_IN_DURATION) {
            animationState = AnimationState.STAY;
            animationStartTime = currentTime;
            AreashintClient.LOGGER.debug("CPURender: 鍔ㄧ敾鐘舵€佹洿鏂?IN 鈫?STAY");
        } else if (animationState == AnimationState.STAY && elapsedTime >= ANIMATION_STAY_DURATION) {
            animationState = AnimationState.OUT;
            animationStartTime = currentTime;
            // 閲嶇疆鎻掑€煎彉閲忎互纭繚骞虫粦杩囨浮
            lastYOffset = 0.0f;
            lastAlpha = 1.0f;
            AreashintClient.LOGGER.debug("CPURender: 鍔ㄧ敾鐘舵€佹洿鏂?STAY 鈫?OUT");
        } else if (animationState == AnimationState.OUT && elapsedTime >= ANIMATION_OUT_DURATION) {
            animationState = AnimationState.NONE;
            currentText = null;
            AreashintClient.LOGGER.debug("CPURender: 鍔ㄧ敾鐘舵€佹洿鏂?OUT 鈫?NONE");
        }
    }
    
    /**
     * 浣跨敤CPU娓叉煋鏂囨湰鍒板浘鍍?
     * @param text 瑕佹覆鏌撶殑鏂囨湰
     * @return 鍖呭惈娓叉煋鏂囨湰鐨勫浘鍍?
     */
    private BufferedImage renderTextToBitmap(String text) {
        // 鍒涘缓涓€涓复鏃跺浘鍍忔潵娴嬮噺鏂囨湰灏哄
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tmp.createGraphics();
        g2d.setFont(FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 20; // 娣诲姞涓€浜涜竟璺?
        int height = fm.getHeight() + 10;
        g2d.dispose();
        
        // 鍒涘缓瀹為檯鐨勫浘鍍?
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        
        // 鍚敤鎶楅敮榻?
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setFont(FONT);
        
        // 缁樺埗鏂囨湰闃村奖
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(text, 11, fm.getAscent() + 1);
        
        // 缁樺埗涓绘枃鏈?
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(text, 10, fm.getAscent());
        
        g2d.dispose();
        
        return image;
    }
    
    /**
     * 鏍规嵁閰嶇疆鑾峰彇鏂囨湰缂╂斁姣斾緥
     * @return 鏂囨湰缂╂斁姣斾緥
     */
    private float getTextScale() {
        String size = ClientConfig.getSubtitleSize();
        switch (size) {
            case "extra_large":
                return 3.0f;
            case "large":
                return 2.5f;
            case "medium_large":
                return 2.0f;
            case "medium":
                return 1.5f;
            case "medium_small":
                return 1.2f;
            case "small":
                return 1.0f;
            case "extra_small":
                return 0.8f;
            default:
                return 1.5f; // 榛樿涓瓑澶у皬
        }
    }

    /**
     * 灏哛GB棰滆壊鍜岄€忔槑搴﹁浆鎹负ARGB棰滆壊鍊?
     * @param rgb RGB棰滆壊鍊?
     * @param alpha 閫忔槑搴︼紙0.0-1.0锛?
     * @return ARGB棰滆壊鍊?
     */
    private int getAlphaColor(int rgb, float alpha) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        return a | (rgb & 0x00FFFFFF);
    }
    
    @Override
    public void renderTitle(String title, String color) {
        if (title == null || title.isEmpty()) {
            return;
        }

        if (animationState != AnimationState.NONE && title.equals(currentText)) {
            return;
        }

        currentText = title;
        currentColor = color != null ? color : "#FFFFFF";
        animationState = AnimationState.IN;
        animationStartTime = System.currentTimeMillis();
        lastYOffset = 0.0f;
        lastAlpha = 0.0f;

        CompletableFuture.supplyAsync(() -> renderTextToBitmap(title), renderThreadPool)
            .thenAccept(renderedTextImage::set)
            .exceptionally(ex -> {
                AreashintClient.LOGGER.error("娓叉煋鏂囨湰鏃跺嚭閿? " + ex.getMessage());
                return null;
            });

        AreashintClient.LOGGER.info("CPURender: 寮€濮嬫樉绀哄尯鍩熸爣棰? {}, 鍔ㄧ敾鐘舵€? {}", title, animationState);
    }

    private static int parseHexColor(String hex) {
        try {
            if (hex != null && hex.startsWith("#") && hex.length() == 7) {
                return Integer.parseInt(hex.substring(1), 16);
            }
        } catch (Exception ignored) {}
        return 0xFFFFFF;
    }
    
    /**
     * 鍔ㄧ敾鐘舵€佹灇涓?
     */
    private enum AnimationState {
        NONE, // 鏃犲姩鐢?
        IN,   // 杩涘叆鍔ㄧ敾
        STAY, // 鍋滅暀
        OUT   // 閫€鍑哄姩鐢?
    }
    
    /**
     * 缂撳叆涓夋鏂圭紦鍔ㄥ嚱鏁?
     * @param x 杩涘害 (0.0 - 1.0)
     * @return 缂撳姩鍊?
     */
    private float easeInCubic(float x) {
        return x * x * x;
    }
    
    /**
     * 缂撳嚭涓夋鏂圭紦鍔ㄥ嚱鏁?
     * @param x 杩涘害 (0.0 - 1.0)
     * @return 缂撳姩鍊?
     */
    private float easeOutCubic(float x) {
        return 1 - (float) Math.pow(1 - x, 3);
    }
} 
