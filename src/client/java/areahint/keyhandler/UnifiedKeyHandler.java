package areahint.keyhandler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import areahint.easyadd.EasyAddManager;
import areahint.expandarea.ExpandAreaManager;
import areahint.shrinkarea.ShrinkAreaManager;

/**
 * 统一的X键处理器
 * 避免多个模块同时注册X键造成冲突
 * 根据当前激活的模块分发按键事件
 */
public class UnifiedKeyHandler {
    
    // X键绑定
    private static KeyBinding xKeyBinding;
    
    /**
     * 注册统一的X键处理器
     */
    public static void register() {
        // 注册X键绑定
        xKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areahint.unified.record", // 翻译键
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X, // X键
            "category.areahint.general" // 通用类别
        ));
        
        // 注册客户端tick事件监听器
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (xKeyBinding != null && xKeyBinding.wasPressed()) {
                handleXKeyPress();
            }
        });
    }
    
    /**
     * 处理X键按下事件
     * 根据当前激活的模块分发事件
     */
    private static void handleXKeyPress() {
        System.out.println("DEBUG: X键被按下");
        
        // 检查EasyAdd是否活跃且在记录状态
        EasyAddManager easyAddManager = EasyAddManager.getInstance();
        if (easyAddManager.getCurrentState() == EasyAddManager.EasyAddState.RECORDING_POINTS) {
            System.out.println("DEBUG: EasyAdd 处理 X 键");
            easyAddManager.recordCurrentPosition();
            return;
        }
        
        // 检查ExpandArea是否活跃且在记录状态
        ExpandAreaManager expandAreaManager = ExpandAreaManager.getInstance();
        System.out.println("DEBUG: ExpandArea - isActive: " + expandAreaManager.isActive() + ", isRecording: " + expandAreaManager.isRecording());
        if (expandAreaManager.isActive() && expandAreaManager.isRecording()) {
            System.out.println("DEBUG: ExpandArea 处理 X 键");
            expandAreaManager.recordCurrentPosition();
            return;
        }
        
        // 检查ShrinkArea是否活跃且在记录状态
        ShrinkAreaManager shrinkAreaManager = ShrinkAreaManager.getInstance();
        if (shrinkAreaManager.isActive() && shrinkAreaManager.isRecording()) {
            System.out.println("DEBUG: ShrinkArea 处理 X 键");
            shrinkAreaManager.handleXKeyPress();
            return;
        }
        
        System.out.println("DEBUG: 没有模块处理 X 键");
        // 如果没有模块处于记录状态，则忽略按键
        // （避免误操作）
    }
    
    /**
     * 获取X键绑定
     */
    public static KeyBinding getXKeyBinding() {
        return xKeyBinding;
    }
    
    /**
     * 获取X键显示名称
     */
    public static String getXKeyDisplayName() {
        if (xKeyBinding != null) {
            return xKeyBinding.getBoundKeyLocalizedText().getString();
        }
        return "X";
    }
} 