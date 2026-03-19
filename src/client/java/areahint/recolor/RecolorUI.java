package areahint.recolor;

import areahint.data.AreaData;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Recolor闁活潿鍔嶉崺娑㈡偩瀹€鍕〃缂侇垵宕电划?
 * 濞达綀娉曢弫銈夋嚂婵犲倶浜繛鎴濈墛娴煎懘宕仦钘夎闁绘劗鎳撻崵顔剧磼閸曨亝顐介悗鍦仧楠炲洦绂嶉妶鍕瀺
 */
public class RecolorUI {

    /**
     * 闁哄嫬澧介妵姘跺春閻旈攱鍊抽梺顐㈩槹鐎氥劑鎮惧畝鍕〃
     */
    public static void showAreaSelectionScreen(List<AreaData> areas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.title.area.color.modify")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.prompt.area.color.modify")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        for (AreaData area : areas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            String currentColor = area.getColor() != null ? area.getColor() : "#FFFFFF";

            // 闁告帗绋戠紓鎾诲春閻旈攱鍊抽梺顐㈩槹鐎氥劑骞愭径鎰唉
            MutableText areaButton = areahint.util.TextCompat.literal(
                String.format(I18nManager.translate("message.button.color.level"), displayName, area.getLevel(), currentColor)
            ).setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint recolor select \"" + area.getName() + "\""))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName + I18nManager.translate("message.message.color.modify"))))
                .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        // 闁哄嫬澧介妵姘跺矗閺嶃劎啸闁圭顦甸幐?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("command.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 闁哄嫬澧介妵姘紣濠婂棗顥忛梺顐㈩槹鐎氥劑鎮惧畝鍕〃
     */
    public static void showColorSelectionScreen(String areaName, String currentColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.color")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.name_2") + areaName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.color_3") + currentColor), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.color")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 缂佹鍏涚粩瀵告偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row1 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_24"), "#FFFFFF", "§f"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_23"), "#808080", "§7"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_18"), "#555555", "§8"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_31"), "#000000", "§0"));

        // 缂佹鍏涚花鈺冩偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row2 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_19"), "#AA0000", "§4"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_27"), "#FF5555", "§c"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_25"), "#FF55FF", "§d"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_17"), "#FFAA00", "§6"));

        // 缂佹鍏涚粭浣烘偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row3 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_30"), "#FFFF55", "§e"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_28"), "#55FF55", "§a"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_20"), "#00AA00", "§2"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_13"), "#55FFFF", "§b"));

        // 缂佹鍓欏ú鎾舵偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row4 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_22"), "#00AAAA", "§3"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_29"), "#5555FF", "§9"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_21"), "#0000AA", "§1"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_26"), "#AA00AA", "§5"));

        // 闂傚偆浜為崕濠囧极閸喓浜柟绋款樀閹稿磭鎮?
        MutableText row5 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_15"), "FLASH_BW_ALL", "§7"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_14"), "FLASH_RAINBOW_ALL", "§b"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_12"), "FLASH_BW_CHAR", "§8"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_11"), "FLASH_RAINBOW_CHAR", "§d"));

        // 闁告瑦鐗楃粔鐑藉箰婢舵劖灏?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("command.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(row3, false);
        client.player.sendMessage(row4, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_4")), false);
        client.player.sendMessage(row5, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 闁哄嫬澧介妵姘辨兜椤旀鍚囬柣锝呯焸濞?
     */
    public static void showConfirmScreen(String areaName, String oldColor, String newColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.color.confirm.modify")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.color.confirm")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.name_2") + areaName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.color_3") + oldColor), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.color_6") + newColor), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 闁哄嫬澧介妵姘辨兜椤旀鍚囬柛婊冭嫰瑜板洤鈽夐崼鐔风樆闂?
        MutableText confirmButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.color.confirm.modify"))))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.error.general"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.modify"))))
                .withColor(Formatting.RED));

        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(confirmButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.confirm")), false);
    }

    /**
     * 闁告帗绋戠紓鎾达紣濠婂棗顥忛梺顐㈩槹鐎氥劑骞愭径鎰唉
     */
    private static MutableText createColorButton(String colorName, String colorValue, String minecraftColor) {
        return areahint.util.TextCompat.literal(minecraftColor + "[" + colorName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint recolor color " + colorValue))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + colorName + I18nManager.translate("gui.message.color")))));
    }

    /**
     * 闁哄嫬澧介妵姘舵煥濞嗘帩鍤栨繛鎴濈墛娴?
     */
    public static void showError(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.general_2") + message), false);
        }
    }

    /**
     * 闁哄嫬澧介妵姘跺箣閹邦剙顫犳繛鎴濈墛娴?
     */
    public static void showSuccess(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of("閹间繘" + message), false);
        }
    }
}
