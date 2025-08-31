package areahint.command;

import areahint.network.ClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.List;

/**
 * SetHigh客户端命令处理器
 * 处理域名高度设置的客户端交互
 */
public class SetHighClientCommand {
    
    /**
     * 高度输入状态枚举
     */
    public enum AltitudeInputState {
        SELECTING_TYPE,     // 选择高度类型
        INPUT_MAX_HEIGHT,   // 输入最高高度
        INPUT_MIN_HEIGHT    // 输入最低高度
    }
    
    // 当前输入状态
    private static AltitudeInputState currentInputState = AltitudeInputState.SELECTING_TYPE;
    
    // 自定义高度值
    private static Double customMaxHeight = null;
    private static Double customMinHeight = null;
    
    // 当前选中的域名
    private static String currentSelectedArea = null;
    
    // 聊天监听器注册状态
    private static boolean chatListenerRegistered = false;
    
    /**
     * 初始化聊天监听器
     */
    public static void init() {
        if (!chatListenerRegistered) {
            ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
                // 始终处理聊天输入，让 handleChatInput 来决定是否处理
                handleChatInput(message.getString());
            });
            chatListenerRegistered = true;
        }
    }
    
    /**
     * 处理服务器发送的域名列表
     * @param areaNames 域名名称列表
     * @param dimensionType 维度类型
     */
    public static void handleAreaList(List<String> areaNames, String dimensionType) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (areaNames.isEmpty()) {
            client.player.sendMessage(Text.of("§c当前维度没有您可以修改高度的域名"), false);
            return;
        }
        
        // 显示域名列表
        client.player.sendMessage(Text.of("§6===== 可修改高度的域名列表 ====="), false);
        for (int i = 0; i < areaNames.size(); i++) {
            String areaName = areaNames.get(i);
            client.player.sendMessage(Text.of(String.format("§a%d. §f%s", i + 1, areaName)), false);
        }
        client.player.sendMessage(Text.of("§e请在指令中输入域名名称来选择要修改的域名"), false);
        
    }
    
    /**
     * 处理域名选择
     * @param selectedArea 选择的域名
     * @param hasAltitude 是否有现有高度限制
     * @param maxHeight 当前最大高度
     * @param minHeight 当前最小高度
     */
    public static void handleAreaSelection(String selectedArea, boolean hasAltitude, 
                                         Double maxHeight, Double minHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 设置当前选中的域名
        currentSelectedArea = selectedArea;
        
        // 显示当前高度设置
        client.player.sendMessage(Text.of("§6===== 域名高度设置: " + selectedArea + " ====="), false);
        
        if (hasAltitude) {
            String maxStr = maxHeight != null ? String.format("%.1f", maxHeight) : "无限制";
            String minStr = minHeight != null ? String.format("%.1f", minHeight) : "无限制";
            client.player.sendMessage(Text.of("§7当前高度: 最高:" + maxStr + ", 最低:" + minStr), false);
        } else {
            client.player.sendMessage(Text.of("§7当前高度: 无限制"), false);
        }
        
        // 显示高度选择界面，像easyadd一样提供按钮选择
        client.player.sendMessage(Text.of("§6=== 高度设置 ==="), false);
        client.player.sendMessage(Text.of("§a请选择高度设置方式："), false);
        client.player.sendMessage(Text.of("§7域名：" + selectedArea), false);
        client.player.sendMessage(Text.of(""), false);
        
        // 自定义高度按钮
        MutableText customButton = Text.literal("§d[自定义高度]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh custom " + selectedArea))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("自定义高度范围\n依次输入最高和最低高度")))
                .withColor(Formatting.LIGHT_PURPLE));
        
        // 不限制高度按钮
        MutableText unlimitedButton = Text.literal("§e[不限制高度]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh unlimited " + selectedArea))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("不限制高度范围\n域名在所有Y坐标生效")))
                .withColor(Formatting.YELLOW));
        
        // 取消按钮
        MutableText cancelButton = Text.literal("§c[取消本次操作]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Text.of("取消高度设置流程")))
                .withColor(Formatting.RED));
        
        // 组合按钮行
        MutableText buttonRow = Text.empty()
            .append(customButton)
            .append(Text.of("  "))
            .append(unlimitedButton)
            .append(Text.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(Text.of("§7自定义可以精确控制域名的高度边界"), false);
        client.player.sendMessage(Text.of("§7不限制高度适用于跨越多个高度层的大型区域"), false);
        client.player.sendMessage(Text.of("§a域名 " + selectedArea + " 的高度设置流程已启动"), false);
    }
    
    /**
     * 处理自定义高度输入
     * @param areaName 域名名称
     * @param input 用户输入的高度字符串
     */
    public static void handleCustomHeightInput(String areaName, String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 检查取消操作
        if (input.trim().isEmpty() || input.contains("取消")) {
            client.player.sendMessage(Text.of("§c已取消高度输入"), false);
            resetCustomHeightState();
            return;
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            
            // 验证高度范围
            if (value < -64 || value > 320) {
                client.player.sendMessage(Text.of("§c高度值超出合理范围 [-64, 320]"), false);
                return;
            }
            
            // 根据当前状态处理输入
            if (currentInputState == AltitudeInputState.INPUT_MAX_HEIGHT) {
                // 输入最高高度
                customMaxHeight = value;
                client.player.sendMessage(Text.of("§a已设置最高高度：§6" + value), false);
                client.player.sendMessage(Text.of("§a请输入最低高度值："), false);
                client.player.sendMessage(Text.of("§7高度范围: -64 到 320"), false);
                
                
                currentInputState = AltitudeInputState.INPUT_MIN_HEIGHT;
                
            } else if (currentInputState == AltitudeInputState.INPUT_MIN_HEIGHT) {
                // 输入最低高度
                if (customMaxHeight != null && value >= customMaxHeight) {
                    client.player.sendMessage(Text.of("§c错误：最低高度必须小于最高高度 " + customMaxHeight), false);
                    return;
                }
                
                customMinHeight = value;
                client.player.sendMessage(Text.of("§a已设置最低高度：§6" + value), false);
                client.player.sendMessage(Text.of("§a高度范围：§6" + customMaxHeight + " ~ " + customMinHeight), false);
                
                // 创建自定义高度数据并发送请求
                sendHeightRequest(areaName, true, customMaxHeight, customMinHeight);
                
                // 重置状态
                resetCustomHeightState();
            }
            
        } catch (NumberFormatException e) {
            client.player.sendMessage(Text.of("§c输入格式错误，请输入有效的数字"), false);
        }
    }
    
    /**
     * 发送高度设置请求到服务器
     * @param areaName 域名名称
     * @param hasAltitude 是否有高度限制
     * @param maxHeight 最大高度
     * @param minHeight 最小高度
     */
    private static void sendHeightRequest(String areaName, boolean hasAltitude, 
                                        Double maxHeight, Double minHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§a正在发送高度设置请求到服务器..."), false);
        
        // 发送网络请求
        ClientNetworking.sendSetHighRequest(areaName, hasAltitude, maxHeight, minHeight);
    }
    
    /**
     * 处理服务器响应
     * @param success 是否成功
     * @param message 响应消息
     */
    public static void handleServerResponse(boolean success, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (success) {
            client.player.sendMessage(Text.of("§a" + message), false);
        } else {
            client.player.sendMessage(Text.of("§c" + message), false);
        }
    }
    
    /**
     * 重置自定义高度状态
     */
    private static void resetCustomHeightState() {
        currentInputState = AltitudeInputState.SELECTING_TYPE;
        customMaxHeight = null;
        customMinHeight = null;
        currentSelectedArea = null;
    }
    
    /**
     * 开始自定义高度输入流程
     * @param areaName 域名名称
     */
    public static void startCustomHeightInput(String areaName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 重置状态
        resetCustomHeightState();
        
        // 设置当前选中的域名
        currentSelectedArea = areaName;
        
        // 开始输入最高高度
        currentInputState = AltitudeInputState.INPUT_MAX_HEIGHT;
        
        client.player.sendMessage(Text.of("§6===== 设置自定义高度 ====="), false);
        client.player.sendMessage(Text.of("§e请输入最高高度值："), false);
        client.player.sendMessage(Text.of("§7高度范围: -64 到 320"), false);
        
    }
    
    /**
     * 处理聊天输入（由聊天监听器调用）
     * @param input 用户输入的聊天内容
     * @return 是否处理了输入
     */
    public static boolean handleChatInput(String input) {
        if (currentSelectedArea == null && currentInputState == AltitudeInputState.SELECTING_TYPE) {
            return false;
        }
        
        // 移除聊天前缀（参考 EasyAdd 的实现）
        if (input.startsWith("<") && input.contains(">")) {
            int endIndex = input.indexOf(">") + 1;
            if (endIndex < input.length()) {
                input = input.substring(endIndex).trim();
            }
        }
        
        // 检查是否是取消命令
        if (input.trim().equals("/areahint sethigh cancel")) {
            resetCustomHeightState();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§c已取消高度设置流程"), false);
            }
            return true;
        }
        
        // 检查是否是自定义高度命令
        if (input.trim().startsWith("/areahint sethigh custom ")) {
            String areaName = input.trim().substring("/areahint sethigh custom ".length());
            if (currentSelectedArea == null || areaName.equals(currentSelectedArea)) {
                startCustomHeightInput(areaName);
                return true;
            }
        }
        
        // 检查是否是不限制高度命令
        if (input.trim().startsWith("/areahint sethigh unlimited ")) {
            String areaName = input.trim().substring("/areahint sethigh unlimited ".length());
            if (currentSelectedArea == null || areaName.equals(currentSelectedArea)) {
                // 直接发送不限制高度请求
                sendHeightRequest(areaName, false, null, null);
                resetCustomHeightState();
                return true;
            }
        }
        
        // 如果正在输入自定义高度，处理高度数值输入
        if (currentInputState != AltitudeInputState.SELECTING_TYPE && currentSelectedArea != null) {
            handleCustomHeightInput(currentSelectedArea, input);
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取当前输入状态
     */
    public static AltitudeInputState getCurrentInputState() {
        return currentInputState;
    }
    
    /**
     * 检查是否正在输入高度
     */
    public static boolean isInputtingAltitude() {
        return currentInputState != AltitudeInputState.SELECTING_TYPE;
    }
} 