package areahint.description;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端域名描述交互状态机。
 */
public class DescriptionManager {
    private static DescriptionManager instance;

    public enum State {
        IDLE,
        ADD_SEARCH_INPUT,
        ADD_SELECT_AREA,
        ADD_DESCRIPTION_INPUT,
        ADD_CONFIRM,
        DELETE_SEARCH_INPUT,
        DELETE_SELECT_AREA,
        DELETE_CONFIRM_FIRST,
        DELETE_CONFIRM_SECOND,
        ADD_DIM_SEARCH_INPUT,
        ADD_DIM_SELECT,
        ADD_DIM_DESCRIPTION_INPUT,
        ADD_DIM_CONFIRM,
        DELETE_DIM_SEARCH_INPUT,
        DELETE_DIM_SELECT,
        DELETE_DIM_CONFIRM_FIRST,
        DELETE_DIM_CONFIRM_SECOND
    }

    private State state = State.IDLE;
    private String operation;
    private String targetType;
    private String currentDimension;
    private final List<DescriptionListEntry> entries = new ArrayList<>();
    private DescriptionListEntry selectedEntry;
    private String pendingDescription;

    private DescriptionManager() {
    }

    public static DescriptionManager getInstance() {
        if (instance == null) {
            instance = new DescriptionManager();
        }
        return instance;
    }

    public void handleClientCommand(String action) {
        if (action.equals("adddescription_start")) {
            startAreaAdd();
        } else if (action.startsWith("adddescription_search:")) {
            searchAreaAdd(action.substring("adddescription_search:".length()));
        } else if (action.startsWith("adddescription_select:")) {
            select(action.substring("adddescription_select:".length()));
        } else if (action.startsWith("adddescription_text:")) {
            setDescription(action.substring("adddescription_text:".length()));
        } else if (action.equals("adddescription_confirm")) {
            confirmAdd();
        } else if (action.equals("adddescription_cancel")) {
            cancel();
        } else if (action.equals("deletedescription_start")) {
            startAreaDelete();
        } else if (action.startsWith("deletedescription_search:")) {
            searchAreaDelete(action.substring("deletedescription_search:".length()));
        } else if (action.startsWith("deletedescription_select:")) {
            select(action.substring("deletedescription_select:".length()));
        } else if (action.equals("deletedescription_confirm")) {
            confirmDeleteFirst();
        } else if (action.equals("deletedescription_confirm2")) {
            confirmDeleteSecond();
        } else if (action.equals("deletedescription_cancel")) {
            cancel();
        } else if (action.equals("adddimensionalitydescription_start")) {
            startDimAdd();
        } else if (action.startsWith("adddimensionalitydescription_search:")) {
            searchDimAdd(action.substring("adddimensionalitydescription_search:".length()));
        } else if (action.startsWith("adddimensionalitydescription_select:")) {
            select(action.substring("adddimensionalitydescription_select:".length()));
        } else if (action.startsWith("adddimensionalitydescription_text:")) {
            setDescription(action.substring("adddimensionalitydescription_text:".length()));
        } else if (action.equals("adddimensionalitydescription_confirm")) {
            confirmAdd();
        } else if (action.equals("adddimensionalitydescription_cancel")) {
            cancel();
        } else if (action.equals("deletedimensionalitydescription_start")) {
            startDimDelete();
        } else if (action.startsWith("deletedimensionalitydescription_search:")) {
            searchDimDelete(action.substring("deletedimensionalitydescription_search:".length()));
        } else if (action.startsWith("deletedimensionalitydescription_select:")) {
            select(action.substring("deletedimensionalitydescription_select:".length()));
        } else if (action.equals("deletedimensionalitydescription_confirm")) {
            confirmDeleteFirst();
        } else if (action.equals("deletedimensionalitydescription_confirm2")) {
            confirmDeleteSecond();
        } else if (action.equals("deletedimensionalitydescription_cancel")) {
            cancel();
        }
    }

    private void startAreaAdd() {
        start("add", "area", State.ADD_SEARCH_INPUT, "adddescription", false);
    }

    private void startAreaDelete() {
        start("delete", "area", State.DELETE_SEARCH_INPUT, "deletedescription", false);
    }

    private void startDimAdd() {
        start("add", "dimension", State.ADD_DIM_SEARCH_INPUT, "adddimensionalitydescription", true);
    }

    private void startDimDelete() {
        start("delete", "dimension", State.DELETE_DIM_SEARCH_INPUT, "deletedimensionalitydescription", true);
    }

    private void start(String operation, String targetType, State nextState, String commandPrefix, boolean dimensionTarget) {
        if (isActive()) {
            sendMessage("已有描述交互流程正在进行", Formatting.RED);
            return;
        }
        this.operation = operation;
        this.targetType = targetType;
        this.currentDimension = getCurrentDimensionType();
        this.state = nextState;
        DescriptionUI.showSearchPrompt(commandPrefix, dimensionTarget, "delete".equals(operation));
    }

    private void searchAreaAdd(String query) {
        if (state != State.ADD_SEARCH_INPUT) return;
        state = State.ADD_SELECT_AREA;
        DescriptionClientNetworking.sendListRequest("add", "area", currentDimension, query);
    }

    private void searchAreaDelete(String query) {
        if (state != State.DELETE_SEARCH_INPUT) return;
        state = State.DELETE_SELECT_AREA;
        DescriptionClientNetworking.sendListRequest("delete", "area", currentDimension, query);
    }

    private void searchDimAdd(String query) {
        if (state != State.ADD_DIM_SEARCH_INPUT) return;
        state = State.ADD_DIM_SELECT;
        DescriptionClientNetworking.sendListRequest("add", "dimension", currentDimension, query);
    }

    private void searchDimDelete(String query) {
        if (state != State.DELETE_DIM_SEARCH_INPUT) return;
        state = State.DELETE_DIM_SELECT;
        DescriptionClientNetworking.sendListRequest("delete", "dimension", currentDimension, query);
    }

    public void receiveList(String operation, String targetType, String dimension, List<DescriptionListEntry> entries) {
        if (!isActive()) {
            return;
        }
        if (!isMatchingListResponse(operation, targetType, dimension)) {
            return;
        }
        this.entries.clear();
        this.entries.addAll(entries);
        if (entries.isEmpty()) {
            sendMessage("没有找到可操作的目标", Formatting.RED);
            reset();
            return;
        }
        DescriptionUI.showSelection(getCommandPrefix(), entries);
    }

    private void select(String rawId) {
        if (!isSelectState()) {
            return;
        }
        String id = stripQuotes(rawId);
        for (DescriptionListEntry entry : entries) {
            if (entry.id().equals(id)) {
                selectedEntry = entry;
                if (isDeleteOperation()) {
                    state = isDimensionTarget() ? State.DELETE_DIM_CONFIRM_FIRST : State.DELETE_CONFIRM_FIRST;
                    DescriptionUI.showDeleteConfirmFirst(getCommandPrefix(), selectedEntry);
                } else {
                    state = isDimensionTarget() ? State.ADD_DIM_DESCRIPTION_INPUT : State.ADD_DESCRIPTION_INPUT;
                    DescriptionUI.showDescriptionInput(getCommandPrefix(), selectedEntry);
                }
                return;
            }
        }
        sendMessage("未找到所选目标：" + id, Formatting.RED);
    }

    private void setDescription(String description) {
        if (state != State.ADD_DESCRIPTION_INPUT && state != State.ADD_DIM_DESCRIPTION_INPUT) {
            return;
        }
        String cleaned = description == null ? "" : description.trim();
        if (cleaned.isEmpty()) {
            sendMessage("描述不能为空", Formatting.RED);
            return;
        }
        if (cleaned.length() > DescriptionServerNetworking.MAX_DESCRIPTION_LENGTH) {
            sendMessage("描述过长，最多 32767 个字符", Formatting.RED);
            return;
        }
        pendingDescription = cleaned;
        state = isDimensionTarget() ? State.ADD_DIM_CONFIRM : State.ADD_CONFIRM;
        DescriptionUI.showAddConfirm(getCommandPrefix(), selectedEntry, pendingDescription);
    }

    private void confirmAdd() {
        if ((state != State.ADD_CONFIRM && state != State.ADD_DIM_CONFIRM) || selectedEntry == null || pendingDescription == null) {
            return;
        }
        if (DescriptionClientNetworking.sendWrite(targetType, currentDimension, selectedEntry.id(), pendingDescription)) {
            sendMessage("已发送保存请求", Formatting.YELLOW);
        }
    }

    private void confirmDeleteFirst() {
        if (state != State.DELETE_CONFIRM_FIRST && state != State.DELETE_DIM_CONFIRM_FIRST) {
            return;
        }
        state = isDimensionTarget() ? State.DELETE_DIM_CONFIRM_SECOND : State.DELETE_CONFIRM_SECOND;
        DescriptionUI.showDeleteConfirmSecond(getCommandPrefix(), selectedEntry);
    }

    private void confirmDeleteSecond() {
        if ((state != State.DELETE_CONFIRM_SECOND && state != State.DELETE_DIM_CONFIRM_SECOND) || selectedEntry == null) {
            return;
        }
        DescriptionClientNetworking.sendDelete(targetType, currentDimension, selectedEntry.id());
        sendMessage("已发送删除请求", Formatting.YELLOW);
    }

    private boolean isSelectState() {
        return state == State.ADD_SELECT_AREA || state == State.DELETE_SELECT_AREA
            || state == State.ADD_DIM_SELECT || state == State.DELETE_DIM_SELECT;
    }

    private boolean isMatchingListResponse(String responseOperation, String responseTargetType, String responseDimension) {
        if (!safeEquals(operation, responseOperation) || !safeEquals(targetType, responseTargetType)) {
            return false;
        }
        return isDimensionTarget() || safeEquals(currentDimension, responseDimension);
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private boolean isDeleteOperation() {
        return "delete".equals(operation);
    }

    private boolean isDimensionTarget() {
        return "dimension".equals(targetType);
    }

    private String getCommandPrefix() {
        if ("dimension".equals(targetType)) {
            return isDeleteOperation() ? "deletedimensionalitydescription" : "adddimensionalitydescription";
        }
        return isDeleteOperation() ? "deletedescription" : "adddescription";
    }

    public boolean isActive() {
        return state != State.IDLE;
    }

    public State getState() {
        return state;
    }

    public void cancel() {
        if (isActive()) {
            sendMessage("已取消描述操作", Formatting.YELLOW);
        }
        reset();
    }

    public void reset() {
        state = State.IDLE;
        operation = null;
        targetType = null;
        currentDimension = null;
        entries.clear();
        selectedEntry = null;
        pendingDescription = null;
    }

    private String getCurrentDimensionType() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return "";
        }
        return areahint.network.Packets.convertDimensionPathToType(client.world.getRegistryKey().getValue().getPath());
    }

    private void sendMessage(String message, Formatting formatting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(formatting), false);
        }
    }

    private String stripQuotes(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        }
        return cleaned;
    }
}
