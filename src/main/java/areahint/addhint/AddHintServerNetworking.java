package areahint.addhint;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import areahint.util.AreaDataConverter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static areahint.network.TranslatableMessage.key;
import static areahint.network.TranslatableMessage.lit;

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
                    sendResponse(player, false, key("addhint.error.general_2"), lit(e.getMessage()));
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
                sendResponse(player, false, key("addhint.error.area_2"));
                return;
            }

            if (updatedArea.getVertices() == null || updatedArea.getVertices().size() < 3) {
                sendResponse(player, false, key("addhint.message.vertex_4"));
                return;
            }

            // 获取维度文件
            String dimType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimType);
            if (fileName == null) {
                sendResponse(player, false, key("addhint.error.dimension"), lit(dimension));
                return;
            }

            // 验证权限
            String playerDimType = convertDimensionIdToType(
                player.getWorld().getRegistryKey().getValue().toString());
            if (!validatePermission(player, updatedArea, playerDimType)) {
                sendResponse(player, false, key("addhint.message.area.modify.permission"));
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
                sendResponse(player, false, key("addhint.message.area_3"), lit(updatedArea.getName()));
                return;
            }

            if (!FileManager.writeAreaData(areaFile, areas)) {
                sendResponse(player, false, key("addhint.error.save"));
                return;
            }

            // 重新分发给所有玩家
            ServerNetworking.sendAllAreaDataToAll();
            sendResponse(player, true, key("addhint.message.area_2"), lit(updatedArea.getName()), key("addhint.success.vertex"));

        } catch (Exception e) {
            sendResponse(player, false, key("addhint.error.general"), lit(e.getMessage()));
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

    private static void sendResponse(ServerPlayerEntity player, boolean success, Part... parts) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(success);
            TranslatableMessage.write(buf, parts);
            ServerPlayNetworking.send(player, Packets.ADDHINT_AREA_RESPONSE_CHANNEL, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
