package areahint.signature;

import areahint.data.AreaData;
import areahint.network.Packets;
import areahint.network.TranslatableMessage;
import areahint.util.AreaDataConverter;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

/**
 * Signature客户端网络通信
 */
public class SignatureClientNetworking {

    public static void sendToServer(String operation, AreaData area, String dimension, String targetPlayerName) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            JsonObject json = AreaDataConverter.toJsonObject(area);
            buf.writeString(operation);
            buf.writeString(json.toString());
            buf.writeString(dimension);
            buf.writeString(targetPlayerName);
            ClientPlayNetworking.send(Packets.SIGNATURE_AREA_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
            Packets.SIGNATURE_AREA_RESPONSE_CHANNEL,
            (client, handler, buf, responseSender) -> {
                boolean success = buf.readBoolean();
                MutableText message = TranslatableMessage.read(buf);
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(
                            message.formatted(success ? Formatting.GREEN : Formatting.RED), false);
                    }
                });
            }
        );
    }
}
