package areahint.replacebutton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplaceButtonKeyRulesTest {
    private static final int KEY_ENTER = 257;
    private static final int KEY_LEFT_SHIFT = 340;
    private static final int KEY_LEFT_SUPER = 343;
    private static final int KEY_RIGHT_SUPER = 347;
    private static final int KEY_X = 88;

    @Test
    void rejectsReservedKeysIncludingSuper() {
        assertTrue(ReplaceButtonKeyRules.isInvalidKey(KEY_ENTER));
        assertTrue(ReplaceButtonKeyRules.isInvalidKey(KEY_LEFT_SHIFT));
        assertTrue(ReplaceButtonKeyRules.isInvalidKey(KEY_LEFT_SUPER));
        assertTrue(ReplaceButtonKeyRules.isInvalidKey(KEY_RIGHT_SUPER));
    }

    @Test
    void allowsRegularKeys() {
        assertFalse(ReplaceButtonKeyRules.isInvalidKey(KEY_X));
    }
}
