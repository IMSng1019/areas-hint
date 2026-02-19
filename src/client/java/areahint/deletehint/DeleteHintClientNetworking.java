package areahint.deletehint;

import areahint.data.AreaData;
import areahint.network.Packets;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
            PacketByteBuf buf = PacketByteBufs.create();
            JsonObject json = AreaDataConverter.toJsonObject(area);
            buf.writeString(json.toString());
            buf.writeString(dimension);
            ClientPlayNetworking.send(Packets.DELETEHINT_AREA_CHANNEL, buf);
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
            (client, handler, buf, responseSender) -> {
                boolean success = buf.readBoolean();
                String message = buf.readString(32767);
                client.execute(() -> {
                    if (client.player != null) {
                        String prefix = success ? "§a" : "§c";
                        client.player.sendMessage(
                            net.minecraft.text.Text.of(prefix + message), false);
                    }
                });
            }
        );
    }
}
