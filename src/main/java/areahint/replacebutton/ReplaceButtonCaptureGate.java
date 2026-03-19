package areahint.replacebutton;

public final class ReplaceButtonCaptureGate {
    private boolean waitingForAllKeysReleased;

    public void startWaiting() {
        waitingForAllKeysReleased = true;
    }

    public boolean shouldCapture(boolean anyKeyPressed) {
        if (!waitingForAllKeysReleased) {
            return true;
        }

        if (anyKeyPressed) {
            return false;
        }

        waitingForAllKeysReleased = false;
        return false;
    }

    public void reset() {
        waitingForAllKeysReleased = false;
    }
}
