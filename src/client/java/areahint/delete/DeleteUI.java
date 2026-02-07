package areahint.delete;

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
 * Delete用户界面系统
 * 使用聊天消息和可点击组件实现交互
 */
public class DeleteUI {

    /**
     * 显示域名选择界面
     */
    public static void showAreaSelectionScreen(List<AreaData> deletableAreas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 删除域名 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要删除的域名："), false);
        client.player.sendMessage(Text.of("§7只显示您的权限内可以删除的域名"), false);
        client.player.sendMessage(Text.of(""), false);

        // 为每个可删除的域名创建按钮
        for (AreaData area : deletableAreas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);

            // 构建悬停提示信息
            String hoverText = "§6域名信息：\n" +
                "§a名称：§f" + area.getName() + "\n" +
                "§a等级：§f" + area.getLevel() + "\n" +
                "§a创建者：§f" + area.getSignature();

            if (area.getBaseName() != null) {
                hoverText += "\n§a上级域名：§f" + area.getBaseName();
            }

            hoverText += "\n\n§e点击选择此域名";

            MutableText areaButton = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint delete select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of(hoverText)))
                    .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        client.player.sendMessage(Text.of(""), false);

        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint delete cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消删除流程")))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 显示确认删除界面（二级确认）
     */
    public static void showConfirmDeleteScreen(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String displayName = areahint.util.AreaDataConverter.getDisplayName(area);

        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§6=== 确认删除 ==="), false);
        client.player.sendMessage(Text.of("§c§l警告：您确认要删除该域名吗？"), false);
        client.player.sendMessage(Text.of("§c§l该过程不可逆！！！"), false);
        client.player.sendMessage(Text.of(""), false);

        // 显示域名详细信息
        client.player.sendMessage(Text.of("§a域名名称：§6" + area.getName()), false);

        if (area.getSurfacename() != null && !area.getSurfacename().trim().isEmpty()) {
            client.player.sendMessage(Text.of("§a联合域名：§6" + area.getSurfacename()), false);
        }

        client.player.sendMessage(Text.of("§a域名等级：§6" + area.getLevel()), false);

        if (area.getBaseName() != null) {
            client.player.sendMessage(Text.of("§a上级域名：§6" + area.getBaseName()), false);
        } else {
            client.player.sendMessage(Text.of("§a上级域名：§6无（顶级域名）"), false);
        }

        client.player.sendMessage(Text.of("§a顶点数量：§6" + area.getVertices().size() + " 个"), false);

        if (area.getAltitude() != null) {
            String minAlt = area.getAltitude().getMin() != null ?
                String.valueOf(area.getAltitude().getMin()) : "无限制";
            String maxAlt = area.getAltitude().getMax() != null ?
                String.valueOf(area.getAltitude().getMax()) : "无限制";
            client.player.sendMessage(Text.of("§a高度范围：§6" + minAlt + " ~ " + maxAlt), false);
        }

        client.player.sendMessage(Text.of("§a创建者：§6" + area.getSignature()), false);

        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(Text.of("§c删除后将无法恢复，请谨慎操作！"), false);
        client.player.sendMessage(Text.of(""), false);

        // 显示确认和取消按钮
        MutableText confirmButton = Text.literal("§c§l[是 - 确认删除]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint delete confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.of("§c确认删除域名 \"" + displayName + "\"\n§c此操作不可逆！")))
                .withColor(Formatting.RED)
                .withBold(true));

        MutableText cancelButton = Text.literal("§a§l[否 - 取消删除]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint delete cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("§a取消删除，返回安全状态")))
                .withColor(Formatting.GREEN)
                .withBold(true));

        MutableText buttonRow = Text.empty()
            .append(confirmButton)
            .append(Text.of("  "))
            .append(cancelButton);

        client.player.sendMessage(buttonRow, false);
    }
}
