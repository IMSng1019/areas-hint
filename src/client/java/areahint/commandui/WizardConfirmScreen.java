package areahint.commandui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 通用确认页，用于危险操作、一次性命令和最终提交。
 */
public class WizardConfirmScreen extends CommandWizardScreen {
    private final String prompt;
    private final List<String> details;
    private final String confirmKey;
    private final Runnable confirmAction;

    public WizardConfirmScreen(Screen parent, String titleKey, String prompt, List<String> details,
                               String confirmKey, Runnable confirmAction, Runnable cancelAction) {
        super(titleKey, parent, cancelAction);
        this.prompt = prompt;
        this.details = details == null ? List.of() : details;
        this.confirmKey = confirmKey == null ? "commandui.button.confirm" : confirmKey;
        this.confirmAction = confirmAction;
    }

    @Override
    protected void init() {
        int buttonWidth = 92;
        int totalWidth = buttonWidth * 2 + 8;
        int x = (this.width - totalWidth) / 2;
        int y = this.height - FOOTER_Y_OFFSET;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t(this.confirmKey)), button -> {
            markFlowHandled();
            if (this.client != null) {
                this.client.setScreen(null);
            }
            if (this.confirmAction != null) {
                this.confirmAction.run();
            }
        }).dimensions(x, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.cancel")), button -> cancelAndCloseToGame())
            .dimensions(x + buttonWidth + 8, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int contentWidth = Math.min(430, this.width - 40);
        int x = (this.width - contentWidth) / 2;
        int y = 42;
        if (this.prompt != null && !this.prompt.isBlank()) {
            drawTrimmed(context, Text.literal(this.prompt), x, y, contentWidth, 0xFFFFFF);
            y += 18;
        }
        for (String detail : this.details) {
            drawTrimmed(context, Text.literal(detail), x, y, contentWidth, 0xAAAAAA);
            y += 13;
        }
    }
}
