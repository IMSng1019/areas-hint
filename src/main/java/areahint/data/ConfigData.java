package areahint.data;

import com.google.gson.annotations.SerializedName;

/**
 * 配置数据模型类
 * 用于表示模组的配置选项
 */
public class ConfigData {
    // 检测频率，每秒检测的最大次数
    private int frequency;
    
    // 提示文字渲染方式：CPU、OpenGL、Vulkan
    private String hintRender;
    
    // 域名标题样式：full、simple、mixed
    private String titleStyle;
    
    // 模组启用状态：true为开启，false为关闭
    private boolean enabled;

    // 记录顶点的按键代码（GLFW键码）
    private int recordKey;

    // 域名标题大小：extra_large、large、medium_large、medium、medium_small、small、extra_small
    private String titleSize;

    // 副字幕大小：auto 表示始终比域名标题小一级，也可以手动使用标题大小的同组选项
    @SerializedName(value = "subtitlesize", alternate = {"subtitleSize"})
    private String subtitleSize;

    // 边界可视化开关：true为开启，false为关闭
    private boolean boundVizEnabled;

    // 语言设置
    private String language;

    // 语言锁定状态：true 为已锁定，false 为未锁定
    private boolean languageLocked;

    // 传送命令头
    private String teleportFormat;

    /**
     * 默认构造方法，使用默认配置
     */
    public ConfigData() {
        // 默认配置
        this.frequency = 1;
        this.hintRender = "OpenGL";
        this.titleStyle = "mixed";
        this.enabled = true; // 默认开启模组
        this.recordKey = 88; // 默认为X键 (GLFW_KEY_X = 88)
        this.titleSize = "medium"; // 默认为中等大小
        this.subtitleSize = "auto"; // 默认比当前域名标题小一级
        this.boundVizEnabled = false; // 默认关闭边界可视化
        this.language = "zh_cn"; // 默认中文
        this.languageLocked = false; // 默认不上锁
        this.teleportFormat = "tp"; // 默认传送命令头
    }
    
    /**
     * 构造方法
     * @param frequency 检测频率
     * @param hintRender 提示文字渲染方式
     * @param titleStyle 域名标题样式
     */
    public ConfigData(int frequency, String hintRender, String titleStyle) {
        this.frequency = frequency;
        this.hintRender = hintRender;
        this.titleStyle = titleStyle;
        this.enabled = true; // 默认开启模组
        this.recordKey = 88; // 默认为X键
        this.titleSize = "medium"; // 默认为中等大小
        this.subtitleSize = "auto"; // 默认比当前域名标题小一级
        this.boundVizEnabled = false; // 默认关闭边界可视化
        this.language = "zh_cn"; // 默认中文
        this.languageLocked = false; // 默认不上锁
        this.teleportFormat = "tp"; // 默认传送命令头
    }

    /**
     * 完整构造方法
     * @param frequency 检测频率
     * @param hintRender 提示文字渲染方式
     * @param titleStyle 域名标题样式
     * @param enabled 模组启用状态
     */
    public ConfigData(int frequency, String hintRender, String titleStyle, boolean enabled) {
        this.frequency = frequency;
        this.hintRender = hintRender;
        this.titleStyle = titleStyle;
        this.enabled = enabled;
        this.recordKey = 88; // 默认为X键
        this.titleSize = "medium"; // 默认为中等大小
        this.subtitleSize = "auto"; // 默认比当前域名标题小一级
        this.boundVizEnabled = false; // 默认关闭边界可视化
        this.language = "zh_cn"; // 默认中文
        this.languageLocked = false; // 默认不上锁
        this.teleportFormat = "tp"; // 默认传送命令头
    }
    
    /**
     * 复制当前配置，用于界面草稿等一次性提交场景。
     * @return 独立的配置副本
     */
    public ConfigData copy() {
        ConfigData copy = new ConfigData();
        copy.setFrequency(this.frequency);
        copy.setHintRender(normalizeRenderMode(this.hintRender));
        copy.setTitleStyle(normalizeStyleMode(this.titleStyle));
        copy.setEnabled(this.enabled);
        copy.setRecordKey(this.recordKey);
        copy.setTitleSize(normalizeSize(this.titleSize));
        copy.setSubtitleSize(normalizeSubtitleSize(this.subtitleSize));
        copy.setBoundVizEnabled(this.boundVizEnabled);
        copy.setLanguage(this.language != null && !this.language.isEmpty() ? this.language : "zh_cn");
        copy.setLanguageLocked(this.languageLocked);
        copy.setTeleportFormat(this.teleportFormat);
        return copy;
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
     * 获取提示文字渲染方式
     * @return 提示文字渲染方式
     */
    public String getHintRender() {
        return hintRender;
    }
    
    /**
     * 设置提示文字渲染方式
     * @param hintRender 提示文字渲染方式
     */
    public void setHintRender(String hintRender) {
        if (isValidRenderMode(hintRender)) {
            this.hintRender = hintRender;
        }
    }
    
    /**
     * 获取域名标题样式
     * @return 域名标题样式
     */
    public String getTitleStyle() {
        return titleStyle;
    }
    
    /**
     * 设置域名标题样式
     * @param titleStyle 域名标题样式
     */
    public void setTitleStyle(String titleStyle) {
        if (isValidStyleMode(titleStyle)) {
            this.titleStyle = titleStyle;
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
     * 获取域名标题大小
     * @return 域名标题大小
     */
    public String getTitleSize() {
        return titleSize;
    }

    /**
     * 设置域名标题大小
     * @param titleSize 域名标题大小
     */
    public void setTitleSize(String titleSize) {
        if (isValidSize(titleSize)) {
            this.titleSize = titleSize;
        }
    }

    /**
     * 获取副字幕大小
     * @return 副字幕大小
     */
    public String getSubtitleSize() {
        return subtitleSize;
    }

    /**
     * 设置副字幕大小
     * @param subtitleSize 副字幕大小，auto 表示跟随主标题并小一级
     */
    public void setSubtitleSize(String subtitleSize) {
        if (isValidSubtitleSize(subtitleSize)) {
            this.subtitleSize = subtitleSize;
        }
    }

    /**
     * 获取边界可视化开关状态
     * @return 边界可视化是否启用
     */
    public boolean isBoundVizEnabled() {
        return boundVizEnabled;
    }

    /**
     * 设置边界可视化开关状态
     * @param boundVizEnabled 边界可视化开关状态
     */
    public void setBoundVizEnabled(boolean boundVizEnabled) {
        this.boundVizEnabled = boundVizEnabled;
    }

    /**
     * 获取语言设置
     * @return 语言代码
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 设置语言
     * @param language 语言代码
     */
    public void setLanguage(String language) {
        if (language != null && !language.isEmpty()) {
            this.language = language;
        }
    }

    /**
     * 获取语言锁定状态
     * @return 语言是否已锁定
     */
    public boolean isLanguageLocked() {
        return languageLocked;
    }

    /**
     * 设置语言锁定状态
     * @param languageLocked 语言锁定状态
     */
    public void setLanguageLocked(boolean languageLocked) {
        this.languageLocked = languageLocked;
    }

    public String getTeleportFormat() {
        return isValidTeleportFormat(teleportFormat) ? teleportFormat : "tp";
    }

    public void setTeleportFormat(String teleportFormat) {
        this.teleportFormat = isValidTeleportFormat(teleportFormat) ? teleportFormat.trim() : "tp";
    }

    public static boolean isValidTeleportFormat(String teleportFormat) {
        if (teleportFormat == null) {
            return false;
        }
        String normalized = teleportFormat.trim().toLowerCase();
        return "tp".equals(normalized)
                || "minecraft:tp".equals(normalized)
                || "teleport".equals(normalized)
                || "minecraft:teleport".equals(normalized);
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
     * 验证域名标题大小是否有效
     * @param size 域名标题大小
     * @return 是否有效
     */
    public static boolean isValidSize(String size) {
        return "extra_large".equals(size) || "large".equals(size) || "medium_large".equals(size) ||
               "medium".equals(size) || "medium_small".equals(size) || "small".equals(size) ||
               "extra_small".equals(size);
    }

    /**
     * 验证副字幕大小是否有效
     * @param size 副字幕大小
     * @return 是否有效
     */
    public static boolean isValidSubtitleSize(String size) {
        return "auto".equals(size) || isValidSize(size);
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

    /**
     * 根据命令行输入转换为合适的域名标题大小
     * @param input 命令行输入
     * @return 标准的域名标题大小字符串
     */
    public static String normalizeSize(String input) {
        if (input == null) {
            return "medium"; // 默认值
        }

        String normalized = input.toLowerCase();
        switch (normalized) {
            case "extra_large":
                return "extra_large";
            case "large":
                return "large";
            case "medium_large":
                return "medium_large";
            case "medium":
                return "medium";
            case "medium_small":
                return "medium_small";
            case "small":
                return "small";
            case "extra_small":
                return "extra_small";
            default:
                return "medium"; // 默认值
        }
    }

    /**
     * 根据命令行输入转换为合适的副字幕大小
     * @param input 命令行输入
     * @return 标准的副字幕大小字符串
     */
    public static String normalizeSubtitleSize(String input) {
        if (input == null) {
            return "auto"; // 默认跟随主标题小一级
        }

        String normalized = input.toLowerCase();
        if ("auto".equals(normalized)) {
            return "auto";
        }
        return normalizeSize(normalized);
    }
} 
