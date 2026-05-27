package areahint.subtitle;

import areahint.data.AreaData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 副字幕聊天交互界面。
 * <p>
 * 与现有 Delete/Recolor/TitleSize 一样使用聊天按钮，让玩家在不打开额外屏幕的情况下完成编辑。
 */
public class SubtitleUI {
    public enum AreaSelectionMode {
        ADD,
        DELETE,
        COLOR
    }

    public static void showAreaSelectionScreen(List<AreaData> areas, AreaSelectionMode mode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of(getAreaSelectionTitle(mode)), false);
        client.player.sendMessage(Text.of(getAreaSelectionPrompt(mode)), false);
        client.player.sendMessage(Text.of(""), false);

        for (AreaData area : areas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            String commandPrefix = getCommandPrefix(mode);
            String subtitlePreview = area.hasSubtitle() ? area.getSubtitle().replace("\n", " / ") : "无";
            MutableText areaButton = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint " + commandPrefix + " select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("域名: " + area.getName() + "\n等级: " + area.getLevel() + "\n当前副字幕: " + subtitlePreview)))
                    .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(createCancelButton(getCommandPrefix(mode)), false);
    }

    public static void showSubtitleInputScreen(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of("§6=== 添加/替换副字幕 ==="), false);
        client.player.sendMessage(Text.of("§e域名: §f" + area.getName()), false);
        if (area.hasSubtitle()) {
            client.player.sendMessage(Text.of("§7当前副字幕: §f" + area.getSubtitle().replace("\n", " / ")), false);
        }
        client.player.sendMessage(Text.of("§a输入: §f/areahint addsubtitle text <副字幕文本>"), false);
        client.player.sendMessage(Text.of("§7可使用 /n 手动换行；使用 /n 后不会自动换行。"), false);
        client.player.sendMessage(createCancelButton("addsubtitle"), false);
    }

    public static void showAddConfirmScreen(AreaData area, String subtitle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of("§6=== 确认副字幕 ==="), false);
        client.player.sendMessage(Text.of("§e域名: §f" + area.getName()), false);
        client.player.sendMessage(Text.of("§e副字幕: §f" + subtitle.replace("\n", " / ")), false);

        MutableText confirmButton = Text.literal("§a[确认]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint addsubtitle confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("保存副字幕")))
                .withColor(Formatting.GREEN));

        client.player.sendMessage(buttonRow(confirmButton, createCancelButton("addsubtitle")), false);
    }

    public static void showDeleteConfirmScreen(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of("§6=== 删除副字幕确认 ==="), false);
        client.player.sendMessage(Text.of("§e域名: §f" + area.getName()), false);
        client.player.sendMessage(Text.of("§e当前副字幕: §f" + area.getSubtitle().replace("\n", " / ")), false);

        MutableText confirmButton = Text.literal("§c[删除副字幕]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletesubtitle confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("只删除 subtitle 和 subtitlecolor 字段")))
                .withColor(Formatting.RED)
                .withBold(true));

        client.player.sendMessage(buttonRow(confirmButton, createCancelButton("deletesubtitle")), false);
    }

    public static void showColorSelectionScreen(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of("§6=== 副字幕颜色选择 ==="), false);
        client.player.sendMessage(Text.of("§e域名: §f" + area.getName()), false);
        client.player.sendMessage(Text.of("§e当前副字幕颜色: §f" + area.getSubtitleColor()), false);
        client.player.sendMessage(Text.of(""), false);

        client.player.sendMessage(colorRow(
            colorButton("白", "#FFFFFF", "§f"), colorButton("灰", "#808080", "§7"),
            colorButton("深灰", "#555555", "§8"), colorButton("黑", "#000000", "§0")), false);
        client.player.sendMessage(colorRow(
            colorButton("暗红", "#AA0000", "§4"), colorButton("红", "#FF5555", "§c"),
            colorButton("粉", "#FF55FF", "§d"), colorButton("金", "#FFAA00", "§6")), false);
        client.player.sendMessage(colorRow(
            colorButton("黄", "#FFFF55", "§e"), colorButton("绿", "#55FF55", "§a"),
            colorButton("深绿", "#00AA00", "§2"), colorButton("青", "#55FFFF", "§b")), false);
        client.player.sendMessage(colorRow(
            colorButton("深青", "#00AAAA", "§3"), colorButton("蓝", "#5555FF", "§9"),
            colorButton("深蓝", "#0000AA", "§1"), colorButton("紫", "#AA00AA", "§5")), false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(colorRow(
            colorButton("黑白闪烁", "FLASH_BW_ALL", "§7"), colorButton("彩虹闪烁", "FLASH_RAINBOW_ALL", "§b"),
            colorButton("逐字黑白", "FLASH_BW_CHAR", "§8"), colorButton("逐字彩虹", "FLASH_RAINBOW_CHAR", "§d")), false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(createCancelButton("replacesubtitlecolor"), false);
    }

    public static void showColorConfirmScreen(AreaData area, String newColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of("§6=== 确认副字幕颜色 ==="), false);
        client.player.sendMessage(Text.of("§e域名: §f" + area.getName()), false);
        client.player.sendMessage(Text.of("§e原颜色: §f" + area.getSubtitleColor()), false);
        client.player.sendMessage(Text.of("§e新颜色: §f" + newColor), false);

        MutableText confirmButton = Text.literal("§a[确认]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint replacesubtitlecolor confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("保存副字幕颜色")))
                .withColor(Formatting.GREEN));

        client.player.sendMessage(buttonRow(confirmButton, createCancelButton("replacesubtitlecolor")), false);
    }

    public static void showSubtitleSizeSelectionScreen(String currentSize) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.of("§6=== 副字幕大小选择 ==="), false);
        client.player.sendMessage(Text.of("§e当前大小: §f" + getSizeDisplayName(currentSize)), false);
        client.player.sendMessage(Text.of(""), false);

        MutableText row0 = Text.empty()
            .append(sizeButton("自动", "auto", "§f"))
            .append(Text.of("  "))
            .append(sizeButton("极大", "extra_large", "§d"))
            .append(Text.of("  "))
            .append(sizeButton("大", "large", "§b"))
            .append(Text.of("  "))
            .append(sizeButton("较大", "medium_large", "§a"));

        MutableText row1 = Text.empty()
            .append(sizeButton("中", "medium", "§e"))
            .append(Text.of("  "))
            .append(sizeButton("较小", "medium_small", "§6"))
            .append(Text.of("  "))
            .append(sizeButton("小", "small", "§c"))
            .append(Text.of("  "))
            .append(sizeButton("极小", "extra_small", "§4"));

        client.player.sendMessage(row0, false);
        client.player.sendMessage(row1, false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(createCancelButton("replacesubtitlesize"), false);
    }

    public static String getSizeDisplayName(String size) {
        switch (size) {
            case "auto":
                return "自动";
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

    private static MutableText colorRow(MutableText first, MutableText second, MutableText third, MutableText fourth) {
        return Text.empty()
            .append(first).append(Text.of("  "))
            .append(second).append(Text.of("  "))
            .append(third).append(Text.of("  "))
            .append(fourth);
    }

    private static MutableText colorButton(String displayName, String colorValue, String colorCode) {
        return Text.literal(colorCode + "[" + displayName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint replacesubtitlecolor color " + colorValue))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("选择副字幕颜色: " + displayName))));
    }

    private static MutableText sizeButton(String displayName, String sizeValue, String colorCode) {
        return Text.literal(colorCode + "[" + displayName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint replacesubtitlesize select " + sizeValue))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("选择副字幕大小: " + displayName))));
    }

    private static MutableText createCancelButton(String commandPrefix) {
        return Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint " + commandPrefix + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消当前副字幕流程")))
                .withColor(Formatting.RED));
    }

    private static MutableText buttonRow(MutableText first, MutableText second) {
        return Text.empty().append(first).append(Text.of("  ")).append(second);
    }

    private static String getCommandPrefix(AreaSelectionMode mode) {
        switch (mode) {
            case DELETE:
                return "deletesubtitle";
            case COLOR:
                return "replacesubtitlecolor";
            case ADD:
            default:
                return "addsubtitle";
        }
    }

    private static String getAreaSelectionTitle(AreaSelectionMode mode) {
        switch (mode) {
            case DELETE:
                return "§6=== 删除副字幕 - 选择域名 ===";
            case COLOR:
                return "§6=== 修改副字幕颜色 - 选择域名 ===";
            case ADD:
            default:
                return "§6=== 添加/替换副字幕 - 选择域名 ===";
        }
    }

    private static String getAreaSelectionPrompt(AreaSelectionMode mode) {
        switch (mode) {
            case DELETE:
                return "§a请选择要删除副字幕的域名:";
            case COLOR:
                return "§a请选择要修改副字幕颜色的域名:";
            case ADD:
            default:
                return "§a请选择要添加或替换副字幕的域名:";
        }
    }
}
