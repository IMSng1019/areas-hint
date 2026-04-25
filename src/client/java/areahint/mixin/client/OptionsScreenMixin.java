package areahint.mixin.client;

import areahint.gui.AreasHintConfigScreen;
import areahint.i18n.I18nManager;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    private OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void areasHint$addConfigButton(CallbackInfo ci) {
        int buttonWidth = 150;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int y = findFreeY(x, buttonWidth, buttonHeight);
        if (y < 0) {
            return;
        }

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(I18nManager.translate("screen.areahint.config.open")),
                button -> this.client.setScreen(new AreasHintConfigScreen(this))
        ).dimensions(x, y, buttonWidth, buttonHeight).build());
    }

    private int findFreeY(int x, int width, int height) {
        int y = this.height / 6 + 168;
        int maxY = this.height - 32 - height;
        while (y <= maxY) {
            if (!overlapsAnyWidget(x, y, width, height)) {
                return y;
            }
            y += height + 4;
        }
        return -1;
    }

    private boolean overlapsAnyWidget(int x, int y, int width, int height) {
        for (Element child : this.children()) {
            if (child instanceof ClickableWidget widget
                    && rectanglesOverlap(x, y, width, height, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())) {
                return true;
            }
        }
        return false;
    }

    private boolean rectanglesOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
}
