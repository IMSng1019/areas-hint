package areahint.subtitlestyle;

import areahint.config.ClientConfig;
import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * SubtitleStyle交互式管理器
 * 负责字幕样式设置的交互流程
 */
public class SubtitleStyleManager {

    /**
     * SubtitleStyle状态枚举
     */
    public enum SubtitleStyleState {
        IDLE,           // 空闲状态
        SELECTING_STYLE // 选择样式状态
    }

    // 单例实例
    private static SubtitleStyleManager instance;

    // 当前状态
    private SubtitleStyleState currentState = SubtitleStyleState.IDLE;

    // 私有构造函数（单例模式）
    private SubtitleStyleManager() {}

    /**
     * 获取单例实例
     */
    public static SubtitleStyleManager getInstance() {
        if (instance == null) {
            instance = new SubtitleStyleManager();
        }
        return instance;
    }

    /**
     * 启动SubtitleStyle交互流程
     */
    public void startSubtitleStyleSelection() {
        if (currentState != SubtitleStyleState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("§c当前已有SubtitleStyle流程在进行中"), false);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 设置状态并显示UI
            currentState = SubtitleStyleState.SELECTING_STYLE;

            // 获取当前样式
            String currentStyle = ClientConfig.getSubtitleStyle();

            // 显示选择界面
            SubtitleStyleUI.showStyleSelectionScreen(currentStyle);
        }
    }

    /**
     * 处理样式选择
     * @param style 选择的样式
     */
    public void handleStyleSelection(String style) {
        if (currentState != SubtitleStyleState.SELECTING_STYLE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 验证样式有效性
        if (!isValidStyle(style)) {
            client.player.sendMessage(Text.of("§c无效的样式选项: " + style), false);
            return;
        }

        // 保存样式设置
        ClientConfig.setSubtitleStyle(style);

        // 显示成功消息
        String styleDisplay = getStyleDisplayName(style);
        client.player.sendMessage(Text.of("§a字幕样式已设置为: §6" + styleDisplay), false);

        // 执行reload
        AreashintClient.reload();
        client.player.sendMessage(Text.of("§a配置已重新加载"), false);

        // 重置状态
        resetState();
    }

    /**
     * 取消SubtitleStyle流程
     */
    public void cancelSubtitleStyle() {
        MinecraftClient.getInstance().player.sendMessage(Text.of("§7SubtitleStyle流程已取消"), false);
        resetState();
    }

    /**
     * 重置状态
     */
    private void resetState() {
        currentState = SubtitleStyleState.IDLE;
    }

    /**
     * 验证样式是否有效
     */
    private boolean isValidStyle(String style) {
        return style.equals("full") || style.equals("simple") || style.equals("mixed");
    }

    /**
     * 获取样式的显示名称
     */
    private String getStyleDisplayName(String style) {
        switch (style) {
            case "full":
                return "完整样式 (Full)";
            case "simple":
                return "简洁样式 (Simple)";
            case "mixed":
                return "混合样式 (Mixed)";
            default:
                return style;
        }
    }

    // Getters
    public SubtitleStyleState getCurrentState() {
        return currentState;
    }
}
