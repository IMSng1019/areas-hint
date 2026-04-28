package areahint.description;

import areahint.Areashint;
import areahint.command.DimensionalNameCommands;
import areahint.data.AreaData;
import areahint.dimensional.DimensionalNameManager;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.permission.PermissionNodes;
import areahint.permission.PermissionService;
import areahint.util.AreaPermissionUtil;
import areahint.world.WorldFolderManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 服务端域名描述网络处理。
 */
public final class DescriptionServerNetworking {
    public static final int MAX_DESCRIPTION_LENGTH = 32767;
    private static final String TARGET_AREA = "area";
    private static final String TARGET_DIMENSION = "dimension";
    private static final String OPERATION_ADD = "add";
    private static final String OPERATION_DELETE = "delete";
    private static final String NO_DESCRIPTION = "对应域名暂无描述";

    private DescriptionServerNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DESCRIPTION_QUERY,
            (server, player, handler, buf, responseSender) -> {
                String targetType = buf.readString();
                String dimensionType = buf.readString();
                String targetName = buf.readString();
                server.execute(() -> handleQuery(player, targetType, dimensionType, targetName));
            });

        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DESCRIPTION_LIST_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                String operation = buf.readString();
                String targetType = buf.readString();
                String dimensionType = buf.readString();
                String query = buf.readString();
                server.execute(() -> handleListRequest(player, operation, targetType, dimensionType, query));
            });

        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DESCRIPTION_WRITE,
            (server, player, handler, buf, responseSender) -> {
                String targetType = buf.readString();
                String dimensionType = buf.readString();
                String targetName = buf.readString();
                String description = buf.readString(MAX_DESCRIPTION_LENGTH);
                server.execute(() -> handleWrite(player, targetType, dimensionType, targetName, description));
            });

        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DESCRIPTION_DELETE,
            (server, player, handler, buf, responseSender) -> {
                String targetType = buf.readString();
                String dimensionType = buf.readString();
                String targetName = buf.readString();
                server.execute(() -> handleDelete(player, targetType, dimensionType, targetName));
            });
    }

    private static void handleQuery(ServerPlayerEntity player, String targetType, String ignoredDimensionType, String targetName) {
        try {
            if (TARGET_DIMENSION.equals(targetType)) {
                String dimensionId = clean(targetName);
                String displayName = DimensionalNameManager.getDimensionalName(dimensionId);
                Path file = DescriptionFileManager.getDimensionalDescriptionFile(displayName);
                DescriptionData data = DescriptionFileManager.readDescription(file);
                sendQueryResponse(player, displayName, data);
                return;
            }

            String dimensionType = getPlayerDimensionType(player);
            if (dimensionType == null) {
                sendQueryResponse(player, "未知维度", null);
                return;
            }

            AreaContext context = findAreaContext(dimensionType, targetName);
            if (context == null) {
                sendQueryResponse(player, clean(targetName), null);
                return;
            }

            Path file = DescriptionFileManager.getAreaDescriptionFile(dimensionType, context.surfaceName());
            DescriptionData data = DescriptionFileManager.readDescription(file);
            sendQueryResponse(player, context.surfaceName(), data);
        } catch (Exception e) {
            Areashint.LOGGER.error("查询域名描述失败", e);
            sendQueryResponse(player, clean(targetName), null);
        }
    }

    private static void handleListRequest(ServerPlayerEntity player, String operation, String targetType, String ignoredDimensionType, String query) {
        if (TARGET_DIMENSION.equals(targetType)) {
            sendDimensionalList(player, operation, query);
        } else {
            sendAreaList(player, operation, query);
        }
    }

    private static void sendAreaList(ServerPlayerEntity player, String operation, String query) {
        String dimensionType = getPlayerDimensionType(player);
        List<ListEntry> entries = new ArrayList<>();
        if (dimensionType != null) {
            List<AreaData> areas = readAreas(dimensionType);
            String normalizedQuery = normalizeQuery(query);
            boolean admin = player.hasPermissionLevel(2);

            for (AreaData area : areas) {
                if (area == null || area.getName() == null) {
                    continue;
                }
                String surfaceName = getSurfaceName(area);
                if (!matches(normalizedQuery, area.getName(), surfaceName, area.getBaseName())) {
                    continue;
                }
                if (admin || AreaPermissionUtil.canModifyArea(player, area, areas)) {
                    entries.add(new ListEntry(area.getName(), surfaceName, area.getLevel(), area.getBaseName(), area.getSignature(), dimensionType));
                }
            }
            entries.sort(Comparator.comparing(ListEntry::displayName, String.CASE_INSENSITIVE_ORDER));
        }
        sendListResponse(player, operation, TARGET_AREA, dimensionType, entries);
    }

    private static void sendDimensionalList(ServerPlayerEntity player, String operation, String query) {
        String permissionNode = OPERATION_DELETE.equals(operation)
            ? PermissionNodes.DELETE_DIMENSIONALITY_DESCRIPTION
            : PermissionNodes.ADD_DIMENSIONALITY_DESCRIPTION;
        if (!PermissionService.hasCommandPermission(player, permissionNode, 2)) {
            sendListResponse(player, operation, TARGET_DIMENSION, "", List.of());
            return;
        }

        DimensionalNameCommands.syncServerDimensions(player.getServer());
        String normalizedQuery = normalizeQuery(query);
        List<ListEntry> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : DimensionalNameManager.getAllDimensionalNames().entrySet()) {
            String dimensionId = entry.getKey();
            String displayName = entry.getValue();
            if (matches(normalizedQuery, dimensionId, displayName)) {
                entries.add(new ListEntry(dimensionId, displayName, 0, null, null, dimensionId));
            }
        }
        entries.sort(Comparator.comparing(ListEntry::displayName, String.CASE_INSENSITIVE_ORDER));
        sendListResponse(player, operation, TARGET_DIMENSION, "", entries);
    }

    private static void handleWrite(ServerPlayerEntity player, String targetType, String ignoredDimensionType, String targetName, String description) {
        if (description == null || description.trim().isEmpty()) {
            sendMutationResponse(player, false, "描述不能为空");
            return;
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            sendMutationResponse(player, false, "描述过长，最多 32767 个字符");
            return;
        }

        if (TARGET_DIMENSION.equals(targetType)) {
            writeDimensionalDescription(player, targetName, description);
        } else {
            writeAreaDescription(player, targetName, description);
        }
    }

    private static void writeAreaDescription(ServerPlayerEntity player, String areaName, String description) {
        String dimensionType = getPlayerDimensionType(player);
        if (dimensionType == null) {
            sendMutationResponse(player, false, "当前维度不支持普通域名描述");
            return;
        }

        AreaContext context = findAreaContext(dimensionType, areaName);
        if (context == null) {
            sendMutationResponse(player, false, "未找到域名：" + clean(areaName));
            return;
        }
        if (!PermissionService.hasCommandPermission(player, PermissionNodes.ADD_DESCRIPTION, 0)
            || !AreaPermissionUtil.canModifyArea(player, context.area(), context.allAreas())) {
            sendMutationResponse(player, false, "你没有权限修改该域名描述");
            return;
        }

        Path file = DescriptionFileManager.getAreaDescriptionFile(dimensionType, context.surfaceName());
        if (hasSurfaceNameConflict(file, context.surfaceName())) {
            sendMutationResponse(player, false, "描述文件名冲突：清理后的文件名已被其他域名描述使用");
            return;
        }

        DescriptionData data = createDescriptionData(TARGET_AREA, context.area().getName(), context.surfaceName(), dimensionType, description, player);
        sendMutationResponse(player, DescriptionFileManager.writeDescription(file, data), "描述已保存：" + context.surfaceName());
    }

    private static void writeDimensionalDescription(ServerPlayerEntity player, String dimensionId, String description) {
        if (!PermissionService.hasCommandPermission(player, PermissionNodes.ADD_DIMENSIONALITY_DESCRIPTION, 2)) {
            sendMutationResponse(player, false, "你没有权限修改维度域名描述");
            return;
        }

        String cleanDimensionId = clean(dimensionId);
        String displayName = DimensionalNameManager.getDimensionalName(cleanDimensionId);
        Path file = DescriptionFileManager.getDimensionalDescriptionFile(displayName);
        if (hasSurfaceNameConflict(file, displayName)) {
            sendMutationResponse(player, false, "描述文件名冲突：清理后的文件名已被其他维度域名描述使用");
            return;
        }

        DescriptionData data = createDescriptionData(TARGET_DIMENSION, cleanDimensionId, displayName, cleanDimensionId, description, player);
        sendMutationResponse(player, DescriptionFileManager.writeDescription(file, data), "维度域名描述已保存：" + displayName);
    }

    private static void handleDelete(ServerPlayerEntity player, String targetType, String ignoredDimensionType, String targetName) {
        if (TARGET_DIMENSION.equals(targetType)) {
            deleteDimensionalDescription(player, targetName);
        } else {
            deleteAreaDescription(player, targetName);
        }
    }

    private static void deleteAreaDescription(ServerPlayerEntity player, String areaName) {
        String dimensionType = getPlayerDimensionType(player);
        if (dimensionType == null) {
            sendMutationResponse(player, false, "当前维度不支持普通域名描述");
            return;
        }

        AreaContext context = findAreaContext(dimensionType, areaName);
        if (context == null) {
            sendMutationResponse(player, false, "未找到域名：" + clean(areaName));
            return;
        }
        if (!PermissionService.hasCommandPermission(player, PermissionNodes.DELETE_DESCRIPTION, 0)
            || !AreaPermissionUtil.canModifyArea(player, context.area(), context.allAreas())) {
            sendMutationResponse(player, false, "你没有权限删除该域名描述");
            return;
        }

        Path file = DescriptionFileManager.getAreaDescriptionFile(dimensionType, context.surfaceName());
        boolean existed = Files.exists(file);
        boolean success = DescriptionFileManager.deleteDescription(file);
        sendMutationResponse(player, success, existed ? "描述已删除：" + context.surfaceName() : "该域名暂无描述或已删除");
    }

    private static void deleteDimensionalDescription(ServerPlayerEntity player, String dimensionId) {
        if (!PermissionService.hasCommandPermission(player, PermissionNodes.DELETE_DIMENSIONALITY_DESCRIPTION, 2)) {
            sendMutationResponse(player, false, "你没有权限删除维度域名描述");
            return;
        }

        String cleanDimensionId = clean(dimensionId);
        String displayName = DimensionalNameManager.getDimensionalName(cleanDimensionId);
        Path file = DescriptionFileManager.getDimensionalDescriptionFile(displayName);
        boolean existed = Files.exists(file);
        boolean success = DescriptionFileManager.deleteDescription(file);
        sendMutationResponse(player, success, existed ? "维度域名描述已删除：" + displayName : "该域名暂无描述或已删除");
    }

    private static DescriptionData createDescriptionData(String targetType, String targetName, String surfaceName,
                                                         String dimension, String description, ServerPlayerEntity player) {
        DescriptionData data = new DescriptionData();
        data.setSchemaVersion(1);
        data.setTargetType(targetType);
        data.setTargetName(targetName);
        data.setSurfaceName(surfaceName);
        data.setDimension(dimension);
        data.setDescription(description);
        data.setAuthor(player.getGameProfile().getName());
        data.setUpdatedAt(System.currentTimeMillis());
        return data;
    }

    private static AreaContext findAreaContext(String dimensionType, String areaName) {
        List<AreaData> areas = readAreas(dimensionType);
        AreaData area = AreaPermissionUtil.findByName(areas, stripQuotes(areaName));
        return area == null ? null : new AreaContext(area, areas, getSurfaceName(area));
    }

    private static List<AreaData> readAreas(String dimensionType) {
        String fileName = Packets.getFileNameForDimension(dimensionType);
        if (fileName == null) {
            return new ArrayList<>();
        }
        return FileManager.readAreaData(WorldFolderManager.getWorldDimensionFile(fileName));
    }

    private static String getPlayerDimensionType(ServerPlayerEntity player) {
        return Packets.convertDimensionPathToType(player.getWorld().getRegistryKey().getValue().getPath());
    }

    private static String getSurfaceName(AreaData area) {
        String surfaceName = area.getSurfacename();
        return surfaceName == null || surfaceName.trim().isEmpty() ? area.getName() : surfaceName;
    }

    private static boolean hasSurfaceNameConflict(Path file, String surfaceName) {
        if (!Files.exists(file)) {
            return false;
        }
        DescriptionData existing = DescriptionFileManager.readDescription(file);
        return existing != null
            && existing.getSurfaceName() != null
            && !existing.getSurfaceName().equals(surfaceName);
    }

    private static void sendQueryResponse(ServerPlayerEntity player, String title, DescriptionData data) {
        String description = data == null || data.getDescription() == null || data.getDescription().trim().isEmpty()
            ? NO_DESCRIPTION
            : clamp(data.getDescription());
        String author = data == null || data.getAuthor() == null || data.getAuthor().trim().isEmpty()
            ? "Areas Hint"
            : data.getAuthor();

        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(clamp(title == null || title.isBlank() ? "域名描述" : title));
        buffer.writeString(clamp(author));
        buffer.writeString(description);
        ServerPlayNetworking.send(player, Packets.S2C_DESCRIPTION_QUERY_RESPONSE, buffer);
    }

    private static void sendListResponse(ServerPlayerEntity player, String operation, String targetType, String dimensionType, List<ListEntry> entries) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(clean(operation));
        buffer.writeString(clean(targetType));
        buffer.writeString(clean(dimensionType));
        buffer.writeInt(entries.size());
        for (ListEntry entry : entries) {
            buffer.writeString(clean(entry.id()));
            buffer.writeString(clean(entry.displayName()));
            buffer.writeInt(entry.level());
            buffer.writeString(clean(entry.baseName()));
            buffer.writeString(clean(entry.signature()));
            buffer.writeString(clean(entry.dimension()));
        }
        ServerPlayNetworking.send(player, Packets.S2C_DESCRIPTION_AREA_LIST, buffer);
    }

    private static void sendMutationResponse(ServerPlayerEntity player, boolean success, String message) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeBoolean(success);
        buffer.writeString(clamp(message));
        ServerPlayNetworking.send(player, Packets.S2C_DESCRIPTION_MUTATION_RESPONSE, buffer);
    }

    private static String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matches(String normalizedQuery, String... values) {
        if (normalizedQuery == null || normalizedQuery.isEmpty()) {
            return true;
        }
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static String clamp(String value) {
        String cleanValue = clean(value);
        return cleanValue.length() > MAX_DESCRIPTION_LENGTH ? cleanValue.substring(0, MAX_DESCRIPTION_LENGTH) : cleanValue;
    }

    private static String stripQuotes(String value) {
        String cleaned = clean(value).trim();
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        }
        return cleaned;
    }

    private record AreaContext(AreaData area, List<AreaData> allAreas, String surfaceName) {
    }

    private record ListEntry(String id, String displayName, int level, String baseName, String signature, String dimension) {
    }
}
