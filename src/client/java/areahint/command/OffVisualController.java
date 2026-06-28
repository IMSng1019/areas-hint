package areahint.command;

import areahint.commandui.CommandUiActions;
import areahint.commandui.WizardConfirmScreen;
import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * off 指令图形流程控制器，只确认后执行现有 /areahint off。
 */
public final class OffVisualController {
    private OffVisualController() {
    }

    public static void openFromCommandUi(Screen parent) {
        List<String> details = new ArrayList<>();
        details.add(I18nManager.translate("commandui.off.detail"));
        details.add(I18nManager.translate(ClientConfig.isEnabled()
            ? "commandui.off.current.enabled"
            : "commandui.off.current.disabled"));

        setScreen(new WizardConfirmScreen(parent,
            "commandui.off.title",
            I18nManager.translate("commandui.off.prompt"),
            details,
            "commandui.off.confirm",
            () -> CommandUiActions.runCommand("areahint off"),
            null));
    }

    private static void setScreen(Screen screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.setScreen(screen);
        }
    }
}
