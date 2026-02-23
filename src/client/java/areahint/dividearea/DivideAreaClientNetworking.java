package areahint.dividearea;

import areahint.data.AreaData;
import areahint.network.BufPayload;
import areahint.network.Packets;
import areahint.util.AreaDataConverter;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import areahint.network.TranslatableMessage;
import com.google.gson.JsonObject;

public class DivideAreaClientNetworking {

    /**
     * 发送分割后的两个域名数据到服务端
     */
    public static void sendDividedAreasToServer(AreaData area1, AreaData area2, String originalAreaName, String dimension) {
        try {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            JsonObject json1 = AreaDataConverter.toJsonObject(area1);
            JsonObject json2 = AreaDataConverter.toJsonObject(area2);
            buf.writeString(json1.toString());
            buf.writeString(json2.toString());
            buf.writeString(originalAreaName);
            buf.writeString(dimension);
            ClientPlayNetworking.send(BufPayload.of(Packets.DIVIDE_AREA_CHANNEL, buf));
        } catch (Exception e) {
            System.err.println("发送分割域名数据失败: " + e.getMessage());
        }
    }

    /**
     * 注册客户端网络处理器
     */
    public static void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(
            Packets.DIVIDE_AREA_RESPONSE_CHANNEL,
            (payload, context) -> {
                PacketByteBuf buf = payload.buf();
                var client = context.client();
                try {
                    boolean success = buf.readBoolean();
                    net.minecraft.text.MutableText message = TranslatableMessage.read(buf);
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.sendMessage(
                                message.formatted(success ? net.minecraft.util.Formatting.GREEN : net.minecraft.util.Formatting.RED), false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        );
    }
}
