package areahint.config;

import areahint.AreashintClient;
import areahint.data.ConfigData;
import areahint.file.FileManager;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 客户端配置类
 * 用于读写和管理客户端配置
 */
public class ClientConfig {
    // 当前配置
    private static ConfigData config = new ConfigData();
    // 配置是否已经加载
    private static boolean loaded = false;
    
    /**
     * 初始化配置
     */
    public static void init() {
        Path configFile = AreashintClient.getConfigFile();
        try {
            // 如果配置文件不存在，创建默认配置
            FileManager.createDefaultConfigFile(configFile);
        } catch (IOException e) {
            AreashintClient.LOGGER.error("创建配置文件失败: " + e.getMessage());
        }
        
        // 加载配置
        load();
    }
    
    /**
     * 加载配置
     */
    public static void load() {
        Path configFile = AreashintClient.getConfigFile();
        config = FileManager.readConfigData(configFile);
        loaded = true;
        AreashintClient.LOGGER.info("已加载配置: 频率={}, 渲染方式={}, 字幕样式={}",
                config.getFrequency(), config.getSubtitleRender(), config.getSubtitleStyle());
    }
    
    /**
     * 保存配置
     */
    public static void save() {
        if (!loaded) {
            return;
        }
        
        Path configFile = AreashintClient.getConfigFile();
        boolean success = FileManager.writeConfigData(configFile, config);
        if (success) {
            AreashintClient.LOGGER.info("已保存配置");
        } else {
            AreashintClient.LOGGER.error("保存配置失败");
        }
    }
    
    /**
     * 获取检测频率
     * @return 检测频率
     */
    public static int getFrequency() {
        return config.getFrequency();
    }
    
    /**
     * 设置检测频率
     * @param frequency 检测频率
     */
    public static void setFrequency(int frequency) {
        config.setFrequency(frequency);
        save();
    }
    
    /**
     * 获取字幕渲染方式
     * @return 字幕渲染方式
     */
    public static String getSubtitleRender() {
        return config.getSubtitleRender();
    }
    
    /**
     * 设置字幕渲染方式
     * @param subtitleRender 字幕渲染方式
     */
    public static void setSubtitleRender(String subtitleRender) {
        String normalizedRender = ConfigData.normalizeRenderMode(subtitleRender);
        config.setSubtitleRender(normalizedRender);
        save();
    }
    
    /**
     * 获取字幕样式
     * @return 字幕样式
     */
    public static String getSubtitleStyle() {
        return config.getSubtitleStyle();
    }
    
    /**
     * 设置字幕样式
     * @param subtitleStyle 字幕样式
     */
    public static void setSubtitleStyle(String subtitleStyle) {
        String normalizedStyle = ConfigData.normalizeStyleMode(subtitleStyle);
        config.setSubtitleStyle(normalizedStyle);
        save();
    }

    /**
     * 获取模组启用状态
     * @return 模组是否启用
     */
    public static boolean isEnabled() {
        return config.isEnabled();
    }
    
    /**
     * 设置模组启用状态
     * @param enabled 模组启用状态
     */
    public static void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
        save(); // 立即保存配置
    }
} 