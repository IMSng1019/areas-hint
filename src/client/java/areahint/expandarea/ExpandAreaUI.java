package areahint.expandarea;

import areahint.data.AreaData;
import areahint.util.SurfaceNameHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.ArrayList;

public class ExpandAreaUI {
    private final ExpandAreaManager manager;
    private final MinecraftClient client;
    private AreaNameInputScreen inputScreen;
    private AreaSelectionScreen selectionScreen;
    private RecordingOverlay recordingOverlay;
    
    public ExpandAreaUI(ExpandAreaManager manager) {
        this.manager = manager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 显示域名名称输入界面
     */
    public void showAreaNameInput() {
        if (inputScreen == null) {
            inputScreen = new AreaNameInputScreen();
        }
        client.setScreen(inputScreen);
    }
    
    /**
     * 显示域名选择界面
     */
    public void showAreaSelection(List<AreaData> areas) {
        selectionScreen = new AreaSelectionScreen(areas);
        client.setScreen(selectionScreen);
    }
    
    /**
     * 显示记录界面
     */
    public void showRecordingInterface() {
        if (recordingOverlay == null) {
            recordingOverlay = new RecordingOverlay();
        }
        // 记录界面是一个覆盖层，不设置为主屏幕
    }
    
    /**
     * 更新记录界面
     */
    public void updateRecordingInterface(int vertexCount) {
        if (recordingOverlay != null) {
            recordingOverlay.updateVertexCount(vertexCount);
        }
    }
    
    /**
     * 显示坐标点记录后的选项界面
     */
    public void showPointRecordedOptions(int vertexCount) {
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§7当前已记录 §6" + vertexCount + " §7个坐标点"), false);
        
        // 显示操作选项
        net.minecraft.text.MutableText continueButton = Text.literal("§a[继续记录]")
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint expandarea continue"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.of("继续记录更多坐标点")))
                .withColor(net.minecraft.util.Formatting.GREEN));
        
        net.minecraft.text.MutableText saveButton = Text.literal("§b[保存域名]")
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint expandarea save"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.of("保存扩展后的域名")))
                .withColor(net.minecraft.util.Formatting.AQUA));
        
        if (vertexCount >= 3) {
            // 有足够的点，显示保存选项
            net.minecraft.text.MutableText buttonRow = Text.empty()
                .append(continueButton)
                .append(Text.of("  "))
                .append(saveButton);
            
            client.player.sendMessage(buttonRow, false);
        } else {
            // 点数不够，只显示继续
            client.player.sendMessage(continueButton, false);
            client.player.sendMessage(Text.of("§7至少需要3个点才能保存域名"), false);
        }
    }
    
    /**
     * 隐藏所有界面
     */
    public void hide() {
        if (client.currentScreen == inputScreen || client.currentScreen == selectionScreen) {
            client.setScreen(null);
        }
        recordingOverlay = null;
    }
    
    /**
     * 域名名称输入屏幕
     */
    private class AreaNameInputScreen extends Screen {
        private TextFieldWidget nameField;
        private ButtonWidget confirmButton;
        private ButtonWidget cancelButton;
        
        public AreaNameInputScreen() {
            super(Text.literal("扩展域名 - 输入域名"));
        }
        
        @Override
        protected void init() {
            super.init();
            
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            
            // 域名输入框
            nameField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 40, 200, 20, Text.literal("域名"));
            nameField.setPlaceholder(Text.literal("请输入要扩展的域名").formatted(Formatting.GRAY));
            nameField.setMaxLength(50);
            this.addSelectableChild(nameField);
            this.setInitialFocus(nameField);
            
            // 确认按钮
            confirmButton = ButtonWidget.builder(Text.literal("确认"), button -> {
                String input = nameField.getText().trim();
                if (!input.isEmpty()) {
                    manager.handleAreaNameInput(input);
                }
            }).dimensions(centerX - 50, centerY + 10, 100, 20).build();
            this.addDrawableChild(confirmButton);
            
            // 取消按钮
            cancelButton = ButtonWidget.builder(Text.literal("取消"), button -> {
                this.close();
                manager.reset();
            }).dimensions(centerX - 50, centerY + 35, 100, 20).build();
            this.addDrawableChild(cancelButton);
        }
        
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            
            // 绘制标题
            context.drawCenteredTextWithShadow(this.textRenderer, "扩展域名", this.width / 2, this.height / 2 - 80, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, "请输入要扩展的域名名称", this.width / 2, this.height / 2 - 60, 0xCCCCCC);
            
            // 绘制输入框
            nameField.render(context, mouseX, mouseY, delta);
            
            super.render(context, mouseX, mouseY, delta);
        }
        
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 257) { // Enter key
                String input = nameField.getText().trim();
                if (!input.isEmpty()) {
                    manager.handleAreaNameInput(input);
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        
        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }
        
        @Override
        public void close() {
            super.close();
            manager.reset();
        }
    }
    
    /**
     * 域名选择屏幕
     */
    private class AreaSelectionScreen extends Screen {
        private final List<AreaData> areas;
        private final List<ButtonWidget> areaButtons = new ArrayList<>();
        private int scrollOffset = 0;
        private final int maxVisibleAreas = 10;
        
        public AreaSelectionScreen(List<AreaData> areas) {
            super(Text.literal("选择要扩展的域名"));
            this.areas = areas;
        }
        
        @Override
        protected void init() {
            super.init();
            
            int centerX = this.width / 2;
            int startY = this.height / 2 - (maxVisibleAreas * 25) / 2;
            
            areaButtons.clear();
            
            // 创建域名按钮
            for (int i = 0; i < Math.min(areas.size() - scrollOffset, maxVisibleAreas); i++) {
                int index = i + scrollOffset;
                if (index >= areas.size()) break;
                
                AreaData area = areas.get(index);
                String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
                String signature = area.getSignature();
                
                String buttonText = displayName + " (创建者: " + signature + ")";
                
                ButtonWidget button = ButtonWidget.builder(
                    Text.literal(buttonText),
                    btn -> manager.handleAreaSelection(area)
                ).dimensions(centerX - 150, startY + i * 25, 300, 20).build();
                
                this.addDrawableChild(button);
                areaButtons.add(button);
            }
            
            // 滚动按钮
            if (scrollOffset > 0) {
                ButtonWidget upButton = ButtonWidget.builder(
                    Text.literal("↑ 上一页"),
                    btn -> {
                        scrollOffset = Math.max(0, scrollOffset - maxVisibleAreas);
                        this.clearAndInit();
                    }
                ).dimensions(centerX - 60, startY - 30, 120, 20).build();
                this.addDrawableChild(upButton);
            }
            
            if (scrollOffset + maxVisibleAreas < areas.size()) {
                ButtonWidget downButton = ButtonWidget.builder(
                    Text.literal("↓ 下一页"),
                    btn -> {
                        scrollOffset = Math.min(areas.size() - maxVisibleAreas, scrollOffset + maxVisibleAreas);
                        this.clearAndInit();
                    }
                ).dimensions(centerX - 60, startY + maxVisibleAreas * 25 + 10, 120, 20).build();
                this.addDrawableChild(downButton);
            }
            
            // 取消按钮
            ButtonWidget cancelButton = ButtonWidget.builder(
                Text.literal("取消"),
                btn -> {
                    this.close();
                    manager.reset();
                }
            ).dimensions(centerX - 50, startY + maxVisibleAreas * 25 + 40, 100, 20).build();
            this.addDrawableChild(cancelButton);
        }
        
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            
            // 绘制标题
            context.drawCenteredTextWithShadow(this.textRenderer, "选择要扩展的域名", this.width / 2, 50, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, "共 " + areas.size() + " 个可修改的域名", this.width / 2, 70, 0xCCCCCC);
            
            super.render(context, mouseX, mouseY, delta);
        }
        
        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }
        
        @Override
        public void close() {
            super.close();
            manager.reset();
        }
    }
    
    /**
     * 记录覆盖层
     */
    private class RecordingOverlay {
        private int vertexCount = 0;
        
        public void updateVertexCount(int count) {
            this.vertexCount = count;
        }
        
        public void render(DrawContext context, int screenWidth, int screenHeight) {
            if (!manager.isRecording()) {
                return;
            }
            
            // 绘制记录提示
            context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                "§a域名扩展模式 - 已记录 " + vertexCount + " 个顶点",
                10, 10, 0xFFFFFF
            );
            
            context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                "§e按 X 键记录当前位置，完成后按确认键",
                10, 25, 0xFFFFFF
            );
            
            if (vertexCount >= 3) {
                context.drawTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    "§b可以完成记录（至少3个顶点）",
                    10, 40, 0xFFFFFF
                );
            }
        }
    }
    
    /**
     * 在HUD中渲染记录覆盖层
     */
    public void renderInGame(DrawContext context, int screenWidth, int screenHeight) {
        if (recordingOverlay != null) {
            recordingOverlay.render(context, screenWidth, screenHeight);
        }
    }
} 