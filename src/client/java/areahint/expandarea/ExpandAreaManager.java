package areahint.expandarea;

import areahint.data.AreaData;
import areahint.util.SurfaceNameHandler;
import areahint.file.FileManager;
import areahint.expandarea.ExpandAreaClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

public class ExpandAreaManager {
    private static ExpandAreaManager instance;
    private MinecraftClient client;
    private String selectedAreaName;
    private AreaData selectedArea;
    private List<Double[]> newVertices;
    private boolean isRecording = false;
    private boolean isActive = false;  // 添加活动状态跟踪
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
        if (client.player == null) {
            return;
        }
        
        isActive = true;  // 设置为活动状态
        // 显示域名询问界面
        ui.showAreaNameInput();
    }
    
    /**
     * 处理用户输入的域名名称
     */
    public void handleAreaNameInput(String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            sendMessage("§c请输入有效的域名", Formatting.RED);
            return;
        }
        
        // 检查域名是否存在
        AreaData area = findAreaByName(areaName.trim());
        if (area == null) {
            sendMessage("§c域名 '" + areaName + "' 不存在", Formatting.RED);
            ui.showAreaNameInput(); // 重新显示输入界面
            return;
        }
        
        // 检查权限
        if (!checkPermission(area)) {
            sendMessage("§c您没有权限扩展此域名", Formatting.RED);
            return;
        }
        
        this.selectedAreaName = areaName.trim();
        this.selectedArea = area;
        
        // 显示可修改的域名列表
        showModifiableAreas();
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
     * 显示可修改的域名列表
     */
    private void showModifiableAreas() {
        if (client.player == null) {
            return;
        }
        
        List<AreaData> modifiableAreas = getModifiableAreas();
        
        if (modifiableAreas.isEmpty()) {
            sendMessage("§c没有可修改的域名", Formatting.RED);
            return;
        }
        
        ui.showAreaSelection(modifiableAreas);
    }
    
    /**
     * 获取可修改的域名列表
     */
    private List<AreaData> getModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        String playerName = client.player.getGameProfile().getName();
        boolean isAdmin = client.player.hasPermissionLevel(2);
        
        // 获取所有域名
        List<AreaData> allAreas = loadAllAreas();
        
        for (AreaData area : allAreas) {
            if (isAdmin) {
                // 管理员可以修改所有域名
                result.add(area);
            } else {
                // 普通玩家只能修改自己创建的或basename引用自己的域名
                if (playerName.equals(area.getSignature())) {
                    result.add(area);
                } else if (area.getBaseName() != null) {
                    AreaData baseArea = findAreaByName(area.getBaseName());
                    if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                        result.add(area);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 处理域名选择
     */
    public void handleAreaSelection(AreaData selectedArea) {
        this.selectedArea = selectedArea;
        this.selectedAreaName = selectedArea.getName();
        
        sendMessage("§a已选择域名: " + areahint.util.AreaDataConverter.getDisplayName(selectedArea), Formatting.GREEN);
        sendMessage("§e请按 X 键开始记录新区域的顶点位置", Formatting.YELLOW);
        sendMessage("§e记录完成后按确认键完成扩展", Formatting.YELLOW);
        
        // 开始记录模式
        startRecording();
    }
    
    /**
     * 开始记录新顶点
     */
    private void startRecording() {
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
     */
    private void processAreaExpansion() {
        sendMessage("§e正在处理域名扩展...", Formatting.YELLOW);
        
        // 1. 高度验证
        if (!validateHeights()) {
            return;
        }
        
        // 2. 几何计算和顶点处理
        GeometryCalculator calculator = new GeometryCalculator(selectedArea, newVertices);
        AreaData expandedArea = calculator.expandArea();
        
        if (expandedArea == null) {
            sendMessage("§c无法计算扩展后的域名", Formatting.RED);
            return;
        }
        
        // 3. 发送给服务端
        ExpandAreaClientNetworking.sendExpandedAreaToServer(expandedArea);
        

        sendMessage("§a域名扩展完成！", Formatting.GREEN);
        
        // 重置状态
        reset();
    }
    
    /**
     * 验证新添加区域的高度
     */
    private boolean validateHeights() {
        if (client.player == null || selectedArea.getAltitude() == null) {
            return true; // 如果没有高度限制则通过
        }
        
        double playerY = client.player.getY();
        AreaData.AltitudeData altitude = selectedArea.getAltitude();
        
        if (altitude != null && altitude.getMax() != null && altitude.getMin() != null) {
            double maxHeight = altitude.getMax();
            double minHeight = altitude.getMin();
            
            // 新区域的高度必须在原域名高度范围内或扩大范围
            if (playerY > maxHeight || playerY < minHeight) {
                // 检查是否是扩大高度范围
                if (playerY > maxHeight) {
                    sendMessage("§e新区域高度超出原域名上限，将更新高度范围", Formatting.YELLOW);
                } else {
                    sendMessage("§e新区域高度低于原域名下限，将更新高度范围", Formatting.YELLOW);
                }
            }
        }
        
        return true;
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
        ui.hide();
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
     * 加载所有域名
     */
    private List<AreaData> loadAllAreas() {
        List<AreaData> areas = new ArrayList<>();
        try {
            Path areaPath = FileManager.checkFolderExist().resolve("overworld.json");
            areas = FileManager.readAreaData(areaPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return areas;
    }
    
    /**
     * 发送消息给玩家
     */
    private void sendMessage(String message, Formatting formatting) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(formatting), false);
        }
    }
    
    // Getter方法
    public AreaData getSelectedArea() { return selectedArea; }
    public List<Double[]> getNewVertices() { return newVertices; }
} 