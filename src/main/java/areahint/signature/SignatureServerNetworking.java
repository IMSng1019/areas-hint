package areahint.signature;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import areahint.permission.PermissionNodes;
import areahint.permission.PermissionService;
import areahint.util.AreaDataConverter;
import areahint.util.AreaPermissionUtil;
import areahint.world.WorldFolderManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.List;

import static areahint.network.TranslatableMessage.lit;

/**
 * Signature服务端网络处理器。
 */
public class SignatureServerNetworking {

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
            Packets.SIGNATURE_AREA_CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String operation = buf.readString(32767);
                    String jsonStr = buf.readString(32767);
                    String dimension = buf.readString(32767);
                    String targetPlayerName = buf.readString(32767);
                    server.execute(() -> handleRequest(player, operation, jsonStr, dimension, targetPlayerName));
                } catch (Exception e) {
                    sendResponse(player, false, lit("签名请求解析失败："), lit(e.getMessage()));
                }
            }
        );
    }

    private static void handleRequest(ServerPlayerEntity player, String operation, String jsonStr, String dimension, String targetPlayerName) {
        try {
            String normalizedOperation = operation == null ? "" : operation.trim();
            if (!"add".equals(normalizedOperation) && !"delete".equals(normalizedOperation)) {
                sendResponse(player, false, lit("未知签名操作："), lit(String.valueOf(operation)));
                return;
            }

            String target = targetPlayerName == null ? "" : targetPlayerName.trim();
            if (target.isEmpty()) {
                sendResponse(player, false, lit("玩家名不能为空"));
                return;
            }

            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            AreaData updatedArea = AreaDataConverter.fromJsonObject(json);
            if (updatedArea == null || updatedArea.getName() == null || updatedArea.getName().trim().isEmpty()) {
                sendResponse(player, false, lit("无效的域名数据"));
                return;
            }

            String dimType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimType);
            if (fileName == null) {
                sendResponse(player, false, lit("未知维度："), lit(String.valueOf(dimension)));
                return;
            }

            Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            AreaData storedArea = AreaPermissionUtil.findByName(areas, updatedArea.getName());
            if (storedArea == null) {
                sendResponse(player, false, lit("未找到域名："), lit(updatedArea.getName()));
                return;
            }

            String node = "add".equals(normalizedOperation) ? PermissionNodes.ADDSIGNATURE : PermissionNodes.DELETESIGNATURE;
            if (!PermissionService.hasNodeOr(player, node, () -> AreaPermissionUtil.canModifyArea(player, storedArea, areas))) {
                sendResponse(player, false, lit("你没有权限修改该域名签名"));
                return;
            }

            if (!matchesImmutableFields(storedArea, updatedArea)) {
                sendResponse(player, false, lit("签名请求包含非法域名字段修改"));
                return;
            }

            if ("add".equals(normalizedOperation)) {
                if (hasAnySignature(storedArea.getAllSignatures(), target)) {
                    sendResponse(player, false, lit("签名已存在："), lit(target));
                    return;
                }
                storedArea.addSignature(target);
            } else {
                if (!hasExtensionSignature(storedArea.getSignatures(), target)) {
                    sendResponse(player, false, lit("扩展签名中不存在："), lit(target));
                    return;
                }
                storedArea.removeSignature(target);
            }

            if (!FileManager.writeAreaData(areaFile, areas)) {
                sendResponse(player, false, lit("保存域名签名失败"));
                return;
            }

            ServerNetworking.sendAllAreaDataToAll();
            sendResponse(player, true,
                lit("add".equals(normalizedOperation) ? "签名添加成功：" : "签名删除成功："),
                lit(target));
        } catch (Exception e) {
            sendResponse(player, false, lit("签名操作失败："), lit(e.getMessage()));
        }
    }

    private static String convertDimensionIdToType(String dimension) {
        if (dimension == null) return null;
        String normalizedDimension = dimension.trim().toLowerCase();
        if ("overworld".equals(normalizedDimension) || "minecraft:overworld".equals(normalizedDimension)) {
            return Packets.DIMENSION_OVERWORLD;
        }
        if ("the_nether".equals(normalizedDimension) || "minecraft:the_nether".equals(normalizedDimension)) {
            return Packets.DIMENSION_NETHER;
        }
        if ("the_end".equals(normalizedDimension) || "minecraft:the_end".equals(normalizedDimension)) {
            return Packets.DIMENSION_END;
        }
        return null;
    }

    private static boolean matchesImmutableFields(AreaData storedArea, AreaData updatedArea) {
        return equalsText(storedArea.getName(), updatedArea.getName())
            && equalsVertices(storedArea.getVertices(), updatedArea.getVertices())
            && equalsVertices(storedArea.getSecondVertices(), updatedArea.getSecondVertices())
            && equalsAltitude(storedArea.getAltitude(), updatedArea.getAltitude())
            && storedArea.getLevel() == updatedArea.getLevel()
            && equalsText(storedArea.getBaseName(), updatedArea.getBaseName())
            && equalsText(storedArea.getSignature(), updatedArea.getSignature())
            && equalsText(storedArea.getColor(), updatedArea.getColor())
            && equalsText(storedArea.getSurfacename(), updatedArea.getSurfacename());
    }

    private static boolean equalsText(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    private static boolean equalsVertices(List<AreaData.Vertex> list1, List<AreaData.Vertex> list2) {
        if (list1 == null) return list2 == null;
        if (list2 == null || list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            AreaData.Vertex vertex1 = list1.get(i);
            AreaData.Vertex vertex2 = list2.get(i);
            if (vertex1 == null || vertex2 == null) {
                if (vertex1 != vertex2) return false;
                continue;
            }
            if (Double.compare(vertex1.getX(), vertex2.getX()) != 0
                || Double.compare(vertex1.getZ(), vertex2.getZ()) != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsAltitude(AreaData.AltitudeData altitude1, AreaData.AltitudeData altitude2) {
        if (altitude1 == null) return altitude2 == null;
        if (altitude2 == null) return false;
        return equalsDouble(altitude1.getMax(), altitude2.getMax())
            && equalsDouble(altitude1.getMin(), altitude2.getMin());
    }

    private static boolean equalsDouble(Double a, Double b) {
        if (a == null) return b == null;
        return b != null && Double.compare(a, b) == 0;
    }

    private static boolean hasExtensionSignature(List<String> signatures, String target) {
        String cleanedTarget = cleanSignature(target);
        if (cleanedTarget == null || signatures == null) return false;
        for (String signature : signatures) {
            if (cleanedTarget.equals(cleanSignature(signature))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnySignature(List<String> signatures, String target) {
        return hasExtensionSignature(signatures, target);
    }

    private static String cleanSignature(String signature) {
        if (signature == null) return null;
        String cleaned = signature.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static void sendResponse(ServerPlayerEntity player, boolean success, Part... parts) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(success);
            TranslatableMessage.write(buf, parts);
            ServerPlayNetworking.send(player, Packets.SIGNATURE_AREA_RESPONSE_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
