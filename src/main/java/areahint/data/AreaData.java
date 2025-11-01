package areahint.data;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

/**
 * 区域数据模型
 * 包含区域的基本信息：名称、顶点、边界框、高度范围、等级、基础名称和颜色
 */
public class AreaData {
    private String name;                    // 区域名称
    private List<Vertex> vertices;          // 多边形顶点列表
    @SerializedName("second-vertices")
    private List<Vertex> secondVertices;    // AABB边界框顶点
    private AltitudeData altitude;          // 高度范围（新增）
    private int level;                      // 区域等级
    @SerializedName("base-name")
    private String baseName;               // 基础名称（上级区域）
    private String signature;              // 域名创建者签名
    private String color;                  // 域名颜色（十六进制格式）
    private String surfacename;            // 联合域名（表面域名）

    // 构造函数
    public AreaData() {
        this.vertices = new ArrayList<>();
        this.secondVertices = new ArrayList<>();
    }

    public AreaData(String name, List<Vertex> vertices, List<Vertex> secondVertices, 
                   AltitudeData altitude, int level, String baseName, String signature) {
        this.name = name;
        this.vertices = vertices;
        this.secondVertices = secondVertices;
        this.altitude = altitude;
        this.level = level;
        this.baseName = baseName;
        this.signature = signature;
        this.color = "#FFFFFF"; // 默认为白色
    }

    public AreaData(String name, List<Vertex> vertices, List<Vertex> secondVertices, 
                   AltitudeData altitude, int level, String baseName, String signature, String color) {
        this.name = name;
        this.vertices = vertices;
        this.secondVertices = secondVertices;
        this.altitude = altitude;
        this.level = level;
        this.baseName = baseName;
        this.signature = signature;
        this.color = color;
        this.surfacename = null; // 默认无联合域名
    }

    public AreaData(String name, List<Vertex> vertices, List<Vertex> secondVertices, 
                   AltitudeData altitude, int level, String baseName, String signature, String color, String surfacename) {
        this.name = name;
        this.vertices = vertices;
        this.secondVertices = secondVertices;
        this.altitude = altitude;
        this.level = level;
        this.baseName = baseName;
        this.signature = signature;
        this.color = color;
        this.surfacename = surfacename;
    }

    // Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Vertex> getSecondVertices() {
        return secondVertices;
    }

    public void setSecondVertices(List<Vertex> secondVertices) {
        this.secondVertices = secondVertices;
    }

    public AltitudeData getAltitude() {
        return altitude;
    }

    public void setAltitude(AltitudeData altitude) {
        this.altitude = altitude;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getColor() {
        return color != null ? color : "#FFFFFF"; // 如果为null，默认返回白色
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSurfacename() {
        return surfacename;
    }

    public void setSurfacename(String surfacename) {
        this.surfacename = surfacename;
    }

    /**
     * 验证区域数据的有效性
     * @return 验证是否通过
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        if (vertices == null || vertices.size() < 3) {
            return false;
        }
        
        if (secondVertices == null || secondVertices.size() != 4) {
            return false;
        }
        
        if (level < 1) {
            return false;
        }
        
        // 验证高度数据
        if (altitude != null && !altitude.isValid()) {
            return false;
        }
        
        // 验证颜色格式（如果存在）
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            return false;
        }
        
        return true;
    }

    /**
     * 顶点数据模型
     */
    public static class Vertex {
        private double x;
        private double z;

        public Vertex() {}

        public Vertex(double x, double z) {
            this.x = x;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("Vertex{x=%.1f, z=%.1f}", x, z);
        }
    }

    /**
     * 高度范围数据模型
     */
    public static class AltitudeData {
        private Double max;  // 最大高度，null表示无限制
        private Double min;  // 最小高度，null表示无限制

        public AltitudeData() {}

        public AltitudeData(Double max, Double min) {
            this.max = max;
            this.min = min;
        }

        public Double getMax() {
            return max;
        }

        public void setMax(Double max) {
            this.max = max;
        }

        public Double getMin() {
            return min;
        }

        public void setMin(Double min) {
            this.min = min;
        }

        /**
         * 验证高度数据的有效性
         * @return 验证是否通过
         */
        public boolean isValid() {
            // 如果max和min都不为null，max必须大于等于min
            if (max != null && min != null) {
                return max >= min;
            }
            return true;
        }

        /**
         * 检查给定高度是否在范围内
         * @param y 要检查的高度
         * @return 是否在范围内
         */
        public boolean isInRange(double y) {
            if (min != null && y < min) {
                return false;
            }
            if (max != null && y > max) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.format("AltitudeData{max=%s, min=%s}", 
                max != null ? max.toString() : "null", 
                min != null ? min.toString() : "null");
        }
    }

    @Override
    public String toString() {
        return String.format("AreaData{name='%s', level=%d, altitude=%s, vertices=%d, baseName='%s', signature='%s', color='%s', surfacename='%s'}", 
            name, level, altitude, vertices != null ? vertices.size() : 0, baseName, signature, color, surfacename);
    }
} 