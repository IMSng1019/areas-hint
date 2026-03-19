package areahint.delete;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.debug.ClientDebugManager;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Delete閸旂喕鍏樼粻锛勬倞閸?
 * 鐠愮喕鐭楁禍銈勭鞍瀵繐鐓欓崥宥呭灩闂勩倗娈戦弫缈犻嚋濞翠胶鈻肩粻锛勬倞
 */
public class DeleteManager {

    /**
     * Delete閻樿埖鈧焦鐏囨稉?
     */
    public enum DeleteState {
        IDLE,           // 缁屾椽妫介悩鑸碘偓?
        SELECT_AREA,    // 闁瀚ㄧ憰浣稿灩闂勩倗娈戦崺鐔锋倳
        CONFIRM_DELETE  // 绾喛顓婚崚鐘绘珟
    }

    // 閸楁洑绶ョ€圭偘绶?
    private static DeleteManager instance;

    // 瑜版挸澧犻悩鑸碘偓?
    private DeleteState currentState = DeleteState.IDLE;

    // 闁鑵戦惃鍕厵閸?
    private String selectedAreaName = null;
    private AreaData selectedArea = null;

    // 瑜版挸澧犵紒鏉戝
    private String currentDimension = null;

    // 閸欘垰鍨归梽銈囨畱閸╃喎鎮曢崚妤勩€?
    private List<AreaData> deletableAreas = new ArrayList<>();

    // 缁変焦婀侀弸鍕偓鐘插毐閺佸府绱欓崡鏇氱伐濡€崇础閿?
    private DeleteManager() {}

    /**
     * 閼惧嘲褰囬崡鏇氱伐鐎圭偘绶?
     */
    public static DeleteManager getInstance() {
        if (instance == null) {
            instance = new DeleteManager();
        }
        return instance;
    }

    /**
     * 閸氼垰濮〥elete濞翠胶鈻?
     */
    public void startDelete() {
        if (currentState != DeleteState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_2")), false);
            return;
        }

        // 閼惧嘲褰囪ぐ鎾冲缂佹潙瀹虫穱鈩冧紖
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            // 閼惧嘲褰囩紒鏉戝閺嶅洩鐦戠粭锕€鑻熸潪顒佸床娑撹櫣娣惔锔捐閸?
            String dimensionPath = client.world.getRegistryKey().getValue().getPath();
            currentDimension = areahint.network.Packets.convertDimensionPathToType(dimensionPath);

            if (currentDimension == null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.dimension")), false);
                ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                    "閺冪姵纭舵潪顒佸床缂佹潙瀹崇捄顖氱窞: " + dimensionPath);
                return;
            }

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "瑜版挸澧犵紒鏉戝: " + currentDimension);

            // 鐠佸墽鐤嗛悩鑸碘偓?
            currentState = DeleteState.SELECT_AREA;

            // 閸氭垶婀囬崝锛勵伂鐠囬攱鐪伴崣顖氬灩闂勩倗娈戦崺鐔锋倳閸掓銆冮敍鍫熸箛閸旓紕顏导姘壌閹诡喗娼堥梽鎰灲閺傤叏绱?
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.area.delete.list_2")), false);
            DeleteNetworking.requestDeletableAreas(currentDimension);
        }
    }

    /**
     * 閹恒儲鏁归張宥呭缁旑垰褰傞柅浣烘畱閸欘垰鍨归梽銈呯厵閸氬秴鍨悰?
     * @param areas 閸欘垰鍨归梽銈囨畱閸╃喎鎮曢崚妤勩€?
     */
    public void receiveDeletableAreas(List<AreaData> areas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        deletableAreas.clear();
        deletableAreas.addAll(areas);

        if (deletableAreas.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.area.dimension.delete")), false);
            resetState();
            return;
        }

        // 閺勫墽銇歎I
        DeleteUI.showAreaSelectionScreen(deletableAreas);
    }


    /**
     * 婢跺嫮鎮婇崺鐔锋倳闁瀚?
     */
    public void handleAreaSelection(String areaName) {
        if (currentState != DeleteState.SELECT_AREA) {
            return;
        }

        // 缁夊娅庡鏇炲娇閿涘牆顩ч弸婊冪摠閸︻煉绱?
        if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
            areaName = areaName.substring(1, areaName.length() - 1);
        }

        // 閺屻儲澹橀柅澶夎厬閻ㄥ嫬鐓欓崥?
        selectedArea = null;
        for (AreaData area : deletableAreas) {
            if (area.getName().equals(areaName)) {
                selectedArea = area;
                selectedAreaName = areaName;
                break;
            }
        }

        if (selectedArea == null) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.area_3") + areaName), false);
            return;
        }

        // 鏉╂稑鍙嗙涵顔款吇閸掔娀娅庨悩鑸碘偓?
        currentState = DeleteState.CONFIRM_DELETE;
        DeleteUI.showConfirmDeleteScreen(selectedArea);
    }

    /**
     * 绾喛顓婚崚鐘绘珟閸╃喎鎮?
     */
    public void confirmDelete() {
        if (currentState != DeleteState.CONFIRM_DELETE) {
            return;
        }

        if (selectedArea == null || selectedAreaName == null) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.area.delete_2")), false);
            cancelDelete();
            return;
        }

        try {
            // 閸欐垿鈧礁鍨归梽銈堫嚞濮瑰倸鍩岄張宥呭缁?
            DeleteNetworking.sendDeleteRequestToServer(selectedAreaName, currentDimension);

            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.prompt.delete")), false);

            resetState();

        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.area.delete") + e.getMessage()), false);
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "閸掔娀娅庢径杈Е: " + e.getMessage());
        }
    }

    /**
     * 閸欐牗绉稤elete濞翠胶鈻?
     */
    public void cancelDelete() {
        MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.cancel")), false);
        resetState();
    }

    /**
     * 闁插秶鐤嗛悩鑸碘偓?
     */
    private void resetState() {
        currentState = DeleteState.IDLE;
        selectedAreaName = null;
        selectedArea = null;
        currentDimension = null;
        deletableAreas.clear();
    }

    /**
     * 閼惧嘲褰囪ぐ鎾冲缂佹潙瀹抽惃鍕瀮娴犺泛鎮?
     */
    private String getFileNameForCurrentDimension() {
        if (currentDimension == null) {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "currentDimension 娑?null");
            return null;
        }

        String fileName = areahint.network.Packets.getFileNameForDimension(currentDimension);

        if (fileName == null) {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "Could not resolve dimension file for " + currentDimension);
        } else {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.AREA_DETECTION,
                "Dimension " + currentDimension + " uses file: " + fileName);
        }

        return fileName;
    }

    // Getters
    public DeleteState getCurrentState() { return currentState; }
    public String getSelectedAreaName() { return selectedAreaName; }
    public AreaData getSelectedArea() { return selectedArea; }
}
