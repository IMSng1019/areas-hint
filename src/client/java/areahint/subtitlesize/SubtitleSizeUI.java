package areahint.subtitlesize;

import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * SubtitleSize闁活潿鍔嶉崺娑㈡偩瀹€鍕〃缂侇垵宕电划?
 * 濞达綀娉曢弫銈夋嚂婵犲倶浜繛鎴濈墛娴煎懘宕仦钘夎闁绘劗鎳撻崵顔剧磼閸曨亝顐介悗鍦仧楠炲洦绂嶉妶鍕瀺
 */
public class SubtitleSizeUI {

    /**
     * 闁哄嫬澧介妵姘緞瑜嶉惃顒勬焻婢跺顏ラ柣锝呯焸濞?
     * @param currentSize 鐟滅増鎸告晶鐘冲緞瑜嶉惃?
     */
    public static void showSizeSelectionScreen(String currentSize) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.title.general")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_8") + getSizeDisplayName(currentSize)), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.general_3")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        // 缂佹鍏涚粩瀵告偘鐏炴儳鐦婚梺绛嬪櫙缁变即寮告担鎼炰海闁靛棔绀侀妵鍥Υ娴ｇ晫绐涘鍫嗕讲鍋撴担鐤幀
        MutableText row1 = areahint.util.TextCompat.empty()
            .append(createSizeButton(I18nManager.translate("message.message.general_193"), "extra_large", "§d"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createSizeButton(I18nManager.translate("message.message.general_104"), "large", "§b"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createSizeButton(I18nManager.translate("message.message.general_225"), "medium_large", "§a"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createSizeButton(I18nManager.translate("message.message.general_58"), "medium", "§e"));

        // 缂佹鍏涚花鈺冩偘鐏炴儳鐦婚梺绛嬪櫙缁辩増娼忛崘銊ф瘓闁靛棔绀侀惃顒勫Υ娴ｅ湱鈧剛浜?
        MutableText row2 = areahint.util.TextCompat.empty()
            .append(createSizeButton(I18nManager.translate("message.message.general_226"), "medium_small", "§6"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createSizeButton(I18nManager.translate("message.message.general_111"), "small", "§c"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createSizeButton(I18nManager.translate("message.message.general_194"), "extra_small", "§4"));

        // 闁告瑦鐗楃粔鐑藉箰婢舵劖灏?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlesize cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("gui.message.cancel_4"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(cancelButton, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.prompt.general")), false);
    }

    /**
     * 闁告帗绋戠紓鎾村緞瑜嶉惃顒勬焻婢跺顏ラ柟绋款樀閹?
     */
    private static MutableText createSizeButton(String displayName, String sizeValue, String colorCode) {
        return areahint.util.TextCompat.literal(colorCode + "[" + displayName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint subtitlesize select " + sizeValue))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName + I18nManager.translate("gui.message.general_2")))));
    }

    /**
     * 闁兼儳鍢茶ぐ鍥ㄥ緞瑜嶉惃顒勬儍閸曨剚鈻旂紒鈧崫鍕€崇紒?
     */
    private static String getSizeDisplayName(String size) {
        switch (size) {
            case "extra_large":
                return I18nManager.translate("message.message.general_193");
            case "large":
                return I18nManager.translate("message.message.general_104");
            case "medium_large":
                return I18nManager.translate("message.message.general_225");
            case "medium":
                return I18nManager.translate("message.message.general_58");
            case "medium_small":
                return I18nManager.translate("message.message.general_226");
            case "small":
                return I18nManager.translate("message.message.general_111");
            case "extra_small":
                return I18nManager.translate("message.message.general_194");
            default:
                return size;
        }
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

    /**
     * 闁哄嫬澧介妵姘┍閳╁啩绱栨繛鎴濈墛娴?
     */
    public static void showInfo(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of("閹?" + message), false);
        }
    }
}
