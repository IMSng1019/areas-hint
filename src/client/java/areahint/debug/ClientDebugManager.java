package areahint.debug;

import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 客户端调试管理器
 * 用于客户端调试功能和向玩家发送调试信息
 */
public class ClientDebugManager {
    // 是否启用调试
    private static boolean debugEnabled = false;
    
    /**
     * 启用调试模式
     */
    public static void enableDebug() {
        debugEnabled = true;
        AreashintClient.LOGGER.info("客户端调试模式已启用");
        sendDebugMessage("已启用区域提示调试模式，您将收到实时调试信息", Formatting.GREEN);
    }
    
    /**
     * 禁用调试模式
     */
    public static void disableDebug() {
        debugEnabled = false;
        AreashintClient.LOGGER.info("客户端调试模式已禁用");
        sendDebugMessage("已禁用区域提示调试模式", Formatting.YELLOW);
    }
    
    /**
     * 检查是否启用了调试
     * @return 是否启用了调试
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    /**
     * 发送调试信息
     * @param category 调试类别
     * @param message 调试信息
     */
    public static void sendDebugInfo(DebugCategory category, String message) {
        if (!debugEnabled) {
            return; // 如果没有启用调试，直接返回
        }
        
        // 记录到客户端日志
        AreashintClient.LOGGER.debug("[{}] {}", category.name(), message);
        
        // 发送到屏幕
        String formattedMessage = String.format("[%s] %s", category.name(), message);
        sendDebugMessage(formattedMessage, category.getFormatting());
    }
    
    /**
     * 向玩家发送带格式的调试消息
     * @param message 消息
     * @param formatting 格式
     */
    private static void sendDebugMessage(String message, Formatting formatting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Text text = Text.literal("[区域提示调试] ").formatted(Formatting.GOLD)
                    .append(Text.literal(message).formatted(formatting));
            client.player.sendMessage(text, false);
        }
    }
    
    /**
     * 调试类别枚举
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