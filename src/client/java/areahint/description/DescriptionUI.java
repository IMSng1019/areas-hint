package areahint.description;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 域名描述聊天交互 UI。
 */
public final class DescriptionUI {
    private DescriptionUI() {
    }

    public static void showSearchPrompt(String commandPrefix, boolean dimensionTarget, boolean deleteOperation) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        String target = dimensionTarget ? "维度域名" : "域名";
        String operation = deleteOperation ? "删除描述" : "添加描述";
        client.player.sendMessage(Text.literal("==== " + target + operation + " ====").formatted(Formatting.AQUA), false);
        client.player.sendMessage(Text.literal("请输入要搜索的" + target + "名称："), false);

        MutableText input = Text.literal("[点击输入搜索词]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint " + commandPrefix + " search "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal("点击后在命令后输入搜索词")))
                .withColor(Formatting.GOLD));
        MutableText cancel = cancelButton(commandPrefix);
        client.player.sendMessage(Text.empty().append(input).append(Text.literal(" ")).append(cancel), false);
    }

    public static void showSelection(String commandPrefix, List<DescriptionListEntry> entries) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal("请选择目标：").formatted(Formatting.AQUA), false);
        for (DescriptionListEntry entry : entries) {
            MutableText button = Text.literal("[" + nullToText(entry.displayName()) + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint " + commandPrefix + " select " + quoteCommandArgument(entry.id())))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createHoverText(entry)))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(button, false);
        }
        client.player.sendMessage(cancelButton(commandPrefix), false);
    }

    public static void showDescriptionInput(String commandPrefix, DescriptionListEntry entry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal("已选择：" + nullToText(entry.displayName())).formatted(Formatting.GREEN), false);
        client.player.sendMessage(Text.literal("请输入描述正文："), false);
        MutableText input = Text.literal("[点击输入描述]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint " + commandPrefix + " text "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal("点击后在命令后输入完整描述")))
                .withColor(Formatting.GOLD));
        client.player.sendMessage(Text.empty().append(input).append(Text.literal(" ")).append(cancelButton(commandPrefix)), false);
    }

    public static void showAddConfirm(String commandPrefix, DescriptionListEntry entry, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal("==== 确认保存描述 ====").formatted(Formatting.AQUA), false);
        client.player.sendMessage(Text.literal("目标：" + nullToText(entry.displayName())), false);
        client.player.sendMessage(Text.literal("描述长度：" + (description == null ? 0 : description.length())), false);
        String preview = description == null ? "" : description;
        if (preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }
        client.player.sendMessage(Text.literal("预览：" + preview), false);

        MutableText confirm = Text.literal("[确认保存]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("保存描述文件")))
                .withColor(Formatting.GREEN));
        client.player.sendMessage(Text.empty().append(confirm).append(Text.literal(" ")).append(cancelButton(commandPrefix)), false);
    }

    public static void showDeleteConfirmFirst(String commandPrefix, DescriptionListEntry entry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal("==== 确认删除描述 ====").formatted(Formatting.RED), false);
        client.player.sendMessage(Text.literal("目标：" + nullToText(entry.displayName())), false);
        client.player.sendMessage(Text.literal("此操作只删除描述文件，不删除域名。"), false);
        MutableText confirm = Text.literal("[继续删除]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("进入二次确认")))
                .withColor(Formatting.RED));
        client.player.sendMessage(Text.empty().append(confirm).append(Text.literal(" ")).append(cancelButton(commandPrefix)), false);
    }

    public static void showDeleteConfirmSecond(String commandPrefix, DescriptionListEntry entry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal("二次确认：只删除描述文件，不删除域名。").formatted(Formatting.RED), false);
        client.player.sendMessage(Text.literal("目标：" + nullToText(entry.displayName())), false);
        MutableText confirm = Text.literal("[确认只删除描述]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " confirm2"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("确认删除描述文件")))
                .withColor(Formatting.RED)
                .withBold(true));
        client.player.sendMessage(Text.empty().append(confirm).append(Text.literal(" ")).append(cancelButton(commandPrefix)), false);
    }

    private static MutableText cancelButton(String commandPrefix) {
        return Text.literal("[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("取消本次操作")))
                .withColor(Formatting.RED));
    }

    private static Text createHoverText(DescriptionListEntry entry) {
        return Text.literal(
            "显示名：" + nullToText(entry.displayName()) + "\n" +
            "真实名称/维度ID：" + nullToText(entry.id()) + "\n" +
            "等级：" + (entry.level() == 0 ? "维度域名" : entry.level()) + "\n" +
            "上级域名：" + nullToText(entry.baseName()) + "\n" +
            "签名者：" + nullToText(entry.signature()) + "\n" +
            "维度：" + nullToText(entry.dimension())
        );
    }

    private static String quoteCommandArgument(String value) {
        String escaped = value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private static String nullToText(String value) {
        return value == null || value.trim().isEmpty() ? "无" : value;
    }
}
