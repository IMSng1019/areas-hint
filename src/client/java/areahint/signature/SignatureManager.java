package areahint.signature;

import areahint.data.AreaData;
import areahint.dimensional.ClientDimensionalNameManager;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.world.ClientWorldFolderManager;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Signature客户端交互状态管理。
 *
 * <p>此类只负责客户端交互和最终请求发送，真正的权限判断、文件读取和写入仍由服务端再次执行。
 * 这样玩家即使伪造客户端数据，也不能绕过服务端的basename引用权限检查。</p>
 */
public class SignatureManager {
    private static SignatureManager instance;
    private static boolean initialized;

    /**
     * 签名操作类型。
     */
    public enum Operation {
        ADD,
        DELETE
    }

    /**
     * 交互流程状态。
     *
     * <p>删除签名拆成两次确认：第一次确认玩家和域名，第二次确认真正删除。</p>
     */
    private enum State {
        IDLE,
        SELECT_AREA,
        INPUT_PLAYER_NAME,
        CONFIRM_ADD,
        CONFIRM_DELETE,
        FINAL_DELETE_CONFIRM
    }

    private final MinecraftClient client;
    private Operation operation;
    private State state = State.IDLE;
    private AreaData selectedArea;
    private String targetPlayerName;
    private String currentDimension;
    private boolean active;
    private boolean currentPlayerAdmin;
    private final List<AreaData> selectableAreas = new ArrayList<>();

    private SignatureManager() {
        this.client = MinecraftClient.getInstance();
    }

    public static SignatureManager getInstance() {
        if (instance == null) {
            instance = new SignatureManager();
        }
        return instance;
    }

    /**
     * 注册聊天输入拦截器。
     *
     * <p>当流程处于玩家名输入阶段时，普通聊天内容会被当作玩家名处理并阻止发出。
     * 命令输入仍然走 /areahint addsignature name 或 /areahint deletesignature name 分支。</p>
     */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            SignatureManager manager = getInstance();
            if (manager.active && manager.state == State.INPUT_PLAYER_NAME) {
                manager.handleChatInput(message);
                return false;
            }
            return true;
        });
    }

    public void startAdd() {
        start(Operation.ADD);
    }

    public void startDelete() {
        start(Operation.DELETE);
    }

    /**
     * 启动添加或删除扩展签名流程。
     */
    private void start(Operation nextOperation) {
        if (client.player == null || client.world == null) {
            return;
        }
        if (active) {
            sendMessage("当前已有签名操作正在进行", Formatting.RED);
            return;
        }

        resetStateOnly();
        this.operation = nextOperation;
        this.state = State.SELECT_AREA;
        this.active = true;
        this.currentDimension = client.world.getRegistryKey().getValue().toString();
        this.currentPlayerAdmin = client.player.hasPermissionLevel(2);

        selectableAreas.clear();
        selectableAreas.addAll(loadModifiableAreas());
        if (selectableAreas.isEmpty()) {
            sendMessage("没有可修改签名的域名", Formatting.RED);
            reset();
            return;
        }

        SignatureUI.showAreaSelectionScreen(operation, selectableAreas, currentPlayerAdmin);
    }

    /**
     * 选择要修改签名的域名。
     */
    public void selectArea(String areaName) {
        if (!active || state != State.SELECT_AREA || client.player == null) {
            return;
        }

        String cleanedName = stripQuotes(areaName);
        AreaData area = findAreaByName(cleanedName, selectableAreas);
        if (area == null) {
            sendMessage("无权修改或未找到域名：" + cleanedName, Formatting.RED);
            return;
        }

        if (operation == Operation.DELETE && getRemovableSignatures(area).isEmpty()) {
            sendMessage("该域名没有可删除的扩展签名：" + cleanedName, Formatting.RED);
            SignatureUI.showAreaSelectionScreen(operation, selectableAreas, currentPlayerAdmin);
            return;
        }

        this.selectedArea = area;
        this.state = State.INPUT_PLAYER_NAME;
        SignatureUI.showPlayerNamePrompt(operation, selectedArea, getRemovableSignatures(selectedArea));
    }

    /**
     * 设置目标玩家名。添加时要求该玩家尚未在全部签名中；删除时要求该玩家存在于扩展签名列表中。
     */
    public void setPlayerName(String playerName) {
        if (!active || state != State.INPUT_PLAYER_NAME || selectedArea == null || client.player == null) {
            return;
        }

        String cleanedPlayerName = stripQuotes(playerName);
        if (cleanedPlayerName.isEmpty()) {
            sendMessage("玩家名不能为空", Formatting.RED);
            SignatureUI.showPlayerNamePrompt(operation, selectedArea, getRemovableSignatures(selectedArea));
            return;
        }

        if (operation == Operation.ADD && selectedArea.hasSignature(cleanedPlayerName)) {
            sendMessage("该玩家已经是此域名签名：" + cleanedPlayerName, Formatting.RED);
            SignatureUI.showPlayerNamePrompt(operation, selectedArea, getRemovableSignatures(selectedArea));
            return;
        }
        if (operation == Operation.DELETE && !hasExtensionSignature(selectedArea, cleanedPlayerName)) {
            sendMessage("扩展签名中不存在该玩家：" + cleanedPlayerName, Formatting.RED);
            SignatureUI.showPlayerNamePrompt(operation, selectedArea, getRemovableSignatures(selectedArea));
            return;
        }

        this.targetPlayerName = cleanedPlayerName;
        this.state = operation == Operation.ADD ? State.CONFIRM_ADD : State.CONFIRM_DELETE;
        SignatureUI.showConfirmScreen(operation, selectedArea, targetPlayerName);
    }

    /**
     * 处理普通聊天输入的玩家名。
     */
    public boolean handleChatInput(String input) {
        if (!active || state != State.INPUT_PLAYER_NAME) {
            return false;
        }

        String cleanedInput = input == null ? "" : input.trim();
        if ("cancel".equalsIgnoreCase(cleanedInput) || "取消".equals(cleanedInput)) {
            cancel();
            return true;
        }

        setPlayerName(cleanedInput);
        return true;
    }

    /**
     * 第一层确认。
     *
     * <p>添加签名只有一层确认；删除签名会进入第二层确认界面。</p>
     */
    public void confirm() {
        if (!active || selectedArea == null || targetPlayerName == null) {
            return;
        }

        if (state == State.CONFIRM_ADD) {
            submitToServer();
            return;
        }

        if (state == State.CONFIRM_DELETE) {
            state = State.FINAL_DELETE_CONFIRM;
            SignatureUI.showFinalDeleteConfirmScreen(selectedArea, targetPlayerName);
        }
    }

    /**
     * 删除签名的第二层确认。
     */
    public void confirmDeleteFinal() {
        if (!active || state != State.FINAL_DELETE_CONFIRM || selectedArea == null || targetPlayerName == null) {
            return;
        }

        submitToServer();
    }

    /**
     * 向服务端发送最终请求。服务端会重新读取域名文件并再次校验权限。
     */
    private void submitToServer() {
        if (client.world == null || selectedArea == null || selectedArea.getName() == null) {
            sendMessage("当前世界或域名不可用", Formatting.RED);
            reset();
            return;
        }

        // 签名流程开始时已经锁定了维度，提交时继续使用同一个维度，避免玩家中途切维度后写错文件。
        String dimension = currentDimension != null
            ? currentDimension
            : client.world.getRegistryKey().getValue().toString();
        String operationName = operation == Operation.ADD ? "add" : "delete";
        SignatureClientNetworking.sendToServer(operationName, selectedArea.getName(), dimension, targetPlayerName);
        sendMessage("签名请求已发送到服务端处理", Formatting.GREEN);
        reset();
    }

    public void cancel() {
        if (active) {
            sendMessage("已取消签名操作", Formatting.YELLOW);
        }
        reset();
    }

    /**
     * 读取当前维度中客户端认为可修改签名的域名。
     *
     * <p>普通玩家只显示其签名域名被base-name引用的下级域名；管理员显示当前维度全部域名。
     * 服务端仍会执行同样的权限判断作为最终依据。</p>
     */
    private List<AreaData> loadModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        if (client.player == null || client.world == null) {
            return result;
        }

        String playerName = client.player.getGameProfile().getName();
        List<AreaData> allAreas = loadCurrentDimensionAreas();

        for (AreaData area : allAreas) {
            if (area == null || area.getName() == null) {
                continue;
            }
            if (currentPlayerAdmin || isBaseSignedByPlayer(area, allAreas, playerName)) {
                result.add(area);
            }
        }

        return result;
    }

    private boolean isBaseSignedByPlayer(AreaData area, List<AreaData> allAreas, String playerName) {
        if (area == null || area.getBaseName() == null || area.getBaseName().trim().isEmpty()) {
            return false;
        }
        AreaData baseArea = findAreaByName(area.getBaseName(), allAreas);
        return baseArea != null && baseArea.hasSignature(playerName)
            || isDimensionalBaseSignedByPlayer(area.getBaseName(), playerName);
    }

    /**
     * 客户端预筛选也要识别维度域名签名。
     *
     * <p>最终权限仍由服务端判断；这里仅用于让普通玩家在交互列表里看到
     * base-name 指向其维度域名签名的域名。</p>
     */
    private boolean isDimensionalBaseSignedByPlayer(String baseName, String playerName) {
        String cleanedBaseName = cleanSignature(baseName);
        String cleanedPlayerName = cleanSignature(playerName);
        if (cleanedBaseName == null || cleanedPlayerName == null) {
            return false;
        }

        if (ClientDimensionalNameManager.isSignedBy(cleanedBaseName, cleanedPlayerName)) {
            return true;
        }

        for (java.util.Map.Entry<String, String> entry : ClientDimensionalNameManager.getAllDimensionalNames().entrySet()) {
            if (cleanedBaseName.equals(entry.getKey()) || cleanedBaseName.equals(entry.getValue())) {
                return ClientDimensionalNameManager.isSignedBy(entry.getKey(), cleanedPlayerName);
            }
        }

        return false;
    }

    private List<AreaData> loadCurrentDimensionAreas() {
        try {
            String fileName = getFileNameForCurrentDimension();
            if (fileName == null) {
                return new ArrayList<>();
            }

            Path path = ClientWorldFolderManager.getWorldDimensionFile(fileName);
            return FileManager.readAreaData(path);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String getFileNameForCurrentDimension() {
        String dimension = currentDimension;
        if (dimension == null && client.world != null) {
            dimension = client.world.getRegistryKey().getValue().toString();
        }
        if (dimension == null) {
            return null;
        }

        String dimensionType = convertDimensionIdToType(dimension);
        return Packets.getFileNameForDimension(dimensionType);
    }

    /**
     * 将完整维度ID或维度路径转换为模组内部维度类型。
     *
     * <p>这里必须精确比较 path，不能用 contains，否则类似 other:end_city
     * 这样的自定义维度会被误判为末地文件。</p>
     */
    private String convertDimensionIdToType(String dimension) {
        if (dimension == null) {
            return null;
        }

        String normalized = dimension.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }

        int colonIndex = normalized.lastIndexOf(':');
        String dimensionPath = colonIndex >= 0 ? normalized.substring(colonIndex + 1) : normalized;
        return Packets.convertDimensionPathToType(dimensionPath);
    }

    private AreaData findAreaByName(String name, List<AreaData> areas) {
        if (name == null || areas == null) {
            return null;
        }
        for (AreaData area : areas) {
            if (area != null && name.equals(area.getName())) {
                return area;
            }
        }
        return null;
    }

    private List<String> getRemovableSignatures(AreaData area) {
        if (area == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(area.getSignatures());
    }

    private boolean hasExtensionSignature(AreaData area, String playerName) {
        String cleanedPlayerName = stripQuotes(playerName);
        if (area == null || cleanedPlayerName.isEmpty()) {
            return false;
        }
        for (String signature : area.getSignatures()) {
            if (cleanedPlayerName.equals(cleanSignature(signature))) {
                return true;
            }
        }
        return false;
    }

    private String stripQuotes(String value) {
        String cleaned = cleanSignature(value);
        if (cleaned == null) {
            return "";
        }
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
            cleaned = cleaned.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return cleaned.trim();
    }

    private String cleanSignature(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private void sendMessage(String message, Formatting formatting) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(formatting), false);
        }
    }

    private void resetStateOnly() {
        this.operation = null;
        this.state = State.IDLE;
        this.selectedArea = null;
        this.targetPlayerName = null;
        this.currentDimension = null;
        this.active = false;
        this.currentPlayerAdmin = false;
        this.selectableAreas.clear();
    }

    private void reset() {
        resetStateOnly();
    }

    public boolean isActive() {
        return active;
    }
}
