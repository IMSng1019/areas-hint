package areahint.keyhandler;

import areahint.config.ClientConfig;
import areahint.easyadd.EasyAddManager;
import areahint.expandarea.ExpandAreaManager;
import areahint.shrinkarea.ShrinkAreaManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class UnifiedKeyHandler {
    private static final KeyBinding.Category GENERAL_CATEGORY = new KeyBinding.Category(
        Identifier.of("areas-hint", "general")
    );

    private static KeyBinding recordKeyBinding;
    private static boolean tickEventRegistered = false;

    public static void register() {
        int keyCode = ClientConfig.getRecordKey();

        recordKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areashint.unified.record",
            InputUtil.Type.KEYSYM,
            keyCode,
            GENERAL_CATEGORY
        ));

        if (!tickEventRegistered) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (recordKeyBinding != null && recordKeyBinding.wasPressed()) {
                    handleRecordKeyPress();
                }
            });
            tickEventRegistered = true;
        }
    }

    public static void reregisterKey() {
        if (recordKeyBinding == null) {
            System.out.println("DEBUG: recordKeyBinding is null, cannot reregister");
            return;
        }

        int keyCode = ClientConfig.getRecordKey();
        recordKeyBinding.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            KeyBinding.updateKeysByCode();
            client.options.write();
        }

        System.out.println("DEBUG: record key updated to code " + keyCode + ", name=" + recordKeyBinding.getBoundKeyLocalizedText().getString());
    }

    private static void handleRecordKeyPress() {
        System.out.println("DEBUG: record key pressed");

        EasyAddManager easyAddManager = EasyAddManager.getInstance();
        if (easyAddManager.getCurrentState() == EasyAddManager.EasyAddState.RECORDING_POINTS) {
            System.out.println("DEBUG: EasyAdd handles record key");
            easyAddManager.recordCurrentPosition();
            return;
        }

        ExpandAreaManager expandAreaManager = ExpandAreaManager.getInstance();
        System.out.println("DEBUG: ExpandArea - isActive: " + expandAreaManager.isActive() + ", isRecording: " + expandAreaManager.isRecording());
        if (expandAreaManager.isActive() && expandAreaManager.isRecording()) {
            System.out.println("DEBUG: ExpandArea handles record key");
            expandAreaManager.recordCurrentPosition();
            return;
        }

        ShrinkAreaManager shrinkAreaManager = ShrinkAreaManager.getInstance();
        if (shrinkAreaManager.isActive() && shrinkAreaManager.isRecording()) {
            System.out.println("DEBUG: ShrinkArea handles record key");
            shrinkAreaManager.handleXKeyPress();
            return;
        }

        areahint.dividearea.DivideAreaManager divideAreaManager = areahint.dividearea.DivideAreaManager.getInstance();
        if (divideAreaManager.isActive() && divideAreaManager.isRecording()) {
            System.out.println("DEBUG: DivideArea handles record key");
            divideAreaManager.recordCurrentPosition();
            return;
        }

        areahint.addhint.AddHintManager addHintManager = areahint.addhint.AddHintManager.getInstance();
        if (addHintManager.isActive() && addHintManager.isRecording()) {
            addHintManager.recordCurrentPosition();
            return;
        }

        System.out.println("DEBUG: no module handled record key");
    }

    public static KeyBinding getRecordKeyBinding() {
        return recordKeyBinding;
    }

    public static String getRecordKeyDisplayName() {
        if (recordKeyBinding != null) {
            return recordKeyBinding.getBoundKeyLocalizedText().getString();
        }
        return "X";
    }
}
