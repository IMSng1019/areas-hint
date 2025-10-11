package areahint.expandarea;

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
 * ExpandArea用户界面系统
 * 使用聊天消息和可点击组件实现交互（类似EasyAdd）
 */
public class ExpandAreaUI {
    private final ExpandAreaManager manager;
    private final MinecraftClient client;
    
    public ExpandAreaUI(ExpandAreaManager manager) {
        this.manager = manager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 显示域名选择界面（使用聊天消息）
     */
    public void showAreaSelection(List<AreaData> areas) {
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§6=== 扩展域名 - 选择域名 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要扩展的域名："), false);
        client.player.sendMessage(Text.of(""), false);
        
        if (areas.isEmpty()) {
            client.player.sendMessage(Text.of("§c没有可以扩展的域名"), false);
            MutableText cancelButton = Text.literal("§c[取消]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint expandarea cancel"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消扩展流程")))
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
                        "/areahint expandarea select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        Text.of("扩展 " + displayName + "\n创建者: " + signature)))
                    .withColor(Formatting.GOLD));
            
            client.player.sendMessage(areaButton, false);
        }
        
        client.player.sendMessage(Text.of(""), false);
        
        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint expandarea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消扩展流程")))
                .withColor(Formatting.RED));
        
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 显示记录界面（显示提示消息）
     */
    public void showRecordingInterface() {
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§a开始记录新区域顶点"), false);
        client.player.sendMessage(Text.of("§e按 §6X §e键记录当前位置"), false);
        client.player.sendMessage(Text.of("§7至少需要记录3个顶点"), false);
    }
    
    /**
     * 显示坐标点记录后的选项界面
     */
    public void showPointRecordedOptions(int vertexCount) {
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§7当前已记录 §6" + vertexCount + " §7个坐标点"), false);
        
        // 显示操作选项
        net.minecraft.text.MutableText continueButton = Text.literal("§a[继续记录]")
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint expandarea continue"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.of("继续记录更多坐标点")))
                .withColor(net.minecraft.util.Formatting.GREEN));
        
        net.minecraft.text.MutableText saveButton = Text.literal("§b[保存域名]")
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint expandarea save"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.of("保存扩展后的域名")))
                .withColor(net.minecraft.util.Formatting.AQUA));
        
        if (vertexCount >= 3) {
            // 有足够的点，显示保存选项
            net.minecraft.text.MutableText buttonRow = Text.empty()
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
        
        client.player.sendMessage(Text.of("§c已取消域名扩展"), false);
    }
} 