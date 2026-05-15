package areahint.description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DescriptionKeyHandlerTest {
    @Test
    void consumeCloseDescriptionBookKeyReturnsFalseWhenNothingMatches() {
        assertFalse(DescriptionKeyHandler.consumeCloseDescriptionBookKey(72, 72));
    }
}
