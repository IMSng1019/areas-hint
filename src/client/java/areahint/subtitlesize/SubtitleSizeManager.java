package areahint.subtitlesize;

import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * SubtitleSize浜や簰寮忕鐞嗗櫒
 * 璐熻矗瀛楀箷澶у皬璁剧疆鐨勪氦浜掓祦绋?
 */
public class SubtitleSizeManager {

    /**
     * SubtitleSize鐘舵€佹灇涓?
     */
    public enum SubtitleSizeState {
        IDLE,           // 绌洪棽鐘舵€?
        SELECTING_SIZE  // 閫夋嫨澶у皬鐘舵€?
    }

    // 鍗曚緥瀹炰緥
    private static SubtitleSizeManager instance;

    // 褰撳墠鐘舵€?
    private SubtitleSizeState currentState = SubtitleSizeState.IDLE;

    // 绉佹湁鏋勯€犲嚱鏁帮紙鍗曚緥妯″紡锛?
    private SubtitleSizeManager() {}

    /**
     * 鑾峰彇鍗曚緥瀹炰緥
     */
    public static SubtitleSizeManager getInstance() {
        if (instance == null) {
            instance = new SubtitleSizeManager();
        }
        return instance;
    }

    /**
     * 鍚姩SubtitleSize浜や簰娴佺▼
     */
    public void startSubtitleSizeSelection() {
        if (currentState != SubtitleSizeState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_5")), false);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 璁剧疆鐘舵€佸苟鏄剧ずUI
            currentState = SubtitleSizeState.SELECTING_SIZE;

            // 鑾峰彇褰撳墠澶у皬
            String currentSize = ClientConfig.getSubtitleSize();

            // 鏄剧ず閫夋嫨鐣岄潰
            SubtitleSizeUI.showSizeSelectionScreen(currentSize);
        }
    }

    /**
     * 澶勭悊澶у皬閫夋嫨
     * @param size 閫夋嫨鐨勫ぇ灏?
     */
    public void handleSizeSelection(String size) {
        if (currentState != SubtitleSizeState.SELECTING_SIZE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 楠岃瘉澶у皬鏈夋晥鎬?
        if (!isValidSize(size)) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_7") + size), false);
            return;
        }

        // 淇濆瓨澶у皬璁剧疆
        ClientConfig.setSubtitleSize(size);

        // 鏄剧ず鎴愬姛娑堟伅
        String sizeDisplay = getSizeDisplayName(size);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_46") + sizeDisplay), false);

        // 鎵цreload
        AreashintClient.reload();
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_51")), false);

        // 閲嶇疆鐘舵€?
        resetState();
    }

    /**
     * 鍙栨秷SubtitleSize娴佺▼
     */
    public void cancelSubtitleSize() {
        MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.cancel_4")), false);
        resetState();
    }

    /**
     * 閲嶇疆鐘舵€?
     */
    private void resetState() {
        currentState = SubtitleSizeState.IDLE;
    }

    /**
     * 楠岃瘉澶у皬鏄惁鏈夋晥
     */
    private boolean isValidSize(String size) {
        return size.equals("extra_large") || size.equals("large") || size.equals("medium_large") ||
               size.equals("medium") || size.equals("medium_small") || size.equals("small") ||
               size.equals("extra_small");
    }

    /**
     * 鑾峰彇澶у皬鐨勬樉绀哄悕绉?
     */
    private String getSizeDisplayName(String size) {
        switch (size) {
            case "extra_large":
                return I18nManager.translate("message.message.general_193");
            case "large":
                return I18nManager.translate("message.message.general_104");
            case "medium_large":
                return I18nManager.translate("message.message.general_225");
            case "medium":
                return I18nManager.translate("message.message.general_58");
            case "medium_small":
                return I18nManager.translate("message.message.general_226");
            case "small":
                return I18nManager.translate("message.message.general_111");
            case "extra_small":
                return I18nManager.translate("message.message.general_194");
            default:
                return size;
        }
    }

    // Getters
    public SubtitleSizeState getCurrentState() {
        return currentState;
    }
}
