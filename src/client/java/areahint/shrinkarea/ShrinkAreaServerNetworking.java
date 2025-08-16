package areahint.shrinkarea;

import areahint.data.AreaData;
import areahint.network.Packets;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import com.google.gson.JsonObject;

/**
 * 收缩域名客户端网络通信
 * 负责客户端与服务端之间的收缩域名数据传输
 */
public class ShrinkAreaServerNetworking {
    
    /**
     * 发送收缩后的域名数据到服务端
     */
    public static void sendShrunkAreaToServer(AreaData shrunkArea) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            
            // 将AreaData转换为JsonObject
            JsonObject areaJson = AreaDataConverter.toJsonObject(shrunkArea);
            
            // 写入数据
            buf.writeString(areaJson.toString());
            
            // 发送到服务端
            ClientPlayNetworking.send(Packets.SHRINK_AREA_CHANNEL, buf);
            
            System.out.println("已发送收缩域名数据到服务端: " + shrunkArea.getName());
            
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
        if (success) {
            System.out.println("收缩域名成功: " + message);
            // 可以在这里添加成功的UI提示
        } else {
            System.err.println("收缩域名失败: " + message);
            // 可以在这里添加失败的UI提示
        }
    }
} 