package areahint.shrinkarea;

import areahint.data.AreaData;
import areahint.util.AreaDataConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 域名收缩用户界面
 * 提供域名选择和操作反馈的图形界面
 */
public class ShrinkAreaUI {
    private final ShrinkAreaManager manager;
    private AreaSelectionScreen areaSelectionScreen;
    
    public ShrinkAreaUI(ShrinkAreaManager manager) {
        this.manager = manager;
    }
    
    /**
     * 显示域名选择界面
     */
    public void showAreaSelectionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        areaSelectionScreen = new AreaSelectionScreen();
        client.setScreen(areaSelectionScreen);
    }
    
    /**
     * 关闭域名选择界面
     */
    public void closeAreaSelectionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == areaSelectionScreen) {
            client.setScreen(null);
        }
        areaSelectionScreen = null;
    }
    
    /**
     * 关闭所有界面
     */
    public void closeAllScreens() {
        closeAreaSelectionScreen();
    }
    
    /**
     * 显示坐标点记录后的选项界面
     */
    public void showPointRecordedOptions(int vertexCount) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(Text.of("§7当前已记录 §6" + vertexCount + " §7个收缩区域顶点"), false);
        
        // 显示操作选项
        net.minecraft.text.MutableText continueButton = Text.literal("§a[继续记录]")
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea continue"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.of("继续记录更多收缩顶点")))
                .withColor(net.minecraft.util.Formatting.GREEN));
        
        net.minecraft.text.MutableText saveButton = Text.literal("§b[保存域名]")
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea save"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.of("保存收缩后的域名")))
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
     * 域名选择界面
     */
    private class AreaSelectionScreen extends Screen {
        private static final int BUTTON_WIDTH = 300;
        private static final int BUTTON_HEIGHT = 20;
        private static final int BUTTON_SPACING = 25;
        
        public AreaSelectionScreen() {
            super(Text.literal("选择要收缩的域名"));
        }
        
        @Override
        protected void init() {
            super.init();
            
            List<AreaData> areas = manager.getAvailableAreas();
            int startY = this.height / 2 - (areas.size() * BUTTON_SPACING) / 2;
            
            // 添加标题
            int titleY = startY - 40;
            
            // 为每个可用域名创建按钮
            for (int i = 0; i < areas.size(); i++) {
                AreaData area = areas.get(i);
                int buttonY = startY + i * BUTTON_SPACING;
                
                String displayName = AreaDataConverter.getDisplayName(area);
                String buttonText = displayName + " (等级: " + area.getLevel() + ")";
                
                ButtonWidget button = ButtonWidget.builder(
                    Text.literal(buttonText),
                    (btn) -> selectArea(area)
                )
                .dimensions(this.width / 2 - BUTTON_WIDTH / 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
                
                this.addDrawableChild(button);
            }
            
            // 添加取消按钮
            ButtonWidget cancelButton = ButtonWidget.builder(
                Text.literal("取消").formatted(Formatting.RED),
                (btn) -> cancelSelection()
            )
            .dimensions(this.width / 2 - BUTTON_WIDTH / 2, startY + areas.size() * BUTTON_SPACING + 10, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
            
            this.addDrawableChild(cancelButton);
        }
        
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            super.render(context, mouseX, mouseY, delta);
            
            // 绘制标题
            Text title = Text.literal("选择要收缩的域名").formatted(Formatting.BOLD, Formatting.YELLOW);
            int titleWidth = this.textRenderer.getWidth(title);
            context.drawText(this.textRenderer, title, this.width / 2 - titleWidth / 2, 20, 0xFFFFFF, true);
            
            // 绘制说明文字
            List<AreaData> areas = manager.getAvailableAreas();
            if (areas.isEmpty()) {
                Text noAreaText = Text.literal("没有可收缩的域名").formatted(Formatting.RED);
                int noAreaWidth = this.textRenderer.getWidth(noAreaText);
                context.drawText(this.textRenderer, noAreaText, this.width / 2 - noAreaWidth / 2, this.height / 2, 0xFFFFFF, false);
            } else {
                String infoText = manager.isAdmin() ? 
                    "管理员权限：可以收缩所有域名" : 
                    "只能收缩您创建的域名（basename为您的用户名）";
                Text info = Text.literal(infoText).formatted(Formatting.GRAY);
                int infoWidth = this.textRenderer.getWidth(info);
                context.drawText(this.textRenderer, info, this.width / 2 - infoWidth / 2, 40, 0xFFFFFF, false);
            }
        }
        
        /**
         * 选择域名
         */
        private void selectArea(AreaData area) {
            manager.selectArea(area);
        }
        
        /**
         * 取消选择
         */
        private void cancelSelection() {
            manager.stop();
        }
        
        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }
        
        @Override
        public void close() {
            manager.stop();
            super.close();
        }
    }
} 