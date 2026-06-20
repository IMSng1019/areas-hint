package areahint.commandui;

import areahint.gui.AreasHintConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * 设置入口桥接，保证主面板和注册表都打开同一个现有配置界面。
 */
final class AreasHintSettingsBridge {
    private AreasHintSettingsBridge() {
    }

    static void open(Screen parent) {
        MinecraftClient.getInstance().setScreen(new AreasHintConfigScreen(parent));
    }
}
