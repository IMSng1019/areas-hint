package areahint.command;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * 模组开关命令处理器
 * 处理客户端的 /areahint on 和 /areahint off 命令
 */
public class ModToggleCommand {
    
    /**
     * 处理模组开关命令
     * @param action 命令参数（"on" 或 "off"）
     */
    public static void handleToggleCommand(String action) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (action == null || action.trim().isEmpty()) {
            // 如果没有参数，显示当前状态
            showCurrentStatus(client);
            return;
        }
        
        String command = action.trim().toLowerCase();
        
        switch (command) {
            case "on":
                enableMod(client);
                break;
            case "off":
                disableMod(client);
                break;
            default:
                showUsage(client);
                break;
        }
    }
    
    /**
     * 启用模组
     * @param client Minecraft客户端实例
     */
    private static void enableMod(MinecraftClient client) {
        ClientConfig.setEnabled(true);
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal(I18nManager.translate("command.button.area")), false);
        }
        
        AreashintClient.LOGGER.info("模组已通过命令启用");
    }
    
    /**
     * 禁用模组
     * @param client Minecraft客户端实例
     */
    private static void disableMod(MinecraftClient client) {
        ClientConfig.setEnabled(false);
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal(I18nManager.translate("command.error.area")), false);
        }
        
        AreashintClient.LOGGER.info("模组已通过命令禁用");
    }
    
    /**
     * 显示当前模组状态
     * @param client Minecraft客户端实例
     */
    private static void showCurrentStatus(MinecraftClient client) {
        boolean enabled = ClientConfig.isEnabled();
        String status = enabled ? I18nManager.translate("command.message.general_12") : I18nManager.translate("command.error.general_11");
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal(String.format(I18nManager.translate("command.button.general"), status)), false);
        }
    }
    
    /**
     * 显示使用方法
     * @param client Minecraft客户端实例
     */
    private static void showUsage(MinecraftClient client) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(I18nManager.translate("command.button.general_2")), false);
            client.player.sendMessage(Text.literal("§7/areahint on  §f- " + I18nManager.translate("command.message.general_26")), false);
            client.player.sendMessage(Text.literal("§7/areahint off §f- " + I18nManager.translate("command.message.general_27")), false);
        }
    }
} 