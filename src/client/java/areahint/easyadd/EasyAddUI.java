package areahint.easyadd;

import areahint.data.AreaData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * EasyAdd用户界面系统
 * 使用聊天消息和可点击组件实现交互
 */
public class EasyAddUI {
    
    /**
     * 显示域名名称输入界面
     */
    public static void showNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§6=== EasyAdd 交互式域名添加 ==="), false);
        client.player.sendMessage(Text.of("§a请在聊天框中输入域名名称："), false);
        client.player.sendMessage(Text.of("§7例如：商业区、住宅区、工业区等"), false);
        
        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
                .withColor(Formatting.RED));
        
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 显示域名等级输入界面
     */
    public static void showLevelInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§a请选择域名等级："), false);
        
        // 创建等级选择按钮
        MutableText level1Button = Text.literal("§b[1-顶级域名]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd level 1"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("选择1级（顶级域名）")))
                .withColor(Formatting.AQUA));
        
        MutableText level2Button = Text.literal("§e[2-二级域名]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd level 2"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("选择2级（二级域名）")))
                .withColor(Formatting.YELLOW));
        
        MutableText level3Button = Text.literal("§d[3-三级域名]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd level 3"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("选择3级（三级域名）")))
                .withColor(Formatting.LIGHT_PURPLE));
        
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
                .withColor(Formatting.RED));
        
        // 组合按钮显示
        MutableText buttonRow = Text.empty()
            .append(level1Button)
            .append(Text.of("  "))
            .append(level2Button)
            .append(Text.of("  "))
            .append(level3Button)
            .append(Text.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7等级说明：1=顶级域名，2/3=次级域名"), false);
    }
    
    /**
     * 显示上级域名选择界面
     */
    public static void showBaseSelectScreen(List<AreaData> availableParentAreas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§a请选择上级域名："), false);
        
        for (AreaData area : availableParentAreas) {
            MutableText areaButton = Text.literal("§6[" + area.getName() + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/areahint easyadd base \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        Text.of("选择 " + area.getName() + " 作为上级域名")))
                    .withColor(Formatting.GOLD));
            
            client.player.sendMessage(areaButton, false);
        }
        
        // 显示取消按钮
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
                .withColor(Formatting.RED));
        
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 显示坐标点记录后的界面
     */
    public static void showPointRecordedScreen(List<BlockPos> recordedPoints, BlockPos lastPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§7当前已记录 §6" + recordedPoints.size() + " §7个坐标点"), false);
        
        // 显示操作选项
        MutableText continueButton = Text.literal("§a[继续记录]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd continue"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("继续记录更多坐标点")))
                .withColor(Formatting.GREEN));
        
        MutableText finishButton = Text.literal("§b[完成记录]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd finish"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("完成坐标记录，进入确认阶段")))
                .withColor(Formatting.AQUA));
        
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
                .withColor(Formatting.RED));
        
        if (recordedPoints.size() >= 3) {
            // 有足够的点，显示完成选项
            MutableText buttonRow = Text.empty()
                .append(continueButton)
                .append(Text.of("  "))
                .append(finishButton)
                .append(Text.of("  "))
                .append(cancelButton);
            
            client.player.sendMessage(buttonRow, false);
        } else {
            // 点数不够，只显示继续和取消
            MutableText buttonRow = Text.empty()
                .append(continueButton)
                .append(Text.of("  "))
                .append(cancelButton);
            
            client.player.sendMessage(buttonRow, false);
            client.player.sendMessage(Text.of("§7至少需要3个点才能完成记录"), false);
        }
    }
    
    /**
     * 显示确认保存界面
     */
    public static void showConfirmSaveScreen(AreaData areaData) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§6=== 域名信息确认 ==="), false);
        client.player.sendMessage(Text.of("§a域名名称：§6" + areaData.getName()), false);
        client.player.sendMessage(Text.of("§a域名等级：§6" + areaData.getLevel()), false);
        
        if (areaData.getBaseName() != null) {
            client.player.sendMessage(Text.of("§a上级域名：§6" + areaData.getBaseName()), false);
        } else {
            client.player.sendMessage(Text.of("§a上级域名：§6无（顶级域名）"), false);
        }
        
        client.player.sendMessage(Text.of("§a记录顶点：§6" + areaData.getVertices().size() + " 个"), false);
        
        if (areaData.getAltitude() != null) {
            client.player.sendMessage(Text.of("§a高度范围：§6" + 
                areaData.getAltitude().getMin() + " ~ " + areaData.getAltitude().getMax()), false);
        }
        
        client.player.sendMessage(Text.of("§a创建者：§6" + areaData.getSignature()), false);
        
        // 显示确认和取消按钮
        MutableText saveButton = Text.literal("§a[保存域名]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd save"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("确认保存该域名")))
                .withColor(Formatting.GREEN));
        
        MutableText cancelButton = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消保存，放弃该域名")))
                .withColor(Formatting.RED));
        
        MutableText buttonRow = Text.empty()
            .append(saveButton)
            .append(Text.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7请确认域名信息无误后选择保存"), false);
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