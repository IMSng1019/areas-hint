package areahint.expandarea;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ExpandAreaKeyHandler {
    private static KeyBinding recordKey;
    private static KeyBinding confirmKey;
    private static boolean wasRecordPressed = false;
    private static boolean wasConfirmPressed = false;
    
    /**
     * 注册按键处理器
     */
    public static void register() {
        // 注册记录位置按键 (X)
        recordKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areashint.expandarea.record", 
            InputUtil.Type.KEYSYM, 
            GLFW.GLFW_KEY_X, 
            "category.areashint.expandarea"
        ));
        
        // 注册确认按键 (Enter)
        confirmKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areashint.expandarea.confirm", 
            InputUtil.Type.KEYSYM, 
            GLFW.GLFW_KEY_ENTER, 
            "category.areashint.expandarea"
        ));
        
        // 注册客户端tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            
            ExpandAreaManager manager = ExpandAreaManager.getInstance();
            
            // 只有在ExpandArea模式活动时才处理按键
            if (!manager.isActive()) {
                wasRecordPressed = recordKey.isPressed();
                wasConfirmPressed = confirmKey.isPressed();
                return;
            }
            
            // 处理记录按键
            if (recordKey.isPressed() && !wasRecordPressed) {
                if (manager.isRecording()) {
                    manager.recordCurrentPosition();
                } else {
                    client.player.sendMessage(
                        Text.literal("§c当前不在域名扩展记录模式中")
                            .formatted(Formatting.RED), 
                        false
                    );
                }
            }
            wasRecordPressed = recordKey.isPressed();
            
            // 处理确认按键
            if (confirmKey.isPressed() && !wasConfirmPressed) {
                if (manager.isRecording()) {
                    manager.finishRecording();
                } else {
                    client.player.sendMessage(
                        Text.literal("§c当前不在域名扩展记录模式中")
                            .formatted(Formatting.RED), 
                        false
                    );
                }
            }
            wasConfirmPressed = confirmKey.isPressed();
        });
    }
} 