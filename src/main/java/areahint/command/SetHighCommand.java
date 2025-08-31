package areahint.command;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

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
     * 执行设置高度命令
     * @param context 命令上下文
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
        
        // 启动交互式高度设置流程
        startInteractiveHeightSetting(source, editableAreas);
        
        return 1;
    }
    
    /**
     * 启动交互式高度设置流程
     * @param source 命令源
     * @param editableAreas 可编辑的域名列表
     */
    private static void startInteractiveHeightSetting(ServerCommandSource source, List<AreaData> editableAreas) {
        try {
            // 发送域名列表到客户端，启动交互式流程
            List<String> areaNames = editableAreas.stream()
                    .map(AreaData::getName)
                    .toList();
            
            // 获取维度类型
            String dimension = source.getPlayer().getWorld().getRegistryKey().getValue().toString();
            String dimensionType = convertDimensionIdToType(dimension);
            
            // 发送数据包到客户端，格式与客户端期望的一致
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("sethigh_area_list"); // 命令类型
            buf.writeString(dimensionType); // 维度类型
            buf.writeInt(editableAreas.size()); // 域名数量
            
            // 写入每个域名的信息
            for (AreaData area : editableAreas) {
                buf.writeString(area.getName());
                
                AreaData.AltitudeData altitude = area.getAltitude();
                boolean hasAltitude = altitude != null && (altitude.getMax() != null || altitude.getMin() != null);
                buf.writeBoolean(hasAltitude);
                
                if (hasAltitude) {
                    buf.writeBoolean(altitude.getMax() != null);
                    if (altitude.getMax() != null) {
                        buf.writeDouble(altitude.getMax());
                    }
                    buf.writeBoolean(altitude.getMin() != null);
                    if (altitude.getMin() != null) {
                        buf.writeDouble(altitude.getMin());
                    }
                }
            }
            
            ServerPlayNetworking.send(source.getPlayer(), Packets.S2C_SETHIGH_AREA_LIST, buf);
            
            source.sendMessage(Text.of("§a已向您发送可修改高度的域名列表，请在客户端选择要设置高度的域名"));
            
        } catch (Exception e) {
            Areashint.LOGGER.error("启动交互式高度设置流程时发生错误", e);
            source.sendMessage(Text.of("§c启动高度设置流程时发生错误"));
        }
    }
    
    /**
     * 执行设置指定域名高度的命令
     * @param context 命令上下文
     * @param areaName 域名名称
     * @return 执行结果
     */
    public static int executeSetHighWithArea(CommandContext<ServerCommandSource> context, String areaName) {
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
        
        // 查找指定的域名
        AreaData targetArea = editableAreas.stream()
                .filter(area -> area.getName().equals(areaName))
                .findFirst()
                .orElse(null);
        
        if (targetArea == null) {
            source.sendMessage(Text.of("§c域名 '" + areaName + "' 不存在或您没有权限修改其高度"));
            // 启动交互式流程，让用户选择其他域名
            startInteractiveHeightSetting(source, editableAreas);
            return 0;
        }
        
        // 直接启动指定域名的交互式高度设置流程
        startInteractiveHeightSettingForSpecificArea(source, targetArea);
        
        return 1;
    }
    
    /**
     * 启动指定域名的交互式高度设置流程
     * @param source 命令源
     * @param targetArea 目标域名
     */
    private static void startInteractiveHeightSettingForSpecificArea(ServerCommandSource source, AreaData targetArea) {
        try {
            // 发送指定域名的数据到客户端
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(targetArea.getName());
            
            // 写入当前高度信息
            AreaData.AltitudeData altitude = targetArea.getAltitude();
            boolean hasAltitude = altitude != null && (altitude.getMax() != null || altitude.getMin() != null);
            buf.writeBoolean(hasAltitude);
            
            if (hasAltitude) {
                buf.writeBoolean(altitude.getMax() != null);
                if (altitude.getMax() != null) {
                    buf.writeDouble(altitude.getMax());
                }
                buf.writeBoolean(altitude.getMin() != null);
                if (altitude.getMin() != null) {
                    buf.writeDouble(altitude.getMin());
                }
            }
            
            ServerPlayNetworking.send(source.getPlayer(), Packets.S2C_SETHIGH_AREA_SELECTION, buf);
            
            source.sendMessage(Text.of("§a已启动域名 §f" + targetArea.getName() + " §a的高度设置流程"));
            
        } catch (Exception e) {
            Areashint.LOGGER.error("启动指定域名高度设置流程时发生错误", e);
            source.sendMessage(Text.of("§c启动高度设置流程时发生错误"));
        }
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
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
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
     * 直接在聊天栏显示可修改高度的域名列表
     * @param source 命令源
     * @param editableAreas 可编辑的域名列表
     * @param isAdmin 是否为管理员
     */
    private static void displayEditableAreasInChat(ServerCommandSource source, List<AreaData> editableAreas, boolean isAdmin) {
        if (editableAreas.isEmpty()) {
            source.sendMessage(Text.literal("§c当前维度没有您可以修改高度的域名"));
            return;
        }

        MutableText message = Text.literal("§a您可以修改以下域名的高度：\n");
        for (AreaData area : editableAreas) {
            message.append(Text.literal("§7- §f" + area.getName() + "§7: " + getHeightDisplayString(area.getAltitude()) + "\n"));
        }
        source.sendMessage(message);
    }
    
    /**
     * 获取高度的显示字符串
     * @param altitude 高度数据
     * @return 显示字符串
     */
    private static String getHeightDisplayString(AreaData.AltitudeData altitude) {
        if (altitude == null) {
            return "§7无限制";
        }
        
        StringBuilder sb = new StringBuilder();
        if (altitude.getMax() != null) {
            sb.append("§c最大: ").append(altitude.getMax());
        }
        if (altitude.getMin() != null) {
            if (sb.length() > 0) {
                sb.append("§7, ");
            }
            sb.append("§9最小: ").append(altitude.getMin());
        }
        
        return sb.length() > 0 ? sb.toString() : "§7无限制";
    }
    
    /**
     * 转换维度ID为类型
     * @param dimensionId 维度ID
     * @return 维度类型
     */
    private static String convertDimensionIdToType(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return Packets.DIMENSION_OVERWORLD;
        } else if (dimensionId.contains("nether")) {
            return Packets.DIMENSION_NETHER;
        } else if (dimensionId.contains("end")) {
            return Packets.DIMENSION_END;
        }
        return Packets.DIMENSION_OVERWORLD; // 默认返回主世界
    }
    
    /**
     * 处理高度设置请求
     * @param player 玩家
     * @param areaName 域名名称
     * @param hasCustomHeight 是否使用自定义高度
     * @param maxHeight 最大高度
     * @param minHeight 最小高度
     */
    public static void handleHeightRequest(ServerPlayerEntity player, String areaName, 
                                         boolean hasCustomHeight, Double maxHeight, Double minHeight) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);
            
            // 获取玩家当前维度
            String dimension = player.getWorld().getRegistryKey().getValue().toString();
            String dimensionType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimensionType);
            
            if (fileName == null) {
                sendResponse(player, false, "无法识别当前维度");
                return;
            }
            
            // 获取可修改高度的域名列表
            List<AreaData> editableAreas = getHeightEditableAreas(fileName, playerName, isAdmin);
            
            // 查找指定的域名
            AreaData targetArea = editableAreas.stream()
                    .filter(area -> area.getName().equals(areaName))
                    .findFirst()
                    .orElse(null);
            
            if (targetArea == null) {
                sendResponse(player, false, "域名 '" + areaName + "' 不存在或您没有权限修改其高度");
                return;
            }
            
            // 验证高度数据
            if (hasCustomHeight) {
                if (maxHeight != null && (maxHeight < -64 || maxHeight > 320)) {
                    sendResponse(player, false, "最高高度超出合理范围 [-64, 320]");
                    return;
                }
                if (minHeight != null && (minHeight < -64 || minHeight > 320)) {
                    sendResponse(player, false, "最低高度超出合理范围 [-64, 320]");
                    return;
                }
                if (maxHeight != null && minHeight != null && maxHeight < minHeight) {
                    sendResponse(player, false, "最高高度不能小于最低高度");
                    return;
                }
            }
            
            // 更新域名高度数据
            AreaData.AltitudeData newAltitude = hasCustomHeight ? 
                    new AreaData.AltitudeData(maxHeight, minHeight) : null;
            
            targetArea.setAltitude(newAltitude);
            
            // 保存到文件
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            List<AreaData> allAreas = FileManager.readAreaData(areaFile);
            
            // 更新指定域名
            for (int i = 0; i < allAreas.size(); i++) {
                if (allAreas.get(i).getName().equals(areaName)) {
                    allAreas.set(i, targetArea);
                    break;
                }
            }
            
            FileManager.writeAreaData(areaFile, allAreas);
            
            // 重新分发数据给所有玩家（相当于执行一次reload指令）
            // 使用 sendAllAreaDataToAll() 确保所有玩家都能收到更新，无论他们在哪个维度
            areahint.network.ServerNetworking.sendAllAreaDataToAll();
            
            // 发送成功响应
            String message = hasCustomHeight ? 
                    "域名 '" + areaName + "' 的高度已设置为 " + 
                    (maxHeight != null ? maxHeight : "无限制") + " ~ " + 
                    (minHeight != null ? minHeight : "无限制") :
                    "域名 '" + areaName + "' 的高度限制已移除";
            
            sendResponse(player, true, message);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("处理高度设置请求时发生错误", e);
            sendResponse(player, false, "处理高度设置请求时发生错误");
        }
    }
    
    /**
     * 执行自定义高度命令
     * @param context 命令上下文
     * @param areaName 域名名称
     * @return 执行结果
     */
    public static int executeSetHighCustom(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendMessage(Text.of("§c此指令只能由玩家执行"));
            return 0;
        }
        
        // 发送命令到客户端，启动自定义高度输入流程
        sendClientCommand(source, "areahint:sethigh_custom", areaName);
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行不限制高度命令
     * @param context 命令上下文
     * @param areaName 域名名称
     * @return 执行结果
     */
    public static int executeSetHighUnlimited(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendMessage(Text.of("§c此指令只能由玩家执行"));
            return 0;
        }
        
        // 直接处理不限制高度请求
        handleHeightRequest(source.getPlayer(), areaName, false, null, null);
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行取消高度设置命令
     * @param context 命令上下文
     * @return 执行结果
     */
    public static int executeSetHighCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() == null) {
            source.sendMessage(Text.of("§c此指令只能由玩家执行"));
            return 0;
        }
        
        source.sendMessage(Text.of("§c已取消高度设置流程"));
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 发送响应到客户端
     * @param player 玩家
     * @param success 是否成功
     * @param message 消息
     */
    private static void sendResponse(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(success);
            buf.writeString(message);
            
            ServerPlayNetworking.send(player, Packets.S2C_SETHIGH_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送响应时发生错误", e);
        }
    }
    
    /**
     * 发送客户端命令
     * @param source 命令源
     * @param command 命令
     * @param args 参数
     */
    private static void sendClientCommand(ServerCommandSource source, String command, String... args) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(command);
            buf.writeInt(args.length);
            for (String arg : args) {
                buf.writeString(arg);
            }
            
            ServerPlayNetworking.send(source.getPlayer(), new Identifier(Packets.S2C_CLIENT_COMMAND), buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送客户端命令时发生错误", e);
        }
    }
} 