package areahint.network;

import areahint.AreashintClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

/**
 * 客户端维度域名网络请求
 */
public class ClientDimNameNetworking {

    public static void sendDimNameChange(String dimensionId, String newName) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(dimensionId);
            buf.writeString(newName);
            ClientPlayNetworking.send(Packets.C2S_DIMNAME_REQUEST, buf);
        } catch (Exception e) {
            AreashintClient.LOGGER.error("发送维度域名修改请求失败", e);
        }
    }

    public static void sendDimColorChange(String dimensionId, String newColor) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(dimensionId);
            buf.writeString(newColor);
            ClientPlayNetworking.send(Packets.C2S_DIMCOLOR_REQUEST, buf);
        } catch (Exception e) {
            AreashintClient.LOGGER.error("发送维度域名颜色修改请求失败", e);
        }
    }

    public static void sendToServer(String action) {
        if (action.startsWith("dimname_apply:")) {
            String rest = action.substring("dimname_apply:".length());
            // dimensionId格式为 namespace:path（如minecraft:overworld），需跳过第一个冒号
            int firstColon = rest.indexOf(":");
            if (firstColon > 0) {
                int secondColon = rest.indexOf(":", firstColon + 1);
                if (secondColon > 0) {
                    sendDimNameChange(rest.substring(0, secondColon), rest.substring(secondColon + 1));
                }
            }
        } else if (action.startsWith("dimcolor_apply:")) {
            String rest = action.substring("dimcolor_apply:".length());
            int firstColon = rest.indexOf(":");
            if (firstColon > 0) {
                int secondColon = rest.indexOf(":", firstColon + 1);
                if (secondColon > 0) {
                    sendDimColorChange(rest.substring(0, secondColon), rest.substring(secondColon + 1));
                }
            }
        }
    }
}
