package areahint.i18n;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.file.FileManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * 国际化管理器
 * 负责加载和管理翻译文件
 */
public class I18nManager {
    private static final Gson GSON = new Gson();
    private static final String LANG_FOLDER = "lang";
    private static final String DEFAULT_LANGUAGE = "zh_cn";

    // 当前语言的翻译映射
    private static Map<String, String> translations = new HashMap<>();
    // 当前语言
    private static String currentLanguage = DEFAULT_LANGUAGE;

    /**
     * 初始化i18n系统
     */
    public static void init() {
        ensureLangFolder();
        createDefaultLangFile();
        loadLanguage(ClientConfig.getLanguage());
    }

    /**
     * 确保lang文件夹存在
     */
    private static void ensureLangFolder() {
        Path langDir = getLangFolder();
        try {
            if (Files.notExists(langDir)) {
                Files.createDirectories(langDir);
                AreashintClient.LOGGER.info("已创建语言文件夹: " + langDir);
            }
        } catch (IOException e) {
            AreashintClient.LOGGER.error("创建语言文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 创建默认语言文件（空模板）
     */
    private static void createDefaultLangFile() {
        Path zhFile = getLangFolder().resolve("zh_cn.json");
        if (Files.notExists(zhFile)) {
            try {
                // 创建空的翻译模板
                Map<String, String> defaultTranslations = new LinkedHashMap<>();
                defaultTranslations.put("language.name", "简体中文");
                String json = new Gson().newBuilder().setPrettyPrinting().create().toJson(defaultTranslations);
                Files.write(zhFile, json.getBytes(StandardCharsets.UTF_8));
                AreashintClient.LOGGER.info("已创建默认语言文件: zh_cn.json");
            } catch (IOException e) {
                AreashintClient.LOGGER.error("创建默认语言文件失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取语言文件夹路径
     */
    public static Path getLangFolder() {
        return FileManager.getConfigFolder().resolve(LANG_FOLDER);
    }

    /**
     * 加载指定语言
     * @param language 语言代码（如 zh_cn, en_us）
     */
    public static void loadLanguage(String language) {
        if (language == null || language.isEmpty()) {
            language = DEFAULT_LANGUAGE;
        }

        Path langFile = getLangFolder().resolve(language + ".json");
        if (Files.notExists(langFile)) {
            AreashintClient.LOGGER.warn("语言文件不存在: " + language + ".json，使用默认语言");
            language = DEFAULT_LANGUAGE;
            langFile = getLangFolder().resolve(language + ".json");
        }

        if (Files.notExists(langFile)) {
            AreashintClient.LOGGER.warn("默认语言文件也不存在，使用空翻译");
            translations = new HashMap<>();
            currentLanguage = language;
            return;
        }

        try {
            String json = Files.readString(langFile, StandardCharsets.UTF_8);
            Map<String, String> loaded = GSON.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
            translations = loaded != null ? loaded : new HashMap<>();
            currentLanguage = language;
            AreashintClient.LOGGER.info("已加载语言: " + language + " (" + translations.size() + " 条翻译)");
        } catch (Exception e) {
            AreashintClient.LOGGER.error("加载语言文件失败: " + e.getMessage());
            translations = new HashMap<>();
            currentLanguage = language;
        }
    }

    /**
     * 获取翻译文本
     * @param key 翻译键
     * @return 翻译后的文本，如果不存在则返回key本身
     */
    public static String translate(String key) {
        return translations.getOrDefault(key, key);
    }

    /**
     * 获取翻译文本（带参数替换）
     * @param key 翻译键
     * @param args 替换参数（按顺序替换 {0}, {1}, ...）
     */
    public static String translate(String key, Object... args) {
        String text = translations.getOrDefault(key, key);
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return text;
    }

    /**
     * 获取所有可用的语言列表
     * @return 语言代码列表（不含.json后缀）
     */
    public static List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        Path langDir = getLangFolder();

        if (Files.notExists(langDir)) {
            return languages;
        }

        try (Stream<Path> files = Files.list(langDir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                 .forEach(p -> {
                     String fileName = p.getFileName().toString();
                     languages.add(fileName.substring(0, fileName.length() - 5));
                 });
        } catch (IOException e) {
            AreashintClient.LOGGER.error("扫描语言文件夹失败: " + e.getMessage());
        }

        return languages;
    }

    /**
     * 获取语言文件中定义的语言显示名称
     * @param langCode 语言代码
     * @return 显示名称，如果没有定义则返回语言代码
     */
    public static String getLanguageDisplayName(String langCode) {
        Path langFile = getLangFolder().resolve(langCode + ".json");
        if (Files.notExists(langFile)) {
            return langCode;
        }

        try {
            String json = Files.readString(langFile, StandardCharsets.UTF_8);
            Map<String, String> data = GSON.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
            if (data != null && data.containsKey("language.name")) {
                return data.get("language.name");
            }
        } catch (Exception e) {
            // 忽略，返回语言代码
        }
        return langCode;
    }

    /**
     * 获取当前语言代码
     */
    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * 获取默认语言代码
     */
    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }
}
