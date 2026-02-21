package areahint.command;

import areahint.Areashint;
import areahint.dimensional.DimensionalNameManager;
import areahint.network.DimensionalNameNetworking;
import areahint.network.ServerNetworking;
import areahint.util.ColorUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 维度域名命令处理器
 * 处理 /areahint dimensionalityname 和 /areahint dimensionalitycolor 命令
 */
public class DimensionalNameCommands {

    // ===== dimensionalityname 交互式命令 =====

    public static int executeStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        // 确保所有服务器维度都已注册
        syncServerDimensions(source.getServer());
        // 先将最新维度列表发送给该玩家
        DimensionalNameNetworking.sendDimensionalNamesToClient(source.getPlayer());
        sendClientCommand(source, "areahint:dimname_start");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSelect(CommandContext<ServerCommandSource> context, String dimensionId) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        // 去除引号
        if (dimensionId.startsWith("\"") && dimensionId.endsWith("\"") && dimensionId.length() > 1) {
            dimensionId = dimensionId.substring(1, dimensionId.length() - 1);
        }
        sendClientCommand(source, "areahint:dimname_select:" + dimensionId);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeName(CommandContext<ServerCommandSource> context, String newName) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }

        if (newName == null || newName.trim().isEmpty()) {
            source.sendError(Text.literal("维度名称不能为空"));
            return 0;
        }
        final String finalNewName = newName.trim();
        if (finalNewName.length() > 50) {
            source.sendError(Text.literal("维度名称过长（最多50个字符）"));
            return 0;
        }

        sendClientCommand(source, "areahint:dimname_name:" + finalNewName);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimname_confirm");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimname_cancel");
        return Command.SINGLE_SUCCESS;
    }

    // ===== dimensionalitycolor 交互式命令 =====

    public static int executeColorStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        syncServerDimensions(source.getServer());
        DimensionalNameNetworking.sendDimensionalNamesToClient(source.getPlayer());
        sendClientCommand(source, "areahint:dimcolor_start");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorSelect(CommandContext<ServerCommandSource> context, String dimensionId) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        if (dimensionId.startsWith("\"") && dimensionId.endsWith("\"") && dimensionId.length() > 1) {
            dimensionId = dimensionId.substring(1, dimensionId.length() - 1);
        }
        sendClientCommand(source, "areahint:dimcolor_select:" + dimensionId);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorColor(CommandContext<ServerCommandSource> context, String colorValue) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimcolor_color:" + colorValue);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimcolor_confirm");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimcolor_cancel");
        return Command.SINGLE_SUCCESS;
    }

    // ===== 服务端处理方法（由客户端Manager调用） =====

    /**
     * 服务端处理维度域名更改
     */
    public static void handleDimNameChange(ServerCommandSource source, String dimensionId, String newName) {
        try {
            String oldName = DimensionalNameManager.getDimensionalName(dimensionId);
            DimensionalNameManager.setDimensionalName(dimensionId, newName);

            if (DimensionalNameManager.saveDimensionalNames()) {
                source.sendFeedback(() -> Text.literal("§a维度域名已更新！"), false);
                source.sendFeedback(() -> Text.literal("§e维度: §b" + dimensionId), false);
                source.sendFeedback(() -> Text.literal("§e旧名称: §7" + oldName), false);
                source.sendFeedback(() -> Text.literal("§e新名称: §a" + newName), false);

                // 广播到所有客户端
                DimensionalNameNetworking.sendDimensionalNamesToAllClients(source.getServer());
                // 通知客户端reload
                ServerNetworking.sendCommandToAllClients(source.getServer(), "areahint:reload");

                Areashint.LOGGER.info("管理员 {} 将维度 {} 的名称从 '{}' 更改为 '{}'",
                    source.getName(), dimensionId, oldName, newName);
            } else {
                source.sendError(Text.literal("保存维度域名配置失败"));
            }
        } catch (Exception e) {
            source.sendError(Text.literal("设置维度名称时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("设置维度名称失败", e);
        }
    }

    /**
     * 服务端处理维度域名颜色更改
     */
    public static void handleDimColorChange(ServerCommandSource source, String dimensionId, String newColor) {
        try {
            String oldColor = DimensionalNameManager.getDimensionalColor(dimensionId);
            String oldColorDisplay = oldColor != null ? oldColor : "#FFFFFF (默认白色)";
            DimensionalNameManager.setDimensionalColor(dimensionId, newColor);

            if (DimensionalNameManager.saveDimensionalNames()) {
                source.sendFeedback(() -> Text.literal("§a维度域名颜色已更新！"), false);
                source.sendFeedback(() -> Text.literal("§e维度: §b" + dimensionId), false);
                source.sendFeedback(() -> Text.literal("§e原颜色: §7" + oldColorDisplay), false);
                source.sendFeedback(() -> Text.literal("§e新颜色: §a" + newColor), false);

                DimensionalNameNetworking.sendDimensionalNamesToAllClients(source.getServer());
                ServerNetworking.sendCommandToAllClients(source.getServer(), "areahint:reload");

                Areashint.LOGGER.info("管理员 {} 将维度 {} 的颜色从 '{}' 更改为 '{}'",
                    source.getName(), dimensionId, oldColorDisplay, newColor);
            } else {
                source.sendError(Text.literal("保存维度域名配置失败"));
            }
        } catch (Exception e) {
            source.sendError(Text.literal("设置维度颜色时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("设置维度颜色失败", e);
        }
    }

    /**
     * 获取服务器所有维度ID
     */
    public static Set<String> getServerDimensions(ServerCommandSource source) {
        Set<String> dims = source.getServer()
            .getWorldRegistryKeys()
            .stream()
            .map(key -> key.getValue().toString())
            .collect(Collectors.toSet());
        dims.add("minecraft:overworld");
        dims.add("minecraft:the_nether");
        dims.add("minecraft:the_end");
        return dims;
    }

    /**
     * 将服务器所有维度同步到管理器中（确保未配置的维度也能显示）
     */
    public static void syncServerDimensions(MinecraftServer server) {
        Set<String> serverDims = server.getWorldRegistryKeys()
            .stream()
            .map(key -> key.getValue().toString())
            .collect(Collectors.toSet());
        for (String dimId : serverDims) {
            if (!DimensionalNameManager.hasDimensionalName(dimId)) {
                // 未配置的维度使用维度ID作为默认名称
                DimensionalNameManager.setDimensionalName(dimId, dimId);
            }
        }
        DimensionalNameManager.saveDimensionalNames();
    }

    // ===== 首次维度命名（无权限要求） =====

    public static int executeFirstDimName(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) return 0;

        String dimId = source.getPlayer().getWorld().getRegistryKey().getValue().toString();
        String currentName = DimensionalNameManager.getDimensionalName(dimId);

        // 仅当维度名称等于维度ID时（未被命名）才允许
        if (!currentName.equals(dimId)) {
            source.sendMessage(Text.of("§c该维度已被命名为: " + currentName));
            return 0;
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 50) {
            source.sendError(Text.literal("名称无效（1-50个字符）"));
            return 0;
        }

        handleDimNameChange(source, dimId, trimmed);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeFirstDimNameSkip(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) return 0;

        String dimId = source.getPlayer().getWorld().getRegistryKey().getValue().toString();
        String currentName = DimensionalNameManager.getDimensionalName(dimId);

        if (currentName.equals(dimId)) {
            // 使用维度路径作为默认名称（如 overworld, the_nether）
            String defaultName = source.getPlayer().getWorld().getRegistryKey().getValue().getPath();
            handleDimNameChange(source, dimId, defaultName);
            source.sendMessage(Text.of("§7已使用默认名称: " + defaultName));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void sendClientCommand(ServerCommandSource source, String command) {
        try {
            if (source.getPlayer() != null) {
                ServerNetworking.sendCommandToClient(source.getPlayer(), command);
            }
        } catch (Exception e) {
            Areashint.LOGGER.error("发送客户端命令时出错", e);
        }
    }
}
