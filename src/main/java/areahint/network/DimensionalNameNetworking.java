package areahint.network;

import areahint.Areashint;
import areahint.data.DimensionalNameData;
import areahint.dimensional.DimensionalNameManager;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 维度域名网络传输
 * 负责在服务端和客户端之间同步维度域名配置
 */
public class DimensionalNameNetworking {
    // 网络包标识符
    public static final String S2C_DIMENSIONAL_NAMES = "areashint:dimensional_names";
    
    private static final Gson GSON = new Gson();
    
    /**
     * 初始化网络处理
     */
    public static void init() {
        Areashint.LOGGER.info("维度域名网络处理初始化完成");
    }
    
    /**
     * 向所有在线客户端发送维度域名配置
     * @param server 服务器实例
     */
    public static void sendDimensionalNamesToAllClients(MinecraftServer server) {
        try {
            // 获取所有维度域名配置
            Map<String, String> dimensionalNames = DimensionalNameManager.getAllDimensionalNames();
            
            // 转换为数据传输格式
            List<DimensionalNameData> dataList = new ArrayList<>();
            for (Map.Entry<String, String> entry : dimensionalNames.entrySet()) {
                dataList.add(new DimensionalNameData(entry.getKey(), entry.getValue()));
            }
            
            // 序列化为JSON
            String jsonData = GSON.toJson(dataList);
            
            // 向所有在线玩家发送
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                sendDimensionalNamesToClient(player, jsonData);
            }
            
            Areashint.LOGGER.info("已向 {} 个在线客户端发送维度域名配置", 
                server.getPlayerManager().getPlayerList().size());
                
        } catch (Exception e) {
            Areashint.LOGGER.error("发送维度域名配置到客户端失败", e);
        }
    }
    
    /**
     * 向指定客户端发送维度域名配置
     * @param player 目标玩家
     */
    public static void sendDimensionalNamesToClient(ServerPlayerEntity player) {
        try {
            // 获取所有维度域名配置
            Map<String, String> dimensionalNames = DimensionalNameManager.getAllDimensionalNames();
            
            // 转换为数据传输格式
            List<DimensionalNameData> dataList = new ArrayList<>();
            for (Map.Entry<String, String> entry : dimensionalNames.entrySet()) {
                dataList.add(new DimensionalNameData(entry.getKey(), entry.getValue()));
            }
            
            // 序列化为JSON
            String jsonData = GSON.toJson(dataList);
            
            sendDimensionalNamesToClient(player, jsonData);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送维度域名配置到客户端失败: " + player.getName().getString(), e);
        }
    }
    
    /**
     * 向指定客户端发送维度域名配置（内部方法）
     * @param player 目标玩家
     * @param jsonData JSON数据
     */
    private static void sendDimensionalNamesToClient(ServerPlayerEntity player, String jsonData) {
        try {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeString(jsonData);
            ServerPlayNetworking.send(player, new Identifier(S2C_DIMENSIONAL_NAMES), buffer);
                
            Areashint.LOGGER.debug("已向客户端 {} 发送维度域名配置", player.getName().getString());
            
        } catch (Exception e) {
            Areashint.LOGGER.error("发送维度域名配置到客户端失败: " + player.getName().getString(), e);
        }
    }
    
    /**
     * 获取网络包标识符（供Packets类使用）
     * @return 网络包标识符
     */
    public static String getPacketId() {
        return S2C_DIMENSIONAL_NAMES;
    }
} 