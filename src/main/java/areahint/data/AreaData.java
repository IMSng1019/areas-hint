package areahint.data;

import java.util.List;
import java.util.ArrayList;

/**
 * 区域数据模型类
 * 用于表示一个区域的数据，包括名称、顶点、等级等
 */
public class AreaData {
    private String name; // 区域名称
    private List<Vertex> vertices; // 一级顶点（多边形的边界点）
    private List<Vertex> secondVertices; // 二级顶点（AABB包围盒的四个点）
    private int level; // 域名等级（1为顶级域名，2为二级域名，以此类推）
    private String baseName; // 上级域名（指向的域名等级必须等于该域名等级-1）

    /**
     * 构造方法
     * @param name 区域名称
     * @param vertices 一级顶点列表
     * @param secondVertices 二级顶点列表
     * @param level 域名等级
     * @param baseName 上级域名
     */
    public AreaData(String name, List<Vertex> vertices, List<Vertex> secondVertices, int level, String baseName) {
        this.name = name;
        this.vertices = vertices;
        this.secondVertices = secondVertices;
        this.level = level;
        this.baseName = baseName;
    }

    /**
     * 默认构造方法
     */
    public AreaData() {
        this.vertices = new ArrayList<>();
        this.secondVertices = new ArrayList<>();
    }

    /**
     * 获取区域名称
     * @return 区域名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置区域名称
     * @param name 区域名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取一级顶点列表
     * @return 一级顶点列表
     */
    public List<Vertex> getVertices() {
        return vertices;
    }

    /**
     * 设置一级顶点列表
     * @param vertices 一级顶点列表
     */
    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    /**
     * 获取二级顶点列表
     * @return 二级顶点列表
     */
    public List<Vertex> getSecondVertices() {
        return secondVertices;
    }

    /**
     * 设置二级顶点列表
     * @param secondVertices 二级顶点列表
     */
    public void setSecondVertices(List<Vertex> secondVertices) {
        this.secondVertices = secondVertices;
    }

    /**
     * 获取域名等级
     * @return 域名等级
     */
    public int getLevel() {
        return level;
    }

    /**
     * 设置域名等级
     * @param level 域名等级
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 获取上级域名
     * @return 上级域名
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * 设置上级域名
     * @param baseName 上级域名
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * 检查数据是否有效
     * @return 如果数据有效返回true，否则返回false
     */
    public boolean isValid() {
        // 名称不能为空
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // 一级顶点至少需要3个点
        if (vertices == null || vertices.size() < 3) {
            return false;
        }
        
        // 二级顶点必须正好是4个点（AABB包围盒）
        if (secondVertices == null || secondVertices.size() != 4) {
            return false;
        }
        
        // 域名等级必须大于0
        if (level <= 0) {
            return false;
        }
        
        // 一级域名的baseName必须为null
        if (level == 1 && baseName != null) {
            return false;
        }
        
        // 非一级域名的baseName不能为null
        if (level > 1 && (baseName == null || baseName.isEmpty())) {
            return false;
        }
        
        return true;
    }

    /**
     * 顶点类，表示一个二维坐标点
     */
    public static class Vertex {
        private double x; // X坐标
        private double z; // Z坐标 (在Minecraft中Y轴是高度，这里使用X,Z作为平面坐标)

        /**
         * 构造方法
         * @param x X坐标
         * @param z Z坐标
         */
        public Vertex(double x, double z) {
            this.x = x;
            this.z = z;
        }

        /**
         * 默认构造方法
         */
        public Vertex() {
        }

        /**
         * 获取X坐标
         * @return X坐标
         */
        public double getX() {
            return x;
        }

        /**
         * 设置X坐标
         * @param x X坐标
         */
        public void setX(double x) {
            this.x = x;
        }

        /**
         * 获取Z坐标
         * @return Z坐标
         */
        public double getZ() {
            return z;
        }

        /**
         * 设置Z坐标
         * @param z Z坐标
         */
        public void setZ(double z) {
            this.z = z;
        }
    }
} 