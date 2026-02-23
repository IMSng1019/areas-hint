package areahint.deletehint;

import areahint.data.AreaData;
import areahint.network.BufPayload;
import areahint.network.Packets;
import areahint.network.TranslatableMessage;
import areahint.util.AreaDataConverter;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import com.google.gson.JsonObject;

/**
 * DeleteHint客户端网络通信
 */
public class DeleteHintClientNetworking {

    /**
     * 发送修改后的域名数据到服务端
     */
    public static void sendToServer(AreaData area, String dimension) {
        try {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            JsonObject json = AreaDataConverter.toJsonObject(area);
            buf.writeString(json.toString());
            buf.writeString(dimension);
            ClientPlayNetworking.send(BufPayload.of(Packets.DELETEHINT_AREA_CHANNEL, buf));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册客户端网络接收器
     */
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
            Packets.DELETEHINT_AREA_RESPONSE_CHANNEL,
            (payload, context) -> {
                PacketByteBuf buf = payload.buf();
                var client = context.client();
                boolean success = buf.readBoolean();
                net.minecraft.text.MutableText message = TranslatableMessage.read(buf);
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(
                            message.formatted(success ? net.minecraft.util.Formatting.GREEN : net.minecraft.util.Formatting.RED), false);
                    }
                });
            }
        );
    }
}
