package areahint.render;

import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CPU渲染实现类
 * 使用CPU进行软件渲染，不使用GPU
 */
public class CPURender implements RenderManager.IRender {
    // Minecraft客户端实例
    private final MinecraftClient client;
    
    // 线程池，用于异步渲染
    private final ExecutorService renderThreadPool;
    
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
    
    // 预渲染的文本图像
    private final AtomicReference<BufferedImage> renderedTextImage = new AtomicReference<>();
    
    // 字体设置
    private static final Font FONT = new Font("Arial", Font.BOLD, 24);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 128);
    
    /**
     * 构造方法
     * @param client Minecraft客户端实例
     */
    public CPURender(MinecraftClient client) {
        this.client = client;
        this.renderThreadPool = Executors.newFixedThreadPool(2); // 创建固定大小的线程池
        
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
        if (animationState != AnimationState.NONE) {
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
        
        BufferedImage textImage = renderedTextImage.get();
        if (textImage == null) {
            return;
        }
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        int imgWidth = textImage.getWidth();
        int imgHeight = textImage.getHeight();
        
        // 计算文本在屏幕上的位置
        int x = (screenWidth - imgWidth) / 2;
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
        
        // 绘制文本（这里不使用OpenGL，仅作为占位符）
        // 在实际实现中，我们需要创建自定义的渲染方法直接修改帧缓冲区
        // 由于Minecraft的渲染系统仍然需要使用OpenGL，这里仅展示结构
        
        // 注意：在实际CPU渲染实现中，我们需要使用更低级别的API
        // 例如直接操作帧缓冲区，而不是通过DrawContext
        client.execute(() -> {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.push();
            matrixStack.translate(x, y + yOffset[0], 0);
            
            Text text = Text.of(currentText);
            TextRenderer textRenderer = client.textRenderer;
            int textWidth = textRenderer.getWidth(text);
            
            // 在实际实现中，应该使用纯软件渲染代替这些方法
            DrawContext drawContext = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
            drawContext.drawTextWithShadow(textRenderer, text, (screenWidth - textWidth) / 2, y + yOffset[0], 
                    getAlphaColor(0xFFFFFF, alpha[0]));
            
            matrixStack.pop();
        });
    }
    
    /**
     * 使用CPU渲染文本到图像
     * @param text 要渲染的文本
     * @return 包含渲染文本的图像
     */
    private BufferedImage renderTextToBitmap(String text) {
        // 创建一个临时图像来测量文本尺寸
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tmp.createGraphics();
        g2d.setFont(FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 20; // 添加一些边距
        int height = fm.getHeight() + 10;
        g2d.dispose();
        
        // 创建实际的图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        
        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setFont(FONT);
        
        // 绘制文本阴影
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(text, 11, fm.getAscent() + 1);
        
        // 绘制主文本
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(text, 10, fm.getAscent());
        
        g2d.dispose();
        
        return image;
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
        
        // 异步预渲染文本图像
        CompletableFuture.supplyAsync(() -> renderTextToBitmap(title), renderThreadPool)
            .thenAccept(renderedTextImage::set)
            .exceptionally(ex -> {
                AreashintClient.LOGGER.error("渲染文本时出错: " + ex.getMessage());
                return null;
            });
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