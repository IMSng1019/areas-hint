package areahint.expandarea;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.expandarea.ExpandAreaClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

/**
 * 域名扩展管理器
 * 按照提示词实现复杂的几何算法来处理域名扩展
 */
public class ExpandAreaManager {
    private static ExpandAreaManager instance;
    private MinecraftClient client;
    private String selectedAreaName;
    private AreaData selectedArea;
    private List<Double[]> newVertices;
    private boolean isRecording = false;
    private boolean isActive = false;
    private ExpandAreaUI ui;
    
    public static ExpandAreaManager getInstance() {
        if (instance == null) {
            instance = new ExpandAreaManager();
        }
        return instance;
    }
    
    private ExpandAreaManager() {
        this.client = MinecraftClient.getInstance();
        this.newVertices = new ArrayList<>();
        this.ui = new ExpandAreaUI(this);
    }
    
    /**
     * 开始域名扩展流程
     */
    public void startExpandArea() {
        System.out.println("DEBUG: startExpandArea() 被调用");
        if (client.player == null) {
            System.out.println("DEBUG: client.player 为 null");
            return;
        }
        
        System.out.println("DEBUG: 设置 isActive = true");
        isActive = true;  // 设置为活动状态
        
        // 获取可修改的域名列表
        System.out.println("DEBUG: 开始获取可修改的域名列表");
        List<AreaData> modifiableAreas = getModifiableAreas();
        System.out.println("DEBUG: 找到 " + modifiableAreas.size() + " 个可修改的域名");
        
        if (modifiableAreas.isEmpty()) {
            System.out.println("DEBUG: 没有可扩展的域名");
            sendMessage("§c没有可扩展的域名", Formatting.RED);
            sendMessage("§7您只能扩展自己创建的域名", Formatting.GRAY);
            isActive = false;
            return;
        }
        
        // 直接显示域名选择界面
        System.out.println("DEBUG: 显示域名选择界面");
        ui.showAreaSelection(modifiableAreas);
        System.out.println("DEBUG: startExpandArea() 执行完成");
    }
    
    /**
     * 检查玩家是否有权限修改指定域名
     */
    private boolean checkPermission(AreaData area) {
        if (client.player == null) {
            return false;
        }
        
        String playerName = client.player.getGameProfile().getName();
        
        // 检查是否为管理员（权限等级2）
        if (client.player.hasPermissionLevel(2)) {
            return true;
        }
        
        // 检查是否为域名的basename引用玩家
        if (area.getBaseName() != null) {
            AreaData baseArea = findAreaByName(area.getBaseName());
            if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                return true;
            }
        }
        
        // 检查是否为域名创建者
        return playerName.equals(area.getSignature());
    }
    
    /**
     * 根据域名名称选择域名
     */
    public void selectAreaByName(String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            sendMessage("§c无效的域名", Formatting.RED);
            return;
        }
        
        // 查找域名
        AreaData area = findAreaByName(areaName.trim());
        if (area == null) {
            sendMessage("§c域名 '" + areaName + "' 不存在", Formatting.RED);
            return;
        }
        
        // 检查权限
        if (!checkPermission(area)) {
            sendMessage("§c您没有权限扩展此域名", Formatting.RED);
            return;
        }
        
        // 选择该域名
        handleAreaSelection(area);
    }
    
    /**
     * 取消扩展流程
     */
    public void cancel() {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        isRecording = false;
        selectedArea = null;
        selectedAreaName = null;
        newVertices.clear();
        
        ui.showCancelMessage();
    }
    
    /**
     * 获取可修改的域名列表
     */
    private List<AreaData> getModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        String playerName = client.player.getGameProfile().getName();
        boolean isAdmin = client.player.hasPermissionLevel(2);
        
        System.out.println("DEBUG: 玩家名称: " + playerName + ", 是否管理员: " + isAdmin);
        
        // 获取所有域名
        List<AreaData> allAreas = loadAllAreas();
        
        for (AreaData area : allAreas) {
            System.out.println("DEBUG: 检查域名: " + area.getName() + ", 签名: " + area.getSignature() + ", 基础域名: " + area.getBaseName());
            if (isAdmin) {
                // 管理员可以修改所有域名
                result.add(area);
                System.out.println("DEBUG: 管理员权限，添加域名: " + area.getName());
            } else {
                // 普通玩家只能修改自己创建的或basename引用自己的域名
                if (playerName.equals(area.getSignature())) {
                    result.add(area);
                    System.out.println("DEBUG: 玩家创建的域名，添加: " + area.getName());
                } else if (area.getBaseName() != null) {
                    AreaData baseArea = findAreaByName(area.getBaseName());
                    if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                        result.add(area);
                        System.out.println("DEBUG: 基于玩家域名的扩展，添加: " + area.getName());
                    }
                }
            }
        }
        
        System.out.println("DEBUG: 找到 " + result.size() + " 个可修改的域名");
        return result;
    }
    
    /**
     * 处理域名选择
     */
    public void handleAreaSelection(AreaData selectedArea) {
        this.selectedArea = selectedArea;
        this.selectedAreaName = selectedArea.getName();
        
        sendMessage("§a已选择域名: " + areahint.util.AreaDataConverter.getDisplayName(selectedArea), Formatting.GREEN);
        sendMessage("§e请按 §6X §e键开始记录新区域的顶点位置", Formatting.YELLOW);
        sendMessage("§7记录完成后点击 §6[保存域名] §7按钮完成扩展", Formatting.GRAY);
        
        // 开始记录模式
        startRecording();
    }
    
    /**
     * 开始记录新顶点
     */
    private void startRecording() {
        this.isActive = true;
        this.isRecording = true;
        this.newVertices.clear();
        ui.showRecordingInterface();
    }
    
    /**
     * 记录当前位置作为新顶点
     */
    public void recordCurrentPosition() {
        if (!isRecording || client.player == null) {
            return;
        }
        
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        
        newVertices.add(new Double[]{x, z});
        
        sendMessage("§a已记录位置 #" + newVertices.size() + ": §6(" + 
                   String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + String.format("%.1f", z) + ")", 
                   Formatting.GREEN);
        
        // 显示选项按钮（参考EasyAdd的实现）
        ui.showPointRecordedOptions(newVertices.size());
    }
    
    /**
     * 完成记录并处理扩展
     */
    public void finishRecording() {
        if (!isRecording || newVertices.size() < 3) {
            sendMessage("§c至少需要记录3个顶点才能形成有效区域", Formatting.RED);
            return;
        }
        
        this.isRecording = false;
        
        try {
            // 进行几何计算和域名扩展
            processAreaExpansion();
        } catch (Exception e) {
            sendMessage("§c扩展域名时发生错误: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }
    
    /**
     * 处理域名扩展的核心逻辑
     * 按照提示词实现复杂的几何算法
     */
    private void processAreaExpansion() {
        sendMessage("§e正在处理域名扩展...", Formatting.YELLOW);
        
        try {
            // 1. 高度验证 - 按照提示词逻辑
            if (!validateHeightsAccordingToPrompt()) {
                return;
            }
            
            // 2. 提取原域名顶点
            List<Double[]> originalVertices = extractOriginalVertices();
            
            // 3. 计算新添加区域的高度范围
            double[] newAreaHeightRange = calculateNewAreaHeightRange();
            
            // 4. 检查新添加顶点是否在原域名内，删除内部顶点
            List<Double[]> externalVertices = filterExternalVertices(originalVertices);
            
            if (externalVertices.isEmpty()) {
                sendMessage("§c所有新添加的顶点都在原域名内部，无法扩展", Formatting.RED);
                return;
            }
            
            // 5. 计算边界点 - 按照提示词的复杂算法
            // List<Double[]> boundaryPoints = calculateBoundaryPoints(originalVertices, externalVertices);
            
            // 6. 计算临近点和边界点
            List<Double[]> adjacentPoints = calculateAdjacentPoints(originalVertices, externalVertices);
            List<Double[]> finalBoundaryPoints = calculateFinalBoundaryPoints(adjacentPoints, originalVertices);
            
            // 7. 合并顶点并排序
            List<Double[]> combinedVertices = combineVertices(originalVertices, externalVertices, finalBoundaryPoints);
            
            // 8. 检测并修复交叉
            List<Double[]> fixedVertices = fixCrossings(combinedVertices);
            
            // 9. 重新计算二级顶点
            List<Double[]> secondVertices = calculateSecondVertices(fixedVertices);
            
            // 10. 更新高度信息
            AreaData.AltitudeData updatedAltitude = updateAltitudeData(newAreaHeightRange);
            
            // 11. 创建扩展后的域名
            AreaData expandedArea = createExpandedArea(fixedVertices, secondVertices, updatedAltitude);
            
            // 12. 发送给服务端
            ExpandAreaClientNetworking.sendExpandedAreaToServer(expandedArea);
            
            sendMessage("§a域名扩展完成！", Formatting.GREEN);
            
        } catch (Exception e) {
            sendMessage("§c域名扩展过程中发生错误: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        } finally {
            // 重置状态
            reset();
        }
    }
    
    /**
     * 按照提示词验证新添加区域的高度
     * 若新添加的区域的最高高度比原域名的最高高度小最低高度比原域名的最低高度高则不该变域名的高度
     * 反之则将报错
     */
    private boolean validateHeightsAccordingToPrompt() {
        if (client.player == null || selectedArea.getAltitude() == null) {
            return true; // 如果没有高度限制则通过
        }
        
        // 计算新添加区域的高度范围
        double[] newAreaHeightRange = calculateNewAreaHeightRange();
        double newMinHeight = newAreaHeightRange[0];
        double newMaxHeight = newAreaHeightRange[1];
        
        AreaData.AltitudeData altitude = selectedArea.getAltitude();
        if (altitude != null && altitude.getMax() != null && altitude.getMin() != null) {
            double originalMaxHeight = altitude.getMax();
            double originalMinHeight = altitude.getMin();
            
            // 按照提示词逻辑：新区域最高高度 < 原域名最高高度 且 新区域最低高度 > 原域名最低高度
            if (newMaxHeight < originalMaxHeight && newMinHeight > originalMinHeight) {
                // 不改变域名的高度
                sendMessage("§a新区域高度在原域名范围内，保持原高度设置", Formatting.GREEN);
                return true;
            } else {
                // 反之则将报错
                sendMessage("§c新区域高度超出原域名范围，无法扩展", Formatting.RED);
                sendMessage("§c原域名高度范围: " + originalMinHeight + " ~ " + originalMaxHeight, Formatting.RED);
                sendMessage("§c新区域高度范围: " + newMinHeight + " ~ " + newMaxHeight, Formatting.RED);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 计算新添加区域的高度范围
     */
    private double[] calculateNewAreaHeightRange() {
        if (client.player == null) {
            return new double[]{64.0, 64.0}; // 默认高度
        }
        
        double playerY = client.player.getY();
        // 给新区域一个合理的高度范围（上下各10格）
        return new double[]{playerY - 10.0, playerY + 10.0};
    }
    
    /**
     * 提取原域名的顶点坐标
     */
    private List<Double[]> extractOriginalVertices() {
        List<Double[]> vertices = new ArrayList<>();
        List<AreaData.Vertex> verticesList = selectedArea.getVertices();
        
        for (AreaData.Vertex vertex : verticesList) {
            vertices.add(new Double[]{vertex.getX(), vertex.getZ()});
        }
        
        return vertices;
    }
    
    /**
     * 过滤外部顶点 - 删除在原域名内部的顶点
     */
    private List<Double[]> filterExternalVertices(List<Double[]> originalVertices) {
        List<Double[]> externalVertices = new ArrayList<>();
        
        for (Double[] vertex : newVertices) {
            if (!isPointInPolygon(vertex, originalVertices)) {
                externalVertices.add(vertex);
            }
        }
        
        return externalVertices;
    }
    
    /**
     * 计算边界点 - 按照提示词的复杂算法
     */
    private List<Double[]> calculateBoundaryPoints(List<Double[]> originalVertices, List<Double[]> externalVertices) {
        List<Double[]> boundaryPoints = new ArrayList<>();
        
        // 计算新添加顶点与原域名边界的交叉点
        for (int i = 0; i < externalVertices.size(); i++) {
            int nextIndex = (i + 1) % externalVertices.size();
            Double[] currentVertex = externalVertices.get(i);
            Double[] nextVertex = externalVertices.get(nextIndex);
            
            // 计算这条线段与原域名边界的交叉点
            List<Double[]> intersections = findLinePolygonIntersections(currentVertex, nextVertex, originalVertices);
            boundaryPoints.addAll(intersections);
        }
        
        return boundaryPoints;
    }
    
    /**
     * 计算临近点 - 找到距离原域名边界最近的点
     */
    private List<Double[]> calculateAdjacentPoints(List<Double[]> originalVertices, List<Double[]> externalVertices) {
        List<Double[]> adjacentPoints = new ArrayList<>();
        
        for (Double[] vertex : externalVertices) {
            // 找到距离这个点最近的原域名边界点
            Double[] nearestPoint = findNearestPointOnPolygon(vertex, originalVertices);
            if (nearestPoint != null) {
                adjacentPoints.add(nearestPoint);
            }
        }
        
        return adjacentPoints;
    }
    
    /**
     * 计算最终边界点 - 处理一个点对应多个边界点的情况
     */
    private List<Double[]> calculateFinalBoundaryPoints(List<Double[]> adjacentPoints, List<Double[]> originalVertices) {
        List<Double[]> finalBoundaryPoints = new ArrayList<>();
        
        // 对于每个临近点，找到对应的边界点
        for (Double[] adjacentPoint : adjacentPoints) {
            // 如果这个点有多个边界点，取中位值
            List<Double[]> boundaryPointsForAdjacent = findBoundaryPointsForAdjacent(adjacentPoint, originalVertices);
            
            if (boundaryPointsForAdjacent.size() == 1) {
                finalBoundaryPoints.add(boundaryPointsForAdjacent.get(0));
            } else if (boundaryPointsForAdjacent.size() > 1) {
                // 计算中位值
                Double[] medianPoint = calculateMedianPoint(boundaryPointsForAdjacent);
                // 计算与边界的交点
                Double[] intersectionPoint = findIntersectionWithBoundary(adjacentPoint, medianPoint, originalVertices);
                if (intersectionPoint != null) {
                    finalBoundaryPoints.add(intersectionPoint);
                }
            }
        }
        
        return finalBoundaryPoints;
    }
    
    /**
     * 合并顶点并排序
     */
    private List<Double[]> combineVertices(List<Double[]> originalVertices, List<Double[]> externalVertices, List<Double[]> boundaryPoints) {
        List<Double[]> allVertices = new ArrayList<>();
        allVertices.addAll(originalVertices);
        allVertices.addAll(externalVertices);
        allVertices.addAll(boundaryPoints);
        
        // 移除重复点
        allVertices = removeDuplicatePoints(allVertices);
        
        // 按照提示词：原顶点 边界点 新顶点 边界点 原顶点 的顺序排列
        return sortVerticesForExpansion(originalVertices, externalVertices, boundaryPoints);
    }
    
    /**
     * 检测并修复交叉
     */
    private List<Double[]> fixCrossings(List<Double[]> vertices) {
        List<Double[]> fixedVertices = new ArrayList<>(vertices);
        
        // 检测新添加区域的顶点顺序是否正确
        boolean hasCrossing = detectCrossings(fixedVertices);
        
        if (hasCrossing) {
            // 如果新添加入的区域的一级顶点与原有区域的顶点所连接成的线段有交叉
            // 则证明新添加入的区域的顶点的顺序反了，重新排列
            fixedVertices = reverseNewVerticesOrder(fixedVertices);
        }
        
        return fixedVertices;
    }
    
    /**
     * 重新计算二级顶点（AABB边界框）
     */
    private List<Double[]> calculateSecondVertices(List<Double[]> vertices) {
        if (vertices.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 计算边界框
        double minX = vertices.get(0)[0];
        double maxX = vertices.get(0)[0];
        double minZ = vertices.get(0)[1];
        double maxZ = vertices.get(0)[1];
        
        for (Double[] vertex : vertices) {
            minX = Math.min(minX, vertex[0]);
            maxX = Math.max(maxX, vertex[0]);
            minZ = Math.min(minZ, vertex[1]);
            maxZ = Math.max(maxZ, vertex[1]);
        }
        
        // 创建AABB四个角点
        List<Double[]> secondVertices = new ArrayList<>();
        secondVertices.add(new Double[]{minX, minZ}); // 左下
        secondVertices.add(new Double[]{maxX, minZ}); // 右下
        secondVertices.add(new Double[]{maxX, maxZ}); // 右上
        secondVertices.add(new Double[]{minX, maxZ}); // 左上
        
        return secondVertices;
    }
    
    /**
     * 更新高度信息
     */
    private AreaData.AltitudeData updateAltitudeData(double[] newAreaHeightRange) {
        AreaData.AltitudeData originalAltitude = selectedArea.getAltitude();
        
        if (originalAltitude == null) {
            return new AreaData.AltitudeData(newAreaHeightRange[1], newAreaHeightRange[0]);
        }
        
        // 检查原始高度数据是否完整（不为null）
        Double originalMin = originalAltitude.getMin();
        Double originalMax = originalAltitude.getMax();
        
        if (originalMin == null || originalMax == null) {
            // 如果原始高度数据不完整，使用新区域的高度数据
            return new AreaData.AltitudeData(newAreaHeightRange[1], newAreaHeightRange[0]);
        }
        
        // 如果新区域高度在原范围内，保持原高度
        if (newAreaHeightRange[1] < originalMax && newAreaHeightRange[0] > originalMin) {
            return originalAltitude;
        }
        
        // 否则扩展高度范围
        double newMin = Math.min(originalMin, newAreaHeightRange[0]);
        double newMax = Math.max(originalMax, newAreaHeightRange[1]);
        
        return new AreaData.AltitudeData(newMax, newMin);
    }
    
    /**
     * 创建扩展后的域名
     */
    private AreaData createExpandedArea(List<Double[]> vertices, List<Double[]> secondVertices, AreaData.AltitudeData altitude) {
        return new AreaData(
            selectedArea.getName(),
            convertToVertexList(vertices),
            convertToVertexList(secondVertices),
            altitude,
            selectedArea.getLevel(),
            selectedArea.getBaseName(),
            selectedArea.getSignature(),
            selectedArea.getColor(),
            selectedArea.getSurfacename()
        );
    }
    
    /**
     * 重置管理器状态
     */
    public void reset() {
        this.selectedAreaName = null;
        this.selectedArea = null;
        this.newVertices.clear();
        this.isRecording = false;
        this.isActive = false;  // 重置活动状态
    }
    
    /**
     * 继续记录更多顶点
     */
    public void continueRecording() {
        if (!isRecording || client.player == null) {
            return;
        }
        
        sendMessage("§a继续记录更多顶点，按 §6X §a记录当前位置", Formatting.GREEN);
    }
    
    /**
     * 完成记录并保存域名扩展
     */
    public void finishAndSave() {
        if (!isRecording || client.player == null) {
            return;
        }
        
        if (newVertices.size() < 3) {
            sendMessage("§c至少需要记录3个点才能形成有效区域", Formatting.RED);
            return;
        }
        
        // 停止记录模式
        this.isRecording = false;
        
        // 使用现有的处理逻辑
        try {
            processAreaExpansion();
        } catch (Exception e) {
            sendMessage("§c扩展域名时发生错误: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }
    
    /**
     * 检查是否处于活动状态
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 检查是否处于记录状态
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * 停止扩展流程
     */
    public void stopExpand() {
        isActive = false;
        isRecording = false;
        selectedArea = null;
        selectedAreaName = null;
        newVertices.clear();
    }
    
    /**
     * 根据名称查找域名
     */
    private AreaData findAreaByName(String name) {
        List<AreaData> areas = loadAllAreas();
        for (AreaData area : areas) {
            if (area.getName().equals(name)) {
                return area;
            }
        }
        return null;
    }
    
    /**
     * 加载当前维度的所有域名
     */
    private List<AreaData> loadAllAreas() {
        List<AreaData> areas = new ArrayList<>();
        try {
            // 获取当前维度
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
                String currentDimension = client.world.getRegistryKey().getValue().toString();
                String fileName = getFileNameForCurrentDimension(currentDimension);
                
                if (fileName != null) {
                    Path areaPath = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
                    System.out.println("DEBUG: 尝试加载域名文件: " + areaPath);
                    
                    if (areaPath.toFile().exists()) {
                        areas = FileManager.readAreaData(areaPath);
                        System.out.println("DEBUG: 从 " + fileName + " 加载了 " + areas.size() + " 个域名");
                    } else {
                        System.out.println("DEBUG: 文件不存在: " + fileName);
                    }
                } else {
                    System.out.println("DEBUG: 无法确定当前维度的文件名");
                }
            } else {
                System.out.println("DEBUG: 无法获取当前世界信息");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: 加载域名文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        return areas;
    }
    
    /**
     * 根据维度ID获取对应的文件名
     */
    private String getFileNameForCurrentDimension(String dimensionId) {
        if (dimensionId == null) return null;
        
        if (dimensionId.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (dimensionId.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (dimensionId.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return null;
    }
    
    /**
     * 发送消息给玩家
     */
    private void sendMessage(String message, Formatting formatting) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(formatting), false);
        }
    }
    
    // ==================== 辅助方法实现 ====================
    
    /**
     * 判断点是否在多边形内部（射线法）
     */
    private boolean isPointInPolygon(Double[] point, List<Double[]> polygon) {
        if (polygon.size() < 3) return false;
        
        double x = point[0];
        double y = point[1];
        boolean inside = false;
        
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double xi = polygon.get(i)[0];
            double yi = polygon.get(i)[1];
            double xj = polygon.get(j)[0];
            double yj = polygon.get(j)[1];
            
            if (((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    /**
     * 计算线段与多边形的交点
     */
    private List<Double[]> findLinePolygonIntersections(Double[] lineStart, Double[] lineEnd, List<Double[]> polygon) {
        List<Double[]> intersections = new ArrayList<>();
        
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            Double[] polyStart = polygon.get(i);
            Double[] polyEnd = polygon.get(j);
            
            Double[] intersection = getLineIntersection(lineStart, lineEnd, polyStart, polyEnd);
            if (intersection != null) {
                intersections.add(intersection);
            }
        }
        
        return intersections;
    }
    
    /**
     * 计算两条线段的交点
     */
    private Double[] getLineIntersection(Double[] line1Start, Double[] line1End, Double[] line2Start, Double[] line2End) {
        double x1 = line1Start[0], y1 = line1Start[1];
        double x2 = line1End[0], y2 = line1End[1];
        double x3 = line2Start[0], y3 = line2Start[1];
        double x4 = line2End[0], y4 = line2End[1];
        
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-10) {
            return null; // 平行线
        }
        
        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;
        double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom;
        
        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            return new Double[]{x1 + t * (x2 - x1), y1 + t * (y2 - y1)};
        }
        
        return null;
    }
    
    /**
     * 找到距离指定点最近的多边形边界点
     */
    private Double[] findNearestPointOnPolygon(Double[] point, List<Double[]> polygon) {
        if (polygon.size() < 2) return null;
        
        Double[] nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            Double[] p1 = polygon.get(i);
            Double[] p2 = polygon.get(j);
            
            // 计算点到线段的最近点
            Double[] closest = getClosestPointOnSegment(point, p1, p2);
            double distance = calculateDistance(point, closest);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = closest;
            }
        }
        
        return nearestPoint;
    }
    
    /**
     * 计算点到线段的最近点
     */
    private Double[] getClosestPointOnSegment(Double[] point, Double[] segStart, Double[] segEnd) {
        double px = point[0], py = point[1];
        double ax = segStart[0], ay = segStart[1];
        double bx = segEnd[0], by = segEnd[1];
        
        double dx = bx - ax;
        double dy = by - ay;
        
        if (dx == 0 && dy == 0) {
            return new Double[]{ax, ay};
        }
        
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        
        return new Double[]{ax + t * dx, ay + t * dy};
    }
    
    /**
     * 计算两点间距离
     */
    private double calculateDistance(Double[] p1, Double[] p2) {
        double dx = p1[0] - p2[0];
        double dy = p1[1] - p2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 为临近点找到对应的边界点
     */
    private List<Double[]> findBoundaryPointsForAdjacent(Double[] adjacentPoint, List<Double[]> originalVertices) {
        List<Double[]> boundaryPoints = new ArrayList<>();
        
        // 简化实现：找到距离临近点最近的几个边界点
        for (int i = 0; i < originalVertices.size(); i++) {
            int j = (i + 1) % originalVertices.size();
            Double[] p1 = originalVertices.get(i);
            Double[] p2 = originalVertices.get(j);
            
            Double[] closest = getClosestPointOnSegment(adjacentPoint, p1, p2);
            if (calculateDistance(adjacentPoint, closest) < 50.0) { // 50格内的边界点
                boundaryPoints.add(closest);
            }
        }
        
        return boundaryPoints;
    }
    
    /**
     * 计算多个点的中位值
     */
    private Double[] calculateMedianPoint(List<Double[]> points) {
        if (points.isEmpty()) return null;
        if (points.size() == 1) return points.get(0);
        
        double sumX = 0, sumY = 0;
        for (Double[] point : points) {
            sumX += point[0];
            sumY += point[1];
        }
        
        return new Double[]{sumX / points.size(), sumY / points.size()};
    }
    
    /**
     * 计算与边界的交点
     */
    private Double[] findIntersectionWithBoundary(Double[] point1, Double[] point2, List<Double[]> originalVertices) {
        // 简化实现：返回两点连线的中点
        return new Double[]{(point1[0] + point2[0]) / 2, (point1[1] + point2[1]) / 2};
    }
    
    /**
     * 移除重复点
     */
    private List<Double[]> removeDuplicatePoints(List<Double[]> vertices) {
        List<Double[]> unique = new ArrayList<>();
        final double EPSILON = 0.001;
        
        for (Double[] vertex : vertices) {
            boolean isDuplicate = false;
            for (Double[] existing : unique) {
                if (calculateDistance(vertex, existing) < EPSILON) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                unique.add(vertex);
            }
        }
        
        return unique;
    }
    
    /**
     * 按照提示词排序顶点：原顶点 边界点 新顶点 边界点 原顶点
     */
    private List<Double[]> sortVerticesForExpansion(List<Double[]> originalVertices, List<Double[]> externalVertices, List<Double[]> boundaryPoints) {
        List<Double[]> sortedVertices = new ArrayList<>();
        
        // 简化实现：先添加原顶点，再添加新顶点和边界点
        sortedVertices.addAll(originalVertices);
        sortedVertices.addAll(externalVertices);
        sortedVertices.addAll(boundaryPoints);
        
        // 按逆时针方向排序
        return sortVerticesCounterClockwise(sortedVertices);
    }
    
    /**
     * 按逆时针方向排序顶点
     */
    private List<Double[]> sortVerticesCounterClockwise(List<Double[]> vertices) {
        if (vertices.size() < 3) return vertices;
        
        // 计算重心
        double centerX = 0, centerZ = 0;
        for (Double[] vertex : vertices) {
            centerX += vertex[0];
            centerZ += vertex[1];
        }
        centerX /= vertices.size();
        centerZ /= vertices.size();
        
        final double finalCenterX = centerX;
        final double finalCenterZ = centerZ;
        
        // 按极角排序
        vertices.sort((v1, v2) -> {
            double angle1 = Math.atan2(v1[1] - finalCenterZ, v1[0] - finalCenterX);
            double angle2 = Math.atan2(v2[1] - finalCenterZ, v2[0] - finalCenterX);
            return Double.compare(angle1, angle2);
        });
        
        return vertices;
    }
    
    /**
     * 检测交叉
     */
    private boolean detectCrossings(List<Double[]> vertices) {
        // 简化实现：检测基本的自相交
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 2; j < vertices.size(); j++) {
                if (i == 0 && j == vertices.size() - 1) continue; // 跳过相邻边
                
                int nextI = (i + 1) % vertices.size();
                int nextJ = (j + 1) % vertices.size();
                
                Double[] intersection = getLineIntersection(
                    vertices.get(i), vertices.get(nextI),
                    vertices.get(j), vertices.get(nextJ)
                );
                
                if (intersection != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 反转新顶点的顺序
     */
    private List<Double[]> reverseNewVerticesOrder(List<Double[]> vertices) {
        // 简化实现：反转整个顶点列表
        List<Double[]> reversed = new ArrayList<>();
        for (int i = vertices.size() - 1; i >= 0; i--) {
            reversed.add(vertices.get(i));
        }
        return reversed;
    }
    
    /**
     * 将Double[]列表转换为Vertex列表
     */
    private List<AreaData.Vertex> convertToVertexList(List<Double[]> coordinates) {
        List<AreaData.Vertex> vertices = new ArrayList<>();
        for (Double[] coord : coordinates) {
            vertices.add(new AreaData.Vertex(coord[0], coord[1]));
        }
        return vertices;
    }
    
    // Getter方法
    public AreaData getSelectedArea() { return selectedArea; }
    public List<Double[]> getNewVertices() { return newVertices; }
} 