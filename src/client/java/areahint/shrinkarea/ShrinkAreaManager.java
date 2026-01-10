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
 *
 * 负责整个域名收缩流程的管理和协调，是 ExpandAreaManager 的反义版本
 *
 * === 功能对比 ===
 *
 * ExpandAreaManager（扩展）:
 * - 在原域名外部添加新区域，扩大域名范围
 * - 删除内部顶点，保留外部顶点
 *
 * ShrinkAreaManager（收缩）:
 * - 在原域名内部标记收缩区域，缩小域名范围
 * - 删除外部顶点，保留内部顶点
 *
 * === 权限规则（与 expandarea 一致）===
 *
 * 1. 管理员（权限等级2）：可以收缩所有域名
 * 2. 普通玩家：
 *    - 可以收缩自己创建的域名（signature 等于玩家名）
 *    - 可以收缩 basename 引用自己的域名（basename 对应域名的 signature 等于玩家名）
 *
 * === 工作流程 ===
 *
 * 1. 启动收缩流程 (start)
 * 2. 加载可收缩的域名列表
 * 3. 玩家选择要收缩的域名
 * 4. 记录收缩区域的顶点（按 X 键）
 * 5. 完成记录并计算收缩后的域名
 * 6. 发送到服务端保存并同步
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

        // 查找域名（从文件加载，而不是从 availableAreas）
        AreaData area = findAreaByName(areaName.trim());
        if (area == null) {
            sendMessage("§c域名 '" + areaName + "' 不存在", Formatting.RED);
            return;
        }

        // 检查权限
        if (!checkPermission(area)) {
            sendMessage("§c您没有权限收缩此域名", Formatting.RED);
            return;
        }

        // 选择该域名（直接处理，不检查状态）
        handleAreaSelection(area);
    }

    /**
     * 处理域名选择
     */
    public void handleAreaSelection(AreaData selectedArea) {
        this.selectedArea = selectedArea;

        sendMessage("§a已选择域名: " + AreaDataConverter.getDisplayName(selectedArea), Formatting.GREEN);
        sendMessage("§e请按 §6X §e键开始记录收缩区域的顶点位置", Formatting.YELLOW);
        sendMessage("§7记录完成后点击 §6[保存域名] §7按钮完成收缩", Formatting.GRAY);

        // 开始记录模式
        startRecording();
    }

    /**
     * 开始记录顶点
     */
    private void startRecording() {
        this.isActive = true;
        this.isRecording = true;
        this.shrinkVertices.clear();
    }

    /**
     * 检查玩家是否有权限修改指定域名
     */
    private boolean checkPermission(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
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
                    java.nio.file.Path areaPath = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
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
     * 重置管理器状态
     */
    public void reset() {
        this.selectedArea = null;
        this.shrinkVertices.clear();
        this.isRecording = false;
        this.isActive = false;  // 重置活动状态
        this.availableAreas.clear();
    }
    
    /**
     * 加载可用域名
     * 按照提示词：
     * - 如果是管理员（权限等级为2），列出所有域名
     * - 如果是普通玩家，列出被basename引用的玩家的域名（即signature等于玩家名的域名）
     */
    private void loadAvailableAreas() {
        try {
            // 使用 loadAllAreas 方法加载所有域名
            List<AreaData> allAreas = loadAllAreas();

            availableAreas.clear();

            for (AreaData area : allAreas) {
                // 如果是管理员，显示所有域名
                if (isAdmin) {
                    availableAreas.add(area);
                } else {
                    // 如果是普通玩家，只显示自己创建的域名（signature等于玩家名）
                    // 或者basename引用自己的域名
                    if (playerName.equals(area.getSignature())) {
                        availableAreas.add(area);
                    } else if (area.getBaseName() != null) {
                        // 查找basename对应的域名，检查其signature是否为当前玩家
                        AreaData baseArea = findAreaByNameInList(area.getBaseName(), allAreas);
                        if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                            availableAreas.add(area);
                        }
                    }
                }
            }

            if (availableAreas.isEmpty()) {
                sendMessage("§c没有找到可以收缩的域名", Formatting.RED);
                sendMessage("§e只能收缩您创建的域名或basename引用您的域名", Formatting.YELLOW);
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
     * 在指定列表中查找域名（辅助方法）
     */
    private AreaData findAreaByNameInList(String name, List<AreaData> areas) {
        for (AreaData area : areas) {
            if (area.getName().equals(name)) {
                return area;
            }
        }
        return null;
    }


    /**
     * 处理X键按下事件
     */
    public void handleXKeyPress() {
        if (!isActive || !isRecording) {
            return;
        }

        // 直接记录当前位置
        recordCurrentPosition();
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
        if (!isRecording) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
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
            sendMessage("§c至少需要记录3个点才能形成有效区域", Formatting.RED);
            return;
        }

        // 停止记录模式
        this.isRecording = false;

        // 使用现有的处理逻辑
        try {
            processAreaShrinking();
        } catch (Exception e) {
            sendMessage("§c收缩域名时发生错误: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }
    
    /**
     * 处理域名收缩的核心逻辑
     */
    private void processAreaShrinking() {
        sendMessage("§e正在处理域名收缩...", Formatting.YELLOW);

        try {
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

            // 3. 获取当前维度信息
            MinecraftClient client = MinecraftClient.getInstance();
            String currentDimension = null;
            if (client.world != null) {
                currentDimension = client.world.getRegistryKey().getValue().toString();
            }

            if (currentDimension == null) {
                sendMessage("§c无法获取当前维度信息", Formatting.RED);
                return;
            }

            // 4. 发送给服务端
            ShrinkAreaClientNetworking.sendShrunkAreaToServer(shrunkArea, currentDimension);

            sendMessage("§a域名收缩完成！", Formatting.GREEN);

        } catch (Exception e) {
            sendMessage("§c域名收缩过程中发生错误: " + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        } finally {
            // 重置状态
            reset();
        }
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