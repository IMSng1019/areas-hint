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
                    String dimension = buf.readString(32767);  // 接收维度信息

                    server.execute(() -> {
                        handleExpandAreaRequest(player, areaJsonString, dimension);
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
    private static void handleExpandAreaRequest(ServerPlayerEntity player, String areaJsonString, String dimension) {
        try {
            // 解析接收到的域名数据
            JsonObject areaJson = JsonParser.parseString(areaJsonString).getAsJsonObject();
            AreaData expandedArea = AreaDataConverter.fromJsonObject(areaJson);

            // 获取玩家所在维度
            String playerDimension = player.getWorld().getRegistryKey().getValue().toString();
            String playerDimensionType = Packets.convertDimensionPathToType(playerDimension);

            // 验证权限（只在玩家所在维度查找）
            if (!validatePermission(player, expandedArea, playerDimensionType)) {
                sendErrorResponse(player, "您没有权限扩展此域名");
                return;
            }

            // 验证域名数据
            if (!validateAreaData(expandedArea)) {
                sendErrorResponse(player, "域名数据验证失败");
                return;
            }

            // 保存扩展后的域名
            boolean success = saveExpandedArea(expandedArea, dimension);
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
     * @param player 玩家
     * @param area 域名数据
     * @param dimensionType 玩家所在维度类型（用于查找basename）
     */
    private static boolean validatePermission(ServerPlayerEntity player, AreaData area, String dimensionType) {
        String playerName = player.getGameProfile().getName();
        
        // 检查是否为管理员
        if (player.hasPermissionLevel(2)) {
            return true;
        }
        
        // 检查是否为域名创建者
        if (playerName.equals(area.getSignature())) {
            return true;
        }
        
        // 检查是否为basename引用的玩家（只在玩家所在维度查找）
        if (area.getBaseName() != null) {
            try {
                AreaData baseArea = findAreaByName(area.getBaseName(), dimensionType);
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
    private static boolean saveExpandedArea(AreaData expandedArea, String dimension) {
        try {
            // 将维度ID转换为Packets期望的维度类型（参考EasyAdd的实现）
            String dimensionType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimensionType);

            if (fileName == null) {
                System.err.println("无效的维度: " + dimension);
                return false;
            }

            // 获取当前区域文件路径
            Path areaPath = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);

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
     * 将维度ID转换为Packets期望的维度类型
     * 参考EasyAdd的实现
     */
    private static String convertDimensionIdToType(String dimension) {
        if (dimension == null) return null;

        if (dimension.contains("overworld")) {
            return Packets.DIMENSION_OVERWORLD;
        } else if (dimension.contains("nether")) {
            return Packets.DIMENSION_NETHER;
        } else if (dimension.contains("end")) {
            return Packets.DIMENSION_END;
        }
        return null;
    }
    
    /**
     * 根据名称查找域名（只在指定维度查找）
     * @param name 域名名称
     * @param dimensionType 维度类型（overworld/the_nether/the_end）
     * @return 找到的域名，如果未找到则返回null
     */
    private static AreaData findAreaByName(String name, String dimensionType) {
        if (dimensionType == null) {
            System.err.println("维度类型为null，无法查找域名");
            return null;
        }
        
        try {
            // 根据维度类型获取文件名
            String fileName = Packets.getFileNameForDimension(dimensionType);
            if (fileName == null) {
                System.err.println("无效的维度类型: " + dimensionType);
                return null;
            }
            
            // 获取当前区域文件路径
            Path areaPath = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            
            // 读取域名列表
            List<AreaData> areas = FileManager.readAreaData(areaPath);
            
            // 查找指定名称的域名
            for (AreaData area : areas) {
                if (area.getName().equals(name)) {
                    return area;
                }
            }
        } catch (Exception e) {
            System.err.println("查找域名失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 重新分发域名给所有玩家（相当于执行一次reload指令）
     * 向所有玩家发送所有维度的区域数据
     */
    private static void redistributeAreasToAllPlayers(net.minecraft.server.MinecraftServer server) {
        try {
            // 使用ServerNetworking的方法发送所有维度的数据（相当于reload）
            ServerNetworking.sendAllAreaDataToAll();
            
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