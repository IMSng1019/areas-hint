package areahint.rename;

import areahint.data.AreaData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import areahint.i18n.I18nManager;

import java.util.List;

/**
 * Rename閻劍鍩涢悾宀勬桨缁崵绮?
 * 娴ｈ法鏁ら懕濠傘亯濞戝牊浼呴崪灞藉讲閻愮懓鍤紒鍕鐎圭偟骞囨禍銈勭鞍
 */
public class RenameUI {

    /**
     * 閺勫墽銇氶崺鐔锋倳闁瀚ㄩ悾宀勬桨
     */
    public static void showAreaSelectScreen(List<AreaData> areas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.rename")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.area.rename")), false);

        for (AreaData area : areas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            String signature = area.getSignature() != null ? area.getSignature() : I18nManager.translate("gui.message.general_16");

            MutableText areaButton = areahint.util.TextCompat.literal("鎼?[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint rename select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName + I18nManager.translate("gui.message.rename") + signature)))
                    .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        // 閺勫墽銇氶崣鏍ㄧХ閹稿鎸?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_3"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 閺勫墽銇氶弬鏉跨厵閸氬秴鎮曠粔鎷岀翻閸忋儳鏅棃?
     */
    public static void showNewNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.area.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.name")), false);

        // 閺勫墽銇氶崣鏍ㄧХ閹稿鎸?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_3"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 閺勫墽銇氶懕鏂挎値閸╃喎鎮曟潏鎾冲弳閻ｅ矂娼?
     */
    public static void showSurfaceNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.title.area.surface")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.area.surface")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.area.surface.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.hint.area.surface")), false);

        // 閺勫墽銇氶崣鏍ㄧХ閹稿鎸?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_3"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 閺勫墽銇氱涵顔款吇閻ｅ矂娼?
     */
    public static void showConfirmScreen(String oldName, String newName, String newSurfaceName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.area.confirm.rename")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.confirm.rename")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area_2") + oldName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area_3") + newName), false);

        if (newSurfaceName != null && !newSurfaceName.trim().isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.surface") + newSurfaceName), false);
        } else {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.surface.name")), false);
        }

        // 閺勫墽銇氱涵顔款吇閸滃苯褰囧☉鍫熷瘻闁?
        MutableText confirmButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.confirm.rename"))))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.error.general"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.rename"))))
                .withColor(Formatting.RED));

        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(confirmButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.confirm_2")), false);
    }

    /**
     * 閺勫墽銇氶柨娆掝嚖濞戝牊浼?
     */
    public static void showError(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.general_2") + message), false);
        }
    }

    /**
     * 閺勫墽銇氶幋鎰濞戝牊浼?
     */
    public static void showSuccess(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of("鎼俛" + message), false);
        }
    }

    /**
     * 閺勫墽銇氭穱鈩冧紖濞戝牊浼?
     */
    public static void showInfo(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of("鎼?" + message), false);
        }
    }
}
