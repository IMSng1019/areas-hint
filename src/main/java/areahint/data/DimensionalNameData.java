package areahint.data;

/**
 * 维度域名数据类
 * 用于存储维度名称信息
 */
public class DimensionalNameData {
    private String dimensionId;     // 维度标识符 (如 "minecraft:overworld")
    private String displayName;     // 显示名称 (如 "蛮荒大陆")
    
    /**
     * 默认构造函数（用于JSON反序列化）
     */
    public DimensionalNameData() {}
    
    /**
     * 构造函数
     * @param dimensionId 维度标识符
     * @param displayName 显示名称
     */
    public DimensionalNameData(String dimensionId, String displayName) {
        this.dimensionId = dimensionId;
        this.displayName = displayName;
    }
    
    // Getters and Setters
    public String getDimensionId() {
        return dimensionId;
    }
    
    public void setDimensionId(String dimensionId) {
        this.dimensionId = dimensionId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return String.format("DimensionalNameData{dimensionId='%s', displayName='%s'}", 
                dimensionId, displayName);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DimensionalNameData that = (DimensionalNameData) obj;
        return java.util.Objects.equals(dimensionId, that.dimensionId) &&
               java.util.Objects.equals(displayName, that.displayName);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(dimensionId, displayName);
    }
} 