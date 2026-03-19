package areahint.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

final class CommandSourceCompat {
    private CommandSourceCompat() {
    }

    static boolean isExecutedByPlayer(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity;
    }

    static ServerPlayerEntity getPlayerOrThrow(ServerCommandSource source) throws CommandSyntaxException {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            return player;
        }

        throw ServerCommandSource.REQUIRES_PLAYER_EXCEPTION.create();
    }

    static void sendMessage(ServerCommandSource source, Text message) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            player.sendMessage(message, false);
            return;
        }

        source.sendFeedback(message, false);
    }
}
