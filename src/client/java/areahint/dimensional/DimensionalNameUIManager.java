package areahint.dimensional;

import areahint.AreashintClient;
import areahint.i18n.I18nManager;
import areahint.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;

/**
 * 瀹㈡埛绔淮搴﹀煙鍚嶄氦浜掔鐞嗗櫒
 * 绠＄悊 dimensionalityname 鍜?dimensionalitycolor 鐨勪氦浜掓祦绋?
 */
public class DimensionalNameUIManager {

    public enum State {
        IDLE,
        // dimname flow
        DIMNAME_SELECT,
        DIMNAME_INPUT,
        DIMNAME_CONFIRM,
        // dimcolor flow
        DIMCOLOR_SELECT,
        DIMCOLOR_INPUT,
        DIMCOLOR_CONFIRM
    }

    private static DimensionalNameUIManager instance;
    private State currentState = State.IDLE;
    private String selectedDimensionId = null;
    private String newName = null;
    private String newColor = null;
    private String originalName = null;
    private String originalColor = null;

    private DimensionalNameUIManager() {}

    public static DimensionalNameUIManager getInstance() {
        if (instance == null) instance = new DimensionalNameUIManager();
        return instance;
    }

    // ===== dimensionalityname 娴佺▼ =====

    public void startDimName() {
        if (currentState != State.IDLE) {
            sendMsg(I18nManager.translate("gui.error.general_3"));
            return;
        }
        currentState = State.DIMNAME_SELECT;
        DimensionalNameUI.showDimensionSelectionScreen("dimname");
    }

    public void handleDimNameSelect(String dimensionId) {
        if (currentState != State.DIMNAME_SELECT) return;
        this.selectedDimensionId = dimensionId;
        this.originalName = ClientDimensionalNameManager.getDimensionalName(dimensionId);
        currentState = State.DIMNAME_INPUT;
        DimensionalNameUI.showNameInputScreen(dimensionId, originalName);
    }

    public void handleDimNameInput(String name) {
        if (currentState != State.DIMNAME_INPUT) return;
        this.newName = name;
        currentState = State.DIMNAME_CONFIRM;
        DimensionalNameUI.showNameConfirmScreen(selectedDimensionId, originalName, newName);
    }

    public void confirmDimName() {
        if (currentState != State.DIMNAME_CONFIRM) return;
        // 鍙戦€佸懡浠ゅ埌鏈嶅姟绔墽琛屽疄闄呮洿鏀?
        sendServerCommand("dimname_apply:" + selectedDimensionId + ":" + newName);
        sendMsg(I18nManager.translate("gui.prompt.area.dimension.modify"));
        resetState();
    }

    // ===== dimensionalitycolor 娴佺▼ =====

    public void startDimColor() {
        if (currentState != State.IDLE) {
            sendMsg(I18nManager.translate("gui.error.general_3"));
            return;
        }
        currentState = State.DIMCOLOR_SELECT;
        DimensionalNameUI.showDimensionSelectionScreen("dimcolor");
    }

    public void handleDimColorSelect(String dimensionId) {
        if (currentState != State.DIMCOLOR_SELECT) return;
        this.selectedDimensionId = dimensionId;
        String dimName = ClientDimensionalNameManager.getDimensionalName(dimensionId);
        this.originalColor = ClientDimensionalNameManager.getDimensionalColor(dimensionId);
        if (originalColor == null) originalColor = "#FFFFFF";
        currentState = State.DIMCOLOR_INPUT;
        DimensionalNameUI.showColorSelectionScreen(dimensionId, dimName, originalColor);
    }

    public void handleDimColorInput(String colorInput) {
        if (currentState != State.DIMCOLOR_INPUT) return;
        String normalized = ColorUtil.normalizeColor(colorInput);
        if (!ColorUtil.isValidColor(normalized)) {
            sendMsg(I18nManager.translate("gui.error.color"));
            return;
        }
        this.newColor = normalized;
        String dimName = ClientDimensionalNameManager.getDimensionalName(selectedDimensionId);
        currentState = State.DIMCOLOR_CONFIRM;
        DimensionalNameUI.showColorConfirmScreen(selectedDimensionId, dimName, originalColor, newColor);
    }

    public void confirmDimColor() {
        if (currentState != State.DIMCOLOR_CONFIRM) return;
        sendServerCommand("dimcolor_apply:" + selectedDimensionId + ":" + newColor);
        sendMsg(I18nManager.translate("gui.prompt.area.color.dimension"));
        resetState();
    }

    // ===== 閫氱敤 =====

    public void cancel() {
        sendMsg(I18nManager.translate("gui.message.cancel"));
        resetState();
    }

    private void resetState() {
        currentState = State.IDLE;
        selectedDimensionId = null;
        newName = null;
        newColor = null;
        originalName = null;
        originalColor = null;
    }

    private void sendMsg(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(msg), false);
        }
    }

    /**
     * 閫氳繃鑱婂ぉ鍛戒护鍙戦€佽姹傚埌鏈嶅姟绔?
     */
    private void sendServerCommand(String action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 閫氳繃缃戠粶鍙戦€佸埌鏈嶅姟绔鐞?
            areahint.network.ClientDimNameNetworking.sendToServer(action);
        }
    }

    public State getCurrentState() { return currentState; }
}
