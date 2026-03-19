package areahint.delete;

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
 * Delete閻劍鍩涢悾宀勬桨缁崵绮?
 * 娴ｈ法鏁ら懕濠傘亯濞戝牊浼呴崪灞藉讲閻愮懓鍤紒鍕鐎圭偟骞囨禍銈勭鞍
 */
public class DeleteUI {

    /**
     * 閺勫墽銇氶崺鐔锋倳闁瀚ㄩ悾宀勬桨
     */
    public static void showAreaSelectionScreen(List<AreaData> deletableAreas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.area.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.delete.permission")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 娑撶儤鐦℃稉顏勫讲閸掔娀娅庨惃鍕厵閸氬秴鍨卞鐑樺瘻闁?
        for (AreaData area : deletableAreas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);

            // 閺嬪嫬缂撻幃顒€浠犻幓鎰仛娣団剝浼?
            String hoverText = I18nManager.translate("gui.message.area") +
                I18nManager.translate("gui.message.name_2") + area.getName() + "\n" +
                I18nManager.translate("gui.message.level") + area.getLevel() + "\n" +
                I18nManager.translate("gui.message.general_7") + area.getSignature();

            if (area.getBaseName() != null) {
                hoverText += I18nManager.translate("gui.message.area.parent") + area.getBaseName();
            }

            hoverText += I18nManager.translate("gui.prompt.area");

            MutableText areaButton = areahint.util.TextCompat.literal("鎼?[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint delete select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(hoverText)))
                    .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 閺勫墽銇氶崣鏍ㄧХ閹稿鎸?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint delete cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.delete_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 閺勫墽銇氱涵顔款吇閸掔娀娅庨悾宀勬桨閿涘牅绨╃痪褏鈥樼拋銈忕礆
     */
    public static void showConfirmDeleteScreen(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String displayName = areahint.util.AreaDataConverter.getDisplayName(area);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.confirm.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.error.area.confirm.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.error.general_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 閺勫墽銇氶崺鐔锋倳鐠囷妇绮忔穱鈩冧紖
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.name_2") + area.getName()), false);

        if (area.getSurfacename() != null && !area.getSurfacename().trim().isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.surface_2") + area.getSurfacename()), false);
        }

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.level") + area.getLevel()), false);

        if (area.getBaseName() != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.parent_2") + area.getBaseName()), false);
        } else {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.parent_3")), false);
        }

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.vertex") + area.getVertices().size() + I18nManager.translate("gui.message.general")), false);

        if (area.getAltitude() != null) {
            String minAlt = area.getAltitude().getMin() != null ?
                String.valueOf(area.getAltitude().getMin()) : I18nManager.translate("command.message.general_10");
            String maxAlt = area.getAltitude().getMax() != null ?
                String.valueOf(area.getAltitude().getMax()) : I18nManager.translate("command.message.general_10");
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_12") + minAlt + " ~ " + maxAlt), false);
        }

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_6") + area.getSignature()), false);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.error.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 閺勫墽銇氱涵顔款吇閸滃苯褰囧☉鍫熷瘻闁?
        MutableText confirmButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.error.confirm.delete"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint delete confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.error.area.confirm.delete_2") + displayName + I18nManager.translate("gui.message.general_3"))))
                .withColor(Formatting.RED)
                .withBold(true));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.cancel.delete"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint delete cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.delete"))))
                .withColor(Formatting.GREEN)
                .withBold(true));

        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(confirmButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
    }
}
