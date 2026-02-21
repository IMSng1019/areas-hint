package areahint.network;

import areahint.Areashint;
import areahint.file.FileManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;

/**
 * 服务端网络处理类
 * 处理服务端与客户端之间的通信
 */
public class ServerNetworking {
    /**
     * 初始化网络处理
     */
    public static void init() {
        // 注册网络通道和处理器
        Areashint.LOGGER.info("初始化服务端网络处理");
        
        // 注册网络请求处理器
        registerNetworkHandlers();
        
        // 注册玩家连接事件，当玩家加入服务器时发送区域数据和维度域名数据
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Areashint.LOGGER.info("玩家 " + player.getName().getString() + " 已连接，发送区域数据和维度域名数据");
            
            // 发送所有维度的区域数据
            sendAllAreaDataToClient(player);
            
            // 发送维度域名配置
            areahint.network.DimensionalNameNetworking.sendDimensionalNamesToClient(player);
            
            Areashint.LOGGER.info("已向玩家 " + player.getName().getString() + " 发送完整的游戏数据");
        });
    }
    
    /**
     * 向客户端发送区域数据
     * @param player 目标玩家
     * @param dimensionName 维度名称（overworld、the_nether、the_end）
     */
    public static void sendAreaDataToClient(ServerPlayerEntity player, String dimensionName) {
        try {
            String fileName = Packets.getFileNameForDimension(dimensionName);
            if (fileName == null) {
                Areashint.LOGGER.warn("未知的维度名称: " + dimensionName);
                return;
            }
            
            // 使用世界文件夹管理器获取文件路径
            Path filePath = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            Areashint.LOGGER.info("[调试] 区域文件路径: " + filePath.toAbsolutePath());
            
            if (!Files.exists(filePath)) {
                Areashint.LOGGER.warn("区域数据文件不存在: " + filePath);
                // 尝试创建空文件
                try {
                    FileManager.createEmptyAreaFile(filePath);
                    Areashint.LOGGER.info("已创建空的区域文件: " + filePath);
                } catch (IOException e) {
                    Areashint.LOGGER.error("创建空区域文件失败: " + e.getMessage());
                }
                return;
            }
            
            // 读取文件内容
            String fileContent = Files.readString(filePath);
            Areashint.LOGGER.info("[调试] 区域文件内容长度: " + fileContent.length() + " 字节");
            if (fileContent.length() < 100) {
                Areashint.LOGGER.info("[调试] 区域文件内容预览: " + fileContent);
            } else {
                Areashint.LOGGER.info("[调试] 区域文件内容预览: " + fileContent.substring(0, 100) + "...");
            }
            
            // 创建数据包
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeString(dimensionName);
            buffer.writeString(fileContent);
            
            // 发送数据包
            ServerPlayNetworking.send(player, new Identifier(Packets.S2C_AREA_DATA), buffer);
            
            Areashint.LOGGER.info("已向玩家 " + player.getName().getString() + " 发送 " + dimensionName + " 的区域数据");
            
        } catch (IOException e) {
            Areashint.LOGGER.error("发送区域数据时出错: " + e.getMessage());
        }
    }
    
    /**
     * 向所有客户端发送区域数据
     * @param dimensionName 维度名称（overworld、the_nether、the_end）
     */
    public static void sendAreaDataToAll(String dimensionName) {
        if (Areashint.getServer() == null) {
            return;
        }
        
        List<ServerPlayerEntity> players = Areashint.getServer().getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            sendAreaDataToClient(player, dimensionName);
        }
    }
    
    /**
     * 向所有客户端发送所有维度的区域数据
     */
    public static void sendAllAreaDataToAll() {
        Areashint.LOGGER.info("[调试] 开始向所有客户端发送所有维度的区域数据");
        sendAreaDataToAll(Packets.DIMENSION_OVERWORLD);
        sendAreaDataToAll(Packets.DIMENSION_NETHER);
        sendAreaDataToAll(Packets.DIMENSION_END);
        Areashint.LOGGER.info("[调试] 完成向所有客户端发送所有维度的区域数据");
    }
    
    /**
     * 向指定玩家发送所有维度的区域数据
     * @param player 目标玩家
     */
    public static void sendAllAreaDataToClient(ServerPlayerEntity player) {
        sendAreaDataToClient(player, Packets.DIMENSION_OVERWORLD);
        sendAreaDataToClient(player, Packets.DIMENSION_NETHER);
        sendAreaDataToClient(player, Packets.DIMENSION_END);
    }
    
    /**
     * 向客户端发送命令
     * @param player 目标玩家
     * @param command 命令字符串
     */
    public static void sendCommandToClient(ServerPlayerEntity player, String command) {
        try {
            // 创建数据包
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeString(command);
            
            // 发送数据包
            ServerPlayNetworking.send(player, new Identifier(Packets.S2C_CLIENT_COMMAND), buffer);
            
            Areashint.LOGGER.info("已向玩家 " + player.getName().getString() + " 发送命令: " + command);
        } catch (Exception e) {
            Areashint.LOGGER.error("发送命令到客户端时出错: " + e.getMessage());
        }
    }
    
    /**
     * 向所有在线客户端发送命令
     */
    public static void sendCommandToAllClients(MinecraftServer server, String command) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendCommandToClient(player, command);
        }
    }

    /**
     * 向客户端发送调试命令
     * @param player 目标玩家
     * @param enabled 是否启用调试
     */
    public static void sendDebugCommandToClient(ServerPlayerEntity player, boolean enabled) {
        try {
            // 创建数据包
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeBoolean(enabled);
            
            // 发送数据包
            ServerPlayNetworking.send(player, new Identifier(Packets.S2C_DEBUG_COMMAND), buffer);
            
            Areashint.LOGGER.info("已向玩家 " + player.getName().getString() + " 发送调试命令: " + (enabled ? "启用" : "禁用"));
        } catch (Exception e) {
            Areashint.LOGGER.error("发送调试命令到客户端时出错: " + e.getMessage());
        }
    }
    
    /**
     * 向所有在线玩家发送指定维度的区域数据
     * @param dimensionType 维度类型
     */
    public static void sendAreaDataToAllPlayers(String dimensionType) {
        try {
            // 获取所有在线玩家
            List<ServerPlayerEntity> players = Areashint.getServer().getPlayerManager().getPlayerList();
            
            for (ServerPlayerEntity player : players) {
                // 检查玩家是否在指定维度中
                String playerDimension = player.getWorld().getRegistryKey().getValue().toString();
                String playerDimensionType = Packets.convertDimensionPathToType(playerDimension);
                
                if (playerDimensionType != null && playerDimensionType.equals(dimensionType)) {
                    // 发送区域数据到该玩家
                    sendAreaDataToClient(player, dimensionType);
                }
            }
            
            Areashint.LOGGER.info("已向所有在 {} 维度的玩家重新分发区域数据", dimensionType);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("向所有玩家发送区域数据时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 注册网络请求处理器
     */
    private static void registerNetworkHandlers() {
        // 注册recolor请求处理器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_RECOLOR_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String areaName = buf.readString();
                    String color = buf.readString();
                    String dimension = buf.readString();

                    server.execute(() -> {
                        areahint.command.RecolorCommand.handleRecolorRequest(player, areaName, color, dimension);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理recolor请求时发生错误", e);
                }
            });

        // 注册SetHigh请求处理器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_SETHIGH_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String areaName = buf.readString();
                    final boolean hasCustomHeight = buf.readBoolean();
                    final Double maxHeight;
                    final Double minHeight;

                    if (hasCustomHeight) {
                        boolean hasMax = buf.readBoolean();
                        maxHeight = hasMax ? buf.readDouble() : null;
                        boolean hasMin = buf.readBoolean();
                        minHeight = hasMin ? buf.readDouble() : null;
                    } else {
                        maxHeight = null;
                        minHeight = null;
                    }

                    server.execute(() -> {
                        areahint.command.SetHighCommand.handleHeightRequest(player, areaName, hasCustomHeight, maxHeight, minHeight);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理SetHigh请求时发生错误", e);
                }
            });

        // 注册请求可删除域名列表处理器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_REQUEST_DELETABLE_AREAS,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimension = buf.readString();

                    server.execute(() -> {
                        sendDeletableAreasList(player, dimension);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理请求可删除域名列表时发生错误", e);
                }
            });

        // 注册Delete请求处理器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DELETE_AREA,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String areaName = buf.readString();
                    final String dimension = buf.readString();

                    server.execute(() -> {
                        handleDeleteRequest(player, areaName, dimension);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理Delete请求时发生错误", e);
                }
            });

        // 注册维度域名修改请求处理器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DIMNAME_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimensionId = buf.readString();
                    final String newName = buf.readString();
                    server.execute(() -> {
                        if (!player.hasPermissionLevel(2)) {
                            player.sendMessage(net.minecraft.text.Text.of("§c权限不足"), false);
                            return;
                        }
                        areahint.command.DimensionalNameCommands.handleDimNameChange(
                            player.getCommandSource(), dimensionId, newName);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理维度域名修改请求时发生错误", e);
                }
            });

        // 注册维度域名颜色修改请求处理器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DIMCOLOR_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimensionId = buf.readString();
                    final String newColor = buf.readString();
                    server.execute(() -> {
                        if (!player.hasPermissionLevel(2)) {
                            player.sendMessage(net.minecraft.text.Text.of("§c权限不足"), false);
                            return;
                        }
                        areahint.command.DimensionalNameCommands.handleDimColorChange(
                            player.getCommandSource(), dimensionId, newColor);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理维度域名颜色修改请求时发生错误", e);
                }
            });
    }

    /**
     * 发送可删除域名列表到客户端
     * @param player 请求的玩家
     * @param dimension 维度标识
     */
    private static void sendDeletableAreasList(ServerPlayerEntity player, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean hasOp = player.hasPermissionLevel(2);

            Areashint.LOGGER.info("处理可删除域名列表请求 - 玩家: " + playerName + ", 维度: " + dimension + ", 是否OP: " + hasOp);

            // 获取文件名
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                Areashint.LOGGER.warn("无法确定维度文件 - 维度: " + dimension);
                // 发送空列表
                sendEmptyDeletableAreasList(player);
                return;
            }

            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);

            // 读取区域数据
            java.util.List<areahint.data.AreaData> allAreas = FileManager.readAreaData(areaFile);
            java.util.List<areahint.data.AreaData> deletableAreas = new java.util.ArrayList<>();

            // 筛选可删除的域名
            for (areahint.data.AreaData area : allAreas) {
                String signature = area.getSignature();

                // 检查权限
                boolean canDelete = false;
                if (signature == null) {
                    // 没有签名的旧域名只有管理员可以删除
                    canDelete = hasOp;
                } else {
                    // 有签名的新域名：创建者或管理员可以删除
                    canDelete = signature.equals(playerName) || hasOp;
                }

                if (canDelete) {
                    // 检查是否有次级域名引用此域名
                    boolean hasChildren = false;
                    for (areahint.data.AreaData childArea : allAreas) {
                        if (area.getName().equals(childArea.getBaseName())) {
                            hasChildren = true;
                            break;
                        }
                    }

                    // 只有没有子域名的才能删除
                    if (!hasChildren) {
                        deletableAreas.add(area);
                    }
                }
            }

            Areashint.LOGGER.info("找到 " + deletableAreas.size() + " 个可删除的域名");

            // 创建数据包
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(deletableAreas.size());

            for (areahint.data.AreaData area : deletableAreas) {
                String json = areahint.file.JsonHelper.toJsonSingle(area);
                buffer.writeString(json);
            }

            // 发送数据包
            ServerPlayNetworking.send(player, Packets.S2C_DELETABLE_AREAS_LIST, buffer);

            Areashint.LOGGER.info("已向玩家 " + playerName + " 发送可删除域名列表");

        } catch (Exception e) {
            Areashint.LOGGER.error("发送可删除域名列表时发生错误", e);
            sendEmptyDeletableAreasList(player);
        }
    }

    /**
     * 发送空的可删除域名列表
     * @param player 目标玩家
     */
    private static void sendEmptyDeletableAreasList(ServerPlayerEntity player) {
        try {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(0);
            ServerPlayNetworking.send(player, Packets.S2C_DELETABLE_AREAS_LIST, buffer);
        } catch (Exception e) {
            Areashint.LOGGER.error("发送空可删除域名列表时发生错误", e);
        }
    }

    /**
     * 处理删除域名请求
     * @param player 请求的玩家
     * @param areaName 要删除的域名名称
     * @param dimension 维度标识
     */
    private static void handleDeleteRequest(ServerPlayerEntity player, String areaName, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean hasOp = player.hasPermissionLevel(2);

            Areashint.LOGGER.info("处理删除请求 - 玩家: " + playerName + ", 域名: " + areaName + ", 维度: " + dimension);

            // 获取文件名
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                Areashint.LOGGER.warn("无法确定维度文件 - 维度: " + dimension);
                sendDeleteResponse(player, false, "无法确定维度文件（维度: " + dimension + "）");
                return;
            }

            Areashint.LOGGER.info("维度文件名: " + fileName);

            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            Areashint.LOGGER.info("域名文件路径: " + areaFile.toAbsolutePath());

            // 读取区域数据
            java.util.List<areahint.data.AreaData> areas = FileManager.readAreaData(areaFile);
            Areashint.LOGGER.info("读取到 " + areas.size() + " 个域名");

            // 查找要删除的域名
            areahint.data.AreaData targetArea = null;
            for (areahint.data.AreaData area : areas) {
                if (area.getName().equals(areaName)) {
                    targetArea = area;
                    break;
                }
            }

            if (targetArea == null) {
                Areashint.LOGGER.warn("未找到域名: " + areaName);
                sendDeleteResponse(player, false, "未找到域名: " + areaName);
                return;
            }

            Areashint.LOGGER.info("找到目标域名: " + areaName + ", 签名: " + targetArea.getSignature());

            // 检查签名权限
            String signature = targetArea.getSignature();
            if (signature == null) {
                // 没有签名的旧域名：只有管理员可以删除
                if (!hasOp) {
                    Areashint.LOGGER.warn("玩家 " + playerName + " 不是管理员，无法删除旧版本域名");
                    sendDeleteResponse(player, false, "该域名没有签名（旧版本域名），只有管理员可以删除");
                    return;
                }
                // 管理员可以继续删除
                Areashint.LOGGER.info("管理员 " + playerName + " 正在删除旧版本域名");
            } else {
                // 有签名的新域名：创建者或管理员可以删除
                if (!signature.equals(playerName) && !hasOp) {
                    Areashint.LOGGER.warn("玩家 " + playerName + " 不是域名创建者 " + signature);
                    sendDeleteResponse(player, false, "你不是该域名的创建者，无法删除");
                    return;
                }
            }

            // 检查是否有次级域名引用此域名
            for (areahint.data.AreaData area : areas) {
                if (areaName.equals(area.getBaseName())) {
                    Areashint.LOGGER.warn("域名 " + areaName + " 被子域名 " + area.getName() + " 引用");
                    sendDeleteResponse(player, false, "不能删除该域名，因为存在次级域名 \"" + area.getName() + "\" 引用了它");
                    return;
                }
            }

            // 执行删除
            areas.remove(targetArea);
            Areashint.LOGGER.info("从列表中移除域名: " + areaName);

            // 保存文件
            FileManager.writeAreaData(areaFile, areas);

            // 向所有客户端发送更新后的区域数据
            sendAllAreaDataToAll();

            // 发送成功响应
            sendDeleteResponse(player, true, "成功删除域名: " + areaName);

            Areashint.LOGGER.info("玩家 " + playerName + " 删除了域名: " + areaName);

        } catch (Exception e) {
            Areashint.LOGGER.error("处理删除请求时发生错误", e);
            sendDeleteResponse(player, false, "删除域名时发生错误: " + e.getMessage());
        }
    }

    /**
     * 发送删除响应到客户端
     * @param player 目标玩家
     * @param success 是否成功
     * @param message 响应消息
     */
    private static void sendDeleteResponse(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeBoolean(success);
            buffer.writeString(message);

            ServerPlayNetworking.send(player, Packets.S2C_DELETE_RESPONSE, buffer);

            Areashint.LOGGER.info("已向玩家 " + player.getName().getString() + " 发送删除响应: " +
                (success ? "成功" : "失败") + " - " + message);
        } catch (Exception e) {
            Areashint.LOGGER.error("发送删除响应时出错: " + e.getMessage());
        }
    }
} 