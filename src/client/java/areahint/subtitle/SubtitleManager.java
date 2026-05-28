package areahint.subtitle;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.data.AreaData;
import areahint.i18n.I18nManager;
import areahint.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 副字幕交互管理器。
 * <p>
 * 一个管理器同时承载添加副字幕、删除副字幕、修改副字幕颜色和修改副字幕大小四类流程，
 * 这样可以复用域名选择、状态清理和服务端提交逻辑。
 */
public class SubtitleManager {
    public enum SubtitleState {
        IDLE,
        ADD_SELECT_AREA,
        ADD_INPUT_TEXT,
        ADD_CONFIRM,
        DELETE_SELECT_AREA,
        DELETE_CONFIRM,
        COLOR_SELECT_AREA,
        COLOR_SELECT_COLOR,
        COLOR_CONFIRM,
        SIZE_SELECT
    }

    private static SubtitleManager instance;

    private SubtitleState currentState = SubtitleState.IDLE;
    private List<AreaData> editableAreas = new ArrayList<>();
    private String currentDimension = null;
    private AreaData selectedArea = null;
    private String selectedAreaName = null;
    private String pendingSubtitle = null;
    private String pendingColor = null;

    private SubtitleManager() {
    }

    public static SubtitleManager getInstance() {
        if (instance == null) {
            instance = new SubtitleManager();
        }
        return instance;
    }

    public void startAddSubtitle(List<AreaData> areas, String dimension) {
        if (!prepareStart(areas, dimension)) {
            return;
        }
        currentState = SubtitleState.ADD_SELECT_AREA;
        SubtitleUI.showAreaSelectionScreen(editableAreas, SubtitleUI.AreaSelectionMode.ADD);
    }

    public void startDeleteSubtitle(List<AreaData> areas, String dimension) {
        if (!prepareStart(areas, dimension)) {
            return;
        }
        currentState = SubtitleState.DELETE_SELECT_AREA;
        SubtitleUI.showAreaSelectionScreen(editableAreas, SubtitleUI.AreaSelectionMode.DELETE);
    }

    public void startReplaceSubtitleColor(List<AreaData> areas, String dimension) {
        if (!prepareStart(areas, dimension)) {
            return;
        }
        currentState = SubtitleState.COLOR_SELECT_AREA;
        SubtitleUI.showAreaSelectionScreen(editableAreas, SubtitleUI.AreaSelectionMode.COLOR);
    }

    public void handleAddAreaSelection(String areaName) {
        if (currentState != SubtitleState.ADD_SELECT_AREA) {
            return;
        }
        if (!selectArea(areaName)) {
            return;
        }
        currentState = SubtitleState.ADD_INPUT_TEXT;
        SubtitleUI.showSubtitleInputScreen(selectedArea);
    }

    public void handleSubtitleText(String subtitleText) {
        if (currentState != SubtitleState.ADD_INPUT_TEXT) {
            return;
        }

        String normalizedSubtitle = normalizeSubtitle(subtitleText);
        if (normalizedSubtitle == null) {
            sendClientMessage(tr("subtitle.manager.error.empty"));
            return;
        }

        pendingSubtitle = normalizedSubtitle;
        currentState = SubtitleState.ADD_CONFIRM;
        SubtitleUI.showAddConfirmScreen(selectedArea, pendingSubtitle);
    }

    public void confirmAddSubtitle() {
        if (currentState != SubtitleState.ADD_CONFIRM || selectedAreaName == null || pendingSubtitle == null) {
            return;
        }
        SubtitleNetworking.sendMutation("set_subtitle", selectedAreaName, pendingSubtitle, currentDimension);
        sendClientMessage(tr("subtitle.manager.message.set_subtitle_sent"));
        resetState();
    }

    public void handleDeleteAreaSelection(String areaName) {
        if (currentState != SubtitleState.DELETE_SELECT_AREA) {
            return;
        }
        if (!selectArea(areaName)) {
            return;
        }
        currentState = SubtitleState.DELETE_CONFIRM;
        SubtitleUI.showDeleteConfirmScreen(selectedArea);
    }

    public void confirmDeleteSubtitle() {
        if (currentState != SubtitleState.DELETE_CONFIRM || selectedAreaName == null) {
            return;
        }
        SubtitleNetworking.sendMutation("delete_subtitle", selectedAreaName, "", currentDimension);
        sendClientMessage(tr("subtitle.manager.message.delete_subtitle_sent"));
        resetState();
    }

    public void handleColorAreaSelection(String areaName) {
        if (currentState != SubtitleState.COLOR_SELECT_AREA) {
            return;
        }
        if (!selectArea(areaName)) {
            return;
        }
        currentState = SubtitleState.COLOR_SELECT_COLOR;
        SubtitleUI.showColorSelectionScreen(selectedArea);
    }

    public void handleColorSelection(String colorInput) {
        if (currentState != SubtitleState.COLOR_SELECT_COLOR) {
            return;
        }

        String normalizedColor = ColorUtil.normalizeColor(colorInput);
        if (!ColorUtil.isValidColor(normalizedColor)) {
            sendClientMessage(tr("subtitle.manager.error.invalid_color", colorInput));
            return;
        }

        pendingColor = normalizedColor;
        currentState = SubtitleState.COLOR_CONFIRM;
        SubtitleUI.showColorConfirmScreen(selectedArea, pendingColor);
    }

    public void confirmReplaceSubtitleColor() {
        if (currentState != SubtitleState.COLOR_CONFIRM || selectedAreaName == null || pendingColor == null) {
            return;
        }
        SubtitleNetworking.sendMutation("set_subtitle_color", selectedAreaName, pendingColor, currentDimension);
        sendClientMessage(tr("subtitle.manager.message.set_color_sent"));
        resetState();
    }

    public void startSubtitleSizeSelection() {
        if (currentState != SubtitleState.IDLE) {
            sendClientMessage(tr("subtitle.manager.error.active"));
            return;
        }
        currentState = SubtitleState.SIZE_SELECT;
        SubtitleUI.showSubtitleSizeSelectionScreen(ClientConfig.getSubtitleSize());
    }

    public void handleSubtitleSizeSelection(String size) {
        if (currentState != SubtitleState.SIZE_SELECT) {
            return;
        }

        if (!areahint.data.ConfigData.isValidSubtitleSize(size)) {
            sendClientMessage(tr("subtitle.manager.error.invalid_size", size));
            return;
        }

        ClientConfig.setSubtitleSize(size);
        sendClientMessage(tr("subtitle.manager.message.size_set", SubtitleUI.getSizeDisplayName(size)));
        AreashintClient.reload();
        resetState();
    }

    public void cancel() {
        if (currentState != SubtitleState.IDLE) {
            sendClientMessage(tr("subtitle.manager.message.cancelled"));
        }
        resetState();
    }

    private boolean prepareStart(List<AreaData> areas, String dimension) {
        if (currentState != SubtitleState.IDLE) {
            sendClientMessage(tr("subtitle.manager.error.active"));
            return false;
        }

        if (areas == null || areas.isEmpty()) {
            sendClientMessage(tr("subtitle.manager.error.no_areas"));
            return false;
        }

        editableAreas = new ArrayList<>(areas);
        currentDimension = dimension;
        selectedArea = null;
        selectedAreaName = null;
        pendingSubtitle = null;
        pendingColor = null;
        return true;
    }

    private boolean selectArea(String rawAreaName) {
        String areaName = stripQuotes(rawAreaName);
        for (AreaData area : editableAreas) {
            if (area.getName().equals(areaName)) {
                selectedArea = area;
                selectedAreaName = areaName;
                return true;
            }
        }

        sendClientMessage(tr("subtitle.manager.error.area_not_found", areaName));
        return false;
    }

    private void resetState() {
        currentState = SubtitleState.IDLE;
        editableAreas.clear();
        currentDimension = null;
        selectedArea = null;
        selectedAreaName = null;
        pendingSubtitle = null;
        pendingColor = null;
    }

    private String normalizeSubtitle(String subtitleText) {
        if (subtitleText == null) {
            return null;
        }
        String normalized = subtitleText.replace("/n", "\n").replace("\\n", "\n").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private void sendClientMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of(message), false);
        }
    }

    private String tr(String key, Object... args) {
        return I18nManager.translate(key, args);
    }

    public SubtitleState getCurrentState() {
        return currentState;
    }
}
