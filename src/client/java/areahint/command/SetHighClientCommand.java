package areahint.command;

import areahint.network.Packets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.List;
import java.util.ArrayList;

/**
 * 客户端域名高度设置命令处理器
 * 处理来自服务器的高度设置界面数据和用户交互
 */
public class SetHighClientCommand {
    
    /**
     * 处理服务器发送的域名列表
     * @param areaNames 域名名称列表
     * @param dimensionType 维度类型
     */
    public static void handleAreaList(List<String> areaNames, String dimensionType) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (areaNames.isEmpty()) {
            client.player.sendMessage(Text.of("§c当前维度没有您可以修改高度的域名"), false);
            return;
        }
        
        // 显示域名列表
        client.player.sendMessage(Text.of("§6===== 可修改高度的域名列表 ====="), false);
        for (int i = 0; i < areaNames.size(); i++) {
            String areaName = areaNames.get(i);
            client.player.sendMessage(Text.of(String.format("§a%d. §f%s", i + 1, areaName)), false);
        }
        client.player.sendMessage(Text.of("§e请在聊天中输入域名编号或域名名称来选择要修改的域名"), false);
        client.player.sendMessage(Text.of("§7例如：输入 '1' 或 '域名名称'"), false);
    }
    
    /**
     * 处理域名选择
     * @param selectedArea 选择的域名
     * @param hasAltitude 是否有现有高度限制
     * @param maxHeight 当前最大高度
     * @param minHeight 当前最小高度
     */
    public static void handleAreaSelection(String selectedArea, boolean hasAltitude, 
                                         Double maxHeight, Double minHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 显示当前高度设置
        client.player.sendMessage(Text.of("§6===== 域名高度设置: " + selectedArea + " ====="), false);
        
        if (hasAltitude) {
            String maxStr = maxHeight != null ? String.format("%.1f", maxHeight) : "无限制";
            String minStr = minHeight != null ? String.format("%.1f", minHeight) : "无限制";
            client.player.sendMessage(Text.of("§7当前高度: 最高:" + maxStr + ", 最低:" + minStr), false);
        } else {
            client.player.sendMessage(Text.of("§7当前高度: 无限制"), false);
        }
        
        // 显示选项
        client.player.sendMessage(Text.of("§e请选择高度设置类型："), false);
        client.player.sendMessage(Text.of("§a1. §f不限制高度 §7(移除所有高度限制)"), false);
        client.player.sendMessage(Text.of("§a2. §f自定义高度 §7(设置具体的高度范围)"), false);
        client.player.sendMessage(Text.of("§7输入 '1' 或 '2' 来选择"), false);
    }
    
    /**
     * 处理高度类型选择
     * @param areaName 域名名称
     * @param customHeight 是否使用自定义高度
     */
    public static void handleHeightTypeSelection(String areaName, boolean customHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (customHeight) {
            // 自定义高度 - 询问具体数值
            client.player.sendMessage(Text.of("§6===== 设置自定义高度 ====="), false);
            client.player.sendMessage(Text.of("§e请依次输入最高高度和最低高度："), false);
            client.player.sendMessage(Text.of("§7格式: <最高高度> <最低高度>"), false);
            client.player.sendMessage(Text.of("§7留空表示无限制，例如: '100 64' 或 '100 ' 或 ' 64'"), false);
            client.player.sendMessage(Text.of("§7高度范围: -64 到 320"), false);
        } else {
            // 不限制高度 - 直接发送请求
            sendHeightRequest(areaName, false, null, null);
        }
    }
    
    /**
     * 处理自定义高度输入
     * @param areaName 域名名称
     * @param input 用户输入的高度字符串
     */
    public static void handleCustomHeightInput(String areaName, String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        try {
            String[] parts = input.trim().split("\\s+");
            Double maxHeight = null;
            Double minHeight = null;
            
            // 解析输入
            if (parts.length >= 1 && !parts[0].isEmpty()) {
                maxHeight = Double.parseDouble(parts[0]);
            }
            if (parts.length >= 2 && !parts[1].isEmpty()) {
                minHeight = Double.parseDouble(parts[1]);
            }
            
            // 验证输入
            if (maxHeight != null && (maxHeight < -64 || maxHeight > 320)) {
                client.player.sendMessage(Text.of("§c最高高度超出合理范围 [-64, 320]"), false);
                return;
            }
            if (minHeight != null && (minHeight < -64 || minHeight > 320)) {
                client.player.sendMessage(Text.of("§c最低高度超出合理范围 [-64, 320]"), false);
                return;
            }
            if (maxHeight != null && minHeight != null && maxHeight < minHeight) {
                client.player.sendMessage(Text.of("§c最高高度不能小于最低高度"), false);
                return;
            }
            
            // 发送请求
            sendHeightRequest(areaName, true, maxHeight, minHeight);
            
        } catch (NumberFormatException e) {
            client.player.sendMessage(Text.of("§c高度格式错误，请输入有效的数字"), false);
            client.player.sendMessage(Text.of("§7格式: <最高高度> <最低高度>"), false);
        }
    }
    
    /**
     * 发送高度设置请求到服务器
     * @param areaName 域名名称
     * @param hasCustomHeight 是否使用自定义高度
     * @param maxHeight 最大高度
     * @param minHeight 最小高度
     */
    private static void sendHeightRequest(String areaName, boolean hasCustomHeight, 
                                        Double maxHeight, Double minHeight) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(areaName);
            buf.writeBoolean(hasCustomHeight);
            
            if (hasCustomHeight) {
                buf.writeBoolean(maxHeight != null);
                if (maxHeight != null) {
                    buf.writeDouble(maxHeight);
                }
                buf.writeBoolean(minHeight != null);
                if (minHeight != null) {
                    buf.writeDouble(minHeight);
                }
            }
            
            ClientPlayNetworking.send(Packets.C2S_SETHIGH_REQUEST, buf);
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§a正在处理高度设置请求..."), false);
            }
            
        } catch (Exception e) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.of("§c发送高度设置请求时发生错误"), false);
            }
        }
    }
    
    /**
     * 处理服务器响应
     * @param success 是否成功
     * @param message 响应消息
     */
    public static void handleResponse(boolean success, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (success) {
            client.player.sendMessage(Text.of(message), false);
        } else {
            client.player.sendMessage(Text.of("§c" + message), false);
        }
    }
} 