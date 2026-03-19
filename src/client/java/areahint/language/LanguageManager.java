package areahint.language;

import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * 璇█閫夋嫨浜や簰寮忕鐞嗗櫒
 */
public class LanguageManager {
    public enum State {
        IDLE,
        SELECTING_LANGUAGE
    }

    private static LanguageManager instance;
    private State currentState = State.IDLE;

    private LanguageManager() {}

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    /**
     * 鍚姩璇█閫夋嫨娴佺▼
     */
    public void startLanguageSelection() {
        if (currentState != State.IDLE) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("language.error.language")), false);
            }
            return;
        }

        currentState = State.SELECTING_LANGUAGE;
        LanguageUI.showLanguageSelectionScreen();
    }

    /**
     * 澶勭悊璇█閫夋嫨
     * @param langCode 璇█浠ｇ爜
     */
    public void handleLanguageSelection(String langCode) {
        if (currentState != State.SELECTING_LANGUAGE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 淇濆瓨璇█璁剧疆
        ClientConfig.setLanguage(langCode);

        // 閲嶆柊鍔犺浇璇█
        I18nManager.loadLanguage(langCode);

        // 鍚屾璇█缁欐湇鍔＄
        areahint.network.ClientNetworking.sendLanguageToServer();

        String displayName = I18nManager.getLanguageDisplayName(langCode);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("language.message.language") + displayName + " (" + langCode + ")"), false);

        resetState();
    }

    /**
     * 鍙栨秷璇█閫夋嫨
     */
    public void cancelLanguageSelection() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("language.prompt.cancel.language")), false);
        }
        resetState();
    }

    private void resetState() {
        currentState = State.IDLE;
    }

    public State getCurrentState() {
        return currentState;
    }
}
