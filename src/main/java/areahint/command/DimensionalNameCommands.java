package areahint.command;

import areahint.Areashint;
import areahint.dimensional.DimensionalNameManager;
import areahint.network.DimensionalNameNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 维度域名命令处理器
 * 处理 /areahint dimensionalityname 命令
 */
public class DimensionalNameCommands {
    
    /**
     * 注册维度域名相关命令
     * @param dispatcher 命令分发器
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> dimensionalityNameCommand = 
            CommandManager.literal("dimensionalityname")
                .requires(source -> source.hasPermissionLevel(2)) // 需要OP权限
                .executes(DimensionalNameCommands::listAllDimensions)
                .then(CommandManager.argument("dimension", StringArgumentType.string())
                    .suggests(createDimensionSuggestionProvider())
                    .executes(DimensionalNameCommands::showCurrentDimensionalName)
                    .then(CommandManager.argument("newName", StringArgumentType.greedyString())
                        .executes(DimensionalNameCommands::setDimensionalName)));
        
        // 注册命令（通过直接添加到dispatcher的方式）
        // 这个方法需要从 ServerCommands 中调用
    }
    
    /**
     * 创建维度建议提供器
     * @return 建议提供器
     */
    public static SuggestionProvider<ServerCommandSource> createDimensionSuggestionProvider() {
        return (context, builder) -> {
            // 获取服务器中所有维度
            Set<String> allDimensions = context.getSource().getServer()
                .getWorldRegistryKeys()
                .stream()
                .map(key -> key.getValue().toString())
                .collect(Collectors.toSet());
            
            // 添加默认维度（确保它们总是可用）
            allDimensions.add("minecraft:overworld");
            allDimensions.add("minecraft:the_nether");
            allDimensions.add("minecraft:the_end");
            
            // 为包含冒号的维度ID添加引号，让命令能正确解析
            Set<String> quotedDimensions = allDimensions.stream()
                .map(dimension -> dimension.contains(":") ? "\"" + dimension + "\"" : dimension)
                .collect(Collectors.toSet());
            
            return CommandSource.suggestMatching(quotedDimensions, builder);
        };
    }
    
    /**
     * 列出所有维度及其当前名称
     */
    public static int listAllDimensions(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // 获取服务器中所有维度
            Set<String> serverDimensions = source.getServer()
                .getWorldRegistryKeys()
                .stream()
                .map(key -> key.getValue().toString())
                .collect(Collectors.toSet());
            
            // 添加默认维度
            serverDimensions.add("minecraft:overworld");
            serverDimensions.add("minecraft:the_nether");
            serverDimensions.add("minecraft:the_end");
            
            source.sendFeedback(() -> Text.literal("=== 维度域名配置 ===").formatted(Formatting.GOLD), false);
            source.sendFeedback(() -> Text.literal("格式: /areahint dimensionalityname <维度> [新名称]").formatted(Formatting.GRAY), false);
            source.sendFeedback(() -> Text.literal(""), false);
            
            for (String dimensionId : serverDimensions.stream().sorted().collect(Collectors.toList())) {
                String displayName = DimensionalNameManager.getDimensionalName(dimensionId);
                String status = DimensionalNameManager.hasDimensionalName(dimensionId) ? "自定义" : "默认";
                
                source.sendFeedback(() -> Text.literal(String.format("• %s", dimensionId))
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(" → ").formatted(Formatting.GRAY))
                    .append(Text.literal(displayName).formatted(Formatting.GREEN))
                    .append(Text.literal(String.format(" (%s)", status)).formatted(Formatting.DARK_GRAY)), false);
            }
            
            source.sendFeedback(() -> Text.literal(""), false);
            source.sendFeedback(() -> Text.literal(String.format("共 %d 个维度", serverDimensions.size()))
                .formatted(Formatting.AQUA), false);
            
            return serverDimensions.size();
            
        } catch (Exception e) {
            source.sendError(Text.literal("获取维度列表时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("获取维度列表失败", e);
            return 0;
        }
    }
    
    /**
     * 显示指定维度的当前名称
     */
    public static int showCurrentDimensionalName(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String dimensionId = StringArgumentType.getString(context, "dimension");
        
        try {
            String displayName = DimensionalNameManager.getDimensionalName(dimensionId);
            String status = DimensionalNameManager.hasDimensionalName(dimensionId) ? "自定义" : "默认";
            
            source.sendFeedback(() -> Text.literal("维度: ").formatted(Formatting.YELLOW)
                .append(Text.literal(dimensionId).formatted(Formatting.AQUA)), false);
            source.sendFeedback(() -> Text.literal("当前名称: ").formatted(Formatting.YELLOW)
                .append(Text.literal(displayName).formatted(Formatting.GREEN))
                .append(Text.literal(String.format(" (%s)", status)).formatted(Formatting.GRAY)), false);
            source.sendFeedback(() -> Text.literal(""), false);
            source.sendFeedback(() -> Text.literal("要修改名称，请输入: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("/areahint dimensionalityname %s <新名称>", dimensionId))
                    .formatted(Formatting.WHITE)), false);
            
            return 1;
            
        } catch (Exception e) {
            source.sendError(Text.literal("获取维度名称时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("获取维度名称失败", e);
            return 0;
        }
    }
    
    /**
     * 设置维度的新名称
     */
    public static int setDimensionalName(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String dimensionId = StringArgumentType.getString(context, "dimension");
        String newName = StringArgumentType.getString(context, "newName");
        
        try {
            // 验证新名称
            if (newName == null || newName.trim().isEmpty()) {
                source.sendError(Text.literal("维度名称不能为空"));
                return 0;
            }
            
            final String finalNewName = newName.trim();
            if (finalNewName.length() > 50) {
                source.sendError(Text.literal("维度名称过长（最多50个字符）"));
                return 0;
            }
            
            String oldName = DimensionalNameManager.getDimensionalName(dimensionId);
            
            // 更新维度名称
            DimensionalNameManager.setDimensionalName(dimensionId, finalNewName);
            
            // 保存配置
            if (DimensionalNameManager.saveDimensionalNames()) {
                source.sendFeedback(() -> Text.literal("维度域名已更新！").formatted(Formatting.GREEN), false);
                source.sendFeedback(() -> Text.literal("维度: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(dimensionId).formatted(Formatting.AQUA)), false);
                source.sendFeedback(() -> Text.literal("旧名称: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(oldName).formatted(Formatting.GRAY)), false);
                source.sendFeedback(() -> Text.literal("新名称: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(finalNewName).formatted(Formatting.GREEN)), false);
                
                // 发送更新到所有客户端
                broadcastDimensionalNameUpdate(source);
                
                Areashint.LOGGER.info("管理员 {} 将维度 {} 的名称从 '{}' 更改为 '{}'", 
                    source.getName(), dimensionId, oldName, finalNewName);
                
                return 1;
            } else {
                source.sendError(Text.literal("保存维度域名配置失败"));
                return 0;
            }
            
        } catch (Exception e) {
            source.sendError(Text.literal("设置维度名称时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("设置维度名称失败", e);
            return 0;
        }
    }
    
    /**
     * 向所有客户端广播维度域名更新
     */
    private static void broadcastDimensionalNameUpdate(ServerCommandSource source) {
        try {
            // 发送维度域名更新到所有在线玩家
            DimensionalNameNetworking.sendDimensionalNamesToAllClients(source.getServer());
            
            source.sendFeedback(() -> Text.literal("维度域名配置已同步到所有在线客户端")
                .formatted(Formatting.AQUA), false);
                
        } catch (Exception e) {
            source.sendError(Text.literal("同步到客户端时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("广播维度域名更新失败", e);
        }
    }
} 