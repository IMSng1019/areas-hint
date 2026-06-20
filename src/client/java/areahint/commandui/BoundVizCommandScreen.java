package areahint.commandui;

import areahint.boundviz.BoundVizManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * 边界可视化图形入口，实际开关仍走原 boundviz 指令。
 */
public class BoundVizCommandScreen extends CommandUiScreen {
    private ButtonWidget toggleButton;

    public BoundVizCommandScreen(Screen parent) {
        super("commandui.boundviz.title", parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.toggleButton = ButtonWidget.builder(toggleText(), button -> {
            CommandUiActions.runCommand("areahint boundviz");
            button.setMessage(toggleText());
        }).dimensions(centerX - 70, 82, 140, BUTTON_HEIGHT).build();
        this.addDrawableChild(this.toggleButton);

        int y = this.height - FOOTER_Y_OFFSET;
        int buttonWidth = 90;
        int x = (this.width - (buttonWidth * 2 + 4)) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.back")), button -> close())
            .dimensions(x, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.close")), button -> closeToGameFromBoundKey())
            .dimensions(x + buttonWidth + 4, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.toggleButton != null) {
            this.toggleButton.setMessage(toggleText());
        }
        String status = BoundVizManager.getInstance().isEnabled()
            ? t("commandui.boundviz.status.on")
            : t("commandui.boundviz.status.off");
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal(t("commandui.boundviz.status") + status), this.width / 2, 56, 0xFFFFFF);
    }

    private Text toggleText() {
        return Text.literal(t("commandui.boundviz.toggle"));
    }
}
