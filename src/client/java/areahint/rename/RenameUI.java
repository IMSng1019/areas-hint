package areahint.rename;

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
 * Rename用户界面系统
 * 使用聊天消息和可点击组件实现交互
 */
public class RenameUI {

    /**
     * 显示域名选择界面
     */
    public static void showAreaSelectScreen(List<AreaData> areas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== Rename 交互式域名重命名 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要重命名的域名："), false);

        for (AreaData area : areas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            String signature = area.getSignature() != null ? area.getSignature() : "未知创建者";

            MutableText areaButton = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint rename select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("选择 " + displayName + " 进行重命名\n创建者: " + signature)))
                    .withColor(Formatting.GOLD));

            client.player.sendMessage(areaButton, false);
        }

        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消Rename流程")))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 显示新域名名称输入界面
     */
    public static void showNewNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 输入新域名名称 ==="), false);
        client.player.sendMessage(Text.of("§a请在聊天框中输入新的域名名称："), false);
        client.player.sendMessage(Text.of("§7新名称不能与现有域名重复"), false);

        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消Rename流程")))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 显示联合域名输入界面
     */
    public static void showSurfaceNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 联合域名设置 ==="), false);
        client.player.sendMessage(Text.of("§a请输入新的联合域名："), false);
        client.player.sendMessage(Text.of("§7联合域名是显示给玩家看的名称，可以与实际域名不同"), false);
        client.player.sendMessage(Text.of("§7留空则使用实际域名作为显示名称"), false);
        client.player.sendMessage(Text.of("§e提示：两个不同的实际域名可以使用相同的联合域名"), false);

        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消Rename流程")))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 显示确认界面
     */
    public static void showConfirmScreen(String oldName, String newName, String newSurfaceName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.of("§6=== 域名重命名确认 ==="), false);
        client.player.sendMessage(Text.of("§f您确认将域名重命名吗？"), false);
        client.player.sendMessage(Text.of("§7原域名: §f" + oldName), false);
        client.player.sendMessage(Text.of("§7新域名: §f" + newName), false);

        if (newSurfaceName != null && !newSurfaceName.trim().isEmpty()) {
            client.player.sendMessage(Text.of("§7新联合域名: §f" + newSurfaceName), false);
        } else {
            client.player.sendMessage(Text.of("§7新联合域名: §7(使用域名作为显示名称)"), false);
        }

        // 显示确认和取消按钮
        MutableText confirmButton = Text.literal("§a[是]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("确认重命名")))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = Text.literal("§c[否]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint rename cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消重命名")))
                .withColor(Formatting.RED));

        MutableText buttonRow = Text.empty()
            .append(confirmButton)
            .append(Text.of("  "))
            .append(cancelButton);

        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7请确认以上信息无误后点击选择"), false);
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
