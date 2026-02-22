package areahint.command;

import areahint.Areashint;
import areahint.debug.DebugManager;
import areahint.debug.DebugManager.DebugCategory;
import areahint.network.ServerNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import areahint.i18n.ServerI18nManager;

/**
 * 调试命令处理类
 * 用于注册和处理调试相关的命令
 */
public class DebugCommand {
    /**
     * 注册调试命令
     * @param dispatcher 命令分发器
     * @param registryAccess 注册表访问
     * @param environment 环境
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                               CommandRegistryAccess registryAccess, 
                               CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(
            CommandManager.literal("areahint")
                .then(CommandManager.literal("debug")
                    .requires(source -> source.hasPermissionLevel(2)) // 需要权限等级2
                    .executes(DebugCommand::toggleDebug)
                    .then(CommandManager.literal("on")
                        .executes(DebugCommand::enableDebug))
                    .then(CommandManager.literal("off")
                        .executes(DebugCommand::disableDebug))
                    .then(CommandManager.literal("status")
                        .executes(DebugCommand::showDebugStatus))
                )
        );
    }
    
    /**
     * 切换调试模式
     * @param context 命令上下文
     * @return 命令结果
     */
    private static int toggleDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendFeedback(() -> Text.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        if (DebugManager.isDebugEnabled(player.getUuid())) {
            return disableDebug(context);
        } else {
            return enableDebug(context);
        }
    }
    
    /**
     * 启用调试模式
     * @param context 命令上下文
     * @return 命令结果
     */
    private static int enableDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendFeedback(() -> Text.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        boolean wasEnabled = DebugManager.enableDebug(player);
        
        // 发送调试命令到客户端
        ServerNetworking.sendDebugCommandToClient(player, true);
        
        // 发送当前配置信息
        if (wasEnabled) {
            DebugManager.sendDebugInfo(DebugCategory.CONFIG, ServerI18nManager.translate("message.message.general_20"));
        }
        
        return 1;
    }
    
    /**
     * 禁用调试模式
     * @param context 命令上下文
     * @return 命令结果
     */
    private static int disableDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendFeedback(() -> Text.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        DebugManager.disableDebug(player);
        
        // 发送调试命令到客户端
        ServerNetworking.sendDebugCommandToClient(player, false);
        
        return 1;
    }
    
    /**
     * 显示调试状态
     * @param context 命令上下文
     * @return 命令结果
     */
    private static int showDebugStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendFeedback(() -> Text.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        boolean isEnabled = DebugManager.isDebugEnabled(player.getUuid());
        String status = isEnabled ? ServerI18nManager.translate("message.message.general_18") : ServerI18nManager.translate("message.message.general_19");
        Formatting color = isEnabled ? Formatting.GREEN : Formatting.RED;
        
        source.sendFeedback(() -> Text.translatable("command.hint.general")
                .append(Text.literal(status).formatted(color)), false);
        
        return 1;
    }
} 