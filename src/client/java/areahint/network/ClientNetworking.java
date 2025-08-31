package areahint.network;

import areahint.Areashint;
import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.file.FileManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端网络处理类
 * 处理客户端接收服务端数据
 */
public class ClientNetworking {
    /**
     * 初始化客户端网络处理
     */
    public static void init() {
        AreashintClient.LOGGER.info("初始化客户端网络处理");
        
        // 注册区域数据接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_AREA_DATA),
                ClientNetworking::handleAreaData
        );
        
        // 注册客户端命令接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_CLIENT_COMMAND),
                ClientNetworking::handleClientCommand
        );
        
        // 注册调试命令接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_DEBUG_COMMAND),
                ClientNetworking::handleDebugCommand
        );
        
        // 注册recolor响应接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_RECOLOR_RESPONSE,
                ClientNetworking::handleRecolorResponse
        );
        
        // 注册rename响应接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_RENAME_RESPONSE,
                ClientNetworking::handleRenameResponse
        );
        
        // 注册SetHigh相关的网络处理器
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_SETHIGH_AREA_LIST,
                ClientNetworking::handleSetHighAreaList
        );
        
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_SETHIGH_AREA_SELECTION,
                ClientNetworking::handleSetHighAreaSelection
        );
        
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_SETHIGH_RESPONSE,
                ClientNetworking::handleSetHighResponse
        );
        
        // 注册客户端命令处理器
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_CLIENT_COMMAND),
                ClientNetworking::handleClientCommand
        );
    }
    
    /**
     * 处理接收到的区域数据
     * @param client Minecraft客户端实例
     * @param handler 网络处理器
     * @param buf 数据包缓冲区
     * @param responseSender 响应发送器
     */
    private static void handleAreaData(MinecraftClient client, 
                                      ClientPlayNetworkHandler handler,
                                      PacketByteBuf buf, 
                                      PacketSender responseSender) {
        // 读取维度名称和文件内容
        String dimensionName = buf.readString();
        String fileContent = buf.readString();
        
        // 确保在主线程中处理
        client.execute(() -> {
            try {
                // 确定文件名
                String fileName = Packets.getFileNameForDimension(dimensionName);
                if (fileName == null) {
                    AreashintClient.LOGGER.warn("接收到未知维度的区域数据: " + dimensionName);
                    return;
                }
                
                // 获取文件路径
                Path filePath = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
                AreashintClient.LOGGER.info("[调试] 客户端保存区域数据到文件: " + filePath.toAbsolutePath());
                
                // 确保目录存在
                FileManager.checkFolderExist();
                
                // 写入文件
                Files.writeString(filePath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                
                AreashintClient.LOGGER.info("已接收并保存 " + dimensionName + " 的区域数据");
                AreashintClient.LOGGER.info("[调试] 区域数据内容长度: " + fileContent.length() + " 字节");
                if (fileContent.length() < 100) {
                    AreashintClient.LOGGER.info("[调试] 区域数据内容预览: " + fileContent);
                } else {
                    AreashintClient.LOGGER.info("[调试] 区域数据内容预览: " + fileContent.substring(0, 100) + "...");
                }
                
                // 如果当前在该维度中，则重新加载区域数据
                if (client.world != null && 
                        dimensionName.equals(Packets.convertDimensionPathToType(client.world.getDimensionKey().getValue().getPath()))) {
                    AreashintClient.LOGGER.info("[调试] 重新加载当前维度的区域数据: " + fileName);
                    AreashintClient.getAreaDetector().loadAreaData(fileName);
                }
                
            } catch (IOException e) {
                AreashintClient.LOGGER.error("保存接收到的区域数据时出错: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理接收到的客户端命令
     * @param client Minecraft客户端实例
     * @param handler 网络处理器
     * @param buf 数据包缓冲区
     * @param responseSender 响应发送器
     */
    private static void handleClientCommand(MinecraftClient client, 
                                          ClientPlayNetworkHandler handler,
                                          PacketByteBuf buf, 
                                          PacketSender responseSender) {
        // 读取命令
        String command = buf.readString();
        
        // 确保在主线程中处理
        client.execute(() -> {
            try {
                AreashintClient.LOGGER.info("接收到服务端命令: " + command);
                
                // 解析命令（只在第一个冒号处分割，保留参数）
                int firstColonIndex = command.indexOf(":");
                if (firstColonIndex == -1) {
                    return;
                }
                
                String type = command.substring(0, firstColonIndex);
                String action = command.substring(firstColonIndex + 1);
                
                // 根据命令类型处理
                if (type.equals("areahint")) {
                    // 处理reload命令
                    if (action.equals("reload")) {
                        AreashintClient.reload();
                    } 
                    // 处理frequency命令
                    else if (action.startsWith("frequency")) {
                        if (action.equals("frequency_info")) {
                            displayFrequencyInfo(client);
                        } else {
                            try {
                                String[] frequencyParts = action.split(" ");
                                if (frequencyParts.length >= 2) {
                                    int value = Integer.parseInt(frequencyParts[1]);
                                    ClientConfig.setFrequency(value);
                                    AreashintClient.reload();
                                }
                            } catch (NumberFormatException e) {
                                AreashintClient.LOGGER.error("解析频率值时出错: " + e.getMessage());
                            }
                        }
                    }
                    // 处理subtitlerender命令
                    else if (action.startsWith("subtitlerender")) {
                        if (action.equals("subtitlerender_info")) {
                            displaySubtitleRenderInfo(client);
                        } else {
                            String[] renderParts = action.split(" ");
                            if (renderParts.length >= 2) {
                                ClientConfig.setSubtitleRender(renderParts[1]);
                                AreashintClient.reload();
                            }
                        }
                    }
                    // 处理subtitlestyle命令
                    else if (action.startsWith("subtitlestyle")) {
                        if (action.equals("subtitlestyle_info")) {
                            displaySubtitleStyleInfo(client);
                        } else {
                            String[] styleParts = action.split(" ");
                            if (styleParts.length >= 2) {
                                ClientConfig.setSubtitleStyle(styleParts[1]);
                                AreashintClient.reload();
                            }
                        }
                    }
                    // 处理EasyAdd命令
                    else if (action.startsWith("easyadd")) {
                        handleEasyAddCommand(action);
                    }
                    // 处理模组开关命令
                    else if (action.equals("on") || action.equals("off")) {
                        areahint.command.ModToggleCommand.handleToggleCommand(action);
                    }
                    // 处理SetHigh命令
                    else if (action.equals("sethigh_start")) {
                        AreashintClient.LOGGER.info("执行sethigh_start");
                        // SetHigh命令由服务端直接处理，客户端只需要等待服务端发送域名列表
                        if (client.player != null) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§a正在获取可修改高度的域名列表..."), false);
                        }
                    }
                    // 处理SetHigh自定义高度命令
                    else if (action.equals("sethigh_custom")) {
                        AreashintClient.LOGGER.info("执行sethigh_custom");
                        // 读取域名名称参数
                        int argCount = buf.readInt();
                        if (argCount > 0) {
                            String areaName = buf.readString();
                            areahint.command.SetHighClientCommand.startCustomHeightInput(areaName);
                        }
                    }
                }
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理客户端命令时出错: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理EasyAdd命令
     * @param action 命令动作
     */
    private static void handleEasyAddCommand(String action) {
        try {
            AreashintClient.LOGGER.info("处理EasyAdd命令: " + action);
            areahint.easyadd.EasyAddManager manager = areahint.easyadd.EasyAddManager.getInstance();
            
            if (action.equals("easyadd_start")) {
                AreashintClient.LOGGER.info("执行easyadd_start");
                manager.startEasyAdd();
            } else if (action.equals("expandarea_start")) {
                AreashintClient.LOGGER.info("执行expandarea_start");
                areahint.expandarea.ExpandAreaManager.getInstance().startExpandArea();
            } else if (action.equals("expandarea_continue")) {
                AreashintClient.LOGGER.info("执行expandarea_continue");
                areahint.expandarea.ExpandAreaManager.getInstance().continueRecording();
            } else if (action.equals("expandarea_save")) {
                AreashintClient.LOGGER.info("执行expandarea_save");
                areahint.expandarea.ExpandAreaManager.getInstance().finishAndSave();
            } else if (action.equals("shrinkarea_start")) {
                AreashintClient.LOGGER.info("执行shrinkarea_start");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().start();
            } else if (action.equals("shrinkarea_continue")) {
                AreashintClient.LOGGER.info("执行shrinkarea_continue");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().continueRecording();
            } else if (action.equals("shrinkarea_save")) {
                AreashintClient.LOGGER.info("执行shrinkarea_save");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().finishAndSave();
            } else if (action.equals("easyadd_cancel")) {
                AreashintClient.LOGGER.info("执行easyadd_cancel");
                manager.cancelEasyAdd();
            } else if (action.startsWith("easyadd_level:")) {
                String levelStr = action.substring("easyadd_level:".length());
                int level = Integer.parseInt(levelStr);
                AreashintClient.LOGGER.info("执行easyadd_level: " + level);
                manager.handleLevelInput(level);
            } else if (action.startsWith("easyadd_base:")) {
                String baseName = action.substring("easyadd_base:".length());
                AreashintClient.LOGGER.info("执行easyadd_base: " + baseName);
                manager.handleBaseSelection(baseName);
            } else if (action.equals("easyadd_continue")) {
                AreashintClient.LOGGER.info("执行easyadd_continue");
                // 继续记录的逻辑在按键处理中，这里不需要做特殊处理
            } else if (action.equals("easyadd_finish")) {
                AreashintClient.LOGGER.info("执行easyadd_finish");
                manager.finishPointRecording();
                    } else if (action.equals("easyadd_save")) {
            AreashintClient.LOGGER.info("执行easyadd_save");
            manager.confirmSave();
        } else if (action.equals("easyadd_altitude_auto")) {
            AreashintClient.LOGGER.info("执行easyadd_altitude_auto");
            areahint.easyadd.EasyAddAltitudeManager.handleAltitudeTypeSelection(
                areahint.easyadd.EasyAddAltitudeManager.AltitudeType.AUTOMATIC);
        } else if (action.equals("easyadd_altitude_custom")) {
            AreashintClient.LOGGER.info("执行easyadd_altitude_custom");
            areahint.easyadd.EasyAddAltitudeManager.handleAltitudeTypeSelection(
                areahint.easyadd.EasyAddAltitudeManager.AltitudeType.CUSTOM);
        } else if (action.equals("easyadd_altitude_unlimited")) {
            AreashintClient.LOGGER.info("执行easyadd_altitude_unlimited");
            areahint.easyadd.EasyAddAltitudeManager.handleAltitudeTypeSelection(
                areahint.easyadd.EasyAddAltitudeManager.AltitudeType.UNLIMITED);
        } else if (action.startsWith("easyadd_color:")) {
            String colorHex = action.substring("easyadd_color:".length());
            try {
                AreashintClient.LOGGER.info("执行easyadd_color: " + colorHex);
                manager.handleColorSelection(colorHex);
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理颜色选择时出错: " + e.getMessage());
            }
        } else {
            AreashintClient.LOGGER.warn("未知的EasyAdd命令: " + action);
        }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("处理EasyAdd命令时出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 显示当前频率信息
     * @param client Minecraft客户端实例
     */
    private static void displayFrequencyInfo(MinecraftClient client) {
        if (client.player != null) {
            int frequency = ClientConfig.getFrequency();
            client.player.sendMessage(net.minecraft.text.Text.of("§a当前检测频率为: §6" + frequency + "§a Hz"));
        }
    }
    
    /**
     * 显示当前渲染方式信息
     * @param client Minecraft客户端实例
     */
    private static void displaySubtitleRenderInfo(MinecraftClient client) {
        if (client.player != null) {
            String renderMode = ClientConfig.getSubtitleRender();
            client.player.sendMessage(net.minecraft.text.Text.of("§a当前字幕渲染方式为: §6" + renderMode));
        }
    }
    
    /**
     * 显示当前字幕样式信息
     * @param client Minecraft客户端实例
     */
    private static void displaySubtitleStyleInfo(MinecraftClient client) {
        if (client.player != null) {
            String style = ClientConfig.getSubtitleStyle();
            client.player.sendMessage(net.minecraft.text.Text.of("§a当前字幕样式为: §6" + style));
        }
    }
    
    /**
     * 处理接收到的调试命令
     * @param client Minecraft客户端实例
     * @param handler 网络处理器
     * @param buf 数据包缓冲区
     * @param responseSender 响应发送器
     */
    private static void handleDebugCommand(MinecraftClient client, 
                                          ClientPlayNetworkHandler handler,
                                          PacketByteBuf buf, 
                                          PacketSender responseSender) {
        // 读取调试状态
        boolean enabled = buf.readBoolean();
        
        // 确保在主线程中处理
        client.execute(() -> {
            try {
                AreashintClient.LOGGER.info("接收到服务端调试命令: " + (enabled ? "启用" : "禁用"));
                
                // 设置调试状态
                if (enabled) {
                    areahint.debug.ClientDebugManager.enableDebug();
                } else {
                    areahint.debug.ClientDebugManager.disableDebug();
                }
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理调试命令时出错: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理recolor响应
     */
    private static void handleRecolorResponse(MinecraftClient client, ClientPlayNetworkHandler handler, 
                                            PacketByteBuf buf, PacketSender responseSender) {
        try {
            String action = buf.readString();
            
            if ("recolor_list".equals(action)) {
                // 处理域名列表响应
                String dimension = buf.readString();
                int count = buf.readInt();
                
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(net.minecraft.text.Text.of("§a可编辑的域名列表:"), false);
                        
                        for (int i = 0; i < count; i++) {
                            try {
                                String areaName = buf.readString();
                                String currentColor = buf.readString();
                                int level = buf.readInt();
                                String baseName = buf.readString();
                                
                                client.player.sendMessage(net.minecraft.text.Text.of(
                                    String.format("§7%d. §r%s §7(等级%d) §8- 当前颜色: %s", 
                                        i + 1, areaName, level, currentColor)
                                ), false);
                            } catch (Exception e) {
                                AreashintClient.LOGGER.error("读取域名信息时出错", e);
                            }
                        }
                        
                        client.player.sendMessage(net.minecraft.text.Text.of(
                            "§a请使用 §e/areahint recolor <域名> <颜色> §a来修改颜色"
                        ), false);
                        client.player.sendMessage(net.minecraft.text.Text.of(
                            "§7可用颜色: 白色, 红色, 粉红色, 橙色, 黄色, 棕色, 浅绿色, 深绿色, 浅蓝色, 深蓝色, 浅紫色, 紫色, 灰色, 黑色, 或自定义十六进制码(如 #FF0000)"
                        ), false);
                    }
                });
                
            } else if ("recolor_interactive".equals(action)) {
                // 处理交互式recolor界面
                String dimension = buf.readString();
                int count = buf.readInt();
                
                client.execute(() -> {
                    if (client.player != null) {
                        showInteractiveRecolorScreen(count, buf);
                    }
                });
            } else if ("recolor_response".equals(action)) {
                // 处理修改颜色响应
                boolean success = buf.readBoolean();
                String message = buf.readString();
                
                client.execute(() -> {
                    if (client.player != null) {
                        if (success) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§a" + message), false);
                        } else {
                            client.player.sendMessage(net.minecraft.text.Text.of("§c" + message), false);
                        }
                    }
                });
            }
            
        } catch (Exception e) {
            AreashintClient.LOGGER.error("处理recolor响应时出错: " + e.getMessage());
        }
    }
    
    /**
     * 处理rename响应
     */
    private static void handleRenameResponse(MinecraftClient client, ClientPlayNetworkHandler handler,
                                           PacketByteBuf buf, PacketSender responseSender) {
        try {
            String action = buf.readString();
            
            if ("rename_list".equals(action)) {
                // 处理可重命名域名列表响应
                int count = buf.readInt();
                String errorMessage = buf.readString();
                
                client.execute(() -> {
                    if (client.player != null) {
                        if (count == 0) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§c" + errorMessage), false);
                        } else {
                            client.player.sendMessage(net.minecraft.text.Text.of("§a可重命名的域名列表:"), false);
                            
                            for (int i = 0; i < count; i++) {
                                try {
                                    String areaName = buf.readString();
                                    String signature = buf.readString();
                                    
                                    client.player.sendMessage(net.minecraft.text.Text.of(
                                        String.format("§7%d. §f%s §8(创建者: %s)", 
                                            i + 1, areaName, signature)
                                    ), false);
                                } catch (Exception e) {
                                    AreashintClient.LOGGER.error("读取域名信息时出错", e);
                                }
                            }
                            
                            client.player.sendMessage(net.minecraft.text.Text.of(
                                "§a请使用 §e/areahint renamearea <域名> <新名称> §a来重命名域名"
                            ), false);
                        }
                    }
                });
                
            } else if ("rename_confirm".equals(action)) {
                // 处理重命名确认请求
                String oldName = buf.readString();
                String newName = buf.readString();
                
                client.execute(() -> {
                    if (client.player != null) {
                        // 显示确认对话框
                        client.player.sendMessage(net.minecraft.text.Text.of("§6===== 域名重命名确认 ====="), false);
                        client.player.sendMessage(net.minecraft.text.Text.of("§f您确认将域名重命名吗？"), false);
                        client.player.sendMessage(net.minecraft.text.Text.of("§7原域名: §f" + oldName), false);
                        client.player.sendMessage(net.minecraft.text.Text.of("§7新域名: §f" + newName), false);
                        client.player.sendMessage(net.minecraft.text.Text.of(""), false);
                        client.player.sendMessage(net.minecraft.text.Text.of("§a点击确认: §e[是] §7或输入 §e/areahint renamearea confirm"), false);
                        client.player.sendMessage(net.minecraft.text.Text.of("§c点击取消: §e[否] §7或输入 §e/areahint renamearea cancel"), false);
                        
                        // 这里可以实现可点击的确认按钮（需要额外的网络包处理）
                        // 为简化实现，暂时使用命令确认方式
                        
                        // 保存待确认的重命名信息（可以使用静态变量或客户端状态管理）
                        storePendingRename(oldName, newName);
                    }
                });
                
            } else if ("rename_response".equals(action)) {
                // 处理重命名结果响应
                boolean success = buf.readBoolean();
                String message = buf.readString();
                
                client.execute(() -> {
                    if (client.player != null) {
                        if (success) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§a" + message), false);
                        } else {
                            client.player.sendMessage(net.minecraft.text.Text.of("§c" + message), false);
                        }
                        // 清除待确认的重命名信息
                        clearPendingRename();
                    }
                });
                
            } else if ("rename_confirm_action".equals(action)) {
                // 处理确认/取消操作
                boolean confirm = buf.readBoolean();
                
                client.execute(() -> {
                    String[] pendingRename = getPendingRename();
                    if (pendingRename == null) {
                        if (client.player != null) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§c没有待确认的重命名操作"), false);
                        }
                        return;
                    }
                    
                    if (confirm) {
                        // 确认重命名，发送重命名请求到服务器
                        String oldName = pendingRename[0];
                        String newName = pendingRename[1];
                        String dimension = "minecraft:" + client.world.getRegistryKey().getValue().getPath();
                        
                        sendRenameRequest(oldName, newName, dimension);
                        
                        if (client.player != null) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§a正在处理重命名请求..."), false);
                        }
                    } else {
                        // 取消重命名
                        clearPendingRename();
                        if (client.player != null) {
                            client.player.sendMessage(net.minecraft.text.Text.of("§c重命名操作已取消"), false);
                        }
                    }
                });
            }
            
        } catch (Exception e) {
            AreashintClient.LOGGER.error("处理rename响应时出错: " + e.getMessage());
        }
    }
    
    // 静态变量保存待确认的重命名信息
    private static String pendingRenameOldName = null;
    private static String pendingRenameNewName = null;
    
    /**
     * 保存待确认的重命名信息
     */
    private static void storePendingRename(String oldName, String newName) {
        pendingRenameOldName = oldName;
        pendingRenameNewName = newName;
    }
    
    /**
     * 清除待确认的重命名信息
     */
    private static void clearPendingRename() {
        pendingRenameOldName = null;
        pendingRenameNewName = null;
    }
    
    /**
     * 获取待确认的重命名信息
     */
    public static String[] getPendingRename() {
        if (pendingRenameOldName != null && pendingRenameNewName != null) {
            return new String[]{pendingRenameOldName, pendingRenameNewName};
        }
        return null;
    }
    
    /**
     * 发送重命名请求到服务器
     * @param oldName 原域名
     * @param newName 新域名
     * @param dimension 维度ID
     */
    private static void sendRenameRequest(String oldName, String newName, String dimension) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(oldName);
        buf.writeString(newName);
        buf.writeString(dimension);
        
        ClientPlayNetworking.send(Packets.C2S_RENAME_REQUEST, buf);
    }

    /**
     * 显示交互式recolor界面
     */
    private static void showInteractiveRecolorScreen(int count, PacketByteBuf buf) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(net.minecraft.text.Text.of("§6=== 域名颜色修改 ==="), false);
        client.player.sendMessage(net.minecraft.text.Text.of("§a请选择要修改颜色的域名："), false);
        client.player.sendMessage(net.minecraft.text.Text.of(""), false);
        
        for (int i = 0; i < count; i++) {
            try {
                String areaName = buf.readString();
                String currentColor = buf.readString();
                int level = buf.readInt();
                String baseName = buf.readString();
                
                // 创建域名选择按钮
                net.minecraft.text.MutableText areaButton = net.minecraft.text.Text.literal(
                    String.format("§6[%s] §7(等级%d) §8当前颜色: %s", areaName, level, currentColor)
                ).setStyle(net.minecraft.text.Style.EMPTY
                    .withClickEvent(new net.minecraft.text.ClickEvent(
                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND, 
                        "/areahint recolor " + areaName))
                    .withHoverEvent(new net.minecraft.text.HoverEvent(
                        net.minecraft.text.HoverEvent.Action.SHOW_TEXT, 
                        net.minecraft.text.Text.of("选择 " + areaName + " 进行颜色修改"))));
                
                client.player.sendMessage(areaButton, false);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("读取域名信息时出错", e);
            }
        }
        
        client.player.sendMessage(net.minecraft.text.Text.of(""), false);
        client.player.sendMessage(net.minecraft.text.Text.of(
            "§7点击域名按钮后，使用 §e/areahint recolor <域名> <颜色> §7来修改颜色"
        ), false);
        client.player.sendMessage(net.minecraft.text.Text.of(
            "§7可用颜色: 白色, 红色, 粉红色, 橙色, 黄色, 棕色, 浅绿色, 深绿色, 浅蓝色, 深蓝色, 浅紫色, 紫色, 灰色, 黑色"
        ), false);
        client.player.sendMessage(net.minecraft.text.Text.of(
            "§7或使用十六进制格式，如: #FF0000"
        ), false);
    }
    
    /**
     * 处理SetHigh域名列表
     */
    private static void handleSetHighAreaList(MinecraftClient client, 
                                            ClientPlayNetworkHandler handler,
                                            PacketByteBuf buf, 
                                            PacketSender responseSender) {
        client.execute(() -> {
            try {
                // 读取域名列表数据
                List<String> areaNames = new ArrayList<>();
                List<Boolean> hasAltitudeList = new ArrayList<>();
                List<Double> maxHeightList = new ArrayList<>();
                List<Double> minHeightList = new ArrayList<>();
                
                // 读取服务端发送的数据格式
                String commandType = buf.readString(); // "sethigh_area_list"
                String dimensionType = buf.readString(); // 维度类型
                int areaCount = buf.readInt(); // 域名数量
                
                AreashintClient.LOGGER.info("接收到SetHigh域名列表: 命令类型={}, 维度={}, 域名数量={}", 
                    commandType, dimensionType, areaCount);
                
                // 读取每个域名的信息
                for (int i = 0; i < areaCount; i++) {
                    String areaName = buf.readString();
                    areaNames.add(areaName);
                    
                    boolean hasAltitude = buf.readBoolean();
                    hasAltitudeList.add(hasAltitude);
                    
                    if (hasAltitude) {
                        boolean hasMax = buf.readBoolean();
                        Double maxHeight = hasMax ? buf.readDouble() : null;
                        maxHeightList.add(maxHeight);
                        
                        boolean hasMin = buf.readBoolean();
                        Double minHeight = hasMin ? buf.readDouble() : null;
                        minHeightList.add(minHeight);
                    } else {
                        maxHeightList.add(null);
                        minHeightList.add(null);
                    }
                }
                
                AreashintClient.LOGGER.info("成功解析域名列表: {}", areaNames);
                
                // 调用SetHighClientCommand处理
                areahint.command.SetHighClientCommand.handleAreaList(areaNames, dimensionType);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理SetHigh域名列表时出错: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 处理SetHigh域名选择
     */
    private static void handleSetHighAreaSelection(MinecraftClient client, 
                                                 ClientPlayNetworkHandler handler,
                                                 PacketByteBuf buf, 
                                                 PacketSender responseSender) {
        client.execute(() -> {
            try {
                // 读取指定域名的数据
                String areaName = buf.readString();
                
                // 读取当前高度信息
                boolean hasAltitude = buf.readBoolean();
                Double maxHeight = null;
                Double minHeight = null;
                
                if (hasAltitude) {
                    boolean hasMax = buf.readBoolean();
                    if (hasMax) {
                        maxHeight = buf.readDouble();
                    }
                    boolean hasMin = buf.readBoolean();
                    if (hasMin) {
                        minHeight = buf.readDouble();
                    }
                }
                
                AreashintClient.LOGGER.info("接收到SetHigh域名选择: 域名={}, 有高度限制={}, 最大高度={}, 最小高度={}", 
                    areaName, hasAltitude, maxHeight, minHeight);
                
                // 调用SetHighClientCommand处理
                areahint.command.SetHighClientCommand.handleAreaSelection(areaName, hasAltitude, maxHeight, minHeight);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理SetHigh域名选择时出错: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 处理SetHigh响应
     */
    private static void handleSetHighResponse(MinecraftClient client, 
                                            ClientPlayNetworkHandler handler,
                                            PacketByteBuf buf, 
                                            PacketSender responseSender) {
        client.execute(() -> {
            try {
                boolean success = buf.readBoolean();
                String message = buf.readString();
                areahint.command.SetHighClientCommand.handleServerResponse(success, message);
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理SetHigh响应时出错: " + e.getMessage());
            }
        });
    }
    
    /**
     * 发送SetHigh高度设置请求到服务器
     * @param areaName 域名名称
     * @param hasAltitude 是否有高度限制
     * @param maxHeight 最大高度
     * @param minHeight 最小高度
     */
    public static void sendSetHighRequest(String areaName, boolean hasAltitude, 
                                        Double maxHeight, Double minHeight) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(areaName);
            buf.writeBoolean(hasAltitude);
            
            if (hasAltitude) {
                buf.writeBoolean(maxHeight != null);
                if (maxHeight != null) {
                    buf.writeDouble(maxHeight);
                }
                buf.writeBoolean(minHeight != null);
                if (minHeight != null) {
                    buf.writeDouble(minHeight);
                }
            }
            
            ClientPlayNetworking.send(Packets.C2S_SETHIGH_REQUEST, buf);
            
            AreashintClient.LOGGER.info("发送SetHigh请求: 域名={}, 有高度限制={}, 最大高度={}, 最小高度={}", 
                areaName, hasAltitude, maxHeight, minHeight);
                
        } catch (Exception e) {
            AreashintClient.LOGGER.error("发送SetHigh请求时发生错误: " + e.getMessage(), e);
        }
    }
} 