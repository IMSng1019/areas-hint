package areahint.expandarea;

import areahint.i18n.I18nManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExpandAreaKeyHandler {
    private static final KeyBinding.Category EXPAND_AREA_CATEGORY = new KeyBinding.Category(
        Identifier.of("areas-hint", "expandarea")
    );

    private static KeyBinding confirmKey;
    private static boolean wasConfirmPressed = false;

    public static void register() {
        confirmKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areashint.expandarea.confirm",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_ENTER,
            EXPAND_AREA_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            ExpandAreaManager manager = ExpandAreaManager.getInstance();
            if (!manager.isActive()) {
                wasConfirmPressed = confirmKey.isPressed();
                return;
            }

            if (confirmKey.isPressed() && !wasConfirmPressed) {
                if (manager.isRecording()) {
                    manager.finishRecording();
                } else {
                    client.player.sendMessage(
                        Text.literal(I18nManager.translate("expandarea.error.area.record.expand"))
                            .formatted(Formatting.RED),
                        false
                    );
                }
            }

            wasConfirmPressed = confirmKey.isPressed();
        });
    }
}
