package areahint.description;

import areahint.AreashintClient;
import areahint.data.AreaData;
import areahint.detection.AreaDetector;
import areahint.log.AreaChangeTracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * 域名描述查询按键。
 */
public final class DescriptionKeyHandler {
    private static KeyBinding queryKeyBinding;
    private static boolean registered;
    private static boolean skipNextQueryKeyPress;

    private DescriptionKeyHandler() {
    }

    public static void register() {
        queryKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areahint.description.query",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.areahint.general"
        ));

        if (!registered) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (queryKeyBinding == null) {
                    return;
                }
                if (queryKeyBinding.wasPressed()) {
                    if (skipNextQueryKeyPress) {
                        skipNextQueryKeyPress = false;
                        drainQueryKeyPresses();
                        return;
                    }
                    if (closeDescriptionBookScreen(client.currentScreen)) {
                        skipNextQueryKeyPress = true;
                        drainQueryKeyPresses();
                        return;
                    }
                    handleQueryKey();
                } else if (skipNextQueryKeyPress) {
                    skipNextQueryKeyPress = false;
                }
            });
            registered = true;
        }
    }

    private static void handleQueryKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        if (client.currentScreen != null || isInteractionActive()) {
            return;
        }

        String dimensionType = areahint.network.Packets.convertDimensionPathToType(client.world.getRegistryKey().getValue().getPath());
        AreaData area = AreaChangeTracker.getCurrentAreaData();
        if (area == null) {
            AreaDetector detector = AreashintClient.getAreaDetector();
            if (detector != null) {
                area = detector.findAreaRaw(client.player.getX(), client.player.getY(), client.player.getZ());
            }
        }
        if (area != null && area.getName() != null) {
            DescriptionClientNetworking.sendQuery("area", dimensionType, area.getName());
        } else {
            DescriptionClientNetworking.sendQuery("dimension", dimensionType, client.world.getRegistryKey().getValue().toString());
        }
    }

    private static boolean isInteractionActive() {
        return DescriptionManager.getInstance().isActive()
            || areahint.easyadd.EasyAddManager.getInstance().getCurrentState() != areahint.easyadd.EasyAddManager.EasyAddState.IDLE
            || areahint.expandarea.ExpandAreaManager.getInstance().isActive()
            || areahint.shrinkarea.ShrinkAreaManager.getInstance().isActive()
            || areahint.dividearea.DivideAreaManager.getInstance().isActive()
            || areahint.addhint.AddHintManager.getInstance().isActive()
            || areahint.deletehint.DeleteHintManager.getInstance().isActive()
            || areahint.delete.DeleteManager.getInstance().getCurrentState() != areahint.delete.DeleteManager.DeleteState.IDLE
            || areahint.dimensional.DimensionalNameUIManager.getInstance().getCurrentState() != areahint.dimensional.DimensionalNameUIManager.State.IDLE
            || areahint.rename.RenameManager.getInstance().getCurrentState() != areahint.rename.RenameManager.RenameState.IDLE
            || areahint.recolor.RecolorManager.getInstance().getCurrentState() != areahint.recolor.RecolorManager.RecolorState.IDLE
            || areahint.signature.SignatureManager.getInstance().isActive();
    }

    public static boolean consumeCloseDescriptionBookKey(int keyCode, int scanCode) {
        if (!shouldCloseOnBoundKey(keyCode, scanCode)) {
            return false;
        }
        skipNextQueryKeyPress = true;
        drainQueryKeyPresses();
        return true;
    }

    public static boolean consumeCloseDescriptionBookMouse(int button) {
        if (!shouldCloseOnBoundMouse(button)) {
            return false;
        }
        skipNextQueryKeyPress = true;
        drainQueryKeyPresses();
        return true;
    }

    static boolean shouldCloseOnBoundKey(int keyCode, int scanCode) {
        return shouldCloseOnBoundInput(
            queryKeyBinding != null,
            queryKeyBinding == null || queryKeyBinding.isUnbound(),
            queryKeyBinding != null && queryKeyBinding.matchesKey(keyCode, scanCode)
        );
    }

    static boolean shouldCloseOnBoundMouse(int button) {
        return shouldCloseOnBoundInput(
            queryKeyBinding != null,
            queryKeyBinding == null || queryKeyBinding.isUnbound(),
            queryKeyBinding != null && queryKeyBinding.matchesMouse(button)
        );
    }

    static boolean shouldCloseOnBoundInput(boolean bindingRegistered, boolean bindingUnbound, boolean bindingMatches) {
        return bindingRegistered && !bindingUnbound && bindingMatches;
    }

    private static boolean closeDescriptionBookScreen(Screen screen) {
        if (screen instanceof DescriptionBookEditScreen || BookDescriptionScreenUtil.isDescriptionBookScreen(screen)) {
            screen.close();
            return true;
        }
        return false;
    }

    private static void drainQueryKeyPresses() {
        if (queryKeyBinding == null) {
            return;
        }
        while (queryKeyBinding.wasPressed()) {
            // Drain queued presses so closing the book does not immediately query again.
        }
    }
}
