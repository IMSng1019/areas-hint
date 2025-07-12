package areahint.render;

import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

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
    
    // 文本大小缩放比例
    private static final float TEXT_SCALE = 1.5f; // 增大文本大小
    
    // 上一帧的Y偏移和透明度，用于平滑插值
    private float lastYOffset = 0;
    private float lastAlpha = 0;
    
    // 插值系数（0-1之间，越小越平滑，但延迟越大）
    private static final float INTERPOLATION_FACTOR = 0.15f;
    
    // 预渲染的文本图像
    private final AtomicReference<BufferedImage> renderedTextImage = new AtomicReference<>();
    
    // 字体设置
    private static final Font FONT = new Font("Arial", Font.BOLD, 32); // 增大字体大小，从24到32
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 128);
    
    /**
     * 构造方法
     * @param client Minecraft客户端实例
     */
    public CPURender(MinecraftClient client) {
        this.client = client;
        this.renderThreadPool = Executors.newFixedThreadPool(2); // 创建固定大小的线程池
        
        // 注册Tick事件用于更新动画状态
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        // 注册HUD渲染事件
        HudRenderCallback.EVENT.register(this::onHudRender);
    }
    
    /**
     * HUD渲染事件处理
     * @param drawContext 绘制上下文
     * @param tickDelta tick间隔时间
     */
    private void onHudRender(DrawContext drawContext, float tickDelta) {
        if (animationState == AnimationState.NONE || currentText == null || client.player == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // 计算文本在屏幕上的位置
        int x = screenWidth / 2;
        int y = screenHeight / 4; // 屏幕1/4位置
        
        // 根据动画状态计算Y偏移和透明度
        float alpha = 1.0f;
        float yOffset = 0.0f;
        
        long elapsedTime = System.currentTimeMillis() - animationStartTime;
        float progress = 0;
        
        switch (animationState) {
            case IN:
                // 确保渐入动画的进度计算更加平滑
                progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_IN_DURATION);
                progress = easeOutCubic(progress); // 使用缓动函数
                alpha = progress;
                // 渐入动画时不使用Y偏移，保持固定位置
                yOffset = 0.0f;
                break;
            case STAY:
                // 保持原位，完全不透明
                alpha = 1.0f;
                yOffset = 0.0f;
                break;
            case OUT:
                progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_OUT_DURATION);
                progress = easeInCubic(progress); // 使用缓动函数
                yOffset = -15.0f * progress; // 使用浮点数计算，避免整数截断导致的闪烁
                alpha = 1.0f - progress;
                break;
            case NONE:
                return; // 不渲染
        }
        
        // 应用平滑插值，减少闪烁
        // 对于渐入动画，直接使用计算值，不进行插值处理
        if (animationState == AnimationState.IN && elapsedTime < 100) {
            // 渐入动画开始阶段，直接使用计算值
            lastYOffset = yOffset;
            lastAlpha = alpha;
        } else {
            // 其他情况使用插值
            yOffset = lastYOffset * (1.0f - INTERPOLATION_FACTOR) + yOffset * INTERPOLATION_FACTOR;
            alpha = lastAlpha * (1.0f - INTERPOLATION_FACTOR) + alpha * INTERPOLATION_FACTOR;
            
            // 保存当前值用于下一帧插值
            lastYOffset = yOffset;
            lastAlpha = alpha;
        }
        
        // 渲染文本
        Text text = Text.of(currentText);
        TextRenderer textRenderer = client.textRenderer;
        
        // 应用缩放来增大文本尺寸
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        // 使用浮点数直接传递给矩阵变换，避免整数转换
        matrixStack.translate(x, y + yOffset, 0);
        matrixStack.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        
        // 获取未缩放的文本宽度
        int textWidth = textRenderer.getWidth(text);
        
        // 计算最终位置 (正确计算居中位置)
        int finalX = -textWidth / 2;
        int finalY = 0;
        
        // 绘制带有阴影的文本
        int color = getAlphaColor(0xFFFFFF, alpha);
        
        // CPU渲染特效：添加一个简单的背景框
        int bgColor = getAlphaColor(0x000000, alpha * 0.5f);
        drawContext.fill(finalX - 4, finalY - 2, finalX + textWidth + 4, finalY + textRenderer.fontHeight + 2, bgColor);
        
        drawContext.drawTextWithShadow(textRenderer, text, finalX, finalY, color);
        
        // 恢复矩阵状态
        matrixStack.pop();
        
        // 输出调试信息
        if (animationState == AnimationState.IN) {
            AreashintClient.LOGGER.debug("CPURender: 渐入动画 - 区域标题: {}, 进度: {}, 透明度: {}", 
                currentText, progress, alpha);
        }
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
            AreashintClient.LOGGER.debug("CPURender: 动画状态更新 IN → STAY");
        } else if (animationState == AnimationState.STAY && elapsedTime >= ANIMATION_STAY_DURATION) {
            animationState = AnimationState.OUT;
            animationStartTime = currentTime;
            // 重置插值变量以确保平滑过渡
            lastYOffset = 0.0f;
            lastAlpha = 1.0f;
            AreashintClient.LOGGER.debug("CPURender: 动画状态更新 STAY → OUT");
        } else if (animationState == AnimationState.OUT && elapsedTime >= ANIMATION_OUT_DURATION) {
            animationState = AnimationState.NONE;
            currentText = null;
            AreashintClient.LOGGER.debug("CPURender: 动画状态更新 OUT → NONE");
        }
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
        
        // 初始化插值变量为0，确保渐入动画从完全透明开始
        lastYOffset = 0.0f;
        lastAlpha = 0.0f;
        
        // 异步预渲染文本图像
        CompletableFuture.supplyAsync(() -> renderTextToBitmap(title), renderThreadPool)
            .thenAccept(renderedTextImage::set)
            .exceptionally(ex -> {
                AreashintClient.LOGGER.error("渲染文本时出错: " + ex.getMessage());
                return null;
            });
        
        // 添加日志记录
        AreashintClient.LOGGER.info("CPURender: 开始显示区域标题: {}, 动画状态: {}", title, animationState);
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