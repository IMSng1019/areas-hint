package areahint.subtitlesize;

import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * SubtitleSize交互式管理器
 * 负责字幕大小设置的交互流程
 */
public class SubtitleSizeManager {

    /**
     * SubtitleSize状态枚举
     */
    public enum SubtitleSizeState {
        IDLE,           // 空闲状态
        SELECTING_SIZE  // 选择大小状态
    }

    // 单例实例
    private static SubtitleSizeManager instance;

    // 当前状态
    private SubtitleSizeState currentState = SubtitleSizeState.IDLE;

    // 私有构造函数（单例模式）
    private SubtitleSizeManager() {}

    /**
     * 获取单例实例
     */
    public static SubtitleSizeManager getInstance() {
        if (instance == null) {
            instance = new SubtitleSizeManager();
        }
        return instance;
    }

    /**
     * 启动SubtitleSize交互流程
     */
    public void startSubtitleSizeSelection() {
        if (currentState != SubtitleSizeState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(Text.of(I18nManager.translate("message.error.general_5")), false);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 设置状态并显示UI
            currentState = SubtitleSizeState.SELECTING_SIZE;

            // 获取当前大小
            String currentSize = ClientConfig.getSubtitleSize();

            // 显示选择界面
            SubtitleSizeUI.showSizeSelectionScreen(currentSize);
        }
    }

    /**
     * 处理大小选择
     * @param size 选择的大小
     */
    public void handleSizeSelection(String size) {
        if (currentState != SubtitleSizeState.SELECTING_SIZE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 验证大小有效性
        if (!isValidSize(size)) {
            client.player.sendMessage(Text.of(I18nManager.translate("message.error.general_7") + size), false);
            return;
        }

        // 保存大小设置
        ClientConfig.setSubtitleSize(size);

        // 显示成功消息
        String sizeDisplay = getSizeDisplayName(size);
        client.player.sendMessage(Text.of(I18nManager.translate("message.message.general_46") + sizeDisplay), false);

        // 执行reload
        AreashintClient.reload();
        client.player.sendMessage(Text.of(I18nManager.translate("message.message.general_51")), false);

        // 重置状态
        resetState();
    }

    /**
     * 取消SubtitleSize流程
     */
    public void cancelSubtitleSize() {
        MinecraftClient.getInstance().player.sendMessage(Text.of(I18nManager.translate("message.message.cancel_4")), false);
        resetState();
    }

    /**
     * 重置状态
     */
    private void resetState() {
        currentState = SubtitleSizeState.IDLE;
    }

    /**
     * 验证大小是否有效
     */
    private boolean isValidSize(String size) {
        return size.equals("extra_large") || size.equals("large") || size.equals("medium_large") ||
               size.equals("medium") || size.equals("medium_small") || size.equals("small") ||
               size.equals("extra_small");
    }

    /**
     * 获取大小的显示名称
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
