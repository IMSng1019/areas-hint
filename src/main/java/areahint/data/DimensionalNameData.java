package areahint.data;

/**
 * 维度域名数据类
 * 用于存储维度名称信息
 */
public class DimensionalNameData {
    private String dimensionId;     // 维度标识符 (如 "minecraft:overworld")
    private String displayName;     // 显示名称 (如 "蛮荒大陆")
    private String color;           // 颜色 (十六进制，如 "#FFFFFF")

    public DimensionalNameData() {}

    public DimensionalNameData(String dimensionId, String displayName) {
        this.dimensionId = dimensionId;
        this.displayName = displayName;
        this.color = null;
    }

    public DimensionalNameData(String dimensionId, String displayName, String color) {
        this.dimensionId = dimensionId;
        this.displayName = displayName;
        this.color = color;
    }

    public String getDimensionId() { return dimensionId; }
    public void setDimensionId(String dimensionId) { this.dimensionId = dimensionId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() {
        return String.format("DimensionalNameData{dimensionId='%s', displayName='%s', color='%s'}",
                dimensionId, displayName, color);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DimensionalNameData that = (DimensionalNameData) obj;
        return java.util.Objects.equals(dimensionId, that.dimensionId) &&
               java.util.Objects.equals(displayName, that.displayName) &&
               java.util.Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dimensionId, displayName, color);
    }
} 