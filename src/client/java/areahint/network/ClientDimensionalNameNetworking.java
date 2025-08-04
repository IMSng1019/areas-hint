package areahint.network;

import areahint.AreashintClient;
import areahint.dimensional.ClientDimensionalNameManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * 客户端维度域名网络处理
 * 负责接收服务端发送的维度域名配置
 */
public class ClientDimensionalNameNetworking {
    
    /**
     * 初始化客户端网络处理
     */
    public static void init() {
        // 注册维度域名接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
            new Identifier("areashint:dimensional_names"),
            ClientDimensionalNameNetworking::handleDimensionalNames
        );
        
        AreashintClient.LOGGER.info("客户端维度域名网络处理初始化完成");
    }
    
    /**
     * 处理接收到的维度域名配置
     * @param client Minecraft客户端实例
     * @param handler 网络处理器
     * @param buf 数据包缓冲区
     * @param responseSender 响应发送器
     */
    private static void handleDimensionalNames(MinecraftClient client,
                                             ClientPlayNetworkHandler handler,
                                             PacketByteBuf buf,
                                             PacketSender responseSender) {
        // 读取JSON数据
        String jsonData = buf.readString();
        
        // 确保在主线程中处理
        client.execute(() -> {
            try {
                // 更新客户端维度域名配置
                ClientDimensionalNameManager.updateFromServer(jsonData);
                
                AreashintClient.LOGGER.info("已接收并更新维度域名配置");
                AreashintClient.LOGGER.debug("维度域名数据: {}", jsonData);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("处理维度域名配置时发生错误", e);
            }
        });
    }
} 