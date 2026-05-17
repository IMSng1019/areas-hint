package areahint.signature;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import areahint.permission.PermissionNodes;
import areahint.permission.PermissionService;
import areahint.util.AreaPermissionUtil;
import areahint.world.WorldFolderManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.List;

import static areahint.network.TranslatableMessage.lit;

/**
 * Signature服务端网络处理器。
 *
 * <p>客户端只提交“操作、域名、维度、目标玩家名”，服务端重新读取世界文件中的完整域名数据，
 * 再执行权限检查和写入。这样可以避免客户端伪造完整域名JSON修改其他字段。</p>
 */
public class SignatureServerNetworking {

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
            Packets.SIGNATURE_AREA_CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String operation = buf.readString(32767);
                    String areaName = buf.readString(32767);
                    String dimension = buf.readString(32767);
                    String targetPlayerName = buf.readString(32767);
                    server.execute(() -> handleRequest(player, operation, areaName, dimension, targetPlayerName));
                } catch (Exception e) {
                    sendResponse(player, false, lit("签名请求解析失败："), lit(e.getMessage()));
                }
            }
        );
    }

    /**
     * 处理添加/删除扩展签名请求。
     */
    private static void handleRequest(ServerPlayerEntity player, String operation, String areaName,
                                      String dimension, String targetPlayerName) {
        try {
            String normalizedOperation = cleanText(operation);
            if (!"add".equals(normalizedOperation) && !"delete".equals(normalizedOperation)) {
                sendResponse(player, false, lit("未知签名操作："), lit(String.valueOf(operation)));
                return;
            }

            String cleanedAreaName = cleanText(areaName);
            if (cleanedAreaName == null) {
                sendResponse(player, false, lit("域名不能为空"));
                return;
            }

            String target = cleanText(targetPlayerName);
            if (target == null) {
                sendResponse(player, false, lit("玩家名不能为空"));
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
            AreaData storedArea = AreaPermissionUtil.findByName(areas, cleanedAreaName);
            if (storedArea == null) {
                sendResponse(player, false, lit("未找到域名："), lit(cleanedAreaName));
                return;
            }

            String node = "add".equals(normalizedOperation)
                ? PermissionNodes.ADDSIGNATURE
                : PermissionNodes.DELETESIGNATURE;
            if (!PermissionService.hasNodeOr(player, node, () -> canModifySignatureArea(player, storedArea, areas))) {
                sendResponse(player, false, lit("你没有权限修改该域名签名"));
                return;
            }

            if ("add".equals(normalizedOperation)) {
                if (storedArea.hasSignature(target)) {
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

    /**
     * 签名扩展权限：
     * 管理员可修改全部域名；普通玩家只能修改base-name指向了自己签名域名的下级域名。
     */
    private static boolean canModifySignatureArea(ServerPlayerEntity player, AreaData area, List<AreaData> allAreas) {
        if (player == null || area == null) {
            return false;
        }
        if (player.hasPermissionLevel(2)) {
            return true;
        }

        String baseName = cleanText(area.getBaseName());
        if (baseName == null) {
            return false;
        }

        String playerName = player.getGameProfile().getName();
        AreaData baseArea = AreaPermissionUtil.findByName(allAreas, baseName);
        return baseArea != null && baseArea.hasSignature(playerName);
    }

    private static String convertDimensionIdToType(String dimension) {
        if (dimension == null) {
            return null;
        }
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

    private static boolean hasExtensionSignature(List<String> signatures, String target) {
        String cleanedTarget = cleanText(target);
        if (cleanedTarget == null || signatures == null) {
            return false;
        }
        for (String signature : signatures) {
            if (cleanedTarget.equals(cleanText(signature))) {
                return true;
            }
        }
        return false;
    }

    private static String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
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
