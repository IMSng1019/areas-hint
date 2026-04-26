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
 * Signature聊天按钮界面
 */
public class SignatureUI {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void showAreaSelectionScreen(SignatureManager.Operation operation, List<AreaData> areas) {
        if (client.player == null) return;

        String operationName = getOperationName(operation);
        String commandPrefix = getCommandPrefix(operation);

        client.player.sendMessage(Text.literal("==== " + operationName + "域名签名 ====").formatted(Formatting.AQUA), false);
        client.player.sendMessage(Text.literal("请选择要操作的域名："), false);

        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            MutableText areaButton = Text.literal("[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint " + commandPrefix + " select " + quoteCommandArgument(area.getName())))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createAreaHoverText(area)))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(areaButton, false);
        }

        MutableText cancelButton = Text.literal("[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("取消本次操作")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelButton, false);
    }

    public static void showPlayerNamePrompt(SignatureManager.Operation operation, AreaData area) {
        if (client.player == null) return;

        String commandPrefix = getCommandPrefix(operation);
        client.player.sendMessage(Text.literal("已选择域名：" + AreaDataConverter.getDisplayName(area)).formatted(Formatting.GREEN), false);
        client.player.sendMessage(Text.literal("请输入目标玩家名："), false);

        MutableText inputButton = Text.literal("[点击输入玩家名]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/areahint " + commandPrefix + " name "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击后补全命令，在后面输入玩家名")))
                .withColor(Formatting.AQUA));

        MutableText cancelButton = Text.literal(" [取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("取消本次操作")))
                .withColor(Formatting.RED));

        client.player.sendMessage(Text.empty().append(inputButton).append(cancelButton), false);
    }

    public static void showConfirmScreen(SignatureManager.Operation operation, AreaData area, String targetPlayerName) {
        if (client.player == null) return;

        String commandPrefix = getCommandPrefix(operation);
        String operationName = getOperationName(operation);

        client.player.sendMessage(Text.literal("==== 确认" + operationName + "签名 ====").formatted(Formatting.AQUA), false);
        client.player.sendMessage(Text.literal("实际名称：" + nullToText(area.getName())), false);
        client.player.sendMessage(Text.literal("显示名称：" + AreaDataConverter.getDisplayName(area)), false);
        client.player.sendMessage(Text.literal("创建者/旧签名：" + nullToText(area.getSignature())), false);
        client.player.sendMessage(Text.literal("扩展签名：" + formatSignatures(area.getSignatures())), false);
        client.player.sendMessage(Text.literal("目标玩家：" + targetPlayerName), false);
        client.player.sendMessage(Text.literal("操作：" + operationName), false);

        MutableText confirmButton = Text.literal("[确认]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " confirm"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("确认执行")))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = Text.literal(" [取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/areahint " + commandPrefix + " cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("取消本次操作")))
                .withColor(Formatting.RED));

        client.player.sendMessage(Text.empty().append(confirmButton).append(cancelButton), false);
    }

    private static Text createAreaHoverText(AreaData area) {
        return Text.literal(
            "实际名称：" + nullToText(area.getName()) + "\n" +
            "显示名称：" + AreaDataConverter.getDisplayName(area) + "\n" +
            "等级：" + area.getLevel() + "\n" +
            "上级域名：" + nullToText(area.getBaseName()) + "\n" +
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
