package areahint.replacebutton;

import areahint.network.Packets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

/**
 * ReplaceButton网络处理
 * 处理客户端接收服务器的replacebutton相关消息
 */
public class ReplaceButtonNetworking {

    /**
     * 注册客户端接收器
     */
    public static void registerClientReceivers() {
        // 接收开始按键更换的消息
        ClientPlayNetworking.registerGlobalReceiver(Packets.REPLACEBUTTON_START, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                ReplaceButtonManager.getInstance().startReplaceButton();
            });
        });

        // 接收取消按键更换的消息
        ClientPlayNetworking.registerGlobalReceiver(Packets.REPLACEBUTTON_CANCEL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                ReplaceButtonManager.getInstance().cancel();
            });
        });

        // 接收确认按键更换的消息
        ClientPlayNetworking.registerGlobalReceiver(Packets.REPLACEBUTTON_CONFIRM, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                ReplaceButtonManager.getInstance().confirmNewKey();
            });
        });
    }

    /**
     * 发送开始按键更换请求到服务器
     */
    public static void sendStartRequest() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(Packets.REPLACEBUTTON_START, buf);
    }

    /**
     * 发送取消按键更换请求到服务器
     */
    public static void sendCancelRequest() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(Packets.REPLACEBUTTON_CANCEL, buf);
    }

    /**
     * 发送确认按键更换请求到服务器
     */
    public static void sendConfirmRequest() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(Packets.REPLACEBUTTON_CONFIRM, buf);
    }
}
