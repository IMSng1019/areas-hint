package areahint.commandui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 指令可视化主面板。
 */
public class CommandPanelScreen extends CommandUiScreen {
    private CommandListWidget list;

    public CommandPanelScreen() {
        super("commandui.title", null);
    }

    @Override
    protected void init() {
        this.list = new CommandListWidget(this.client, this.width, this.height, 32, this.height - 32);
        this.addDrawableChild(this.list);
        for (CommandVisualHandler handler : CommandVisualRegistry.getHandlers()) {
            this.list.addCommand(handler);
        }

        int y = this.height - FOOTER_Y_OFFSET;
        int buttonWidth = 78;
        int totalWidth = buttonWidth * 3 + 8;
        int x = (this.width - totalWidth) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.help")),
            button -> MinecraftClient.getInstance().setScreen(new HelpCommandScreen(this)))
            .dimensions(x, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.settings")),
            button -> AreasHintSettingsBridge.open(this))
            .dimensions(x + buttonWidth + 4, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.close")),
            button -> closeToGameFromBoundKey())
            .dimensions(x + (buttonWidth + 4) * 2, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    private class CommandListWidget extends ElementListWidget<CommandListWidget.Entry> {
        CommandListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
            super(client, width, bottom - top, top, 32);
            this.setRenderBackground(false);
        }

        void addCommand(CommandVisualHandler handler) {
            this.addEntry(new Entry(handler));
        }

        @Override
        public int getRowWidth() {
            return Math.min(520, CommandPanelScreen.this.width - 36);
        }

        private class Entry extends ElementListWidget.Entry<Entry> {
            private final CommandVisualHandler handler;

            private Entry(CommandVisualHandler handler) {
                this.handler = handler;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                               int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int markerColor = handler.hasVisualFlow() ? 0x55FF55 : 0xAAAAAA;
                String marker = handler.hasVisualFlow() ? t("commandui.marker.visual") : t("commandui.marker.command");
                context.drawTextWithShadow(CommandPanelScreen.this.textRenderer, Text.literal(marker), x + 4, y + 5, markerColor);
                int textX = x + 64;
                CommandPanelScreen.this.drawTrimmed(context, handler.displayName(), textX, y + 5, entryWidth - 72, 0xFFFFFF);
                Text detail = hovered ? handler.description() : Text.literal(displayCommand(handler));
                CommandPanelScreen.this.drawTrimmed(context, detail, textX, y + 18, entryWidth - 72, hovered ? 0xFFFFAA : 0xAAAAAA);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    handler.open(CommandPanelScreen.this);
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

            private String displayCommand(CommandVisualHandler handler) {
                String command = handler.defaultCommand();
                return command == null || command.isBlank() ? t("commandui.command.settings.name") : "/" + command;
            }
        }
    }
}
