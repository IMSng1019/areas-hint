package areahint.replacebutton;

import areahint.i18n.I18nManager;
import areahint.util.TextCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReplaceButtonUI {
    public static void showWaitingForKeyScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.record.key")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.key_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.hint.confirm")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_5")), false);

        MutableText cancelButton = TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint replacebutton cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.key"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    public static void showConfirmScreen(String keyName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.confirm.key")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.key") + keyName), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.vertex.record")), false);

        MutableText confirmButton = TextCompat.literal(I18nManager.translate("gui.button.confirm"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint replacebutton confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.confirm.key"))))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint replacebutton cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel.key"))))
                .withColor(Formatting.RED));

        MutableText buttonRow = TextCompat.empty()
            .append(confirmButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
    }

    public static void showError(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.general_2") + message), false);
        }
    }

    public static void showSuccess(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(TextCompat.literal("搂a" + message), false);
        }
    }

    public static void showInfo(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(TextCompat.literal("搂7" + message), false);
        }
    }
}
