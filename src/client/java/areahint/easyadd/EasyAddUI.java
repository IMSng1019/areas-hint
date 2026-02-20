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
     * 显示联合域名输入界面
     */
    public static void showSurfaceNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§6=== 联合域名设置 ==="), false);
        client.player.sendMessage(Text.of("§a请输入联合域名："), false);
        client.player.sendMessage(Text.of("§7联合域名是显示给玩家看的名称，可以与实际域名不同"), false);
        client.player.sendMessage(Text.of("§7留空则使用实际域名作为显示名称"), false);
        client.player.sendMessage(Text.of("§e提示：两个不同的实际域名可以使用相同的联合域名"), false);
        
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
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            MutableText areaButton = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/areahint easyadd base \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        Text.of("选择 " + displayName + " 作为上级域名")))
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
        if (areaData.getSurfacename() != null && !areaData.getSurfacename().trim().isEmpty()) {
            client.player.sendMessage(Text.of("§a联合域名：§6" + areaData.getSurfacename()), false);
        }
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
        
        // 显示颜色信息（新增）
        String colorHex = areaData.getColor();
        String colorDisplay = colorHex != null ? colorHex : "#FFFFFF";
        client.player.sendMessage(Text.of("§a域名颜色：§6" + colorDisplay), false);
        
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
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
                .withColor(Formatting.RED));
        
        MutableText buttonRow = Text.empty()
            .append(saveButton)
            .append(Text.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(Text.of(""), false);
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7请确认以上信息无误后点击保存"), false);
    }
    
    /**
     * 显示高度选择界面
     */
    public static void showAltitudeSelectionScreen(List<BlockPos> recordedPoints) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 计算当前坐标点的高度范围用于显示
        int minY = recordedPoints.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int maxY = recordedPoints.stream().mapToInt(BlockPos::getY).max().orElse(0);
        
        client.player.sendMessage(Text.of("§6=== 高度设置 ==="), false);
        client.player.sendMessage(Text.of("§a请选择高度设置方式："), false);
        client.player.sendMessage(Text.of("§7当前记录点高度范围：" + minY + " ~ " + maxY), false);
        client.player.sendMessage(Text.of(""), false);
        
        // 自动计算按钮
        MutableText autoButton = Text.literal("§b[智能高度]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd altitude auto"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("自动计算高度范围\n基于记录点高度±扩展值")))
                .withColor(Formatting.AQUA));
        
        // 自定义按钮
        MutableText customButton = Text.literal("§d[自定义高度]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd altitude custom"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("自定义高度范围\n手动输入最低和最高高度")))
                .withColor(Formatting.LIGHT_PURPLE));
        
        // 不限制高度按钮
        MutableText unlimitedButton = Text.literal("§e[不限制高度]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd altitude unlimited"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("不限制高度范围\n域名在所有Y坐标生效")))
                .withColor(Formatting.YELLOW));
        
        // 取消按钮
        MutableText cancelButton = Text.literal("§c[取消本次操作]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
                .withColor(Formatting.RED));
        
        // 组合按钮行
        MutableText buttonRow = Text.empty()
            .append(autoButton)
            .append(Text.of("  "))
            .append(customButton)
            .append(Text.of("  "))
            .append(unlimitedButton)
            .append(Text.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7自动计算会基于记录点扩展高度范围"), false);
        client.player.sendMessage(Text.of("§7自定义可以精确控制域名的高度边界"), false);
    }
    
    /**
     * 显示颜色选择界面
     */
    public static void showColorSelectionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§6=== 域名颜色设置 ==="), false);
        client.player.sendMessage(Text.of("§a请选择域名颜色："), false);
        client.player.sendMessage(Text.of("§7颜色将用于域名显示时的视觉效果"), false);
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
            .append(createColorButton("红色", "#FF0000", "§c"))  
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
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消EasyAdd流程")))
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
     * 创建颜色选择按钮
     */
    private static MutableText createColorButton(String colorName, String colorValue, String minecraftColor) {
        String command = colorValue.equals("custom") ? 
            "/areahint easyadd color custom" : 
            "/areahint easyadd color " + colorValue;
        
        return Text.literal(minecraftColor + "[" + colorName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("选择 " + colorName + " 作为域名颜色"))));
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