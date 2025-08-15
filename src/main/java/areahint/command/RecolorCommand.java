package areahint.command;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.util.ColorUtil;
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
 * 域名重新着色指令处理器
 */
public class RecolorCommand {
    
    /**
     * 执行recolor指令
     * @param context 指令上下文
     * @return 执行结果
     */
    public static int executeRecolor(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendMessage(Text.of("§c此指令只能由玩家执行"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        String playerName = player.getName().getString();
        boolean hasOp = source.hasPermissionLevel(2);
        
        // 获取玩家当前维度
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        String fileName = Packets.getFileNameForDimension(dimensionType);
        
        if (fileName == null) {
            source.sendMessage(Text.of("§c无法识别当前维度"));
            return 0;
        }
        
        // 获取可编辑的域名列表
        List<AreaData> editableAreas = getEditableAreas(fileName, playerName, hasOp);
        
        if (editableAreas.isEmpty()) {
            source.sendMessage(Text.of("§c当前维度没有您可以编辑的域名"));
            return 0;
        }
        
        // 发送域名列表到客户端
        sendAreaListToClient(player, editableAreas, dimensionType);
        
        source.sendMessage(Text.of("§a已向您发送可编辑的域名列表，请在客户端选择要重新着色的域名"));
        return 1;
    }
    
    /**
     * 执行带参数的recolor指令（直接修改颜色）
     * @param context 指令上下文
     * @param areaName 域名名称
     * @param colorInput 颜色输入
     * @return 执行结果
     */
    public static int executeRecolorChange(CommandContext<ServerCommandSource> context, String areaName, String colorInput) {
        ServerCommandSource source = context.getSource();
        
        if (source.getPlayer() == null) {
            source.sendMessage(Text.of("§c此指令只能由玩家执行"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        String playerName = player.getName().getString();
        
        // 标准化颜色输入
        String normalizedColor = areahint.util.ColorUtil.normalizeColor(colorInput);
        if (!areahint.util.ColorUtil.isValidColor(normalizedColor)) {
            source.sendMessage(Text.of("§c无效的颜色: " + colorInput));
            source.sendMessage(Text.of("§7可用颜色: 白色, 红色, 粉红色, 橙色, 黄色, 棕色, 浅绿色, 深绿色, 浅蓝色, 深蓝色, 浅紫色, 紫色, 灰色, 黑色"));
            source.sendMessage(Text.of("§7或使用十六进制格式，如: #FF0000"));
            return 0;
        }
        
        // 获取玩家当前维度
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        
        // 处理重新着色请求
        handleRecolorRequest(player, areaName, normalizedColor, dimensionType);
        
        return 1;
    }
    
    /**
     * 获取玩家可编辑的域名列表
     */
    private static List<AreaData> getEditableAreas(String fileName, String playerName, boolean hasOp) {
        List<AreaData> editableAreas = new ArrayList<>();
        
        try {
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (areaFile == null || !areaFile.toFile().exists()) {
                return editableAreas;
            }
            
            List<AreaData> allAreas = FileManager.readAreaData(areaFile);
            
            for (AreaData area : allAreas) {
                // 管理员可以编辑所有域名，玩家只能编辑自己创建的域名
                if (hasOp || playerName.equals(area.getSignature())) {
                    editableAreas.add(area);
                }
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error("获取可编辑域名列表时发生错误", e);
        }
        
        return editableAreas;
    }
    
    /**
     * 发送域名列表到客户端
     */
    private static void sendAreaListToClient(ServerPlayerEntity player, List<AreaData> areas, String dimension) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("recolor_list");
            buf.writeString(dimension);
            buf.writeInt(areas.size());
            
            for (AreaData area : areas) {
                buf.writeString(area.getName());
                buf.writeString(area.getColor());
                buf.writeInt(area.getLevel());
                buf.writeString(area.getBaseName() != null ? area.getBaseName() : "");
            }
            
            ServerPlayNetworking.send(player, Packets.S2C_RECOLOR_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送域名列表到客户端时发生错误", e);
        }
    }
    
    /**
     * 处理客户端发送的重新着色请求
     */
    public static void handleRecolorRequest(ServerPlayerEntity player, String areaName, String newColor, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean hasOp = player.hasPermissionLevel(2);
            
            // 验证颜色格式
            if (!ColorUtil.isValidColor(newColor)) {
                sendRecolorResponse(player, false, "无效的颜色格式: " + newColor);
                return;
            }
            
            // 获取维度文件
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                sendRecolorResponse(player, false, "无效的维度: " + dimension);
                return;
            }
            
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (areaFile == null || !areaFile.toFile().exists()) {
                sendRecolorResponse(player, false, "维度文件不存在");
                return;
            }
            
            // 读取和更新域名数据
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            boolean found = false;
            String oldColor = "";
            
            for (AreaData area : areas) {
                if (area.getName().equals(areaName)) {
                    // 检查权限
                    if (!hasOp && !playerName.equals(area.getSignature())) {
                        sendRecolorResponse(player, false, "您没有权限修改域名 \"" + areaName + "\" 的颜色");
                        return;
                    }
                    
                    oldColor = area.getColor();
                    area.setColor(newColor);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                sendRecolorResponse(player, false, "未找到域名: " + areaName);
                return;
            }
            
            // 保存文件
            if (FileManager.writeAreaData(areaFile, areas)) {
                sendRecolorResponse(player, true, 
                    String.format("域名 \"%s\" 颜色已从 %s 更改为 %s", areaName, oldColor, newColor));
                
                // 重新加载并发送给所有客户端
                ServerNetworking.sendAllAreaDataToAll();
                
                Areashint.LOGGER.info("玩家 " + playerName + " 将域名 \"" + areaName + "\" 的颜色从 " + oldColor + " 更改为 " + newColor);
                
            } else {
                sendRecolorResponse(player, false, "保存域名数据时失败");
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error("处理重新着色请求时发生错误", e);
            sendRecolorResponse(player, false, "处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 发送重新着色响应
     */
    private static void sendRecolorResponse(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("recolor_response");
            buf.writeBoolean(success);
            buf.writeString(message);
            
            ServerPlayNetworking.send(player, Packets.S2C_RECOLOR_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送重新着色响应时发生错误", e);
        }
    }
    
    /**
     * 将维度ID转换为Packets期望的维度类型
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
} 