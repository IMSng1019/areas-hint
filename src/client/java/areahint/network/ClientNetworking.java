package areahint.network;

import areahint.Areashint;
import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.file.FileManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
                Path filePath = FileManager.getDimensionFile(fileName);
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
                
                // 解析命令
                String[] parts = command.split(":");
                if (parts.length < 2) {
                    return;
                }
                
                String type = parts[0];
                String action = parts[1];
                
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
                }
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理客户端命令时出错: " + e.getMessage());
            }
        });
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
} 