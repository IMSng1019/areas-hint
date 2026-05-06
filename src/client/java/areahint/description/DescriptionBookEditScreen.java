package areahint.description;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 使用原版书本编辑界面录入描述。
 */
public final class DescriptionBookEditScreen extends BookEditScreen {
    private final ItemStack descriptionBook;
    private final Screen previousScreen;
    private boolean submitted;

    private DescriptionBookEditScreen(PlayerEntity player, ItemStack descriptionBook, Screen previousScreen) {
        super(player, descriptionBook, Hand.MAIN_HAND);
        this.descriptionBook = descriptionBook;
        this.previousScreen = previousScreen;
    }

    public static void open(String title, String initialDescription) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        ItemStack book = new ItemStack(Items.WRITABLE_BOOK);
        NbtCompound nbt = new NbtCompound();
        NbtList pages = new NbtList();
        for (String page : splitToBookPages(initialDescription)) {
            pages.add(NbtString.of(page));
        }
        nbt.put("pages", pages);
        book.setNbt(nbt);

        client.player.sendMessage(Text.literal("请在打开的原版书本中编辑描述，点击『完成』后将进入确认保存。"), false);
        client.setScreen(new DescriptionBookEditScreen(client.player, book, client.currentScreen));
    }

    @Override
    public void removed() {
        super.removed();
        if (submitted) {
            return;
        }
        Optional<String> description = readDescriptionFromBook(descriptionBook);
        if (description.isPresent()) {
            submitted = true;
            DescriptionManager.getInstance().receiveBookDescription(description.get());
        } else if (DescriptionManager.getInstance().isWaitingForBookInput()) {
            DescriptionManager.getInstance().cancel();
        }
    }

    @Override
    public void close() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.setScreen(previousScreen);
        } else {
            super.close();
        }
    }

    private static Optional<String> readDescriptionFromBook(ItemStack book) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("pages", NbtElement.LIST_TYPE)) {
            return Optional.empty();
        }
        NbtList pages = nbt.getList("pages", NbtElement.STRING_TYPE);
        List<String> cleanedPages = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            String page = pages.getString(i);
            if (!page.trim().isEmpty() || !cleanedPages.isEmpty()) {
                cleanedPages.add(page);
            }
        }
        while (!cleanedPages.isEmpty() && cleanedPages.get(cleanedPages.size() - 1).trim().isEmpty()) {
            cleanedPages.remove(cleanedPages.size() - 1);
        }
        String description = String.join("\n\n", cleanedPages).trim();
        return description.isEmpty() ? Optional.empty() : Optional.of(description);
    }

    private static List<String> splitToBookPages(String description) {
        List<String> pages = new ArrayList<>();
        String clean = description == null ? "" : description;
        if (clean.isEmpty()) {
            pages.add("");
            return pages;
        }
        String[] split = clean.split("\\n\\n", -1);
        for (String page : split) {
            if (page.length() <= DescriptionServerNetworking.MAX_DESCRIPTION_LENGTH) {
                pages.add(page);
                continue;
            }
            int index = 0;
            while (index < page.length()) {
                int end = Math.min(index + DescriptionServerNetworking.MAX_DESCRIPTION_LENGTH, page.length());
                pages.add(page.substring(index, end));
                index = end;
            }
        }
        if (pages.isEmpty()) {
            pages.add("");
        }
        return pages;
    }
}
