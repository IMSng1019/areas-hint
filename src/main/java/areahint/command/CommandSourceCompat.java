package areahint.command;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

final class CommandSourceCompat {
    private CommandSourceCompat() {
    }

    static void sendMessage(ServerCommandSource source, Text message) {
        ServerPlayerEntity player = source.getPlayer();
        if (player != null) {
            player.sendMessage(message, false);
            return;
        }

        source.sendFeedback(message, false);
    }
}
