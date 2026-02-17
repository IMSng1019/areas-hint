package areahint.subtitlesize;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * SubtitleSize用户界面系统
 * 使用聊天消息和可点击组件实现交互
 */
public class SubtitleSizeUI {

    /**
     * 显示大小选择界面
     * @param currentSize 当前大小
     */
    public static void showSizeSelectionScreen(String currentSize) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 字幕大小设置 ==="), false);
        client.player.sendMessage(Text.of("§a当前大小: §6" + getSizeDisplayName(currentSize)), false);
        client.player.sendMessage(Text.of("§a请选择新的字幕大小："), false);
        client.player.sendMessage(Text.of(""), false);

        // 第一行按钮：极大、大、较大、中
        MutableText row1 = Text.empty()
            .append(createSizeButton("极大", "extra_large", "§d"))
            .append(Text.of("  "))
            .append(createSizeButton("大", "large", "§b"))
            .append(Text.of("  "))
            .append(createSizeButton("较大", "medium_large", "§a"))
            .append(Text.of("  "))
            .append(createSizeButton("中", "medium", "§e"));

        // 第二行按钮：较小、小、极小
        MutableText row2 = Text.empty()
            .append(createSizeButton("较小", "medium_small", "§6"))
            .append(Text.of("  "))
            .append(createSizeButton("小", "small", "§c"))
            .append(Text.of("  "))
            .append(createSizeButton("极小", "extra_small", "§4"));

        // 取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlesize cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消大小设置")))
                .withColor(Formatting.RED));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(cancelButton, false);
        client.player.sendMessage(Text.of("§7提示：选择大小后将自动重新加载配置"), false);
    }

    /**
     * 创建大小选择按钮
     */
    private static MutableText createSizeButton(String displayName, String sizeValue, String colorCode) {
        return Text.literal(colorCode + "[" + displayName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint subtitlesize select " + sizeValue))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("选择 " + displayName + " 作为字幕大小"))));
    }

    /**
     * 获取大小的显示名称
     */
    private static String getSizeDisplayName(String size) {
        switch (size) {
            case "extra_large":
                return "极大";
            case "large":
                return "大";
            case "medium_large":
                return "较大";
            case "medium":
                return "中";
            case "medium_small":
                return "较小";
            case "small":
                return "小";
            case "extra_small":
                return "极小";
            default:
                return size;
        }
    }

    /**
     * 显示错误消息
     */
    public static void showError(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of("§c错误：" + message), false);
        }
    }

    /**
     * 显示成功消息
     */
    public static void showSuccess(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of("§a" + message), false);
        }
    }

    /**
     * 显示信息消息
     */
    public static void showInfo(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of("§7" + message), false);
        }
    }
}
