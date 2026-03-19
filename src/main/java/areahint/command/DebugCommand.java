package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.debug.DebugManager;
import areahint.debug.DebugManager.DebugCategory;
import areahint.i18n.ServerI18nManager;
import areahint.network.ServerNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


/**
 * 闂佽崵濮撮鍛村疮閹惰姤鍎婃い鏍仜瀹告繈鏌熺€涙ê绗氬┑顔哄灩椤法鎹勯崫鍕典紑闂佽鍠栭敃锕傛偖?
 * 闂備焦妞垮鍧楀礉鐎ｎ剝濮虫い鎺嗗亾闁崇粯妫冩俊鎼佸Ψ閵夛附婢戦梻浣告啞缁矂鎳熼鐐堝洭鍩￠崨顔间哗闂佺硶鍓濆銊モ枔閸洘鍋ｉ柛銉ｅ妽缁€鍫熺箾閺夋埈妯€鐎规洘顨呴悾鐑藉炊閵娧勬闂備礁鎲＄粙鎺楀垂濠靛绠?
 */
public class DebugCommand {
    /**
     * 婵犵數鍋涢ˇ顓㈠礉瀹€鍕埞闁伙絽鏈€氼剟鏌涢幇闈涘箻婵″弶鎮傞弻娑樷槈濞嗗繒浜伴梺?
     * @param dispatcher 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濐槸缁€鍡涙煕閳╁喚娈旂紒鍙夋そ閺?
     * @param registryAccess 婵犵數鍋涢ˇ顓㈠礉瀹€鍕埞闁伙絽鏈崑姗€鏌曟繝搴ｅ帥闁哥喎绻樺?
     * @param environment 闂備胶绮划鐘诲垂娴兼番鈧?
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        
        dispatcher.register(
            CommandManager.literal("areahint")
                .then(CommandManager.literal("debug")
                    .requires(source -> source.hasPermissionLevel(2)) // 闂傚倸鍊稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄦ皑閸斿秵绻涢悡搴ｅⅵ婵﹣绮欓獮鍥敊閻愵剚鍎紓?
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
     * 闂備礁鎲＄敮鎺懨洪敃鈧悾鐑芥嚑椤掍礁顏╅梺鍛婂姦閸樻儳危閸涘﹣绻嗘い鏍ㄣ仜閸嬫挸鐣烽崶鈺婃敤
     * @param context 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇閳绘柨鈹戦悩杈厡闁荤喐鎹囬弻?
     * @return 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇川绾惧ジ鏌熼幆褜鍤熷ù?
     */
    private static int toggleDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
        
        if (player == null) {
            source.sendFeedback(TextCompat.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        if (DebugManager.isDebugEnabled(player.getUuid())) {
            return disableDebug(context);
        } else {
            return enableDebug(context);
        }
    }
    
    /**
     * 闂備礁鎲￠崙褰掑垂閹惰棄鏋侀柕鍫濇处鐎氼剟鏌涢幇闈涘箻婵℃彃鎲℃穱濠囶敍濡炶浜剧€规洖娲ㄩ、?
     * @param context 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇閳绘柨鈹戦悩杈厡闁荤喐鎹囬弻?
     * @return 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇川绾惧ジ鏌熼幆褜鍤熷ù?
     */
    private static int enableDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
        
        if (player == null) {
            source.sendFeedback(TextCompat.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        boolean wasEnabled = DebugManager.enableDebug(player);
        
        // 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴炲缁佷即鏌ｉ妸褍鏋旈柟椋庡█瀵挳鎮欓弶鎴烆吋濠电偛顕慨浼村磹閺囥垹鏄ョ€光偓閳ь剟鍩€椤掆偓缁犲秹宕瑰ú顏勬槬婵炴垶姘ㄩ崡?
        ServerNetworking.sendDebugCommandToClient(player, true);
        
        // 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶椤︼妇绱撳鍜佸剶鐎规洘绮岄鍏煎緞婵犲倽绁寸紓鍌氬€搁崰姘跺窗閺囩喓鈹嶅┑鐘叉搐缁?
        if (wasEnabled) {
            DebugManager.sendTranslatableDebugInfo(DebugCategory.CONFIG, "message.message.general_20");
        }
        
        return 1;
    }
    
    /**
     * 缂傚倷绀侀崐鐑芥嚄閸洖鏋侀柕鍫濇处鐎氼剟鏌涢幇闈涘箻婵℃彃鎲℃穱濠囶敍濡炶浜剧€规洖娲ㄩ、?
     * @param context 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇閳绘柨鈹戦悩杈厡闁荤喐鎹囬弻?
     * @return 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇川绾惧ジ鏌熼幆褜鍤熷ù?
     */
    private static int disableDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
        
        if (player == null) {
            source.sendFeedback(TextCompat.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        DebugManager.disableDebug(player);
        
        // 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴炲缁佷即鏌ｉ妸褍鏋旈柟椋庡█瀵挳鎮欓弶鎴烆吋濠电偛顕慨浼村磹閺囥垹鏄ョ€光偓閳ь剟鍩€椤掆偓缁犲秹宕瑰ú顏勬槬婵炴垶姘ㄩ崡?
        ServerNetworking.sendDebugCommandToClient(player, false);
        
        return 1;
    }
    
    /**
     * 闂備礁鎼€氼剚鏅舵禒瀣︽慨妯块哺鐎氼剟鏌涢幇闈涘箻婵″弶鎮傞弻锝呂熼崹顔惧帿闂?
     * @param context 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇閳绘柨鈹戦悩杈厡闁荤喐鎹囬弻?
     * @return 闂備礁鎲＄粙鎺楀垂濠靛绠柕鍫濇川绾惧ジ鏌熼幆褜鍤熷ù?
     */
    private static int showDebugStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null;
        
        if (player == null) {
            source.sendFeedback(TextCompat.translatable("command.message.general_30").formatted(Formatting.RED), false);
            return 0;
        }

        boolean isEnabled = DebugManager.isDebugEnabled(player.getUuid());
        String status = isEnabled ? ServerI18nManager.translateForPlayer(player.getUuid(), "message.message.general_18") : ServerI18nManager.translateForPlayer(player.getUuid(), "message.message.general_19");
        Formatting color = isEnabled ? Formatting.GREEN : Formatting.RED;

        source.sendFeedback(TextCompat.literal(ServerI18nManager.translateForPlayer(player.getUuid(), "command.hint.general"))
                .append(TextCompat.literal(status).formatted(color)), false);
        
        return 1;
    }
} 


