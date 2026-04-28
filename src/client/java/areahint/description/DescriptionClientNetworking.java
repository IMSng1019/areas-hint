package areahint.description;

import areahint.AreashintClient;
import areahint.network.Packets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端域名描述网络处理。
 */
public final class DescriptionClientNetworking {
    private DescriptionClientNetworking() {
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_DESCRIPTION_QUERY_RESPONSE,
            (client, handler, buf, responseSender) -> {
                String title = buf.readString(32767);
                String author = buf.readString(32767);
                String description = buf.readString(32767);
                client.execute(() -> BookDescriptionScreenUtil.openDescriptionBook(title, author, description));
            });

        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_DESCRIPTION_AREA_LIST,
            (client, handler, buf, responseSender) -> {
                String operation = buf.readString();
                String targetType = buf.readString();
                String dimension = buf.readString();
                int count = buf.readInt();
                List<DescriptionListEntry> entries = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    entries.add(new DescriptionListEntry(
                        buf.readString(32767),
                        buf.readString(32767),
                        buf.readInt(),
                        buf.readString(32767),
                        buf.readString(32767),
                        buf.readString(32767)
                    ));
                }
                client.execute(() -> DescriptionManager.getInstance().receiveList(operation, targetType, dimension, entries));
            });

        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_DESCRIPTION_MUTATION_RESPONSE,
            (client, handler, buf, responseSender) -> {
                boolean success = buf.readBoolean();
                String message = buf.readString(32767);
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal(message).formatted(success ? Formatting.GREEN : Formatting.RED), false);
                    }
                    DescriptionManager.getInstance().reset();
                });
            });
    }

    public static void sendQuery(String targetType, String dimensionType, String targetName) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(clean(targetType));
        buf.writeString(clean(dimensionType));
        buf.writeString(clean(targetName));
        ClientPlayNetworking.send(Packets.C2S_DESCRIPTION_QUERY, buf);
    }

    public static void sendListRequest(String operation, String targetType, String dimensionType, String query) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(clean(operation));
        buf.writeString(clean(targetType));
        buf.writeString(clean(dimensionType));
        buf.writeString(clean(query));
        ClientPlayNetworking.send(Packets.C2S_DESCRIPTION_LIST_REQUEST, buf);
    }

    public static boolean sendWrite(String targetType, String dimensionType, String targetName, String description) {
        if (description == null || description.length() > DescriptionServerNetworking.MAX_DESCRIPTION_LENGTH) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("描述过长，最多 32767 个字符").formatted(Formatting.RED), false);
            }
            return false;
        }
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(clean(targetType));
        buf.writeString(clean(dimensionType));
        buf.writeString(clean(targetName));
        buf.writeString(description);
        ClientPlayNetworking.send(Packets.C2S_DESCRIPTION_WRITE, buf);
        return true;
    }

    public static void sendDelete(String targetType, String dimensionType, String targetName) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(clean(targetType));
        buf.writeString(clean(dimensionType));
        buf.writeString(clean(targetName));
        ClientPlayNetworking.send(Packets.C2S_DESCRIPTION_DELETE, buf);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
