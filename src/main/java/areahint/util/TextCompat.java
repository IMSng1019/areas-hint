package areahint.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class TextCompat {
    private TextCompat() {
    }

    public static MutableText empty() {
        return new LiteralText("");
    }

    public static MutableText literal(String value) {
        return new LiteralText(FormattingCodeCompat.normalizeFormattingCodes(value));
    }

    public static Text of(String value) {
        return Text.of(FormattingCodeCompat.normalizeFormattingCodes(value));
    }

    public static MutableText translatable(String key, Object... args) {
        return new TranslatableText(key, args);
    }

    static String normalizeFormattingCodes(String value) {
        return FormattingCodeCompat.normalizeFormattingCodes(value);
    }
}
