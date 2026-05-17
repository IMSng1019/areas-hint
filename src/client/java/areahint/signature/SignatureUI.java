package areahint.signature;

import areahint.data.AreaData;
import areahint.util.AreaDataConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.StringJoiner;

/**
 * Signature聊天按钮界面。
 *
 * <p>界面风格参考EasyAdd：每一步都用聊天提示和可点击按钮推进，
 * 同时保留命令输入方式，方便玩家手动补全。</p>
 */
public class SignatureUI {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    /**
     * 显示可修改签名的域名列表。
     */
    public static void showAreaSelectionScreen(SignatureManager.Operation operation, List<AreaData> areas, boolean admin) {
        if (client.player == null) {
            return;
        }

        String commandPrefix = getCommandPrefix(operation);
        String operationName = getOperationName(operation);

        client.player.sendMessage(Text.literal("§6=== " + operationName + "域名扩展签名 - 选择域名 ==="), false);
        client.player.sendMessage(Text.literal("§a请选择需要" + operationName + "扩展签名的域名："), false);
        if (admin) {
            client.player.sendMessage(Text.literal("§7管理员模式：当前维度全部域名均可选择"), false);
        } else {
            client.player.sendMessage(Text.literal("§7普通模式：只显示base-name引用了你签名域名的域名"), false);
        }

        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            MutableText areaButton = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint " + commandPrefix + " select " + quoteCommandArgument(area.getName())))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createAreaHoverText(area)))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(areaButton, false);
        }

        client.player.sendMessage(createCancelButton(commandPrefix), false);
    }

    /**
     * 显示玩家名输入界面。玩家直接在聊天栏输入名字，流程会拦截该聊天内容并推进到确认阶段。
     */
    public static void showPlayerNamePrompt(SignatureManager.Operation operation, AreaData area, List<String> removableSignatures) {
        if (client.player == null) {
            return;
        }

        String commandPrefix = getCommandPrefix(operation);

        client.player.sendMessage(Text.literal("§a已选择域名：§6" + AreaDataConverter.getDisplayName(area)), false);
        client.player.sendMessage(Text.literal("§7实际名称：" + nullToText(area.getName())), false);

        if (operation == SignatureManager.Operation.DELETE) {
            client.player.sendMessage(Text.literal("§7当前扩展签名：" + formatSignatures(removableSignatures)), false);
            client.player.sendMessage(Text.literal("§a请直接在聊天栏输入要删除的扩展签名玩家名："), false);
        } else {
            client.player.sendMessage(Text.literal("§a请直接在聊天栏输入需要扩展签名的玩家名："), false);
        }

        client.player.sendMessage(Text.literal("§7输入 cancel 或 取消 可以取消本次操作"), false);
        client.player.sendMessage(createCancelButton(commandPrefix), false);
    }

    /**
     * 显示第一层确认界面。
     */
    public static void showConfirmScreen(SignatureManager.Operation operation, AreaData area, String targetPlayerName) {
        if (client.player == null) {
            return;
        }

        String commandPrefix = getCommandPrefix(operation);
        String operationName = getOperationName(operation);

        client.player.sendMessage(Text.literal("§6=== 确认" + operationName + "扩展签名 ==="), false);
        client.player.sendMessage(Text.literal("§7实际名称：" + nullToText(area.getName())), false);
        client.player.sendMessage(Text.literal("§7显示名称：" + AreaDataConverter.getDisplayName(area)), false);
        client.player.sendMessage(Text.literal("§7base-name：" + nullToText(area.getBaseName())), false);
        client.player.sendMessage(Text.literal("§7创建者/旧签名：" + nullToText(area.getSignature())), false);
        client.player.sendMessage(Text.literal("§7扩展签名：" + formatSignatures(area.getSignatures())), false);
        client.player.sendMessage(Text.literal("§a目标玩家：§6" + targetPlayerName), false);

        String hover = operation == SignatureManager.Operation.DELETE ? "进入删除二次确认" : "确认添加扩展签名";
        MutableText confirmButton = Text.literal("§a[确认]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover)))
                .withColor(Formatting.GREEN));

        client.player.sendMessage(Text.empty()
            .append(confirmButton)
            .append(Text.literal("  "))
            .append(createCancelButton(commandPrefix)), false);
    }

    /**
     * 显示删除签名的二次确认界面。
     */
    public static void showFinalDeleteConfirmScreen(AreaData area, String targetPlayerName) {
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(Text.literal("§c=== 删除扩展签名 - 二次确认 ==="), false);
        client.player.sendMessage(Text.literal("§c即将从域名 §6" + AreaDataConverter.getDisplayName(area)
            + " §c删除扩展签名：§6" + targetPlayerName), false);
        client.player.sendMessage(Text.literal("§7此操作会写入当前世界维度的域名文件，并重新分发给在线玩家"), false);

        MutableText finalConfirmButton = Text.literal("§c[二次确认删除]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint deletesignature confirm2"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal("确认删除该扩展签名")))
                .withColor(Formatting.RED));

        client.player.sendMessage(Text.empty()
            .append(finalConfirmButton)
            .append(Text.literal("  "))
            .append(createCancelButton("deletesignature")), false);
    }

    private static MutableText createCancelButton(String commandPrefix) {
        return Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal("取消本次签名操作")))
                .withColor(Formatting.RED));
    }

    private static Text createAreaHoverText(AreaData area) {
        return Text.literal(
            "实际名称：" + nullToText(area.getName()) + "\n" +
            "显示名称：" + AreaDataConverter.getDisplayName(area) + "\n" +
            "等级：" + area.getLevel() + "\n" +
            "base-name：" + nullToText(area.getBaseName()) + "\n" +
            "创建者/旧签名：" + nullToText(area.getSignature()) + "\n" +
            "扩展签名：" + formatSignatures(area.getSignatures())
        );
    }

    private static String getCommandPrefix(SignatureManager.Operation operation) {
        return operation == SignatureManager.Operation.ADD ? "addsignature" : "deletesignature";
    }

    private static String getOperationName(SignatureManager.Operation operation) {
        return operation == SignatureManager.Operation.ADD ? "添加" : "删除";
    }

    private static String quoteCommandArgument(String value) {
        String escaped = value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private static String formatSignatures(List<String> signatures) {
        if (signatures == null || signatures.isEmpty()) {
            return "无";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (String signature : signatures) {
            joiner.add(signature);
        }
        return joiner.toString();
    }

    private static String nullToText(String value) {
        return value == null || value.trim().isEmpty() ? "无" : value;
    }
}
