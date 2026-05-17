package areahint.description;

import areahint.i18n.I18nManager;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
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
        WAITING_SEARCH_INPUT,
        WAITING_TARGET_SELECT,
        WAITING_DESCRIPTION_INPUT,
        WAITING_CONFIRM,
        WAITING_SECOND_CONFIRM,
        SUBMITTING
    }

    private State state = State.IDLE;
    private String operation;
    private String targetType;
    private String commandPrefix;
    private String currentDimension;
    private final List<DescriptionListEntry> entries = new ArrayList<>();
    private DescriptionListEntry selectedEntry;
    private String pendingDescription;
    private boolean chatListenerRegistered;

    private DescriptionManager() {
    }

    public static DescriptionManager getInstance() {
        if (instance == null) {
            instance = new DescriptionManager();
        }
        return instance;
    }

    public void handleClientCommand(String action) {
        if (handleCommand(action, "adddescription", "add", "area")) {
            return;
        }
        if (handleCommand(action, "deletedescription", "delete", "area")) {
            return;
        }
        if (handleCommand(action, "adddimensionalitydescription", "add", "dimension")) {
            return;
        }
        handleCommand(action, "deletedimensionalitydescription", "delete", "dimension");
    }

    private boolean handleCommand(String action, String prefix, String operation, String targetType) {
        if (action.equals(prefix + "_start")) {
            start(operation, targetType, prefix);
            return true;
        }
        if (action.startsWith(prefix + "_search:")) {
            requestList();
            return true;
        }
        if (action.startsWith(prefix + "_select:")) {
            select(action.substring((prefix + "_select:").length()));
            return true;
        }
        if (action.startsWith(prefix + "_text:")) {
            openBookEditor();
            return true;
        }
        if (action.equals(prefix + "_confirm")) {
            confirm();
            return true;
        }
        if (action.equals(prefix + "_confirm2")) {
            confirmSecond();
            return true;
        }
        if (action.equals(prefix + "_cancel")) {
            cancel();
            return true;
        }
        return false;
    }

    private void start(String operation, String targetType, String commandPrefix) {
        if (isActive()) {
            sendMessage(I18nManager.translate("description.manager.error.active"), Formatting.RED);
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        registerChatListener();
        resetStateOnly();
        this.operation = operation;
        this.targetType = targetType;
        this.commandPrefix = commandPrefix;
        this.currentDimension = getCurrentDimensionType();
        this.state = State.WAITING_SEARCH_INPUT;
        DescriptionUI.showLoadingList(commandPrefix, isDimensionTarget(), isDeleteOperation());
        requestList();
    }

    private void registerChatListener() {
        if (chatListenerRegistered) {
            return;
        }
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (state != State.IDLE && state != State.SUBMITTING && isLocalPlayerMessage(sender)) {
                handleChatInput(message.getString());
            }
        });
        chatListenerRegistered = true;
    }

    private boolean isLocalPlayerMessage(GameProfile sender) {
        if (sender == null) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        if (sender.getId() != null && sender.getId().equals(client.player.getUuid())) {
            return true;
        }
        return sender.getName() != null && sender.getName().equals(client.player.getGameProfile().getName());
    }

    private void handleChatInput(String input) {
    }

    private void requestList() {
        if (state != State.WAITING_SEARCH_INPUT || operation == null || targetType == null) {
            return;
        }
        entries.clear();
        selectedEntry = null;
        pendingDescription = null;
        state = State.WAITING_TARGET_SELECT;
        sendMessage(I18nManager.translate("description.manager.loading.targets"), Formatting.YELLOW);
        DescriptionClientNetworking.sendListRequest(operation, targetType, currentDimension, "");
    }

    public void receiveList(String operation, String targetType, String dimension, List<DescriptionListEntry> entries) {
        if (state != State.WAITING_TARGET_SELECT) {
            return;
        }
        if (!isMatchingListResponse(operation, targetType, dimension)) {
            return;
        }
        this.entries.clear();
        this.entries.addAll(entries);
        if (entries.isEmpty()) {
            sendMessage(I18nManager.translate("description.manager.error.no_targets"), Formatting.RED);
            reset();
            return;
        }
        DescriptionUI.showSelection(getCommandPrefix(), entries);
    }

    private void select(String rawId) {
        if (state != State.WAITING_TARGET_SELECT) {
            return;
        }
        String id = stripQuotes(rawId);
        for (DescriptionListEntry entry : entries) {
            if (entry.id().equals(id)) {
                selectedEntry = entry;
                if (isDeleteOperation()) {
                    state = State.WAITING_CONFIRM;
                    DescriptionUI.showDeleteConfirmFirst(getCommandPrefix(), selectedEntry);
                } else {
                    state = State.WAITING_DESCRIPTION_INPUT;
                    DescriptionUI.showDescriptionInput(getCommandPrefix(), selectedEntry);
                    DescriptionBookEditScreen.open(selectedEntry.displayName(), pendingDescription);
                }
                return;
            }
        }
        sendMessage(I18nManager.translate("description.manager.error.not_found", id), Formatting.RED);
    }

    private void openBookEditor() {
        if (state != State.WAITING_DESCRIPTION_INPUT || selectedEntry == null) {
            return;
        }
        DescriptionBookEditScreen.open(selectedEntry.displayName(), pendingDescription);
    }

    public void receiveBookDescription(String description) {
        handleDescriptionInput(description);
    }

    public boolean isWaitingForBookInput() {
        return state == State.WAITING_DESCRIPTION_INPUT;
    }

    private void handleDescriptionInput(String description) {
        if (state != State.WAITING_DESCRIPTION_INPUT) {
            return;
        }
        String cleaned = description == null ? "" : description.trim();
        if (cleaned.isEmpty()) {
            sendMessage(I18nManager.translate("description.manager.error.empty.retry"), Formatting.RED);
            return;
        }
        if (cleaned.length() > DescriptionServerNetworking.MAX_DESCRIPTION_LENGTH) {
            sendMessage(I18nManager.translate("description.manager.error.too_long.retry"), Formatting.RED);
            return;
        }
        pendingDescription = cleaned;
        confirmAdd();
    }

    private void confirm() {
        if (isAddOperation()) {
            confirmAdd();
        } else if (isDeleteOperation()) {
            confirmDeleteFirst();
        }
    }

    private void confirmAdd() {
        if ((state != State.WAITING_CONFIRM && state != State.WAITING_DESCRIPTION_INPUT) || selectedEntry == null || pendingDescription == null) {
            return;
        }
        if (DescriptionClientNetworking.sendWrite(targetType, currentDimension, selectedEntry.id(), pendingDescription)) {
            state = State.SUBMITTING;
            sendMessage(I18nManager.translate("description.manager.message.save.sent"), Formatting.YELLOW);
        }
    }

    private void confirmDeleteFirst() {
        if (state != State.WAITING_CONFIRM || selectedEntry == null) {
            return;
        }
        state = State.WAITING_SECOND_CONFIRM;
        DescriptionUI.showDeleteConfirmSecond(getCommandPrefix(), selectedEntry);
    }

    private void confirmSecond() {
        if (state != State.WAITING_SECOND_CONFIRM || !isDeleteOperation() || selectedEntry == null) {
            return;
        }
        DescriptionClientNetworking.sendDelete(targetType, currentDimension, selectedEntry.id());
        state = State.SUBMITTING;
        sendMessage(I18nManager.translate("description.manager.message.delete.sent"), Formatting.YELLOW);
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

    private boolean isAddOperation() {
        return "add".equals(operation);
    }

    private boolean isDeleteOperation() {
        return "delete".equals(operation);
    }

    private boolean isDimensionTarget() {
        return "dimension".equals(targetType);
    }

    private String getCommandPrefix() {
        if (commandPrefix != null) {
            return commandPrefix;
        }
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
            sendMessage(I18nManager.translate("description.manager.message.cancelled"), Formatting.YELLOW);
        }
        reset();
    }

    public void reset() {
        resetStateOnly();
    }

    private void resetStateOnly() {
        state = State.IDLE;
        operation = null;
        targetType = null;
        commandPrefix = null;
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
        String dimensionType = areahint.network.Packets.convertDimensionPathToType(client.world.getRegistryKey().getValue().getPath());
        return dimensionType == null ? "" : dimensionType;
    }

    private void sendMessage(String message, Formatting formatting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(formatting), false);
        }
    }

    private String stripChatPrefix(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.startsWith("<") && cleaned.contains(">")) {
            int endIndex = cleaned.indexOf(">") + 1;
            if (endIndex < cleaned.length()) {
                cleaned = cleaned.substring(endIndex).trim();
            }
        }
        return cleaned;
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
