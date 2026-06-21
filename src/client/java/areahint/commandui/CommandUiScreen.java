package areahint.commandui;

import areahint.i18n.I18nManager;
import areahint.keyhandler.UnifiedKeyHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * 指令可视化界面基类，统一返回、关闭和绑定键关闭行为。
 */
public abstract class CommandUiScreen extends Screen {
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int FOOTER_Y_OFFSET = 26;
    protected final Screen parent;

    protected CommandUiScreen(String titleKey, Screen parent) {
        super(Text.literal(t(titleKey)));
        this.parent = parent;
    }

    public static boolean closeIfCommandUiOpen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen instanceof CommandUiScreen screen) {
            screen.closeToGameFromBoundKey();
            return true;
        }
        return false;
    }

    protected static String t(String key) {
        return I18nManager.translate(key);
    }

    protected void onDiscard() {
        // 子界面可在这里丢弃自己的临时流程状态。
    }

    protected void closeToGameFromBoundKey() {
        onDiscard();
        UnifiedKeyHandler.suppressRecordKeyUntilRelease();
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (UnifiedKeyHandler.matchesRecordKey(keyCode, scanCode)) {
            if (UnifiedKeyHandler.isRecordKeySuppressedUntilRelease()) {
                return true;
            }
            closeToGameFromBoundKey();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (UnifiedKeyHandler.releaseSuppressedRecordKeyIfMatches(keyCode, scanCode)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    protected void drawTrimmed(DrawContext context, Text text, int x, int y, int maxWidth, int color) {
        String value = text.getString();
        String trimmed = this.textRenderer.trimToWidth(value, Math.max(0, maxWidth));
        if (!trimmed.equals(value) && maxWidth > this.textRenderer.getWidth("...")) {
            trimmed = this.textRenderer.trimToWidth(value, maxWidth - this.textRenderer.getWidth("...")) + "...";
        }
        context.drawTextWithShadow(this.textRenderer, Text.literal(trimmed), x, y, color);
    }
}
