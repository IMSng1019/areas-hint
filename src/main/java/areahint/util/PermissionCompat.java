package areahint.util;

import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PermissionCompat {
    private PermissionCompat() {
    }

    public static boolean hasPermissionLevel(ServerCommandSource source, int level) {
        return source.getPermissions().hasPermission(permissionForLevel(level));
    }

    public static boolean hasPermissionLevel(ServerPlayerEntity player, int level) {
        return player.getPermissions().hasPermission(permissionForLevel(level));
    }

    public static boolean hasPermissionLevel(PlayerEntity player, int level) {
        return player != null && player.getPermissions().hasPermission(permissionForLevel(level));
    }

    private static Permission permissionForLevel(int level) {
        if (level == 1) return DefaultPermissions.MODERATORS;
        if (level == 2) return DefaultPermissions.GAMEMASTERS;
        if (level == 3) return DefaultPermissions.ADMINS;
        if (level == 4) return DefaultPermissions.OWNERS;
        return new Permission.Level(PermissionLevel.fromLevel(level));
    }
}
