package areahint.replacebutton;

import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;

/**
 * Handles the state machine for replacing the record key.
 */
public class ReplaceButtonManager {
    private static ReplaceButtonManager instance;

    public enum ReplaceButtonState {
        IDLE,
        WAITING_FOR_KEY,
        CONFIRMING
    }

    private ReplaceButtonState currentState = ReplaceButtonState.IDLE;
    private int pendingKeyCode = -1;
    private String pendingKeyName = "";
    private final ReplaceButtonCaptureGate captureGate = new ReplaceButtonCaptureGate();

    private ReplaceButtonManager() {
    }

    public static ReplaceButtonManager getInstance() {
        if (instance == null) {
            instance = new ReplaceButtonManager();
        }
        return instance;
    }

    public void startReplaceButton() {
        currentState = ReplaceButtonState.WAITING_FOR_KEY;
        captureGate.startWaiting();
        ReplaceButtonUI.showWaitingForKeyScreen();
    }

    public void handleKeyPress(int keyCode, String keyName) {
        if (currentState != ReplaceButtonState.WAITING_FOR_KEY) {
            return;
        }

        if (ReplaceButtonKeyRules.isInvalidKey(keyCode)) {
            ReplaceButtonUI.showError(I18nManager.translate("replacebutton.message.record.key_2"));
            captureGate.startWaiting();
            return;
        }

        pendingKeyCode = keyCode;
        pendingKeyName = keyName;
        currentState = ReplaceButtonState.CONFIRMING;
        ReplaceButtonUI.showConfirmScreen(keyName);
    }

    public void confirmNewKey() {
        if (currentState != ReplaceButtonState.CONFIRMING) {
            return;
        }

        ClientConfig.setRecordKey(pendingKeyCode);
        areahint.keyhandler.UnifiedKeyHandler.reregisterKey();
        ReplaceButtonUI.showSuccess(I18nManager.translate("replacebutton.message.record.key") + pendingKeyName);
        reset();
    }

    public void cancel() {
        ReplaceButtonUI.showInfo(I18nManager.translate("replacebutton.message.cancel.key"));
        reset();
    }

    public void reset() {
        currentState = ReplaceButtonState.IDLE;
        pendingKeyCode = -1;
        pendingKeyName = "";
        captureGate.reset();
    }

    public ReplaceButtonState getCurrentState() {
        return currentState;
    }

    public boolean isActive() {
        return currentState != ReplaceButtonState.IDLE;
    }

    public boolean shouldCapture(boolean anyKeyPressed) {
        return currentState == ReplaceButtonState.WAITING_FOR_KEY && captureGate.shouldCapture(anyKeyPressed);
    }
}
