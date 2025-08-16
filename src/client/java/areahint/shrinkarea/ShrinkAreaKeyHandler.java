package areahint.shrinkarea;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * 收缩域名键盘事件处理器
 * 处理X键用于记录收缩区域顶点
 */
public class ShrinkAreaKeyHandler {
    private static KeyBinding recordKey;
    private static boolean wasRecordPressed = false;
    
    /**
     * 注册按键处理器
     */
    public static void register() {
        // 注册记录位置按键 (X)
        recordKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areashint.shrinkarea.record", 
            InputUtil.Type.KEYSYM, 
            GLFW.GLFW_KEY_X, 
            "category.areashint.shrinkarea"
        ));
        
        // 注册客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            
            ShrinkAreaManager manager = ShrinkAreaManager.getInstance();
            
            // 处理记录按键
            if (recordKey.isPressed() && !wasRecordPressed) {
                manager.handleXKeyPress();
            }
            wasRecordPressed = recordKey.isPressed();
        });
    }
} 