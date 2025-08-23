package areahint.expandarea;

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
import java.nio.file.Path;

public class ExpandAreaServerNetworking {
    
    /**
     * 注册服务端网络处理器
     */
    public static void registerServerNetworking() {
        // 注册接收客户端扩展域名请求的处理器
        ServerPlayNetworking.registerGlobalReceiver(
            Packets.EXPAND_AREA_CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String areaJsonString = buf.readString(32767);
                    
                    server.execute(() -> {
                        handleExpandAreaRequest(player, areaJsonString);
                    });
                    
                } catch (Exception e) {
                    System.err.println("处理扩展域名请求时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    sendErrorResponse(player, "处理请求时发生内部错误");
                }
            }
        );
    }
    
    /**
     * 处理扩展域名请求
     */
    private static void handleExpandAreaRequest(ServerPlayerEntity player, String areaJsonString) {
        try {
            // 解析接收到的域名数据
            JsonObject areaJson = JsonParser.parseString(areaJsonString).getAsJsonObject();
            AreaData expandedArea = AreaDataConverter.fromJsonObject(areaJson);
            
            // 验证权限
            if (!validatePermission(player, expandedArea)) {
                sendErrorResponse(player, "您没有权限扩展此域名");
                return;
            }
            
            // 验证域名数据
            if (!validateAreaData(expandedArea)) {
                sendErrorResponse(player, "域名数据验证失败");
                return;
            }
            
            // 保存扩展后的域名
            boolean success = saveExpandedArea(expandedArea);
            if (!success) {
                sendErrorResponse(player, "保存域名失败");
                return;
            }
            
            // 重新分发给所有玩家
            redistributeAreasToAllPlayers(player.getServer());
            
            // 发送成功响应
            sendSuccessResponse(player, "域名 '" + expandedArea.getName() + "' 扩展成功");
            
            // 服务端日志
            System.out.println("玩家 " + player.getGameProfile().getName() + 
                             " 成功扩展域名: " + expandedArea.getName());
            
        } catch (Exception e) {
            System.err.println("处理扩展域名请求失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(player, "处理请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证玩家权限
     */
    private static boolean validatePermission(ServerPlayerEntity player, AreaData area) {
        String playerName = player.getGameProfile().getName();
        
        // 检查是否为管理员
        if (player.hasPermissionLevel(2)) {
            return true;
        }
        
        // 检查是否为域名创建者
        if (playerName.equals(area.getSignature())) {
            return true;
        }
        
        // 检查是否为basename引用的玩家
        if (area.getBaseName() != null) {
            try {
                AreaData baseArea = findAreaByName(area.getBaseName());
                if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                    return true;
                }
            } catch (Exception e) {
                System.err.println("检查basename权限时发生错误: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * 验证域名数据
     */
    private static boolean validateAreaData(AreaData area) {
        // 检查基本字段
        if (area.getName() == null || area.getName().trim().isEmpty()) {
            return false;
        }
        
        if (area.getVertices() == null || area.getVertices().size() < 3) {
            return false;
        }
        
        if (area.getSignature() == null || area.getSignature().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 保存扩展后的域名
     */
    private static boolean saveExpandedArea(AreaData expandedArea) {
        try {
            // 获取当前区域文件路径
            Path areaPath = FileManager.checkFolderExist().resolve("overworld.json");
            
            // 加载现有域名
            List<AreaData> existingAreas = FileManager.readAreaData(areaPath);
            
            // 查找并更新现有域名
            boolean found = false;
            for (int i = 0; i < existingAreas.size(); i++) {
                AreaData existingArea = existingAreas.get(i);
                if (existingArea.getName().equals(expandedArea.getName())) {
                    // 更新现有域名
                    existingAreas.set(i, expandedArea);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                System.err.println("未找到要扩展的域名: " + expandedArea.getName());
                return false;
            }
            
            // 保存更新后的域名列表
            return FileManager.writeAreaData(areaPath, existingAreas);
            
        } catch (Exception e) {
            System.err.println("保存扩展域名失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据名称查找域名
     */
    private static AreaData findAreaByName(String name) {
        try {
            Path areaPath = FileManager.checkFolderExist().resolve("overworld.json");
            List<AreaData> areas = FileManager.readAreaData(areaPath);
            
            for (AreaData area : areas) {
                if (area.getName().equals(name)) {
                    return area;
                }
            }
        } catch (Exception e) {
            System.err.println("查找域名失败: " + e.getMessage());
        }
        return null;
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
            
            ServerPlayNetworking.send(player, Packets.EXPAND_AREA_RESPONSE_CHANNEL, buf);
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
            
            ServerPlayNetworking.send(player, Packets.EXPAND_AREA_RESPONSE_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


} 