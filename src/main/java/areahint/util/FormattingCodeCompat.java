package areahint.util;

public final class FormattingCodeCompat {
    private static final char BROKEN_SECTION_PREFIX = '\u6402';
    private static final char SECTION_PREFIX = '\u00A7';
    private static final String[][] BROKEN_PREFIX_PAIRS = new String[][]{
        {"鎼?", "§7"},
        {"鎼俛", "§a"},
        {"鎼俠", "§b"},
        {"鎼俢", "§c"},
        {"鎼俤", "§d"},
        {"鎼俥", "§e"},
        {"鎼俧", "§f"}
    };

    private FormattingCodeCompat() {
    }

    public static String normalizeFormattingCodes(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        for (String[] pair : BROKEN_PREFIX_PAIRS) {
            value = value.replace(pair[0], pair[1]);
        }

        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == BROKEN_SECTION_PREFIX && i + 1 < value.length() && isFormattingCode(value.charAt(i + 1))) {
                result.append(SECTION_PREFIX);
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }

    private static boolean isFormattingCode(char value) {
        switch (Character.toLowerCase(value)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'r':
                return true;
            default:
                return false;
        }
    }
}
