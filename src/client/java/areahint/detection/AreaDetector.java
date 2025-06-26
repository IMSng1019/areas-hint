package areahint.detection;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.data.AreaData;
import areahint.file.FileManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区域检测器类
 * 用于检测玩家所在的区域
 */
public class AreaDetector {
    // 当前维度的区域数据
    private List<AreaData> areas = new ArrayList<>();
    
    // 按区域等级分组的区域数据
    private Map<Integer, List<AreaData>> areasByLevel = new HashMap<>();
    
    // 最后一次检测的时间（毫秒）
    private long lastDetectionTime = 0;
    
    /**
     * 加载区域数据
     * @param fileName 区域数据文件名
     */
    public void loadAreaData(String fileName) {
        Path areaFile = FileManager.getDimensionFile(fileName);
        areas = FileManager.readAreaData(areaFile);
        AreashintClient.LOGGER.info("已加载区域数据: {} 个区域", areas.size());
        
        // 按等级分组
        areasByLevel.clear();
        for (AreaData area : areas) {
            areasByLevel.computeIfAbsent(area.getLevel(), k -> new ArrayList<>()).add(area);
        }
    }
    
    /**
     * 检测玩家所在的区域
     * @param x 玩家的X坐标
     * @param z 玩家的Z坐标
     * @return 玩家所在的区域名称（根据配置的样式格式化），如果不在任何区域内则返回null
     */
    public String detectPlayerArea(double x, double z) {
        // 更新最后一次检测时间
        lastDetectionTime = System.currentTimeMillis();
        
        // 获取玩家当前所在的区域
        AreaData currentArea = findArea(x, z);
        
        // 如果没有找到区域
        if (currentArea == null) {
            return null;
        }
        
        // 根据配置的样式格式化区域名称
        String style = ClientConfig.getSubtitleStyle();
        return formatAreaName(currentArea, style);
    }
    
    /**
     * 检查是否应该进行检测（根据配置的频率）
     * @return 如果应该进行检测返回true，否则返回false
     */
    public boolean shouldDetect() {
        int frequency = ClientConfig.getFrequency();
        long interval = 1000 / frequency; // 毫秒
        return System.currentTimeMillis() - lastDetectionTime >= interval;
    }
    
    /**
     * 查找玩家所在的区域
     * @param x 玩家的X坐标
     * @param z 玩家的Z坐标
     * @return 玩家所在的区域，如果不在任何区域内则返回null
     */
    private AreaData findArea(double x, double z) {
        // 按照区域等级排序（1为顶级域名，2为二级域名，以此类推）
        List<Integer> levels = new ArrayList<>(areasByLevel.keySet());
        Collections.sort(levels);
        
        // 先检查玩家是否在一级域名内
        List<AreaData> levelOneAreas = areasByLevel.getOrDefault(1, Collections.emptyList());
        AreaData inLevelOne = null;
        
        // 对每个一级域名进行距离排序
        List<AreaData> sortedLevelOneAreas = sortAreasByDistance(levelOneAreas, x, z);
        
        for (AreaData area : sortedLevelOneAreas) {
            // 先用AABB快速排除
            if (RayCasting.isPointInAABB(x, z, area.getSecondVertices())) {
                // 再用精确的射线法检测
                if (RayCasting.isPointInPolygon(x, z, area.getVertices())) {
                    inLevelOne = area;
                    break;
                }
            }
        }
        
        // 如果玩家不在任何一级域名内
        if (inLevelOne == null) {
            return null;
        }
        
        // 继续检查更高级别的域名
        AreaData highestArea = inLevelOne;
        String baseName = inLevelOne.getName();
        
        for (int i = 1; i < levels.size(); i++) {
            int currentLevel = levels.get(i);
            List<AreaData> currentLevelAreas = areasByLevel.getOrDefault(currentLevel, Collections.emptyList());
            
            // 只检查基于当前域名的下一级域名
            final String finalBaseName = baseName; // 创建final副本供lambda表达式使用
            List<AreaData> childAreas = currentLevelAreas.stream()
                    .filter(area -> finalBaseName.equals(area.getBaseName()))
                    .collect(Collectors.toList());
            
            // 按距离排序
            List<AreaData> sortedChildAreas = sortAreasByDistance(childAreas, x, z);
            
            boolean foundInCurrentLevel = false;
            
            for (AreaData area : sortedChildAreas) {
                // 先用AABB快速排除
                if (RayCasting.isPointInAABB(x, z, area.getSecondVertices())) {
                    // 再用精确的射线法检测
                    if (RayCasting.isPointInPolygon(x, z, area.getVertices())) {
                        highestArea = area;
                        baseName = area.getName();
                        foundInCurrentLevel = true;
                        break;
                    }
                }
            }
            
            // 如果在当前级别没有找到区域，就停止查找
            if (!foundInCurrentLevel) {
                break;
            }
        }
        
        return highestArea;
    }
    
    /**
     * 按照与玩家的距离排序区域列表
     * @param areas 区域列表
     * @param x 玩家的X坐标
     * @param z 玩家的Z坐标
     * @return 排序后的区域列表
     */
    private List<AreaData> sortAreasByDistance(List<AreaData> areas, double x, double z) {
        if (areas == null || areas.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 创建一个新列表，避免修改原始列表
        List<AreaData> result = new ArrayList<>(areas);
        
        // 按到玩家的距离排序
        result.sort((a1, a2) -> {
            double d1 = distanceToArea(a1, x, z);
            double d2 = distanceToArea(a2, x, z);
            return Double.compare(d1, d2);
        });
        
        return result;
    }
    
    /**
     * 计算点到区域的近似距离
     * 这里使用到多边形中心点的距离作为近似
     * @param area 区域
     * @param x 点的X坐标
     * @param z 点的Z坐标
     * @return 距离
     */
    private double distanceToArea(AreaData area, double x, double z) {
        // 计算多边形的中心点
        double centerX = 0;
        double centerZ = 0;
        List<AreaData.Vertex> vertices = area.getVertices();
        
        for (AreaData.Vertex vertex : vertices) {
            centerX += vertex.getX();
            centerZ += vertex.getZ();
        }
        
        centerX /= vertices.size();
        centerZ /= vertices.size();
        
        // 计算到中心点的距离
        double dx = x - centerX;
        double dz = z - centerZ;
        
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * 根据配置的样式格式化区域名称
     * @param area 区域
     * @param style 样式（full、simple、mixed）
     * @return 格式化后的区域名称
     */
    private String formatAreaName(AreaData area, String style) {
        if (area == null) {
            return null;
        }
        
        String result;
        
        switch (style) {
            case "full":
                // 显示完整路径
                result = buildFullPath(area);
                break;
            case "simple":
                // 仅显示当前级别
                result = area.getName();
                break;
            case "mixed":
                // 混合模式
                if (area.getLevel() == 1) {
                    // 一级域名只显示自身
                    result = area.getName();
                } else if (area.getLevel() == 2) {
                    // 二级域名显示一级+二级
                    AreaData parent = findAreaByName(area.getBaseName());
                    if (parent != null) {
                        result = parent.getName() + "·" + area.getName();
                    } else {
                        result = area.getName();
                    }
                } else {
                    // 三级及以上只显示当前级别
                    result = area.getName();
                }
                break;
            default:
                result = area.getName();
                break;
        }
        
        return result;
    }
    
    /**
     * 构建区域的完整路径
     * @param area 区域
     * @return 完整路径
     */
    private String buildFullPath(AreaData area) {
        if (area == null) {
            return "";
        }
        
        List<String> path = new ArrayList<>();
        path.add(area.getName());
        
        AreaData current = area;
        while (current.getLevel() > 1 && current.getBaseName() != null) {
            AreaData parent = findAreaByName(current.getBaseName());
            if (parent == null) {
                break;
            }
            
            path.add(0, parent.getName());
            current = parent;
        }
        
        return String.join("·", path);
    }
    
    /**
     * 通过名称查找区域
     * @param name 区域名称
     * @return 区域
     */
    private AreaData findAreaByName(String name) {
        if (name == null) {
            return null;
        }
        
        for (AreaData area : areas) {
            if (name.equals(area.getName())) {
                return area;
            }
        }
        
        return null;
    }
} 