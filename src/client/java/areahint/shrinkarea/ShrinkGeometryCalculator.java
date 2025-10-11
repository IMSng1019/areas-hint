package areahint.shrinkarea;

import areahint.data.AreaData;

import java.util.ArrayList;
import java.util.List;

/**
 * 收缩几何计算器
 * 完全按照提示词实现复杂的域名收缩算法：
 * 1. 检测收缩区域顶点是否在原域名外（包括边界上）
 * 2. 计算线段与原域名边界的交叉点作为边界点
 * 3. 删除在原域名内的收缩顶点
 * 4. 计算初始点和末尾点的临近点
 * 5. 计算临近点最近的边界点
 * 6. 处理一个点对应多个边界点的情况（取中位值）
 * 7. 判断边界点在原域名哪两个顶点之间
 * 8. 删除两个边界点之间的原域名顶点（较短路径）
 * 9. 判断新顶点插入方式（正向或反向）
 * 10. 插入：原顶点 → 边界点 → 新顶点 → 边界点 → 原顶点
 * 11. 检查并修复线段交叉
 * 12. 重新计算二级顶点
 */
public class ShrinkGeometryCalculator {
    private final AreaData originalArea;
    private final List<AreaData.Vertex> shrinkVertices;
    
    // 计算中间结果
    private List<AreaData.Vertex> externalShrinkVertices = new ArrayList<>();  // 在原域名外的收缩顶点
    private List<AreaData.Vertex> boundaryPoints = new ArrayList<>();          // 边界交叉点
    private AreaData.Vertex startBoundaryPoint = null;  // 起始边界点
    private AreaData.Vertex endBoundaryPoint = null;    // 结束边界点
    private int startBoundaryIndex = -1;  // 起始边界点在原域名顶点中的插入位置
    private int endBoundaryIndex = -1;    // 结束边界点在原域名顶点中的插入位置
    
    private static final double EPSILON = 1e-9;
    
    public ShrinkGeometryCalculator(AreaData originalArea, List<AreaData.Vertex> shrinkVertices) {
        this.originalArea = originalArea;
        this.shrinkVertices = new ArrayList<>(shrinkVertices);
    }
    
    /**
     * 执行域名收缩计算
     * 完全按照提示词实现
     * @return 收缩后的域名数据，如果计算失败返回null
     */
    public AreaData shrinkArea() {
        try {
            System.out.println("=== 开始域名收缩计算 ===");
            
            // 1. 验证输入
            if (!validateInput()) {
                System.err.println("输入验证失败");
                return null;
            }
            
            // 2. 检测收缩顶点位置，分离内外部顶点
            if (!separateInternalExternalVertices()) {
                System.err.println("顶点分离失败");
                return null;
            }
            
            // 3. 计算线段与原域名边界的交叉点（边界点）
            if (!calculateBoundaryIntersections()) {
                System.err.println("边界交叉点计算失败");
                return null;
            }
            
            // 4. 计算临近点和最终边界点
            if (!calculateAdjacentAndBoundaryPoints()) {
                System.err.println("临近点和边界点计算失败");
                return null;
            }
            
            // 5. 构造最终的顶点列表（删除原域名顶点，插入新顶点）
            List<AreaData.Vertex> finalVertices = constructFinalVertices();
            if (finalVertices == null || finalVertices.size() < 3) {
                System.err.println("最终顶点列表构造失败");
                return null;
            }
            
            // 6. 检查并修复线段交叉
            finalVertices = fixCrossingsIfNeeded(finalVertices);
            if (finalVertices == null || finalVertices.size() < 3) {
                System.err.println("修复线段交叉失败");
                return null;
            }
            
            // 7. 重新计算二级顶点（边界框）
            List<AreaData.Vertex> secondVertices = calculateBoundingBox(finalVertices);
            
            // 8. 创建新的域名数据
            AreaData shrunkArea = createShrunkAreaData(finalVertices, secondVertices);
            
            System.out.println("=== 域名收缩计算完成 ===");
            System.out.println("最终顶点数量: " + finalVertices.size());
            
            return shrunkArea;
            
        } catch (Exception e) {
            System.err.println("域名收缩计算失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 步骤1: 验证输入数据
     */
    private boolean validateInput() {
        if (originalArea == null || originalArea.getVertices() == null || originalArea.getVertices().isEmpty()) {
            System.err.println("原域名数据无效");
            return false;
        }
        
        if (shrinkVertices == null || shrinkVertices.size() < 3) {
            System.err.println("收缩区域顶点数量不足（至少需要3个）");
            return false;
        }
        
        if (originalArea.getVertices().size() < 3) {
            System.err.println("原域名顶点数量不足");
            return false;
        }
        
        System.out.println("输入验证通过: 原域名顶点=" + originalArea.getVertices().size() + ", 收缩顶点=" + shrinkVertices.size());
        return true;
    }
    
    /**
     * 步骤2: 分离内外部顶点
     * 检测收缩顶点是否在原域名外，删除在原域名内的顶点
     */
    private boolean separateInternalExternalVertices() {
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        externalShrinkVertices.clear();
        
        System.out.println("开始检测收缩顶点位置...");
        
        for (int i = 0; i < shrinkVertices.size(); i++) {
            AreaData.Vertex vertex = shrinkVertices.get(i);
            boolean isInside = isPointInPolygon(vertex, originalVertices);
            boolean isOnBoundary = isPointOnPolygonBoundary(vertex, originalVertices);
            
            System.out.println("顶点 " + i + " (" + vertex.getX() + ", " + vertex.getZ() + "): " + 
                             (isInside ? "内部" : isOnBoundary ? "边界" : "外部"));
            
            // 在原域名外的点（包括边界上的点）保留
            if (!isInside || isOnBoundary) {
                externalShrinkVertices.add(vertex);
            }
        }
        
        System.out.println("外部/边界收缩顶点数量: " + externalShrinkVertices.size());
        
        // 如果所有顶点都在内部，无法收缩
        if (externalShrinkVertices.isEmpty()) {
            System.err.println("所有收缩顶点都在原域名内部，无法进行收缩");
            return false;
        }
        
        return true;
    }
    
    /**
     * 步骤3: 计算线段与原域名边界的交叉点
     * 检查收缩区域的线段是否跨越原域名边界
     */
    private boolean calculateBoundaryIntersections() {
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        boundaryPoints.clear();
        
        System.out.println("开始计算边界交叉点...");
        
        // 遍历收缩顶点的每条边
        for (int i = 0; i < shrinkVertices.size(); i++) {
            AreaData.Vertex current = shrinkVertices.get(i);
            AreaData.Vertex next = shrinkVertices.get((i + 1) % shrinkVertices.size());
            
            // 排除最开始点和最终点的线段（首尾相连的线段）
            if (i == shrinkVertices.size() - 1) {
                continue;
            }
            
            boolean currentInside = isPointInPolygon(current, originalVertices);
            boolean nextInside = isPointInPolygon(next, originalVertices);
            
            // 如果一个在内一个在外，说明线段跨越边界
            if (currentInside != nextInside) {
                System.out.println("检测到跨越边界的线段: " + i + " -> " + (i + 1));
                
                // 计算交点
                AreaData.Vertex intersection = findSegmentPolygonIntersection(current, next, originalVertices);
                if (intersection != null) {
                    boundaryPoints.add(intersection);
                    System.out.println("  交点: (" + intersection.getX() + ", " + intersection.getZ() + ")");
                }
            }
        }
        
        System.out.println("边界交叉点数量: " + boundaryPoints.size());
        return true;
    }
    
    /**
     * 步骤4: 计算临近点和最终边界点
     * 按照提示词：计算初始点和末尾点的临近点，再计算边界点
     */
    private boolean calculateAdjacentAndBoundaryPoints() {
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        
        System.out.println("开始计算临近点和边界点...");
        
        // 如果没有外部顶点，无法继续
        if (externalShrinkVertices.isEmpty()) {
            System.err.println("没有外部顶点");
            return false;
        }
        
        // 获取初始点和末尾点（外部顶点列表的首尾）
        AreaData.Vertex startPoint = externalShrinkVertices.get(0);
        AreaData.Vertex endPoint = externalShrinkVertices.get(externalShrinkVertices.size() - 1);
        
        System.out.println("初始点: (" + startPoint.getX() + ", " + startPoint.getZ() + ")");
        System.out.println("末尾点: (" + endPoint.getX() + ", " + endPoint.getZ() + ")");
        
        // 如果已经有边界交叉点，使用它们
        if (boundaryPoints.size() >= 2) {
            startBoundaryPoint = boundaryPoints.get(0);
            endBoundaryPoint = boundaryPoints.get(1);
        } else if (boundaryPoints.size() == 1) {
            // 只有一个边界点的情况
            startBoundaryPoint = boundaryPoints.get(0);
            endBoundaryPoint = findNearestBoundaryPoint(endPoint, originalVertices);
        } else {
            // 没有边界交叉点，需要计算临近点和边界点
            
            // 计算初始点的临近点（原域名上最近的顶点）
            AreaData.Vertex startAdjacentPoint = findNearestVertexInPolygon(startPoint, originalVertices);
            AreaData.Vertex endAdjacentPoint = findNearestVertexInPolygon(endPoint, originalVertices);
            
            System.out.println("初始点的临近点: (" + startAdjacentPoint.getX() + ", " + startAdjacentPoint.getZ() + ")");
            System.out.println("末尾点的临近点: (" + endAdjacentPoint.getX() + ", " + endAdjacentPoint.getZ() + ")");
            
            // 计算边界点（临近点最近的边界上的点）
            startBoundaryPoint = findNearestBoundaryPoint(startAdjacentPoint, originalVertices);
            endBoundaryPoint = findNearestBoundaryPoint(endAdjacentPoint, originalVertices);
            
            // 处理一个点对应多个边界点的情况（提示词要求取中位值）
            startBoundaryPoint = handleMultipleBoundaryPoints(startAdjacentPoint, originalVertices);
            endBoundaryPoint = handleMultipleBoundaryPoints(endAdjacentPoint, originalVertices);
        }
        
        if (startBoundaryPoint == null || endBoundaryPoint == null) {
            System.err.println("边界点计算失败");
            return false;
        }
        
        System.out.println("起始边界点: (" + startBoundaryPoint.getX() + ", " + startBoundaryPoint.getZ() + ")");
        System.out.println("结束边界点: (" + endBoundaryPoint.getX() + ", " + endBoundaryPoint.getZ() + ")");
        
        // 判断边界点在原域名哪两个顶点之间
        startBoundaryIndex = findBoundaryPointEdgeIndex(startBoundaryPoint, originalVertices);
        endBoundaryIndex = findBoundaryPointEdgeIndex(endBoundaryPoint, originalVertices);
        
        System.out.println("起始边界索引: " + startBoundaryIndex);
        System.out.println("结束边界索引: " + endBoundaryIndex);
        
        return startBoundaryIndex != -1 && endBoundaryIndex != -1;
    }
    
    /**
     * 步骤5: 构造最终的顶点列表
     * 删除原域名顶点，插入新顶点
     * 插入方式：原顶点 → 边界点1 → 新顶点 → 边界点2 → 原顶点
     */
    private List<AreaData.Vertex> constructFinalVertices() {
        List<AreaData.Vertex> finalVertices = new ArrayList<>();
        List<AreaData.Vertex> originalVertices = originalArea.getVertices();
        
        System.out.println("开始构造最终顶点列表...");
        
        // 确保 startBoundaryIndex < endBoundaryIndex
        int idx1 = startBoundaryIndex;
        int idx2 = endBoundaryIndex;
        AreaData.Vertex bp1 = startBoundaryPoint;
        AreaData.Vertex bp2 = endBoundaryPoint;
        
        if (idx1 > idx2) {
            int temp = idx1;
            idx1 = idx2;
            idx2 = temp;
            AreaData.Vertex tempVertex = bp1;
            bp1 = bp2;
            bp2 = tempVertex;
        }
        
        // 计算两个边界点之间的距离（两种路径）
        int shortPath = idx2 - idx1;
        int longPath = originalVertices.size() - shortPath;
        
        System.out.println("短路径距离: " + shortPath + ", 长路径距离: " + longPath);
        
        // 删除两个边界点之间较短路径的点
        if (shortPath <= longPath) {
            // 保留 [0, idx1] 的原顶点
            for (int i = 0; i <= idx1; i++) {
                finalVertices.add(originalVertices.get(i));
            }
            // 添加第一个边界点
            finalVertices.add(bp1);
            // 判断新顶点的插入方式（正向或反向）
            List<AreaData.Vertex> orderedNewVertices = determineVertexOrder(externalShrinkVertices, bp1, bp2);
            // 添加新顶点
            finalVertices.addAll(orderedNewVertices);
            // 添加第二个边界点
            finalVertices.add(bp2);
            // 保留 [idx2+1, end] 的原顶点
            for (int i = idx2 + 1; i < originalVertices.size(); i++) {
                finalVertices.add(originalVertices.get(i));
            }
        } else {
            // 保留较长路径
            // 保留 [idx2+1, end] 和 [0, idx1] 的原顶点
            for (int i = idx2 + 1; i < originalVertices.size(); i++) {
                finalVertices.add(originalVertices.get(i));
            }
            for (int i = 0; i <= idx1; i++) {
                finalVertices.add(originalVertices.get(i));
            }
            // 添加第一个边界点
            finalVertices.add(bp1);
            // 判断新顶点的插入方式
            List<AreaData.Vertex> orderedNewVertices = determineVertexOrder(externalShrinkVertices, bp1, bp2);
            // 添加新顶点
            finalVertices.addAll(orderedNewVertices);
            // 添加第二个边界点
            finalVertices.add(bp2);
        }
        
        System.out.println("最终顶点列表构造完成，顶点数: " + finalVertices.size());
        return finalVertices;
    }
    
    /**
     * 步骤6: 检查并修复线段交叉
     * 如果有交叉，反转新顶点的顺序
     */
    private List<AreaData.Vertex> fixCrossingsIfNeeded(List<AreaData.Vertex> vertices) {
        if (vertices.size() < 4) {
            return vertices; // 少于4个顶点不会有交叉
        }
        
        System.out.println("开始检查线段交叉...");
        
        // 检查是否有线段交叉
        if (hasCrossing(vertices)) {
            System.out.println("检测到线段交叉，尝试反转新顶点顺序...");
            
            // 反转新顶点的顺序
            List<AreaData.Vertex> reversedNewVertices = new ArrayList<>();
            for (int i = externalShrinkVertices.size() - 1; i >= 0; i--) {
                reversedNewVertices.add(externalShrinkVertices.get(i));
            }
            externalShrinkVertices = reversedNewVertices;
            
            // 重新构造顶点列表
            vertices = constructFinalVertices();
            
            // 再次检查
            if (hasCrossing(vertices)) {
                System.err.println("反转后仍然有交叉，可能存在几何问题");
            } else {
                System.out.println("反转后交叉问题已解决");
            }
        } else {
            System.out.println("没有检测到线段交叉");
        }
        
        return vertices;
    }
    
    /**
     * 创建收缩后的域名数据
     */
    private AreaData createShrunkAreaData(List<AreaData.Vertex> finalVertices, List<AreaData.Vertex> secondVertices) {
        // 计算收缩区域的高度范围
        AreaData.AltitudeData mergedAltitude = calculateMergedAltitude(finalVertices);
        
        // 使用原域名的基本信息，但更新顶点列表和高度
        AreaData shrunkArea = new AreaData(
            originalArea.getName(),        // 保持原名称
            finalVertices,                 // 新的顶点列表
            secondVertices,                // 新的边界框
            mergedAltitude,                // 合并后的高度设置
            originalArea.getLevel(),       // 保持原等级
            originalArea.getBaseName(),    // 保持原basename
            originalArea.getSignature(),   // 保持原签名
            originalArea.getColor(),       // 保持原颜色
            originalArea.getSurfacename()  // 保持原surfacename
        );
        
        return shrunkArea;
    }
    
    /**
     * 计算合并后的高度信息
     * 合并收缩区域的高度与原区域的高度：
     * - 最高高度：取两个区域中更高的那一个（null表示无限制，比任何数值都大）
     * - 最低高度：取两个区域中更低的那一个（null表示无限制，比任何数值都小）
     */
    private AreaData.AltitudeData calculateMergedAltitude(List<AreaData.Vertex> finalVertices) {
        AreaData.AltitudeData originalAltitude = originalArea.getAltitude();
        
        if (originalAltitude == null) {
            // 如果原域名没有高度信息，使用收缩区域的高度
            return calculateShrinkAreaAltitude(finalVertices);
        }
        
        // 检查原始高度数据是否完整（不为null）
        Double originalMin = originalAltitude.getMin();
        Double originalMax = originalAltitude.getMax();
        
        // 计算收缩区域的高度范围
        AreaData.AltitudeData shrinkAltitude = calculateShrinkAreaAltitude(finalVertices);
        if (shrinkAltitude == null) {
            // 如果无法计算收缩区域高度，保持原高度
            return originalAltitude;
        }
        
        // 处理null高度的情况（null表示无限制）
        Double mergedMin;
        Double mergedMax;
        
        if (originalMin == null) {
            // 原域名最低高度无限制，合并后也应该无限制
            mergedMin = null;
        } else if (shrinkAltitude.getMin() == null) {
            // 收缩区域最低高度无限制，合并后也应该无限制
            mergedMin = null;
        } else {
            // 两个区域都有具体的最低高度，取更低的
            mergedMin = Math.min(originalMin, shrinkAltitude.getMin());
        }
        
        if (originalMax == null) {
            // 原域名最高高度无限制，合并后也应该无限制
            mergedMax = null;
        } else if (shrinkAltitude.getMax() == null) {
            // 收缩区域最高高度无限制，合并后也应该无限制
            mergedMax = null;
        } else {
            // 两个区域都有具体的最高高度，取更高的
            mergedMax = Math.max(originalMax, shrinkAltitude.getMax());
        }
        
        return new AreaData.AltitudeData(mergedMax, mergedMin);
    }
    
    /**
     * 计算收缩区域的高度范围
     * 这里简化处理，使用原域名的高度设置
     * 在实际应用中，可以根据收缩区域的顶点位置计算实际高度
     */
    private AreaData.AltitudeData calculateShrinkAreaAltitude(List<AreaData.Vertex> finalVertices) {
        // 简化实现：直接使用原域名的高度设置
        // 在实际应用中，这里可以根据finalVertices的位置计算实际的高度范围
        return originalArea.getAltitude();
    }
    
    // ==================== 辅助工具方法 ====================
    
    /**
     * 计算边界框（二级顶点）
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
     * 判断点是否在多边形边界上
     */
    private boolean isPointOnPolygonBoundary(AreaData.Vertex point, List<AreaData.Vertex> polygon) {
        for (int i = 0; i < polygon.size(); i++) {
            AreaData.Vertex v1 = polygon.get(i);
            AreaData.Vertex v2 = polygon.get((i + 1) % polygon.size());
            
            if (isPointOnSegment(point, v1, v2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断点是否在线段上
     */
    private boolean isPointOnSegment(AreaData.Vertex point, AreaData.Vertex segStart, AreaData.Vertex segEnd) {
        double dist = distanceToLineSegment(point, segStart, segEnd);
        return dist < EPSILON;
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
     * 找到线段与多边形边界的交点
     */
    private AreaData.Vertex findSegmentPolygonIntersection(AreaData.Vertex p1, AreaData.Vertex p2, List<AreaData.Vertex> polygon) {
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
     * 找到距离指定点最近的原域名顶点（临近点）
     */
    private AreaData.Vertex findNearestVertexInPolygon(AreaData.Vertex point, List<AreaData.Vertex> polygon) {
        AreaData.Vertex nearest = polygon.get(0);
        double minDistance = distance(point, nearest);
        
        for (AreaData.Vertex vertex : polygon) {
            double dist = distance(point, vertex);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = vertex;
            }
        }
        
        return nearest;
    }
    
    /**
     * 找到距离指定点最近的边界上的点
     */
    private AreaData.Vertex findNearestBoundaryPoint(AreaData.Vertex point, List<AreaData.Vertex> polygon) {
        AreaData.Vertex nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < polygon.size(); i++) {
            AreaData.Vertex v1 = polygon.get(i);
            AreaData.Vertex v2 = polygon.get((i + 1) % polygon.size());
            
            AreaData.Vertex closestOnSegment = getClosestPointOnSegment(point, v1, v2);
            double dist = distance(point, closestOnSegment);
            
            if (dist < minDistance) {
                minDistance = dist;
                nearestPoint = closestOnSegment;
            }
        }
        
        return nearestPoint;
    }
    
    /**
     * 处理一个点对应多个边界点的情况
     * 按照提示词：取中位值，再计算与边界的交点
     */
    private AreaData.Vertex handleMultipleBoundaryPoints(AreaData.Vertex adjacentPoint, List<AreaData.Vertex> polygon) {
        // 找到所有距离最近的边界点
        List<AreaData.Vertex> nearestBoundaryPoints = new ArrayList<>();
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < polygon.size(); i++) {
            AreaData.Vertex v1 = polygon.get(i);
            AreaData.Vertex v2 = polygon.get((i + 1) % polygon.size());
            
            AreaData.Vertex closestOnSegment = getClosestPointOnSegment(adjacentPoint, v1, v2);
            double dist = distance(adjacentPoint, closestOnSegment);
            
            if (dist < minDistance) {
                minDistance = dist;
                nearestBoundaryPoints.clear();
                nearestBoundaryPoints.add(closestOnSegment);
            } else if (Math.abs(dist - minDistance) < EPSILON) {
                nearestBoundaryPoints.add(closestOnSegment);
            }
        }
        
        // 如果只有一个，直接返回
        if (nearestBoundaryPoints.size() == 1) {
            return nearestBoundaryPoints.get(0);
        }
        
        // 如果有多个，取中位值
        double avgX = 0, avgZ = 0;
        for (AreaData.Vertex pt : nearestBoundaryPoints) {
            avgX += pt.getX();
            avgZ += pt.getZ();
        }
        avgX /= nearestBoundaryPoints.size();
        avgZ /= nearestBoundaryPoints.size();
        
        AreaData.Vertex midPoint = new AreaData.Vertex(avgX, avgZ);
        
        // 计算从临近点到中位值的直线与边界的交点
        AreaData.Vertex intersection = findSegmentPolygonIntersection(adjacentPoint, midPoint, polygon);
        if (intersection != null) {
            return intersection;
        }
        
        // 如果没有交点，返回最近的边界点
        return nearestBoundaryPoints.get(0);
    }
    
    /**
     * 判断边界点在原域名哪两个顶点之间（返回边的起始索引）
     */
    private int findBoundaryPointEdgeIndex(AreaData.Vertex boundaryPoint, List<AreaData.Vertex> polygon) {
        double minDistance = Double.MAX_VALUE;
        int edgeIndex = -1;
        
        for (int i = 0; i < polygon.size(); i++) {
            AreaData.Vertex v1 = polygon.get(i);
            AreaData.Vertex v2 = polygon.get((i + 1) % polygon.size());
            
            double dist = distanceToLineSegment(boundaryPoint, v1, v2);
            if (dist < minDistance) {
                minDistance = dist;
                edgeIndex = i;
            }
        }
        
        return edgeIndex;
    }
    
    /**
     * 判断新顶点的插入顺序（正向或反向）
     */
    private List<AreaData.Vertex> determineVertexOrder(List<AreaData.Vertex> vertices, 
                                                       AreaData.Vertex boundaryStart, 
                                                       AreaData.Vertex boundaryEnd) {
        // 简单实现：检查第一个顶点离哪个边界点更近
        if (vertices.isEmpty()) {
            return new ArrayList<>(vertices);
        }
        
        double distToStart = distance(vertices.get(0), boundaryStart);
        double distToEnd = distance(vertices.get(0), boundaryEnd);
        
        // 如果第一个顶点离起始边界点更近，保持正向
        if (distToStart < distToEnd) {
            return new ArrayList<>(vertices);
        } else {
            // 否则反向
            List<AreaData.Vertex> reversed = new ArrayList<>();
            for (int i = vertices.size() - 1; i >= 0; i--) {
                reversed.add(vertices.get(i));
            }
            return reversed;
        }
    }
    
    /**
     * 检查多边形是否有线段交叉
     */
    private boolean hasCrossing(List<AreaData.Vertex> vertices) {
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 2; j < n; j++) {
                // 跳过相邻的线段
                if (j == n - 1 && i == 0) continue;
                
                AreaData.Vertex p1 = vertices.get(i);
                AreaData.Vertex p2 = vertices.get((i + 1) % n);
                AreaData.Vertex p3 = vertices.get(j);
                AreaData.Vertex p4 = vertices.get((j + 1) % n);
                
                if (findLineIntersection(p1, p2, p3, p4) != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取点到线段的最近点
     */
    private AreaData.Vertex getClosestPointOnSegment(AreaData.Vertex point, AreaData.Vertex segStart, AreaData.Vertex segEnd) {
        double px = point.getX(), py = point.getZ();
        double ax = segStart.getX(), ay = segStart.getZ();
        double bx = segEnd.getX(), by = segEnd.getZ();
        
        double dx = bx - ax;
        double dy = by - ay;
        
        if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
            return new AreaData.Vertex(ax, ay);
        }
        
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        
        return new AreaData.Vertex(ax + t * dx, ay + t * dy);
    }
    
    /**
     * 计算点到线段的距离
     */
    private double distanceToLineSegment(AreaData.Vertex point, AreaData.Vertex lineStart, AreaData.Vertex lineEnd) {
        AreaData.Vertex closest = getClosestPointOnSegment(point, lineStart, lineEnd);
        return distance(point, closest);
    }
    
    /**
     * 计算两点之间的距离
     */
    private double distance(AreaData.Vertex p1, AreaData.Vertex p2) {
        double dx = p1.getX() - p2.getX();
        double dz = p1.getZ() - p2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
} 