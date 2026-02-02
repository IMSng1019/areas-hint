package areahint.recolor;

import areahint.AreashintClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Recolor客户端命令处理器
 */
public class RecolorClientCommand {

    /**
     * 注册客户端命令
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("areahint")
            .then(ClientCommandManager.literal("recolor")
                // /areahint recolor select <域名>
                .then(ClientCommandManager.literal("select")
                    .then(ClientCommandManager.argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> executeSelect(context, StringArgumentType.getString(context, "areaName")))))
                // /areahint recolor color <颜色>
                .then(ClientCommandManager.literal("color")
                    .then(ClientCommandManager.argument("colorValue", StringArgumentType.greedyString())
                        .executes(context -> executeColor(context, StringArgumentType.getString(context, "colorValue")))))
                // /areahint recolor confirm
                .then(ClientCommandManager.literal("confirm")
                    .executes(RecolorClientCommand::executeConfirm))
                // /areahint recolor cancel
                .then(ClientCommandManager.literal("cancel")
                    .executes(RecolorClientCommand::executeCancel))));
    }

    /**
     * 执行域名选择
     */
    private static int executeSelect(CommandContext<FabricClientCommandSource> context, String areaName) {
        try {
            // 移除引号（如果存在）
            if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
                areaName = areaName.substring(1, areaName.length() - 1);
            }

            RecolorManager.getInstance().handleAreaSelection(areaName);
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error("执行recolor select时出错", e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§c执行命令时出错: " + e.getMessage()), false);
            }
            return 0;
        }
    }

    /**
     * 执行颜色选择
     */
    private static int executeColor(CommandContext<FabricClientCommandSource> context, String colorValue) {
        try {
            RecolorManager.getInstance().handleColorSelection(colorValue);
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error("执行recolor color时出错", e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§c执行命令时出错: " + e.getMessage()), false);
            }
            return 0;
        }
    }

    /**
     * 执行确认
     */
    private static int executeConfirm(CommandContext<FabricClientCommandSource> context) {
        try {
            RecolorManager.getInstance().confirmChange();
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error("执行recolor confirm时出错", e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§c执行命令时出错: " + e.getMessage()), false);
            }
            return 0;
        }
    }

    /**
     * 执行取消
     */
    private static int executeCancel(CommandContext<FabricClientCommandSource> context) {
        try {
            RecolorManager.getInstance().cancelRecolor();
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error("执行recolor cancel时出错", e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§c执行命令时出错: " + e.getMessage()), false);
            }
            return 0;
        }
    }
}
