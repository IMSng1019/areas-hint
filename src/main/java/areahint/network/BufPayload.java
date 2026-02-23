package areahint.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * 通用的 CustomPayload 包装类
 * 将原始字节数据包装为 1.20.5 的 CustomPayload 格式
 */
public record BufPayload(Id<BufPayload> id, byte[] data) implements CustomPayload {
    @Override
    public Id<? extends CustomPayload> getId() { return id; }

    public PacketByteBuf buf() {
        return new PacketByteBuf(Unpooled.wrappedBuffer(data));
    }

    public static BufPayload of(Id<BufPayload> id, PacketByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return new BufPayload(id, bytes);
    }

    public static PacketCodec<PacketByteBuf, BufPayload> codec(Id<BufPayload> id) {
        return PacketCodec.of(
            (value, buf) -> buf.writeBytes(value.data()),
            buf -> {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                return new BufPayload(id, bytes);
            }
        );
    }
}
