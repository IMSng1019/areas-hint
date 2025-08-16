package areahint.expandarea;

import areahint.data.AreaData;
import areahint.network.Packets;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;

public class ExpandAreaServerNetworking {
    
    /**
     * 发送扩展后的域名数据到服务端
     */
    public static void sendExpandedAreaToServer(AreaData expandedArea) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            
            // 将AreaData转换为JsonObject
            JsonObject areaJson = AreaDataConverter.toJsonObject(expandedArea);
            
            // 写入数据
            buf.writeString(areaJson.toString());
            
            // 发送到服务端
            ClientPlayNetworking.send(Packets.EXPAND_AREA_CHANNEL, buf);
            
            System.out.println("已发送扩展域名数据到服务端: " + expandedArea.getName());
            
        } catch (Exception e) {
            System.err.println("发送扩展域名数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 注册客户端网络处理器
     */
    public static void registerClientNetworking() {
        // 注册接收服务端响应的处理器
        ClientPlayNetworking.registerGlobalReceiver(
            Packets.EXPAND_AREA_RESPONSE_CHANNEL,
            (client, handler, buf, responseSender) -> {
                try {
                    boolean success = buf.readBoolean();
                    String message = buf.readString(32767);
                    
                    client.execute(() -> {
                        if (client.player != null) {
                            if (success) {
                                client.player.sendMessage(
                                    net.minecraft.text.Text.literal("§a域名扩展成功！" + message)
                                        .formatted(net.minecraft.util.Formatting.GREEN),
                                    false
                                );
                            } else {
                                client.player.sendMessage(
                                    net.minecraft.text.Text.literal("§c域名扩展失败：" + message)
                                        .formatted(net.minecraft.util.Formatting.RED),
                                    false
                                );
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