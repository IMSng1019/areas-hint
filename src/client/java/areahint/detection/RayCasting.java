package areahint.detection;

import areahint.data.AreaData;

import java.util.List;

/**
 * 射线法实现类
 * 用于检测点是否在多边形内
 */
public class RayCasting {
    
    /**
     * 使用射线法检测点是否在多边形内部
     * 基本思想是从给定点向右（正X方向）发射一条水平射线，
     * 并计算该射线与多边形边的交点数量：
     * - 如果交点数量为奇数：点在多边形内部。
     * - 如果交点数量为偶数：点在多边形外部。
     *
     * @param x 点的X坐标
     * @param z 点的Z坐标
     * @param vertices 多边形的顶点列表
     * @return 如果点在多边形内部返回true，否则返回false
     */
    public static boolean isPointInPolygon(double x, double z, List<AreaData.Vertex> vertices) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }
        
        int intersections = 0;
        int vertexCount = vertices.size();
        
        for (int i = 0; i < vertexCount; i++) {
            // 获取当前边的两个顶点
            AreaData.Vertex current = vertices.get(i);
            AreaData.Vertex next = vertices.get((i + 1) % vertexCount);
            
            // 检查边是否与射线相交
            if (isIntersect(x, z, current, next)) {
                intersections++;
            }
        }
        
        // 奇数个交点表示点在多边形内部，偶数个交点表示点在多边形外部
        return (intersections % 2 == 1);
    }
    
    /**
     * 检查从点(x,z)向右发射的水平射线是否与线段相交
     * 
     * @param x 点的X坐标
     * @param z 点的Z坐标
     * @param v1 线段的第一个顶点
     * @param v2 线段的第二个顶点
     * @return 如果射线与线段相交返回true，否则返回false
     */
    private static boolean isIntersect(double x, double z, AreaData.Vertex v1, AreaData.Vertex v2) {
        // 获取线段的两个端点坐标
        double x1 = v1.getX();
        double z1 = v1.getZ();
        double x2 = v2.getX();
        double z2 = v2.getZ();
        
        // 检查点是否在线段的两个端点的水平线上
        if ((z1 <= z && z2 > z) || (z2 <= z && z1 > z)) {
            // 计算交点的X坐标
            double xIntersect = x1 + (z - z1) * (x2 - x1) / (z2 - z1);
            
            // 如果交点在射线的右侧（点的X坐标的右侧），则射线与线段相交
            if (xIntersect > x) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查点是否在轴对齐包围盒（AABB）内部
     * AABB由四个点定义，分别是左上角、右上角、右下角和左下角
     * 
     * @param x 点的X坐标
     * @param z 点的Z坐标
     * @param secondVertices AABB的四个顶点（左上、右上、右下、左下）
     * @return 如果点在AABB内部返回true，否则返回false
     */
    public static boolean isPointInAABB(double x, double z, List<AreaData.Vertex> secondVertices) {
        if (secondVertices == null || secondVertices.size() != 4) {
            return false;
        }
        
        // 计算AABB的边界
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        
        for (AreaData.Vertex vertex : secondVertices) {
            minX = Math.min(minX, vertex.getX());
            maxX = Math.max(maxX, vertex.getX());
            minZ = Math.min(minZ, vertex.getZ());
            maxZ = Math.max(maxZ, vertex.getZ());
        }
        
        // 检查点是否在AABB内部
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
} 