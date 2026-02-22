package areahint.language;

import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import areahint.AreashintClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * 语言选择交互式管理器
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
     * 启动语言选择流程
     */
    public void startLanguageSelection() {
        if (currentState != State.IDLE) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of(I18nManager.translate("language.error.language")), false);
            }
            return;
        }

        currentState = State.SELECTING_LANGUAGE;
        LanguageUI.showLanguageSelectionScreen();
    }

    /**
     * 处理语言选择
     * @param langCode 语言代码
     */
    public void handleLanguageSelection(String langCode) {
        if (currentState != State.SELECTING_LANGUAGE) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 保存语言设置
        ClientConfig.setLanguage(langCode);

        // 重新加载语言
        I18nManager.loadLanguage(langCode);

        // 同步语言给服务端
        areahint.network.ClientNetworking.sendLanguageToServer();

        String displayName = I18nManager.getLanguageDisplayName(langCode);
        client.player.sendMessage(Text.of(I18nManager.translate("language.message.language") + displayName + " (" + langCode + ")"), false);

        resetState();
    }

    /**
     * 取消语言选择
     */
    public void cancelLanguageSelection() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.of(I18nManager.translate("language.prompt.cancel.language")), false);
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
