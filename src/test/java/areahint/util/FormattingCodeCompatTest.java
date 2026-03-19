package areahint.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormattingCodeCompatTest {
    @Test
    void normalizesBrokenFormattingPrefixes() {
        assertEquals("\u00A7aHello \u00A77World \u00A76!", FormattingCodeCompat.normalizeFormattingCodes("\u6402aHello \u64027World \u64026!"));
    }

    @Test
    void normalizesBrokenFormattingPairs() {
        assertEquals("§fWhite §7Gray §aGreen §cRed", FormattingCodeCompat.normalizeFormattingCodes("鎼俧White 鎼?Gray 鎼俛Green 鎼俢Red"));
    }

    @Test
    void leavesOrdinaryTextUntouched() {
        assertEquals("hello world", FormattingCodeCompat.normalizeFormattingCodes("hello world"));
    }
}
