package areahint.file;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.data.ConfigData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理工具类
 * 用于读写配置和域名文件
 */
public class FileManager {
    // Gson实例，用于JSON序列化和反序列化
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 基础路径，首次使用时初始化
    private static Path BASE_PATH;
    
    /**
     * 检查并创建配置文件夹
     * 该方法确保配置文件夹存在，如不存在则创建
     * @return 配置文件夹路径
     */
    public static Path checkFolderExist() {
        if (BASE_PATH != null) {
            return BASE_PATH;
        }

        // 获取模组JAR文件所在目录
        Path gameDir = FabricLoader.getInstance().getGameDir().normalize();
        // gameDir指向.minecraft目录，我们需要areas-hint目录
        Path configDir = gameDir.resolve("areas-hint");
        
        try {
            if (Files.notExists(configDir)) {
                Files.createDirectories(configDir);
                Areashint.LOGGER.info("已创建配置目录: " + configDir);
            } else {
                Areashint.LOGGER.info("配置目录已存在: " + configDir);
            }
            
            BASE_PATH = configDir;
            return configDir;
        } catch (IOException e) {
            Areashint.LOGGER.error("创建配置目录失败: " + e.getMessage());
            // 失败时返回一个备用路径
            BASE_PATH = gameDir;
            return gameDir;
        }
    }
    
    /**
     * 获取配置文件夹路径
     * @return 配置文件夹路径
     */
    public static Path getConfigFolder() {
        if (BASE_PATH == null) {
            return checkFolderExist();
        }
        return BASE_PATH;
    }

    /**
     * 获取特定维度的区域数据文件路径
     * @param dimensionFileName 维度文件名称
     * @return 文件路径
     */
    public static Path getDimensionFile(String dimensionFileName) {
        return getConfigFolder().resolve(dimensionFileName);
    }
    
    /**
     * 获取配置文件路径
     * @param filename 配置文件名
     * @return 文件路径
     */
    public static Path getConfigFile(String filename) {
        return getConfigFolder().resolve(filename);
    }
    
    /**
     * 创建空的区域文件
     * @param path 文件路径
     * @throws IOException 如果创建文件失败
     */
    public static void createEmptyAreaFile(Path path) throws IOException {
        if (Files.notExists(path)) {
            List<AreaData> emptyList = new ArrayList<>();
            String json = GSON.toJson(emptyList);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
            Areashint.LOGGER.info("已创建空的区域文件: " + path);
        }
    }
    
    /**
     * 创建默认配置文件
     * @param path 文件路径
     * @throws IOException 如果创建文件失败
     */
    public static void createDefaultConfigFile(Path path) throws IOException {
        if (Files.notExists(path)) {
            ConfigData defaultConfig = new ConfigData();
            String json = GSON.toJson(defaultConfig);

            // 添加注释
            String jsonWithComments = "{\n" +
                    "  // Frequency: 检测频率，每秒检测的最大次数（必须为正整数）\n" +
                    "  \"frequency\": " + defaultConfig.getFrequency() + ",\n\n" +
                    "  // SubtitleRender: 字幕渲染方式\n" +
                    "  // 选项: \"CPU\" (使用CPU渲染), \"OpenGL\" (使用OpenGL渲染), \"Vulkan\" (使用Vulkan渲染)\n" +
                    "  \"subtitleRender\": \"" + defaultConfig.getSubtitleRender() + "\",\n\n" +
                    "  // SubtitleStyle: 字幕样式\n" +
                    "  // 选项: \"full\" (显示完整路径), \"simple\" (仅显示当前级别), \"mixed\" (混合模式)\n" +
                    "  \"subtitleStyle\": \"" + defaultConfig.getSubtitleStyle() + "\",\n\n" +
                    "  // Enabled: 模组启用状态\n" +
                    "  // true: 启用模组, false: 禁用模组\n" +
                    "  \"enabled\": " + defaultConfig.isEnabled() + ",\n\n" +
                    "  // RecordKey: 记录顶点的按键代码（GLFW键码）\n" +
                    "  // 默认: 88 (X键), 可通过 /areahint replacebutton 命令修改\n" +
                    "  \"recordKey\": " + defaultConfig.getRecordKey() + ",\n\n" +
                    "  // SubtitleSize: 字幕大小\n" +
                    "  // 选项: \"extra_large\", \"large\", \"medium_large\", \"medium\", \"medium_small\", \"small\", \"extra_small\"\n" +
                    "  \"subtitleSize\": \"" + defaultConfig.getSubtitleSize() + "\"\n" +
                    "}";

            Files.write(path, jsonWithComments.getBytes(StandardCharsets.UTF_8));
            Areashint.LOGGER.info("已创建默认配置文件: " + path);
        }
    }
    
    /**
     * 读取区域数据
     * @param path 文件路径
     * @return 区域数据列表，如果读取失败则返回空列表
     */
    public static List<AreaData> readAreaData(Path path) {
        if (Files.notExists(path)) {
            return new ArrayList<>();
        }
        
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return GSON.fromJson(json, new TypeToken<List<AreaData>>(){}.getType());
        } catch (IOException | JsonSyntaxException e) {
            Areashint.LOGGER.error("读取区域数据失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 写入区域数据
     * @param path 文件路径
     * @param areas 区域数据列表
     * @return 是否写入成功
     */
    public static boolean writeAreaData(Path path, List<AreaData> areas) {
        try {
            String json = GSON.toJson(areas);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            Areashint.LOGGER.error("写入区域数据失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 读取配置数据
     * @param path 文件路径
     * @return 配置数据，如果读取失败则返回默认配置
     */
    public static ConfigData readConfigData(Path path) {
        if (Files.notExists(path)) {
            return new ConfigData();
        }

        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            // 移除注释（以 // 开头的行）
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(json));
            String line;
            while ((line = reader.readLine()) != null) {
                int commentStart = line.indexOf("//");
                if (commentStart != -1) {
                    line = line.substring(0, commentStart);
                }
                sb.append(line).append("\n");
            }

            ConfigData config = GSON.fromJson(sb.toString(), ConfigData.class);
            // 如果解析失败，返回默认配置
            if (config == null) {
                return new ConfigData();
            }

            // 检测配置完整性并补全缺失项
            boolean needsUpdate = false;
            ConfigData defaultConfig = new ConfigData();

            // 检查并补全 frequency
            if (config.getFrequency() <= 0) {
                config.setFrequency(defaultConfig.getFrequency());
                needsUpdate = true;
                Areashint.LOGGER.warn("配置项 'frequency' 无效或缺失，已补全为默认值: " + defaultConfig.getFrequency());
            }

            // 检查并补全 subtitleRender
            if (config.getSubtitleRender() == null || !ConfigData.isValidRenderMode(config.getSubtitleRender())) {
                config.setSubtitleRender(defaultConfig.getSubtitleRender());
                needsUpdate = true;
                Areashint.LOGGER.warn("配置项 'subtitleRender' 无效或缺失，已补全为默认值: " + defaultConfig.getSubtitleRender());
            }

            // 检查并补全 subtitleStyle
            if (config.getSubtitleStyle() == null || !ConfigData.isValidStyleMode(config.getSubtitleStyle())) {
                config.setSubtitleStyle(defaultConfig.getSubtitleStyle());
                needsUpdate = true;
                Areashint.LOGGER.warn("配置项 'subtitleStyle' 无效或缺失，已补全为默认值: " + defaultConfig.getSubtitleStyle());
            }

            // 检查并补全 subtitleSize
            if (config.getSubtitleSize() == null || !ConfigData.isValidSize(config.getSubtitleSize())) {
                config.setSubtitleSize(defaultConfig.getSubtitleSize());
                needsUpdate = true;
                Areashint.LOGGER.warn("配置项 'subtitleSize' 无效或缺失，已补全为默认值: " + defaultConfig.getSubtitleSize());
            }

            // 检查并补全 recordKey
            if (config.getRecordKey() <= 0) {
                config.setRecordKey(defaultConfig.getRecordKey());
                needsUpdate = true;
                Areashint.LOGGER.warn("配置项 'recordKey' 无效或缺失，已补全为默认值: " + defaultConfig.getRecordKey());
            }

            // 如果有缺失项，立即保存更新后的配置
            if (needsUpdate) {
                Areashint.LOGGER.info("检测到配置不完整，正在保存补全后的配置...");
                writeConfigData(path, config);
            }

            return config;
        } catch (IOException | JsonSyntaxException e) {
            Areashint.LOGGER.error("读取配置数据失败: " + e.getMessage());
            return new ConfigData();
        }
    }
    
    /**
     * 写入配置数据
     * @param path 文件路径
     * @param config 配置数据
     * @return 是否写入成功
     */
    public static boolean writeConfigData(Path path, ConfigData config) {
        try {
            // 添加注释
            String jsonWithComments = "{\n" +
                    "  // Frequency: 检测频率，每秒检测的最大次数（必须为正整数）\n" +
                    "  \"frequency\": " + config.getFrequency() + ",\n\n" +
                    "  // SubtitleRender: 字幕渲染方式\n" +
                    "  // 选项: \"CPU\" (使用CPU渲染), \"OpenGL\" (使用OpenGL渲染), \"Vulkan\" (使用Vulkan渲染)\n" +
                    "  \"subtitleRender\": \"" + config.getSubtitleRender() + "\",\n\n" +
                    "  // SubtitleStyle: 字幕样式\n" +
                    "  // 选项: \"full\" (显示完整路径), \"simple\" (仅显示当前级别), \"mixed\" (混合模式)\n" +
                    "  \"subtitleStyle\": \"" + config.getSubtitleStyle() + "\",\n\n" +
                    "  // Enabled: 模组启用状态\n" +
                    "  // true: 启用模组, false: 禁用模组\n" +
                    "  \"enabled\": " + config.isEnabled() + ",\n\n" +
                    "  // RecordKey: 记录顶点的按键代码（GLFW键码）\n" +
                    "  // 默认: 88 (X键), 可通过 /areahint replacebutton 命令修改\n" +
                    "  \"recordKey\": " + config.getRecordKey() + ",\n\n" +
                    "  // SubtitleSize: 字幕大小\n" +
                    "  // 选项: \"extra_large\", \"large\", \"medium_large\", \"medium\", \"medium_small\", \"small\", \"extra_small\"\n" +
                    "  \"subtitleSize\": \"" + config.getSubtitleSize() + "\"\n" +
                    "}";

            Files.write(path, jsonWithComments.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            Areashint.LOGGER.error("写入配置数据失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 添加区域数据
     * @param path 文件路径
     * @param area 区域数据
     * @return 是否添加成功
     */
    public static boolean addAreaData(Path path, AreaData area) {
        if (!area.isValid()) {
            Areashint.LOGGER.error("区域数据无效，无法添加");
            return false;
        }
        
        // 验证顶点格式
        if (!validateVerticesFormat(area)) {
            Areashint.LOGGER.error("区域顶点格式无效，必须使用标准格式");
            return false;
        }
        
        List<AreaData> areas = readAreaData(path);
        areas.add(area);
        return writeAreaData(path, areas);
    }
    
    /**
     * 验证顶点格式是否符合标准
     * @param area 区域数据
     * @return 如果顶点格式有效返回true，否则返回false
     */
    private static boolean validateVerticesFormat(AreaData area) {
        // 验证一级顶点
        List<AreaData.Vertex> vertices = area.getVertices();
        if (vertices == null || vertices.size() < 3) {
            Areashint.LOGGER.error("一级顶点数量不足，至少需要3个点");
            return false;
        }
        
        // 验证二级顶点
        List<AreaData.Vertex> secondVertices = area.getSecondVertices();
        if (secondVertices == null || secondVertices.size() != 4) {
            Areashint.LOGGER.error("二级顶点数量不正确，必须是4个点");
            return false;
        }
        
        Areashint.LOGGER.info("区域 '" + area.getName() + "' 顶点格式验证通过");
        return true;
    }
    
    /**
     * 根据JSON字符串解析区域数据
     * @param json JSON字符串
     * @return 区域数据，如果解析失败则返回null
     */
    public static AreaData parseAreaData(String json) {
        try {
            return GSON.fromJson(json, AreaData.class);
        } catch (JsonSyntaxException e) {
            Areashint.LOGGER.error("解析区域数据失败: " + e.getMessage());
            return null;
        }
    }
} 