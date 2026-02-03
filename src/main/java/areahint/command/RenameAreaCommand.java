package areahint.command;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 域名重命名命令处理类
 * 实现 /areahint rename 指令功能（交互式流程）
 */
public class RenameAreaCommand {

    /**
     * 执行rename指令（启动交互式流程）
     * @param context 命令上下文
     * @return 执行结果
     */
    public static int executeRename(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        try {
            if (source.getEntity() instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
                sendRenameableAreaList(player);
            } else {
                source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            }
        } catch (Exception e) {
            Areashint.LOGGER.error("执行rename命令时发生错误: " + e.getMessage());
            source.sendMessage(Text.of("§c执行命令时发生错误，请稍后重试"));
        }

        return 1;
    }

    /**
     * 处理域名选择命令
     * @param context 命令上下文
     * @param areaName 域名名称
     * @return 执行结果
     */
    public static int executeRenameSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();

        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }

        try {
            // 发送命令到客户端
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:rename_select:" + areaName);
            return 1;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择域名时发生错误: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 处理确认命令
     * @param context 命令上下文
     * @return 执行结果
     */
    public static int executeRenameConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }

        try {
            // 发送命令到客户端
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:rename_confirm");
            return 1;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c确认重命名时发生错误: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 处理取消命令
     * @param context 命令上下文
     * @return 执行结果
     */
    public static int executeRenameCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }

        try {
            // 发送命令到客户端
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:rename_cancel");
            return 1;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c取消重命名时发生错误: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 发送可重命名域名列表到客户端
     * @param player 玩家
     */
    private static void sendRenameableAreaList(ServerPlayerEntity player) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);
            RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
            String dimensionId = dimensionType.getValue().toString();

            // 获取维度文件名
            String dimensionPath = dimensionType.getValue().getPath();
            String fileName = Packets.getFileNameForDimension(
                Packets.convertDimensionPathToType(dimensionPath));

            if (fileName == null) {
                sendRenameResponse(player, false, "无法确定当前维度文件");
                return;
            }

            // 获取域名文件路径
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("rename_list");
                buf.writeString(dimensionId);
                buf.writeInt(0); // 域名数量
                ServerPlayNetworking.send(player, Packets.S2C_RENAME_RESPONSE, buf);
                return;
            }

            // 读取域名数据
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            List<AreaData> editableAreas = new ArrayList<>();

            // 筛选可编辑的域名
            for (AreaData area : areas) {
                if (canRenameArea(area, playerName, isAdmin)) {
                    editableAreas.add(area);
                }
            }

            // 发送到客户端
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("rename_list");
            buf.writeString(dimensionId);
            buf.writeInt(editableAreas.size());

            for (AreaData area : editableAreas) {
                buf.writeString(area.getName());
                buf.writeString(area.getSignature() != null ? area.getSignature() : "未知创建者");
            }

            ServerPlayNetworking.send(player, Packets.S2C_RENAME_RESPONSE, buf);

        } catch (Exception e) {
            Areashint.LOGGER.error("发送域名列表时出错: " + e.getMessage());
            sendRenameResponse(player, false, "读取域名数据时发生错误");
        }
    }

    /**
     * 处理域名重命名请求
     * @param player 玩家
     * @param oldName 原域名
     * @param newName 新域名
     * @param newSurfaceName 新联合域名（可为null或空字符串）
     * @param dimension 维度ID
     */
    public static void handleRenameRequest(ServerPlayerEntity player, String oldName, String newName,
                                          String newSurfaceName, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);

            // 验证新域名格式
            if (newName == null || newName.trim().isEmpty()) {
                sendRenameResponse(player, false, "新域名不能为空");
                return;
            }

            newName = newName.trim();

            // 处理空字符串的联合域名
            if (newSurfaceName != null && newSurfaceName.trim().isEmpty()) {
                newSurfaceName = null;
            }

            // 获取维度文件名
            String dimensionPath = dimension.substring(dimension.lastIndexOf(":") + 1);
            String fileName = Packets.getFileNameForDimension(
                Packets.convertDimensionPathToType(dimensionPath));

            if (fileName == null) {
                sendRenameResponse(player, false, "无法确定维度文件");
                return;
            }

            // 获取域名文件
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                sendRenameResponse(player, false, "维度文件不存在");
                return;
            }

            // 读取域名数据
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            AreaData targetArea = null;
            boolean newNameExists = false;

            // 查找目标域名和检查新名称是否已存在
            for (AreaData area : areas) {
                if (area.getName().equals(oldName)) {
                    targetArea = area;
                }
                if (area.getName().equals(newName)) {
                    newNameExists = true;
                }
            }

            // 验证
            if (targetArea == null) {
                sendRenameResponse(player, false, "未找到域名: " + oldName);
                return;
            }

            if (newNameExists) {
                sendRenameResponse(player, false, "域名 \"" + newName + "\" 已存在，请选择其他名称");
                return;
            }

            if (!canRenameArea(targetArea, playerName, isAdmin)) {
                sendRenameResponse(player, false, "您没有权限重命名域名 \"" + oldName + "\"");
                return;
            }

            // 执行重命名
            targetArea.setName(newName);

            // 更新联合域名
            if (newSurfaceName != null) {
                targetArea.setSurfacename(newSurfaceName);
            }

            // 更新所有引用该域名的子域名的 base-name
            for (AreaData area : areas) {
                if (oldName.equals(area.getBaseName())) {
                    area.setBaseName(newName);
                }
            }

            // 保存文件
            if (FileManager.writeAreaData(areaFile, areas)) {
                String successMessage = "§a域名重命名成功！\n§7原域名: §f" + oldName + "\n§7新域名: §f" + newName;
                if (newSurfaceName != null) {
                    successMessage += "\n§7新联合域名: §f" + newSurfaceName;
                }
                sendRenameResponse(player, true, successMessage);

                // 向所有客户端发送更新后的区域数据
                ServerNetworking.sendAllAreaDataToAll();

                Areashint.LOGGER.info("玩家 " + playerName + " 将域名 \"" + oldName + "\" 重命名为 \"" + newName + "\"");
            } else {
                sendRenameResponse(player, false, "保存域名数据时失败");
            }

        } catch (Exception e) {
            Areashint.LOGGER.error("处理重命名请求时发生错误: " + e.getMessage());
            sendRenameResponse(player, false, "处理请求时发生错误: " + e.getMessage());
        }
    }

    /**
     * 检查是否可以重命名域名
     * @param area 域名数据
     * @param playerName 玩家名称
     * @param isAdmin 是否为管理员
     * @return 是否可以重命名
     */
    private static boolean canRenameArea(AreaData area, String playerName, boolean isAdmin) {
        // 管理员可以重命名所有域名
        if (isAdmin) {
            return true;
        }

        // 普通玩家只能重命名自己创建的域名（signature匹配）
        return area.getSignature() != null && area.getSignature().equals(playerName);
    }

    /**
     * 发送重命名响应到客户端
     * @param player 玩家
     * @param success 是否成功
     * @param message 消息
     */
    private static void sendRenameResponse(ServerPlayerEntity player, boolean success, String message) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("rename_response");
        buf.writeBoolean(success);
        buf.writeString(message);
        ServerPlayNetworking.send(player, Packets.S2C_RENAME_RESPONSE, buf);
    }

    /**
     * 注册服务端网络接收器
     */
    public static void registerServerReceivers() {
        // 注册重命名请求接收器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_RENAME_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String oldName = buf.readString();
                    String newName = buf.readString();
                    String newSurfaceName = buf.readString();
                    String dimension = buf.readString();

                    server.execute(() -> {
                        handleRenameRequest(player, oldName, newName, newSurfaceName, dimension);
                    });

                } catch (Exception e) {
                    Areashint.LOGGER.error("处理重命名请求时发生错误", e);
                    sendRenameResponse(player, false, "服务端处理请求时发生错误: " + e.getMessage());
                }
            });
    }
}
