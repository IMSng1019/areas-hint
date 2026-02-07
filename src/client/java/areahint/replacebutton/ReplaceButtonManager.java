package areahint.replacebutton;

import areahint.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * ReplaceButton管理器
 * 管理按键更换流程的状态和逻辑
 */
public class ReplaceButtonManager {

    // 单例实例
    private static ReplaceButtonManager instance;

    // 状态枚举
    public enum ReplaceButtonState {
        IDLE,              // 空闲状态
        WAITING_FOR_KEY,   // 等待玩家按键
        CONFIRMING         // 等待确认
    }

    // 当前状态
    private ReplaceButtonState currentState = ReplaceButtonState.IDLE;

    // 临时存储的新按键代码
    private int pendingKeyCode = -1;

    // 临时存储的新按键名称
    private String pendingKeyName = "";

    /**
     * 私有构造方法（单例模式）
     */
    private ReplaceButtonManager() {
    }

    /**
     * 获取单例实例
     */
    public static ReplaceButtonManager getInstance() {
        if (instance == null) {
            instance = new ReplaceButtonManager();
        }
        return instance;
    }

    /**
     * 开始按键更换流程
     */
    public void startReplaceButton() {
        currentState = ReplaceButtonState.WAITING_FOR_KEY;
        ReplaceButtonUI.showWaitingForKeyScreen();
    }

    /**
     * 处理玩家按下的键
     * @param keyCode GLFW键码
     * @param keyName 按键名称
     */
    public void handleKeyPress(int keyCode, String keyName) {
        if (currentState != ReplaceButtonState.WAITING_FOR_KEY) {
            return;
        }

        // 忽略某些特殊键
        if (isInvalidKey(keyCode)) {
            ReplaceButtonUI.showError("该按键不能用作记录键！");
            return;
        }

        // 保存临时按键信息
        pendingKeyCode = keyCode;
        pendingKeyName = keyName;

        // 切换到确认状态
        currentState = ReplaceButtonState.CONFIRMING;
        ReplaceButtonUI.showConfirmScreen(keyName);
    }

    /**
     * 确认使用新按键
     */
    public void confirmNewKey() {
        if (currentState != ReplaceButtonState.CONFIRMING) {
            return;
        }

        // 保存新按键到配置
        ClientConfig.setRecordKey(pendingKeyCode);

        // 通知 UnifiedKeyHandler 重新注册按键
        areahint.keyhandler.UnifiedKeyHandler.reregisterKey();

        // 显示成功消息
        ReplaceButtonUI.showSuccess("记录按键已更改为：" + pendingKeyName);

        // 重置状态
        reset();
    }

    /**
     * 取消按键更换
     */
    public void cancel() {
        ReplaceButtonUI.showInfo("已取消按键更换");
        reset();
    }

    /**
     * 重置状态
     */
    public void reset() {
        currentState = ReplaceButtonState.IDLE;
        pendingKeyCode = -1;
        pendingKeyName = "";
    }

    /**
     * 获取当前状态
     */
    public ReplaceButtonState getCurrentState() {
        return currentState;
    }

    /**
     * 检查是否处于活跃状态
     */
    public boolean isActive() {
        return currentState != ReplaceButtonState.IDLE;
    }

    /**
     * 检查按键是否无效（不能用作记录键）
     */
    private boolean isInvalidKey(int keyCode) {
        // 不允许使用的按键
        return keyCode == GLFW.GLFW_KEY_ESCAPE ||      // ESC键
               keyCode == GLFW.GLFW_KEY_ENTER ||       // 回车键
               keyCode == GLFW.GLFW_KEY_TAB ||         // Tab键
               keyCode == GLFW.GLFW_KEY_LEFT_SHIFT ||  // Shift键
               keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT ||
               keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || // Ctrl键
               keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL ||
               keyCode == GLFW.GLFW_KEY_LEFT_ALT ||    // Alt键
               keyCode == GLFW.GLFW_KEY_RIGHT_ALT ||
               keyCode == GLFW.GLFW_KEY_UNKNOWN;       // 未知键
    }
}
