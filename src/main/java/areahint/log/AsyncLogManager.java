package areahint.log;

import areahint.Areashint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * 异步日志管理器
 * 负责异步写入日志到文件，支持文件大小限制和自动分割
 */
public class AsyncLogManager {
    // 日志文件最大大小（10MB）
    private static final long MAX_LOG_FILE_SIZE = 10 * 1024 * 1024;

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd HH:mm:ss");

    // 异步执行器
    private final ExecutorService executorService;

    // 日志文件夹路径
    private final Path logFolder;

    // 日志类型（server或client）
    private final String logType;

    // 当前日志文件路径
    private Path currentLogFile;

    // 当前日志文件序号
    private int currentFileNumber = 1;

    // 当前日期（用于检测日期变化）
    private String currentDate;

    // 日志保留天数
    private final int retentionDays;

    // 是否已关闭
    private volatile boolean shutdown = false;

    /**
     * 构造函数
     * @param logFolder 日志文件夹路径
     * @param logType 日志类型（server或client）
     * @param retentionDays 日志保留天数
     */
    public AsyncLogManager(Path logFolder, String logType, int retentionDays) {
        this.logFolder = logFolder;
        this.logType = logType;
        this.retentionDays = retentionDays;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "AsyncLogManager-" + logType);
            thread.setDaemon(true);
            return thread;
        });

        // 初始化日志文件
        initLogFile();

        // 清理旧日志
        cleanOldLogs();
    }

    /**
     * 初始化日志文件
     */
    private void initLogFile() {
        try {
            // 确保日志文件夹存在
            if (Files.notExists(logFolder)) {
                Files.createDirectories(logFolder);
                Areashint.LOGGER.info("创建日志文件夹: {}", logFolder);
            }

            // 获取当前日期
            currentDate = LocalDate.now().format(DATE_FORMATTER);

            // 查找当前日期的最大文件序号
            currentFileNumber = findMaxFileNumber(currentDate);

            // 创建或打开日志文件
            currentLogFile = getLogFilePath(currentDate, currentFileNumber);

            // 如果文件已存在且超过大小限制，创建新文件
            if (Files.exists(currentLogFile) && Files.size(currentLogFile) >= MAX_LOG_FILE_SIZE) {
                currentFileNumber++;
                currentLogFile = getLogFilePath(currentDate, currentFileNumber);
            }

            // 确保文件存在
            if (Files.notExists(currentLogFile)) {
                Files.createFile(currentLogFile);
                Areashint.LOGGER.info("创建日志文件: {}", currentLogFile);
            }

        } catch (IOException e) {
            Areashint.LOGGER.error("初始化日志文件失败", e);
        }
    }

    /**
     * 查找指定日期的最大文件序号
     * @param date 日期字符串
     * @return 最大文件序号
     */
    private int findMaxFileNumber(String date) {
        try (Stream<Path> files = Files.list(logFolder)) {
            return files
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.startsWith(date + "_") && name.endsWith(".log"))
                .map(name -> {
                    try {
                        String numberPart = name.substring(date.length() + 1, name.length() - 4);
                        return Integer.parseInt(numberPart);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(1);
        } catch (IOException e) {
            Areashint.LOGGER.error("查找最大文件序号失败", e);
            return 1;
        }
    }

    /**
     * 获取日志文件路径
     * @param date 日期字符串
     * @param fileNumber 文件序号
     * @return 日志文件路径
     */
    private Path getLogFilePath(String date, int fileNumber) {
        return logFolder.resolve(date + "_" + fileNumber + ".log");
    }

    /**
     * 异步写入日志
     * @param message 日志消息
     */
    public void log(String message) {
        if (shutdown) {
            return;
        }

        executorService.submit(() -> {
            try {
                // 检查日期是否变化
                String today = LocalDate.now().format(DATE_FORMATTER);
                if (!today.equals(currentDate)) {
                    currentDate = today;
                    currentFileNumber = 1;
                    currentLogFile = getLogFilePath(currentDate, currentFileNumber);

                    // 清理旧日志
                    cleanOldLogs();
                }

                // 检查文件大小
                if (Files.exists(currentLogFile) && Files.size(currentLogFile) >= MAX_LOG_FILE_SIZE) {
                    currentFileNumber++;
                    currentLogFile = getLogFilePath(currentDate, currentFileNumber);
                }

                // 添加时间戳
                String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
                String logLine = "[" + timestamp + "] " + message + "\n";

                // 写入日志
                Files.writeString(currentLogFile, logLine, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            } catch (IOException e) {
                Areashint.LOGGER.error("写入日志失败", e);
            }
        });
    }

    /**
     * 清理旧日志文件
     */
    private void cleanOldLogs() {
        executorService.submit(() -> {
            try {
                LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);

                try (Stream<Path> files = Files.list(logFolder)) {
                    files.filter(Files::isRegularFile)
                        .filter(file -> {
                            String fileName = file.getFileName().toString();
                            if (!fileName.endsWith(".log")) {
                                return false;
                            }

                            try {
                                // 提取日期部分
                                String datePart = fileName.substring(0, 8);
                                LocalDate fileDate = LocalDate.parse(datePart, DATE_FORMATTER);
                                return fileDate.isBefore(cutoffDate);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                                Areashint.LOGGER.info("删除旧日志文件: {}", file);
                            } catch (IOException e) {
                                Areashint.LOGGER.error("删除旧日志文件失败: {}", file, e);
                            }
                        });
                }
            } catch (IOException e) {
                Areashint.LOGGER.error("清理旧日志失败", e);
            }
        });
    }

    /**
     * 关闭日志管理器
     */
    public void shutdown() {
        shutdown = true;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
