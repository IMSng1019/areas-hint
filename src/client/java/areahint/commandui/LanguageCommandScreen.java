package areahint.commandui;

import areahint.i18n.I18nManager;
import areahint.language.LanguageManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 图形化语言选择面板，选择和取消仍派发原 language 子命令。
 */
public class LanguageCommandScreen extends CommandUiScreen {
    private LanguageListWidget list;

    public LanguageCommandScreen(Screen parent) {
        super("commandui.language.title", parent);
    }

    @Override
    protected void init() {
        LanguageManager.getInstance().startVisualLanguageSelection();
        this.list = new LanguageListWidget(this.client, this.width, this.height, 54, this.height - 32);
        this.addDrawableChild(this.list);
        for (String language : getLanguages()) {
            this.list.addLanguage(language);
        }

        int y = this.height - FOOTER_Y_OFFSET;
        int buttonWidth = 90;
        int totalWidth = buttonWidth * 3 + 8;
        int x = (this.width - totalWidth) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.cancel")),
            button -> CommandUiActions.runCommandAndClose(this, "areahint language cancel"))
            .dimensions(x, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.back")), button -> close())
            .dimensions(x + buttonWidth + 4, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.close")), button -> closeToGameFromBoundKey())
            .dimensions(x + (buttonWidth + 4) * 2, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    @Override
    protected void onDiscard() {
        LanguageManager.getInstance().discardVisualLanguageSelection();
    }

    @Override
    public void close() {
        onDiscard();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        String current = I18nManager.getCurrentLanguage();
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal(t("commandui.language.current") + I18nManager.getLanguageDisplayName(current) + " (" + current + ")"),
            this.width / 2, 34, 0xFFFFFF);
    }

    private List<String> getLanguages() {
        List<String> languages = new ArrayList<>(I18nManager.getAvailableLanguages());
        Collections.sort(languages);
        return languages;
    }

    private class LanguageListWidget extends ElementListWidget<LanguageListWidget.Entry> {
        LanguageListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
            super(client, width, bottom - top, top, 24);
            this.setRenderBackground(false);
        }

        void addLanguage(String languageCode) {
            this.addEntry(new Entry(languageCode));
        }

        @Override
        public int getRowWidth() {
            return Math.min(420, LanguageCommandScreen.this.width - 36);
        }

        private class Entry extends ElementListWidget.Entry<Entry> {
            private final String languageCode;

            private Entry(String languageCode) {
                this.languageCode = languageCode;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                               int mouseX, int mouseY, boolean hovered, float tickDelta) {
                boolean current = languageCode.equals(I18nManager.getCurrentLanguage());
                String prefix = current ? "✓ " : "  ";
                int color = current ? 0x55FF55 : (hovered ? 0xFFFFAA : 0xFFFFFF);
                String display = prefix + I18nManager.getLanguageDisplayName(languageCode) + " (" + languageCode + ")";
                LanguageCommandScreen.this.drawTrimmed(context, Text.literal(display), x + 4, y + 7, entryWidth - 8, color);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    CommandUiActions.runCommandAndClose(LanguageCommandScreen.this,
                        "areahint language select " + languageCode);
                    return true;
                }
                return false;
            }

            @Override
            public List<? extends Element> children() {
                return List.of();
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return List.of();
            }
        }
    }
}
