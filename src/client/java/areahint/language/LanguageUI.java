package areahint.language;

import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 语言选择UI
 */
public class LanguageUI {

    public static void showLanguageSelectionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String currentLang = I18nManager.getCurrentLanguage();
        String currentDisplayName = I18nManager.getLanguageDisplayName(currentLang);

        client.player.sendMessage(Text.of("§6=== 语言设置 / Language Settings ==="), false);
        client.player.sendMessage(Text.of("§a当前语言: §6" + currentDisplayName + " (" + currentLang + ")"), false);
        client.player.sendMessage(Text.of("§a请选择语言 / Select language:"), false);
        client.player.sendMessage(Text.of(""), false);

        List<String> languages = I18nManager.getAvailableLanguages();

        if (languages.isEmpty()) {
            client.player.sendMessage(Text.of("§c未找到任何语言文件"), false);
            client.player.sendMessage(Text.of("§7请在 areas-hint/lang/ 文件夹中添加语言文件"), false);
            return;
        }

        // 为每个语言创建按钮
        for (String langCode : languages) {
            String displayName = I18nManager.getLanguageDisplayName(langCode);
            boolean isCurrent = langCode.equals(currentLang);

            String prefix = isCurrent ? "§a✓ " : "§b  ";
            MutableText button = Text.literal(prefix + "[" + displayName + " (" + langCode + ")]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint language select " + langCode))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("选择 " + displayName)))
                    .withColor(isCurrent ? Formatting.GREEN : Formatting.AQUA));

            client.player.sendMessage(button, false);
        }

        client.player.sendMessage(Text.of(""), false);

        // 取消按钮
        MutableText cancelButton = Text.literal("§c[取消 / Cancel]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint language cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消语言选择")))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }
}
