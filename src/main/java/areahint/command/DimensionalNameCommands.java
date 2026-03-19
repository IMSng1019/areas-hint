package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.dimensional.DimensionalNameManager;
import areahint.network.DimensionalNameNetworking;
import areahint.network.ServerNetworking;
import areahint.util.ColorUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import areahint.i18n.ServerI18nManager;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缂傚倸鍊烽悞锕€顭囧▎鎴斿亾鐟欏嫬鈻曠€规洘妞介幃銏ゆ煥鐎ｎ亖鍋撻幎鑺ョ厱婵炴垶鐟ラ悘濠囨煙闊彃鈧牕顕ラ崟顖氱妞ゆ挾鍠庨埀顒傚仱閺?
 * 濠电姰鍨煎▔娑氣偓姘煎櫍楠?/areahint dimensionalityname 闂?/areahint dimensionalitycolor 闂備礁鎲＄粙鎺楀垂濠靛绠?
 */
public class DimensionalNameCommands {

    // ===== dimensionalityname 濠电偛鐡ㄩ崵搴ㄥ磹閹炬儼濮抽柡澶嬵儥閸ゆ洟鐓崶銊﹀鞍闁搞倖甯掗湁?=====

    public static int executeStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        // 缂備胶铏庨崣搴ㄥ窗閺囩姵宕叉慨妯挎硾缁犮儵鏌嶈閸撶喎顕ｉ崹顐㈢窞閻庯綆鍋嗛悿鈧梻浣告啞閺岋繝鍩€椤掑啯鐝柣婵勫€楃槐鎾存媴鐟欏嫮绋囬柣搴ゎ潐婵炲﹪寮婚崼銉﹀癄濠㈣泛锕ゅ▓銉モ攽閻愬樊妲堕柛鏂跨焸瀹?
        syncServerDimensions(source.getServer());
        // 闂備胶顭堢换鎰版偋閸℃稒鍎嶉柕蹇嬪€曠€氬鏌嶈閸撶喎顕ｉ鍕妞ゆ棁濮ら埢鏇㈡煙閼圭増褰х紒鐘冲灴瀹曞綊顢旈崱妯轰粧闂侀潧顭堥崕杈╁緤濞差亝鈷戞い鎯点倕鍘＄紓渚囧枟婢瑰棝骞忛悩璇参ㄧ憸宥囨嫻閻斿吋鍊?
        DimensionalNameNetworking.sendDimensionalNamesToClient((ServerPlayerEntity) source.getEntity());
        sendClientCommand(source, "areahint:dimname_start");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeSelect(CommandContext<ServerCommandSource> context, String dimensionId) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        // 闂備礁鎲￠敋妞ゎ厼鍢查埢宥嗘償閳藉棗娈ㄩ梺鍝勬川閸嬫稒绻?
        if (dimensionId.startsWith("\"") && dimensionId.endsWith("\"") && dimensionId.length() > 1) {
            dimensionId = dimensionId.substring(1, dimensionId.length() - 1);
        }
        sendClientCommand(source, "areahint:dimname_select:" + dimensionId);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeName(CommandContext<ServerCommandSource> context, String newName) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        if (newName == null || newName.trim().isEmpty()) {
            source.sendError(TextCompat.translatable("command.message.dimension.name_2"));
            return 0;
        }
        final String finalNewName = newName.trim();
        if (finalNewName.length() > 50) {
            source.sendError(TextCompat.translatable("command.message.dimension.name_3"));
            return 0;
        }

        sendClientCommand(source, "areahint:dimname_name:" + finalNewName);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimname_confirm");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimname_cancel");
        return Command.SINGLE_SUCCESS;
    }

    // ===== dimensionalitycolor 濠电偛鐡ㄩ崵搴ㄥ磹閹炬儼濮抽柡澶嬵儥閸ゆ洟鐓崶銊﹀鞍闁搞倖甯掗湁?=====

    public static int executeColorStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        syncServerDimensions(source.getServer());
        DimensionalNameNetworking.sendDimensionalNamesToClient((ServerPlayerEntity) source.getEntity());
        sendClientCommand(source, "areahint:dimcolor_start");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorSelect(CommandContext<ServerCommandSource> context, String dimensionId) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        if (dimensionId.startsWith("\"") && dimensionId.endsWith("\"") && dimensionId.length() > 1) {
            dimensionId = dimensionId.substring(1, dimensionId.length() - 1);
        }
        sendClientCommand(source, "areahint:dimcolor_select:" + dimensionId);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorColor(CommandContext<ServerCommandSource> context, String colorValue) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimcolor_color:" + colorValue);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimcolor_confirm");
        return Command.SINGLE_SUCCESS;
    }

    public static int executeColorCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        sendClientCommand(source, "areahint:dimcolor_cancel");
        return Command.SINGLE_SUCCESS;
    }

    // ===== 闂備礁鎼悧鍡欑矓鐎涙ɑ鍙忛柣鏃囨閸楁碍銇勯弽銊ュ毈婵℃煡浜堕弻锝夋倷閸欏妫￠梺鍝勭灱閸犲酣鍩㈤弮鍫濆嵆妞ゅ繐妫涢ˇ鈺呮⒑濮瑰洤鐏╃€规洦鍓熼、妯裤亹閹烘垹顓烘俊鐐差儏鐎垫帗瀵兼笟绫ager闂佽崵濮撮鍛村疮娴兼潙鏋侀柕鍫濐槹閺?=====

    /**
     * 闂備礁鎼悧鍡欑矓鐎涙ɑ鍙忛柣鏃囨閸楁碍銇勯弽銊ュ毈婵℃煡浜堕弻锝夋倷閸欏妫ゅ┑鐑囩秵閸犳骞嗛弮鍫熸櫇闁逞屽墴閹€斥枎閹惧疇袝閻庡箍鍎卞Λ娆戠棯椤栫偞鐓涢柍?
     */
    public static void handleDimNameChange(ServerCommandSource source, String dimensionId, String newName) {
        try {
            String oldName = DimensionalNameManager.getDimensionalName(dimensionId);
            DimensionalNameManager.setDimensionalName(dimensionId, newName);

            if (DimensionalNameManager.saveDimensionalNames()) {
                source.sendFeedback(TextCompat.translatable("command.message.area.dimension_2"), false);
                source.sendFeedback(TextCompat.translatable("command.message.dimension").append(TextCompat.literal(dimensionId)), false);
                source.sendFeedback(TextCompat.translatable("command.message.name_3").append(TextCompat.literal(oldName)), false);
                source.sendFeedback(TextCompat.translatable("command.message.name_2").append(TextCompat.literal(newName)), false);

                // 婵°倗濮烽崑鐐碘偓绗涘洤绠伴梺顒€绉寸粈鍡涙煟濡も偓閻楀棝銆傞弻銉︾厸闁割偅绻嶅Σ鍛娿亜閹惧磭绉烘鐐差儐椤︾増鎯旈鑽ょ＝
                DimensionalNameNetworking.sendDimensionalNamesToAllClients(source.getServer());
                // 闂傚倷绶￠崑鍛潩閵娾晜鍋傞柨娑樺閸嬫捇鐛崹顔句痪闂佺硶鏅滅粙鎾跺垝閳哄懏鍎夐柣顔炬杸oad
                ServerNetworking.sendCommandToAllClients(source.getServer(), "areahint:reload");

                Areashint.LOGGER.info(ServerI18nManager.translate("command.message.dimension.name"),
                    source.getName(), dimensionId, oldName, newName);
            } else {
                source.sendError(TextCompat.translatable("command.error.area.dimension.save"));
            }
        } catch (Exception e) {
            source.sendError(TextCompat.translatable("command.error.dimension.name_2").append(TextCompat.literal(e.getMessage())));
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.dimension.name"), e);
        }
    }

    /**
     * 闂備礁鎼悧鍡欑矓鐎涙ɑ鍙忛柣鏃囨閸楁碍銇勯弽銊ュ毈婵℃煡浜堕弻锝夋倷閸欏妫ゅ┑鐑囩秵閸犳骞嗛弮鍫熸櫇闁逞屽墴閹€斥枎閹惧疇袝閻庡箍鍎遍ˇ浠嬪级瑜版帗鐓犻柣鐔告緲缁狙勭箾閹绘帗鍋ョ€?
     */
    public static void handleDimColorChange(ServerCommandSource source, String dimensionId, String newColor) {
        try {
            String oldColor = DimensionalNameManager.getDimensionalColor(dimensionId);
            String oldColorDisplay = oldColor != null ? oldColor : ServerI18nManager.translate("command.message.general_6");
            DimensionalNameManager.setDimensionalColor(dimensionId, newColor);

            if (DimensionalNameManager.saveDimensionalNames()) {
                source.sendFeedback(TextCompat.translatable("command.message.area.color.dimension"), false);
                source.sendFeedback(TextCompat.translatable("command.message.dimension").append(TextCompat.literal(dimensionId)), false);
                source.sendFeedback(TextCompat.translatable("command.message.color_5").append(TextCompat.literal(oldColorDisplay)), false);
                source.sendFeedback(TextCompat.translatable("command.message.color_6").append(TextCompat.literal(newColor)), false);

                DimensionalNameNetworking.sendDimensionalNamesToAllClients(source.getServer());
                ServerNetworking.sendCommandToAllClients(source.getServer(), "areahint:reload");

                Areashint.LOGGER.info(ServerI18nManager.translate("command.message.color.dimension"),
                    source.getName(), dimensionId, oldColorDisplay, newColor);
            } else {
                source.sendError(TextCompat.translatable("command.error.area.dimension.save"));
            }
        } catch (Exception e) {
            source.sendError(TextCompat.translatable("command.error.color.dimension_2").append(TextCompat.literal(e.getMessage())));
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.color.dimension"), e);
        }
    }

    /**
     * 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墮鐎氬鈧箍鍎遍幊搴綖閵堝鐓曢柨婵嗗暙婵¤棄顭胯缁夌懓顕ｉ崹顐㈢窞閻忕偟鍋撻埢鏇㈡煙閸忓吋鍎楁慨锝呯摃
     */
    public static Set<String> getServerDimensions(ServerCommandSource source) {
        Set<String> dims = source.getServer()
            .getWorldRegistryKeys()
            .stream()
            .map(key -> key.getValue().toString())
            .collect(Collectors.toSet());
        dims.add("minecraft:overworld");
        dims.add("minecraft:the_nether");
        dims.add("minecraft:the_end");
        return dims;
    }

    /**
     * 闂佽绻愮换鎰涘▎蹇ヨ€块柛銉墮缁€澶愭煃閵夈劍鐝柣婵勫€濋弻鐔哄枈濡桨澹曢梻浣告惈閻楀棝藝閸楃們搴敊閼恒儱鍔呴梺鎸庢磵閸嬫捇鏌熼鑺ュ碍妞ゎ偁鍨芥俊鐑藉Ψ閵壯冨笓缂傚倷鑳舵刊瀵告閺囥垹绠栧┑鐘叉搐闂傤垶鏌曟繛鍨偓娑㈠储椤掑嫭鐓ユ繛鎴烆焽婢с垽鏌嶉妷喂鎴犵矚閸楃偐鏀介柛銉ｅ妼缁憋箓姊绘担瑙勭凡缂佸鐖奸幃鍧楀幢濞戞瑥鍓梺鍛婃处閸樿棄螣婵犲洤绠归弶鍫濆⒔缁辨壆绱掗垾鍐叉殻闁硅櫕顨婇幊鐐哄Ψ閿旇鐏冪紓鍌欒閸嬫捇鏌ｅ顒夊殶缂佲偓?
     */
    public static void syncServerDimensions(MinecraftServer server) {
        Set<String> serverDims = server.getWorldRegistryKeys()
            .stream()
            .map(key -> key.getValue().toString())
            .collect(Collectors.toSet());
        for (String dimId : serverDims) {
            if (!DimensionalNameManager.hasDimensionalName(dimId)) {
                // 闂備礁鎼悧婊勭椤忓牆鍌ㄩ柕鍫濇川绾剧偓銇勯弮鍌氫壕闁伙綀浜槐鎾存媴鐟欏嫮绋囬柣搴ｎ攰濞呮洟骞忛崨顖涘磯闁靛闄勫▓銏㈢磽閸屾瑧鍔嶆繛鍏肩懅閳ь剙婀遍悡绯勫┑鐐舵彧缁插墽鍒掗崼銏″闁绘柨澹婂〒鑸典繆椤栨侗鍎ラ柛姘喘閺屾稑顫濋鍌傘倗鎮?
                DimensionalNameManager.setDimensionalName(dimId, dimId);
            }
        }
        DimensionalNameManager.saveDimensionalNames();
    }

    // ===== 濠碘槅鍋撶徊楣冩偋濡ゅ拋鏁冮柤娴嬫櫇绾惧ジ鏌℃径瀣仴妞ゆ柨锕弻娑樷槈濠婂嫷妫勯梺璇″枟濞茬喖寮澶婇唶闁绘棃娼绘蹇涙⒑閸濆嫷妲搁柛鐔告綑椤﹪鎼圭憴鍕毇婵炶揪绲藉﹢鍗烇耿閹绢喗鐓?=====

    public static int executeFirstDimName(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) return 0;

        String dimId = ((ServerPlayerEntity) source.getEntity()).getWorld().getRegistryKey().getValue().toString();
        String currentName = DimensionalNameManager.getDimensionalName(dimId);

        // 濠电偛顕慨鎾箠鎼达絿绀婇悗锝庡墰绾惧ジ鏌℃径瀣仴妞ゆ柨锕弻娑橆潩椤掑倐銈囨偖閵娧呯＜婵炴垶锕╁Σ椋庣磼椤垵澧扮紒杈ㄥ浮瀵爼骞嬪┑鎰暯ID闂備礁鎼崯鎶筋敊閹邦喗顫曟繛鍡樻尭鐎氬銇勮箛鎾寸ォ婵炵厧鐖奸弻娑樷槈濠婂嫷妫勯梺璇″枟濞茬喖寮鍡楃窞閻庯綆浜堕弸鈧梻浣侯焾缁诲霉閸ヮ剦鏁?
        if (!currentName.equals(dimId)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.dimension_4").append(TextCompat.literal(currentName)));
            return 0;
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 50) {
            source.sendError(TextCompat.translatable("command.error.name"));
            return 0;
        }

        handleDimNameChange(source, dimId, trimmed);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeFirstDimNameSkip(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) return 0;

        String dimId = ((ServerPlayerEntity) source.getEntity()).getWorld().getRegistryKey().getValue().toString();
        String currentName = DimensionalNameManager.getDimensionalName(dimId);

        if (currentName.equals(dimId)) {
            // 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡堕崶鈺冿紲闂佸搫顦伴崹鐢割敊婵犲洦鍋ｅ〒姘煎灠閻忕姷绱掗崜浣镐沪闁瑰嘲鎳庨…銊╁焵椤掑倹瀚婚柣鏂垮濞撹埖淇婇娑卞劌闁告艾娲弻娑橆潩椤掑倐銈囨偖閵娾晜鐓ユ繛鎴烆焽婢ф洘銇?overworld, the_nether闂?
            String defaultName = ((ServerPlayerEntity) source.getEntity()).getWorld().getRegistryKey().getValue().getPath();
            handleDimNameChange(source, dimId, defaultName);
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.name").append(TextCompat.literal(defaultName)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void sendClientCommand(ServerCommandSource source, String command) {
        try {
            if (source.getPlayer() != null) {
                ServerNetworking.sendCommandToClient(source.getPlayer(), command);
            }
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.message.general_17"), e);
        }
    }
}
