package areahint.subtitlestyle;

import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * SubtitleStyle浜や簰寮忕鐞嗗櫒
 * 璐熻矗瀛楀箷鏍峰紡璁剧疆鐨勪氦浜掓祦绋?
 */
public class SubtitleStyleManager {

    /**
     * SubtitleStyle鐘舵€佹灇涓?
     */
    public enum SubtitleStyleState {
        IDLE,           // 绌洪棽鐘舵€?
        SELECTING_STYLE // 閫夋嫨鏍峰紡鐘舵€?
    }

    // 鍗曚緥瀹炰緥
    private static SubtitleStyleManager instance;

    // 褰撳墠鐘舵€?
    private SubtitleStyleState currentState = SubtitleStyleState.IDLE;

    // 绉佹湁鏋勯€犲嚱鏁帮紙鍗曚緥妯″紡锛?
    private SubtitleStyleManager() {}

    /**
     * 鑾峰彇鍗曚緥瀹炰緥
     */
    public static SubtitleStyleManager getInstance() {
        if (instance == null) {
            instance = new SubtitleStyleManager();
        }
        return instance;
    }

    /**
     * 鍚姩SubtitleStyle浜や簰娴佺▼
     */
    public void startSubtitleStyleSelection() {
        if (currentState != SubtitleStyleState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_6")), false);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 璁剧疆鐘舵€佸苟鏄剧ずUI
            currentState = SubtitleStyleState.SELECTING_STYLE;

            // 鑾峰彇褰撳墠鏍峰紡
            String currentStyle = ClientConfig.getSubtitleStyle();

            // 鏄剧ず閫夋嫨鐣岄潰
            SubtitleStyleUI.showStyleSelectionScreen(currentStyle);
        }
    }

    /**
     * 澶勭悊鏍峰紡閫夋嫨
     * @param style 閫夋嫨鐨勬牱寮?
     */
    public void handleStyleSelection(String style) {
        if (currentState != SubtitleStyleState.SELECTING_STYLE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 楠岃瘉鏍峰紡鏈夋晥鎬?
        if (!isValidStyle(style)) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_8") + style), false);
            return;
        }

        // 淇濆瓨鏍峰紡璁剧疆
        ClientConfig.setSubtitleStyle(style);

        // 鏄剧ず鎴愬姛娑堟伅
        String styleDisplay = getStyleDisplayName(style);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_47") + styleDisplay), false);

        // 鎵цreload
        AreashintClient.reload();
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_51")), false);

        // 閲嶇疆鐘舵€?
        resetState();
    }

    /**
     * 鍙栨秷SubtitleStyle娴佺▼
     */
    public void cancelSubtitleStyle() {
        MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.cancel_5")), false);
        resetState();
    }

    /**
     * 閲嶇疆鐘舵€?
     */
    private void resetState() {
        currentState = SubtitleStyleState.IDLE;
    }

    /**
     * 楠岃瘉鏍峰紡鏄惁鏈夋晥
     */
    private boolean isValidStyle(String style) {
        return style.equals("full") || style.equals("simple") || style.equals("mixed");
    }

    /**
     * 鑾峰彇鏍峰紡鐨勬樉绀哄悕绉?
     */
    private String getStyleDisplayName(String style) {
        switch (style) {
            case "full":
                return I18nManager.translate("message.message.general_106");
            case "simple":
                return I18nManager.translate("message.message.general_215");
            case "mixed":
                return I18nManager.translate("message.message.general_209");
            default:
                return style;
        }
    }

    // Getters
    public SubtitleStyleState getCurrentState() {
        return currentState;
    }
}
