package areahint.recolor;

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
 * Recolor用户界面系统
 * 使用聊天消息和可点击组件实现交互
 */
public class RecolorUI {

    /**
     * 显示域名选择界面
     */
    public static void showAreaSelectionScreen(List<AreaData> areas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 域名颜色修改 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要修改颜色的域名："), false);
        client.player.sendMessage(Text.of(""), false);

        for (AreaData area : areas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            String currentColor = area.getColor() != null ? area.getColor() : "#FFFFFF";

            // 创建域名选择按钮
            MutableText areaButton = Text.literal(
                String.format("§6[%s] §7(等级%d) §8当前颜色: %s", displayName, area.getLevel(), currentColor)
            ).setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint recolor select \"" + area.getName() + "\""))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("选择 " + displayName + " 进行颜色修改")))
                .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消本次操作]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消Recolor流程")))
                .withColor(Formatting.RED));

        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 显示颜色选择界面
     */
    public static void showColorSelectionScreen(String areaName, String currentColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 选择新颜色 ==="), false);
        client.player.sendMessage(Text.of("§a域名：§6" + areaName), false);
        client.player.sendMessage(Text.of("§a当前颜色：§6" + currentColor), false);
        client.player.sendMessage(Text.of("§a请选择新的颜色："), false);
        client.player.sendMessage(Text.of(""), false);

        // 第一行颜色按钮
        MutableText row1 = Text.empty()
            .append(createColorButton("白色", "#FFFFFF", "§f"))
            .append(Text.of("  "))
            .append(createColorButton("灰色", "#808080", "§7"))
            .append(Text.of("  "))
            .append(createColorButton("深灰色", "#555555", "§8"))
            .append(Text.of("  "))
            .append(createColorButton("黑色", "#000000", "§0"));

        // 第二行颜色按钮
        MutableText row2 = Text.empty()
            .append(createColorButton("深红色", "#AA0000", "§4"))
            .append(Text.of("  "))
            .append(createColorButton("红色", "#FF5555", "§c"))
            .append(Text.of("  "))
            .append(createColorButton("粉红色", "#FF55FF", "§d"))
            .append(Text.of("  "))
            .append(createColorButton("橙色", "#FFAA00", "§6"));

        // 第三行颜色按钮
        MutableText row3 = Text.empty()
            .append(createColorButton("黄色", "#FFFF55", "§e"))
            .append(Text.of("  "))
            .append(createColorButton("绿色", "#55FF55", "§a"))
            .append(Text.of("  "))
            .append(createColorButton("深绿色", "#00AA00", "§2"))
            .append(Text.of("  "))
            .append(createColorButton("天蓝色", "#55FFFF", "§b"));

        // 第四行颜色按钮
        MutableText row4 = Text.empty()
            .append(createColorButton("湖蓝色", "#00AAAA", "§3"))
            .append(Text.of("  "))
            .append(createColorButton("蓝色", "#5555FF", "§9"))
            .append(Text.of("  "))
            .append(createColorButton("深蓝色", "#0000AA", "§1"))
            .append(Text.of("  "))
            .append(createColorButton("紫色", "#AA00AA", "§5"));

        // 闪烁效果按钮行
        MutableText row5 = Text.empty()
            .append(createColorButton("整体黑白闪烁", "FLASH_BW_ALL", "§7"))
            .append(Text.of("  "))
            .append(createColorButton("整体彩虹闪烁", "FLASH_RAINBOW_ALL", "§b"))
            .append(Text.of("  "))
            .append(createColorButton("单字黑白闪烁", "FLASH_BW_CHAR", "§8"))
            .append(Text.of("  "))
            .append(createColorButton("单字彩虹闪烁", "FLASH_RAINBOW_CHAR", "§d"));

        // 取消按钮
        MutableText cancelButton = Text.literal("§c[取消本次操作]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消Recolor流程")))
                .withColor(Formatting.RED));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(row3, false);
        client.player.sendMessage(row4, false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§6--- 闪烁效果 ---"), false);
        client.player.sendMessage(row5, false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 显示确认界面
     */
    public static void showConfirmScreen(String areaName, String oldColor, String newColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 确认颜色修改 ==="), false);
        client.player.sendMessage(Text.of("§f您确认要修改域名颜色吗？"), false);
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§a域名：§6" + areaName), false);
        client.player.sendMessage(Text.of("§a原颜色：§6" + oldColor), false);
        client.player.sendMessage(Text.of("§a新颜色：§6" + newColor), false);
        client.player.sendMessage(Text.of(""), false);

        // 显示确认和取消按钮
        MutableText confirmButton = Text.literal("§a[是]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("确认修改颜色")))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = Text.literal("§c[否]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint recolor cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消修改")))
                .withColor(Formatting.RED));

        MutableText buttonRow = Text.empty()
            .append(confirmButton)
            .append(Text.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7请确认以上信息无误后点击按钮"), false);
    }

    /**
     * 创建颜色选择按钮
     */
    private static MutableText createColorButton(String colorName, String colorValue, String minecraftColor) {
        return Text.literal(minecraftColor + "[" + colorName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint recolor color " + colorValue))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("选择 " + colorName + " 作为新颜色"))));
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
}
