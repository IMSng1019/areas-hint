package areahint.commandui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 指令可视化注册表，后续单个指令只替换对应 handler 即可扩展图形流程。
 */
public final class CommandVisualRegistry {
    private static final List<CommandVisualHandler> HANDLERS = createHandlers();

    private CommandVisualRegistry() {
    }

    public static List<CommandVisualHandler> getHandlers() {
        return HANDLERS;
    }

    public static CommandVisualHandler getById(String id) {
        for (CommandVisualHandler handler : HANDLERS) {
            if (handler.id().equals(id)) {
                return handler;
            }
        }
        return null;
    }

    private static List<CommandVisualHandler> createHandlers() {
        List<CommandVisualHandler> handlers = new ArrayList<>();
        handlers.add(visual("help", "areahint help", "help.command.help", HelpCommandScreen::new));
        handlers.add(visual("settings", null, "commandui.command.settings.description",
            AreasHintSettingsBridge::open));
        handlers.add(visual("boundviz", "areahint boundviz", "help.command.boundviz", BoundVizCommandScreen::new));
        handlers.add(visual("language", "areahint language", "help.command.language", LanguageCommandScreen::new));
        handlers.add(command("on", "areahint on", "command.usage.on"));
        handlers.add(command("off", "areahint off", "command.usage.off"));
        handlers.add(command("reload", "areahint reload", "help.command.reload"));
        handlers.add(command("delete", "areahint delete", "help.command.delete"));
        handlers.add(command("frequency", "areahint frequency", "help.command.frequency"));
        handlers.add(command("hintrender", "areahint hintrender", "help.command.hintrender"));
        handlers.add(command("titlestyle", "areahint titlestyle", "help.command.titlestyle"));
        handlers.add(command("titlesize", "areahint titlesize", "help.command.titlesize"));
        handlers.add(command("addsubtitle", "areahint addsubtitle", "help.command.addsubtitle"));
        handlers.add(command("replacesubtitle", "areahint replacesubtitle", "help.command.replacesubtitle"));
        handlers.add(command("deletesubtitle", "areahint deletesubtitle", "help.command.deletesubtitle"));
        handlers.add(command("replacesubtitlecolor", "areahint replacesubtitlecolor", "help.command.replacesubtitlecolor"));
        handlers.add(command("replacesubtitlesize", "areahint replacesubtitlesize", "help.command.replacesubtitlesize"));
        handlers.add(command("add", "areahint add", "help.command.add"));
        handlers.add(command("easyadd", "areahint easyadd", "help.command.easyadd"));
        handlers.add(command("addarea", "areahint addarea", "help.command.addarea"));
        handlers.add(command("recolor", "areahint recolor", "help.command.recolor"));
        handlers.add(command("rename", "areahint rename", "help.command.rename"));
        handlers.add(command("sethigh", "areahint sethigh", "help.command.sethigh"));
        handlers.add(command("tcp", "areahint tcp", "help.command.tcp"));
        handlers.add(command("udp", "areahint udp", "help.command.udp"));
        handlers.add(command("settp", "areahint settp", "help.command.settp"));
        handlers.add(command("replacebutton", "areahint replacebutton", "help.command.replacebutton"));
        handlers.add(command("check", "areahint check", "help.command.check"));
        handlers.add(command("dimensionalityname", "areahint dimensionalityname", "help.command.dimensionalityname"));
        handlers.add(command("dimensionalitycolor", "areahint dimensionalitycolor", "help.command.dimensionalitycolor"));
        handlers.add(command("expandarea", "areahint expandarea", "help.command.expandarea"));
        handlers.add(command("shrinkarea", "areahint shrinkarea", "help.command.shrinkarea"));
        handlers.add(command("dividearea", "areahint dividearea", "help.command.dividearea"));
        handlers.add(command("addhint", "areahint addhint", "help.command.addhint"));
        handlers.add(command("deletehint", "areahint deletehint", "help.command.deletehint"));
        handlers.add(command("firstdimname", "areahint firstdimname", "help.command.firstdimname"));
        handlers.add(command("firstdimname_skip", "areahint firstdimname_skip", "help.command.firstdimname_skip"));
        handlers.add(command("debug", "areahint debug", "help.command.debug"));
        handlers.add(command("adddescription", "areahint adddescription", "help.command.adddescription"));
        handlers.add(command("replacedescription", "areahint replacedescription", "help.command.replacedescription"));
        handlers.add(command("deletedescription", "areahint deletedescription", "help.command.deletedescription"));
        handlers.add(command("adddimensionalitydescription", "areahint adddimensionalitydescription", "help.command.adddimensionalitydescription"));
        handlers.add(command("replacedimensionalitydescription", "areahint replacedimensionalitydescription", "help.command.replacedimensionalitydescription"));
        handlers.add(command("deletedimensionalitydescription", "areahint deletedimensionalitydescription", "help.command.deletedimensionalitydescription"));
        handlers.add(command("addsignature", "areahint addsignature", "help.command.addsignature"));
        handlers.add(command("deletesignature", "areahint deletesignature", "help.command.deletesignature"));
        handlers.add(command("serverlanguage", "areahint serverlanguage", "help.command.serverlanguage"));
        return Collections.unmodifiableList(handlers);
    }

    private static CommandVisualHandler command(String id, String defaultCommand, String descriptionKey) {
        return new SimpleHandler(id, defaultCommand, descriptionKey, false,
            parent -> MinecraftClient.getInstance().setScreen(new PlaceholderCommandScreen(parent, getById(id))));
    }

    private static CommandVisualHandler visual(String id, String defaultCommand, String descriptionKey, Consumer<Screen> opener) {
        return new SimpleHandler(id, defaultCommand, descriptionKey, true, opener);
    }

    private record SimpleHandler(String id, String defaultCommand, String descriptionKey, boolean hasVisualFlow,
                                 Consumer<Screen> opener) implements CommandVisualHandler {
        @Override
        public void open(Screen parent) {
            opener.accept(parent);
        }
    }
}
