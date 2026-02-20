package areahint.render;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import net.minecraft.client.MinecraftClient;

/**
 * 渲染管理器类
 * 负责管理不同渲染模式和显示区域名称
 */
public class RenderManager {
    // 不同的渲染实现
    private final CPURender cpuRender;
    private final GLRender glRender;
    private final VulkanRender vulkanRender;
    
    // 当前渲染器
    private IRender currentRender;
    
    // Minecraft客户端实例
    private final MinecraftClient client;
    
    /**
     * 构造方法，初始化各种渲染实现
     */
    public RenderManager() {
        this.client = MinecraftClient.getInstance();
        this.cpuRender = new CPURender(client);
        this.glRender = new GLRender(client);
        this.vulkanRender = new VulkanRender(client);
        
        // 根据配置设置当前渲染器
        updateRenderMode();
    }
    
    /**
     * 更新当前的渲染模式
     */
    public void updateRenderMode() {
        String mode = ClientConfig.getSubtitleRender();
        switch (mode) {
            case "CPU":
                currentRender = cpuRender;
                AreashintClient.LOGGER.info("使用CPU渲染模式");
                break;
            case "OpenGL":
                currentRender = glRender;
                AreashintClient.LOGGER.info("使用OpenGL渲染模式");
                break;
            case "Vulkan":
                currentRender = vulkanRender;
                AreashintClient.LOGGER.info("使用Vulkan渲染模式");
                break;
            default:
                currentRender = glRender;
                AreashintClient.LOGGER.info("未知渲染模式，使用默认OpenGL");
                break;
        }
    }
    
    /**
     * 显示区域名称
     * @param areaName 区域名称
     */
    public void showAreaTitle(String areaName) {
        showAreaTitle(areaName, "#FFFFFF");
    }

    /**
     * 显示区域名称（带颜色）
     * @param areaName 区域名称
     * @param color 颜色值（十六进制或闪烁模式）
     */
    public void showAreaTitle(String areaName, String color) {
        if (areaName == null || areaName.isEmpty()) {
            return;
        }
        currentRender.renderTitle(areaName, color != null ? color : "#FFFFFF");
    }

    /**
     * 渲染接口，定义渲染方法
     */
    public interface IRender {
        /**
         * 渲染标题（带颜色）
         * @param title 标题文本
         * @param color 颜色值
         */
        void renderTitle(String title, String color);
    }
} 