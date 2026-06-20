package areahint.commandui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * 指令可视化通用动作，所有按钮都从这里回到原指令流程。
 */
public final class CommandUiActions {
    private CommandUiActions() {
    }

    public static void runCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.networkHandler == null || command == null) {
            return;
        }

        String normalizedCommand = command.trim();
        if (normalizedCommand.startsWith("/")) {
            normalizedCommand = normalizedCommand.substring(1);
        }
        if (!normalizedCommand.isEmpty()) {
            client.player.networkHandler.sendChatCommand(normalizedCommand);
        }
    }

    public static void runCommandAndClose(Screen screen, String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.setScreen(null);
        }
        runCommand(command);
    }
}
