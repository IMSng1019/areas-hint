package areahint.dimensional;

import areahint.Areashint;
import areahint.data.DimensionalNameData;
import areahint.file.FileManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 维度域名管理器
 * 负责管理维度名称的配置、加载和保存
 */
public class DimensionalNameManager {
    private static final String DIMENSIONAL_NAMES_FILE = "dimensional_names.json";
    private static final Gson GSON = new Gson();
    
    // 默认维度名称
    private static final Map<String, String> DEFAULT_DIMENSIONAL_NAMES = Map.of(
        "minecraft:overworld", "蛮荒大陆",
        "minecraft:the_nether", "恶堕之域", 
        "minecraft:the_end", "终末之地"
    );
    
    // 当前维度名称配置
    private static Map<String, String> dimensionalNames = new HashMap<>();
    
    /**
     * 初始化维度域名管理器
     */
    public static void init() {
        loadDimensionalNames();
        Areashint.LOGGER.info("维度域名管理器初始化完成");
    }
    
    /**
     * 加载维度域名配置
     */
    public static void loadDimensionalNames() {
        Path configFile = getDimensionalNamesFile();
        
        try {
            if (Files.notExists(configFile)) {
                // 创建默认配置文件
                createDefaultDimensionalNamesFile();
            }
            
            String json = Files.readString(configFile, StandardCharsets.UTF_8);
            List<DimensionalNameData> dataList = GSON.fromJson(json, 
                new TypeToken<List<DimensionalNameData>>(){}.getType());
            
            dimensionalNames.clear();
            for (DimensionalNameData data : dataList) {
                dimensionalNames.put(data.getDimensionId(), data.getDisplayName());
            }
            
            Areashint.LOGGER.info("已加载维度域名配置: {} 个维度", dimensionalNames.size());
            
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            Areashint.LOGGER.error("加载维度域名配置失败，使用默认配置: " + e.getMessage());
            dimensionalNames.clear();
            dimensionalNames.putAll(DEFAULT_DIMENSIONAL_NAMES);
        }
    }
    
    /**
     * 保存维度域名配置
     * @return 是否保存成功
     */
    public static boolean saveDimensionalNames() {
        Path configFile = getDimensionalNamesFile();
        
        try {
            List<DimensionalNameData> dataList = new ArrayList<>();
            for (Map.Entry<String, String> entry : dimensionalNames.entrySet()) {
                dataList.add(new DimensionalNameData(entry.getKey(), entry.getValue()));
            }
            
            String json = GSON.toJson(dataList);
            Files.writeString(configFile, json, StandardCharsets.UTF_8);
            
            Areashint.LOGGER.info("维度域名配置已保存");
            return true;
            
        } catch (IOException e) {
            Areashint.LOGGER.error("保存维度域名配置失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 创建默认维度域名配置文件
     */
    private static void createDefaultDimensionalNamesFile() throws IOException {
        List<DimensionalNameData> defaultData = new ArrayList<>();
        for (Map.Entry<String, String> entry : DEFAULT_DIMENSIONAL_NAMES.entrySet()) {
            defaultData.add(new DimensionalNameData(entry.getKey(), entry.getValue()));
        }
        
        String json = GSON.toJson(defaultData);
        Path configFile = getDimensionalNamesFile();
        
        // 确保目录存在
        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, json, StandardCharsets.UTF_8);
        
        // 初始化当前配置
        dimensionalNames.clear();
        dimensionalNames.putAll(DEFAULT_DIMENSIONAL_NAMES);
        
        Areashint.LOGGER.info("已创建默认维度域名配置文件: " + configFile);
    }
    
    /**
     * 获取维度域名配置文件路径
     * @return 配置文件路径
     */
    private static Path getDimensionalNamesFile() {
        return FileManager.getConfigFile(DIMENSIONAL_NAMES_FILE);
    }
    
    /**
     * 获取维度的显示名称
     * @param dimensionId 维度标识符
     * @return 维度显示名称
     */
    public static String getDimensionalName(String dimensionId) {
        return dimensionalNames.getOrDefault(dimensionId, 
            DEFAULT_DIMENSIONAL_NAMES.getOrDefault(dimensionId, dimensionId));
    }
    
    /**
     * 设置维度的显示名称
     * @param dimensionId 维度标识符
     * @param displayName 显示名称
     */
    public static void setDimensionalName(String dimensionId, String displayName) {
        dimensionalNames.put(dimensionId, displayName);
        Areashint.LOGGER.info("已更新维度域名: {} -> {}", dimensionId, displayName);
    }
    
    /**
     * 获取所有维度域名配置
     * @return 维度域名映射
     */
    public static Map<String, String> getAllDimensionalNames() {
        return new HashMap<>(dimensionalNames);
    }
    
    /**
     * 获取所有配置的维度ID列表
     * @return 维度ID集合
     */
    public static Set<String> getAllDimensionIds() {
        return new HashSet<>(dimensionalNames.keySet());
    }
    
    /**
     * 检查维度是否有自定义名称
     * @param dimensionId 维度标识符
     * @return 是否有自定义名称
     */
    public static boolean hasDimensionalName(String dimensionId) {
        return dimensionalNames.containsKey(dimensionId);
    }
    
    /**
     * 重置所有维度名称为默认值
     */
    public static void resetToDefaults() {
        dimensionalNames.clear();
        dimensionalNames.putAll(DEFAULT_DIMENSIONAL_NAMES);
        Areashint.LOGGER.info("维度域名配置已重置为默认值");
    }
} 