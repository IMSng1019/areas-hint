package areahint.log;

import areahint.Areashint;
import areahint.file.FileManager;

import java.nio.file.Path;

/**
 * 服务端日志管理器
 * 负责管理服务端日志的写入和清理
 */
public class ServerLogManager {
    private static AsyncLogManager logManager;
    private static final String LOG_FOLDER_NAME = "server_log";
    private static final int RETENTION_DAYS = 7; // 服务端日志保留7天

    /**
     * 初始化服务端日志管理器
     */
    public static void init() {
        try {
            Path configFolder = FileManager.getConfigFolder();
            Path logFolder = configFolder.resolve(LOG_FOLDER_NAME);

            logManager = new AsyncLogManager(logFolder, "server", RETENTION_DAYS);
            Areashint.LOGGER.info("服务端日志管理器初始化完成");
        } catch (Exception e) {
            Areashint.LOGGER.error("初始化服务端日志管理器失败", e);
        }
    }

    /**
     * 记录玩家进入域名
     * @param playerName 玩家名称
     * @param areaLevel 域名等级
     * @param surfaceName 联合域名名称
     * @param areaName 域名名称
     * @param dimensionalName 维度域名名称
     */
    public static void logPlayerEnterArea(String playerName, int areaLevel, String surfaceName, String areaName, String dimensionalName) {
        if (logManager == null) {
            return;
        }

        String levelText = getLevelText(areaLevel);
        String message;

        if (surfaceName != null && !surfaceName.isEmpty()) {
            message = String.format("[%s][%s]进入了[%s] [%s]的[%s]",
                dimensionalName != null ? dimensionalName : "", playerName, levelText, surfaceName, areaName);
        } else {
            message = String.format("[%s][%s]进入了[%s] [%s]",
                dimensionalName != null ? dimensionalName : "", playerName, levelText, areaName);
        }

        logManager.log(message);
        Areashint.LOGGER.info(message);
    }

    /**
     * 记录玩家离开域名
     * @param playerName 玩家名称
     * @param areaLevel 域名等级
     * @param surfaceName 联合域名名称
     * @param areaName 域名名称
     * @param dimensionalName 维度域名名称
     */
    public static void logPlayerLeaveArea(String playerName, int areaLevel, String surfaceName, String areaName, String dimensionalName) {
        if (logManager == null) {
            return;
        }

        String levelText = getLevelText(areaLevel);
        String message;

        if (surfaceName != null && !surfaceName.isEmpty()) {
            message = String.format("[%s][%s]离开了[%s] [%s]的[%s]",
                dimensionalName != null ? dimensionalName : "", playerName, levelText, surfaceName, areaName);
        } else {
            message = String.format("[%s][%s]离开了[%s] [%s]",
                dimensionalName != null ? dimensionalName : "", playerName, levelText, areaName);
        }

        logManager.log(message);
        Areashint.LOGGER.info(message);
    }

    /**
     * 获取域名等级文本
     * @param level 域名等级
     * @return 等级文本
     */
    private static String getLevelText(int level) {
        if (level == 1) {
            return "顶级域名";
        } else if (level == 2) {
            return "二级域名";
        } else if (level == 3) {
            return "三级域名";
        } else {
            return level + "级域名";
        }
    }

    /**
     * 记录一般日志信息
     * @param message 日志消息
     */
    public static void log(String message) {
        if (logManager != null) {
            logManager.log(message);
        }
    }

    /**
     * 关闭服务端日志管理器
     */
    public static void shutdown() {
        if (logManager != null) {
            logManager.shutdown();
            logManager = null;
        }
    }
}
