package areahint.replacebutton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplaceButtonCaptureGateTest {
    @Test
    void waitsForAllKeysToBeReleasedBeforeCapturing() {
        ReplaceButtonCaptureGate gate = new ReplaceButtonCaptureGate();

        gate.startWaiting();

        assertFalse(gate.shouldCapture(true));
        assertFalse(gate.shouldCapture(false));
        assertTrue(gate.shouldCapture(true));
    }

    @Test
    void resetClearsTheGate() {
        ReplaceButtonCaptureGate gate = new ReplaceButtonCaptureGate();

        gate.startWaiting();
        gate.reset();

        assertTrue(gate.shouldCapture(true));
    }
}
