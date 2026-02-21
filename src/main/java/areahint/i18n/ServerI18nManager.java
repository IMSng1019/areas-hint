package areahint.i18n;

import areahint.Areashint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端国际化管理器
 * 从JAR资源加载翻译文件
 */
public class ServerI18nManager {
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_LANGUAGE = "zh_cn";
    private static Map<String, String> translations = new HashMap<>();
    private static String currentLanguage = DEFAULT_LANGUAGE;

    public static void init() {
        loadLanguage(DEFAULT_LANGUAGE);
    }

    public static void loadLanguage(String language) {
        if (language == null || language.isEmpty()) {
            language = DEFAULT_LANGUAGE;
        }
        String path = "assets/areas-hint/lang/" + language + ".json";
        try (InputStream is = ServerI18nManager.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                Areashint.LOGGER.warn("Server lang file not found: " + path);
                if (!language.equals(DEFAULT_LANGUAGE)) {
                    loadLanguage(DEFAULT_LANGUAGE);
                }
                return;
            }
            Map<String, String> loaded = GSON.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                new TypeToken<Map<String, String>>(){}.getType()
            );
            translations = loaded != null ? loaded : new HashMap<>();
            currentLanguage = language;
            Areashint.LOGGER.info("Server loaded language: " + language + " (" + translations.size() + " entries)");
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to load server lang: " + e.getMessage());
            translations = new HashMap<>();
        }
    }

    public static String translate(String key) {
        return translations.getOrDefault(key, key);
    }

    public static String translate(String key, Object... args) {
        String text = translations.getOrDefault(key, key);
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return text;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }
}
