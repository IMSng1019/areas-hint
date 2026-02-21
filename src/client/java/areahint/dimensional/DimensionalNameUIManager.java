package areahint.dimensional;

import areahint.AreashintClient;
import areahint.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;

/**
 * 客户端维度域名交互管理器
 * 管理 dimensionalityname 和 dimensionalitycolor 的交互流程
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

    // ===== dimensionalityname 流程 =====

    public void startDimName() {
        if (currentState != State.IDLE) {
            sendMsg("§c当前已有操作在进行中");
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
        // 发送命令到服务端执行实际更改
        sendServerCommand("dimname_apply:" + selectedDimensionId + ":" + newName);
        sendMsg("§a正在处理维度域名修改请求...");
        resetState();
    }

    // ===== dimensionalitycolor 流程 =====

    public void startDimColor() {
        if (currentState != State.IDLE) {
            sendMsg("§c当前已有操作在进行中");
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
            sendMsg("§c无效的颜色格式，请重新选择");
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
        sendMsg("§a正在处理维度域名颜色修改请求...");
        resetState();
    }

    // ===== 通用 =====

    public void cancel() {
        sendMsg("§7操作已取消");
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
            client.player.sendMessage(Text.of(msg), false);
        }
    }

    /**
     * 通过聊天命令发送请求到服务端
     */
    private void sendServerCommand(String action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 通过网络发送到服务端处理
            areahint.network.ClientDimNameNetworking.sendToServer(action);
        }
    }

    public State getCurrentState() { return currentState; }
}
