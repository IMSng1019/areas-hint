package areahint.network;

import areahint.Areashint;
import areahint.file.FileManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;
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
        
        // 注册玩家连接事件，当玩家加入服务器时发送区域数据
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Areashint.LOGGER.info("玩家 " + player.getName().getString() + " 已连接，发送区域数据");
            sendAllAreaDataToClient(player);
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
            
            // 读取区域数据文件
            Path filePath = FileManager.getDimensionFile(fileName);
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
} 