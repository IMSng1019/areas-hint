package areahint.keyhandler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import areahint.easyadd.EasyAddManager;
import areahint.expandarea.ExpandAreaManager;
import areahint.shrinkarea.ShrinkAreaManager;
import areahint.config.ClientConfig;

/**
 * 统一的记录键处理器
 * 避免多个模块同时注册同一个键造成冲突
 * 根据当前激活的模块分发按键事件
 * 支持动态更改按键
 */
public class UnifiedKeyHandler {

    // 记录键绑定
    private static KeyBinding recordKeyBinding;

    // 是否已注册tick事件
    private static boolean tickEventRegistered = false;

    /**
     * 注册统一的记录键处理器
     */
    public static void register() {
        // 获取配置的按键代码
        int keyCode = ClientConfig.getRecordKey();

        // 注册记录键绑定
        recordKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areahint.unified.record", // 翻译键
            InputUtil.Type.KEYSYM,
            keyCode, // 使用配置的按键
            "category.areahint.general" // 通用类别
        ));

        // 只注册一次tick事件
        if (!tickEventRegistered) {
            // 注册客户端tick事件监听器
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (recordKeyBinding != null && recordKeyBinding.wasPressed()) {
                    handleRecordKeyPress();
                }
            });
            tickEventRegistered = true;
        }
    }

    /**
     * 重新注册按键（当按键配置改变时调用）
     */
    public static void reregisterKey() {
        // 获取新的按键代码
        int keyCode = ClientConfig.getRecordKey();

        // 由于Fabric API没有unregisterKeyBinding方法，我们直接创建新的按键绑定
        // 旧的按键绑定会被自动覆盖
        recordKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.areahint.unified.record", // 翻译键
            InputUtil.Type.KEYSYM,
            keyCode, // 使用新的按键
            "category.areahint.general" // 通用类别
        ));

        System.out.println("DEBUG: 记录键已重新注册为键码 " + keyCode);
    }

    /**
     * 处理记录键按下事件
     * 根据当前激活的模块分发事件
     */
    private static void handleRecordKeyPress() {
        System.out.println("DEBUG: 记录键被按下");

        // 检查EasyAdd是否活跃且在记录状态
        EasyAddManager easyAddManager = EasyAddManager.getInstance();
        if (easyAddManager.getCurrentState() == EasyAddManager.EasyAddState.RECORDING_POINTS) {
            System.out.println("DEBUG: EasyAdd 处理记录键");
            easyAddManager.recordCurrentPosition();
            return;
        }

        // 检查ExpandArea是否活跃且在记录状态
        ExpandAreaManager expandAreaManager = ExpandAreaManager.getInstance();
        System.out.println("DEBUG: ExpandArea - isActive: " + expandAreaManager.isActive() + ", isRecording: " + expandAreaManager.isRecording());
        if (expandAreaManager.isActive() && expandAreaManager.isRecording()) {
            System.out.println("DEBUG: ExpandArea 处理记录键");
            expandAreaManager.recordCurrentPosition();
            return;
        }

        // 检查ShrinkArea是否活跃且在记录状态
        ShrinkAreaManager shrinkAreaManager = ShrinkAreaManager.getInstance();
        if (shrinkAreaManager.isActive() && shrinkAreaManager.isRecording()) {
            System.out.println("DEBUG: ShrinkArea 处理记录键");
            shrinkAreaManager.handleXKeyPress();
            return;
        }

        System.out.println("DEBUG: 没有模块处理记录键");
        // 如果没有模块处于记录状态，则忽略按键
        // （避免误操作）
    }

    /**
     * 获取记录键绑定
     */
    public static KeyBinding getRecordKeyBinding() {
        return recordKeyBinding;
    }

    /**
     * 获取记录键显示名称
     */
    public static String getRecordKeyDisplayName() {
        if (recordKeyBinding != null) {
            return recordKeyBinding.getBoundKeyLocalizedText().getString();
        }
        return "X";
    }
} 