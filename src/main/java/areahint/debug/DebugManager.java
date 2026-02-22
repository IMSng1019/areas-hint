package areahint.debug;

import areahint.Areashint;
import areahint.i18n.ServerI18nManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 调试管理器
 * 用于管理调试功能和向玩家发送调试信息
 */
public class DebugManager {
    // 启用调试的玩家UUID集合
    private static final Set<UUID> debugEnabledPlayers = new HashSet<>();
    
    // 是否有任何玩家启用了调试
    private static boolean anyDebugEnabled = false;
    
    /**
     * 为指定玩家启用调试模式
     * @param player 玩家
     * @return 是否成功启用
     */
    public static boolean enableDebug(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        boolean wasEnabled = debugEnabledPlayers.contains(playerUUID);
        
        if (!wasEnabled) {
            debugEnabledPlayers.add(playerUUID);
            anyDebugEnabled = true;
            Areashint.LOGGER.info(ServerI18nManager.translate("debug.message.general"), player.getName().getString());
            sendTranslatableDebugMessage(player, "debug.hint.general", Formatting.GREEN);
        } else {
            sendTranslatableDebugMessage(player, "debug.message.general_5", Formatting.YELLOW);
        }
        
        return !wasEnabled;
    }
    
    /**
     * 为指定玩家禁用调试模式
     * @param player 玩家
     * @return 是否成功禁用
     */
    public static boolean disableDebug(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        boolean wasEnabled = debugEnabledPlayers.remove(playerUUID);
        
        // 更新全局调试状态
        anyDebugEnabled = !debugEnabledPlayers.isEmpty();
        
        if (wasEnabled) {
            Areashint.LOGGER.info(ServerI18nManager.translate("debug.message.general_2"), player.getName().getString());
            sendTranslatableDebugMessage(player, "debug.hint.general_2", Formatting.YELLOW);
        } else {
            sendTranslatableDebugMessage(player, "debug.message.general_6", Formatting.RED);
        }
        
        return wasEnabled;
    }
    
    /**
     * 检查是否有任何玩家启用了调试
     * @return 是否有任何玩家启用了调试
     */
    public static boolean isAnyDebugEnabled() {
        return anyDebugEnabled;
    }
    
    /**
     * 检查指定玩家是否启用了调试
     * @param playerUUID 玩家UUID
     * @return 是否启用了调试
     */
    public static boolean isDebugEnabled(UUID playerUUID) {
        return debugEnabledPlayers.contains(playerUUID);
    }
    
    /**
     * 向启用调试的玩家发送调试信息
     * @param category 调试类别
     * @param message 调试信息
     */
    public static void sendDebugInfo(DebugCategory category, String message) {
        if (!anyDebugEnabled) {
            return; // 如果没有玩家启用调试，直接返回，避免不必要的处理
        }
        
        // 记录到服务端日志
        Areashint.LOGGER.debug("[{}] {}", category.name(), message);
        
        // 遍历所有启用调试的玩家
        for (UUID playerUUID : debugEnabledPlayers) {
            ServerPlayerEntity player = Areashint.getServer().getPlayerManager().getPlayer(playerUUID);
            if (player != null && player.isAlive()) {
                String formattedMessage = String.format("[%s] %s", category.name(), message);
                sendDebugMessage(player, formattedMessage, category.getFormatting());
            }
        }
    }
    
    /**
     * 向玩家发送带格式的调试消息
     * @param player 玩家
     * @param message 消息
     * @param formatting 格式
     */
    private static void sendDebugMessage(ServerPlayerEntity player, String message, Formatting formatting) {
        Text text = Text.translatable("debug.button.general").formatted(Formatting.GOLD)
                .append(Text.literal(message).formatted(formatting));
        player.sendMessage(text, false);
    }

    private static void sendTranslatableDebugMessage(ServerPlayerEntity player, String key, Formatting formatting) {
        Text text = Text.literal(ServerI18nManager.translateForPlayer(player.getUuid(), "debug.button.general")).formatted(Formatting.GOLD)
                .append(Text.literal(ServerI18nManager.translateForPlayer(player.getUuid(), key)).formatted(formatting));
        player.sendMessage(text, false);
    }

    public static void sendTranslatableDebugInfo(DebugCategory category, String key) {
        if (!anyDebugEnabled) return;
        for (UUID playerUUID : debugEnabledPlayers) {
            ServerPlayerEntity player = Areashint.getServer().getPlayerManager().getPlayer(playerUUID);
            if (player != null && player.isAlive()) {
                Text text = Text.literal(ServerI18nManager.translateForPlayer(playerUUID, "debug.button.general")).formatted(Formatting.GOLD)
                        .append(Text.literal("[" + category.name() + "] ").formatted(category.getFormatting()))
                        .append(Text.literal(ServerI18nManager.translateForPlayer(playerUUID, key)).formatted(category.getFormatting()));
                player.sendMessage(text, false);
            }
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