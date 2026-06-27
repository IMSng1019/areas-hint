package areahint.commandui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * help 图形流程控制器，负责打开复用指令元数据的帮助列表界面。
 */
public final class HelpVisualController {
    private HelpVisualController() {
    }

    public static void openFromCommandUi(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.setScreen(new HelpCommandScreen(parent));
        }
    }
}
