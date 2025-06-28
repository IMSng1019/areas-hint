package areahint.command;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.data.ConfigData;
import areahint.file.FileManager;
import areahint.file.JsonHelper;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

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
        dispatcher.register(literal("areahint")
            // help 命令
            .then(literal("help")
                .executes(ServerCommands::executeHelp))
            
            // reload 命令
            .then(literal("reload")
                .executes(ServerCommands::executeReload))
            
            // add 命令 (仅服务端)
            .then(literal("add")
                .requires(source -> source.hasPermissionLevel(2)) // 需要管理员权限
                .then(argument("json", StringArgumentType.greedyString())
                    .executes(context -> executeAdd(context, StringArgumentType.getString(context, "json"))))
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
        source.sendMessage(Text.of("§a/areahint frequency [值] §7- 设置或显示检测频率"));
        source.sendMessage(Text.of("§a/areahint subtitlerender [cpu|opengl|vulkan] §7- 设置或显示字幕渲染方式"));
        source.sendMessage(Text.of("§a/areahint subtitlestyle [full|simple|mixed] §7- 设置或显示字幕样式"));
        source.sendMessage(Text.of("§a/areahint add <JSON> §7- 添加新的域名 (管理员专用)"));
        source.sendMessage(Text.of("§6===== JSON格式示例 ====="));
        source.sendMessage(Text.of("§7{\"name\": \"区域名称\", \"vertices\": [{\"x\":0,\"z\":10},{\"x\":10,\"z\":0},...], \"second-vertices\": [{\"x\":-10,\"z\":10},...], \"level\": 1, \"base-name\": null}"));
        
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
        
        // 通知客户端重载配置
        sendClientCommand(source, "areahint:reload");
        
        source.sendMessage(Text.of("§a区域数据已重新加载并发送给所有客户端"));
        
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
        AreaData areaData = JsonHelper.fromJson(json);
        
        if (areaData == null) {
            source.sendMessage(Text.of("§c无效的JSON数据，请检查格式"));
            return 0;
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
        
        Path filePath = FileManager.getDimensionFile(fileName);
        
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
} 