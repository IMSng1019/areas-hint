package areahint.easyadd;

import areahint.data.AreaData;
import areahint.file.JsonHelper;
import areahint.network.Packets;
import areahint.debug.ClientDebugManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

/**
 * EasyAdd缃戠粶閫氫俊澶勭悊
 * 璐熻矗瀹㈡埛绔笌鏈嶅姟绔殑鏁版嵁浼犺緭
 */
public class EasyAddNetworking {
    
    /**
     * 鍙戦€佸煙鍚嶆暟鎹埌鏈嶅姟绔?
     * @param areaData 鍩熷悕鏁版嵁
     * @param dimension 缁村害鏍囪瘑
     */
    public static void sendAreaDataToServer(AreaData areaData, String dimension) {
        try {
            // 搴忓垪鍖栧煙鍚嶆暟鎹?
            String jsonData = JsonHelper.toJsonSingle(areaData);
            
            // 鍒涘缓鏁版嵁鍖?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(jsonData);
            buf.writeString(dimension);
            
            // 鍙戦€佸埌鏈嶅姟绔?
            ClientPlayNetworking.send(Packets.C2S_EASYADD_AREA_DATA, buf);
            
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                "鍚戞湇鍔＄鍙戦€佸煙鍚嶆暟鎹? " + areaData.getName() + " (缁村害: " + dimension + ")");
            
        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(areahint.i18n.I18nManager.translate("easyadd.error.area") + e.getMessage()), false);
            
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                "鍙戦€佸煙鍚嶆暟鎹け璐? " + e.getMessage());
        }
    }
    
    /**
     * 娉ㄥ唽瀹㈡埛绔綉缁滄帴鏀跺櫒
     */
    public static void registerClientReceivers() {
        // 娉ㄥ唽鏈嶅姟绔搷搴旀帴鏀跺櫒
        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_EASYADD_RESPONSE, 
            (client, handler, buf, responseSender) -> {
                try {
                    boolean success = buf.readBoolean();
                    String message = buf.readString();
                    int argCount = buf.readInt();
                    String[] args = new String[argCount];
                    for (int i = 0; i < argCount; i++) {
                        args[i] = buf.readString();
                    }

                    client.execute(() -> {
                        if (client.player != null) {
                            String translated = areahint.i18n.I18nManager.translate(message, (Object[]) args);
                            String color = success ? "搂a" : "搂c";
                            client.player.sendMessage(
                                areahint.util.TextCompat.of(color + translated), false);
                        }
                    });
                    
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                        "鏀跺埌鏈嶅姟绔搷搴? " + (success ? "鎴愬姛" : "澶辫触") + " - " + message);
                    
                } catch (Exception e) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                        "澶勭悊鏈嶅姟绔搷搴旀椂鍙戠敓閿欒: " + e.getMessage());
                }
            });
    }
} 