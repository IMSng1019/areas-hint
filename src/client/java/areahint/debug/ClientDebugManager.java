package areahint.debug;

import areahint.AreashintClient;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 瀹㈡埛绔皟璇曠鐞嗗櫒
 * 鐢ㄤ簬瀹㈡埛绔皟璇曞姛鑳藉拰鍚戠帺瀹跺彂閫佽皟璇曚俊鎭?
 */
public class ClientDebugManager {
    // 鏄惁鍚敤璋冭瘯
    private static boolean debugEnabled = false;
    
    /**
     * 鍚敤璋冭瘯妯″紡
     */
    public static void enableDebug() {
        debugEnabled = true;
        AreashintClient.LOGGER.info("瀹㈡埛绔皟璇曟ā寮忓凡鍚敤");
        sendDebugMessage(I18nManager.translate("debug.hint.general"), Formatting.GREEN);
    }
    
    /**
     * 绂佺敤璋冭瘯妯″紡
     */
    public static void disableDebug() {
        debugEnabled = false;
        AreashintClient.LOGGER.info("瀹㈡埛绔皟璇曟ā寮忓凡绂佺敤");
        sendDebugMessage(I18nManager.translate("debug.hint.general_2"), Formatting.YELLOW);
    }
    
    /**
     * 妫€鏌ユ槸鍚﹀惎鐢ㄤ簡璋冭瘯
     * @return 鏄惁鍚敤浜嗚皟璇?
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    /**
     * 鍙戦€佽皟璇曚俊鎭?
     * @param category 璋冭瘯绫诲埆
     * @param message 璋冭瘯淇℃伅
     */
    public static void sendDebugInfo(DebugCategory category, String message) {
        if (!debugEnabled) {
            return; // 濡傛灉娌℃湁鍚敤璋冭瘯锛岀洿鎺ヨ繑鍥?
        }
        
        // 璁板綍鍒板鎴风鏃ュ織
        AreashintClient.LOGGER.debug("[{}] {}", category.name(), message);
        
        // 鍙戦€佸埌灞忓箷
        String formattedMessage = String.format("[%s] %s", category.name(), message);
        sendDebugMessage(formattedMessage, category.getFormatting());
    }
    
    /**
     * 鍚戠帺瀹跺彂閫佸甫鏍煎紡鐨勮皟璇曟秷鎭?
     * @param message 娑堟伅
     * @param formatting 鏍煎紡
     */
    private static void sendDebugMessage(String message, Formatting formatting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Text text = areahint.util.TextCompat.literal(I18nManager.translate("debug.button.general")).formatted(Formatting.GOLD)
                    .append(areahint.util.TextCompat.literal(message).formatted(formatting));
            client.player.sendMessage(text, false);
        }
    }
    
    /**
     * 璋冭瘯绫诲埆鏋氫妇
     */
    public enum DebugCategory {
        AREA_DETECTION(Formatting.AQUA),
        PLAYER_POSITION(Formatting.GREEN),
        CONFIG(Formatting.YELLOW),
        NETWORK(Formatting.LIGHT_PURPLE),
        RENDER(Formatting.BLUE),
        COMMAND(Formatting.WHITE),
        EASY_ADD(Formatting.GOLD),
        GENERAL(Formatting.GRAY);
        
        private final Formatting formatting;
        
        DebugCategory(Formatting formatting) {
            this.formatting = formatting;
        }
        
        public Formatting getFormatting() {
            return formatting;
        }
    }
} 