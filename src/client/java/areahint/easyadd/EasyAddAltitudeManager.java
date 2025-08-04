package areahint.easyadd;

import areahint.data.AreaData;
import areahint.debug.ClientDebugManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * EasyAdd高度管理器
 * 负责处理域名高度的自定义设置
 */
public class EasyAddAltitudeManager {
    
    /**
     * 高度选择类型
     */
    public enum AltitudeType {
        AUTOMATIC,  // 自动计算（原逻辑）
        CUSTOM,     // 自定义高度
        UNLIMITED   // 不限制高度
    }
    
    // 高度选择状态
    public enum AltitudeInputState {
        SELECTING_TYPE,     // 选择高度类型
        INPUT_MIN_HEIGHT,   // 输入最低高度
        INPUT_MAX_HEIGHT    // 输入最高高度
    }
    
    // 当前输入状态
    private static AltitudeInputState currentInputState = AltitudeInputState.SELECTING_TYPE;
    
    // 自定义高度值
    private static Double customMinHeight = null;
    private static Double customMaxHeight = null;
    
    // 选择的高度类型
    private static AltitudeType selectedType = AltitudeType.AUTOMATIC;
    
    /**
     * 开始高度选择流程
     * @param recordedPoints 记录的坐标点
     */
    public static void startAltitudeSelection(List<BlockPos> recordedPoints) {
        reset();
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 显示高度选择界面
        EasyAddUI.showAltitudeSelectionScreen(recordedPoints);
        
        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "开始高度选择流程");
    }
    
    /**
     * 处理高度类型选择
     * @param type 选择的高度类型
     */
    public static void handleAltitudeTypeSelection(AltitudeType type) {
        selectedType = type;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (type == AltitudeType.AUTOMATIC) {
            // 选择自动计算，直接进入下一步
            client.player.sendMessage(Text.of("§a已选择自动计算高度范围"), false);
            EasyAddManager.getInstance().proceedWithAltitudeData(null);
        } else if (type == AltitudeType.UNLIMITED) {
            // 选择不限制高度，使用null值表示无高度限制
            client.player.sendMessage(Text.of("§a已选择不限制高度范围"), false);
            client.player.sendMessage(Text.of("§7该域名将在所有Y坐标生效"), false);
            AreaData.AltitudeData unlimitedAltitude = new AreaData.AltitudeData(null, null);
            EasyAddManager.getInstance().proceedWithAltitudeData(unlimitedAltitude);
        } else {
            // 选择自定义，开始输入流程
            client.player.sendMessage(Text.of("§a已选择自定义高度："), false);
            client.player.sendMessage(Text.of("§请输入最低高度值："), false);
            client.player.sendMessage(Text.of("§最低高度-64"), false);
            client.player.sendMessage(Text.of("§c[取消本次操作]"), false);
            
            currentInputState = AltitudeInputState.INPUT_MIN_HEIGHT;
        }
        
        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "选择高度类型: " + type);
    }
    
    /**
     * 处理高度数值输入
     * @param input 用户输入的字符串
     * @return 是否成功处理输入
     */
    public static boolean handleAltitudeInput(String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        
        // 检查取消操作
        if (input.trim().isEmpty() || input.contains("取消")) {
            client.player.sendMessage(Text.of("§c已取消高度输入"), false);
            EasyAddManager.getInstance().cancelEasyAdd();
            return true;
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            
            switch (currentInputState) {
                case INPUT_MIN_HEIGHT:
                    customMinHeight = value;
                    client.player.sendMessage(Text.of("§a已设置最低高度：§6" + value), false);
                    client.player.sendMessage(Text.of("§a请输入最高高度值："), false);
                    client.player.sendMessage(Text.of("§最高高度320"), false);
                    client.player.sendMessage(Text.of("§c[取消]"), false);
                    
                    currentInputState = AltitudeInputState.INPUT_MAX_HEIGHT;
                    return true;
                    
                case INPUT_MAX_HEIGHT:
                    if (customMinHeight != null && value <= customMinHeight) {
                        client.player.sendMessage(Text.of("§c错误：最高高度必须大于最低高度 " + customMinHeight), false);
                        return true;
                    }
                    
                    customMaxHeight = value;
                    client.player.sendMessage(Text.of("§a已设置最高高度：§6" + value), false);
                    client.player.sendMessage(Text.of("§a高度范围：§6" + customMinHeight + " ~ " + customMaxHeight), false);
                    
                    // 创建自定义高度数据并继续流程
                    AreaData.AltitudeData customAltitude = new AreaData.AltitudeData(customMaxHeight, customMinHeight);
                    EasyAddManager.getInstance().proceedWithAltitudeData(customAltitude);
                    return true;
                    
                default:
                    return false;
            }
            
        } catch (NumberFormatException e) {
            client.player.sendMessage(Text.of("§c输入格式错误，请输入有效的数字"), false);
            return true;
        }
    }
    
    /**
     * 获取当前输入状态
     */
    public static AltitudeInputState getCurrentInputState() {
        return currentInputState;
    }
    
    /**
     * 获取选择的高度类型
     */
    public static AltitudeType getSelectedType() {
        return selectedType;
    }
    
    /**
     * 重置所有状态
     */
    public static void reset() {
        currentInputState = AltitudeInputState.SELECTING_TYPE;
        customMinHeight = null;
        customMaxHeight = null;
        selectedType = AltitudeType.AUTOMATIC;
        
        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "重置高度管理器状态");
    }
    
    /**
     * 检查是否正在进行高度输入
     */
    public static boolean isInputtingAltitude() {
        return currentInputState == AltitudeInputState.INPUT_MIN_HEIGHT || 
               currentInputState == AltitudeInputState.INPUT_MAX_HEIGHT;
    }
} 