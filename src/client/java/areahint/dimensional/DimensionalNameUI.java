package areahint.dimensional;

import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Map;

/**
 * 缂備焦娼欑€规娊宕洪悢閿嬪€冲ù婧垮€撶花鐧營
 */
public class DimensionalNameUI {

    /**
     * 闁哄嫬澧介妵姘辩磼閺夋垵顔婇梺顐㈩槹鐎氥劑鎮惧畝鍕〃
     * @param commandPrefix "dimname" 闁?"dimcolor"
     */
    public static void showDimensionSelectionScreen(String commandPrefix) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String titleKey = commandPrefix.equals("dimname") ? "gui.title.area.dimension.modify" : "gui.title.area.color.dimension.modify";
        String cmdBase = commandPrefix.equals("dimname") ? "dimensionalityname" : "dimensionalitycolor";

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate(titleKey)), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.dimension")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        Map<String, String> names = ClientDimensionalNameManager.getAllDimensionalNames();
        for (Map.Entry<String, String> entry : names.entrySet()) {
            String dimId = entry.getKey();
            String dimName = entry.getValue();
            String color = ClientDimensionalNameManager.getDimensionalColor(dimId);
            String colorDisplay = color != null ? color : I18nManager.translate("gui.message.general_10");

            String label;
            if (commandPrefix.equals("dimcolor")) {
                label = String.format(I18nManager.translate("gui.button.color"), dimName, dimId, colorDisplay);
            } else {
                label = String.format("§6[%s] §7(%s)", dimName, dimId);
            }

            MutableText btn = areahint.util.TextCompat.literal(label)
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint " + cmdBase + " select \"" + dimId + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + dimName)))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(btn, false);
        }

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + cmdBase + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_5"))))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    /**
     * 闁哄嫬澧介妵姘跺触瀹ュ泦鐐存綇閹惧啿寮抽柟缁樺姉閵囨岸鎮惧畝鍕〃
     */
    public static void showNameInputScreen(String dimensionId, String currentName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.dimension")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.dimension_2") + dimensionId), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.name_3") + currentName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 闁圭粯鍔掔欢鍨▔閳ь剚绋夐鍕闁绘劗鎳撻崵顕€鎯冮崟顐ょ处閻犱緡鍠栭幊鈩冪?
        MutableText inputHint = areahint.util.TextCompat.literal(I18nManager.translate("gui.prompt.name_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint dimensionalityname name "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.name_3")))));
        client.player.sendMessage(inputHint, false);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalityname cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_5"))))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    /**
     * 闁哄嫬澧介妵姘跺触瀹ュ泦鐐垫兜椤旀鍚囬柣锝呯焸濞?
     */
    public static void showNameConfirmScreen(String dimensionId, String oldName, String newName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.dimension.confirm")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.dimension.confirm")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.dimension_2") + dimensionId), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.name") + oldName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.name_4") + newName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        MutableText confirmBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalityname confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.confirm.modify"))))
                .withColor(Formatting.GREEN));

        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.error.general"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalityname cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.modify"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(areahint.util.TextCompat.empty().append(confirmBtn).append(areahint.util.TextCompat.of("  ")).append(cancelBtn), false);
    }

    /**
     * 闁哄嫬澧介妵姘紣濠婂棗顥忛梺顐㈩槹鐎氥劑鎮惧畝鍕〃
     */
    public static void showColorSelectionScreen(String dimensionId, String dimName, String currentColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.color.dimension_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.dimension_2") + dimName + " 閹?(" + dimensionId + ")"), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.color_4") + currentColor), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.color")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 濡増绮忔竟濠囧箰婢舵劖灏﹂悶?
        showColorRows(client);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_5"))))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    private static void showColorRows(MinecraftClient client) {
        MutableText row1 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_24"), "#FFFFFF", "§f"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_23"), "#808080", "§7"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_18"), "#555555", "§8"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_31"), "#000000", "§0"));

        MutableText row2 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_19"), "#AA0000", "§4"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_27"), "#FF5555", "§c"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_25"), "#FF55FF", "§d"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_17"), "#FFAA00", "§6"));

        MutableText row3 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_30"), "#FFFF55", "§e"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_28"), "#55FF55", "§a"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_20"), "#00AA00", "§2"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_13"), "#55FFFF", "§b"));

        MutableText row4 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_22"), "#00AAAA", "§3"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_29"), "#5555FF", "§9"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_21"), "#0000AA", "§1"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_26"), "#AA00AA", "§5"));

        MutableText row5 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_15"), "FLASH_BW_ALL", "§7"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_14"), "FLASH_RAINBOW_ALL", "§b"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_12"), "FLASH_BW_CHAR", "§8"))
            .append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_11"), "FLASH_RAINBOW_CHAR", "§d"));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(row3, false);
        client.player.sendMessage(row4, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_4")), false);
        client.player.sendMessage(row5, false);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        MutableText customBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.color_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint dimensionalitycolor color #"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.color_2")))));
        client.player.sendMessage(customBtn, false);
    }

    private static MutableText colorBtn(String name, String value, String mcColor) {
        return areahint.util.TextCompat.literal(mcColor + "[" + name + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor color " + value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + name))));
    }

    /**
     * 闁哄嫬澧介妵姘紣濠婂棗顥忕痪顓у枦椤撳鎮惧畝鍕〃
     */
    public static void showColorConfirmScreen(String dimensionId, String dimName,
                                               String oldColor, String newColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.color.dimension")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.color.dimension")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.dimension_2") + dimName + " 閹?(" + dimensionId + ")"), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.color_2") + oldColor), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.color_5") + newColor), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        MutableText confirmBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.confirm.modify"))))
                .withColor(Formatting.GREEN));

        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.error.general"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.modify"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(areahint.util.TextCompat.empty().append(confirmBtn).append(areahint.util.TextCompat.of("  ")).append(cancelBtn), false);
    }

    /**
     * 闁哄嫬澧介妵姘純閺嶎煈鍋х紓浣规綑鐎规娊宕ㄩ挊澶嬪€抽柟缁樺姉閵?
     */
    public static void showFirstNamePrompt(String dimensionId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.dimension")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.dimension") + dimensionId), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.dimension.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        MutableText inputBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.prompt.name"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint firstdimname "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.dimension.name_2")))));
        client.player.sendMessage(inputBtn, false);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        MutableText skipBtn = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint firstdimname_skip"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.message.name_5")))));
        client.player.sendMessage(skipBtn, false);
    }
}
