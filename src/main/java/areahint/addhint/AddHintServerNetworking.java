package areahint.addhint;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Path;
import java.util.List;

/**
 * AddHint服务端网络处理器
 */
public class AddHintServerNetworking {

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
            Packets.ADDHINT_AREA_CHANNEL,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String jsonStr = buf.readString(32767);
                    String dimension = buf.readString(32767);
                    server.execute(() -> handleRequest(player, jsonStr, dimension));
                } catch (Exception e) {
                    sendResponse(player, false, "服务端处理错误: " + e.getMessage());
                }
            }
        );
    }

    private static void handleRequest(ServerPlayerEntity player, String jsonStr, String dimension) {
        try {
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            AreaData updatedArea = AreaDataConverter.fromJsonObject(json);

            // 验证基本数据
            if (updatedArea == null || updatedArea.getName() == null) {
                sendResponse(player, false, "域名数据无效");
                return;
            }

            if (updatedArea.getVertices() == null || updatedArea.getVertices().size() < 3) {
                sendResponse(player, false, "顶点数量不足");
                return;
            }

            // 获取维度文件
            String dimType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimType);
            if (fileName == null) {
                sendResponse(player, false, "无效的维度: " + dimension);
                return;
            }

            // 验证权限
            String playerDimType = convertDimensionIdToType(
                player.getWorld().getRegistryKey().getValue().toString());
            if (!validatePermission(player, updatedArea, playerDimType)) {
                sendResponse(player, false, "您没有权限修改此域名");
                return;
            }

            // 读取并更新域名
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            List<AreaData> areas = FileManager.readAreaData(areaFile);

            boolean found = false;
            for (int i = 0; i < areas.size(); i++) {
                if (areas.get(i).getName().equals(updatedArea.getName())) {
                    areas.set(i, updatedArea);
                    found = true;
                    break;
                }
            }

            if (!found) {
                sendResponse(player, false, "未找到域名: " + updatedArea.getName());
                return;
            }

            if (!FileManager.writeAreaData(areaFile, areas)) {
                sendResponse(player, false, "保存文件失败");
                return;
            }

            // 重新分发给所有玩家
            ServerNetworking.sendAllAreaDataToAll();
            sendResponse(player, true, "域名 '" + updatedArea.getName() + "' 顶点更新成功");

        } catch (Exception e) {
            sendResponse(player, false, "处理失败: " + e.getMessage());
        }
    }

    private static boolean validatePermission(ServerPlayerEntity player, AreaData area, String dimType) {
        if (player.hasPermissionLevel(2)) return true;

        String playerName = player.getGameProfile().getName();
        if (playerName.equals(area.getSignature())) return true;

        if (area.getBaseName() != null && dimType != null) {
            try {
                String fileName = Packets.getFileNameForDimension(dimType);
                if (fileName != null) {
                    Path path = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
                    List<AreaData> areas = FileManager.readAreaData(path);
                    for (AreaData a : areas) {
                        if (a.getName().equals(area.getBaseName())) {
                            return playerName.equals(a.getSignature());
                        }
                    }
                }
            } catch (Exception e) { /* ignore */ }
        }
        return false;
    }

    private static String convertDimensionIdToType(String dim) {
        if (dim == null) return null;
        if (dim.contains("overworld")) return Packets.DIMENSION_OVERWORLD;
        if (dim.contains("nether")) return Packets.DIMENSION_NETHER;
        if (dim.contains("end")) return Packets.DIMENSION_END;
        return null;
    }

    private static void sendResponse(ServerPlayerEntity player, boolean success, String message) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(success);
            buf.writeString(message);
            ServerPlayNetworking.send(player, Packets.ADDHINT_AREA_RESPONSE_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
