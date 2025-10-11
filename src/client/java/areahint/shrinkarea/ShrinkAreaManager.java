package areahint.shrinkarea;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.util.AreaDataConverter;
import areahint.shrinkarea.ShrinkAreaClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 域名收缩管理器
 * 负责整个域名收缩流程的管理和协调
 */
public class ShrinkAreaManager {
    private static ShrinkAreaManager instance;
    
    // 状态管理
    private boolean isActive = false;
    private ShrinkState currentState = ShrinkState.IDLE;
    
    // 域名相关
    private List<AreaData> availableAreas = new ArrayList<>();
    private AreaData selectedArea = null;
    private String playerName = "";
    private boolean isAdmin = false;
    
    // 收缩区域顶点
    private List<AreaData.Vertex> shrinkVertices = new ArrayList<>();
    private boolean isRecording = false;
    
    // UI管理
    private ShrinkAreaUI ui;
    
    /**
     * 状态枚举
     */
    public enum ShrinkState {
        IDLE,           // 空闲状态
        SELECTING_AREA, // 选择域名状态
        RECORDING,      // 记录顶点状态
        CALCULATING,    // 计算状态
        CONFIRMING      // 确认状态
    }
    
    private ShrinkAreaManager() {
        this.ui = new ShrinkAreaUI(this);
    }
    
    public static ShrinkAreaManager getInstance() {
        if (instance == null) {
            instance = new ShrinkAreaManager();
        }
        return instance;
    }
    
    /**
     * 启动域名收缩流程
     */
    public void start() {
        if (isActive) {
            sendMessage("§c域名收缩功能已经在运行中", Formatting.RED);
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) {
            sendMessage("§c无法获取玩家信息", Formatting.RED);
            return;
        }
        
        playerName = player.getGameProfile().getName();
        
        // 检查玩家权限（简单检查是否为OP，真实权限由服务端验证）
        isAdmin = client.player.hasPermissionLevel(2);
        
        isActive = true;
        currentState = ShrinkState.SELECTING_AREA;
        
        sendMessage("§a域名收缩功能已启动", Formatting.GREEN);
        sendMessage("§e正在加载可用域名...", Formatting.YELLOW);
        
        // 加载可用域名
        loadAvailableAreas();
        
        // 显示域名选择界面
        ui.showAreaSelectionScreen();
    }
    
    /**
     * 停止域名收缩流程
     */
    public void stop() {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        currentState = ShrinkState.IDLE;
        
        // 清理状态
        reset();
        
        ui.showCancelMessage();
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
        AreaData area = null;
        for (AreaData a : availableAreas) {
            if (a.getName().equals(areaName.trim())) {
                area = a;
                break;
            }
        }
        
        if (area == null) {
            sendMessage("§c域名 '" + areaName + "' 不存在或无法收缩", Formatting.RED);
            return;
        }
        
        // 选择该域名
        selectArea(area);
    }
    
    /**
     * 重置状态
     */
    private void reset() {
        selectedArea = null;
        shrinkVertices.clear();
        isRecording = false;
        availableAreas.clear();
    }
    
    /**
     * 加载可用域名
     */
    private void loadAvailableAreas() {
        try {
            String dimension = getCurrentDimension();
            java.nio.file.Path areaPath = FileManager.checkFolderExist().resolve(dimension + ".json");
            List<AreaData> allAreas = FileManager.readAreaData(areaPath);
            
            availableAreas.clear();
            
            for (AreaData area : allAreas) {
                // 如果是管理员，显示所有域名
                if (isAdmin) {
                    availableAreas.add(area);
                } else {
                    // 如果是普通玩家，只显示basename为自己的域名
                    if (playerName.equals(area.getBaseName())) {
                        availableAreas.add(area);
                    }
                }
            }
            
            if (availableAreas.isEmpty()) {
                sendMessage("§c没有找到可以收缩的域名", Formatting.RED);
                sendMessage("§e只能收缩您创建的域名（basename为您的用户名）", Formatting.YELLOW);
                stop();
                return;
            }
            
            sendMessage("§a找到 " + availableAreas.size() + " 个可收缩的域名", Formatting.GREEN);
            
        } catch (Exception e) {
            sendMessage("§c加载域名失败: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
            stop();
        }
    }
    
    /**
     * 选择要收缩的域名
     */
    public void selectArea(AreaData area) {
        if (currentState != ShrinkState.SELECTING_AREA) {
            sendMessage("§c当前状态不允许选择域名", Formatting.RED);
            return;
        }
        
        selectedArea = area;
        currentState = ShrinkState.RECORDING;
        
        sendMessage("§a已选择域名: " + AreaDataConverter.getDisplayName(area), Formatting.GREEN);
        sendMessage("§e请按 §6X §e键记录收缩区域的顶点", Formatting.YELLOW);
        sendMessage("§7记录完成后点击 §6[保存域名] §7按钮完成收缩", Formatting.GRAY);
    }
    
    /**
     * 处理X键按下事件
     */
    public void handleXKeyPress() {
        if (!isActive || currentState != ShrinkState.RECORDING) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) {
            return;
        }
        
        if (!isRecording) {
            // 开始记录
            startRecording();
        } else {
            // 结束记录
            stopRecording();
        }
    }
    
    /**
     * 开始记录顶点
     */
    private void startRecording() {
        isRecording = true;
        shrinkVertices.clear();
        
        sendMessage("§a开始记录收缩区域顶点", Formatting.GREEN);
        sendMessage("§e按 §6X §e键记录当前位置", Formatting.YELLOW);
        sendMessage("§7至少需要记录3个顶点", Formatting.GRAY);
    }
    
    /**
     * 停止记录顶点
     */
    private void stopRecording() {
        if (shrinkVertices.size() < 3) {
            sendMessage("§c收缩区域至少需要3个顶点，当前只有 " + shrinkVertices.size() + " 个", Formatting.RED);
            return;
        }
        
        isRecording = false;
        currentState = ShrinkState.CALCULATING;
        
        sendMessage("§a记录完成，共记录了 " + shrinkVertices.size() + " 个顶点", Formatting.GREEN);
        sendMessage("§e正在计算收缩后的域名...", Formatting.YELLOW);
        
        // 开始几何计算
        processAreaShrinking();
    }
    
    /**
     * 记录当前位置为顶点
     */
    public void recordCurrentPosition() {
        if (!isRecording) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) {
            return;
        }
        
        BlockPos pos = player.getBlockPos();
        AreaData.Vertex vertex = new AreaData.Vertex(pos.getX(), pos.getZ());
        shrinkVertices.add(vertex);
        
        sendMessage("§a记录顶点 " + shrinkVertices.size() + ": §6(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")", Formatting.GREEN);
        
        // 显示选项按钮（参考EasyAdd的实现）
        ui.showPointRecordedOptions(shrinkVertices.size());
    }
    
    /**
     * 继续记录更多顶点
     */
    public void continueRecording() {
        if (!isRecording || !isActive) {
            return;
        }
        
        sendMessage("§a继续记录更多顶点，按 §6X §a记录当前位置", Formatting.GREEN);
    }
    
    /**
     * 完成记录并保存域名收缩
     */
    public void finishAndSave() {
        if (!isRecording || !isActive) {
            return;
        }
        
        if (shrinkVertices.size() < 3) {
            sendMessage("§c收缩区域至少需要3个顶点，当前只有 " + shrinkVertices.size() + " 个", Formatting.RED);
            return;
        }
        
        isRecording = false;
        currentState = ShrinkState.CALCULATING;
        
        sendMessage("§a记录完成，共记录了 " + shrinkVertices.size() + " 个顶点", Formatting.GREEN);
        sendMessage("§e正在计算收缩后的域名...", Formatting.YELLOW);
        
        // 开始几何计算
        processAreaShrinking();
    }
    
    /**
     * 处理域名收缩的核心逻辑
     */
    private void processAreaShrinking() {
        sendMessage("§e正在处理域名收缩...", Formatting.YELLOW);
        
        // 1. 高度验证
        if (!validateHeights()) {
            return;
        }
        
        // 2. 几何计算和顶点处理
        ShrinkGeometryCalculator calculator = new ShrinkGeometryCalculator(selectedArea, shrinkVertices);
        AreaData shrunkArea = calculator.shrinkArea();
        
        if (shrunkArea == null) {
            sendMessage("§c无法计算收缩后的域名", Formatting.RED);
            return;
        }
        
        // 3. 发送给服务端
        ShrinkAreaClientNetworking.sendShrunkAreaToServer(shrunkArea);
        
        sendMessage("§a域名收缩完成！", Formatting.GREEN);
        
        // 重置状态
        reset();
    }
    
    /**
     * 验证新收缩区域的高度
     */
    private boolean validateHeights() {
        if (selectedArea == null || selectedArea.getAltitude() == null) {
            sendMessage("§c原域名没有高度信息", Formatting.RED);
            return false;
        }
        
        // 收缩区域继承原域名的高度设置
        return true;
    }
    
    /**
     * 发送消息给玩家
     */
    private void sendMessage(String message, Formatting color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message).formatted(color), false);
        }
    }
    
    /**
     * 获取当前维度
     */
    private String getCurrentDimension() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return "overworld";
        }
        
        String dimensionId = client.world.getRegistryKey().getValue().toString();
        if (dimensionId.contains("nether")) {
            return "the_nether";
        } else if (dimensionId.contains("end")) {
            return "the_end";
        } else {
            return "overworld";
        }
    }
    
    // Getters
    public boolean isActive() { return isActive; }
    public ShrinkState getCurrentState() { return currentState; }
    public List<AreaData> getAvailableAreas() { return availableAreas; }
    public AreaData getSelectedArea() { return selectedArea; }
    public List<AreaData.Vertex> getShrinkVertices() { return shrinkVertices; }
    public boolean isRecording() { return isRecording; }
    public boolean isAdmin() { return isAdmin; }
    public String getPlayerName() { return playerName; }
} 