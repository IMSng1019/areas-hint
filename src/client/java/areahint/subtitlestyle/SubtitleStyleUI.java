package areahint.subtitlestyle;

import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * SubtitleStyle閻劍鍩涢悾宀勬桨缁崵绮?
 * 娴ｈ法鏁ら懕濠傘亯濞戝牊浼呴崪灞藉讲閻愮懓鍤紒鍕鐎圭偟骞囨禍銈勭鞍
 */
public class SubtitleStyleUI {

    /**
     * 閺勫墽銇氶弽宄扮础闁瀚ㄩ悾宀勬桨
     * @param currentStyle 瑜版挸澧犻弽宄扮础
     */
    public static void showStyleSelectionScreen(String currentStyle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.general_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_9") + getStyleDisplayName(currentStyle)), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.general_4")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 閸掓稑缂撻弽宄扮础闁瀚ㄩ幐澶愭尦
        MutableText fullButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_3"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle select full"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.message.area_6"))))
                .withColor(Formatting.AQUA));

        MutableText simpleButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_5"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle select simple"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.message.area_5"))))
                .withColor(Formatting.YELLOW));

        MutableText mixedButton = areahint.util.TextCompat.literal(I18nManager.translate("gui.button.general_4"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle select mixed"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("gui.message.area_7"))))
                .withColor(Formatting.LIGHT_PURPLE));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_6"))))
                .withColor(Formatting.RED));

        // 缂佸嫬鎮庨幐澶愭尦閺勫墽銇?
        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(fullButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(simpleButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(mixedButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.general_2")), false);
    }

    /**
     * 閼惧嘲褰囬弽宄扮础閻ㄥ嫭妯夌粈鍝勬倳缁?
     */
    private static String getStyleDisplayName(String style) {
        switch (style) {
            case "full":
                return I18nManager.translate("message.message.general_106");
            case "simple":
                return I18nManager.translate("message.message.general_215");
            case "mixed":
                return I18nManager.translate("message.message.general_209");
            default:
                return style;
        }
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
