package areahint.language;

import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 鐠囶叀鈻堥柅澶嬪UI
 */
public class LanguageUI {

    public static void showLanguageSelectionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String currentLang = I18nManager.getCurrentLanguage();
        String currentDisplayName = I18nManager.getLanguageDisplayName(currentLang);

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("language.title.settings")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.language") + currentDisplayName + " (" + currentLang + ")"), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("language.prompt.select")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        List<String> languages = I18nManager.getAvailableLanguages();

        if (languages.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.error.language")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("language.error.no_files")), false);
            return;
        }

        // 娑撶儤鐦℃稉顏囶嚔鐟封偓閸掓稑缂撻幐澶愭尦
        for (String langCode : languages) {
            String displayName = I18nManager.getLanguageDisplayName(langCode);
            boolean isCurrent = langCode.equals(currentLang);

            String prefix = isCurrent ? "鎼俛閴?" : "鎼俠  ";
            MutableText button = areahint.util.TextCompat.literal(prefix + "[" + displayName + " (" + langCode + ")]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint language select " + langCode))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName)))
                    .withColor(isCurrent ? Formatting.GREEN : Formatting.AQUA));

            client.player.sendMessage(button, false);
        }

        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 閸欐牗绉烽幐澶愭尦
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("language.button.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint language cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.cancel.language"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }
}
