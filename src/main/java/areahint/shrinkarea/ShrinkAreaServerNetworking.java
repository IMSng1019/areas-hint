package areahint.shrinkarea;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

/**
 * 收缩域名服务端网络处理
 * 处理客户端发送的收缩域名请求
 */
public class ShrinkAreaServerNetworking {
    
    /**
     * 注册服务端网络处理器
     */
    public static void registerServerNetworking() {
        // 注册接收客户端收缩域名请求的处理器
        ServerPlayNetworking.registerGlobalReceiver(
            Packets.SHRINK_AREA_CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String areaJsonString = buf.readString(32767);
                    
                    server.execute(() -> {
                        handleShrinkAreaRequest(player, areaJsonString);
                    });
                    
                } catch (Exception e) {
                    System.err.println("处理收缩域名请求时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    sendErrorResponse(player, "处理请求时发生内部错误");
                }
            }
        );
    }
    
    /**
     * 处理收缩域名请求
     */
    private static void handleShrinkAreaRequest(ServerPlayerEntity player, String areaJsonString) {
        try {
            // 解析接收到的域名数据
            JsonObject areaJson = JsonParser.parseString(areaJsonString).getAsJsonObject();
            AreaData shrunkArea = AreaDataConverter.fromJsonObject(areaJson);
            
            // 验证权限
            if (!validatePermission(player, shrunkArea)) {
                sendErrorResponse(player, "您没有权限收缩此域名");
                return;
            }
            
            // 验证域名数据
            if (!validateAreaData(shrunkArea)) {
                sendErrorResponse(player, "域名数据验证失败");
                return;
            }
            
            // 保存收缩后的域名
            boolean success = saveShrunkArea(shrunkArea);
            if (!success) {
                sendErrorResponse(player, "保存域名失败");
                return;
            }
            
            // 重新分发给所有玩家
            redistributeAreasToAllPlayers(player.getServer());
            
            // 发送成功响应
            sendSuccessResponse(player, "域名 '" + shrunkArea.getName() + "' 收缩成功");
            
            // 服务端日志
            System.out.println("玩家 " + player.getGameProfile().getName() + 
                             " 成功收缩域名: " + shrunkArea.getName());
            
        } catch (Exception e) {
            System.err.println("处理收缩域名请求失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(player, "处理请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证玩家权限
     */
    private static boolean validatePermission(ServerPlayerEntity player, AreaData area) {
        try {
            String playerName = player.getGameProfile().getName();
            
            // 检查是否为管理员
            if (player.hasPermissionLevel(2)) {
                return true;
            }
            
            // 检查是否为域名的创建者（basename匹配）
            if (playerName.equals(area.getBaseName())) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("验证权限时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证域名数据
     */
    private static boolean validateAreaData(AreaData area) {
        if (area == null) {
            return false;
        }
        
        // 基本验证
        if (!area.isValid()) {
            return false;
        }
        
        // 检查顶点数量
        if (area.getVertices() == null || area.getVertices().size() < 3) {
            return false;
        }
        
        // 检查名称
        if (area.getName() == null || area.getName().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 保存收缩后的域名
     */
    private static boolean saveShrunkArea(AreaData shrunkArea) {
        try {
            // 获取当前区域文件路径
            java.nio.file.Path areaPath = FileManager.checkFolderExist().resolve("overworld.json");
            
            // 加载现有域名
            List<AreaData> existingAreas = FileManager.readAreaData(areaPath);
            
            // 查找并替换同名域名
            boolean found = false;
            for (int i = 0; i < existingAreas.size(); i++) {
                AreaData existing = existingAreas.get(i);
                if (existing.getName().equals(shrunkArea.getName())) {
                    existingAreas.set(i, shrunkArea);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                System.err.println("未找到要收缩的域名: " + shrunkArea.getName());
                return false;
            }
            
            // 保存更新后的域名列表
            return FileManager.writeAreaData(areaPath, existingAreas);
            
        } catch (Exception e) {
            System.err.println("保存收缩域名失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 重新分发域名给所有玩家
     */
    private static void redistributeAreasToAllPlayers(net.minecraft.server.MinecraftServer server) {
        try {
            // 向所有玩家发送重新加载的区域数据
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerNetworking.sendAreaDataToClient(player, "overworld");
            }
            
        } catch (Exception e) {
            System.err.println("重新分发域名失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 发送成功响应
     */
    private static void sendSuccessResponse(ServerPlayerEntity player, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(true);  // 成功
            buf.writeString(message);
            
            ServerPlayNetworking.send(player, Packets.SHRINK_AREA_RESPONSE_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 发送错误响应
     */
    private static void sendErrorResponse(ServerPlayerEntity player, String errorMessage) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(false); // 失败
            buf.writeString(errorMessage);
            
            ServerPlayNetworking.send(player, Packets.SHRINK_AREA_RESPONSE_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 