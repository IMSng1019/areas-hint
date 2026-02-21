package areahint.dimensional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Map;

/**
 * 维度域名交互UI
 */
public class DimensionalNameUI {

    /**
     * 显示维度选择界面
     * @param commandPrefix "dimname" 或 "dimcolor"
     */
    public static void showDimensionSelectionScreen(String commandPrefix) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String title = commandPrefix.equals("dimname") ? "维度域名修改" : "维度域名颜色修改";
        String cmdBase = commandPrefix.equals("dimname") ? "dimensionalityname" : "dimensionalitycolor";

        client.player.sendMessage(Text.of("§6=== " + title + " ==="), false);
        client.player.sendMessage(Text.of("§a请选择要操作的维度："), false);
        client.player.sendMessage(Text.of(""), false);

        Map<String, String> names = ClientDimensionalNameManager.getAllDimensionalNames();
        for (Map.Entry<String, String> entry : names.entrySet()) {
            String dimId = entry.getKey();
            String dimName = entry.getValue();
            String color = ClientDimensionalNameManager.getDimensionalColor(dimId);
            String colorDisplay = color != null ? color : "§f#FFFFFF(默认)";

            String label;
            if (commandPrefix.equals("dimcolor")) {
                label = String.format("§6[%s] §7(%s) §8颜色: %s", dimName, dimId, colorDisplay);
            } else {
                label = String.format("§6[%s] §7(%s)", dimName, dimId);
            }

            MutableText btn = Text.literal(label)
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint " + cmdBase + " select \"" + dimId + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("选择 " + dimName)))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(btn, false);
        }

        client.player.sendMessage(Text.of(""), false);
        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + cmdBase + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消操作")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    /**
     * 显示名称输入提示界面
     */
    public static void showNameInputScreen(String dimensionId, String currentName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 输入新的维度域名 ==="), false);
        client.player.sendMessage(Text.of("§a维度: §b" + dimensionId), false);
        client.player.sendMessage(Text.of("§a当前名称: §6" + currentName), false);
        client.player.sendMessage(Text.of(""), false);

        // 提供一个可点击的建议命令
        MutableText inputHint = Text.literal("§e[点击此处输入新名称]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint dimensionalityname name "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("点击后在聊天栏输入新名称"))));
        client.player.sendMessage(inputHint, false);

        client.player.sendMessage(Text.of(""), false);
        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalityname cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消操作")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    /**
     * 显示名称确认界面
     */
    public static void showNameConfirmScreen(String dimensionId, String oldName, String newName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 确认维度域名修改 ==="), false);
        client.player.sendMessage(Text.of("§f您确认要修改维度域名吗？"), false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§a维度: §b" + dimensionId), false);
        client.player.sendMessage(Text.of("§a原名称: §7" + oldName), false);
        client.player.sendMessage(Text.of("§a新名称: §6" + newName), false);
        client.player.sendMessage(Text.of(""), false);

        MutableText confirmBtn = Text.literal("§a[是]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalityname confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("确认修改")))
                .withColor(Formatting.GREEN));

        MutableText cancelBtn = Text.literal("§c[否]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalityname cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消修改")))
                .withColor(Formatting.RED));

        client.player.sendMessage(Text.empty().append(confirmBtn).append(Text.of("  ")).append(cancelBtn), false);
    }

    /**
     * 显示颜色选择界面
     */
    public static void showColorSelectionScreen(String dimensionId, String dimName, String currentColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 选择维度域名颜色 ==="), false);
        client.player.sendMessage(Text.of("§a维度: §b" + dimName + " §7(" + dimensionId + ")"), false);
        client.player.sendMessage(Text.of("§a当前颜色: §6" + currentColor), false);
        client.player.sendMessage(Text.of("§a请选择新的颜色："), false);
        client.player.sendMessage(Text.of(""), false);

        // 颜色按钮行
        showColorRows(client);

        client.player.sendMessage(Text.of(""), false);
        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消操作")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    private static void showColorRows(MinecraftClient client) {
        MutableText row1 = Text.empty()
            .append(colorBtn("白色", "#FFFFFF", "§f"))
            .append(Text.of("  "))
            .append(colorBtn("灰色", "#808080", "§7"))
            .append(Text.of("  "))
            .append(colorBtn("深灰色", "#555555", "§8"))
            .append(Text.of("  "))
            .append(colorBtn("黑色", "#000000", "§0"));

        MutableText row2 = Text.empty()
            .append(colorBtn("深红色", "#AA0000", "§4"))
            .append(Text.of("  "))
            .append(colorBtn("红色", "#FF5555", "§c"))
            .append(Text.of("  "))
            .append(colorBtn("粉红色", "#FF55FF", "§d"))
            .append(Text.of("  "))
            .append(colorBtn("橙色", "#FFAA00", "§6"));

        MutableText row3 = Text.empty()
            .append(colorBtn("黄色", "#FFFF55", "§e"))
            .append(Text.of("  "))
            .append(colorBtn("绿色", "#55FF55", "§a"))
            .append(Text.of("  "))
            .append(colorBtn("深绿色", "#00AA00", "§2"))
            .append(Text.of("  "))
            .append(colorBtn("天蓝色", "#55FFFF", "§b"));

        MutableText row4 = Text.empty()
            .append(colorBtn("湖蓝色", "#00AAAA", "§3"))
            .append(Text.of("  "))
            .append(colorBtn("蓝色", "#5555FF", "§9"))
            .append(Text.of("  "))
            .append(colorBtn("深蓝色", "#0000AA", "§1"))
            .append(Text.of("  "))
            .append(colorBtn("紫色", "#AA00AA", "§5"));

        MutableText row5 = Text.empty()
            .append(colorBtn("整体黑白闪烁", "FLASH_BW_ALL", "§7"))
            .append(Text.of("  "))
            .append(colorBtn("整体彩虹闪烁", "FLASH_RAINBOW_ALL", "§b"))
            .append(Text.of("  "))
            .append(colorBtn("单字黑白闪烁", "FLASH_BW_CHAR", "§8"))
            .append(Text.of("  "))
            .append(colorBtn("单字彩虹闪烁", "FLASH_RAINBOW_CHAR", "§d"));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(row3, false);
        client.player.sendMessage(row4, false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§6--- 闪烁效果 ---"), false);
        client.player.sendMessage(row5, false);

        client.player.sendMessage(Text.of(""), false);
        MutableText customBtn = Text.literal("§e[自定义颜色(十六进制)]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint dimensionalitycolor color #"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("点击后输入十六进制颜色，如 #FF0000"))));
        client.player.sendMessage(customBtn, false);
    }

    private static MutableText colorBtn(String name, String value, String mcColor) {
        return Text.literal(mcColor + "[" + name + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor color " + value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("选择 " + name))));
    }

    /**
     * 显示颜色确认界面
     */
    public static void showColorConfirmScreen(String dimensionId, String dimName,
                                               String oldColor, String newColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 确认维度域名颜色修改 ==="), false);
        client.player.sendMessage(Text.of("§f您确认将该维度域名的颜色更改为该颜色吗？"), false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§a维度: §b" + dimName + " §7(" + dimensionId + ")"), false);
        client.player.sendMessage(Text.of("§a原颜色: §7" + oldColor), false);
        client.player.sendMessage(Text.of("§a新颜色: §6" + newColor), false);
        client.player.sendMessage(Text.of(""), false);

        MutableText confirmBtn = Text.literal("§a[是]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("确认修改")))
                .withColor(Formatting.GREEN));

        MutableText cancelBtn = Text.literal("§c[否]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint dimensionalitycolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消修改")))
                .withColor(Formatting.RED));

        client.player.sendMessage(Text.empty().append(confirmBtn).append(Text.of("  ")).append(cancelBtn), false);
    }

    /**
     * 显示首次维度命名提示
     */
    public static void showFirstNamePrompt(String dimensionId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 新维度发现 ==="), false);
        client.player.sendMessage(Text.of("§a你进入了一个未命名的维度: §b" + dimensionId), false);
        client.player.sendMessage(Text.of("§a请为这个维度命名（5分钟内未提交将使用默认名称）："), false);
        client.player.sendMessage(Text.of(""), false);

        MutableText inputBtn = Text.literal("§e[点击此处输入名称]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint firstdimname "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("点击后在聊天栏输入维度名称"))));
        client.player.sendMessage(inputBtn, false);

        client.player.sendMessage(Text.of(""), false);
        MutableText skipBtn = Text.literal("§7[跳过]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint firstdimname_skip"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("使用默认名称"))));
        client.player.sendMessage(skipBtn, false);
    }
}
