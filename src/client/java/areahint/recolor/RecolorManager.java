package areahint.recolor;

import areahint.AreashintClient;
import areahint.data.AreaData;
import areahint.i18n.I18nManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Recolor鍔熻兘绠＄悊鍣?
 * 璐熻矗浜や簰寮忓煙鍚嶉噸鏂扮潃鑹茬殑鏁翠釜娴佺▼绠＄悊
 */
public class RecolorManager {

    /**
     * Recolor鐘舵€佹灇涓?
     */
    public enum RecolorState {
        IDLE,               // 绌洪棽鐘舵€?
        AREA_SELECTION,     // 鍩熷悕閫夋嫨
        COLOR_SELECTION,    // 棰滆壊閫夋嫨
        CONFIRM_CHANGE      // 纭淇敼
    }

    // 鍗曚緥瀹炰緥
    private static RecolorManager instance;

    // 褰撳墠鐘舵€?
    private RecolorState currentState = RecolorState.IDLE;

    // 鏁版嵁鏀堕泦
    private List<AreaData> editableAreas = new ArrayList<>();
    private String selectedAreaName = null;
    private String selectedColor = null;
    private String currentDimension = null;
    private String originalColor = null;

    // 绉佹湁鏋勯€犲嚱鏁帮紙鍗曚緥妯″紡锛?
    private RecolorManager() {}

    /**
     * 鑾峰彇鍗曚緥瀹炰緥
     */
    public static RecolorManager getInstance() {
        if (instance == null) {
            instance = new RecolorManager();
        }
        return instance;
    }

    /**
     * 鍚姩Recolor娴佺▼
     * @param areas 鍙紪杈戠殑鍩熷悕鍒楄〃
     * @param dimension 褰撳墠缁村害
     */
    public void startRecolor(List<AreaData> areas, String dimension) {
        if (currentState != RecolorState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general_3")), false);
            return;
        }

        this.editableAreas = new ArrayList<>(areas);
        this.currentDimension = dimension;

        // 璁剧疆鐘舵€佸苟鏄剧ずUI
        currentState = RecolorState.AREA_SELECTION;
        RecolorUI.showAreaSelectionScreen(editableAreas);
    }

    /**
     * 澶勭悊鍩熷悕閫夋嫨
     * @param areaName 閫夋嫨鐨勫煙鍚嶅悕绉?
     */
    public void handleAreaSelection(String areaName) {
        if (currentState != RecolorState.AREA_SELECTION) {
            return;
        }

        // 鏌ユ壘閫夋嫨鐨勫煙鍚?
        AreaData selectedArea = null;
        for (AreaData area : editableAreas) {
            if (area.getName().equals(areaName)) {
                selectedArea = area;
                break;
            }
        }

        if (selectedArea == null) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.area_2") + areaName), false);
            return;
        }

        this.selectedAreaName = areaName;
        this.originalColor = selectedArea.getColor();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.prompt.area") + areaName), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.color_3") + originalColor), false);
        }

        // 杩涘叆棰滆壊閫夋嫨鐘舵€?
        currentState = RecolorState.COLOR_SELECTION;
        RecolorUI.showColorSelectionScreen(areaName, originalColor);
    }

    /**
     * 澶勭悊棰滆壊閫夋嫨
     * @param colorInput 棰滆壊杈撳叆
     */
    public void handleColorSelection(String colorInput) {
        if (currentState != RecolorState.COLOR_SELECTION) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 楠岃瘉棰滆壊鏍煎紡
        String normalizedColor = areahint.util.ColorUtil.normalizeColor(colorInput);
        if (!areahint.util.ColorUtil.isValidColor(normalizedColor)) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.error.color")), false);
            return;
        }

        this.selectedColor = normalizedColor;

        // 杩涘叆纭鐘舵€?
        currentState = RecolorState.CONFIRM_CHANGE;
        RecolorUI.showConfirmScreen(selectedAreaName, originalColor, selectedColor);
    }

    /**
     * 纭棰滆壊淇敼
     */
    public void confirmChange() {
        if (currentState != RecolorState.CONFIRM_CHANGE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        try {
            // 鍙戦€侀噸鏂扮潃鑹茶姹傚埌鏈嶅姟绔?
            sendRecolorRequest(selectedAreaName, selectedColor, currentDimension);

            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.prompt.color.modify")), false);

            // 閲嶇疆鐘舵€?
            resetState();

        } catch (Exception e) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.error.general") + e.getMessage()), false);
            AreashintClient.LOGGER.error(I18nManager.translate("message.error.general_32"), e);
        }
    }

    /**
     * 鍙栨秷Recolor娴佺▼
     */
    public void cancelRecolor() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.cancel_2")), false);
        }
        resetState();
    }

    /**
     * 閲嶇疆鐘舵€?
     */
    private void resetState() {
        currentState = RecolorState.IDLE;
        editableAreas.clear();
        selectedAreaName = null;
        selectedColor = null;
        currentDimension = null;
        originalColor = null;
    }

    /**
     * 鍙戦€侀噸鏂扮潃鑹茶姹傚埌鏈嶅姟绔?
     * @param areaName 鍩熷悕鍚嶇О
     * @param color 鏂伴鑹?
     * @param dimension 缁村害
     */
    private void sendRecolorRequest(String areaName, String color, String dimension) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(areaName);
            buf.writeString(color);
            buf.writeString(dimension);

            ClientPlayNetworking.send(areahint.network.Packets.C2S_RECOLOR_REQUEST, buf);

            AreashintClient.LOGGER.info(I18nManager.translate("message.prompt.area.color.dimension"),
                areaName, color, dimension);

        } catch (Exception e) {
            AreashintClient.LOGGER.error(I18nManager.translate("message.error.general_30") + e.getMessage(), e);
        }
    }

    // Getters
    public RecolorState getCurrentState() { return currentState; }
    public String getSelectedAreaName() { return selectedAreaName; }
    public String getSelectedColor() { return selectedColor; }
    public String getOriginalColor() { return originalColor; }
}
