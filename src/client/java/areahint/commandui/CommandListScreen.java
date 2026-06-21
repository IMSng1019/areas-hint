package areahint.commandui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 命令面板，集中展示全部用户可见指令按钮。
 */
public class CommandListScreen extends CommandUiScreen {
    private CommandListWidget list;

    public CommandListScreen(Screen parent) {
        super("commandui.commands.title", parent);
    }

    @Override
    protected void init() {
        this.list = new CommandListWidget(this.client, this.width, this.height, 32, this.height - 32);
        this.addDrawableChild(this.list);
        for (CommandVisualHandler handler : CommandVisualRegistry.getHandlers()) {
            this.list.addCommand(handler);
        }

        int y = this.height - FOOTER_Y_OFFSET;
        int buttonWidth = 90;
        int x = (this.width - (buttonWidth * 2 + 4)) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.back")), button -> close())
            .dimensions(x, y, buttonWidth, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("commandui.button.close")), button -> closeToGameFromBoundKey())
            .dimensions(x + buttonWidth + 4, y, buttonWidth, BUTTON_HEIGHT).build());
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
            return Math.min(560, CommandListScreen.this.width - 36);
        }

        private class Entry extends ElementListWidget.Entry<Entry> {
            private static final int BUTTON_WIDTH = 150;
            private final CommandVisualHandler handler;
            private final ButtonWidget commandButton;

            private Entry(CommandVisualHandler handler) {
                this.handler = handler;
                this.commandButton = ButtonWidget.builder(handler.displayName(), button -> handler.open(CommandListScreen.this))
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                               int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int buttonWidth = Math.min(BUTTON_WIDTH, Math.max(80, entryWidth / 2));
                this.commandButton.setWidth(buttonWidth);
                this.commandButton.setPosition(x + 4, y + 6);
                this.commandButton.render(context, mouseX, mouseY, tickDelta);

                int markerColor = handler.hasVisualFlow() ? 0x55FF55 : 0xAAAAAA;
                String marker = handler.hasVisualFlow() ? t("commandui.marker.visual") : t("commandui.marker.command");
                int detailX = x + buttonWidth + 12;
                context.drawTextWithShadow(CommandListScreen.this.textRenderer, Text.literal(marker), detailX, y + 5, markerColor);
                Text detail = this.commandButton.isMouseOver(mouseX, mouseY) || hovered
                    ? handler.description()
                    : Text.literal(displayCommand(handler));
                CommandListScreen.this.drawTrimmed(context, detail, detailX, y + 18,
                    entryWidth - buttonWidth - 16, this.commandButton.isMouseOver(mouseX, mouseY) ? 0xFFFFAA : 0xAAAAAA);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return this.commandButton.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return this.commandButton.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public List<? extends Element> children() {
                return List.of(this.commandButton);
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return List.of(this.commandButton);
            }

            private String displayCommand(CommandVisualHandler handler) {
                String command = handler.defaultCommand();
                return command == null || command.isBlank() ? t("commandui.command.settings.name") : "/" + command;
            }
        }
    }
}
