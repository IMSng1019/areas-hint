package areahint.rename;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.debug.ClientDebugManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Rename功能管理器
 * 负责交互式域名重命名的整个流程管理
 */
public class RenameManager {

    /**
     * Rename状态枚举
     */
    public enum RenameState {
        IDLE,                   // 空闲状态
        SELECT_AREA,            // 选择要重命名的域名
        INPUT_NEW_NAME,         // 输入新域名名称
        INPUT_NEW_SURFACE_NAME, // 输入新联合域名
        CONFIRM_RENAME          // 确认重命名
    }

    // 单例实例
    private static RenameManager instance;

    // 当前状态
    private RenameState currentState = RenameState.IDLE;

    // 域名数据收集
    private String selectedAreaName = null;      // 选中的域名名称
    private String newAreaName = null;           // 新域名名称
    private String newSurfaceName = null;        // 新联合域名
    private String currentDimension = null;      // 当前维度
    private List<AreaData> availableAreas = new ArrayList<>();  // 可重命名的域名列表

    // 聊天监听器注册状态
    private boolean chatListenerRegistered = false;

    // 私有构造函数（单例模式）
    private RenameManager() {}

    /**
     * 获取单例实例
     */
    public static RenameManager getInstance() {
        if (instance == null) {
            instance = new RenameManager();
        }
        return instance;
    }

    /**
     * 启动Rename流程
     * @param areas 可重命名的域名列表
     * @param dimension 当前维度
     */
    public void startRename(List<AreaData> areas, String dimension) {
        if (currentState != RenameState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("§c当前已有Rename流程在进行中"), false);
            return;
        }

        this.availableAreas = new ArrayList<>(areas);
        this.currentDimension = dimension;

        if (areas.isEmpty()) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("§c当前维度没有您可以重命名的域名"), false);
            return;
        }

        // 注册聊天监听器
        registerChatListener();

        // 设置状态并显示UI
        currentState = RenameState.SELECT_AREA;
        RenameUI.showAreaSelectScreen(availableAreas);
    }

    /**
     * 注册聊天监听器来捕获用户输入
     */
    private void registerChatListener() {
        if (!chatListenerRegistered) {
            ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
                if (currentState != RenameState.IDLE) {
                    handleChatInput(message.getString());
                }
            });
            chatListenerRegistered = true;
        }
    }

    /**
     * 处理用户聊天输入
     */
    private void handleChatInput(String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 移除前缀符号（如果有的话）
        if (input.startsWith("<") && input.contains(">")) {
            int endIndex = input.indexOf(">") + 1;
            if (endIndex < input.length()) {
                input = input.substring(endIndex).trim();
            }
        }

        switch (currentState) {
            case INPUT_NEW_NAME:
                if (!input.trim().isEmpty()) {
                    newAreaName = input.trim();

                    // 检查新域名名称是否已存在
                    if (checkAreaNameExists(newAreaName)) {
                        client.player.sendMessage(Text.of("§c域名名称 \"" + newAreaName + "\" 已存在于当前维度"), false);
                        client.player.sendMessage(Text.of("§7请输入一个不同的域名名称："), false);
                        return;
                    }

                    // 检查新名称是否与原名称相同
                    if (newAreaName.equals(selectedAreaName)) {
                        client.player.sendMessage(Text.of("§c新域名名称不能与原名称相同"), false);
                        client.player.sendMessage(Text.of("§7请输入一个不同的域名名称："), false);
                        return;
                    }

                    client.player.sendMessage(Text.of("§a已设置新域名名称：§6" + newAreaName), false);

                    // 进入联合域名输入
                    currentState = RenameState.INPUT_NEW_SURFACE_NAME;
                    RenameUI.showSurfaceNameInputScreen();
                }
                break;

            case INPUT_NEW_SURFACE_NAME:
                // 联合域名可以为空
                newSurfaceName = input.trim().isEmpty() ? null : input.trim();
                if (newSurfaceName != null) {
                    client.player.sendMessage(Text.of("§a已设置新联合域名：§6" + newSurfaceName), false);
                } else {
                    client.player.sendMessage(Text.of("§7跳过联合域名设置"), false);
                }

                // 进入确认状态
                currentState = RenameState.CONFIRM_RENAME;
                RenameUI.showConfirmScreen(selectedAreaName, newAreaName, newSurfaceName);
                break;

            default:
                break;
        }
    }

    /**
     * 处理域名选择（从命令调用）
     */
    public void handleAreaSelection(String areaName) {
        if (currentState != RenameState.SELECT_AREA) {
            return;
        }

        // 移除引号（如果存在）
        if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
            areaName = areaName.substring(1, areaName.length() - 1);
        }

        // 验证域名是否在可选列表中
        boolean found = false;
        for (AreaData area : availableAreas) {
            if (area.getName().equals(areaName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("§c未找到域名: " + areaName), false);
            return;
        }

        selectedAreaName = areaName;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of("§a已选择域名：§6" + areaName), false);
        }

        // 进入新名称输入状态
        currentState = RenameState.INPUT_NEW_NAME;
        RenameUI.showNewNameInputScreen();
    }

    /**
     * 确认重命名
     */
    public void confirmRename() {
        if (currentState != RenameState.CONFIRM_RENAME) {
            return;
        }

        try {
            // 发送到服务端
            RenameNetworking.sendRenameRequest(selectedAreaName, newAreaName, newSurfaceName, currentDimension);

            MinecraftClient.getInstance().player.sendMessage(
                Text.of("§a重命名请求已发送到服务端，等待处理..."), false);

            resetState();

        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                Text.of("§c重命名域名时发生错误: " + e.getMessage()), false);
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "重命名失败: " + e.getMessage());
        }
    }

    /**
     * 取消Rename流程
     */
    public void cancelRename() {
        MinecraftClient.getInstance().player.sendMessage(Text.of("§7Rename流程已取消"), false);
        resetState();
    }

    /**
     * 重置状态
     */
    private void resetState() {
        currentState = RenameState.IDLE;
        selectedAreaName = null;
        newAreaName = null;
        newSurfaceName = null;
        currentDimension = null;
        availableAreas.clear();
    }

    /**
     * 检查域名名称是否已存在于当前维度
     * @param areaName 要检查的域名名称
     * @return 如果域名名称已存在返回true，否则返回false
     */
    private boolean checkAreaNameExists(String areaName) {
        try {
            String fileName = getFileNameForCurrentDimension();
            if (fileName == null) {
                ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                    "无法确定当前维度文件名，跳过查重");
                return false;
            }

            // 读取当前维度的所有域名数据
            List<AreaData> existingAreas = FileManager.readAreaData(
                areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName));

            // 检查是否存在相同的域名名称（name字段）
            for (AreaData area : existingAreas) {
                if (area.getName().equals(areaName)) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                        "发现重复域名名称: " + areaName);
                    return true;
                }
            }

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "域名名称 \"" + areaName + "\" 未重复，可以使用");
            return false;

        } catch (Exception e) {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.NETWORK,
                "检查域名名称时发生错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前维度的文件名
     */
    private String getFileNameForCurrentDimension() {
        if (currentDimension == null) return null;

        if (currentDimension.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (currentDimension.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (currentDimension.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return null;
    }

    // Getters
    public RenameState getCurrentState() { return currentState; }
    public String getSelectedAreaName() { return selectedAreaName; }
    public String getNewAreaName() { return newAreaName; }
    public String getNewSurfaceName() { return newSurfaceName; }
}
