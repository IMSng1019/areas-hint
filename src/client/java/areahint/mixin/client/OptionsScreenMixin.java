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
        int buttonWidth = Math.min(150, Math.max(80, this.width - 16));
        int buttonHeight = 20;
        int[] position = findVisibleButtonPosition(buttonWidth, buttonHeight);
        if (position == null) {
            return;
        }

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(I18nManager.translate("screen.areahint.config.title")),
                button -> this.client.setScreen(new AreasHintConfigScreen(this))
        ).dimensions(position[0], position[1], buttonWidth, buttonHeight).build());
    }

    private int[] findVisibleButtonPosition(int width, int height) {
        int rightX = Math.max(8, this.width - width - 8);
        int bottomY = Math.max(8, this.height - 28);
        int[][] preferredPositions = {
                {rightX, 8},
                {8, 8},
                {rightX, bottomY},
                {8, bottomY}
        };

        // 优先放在屏幕边角，避免旧逻辑在低分辨率下找不到空位导致入口消失。
        for (int[] position : preferredPositions) {
            if (!overlapsAnyWidget(position[0], position[1], width, height)) {
                return position;
            }
        }

        return findFreeCenterPosition(width, height);
    }

    private int[] findFreeCenterPosition(int width, int height) {
        int x = this.width / 2 - width / 2;
        int y = this.height / 6 + 168;
        int maxY = this.height - 32 - height;
        while (y <= maxY) {
            if (!overlapsAnyWidget(x, y, width, height)) {
                return new int[]{x, y};
            }
            y += height + 4;
        }
        return null;
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
