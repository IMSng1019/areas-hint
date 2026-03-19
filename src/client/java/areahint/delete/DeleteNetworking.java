package areahint.delete;

import areahint.data.AreaData;
import areahint.network.Packets;
import areahint.debug.ClientDebugManager;
import areahint.i18n.I18nManager;
import areahint.file.JsonHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Delete缂傚啯鍨圭划鍫曟焻濮橆偂绻嗗璺哄閹?
 * 閻犳劗鍠曢惌妤冣偓骞垮灪閸╂稓绮╅娆戠憿闁哄牆绉存慨鐔虹博椤栨粍鐣遍柛鎺斿█濞呭海鎷犻柨瀣勾濞磋偐濮剧欢?
 */
public class DeleteNetworking {

    /**
     * 閻犲洭鏀遍惇浼村嫉瀹ュ懎顫ょ紒鏃戝灠瑜板倿鏌呮担绋胯闁告帞濞€濞呭酣鎯冮崟顐ゅ幍闁告艾绉撮崹顏嗘偘?
     * @param dimension 缂備焦娼欑€规娊寮介崶顏嗘
     */
    public static void requestDeletableAreas(String dimension) {
        try {
            // 闁告帗绋戠紓鎾诲极閻楀牆绁﹂柛?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(dimension);

            // 闁告瑦鍨块埀顑跨閸╁矂寮靛鍛潳缂?
            ClientPlayNetworking.send(Packets.C2S_REQUEST_DELETABLE_AREAS, buf);

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "闁告碍鍨跺﹢鍥礉閿涘嫷浼傞悹鍥敱閻即宕ｉ姘仼闂傚嫨鍊曢悡娆撳触瀹ュ懎鐏欓悶?(缂備焦娼欑€? " + dimension + ")");

        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.area.delete.list") + e.getMessage()), false);

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "閻犲洭鏀遍惇浼村矗椤栨艾鐏╅梻鍕╁€曢悡娆撳触瀹ュ懎鐏欓悶娑栧妼閵囨垹鎷? " + e.getMessage());
        }
    }

    /**
     * 闁告瑦鍨块埀顑跨閸ㄥ綊姊介妶鍫殲婵懓鍊搁崺宀勫嫉瀹ュ懎顫ょ紒?
     * @param areaName 閻熸洑绀侀崹褰掓⒔閵堝洦鐣遍柛鈺冨枎閹洟宕ュ鍥?
     * @param dimension 缂備焦娼欑€规娊寮介崶顏嗘
     */
    public static void sendDeleteRequestToServer(String areaName, String dimension) {
        try {
            // 闁告帗绋戠紓鎾诲极閻楀牆绁﹂柛?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(areaName);
            buf.writeString(dimension);

            // 闁告瑦鍨块埀顑跨閸╁矂寮靛鍛潳缂?
            ClientPlayNetworking.send(Packets.C2S_DELETE_AREA, buf);

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "闁告碍鍨跺﹢鍥礉閿涘嫷浼傞柛娆愬灴閳ь兛绀侀崹褰掓⒔閵堝牜鍤炴慨? " + areaName + " (缂備焦娼欑€? " + dimension + ")");

        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.delete") + e.getMessage()), false);

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "闁告瑦鍨块埀顑跨閸ㄥ綊姊介妶鍫殲婵懓鍊搁妵鎴犳嫻? " + e.getMessage());
        }
    }

    /**
     * 婵炲鍔岄崬鐣屸偓骞垮灪閸╂稓绮╅婊呯Ч缂備焦绮嶇敮鎾绩鐠虹儤鐝?
     */
    public static void registerClientReceivers() {
        // 婵炲鍔岄崬浠嬪矗椤栨艾鐏╅梻鍕╁€曢悡娆撳触瀹ュ懎鐏欓悶娑栧妽鐢挳寮ㄧ捄鐑樼彜
        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_DELETABLE_AREAS_LIST,
            (client, handler, buf, responseSender) -> {
                try {
                    int count = buf.readInt();
                    List<AreaData> areas = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        String json = buf.readString();
                        AreaData area = JsonHelper.fromJsonSingle(json);
                        if (area != null) {
                            areas.add(area);
                        }
                    }

                    client.execute(() -> {
                        DeleteManager.getInstance().receiveDeletableAreas(areas);
                    });

                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                        "Received deletable area list: " + count + " areas");

                } catch (Exception e) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                        "濠㈣泛瀚幃濠囧矗椤栨艾鐏╅梻鍕╁€曢悡娆撳触瀹ュ懎鐏欓悶娑栧妽濡炲倿宕ｉ幋鐘虫櫢闂佹寧鐟ㄩ? " + e.getMessage());
                }
            });

        // 婵炲鍔岄崬浠嬪嫉瀹ュ懎顫ょ紒鏃戝灠閹奸攱鎯旈弮鈧敮鎾绩鐠虹儤鐝?
        ClientPlayNetworking.registerGlobalReceiver(Packets.S2C_DELETE_RESPONSE,
            (client, handler, buf, responseSender) -> {
                try {
                    boolean success = buf.readBoolean();
                    String message = buf.readString();

                    client.execute(() -> {
                        if (client.player != null) {
                            if (success) {
                                client.player.sendMessage(
                                    areahint.util.TextCompat.of("閹间繘" + I18nManager.translate("message.success.area.delete") + message), false);
                            } else {
                                client.player.sendMessage(
                                    areahint.util.TextCompat.of("閹间竣" + message), false);
                            }
                        }
                    });

                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                        "Delete response received: " + (success ? "success" : "error") + " - " + message);

                } catch (Exception e) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                        "濠㈣泛瀚幃濠囧嫉瀹ュ懎顫ょ紒鏃戝灠閸ㄥ綊姊介妶鍛儥閹煎瓨姊瑰鍌炲矗閹寸姵鏅搁梺鎸庣懆椤? " + e.getMessage());
                }
            });
    }
}
