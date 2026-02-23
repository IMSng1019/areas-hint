package areahint.replacebutton;

import areahint.network.BufPayload;
import areahint.network.Packets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
        ClientPlayNetworking.registerGlobalReceiver(Packets.REPLACEBUTTON_START, (payload, context) -> {
            context.client().execute(() -> {
                ReplaceButtonManager.getInstance().startReplaceButton();
            });
        });

        // 接收取消按键更换的消息
        ClientPlayNetworking.registerGlobalReceiver(Packets.REPLACEBUTTON_CANCEL, (payload, context) -> {
            context.client().execute(() -> {
                ReplaceButtonManager.getInstance().cancel();
            });
        });

        // 接收确认按键更换的消息
        ClientPlayNetworking.registerGlobalReceiver(Packets.REPLACEBUTTON_CONFIRM, (payload, context) -> {
            context.client().execute(() -> {
                ReplaceButtonManager.getInstance().confirmNewKey();
            });
        });
    }

    /**
     * 发送开始按键更换请求到服务器
     */
    public static void sendStartRequest() {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        ClientPlayNetworking.send(BufPayload.of(Packets.REPLACEBUTTON_START, buf));
    }

    /**
     * 发送取消按键更换请求到服务器
     */
    public static void sendCancelRequest() {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        ClientPlayNetworking.send(BufPayload.of(Packets.REPLACEBUTTON_CANCEL, buf));
    }

    /**
     * 发送确认按键更换请求到服务器
     */
    public static void sendConfirmRequest() {
        PacketByteBuf buf = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        ClientPlayNetworking.send(BufPayload.of(Packets.REPLACEBUTTON_CONFIRM, buf));
    }
}
