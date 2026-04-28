package areahint.description;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用原版书本界面展示域名描述。
 */
public final class BookDescriptionScreenUtil {
    private static final int PAGE_CHARACTER_LIMIT = 240;

    private BookDescriptionScreenUtil() {
    }

    public static void openDescriptionBook(String title, String author, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound nbt = new NbtCompound();
        nbt.putString("title", sanitizeBookField(title, "域名描述"));
        nbt.putString("author", sanitizeBookField(author, "Areas Hint"));

        NbtList pages = new NbtList();
        for (String page : paginate(description == null || description.isEmpty() ? "对应域名暂无描述" : description)) {
            pages.add(NbtString.of(Text.Serialization.toJsonString(Text.literal(page))));
        }
        nbt.put("pages", pages);
        book.setNbt(nbt);

        client.setScreen(new BookScreen(BookScreen.Contents.create(book)));
    }

    private static String sanitizeBookField(String value, String fallback) {
        String clean = value == null || value.trim().isEmpty() ? fallback : value.trim();
        return clean.length() > 32 ? clean.substring(0, 32) : clean;
    }

    private static List<String> paginate(String text) {
        List<String> pages = new ArrayList<>();
        String[] lines = text.split("\\R", -1);
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            if (line.length() > PAGE_CHARACTER_LIMIT) {
                if (!current.isEmpty()) {
                    pages.add(current.toString());
                    current.setLength(0);
                }
                int index = 0;
                while (index < line.length()) {
                    int end = Math.min(index + PAGE_CHARACTER_LIMIT, line.length());
                    pages.add(line.substring(index, end));
                    index = end;
                }
                continue;
            }

            int additionalLength = line.length() + (current.isEmpty() ? 0 : 1);
            if (!current.isEmpty() && current.length() + additionalLength > PAGE_CHARACTER_LIMIT) {
                pages.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append('\n');
            }
            current.append(line);
        }

        if (!current.isEmpty() || pages.isEmpty()) {
            pages.add(current.toString());
        }
        return pages;
    }
}
