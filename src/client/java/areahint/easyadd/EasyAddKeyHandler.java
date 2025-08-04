package areahint.easyadd;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * EasyAdd按键处理器
 * 负责监听和处理EasyAdd功能的按键事件
 */
public class EasyAddKeyHandler {
    
    // 记录坐标的按键绑定
    private static KeyBinding recordKeyBinding;
    
    /**
     * 注册按键绑定和事件监听器
     */
    public static void register() {
        // 注册按键绑定
        recordKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areahint.easyadd.record", // 翻译键
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X, // 默认绑定X键
            "category.areahint.easyadd" // 按键类别
        ));
        
        // 注册客户端tick事件监听器
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (recordKeyBinding != null) {
                // 检查X键是否被按下
                while (recordKeyBinding.wasPressed()) {
                    onRecordKeyPressed();
                }
            }
        });
    }
    
    /**
     * 处理记录按键按下事件
     */
    private static void onRecordKeyPressed() {
        EasyAddManager manager = EasyAddManager.getInstance();
        
        // 只在记录坐标状态下响应按键
        if (manager.getCurrentState() == EasyAddManager.EasyAddState.RECORDING_POINTS) {
            manager.recordCurrentPosition();
        }
    }
    
    /**
     * 获取记录按键绑定
     */
    public static KeyBinding getRecordKeyBinding() {
        return recordKeyBinding;
    }
    
    /**
     * 获取当前按键的显示名称
     */
    public static String getRecordKeyDisplayName() {
        if (recordKeyBinding != null) {
            return recordKeyBinding.getBoundKeyLocalizedText().getString();
        }
        return "X";
    }
} 