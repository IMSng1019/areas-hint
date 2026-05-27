package areahint.gui;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.data.ConfigData;
import areahint.i18n.I18nManager;
import areahint.render.VulkanModCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AreasHintConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BOTTOM_BUTTON_WIDTH = 74;
    private static final int LIST_SCROLLBAR_GAP = 10;
    private static final int FREQUENCY_MIN = 1;
    private static final int FREQUENCY_MAX = 20;
    private static final String[] RENDER_MODES = {"CPU", "OpenGL", "Vulkan"};
    private static final String[] STYLE_MODES = {"full", "simple", "mixed"};
    private static final String[] SIZE_MODES = {"extra_large", "large", "medium_large", "medium", "medium_small", "small", "extra_small"};
    private static final String[] SUBTITLE_SIZE_MODES = {"auto", "extra_large", "large", "medium_large", "medium", "medium_small", "small", "extra_small"};
    private static final String[] TELEPORT_FORMATS = {"tp", "minecraft:tp", "teleport", "minecraft:teleport"};

    private final Screen parent;
    private ConfigData draft;
    private ConfigListWidget list;
    private boolean listeningForKey;

    public AreasHintConfigScreen(Screen parent) {
        super(Text.literal(t("screen.areahint.config.title")));
        this.parent = parent;
        this.draft = ClientConfig.copy();
    }

    @Override
    protected void init() {
        this.listeningForKey = false;
        this.list = new ConfigListWidget(this.client, this.width, this.height, 32, this.height - 32);
        this.addDrawableChild(this.list);
        rebuildList();
        addFooterButtons();
    }

    private void rebuildList() {
        this.list.clearConfigEntries();
        this.list.addGroup("screen.areahint.config.group.general");
        this.list.addButton("screen.areahint.config.enabled", onOffButton(draft.isEnabled(), button -> {
            draft.setEnabled(!draft.isEnabled());
            button.setMessage(onOffText(draft.isEnabled()));
        }));
        this.list.addButton("screen.areahint.config.frequency", new FrequencySlider(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT));

        this.list.addGroup("screen.areahint.config.group.title");
        this.list.addButton("screen.areahint.config.hint_render", cycleButton(renderText(draft.getHintRender()), button -> {
            draft.setHintRender(nextRenderMode(draft.getHintRender()));
            button.setMessage(renderText(draft.getHintRender()));
        }));
        this.list.addButton("screen.areahint.config.title_style", cycleButton(styleText(draft.getTitleStyle()), button -> {
            draft.setTitleStyle(nextValue(draft.getTitleStyle(), STYLE_MODES));
            button.setMessage(styleText(draft.getTitleStyle()));
        }));
        this.list.addButton("screen.areahint.config.title_size", cycleButton(sizeText(draft.getTitleSize()), button -> {
            draft.setTitleSize(nextValue(draft.getTitleSize(), SIZE_MODES));
            button.setMessage(sizeText(draft.getTitleSize()));
        }));
        this.list.addButton("screen.areahint.config.subtitle_size", cycleButton(subtitleSizeText(draft.getSubtitleSize()), button -> {
            draft.setSubtitleSize(nextValue(draft.getSubtitleSize(), SUBTITLE_SIZE_MODES));
            button.setMessage(subtitleSizeText(draft.getSubtitleSize()));
        }));

        this.list.addGroup("screen.areahint.config.group.input");
        this.list.addButton("screen.areahint.config.record_key", cycleButton(recordKeyText(), button -> {
            listeningForKey = true;
            button.setMessage(Text.literal(t("screen.areahint.config.record_key.listening")));
        }));

        this.list.addGroup("screen.areahint.config.group.language");
        this.list.addButton("screen.areahint.config.language", cycleButton(languageText(draft.getLanguage()), button -> {
            List<String> languages = getLanguages();
            if (!languages.isEmpty()) {
                String current = draft.getLanguage();
                int index = languages.indexOf(current);
                draft.setLanguage(languages.get((index + 1 + languages.size()) % languages.size()));
                button.setMessage(languageText(draft.getLanguage()));
            }
        }));
        this.list.addButton("screen.areahint.config.language_locked", onOffButton(draft.isLanguageLocked(), button -> {
            draft.setLanguageLocked(!draft.isLanguageLocked());
            button.setMessage(onOffText(draft.isLanguageLocked()));
        }));

        this.list.addGroup("screen.areahint.config.group.advanced");
        this.list.addButton("screen.areahint.config.bound_viz", onOffButton(draft.isBoundVizEnabled(), button -> {
            draft.setBoundVizEnabled(!draft.isBoundVizEnabled());
            button.setMessage(onOffText(draft.isBoundVizEnabled()));
        }));
        this.list.addButton("screen.areahint.config.teleport_format", cycleButton(Text.literal(draft.getTeleportFormat()), button -> {
            draft.setTeleportFormat(nextValue(draft.getTeleportFormat(), TELEPORT_FORMATS));
            button.setMessage(Text.literal(draft.getTeleportFormat()));
        }));
    }

    private void addFooterButtons() {
        int y = this.height - 26;
        int totalWidth = BOTTOM_BUTTON_WIDTH * 4 + 12;
        int x = (this.width - totalWidth) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("screen.areahint.config.reset")), button -> {
            this.draft = new ConfigData();
            this.rebuildList();
        }).dimensions(x, y, BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("screen.areahint.config.cancel")), button -> this.close()).dimensions(x + BOTTOM_BUTTON_WIDTH + 4, y, BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("screen.areahint.config.apply")), button -> applyDraft()).dimensions(x + (BOTTOM_BUTTON_WIDTH + 4) * 2, y, BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(t("screen.areahint.config.done")), button -> {
            applyDraft();
            this.close();
        }).dimensions(x + (BOTTOM_BUTTON_WIDTH + 4) * 3, y, BOTTOM_BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void applyDraft() {
        boolean recordKeyChanged = ClientConfig.getRecordKey() != draft.getRecordKey();
        AreashintClient.applyClientConfig(draft, recordKeyChanged);
        this.draft = ClientConfig.copy();
        this.listeningForKey = false;
        this.clearAndInit();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_UNKNOWN && keyCode > 0) {
                draft.setRecordKey(keyCode);
            }
            listeningForKey = false;
            rebuildList();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private ButtonWidget cycleButton(Text message, ButtonWidget.PressAction action) {
        return ButtonWidget.builder(message, action).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }

    private ButtonWidget onOffButton(boolean value, ButtonWidget.PressAction action) {
        return cycleButton(onOffText(value), action);
    }

    private Text onOffText(boolean value) {
        return Text.literal(value ? t("screen.areahint.config.value.on") : t("screen.areahint.config.value.off"));
    }

    private Text renderText(String value) {
        if ("Vulkan".equals(value) && !VulkanModCompat.isLoaded()) {
            return Text.literal(value + " (" + t("screen.areahint.config.vulkan_unavailable") + ")");
        }
        return Text.literal(value);
    }

    private Text styleText(String value) {
        return Text.literal(t("screen.areahint.config.value.style." + value));
    }

    private Text sizeText(String value) {
        return Text.literal(t("screen.areahint.config.value.size." + value));
    }

    private Text subtitleSizeText(String value) {
        return Text.literal(t("screen.areahint.config.value.subtitle_size." + value));
    }

    private Text languageText(String language) {
        return Text.literal(I18nManager.getLanguageDisplayName(language) + " (" + language + ")");
    }

    private Text recordKeyText() {
        return InputUtil.Type.KEYSYM.createFromCode(draft.getRecordKey()).getLocalizedText();
    }

    private String nextRenderMode(String current) {
        String next = current;
        do {
            next = nextValue(next, RENDER_MODES);
        } while ("Vulkan".equals(next) && !VulkanModCompat.isLoaded());
        return next;
    }

    private String nextValue(String current, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    private List<String> getLanguages() {
        List<String> languages = new ArrayList<>(I18nManager.getAvailableLanguages());
        Collections.sort(languages);
        if (languages.isEmpty()) {
            languages.add(draft.getLanguage());
        }
        return languages;
    }

    private static String t(String key) {
        return I18nManager.translate(key);
    }

    private class FrequencySlider extends SliderWidget {
        FrequencySlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), AreasHintConfigScreen.this.toSliderValue(draft.getFrequency()));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(String.valueOf(getFrequencyValue())));
        }

        @Override
        protected void applyValue() {
            draft.setFrequency(getFrequencyValue());
            updateMessage();
        }

        private int getFrequencyValue() {
            return FREQUENCY_MIN + (int) Math.round(this.value * (FREQUENCY_MAX - FREQUENCY_MIN));
        }

    }

    private double toSliderValue(int frequency) {
        int clamped = Math.max(FREQUENCY_MIN, Math.min(FREQUENCY_MAX, frequency));
        return (clamped - FREQUENCY_MIN) / (double) (FREQUENCY_MAX - FREQUENCY_MIN);
    }

    private class ConfigListWidget extends ElementListWidget<ConfigListWidget.Entry> {
        private ClickableWidget focusedConfigWidget;

        ConfigListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
            super(client, width, bottom - top, top, 24);
            this.setRenderBackground(false);
        }

        void addGroup(String key) {
            this.addEntry(new GroupEntry(key));
        }

        void addButton(String key, ClickableWidget widget) {
            this.addEntry(new ButtonEntry(key, widget));
        }

        void clearConfigEntries() {
            super.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return Math.min(420, AreasHintConfigScreen.this.width - 40);
        }

        @Override
        protected int getScrollbarPositionX() {
            // 原版列表会按默认宽度计算滚动条位置，宽屏下会压到右侧配置按钮中间。
            return Math.min(this.width - 6, this.getRowRight() + LIST_SCROLLBAR_GAP);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ClickableWidget widget = findConfigWidgetAt(mouseX, mouseY);
            if (widget != null && widget.mouseClicked(mouseX, mouseY, button)) {
                // 原版列表的行命中只知道“点中了某一行”，这里再明确把点击交给行内按钮或滑块。
                this.focusedConfigWidget = widget;
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.focusedConfigWidget != null) {
                ClickableWidget widget = this.focusedConfigWidget;
                this.focusedConfigWidget = null;
                if (widget.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }

            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.focusedConfigWidget != null
                    && this.focusedConfigWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }

            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        private ClickableWidget findConfigWidgetAt(double mouseX, double mouseY) {
            for (Entry entry : this.children()) {
                ClickableWidget widget = entry.getConfigWidget();
                if (widget != null && widget.isMouseOver(mouseX, mouseY)) {
                    return widget;
                }
            }

            return null;
        }

        abstract class Entry extends ElementListWidget.Entry<Entry> {
            ClickableWidget getConfigWidget() {
                return null;
            }
        }

        private class GroupEntry extends Entry {
            private final String key;

            private GroupEntry(String key) {
                this.key = key;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawTextWithShadow(AreasHintConfigScreen.this.textRenderer, Text.literal(t(key)), x + 4, y + 7, 0xFFFF55);
            }

            @Override
            public List<? extends net.minecraft.client.gui.Element> children() {
                return List.of();
            }

            @Override
            public List<? extends net.minecraft.client.gui.Selectable> selectableChildren() {
                return List.of();
            }
        }

        private class ButtonEntry extends Entry {
            private final String key;
            private final ClickableWidget widget;

            private ButtonEntry(String key, ClickableWidget widget) {
                this.key = key;
                this.widget = widget;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawTextWithShadow(AreasHintConfigScreen.this.textRenderer, Text.literal(t(key)), x + 4, y + 6, 0xFFFFFF);
                this.widget.setPosition(x + entryWidth - BUTTON_WIDTH - 4, y + 2);
                this.widget.render(context, mouseX, mouseY, tickDelta);
            }

            @Override
            public List<? extends net.minecraft.client.gui.Element> children() {
                return List.of(this.widget);
            }

            @Override
            public List<? extends net.minecraft.client.gui.Selectable> selectableChildren() {
                return List.of(this.widget);
            }

            @Override
            ClickableWidget getConfigWidget() {
                return this.widget;
            }
        }
    }
}
