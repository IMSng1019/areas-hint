package areahint.signature;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.world.ClientWorldFolderManager;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Signature客户端交互状态管理
 */
public class SignatureManager {
    private static SignatureManager instance;
    private static boolean initialized;

    public enum Operation {
        ADD,
        DELETE
    }

    private enum State {
        IDLE,
        SELECT_AREA,
        INPUT_PLAYER_NAME,
        CONFIRM
    }

    private Operation operation;
    private AreaData selectedArea;
    private String targetPlayerName;
    private boolean active;
    private State state;
    private final MinecraftClient client;

    private SignatureManager() {
        this.client = MinecraftClient.getInstance();
        this.state = State.IDLE;
    }

    public static SignatureManager getInstance() {
        if (instance == null) {
            instance = new SignatureManager();
        }
        return instance;
    }

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

    private void start(Operation operation) {
        if (client.player == null || client.world == null) return;

        resetStateOnly();
        this.operation = operation;
        this.active = true;
        this.state = State.SELECT_AREA;

        List<AreaData> areas = getModifiableAreas();
        if (areas.isEmpty()) {
            sendMessage("没有可修改的域名", Formatting.RED);
            reset();
            return;
        }

        SignatureUI.showAreaSelectionScreen(operation, areas);
    }

    public void selectArea(String areaName) {
        if (!active || state != State.SELECT_AREA || client.player == null) return;

        String cleanedName = stripQuotes(areaName);
        AreaData area = null;
        for (AreaData candidate : getModifiableAreas()) {
            if (candidate.getName() != null && candidate.getName().equals(cleanedName)) {
                area = candidate;
                break;
            }
        }

        if (area == null) {
            sendMessage("无权修改或未找到域名：" + cleanedName, Formatting.RED);
            return;
        }

        this.selectedArea = area;
        this.state = State.INPUT_PLAYER_NAME;
        SignatureUI.showPlayerNamePrompt(operation, selectedArea);
    }

    public void setPlayerName(String playerName) {
        if (!active || state != State.INPUT_PLAYER_NAME || client.player == null) return;

        String cleanedPlayerName = playerName == null ? "" : playerName.trim();
        if (cleanedPlayerName.isEmpty()) {
            sendMessage("玩家名不能为空", Formatting.RED);
            return;
        }

        this.targetPlayerName = cleanedPlayerName;
        this.state = State.CONFIRM;
        SignatureUI.showConfirmScreen(operation, selectedArea, targetPlayerName);
    }

    public boolean handleChatInput(String input) {
        if (!active || state != State.INPUT_PLAYER_NAME) {
            return false;
        }
        String cleanedInput = input == null ? "" : input.trim();
        if ("cancel".equalsIgnoreCase(cleanedInput) || "取消".equals(cleanedInput)) {
            cancel();
            return true;
        }
        setPlayerName(input);
        return true;
    }

    public void confirm() {
        if (!active || state != State.CONFIRM || selectedArea == null || targetPlayerName == null || targetPlayerName.trim().isEmpty()) {
            return;
        }
        if (client.world == null) {
            sendMessage("当前世界不可用", Formatting.RED);
            reset();
            return;
        }

        if (operation == Operation.ADD) {
            selectedArea.addSignature(targetPlayerName);
        } else if (operation == Operation.DELETE) {
            selectedArea.removeSignature(targetPlayerName);
        }

        String dimension = client.world.getRegistryKey().getValue().toString();
        SignatureClientNetworking.sendToServer(
            operation == Operation.ADD ? "add" : "delete",
            selectedArea,
            dimension,
            targetPlayerName
        );
        reset();
    }

    public void cancel() {
        if (active) {
            sendMessage("已取消签名操作", Formatting.YELLOW);
        }
        reset();
    }

    private List<AreaData> getModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        if (client.player == null || client.world == null) return result;

        String playerName = client.player.getGameProfile().getName();
        boolean isAdmin = client.player.hasPermissionLevel(2);
        List<AreaData> allAreas = loadCurrentDimensionAreas();

        for (AreaData area : allAreas) {
            if (isAdmin) {
                result.add(area);
            } else if (area.hasSignature(playerName)) {
                result.add(area);
            } else if (area.getBaseName() != null) {
                AreaData baseArea = findAreaByName(area.getBaseName(), allAreas);
                if (baseArea != null && baseArea.hasSignature(playerName)) {
                    result.add(area);
                }
            }
        }
        return result;
    }

    private List<AreaData> loadCurrentDimensionAreas() {
        try {
            if (client.world == null) return new ArrayList<>();
            String dimension = client.world.getRegistryKey().getValue().toString();
            String fileName = getFileNameForDimension(dimension);
            if (fileName == null) return new ArrayList<>();

            Path path = ClientWorldFolderManager.getWorldDimensionFile(fileName);
            return FileManager.readAreaData(path);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String getFileNameForDimension(String dimension) {
        if (dimension == null) return null;
        String lower = dimension.toLowerCase();
        if (lower.contains("overworld")) return Areashint.OVERWORLD_FILE;
        if (lower.contains("nether")) return Areashint.NETHER_FILE;
        if (lower.contains("end")) return Areashint.END_FILE;
        return null;
    }

    private AreaData findAreaByName(String name, List<AreaData> areas) {
        if (name == null) return null;
        for (AreaData area : areas) {
            if (name.equals(area.getName())) {
                return area;
            }
        }
        return null;
    }

    private String stripQuotes(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
            cleaned = cleaned.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return cleaned;
    }

    private void sendMessage(String message, Formatting formatting) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(formatting), false);
        }
    }

    private void resetStateOnly() {
        this.operation = null;
        this.selectedArea = null;
        this.targetPlayerName = null;
        this.active = false;
        this.state = State.IDLE;
    }

    public boolean isActive() {
        return active;
    }

    private void reset() {
        resetStateOnly();
    }
}
