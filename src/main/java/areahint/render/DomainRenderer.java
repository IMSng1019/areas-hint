package areahint.render;

import areahint.data.AreaData;
import areahint.util.ColorUtil;
import areahint.util.AreaDataConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 域名渲染工具类
 * 处理域名的颜色显示和层级关系
 */
public class DomainRenderer {
    
    /**
     * 构建完整的域名显示文本
     * @param currentArea 当前区域
     * @param allAreas 所有区域数据
     * @return 带颜色的域名字符串
     */
    public static String buildDomainDisplayText(AreaData currentArea, List<AreaData> allAreas) {
        if (currentArea == null) {
            return "";
        }
        
        // 构建域名层级链
        List<AreaData> domainChain = buildDomainChain(currentArea, allAreas);
        
        // 生成带颜色的显示文本
        return buildColoredDisplayText(domainChain);
    }
    
    /**
     * 构建域名层级链（从顶级域名到当前域名）
     * @param currentArea 当前区域
     * @param allAreas 所有区域数据
     * @return 域名链列表
     */
    private static List<AreaData> buildDomainChain(AreaData currentArea, List<AreaData> allAreas) {
        List<AreaData> chain = new ArrayList<>();
        Map<String, AreaData> areaMap = new HashMap<>();
        
        // 建立名称到区域的映射
        for (AreaData area : allAreas) {
            areaMap.put(area.getName(), area);
        }
        
        // 从当前区域向上追溯
        AreaData current = currentArea;
        while (current != null) {
            chain.add(0, current); // 插入到列表开头
            
            // 查找上级域名
            String baseName = current.getBaseName();
            if (baseName == null || baseName.trim().isEmpty()) {
                break;
            }
            
            current = areaMap.get(baseName);
            
            // 防止循环引用
            if (chain.contains(current)) {
                break;
            }
        }
        
        return chain;
    }
    
    /**
     * 构建带颜色的显示文本
     * @param domainChain 域名链
     * @return 带颜色的文本
     */
    private static String buildColoredDisplayText(List<AreaData> domainChain) {
        if (domainChain.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < domainChain.size(); i++) {
            AreaData area = domainChain.get(i);
            
            // 添加域名（带颜色，优先显示surfacename）
            String displayName = AreaDataConverter.getDisplayName(area);
            String domainText = ColorUtil.colorText(displayName, area.getColor());
            result.append(domainText);
            
            // 如果不是最后一个域名，添加分隔符
            if (i < domainChain.size() - 1) {
                result.append("§f·"); // 白色分隔符
            }
        }
        
        return result.toString();
    }
    
    /**
     * 获取域名的简单显示文本（只显示当前域名）
     * @param area 区域数据
     * @return 带颜色的域名文本
     */
    public static String getSimpleDomainText(AreaData area) {
        if (area == null) {
            return "";
        }
        
        String displayName = AreaDataConverter.getDisplayName(area);
        return ColorUtil.colorText(displayName, area.getColor());
    }
    
    /**
     * 验证域名层级关系的有效性
     * @param allAreas 所有区域数据
     * @return 验证结果和错误信息
     */
    public static ValidationResult validateDomainHierarchy(List<AreaData> allAreas) {
        Map<String, AreaData> areaMap = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        // 建立映射
        for (AreaData area : allAreas) {
            areaMap.put(area.getName(), area);
        }
        
        // 验证每个域名的层级关系
        for (AreaData area : allAreas) {
            String baseName = area.getBaseName();
            
            if (baseName != null && !baseName.trim().isEmpty()) {
                AreaData baseArea = areaMap.get(baseName);
                
                if (baseArea == null) {
                    errors.add("域名 \"" + area.getName() + "\" 的上级域名 \"" + baseName + "\" 不存在");
                } else {
                    // 检查等级关系
                    if (baseArea.getLevel() != area.getLevel() - 1) {
                        errors.add("域名 \"" + area.getName() + "\" (等级 " + area.getLevel() + 
                                 ") 与其上级域名 \"" + baseName + "\" (等级 " + baseArea.getLevel() + ") 的等级关系不正确");
                    }
                    
                    // 检查循环引用
                    if (hasCircularReference(area, areaMap)) {
                        errors.add("域名 \"" + area.getName() + "\" 存在循环引用");
                    }
                }
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * 检查是否存在循环引用
     * @param area 要检查的区域
     * @param areaMap 区域映射
     * @return 是否存在循环引用
     */
    private static boolean hasCircularReference(AreaData area, Map<String, AreaData> areaMap) {
        List<String> visited = new ArrayList<>();
        AreaData current = area;
        
        while (current != null) {
            if (visited.contains(current.getName())) {
                return true;
            }
            
            visited.add(current.getName());
            String baseName = current.getBaseName();
            
            if (baseName == null || baseName.trim().isEmpty()) {
                break;
            }
            
            current = areaMap.get(baseName);
        }
        
        return false;
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            if (valid) {
                return "";
            }
            return String.join("\n", errors);
        }
    }
} 