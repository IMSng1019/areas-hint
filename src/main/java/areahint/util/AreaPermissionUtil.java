package areahint.util;

import areahint.data.AreaData;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public final class AreaPermissionUtil {
    private AreaPermissionUtil() {}

    public static boolean isSignedBy(AreaData area, String playerName) {
        return area != null && playerName != null && area.hasSignature(playerName);
    }

    // 语义较宽：允许域名签名者或上级域名签名者修改，不适合 delete/shrink 等特殊权限场景。
    public static boolean canModifyArea(ServerPlayerEntity player, AreaData area, List<AreaData> allAreas) {
        if (player == null || area == null) return false;
        if (player.hasPermissionLevel(2)) return true;
        String playerName = player.getGameProfile().getName();
        if (isSignedBy(area, playerName)) return true;
        AreaData baseArea = findByName(allAreas, area.getBaseName());
        return isSignedBy(baseArea, playerName);
    }

    public static boolean canModifyAreaName(ServerPlayerEntity player, String areaName, List<AreaData> allAreas) {
        return canModifyArea(player, findByName(allAreas, areaName), allAreas);
    }

    public static AreaData findByName(List<AreaData> areas, String name) {
        if (areas == null || name == null) return null;
        for (AreaData area : areas) {
            if (area != null && name.equals(area.getName())) return area;
        }
        return null;
    }
}
