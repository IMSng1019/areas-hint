package areahint.shrinkarea;

import areahint.data.AreaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收缩几何计算器
 * 实现复杂的域名收缩算法，包括：
 * 1. 检测收缩区域顶点是否在原域名外
 * 2. 计算线段交叉点作为边界点
 * 3. 删除在原域名内的顶点
 * 4. 计算最近的边界点
 * 5. 重新排列顶点以防止线段交叉
 */
public class ShrinkGeometryCalculator {
    private final AreaData originalArea;
    private final List<AreaData.Vertex> shrinkVertices;
    
    // 计算结果
    private List<AreaData.Vertex> finalVertices = new ArrayList<>();
    private List<AreaData.Vertex> boundaryPoints = new ArrayList<>();
    private List<AreaData.Vertex> validShrinkVertices = new ArrayList<>();
    private Map<AreaData.Vertex, Integer> boundaryInsertionMap = new HashMap<>();
    
    private static final double EPSILON = 1e-9;
    
    public ShrinkGeometryCalculator(AreaData originalArea, List<AreaData.Vertex> shrinkVertices) {
        this.originalArea = originalArea;
        this.shrinkVertices = new ArrayList<>(shrinkVertices);
    }
    
    /**
     * 执行域名收缩计算
     * @return 收缩后的域名数据，如果计算失败返回null
     */
    public AreaData shrinkArea() {
        try {
            // 1. 验证输入
            if (!validateInput()) {
                return null;
            }
            
            // 2. 检测收缩区域顶点并处理边界
            if (!processVerticesAndBoundaries()) {
                return null;
            }
            
            // 3. 计算边界点的插入位置
            if (!calculateBoundaryInsertion()) {
                return null;
            }
            
            // 4. 构造最终的顶点列表
            if (!constructFinalVertices()) {
                return null;
            }
            
            // 5. 检查并修复线段交叉
            if (!fixCrossings()) {
                return null;
            }
            
            // 6. 创建新的域名数据
            return createShrunkAreaData();
            
        } catch (Exception e) {
            System.err.println("域名收缩计算失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 验证输入数据
     */
    private boolean validateInput() {
        if (originalArea == null || originalArea.getVertices() == null || originalArea.getVertices().isEmpty()) {
            System.err.println("原域名数据无效");
            return false;
        }
        
        if (shrinkVertices == null || shrinkVertices.size() < 3) {
            System.err.println("收缩区域顶点数量不足");
            return false;
        }
        
        if (originalArea.getVertices().size() < 3) {
            System.err.println("原域名顶点数量不足");
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理顶点和边界计算
     */
    private boolean processVerticesAndBoundaries() {
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        
        // 清空结果列表
        validShrinkVertices.clear();
        boundaryPoints.clear();
        boundaryInsertionMap.clear();
        
        // 处理每个收缩顶点
        for (int i = 0; i < shrinkVertices.size(); i++) {
            AreaData.Vertex currentVertex = shrinkVertices.get(i);
            AreaData.Vertex nextVertex = shrinkVertices.get((i + 1) % shrinkVertices.size());
            
            // 检查当前顶点是否在原域名内
            boolean currentInside = isPointInPolygon(currentVertex, originalVertices);
            boolean nextInside = isPointInPolygon(nextVertex, originalVertices);
            
            if (!currentInside) {
                // 当前顶点在域名外，保留
                validShrinkVertices.add(currentVertex);
            }
            
            // 检查当前顶点到下一个顶点的线段是否与原域名边界相交
            if (currentInside != nextInside) {
                // 一个在内一个在外，计算交点
                AreaData.Vertex intersection = findPolygonIntersection(currentVertex, nextVertex, originalVertices);
                if (intersection != null) {
                    boundaryPoints.add(intersection);
                }
            }
        }
        
        if (validShrinkVertices.isEmpty() && boundaryPoints.isEmpty()) {
            System.err.println("所有收缩顶点都在原域名内，无法进行收缩");
            return false;
        }
        
        return true;
    }
    
    /**
     * 计算边界点的插入位置
     */
    private boolean calculateBoundaryInsertion() {
        if (boundaryPoints.isEmpty()) {
            // 没有边界点，直接使用有效的收缩顶点
            return true;
        }
        
        // 为每个边界点找到最近的原域名顶点对
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        
        for (AreaData.Vertex boundaryPoint : boundaryPoints) {
            double minDistance = Double.MAX_VALUE;
            int nearestEdgeIndex = -1;
            
            // 找到边界点最近的边
            for (int i = 0; i < originalVertices.size(); i++) {
                AreaData.Vertex v1 = originalVertices.get(i);
                AreaData.Vertex v2 = originalVertices.get((i + 1) % originalVertices.size());
                
                double distance = distanceToLineSegment(boundaryPoint, v1, v2);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestEdgeIndex = i;
                }
            }
            
            // 记录边界点的插入位置（在原顶点列表中的索引）
            boundaryInsertionMap.put(boundaryPoint, nearestEdgeIndex);
        }
        
        return true;
    }
    
    /**
     * 构造最终的顶点列表
     */
    private boolean constructFinalVertices() {
        finalVertices.clear();
        
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        
        // 如果没有边界点，只保留有效的收缩顶点
        if (boundaryPoints.isEmpty()) {
            finalVertices.addAll(validShrinkVertices);
            return true;
        }
        
        // 简化处理：只使用前两个边界点
        AreaData.Vertex boundary1 = null;
        AreaData.Vertex boundary2 = null;
        
        if (boundaryPoints.size() >= 2) {
            boundary1 = boundaryPoints.get(0);
            boundary2 = boundaryPoints.get(1);
        } else if (boundaryPoints.size() == 1) {
            boundary1 = boundaryPoints.get(0);
            boundary2 = new AreaData.Vertex(boundary1.getX(), boundary1.getZ());
            boundaryInsertionMap.put(boundary2, boundaryInsertionMap.get(boundary1));
        } else {
            return false;
        }
        
        int index1 = boundaryInsertionMap.get(boundary1);
        int index2 = boundaryInsertionMap.get(boundary2);
        
        // 确保index1 < index2
        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
            AreaData.Vertex tempVertex = boundary1;
            boundary1 = boundary2;
            boundary2 = tempVertex;
        }
        
        // 构建最终顶点列表：保留边界点之间较短路径的原顶点
        int shortPath = index2 - index1;
        int longPath = originalVertices.size() - shortPath;
        
        if (shortPath <= longPath) {
            // 保留 0 到 index1 的原顶点
            for (int i = 0; i <= index1; i++) {
                finalVertices.add(originalVertices.get(i));
            }
            // 添加第一个边界点
            finalVertices.add(boundary1);
            // 添加收缩顶点
            finalVertices.addAll(validShrinkVertices);
            // 添加第二个边界点
            finalVertices.add(boundary2);
            // 保留 index2+1 到末尾的原顶点
            for (int i = index2 + 1; i < originalVertices.size(); i++) {
                finalVertices.add(originalVertices.get(i));
            }
        } else {
            // 保留较长路径的原顶点
            for (int i = 0; i <= index1; i++) {
                finalVertices.add(originalVertices.get(i));
            }
            finalVertices.add(boundary1);
            finalVertices.addAll(validShrinkVertices);
            finalVertices.add(boundary2);
            for (int i = index2 + 1; i < originalVertices.size(); i++) {
                finalVertices.add(originalVertices.get(i));
            }
        }
        
        return true;
    }
    
    /**
     * 检查并修复线段交叉
     */
    private boolean fixCrossings() {
        if (finalVertices.size() < 4) {
            return true; // 少于4个顶点不会有交叉
        }
        
        // 检查是否有线段交叉
        boolean hasCrossing = false;
        for (int i = 0; i < finalVertices.size(); i++) {
            for (int j = i + 2; j < finalVertices.size(); j++) {
                // 跳过相邻的线段
                if (j == finalVertices.size() - 1 && i == 0) continue;
                
                AreaData.Vertex p1 = finalVertices.get(i);
                AreaData.Vertex p2 = finalVertices.get((i + 1) % finalVertices.size());
                AreaData.Vertex p3 = finalVertices.get(j);
                AreaData.Vertex p4 = finalVertices.get((j + 1) % finalVertices.size());
                
                if (doLinesIntersect(p1, p2, p3, p4)) {
                    hasCrossing = true;
                    break;
                }
            }
            if (hasCrossing) break;
        }
        
        if (hasCrossing) {
            // 尝试反转收缩顶点的顺序
            List<AreaData.Vertex> reversedShrinkVertices = new ArrayList<>();
            for (int i = validShrinkVertices.size() - 1; i >= 0; i--) {
                reversedShrinkVertices.add(validShrinkVertices.get(i));
            }
            validShrinkVertices = reversedShrinkVertices;
            
            // 重新构造顶点列表
            return constructFinalVertices();
        }
        
        return true;
    }
    
    /**
     * 创建收缩后的域名数据
     */
    private AreaData createShrunkAreaData() {
        if (finalVertices.isEmpty()) {
            return null;
        }
        
        // 计算新的边界框
        List<AreaData.Vertex> secondVertices = calculateBoundingBox(finalVertices);
        
        // 使用原域名的基本信息，但更新顶点列表
        AreaData shrunkArea = new AreaData(
            originalArea.getName(),        // 保持原名称
            finalVertices,                 // 新的顶点列表
            secondVertices,                // 新的边界框
            originalArea.getAltitude(),    // 保持原高度设置
            originalArea.getLevel(),       // 保持原等级
            originalArea.getBaseName(),    // 保持原basename
            originalArea.getSignature(),   // 保持原签名
            originalArea.getColor()        // 保持原颜色
        );
        
        return shrunkArea;
    }
    
    /**
     * 计算边界框
     */
    private List<AreaData.Vertex> calculateBoundingBox(List<AreaData.Vertex> vertices) {
        if (vertices.isEmpty()) {
            return new ArrayList<>();
        }
        
        double minX = vertices.get(0).getX();
        double maxX = vertices.get(0).getX();
        double minZ = vertices.get(0).getZ();
        double maxZ = vertices.get(0).getZ();
        
        for (AreaData.Vertex vertex : vertices) {
            minX = Math.min(minX, vertex.getX());
            maxX = Math.max(maxX, vertex.getX());
            minZ = Math.min(minZ, vertex.getZ());
            maxZ = Math.max(maxZ, vertex.getZ());
        }
        
        List<AreaData.Vertex> boundingBox = new ArrayList<>();
        boundingBox.add(new AreaData.Vertex(minX, minZ));
        boundingBox.add(new AreaData.Vertex(maxX, minZ));
        boundingBox.add(new AreaData.Vertex(maxX, maxZ));
        boundingBox.add(new AreaData.Vertex(minX, maxZ));
        
        return boundingBox;
    }
    
    /**
     * 判断点是否在多边形内（射线法）
     */
    private boolean isPointInPolygon(AreaData.Vertex point, List<AreaData.Vertex> polygon) {
        int intersections = 0;
        int n = polygon.size();
        
        for (int i = 0; i < n; i++) {
            AreaData.Vertex v1 = polygon.get(i);
            AreaData.Vertex v2 = polygon.get((i + 1) % n);
            
            if (rayIntersectsSegment(point, v1, v2)) {
                intersections++;
            }
        }
        
        return (intersections % 2) == 1;
    }
    
    /**
     * 射线与线段的交点检测
     */
    private boolean rayIntersectsSegment(AreaData.Vertex point, AreaData.Vertex v1, AreaData.Vertex v2) {
        double px = point.getX();
        double py = point.getZ();
        double ax = v1.getX();
        double ay = v1.getZ();
        double bx = v2.getX();
        double by = v2.getZ();
        
        if (ay > by) {
            double temp = ax; ax = bx; bx = temp;
            temp = ay; ay = by; by = temp;
        }
        
        if (py == ay || py == by) py += EPSILON;
        
        if (py < ay || py > by) return false;
        if (px >= Math.max(ax, bx)) return false;
        if (px < Math.min(ax, bx)) return true;
        
        double red = (py - ay) / (by - ay);
        double blue = (bx - ax) * red + ax;
        
        return px < blue;
    }
    
    /**
     * 找到线段与多边形的交点
     */
    private AreaData.Vertex findPolygonIntersection(AreaData.Vertex p1, AreaData.Vertex p2, List<AreaData.Vertex> polygon) {
        for (int i = 0; i < polygon.size(); i++) {
            AreaData.Vertex v1 = polygon.get(i);
            AreaData.Vertex v2 = polygon.get((i + 1) % polygon.size());
            
            AreaData.Vertex intersection = findLineIntersection(p1, p2, v1, v2);
            if (intersection != null) {
                return intersection;
            }
        }
        return null;
    }
    
    /**
     * 计算两条线段的交点
     */
    private AreaData.Vertex findLineIntersection(AreaData.Vertex p1, AreaData.Vertex p2, AreaData.Vertex p3, AreaData.Vertex p4) {
        double x1 = p1.getX(), y1 = p1.getZ();
        double x2 = p2.getX(), y2 = p2.getZ();
        double x3 = p3.getX(), y3 = p3.getZ();
        double x4 = p4.getX(), y4 = p4.getZ();
        
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        
        if (Math.abs(denom) < EPSILON) {
            return null; // 平行线
        }
        
        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;
        double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom;
        
        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            double x = x1 + t * (x2 - x1);
            double y = y1 + t * (y2 - y1);
            return new AreaData.Vertex(x, y);
        }
        
        return null;
    }
    
    /**
     * 检查两条线段是否相交
     */
    private boolean doLinesIntersect(AreaData.Vertex p1, AreaData.Vertex p2, AreaData.Vertex p3, AreaData.Vertex p4) {
        return findLineIntersection(p1, p2, p3, p4) != null;
    }
    
    /**
     * 计算点到线段的距离
     */
    private double distanceToLineSegment(AreaData.Vertex point, AreaData.Vertex lineStart, AreaData.Vertex lineEnd) {
        double px = point.getX(), py = point.getZ();
        double ax = lineStart.getX(), ay = lineStart.getZ();
        double bx = lineEnd.getX(), by = lineEnd.getZ();
        
        double dx = bx - ax;
        double dy = by - ay;
        
        if (dx == 0 && dy == 0) {
            return Math.sqrt((px - ax) * (px - ax) + (py - ay) * (py - ay));
        }
        
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        
        double closestX = ax + t * dx;
        double closestY = ay + t * dy;
        
        return Math.sqrt((px - closestX) * (px - closestX) + (py - closestY) * (py - closestY));
    }
} 