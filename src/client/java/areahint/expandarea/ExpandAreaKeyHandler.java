package areahint.expandarea;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import areahint.i18n.I18nManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ExpandAreaKeyHandler {
    private static KeyBinding confirmKey;
    private static boolean wasConfirmPressed = false;
    
    /**
     * 娉ㄥ唽鎸夐敭澶勭悊鍣?
     * X閿敱UnifiedKeyHandler缁熶竴澶勭悊锛岃繖閲屽彧澶勭悊Enter閿?
     */
    public static void register() {
        // 娉ㄥ唽纭鎸夐敭 (Enter)
        confirmKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areashint.expandarea.confirm", 
            InputUtil.Type.KEYSYM, 
            GLFW.GLFW_KEY_ENTER, 
            "category.areashint.expandarea"
        ));
        
        // 娉ㄥ唽瀹㈡埛绔痶ick浜嬩欢
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            
            ExpandAreaManager manager = ExpandAreaManager.getInstance();
            
            // 鍙湁鍦‥xpandArea妯″紡娲诲姩鏃舵墠澶勭悊鎸夐敭
            if (!manager.isActive()) {
                wasConfirmPressed = confirmKey.isPressed();
                return;
            }
            
            // 澶勭悊纭鎸夐敭
            if (confirmKey.isPressed() && !wasConfirmPressed) {
                if (manager.isRecording()) {
                    manager.finishRecording();
                } else {
                    client.player.sendMessage(
                        areahint.util.TextCompat.literal(I18nManager.translate("expandarea.error.area.record.expand"))
                            .formatted(Formatting.RED), 
                        false
                    );
                }
            }
            wasConfirmPressed = confirmKey.isPressed();
        });
    }
} 