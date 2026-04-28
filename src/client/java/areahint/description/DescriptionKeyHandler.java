package areahint.description;

import areahint.data.AreaData;
import areahint.log.AreaChangeTracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/**
 * 域名描述查询按键。
 */
public final class DescriptionKeyHandler {
    private static KeyBinding queryKeyBinding;
    private static boolean registered;

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
                if (queryKeyBinding != null && queryKeyBinding.wasPressed()) {
                    handleQueryKey();
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
        if (isInteractionActive()) {
            client.player.sendMessage(Text.literal("当前有交互流程正在进行，暂不查询描述").formatted(Formatting.YELLOW), false);
            return;
        }

        String dimensionType = areahint.network.Packets.convertDimensionPathToType(client.world.getRegistryKey().getValue().getPath());
        AreaData area = AreaChangeTracker.getCurrentAreaData();
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
}
