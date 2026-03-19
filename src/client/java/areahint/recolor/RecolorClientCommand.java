package areahint.recolor;

import areahint.AreashintClient;
import areahint.i18n.I18nManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Recolor鐎广垺鍩涚粩顖氭嚒娴犮倕顦╅悶鍡楁珤
 */
public class RecolorClientCommand {

    /**
     * 濞夈劌鍞界€广垺鍩涚粩顖氭嚒娴?
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("areahint")
            .then(ClientCommandManager.literal("recolor")
                // /areahint recolor select <閸╃喎鎮?
                .then(ClientCommandManager.literal("select")
                    .then(ClientCommandManager.argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> executeSelect(context, StringArgumentType.getString(context, "areaName")))))
                // /areahint recolor color <妫版粏澹?
                .then(ClientCommandManager.literal("color")
                    .then(ClientCommandManager.argument("colorValue", StringArgumentType.greedyString())
                        .executes(context -> executeColor(context, StringArgumentType.getString(context, "colorValue")))))
                // /areahint recolor confirm
                .then(ClientCommandManager.literal("confirm")
                    .executes(RecolorClientCommand::executeConfirm))
                // /areahint recolor cancel
                .then(ClientCommandManager.literal("cancel")
                    .executes(RecolorClientCommand::executeCancel))));
    }

    /**
     * 閹笛嗩攽閸╃喎鎮曢柅澶嬪
     */
    private static int executeSelect(CommandContext<FabricClientCommandSource> context, String areaName) {
        try {
            // 缁夊娅庡鏇炲娇閿涘牆顩ч弸婊冪摠閸︻煉绱?
            if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
                areaName = areaName.substring(1, areaName.length() - 1);
            }

            RecolorManager.getInstance().handleAreaSelection(areaName);
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error(I18nManager.translate("command.message.general_24"), e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.general_3") + e.getMessage()), false);
            }
            return 0;
        }
    }

    /**
     * 閹笛嗩攽妫版粏澹婇柅澶嬪
     */
    private static int executeColor(CommandContext<FabricClientCommandSource> context, String colorValue) {
        try {
            RecolorManager.getInstance().handleColorSelection(colorValue);
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error(I18nManager.translate("command.message.general_22"), e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.general_3") + e.getMessage()), false);
            }
            return 0;
        }
    }

    /**
     * 閹笛嗩攽绾喛顓?
     */
    private static int executeConfirm(CommandContext<FabricClientCommandSource> context) {
        try {
            RecolorManager.getInstance().confirmChange();
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error(I18nManager.translate("command.message.general_23"), e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.general_3") + e.getMessage()), false);
            }
            return 0;
        }
    }

    /**
     * 閹笛嗩攽閸欐牗绉?
     */
    private static int executeCancel(CommandContext<FabricClientCommandSource> context) {
        try {
            RecolorManager.getInstance().cancelRecolor();
            return 1;
        } catch (Exception e) {
            AreashintClient.LOGGER.error(I18nManager.translate("command.message.general_21"), e);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.general_3") + e.getMessage()), false);
            }
            return 0;
        }
    }
}
