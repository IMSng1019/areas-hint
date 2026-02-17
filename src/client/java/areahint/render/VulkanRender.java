package areahint.render;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Vulkan渲染实现类
 * 使用Vulkan进行渲染
 * 注意：由于Minecraft没有原生支持Vulkan API，
 * 这个类实际上仍然使用OpenGL进行渲染，但预留了未来集成Vulkan的可能性
 */
public class VulkanRender implements RenderManager.IRender {
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
    
    // 上一帧的Y偏移和透明度，用于平滑插值
    private float lastYOffset = 0;
    private float lastAlpha = 0;
    
    // 插值系数（0-1之间，越小越平滑，但延迟越大）
    private static final float INTERPOLATION_FACTOR = 0.15f;
    
    /**
     * 构造方法
     * @param client Minecraft客户端实例
     */
    public VulkanRender(MinecraftClient client) {
        this.client = client;
        
        // 注册Tick事件用于更新动画状态
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        // 注册HUD渲染事件
        HudRenderCallback.EVENT.register(this::onHudRender);
        
        AreashintClient.LOGGER.info("Vulkan渲染器初始化 (注：实际使用兼容模式)");
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
        // 根据配置获取字幕大小
        float textScale = getTextScale();
        matrixStack.scale(textScale, textScale, 1.0f);
        
        // 获取未缩放的文本宽度
            int textWidth = textRenderer.getWidth(text);
            
        // 计算最终位置 (正确计算居中位置)
        int finalX = -textWidth / 2;
        int finalY = 0;
            
            // 为Vulkan渲染添加额外效果
            // 注意：这仅是模拟，实际上没有使用Vulkan API
            
            // 绘制发光背景（简单模拟高级效果）
        int bgColor = getAlphaColor(0x2266FF, alpha * 0.3f);
            drawContext.fill(finalX - 10, finalY - 5, finalX + textWidth + 10, finalY + 15, bgColor);
            
            // 绘制文本阴影
        int color = getAlphaColor(0xFFFFFF, alpha);
            drawContext.drawTextWithShadow(textRenderer, text, finalX, finalY, color);
            
        // 恢复矩阵状态
            matrixStack.pop();
        
        // 输出调试信息
        if (animationState == AnimationState.IN) {
            AreashintClient.LOGGER.debug("VulkanRender: 渐入动画 - 区域标题: {}, 进度: {}, 透明度: {}", 
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
            AreashintClient.LOGGER.debug("VulkanRender: 动画状态更新 IN → STAY");
        } else if (animationState == AnimationState.STAY && elapsedTime >= ANIMATION_STAY_DURATION) {
            animationState = AnimationState.OUT;
            animationStartTime = currentTime;
            // 重置插值变量以确保平滑过渡
            lastYOffset = 0.0f;
            lastAlpha = 1.0f;
            AreashintClient.LOGGER.debug("VulkanRender: 动画状态更新 STAY → OUT");
        } else if (animationState == AnimationState.OUT && elapsedTime >= ANIMATION_OUT_DURATION) {
            animationState = AnimationState.NONE;
            currentText = null;
            AreashintClient.LOGGER.debug("VulkanRender: 动画状态更新 OUT → NONE");
        }
    }
    
    /**
     * 根据配置获取文本缩放比例
     * @return 文本缩放比例
     */
    private float getTextScale() {
        String size = ClientConfig.getSubtitleSize();
        switch (size) {
            case "extra_large":
                return 3.0f;
            case "large":
                return 2.5f;
            case "medium_large":
                return 2.0f;
            case "medium":
                return 1.5f;
            case "medium_small":
                return 1.2f;
            case "small":
                return 1.0f;
            case "extra_small":
                return 0.8f;
            default:
                return 1.5f; // 默认中等大小
        }
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
        
        // 添加日志记录
        AreashintClient.LOGGER.info("VulkanRender: 开始显示区域标题: {}, 动画状态: {}", title, animationState);
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