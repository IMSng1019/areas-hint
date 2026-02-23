package areahint.network;

import areahint.AreashintClient;
import areahint.world.ClientWorldFolderManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

/**
 * 客户端世界网络处理
 * 负责请求和接收服务端的世界信息
 */
public class ClientWorldNetworking {

    /**
     * 初始化客户端世界网络处理
     */
    public static void init() {
        // 注册世界信息接收处理器
        ClientPlayNetworking.registerGlobalReceiver(
            Packets.S2C_WORLD_INFO,
            (payload, context) -> {
                PacketByteBuf buf = payload.buf();
                MinecraftClient client = context.client();
                try {
                    String worldName = buf.readString();

                    client.execute(() -> {
                        try {
                            AreashintClient.LOGGER.info("收到服务端世界信息: '{}'", worldName);
                            AreashintClient.LOGGER.info("开始完成世界文件夹初始化...");

                            ClientWorldFolderManager.finalizeWorldInitialization(worldName);
                            reloadCurrentAreaData();

                        } catch (Exception e) {
                            AreashintClient.LOGGER.error("处理世界信息时发生错误", e);
                        }
                    });

                } catch (Exception e) {
                    AreashintClient.LOGGER.error("解析世界信息包时发生错误", e);
                }
            }
        );

        AreashintClient.LOGGER.info("客户端世界网络处理初始化完成");
    }

    /**
     * 向服务端请求世界信息
     */
    public static void requestWorldInfo() {
        try {
            if (ClientPlayNetworking.canSend(Packets.C2S_REQUEST_WORLD_INFO)) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                ClientPlayNetworking.send(BufPayload.of(Packets.C2S_REQUEST_WORLD_INFO, buf));

                AreashintClient.LOGGER.info("已向服务端请求世界信息");
            } else {
                AreashintClient.LOGGER.warn("无法发送世界信息请求包，服务端可能不支持");
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("请求世界信息时发生错误", e);
        }
    }

    /**
     * 重新加载当前区域数据
     */
    private static void reloadCurrentAreaData() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
                String dimensionId = client.world.getRegistryKey().getValue().toString();
                String dimensionFileName = getDimensionFileName(dimensionId);

                AreashintClient.LOGGER.info("网络同步完成，重新加载区域数据: {}", dimensionFileName);
                AreashintClient.forceRedetectCurrentArea();
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("重新加载区域数据时发生错误", e);
        }
    }

    /**
     * 根据维度ID获取文件名
     */
    private static String getDimensionFileName(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (dimensionId.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (dimensionId.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return areahint.Areashint.OVERWORLD_FILE;
    }
}
