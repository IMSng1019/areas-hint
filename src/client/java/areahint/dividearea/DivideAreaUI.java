package areahint.dividearea;

import areahint.data.AreaData;
import areahint.util.AreaDataConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;

public class DivideAreaUI {
    private final DivideAreaManager manager;
    private final MinecraftClient client;

    public DivideAreaUI(DivideAreaManager manager) {
        this.manager = manager;
        this.client = MinecraftClient.getInstance();
    }

    public void showAreaSelection(List<AreaData> areas) {
        if (client.player == null) return;
        client.player.sendMessage(Text.of("§6=== 分割域名 - 选择域名 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要分割的域名："), false);
        client.player.sendMessage(Text.of(""), false);

        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            MutableText btn = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint dividearea select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("分割 " + displayName + "\n创建者: " + area.getSignature())))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(btn, false);
        }

        client.player.sendMessage(Text.of(""), false);
        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消分割流程")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    public void showRecordingInterface() {
        if (client.player == null) return;
        client.player.sendMessage(Text.of("§a开始记录分割线顶点"), false);
        client.player.sendMessage(Text.of("§e按 §6" + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + " §e键记录当前位置"), false);
        client.player.sendMessage(Text.of("§7至少需要记录2个顶点来定义分割线"), false);

        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消分割流程")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    public void showPointRecordedOptions(int count) {
        if (client.player == null) return;
        client.player.sendMessage(Text.of("§7当前已记录 §6" + count + " §7个坐标点"), false);

        MutableText continueBtn = Text.literal("§a[继续记录]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint dividearea continue"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("继续记录更多坐标点")))
                .withColor(Formatting.GREEN));

        MutableText saveBtn = Text.literal("§b[完成记录]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint dividearea save"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("完成坐标记录，开始分割")))
                .withColor(Formatting.AQUA));

        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消分割流程")))
                .withColor(Formatting.RED));

        if (count >= 2) {
            client.player.sendMessage(Text.empty().append(continueBtn).append(Text.of("  ")).append(saveBtn).append(Text.of("  ")).append(cancelBtn), false);
        } else {
            client.player.sendMessage(Text.empty().append(continueBtn).append(Text.of("  ")).append(cancelBtn), false);
            client.player.sendMessage(Text.of("§7至少需要2个点才能分割"), false);
        }
    }

    public void showCancelMessage() {
        if (client.player == null) return;
        client.player.sendMessage(Text.of("§c已取消域名分割"), false);
    }
}
