package areahint.rename;

import areahint.data.AreaData;

import areahint.chat.ClientChatCompat;
import areahint.file.FileManager;
import areahint.debug.ClientDebugManager;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Rename鍔熻兘绠＄悊鍣?
 * 璐熻矗浜や簰寮忓煙鍚嶉噸鍛藉悕鐨勬暣涓祦绋嬬鐞?
 */
public class RenameManager {

    /**
     * Rename鐘舵€佹灇涓?
     */
    public enum RenameState {
        IDLE,                   // 绌洪棽鐘舵€?
        SELECT_AREA,            // 閫夋嫨瑕侀噸鍛藉悕鐨勫煙鍚?
        INPUT_NEW_NAME,         // 杈撳叆鏂板煙鍚嶅悕绉?
        INPUT_NEW_SURFACE_NAME, // 杈撳叆鏂拌仈鍚堝煙鍚?
        CONFIRM_RENAME          // 纭閲嶅懡鍚?
    }

    // 鍗曚緥瀹炰緥
    private static RenameManager instance;

    // 褰撳墠鐘舵€?
    private RenameState currentState = RenameState.IDLE;

    // 鍩熷悕鏁版嵁鏀堕泦
    private String selectedAreaName = null;      // 閫変腑鐨勫煙鍚嶅悕绉?
    private String newAreaName = null;           // 鏂板煙鍚嶅悕绉?
    private String newSurfaceName = null;        // 鏂拌仈鍚堝煙鍚?
    private String currentDimension = null;      // 褰撳墠缁村害
    private List<AreaData> availableAreas = new ArrayList<>();  // 鍙噸鍛藉悕鐨勫煙鍚嶅垪琛?

    // 鑱婂ぉ鐩戝惉鍣ㄦ敞鍐岀姸鎬?
    private boolean chatListenerRegistered = false;

    // 绉佹湁鏋勯€犲嚱鏁帮紙鍗曚緥妯″紡锛?
    private RenameManager() {}

    /**
     * 鑾峰彇鍗曚緥瀹炰緥
     */
    public static RenameManager getInstance() {
        if (instance == null) {
            instance = new RenameManager();
        }
        return instance;
    }

    /**
     * 鍚姩Rename娴佺▼
     * @param areas 鍙噸鍛藉悕鐨勫煙鍚嶅垪琛?
     * @param dimension 褰撳墠缁村害
     */
    public void startRename(List<AreaData> areas, String dimension) {
        if (currentState != RenameState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_4")), false);
            return;
        }

        this.availableAreas = new ArrayList<>(areas);
        this.currentDimension = dimension;

        if (areas.isEmpty()) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.area.dimension.rename_2")), false);
            return;
        }

        // 娉ㄥ唽鑱婂ぉ鐩戝惉鍣?
        registerChatListener();

        // 璁剧疆鐘舵€佸苟鏄剧ずUI
        currentState = RenameState.SELECT_AREA;
        RenameUI.showAreaSelectScreen(availableAreas);
    }

    /**
     * 娉ㄥ唽鑱婂ぉ鐩戝惉鍣ㄦ潵鎹曡幏鐢ㄦ埛杈撳叆
     */
    private void registerChatListener() {
        if (!chatListenerRegistered) {
            ClientChatCompat.register(input -> {
                if (currentState != RenameState.IDLE) {
                    handleChatInput(input);
                }
            });
            chatListenerRegistered = true;
        }
    }

    /**
     * 澶勭悊鐢ㄦ埛鑱婂ぉ杈撳叆
     */
    private void handleChatInput(String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 绉婚櫎鍓嶇紑绗﹀彿锛堝鏋滄湁鐨勮瘽锛?
        if (input.startsWith("<") && input.contains(">")) {
            int endIndex = input.indexOf(">") + 1;
            if (endIndex < input.length()) {
                input = input.substring(endIndex).trim();
            }
        }

        switch (currentState) {
            case INPUT_NEW_NAME:
                if (!input.trim().isEmpty()) {
                    newAreaName = input.trim();

                    // 妫€鏌ユ柊鍩熷悕鍚嶇О鏄惁宸插瓨鍦?
                    if (checkAreaNameExists(newAreaName)) {
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.area") + newAreaName + I18nManager.translate("easyadd.message.dimension")), false);
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.name")), false);
                        return;
                    }

                    // 妫€鏌ユ柊鍚嶇О鏄惁涓庡師鍚嶇О鐩稿悓
                    if (newAreaName.equals(selectedAreaName)) {
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.area.name")), false);
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.name")), false);
                        return;
                    }

                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.area.name") + newAreaName), false);

                    // 杩涘叆鑱斿悎鍩熷悕杈撳叆
                    currentState = RenameState.INPUT_NEW_SURFACE_NAME;
                    RenameUI.showSurfaceNameInputScreen();
                }
                break;

            case INPUT_NEW_SURFACE_NAME:
                // 鑱斿悎鍩熷悕鍙互涓虹┖
                newSurfaceName = input.trim().isEmpty() ? null : input.trim();
                if (newSurfaceName != null) {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.area.surface") + newSurfaceName), false);
                } else {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.area.surface")), false);
                }

                // 杩涘叆纭鐘舵€?
                currentState = RenameState.CONFIRM_RENAME;
                RenameUI.showConfirmScreen(selectedAreaName, newAreaName, newSurfaceName);
                break;

            default:
                break;
        }
    }

    /**
     * 澶勭悊鍩熷悕閫夋嫨锛堜粠鍛戒护璋冪敤锛?
     */
    public void handleAreaSelection(String areaName) {
        if (currentState != RenameState.SELECT_AREA) {
            return;
        }

        // 绉婚櫎寮曞彿锛堝鏋滃瓨鍦級
        if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
            areaName = areaName.substring(1, areaName.length() - 1);
        }

        // 楠岃瘉鍩熷悕鏄惁鍦ㄥ彲閫夊垪琛ㄤ腑
        boolean found = false;
        for (AreaData area : availableAreas) {
            if (area.getName().equals(areaName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.area_2") + areaName), false);
            return;
        }

        selectedAreaName = areaName;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.prompt.area") + areaName), false);
        }

        // 杩涘叆鏂板悕绉拌緭鍏ョ姸鎬?
        currentState = RenameState.INPUT_NEW_NAME;
        RenameUI.showNewNameInputScreen();
    }

    /**
     * 纭閲嶅懡鍚?
     */
    public void confirmRename() {
        if (currentState != RenameState.CONFIRM_RENAME) {
            return;
        }

        try {
            // 鍙戦€佸埌鏈嶅姟绔?
            RenameNetworking.sendRenameRequest(selectedAreaName, newAreaName, newSurfaceName, currentDimension);

            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.prompt.rename")), false);

            resetState();

        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("message.error.area.rename") + e.getMessage()), false);
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "閲嶅懡鍚嶅け璐? " + e.getMessage());
        }
    }

    /**
     * 鍙栨秷Rename娴佺▼
     */
    public void cancelRename() {
        MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.cancel_3")), false);
        resetState();
    }

    /**
     * 閲嶇疆鐘舵€?
     */
    private void resetState() {
        currentState = RenameState.IDLE;
        selectedAreaName = null;
        newAreaName = null;
        newSurfaceName = null;
        currentDimension = null;
        availableAreas.clear();
    }

    /**
     * 妫€鏌ュ煙鍚嶅悕绉版槸鍚﹀凡瀛樺湪浜庡綋鍓嶇淮搴?
     * @param areaName 瑕佹鏌ョ殑鍩熷悕鍚嶇О
     * @return 濡傛灉鍩熷悕鍚嶇О宸插瓨鍦ㄨ繑鍥瀟rue锛屽惁鍒欒繑鍥瀎alse
     */
    private boolean checkAreaNameExists(String areaName) {
        try {
            String fileName = getFileNameForCurrentDimension();
            if (fileName == null) {
                ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                    "鏃犳硶纭畾褰撳墠缁村害鏂囦欢鍚嶏紝璺宠繃鏌ラ噸");
                return false;
            }

            // 璇诲彇褰撳墠缁村害鐨勬墍鏈夊煙鍚嶆暟鎹?
            List<AreaData> existingAreas = FileManager.readAreaData(
                areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName));

            // 妫€鏌ユ槸鍚﹀瓨鍦ㄧ浉鍚岀殑鍩熷悕鍚嶇О锛坣ame瀛楁锛?
            for (AreaData area : existingAreas) {
                if (area.getName().equals(areaName)) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                        "鍙戠幇閲嶅鍩熷悕鍚嶇О: " + areaName);
                    return true;
                }
            }

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "鍩熷悕鍚嶇О \"" + areaName + "\" 鏈噸澶嶏紝鍙互浣跨敤");
            return false;

        } catch (Exception e) {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "妫€鏌ュ煙鍚嶅悕绉版椂鍙戠敓閿欒: " + e.getMessage());
            return false;
        }
    }

    /**
     * 鑾峰彇褰撳墠缁村害鐨勬枃浠跺悕
     */
    private String getFileNameForCurrentDimension() {
        if (currentDimension == null) return null;

        if (currentDimension.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (currentDimension.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (currentDimension.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return null;
    }

    // Getters
    public RenameState getCurrentState() { return currentState; }
    public String getSelectedAreaName() { return selectedAreaName; }
    public String getNewAreaName() { return newAreaName; }
    public String getNewSurfaceName() { return newSurfaceName; }
}
