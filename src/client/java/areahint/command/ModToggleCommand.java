package areahint.command;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
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
            client.player.sendMessage(Text.literal("§a[AreaHint] 模组已启用！域名显示和检测功能已开启。"), false);
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
            client.player.sendMessage(Text.literal("§c[AreaHint] 模组已禁用！域名将不再显示，但仍会接收服务器数据。"), false);
        }
        
        AreashintClient.LOGGER.info("模组已通过命令禁用");
    }
    
    /**
     * 显示当前模组状态
     * @param client Minecraft客户端实例
     */
    private static void showCurrentStatus(MinecraftClient client) {
        boolean enabled = ClientConfig.isEnabled();
        String status = enabled ? "§a启用" : "§c禁用";
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal(String.format("§e[AreaHint] 模组当前状态：%s", status)), false);
        }
    }
    
    /**
     * 显示使用方法
     * @param client Minecraft客户端实例
     */
    private static void showUsage(MinecraftClient client) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§e[AreaHint] 用法："), false);
            client.player.sendMessage(Text.literal("§7/areahint on  §f- 启用模组"), false);
            client.player.sendMessage(Text.literal("§7/areahint off §f- 禁用模组"), false);
        }
    }
} 