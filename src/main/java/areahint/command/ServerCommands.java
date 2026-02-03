package areahint.command;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.data.ConfigData;
import areahint.file.FileManager;
import areahint.file.JsonHelper;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.dimensional.DimensionalNameManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * 统一命令处理类
 * 实现服务端和客户端共享的命令功能
 */
public class ServerCommands {
    /**
     * 注册命令
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ServerCommands::registerCommands);
    }
    
    /**
     * 注册所有命令
     * @param dispatcher 命令分发器
     * @param registryAccess 命令注册访问器
     * @param environment 环境
     */
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, 
                                        CommandRegistryAccess registryAccess, 
                                        CommandManager.RegistrationEnvironment environment) {
        // 注册调试命令
        DebugCommand.register(dispatcher, registryAccess, environment);
        
        // 注册check命令
        CheckCommand.register(dispatcher, registryAccess, environment);
        
        dispatcher.register(literal("areahint")
            // help 命令
            .then(literal("help")
                .executes(ServerCommands::executeHelp))
            
            // reload 命令
            .then(literal("reload")
                .executes(ServerCommands::executeReload))
            
            // dimensionalityname 命令 (维度域名管理)
            .then(literal("dimensionalityname")
                .requires(source -> source.hasPermissionLevel(2)) // 需要OP权限
                .executes(DimensionalNameCommands::listAllDimensions)
                .then(argument("dimension", StringArgumentType.string())
                    .suggests(DimensionalNameCommands.createDimensionSuggestionProvider())
                    .executes(DimensionalNameCommands::showCurrentDimensionalName)
                    .then(argument("newName", StringArgumentType.greedyString())
                        .executes(DimensionalNameCommands::setDimensionalName))))
            
            // add 命令 (仅服务端)
            .then(literal("add")
                .requires(source -> source.hasPermissionLevel(2)) // 需要管理员权限
                .then(argument("json", StringArgumentType.greedyString())
                    .executes(context -> executeAdd(context, StringArgumentType.getString(context, "json"))))
            )
            
            // delete 命令 (任何玩家都可以使用，但有权限检查)
            .then(literal("delete")
                .then(argument("areaName", StringArgumentType.greedyString())
                    .suggests(DELETABLE_AREA_SUGGESTIONS)
                    .executes(context -> executeDelete(context, StringArgumentType.getString(context, "areaName"))))
                .executes(ServerCommands::executeDeleteList)
            )
            
            // frequency 命令
            .then(literal("frequency")
                .then(argument("value", IntegerArgumentType.integer(1, 60))
                    .executes(context -> executeFrequency(context, IntegerArgumentType.getInteger(context, "value"))))
                .executes(ServerCommands::executeFrequencyInfo))
            
            // subtitlerender 命令
            .then(literal("subtitlerender")
                .then(argument("mode", StringArgumentType.word())
                    .executes(context -> executeSubtitleRender(context, StringArgumentType.getString(context, "mode"))))
                .executes(ServerCommands::executeSubtitleRenderInfo))
            
            // subtitlestyle 命令
            .then(literal("subtitlestyle")
                .then(argument("style", StringArgumentType.word())
                    .executes(context -> executeSubtitleStyle(context, StringArgumentType.getString(context, "style"))))
                .executes(ServerCommands::executeSubtitleStyleInfo))
                
            // easyadd 命令（带多个子命令）
            .then(literal("easyadd")
                .executes(ServerCommands::executeEasyAddStart)
                .then(literal("cancel")
                    .executes(ServerCommands::executeEasyAddCancel))
                .then(literal("level")
                    .then(argument("levelValue", IntegerArgumentType.integer(1, 3))
                        .executes(context -> executeEasyAddLevel(context, IntegerArgumentType.getInteger(context, "levelValue")))))
                .then(literal("base")
                    .then(argument("baseName", StringArgumentType.greedyString())
                        .executes(context -> executeEasyAddBase(context, StringArgumentType.getString(context, "baseName")))))
                .then(literal("continue")
                    .executes(ServerCommands::executeEasyAddContinue))
                                        .then(literal("finish")
                            .executes(ServerCommands::executeEasyAddFinish))
                        .then(literal("altitude")
                            .then(literal("auto")
                                .executes(ServerCommands::executeEasyAddAltitudeAuto))
                            .then(literal("custom")
                                .executes(ServerCommands::executeEasyAddAltitudeCustom))
                            .then(literal("unlimited")
                                .executes(ServerCommands::executeEasyAddAltitudeUnlimited)))
                        .then(literal("color")
                            .then(argument("colorValue", StringArgumentType.greedyString())
                                .executes(context -> executeEasyAddColor(context, StringArgumentType.getString(context, "colorValue")))))
                        .then(literal("save")
                            .executes(ServerCommands::executeEasyAddSave)))
                            
            // expandarea 命令 (域名扩展)
            .then(literal("expandarea")
                .executes(ServerCommands::executeExpandAreaStart)
                // 直接指定域名：/areahint expandarea "域名"
                .then(argument("areaName", StringArgumentType.greedyString())
                    .suggests(EXPANDABLE_AREA_SUGGESTIONS)
                    .executes(context -> executeExpandAreaSelect(context, 
                        StringArgumentType.getString(context, "areaName"))))
                // 子命令（保持向后兼容）
                .then(literal("select")
                    .then(argument("selectAreaName", StringArgumentType.greedyString())
                        .suggests(EXPANDABLE_AREA_SUGGESTIONS)
                        .executes(context -> executeExpandAreaSelect(context, 
                            StringArgumentType.getString(context, "selectAreaName")))))
                .then(literal("continue")
                    .executes(ServerCommands::executeExpandAreaContinue))
                .then(literal("save")
                    .executes(ServerCommands::executeExpandAreaSave))
                .then(literal("cancel")
                    .executes(ServerCommands::executeExpandAreaCancel)))
            
            // shrinkarea 命令 (域名收缩)
            .then(literal("shrinkarea")
                .executes(ServerCommands::executeShrinkAreaStart)
                // 直接指定域名：/areahint shrinkarea "域名"
                .then(argument("areaName", StringArgumentType.greedyString())
                    .suggests(SHRINKABLE_AREA_SUGGESTIONS)
                    .executes(context -> executeShrinkAreaSelect(context, 
                        StringArgumentType.getString(context, "areaName"))))
                // 子命令（保持向后兼容）
                .then(literal("select")
                    .then(argument("selectAreaName", StringArgumentType.greedyString())
                        .suggests(SHRINKABLE_AREA_SUGGESTIONS)
                        .executes(context -> executeShrinkAreaSelect(context, 
                            StringArgumentType.getString(context, "selectAreaName")))))
                .then(literal("continue")
                    .executes(ServerCommands::executeShrinkAreaContinue))
                .then(literal("save")
                    .executes(ServerCommands::executeShrinkAreaSave))
                .then(literal("cancel")
                    .executes(ServerCommands::executeShrinkAreaCancel)))
            
            // recolor 命令
            .then(literal("recolor")
                .executes(RecolorCommand::executeRecolor)
                // /areahint recolor select <域名>
                .then(literal("select")
                    .then(argument("selectAreaName", StringArgumentType.greedyString())
                        .executes(context -> executeRecolorSelect(context,
                            StringArgumentType.getString(context, "selectAreaName")))))
                // /areahint recolor color <颜色>
                .then(literal("color")
                    .then(argument("colorValue", StringArgumentType.greedyString())
                        .executes(context -> executeRecolorColor(context,
                            StringArgumentType.getString(context, "colorValue")))))
                // /areahint recolor confirm
                .then(literal("confirm")
                    .executes(ServerCommands::executeRecolorConfirm))
                // /areahint recolor cancel
                .then(literal("cancel")
                    .executes(ServerCommands::executeRecolorCancel)))
            
            // rename 命令（交互式流程）
            .then(literal("rename")
                .executes(RenameAreaCommand::executeRename)
                .then(literal("select")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> RenameAreaCommand.executeRenameSelect(context,
                            StringArgumentType.getString(context, "areaName")))))
                .then(literal("confirm")
                    .executes(RenameAreaCommand::executeRenameConfirm))
                .then(literal("cancel")
                    .executes(RenameAreaCommand::executeRenameCancel)))
                            
            // sethigh 命令
            .then(literal("sethigh")
                .then(literal("custom")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> SetHighCommand.executeSetHighCustom(context, StringArgumentType.getString(context, "areaName")))))
                .then(literal("unlimited")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> SetHighCommand.executeSetHighUnlimited(context, StringArgumentType.getString(context, "areaName")))))
                .then(literal("cancel")
                    .executes(context -> SetHighCommand.executeSetHighCancel(context)))
                .then(argument("areaName", StringArgumentType.greedyString())
                    .suggests(SETHIGH_AREA_SUGGESTIONS)
                    .executes(context -> SetHighCommand.executeSetHighWithArea(context, StringArgumentType.getString(context, "areaName"))))
                .executes(SetHighCommand::executeSetHigh))
        );
    }
    
    /**
     * 执行help命令
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        source.sendMessage(Text.of("§6===== 区域提示模组命令帮助 ====="));
        source.sendMessage(Text.of("§a/areahint help §7- 显示此帮助"));
        source.sendMessage(Text.of("§a/areahint reload §7- 重新加载配置和域名文件"));
        source.sendMessage(Text.of("§a/areahint delete §7- 列出所有可删除的域名"));
        source.sendMessage(Text.of("§a/areahint delete <域名> §7- 删除指定域名"));
        source.sendMessage(Text.of("§a/areahint frequency [值] §7- 设置或显示检测频率"));
        source.sendMessage(Text.of("§a/areahint subtitlerender [cpu|opengl|vulkan] §7- 设置或显示字幕渲染方式"));
        source.sendMessage(Text.of("§a/areahint subtitlestyle [full|simple|mixed] §7- 设置或显示字幕样式"));
        source.sendMessage(Text.of("§a/areahint add <JSON> §7- 添加新的域名 (管理员专用)"));
        source.sendMessage(Text.of("§a/areahint easyadd §7- 启动交互式域名添加 (普通玩家可用)"));
        source.sendMessage(Text.of("§a/areahint recolor §7- 列出当前维度可编辑的域名"));
        source.sendMessage(Text.of("§a/areahint recolor <域名> <颜色> §7- 修改指定域名的颜色"));
        source.sendMessage(Text.of("§a/areahint rename §7- 启动交互式域名重命名流程"));
        source.sendMessage(Text.of("§a/areahint sethigh §7- 列出当前维度可修改高度的域名"));
        source.sendMessage(Text.of("§a/areahint debug §7- 切换调试模式 (管理员专用)"));
        source.sendMessage(Text.of("§a/areahint debug [on|off|status] §7- 启用/禁用/查看调试模式状态 (管理员专用)"));
        source.sendMessage(Text.of("§6===== JSON格式示例 ====="));
        source.sendMessage(Text.of("§7{\"name\": \"区域名称\", \"vertices\": [{\"x\":0,\"z\":10},{\"x\":10,\"z\":0},...], \"second-vertices\": [{\"x\":-10,\"z\":10},...], \"altitude\": {\"max\":100,\"min\":0}, \"level\": 1, \"base-name\": null, \"signature\": null}"));
        source.sendMessage(Text.of("§7altitude字段可选: max/min可为null表示无限制，如{\"max\":null,\"min\":64}"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行reload命令
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeReload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 重新加载区域数据并发送给所有客户端
        ServerNetworking.sendAllAreaDataToAll();
        
        // 重新发送维度域名配置给所有客户端
        try {
            MinecraftServer server = source.getServer();
            areahint.network.DimensionalNameNetworking.sendDimensionalNamesToAllClients(server);
            Areashint.LOGGER.info("已重新发送维度域名配置给所有客户端");
        } catch (Exception e) {
            Areashint.LOGGER.error("重新发送维度域名配置失败", e);
        }
        
        // 通知客户端重载配置
        sendClientCommand(source, "areahint:reload");
        
        source.sendMessage(Text.of("§a区域数据和维度域名配置已重新加载并发送给所有客户端"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行add命令
     * @param context 命令上下文
     * @param json JSON字符串
     * @return 执行结果
     */
    private static int executeAdd(CommandContext<ServerCommandSource> context, String json) {
        ServerCommandSource source = context.getSource();
        
        // 解析JSON数据
        AreaData areaData = JsonHelper.fromJsonSingle(json);
        
        if (areaData == null) {
            source.sendMessage(Text.of("§c无效的JSON数据，请检查格式"));
            return 0;
        }
        
        // 验证高度数据（如果存在）
        if (areaData.getAltitude() != null) {
            if (!areaData.getAltitude().isValid()) {
                source.sendMessage(Text.of("§c高度数据无效: 最大高度不能小于最小高度"));
                return 0;
            }
            
            // 检查高度值的合理性（Minecraft世界高度通常在-64到320之间）
            Double minAlt = areaData.getAltitude().getMin();
            Double maxAlt = areaData.getAltitude().getMax();
            
            if (minAlt != null && (minAlt < -64 || minAlt > 320)) {
                source.sendMessage(Text.of(String.format("§c最小高度 %.1f 超出合理范围 [-64, 320]", minAlt)));
                return 0;
            }
            
            if (maxAlt != null && (maxAlt < -64 || maxAlt > 320)) {
                source.sendMessage(Text.of(String.format("§c最大高度 %.1f 超出合理范围 [-64, 320]", maxAlt)));
                return 0;
            }
        }
        
        if (!areaData.isValid()) {
            source.sendMessage(Text.of("§c域名数据无效，请确保至少有3个点和正确的域名等级"));
            return 0;
        }
        
        // 根据维度决定写入哪个文件
        String dimensionId = getDimensionFromSource(source);
        if (dimensionId == null) {
            source.sendMessage(Text.of("§c无法确定当前维度，请指定维度"));
            return 0;
        }
        
        String fileName = Packets.getFileNameForDimension(dimensionId);
        if (fileName == null) {
            source.sendMessage(Text.of("§c无法确定文件名，维度ID: " + dimensionId));
            return 0;
        }
        
        Path filePath = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
        
        // 自动设置创建者签名（如果没有设置的话）
        if (areaData.getSignature() == null) {
            areaData.setSignature(source.getName());
        }
        
        // 添加区域数据
        if (FileManager.addAreaData(filePath, areaData)) {
            source.sendMessage(Text.of("§a成功添加域名: " + areaData.getName()));
            
            // 通知所有客户端更新
            ServerNetworking.sendAreaDataToAll(dimensionId);
            
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendMessage(Text.of("§c添加域名失败，请检查日志"));
            return 0;
        }
    }
    
    /**
     * 从命令源获取当前维度
     * @param source 命令源
     * @return 维度ID
     */
    private static String getDimensionFromSource(ServerCommandSource source) {
        try {
            Identifier dimension = source.getWorld().getDimensionKey().getValue();
            return Packets.convertDimensionPathToType(dimension.getPath());
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     * 发送客户端命令
     * @param source 命令源
     * @param command 客户端命令
     */
    private static void sendClientCommand(ServerCommandSource source, String command) {
        try {
            // 向运行该命令的玩家发送客户端命令
            if (source.getPlayer() != null) {
                ServerNetworking.sendCommandToClient(source.getPlayer(), command);
            }
        } catch (Exception e) {
            Areashint.LOGGER.error("发送客户端命令时出错", e);
        }
    }
    
    /**
     * 执行frequency命令（显示当前频率）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeFrequencyInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 向客户端发送命令
        sendClientCommand(source, "areahint:frequency_info");
        
        source.sendMessage(Text.of("§a检测频率信息已发送到客户端"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行frequency命令（设置频率）
     * @param context 命令上下文
     * @param value 频率值
     * @return 执行结果
     */
    private static int executeFrequency(CommandContext<ServerCommandSource> context, int value) {
        ServerCommandSource source = context.getSource();
        
        // 向客户端发送命令
        sendClientCommand(source, "areahint:frequency " + value);
        
        source.sendMessage(Text.of("§a检测频率已设置为: §6" + value + "§a Hz"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行subtitlerender命令（显示当前渲染方式）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeSubtitleRenderInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 向客户端发送命令
        sendClientCommand(source, "areahint:subtitlerender_info");
        
        source.sendMessage(Text.of("§a字幕渲染方式信息已发送到客户端"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行subtitlerender命令（设置渲染方式）
     * @param context 命令上下文
     * @param mode 渲染方式
     * @return 执行结果
     */
    private static int executeSubtitleRender(CommandContext<ServerCommandSource> context, String mode) {
        ServerCommandSource source = context.getSource();
        
        String normalizedMode = ConfigData.normalizeRenderMode(mode);
        
        if (ConfigData.isValidRenderMode(normalizedMode)) {
            // 向客户端发送命令
            sendClientCommand(source, "areahint:subtitlerender " + normalizedMode);
            
            source.sendMessage(Text.of("§a字幕渲染方式已设置为: §6" + normalizedMode));
            
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendMessage(Text.of("§c无效的渲染方式: §6" + mode + "§c。有效选项: cpu, opengl, vulkan"));
            return 0;
        }
    }
    
    /**
     * 执行subtitlestyle命令（显示当前样式）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeSubtitleStyleInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 向客户端发送命令
        sendClientCommand(source, "areahint:subtitlestyle_info");
        
        source.sendMessage(Text.of("§a字幕样式信息已发送到客户端"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行subtitlestyle命令（设置样式）
     * @param context 命令上下文
     * @param style 样式
     * @return 执行结果
     */
    private static int executeSubtitleStyle(CommandContext<ServerCommandSource> context, String style) {
        ServerCommandSource source = context.getSource();
        
        String normalizedStyle = ConfigData.normalizeStyleMode(style);
        
        if (ConfigData.isValidStyleMode(normalizedStyle)) {
            // 向客户端发送命令
            sendClientCommand(source, "areahint:subtitlestyle " + normalizedStyle);
            
            source.sendMessage(Text.of("§a字幕样式已设置为: §6" + normalizedStyle));
            
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendMessage(Text.of("§c无效的样式: §6" + style + "§c。有效选项: full, simple, mixed"));
            return 0;
        }
    }
    
    /**
     * 执行delete命令列表（显示所有可删除的域名）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeDeleteList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 获取当前维度
        String dimensionId = getDimensionFromSource(source);
        if (dimensionId == null) {
            source.sendMessage(Text.of("§c无法确定当前维度"));
            return 0;
        }
        
        String fileName = Packets.getFileNameForDimension(dimensionId);
        if (fileName == null) {
            source.sendMessage(Text.of("§c无法确定文件名，维度ID: " + dimensionId));
            return 0;
        }
        
        Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
        
        // 读取区域数据
        java.util.List<AreaData> areas = FileManager.readAreaData(areaFile);
        
        if (areas.isEmpty()) {
            source.sendMessage(Text.of("§7当前维度没有任何域名"));
            return Command.SINGLE_SUCCESS;
        }
        
        source.sendMessage(Text.of("§6===== 可删除的域名列表 ====="));
        
        for (AreaData area : areas) {
            String signature = area.getSignature();
            String playerName = source.getName();
            boolean hasOp = source.hasPermissionLevel(2);
            boolean canDelete = false;
            
            if (signature == null) {
                // 没有签名的旧域名：只有管理员可以删除
                if (hasOp) {
                    source.sendMessage(Text.of("§a" + area.getName() + " §7- 可删除 (旧版本域名，管理员权限)"));
                    canDelete = true;
                } else {
                    source.sendMessage(Text.of("§c" + area.getName() + " §7- 不可删除 (旧版本域名，需要管理员权限)"));
                }
            } else if (signature.equals(playerName) || hasOp) {
                // 可以删除
                source.sendMessage(Text.of("§a" + area.getName() + " §7- 可删除 (创建者: " + signature + ")"));
                canDelete = true;
            } else {
                // 不可删除
                source.sendMessage(Text.of("§c" + area.getName() + " §7- 不可删除 (创建者: " + signature + ")"));
            }
        }
        
        source.sendMessage(Text.of("§7使用 §a/areahint delete <域名> §7来删除指定域名"));
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 执行delete命令（删除指定域名）
     * @param context 命令上下文
     * @param areaName 要删除的域名
     * @return 执行结果
     */
    private static int executeDelete(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        String playerName = source.getName();
        boolean hasOp = source.hasPermissionLevel(2);
        
        // 获取当前维度
        String dimensionId = getDimensionFromSource(source);
        if (dimensionId == null) {
            source.sendMessage(Text.of("§c无法确定当前维度"));
            return 0;
        }
        
        String fileName = Packets.getFileNameForDimension(dimensionId);
        if (fileName == null) {
            source.sendMessage(Text.of("§c无法确定文件名，维度ID: " + dimensionId));
            return 0;
        }
        
        Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
        
        // 读取区域数据
        java.util.List<AreaData> areas = FileManager.readAreaData(areaFile);
        
        // 查找要删除的域名
        AreaData targetArea = null;
        for (AreaData area : areas) {
            if (area.getName().equals(areaName)) {
                targetArea = area;
                break;
            }
        }
        
        if (targetArea == null) {
            source.sendMessage(Text.of("§c未找到域名: §6" + areaName));
            return 0;
        }
        
        // 检查签名权限
        String signature = targetArea.getSignature();
        if (signature == null) {
            // 没有签名的旧域名：只有管理员可以删除
            if (!hasOp) {
                source.sendMessage(Text.of("§c该域名没有签名（旧版本域名），只有管理员可以删除"));
                return 0;
            }
        } else {
            // 有签名的新域名：创建者或管理员可以删除
            if (!signature.equals(playerName) && !hasOp) {
                source.sendMessage(Text.of("§c你不是该域名的创建者，无法删除"));
                return 0;
            }
        }
        
        // 检查是否有次级域名引用此域名
        for (AreaData area : areas) {
            if (areaName.equals(area.getBaseName())) {
                source.sendMessage(Text.of("§c不能删除该域名，因为存在次级域名 §6" + area.getName() + " §c引用了它"));
                return 0;
            }
        }
        
        // 执行删除
        areas.remove(targetArea);
        
        // 保存文件
        try {
            FileManager.writeAreaData(areaFile, areas);
            source.sendMessage(Text.of("§a成功删除域名: §6" + areaName));
            
            // 向所有客户端发送更新后的区域数据
            ServerNetworking.sendAllAreaDataToAll();
            
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c删除域名时发生错误: " + e.getMessage()));
            Areashint.LOGGER.error("删除域名时发生错误", e);
            return 0;
        }
    }
    
    /**
     * 获取当前玩家可以删除的域名列表
     * @param source 命令源
     * @param dimension 维度
     * @return 可删除的域名列表
     */
    private static List<String> getDeletableAreaNames(ServerCommandSource source, String dimension) {
        try {
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                return List.of();
            }
            
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                return List.of();
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            String playerName = source.getName();
            boolean hasOp = source.hasPermissionLevel(2);
            
            return areas.stream()
                .filter(area -> {
                    String signature = area.getSignature();
                    if (signature == null) {
                        // 旧域名只有管理员可以删除
                        return hasOp;
                    } else {
                        // 新域名创建者或管理员可以删除
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("获取可删除域名列表时发生错误", e);
            return List.of();
        }
    }
    
    /**
     * 可删除域名的建议提供器
     */
    private static final SuggestionProvider<ServerCommandSource> DELETABLE_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> deletableAreas = getDeletableAreaNames(source, dimension);
            
            // 添加所有可删除的域名到建议列表
            for (String areaName : deletableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }
            
            return builder.buildFuture();
        };
    
    // ===== EasyAdd 命令处理方法 =====
    
    /**
     * 处理EasyAdd开始命令（仅客户端）
     */
    private static int executeEasyAddStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 检查是否为客户端命令
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        // 发送客户端命令
        try {
            // 通过网络发送到客户端处理
            sendClientCommand(source, "areahint:easyadd_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c启动EasyAdd时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd取消命令（仅客户端）
     */
    private static int executeEasyAddCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c取消EasyAdd时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd等级选择命令（仅客户端）
     */
    private static int executeEasyAddLevel(CommandContext<ServerCommandSource> context, int level) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_level:" + level);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c设置域名等级时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd上级域名选择命令（仅客户端）
     */
    private static int executeEasyAddBase(CommandContext<ServerCommandSource> context, String baseName) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_base:" + baseName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择上级域名时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd继续记录命令（仅客户端）
     */
    private static int executeEasyAddContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c继续记录时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd完成记录命令（仅客户端）
     */
    private static int executeEasyAddFinish(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_finish");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c完成记录时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd保存命令（仅客户端）
     */
    private static int executeEasyAddSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c保存域名时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd自动高度选择命令（仅客户端）
     */
    private static int executeEasyAddAltitudeAuto(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_altitude_auto");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择自动高度时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd自定义高度选择命令（仅客户端）
     */
    private static int executeEasyAddAltitudeCustom(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_altitude_custom");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择自定义高度时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 处理EasyAdd不限制高度选择命令（仅客户端）
     */
    private static int executeEasyAddAltitudeUnlimited(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_altitude_unlimited");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择不限制高度时发生错误: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 处理EasyAdd颜色选择命令（仅客户端）
     */
    private static int executeEasyAddColor(CommandContext<ServerCommandSource> context, String color) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_color:" + color);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择颜色时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行expandarea命令
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeExpandAreaStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // 检查是否为玩家执行
            if (source.getPlayer() == null) {
                source.sendMessage(Text.of("§c此命令只能由玩家执行"));
                return 0;
            }
            
            // 发送客户端命令，启动域名扩展流程
            source.sendMessage(Text.of("§a启动域名扩展模式..."));
            source.sendMessage(Text.of("§e请在客户端界面中选择要扩展的域名"));
            
            // 通过客户端命令通道发送启动命令
            sendClientCommand(source, "areahint:expandarea_start");
            
            return Command.SINGLE_SUCCESS;
            
        } catch (Exception e) {
            source.sendMessage(Text.of("§c启动域名扩展失败: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 执行expandarea命令（继续）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeExpandAreaContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c继续域名扩展时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行expandarea命令（保存）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeExpandAreaSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c保存域名扩展时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行shrinkarea命令
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeShrinkAreaStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // 检查是否为玩家执行
            if (source.getPlayer() == null) {
                source.sendMessage(Text.of("§c此命令只能由玩家执行"));
                return 0;
            }
            
            // 发送客户端命令，启动域名收缩流程
            source.sendMessage(Text.of("§a启动域名收缩模式..."));
            source.sendMessage(Text.of("§e请在客户端界面中选择要收缩的域名"));
            
            // 通过客户端命令通道发送启动命令
            sendClientCommand(source, "areahint:shrinkarea_start");
            
            return Command.SINGLE_SUCCESS;
            
        } catch (Exception e) {
            source.sendMessage(Text.of("§c启动域名收缩失败: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 执行shrinkarea命令（继续）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeShrinkAreaContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c继续域名收缩时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行shrinkarea命令（保存）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeShrinkAreaSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c保存域名收缩时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行expandarea命令（选择域名）
     * @param context 命令上下文
     * @param areaName 域名名称
     * @return 执行结果
     */
    private static int executeExpandAreaSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择域名时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行expandarea命令（取消）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeExpandAreaCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c取消扩展时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行shrinkarea命令（选择域名）
     * @param context 命令上下文
     * @param areaName 域名名称
     * @return 执行结果
     */
    private static int executeShrinkAreaSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c选择域名时发生错误: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * 执行shrinkarea命令（取消）
     * @param context 命令上下文
     * @return 执行结果
     */
    private static int executeShrinkAreaCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendMessage(Text.of("§c此命令只能由玩家执行"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendMessage(Text.of("§c取消收缩时发生错误: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 获取当前玩家可以设置高度的域名列表
     * @param source 命令源
     * @param dimension 维度
     * @return 可设置高度的域名列表
     */
    private static List<String> getSettableAreaNames(ServerCommandSource source, String dimension) {
        try {
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                return List.of();
            }
            
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                return List.of();
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            String playerName = source.getName();
            boolean hasOp = source.hasPermissionLevel(2);
            
            return areas.stream()
                .filter(area -> {
                    String signature = area.getSignature();
                    if (signature == null) {
                        // 旧域名只有管理员可以设置高度
                        return hasOp;
                    } else {
                        // 新域名创建者或管理员可以设置高度
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("获取可设置高度的域名列表时发生错误", e);
            return List.of();
        }
    }

    /**
     * 可设置高度的域名的建议提供器
     */
    private static final SuggestionProvider<ServerCommandSource> SETHIGH_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> settableAreas = getSettableAreaNames(source, dimension);
            
            // 添加所有可设置高度的域名到建议列表
            for (String areaName : settableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }
            
            return builder.buildFuture();
        };
    
    /**
     * 获取当前玩家可扩展的域名列表
     * @param source 命令源
     * @param dimension 维度名称
     * @return 可扩展的域名名称列表
     */
    private static List<String> getExpandableAreaNames(ServerCommandSource source, String dimension) {
        try {
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                return List.of();
            }
            
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                return List.of();
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            String playerName = source.getName();
            boolean hasOp = source.hasPermissionLevel(2);
            
            return areas.stream()
                .filter(area -> {
                    String signature = area.getSignature();
                    if (signature == null) {
                        // 旧域名只有管理员可以扩展
                        return hasOp;
                    } else {
                        // 新域名创建者或管理员可以扩展
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("获取可扩展域名列表时发生错误", e);
            return List.of();
        }
    }
    
    /**
     * 可扩展域名的建议提供器
     */
    private static final SuggestionProvider<ServerCommandSource> EXPANDABLE_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> expandableAreas = getExpandableAreaNames(source, dimension);
            
            // 添加所有可扩展的域名到建议列表
            for (String areaName : expandableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }
            
            return builder.buildFuture();
        };
    
    /**
     * 获取当前玩家可收缩的域名列表
     * @param source 命令源
     * @param dimension 维度名称
     * @return 可收缩的域名名称列表
     */
    private static List<String> getShrinkableAreaNames(ServerCommandSource source, String dimension) {
        try {
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                return List.of();
            }
            
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                return List.of();
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            String playerName = source.getName();
            boolean hasOp = source.hasPermissionLevel(2);
            
            return areas.stream()
                .filter(area -> {
                    String signature = area.getSignature();
                    if (signature == null) {
                        // 旧域名只有管理员可以收缩
                        return hasOp;
                    } else {
                        // 新域名创建者或管理员可以收缩
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("获取可收缩域名列表时发生错误", e);
            return List.of();
        }
    }
    
    /**
     * 可收缩域名的建议提供器
     */
    private static final SuggestionProvider<ServerCommandSource> SHRINKABLE_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> shrinkableAreas = getShrinkableAreaNames(source, dimension);
            
            // 添加所有可收缩的域名到建议列表
            for (String areaName : shrinkableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }

            return builder.buildFuture();
        };

    /**
     * 执行recolor select命令（客户端通过网络调用）
     */
    private static int executeRecolorSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();

        // 移除引号（如果存在）
        if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
            areaName = areaName.substring(1, areaName.length() - 1);
        }

        // 发送命令到客户端
        if (source.getPlayer() != null) {
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:recolor_select:" + areaName);
        }

        return 1;
    }

    /**
     * 执行recolor color命令（客户端通过网络调用）
     */
    private static int executeRecolorColor(CommandContext<ServerCommandSource> context, String colorValue) {
        ServerCommandSource source = context.getSource();

        // 发送命令到客户端
        if (source.getPlayer() != null) {
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:recolor_color:" + colorValue);
        }

        return 1;
    }

    /**
     * 执行recolor confirm命令（客户端通过网络调用）
     */
    private static int executeRecolorConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 发送命令到客户端
        if (source.getPlayer() != null) {
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:recolor_confirm");
        }

        return 1;
    }

    /**
     * 执行recolor cancel命令（客户端通过网络调用）
     */
    private static int executeRecolorCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 发送命令到客户端
        if (source.getPlayer() != null) {
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:recolor_cancel");
        }

        return 1;
    }
}