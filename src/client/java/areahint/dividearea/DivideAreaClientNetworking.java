package areahint.dividearea;

import areahint.data.AreaData;
import areahint.network.Packets;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import com.google.gson.JsonObject;

public class DivideAreaClientNetworking {

    /**
     * 发送分割后的两个域名数据到服务端
     */
    public static void sendDividedAreasToServer(AreaData area1, AreaData area2, String originalAreaName, String dimension) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            JsonObject json1 = AreaDataConverter.toJsonObject(area1);
            JsonObject json2 = AreaDataConverter.toJsonObject(area2);
            buf.writeString(json1.toString());
            buf.writeString(json2.toString());
            buf.writeString(originalAreaName);
            buf.writeString(dimension);
            ClientPlayNetworking.send(Packets.DIVIDE_AREA_CHANNEL, buf);
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
            (client, handler, buf, responseSender) -> {
                try {
                    boolean success = buf.readBoolean();
                    String message = buf.readString(32767);
                    client.execute(() -> {
                        if (client.player != null) {
                            if (success) {
                                client.player.sendMessage(
                                    net.minecraft.text.Text.literal("§a域名分割成功！" + message)
                                        .formatted(net.minecraft.util.Formatting.GREEN), false);
                            } else {
                                client.player.sendMessage(
                                    net.minecraft.text.Text.literal("§c域名分割失败：" + message)
                                        .formatted(net.minecraft.util.Formatting.RED), false);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        );
    }
}
