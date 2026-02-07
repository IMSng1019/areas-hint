package areahint.data;

/**
 * 配置数据模型类
 * 用于表示模组的配置选项
 */
public class ConfigData {
    // 检测频率，每秒检测的最大次数
    private int frequency;
    
    // 字幕渲染方式：CPU、OpenGL、Vulkan
    private String subtitleRender;
    
    // 字幕样式：full、simple、mixed
    private String subtitleStyle;
    
    // 模组启用状态：true为开启，false为关闭
    private boolean enabled;

    // 记录顶点的按键代码（GLFW键码）
    private int recordKey;

    /**
     * 默认构造方法，使用默认配置
     */
    public ConfigData() {
        // 默认配置
        this.frequency = 1;
        this.subtitleRender = "OpenGL";
        this.subtitleStyle = "mixed";
        this.enabled = true; // 默认开启模组
        this.recordKey = 88; // 默认为X键 (GLFW_KEY_X = 88)
    }
    
    /**
     * 构造方法
     * @param frequency 检测频率
     * @param subtitleRender 字幕渲染方式
     * @param subtitleStyle 字幕样式
     */
    public ConfigData(int frequency, String subtitleRender, String subtitleStyle) {
        this.frequency = frequency;
        this.subtitleRender = subtitleRender;
        this.subtitleStyle = subtitleStyle;
        this.enabled = true; // 默认开启模组
        this.recordKey = 88; // 默认为X键
    }

    /**
     * 完整构造方法
     * @param frequency 检测频率
     * @param subtitleRender 字幕渲染方式
     * @param subtitleStyle 字幕样式
     * @param enabled 模组启用状态
     */
    public ConfigData(int frequency, String subtitleRender, String subtitleStyle, boolean enabled) {
        this.frequency = frequency;
        this.subtitleRender = subtitleRender;
        this.subtitleStyle = subtitleStyle;
        this.enabled = enabled;
        this.recordKey = 88; // 默认为X键
    }
    
    /**
     * 获取检测频率
     * @return 检测频率
     */
    public int getFrequency() {
        return frequency;
    }
    
    /**
     * 设置检测频率
     * @param frequency 检测频率
     */
    public void setFrequency(int frequency) {
        if (frequency <= 0) {
            frequency = 1; // 确保频率至少为1
        }
        this.frequency = frequency;
    }
    
    /**
     * 获取字幕渲染方式
     * @return 字幕渲染方式
     */
    public String getSubtitleRender() {
        return subtitleRender;
    }
    
    /**
     * 设置字幕渲染方式
     * @param subtitleRender 字幕渲染方式
     */
    public void setSubtitleRender(String subtitleRender) {
        if (isValidRenderMode(subtitleRender)) {
            this.subtitleRender = subtitleRender;
        }
    }
    
    /**
     * 获取字幕样式
     * @return 字幕样式
     */
    public String getSubtitleStyle() {
        return subtitleStyle;
    }
    
    /**
     * 设置字幕样式
     * @param subtitleStyle 字幕样式
     */
    public void setSubtitleStyle(String subtitleStyle) {
        if (isValidStyleMode(subtitleStyle)) {
            this.subtitleStyle = subtitleStyle;
        }
    }
    
    /**
     * 获取模组启用状态
     * @return 模组是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置模组启用状态
     * @param enabled 模组启用状态
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取记录按键代码
     * @return 记录按键代码（GLFW键码）
     */
    public int getRecordKey() {
        return recordKey;
    }

    /**
     * 设置记录按键代码
     * @param recordKey 记录按键代码（GLFW键码）
     */
    public void setRecordKey(int recordKey) {
        this.recordKey = recordKey;
    }

    /**
     * 验证渲染方式是否有效
     * @param mode 渲染方式
     * @return 是否有效
     */
    public static boolean isValidRenderMode(String mode) {
        return "CPU".equals(mode) || "OpenGL".equals(mode) || "Vulkan".equals(mode);
    }
    
    /**
     * 验证样式是否有效
     * @param style 样式
     * @return 是否有效
     */
    public static boolean isValidStyleMode(String style) {
        return "full".equals(style) || "simple".equals(style) || "mixed".equals(style);
    }
    
    /**
     * 根据命令行输入转换为合适的渲染模式
     * @param input 命令行输入
     * @return 标准的渲染模式字符串
     */
    public static String normalizeRenderMode(String input) {
        if (input == null) {
            return "OpenGL"; // 默认值
        }
        
        String normalized = input.toLowerCase();
        switch (normalized) {
            case "cpu":
                return "CPU";
            case "opengl":
                return "OpenGL";
            case "vulkan":
                return "Vulkan";
            default:
                return "OpenGL"; // 默认值
        }
    }
    
    /**
     * 根据命令行输入转换为合适的样式模式
     * @param input 命令行输入
     * @return 标准的样式模式字符串
     */
    public static String normalizeStyleMode(String input) {
        if (input == null) {
            return "mixed"; // 默认值
        }
        
        String normalized = input.toLowerCase();
        switch (normalized) {
            case "full":
                return "full";
            case "simple":
                return "simple";
            case "mixed":
                return "mixed";
            default:
                return "mixed"; // 默认值
        }
    }
} 