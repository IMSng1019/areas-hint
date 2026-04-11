package areahint.permission;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BooleanSupplier;

/**
 * 统一权限判断入口。
 */
public final class PermissionService {
    private PermissionService() {
    }

    public static boolean hasCommandPermission(ServerCommandSource source, String node, int fallbackLevel) {
        if (source == null) {
            return false;
        }
        return resolve(node, source.getPlayer(), () -> source.hasPermissionLevel(fallbackLevel));
    }

    public static boolean hasCommandPermission(ServerPlayerEntity player, String node, int fallbackLevel) {
        if (player == null) {
            return false;
        }
        return resolve(node, player, () -> player.hasPermissionLevel(fallbackLevel));
    }

    public static boolean hasNodeOr(ServerPlayerEntity player, String node, BooleanSupplier fallbackRule) {
        if (player == null) {
            return false;
        }
        return resolve(node, player, fallbackRule);
    }

    public static boolean hasNodeOr(ServerCommandSource source, String node, BooleanSupplier fallbackRule) {
        if (source == null) {
            return false;
        }
        return resolve(node, source.getPlayer(), fallbackRule);
    }

    private static boolean resolve(String node, ServerPlayerEntity player, BooleanSupplier fallbackRule) {
        LuckPermsCompat.Result result = LuckPermsCompat.checkPermission(player, node);
        if (result == LuckPermsCompat.Result.TRUE) {
            return true;
        }
        if (result == LuckPermsCompat.Result.FALSE) {
            return false;
        }
        return fallbackRule.getAsBoolean();
    }
}
