package areahint.commandui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 通用选项按钮页，用于等级、高度模式、颜色预设等固定选项。
 */
public class WizardOptionScreen extends CommandWizardScreen {
    public record OptionSpec(String labelKey, String detailKey, int swatchColor, Runnable action) {
    }

    private static final int BUTTON_WIDTH = 170;
    private static final int ROW_HEIGHT = 28;
    private static final int SWATCH_SIZE = 10;

    private final String promptKey;
    private final String detailKey;
    private final List<OptionSpec> options;

    public WizardOptionScreen(Screen parent, String titleKey, String promptKey, String detailKey,
                              List<OptionSpec> options, Runnable cancelAction) {
        super(titleKey, parent, cancelAction);
        this.promptKey = promptKey;
        this.detailKey = detailKey;
        this.options = options;
    }

    @Override
    protected void init() {
        int availableHeight = Math.max(ROW_HEIGHT, this.height - 110);
        int maxRows = Math.max(1, availableHeight / ROW_HEIGHT);
        int columns = Math.max(1, (int) Math.ceil(this.options.size() / (double) maxRows));
        if (this.width < 420) {
            columns = 1;
        } else {
            int maxColumns = Math.max(1, (this.width - 40 + 8) / (BUTTON_WIDTH + 8));
            columns = Math.min(columns, maxColumns);
            if (this.options.size() > 3) {
                columns = Math.max(2, columns);
            }
        }
        int rows = (int) Math.ceil(this.options.size() / (double) columns);
        int totalWidth = columns * BUTTON_WIDTH + (columns - 1) * 8;
        int startX = (this.width - totalWidth) / 2;
        int startY = Math.max(58, this.height / 2 - rows * ROW_HEIGHT / 2 - 10);

        for (int i = 0; i < this.options.size(); i++) {
            OptionSpec option = this.options.get(i);
            int column = i % columns;
            int row = i / columns;
            int x = startX + column * (BUTTON_WIDTH + 8);
            int y = startY + row * ROW_HEIGHT;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(t(option.labelKey())), button -> option.action().run())
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        int y = this.height - FOOTER_Y_OFFSET;
        int buttonWidth = 90;
        int x = (this.width - buttonWidth) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.cancel")), button -> cancelAndCloseToGame())
            .dimensions(x, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int contentWidth = Math.min(420, this.width - 40);
        int x = (this.width - contentWidth) / 2;
        int y = 34;
        if (this.promptKey != null && !this.promptKey.isBlank()) {
            drawTrimmed(context, Text.literal(t(this.promptKey)), x, y, contentWidth, 0xFFFFFF);
            y += 14;
        }
        if (this.detailKey != null && !this.detailKey.isBlank()) {
            drawTrimmed(context, Text.literal(t(this.detailKey)), x, y, contentWidth, 0xAAAAAA);
        }

        for (OptionSpec option : this.options) {
            if (option.swatchColor() < 0) {
                continue;
            }
            drawSwatchForOption(context, option);
        }
    }

    private void drawSwatchForOption(DrawContext context, OptionSpec option) {
        for (var child : this.children()) {
            if (child instanceof ButtonWidget button && button.getMessage().getString().equals(t(option.labelKey()))) {
                int swatchX = button.getX() + 6;
                int swatchY = button.getY() + (button.getHeight() - SWATCH_SIZE) / 2;
                context.fill(swatchX, swatchY, swatchX + SWATCH_SIZE, swatchY + SWATCH_SIZE, 0xFF000000 | option.swatchColor());
                context.drawBorder(swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, 0xFFFFFFFF);
                return;
            }
        }
    }
}
