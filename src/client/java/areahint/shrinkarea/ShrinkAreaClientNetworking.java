package areahint.shrinkarea;

import areahint.data.AreaData;
import areahint.i18n.I18nManager;
import areahint.network.Packets;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;

/**
 * 收缩域名客户端网络通信
 * 负责客户端与服务端之间的收缩域名数据传输
 */
public class ShrinkAreaClientNetworking {
    
    /**
     * 发送收缩后的域名数据到服务端
     */
    public static void sendShrunkAreaToServer(AreaData shrunkArea, String dimension) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();

            // 将AreaData转换为JsonObject
            JsonObject areaJson = AreaDataConverter.toJsonObject(shrunkArea);

            // 写入数据
            buf.writeString(areaJson.toString());
            buf.writeString(dimension);  // 添加维度信息

            // 发送到服务端
            ClientPlayNetworking.send(Packets.SHRINK_AREA_CHANNEL, buf);

            System.out.println("已发送收缩域名数据到服务端: " + shrunkArea.getName() + " (维度: " + dimension + ")");

        } catch (Exception e) {
            System.err.println("发送收缩域名数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 注册客户端网络处理器
     */
    public static void registerClientNetworking() {
        // 注册接收服务端响应的处理器
        ClientPlayNetworking.registerGlobalReceiver(
            Packets.SHRINK_AREA_RESPONSE_CHANNEL,
            (client, handler, buf, responseSender) -> {
                try {
                    boolean success = buf.readBoolean();
                    String message = buf.readString(32767);
                    
                    client.execute(() -> {
                        handleShrinkAreaResponse(success, message);
                    });
                    
                } catch (Exception e) {
                    System.err.println("处理收缩域名响应时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        );
    }
    
    /**
     * 处理收缩域名响应
     */
    private static void handleShrinkAreaResponse(boolean success, String message) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.player != null) {
                if (success) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal(I18nManager.translate("shrinkarea.success.area.shrink_2") + message)
                            .formatted(net.minecraft.util.Formatting.GREEN),
                        false
                    );
                } else {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal(I18nManager.translate("shrinkarea.error.area.shrink_2") + I18nManager.translate(message))
                            .formatted(net.minecraft.util.Formatting.RED),
                        false
                    );
                }
            }
        });
    }
} 