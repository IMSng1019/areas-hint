package areahint.easyadd;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.file.JsonHelper;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.List;

/**
 * EasyAdd服务端网络处理器
 * 处理客户端发送的域名数据
 */
public class EasyAddServerNetworking {
    
    /**
     * 注册服务端网络接收器
     */
    public static void registerServerReceivers() {
        // 注册EasyAdd域名数据接收器
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_EASYADD_AREA_DATA, 
            (server, player, handler, buf, responseSender) -> {
                try {
                    String jsonData = buf.readString();
                    String dimension = buf.readString();
                    
                    server.execute(() -> {
                        handleEasyAddAreaData(player, jsonData, dimension);
                    });
                    
                } catch (Exception e) {
                    Areashint.LOGGER.error("处理EasyAdd域名数据时发生错误", e);
                    sendResponseToClient(player, false, "服务端处理域名数据时发生错误: " + e.getMessage());
                }
            });
    }
    
    /**
     * 处理EasyAdd域名数据
     */
    private static void handleEasyAddAreaData(ServerPlayerEntity player, String jsonData, String dimension) {
        try {
            // 反序列化域名数据
            AreaData areaData = JsonHelper.fromJsonSingle(jsonData);
            
            // 验证域名数据
            if (!validateAreaData(areaData)) {
                sendResponseToClient(player, false, "域名数据验证失败");
                return;
            }
            
            // 获取维度文件名 - 使用统一的命名规则
            String dimensionType = convertDimensionIdToType(dimension);
            String fileName = areahint.network.Packets.getFileNameForDimension(dimensionType);
            if (fileName == null) {
                sendResponseToClient(player, false, "无效的维度: " + dimension);
                return;
            }
            
            // 保存域名数据
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            
            // 读取现有域名数据
            List<AreaData> existingAreas = FileManager.readAreaData(areaFile);
            
            // 检查域名名称（name字段）是否已存在
            for (AreaData existing : existingAreas) {
                if (existing.getName().equals(areaData.getName())) {
                    sendResponseToClient(player, false, "域名 \"" + areaData.getName() + "\" 已存在");
                    return;
                }
            }
            
            // 添加新域名
            existingAreas.add(areaData);
            
            // 保存到文件
            if (FileManager.writeAreaData(areaFile, existingAreas)) {
                // 发送成功响应
                sendResponseToClient(player, true, "域名 \"" + areaData.getName() + "\" 创建成功");
                
                // 向所有客户端发送更新
                ServerNetworking.sendAllAreaDataToAll();
                
                Areashint.LOGGER.info("玩家 " + player.getName().getString() + " 通过EasyAdd创建了域名: " + areaData.getName());
                
            } else {
                sendResponseToClient(player, false, "保存域名数据到文件时失败");
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error("处理EasyAdd域名数据时发生错误", e);
            sendResponseToClient(player, false, "处理域名数据时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 验证域名数据的基本有效性
     */
    private static boolean validateAreaData(AreaData areaData) {
        if (areaData == null) {
            return false;
        }
        
        if (areaData.getName() == null || areaData.getName().trim().isEmpty()) {
            return false;
        }
        
        if (areaData.getVertices() == null || areaData.getVertices().size() < 3) {
            return false;
        }
        
        if (areaData.getLevel() < 1 || areaData.getLevel() > 3) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 将维度ID转换为Packets期望的维度类型
     */
    private static String convertDimensionIdToType(String dimension) {
        if (dimension == null) return null;
        
        if (dimension.contains("overworld")) {
            return areahint.network.Packets.DIMENSION_OVERWORLD;
        } else if (dimension.contains("nether")) {
            return areahint.network.Packets.DIMENSION_NETHER;
        } else if (dimension.contains("end")) {
            return areahint.network.Packets.DIMENSION_END;
        }
        return null;
    }
    
    /**
     * 向客户端发送响应
     */
    private static void sendResponseToClient(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(success);
            buf.writeString(message);
            
            ServerPlayNetworking.send(player, Packets.S2C_EASYADD_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送EasyAdd响应到客户端时发生错误", e);
        }
    }
} 