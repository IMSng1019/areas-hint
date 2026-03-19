package areahint.rename;

import areahint.network.Packets;
import areahint.debug.ClientDebugManager;
import areahint.i18n.I18nManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

/**
 * Rename缃戠粶閫氫俊澶勭悊
 * 璐熻矗瀹㈡埛绔笌鏈嶅姟绔殑鏁版嵁浼犺緭
 */
public class RenameNetworking {

    /**
     * 鍙戦€侀噸鍛藉悕璇锋眰鍒版湇鍔＄
     * @param oldName 鍘熷煙鍚嶅悕绉?
     * @param newName 鏂板煙鍚嶅悕绉?
     * @param newSurfaceName 鏂拌仈鍚堝煙鍚嶏紙鍙负null锛?
     * @param dimension 缁村害鏍囪瘑
     */
    public static void sendRenameRequest(String oldName, String newName, String newSurfaceName, String dimension) {
        try {
            // 鍒涘缓鏁版嵁鍖?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(oldName);
            buf.writeString(newName);
            buf.writeString(newSurfaceName != null ? newSurfaceName : ""); // 绌哄瓧绗︿覆琛ㄧずnull
            buf.writeString(dimension);

            // 鍙戦€佸埌鏈嶅姟绔?
            ClientPlayNetworking.send(Packets.C2S_RENAME_REQUEST, buf);

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "鍚戞湇鍔＄鍙戦€侀噸鍛藉悕璇锋眰: " + oldName + " -> " + newName + " (缁村害: " + dimension + ")");

        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.rename") + e.getMessage()), false);

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "鍙戦€侀噸鍛藉悕璇锋眰澶辫触: " + e.getMessage());
        }
    }

    /**
     * 娉ㄥ唽瀹㈡埛绔綉缁滄帴鏀跺櫒锛堝湪 ClientNetworking 涓皟鐢級
     */
    public static void registerClientReceivers() {
        // 娉ㄥ唽鏈嶅姟绔搷搴旀帴鏀跺櫒
        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_RENAME_RESPONSE,
            (client, handler, buf, responseSender) -> {
                try {
                    String action = buf.readString();

                    if ("rename_list".equals(action)) {
                        // 澶勭悊鍙噸鍛藉悕鍩熷悕鍒楄〃鍝嶅簲
                        handleRenameListResponse(client, buf);
                    } else if ("rename_response".equals(action)) {
                        // 澶勭悊閲嶅懡鍚嶇粨鏋滃搷搴?
                        handleRenameResultResponse(client, buf);
                    }

                } catch (Exception e) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                        "澶勭悊鏈嶅姟绔搷搴旀椂鍙戠敓閿欒: " + e.getMessage());
                }
            });
    }

    /**
     * 澶勭悊鍩熷悕鍒楄〃鍝嶅簲
     */
    private static void handleRenameListResponse(MinecraftClient client, PacketByteBuf buf) {
        String dimension = buf.readString();
        int count = buf.readInt();

        // 璇诲彇鍩熷悕鍒楄〃
        java.util.List<areahint.data.AreaData> areas = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                String areaName = buf.readString();
                String signature = buf.readString();

                // 鍒涘缓绠€鍖栫殑AreaData瀵硅薄锛堝彧鍖呭惈蹇呰淇℃伅锛?
                areahint.data.AreaData area = new areahint.data.AreaData();
                area.setName(areaName);
                area.setSignature(signature.isEmpty() ? null : signature);

                areas.add(area);
            } catch (Exception e) {
                ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                    "璇诲彇鍩熷悕淇℃伅鏃跺嚭閿? " + e.getMessage());
            }
        }

        client.execute(() -> {
            if (client.player != null) {
                if (areas.isEmpty()) {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.area.dimension.rename")), false);
                } else {
                    // 鍚姩浜や簰寮廟ename娴佺▼
                    RenameManager.getInstance().startRename(areas, dimension);
                }
            }
        });
    }

    /**
     * 澶勭悊閲嶅懡鍚嶇粨鏋滃搷搴?
     */
    private static void handleRenameResultResponse(MinecraftClient client, PacketByteBuf buf) {
        boolean success = buf.readBoolean();
        String message = buf.readString();

        client.execute(() -> {
            if (client.player != null) {
                if (success) {
                    client.player.sendMessage(areahint.util.TextCompat.of("搂a" + message), false);
                } else {
                    client.player.sendMessage(areahint.util.TextCompat.of("搂c" + message), false);
                }
            }
        });

        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
            "鏀跺埌鏈嶅姟绔搷搴? " + (success ? "鎴愬姛" : "澶辫触") + " - " + message);
    }
}
