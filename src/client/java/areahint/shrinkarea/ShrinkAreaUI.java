package areahint.shrinkarea;

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

/**
 * ShrinkArea用户界面系统
 * 使用聊天消息和可点击组件实现交互（类似EasyAdd）
 */
public class ShrinkAreaUI {
    private final ShrinkAreaManager manager;
    private final MinecraftClient client;
    
    public ShrinkAreaUI(ShrinkAreaManager manager) {
        this.manager = manager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 显示域名选择界面（使用聊天消息）
     */
    public void showAreaSelectionScreen() {
        if (client.player == null) return;
        
        List<AreaData> areas = manager.getAvailableAreas();
        
        client.player.sendMessage(Text.of("§6=== 收缩域名 - 选择域名 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要收缩的域名："), false);
        
        if (manager.isAdmin()) {
            client.player.sendMessage(Text.of("§7管理员权限：可以收缩所有域名"), false);
        } else {
            client.player.sendMessage(Text.of("§7您只能收缩自己创建的域名"), false);
        }
        
        client.player.sendMessage(Text.of(""), false);
        
        if (areas.isEmpty()) {
            client.player.sendMessage(Text.of("§c没有可以收缩的域名"), false);
            MutableText cancelButton = Text.literal("§c[取消]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea cancel"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消收缩流程")))
                    .withColor(Formatting.RED));
            client.player.sendMessage(cancelButton, false);
            return;
        }
        
        // 显示域名列表按钮
        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            String signature = area.getSignature();
            
            MutableText areaButton = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/areahint shrinkarea select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        Text.of("收缩 " + displayName + "\n创建者: " + signature + "\n等级: " + area.getLevel())))
                    .withColor(Formatting.GOLD));
            
            client.player.sendMessage(areaButton, false);
        }
        
        client.player.sendMessage(Text.of(""), false);
        
        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消收缩流程")))
                .withColor(Formatting.RED));
        
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 显示坐标点记录后的选项界面
     */
    public void showPointRecordedOptions(int vertexCount) {
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§7当前已记录 §6" + vertexCount + " §7个收缩区域顶点"), false);
        
        // 显示操作选项
        MutableText continueButton = Text.literal("§a[继续记录]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea continue"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("继续记录更多收缩顶点")))
                .withColor(Formatting.GREEN));
        
        MutableText saveButton = Text.literal("§b[保存域名]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea save"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("保存收缩后的域名")))
                .withColor(Formatting.AQUA));
        
        if (vertexCount >= 3) {
            // 有足够的点，显示保存选项
            MutableText buttonRow = Text.empty()
                .append(continueButton)
                .append(Text.of("  "))
                .append(saveButton);
            
            client.player.sendMessage(buttonRow, false);
        } else {
            // 点数不够，只显示继续
            client.player.sendMessage(continueButton, false);
            client.player.sendMessage(Text.of("§7至少需要3个点才能保存域名"), false);
        }
    }
    
    /**
     * 显示取消确认消息
     */
    public void showCancelMessage() {
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§c已取消域名收缩"), false);
    }
} 