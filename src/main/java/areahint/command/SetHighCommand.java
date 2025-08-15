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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 域名高度设置命令处理器
 * 处理 /areahint sethigh 命令
 */
public class SetHighCommand {
    
    /**
     * 执行sethigh指令 - 列出可修改的域名
     * @param context 指令上下文
     * @return 执行结果
     */
    public static int executeSetHigh(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendMessage(Text.of("§c此指令只能由玩家执行"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        String playerName = player.getName().getString();
        boolean isAdmin = source.hasPermissionLevel(2);
        
        // 获取玩家当前维度
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        String fileName = Packets.getFileNameForDimension(dimensionType);
        
        if (fileName == null) {
            source.sendMessage(Text.of("§c无法识别当前维度"));
            return 0;
        }
        
        // 获取可修改高度的域名列表
        List<AreaData> editableAreas = getHeightEditableAreas(fileName, playerName, isAdmin);
        
        if (editableAreas.isEmpty()) {
            source.sendMessage(Text.of("§c当前维度没有您可以修改高度的域名"));
            return 0;
        }
        
        // 发送域名列表到客户端
        sendAreaListForHeightEdit(player, editableAreas, dimensionType);
        
        source.sendMessage(Text.of("§a已向您发送可修改高度的域名列表，请在客户端选择要设置高度的域名"));
        return 1;
    }
    
    /**
     * 获取可编辑高度的域名列表
     * @param fileName 文件名
     * @param playerName 玩家名称
     * @param isAdmin 是否为管理员
     * @return 可编辑的域名列表
     */
    private static List<AreaData> getHeightEditableAreas(String fileName, String playerName, boolean isAdmin) {
        try {
            Path areaFile = FileManager.getDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                return new ArrayList<>();
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            
            return areas.stream()
                    .filter(area -> canEditAreaHeight(area, playerName, isAdmin, areas))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            Areashint.LOGGER.error("读取域名数据时发生错误", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 检查是否可以编辑域名高度
     * @param area 域名数据
     * @param playerName 玩家名称
     * @param isAdmin 是否为管理员
     * @param allAreas 所有域名数据（用于检查basename引用）
     * @return 是否可以编辑
     */
    private static boolean canEditAreaHeight(AreaData area, String playerName, boolean isAdmin, List<AreaData> allAreas) {
        // 管理员可以编辑所有域名的高度
        if (isAdmin) {
            return true;
        }
        
        // 普通玩家可以编辑自己创建的域名（signature匹配）
        if (area.getSignature() != null && area.getSignature().equals(playerName)) {
            return true;
        }
        
        // 普通玩家可以编辑被自己basename引用的域名
        // 即检查是否有其他域名的baseName指向当前玩家创建的域名
        for (AreaData otherArea : allAreas) {
            // 如果其他域名的baseName指向当前检查的域名，并且其他域名是玩家创建的
            if (area.getName().equals(otherArea.getBaseName()) && 
                otherArea.getSignature() != null && 
                otherArea.getSignature().equals(playerName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 向客户端发送域名列表用于高度编辑
     * @param player 玩家
     * @param areas 域名列表
     * @param dimensionType 维度类型
     */
    private static void sendAreaListForHeightEdit(ServerPlayerEntity player, List<AreaData> areas, String dimensionType) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("sethigh_area_list");
            buf.writeString(dimensionType);
            buf.writeInt(areas.size());
            
            for (AreaData area : areas) {
                buf.writeString(area.getName());
                
                // 发送当前高度信息
                if (area.getAltitude() != null) {
                    buf.writeBoolean(true); // 有高度限制
                    buf.writeBoolean(area.getAltitude().getMax() != null);
                    if (area.getAltitude().getMax() != null) {
                        buf.writeDouble(area.getAltitude().getMax());
                    }
                    buf.writeBoolean(area.getAltitude().getMin() != null);
                    if (area.getAltitude().getMin() != null) {
                        buf.writeDouble(area.getAltitude().getMin());
                    }
                } else {
                    buf.writeBoolean(false); // 无高度限制
                }
            }
            
            ServerPlayNetworking.send(player, Packets.S2C_SETHIGH_AREA_LIST, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送高度编辑域名列表时发生错误", e);
        }
    }
    
    /**
     * 处理来自客户端的高度设置请求
     * @param player 玩家
     * @param areaName 域名名称
     * @param hasCustomHeight 是否使用自定义高度
     * @param maxHeight 最大高度（如果hasCustomHeight为true）
     * @param minHeight 最小高度（如果hasCustomHeight为true）
     */
    public static void handleSetHeightRequest(ServerPlayerEntity player, String areaName, 
                                            boolean hasCustomHeight, Double maxHeight, Double minHeight) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);
            
            // 获取玩家当前维度
            String dimension = player.getWorld().getRegistryKey().getValue().toString();
            String dimensionType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimensionType);
            
            if (fileName == null) {
                sendSetHeightResponse(player, false, "无法识别当前维度");
                return;
            }
            
            Path areaFile = FileManager.getDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                sendSetHeightResponse(player, false, "域名文件不存在");
                return;
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            AreaData targetArea = null;
            
            // 查找目标域名
            for (AreaData area : areas) {
                if (area.getName().equals(areaName)) {
                    targetArea = area;
                    break;
                }
            }
            
            if (targetArea == null) {
                sendSetHeightResponse(player, false, "未找到域名: " + areaName);
                return;
            }
            
            // 权限检查
            if (!canEditAreaHeight(targetArea, playerName, isAdmin, areas)) {
                sendSetHeightResponse(player, false, "您没有权限修改域名 \"" + areaName + "\" 的高度");
                return;
            }
            
            // 验证高度数据
            if (hasCustomHeight) {
                if (maxHeight != null && minHeight != null && maxHeight < minHeight) {
                    sendSetHeightResponse(player, false, "最大高度不能小于最小高度");
                    return;
                }
                
                // 检查高度值的合理性
                if (maxHeight != null && (maxHeight < -64 || maxHeight > 320)) {
                    sendSetHeightResponse(player, false, "最大高度超出合理范围 [-64, 320]");
                    return;
                }
                if (minHeight != null && (minHeight < -64 || minHeight > 320)) {
                    sendSetHeightResponse(player, false, "最小高度超出合理范围 [-64, 320]");
                    return;
                }
            }
            
            // 保存原始高度信息用于日志
            String oldHeightInfo = getHeightDisplayString(targetArea.getAltitude());
            
            // 设置新的高度
            if (hasCustomHeight) {
                if (targetArea.getAltitude() == null) {
                    targetArea.setAltitude(new AreaData.AltitudeData());
                }
                targetArea.getAltitude().setMax(maxHeight);
                targetArea.getAltitude().setMin(minHeight);
            } else {
                // 不限制高度
                targetArea.setAltitude(null);
            }
            
            // 保存文件
            if (FileManager.writeAreaData(areaFile, areas)) {
                String newHeightInfo = getHeightDisplayString(targetArea.getAltitude());
                sendSetHeightResponse(player, true, 
                    String.format("§a域名 \"%s\" 高度设置成功！\n§7原高度: %s\n§7新高度: %s", 
                                areaName, oldHeightInfo, newHeightInfo));
                
                // 重新加载并发送给所有客户端（相当于执行reload）
                ServerNetworking.sendAllAreaDataToAll();
                
                Areashint.LOGGER.info("玩家 " + playerName + " 将域名 \"" + areaName + "\" 的高度从 " + oldHeightInfo + " 更改为 " + newHeightInfo);
                
            } else {
                sendSetHeightResponse(player, false, "保存域名数据时失败");
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error("处理高度设置请求时发生错误", e);
            sendSetHeightResponse(player, false, "处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取高度的显示字符串
     * @param altitude 高度数据
     * @return 显示字符串
     */
    private static String getHeightDisplayString(AreaData.AltitudeData altitude) {
        if (altitude == null) {
            return "无限制";
        }
        
        String maxStr = altitude.getMax() != null ? String.format("%.1f", altitude.getMax()) : "无限制";
        String minStr = altitude.getMin() != null ? String.format("%.1f", altitude.getMin()) : "无限制";
        
        return String.format("最高:%s, 最低:%s", maxStr, minStr);
    }
    
    /**
     * 发送高度设置响应到客户端
     * @param player 玩家
     * @param success 是否成功
     * @param message 消息
     */
    private static void sendSetHeightResponse(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("sethigh_response");
            buf.writeBoolean(success);
            buf.writeString(message);
            
            ServerPlayNetworking.send(player, Packets.S2C_SETHIGH_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送高度设置响应时发生错误", e);
        }
    }
    
    /**
     * 将维度ID转换为Packets期望的维度类型
     * @param dimension 维度ID
     * @return 维度类型字符串
     */
    private static String convertDimensionIdToType(String dimension) {
        if (dimension.contains("overworld")) {
            return Packets.DIMENSION_OVERWORLD;
        } else if (dimension.contains("the_nether")) {
            return Packets.DIMENSION_NETHER;
        } else if (dimension.contains("the_end")) {
            return Packets.DIMENSION_END;
        }
        return Packets.DIMENSION_OVERWORLD; // 默认返回主世界
    }
} 