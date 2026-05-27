package areahint.subtitle;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.file.JsonHelper;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.permission.PermissionNodes;
import areahint.permission.PermissionService;
import areahint.util.AreaPermissionUtil;
import areahint.util.ColorUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 副字幕服务端处理器。
 * <p>
 * 这里集中处理 addsubtitle、deletesubtitle、replacesubtitlecolor 三个流程的服务端部分：
 * 1. 向客户端发送可操作域名列表；
 * 2. 接收客户端确认后的修改请求；
 * 3. 校验权限并写回对应维度的域名 JSON 文件。
 */
public class SubtitleCommand {
    private static final String ACTION_ADD_INTERACTIVE = "addsubtitle_interactive";
    private static final String ACTION_DELETE_INTERACTIVE = "deletesubtitle_interactive";
    private static final String ACTION_COLOR_INTERACTIVE = "replacesubtitlecolor_interactive";

    private static final String MUTATION_SET_SUBTITLE = "set_subtitle";
    private static final String MUTATION_DELETE_SUBTITLE = "delete_subtitle";
    private static final String MUTATION_SET_COLOR = "set_subtitle_color";

    /**
     * 注册客户端提交到服务端的副字幕修改请求。
     */
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_SUBTITLE_MUTATION,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String mutation = buf.readString();
                    String areaName = buf.readString();
                    String value = buf.readString();
                    String dimension = buf.readString();

                    server.execute(() -> handleSubtitleMutation(player, mutation, areaName, value, dimension));
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理副字幕修改请求时发生错误", e);
                    sendResponse(player, false, "处理副字幕修改请求时发生错误: " + e.getMessage());
                }
            });
    }

    /**
     * 启动添加/替换副字幕流程。
     */
    public static int executeAddSubtitle(CommandContext<ServerCommandSource> context) {
        return sendInteractiveList(context, ACTION_ADD_INTERACTIVE, SubtitlePermissionMode.MODIFY_BY_REFERENCE, false);
    }

    /**
     * 启动删除副字幕流程。
     */
    public static int executeDeleteSubtitle(CommandContext<ServerCommandSource> context) {
        return sendInteractiveList(context, ACTION_DELETE_INTERACTIVE, SubtitlePermissionMode.DELETE_LIKE, true);
    }

    /**
     * 启动替换副字幕颜色流程。
     */
    public static int executeReplaceSubtitleColor(CommandContext<ServerCommandSource> context) {
        return sendInteractiveList(context, ACTION_COLOR_INTERACTIVE, SubtitlePermissionMode.COLOR_BY_REFERENCE, true);
    }

    private static int sendInteractiveList(CommandContext<ServerCommandSource> context, String action,
                                           SubtitlePermissionMode permissionMode, boolean onlyWithSubtitle) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.translatable("command.error.general_9"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        String dimensionType = convertDimensionIdToType(player.getWorld().getRegistryKey().getValue().toString());
        String fileName = Packets.getFileNameForDimension(dimensionType);
        if (fileName == null) {
            source.sendMessage(Text.literal("§c无法识别当前维度，无法启动副字幕流程"));
            return 0;
        }

        Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
        List<AreaData> allAreas = FileManager.readAreaData(areaFile);
        List<AreaData> editableAreas = new ArrayList<>();

        for (AreaData area : allAreas) {
            if (onlyWithSubtitle && !area.hasSubtitle()) {
                continue;
            }
            if (canOperate(player, area, allAreas, permissionMode)) {
                editableAreas.add(area);
            }
        }

        if (editableAreas.isEmpty()) {
            source.sendMessage(Text.literal("§c当前维度没有可操作的副字幕域名"));
            return 0;
        }

        sendAreaList(player, action, dimensionType, editableAreas);
        source.sendMessage(Text.literal("§a已发送副字幕交互界面"));
        return Command.SINGLE_SUCCESS;
    }

    private static void handleSubtitleMutation(ServerPlayerEntity player, String mutation, String areaName,
                                               String value, String dimension) {
        try {
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                sendResponse(player, false, "无效维度: " + dimension);
                return;
            }

            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            AreaData targetArea = AreaPermissionUtil.findByName(areas, areaName);
            if (targetArea == null) {
                sendResponse(player, false, "未找到域名: " + areaName);
                return;
            }

            if (MUTATION_SET_SUBTITLE.equals(mutation)) {
                handleSetSubtitle(player, targetArea, areas, value);
            } else if (MUTATION_DELETE_SUBTITLE.equals(mutation)) {
                handleDeleteSubtitle(player, targetArea, areas);
            } else if (MUTATION_SET_COLOR.equals(mutation)) {
                handleSetSubtitleColor(player, targetArea, areas, value);
            } else {
                sendResponse(player, false, "未知副字幕操作: " + mutation);
                return;
            }

            if (FileManager.writeAreaData(areaFile, areas)) {
                ServerNetworking.sendAllAreaDataToAll();
                sendResponse(player, true, "副字幕已更新: " + targetArea.getName());
            } else {
                sendResponse(player, false, "保存域名文件失败");
            }
        } catch (Exception e) {
            Areashint.LOGGER.error("写入副字幕数据时发生错误", e);
            sendResponse(player, false, "写入副字幕数据时发生错误: " + e.getMessage());
        }
    }

    private static void handleSetSubtitle(ServerPlayerEntity player, AreaData targetArea,
                                          List<AreaData> allAreas, String rawSubtitle) {
        if (!canOperate(player, targetArea, allAreas, SubtitlePermissionMode.MODIFY_BY_REFERENCE)) {
            throw new IllegalStateException("你没有修改该域名副字幕的权限");
        }

        String subtitle = normalizeSubtitle(rawSubtitle);
        if (subtitle == null) {
            throw new IllegalStateException("副字幕不能为空");
        }

        targetArea.setSubtitle(subtitle);
        // 添加副字幕时补齐 subtitlecolor 字段。已有颜色会保留，没有颜色则使用白色。
        targetArea.setSubtitleColor(targetArea.getSubtitleColor());
    }

    private static void handleDeleteSubtitle(ServerPlayerEntity player, AreaData targetArea, List<AreaData> allAreas) {
        if (!canOperate(player, targetArea, allAreas, SubtitlePermissionMode.DELETE_LIKE)) {
            throw new IllegalStateException("你没有删除该域名副字幕的权限");
        }

        if (!targetArea.hasSubtitle()) {
            throw new IllegalStateException("该域名没有副字幕");
        }

        targetArea.setSubtitle(null);
        targetArea.setSubtitleColor(null);
    }

    private static void handleSetSubtitleColor(ServerPlayerEntity player, AreaData targetArea,
                                               List<AreaData> allAreas, String rawColor) {
        if (!canOperate(player, targetArea, allAreas, SubtitlePermissionMode.COLOR_BY_REFERENCE)) {
            throw new IllegalStateException("你没有修改该域名副字幕颜色的权限");
        }

        if (!targetArea.hasSubtitle()) {
            throw new IllegalStateException("该域名没有副字幕，无法修改副字幕颜色");
        }

        String normalizedColor = ColorUtil.normalizeColor(rawColor);
        if (!ColorUtil.isValidColor(normalizedColor)) {
            throw new IllegalStateException("无效副字幕颜色: " + rawColor);
        }

        targetArea.setSubtitleColor(normalizedColor);
    }

    private static boolean canOperate(ServerPlayerEntity player, AreaData area, List<AreaData> allAreas,
                                      SubtitlePermissionMode permissionMode) {
        String playerName = player.getName().getString();
        switch (permissionMode) {
            case DELETE_LIKE:
                // deletesubtitle 只复用 delete 的核心权限：管理员、权限节点或该域名签名者。
                return PermissionService.hasNodeOr(player, PermissionNodes.DELETE_SUBTITLE,
                    () -> player.hasPermissionLevel(2) || AreaPermissionUtil.isSignedBy(area, playerName));
            case COLOR_BY_REFERENCE:
                return PermissionService.hasNodeOr(player, PermissionNodes.REPLACE_SUBTITLE_COLOR,
                    () -> AreaPermissionUtil.canModifyArea(player, area, allAreas));
            case MODIFY_BY_REFERENCE:
            default:
                return PermissionService.hasNodeOr(player, PermissionNodes.ADD_SUBTITLE,
                    () -> AreaPermissionUtil.canModifyArea(player, area, allAreas));
        }
    }

    private static void sendAreaList(ServerPlayerEntity player, String action, String dimension, List<AreaData> areas) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(action);
            buf.writeString(dimension);
            buf.writeInt(areas.size());

            for (AreaData area : areas) {
                buf.writeString(JsonHelper.toJsonSingle(area));
            }

            ServerPlayNetworking.send(player, Packets.S2C_SUBTITLE_RESPONSE, buf);
        } catch (Exception e) {
            Areashint.LOGGER.error("发送副字幕域名列表时发生错误", e);
            sendResponse(player, false, "发送副字幕域名列表时发生错误: " + e.getMessage());
        }
    }

    private static void sendResponse(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("subtitle_response");
            buf.writeBoolean(success);
            buf.writeString(message);
            ServerPlayNetworking.send(player, Packets.S2C_SUBTITLE_RESPONSE, buf);
        } catch (Exception e) {
            Areashint.LOGGER.error("发送副字幕响应时发生错误", e);
        }
    }

    private static String normalizeSubtitle(String subtitle) {
        if (subtitle == null) {
            return null;
        }
        String normalized = subtitle.replace("/n", "\n").replace("\\n", "\n").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String convertDimensionIdToType(String dimension) {
        if (dimension == null) {
            return null;
        }
        if (dimension.contains("overworld")) {
            return Packets.DIMENSION_OVERWORLD;
        } else if (dimension.contains("nether")) {
            return Packets.DIMENSION_NETHER;
        } else if (dimension.contains("end")) {
            return Packets.DIMENSION_END;
        }
        return null;
    }

    private enum SubtitlePermissionMode {
        MODIFY_BY_REFERENCE,
        DELETE_LIKE,
        COLOR_BY_REFERENCE
    }
}
