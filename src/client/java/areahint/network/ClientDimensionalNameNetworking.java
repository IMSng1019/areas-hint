package areahint.network;

import areahint.AreashintClient;
import areahint.dimensional.ClientDimensionalNameManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

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
            Packets.S2C_DIMENSIONAL_NAMES,
            (payload, context) -> {
                PacketByteBuf buf = payload.buf();
                String jsonData = buf.readString();

                context.client().execute(() -> {
                    try {
                        ClientDimensionalNameManager.updateFromServer(jsonData);
                        AreashintClient.LOGGER.info("已接收并更新维度域名配置");
                        AreashintClient.LOGGER.debug("维度域名数据: {}", jsonData);
                    } catch (Exception e) {
                        AreashintClient.LOGGER.error("处理维度域名配置时发生错误", e);
                    }
                });
            }
        );

        AreashintClient.LOGGER.info("客户端维度域名网络处理初始化完成");
    }
}
