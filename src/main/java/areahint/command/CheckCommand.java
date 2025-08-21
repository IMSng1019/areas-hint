package areahint.command;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.util.SurfaceNameHandler;
import areahint.world.WorldFolderManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Check命令处理器
 * 处理 /areahint check 命令，显示联合域名和域名详细信息
 */
public class CheckCommand {
    
    /**
     * 注册check命令
     * @param dispatcher 命令分发器
     * @param registryAccess 注册表访问
     * @param environment 环境
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                               CommandRegistryAccess registryAccess, 
                               CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(
            CommandManager.literal("areahint")
                .then(CommandManager.literal("check")
                    .requires(source -> source.hasPermissionLevel(0)) // 权限等级0，不需要管理员
                    .executes(CheckCommand::executeCheckAll)
                    .then(CommandManager.argument("unionName", StringArgumentType.string())
                        .suggests(createUnionNameSuggestionProvider())
                        .executes(CheckCommand::executeCheckUnion)
                    )
                )
        );
    }
    
    /**
     * 执行 /areahint check 命令 - 显示所有联合域名
     * @param context 命令上下文
     * @return 命令结果
     */
    private static int executeCheckAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
            String dimensionId = dimensionType.getValue().toString();
            
            // 获取当前维度的域名文件
            String fileName = getDimensionFileName(dimensionId);
            Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
            
            if (areaFile == null || !areaFile.toFile().exists()) {
                source.sendMessage(Text.literal("§c当前维度暂无域名数据").formatted(Formatting.RED));
                return 1;
            }
            
            // 读取域名数据
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            
            if (areas.isEmpty()) {
                source.sendMessage(Text.literal("§c当前维度暂无域名").formatted(Formatting.RED));
                return 1;
            }
            
            // 收集所有联合域名
            Map<String, List<AreaData>> unionNameGroups = new LinkedHashMap<>();
            
            for (AreaData area : areas) {
                String unionName = area.getSurfacename();
                if (unionName == null || unionName.trim().isEmpty()) {
                    unionName = area.getName(); // 如果没有联合域名，使用实际域名
                }
                
                unionNameGroups.computeIfAbsent(unionName, k -> new ArrayList<>()).add(area);
            }
            
            // 发送标题
            source.sendMessage(Text.literal(""));
            source.sendMessage(Text.literal("§6=== 联合域名列表 ===").formatted(Formatting.GOLD));
            source.sendMessage(Text.literal("§7点击联合域名查看详细信息").formatted(Formatting.GRAY));
            source.sendMessage(Text.literal(""));
            
            // 显示每个联合域名
            for (Map.Entry<String, List<AreaData>> entry : unionNameGroups.entrySet()) {
                String unionName = entry.getKey();
                List<AreaData> areasInUnion = entry.getValue();
                
                MutableText unionText = Text.literal("§a▶ " + unionName).formatted(Formatting.GREEN);
                
                // 添加悬停提示
                StringBuilder hoverText = new StringBuilder();
                hoverText.append("§6联合域名: §f").append(unionName).append("\n");
                hoverText.append("§7包含 §e").append(areasInUnion.size()).append(" §7个域名:\n");
                
                for (int i = 0; i < areasInUnion.size(); i++) {
                    AreaData area = areasInUnion.get(i);
                    hoverText.append("§f- ").append(area.getName());
                    if (i < areasInUnion.size() - 1) {
                        hoverText.append("\n");
                    }
                }
                
                unionText.styled(style -> style
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hoverText.toString())))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint check \"" + unionName + "\""))
                );
                
                source.sendMessage(unionText);
            }
            
            source.sendMessage(Text.literal(""));
            source.sendMessage(Text.literal("§7总计: §e" + unionNameGroups.size() + " §7个联合域名，§e" + areas.size() + " §7个域名").formatted(Formatting.GRAY));
            
        } catch (Exception e) {
            Areashint.LOGGER.error("执行check命令时发生错误", e);
            source.sendMessage(Text.literal("§c命令执行失败: " + e.getMessage()).formatted(Formatting.RED));
        }
        
        return 1;
    }
    
    /**
     * 执行 /areahint check <联合域名> 命令 - 显示指定联合域名的详细信息
     * @param context 命令上下文
     * @return 命令结果
     */
    private static int executeCheckUnion(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String unionName = StringArgumentType.getString(context, "unionName");
        
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
            String dimensionId = dimensionType.getValue().toString();
            
            // 获取当前维度的域名文件
            String fileName = getDimensionFileName(dimensionId);
            Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
            
            if (areaFile == null || !areaFile.toFile().exists()) {
                source.sendMessage(Text.literal("§c当前维度暂无域名数据").formatted(Formatting.RED));
                return 1;
            }
            
            // 读取域名数据
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            
            // 查找匹配的域名
            List<AreaData> matchedAreas = new ArrayList<>();
            
            for (AreaData area : areas) {
                String areaUnionName = area.getSurfacename();
                if (areaUnionName == null || areaUnionName.trim().isEmpty()) {
                    areaUnionName = area.getName();
                }
                
                if (areaUnionName.equals(unionName)) {
                    matchedAreas.add(area);
                }
            }
            
            if (matchedAreas.isEmpty()) {
                source.sendMessage(Text.literal("§c未找到联合域名: " + unionName).formatted(Formatting.RED));
                return 1;
            }
            
            // 发送详细信息
            source.sendMessage(Text.literal(""));
            source.sendMessage(Text.literal("§6=== 联合域名详细信息 ===").formatted(Formatting.GOLD));
            source.sendMessage(Text.literal("§6联合域名: §f" + unionName).formatted(Formatting.GOLD));
            source.sendMessage(Text.literal("§7包含域名数量: §e" + matchedAreas.size()).formatted(Formatting.GRAY));
            source.sendMessage(Text.literal(""));
            
            // 显示每个域名的详细信息
            for (int i = 0; i < matchedAreas.size(); i++) {
                AreaData area = matchedAreas.get(i);
                
                source.sendMessage(Text.literal("§a域名 #" + (i + 1) + ":").formatted(Formatting.GREEN));
                source.sendMessage(Text.literal("  §7实际域名: §f" + area.getName()));
                
                if (area.getSurfacename() != null && !area.getSurfacename().trim().isEmpty()) {
                    source.sendMessage(Text.literal("  §7联合域名: §f" + area.getSurfacename()));
                }
                
                source.sendMessage(Text.literal("  §7域名等级: §e" + area.getLevel() + "级").formatted(Formatting.YELLOW));
                
                if (area.getBaseName() != null) {
                    source.sendMessage(Text.literal("  §7上级域名: §f" + area.getBaseName()));
                }
                
                if (area.getSignature() != null) {
                    source.sendMessage(Text.literal("  §7创建者: §f" + area.getSignature()));
                }
                
                if (area.getColor() != null) {
                    source.sendMessage(Text.literal("  §7颜色: §f" + area.getColor()));
                }
                
                // 显示高度信息
                if (area.getAltitude() != null) {
                    String altitudeText = "  §7高度范围: §f";
                    if (area.getAltitude().getMin() != null && area.getAltitude().getMax() != null) {
                        altitudeText += area.getAltitude().getMin() + " 到 " + area.getAltitude().getMax();
                    } else if (area.getAltitude().getMin() != null) {
                        altitudeText += area.getAltitude().getMin() + " 以上";
                    } else if (area.getAltitude().getMax() != null) {
                        altitudeText += area.getAltitude().getMax() + " 以下";
                    } else {
                        altitudeText += "无限制";
                    }
                    source.sendMessage(Text.literal(altitudeText));
                } else {
                    source.sendMessage(Text.literal("  §7高度范围: §f无限制"));
                }
                
                // 显示顶点信息
                if (area.getVertices() != null && !area.getVertices().isEmpty()) {
                    source.sendMessage(Text.literal("  §7一级顶点数量: §e" + area.getVertices().size()));
                    
                    // 显示前几个顶点作为示例
                    StringBuilder verticesText = new StringBuilder("  §7顶点坐标: §f");
                    int maxShow = Math.min(3, area.getVertices().size());
                    for (int j = 0; j < maxShow; j++) {
                        AreaData.Vertex vertex = area.getVertices().get(j);
                        verticesText.append("(").append(vertex.getX()).append(", ").append(vertex.getZ()).append(")");
                        if (j < maxShow - 1) {
                            verticesText.append(", ");
                        }
                    }
                    if (area.getVertices().size() > 3) {
                        verticesText.append("...");
                    }
                    source.sendMessage(Text.literal(verticesText.toString()));
                }
                
                if (i < matchedAreas.size() - 1) {
                    source.sendMessage(Text.literal(""));
                }
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error("执行check命令时发生错误", e);
            source.sendMessage(Text.literal("§c命令执行失败: " + e.getMessage()).formatted(Formatting.RED));
        }
        
        return 1;
    }
    
    /**
     * 创建联合域名建议提供器
     * @return 建议提供器
     */
    public static SuggestionProvider<ServerCommandSource> createUnionNameSuggestionProvider() {
        return (context, builder) -> {
            try {
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
                String dimensionId = dimensionType.getValue().toString();
                
                // 获取当前维度的域名文件
                String fileName = getDimensionFileName(dimensionId);
                Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
                
                if (areaFile == null || !areaFile.toFile().exists()) {
                    return Suggestions.empty();
                }
                
                // 读取域名数据
                List<AreaData> areas = FileManager.readAreaData(areaFile);
                
                // 收集所有联合域名
                Set<String> unionNames = new LinkedHashSet<>();
                
                for (AreaData area : areas) {
                    String unionName = area.getSurfacename();
                    if (unionName == null || unionName.trim().isEmpty()) {
                        unionName = area.getName();
                    }
                    unionNames.add(unionName);
                }
                
                // 过滤并添加建议
                String input = builder.getRemaining().toLowerCase();
                for (String unionName : unionNames) {
                    if (unionName.toLowerCase().contains(input)) {
                        builder.suggest("\"" + unionName + "\"");
                    }
                }
                
            } catch (Exception e) {
                // 静默处理错误，返回空建议
            }
            
            return builder.buildFuture();
        };
    }
    
    /**
     * 根据维度ID获取文件名
     * @param dimensionId 维度ID
     * @return 文件名
     */
    private static String getDimensionFileName(String dimensionId) {
        switch (dimensionId) {
            case "minecraft:overworld":
                return "overworld.json";
            case "minecraft:the_nether":
                return "the_nether.json";
            case "minecraft:the_end":
                return "the_end.json";
            default:
                return "overworld.json";
        }
    }
} 