package areahint.network;

import areahint.util.TextCompat;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * 缂冩垹绮堕崠鍛倳鐠囨垶绉烽幁顖氫紣閸忛琚?
 * 閺堝秴濮熺粩顖氬晸閸忋儳鐐曠拠鎴︽暛/鐎涙娼伴弬鍥ㄦ拱閿涘苯顓归幋椋庮伂鐠囪褰囬獮鑸电€?Text
 */
public class TranslatableMessage {

    public record Part(boolean isKey, String value) {}

    public static Part key(String key) { return new Part(true, key); }
    public static Part lit(String text) { return new Part(false, text); }

    /** 閸愭瑥鍙嗛崡鏇氶嚋缂堟槒鐦ч柨?*/
    public static void write(PacketByteBuf buf, String key) {
        buf.writeInt(1);
        buf.writeBoolean(true);
        buf.writeString(key);
    }

    /** 閸愭瑥鍙嗘径姘嚋闁劌鍨庨敍鍫㈢倳鐠囨垿鏁?鐎涙娼伴弬鍥ㄦ拱濞ｅ嘲鎮庨敍?*/
    public static void write(PacketByteBuf buf, Part... parts) {
        buf.writeInt(parts.length);
        for (Part part : parts) {
            buf.writeBoolean(part.isKey);
            buf.writeString(part.value);
        }
    }

    /** 鐎广垺鍩涚粩顖濐嚢閸欐牕鑻熼弸鍕紦 MutableText */
    public static MutableText read(PacketByteBuf buf) {
        int count = buf.readInt();
        MutableText result = TextCompat.empty();
        for (int i = 0; i < count; i++) {
            boolean isKey = buf.readBoolean();
            String value = buf.readString(32767);
            result.append(isKey ? TextCompat.translatable(value) : TextCompat.literal(value));
        }
        return result;
    }
}
