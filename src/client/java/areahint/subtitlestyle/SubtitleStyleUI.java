package areahint.subtitlestyle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * SubtitleStyle用户界面系统
 * 使用聊天消息和可点击组件实现交互
 */
public class SubtitleStyleUI {

    /**
     * 显示样式选择界面
     * @param currentStyle 当前样式
     */
    public static void showStyleSelectionScreen(String currentStyle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 字幕样式设置 ==="), false);
        client.player.sendMessage(Text.of("§a当前样式: §6" + getStyleDisplayName(currentStyle)), false);
        client.player.sendMessage(Text.of("§a请选择新的字幕样式："), false);
        client.player.sendMessage(Text.of(""), false);

        // 创建样式选择按钮
        MutableText fullButton = Text.literal("§b[完整样式]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle select full"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("显示完整的域名层级信息\n例如: 一级域名 > 二级域名 > 三级域名")))
                .withColor(Formatting.AQUA));

        MutableText simpleButton = Text.literal("§e[简洁样式]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle select simple"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("只显示当前所在的最低级域名\n例如: 三级域名")))
                .withColor(Formatting.YELLOW));

        MutableText mixedButton = Text.literal("§d[混合样式]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle select mixed"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("智能显示域名信息\n根据层级自动调整显示方式")))
                .withColor(Formatting.LIGHT_PURPLE));

        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint subtitlestyle cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消样式设置")))
                .withColor(Formatting.RED));

        // 组合按钮显示
        MutableText buttonRow = Text.empty()
            .append(fullButton)
            .append(Text.of("  "))
            .append(simpleButton)
            .append(Text.of("  "))
            .append(mixedButton)
            .append(Text.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§7提示：选择样式后将自动重新加载配置"), false);
    }

    /**
     * 获取样式的显示名称
     */
    private static String getStyleDisplayName(String style) {
        switch (style) {
            case "full":
                return "完整样式 (Full)";
            case "simple":
                return "简洁样式 (Simple)";
            case "mixed":
                return "混合样式 (Mixed)";
            default:
                return style;
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
