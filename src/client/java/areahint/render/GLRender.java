package areahint.render;

import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * OpenGL渲染实现类
 * 使用OpenGL进行渲染
 */
public class GLRender implements RenderManager.IRender {
    // Minecraft客户端实例
    private final MinecraftClient client;
    
    // 当前动画状态
    private AnimationState animationState = AnimationState.NONE;
    
    // 当前显示的文本
    private String currentText = null;
    
    // 动画开始时间
    private long animationStartTime = 0;
    
    // 动画持续时间（毫秒）
    private static final long ANIMATION_IN_DURATION = 500; // 进入动画持续时间
    private static final long ANIMATION_STAY_DURATION = 3000; // 显示持续时间
    private static final long ANIMATION_OUT_DURATION = 300; // 退出动画持续时间
    
    /**
     * 构造方法
     * @param client Minecraft客户端实例
     */
    public GLRender(MinecraftClient client) {
        this.client = client;
        
        // 注册Tick事件用于渲染
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }
    
    /**
     * 客户端Tick事件处理，用于更新动画状态
     * @param minecraftClient Minecraft客户端实例
     */
    private void onClientTick(MinecraftClient minecraftClient) {
        if (animationState == AnimationState.NONE || currentText == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;
        
        // 更新动画状态
        if (animationState == AnimationState.IN && elapsedTime >= ANIMATION_IN_DURATION) {
            animationState = AnimationState.STAY;
            animationStartTime = currentTime;
        } else if (animationState == AnimationState.STAY && elapsedTime >= ANIMATION_STAY_DURATION) {
            animationState = AnimationState.OUT;
            animationStartTime = currentTime;
        } else if (animationState == AnimationState.OUT && elapsedTime >= ANIMATION_OUT_DURATION) {
            animationState = AnimationState.NONE;
            currentText = null;
        }
        
        // 如果有活动的动画，渲染到屏幕
        if (animationState != AnimationState.NONE && client.world != null) {
            renderToScreen();
        }
    }
    
    /**
     * 将动画渲染到屏幕
     */
    private void renderToScreen() {
        if (client.currentScreen != null || currentText == null) {
            return;
        }
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // 计算文本在屏幕上的位置
        int x = screenWidth / 2;
        int y = screenHeight / 4; // 屏幕1/4位置
        
        // 根据动画状态计算Y偏移和透明度
        final float[] alpha = {1.0f};
        final int[] yOffset = {0};
        
        long elapsedTime = System.currentTimeMillis() - animationStartTime;
        float progress = 0;
        
        switch (animationState) {
            case IN:
                progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_IN_DURATION);
                progress = easeOutCubic(progress); // 使用缓动函数
                yOffset[0] = (int) ((1.0f - progress) * 50); // 从上方50像素移动到原位
                alpha[0] = progress;
                break;
            case STAY:
                // 保持原位，完全不透明
                break;
            case OUT:
                progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_OUT_DURATION);
                progress = easeInCubic(progress); // 使用缓动函数
                yOffset[0] = (int) (progress * -30); // 向上移动30像素
                alpha[0] = 1.0f - progress;
                break;
            case NONE:
                return; // 不渲染
        }
        
        // 渲染文本
        client.execute(() -> {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.push();
            
            // 将文本居中
            Text text = Text.of(currentText);
            TextRenderer textRenderer = client.textRenderer;
            int textWidth = textRenderer.getWidth(text);
            
            // 计算最终位置
            int finalX = x - textWidth / 2;
            int finalY = y + yOffset[0];
            
            // 绘制带有阴影的文本
            DrawContext drawContext = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
            int color = getAlphaColor(0xFFFFFF, alpha[0]);
            drawContext.drawTextWithShadow(textRenderer, text, finalX, finalY, color);
            
            matrixStack.pop();
        });
    }
    
    /**
     * 将RGB颜色和透明度转换为ARGB颜色值
     * @param rgb RGB颜色值
     * @param alpha 透明度（0.0-1.0）
     * @return ARGB颜色值
     */
    private int getAlphaColor(int rgb, float alpha) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        return a | (rgb & 0x00FFFFFF);
    }
    
    @Override
    public void renderTitle(String title) {
        if (title == null || title.isEmpty()) {
            return;
        }
        
        // 如果已经有动画在播放，检查是否是相同的文本
        if (animationState != AnimationState.NONE) {
            if (title.equals(currentText)) {
                return; // 已经在显示相同的文本
            }
        }
        
        // 设置新的动画状态
        currentText = title;
        animationState = AnimationState.IN;
        animationStartTime = System.currentTimeMillis();
    }
    
    /**
     * 动画状态枚举
     */
    private enum AnimationState {
        NONE, // 无动画
        IN,   // 进入动画
        STAY, // 停留
        OUT   // 退出动画
    }
    
    /**
     * 缓入三次方缓动函数
     * @param x 进度 (0.0 - 1.0)
     * @return 缓动值
     */
    private float easeInCubic(float x) {
        return x * x * x;
    }
    
    /**
     * 缓出三次方缓动函数
     * @param x 进度 (0.0 - 1.0)
     * @return 缓动值
     */
    private float easeOutCubic(float x) {
        return 1 - (float) Math.pow(1 - x, 3);
    }
} 