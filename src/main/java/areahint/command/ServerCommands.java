package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.data.ConfigData;
import areahint.file.FileManager;
import areahint.file.JsonHelper;
import areahint.i18n.ServerI18nManager;
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
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * 缂傚倸鍊搁崐鎼佸磹閹间礁纾归柣鎴ｅГ閸ゅ嫰鏌涢锝嗙缂佹劖顨堥埀顒€绠嶉崕鍗灻洪妸鈺佺婵鍩栭悡娆戠磽娴ｉ潧鐏╅柡瀣〒閳ь剛鎳撻幉锛勬崲閸儱钃熼柣鏃囥€€閸嬫挸鈽夊▍顓т簽缁厼顫濋鑺ユ杸濡炪倖鏌ㄩ幖顐︽倶閳哄倶浜滄い鎾墲绾爼鏌熼悷鏉款伃鐎规洦鍋婂畷鐔煎礂閸濄儳锛涢梻鍌氬€峰ù鍥敋閺嶎厼绐楅柡宥庡幗閺呮繈鏌ㄩ弴鐐测偓褰掑磻?
 * 闂傚倸鍊峰ù鍥敋瑜庨〃銉х矙閸柭も偓鍧楁⒑椤掆偓缁夊澹曟繝姘厽闁哄啫娲ゆ禍鍦偓瑙勬尫缁舵岸寮诲☉銏犖ㄦい鏃傚帶椤晠鏌ｆ惔銏ｅ妞わ箓浜堕崺鈧い鎺嗗亾缂佺姴绉瑰畷鏇㈡焼瀹ュ懐鐤囬柟鍏兼儗閻撳绱為弽顓熺厪闁割偅绻冨婵堢棯閹佸仮闁哄矉绻濆畷姗€鍩℃担杞版樊闂備浇顕х换鎺撶箾閳ь剟鏌″畝鈧崰鏍蓟閵娧€鍋撻敐搴濇喚婵☆偄妫濆娲焻閻愯尪瀚板褜鍠氱槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣烽垾鎰佹僵妞ゆ挻绮堢花濠氭⒑閸濆嫮袪闁告柨绻戠粩鐔肺熼崗鐓庢瀾闂佺粯顨呴悧蹇涘箠閸愵喗顥嗗璺侯儑缁♀偓婵犵數濮撮崐鍧楊敁濡も偓闇夋繝濠傛噹娴狅妇绱掔紒妯兼创鐎规洖銈搁幃銏☆槹鎼存繄绀夐梺璇叉唉椤煤韫囨稑绀夐柟杈惧瀹撲線鎮楅敐搴℃灈缂佺姵濞婇弻鐔兼倷椤掆偓椤ョ偤鏌熸潏楣冩闁抽攱鍨块弻娑樷槈濮楀牊鏁鹃梺鎶芥敱鐢繝寮诲☉銏″亹闁惧浚鍋嗛ˇ浼存倵?
 */
public class ServerCommands {
    /**
     * 濠电姷鏁告慨鐑藉极閹间礁纾绘繛鎴旀嚍閸ヮ剦鏁囬柕蹇曞Х椤︻噣鎮楅崗澶婁壕闂佸憡娲﹂崑澶愬春閻愬绠鹃悗鐢殿焾瀛濆銈嗗灥閹冲酣顢欒箛鎾斀閻庯綆鍋嗛崢鐢告煟鎼淬垻鈯曢柨姘辩磼濡や浇澹橀柍?
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ServerCommands::registerCommands);
    }
    
    /**
     * 濠电姷鏁告慨鐑藉极閹间礁纾绘繛鎴旀嚍閸ヮ剦鏁囬柕蹇曞Х椤︻噣鎮楅崗澶婁壕闂佸憡娲﹂崑澶愬春閻愬绠鹃悗鐢殿焾瀛濆銈嗗灥閹虫劗鍒掓繝姘ㄩ柍鍝勫€婚崢鎾绘偡濠婂嫮鐭掔€规洘绮撻幃銏＄附婢跺﹥顓块梻浣稿閻撳牓宕戦崟顓燁偨闁绘劖绁撮弨浠嬫煟濡绲婚柡鍡樼懄缁绘盯宕煎☉妯哄缂?
     * @param dispatcher 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栫瑧缂傚倷璁查崑鎾绘煕閳╁啰鈯曢柣鎾存礋閺屽秹鍩℃担鍛婄亾濠电偛鐗婂鑽ゆ閹烘鐭楀璺侯儍娴犮垽姊?
     * @param registryAccess 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑閸濆嫷鍎愰柣妤冨█瀵寮撮悢铏诡啎闂佸壊鐓堥崰鏍ㄦ叏鎼淬劍鈷戦柛婵嗗閳ь剛鏁诲畷鎴﹀箻缂佹ǚ鎷洪梺鐓庮潟閸婃洖鐨紓鍌欒閸嬫挸霉閿濆懎顥忛柣鎺嶇矙閺屽秹濡烽敂鍓х嵁濠电偞鍨崹褰掓煁閸ヮ剚鐓涢柛銉㈡櫅娴犺鲸淇?
     * @param environment 闂傚倸鍊搁崐鐑芥嚄閸撲礁鍨濇い鏍ㄧ矊閸ㄦ繈鏌ｅΟ娆惧殭闁搞劌鍊搁湁闁稿繗鍋愰弳姗€鏌?
     */
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // 濠电姷鏁告慨鐑藉极閹间礁纾绘繛鎴旀嚍閸ヮ剦鏁囬柕蹇曞Х椤︻噣鎮楅崗澶婁壕闂佸憡娲﹂崑澶愬春閻愮儤鈷戝ù鍏肩懅閸掍即鏌￠崼顐㈠闁逞屽墯閸戝綊宕滈悢鐓庤摕婵炴垯鍨圭粻娑㈡⒒閸喓鈽夌紒鐘侯嚙閳规垿鍨鹃崘鑼獓闂佽鍠栭崐鍨嚕鐠囨祴妲堟俊顖炴敱椤秴鈹戦悙鍙夘棞缂佺粯甯楃粋宥嗗鐎涙ǚ鎷?
        DebugCommand.register(dispatcher, dedicated);
        
        // 濠电姷鏁告慨鐑藉极閹间礁纾绘繛鎴旀嚍閸ヮ剦鏁囬柕蹇曞Х椤︻噣鎮楅崗澶婁壕闂佸憡娲﹂崑澶愬春閻愮數纾藉ù锝囶焾缁狙勩亜閹烘繃鎳€ck闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
        CheckCommand.register(dispatcher, dedicated);
        
        dispatcher.register(literal("areahint")
            // help 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("help")
                .executes(ServerCommands::executeHelp))
            
            // reload 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("reload")
                .executes(ServerCommands::executeReload))
            
            // dimensionalityname 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(婵犵數濮烽弫鎼佸磻濞戙垺鍋ら柕濞炬櫅閸氬綊骞栧ǎ顒€濡肩痪鎯х秺閺岀喖鎮欓鈧崝璺衡攽椤旇棄鈻曢柡灞稿墲瀵板嫮鈧綁娼ч崝宀勬⒑閹肩偛鈧牕煤閻斿吋鍋傛い鎰剁畱閻愬﹪鏌曟繝蹇擃洭妞わ负鍔岄埞鎴﹀煡閸℃浠╅梺鎸庢处娴滎亪鎮伴鈧浠嬪Ω閿斿墽肖闂備礁鎲￠幐鍡涘川椤旂瓔鍟呴梻鍌氬€烽懗鍓佸垝椤栫偛绠板┑鐘崇閸嬶繝鏌嶆潪鎷屽厡闁哄棴绠撻弻鏇熷緞閸繂濮堕梺鍛婅壘椤戝寮诲☉銏犖ㄩ柕濠忓閵嗘劙姊虹拠鈥虫灆缂佽埖鑹鹃～?
            .then(literal("dimensionalityname")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DimensionalNameCommands::executeStart)
                .then(literal("select")
                    .then(argument("dimension", StringArgumentType.greedyString())
                        .executes(context -> DimensionalNameCommands.executeSelect(context,
                            StringArgumentType.getString(context, "dimension")))))
                .then(literal("name")
                    .then(argument("newName", StringArgumentType.greedyString())
                        .executes(context -> DimensionalNameCommands.executeName(context,
                            StringArgumentType.getString(context, "newName")))))
                .then(literal("confirm")
                    .executes(DimensionalNameCommands::executeConfirm))
                .then(literal("cancel")
                    .executes(DimensionalNameCommands::executeCancel)))

            // dimensionalitycolor 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(婵犵數濮烽弫鎼佸磻濞戙垺鍋ら柕濞炬櫅閸氬綊骞栧ǎ顒€濡肩痪鎯х秺閺岀喖鎮欓鈧崝璺衡攽椤旇棄鈻曢柡灞稿墲瀵板嫮鈧綁娼ч崝宀勬⒑閹肩偛鈧牕煤閻斿吋鍋傛い鎰剁畱閻愬﹪鏌曟繝蹇擃洭妞わ负鍔岄埞鎴﹀煡閸℃浠╅梺鎸庢处娴滎亪鎮伴鈧浠嬪Ω閿斿墽肖闂備礁鎲￠幐鍡涘川椤旂瓔鍟呴梻鍌氬€烽懗鍓佸垝椤栫偛绠板┑鐘崇閸嬶繝鏌嶆潪鎷屽厡闁哄棴绠撻弻鏇熷緞閸繂濮堕梺鍛婅壘椤戝懘鈥﹂崸妤佸殝闁活剦浜濋崹鐢糕€﹂崸妤€绠虫俊銈勮閹锋椽姊洪崗鑲┿偞闁哄應鏅犲銊﹀鐎涙ê鈧爼鐓崶銊︹拻闁瑰啿娲﹂妵鍕閳藉懓鈧寧顨ラ悙瀵稿闁瑰嘲鎳橀幃閿嬶紣娴ｅ憡鎲?
            .then(literal("dimensionalitycolor")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(DimensionalNameCommands::executeColorStart)
                .then(literal("select")
                    .then(argument("dimension", StringArgumentType.greedyString())
                        .executes(context -> DimensionalNameCommands.executeColorSelect(context,
                            StringArgumentType.getString(context, "dimension")))))
                .then(literal("color")
                    .then(argument("colorValue", StringArgumentType.greedyString())
                        .executes(context -> DimensionalNameCommands.executeColorColor(context,
                            StringArgumentType.getString(context, "colorValue")))))
                .then(literal("confirm")
                    .executes(DimensionalNameCommands::executeColorConfirm))
                .then(literal("cancel")
                    .executes(DimensionalNameCommands::executeColorCancel)))

            // firstdimname 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ璇茬祷濠殿喛娉涢埞鎴︽倷閼碱剚鎲煎┑鐐插级椤洭骞戦姀鐘闁靛繒濮撮懓鍨渻閵堝棙灏靛┑顔惧厴閺佸秹鎮㈢亸浣规杸闂佺粯鍨靛ú銊х矓閻㈠憡鐓曢柣妯诲墯濞堟粍顨ラ悙鍙夘棦鐎殿噮鍣ｅ畷濂告偄閸涘﹦褰囬梻鍌欑窔閳ь剛鍋涢懟顖涙櫠鐎涙﹩娈介柣鎰嚋闊剟鏌℃担瑙勫磳闁诡喒鏅犻幊鐘电磽鎼淬垺绀嬮柟顔筋殜閻涱噣宕归鐓庮潛缂傚倷鑳剁划顖滄暜閻愰潧鍨濋柛顐ゅ枎缁剁偤鏌熼柇锕€骞橀柛妯绘倐濮婃椽宕ㄦ繝鍌毿曟繛瀛樼矋缁秹鎮ф惔銏㈢瘈闁汇垽娼ф禒褔鏌涘Ο鐘叉处閸嬪鏌熼悙顒€澧繛鍏肩墪闇夐柣妯烘▕閸庡繒绱掗悩宕団姇闁靛洤瀚板浠嬫偨閻㈢灙鎴︽⒑濞茶骞楁い銊ユ嚇閸╃偤骞嬮敂缁樻櫓闂佺粯鍔﹂崜娆忊枔瀹€鍕拺缂佸顑欓崕蹇涙煥閺囶亞鐣遍崡?
            .then(literal("firstdimname")
                .then(argument("name", StringArgumentType.greedyString())
                    .executes(context -> DimensionalNameCommands.executeFirstDimName(context,
                        StringArgumentType.getString(context, "name")))))
            .then(literal("firstdimname_skip")
                .executes(DimensionalNameCommands::executeFirstDimNameSkip))

            // add 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(婵犵數濮烽弫鎼佸磻濞戙埄鏁嬫い鎾跺枑閸欏繘鏌熺紒銏犳灈缂佺姷濞€閻擃偊宕堕妸锔藉創闂佸吋婢樺锟犲蓟濞戙垹唯妞ゆ梻鍘ч～鈺冪磽娴ｆ彃浜炬繝銏ｅ煐閸旀牠鎮￠妷锔剧闁瑰浼濋鍫晜闁哄鍤?
            .then(literal("add")
                .requires(source -> source.hasPermissionLevel(2)) // 闂傚倸鍊搁崐鎼佸磹閹间礁纾圭紒瀣紩濞差亝鍋愰悹鍥皺閿涙盯姊洪悷鏉库挃缂侇噮鍨跺畷鎴︽晸閻樺磭鍘搁梺鎼炲劘閸斿绂嶉姀銈嗙厱闊洦鎸诲﹢浼存煏閸パ冾伃妤犵偞顭囬埀顒佺⊕閿氶柍褜鍓氶崝娆撳蓟閻旂⒈鏁婇柤娴嬫櫅閻撶喖鎮楃憴鍕鐎规洦鍓濋悘鍐╃箾鏉堝墽鍒伴柟璇х節閹顢曢敂瑙ｆ嫽婵炶揪绲介幉锟犲箚閸喆浜滈柨鏃囶嚙閻忥妇鈧娲﹂崹璺侯嚕閸洖绠ｆい鎾跺Л閸?
                .then(argument("json", StringArgumentType.greedyString())
                    .executes(context -> executeAdd(context, StringArgumentType.getString(context, "json"))))
            )
            
            // delete 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(婵犵數濮烽弫鎼佸磻濞戙垺鍋ら柕濞炬櫅閸氬綊骞栧ǎ顒€濡肩痪鎯х秺閺岀喖鎮欓鈧崝璺衡攽椤旇棄鈻曢柡灞稿墲瀵板嫮鈧綁娼ч崝宀勬⒑閹肩偛鈧牕煤閻斿吋鍋傛い鎰剁畱閻愬﹪鏌曟繝蹇曠暠缁炬澘绉瑰铏规嫚閼碱剛鐣鹃梺鍝勬噽婵挳锝?
            .then(literal("delete")
                .executes(ServerCommands::executeDeleteStart)
                .then(literal("select")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .suggests(DELETABLE_AREA_SUGGESTIONS)
                        .executes(context -> executeDeleteSelect(context, StringArgumentType.getString(context, "areaName")))))
                .then(literal("confirm")
                    .executes(ServerCommands::executeDeleteConfirm))
                .then(literal("cancel")
                    .executes(ServerCommands::executeDeleteCancel))
            )
            
            // frequency 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("frequency")
                .then(argument("value", IntegerArgumentType.integer(1, 60))
                    .executes(context -> executeFrequency(context, IntegerArgumentType.getInteger(context, "value"))))
                .executes(ServerCommands::executeFrequencyInfo))
            
            // subtitlerender 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("subtitlerender")
                .then(argument("mode", StringArgumentType.word())
                    .executes(context -> executeSubtitleRender(context, StringArgumentType.getString(context, "mode"))))
                .executes(ServerCommands::executeSubtitleRenderInfo))
            
            // subtitlestyle 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁绘挷绶氬娲传閵夈儰绮跺銈忓瘜閸ㄨ泛锕㈡担绯曟斀闁绘顕滃銉╂倵濮樼厧澧扮紒顕呭弮楠炴帡寮崒婊愮床闂備浇顫夎ぐ鍐敄閸モ晛濮柍褜鍓熷?
            .then(literal("subtitlestyle")
                .executes(ServerCommands::executeSubtitleStyleStart)
                .then(literal("select")
                    .then(argument("style", StringArgumentType.word())
                        .executes(context -> executeSubtitleStyleSelect(context, StringArgumentType.getString(context, "style")))))
                .then(literal("cancel")
                    .executes(ServerCommands::executeSubtitleStyleCancel)))

            // subtitlesize 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁绘挷绶氬娲传閵夈儰绮跺銈忓瘜閸ㄨ泛锕㈡担绯曟斀闁绘顕滃銉╂倵濮樼厧澧扮紒顕呭弮楠炴帡寮崒婊愮床闂備浇顫夎ぐ鍐敄閸モ晛濮柍褜鍓熷?
            .then(literal("subtitlesize")
                .executes(ServerCommands::executeSubtitleSizeStart)
                .then(literal("select")
                    .then(argument("size", StringArgumentType.word())
                        .executes(context -> executeSubtitleSizeSelect(context, StringArgumentType.getString(context, "size")))))
                .then(literal("cancel")
                    .executes(ServerCommands::executeSubtitleSizeCancel)))
                
            // easyadd 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絽銈稿鐑樻姜娴煎瓨顎栭梺鍛婃煥閻倿鍨鹃敃鍌氶敜婵°倓绀佸▓婵嬫⒑濮瑰洤鐏柡浣规倐瀹曪絽鈹戠€ｎ偀鎷绘繛杈剧悼椤牏鐥缁辨帡顢欐總绋垮及闂佺硶鏂侀崑鎾愁渻閵堝棗绗傞柤鍐茬埣閸╁﹪寮撮姀锛勫幈闂佸搫鍟犻崑鎾寸箾閼碱剙鏋涢柣娑卞枟缁绘繈宕惰閸旀挳姊洪崨濠傚Е濞存粏娉涢湁闁告洦鍨遍埛鎴︽⒒閸喓銆掑褎娲滅槐鎺旂磼濡櫣顑傜紓?
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
                            
            // expandarea 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼閻樺磭澧甸柡宀嬬節瀹曞爼濡烽妷褌鎮ｇ紓鍌欒兌婵即宕曢悽绋胯摕鐎广儱妫欐慨婊堟煙瀹勬媽瀚伴柛妯封偓鏂ユ斀?
            .then(literal("expandarea")
                .executes(ServerCommands::executeExpandAreaStart)
                // 闂傚倸鍊搁崐鐑芥嚄閸洖纾块柣銏㈩焾閻ょ偓绻涢幋娆忕仾闁稿鍊濋弻鏇熺箾瑜嶇€氼厼鈻撴导瀛樷拺闁革富鍙€濡炬悂鏌涢悩宕囧⒈缂侇喚绮€佃偐鈧稒顭囬崢閬嶆⒑闂堟侗妲归柛銊ф暬楠炲﹨绠涢幘鏃€甯楅幆鏃堝Ω閿旇瀚肩紓鍌欑贰閸ㄥ崬煤濡　鏋嶉柛銉墯閻撴洟鏌ｅΟ鑽ゅ弨闁告瑥瀚〃銉╂倷閹绘帗娈婚悗娈垮枟閹歌櫕鎱ㄩ埀顒勬煃閳轰礁鏆為柣?areahint expandarea "闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼?
                .then(argument("areaName", StringArgumentType.greedyString())
                    .suggests(EXPANDABLE_AREA_SUGGESTIONS)
                    .executes(context -> executeExpandAreaSelect(context, 
                        StringArgumentType.getString(context, "areaName"))))
                // 闂傚倸鍊峰ù鍥敋瑜忛埀顒佺▓閺呮繄鍒掑▎鎾崇婵°倐鍋撶紒鈧径鎰厸闁搞儯鍎遍悘鈺冪磼閻橀潧鈻堥柡灞界Ч閸┾剝鎷呴崨濠冾啀婵＄偑鍊愰弲婵嬪礂濮椻偓瀵寮撮姀鐘诲敹濠电娀娼ч悧蹇涱敊婵犲嫮纾藉ù锝嗗絻娴滈箖姊洪崨濠冨闁稿﹥鎸婚幆鏃堝Ω閵壯冣偓鐐烘⒑闂堟丹娑㈠川椤栨稒鐦掗梻鍌欐祰椤曆勵殽閹间焦鍊舵慨姗嗗劦濞戙垹绀冮柕濞у嫭顔曢梻渚€娼чˇ顐﹀疾濞戞氨涓嶉柨婵嗘缁♀偓闂傚倸鐗婄粙鎺楀箹閹扮増鐓冪憸婊堝礈濞戙垹鏋佸┑鐘宠壘閽冪喐绻涢幋鐐电煂闁汇倐鍋撻梻浣告惈濞层劑宕戝☉娆戭浄婵☆垯璀﹀〒濠氭煏閸繃顥為柍閿嬪姍閺屾盯寮埀顒勬偡閳轰緡鍤?
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
            
            // shrinkarea 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼閻樺磭澧甸柡宀嬬節瀹曞爼濡烽妷褌鎮ｇ紓鍌氬€风拋鏌ュ疾閻樿钃熸繛鎴欏灩缁犲鏌ら懝鐗堢【闁告﹫绱曠槐?
            .then(literal("shrinkarea")
                .executes(ServerCommands::executeShrinkAreaStart)
                // 闂傚倸鍊搁崐鐑芥嚄閸洖纾块柣銏㈩焾閻ょ偓绻涢幋娆忕仾闁稿鍊濋弻鏇熺箾瑜嶇€氼厼鈻撴导瀛樷拺闁革富鍙€濡炬悂鏌涢悩宕囧⒈缂侇喚绮€佃偐鈧稒顭囬崢閬嶆⒑闂堟侗妲归柛銊ф暬楠炲﹨绠涢幘鏃€甯楅幆鏃堝Ω閿旇瀚肩紓鍌欑贰閸ㄥ崬煤濡　鏋嶉柛銉墯閻撴洟鏌ｅΟ鑽ゅ弨闁告瑥瀚〃銉╂倷閹绘帗娈婚悗娈垮枟閹歌櫕鎱ㄩ埀顒勬煃閳轰礁鏆為柣?areahint shrinkarea "闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼?
                .then(argument("areaName", StringArgumentType.greedyString())
                    .suggests(SHRINKABLE_AREA_SUGGESTIONS)
                    .executes(context -> executeShrinkAreaSelect(context, 
                        StringArgumentType.getString(context, "areaName"))))
                // 闂傚倸鍊峰ù鍥敋瑜忛埀顒佺▓閺呮繄鍒掑▎鎾崇婵°倐鍋撶紒鈧径鎰厸闁搞儯鍎遍悘鈺冪磼閻橀潧鈻堥柡灞界Ч閸┾剝鎷呴崨濠冾啀婵＄偑鍊愰弲婵嬪礂濮椻偓瀵寮撮姀鐘诲敹濠电娀娼ч悧蹇涱敊婵犲嫮纾藉ù锝嗗絻娴滈箖姊洪崨濠冨闁稿﹥鎸婚幆鏃堝Ω閵壯冣偓鐐烘⒑闂堟丹娑㈠川椤栨稒鐦掗梻鍌欐祰椤曆勵殽閹间焦鍊舵慨姗嗗劦濞戙垹绀冮柕濞у嫭顔曢梻渚€娼чˇ顐﹀疾濞戞氨涓嶉柨婵嗘缁♀偓闂傚倸鐗婄粙鎺楀箹閹扮増鐓冪憸婊堝礈濞戙垹鏋佸┑鐘宠壘閽冪喐绻涢幋鐐电煂闁汇倐鍋撻梻浣告惈濞层劑宕戝☉娆戭浄婵☆垯璀﹀〒濠氭煏閸繃顥為柍閿嬪姍閺屾盯寮埀顒勬偡閳轰緡鍤?
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

            // dividearea 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼閻樺磭澧甸柡宀嬬節瀹曞爼濡烽妷褌鎮ｇ紓鍌欒閸嬫捇鏌涢埄鍐姇闁绘挻娲熼弻宥夊煛娴ｅ憡鐏撳┑鐐茬墛濮婂綊濡?
            .then(literal("dividearea")
                .executes(ServerCommands::executeDivideAreaStart)
                .then(literal("select")
                    .then(argument("selectAreaName", StringArgumentType.greedyString())
                        .executes(context -> executeDivideAreaSelect(context,
                            StringArgumentType.getString(context, "selectAreaName")))))
                .then(literal("continue")
                    .executes(ServerCommands::executeDivideAreaContinue))
                .then(literal("save")
                    .executes(ServerCommands::executeDivideAreaSave))
                .then(literal("name")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> executeDivideAreaName(context,
                            StringArgumentType.getString(context, "areaName")))))
                .then(literal("level")
                    .then(argument("levelValue", IntegerArgumentType.integer(1, 3))
                        .executes(context -> executeDivideAreaLevel(context,
                            IntegerArgumentType.getInteger(context, "levelValue")))))
                .then(literal("base")
                    .then(argument("baseName", StringArgumentType.greedyString())
                        .executes(context -> executeDivideAreaBase(context,
                            StringArgumentType.getString(context, "baseName")))))
                .then(literal("color")
                    .then(argument("colorValue", StringArgumentType.greedyString())
                        .executes(context -> executeDivideAreaColor(context,
                            StringArgumentType.getString(context, "colorValue")))))
                .then(literal("cancel")
                    .executes(ServerCommands::executeDivideAreaCancel)))

            // recolor 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("recolor")
                .executes(RecolorCommand::executeRecolor)
                // /areahint recolor select <闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼?
                .then(literal("select")
                    .then(argument("selectAreaName", StringArgumentType.greedyString())
                        .executes(context -> executeRecolorSelect(context,
                            StringArgumentType.getString(context, "selectAreaName")))))
                // /areahint recolor color <婵犵數濮烽。钘壩ｉ崨鏉戠；闁糕剝蓱濞呯姵淇婇妶鍛殭闁搞劍绻堥悡顐﹀炊閵婏腹鎷婚梺?
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
            
            // rename 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁绘挷绶氬娲传閵夈儰绮跺銈忓瘜閸ㄨ泛锕㈡担绯曟斀闁绘顕滃銉╂倵濮樼厧澧扮紒顕呭弮楠炴帡寮崒婊愮床闂備胶鎳撻崥鈧悹浣圭叀瀹曟垿骞橀懜鍨劚婵炶揪绲块悺鏂款焽閹扮増鐓欑€瑰嫮澧楅崳浠嬫煙椤旇宓嗛柛鈺佸瀹曟﹢顢旈崟顐ゎ啋闂?
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
                            
            // sethigh 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
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

            // addhint 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐寸厽闁靛繈鍩勯悞鍓х磼閳ь剟宕熼娑氬弳闂佺粯鏌ㄩ幖顐㈢摥闂佽瀛╅懝楣兯囬鐐┾偓锔炬崉閵婏箑纾梺缁樼濞兼瑦鎱ㄥ☉銏♀拺闁告稑顭悞浠嬫煛娴ｅ壊鐓肩€殿喛顕ч埥澶婎潨閸℃ê鍏婃俊鐐€栫敮鎺楀磹閹扮増鏅插璺侯儑閸橀亶鎮峰鍐ч柡浣稿暣椤㈡棃宕熼鐔割啎婵犵數濞€濞佳囶敄閸℃稑纾婚柟閭﹀幗閸欏繘鏌ｉ悢鍝勵暭缂佲偓瀹€鍕厱閻庯綆鍋呯亸鎵磼缂佹绠炵€规洖鐖兼俊鎼佸Ψ閵忕姳澹?
            .then(literal("addhint")
                .executes(ServerCommands::executeAddHintStart)
                .then(literal("select")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> executeAddHintSelect(context,
                            StringArgumentType.getString(context, "areaName")))))
                .then(literal("continue")
                    .executes(ServerCommands::executeAddHintContinue))
                .then(literal("submit")
                    .executes(ServerCommands::executeAddHintSubmit))
                .then(literal("cancel")
                    .executes(ServerCommands::executeAddHintCancel)))

            // deletehint 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?(婵犵數濮烽弫鎼佸磻濞戙埄鏁嬫い鎾跺枑閸欏繘鎮楀☉娆欎緵婵炲牅绮欓弻鐔兼⒒鐎靛壊妲紓浣插亾闁告洦鍨遍悡銉╂煟閺傛寧鎯堢€涙繈鏌ｆ惔銏ｅ妞わ富鍨堕垾锔炬崉閵婏箑纾梺缁樼濞兼瑦鎱ㄥ☉銏♀拺闁告稑顭悞浠嬫煛娴ｅ壊鐓肩€殿喛顕ч埥澶婎潨閸℃ê鍏婃俊鐐€栫敮鎺楀磹閹版澘顫呴柕鍫濇閸橀亶姊洪棃娑辩劸闁稿骸纾划鏃堟倻濡寮挎繝鐢靛С閼冲爼鎯屽▎蹇婃斀闂勫洭宕洪弽褜鍤楅柛鏇ㄥ灠闁卞洦鎱ㄥ鍡楀幐濠㈣娲樼换婵嬫偨闂堟刀銏＄箾鐠囇呯暤鐎?
            .then(literal("deletehint")
                .executes(ServerCommands::executeDeleteHintStart)
                .then(literal("select")
                    .then(argument("areaName", StringArgumentType.greedyString())
                        .executes(context -> executeDeleteHintSelect(context,
                            StringArgumentType.getString(context, "areaName")))))
                .then(literal("toggle")
                    .then(argument("index", IntegerArgumentType.integer(0))
                        .executes(context -> executeDeleteHintToggle(context,
                            IntegerArgumentType.getInteger(context, "index")))))
                .then(literal("submit")
                    .executes(ServerCommands::executeDeleteHintSubmit))
                .then(literal("cancel")
                    .executes(ServerCommands::executeDeleteHintCancel)))

            // replacebutton 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("replacebutton")
                .executes(ServerCommands::executeReplaceButtonStart)
                .then(literal("confirm")
                    .executes(ServerCommands::executeReplaceButtonConfirm))
                .then(literal("cancel")
                    .executes(ServerCommands::executeReplaceButtonCancel)))

            // language 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁绘挷绶氬娲传閵夈儰绮跺銈忓瘜閸ㄨ泛锕㈡担绯曟斀闁绘顕滃銉╂倵濮樼厧澧扮紒顕呭弮楠炴帡寮崒婊愮床闂備浇顫夎ぐ鍐敄閸モ晛顥氶柛褎顨嗛悡娆忣渻鐎ｎ亪顎楅柍璇茬墛閵囧嫰濮€閳藉懓鈧潡鏌涢埡瀣暤闁糕斁鍋撳銈嗗笒鐎氼剟宕欓悩缁樼厱婵炴垶顭囬幗鐘绘煃闁垮鐏﹂柕鍥у楠炲洭鍩℃担鍝勫Ф婵犵鍓濊ぐ鍐礊婵犲洤绠栨俊銈傚亾妞ゎ偅绻堟俊鐑藉Ψ閵夘喗缍嗛梻?
            .then(literal("language")
                .executes(ServerCommands::executeLanguageStart)
                .then(literal("select")
                    .then(argument("langCode", StringArgumentType.word())
                        .executes(context -> executeLanguageSelect(context, StringArgumentType.getString(context, "langCode")))))
                .then(literal("cancel")
                    .executes(ServerCommands::executeLanguageCancel)))

            // boundviz 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
            .then(literal("boundviz")
                .executes(ServerCommands::executeBoundViz))

            // serverlanguage 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌曢崼婵囧櫤闁诲骸澧界槐鎾存媴閸濆嫅锝夋煟閳哄﹤鐏︾€殿喛顕ч埥澶愬閻樼數娼夐梻浣筋潐閸庤櫕鏅舵惔銊︽櫖闁绘柨顨庡〒濠氭煏閸繃鍣虹紒鍌氼儔閺屾盯骞樼€靛憡鍒涘Δ鐘靛仜缁绘﹢骞冮埡浼辫櫣绱掑Ο缁樼彎闂傚倷鐒︽繛濠囧极椤曗偓瀹曟垿骞樼紒妯煎幐闁诲函缍嗘禍婵嬪吹閸ヮ剚鐓涢悘鐐额嚙婵″ジ鏌嶉挊澶樻Ц閾伙絿绱撴担鑲℃垹鈧俺娅曠换婵嬫偨闂堟稐鎴烽梺鍛婎焼閸ャ劌浜遍梺鍦亾閸撴艾顭囬弽顬″綊鎮╁顔煎壈缂備胶濮电粙鎺楀Φ閸曨垰绫嶉柛灞剧煯婢规洟姊虹拠鈥崇伈缂佽尪娉曞Σ?闂?
            .then(literal("serverlanguage")
                .requires(source -> source.hasPermissionLevel(4))
                .then(argument("langCode", StringArgumentType.word())
                    .executes(context -> executeServerLanguage(context, StringArgumentType.getString(context, "langCode")))))
        );
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€寰峫p闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.title.general"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.help"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.reload"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.delete"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.frequency"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.subtitlerender"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.subtitlestyle"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.subtitlesize"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.add"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.easyadd"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.recolor"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.rename"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.sethigh"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.replacebutton"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.check"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.dimensionalityname"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.dimensionalitycolor"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.expandarea"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.shrinkarea"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.dividearea"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.addhint"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.deletehint"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.boundviz"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.language"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.firstdimname"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.firstdimname_skip"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.debug"));
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("help.command.serverlanguage"));

        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔load闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeReload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 闂傚倸鍊搁崐鎼佸磹閻戣姤鍊块柨鏇氶檷娴滃綊鏌涢幇鍏哥敖闁活厽鎹囬弻锝夊閵忊晝鍔搁梺钘夊暟閸犲酣鍩為幋锔藉亹闁告瑥顦伴幃娆戠磽娴ｆ彃浜炬繝銏ｅ煐閸旀牠鎮￠悢闀愮箚妞ゆ牗绮岀敮鍫曟煕閺傛鍎戠紒杈ㄥ笚閹峰懎鐣￠弶璺ㄣ偖闂備礁鎼張顒勬儎椤栨凹鍤曢柛濠勫櫏濡插ジ姊虹拠鈥崇仩闁绘鎹囧濠氭晲婢跺﹦顔掗柣搴ㄦ涧閹诧繝宕虫导瀛樺€垫繛鍫濈仢濞呮﹢鏌涚€ｎ亷韬鐐插暢椤﹀湱鈧娲滈崢褔鍩為幋锕€閱囨繛鎴灻奸崰濠冪節瀵伴攱婢橀埀顒侇殕閹便劑鎮滈挊澶岋紱闂佺粯蓱缁佹挳寮搁弽銊х闁瑰鍋涘▓鐘裁瑰鍐Ш闁哄本绋戦悾婵嬪焵椤掑嫬纾婚柣鎰劋閸婂潡鎮归崶顏嶆⒖鐟滅増甯楅弲鏌ユ煕椤愩倕娅忓ù鐘櫊濮婃椽鎳栭埡濠勫姼闂佺粯鐗曢妶绋款嚕婵犳碍鍋勯柛婵嗗閻庡啿鈹戦埥鍡楃仴婵℃ぜ鍔嶇粩鐔煎即閵忊檧鎷绘繛杈剧到閹诧繝骞嗛崼銉︾厽婵°倐鍋撴俊顐ｇ箚濡喎顪冮妶鍡欏⒈闁稿孩鍔欏顐﹀炊閳哄倸鏋戦梺缁橆殔閻楀棛绮幒鏃傜＜闁绘瑥鎳愰崚鐗堛亜椤忓嫬鏆熼柍褜鍓ㄧ紞鍡樼濠婂牊鍎婇柕濞垮剭?
        ServerNetworking.sendAllAreaDataToAll();
        
        // 闂傚倸鍊搁崐鎼佸磹閻戣姤鍊块柨鏇氶檷娴滃綊鏌涢幇鍏哥敖闁活厽鎹囬弻锝夊閵忊晝鍔搁梺钘夊暟閸犲酣鍩為幋锔藉亹闁告瑥顦伴幃娆撴⒑濞茶骞楁い銊ワ躬瀵鎮㈤崨濠勭Ф闂佺顫夐崝鏇㈠箖閸涘瓨鈷戠紒瀣儥閸庢劙鏌熺粙娆剧吋妤犵偛绻樺畷銊р偓娑櫭埀顒傜帛娣囧﹪顢涘鍐ㄥЕ闂佸搫顑嗛悷鈺侇潖閾忚鍏滈柛娑卞弾濡牓姊虹憴鍕仧濞存粎鍋熼崚鎺撶節濮橆剛顓洪梺鎸庢濡嫭绂嶅Δ鈧埞鎴︽倷閸欏妫￠梺鍦焾閹芥粓骞夐幘顔芥櫆闂佹鍨版禍鐐殽閻愯尙浠㈤柛鏃€纰嶉妵鍕晜鐠囪尙浠梺杞扮贰閸犳牠鎮鹃悜钘夌倞闁冲搫鍠涚槐鎶芥⒒娴ｅ憡鍟為柛鏃€鐗犲畷鏇㈠箮閽樺鍤戦梺闈涚墕濞层劎澹曢崗绗轰簻闁哄啫娴傞崵娆戔偓瑙勬尭濡瑩銆冮妷鈺傚€烽柤纰卞劮瑜旈弻娑㈠煘閹傚濠碉紕鍋戦崐鏍暜閹烘柡鍋撳鐓庡闁逞屽墯閼归箖藝椤栫偐鈧妇鎹勯妸锕€纾繛鎾村嚬閸ㄦ娊宕ｉ弴銏♀拺闁告稑锕ㄦ竟姗€鏌涙繝鍌涘仴鐎殿喖顭烽幃銏ゆ偂鎼达綆鍟嬫俊鐐€栧ú鏍箠韫囨蛋澶愬冀瑜滃〒?
        try {
            MinecraftServer server = source.getServer();
            areahint.network.DimensionalNameNetworking.sendDimensionalNamesToAllClients(server);
            Areashint.LOGGER.info(ServerI18nManager.translate("command.message.area.dimension_3"));
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.area.dimension_4"), e);
        }
        
        // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炲季婵炲皷鏅犻弻鏇熺箾閻愵剚鐝曢梺绋款儏閸婂潡寮婚妸銉㈡婵☆垯璀︽禒閬嶆⒑缁嬫鍎愰柟鐟版喘閹即顢氶埀顒€鐣疯ぐ鎺濇晩闁告瑣鍎冲Λ顖炴⒒閸屾瑨鍏岀痪顓炵埣瀵彃顭ㄩ崨顖滅厯闂佽宕樺▔娑㈠垂濠靛鐓冮柛婵嗗婵ジ鏌℃担鍝バч柡宀嬬秮楠炲洭顢楅埀顒傜棯瑜庨妵鍕疀閺囩偞鐏堥梺鍝勫閸撴繂顕ラ崟顓濇勃闁瑰瓨甯為柦鐢电磽閸屾瑧璐伴柛鐘愁殜閹兘鍩℃担鐑樻闂侀潧绻堥崐鏇炴纯闂備焦鎮堕崕顕€寮插┑瀣櫖?
        sendClientCommand(source, "areahint:reload");
        
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.dimension"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏂跨暢d闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param json JSON闂傚倸鍊峰ù鍥敋瑜忛埀顒佺▓閺呮繄鍒掑▎鎾崇婵＄偛鐨烽崑鎾诲礃椤斿ジ鍞堕梺闈涱樈閸犳寮查悙瀵哥閺夊牆澧介崚鐗堢節閳ь剟鏌嗗鍛幒?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeAdd(CommandContext<ServerCommandSource> context, String json) {
        ServerCommandSource source = context.getSource();
        
        // 闂傚倸鍊峰ù鍥х暦閻㈢绐楅柟鎵閸嬶繝寮堕崼姘珔缂佽翰鍊曡灃闁挎繂鎳庨弳鐐烘煕婵犲嫭鏆╃紒杈ㄥ浮閻擃偊顢橀埥鍡楁倛ON闂傚倸鍊搁崐宄懊归崶褜娴栭柕濞炬櫆閸ゅ嫰鏌ょ粙璺ㄤ粵婵炲懐濮垫穱濠囧Χ閸屾矮澹曢梻?
        AreaData areaData = JsonHelper.fromJsonSingle(json);
        
        if (areaData == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_6"));
            return 0;
        }
        
        // 婵犵數濮撮惀澶愬级鎼存挸浜炬俊銈勭劍閸欏繘鏌熺紒銏犳灍闁稿孩顨呴妴鎺戭潩閿濆懍澹曢梻浣筋嚃閸垶鎮為敃鈧銉╁礋椤撴稑浜鹃柨婵嗘噽娴犳盯鏌ｉ敐鍫殭闁宠鍨块崺銉╁幢濡ゅ啩鍝楅梻浣告贡閳峰牓宕戞繝鍥モ偓浣糕枎閹邦喚鐦堥梺鎼炲劘閸斿酣宕㈤崨濠勭閺夊牆澧介崚浼存煙绾板崬浜伴柕鍡楀€块幊鏍煘閹傚闁荤喐鐟ョ€氼厾浜搁銈囩＜闁兼悂娼ч崫铏光偓娈垮櫘閸嬪嫰顢橀崗鐓庣窞濠电姴娲ら弫瑙勭節閻㈤潧孝闁诲繑宀稿畷婵嬪冀椤撶偟顔愰梺瑙勫婢ф鍩涢幋锔界厱婵犻潧妫楅鎾煕鎼淬垹濮夌紒杈ㄥ浮椤㈡瑩鎳栭埡鍐ㄥП闂備浇顕栭崰妤€顪冮懞銉ょ箚闁归棿绀侀悡娑樏归敐鍥ㄥ殌缂佷浇鍩栨穱濠囨倷椤忓嫧鍋撻妶澶婂偍闁伙絽鏈弳婊堟煟閹邦喗鏆╅柣?
        if (areaData.getAltitude() != null) {
            if (!areaData.getAltitude().isValid()) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude_8"));
                return 0;
            }
            
            // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍤╅弴銏♀拻濞达綀娅ｇ敮娑氱磼鐠囪尙效鐎规洘婢樿灃闁告侗鍘鹃悾楣冩⒑閸濆嫬鏆欓柣妤€锕鎻掆攽鐎ｎ偆鍘撻悷婊勭矒瀹曟粓鎮㈤悡搴㈡К闂侀€炲苯澧柕鍥у楠炴帡骞嬪┑鍥唲濠电偛鐡ㄧ划蹇涳綖婢跺本宕叉繛鎴欏灩闁卞洭鏌ｉ弮鍫濞寸厧鐭傞幃妤冩喆閸曨剛锛橀梺绋块叄濞佳囨偩闁垮闄勭紒瀣儥濞煎﹪姊洪悙钘夊姕闁哄銈稿畷鎴﹀箻缂佹ê鈧兘鏌ｉ幋鐑嗙劷闁告﹢娼ч—鍐Χ閸℃鍔搁梺鍛婃尰閻熝呭垝閺冨牆绀堝ù锝堟閻掑ジ姊洪崨濠傜仼閻忓繐绐塭craft婵犵數濮烽弫鎼佸磻閻愬搫鍨傞柛顐ｆ礀缁犳澘鈹戦悩宕囶暡闁稿顑嗙换婵囩節閸屾粌顣洪梺姹囧€楅崑鎾诲Φ閸曨垰绠涢柛鎾茶兌鏍℃繝鐢靛仜閻楀﹪銆冮崱妯尖攳濠电姴娴傞弫宥嗘叏濮楀棗骞楅柛搴㈡尭椤啴濡堕崱妯煎弳闂佹寧娲︽禍婊堬綖韫囨拋娲敂瀹ュ棙娅囬梻浣瑰缁诲倿鎮ч崱娴板鈹戠€ｎ偀鎷虹紓鍌欑劍椤洦鏅堕鍫熺厽闁哄稁鍋勭敮鍫曟煟?64闂?20婵犵數濮烽弫鎼佸磻閻愬搫鍨傞柛顐ｆ礀閽冪喖鏌曟繛鐐珦闁轰礁瀚…璺ㄦ崉婵傝В鈧枼妲堥柕蹇曞█閺佹粌鈹戞幊閸婃捇鎮為敃鈧埢?
            Double minAlt = areaData.getAltitude().getMin();
            Double maxAlt = areaData.getAltitude().getMax();
            
            if (minAlt != null && (minAlt < -64 || minAlt > 320)) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude_2", minAlt));
                return 0;
            }
            
            if (maxAlt != null && (maxAlt < -64 || maxAlt > 320)) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude", maxAlt));
                return 0;
            }
        }
        
        if (!areaData.isValid()) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.level"));
            return 0;
        }
        
        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢妶鍥╃厠闂佺粯鍨堕弸鑽ょ礊閺嵮岀唵閻犺櫣灏ㄩ崝鐔兼煛閸℃劕鈧洟婀侀梺鎸庣箓閻楀﹪顢旈悩鐢垫／闁诡垎浣镐划闂佸搫澶囬崜婵嗩嚗閸曨厸鍋撻敐搴濈敖濞寸姵娼欓—鍐Χ閸℃鍙嗛梺鎸庢处娴滎亜顕ｇ拠娴嬫闁靛繒濮烽悿鈧俊鐐€栧濠氬磻閹剧粯鐓涢柛鈩冪◥閹查箖鏌″畝瀣М闁轰焦鍔栧鍕暆閳ь剟寮抽锔藉€甸悷娆忓缁€鍐煥濮樿埖鐓冮悹鍥у级閸炲绱掗悩宕囨创妤犵偞锕㈤幖褰掝敃閵壯冨絺闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顒€搴婇悗骞垮劚濡盯銆呴崣澶岀闁糕剝蓱鐏忎即鏌ｉ幘鍐测偓鍦崲濠靛洨绡€闁稿本绮岄。鍝勨攽閳藉棗浜濋柨鏇樺灲瀵鈽夐姀鐘栥劍銇勯弽顐沪妞ゅ骸绉撮—?
        String dimensionId = getDimensionFromSource(source);
        if (dimensionId == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.dimension"));
            return 0;
        }
        
        String fileName = Packets.getFileNameForDimension(dimensionId);
        if (fileName == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.dimension_2").append(TextCompat.literal(dimensionId)));
            return 0;
        }
        
        Path filePath = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
        
        // 闂傚倸鍊搁崐鐑芥嚄閸洖鍌ㄧ憸鏃堝Υ閸愨晜鍎熼柕蹇嬪焺濞茬鈹戦悩璇у伐閻庢凹鍙冨畷锝堢疀濞戞瑧鍘撻梺鍛婄箓鐎氼剟寮抽悙娴嬫斀妞ゆ牗鍝庨崑銏ゆ煛鐏炲墽娲存鐐疵灃闁逞屽墴楠炲﹨绠涘☉娆忊偓鍨亜閹惧崬鐏柍閿嬪灴閺屾稑鈽夊鍫濅紣缂備焦顨嗗銊ф閹烘柡鍋撻敐搴′簻闁诲繆鏅濈槐鎺撴綇閵婏箑纾冲Δ鐘靛仦椤洨妲愰幒鎳崇喖鎮滃Ο鍏兼闂傚倸鍊风粈渚€骞夐埄鍐懝婵°倕鎳庨崹鍌氣攽閸屾粠鐒剧紒鈧径瀣ㄤ簻闁哄洦顨呮禍楣冩⒑閸濆嫭婀扮紒瀣灴閿濈偛鈹戠€ｅ灚鏅㈤梺绋挎湰椤ㄥ棝鎮楅崫銉х＝闁稿本鐟︾粊鏉款渻鐎涙ɑ鍊愰柟顔惧厴閺佸倿鎮剧仦鍛婃暤濠电姷鏁告慨鏉懨洪妶鍥ь棜闁稿繗鍋愮粻楣冩煙鐎电浠ч柣鎿冨灦閺屾盯骞嬮敐鍡╂闂佸疇顫夐崹鍧楀箖濞嗘挸绠甸柟鐑樼箖鏍￠梻鍌欑閹诧繝骞愰懡銈嗗床闁稿瞼鍋為弲婵嬫煏韫囧﹤澧插┑顖涙尦閺岋綁骞嬮悘娲讳邯椤㈡挸鈽夐姀鈾€鎷洪梺鍛婄☉閿曘儳鈧灚鐟╅弻娑樷槈閸楃偞鐏撻梺閫炲苯澧婚柛娆忓暙椤繘宕崟鎳峰洤鐐婄憸宥夆€栭崼銉︾厽閹兼番鍨归崵顒勬煕閵娿儳鍩ｆ鐐叉閹峰懘宕烽褎绁梺璇插嚱缂嶅棝宕戦崨顓涙瀺?
        if (areaData.getSignature() == null) {
            areaData.setSignature(source.getName());
        }
        
        // 濠电姷鏁告慨鐑藉极閹间礁纾块柟瀵稿Х缁€濠囨煃瑜滈崜姘跺Φ閸曨垰鍗抽柛鈩冾殔椤忣亪鏌涘▎蹇曠闁哄矉缍侀獮鍥敆娴ｇ懓鍓电紓鍌欒閸嬫挾鈧厜鍋撻柛鏇ㄥ墰閸樺崬鈹戦悩缁樻锭婵☆偅顨婇、鏃堫敃閿旂晫鍘卞┑鈽嗗灠閸氬寮抽鍕厸閻忕偟顭堟晶鏌ユ煙瀹勭増鍤囩€规洦鍋婂畷鐔煎垂椤愶絿妲橀梻?
        if (FileManager.addAreaData(filePath, areaData)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.success.area.add").append(TextCompat.literal(areaData.getName())));
            
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炲季婵炲皷鏅犻弻鏇熺箾閻愵剚鐝曢梺绋款儏閸婂潡寮婚妸鈺傚亜闁告繂瀚呴姀銏㈢＜闁绘﹩鍠栭崝锕傛煛鐏炵晫啸妞ぱ傜窔閺屾盯骞樼捄鐑樼€诲銈嗘穿缂嶄礁鐣疯ぐ鎺濇晝闁靛繈鍨婚悰顔尖攽閻樺灚鏆╁┑顔肩摠椤ㄣ儵骞栨担鍝ョ暫闂佸憡绋戦悺銊╁煕閹达附鍋℃繛鍡楃箰椤忣亪鎮樿箛銉╂闁靛洤瀚版俊鐑芥晜閽樺锛撴俊鐐€戦崹鍝勎涢崘顔衡偓浣肝旈崨顓狀槹濡炪倖鍔戦崹鍦矈椤曗偓濮婅櫣鎷犻幓鎺濆妷缂備礁顑嗙敮鐔煎箺椤愶附鈷戠紓浣股戠亸銊╂煕?
            ServerNetworking.sendAreaDataToAll(dimensionId);
            
            return Command.SINGLE_SUCCESS;
        } else {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.add"));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鎼佸磻濞戙埄鏁嬫い鎾跺枑閸欏繘鎮楀☉娆欎緵婵炲牅绮欓弻鐔兼⒒鐎靛壊妲紓浣哄Х婵炩偓闁哄苯绉归崺鈩冩媴閸涘﹥顔嶆俊鐐€愰弲婵嬪礂濮椻偓瀵寮撮姀鐘诲敹濠电娀娼уù鍌毼涢妶澶嬧拺缂備焦顭囨晶闈浢瑰鍐煟鐎殿喖顭锋俊鑸靛緞婵犲嫮鏆伴柣鐔哥矊缁夋潙宓勯梺鐓庮潟閸婃牠宕伴幇鐗堢厸濠㈣泛顑呴婊呯磼鐏炶姤鍋ラ柡灞炬礋瀹曞爼濡搁妷銉綒婵°倗濮烽崑鐐烘晝閵忕姷鏆︽俊顖欒閸熷懏銇勯弮鍌涙珪闁逞屽墻閸嬪棛妲愰幘璇茬＜婵﹩鍏橀崑鎾绘倻閼恒儱鈧潡鏌ㄩ弴妤€浜惧銈庡幖濞层倝鍩㈡惔銊ョ闁哄倸銇樻竟?
     * @param source 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑鐞涒€充壕?
     * @return 缂傚倸鍊搁崐鎼佸磹閹间礁纾归柣鎴ｅГ閸婂潡鏌ㄩ弴妤€浜惧銈庡幖濞层倝鍩㈡惔銊ョ闁哄倸銇樻竟鏇㈡⒑闂堚晛鐦滈柛姗€绠栧畷銉︾節?
     */
    private static String getDimensionFromSource(ServerCommandSource source) {
        try {
            Identifier dimension = source.getWorld().getRegistryKey().getValue();
            return Packets.convertDimensionPathToType(dimension.getPath());
        } catch (Exception e) {
            return null;
        }
    }
    
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜婵傚憡顎嶉梺闈涙搐鐎氫即鐛Ο铏规殾闁搞儮鏁╅妸銉庢棃鎮╅棃娑楃捕濡炪倖鍨靛Λ婵婃闂佺粯顨呴悧濠囧磿閻旀悶浜滈柡鍐ㄦ搐閸氬湱鐥崣銉х煓婵﹦绮幏鍛村川婵犲倹娈樼紓鍌欑椤戝棛鏁垾宕囨殾闁荤喐澹嬮弨浠嬫倵閿濆簼绨芥い?
     * @param source 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑鐞涒€充壕?
     * @param command 闂傚倸鍊峰ù鍥敋瑜庨〃銉╁传閵壯傜瑝閻庡箍鍎遍ˇ顖炲垂閸屾稓绠剧€瑰壊鍠曠花濠氭煛閸曗晛鍔滅紒缁樼洴楠炲鎮欑€靛憡顓婚梻浣告啞椤ㄥ棛鍠婂澶娢﹂柛鏇ㄥ灠閸愨偓闂侀潧臎鐏炵偓顔愰梻鍌欑劍閹爼宕愰弽顐ｆ殰闁圭儤顨嗙粻?
     */
    private static void sendClientCommand(ServerCommandSource source, String command) {
        try {
            // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐寸厾婵炴潙顑嗗▍鍡欌偓瑙勬礉鐏忔瑩鍩€椤掆偓缁犲秹宕曢崡鐏绘椽鏁冮崒姘憋紱濠碘槅鍨伴惃鐑藉磻閹捐绀傚璺猴梗婢规洟姊绘担鍛婂暈闁告梹鐗犲畷鏇㈠礃濞村鐏佸┑鐐叉閹稿鍩涢幋锔界厱婵炴垶锕崝鐔兼煃椤栨稒绀嬮柡灞剧洴婵℃悂鏁傞崜褏鏉芥俊銈囧Х閸嬫盯鏌婇敐鍡曠箚闁兼悂娼х欢鐐烘倵閿濆簼绨诲鐟板濮婄粯鎷呴崨濠傛殘濠电偠顕滅粻鎾崇暦閵忋倖鍤冮柍鍝勫€搁鎼佹⒑缂佹ɑ鐓ラ柛姘儔瀹曟劙鎮介悽鍨紡濡炪倖鎸鹃崑鐐哄闯閾忓湱妫い鎾跺仜閳锋棃鏌熼崣澶嬪€愮€殿喖鐖煎畷褰掝敊閻熼澹曢梺绉嗗嫷娈旈柦鍐枛閺屾稑鈽夐崡鐐寸亶婵犳鍨遍幐鎶藉蓟濞戙垹绠婚柡澶嬪灩缁佺兘姊虹拠鈥虫灈闁硅櫕锚椤繘鎼圭憴鍕瀭闂佸憡娲﹂崜娆撳船婢跺娓婚柕鍫濆暙閸旀粓鏌熼悷鐗堝枠闁靛棔绶氬顕€宕奸锝嗘珜闂備礁鎲″ú锕傚磻閸曨厾鐜绘繛鎴欏灪閳锋垿鏌ｉ悢绋款棆闁圭晫濞€閹粙顢涘☉杈ㄧ暦闂?
            if (CommandSourceCompat.isExecutedByPlayer(source)) {
                ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(), command);
            }
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to send client command", e);
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€鍞礶quency闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌曢崼婵囧櫤闁诲酣绠栧鍝勭暦閸モ晛绗″┑顔硷龚瀹曢潧危閹版澘绠虫俊銈傚亾闁绘帒鐏氶妵鍕箳閹存繍浠兼繛纾嬪亹婵炩偓闁哄本鐩鎾Ω閵夈儳顔掑┑顔界箓濞差厼顫忕紒妯诲闁告稑锕ら弳鍫ユ⒑閸︻収鐒鹃柟鑺ョ矌閸掓帡寮崶銉ゆ睏闂佺懓鎼鍌炲磻閹捐纾奸柣鎰叀閸炲爼姊洪崫鍕窛闁哥姴瀛╃粋鎺楀閵堝棌鎷洪梺纭呭亹閸嬫盯宕濋敂濮愪簻闁靛闄勭亸鐢电磼?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeFrequencyInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐寸厽闁靛繒濮撮ˉ蹇涙煛娴ｅ憡宸濋柟鍙夋倐閹囧醇濠靛牏鎳嗙紓鍌欒兌婵寰婃禒瀣р偓鏃堝礃椤忎礁浜鹃柨婵嗙凹缁ㄥ鏌ｉ敂鍝勫闂囧鏌ｉ幘鎶筋€楅柍褜鍓欏鈥愁嚕鐠囨祴妲堟俊顖炴敱閻庡姊鸿ぐ鎺戜喊闁告挻绋掔€电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ帒瀚畵浣糕攽閻樺弶鎼愰柣鎺撴そ閺屾盯骞囬崗鍛婂枤濠?
        sendClientCommand(source, "areahint:frequency_info");
        
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.general_15"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€鍞礶quency闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啯銇勯幒鎴濃偓缁樼墡闂傚倷绀侀幗婊堝窗鎼粹埗瑙勵槹鎼淬埄娼熼梺瑙勫礃椤曆呯矆閸愵喗鐓欐い鏍ф鐎氼喛銇愭惔顫箚闁绘劦浜滈埀顑懎绶ゅù鐘差儐閸嬪鈹戦崒婊庣劸缂佺姵鐗犻弻鐔煎箚閻楀牜妫勯梺缁樺姇閿曘倝鈥旈崘顏佸亾閿濆簼绨奸柟鐧哥稻閵?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param value 婵犵數濮烽。钘壩ｉ崨鏉戠；闁糕剝蓱濞呯姵淇婇妶鍛櫣闁搞劌鍊块弻锝夋偄閻撳簼鍠婇梺鍝ュ枎閹冲酣鍩為幋锕€纾兼繝濠傛捣閸斿摜绱?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeFrequency(CommandContext<ServerCommandSource> context, int value) {
        ServerCommandSource source = context.getSource();
        
        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐寸厽闁靛繒濮撮ˉ蹇涙煛娴ｅ憡宸濋柟鍙夋倐閹囧醇濠靛牏鎳嗙紓鍌欒兌婵寰婃禒瀣р偓鏃堝礃椤忎礁浜鹃柨婵嗙凹缁ㄥ鏌ｉ敂鍝勫闂囧鏌ｉ幘鎶筋€楅柍褜鍓欏鈥愁嚕鐠囨祴妲堟俊顖炴敱閻庡姊鸿ぐ鎺戜喊闁告挻绋掔€电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ帒瀚畵浣糕攽閻樺弶鎼愰柣鎺撴そ閺屾盯骞囬崗鍛婂枤濠?
        sendClientCommand(source, "areahint:frequency " + value);
        
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.general_16").append(TextCompat.literal(value + "闂傚倸鍊风粈浣虹礊婵犲伣娑氭崉閵婏箑搴?Hz")));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlerender闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌曢崼婵囧櫤闁诲酣绠栧鍝勭暦閸モ晛绗″┑顔硷龚瀹曢潧危閹版澘绠虫俊銈傚亾闁绘帒鐏氶妵鍕箳閹存繍浠兼繛纾嬪亹婵炩偓闁哄本鐩鎾Ω閵夈儳顔掑┑顔界箓濞差厼顫忕紒妯诲闁告稑锕ら弳鍫ユ⒑閸︻収鐒鹃柟鑺ョ矌閸掓帡寮崼鐔蜂缓闂佸壊鐓堥崑鍕汲椤愶附鈷戠紒顖涙礀婢ц尙绱掔€ｎ偄鐏撮柟閿嬪灴閹垽宕楅懖鈺佸箥闂備浇顕栭崹搴ㄥ川椤旇棄鏋涢梻鍌欒兌缁垶骞愭繝姘仭鐟滄棃宕洪埀顒併亜閹哄秶璐伴柛鐔风箻閺屾盯鎮╁畷鍥ь潷濡炪們鍔婇崕瀵哥不濞戙垹鍗抽柣鏇氱劍缂?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleRenderInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐寸厽闁靛繒濮撮ˉ蹇涙煛娴ｅ憡宸濋柟鍙夋倐閹囧醇濠靛牏鎳嗙紓鍌欒兌婵寰婃禒瀣р偓鏃堝礃椤忎礁浜鹃柨婵嗙凹缁ㄥ鏌ｉ敂鍝勫闂囧鏌ｉ幘鎶筋€楅柍褜鍓欏鈥愁嚕鐠囨祴妲堟俊顖炴敱閻庡姊鸿ぐ鎺戜喊闁告挻绋掔€电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ帒瀚畵浣糕攽閻樺弶鎼愰柣鎺撴そ閺屾盯骞囬崗鍛婂枤濠?
        sendClientCommand(source, "areahint:subtitlerender_info");
        
        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.general_13"));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlerender闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啯銇勯幒鎴濃偓缁樼墡闂傚倷绀侀幗婊堝窗鎼粹埗瑙勵槹鎼淬埄娼熼梺瑙勫礃椤曆呯矆閸愵喗鐓欐い鏍ф鐎氼喗绂嶉崼鏇熲拺闁煎鍊曢弸鎴炵箾閸欏澧摶鐐寸節闂堟稒锛嶉柛銈嗘礀閳规垿鎮╃€圭姴顥濈紓浣哄Т椤兘骞冨Δ鈧埥澶娾枎濡厧濮洪梻浣告啞椤洭寮繝姘畺婵°倐鍋撻柍缁樻崌瀹曞綊顢欓悾灞奸偗婵犲痉鏉库偓妤佹叏鐎靛憡宕查柟鐑樺殾?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param mode 濠电姷鏁告慨鐑藉极閹间礁纾婚柣鎰惈缁犳壆绱掔€ｎ偒鍎ラ柛銈嗘礋閺屾盯顢曢敐鍡欘槰闂佺顑呴崐鍧楀箖濡ゅ懏鏅查幖绮瑰墲閻忓牆鈹戦埥鍡椾簼闁挎洏鍨藉濠氭晲婢跺浜滅紓浣割儐椤戞瑥螞閸℃瑧纾肩紓浣靛灩楠炴劙鏌?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleRender(CommandContext<ServerCommandSource> context, String mode) {
        ServerCommandSource source = context.getSource();
        
        String normalizedMode = ConfigData.normalizeRenderMode(mode);
        
        if (ConfigData.isValidRenderMode(normalizedMode)) {
            // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顐ｇ€抽悗骞垮劚椤︻垶宕归崒鐐寸厽闁靛繒濮撮ˉ蹇涙煛娴ｅ憡宸濋柟鍙夋倐閹囧醇濠靛牏鎳嗙紓鍌欒兌婵寰婃禒瀣р偓鏃堝礃椤忎礁浜鹃柨婵嗙凹缁ㄥ鏌ｉ敂鍝勫闂囧鏌ｉ幘鎶筋€楅柍褜鍓欏鈥愁嚕鐠囨祴妲堟俊顖炴敱閻庡姊鸿ぐ鎺戜喊闁告挻绋掔€电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ帒瀚畵浣糕攽閻樺弶鎼愰柣鎺撴そ閺屾盯骞囬崗鍛婂枤濠?
            sendClientCommand(source, "areahint:subtitlerender " + normalizedMode);
            
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.general_14").append(TextCompat.literal(normalizedMode)));
            
            return Command.SINGLE_SUCCESS;
        } else {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_7").append(TextCompat.literal(mode)).append(TextCompat.translatable("command.error.general")));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlestyle闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絾妞藉铏圭矙閸噮鍔夐梺鑽ゅ暱閺呯姴顕ｇ拠娴嬫闁靛繒濮甸ˉ婵嬫煟閻樺弶澶勭憸鏉垮暣閳ユ牠宕卞缁樻杸濡炪倖姊归弸濠氬礂椤掑嫭鈷掗柛鏇ㄥ亜椤忣偆绱掗纰卞剰妞ゆ挸鍚嬪鍕節閸曢潧鏁介梻鍌欒兌閹虫捇顢氶鐔稿弿闁绘垵顫曢埀顑跨窔瀹曘劎鈧稒菤閹锋椽姊洪崨濠勭畵閻庢凹鍠栭悺顓㈡⒒娴ｈ鍋犻柛銊ㄥ亹娴滅鈻庡顐秮楠炲洭鎮ч崼鐔割仧闂備胶绮敋缁剧虎鍙冨畷婵嬪垂椤愩倗鐦堥梺闈涢獜缁插墽娑甸悙顑句簻闁瑰瓨绻冮崵鍥ㄣ亜閵忊剝顥堝┑锛勫厴閺佸倿宕滆閸熷海绱撻崒姘偓鎼佹偋韫囨梹鍙忓Δ锝呭暙缁€鍌溾偓鍏夊亾闁告洦鍓涢崢鎾绘煟閻樺弶鎼愰柣顏冨嵆瀹曟垿濮€閵堝棛鍘?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleStyleStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁绘挾鍠栭獮鏍ㄦ綇閸撗咃紵闂佷紮缍嗛崣鍐蓟濞戙垹惟闁挎柨顫曟禒銏ゆ倵鐟欏嫭绀€闁圭⒈鍋婇崺銉﹀緞婵犲孩鍍甸梺鍛婎殘閸嬫稓鎲撮敂鎴掔箚闁绘劦浜滈埀顒佺墵閹兾旈崘銊︾€抽悗骞垮劚椤︻垶宕ヨぐ鎺撶厵闁绘垶锚濞?
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜婵傚憡顎嶉梺闈涙搐鐎氫即鐛Ο铏规殾闁搞儮鏁╅妸銉庢棃鎮╅棃娑楃捕濡炪倖鍨靛Λ婵婃闂佺粯顨呴悧濠囧磿閻旀悶浜滈柡鍐ㄦ搐閸氬湱鐥崣銉х煓婵﹦绮幏鍛村川婵犲倹娈樼紓鍌欑椤戝棛鏁垾宕囨殾闁荤喐澹嬮弨浠嬫倵閿濆簼绨芥い?
        try {
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炴灓闁崇懓绉归弻褑绠涘鍏肩秷閻庤娲橀悡锟犲蓟濞戙垹绠涢柛蹇撴憸閺佹牜绱掓ィ鍐暫缂佺姵鐗犲濠氭偄鐞涒€充壕闁汇垺顔栭悞楣冨疮閹间焦鈷戠痪顓炴媼濞兼劖绻涢懠顒€鏋涚€殿喛顕ч埥澶娢熼柨瀣偓濠氭⒑瑜版帒浜伴柛鎾寸⊕鐎电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ巻鍋撻柣锝囧厴瀹曨偊宕熼鐔哥暦闂備線鈧偛鑻晶鎾煕閳规儳浜炬俊鐐€栧濠氬磻閹惧墎纾奸柣妯垮皺鏁堥悗瑙勬礈閹虫挾鍙呭銈呯箰鐎氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣烽垾鎰佹僵妞ゆ挻绮堢花濠氭⒑閸濆嫮袪闁告柨鐭傚畷鐢割敆娴ｉ绠氶梺鍝勮閸庝即骞夋ィ鍐╃厸?
            sendClientCommand(source, "areahint:subtitlestyle_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.start_4").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlestyle select闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ璇茬祷濠殿喖顦靛铏圭矙濞嗘儳鍓遍梺瑙勭摃椤曆囶敋閵夆晛绀嬫い鎾寸☉娴滈箖鏌ㄥ┑鍡欏嚬闁瑰弶鍎抽湁闁绘娅曠亸锔芥叏婵犲啯銇濈€规洦鍋婂畷鐔碱敇婢跺牆鐏查柡灞剧洴婵偓闁挎繂鎳愬В銏犫攽閳ュ啿绾ч柛鏃€鐟╁顐﹀箻缂佹ê浜归梺鎯ф禋閸嬪懘鎮?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param style 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢妶鍥╃厠闂佸湱铏庨崰鏍ㄦ償婵犲洦鐓犵痪鏉垮船婢ь垱銇?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleStyleSelect(CommandContext<ServerCommandSource> context, String style) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:subtitlestyle_select:" + style);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_14").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlestyle cancel闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁哥槐鎾存媴缁嬭法楠囬柣搴ｇ懗閸涱喖搴婂┑鐐村灟閸ㄥ湱绮婚敐鍡欑瘈闂傚牊绋撴晶鏇熴亜閵堝倸浜鹃梻鍌氬€烽懗鍓佸垝椤栫偑鈧啴宕ㄩ鐘虫濡炪倖甯掔€氱兘寮€ｎ喗鐓ユ繝闈涙婢с垽鏌ｉ妶鍌氫壕濠电姷鏁搁崑鐐哄垂閸撲焦鏆滈柟鐑樻尵椤╂煡鏌熼鍡楃灱閸炵敻鏌ｉ悩鐑樸€冮悹鈧敃鍌氱？闊洦鏌ｆ禍婊堢叓閸パ屽剰闁告棑绠撻弻?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleStyleCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:subtitlestyle_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel_5").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlesize闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絾妞藉铏圭矙閸噮鍔夐梺鑽ゅ暱閺呯姴顕ｇ拠娴嬫闁靛繒濮甸ˉ婵嬫煟閻樺弶澶勭憸鏉垮暣閳ユ牠宕卞缁樻杸濡炪倖姊归弸濠氬礂椤掑嫭鈷掗柛鏇ㄥ亜椤忣偆绱掗纰卞剰妞ゆ挸鍚嬪鍕節閸曢潧鏁介梻鍌欒兌閹虫捇顢氶鐔稿弿闁绘垵顫曢埀顑跨窔瀹曘劑顢欓崜褏妲囬梻渚€娼х换鎺撴叏閻戣棄鍨傚┑鐘崇閻撴洟鏌熼弶鍨倎缂併劌銈搁弻锝夋晜閼恒儱纾冲┑顔硷功缁垳绮悢鐓庣倞鐟滃瞼鑺辩拠宸富闁靛牆绻樺顔碱熆閻熺増顥㈤柛鈹惧亾濡炪倖甯婄粈渚€骞栭幖浣圭厱闁哄倽娉曢悞鍛婄節閳ь剚鎷呴崷顓狀啎闁哄鐗嗘晶鐣岀矓椤掑嫭鐓忛柛鈥崇箰娴滈箖姊绘担鍛婃儓閻炴凹鍋婂畷鎰攽鐎ｎ亣鎽?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleSizeStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁绘挾鍠栭獮鏍ㄦ綇閸撗咃紵闂佷紮缍嗛崣鍐蓟濞戙垹惟闁挎柨顫曟禒銏ゆ倵鐟欏嫭绀€闁圭⒈鍋婇崺銉﹀緞婵犲孩鍍甸梺鍛婎殘閸嬫稓鎲撮敂鎴掔箚闁绘劦浜滈埀顒佺墵閹兾旈崘銊︾€抽悗骞垮劚椤︻垶宕ヨぐ鎺撶厵闁绘垶锚濞?
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜婵傚憡顎嶉梺闈涙搐鐎氫即鐛Ο铏规殾闁搞儮鏁╅妸銉庢棃鎮╅棃娑楃捕濡炪倖鍨靛Λ婵婃闂佺粯顨呴悧濠囧磿閻旀悶浜滈柡鍐ㄦ搐閸氬湱鐥崣銉х煓婵﹦绮幏鍛村川婵犲倹娈樼紓鍌欑椤戝棛鏁垾宕囨殾闁荤喐澹嬮弨浠嬫倵閿濆簼绨芥い?
        try {
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炴灓闁崇懓绉归弻褑绠涘鍏肩秷閻庤娲橀悡锟犲蓟濞戙垹绠涢柛蹇撴憸閺佹牜绱掓ィ鍐暫缂佺姵鐗犲濠氭偄鐞涒€充壕闁汇垺顔栭悞楣冨疮閹间焦鈷戠痪顓炴媼濞兼劖绻涢懠顒€鏋涚€殿喛顕ч埥澶娢熼柨瀣偓濠氭⒑瑜版帒浜伴柛鎾寸⊕鐎电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ巻鍋撻柣锝囧厴瀹曨偊宕熼鐔哥暦闂備線鈧偛鑻晶鎾煕閳规儳浜炬俊鐐€栧濠氬磻閹惧墎纾奸柣妯垮皺鏁堥悗瑙勬礈閹虫挾鍙呭銈呯箰鐎氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣烽垾鎰佹僵妞ゆ挻绮堢花濠氭⒑閸濆嫮袪闁告柨鐭傚畷鐢割敆娴ｉ绠氶梺鍝勮閸庝即骞夋ィ鍐╃厸?
            sendClientCommand(source, "areahint:subtitlesize_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.start_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlesize select闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ璇茬祷濠殿喖顦靛铏圭矙濞嗘儳鍓遍梺瑙勭摃椤曆囶敋閵夆晛绀嬫い鎾寸☉娴滈箖鏌ㄥ┑鍡欏嚬闁瑰弶鍎抽湁闁绘灏欓幗鐘电磼缂佹绠炲┑顔瑰亾闂佺粯锕㈠褔鎮橀鈧娲川婵犲倸袝婵炲瓨绮嶇换鍫ュ箚娓氣偓楠炴绱掑Ο閿嬪?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param size 婵犵數濮烽弫鍛婃叏娴兼潙鍨傜憸鐗堝笚閸嬪鏌曡箛瀣偓鏇犵矆閸愨斂浜滈煫鍥ㄦ尰閸ｈ姤淇?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleSizeSelect(CommandContext<ServerCommandSource> context, String size) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:subtitlesize_select:" + size);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_13").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞Бbtitlesize cancel闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁哥槐鎾存媴缁嬭法楠囬柣搴ｇ懗閸涱喖搴婂┑鐐村灟閸ㄥ湱绮婚敐鍡欑瘈闂傚牊绋撴晶鏇熴亜閿濆棙銇濇慨濠冩そ濡啫鈽夊▎鎰€锋繝鐢靛仜閻即宕濆鍥╃焿鐎广儱鎷嬪鎵偓鍏夊亾闁逞屽墴閹潡鍩€椤掆偓閳规垿鎮欓弶鎴犱桓闁汇埄鍨辩敮妤冪矉瀹ュ绠氱憸婊堟偄閸℃稒鍋ｉ弶鐐村椤掔喖鏌涙惔锛勭闁靛洤瀚粻娑氣偓锝庝簻婵箓姊?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeSubtitleSizeCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:subtitlesize_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏂垮潖lete闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絾妞藉铏圭矙閸噮鍔夐梺鑽ゅ暱閺呯姴顕ｇ拠娴嬫闁靛繒濮甸ˉ婵嬫煟閻樺弶澶勭憸鏉垮暣閳ユ牠宕卞缁樻杸濡炪倖姊归弸濠氬礂椤掑嫭鈷掗柛鏇ㄥ亜椤忣偆绱掗纰卞剰妞ゆ挸鍚嬪鍕節閸曢潧鏁介梻鍌欒兌閹虫捇顢氶鐔稿弿闁绘垵顫曢埀顑跨窔瀹曘劎鈧稒菤閹锋椽姊洪崨濠勨槈闁挎洏鍊楃划顓☆槻闁宠鍨块幃娆撳煛娴ｅ嘲顥氭繝鐢靛Х閺佹悂宕戝☉銏犵疇閹兼番鍩勫▓浠嬫煙闂傚鍔嶇紒鐘冲笒閳规垿鎮欓懜闈涙锭缂傚倸绉崑鎾绘⒑闂堟稒顥戦柛瀣崌濮婃椽宕崟顕呮蕉闂佺锕ョ换鍫濐嚕?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeDeleteStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁绘挾鍠栭獮鏍ㄦ綇閸撗咃紵闂佷紮缍嗛崣鍐蓟濞戙垹惟闁挎柨顫曟禒銏ゆ倵鐟欏嫭绀€闁圭⒈鍋婇崺銉﹀緞婵犲孩鍍甸梺鍛婎殘閸嬫稓鎲撮敂鎴掔箚闁绘劦浜滈埀顒佺墵閹兾旈崘銊︾€抽悗骞垮劚椤︻垶宕ヨぐ鎺撶厵闁绘垶锚濞?
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜婵傚憡顎嶉梺闈涙搐鐎氫即鐛Ο铏规殾闁搞儮鏁╅妸銉庢棃鎮╅棃娑楃捕濡炪倖鍨靛Λ婵婃闂佺粯顨呴悧濠囧磿閻旀悶浜滈柡鍐ㄦ搐閸氬湱鐥崣銉х煓婵﹦绮幏鍛村川婵犲倹娈樼紓鍌欑椤戝棛鏁垾宕囨殾闁荤喐澹嬮弨浠嬫倵閿濆簼绨芥い?
        try {
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炴灓闁崇懓绉归弻褑绠涘鍏肩秷閻庤娲橀悡锟犲蓟濞戙垹绠涢柛蹇撴憸閺佹牜绱掓ィ鍐暫缂佺姵鐗犲濠氭偄鐞涒€充壕闁汇垺顔栭悞楣冨疮閹间焦鈷戠痪顓炴媼濞兼劖绻涢懠顒€鏋涚€殿喛顕ч埥澶娢熼柨瀣偓濠氭⒑瑜版帒浜伴柛鎾寸⊕鐎电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ巻鍋撻柣锝囧厴瀹曨偊宕熼鐔哥暦闂備線鈧偛鑻晶鎾煕閳规儳浜炬俊鐐€栧濠氬磻閹惧墎纾奸柣妯垮皺鏁堥悗瑙勬礈閹虫挾鍙呭銈呯箰鐎氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣烽垾鎰佹僵妞ゆ挻绮堢花濠氭⒑閸濆嫮袪闁告柨鐭傚畷鐢割敆娴ｉ绠氶梺鍝勮閸庝即骞夋ィ鍐╃厸?
            sendClientCommand(source, "areahint:delete_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.start").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏂垮潖lete select闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ璇茬祷濠殿喖顦靛铏圭矙濞嗘儳鍓遍梺瑙勭摃椤曆囶敋閵夆晛绀嬫い鎾寸☉娴滈箖鏌ㄥ┑鍡欏嚬闁瑰弶鍎抽湁闁绘娅曠亸锕傛煛瀹€瀣М闁诡喓鍨藉畷銊︾節閸曞墎骞㈢紓鍌氬€风拋鏌ュ磻閹剧偨鈧帒顫濋敐鍛闁诲氦顫夊ú婊堛€佹繝鍥﹂柟鐗堟緲缁犳娊鏌熺紒銏犵仭閻庡灚鐗犲缁樼瑹閳ь剙顭囪閹囧幢濡炴洘妞介弫鍐磼濞戞ü鎮ｉ梻鍌欑贰閸撴瑧绮旈悽鍛婂亗闁哄洢鍨洪悡鍐煕濠靛棗顏╅柡鍡樼懇閺岋紕鈧綆浜崣鍕煛瀹€鈧崰鏍€佸▎鎾村殟闁靛／灞拘ч梻?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param areaName 闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼閻樺磭澧甸柡宀嬬節瀹曞爼濡烽妷褌鎮ｉ梺鑽ゅ枑閻熻鲸淇婇崶顒€鐒垫い鎺嗗亾缂佺姴绉瑰畷鏇㈠础閻愨晜鐏?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeDeleteSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:delete_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏂垮潖lete confirm闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｉ弮鍌ょ劸婵炲懏宀稿娲焻閻愯尪瀚板褜鍨遍妵鍕敇閻愭潙鏋犲Δ鐘靛仜缁绘帡鍩€椤掑﹦绉甸柛鐘崇墪濞插潡姊绘担鍛婂暈濞撴碍顨婂畷銏ゆ倷椤掑偆娴勬繝闈涘€搁幉锟犲煕閹烘鐓曢悘鐐村礃婢规ɑ銇勮箛瀣姦闁哄本绋掔换娑滎槻闁瑰啿绻橀敐?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeDeleteConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:delete_confirm");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.confirm.delete").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏂垮潖lete cancel闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁哥槐鎾存媴缁嬭法楠囬柣搴ｇ懗閸涱喖搴婂┑鐐村灟閸ㄥ湱绮婚敐鍡欑瘈闂傚牊绋撴晶鏇熴亜閿濆棙銇濇慨濠冩そ楠炴牠鎮欓幓鎺濈€崇紓鍌氬€哥粔鎾晝閵堝缍栭煫鍥ㄦ⒒椤╃兘鎮楅敐搴樺亾椤撳﹤娲ㄩ崣鎾绘煕閵夛絽濡界紒鈧崼鐔翠簻?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeDeleteCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            sendClientCommand(source, "areahint:delete_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel.delete").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆鍠栫壕鍧楁煙閹増顥夐幖鏉戯躬閺屻倝鎳濋幍顔肩墯婵炲瓨绮岀紞濠囧蓟濞戙垹唯妞ゆ棁宕甸弳妤佺箾鐎涙鐭婄紓宥咃躬瀵鎮㈤悡搴ｇ暰閻熸粌绉瑰铏綇閵婏絼绨婚梺闈涚墕閹冲繘宕甸崶顒佺厸鐎光偓鐎ｎ剙鍩岄梺宕囨嚀閸熸挳骞冨▎鎿冩晢濞达絽鎼徊钘夆攽閿涘嫬浜奸柛濠冪墵瀵濡歌閻捇鏌ｉ悢绋款棆闁哄棴绠撻弻銊モ攽閸♀晜鈻撻梺杞扮缁夌數鎹㈠☉銏犲耿婵°倓鐒﹀畷鎶芥⒒閸屾凹妲哥紒澶屾嚀椤繐煤椤忓嫬绐涙繝鐢靛Т閸婂顢栭崒娑氱瘈闁汇垽娼ф禍褰掓煕鐎ｎ偅灏柍瑙勫灴閹瑩宕ｆ径濠冾仩闂傚倷绀佹惔婊呯礊娓氣偓閻涱噣宕卞☉妯碱槰濡炪倖妫侀崑鎰八囬弶娆炬富闁靛牆妫楁慨鍌炴煕婵犲啯绀嬮柣娑卞枛椤撳吋寰勭€Ｑ勫闂備礁鎲＄粙鎴︽晝閵婏妇鐝舵俊顖濆亹绾剧厧顭跨捄渚剱閻忓骏濡囬埀顒冾潐濞叉牜绱炴繝鍥モ偓渚€寮崼婵嗚€垮┑鈽嗗灥濡椼劎鍒?
     * @param source 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑鐞涒€充壕?
     * @param dimension 缂傚倸鍊搁崐鎼佸磹閹间礁纾归柣鎴ｅГ閸婂潡鏌ㄩ弴妤€浜惧銈庡幖濞层倝鍩㈡惔銊ョ闁哄倸銇樻竟?
     * @return 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸屾褰掑礂閸忚偐绋囬梺鎼炲妼閸婂骞夐幖浣瑰亱闁割偅绻勯悷鏌ユ⒑閹惰姤鏁辨俊顐㈠暣瀵寮撮姀鐘诲敹濠电娀娼уú銈壦囬埡鍛拺濞村吋鐟х粔顔济瑰鍐煟鐎殿喛顕ч埥澶愬閻橀潧骞愰梻浣告啞閸旀垿宕濆澶嬪€靛┑鐘崇閳锋垿鎮归崶锝傚亾閾忣偆浜舵繝鐢靛仩鐏忔瑩宕版惔銊﹀仼鐎瑰嫰鍋婂鈺傘亜閹存梹娅嗘俊顐㈠暙閻ｇ兘鎮㈤悡搴ｅ幋闂佽鍨庨崒姘兼?
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
                        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧懜褰掓煛鐏炶鍔氱紒鈧崘鈹夸簻闊洦鎸搁鈺冪磼閻橆喖鍔︽慨濠勭帛閹峰懘宕ㄦ繝鍌涙畼闂備浇鍋愰幊鎾存櫠閻ｅ苯鍨濋柡鍐ㄥ€搁ˉ姘舵倵閿濆啫濡烘慨瑙勵殜閹嘲顭ㄩ崟顒傚弳濡炪倖娲╃紞浣哥暦瑜版帩鏁冮柕蹇嬪灮閻涱噣鏌ｆ惔鈥冲辅闁稿鎸搁埞鎴︽偐閹绘巻鍋撹ぐ鎺戠９閹兼番鍔嶉埛鎺懨归敐鍫燁棄闁告艾缍婇弻娑氣偓锝庡亞閳藉鎽堕悙缈犵箚闁靛牆鎳忛崳鍦磼閻橀潧鏋涢柡宀€鍠栭獮鍡涙偋閸偅顥夐梻浣规た閸樹粙銆冮崱娑樜﹂柛鏇ㄥ灠閸愨偓闂侀潧臎閸曨偅鐝＄紓鍌氬€烽梽宥夊礉鐎ｎ喖纭€闁规儼妫勯拑鐔哥箾閹存瑥鐏╃紒鐘冲▕閺岋繝宕掑杈ㄧ殤闂佸搫顑呴幊搴ㄥ煘?
                        return hasOp;
                    } else {
                        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧壕褰掓煟閻旂厧浜伴柣鏂挎閹便劌顪冪拠韫闂備浇顕栭崰鎺楀焵椤掍焦鐏遍柡瀣叄閺岀喖骞嗚閺嬪啰鎮┑瀣厽閹艰揪绱曢悾顓㈡煕鎼淬劋鎲炬鐐诧攻閹棃濡搁敃鈧埀顒€鐖奸弻銊モ槈濡警浠遍梺绋款儐閹瑰洭骞冨▎鎾村仭闁归潧鍟挎禍鐐節闂堟稓澧㈢痪鎹愬Г閹便劌螣閹稿海銆愰梺缁樺笒閻忔岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｇ儤鍤€闁哥喕娉曢幑銏犖熼搹瑙勬閻熸粎澧楃敮鎺撳劔闁荤喐绮岀换鎴犵矙婢跺鍚嬪璺侯儑閸欏棗鈹戦悙鏉戠仸闁挎碍銇勮箛锝勯偗闁哄本绋掔换婵嬪礋椤愨剝顫曢梻浣告惈閺堫剛绮欓幋锕€鐓″璺号堥弸宥嗐亜閹炬鍊归幑锝夋⒑闂堟稒澶勯柛鏃€鐟ラ悾鐑芥晲閸℃绐為悗鍏夊亾濠电姴鍟伴妶顐︽⒒閸屾瑧绐旀繛浣冲泚鍥敇閵忕姷锛熼梻渚囧墮缁夌敻宕愰崼鏇熺厓闁告繂瀚崳娲煃?
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to get deletable area names", e);
            return List.of();
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸屾褰掑礂閸忚偐绋囬梺鎼炲妼閸婂骞夐幖浣瑰亱闁割偅绻勯悷鏌ユ⒑閹惰姤鏁辨俊顐㈠暣瀵寮撮姀鐘诲敹濠电娀娼уú銈呪枍瑜斿娲川婵犲海鍔烽梺杞扮椤嘲顕ｇ拠娴嬫婵妫欓崓闈涱渻閵堝棗绗掗柛濠冨姍婵″爼宕卞▎鎴晭闂備礁澹婇崑鍡涘窗鎼淬劍鍊堕柕澶涜礋娴滄粓鏌￠崶鈺佹灁闁瑰啿瀚伴弻鏇㈠醇椤掑倻鏆犻梺瀹狀嚙闁帮綁鐛崱姘兼Ъ闂佸搫妫庨崕闈涱潖缂佹ɑ濯撮柛娑橈龚绾偓缂傚倷绶￠崳顕€宕圭捄铏规殾婵犻潧顑呯粻鎶芥煛閸愶絽浜惧銈嗗姃缁瑩寮婚敐澶婎潊闁靛繆鏅濋崝鎼佹⒒?
     */
    private static final SuggestionProvider<ServerCommandSource> DELETABLE_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> deletableAreas = getDeletableAreaNames(source, dimension);
            
            // 濠电姷鏁告慨鐑藉极閹间礁纾块柟瀵稿Х缁€濠囨煃瑜滈崜姘跺Φ閸曨垰鍗抽柛鈩冾殔椤忣亪鏌涘▎蹇曠闁哄矉缍侀獮鍥敆娴ｇ懓鍓电紓鍌欒兌婵即宕曢悽绋胯摕鐎广儱鐗滃銊╂⒑閸涘﹥灏甸柛鐘查叄椤㈡岸鏁愭径濠傜€銈嗗姂閸ㄨ崵绮ｉ悙瀵哥瘈闁汇垽娼у瓭闂佺锕ｇ划娆撱€佸▎鎰窞闁归偊鍘搁幏娲⒑閸涘﹦鈽夐柨鏇樺€楃划顓☆槻闁宠鍨块幃娆撳煛娴ｅ嘲顥氭繝鐢靛Х閺佹悂宕戝☉銏犵疇閹兼番鍔嶉悡鈧梺鎸庣箓椤︻垳绮诲鑸电厱闁逛即娼ч弸娑氱磼閳ь剛鈧綆鍋佹禍婊堟煙閹佃櫕娅呴柍褜鍓氬ú鐔兼偘椤旂⒈鍚嬪璺侯儌閹锋椽姊洪崨濠勨槈闁挎洏鍔嶇粩鐔肺熼懖鈺冿紲婵犮垼娉涢鍡欎焊閿旈敮鍋撶憴鍕婵炲拑绲剧粋鎺楁晜閸撗団攺闁诲函缍嗘禍婵嬫偩閻撳簶鏀介幒鎶藉磹瑜斿浠嬪礋椤栵絾鏅炲┑鐐叉閹告悂鎳滆ぐ鎺撶厵缂備降鍨归弸鐔兼煟閹惧瓨绀嬮柡宀€鍠栭幃婊兾熺紒妯哄壆婵犵數鍋涢悧濠囨偡閳哄懎钃?
            for (String areaName : deletableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }
            
            return builder.buildFuture();
        };
    
    // ===== EasyAdd 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ鐐电磽娴ｇ顣抽柛瀣ㄥ€濆璇测槈閵忕姴宓嗛梺闈涱焾閸庤京绮诲ú顏呪拺缂佸灏呴崝鐔兼煕鐎ｃ劌鈧繂顕ｆ繝姘櫢闁绘灏欓敍婊冣攽閻樿尙浠涢柛鏃€鐗滅槐鐐哄醇閵忋垻锛?=====
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd闂傚倷娴囬褏鈧稈鏅犻、娆撳冀椤撶偟鐛ラ梺鍦劋椤ㄥ懐澹曟繝姘厵闁绘劦鍓氶悘閬嶆煛閳ь剟鎳為妷锝勭盎闂佸搫鍟崐鐢稿箯閿熺姵鐓曢幖杈剧磿缁犲鏌″畝鈧崰鏍ь嚕椤掑嫬唯闁靛鍎幏銈囩磽閸屾瑧顦﹂柛濠傛贡閺侇噣鍩勯崘褏绠氶梺璇″灡婢瑰棝鎮块埀顒勬⒑閸濆嫭宸濆┑顖ｅ幖鍗遍柛妤冨亹閺€浠嬫煟閹邦剚鈻曢柛銈囧枎閳规垿顢欓懞銉ュ攭濡ょ姷鍋涢敃銉╁箚閺冨牃鈧箓骞嬮悙瀛樼彎闂佽崵鍠愮划搴㈡櫠濡ゅ啯鏆滈柟鐑樻⒒娑撳秹鏌ㄥ┑鍡樺婵炴挸顭烽幃妤呮晲鎼存繄鐩庨梺浼欒礋閸斿矂婀侀梺缁樻尭妤犳悂鍩€椤掆偓濞尖€愁嚕?
     */
    private static int executeEasyAddStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁绘挾鍠栭獮鏍ㄦ綇閸撗咃紵闂佷紮缍嗛崣鍐蓟濞戙垹惟闁挎柨顫曟禒銏ゆ倵鐟欏嫭绀€闁圭⒈鍋婇崺銉﹀緞婵犲孩鍍甸梺鍛婎殘閸嬫稓鎲撮敂鎴掔箚闁绘劦浜滈埀顒佺墵閹兾旈崘銊︾€抽悗骞垮劚椤︻垶宕ヨぐ鎺撶厵闁绘垶锚濞?
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜婵傚憡顎嶉梺闈涙搐鐎氫即鐛Ο铏规殾闁搞儮鏁╅妸銉庢棃鎮╅棃娑楃捕濡炪倖鍨靛Λ婵婃闂佺粯顨呴悧濠囧磿閻旀悶浜滈柡鍐ㄦ搐閸氬湱鐥崣銉х煓婵﹦绮幏鍛村川婵犲倹娈樼紓鍌欑椤戝棛鏁垾宕囨殾闁荤喐澹嬮弨浠嬫倵閿濆簼绨芥い?
        try {
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炴灓闁崇懓绉归弻褑绠涘鍏肩秷閻庤娲橀悡锟犲蓟濞戙垹绠涢柛蹇撴憸閺佹牜绱掓ィ鍐暫缂佺姵鐗犲濠氭偄鐞涒€充壕闁汇垺顔栭悞楣冨疮閹间焦鈷戠痪顓炴媼濞兼劖绻涢懠顒€鏋涚€殿喛顕ч埥澶娢熼柨瀣偓濠氭⒑瑜版帒浜伴柛鎾寸⊕鐎电厧鐣濋崟顑芥嫼闂備緡鍋嗛崑娑㈡嚐椤栫偛鍌ㄩ柛婵勫劤绾惧ジ鏌嶈閸撴氨绮悢鐓庣劦妞ゆ巻鍋撻柣锝囧厴瀹曨偊宕熼鐔哥暦闂備線鈧偛鑻晶鎾煕閳规儳浜炬俊鐐€栧濠氬磻閹惧墎纾奸柣妯垮皺鏁堥悗瑙勬礈閹虫挾鍙呭銈呯箰鐎氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣烽垾鎰佹僵妞ゆ挻绮堢花濠氭⒑閸濆嫮袪闁告柨鐭傚畷鐢割敆娴ｉ绠氶梺鍝勮閸庝即骞夋ィ鍐╃厸?
            sendClientCommand(source, "areahint:easyadd_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.start_2").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐褰掑磻鐎ｎ偂绻嗛柕鍫濇噹閺嗙喖鏌ｉ鐔稿磳闁哄矉缍侀獮瀣晲閸♀晜顥夐柣搴ｆ嚀閹诧紕鎹㈤崼銉ヨ摕闁绘棁銆€閸嬫挸鈽夊▍顓т簽缁厼顫濋鑺ユ杸濡炪倖鏌ㄩ幖顐︽倶閺屻儲鐓涢悘鐐额嚙婵″ジ鏌嶉挊澶樻Ц閾伙綁鏌ｉ幘鍐茬槰闁哄棭浜炵槐鎾诲磼濞嗘垼绐楅梺鍝ュУ閻楁洟婀侀梺缁樺灱濡嫰鎮為崹顐犱簻闁瑰搫妫楁禍鍓х磽娴ｅ搫孝缂佸鎳撻悾鐑芥偡閹冲﹥妞介、鏃堝礋椤掍絿姘節閻㈤潧浠﹂柛銊ㄦ硾椤繈濡搁埡浣侯攨濠殿喗锕╅崢瑙勭濠婂牊鐓涚€广儱鍟崝姘舵煃瑜滈崜姘躲€冮崼銏犲灊?
     */
    private static int executeEasyAddCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel_2").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd缂傚倸鍊搁崐鎼佸磹閻戣姤鍊块柨鏂垮⒔閻瑩鏌熷▎陇顕уú锕€顭囪箛娑掆偓锕傚箣閻戝棗鏅梻鍌欑劍閹爼宕曞ú顏勭婵鍩栭崐鍧楁偣閸パ勨枙闁绘柨妫濋幃瑙勬姜閹峰矈鍔呴梺绋块缁绘垿濡甸崟顔剧杸閻庯綆浜滄慨锕傛⒑閸濆嫭婀扮紒瀣灴閸┿儲寰勬繛鐐€婚棅顐㈡处濞叉垿宕版繝鍥ㄢ拻濞达絼璀﹂弨鎵磽閸粌宓嗙€规洘鍔曢埞鎴犫偓锝庝簼閻庮剟鎮楅獮鍨姎妞わ富鍨堕弻瀣炊閵娧呯槇婵犵數濮撮崐褰掑闯娴犲鐓熼柟鎯у暱閳ь剙娼″濠氭晲閸℃ê鍔呴梺鎸庣☉鐎氼噣寮冲☉銏♀拺闁硅偐鍋涙俊鑺ヤ繆閻愯埖顥夐柣锝囧厴瀹曨偊宕熼妸锔绘綌婵犳鍠楅敋鐎规洦鍓欓悾鐑藉Ψ閵夘喗瀵岄梺闈涚墕濡稒鏅堕敃鍌涚厱濠电姴鍠氬▓妯肩磼?
     */
    private static int executeEasyAddLevel(CommandContext<ServerCommandSource> context, int level) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_level:" + level);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.level_2").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd婵犵數濮烽弫鎼佸磻閻愬搫鍨傞柛顐ｆ礀缁犱即鏌熼梻瀵稿妽闁哄懏绻堥弻銊╁籍閸屾粍鎲樺┑鐐茬墔缁瑩寮诲☉銏犖ㄦい鏃傚帶椤晠姊洪崫鍕仴闁稿海鏁诲濠氭晲婢跺﹦鐫勯梺鍓插亞閸犳劙寮抽悩缁樷拺缂佸灏呴弨璇测攽閻愯宸ユい顐㈢箰鐓ゆい蹇撶У閺呮繈姊洪棃娑氱畾闁哄懏绮嶉弲銉╂⒒娴ｇ瓔鍤欓悗娑掓櫊瀹曨偅鎯旈妸銉ь槷闂佺懓鐡ㄧ换鍕汲閿曞倹鐓忓┑鐐靛亾濞呭懐绱掗悪鍛М闁哄瞼鍠栭幃婊兾熺拠鑼暡闁荤偞纰嶉敃銏狀潖缂佹ɑ濯撮柣鐔煎亰閸ゅ绱撴担鍓插剱闁搞劌娼￠獮鍐樄鐎规洏鍔戦、娑橆潩椤掑倷鍠婇梻浣侯攰婢瑰牓骞撻鍡楃筏婵炲樊浜滅壕濠氭煙閹规劕鍚圭€规挷绶氶弻娑⑩€﹂幋婵囩亾婵炲濞€缁犳牕顫忛搹鐟板闁哄洨鍠愰悵鏇犵磽娴ｅ壊鍎愰柟鎼佺畺瀹曟岸骞掑Δ鈧悡娑㈡煕濠娾偓閻掞箓寮查鍕拺闂傚牊绋撴晶鏇熺箾鐏炲倸濮傞挊?
     */
    private static int executeEasyAddBase(CommandContext<ServerCommandSource> context, String baseName) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_base:" + baseName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.parent").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd缂傚倸鍊搁崐鎼佸磹閹间礁纾瑰瀣捣缁€濠囨煃瑜滈崜鐔煎蓟濞戞ǚ妲堥柛妤冨仧娴狀參姊洪崫鍕棛闁告濞婂濠氭晲婢跺浜归柡澶婄墐閺呮稒淇婇挊澶嗘斀闁绘﹩鍠栭悘顏呫亜椤撶偞宸濇俊鍙夊姍閹瑥顔忛鍏煎€┑鐘灱濞夋盯鏁冮妶鍥╃幓婵炴垯鍨洪埛鎴︽煟閻旂顥嬮柟鐣屽█閹粙顢涘☉杈ㄧ暦闂侀€涚┒閸旀垶鎱ㄩ埀顒勬煏閸繃顥犻柛娆忔閳规垿鎮欓弶鎴犱桓濡炪値鍘煎ú锕傚疾閸洖绫嶉柛灞剧矌閿涙粌顪冮妶鍡樼叆闁告艾顑夐獮鎴︽晲婢跺鍘撻悷婊勭矒瀹曟粌鈻庨幋鐘辩瑝閻庡箍鍎遍ˇ顖炲垂閸屾稓绠剧€瑰壊鍠曠花濠氭煛閸曗晛鍔滅紒缁樼洴楠炲鎮欑€靛憡顓婚梻浣告啞椤ㄥ棛鍠婂澶娢﹂柛鏇ㄥ灠閸愨偓闂侀潧顦崕顕€宕戦幘婢勬棃宕橀鍡欏姸?
     */
    private static int executeEasyAddContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.record.continue").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd闂傚倸鍊峰ù鍥敋瑜嶉湁闁绘垼妫勭粻鐘绘煙閹规劦鍤欓悗姘槹閵囧嫰骞掗幋婵愪患闂佹悶鍔岄崐褰掑箞閵娿儙鐔煎锤濡も偓閹藉灚绻濆▓鍨仩闁靛牊鎮傚濠氬焺閸愩劎绐為柟鍏肩暘閸ㄥ濡存繝鍕＝濞达絽澹婂Σ鍛婄箾绾绡€鐎殿喛顕ч埥澶娢熼柨瀣垫綌婵犵數鍋涘Λ妤冩崲閹烘挾顩插ù鐓庣摠閳锋垹鐥鐐村櫧闁割偒浜弻娑欑節閸愵亞鐤勯悗娈垮櫘閸嬪嫰顢橀崗鐓庣窞濠电姴娲ら弫瑙勭節閻㈤潧孝闁挎洏鍊濋獮濠囧箛閻楀牆浜楅梺绋跨灱閸嬬偤鎮￠悢鐓庣閺夊牆澧界壕鍧楁煟閿濆骸寮柡灞剧洴婵℃悂鏁傛慨鎰檸闁诲氦顫夊ú鏍箹椤愶箑鐓″璺侯煬濞尖晠鏌涘Δ鍐ㄤ粶閻熸瑱闄勬穱濠囨倷椤忓嫧鍋撻弽顓熷亱闁规崘顕х壕瑙勪繆閵堝懎鏆熼柣?
     */
    private static int executeEasyAddFinish(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_finish");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.record.finish").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd婵犵數濮烽弫鎼佸磿閹寸姴绶ら柦妯侯棦濞差亝鏅滈柣鎰靛墮鎼村﹪姊虹粙璺ㄧ伇闁稿鍋ゅ畷鎴﹀Χ婢跺鍘繝鐢靛仧閸嬫挸鈻嶉崨顖楀亾閻熺増鍟炵紒璇茬墦瀵鎮㈢悰鈥充壕婵炴垶顏鍡欑當婵﹩鍏橀弨鑺ャ亜閺傛寧鎯堥柣蹇旂叀閺岋紕浠︾拠鎻掝瀳闂佸疇妫勯ˇ顖濈亽闂佺粯鎸哥€垫帡寮抽鍡欑＝闁稿本鐟х拹浼存煕閻樺磭澧棁澶愭煟閹达絾顥夐柣鎺戠仛閵囧嫰骞掗崱妞惧缂傚倷鑳舵慨瀵哥矓閻熸壆鏆﹂柣鐔稿櫞濞差亶鏁傞柛鏇ㄥ墯琚ｅ┑鐘垫暩閸嬬偤宕圭捄渚僵闁靛ň鏅涚涵鈧┑顔斤供閸樿绂嶅鍫熺厸鐎广儱鍟崝姘舵煃瑜滈崜姘躲€冮崼銏犲灊?
     */
    private static int executeEasyAddSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("easyadd.error.area.save").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd闂傚倸鍊搁崐鐑芥嚄閸洖鍌ㄧ憸鏃堝Υ閸愨晜鍎熼柕蹇嬪焺濞茬鈹戦悩璇у伐閻庢凹鍙冨畷锝堢疀濞戞瑧鍘撻梺鍛婄箓鐎氼剚绂嶉悙瀵哥闁割偆鍣ュ▓鏇炃庨崶褝韬い銏＄☉椤繈顢橀悢宄板濠碉紕鍋戦崐鏍蓟閵娾晜鏅濇い蹇撳濞兼牗绻涘顔绘喚闁轰礁鍟撮弻鏇＄疀婵犲啯鐝曢柡浣哥墦濮婅櫣鎷犻幓鎺濆妷闂佸憡绻傞柊锝呯暦濠靛绠ｆ繝闈涚墛濞堥箖姊洪棃娴ュ牓寮插☉姘变笉闁挎繂顦伴悡娆撴煟閹寸倖鎴犱焊椤撶姵鍋栨慨妞诲亾婵﹦绮幏鍛存偡闁箑娈濈紓鍌欑椤戝棝宕归崸妤€绠栫憸鏂跨暦閵娾晩鏁嶆慨姗嗗墰娴滀即姊虹涵鍛棈闁规椿浜炲濠偯洪鍕紱闂佽宕樺畷闈涚暤娓氣偓閺屾盯鈥﹂幋婵囩亾婵炲濞€缁犳牕顫忛搹鐟板闁哄洨鍠愰悵鏇犵磽娴ｅ壊鍎愰柟鎼佺畺瀹曟岸骞掑Δ鈧悡娑㈡煕濠娾偓閻掞箓寮查鍕拺闂傚牊绋撴晶鏇熺箾鐏炲倸濮傞挊?
     */
    private static int executeEasyAddAltitudeAuto(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_altitude_auto");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude_4").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd闂傚倸鍊搁崐鐑芥嚄閸洖鍌ㄧ憸鏃堝Υ閸愨晜鍎熼柕蹇嬪焺濞茬鈹戦悩璇у伐闁绘锕畷鎴﹀煛閸涱喚鍘介梺閫涘嵆濞佳勬櫠娴煎瓨鐓冪紓浣股戠粈鈧銈庡幖濞硷繝骞婂鍫燁棃婵炴垶锕╁鏇㈡煟閻斿摜鐭掗柛瀣躬瀹曞綊宕奸弴鐘茬ウ闂佸搫绉查崝宥囩礊閸ヮ剚鐓曢柟鐐殔閹冲繐鈻撴總鍛娾拻濞撴埃鍋撻柍褜鍓涢崑娑㈡嚐椤栨稒娅犳い鏂款潟娴滄粓鏌ㄩ弮鍥棄妞ゃ儱顑夐弻宥堫檨闁告挻宀稿畷顐ｆ償閵娿儳顦梺鐟扮摠缁诲嫰寮抽敃鍌涚厪濠电偟鍋撳▍鍛磼閻欏懐绉柡宀€鍠栭幃婊兾熺拠鑼暡闁荤偞纰嶉敃銏狀潖缂佹ɑ濯撮柣鐔煎亰閸ゅ绱撴担鍓插剱闁搞劌娼￠獮鍐樄鐎规洏鍔戦、娑橆潩椤掑倷鍠婇梻浣侯攰婢瑰牓骞撻鍡楃筏婵炲樊浜滅壕濠氭煙閹规劕鍚圭€规挷绶氶弻娑⑩€﹂幋婵囩亾婵炲濞€缁犳牕顫忛搹鐟板闁哄洨鍠愰悵鏇犵磽娴ｅ壊鍎愰柟鎼佺畺瀹曟岸骞掑Δ鈧悡娑㈡煕濠娾偓閻掞箓寮查鍕拺闂傚牊绋撴晶鏇熺箾鐏炲倸濮傞挊?
     */
    private static int executeEasyAddAltitudeCustom(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_altitude_custom");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude_5").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd婵犵數濮烽弫鎼佸磻閻愬搫鍨傞柛顐ｆ礀缁犱即鏌涘┑鍕姢闁活厽鎹囬弻锝夋偄閻撳簼鍠婇梺璇叉唉閸╂牜鎹㈠☉姗嗗晠妞ゆ棁宕甸惄搴ｇ磽娴ｆ彃浜鹃梺绯曞墲缁嬫帡鎮￠弴鐔翠簻闁规澘澧庣粙鑽ょ磼閳ь剟宕奸姀鈥虫瀾闂備緡鍙忕粻鎴︽倶閿旂瓔娈介柣鎰嚟婢ь剚绻涢悡搴ｇ鐎规洘绮忛ˇ鎾煛閸℃劕鈧繂顫忓ú顏呯劵闁绘劘灏€氭澘顭胯閸犳濡甸崟顖涙櫆闁芥ê顦藉Λ鍡涙⒑闁偛鑻晶顖炴煕濠靛棝鍙勭€规洘绻堥獮瀣攽閸喐顔曢梻渚€娼ц墝闁哄懏绋撶划濠氭晲婢跺鍘甸梺缁樺灦钃遍悘蹇ｅ幘閹喖顫滈埀顒€顫忕紒妯诲闁荤喖鍋婇崵瀣磽娴ｅ壊鍎愰柛銊ユ健楠炲啳顦圭€规洏鍔戦、娑橆潩椤掑倷鍠婇梻浣侯攰婢瑰牓骞撻鍡楃筏婵炲樊浜滅壕濠氭煙閹规劕鍚圭€规挷绶氶弻娑⑩€﹂幋婵囩亾婵炲濞€缁犳牕顫忛搹鐟板闁哄洨鍠愰悵鏇犵磽娴ｅ壊鍎愰柟鎼佺畺瀹曟岸骞掑Δ鈧悡娑㈡煕濠娾偓閻掞箓寮查鍕拺闂傚牊绋撴晶鏇熺箾鐏炲倸濮傞挊?
     */
    private static int executeEasyAddAltitudeUnlimited(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_altitude_unlimited");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 婵犵數濮烽弫鍛婃叏娴兼潙鍨傞柣鎾崇岸閺嬫牗绻涢幋鐐茬劰闁稿鎸搁～婵嬫偂鎼淬垻褰庢俊銈囧Х閸嬫盯宕婊呯处濞寸姴顑呯涵鈧梺鍝ュ仦濞肩稓dd婵犵數濮烽。钘壩ｉ崨鏉戠；闁糕剝蓱濞呯姵淇婇妶鍛殭闁搞劍绻堥悡顐﹀炊閵婏腹鎷婚梺缁樼箚濞夋盯鍩為幋锔藉亹闁告瑥顦ˇ鈺呮⒑缂佹ê绗╂い鏇嗗洠鈧妇鎹勯妸锕€纾繛鎾村嚬閸ㄤ即宕滈弶娆炬富闁靛牆鎳愮粻鐐烘煕鎼达絾鏆€殿喛顕ч埥澶娢熼柨瀣垫綌婵犵數鍋涘Λ妤冩崲閹烘挾顩插ù鐓庣摠閳锋垹鐥鐐村櫧闁割偒浜弻娑欑節閸愵亞鐤勯悗娈垮櫘閸嬪嫰顢橀崗鐓庣窞濠电姴娲ら弫瑙勭節閻㈤潧孝闁挎洏鍊濋獮濠囧箛閻楀牆浜楅梺绋跨灱閸嬬偤鎮￠悢鐓庣閺夊牆澧界壕鍧楁煟閿濆骸寮柡灞剧洴婵℃悂鏁傛慨鎰檸闁诲氦顫夊ú鏍箹椤愶箑鐓″璺侯煬濞尖晠鏌涘Δ鍐ㄤ粶閻熸瑱闄勬穱濠囨倷椤忓嫧鍋撻弽顓熷亱闁规崘顕х壕瑙勪繆閵堝懎鏆熼柣?
     */
    private static int executeEasyAddColor(CommandContext<ServerCommandSource> context, String color) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:easyadd_color:" + color);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.color_2").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€浼噋andarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeExpandAreaStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁抽攱鍨圭槐鎾存媴婵埈浜棢婵鍩栭悡娑㈡煕濞戝崬鏋ゆ繛鎻掔摠閵囧嫰濮€閿涘嫭鍣у銈嗘尭閸氬顕ラ崟顒傜瘈闁告洟娼ч幃鎴︽⒑閼姐倕鏋戠紒顔肩Ч瀹曞綊宕稿Δ鈧繚?
            if (source.getPlayer() == null) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
                return 0;
            }
            
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炴灓闁崇懓绉归弻褑绠涘鍏肩秷閻庤娲橀悡锟犲蓟濞戙垹绠涢柛蹇撴憸閹稿姊虹粙娆惧剱闁圭懓娲幃浼搭敋閳ь剙鐣疯ぐ鎺濇晩闁告瑣鍎冲Λ顖炴⒒閸屾瑨鍏岀痪顓炵埣瀵彃顭ㄩ崨顖滅厯闂佽宕樺▔娑㈠垂濠靛鐓冮柛婵嗗婵ジ鏌℃担鍝バｉ柟渚垮妼椤粓宕卞Δ鈧粻鐟扳攽閻愯尙澧戦柛搴ゅ皺閹广垹鈽夊锝呬壕闁汇垺顔栭悞鍓ф偖閵娾晜鈷戠紒瀣皡閺€鑽ょ磼鐠囪尙澧︾€规洘妞介崺鈧い鎺嶉檷娴滄粓鏌熼崫鍕ゆい锔肩畵閺屾稑鈹戦崱妯诲創闂傚洤顦扮换婵囩節閸屾粌鈪遍梺浼欑秬濞咃綁鍩€椤掑喚娼愭繛娴嬫櫇缁辩偞绗熼埀顒勬偘椤旈敮鍋撻敐搴℃灍闁哄懏绮撻弻锝夋偄绾拌鲸娈ユ繝銏ｎ潐濞叉鎹㈠☉娆愮秶闁告挆鍐ㄧ厒闂備胶顢婇婊呮崲濠靛棛鏆﹂柡鍥╁亹閺嬪酣鏌熼幆褜鍤熼柛姗€浜跺娲传閸曨偀鍋撴禒瀣垫晪鐟滃孩绌辨繝鍐檮闁告稑锕﹂崢鐢告⒑閸濆嫬鈧爼宕曟潏銊︽珷闁肩⒈鍓涚壕?
            sendClientCommand(source, "areahint:expandarea_start");
            
            return Command.SINGLE_SUCCESS;
            
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.expand.start").append(TextCompat.literal(e.getMessage())));
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€浼噋andarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ鍨毢濞寸娀绠栧铏瑰寲閺囩喐鐝曠紓浣割儐閹告儳危閹版澘绠婚悗闈涙憸閹虫繈姊洪幖鐐插妧闁告剬鍛槹缂?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeExpandAreaContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.continue.expand").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€浼噋andarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁绘挸鍊荤槐鎾存媴閸濆嫅锛勭磼椤旂晫鎳囨鐐插暙铻栭柍褜鍓熼崺銉﹀緞婵炵偓鐎诲┑鐐叉缁绘劘銇愰崱娆戠＝?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeExpandAreaSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.save.expand").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞rinkarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeShrinkAreaStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁抽攱鍨圭槐鎾存媴婵埈浜棢婵鍩栭悡娑㈡煕濞戝崬鏋ゆ繛鎻掔摠閵囧嫰濮€閿涘嫭鍣у銈嗘尭閸氬顕ラ崟顒傜瘈闁告洟娼ч幃鎴︽⒑閼姐倕鏋戠紒顔肩Ч瀹曞綊宕稿Δ鈧繚?
            if (source.getPlayer() == null) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
                return 0;
            }
            
            // 闂傚倸鍊搁崐鎼佸磹妞嬪孩顐介柨鐔哄Т绾惧鏌涘☉鍗炴灓闁崇懓绉归弻褑绠涘鍏肩秷閻庤娲橀悡锟犲蓟濞戙垹绠涢柛蹇撴憸閹稿姊虹粙娆惧剱闁圭懓娲幃浼搭敋閳ь剙鐣疯ぐ鎺濇晩闁告瑣鍎冲Λ顖炴⒒閸屾瑨鍏岀痪顓炵埣瀵彃顭ㄩ崨顖滅厯闂佽宕樺▔娑㈠垂濠靛鐓冮柛婵嗗婵ジ鏌℃担鍝バｉ柟渚垮妼椤粓宕卞Δ鈧粻鐟扳攽閻愯尙澧戦柛搴ゅ皺閹广垹鈽夊锝呬壕闁汇垺顔栭悞鍓ф偖閵娾晜鈷戠紒瀣皡閺€鑽ょ磼鐠囪尙澧︾€规洘妞介崺鈧い鎺嶉檷娴滄粓鏌熼崫鍕ゆい锔肩畵閺屾稑鈹戦崱妯诲創闂傚洤顦扮换婵囩節閸屾粌鈪遍梺浼欑秬濞咃綁鍩€椤掑喚娼愭繛娴嬫櫇缁辩偞绗熼埀顒勬偘椤旈敮鍋撻敐搴℃灍闁哄懏绮撻弻锝夋偄绾拌鲸娈ユ繝銏ｎ潐濞叉鎹㈠☉娆愮秶闁告挆鍐ㄧ厒闂備胶顢婇婊呮崲濠靛棛鏆﹂柡鍥╁亹閺嬪酣鏌熼幆褜鍤熼柛姗€浜跺娲传閸曨偀鍋撴禒瀣垫晪鐟滃孩绌辨繝鍐檮闁告稑锕﹂崢鐢告⒑閸濆嫬鈧爼宕曟潏銊︽珷闁肩⒈鍓涚壕?
            sendClientCommand(source, "areahint:shrinkarea_start");
            
            return Command.SINGLE_SUCCESS;
            
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.shrink.start").append(TextCompat.literal(e.getMessage())));
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞rinkarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ鍨毢濞寸娀绠栧铏瑰寲閺囩喐鐝曠紓浣割儐閹告儳危閹版澘绠婚悗闈涙憸閹虫繈姊洪幖鐐插妧闁告剬鍛槹缂?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeShrinkAreaContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.continue.shrink").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞rinkarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁绘挸鍊荤槐鎾存媴閸濆嫅锛勭磼椤旂晫鎳囨鐐插暙铻栭柍褜鍓熼崺銉﹀緞婵炵偓鐎诲┑鐐叉缁绘劘銇愰崱娆戠＝?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeShrinkAreaSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.save.shrink").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€浼噋andarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ璇茬祷濠殿喖顦靛铏圭矙濞嗘儳鍓遍梺瑙勭摃椤曆囶敋閵夆晛绀嬫い鎾寸☉娴滈箖鏌ㄥ┑鍡欏嚬闁瑰弶鍎抽湁闁绘娅曠亸锔芥叏婵犲懏顏犻柛鏍ㄧ墵瀵挳鎮㈤崫銉ョ悼闂傚倷绀侀幗婊勬叏閹绢喗鐓€闁挎繂顦卞畵渚€鏌涢幇闈涙灈闁告濞婇弻鏇＄疀婵犲倸鈷夊┑?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param areaName 闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼閻樺磭澧甸柡宀嬬節瀹曞爼濡烽妷褌鎮ｉ梺鑽ゅ枑閻熻鲸淇婇崶顒€鐒垫い鎺嗗亾缂佺姴绉瑰畷鏇㈠础閻愨晜鐏?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeExpandAreaSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃€浼噋andarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁哥槐鎾存媴缁嬭法楠囬柣搴ｇ懗閸涱喖搴婂┑鐐村灟閸ㄥ湱绮婚敐鍡欑瘈闂傚牊绋撴晶鏇㈡煃瀹勫府鍔熺紒?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeExpandAreaCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:expandarea_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel.expand").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    // ===== DivideArea 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ鐐电磽娴ｇ顣抽柛瀣ㄥ€濆璇测槈閵忕姴宓嗛梺闈涱焾閸庤京绮诲ú顏呪拺?=====

    private static int executeDivideAreaStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            if (source.getPlayer() == null) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.divide.start").append(TextCompat.literal(e.getMessage()))); return 0; }
    }

    private static int executeDivideAreaSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_2").append(TextCompat.literal(e.getMessage()))); return 0; }
    }

    private static int executeDivideAreaContinue(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_continue");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    private static int executeDivideAreaSave(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_save");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    private static int executeDivideAreaName(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_name:" + name);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    private static int executeDivideAreaLevel(CommandContext<ServerCommandSource> context, int level) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_level:" + level);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    private static int executeDivideAreaBase(CommandContext<ServerCommandSource> context, String baseName) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_base:" + baseName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    private static int executeDivideAreaColor(CommandContext<ServerCommandSource> context, String color) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_color:" + color);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    private static int executeDivideAreaCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            if (!CommandSourceCompat.isExecutedByPlayer(source)) { CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9")); return 0; }
            sendClientCommand(source, "areahint:dividearea_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) { return 0; }
    }

    // ===== AddHint 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ鐐电磽娴ｇ顣抽柛瀣ㄥ€濆璇测槈閵忕姴宓嗛梺闈涱焾閸庤京绮诲ú顏呪拺?=====

    private static int executeAddHintStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            if (source.getPlayer() == null) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
                return 0;
            }
            sendClientCommand(source, "areahint:addhint_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.vertex.add.start").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeAddHintSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:addhint_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_2").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeAddHintContinue(CommandContext<ServerCommandSource> context) {
        // 缂傚倸鍊搁崐鎼佸磹閹间礁纾瑰瀣捣缁€濠囨煃瑜滈崜鐔煎蓟濞戞ǚ妲堥柛妤冨仧娴狀參姊洪崫鍕棛闁告濞婂濠氭晲婢跺浜归柡澶婄墐閺呮稒淇婇挊澶嗘斀闁绘﹩鍠栭悘顏呫亜椤撶偞宸濇俊鍙夊姍閹瑥顔忛鍏煎€┑鐘灱濞夋盯顢栭崨顔绢浄婵炲樊浜濋悡娑橆熆鐠虹尨鍔熷褎鍨块弻娑㈠棘鐠恒劎鍔梺璇″灟閻掞妇鎹㈠┑鍡╂僵妞ゆ挻绋掔€氬吋绻濋悽闈涗粶婵☆偅鐟╁畷鏇㈠箮閽樺鍤戝┑鐐村灦閻燂絾绂嶅鍫熺叆闁哄啫娴傚鎰箾閸涱叏韬柡宀€鍠栭、娆撴嚃閳轰胶鍘介柣搴ゎ潐濞茬喐绂嶉崼鏇樷偓浣割潨閳ь剟骞冮埡浣叉灁闁圭宸╅妷銉㈡斀闁绘ɑ鍓氶崯蹇涙煕閻樻剚娈樼紓鍌涙崌瀹曠螖閳ь剛澹曡ぐ鎺撶厸闁搞儻绲鹃崵宥夋煕閵夘喖澧柣鎾寸洴閺屾稑鈽夐崡鐐茬闂佹椿鍋勭€氭澘顫忛搹鍦＜婵妫涢崝椋庣磽娓氬洤娅橀柛銊ㄤ含缁骞掗幋顓熷缓闂侀€炲苯澧寸€殿喖顭烽幃銏ゆ偂鎼达紕鈧厼顪冮妶鍡樷拹閻庢艾閰ｉ弫宥夊礋椤撶媴绱?
        return Command.SINGLE_SUCCESS;
    }

    private static int executeAddHintSubmit(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:addhint_submit");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_5").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeAddHintCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:addhint_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel_4").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    // ===== DeleteHint 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栫瑧婵犵妲呴崑鍛存晝閵忋倕绠栫€瑰嫭澹嬮弸搴ㄧ叓閸ャ劍鎯勫ù灏栧亾闂?=====

    private static int executeDeleteHintStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        try {
            if (source.getPlayer() == null) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
                return 0;
            }
            sendClientCommand(source, "areahint:deletehint_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.vertex.delete.start").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeDeleteHintSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:deletehint_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_2").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeDeleteHintToggle(CommandContext<ServerCommandSource> context, int index) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:deletehint_toggle:" + index);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.vertex").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeDeleteHintSubmit(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:deletehint_submit");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_5").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeDeleteHintCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:deletehint_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel_4").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞rinkarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｅΟ璇茬祷濠殿喖顦靛铏圭矙濞嗘儳鍓遍梺瑙勭摃椤曆囶敋閵夆晛绀嬫い鎾寸☉娴滈箖鏌ㄥ┑鍡欏嚬闁瑰弶鍎抽湁闁绘娅曠亸锔芥叏婵犲懏顏犻柛鏍ㄧ墵瀵挳鎮㈤崫銉ョ悼闂傚倷绀侀幗婊勬叏閹绢喗鐓€闁挎繂顦卞畵渚€鏌涢幇闈涙灈闁告濞婇弻鏇＄疀婵犲倸鈷夊┑?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @param areaName 闂傚倸鍊搁崐鐑芥嚄閸撲焦鍏滈柛顐ｆ礀閻ょ偓绻濋棃娑卞剰缂佹劖顨婇獮鏍庨鈧俊鑲╃磼閻樺磭澧甸柡宀嬬節瀹曞爼濡烽妷褌鎮ｉ梺鑽ゅ枑閻熻鲸淇婇崶顒€鐒垫い鎺嗗亾缂佺姴绉瑰畷鏇㈠础閻愨晜鐏?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeShrinkAreaSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_select:" + areaName);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞rinkarea闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁哥槐鎾存媴缁嬭法楠囬柣搴ｇ懗閸涱喖搴婂┑鐐村灟閸ㄥ湱绮婚敐鍡欑瘈闂傚牊绋撴晶鏇㈡煃瀹勫府鍔熺紒?
     * @param context 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娴傚Λ銈夋⒑瀹曞洨甯涢柡灞诲姂閸╃偤骞嬮敂钘変汗闁哄鐗滈崑鍕储閿熺姵鈷戦柤濮愬€曢弸鎴︽煙閻熺増鍠樼€?
     * @return 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾ч柛婵勫劤绾句粙鏌涚仦鎹愬闁逞屽墰閸忔﹢骞冮悙鐑樻櫇闁稿本姘ㄩ鍥ㄧ節閻㈤潧校缁炬澘绉瑰畷?
     */
    private static int executeShrinkAreaCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        
        try {
            sendClientCommand(source, "areahint:shrinkarea_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel.shrink").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆鍠栫壕鍧楁煙閹増顥夐幖鏉戯躬閺屻倝鎳濋幍顔肩墯婵炲瓨绮岀紞濠囧蓟濞戙垹唯妞ゆ棁宕甸弳妤佺箾鐎涙鐭婄紓宥咃躬瀵鎮㈤悡搴ｇ暰閻熸粌绉瑰铏綇閵婏絼绨婚梺闈涚墕閹冲繘宕甸崶顒佺厸鐎光偓鐎ｎ剙鍩岄梺宕囨嚀閸熸挳骞冨▎鎿冩晢濞达絽鎼徊钘夆攽閿涘嫬浜奸柛濠冪墵瀵濡歌閻捇鏌ｉ悢绋款棆闁哄棴绠撻弻銊モ攽閸♀晜鈻撻梺杞扮缁夌數鎹㈠☉銏犲耿婵°倓鐒﹀畷鎶芥⒒閸屾凹妲哥紒澶婄秺瀵鏁愭径瀣汗闁哄鐗冮弲鈺佄ｉ鐣岀瘈闁冲皝鍋撻柛灞剧矌閻撴捇姊虹拠鈥虫灈婵炲皷鈧磭鏆﹂柛妤冨剱濞笺劑鏌涢埄鍐剧劷闁哄棭鍓氱换婵堝枈濡椿娼戦梺鎼炲妺閸楁娊鐛弽顓ф晝闁挎梻绮弲鈺傜節閻㈤潧孝婵炶绠撻幃锟犳偄閸忚偐鍘繝鐢靛仜閻忔繈宕濆顓滀簻闊洤锕ュ▍濠囨煛瀹€瀣М妤犵偞顭囬埀顒勬涧閹诧繝宕虫导瀛樺€甸悷娆忓绾炬悂鏌涙惔銊ゆ喚妞ゃ垺蓱缁虹晫绮欓崹顔剧崺婵＄偑鍊栭幐鐐叏瀹曞洤鍨濋柟缁㈠枟閳?
     * @param source 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑鐞涒€充壕?
     * @param dimension 缂傚倸鍊搁崐鎼佸磹閹间礁纾归柣鎴ｅГ閸婂潡鏌ㄩ弴妤€浜惧銈庡幖濞层倝鍩㈡惔銊ョ闁哄倸銇樻竟?
     * @return 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸岀偞鐓熼柕蹇曞Т椤ュ繘鏌℃担闈╄含闁哄矉绻濆畷鍫曞煛娴ｅ洢鍎抽惀顏堝礈瑜嶆禍楣冩煏閸パ冾伃鐎殿喗娼欒灃闁逞屽墯缁傚秵銈ｉ崘鈹炬嫼缂傚倷鐒﹂…鍥╃不閵夆晜鐓曢柣妯诲墯濞堟粍顨ラ悙鍙夘棦鐎殿噮鍣ｅ畷濂告偄閸涘﹦褰嗛梻鍌欒兌缁垶宕濋敃鍌氱婵炴垯鍨瑰Ч鏌ユ煟濡偐甯涢柍閿嬪灩缁辨帡顢涘☉娆戭槰濠电偛鎳庡ú顓㈠蓟濞戙垺鍋愰柡灞诲劚瀵澘螖閻橀潧浠滈柛鐔告綑椤曪綁骞橀纰辨綂闂佺粯蓱椤旀牕危鐎涙绡€鐎典即鏀卞姗€鍩€椤掍焦绀堥柍褜鍓氶崫搴ㄥ礉閹达箑鏄ラ柍?
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
                        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧懜褰掓煛鐏炶鍔氱紒鈧崘鈹夸簻闊洦鎸搁鈺冪磼閻橆喖鍔︽慨濠勭帛閹峰懘宕ㄦ繝鍌涙畼闂備浇鍋愰幊鎾存櫠閻ｅ苯鍨濋柡鍐ㄥ€搁ˉ姘舵倵閿濆啫濡烘慨瑙勵殜閹嘲顭ㄩ崟顒傚弳濡炪倖娲╃紞浣哥暦瑜版帩鏁冮柕蹇嬪灮閻涱噣鏌ｆ惔鈥冲辅闁稿鎸搁埞鎴︽偐閹绘巻鍋撹ぐ鎺戠９閹兼番鍔嶉埛鎺懨归敐鍫燁棄闁告艾缍婇弻娑氣偓锝庡亞閳藉鎽堕悙缈犵箚闁靛牆鎳忛崳鍦磼閻橀潧鏋涢柡宀€鍠栭獮鍡涙偋閸偅顥夐梻浣规た閸樹粙銆冮崱娑樜﹂柛鏇ㄥ灠閸愨偓闂侀潧臎閸曨偅鐝＄紓鍌氬€烽梽宥夊礉鐎ｎ喖纭€闁规儼妫勭粻鏍偡濞嗗繐顏┑顖氼嚟缁辨帞鈧綆浜炲銊╂煙缁嬪灝鏆ｆ慨濠傛惈鏁堥柛銉戔偓閸嬫捇寮撮悙宥嗙☉閳诲酣骞橀幍浣镐壕闁告劦鍠栭悙濠冦亜椤掑澧伴柛鐘崇墪閻ｇ兘顢曢敃鈧粈瀣⒒閸喓鈯曢柡?
                        return hasOp;
                    } else {
                        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧壕褰掓煟閻旂厧浜伴柣鏂挎閹便劌顪冪拠韫闂備浇顕栭崰鎺楀焵椤掍焦鐏遍柡瀣叄閺岀喖骞嗚閺嬪啰鎮┑瀣厽閹艰揪绱曢悾顓㈡煕鎼淬劋鎲炬鐐诧攻閹棃濡搁敃鈧埀顒€鐖奸弻銊モ槈濡警浠遍梺绋款儐閹瑰洭骞冨▎鎾村仭闁归潧鍟挎禍鐐節闂堟稓澧㈢痪鎹愬Г閹便劌螣閹稿海銆愰梺缁樺笒閻忔岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｇ儤鍤€闁哥喕娉曢幑銏犖熼搹瑙勬閻熸粎澧楃敮鎺撳劔闁荤喐绮岀换鎴犵矙婢跺鍚嬪璺侯儑閸欏棗鈹戦悙鏉戠仸闁挎碍銇勮箛锝勯偗闁哄本绋掔换婵嬪礋椤愨剝顫曢梻浣告惈閺堫剛绮欓幋锕€鐓″璺号堥弸宥嗐亜閹炬鍊归幑锝夋⒑闂堟稒澶勯柛鏃€鐟ラ悾鐑芥晲閸℃绐為悗鍏夊亾濠电姴鍟伴妶顐︽⒒閸屾瑧绐旀繛浣冲泚鍥ㄧ鐎ｎ偄浜楅梺鍓插亖閸庨亶鎮块鈧獮鏍ㄦ綇閸撗勫仹缂佹儳褰炵划娆撳蓟濞戙垹绠涙い鎾跺仜婵垹鈹戦悙鍙夊櫣缂佸缍婂濠氭晲閸涱亝鏂€闂佸綊鍋婇崜姘舵倶瀹ュ鈷?
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to get settable area names", e);
            return List.of();
        }
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸岀偞鐓熼柕蹇曞Т椤ュ繘鏌℃担闈╄含闁哄矉绻濆畷鍫曞煛娴ｅ洢鍎抽惀顏堝礈瑜嶆禍楣冩煏閸パ冾伃鐎殿喗娼欒灃闁逞屽墯缁傚秵銈ｉ崘鈹炬嫼缂傚倷鐒﹂…鍥╃不閵夆晜鐓曢柣妯诲墯濞堟粍顨ラ悙鍙夘棦鐎殿噮鍣ｅ畷濂告偄閸涘﹦褰嗛梻鍌欒兌缁垶宕濋敃鍌氱婵炴垯鍨瑰Ч鏌ユ煟濡偐甯涢柍閿嬪灩缁辨帡顢涘☉娆戭槰濠电偛鎳庡ú顓㈠蓟濞戙垺鍋愰柡灞诲劚瀵澘螖閻橀潧浠滈柛鐔告綑椤曪綁骞橀纰辨綂闂佺偨鍎遍崯鎸庣珶閸曨垱鈷掑ù锝呮啞閸熺偞绻涚拠褏鐣电€规洘鍨垮畷鍗炩枎閹板灚缍楅梻浣瑰濞叉牠宕愯ぐ鎺戠；闁挎繂鎳岄埀顒佸笒椤繈顢楁担瑙勫€烽梻浣告惈椤戝洭宕伴幘璇茬疅闁告稑顭槐锝吤归敐鍛儓濠碉紕鏅槐鎾存媴鐠団剝鐣奸梺鍝ュТ闁帮綁宕洪妷锕€绶炲┑鐐灮閸犳牠骞婇敓鐘参ч柛娑卞灣閳?
     */
    private static final SuggestionProvider<ServerCommandSource> SETHIGH_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> settableAreas = getSettableAreaNames(source, dimension);
            
            // 濠电姷鏁告慨鐑藉极閹间礁纾块柟瀵稿Х缁€濠囨煃瑜滈崜姘跺Φ閸曨垰鍗抽柛鈩冾殔椤忣亪鏌涘▎蹇曠闁哄矉缍侀獮鍥敆娴ｇ懓鍓电紓鍌欒兌婵即宕曢悽绋胯摕鐎广儱鐗滃銊╂⒑閸涘﹥灏甸柛鐘查叄椤㈡岸鏁愭径濠傜€銈嗗姂閸ㄨ崵绮ｉ悙瀵哥瘈闁汇垽娼у瓭闂佺锕ｇ划娆撱€佸▎鎰窞闁归偊鍘鹃崢浠嬫⒑缂佹﹩鐒惧ù婊庡墴钘濋柨鏇楀亾妞ゎ叀鍎婚ˇ杈╃磼椤旂晫鎳囨鐐插暣瀹曠螖閳ь剟鐛姀鈩冨弿婵°倐鍋撴俊顐ｇ懇閹墽鎷犲ù瀣杸闂佸疇妫勫Λ妤呮倶閻樼粯鐓欑痪鏉垮船娴滅増銇勯姀锛勬噭缂佺粯绻堝畷鍫曞Ω瑜嶉獮妤呮⒒娓氣偓濞佳団€﹂崼銉ョ？闂侇剙绋侀弫鍌炴煃閸濆嫭鍣洪柣鎾跺枛閺岀喖骞嗚閸ょ喖鏌熼崘鍙夊窛闁逞屽墲椤煤韫囨稑纾块梺顒€绋侀弫鍥р攽閸屾粠鐒剧€瑰憡绻冮妵鍕箻鐠虹洅銏ゆ偨椤栨稒宕岄柡宀嬬稻閹棃鏁愰崨顒€瀵查梻浣侯焾閿曘儳鎹㈤崟顓燁潟闁圭偓鍓氬鈺佄涢悧鍫㈢畺闁哄應鏅犲娲川婵犲嫧濮囧┑鐐插悑閻熝呭垝閸儱骞㈡俊顖氭贡缁犳岸姊洪棃娑氬闁瑰啿閰ｉ、鏃堝Ψ閳哄倻鍘?
            for (String areaName : settableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }
            
            return builder.buildFuture();
        };
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆鍠栫壕鍧楁煙閹増顥夐幖鏉戯躬閺屻倝鎳濋幍顔肩墯婵炲瓨绮岀紞濠囧蓟濞戙垹唯妞ゆ棁宕甸弳妤佺箾鐎涙鐭婄紓宥咃躬瀵鎮㈤悡搴ｇ暰閻熸粌绉瑰铏綇閵婏絼绨婚梺闈涚墕閹冲繘宕甸崶顒佺厸鐎光偓鐎ｎ剙鍩岄梺宕囨嚀閸熸挳骞冨▎鎿冩晢濞达絽鎼徊钘夆攽閿涘嫬浜奸柛濠冪墵瀵濡歌閻捇鏌ｉ悢绋款棆闁哄棴绠撻弻銊モ攽閸♀晜鈻撻梺杞扮缁夌數鎹㈠☉銏犵闁绘劕鍟畝鎼佺嵁閹版澘宸濋悗娑欘焽閸樹粙妫呴銏″婵炲弶鐗楃粋鎺楁晜閻ｅ瞼顔曢梺鍛婄懃椤﹂亶鎯屾繝鍥ㄧ厓缂備焦蓱瀹曞本顨ラ悙宸剶闁轰礁鍟撮崺鈧い鎺戝閸欏﹪鏌曟径鍡樻珕闁绘挾鍠栭弻锟犲礃閵娧冾杸闂佽桨绶氭禍鍫曞蓟濞戞瑧绡€闁告劑鍔夐弸娆撴⒑閸濆嫭婀伴柣鈺婂灦閻涱噣骞掑Δ鈧粻濂告煕閹般劍娅囬柡鍡╁灦濮?
     * @param source 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑鐞涒€充壕?
     * @param dimension 缂傚倸鍊搁崐鎼佸磹閹间礁纾归柣鎴ｅГ閸婂潡鏌ㄩ弴妤€浜惧銈庡幖濞层倝鍩㈡惔銊ョ闁哄倸銇樻竟鏇㈡煟閻斿摜鎳冮悗姘煎墴閸┿垽寮撮悩顐壕閻熸瑥瀚壕鎼佹煕鎼淬劋鎲炬い銏∩戠缓鐣岀矙鐠恒劌鈧?
     * @return 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸岀偛绾ч柛顐ｇ☉婵¤姤绻涢崨顖氣枅闁诡喖缍婇獮渚€骞掗幋婵愮€辨繝鐢靛仜閻牊绂嶉鍫濊摕闁跨喓濮撮悙濠囨煃鏉炴壆鎮奸柨婵嗩槹閻撶喖鏌ㄥ┑鍡╂Х闁规煡绠栭弻锛勪沪閸撗勫垱闂佺硶鏅换婵嗙暦濮椻偓婵℃悂鏁傞柨顖氫壕濠电姵纰嶉埛鎴︽偣閸ワ絺鍋撻搹顐や憾婵犵數鍋犵亸娆撳窗鎼淬劍鍋╅柣鎴ｆ鍞梺闈涱樈閸犳牠骞冮幋锔解拺闁硅偐鍋涢崝姘繆椤愶絿娲寸€规洘鍨挎俊鍫曞川椤栨稒顔曟繝娈垮枟閿曗晠宕曢悽绋跨睄闁割偅绻勯ˇ浼存⒑鐎圭媭娼愰柛搴ゆ珪缁?
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
                        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧懜褰掓煛鐏炶鍔氱紒鈧崘鈹夸簻闊洦鎸搁鈺冪磼閻橆喖鍔︽慨濠勭帛閹峰懘宕ㄦ繝鍌涙畼闂備浇鍋愰幊鎾存櫠閻ｅ苯鍨濋柡鍐ㄥ€搁ˉ姘舵倵閿濆啫濡烘慨瑙勵殜閹嘲顭ㄩ崟顒傚弳濡炪倖娲╃紞浣哥暦瑜版帩鏁冮柕蹇嬪灮閻涱噣鏌ｆ惔鈥冲辅闁稿鎸搁埞鎴︽偐閹绘巻鍋撹ぐ鎺戠９閹兼番鍔嶉埛鎺懨归敐鍫燁棄闁告艾缍婇弻娑氣偓锝庡亞閳藉鎽堕悙缈犵箚闁靛牆鎳忛崳鍦磼閻橀潧鏋涢柡宀€鍠栭獮鍡涙偋閸偅顥夐梻浣规た閸樹粙銆冮崱娑樜﹂柛鏇ㄥ灠閸愨偓闂侀潧臎閸曨偅鐝＄紓鍌氬€烽梽宥夊礉鐎ｎ喖纭€闁规儼妫勯拑鐔兼煟閺傛寧鎯堥柡瀣墛娣囧﹪顢涘鍛闂佺粯绻冨Λ鍐潖?
                        return hasOp;
                    } else {
                        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炴牠顢曢敃鈧壕褰掓煟閻旂厧浜伴柣鏂挎閹便劌顪冪拠韫闂備浇顕栭崰鎺楀焵椤掍焦鐏遍柡瀣叄閺岀喖骞嗚閺嬪啰鎮┑瀣厽閹艰揪绱曢悾顓㈡煕鎼淬劋鎲炬鐐诧攻閹棃濡搁敃鈧埀顒€鐖奸弻銊モ槈濡警浠遍梺绋款儐閹瑰洭骞冨▎鎾村仭闁归潧鍟挎禍鐐節闂堟稓澧㈢痪鎹愬Г閹便劌螣閹稿海銆愰梺缁樺笒閻忔岸濡甸崟顖氱闁糕剝銇炴竟鏇㈡⒒娴ｇ儤鍤€闁哥喕娉曢幑銏犖熼搹瑙勬閻熸粎澧楃敮鎺撳劔闁荤喐绮岀换鎴犵矙婢跺鍚嬪璺侯儑閸欏棗鈹戦悙鏉戠仸闁挎碍銇勮箛锝勯偗闁哄本绋掔换婵嬪礋椤愨剝顫曢梻浣告惈閺堫剛绮欓幋锕€鐓″璺号堥弸宥嗐亜閹炬鍊归幑锝夋⒑闂堟稒澶勯柛鏃€鐟ラ悾鐑芥晲閸℃绐為悗鍏夊亾濠电姴鍟伴妶顐︽⒒閸屾瑧绐旀繛浣冲毝銊╁焵椤掑倻纾奸柣妯哄暱閻忔挳鏌涢埡鍌滄创鐎殿喕绮欓、姗€鎮㈢亸浣镐壕?
                        return signature.equals(playerName) || hasOp;
                    }
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to get expandable area names", e);
            return List.of();
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸岀偛绾ч柛顐ｇ☉婵¤姤绻涢崨顖氣枅闁诡喖缍婇獮渚€骞掗幋婵愮€辨繝鐢靛仜閻牊绂嶉鍫濊摕闁跨喓濮撮悙濠囨煃鏉炵増顦烽柟瀵稿厴濮婃椽宕ㄦ繝搴ｅ姺闂佽桨绀侀…宄邦嚕鐠囨祴妲堟慨妤€妫欓崓闈涱渻閵堝棗绗掗柛濠冨姍婵″爼宕卞▎鎴晭闂備礁澹婇崑鍡涘窗鎼淬劍鍊堕柕澶涜礋娴滄粓鏌￠崶鈺佹灁闁瑰啿瀚伴弻鏇㈠醇椤掑倻鏆犻梺瀹狀嚙闁帮綁鐛崱姘兼Ъ闂佸搫妫庨崕闈涱潖缂佹ɑ濯撮柛娑橈龚绾偓缂傚倷绶￠崳顕€宕圭捄铏规殾婵犻潧顑呯粻鎶芥煛閸愶絽浜惧銈嗗姃缁瑩寮婚敐澶婎潊闁靛繆鏅濋崝鎼佹⒒?
     */
    private static final SuggestionProvider<ServerCommandSource> EXPANDABLE_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> expandableAreas = getExpandableAreaNames(source, dimension);
            
            // 濠电姷鏁告慨鐑藉极閹间礁纾块柟瀵稿Х缁€濠囨煃瑜滈崜姘跺Φ閸曨垰鍗抽柛鈩冾殔椤忣亪鏌涘▎蹇曠闁哄矉缍侀獮鍥敆娴ｇ懓鍓电紓鍌欒兌婵即宕曢悽绋胯摕鐎广儱鐗滃銊╂⒑閸涘﹥灏甸柛鐘查叄椤㈡岸鏁愭径濠傜€銈嗗姂閸ㄨ崵绮ｉ悙瀵哥瘈闁汇垽娼у瓭闂佺锕ｇ划娆撱€佸▎鎰窞闁归偊鍘搁幏娲⒑闂堚晛鐦滈柛娆忛叄瀵娊顢涢悙瀵稿幐闂佸憡渚楅崰姘洪幘顔界厵妞ゆ牗姘ㄦ晶鏇熴亜閹惧啿鎮戠€垫澘瀚埀顒婄秵娴滄粍绔熼崟顖涒拻濞达絽鎲￠崯鐐寸箾鐠囇呯暤鐎规洘鍨垮畷鍗炩槈濡搫浜堕梻浣圭湽閸ㄥ綊骞夐敓鐘冲亗闁哄洨鍠愰崣蹇旀叏濡も偓濡鏅堕幍顔瑰亾濞戞瑧绠炴慨濠呮閸栨牠寮撮悢鍝ュ絽闂備礁鍚嬪鍧楀垂閸洏鈧礁顫濋鑺ョ€婚梺瑙勫閺呮稒绂嶉弶搴撴斀闁绘劕寮堕ˉ鐐烘煕鎼淬垹鈻曠€殿噮鍋婂畷濂稿即閻斿弶瀚奸梻浣告啞缁嬫垿鏁冮妶鍥╃當婵﹩鍘规禍婊堟煏婵炲灝鍔存俊顖楀亾濠?
            for (String areaName : expandableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }
            
            return builder.buildFuture();
        };
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓瀹曘儳鈧綆鍠栫壕鍧楁煙閹増顥夐幖鏉戯躬閺屻倝鎳濋幍顔肩墯婵炲瓨绮岀紞濠囧蓟濞戙垹唯妞ゆ棁宕甸弳妤佺箾鐎涙鐭婄紓宥咃躬瀵鎮㈤悡搴ｇ暰閻熸粌绉瑰铏綇閵婏絼绨婚梺闈涚墕閹冲繘宕甸崶顒佺厸鐎光偓鐎ｎ剙鍩岄梺宕囨嚀閸熸挳骞冨▎鎿冩晢濞达絽鎼徊钘夆攽閿涘嫬浜奸柛濠冪墵瀵濡歌閻捇鏌ｉ悢绋款棆闁哄棴绠撻弻銊モ攽閸♀晜鈻撻梺杞扮缁夌數鎹㈠☉銏犵闁绘劕鐏氶崳顓炩攽椤曞棛鍒版俊顐ｇ〒濡叉劙骞樼拠鑼紲濠殿喗锕╅崗姗€宕戦幘缁樺仺缂佸娉曢弻褍鈹戦悩缁樻锭婵☆偄鐭傚鎼佸籍閸喓鍘甸柡澶婄墕婢т粙鎮鹃柆宥嗙厸闁告洍鏅涢崝锕傛煛瀹€瀣М鐎殿噮鍣ｅ畷濂告偄閸涘﹥顔忛梻鍌欒兌椤牓鏁冮妷锔剧濠电姴娲ら拑鐔哥箾閹存瑥鐏╃紒鐘冲▕閺岀喖宕归鍏兼闂佷紮绲肩划娆忣潖?
     * @param source 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犻潧娲ゅ▍婵嬫⒑鐞涒€充壕?
     * @param dimension 缂傚倸鍊搁崐鎼佸磹閹间礁纾归柣鎴ｅГ閸婂潡鏌ㄩ弴妤€浜惧銈庡幖濞层倝鍩㈡惔銊ョ闁哄倸銇樻竟鏇㈡煟閻斿摜鎳冮悗姘煎墴閸┿垽寮撮悩顐壕閻熸瑥瀚壕鎼佹煕鎼淬劋鎲炬い銏∩戠缓鐣岀矙鐠恒劌鈧?
     * @return 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸岀偞鈷戞い鎺嗗亾缂佸鎸冲鍛婄瑹閳ь剟寮诲☉銏℃櫆閻犲洦褰冪粻鑽ょ磼閹冪稏缂侇喗鐟╁璇差吋婢跺﹣绱堕梺鍛婃处閸嬪懎鈻撻弻銉︹拺濞村吋鐟х粔顔济瑰鍐煟鐎殿喛顕ч埥澶愬閻橀潧骞愰梻浣告啞閸旀垿宕濆澶嬪€靛┑鐘崇閳锋垿鎮归崶锝傚亾閾忣偆浜舵繝鐢靛仩鐏忔瑩宕版惔銊﹀仼闁绘垼妫勫敮闂侀潧顦介崰鏍箖閹达附鈷戦柟鑲╁仜閸旀碍淇婇锝囨创鐎规洘鍨挎俊鍫曞川椤栨稒顔曟繝娈垮枟閿曗晠宕曢悽绋跨睄闁割偅绻勯ˇ浼存⒑鐎圭媭娼愰柛搴ゆ珪缁?
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
                    if (hasOp) return true;
                    // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸閻忕偟纭堕崑鎾崇暦閸ャ劍鐣峰┑鐘垫暩婵挳宕幏灞讳汗婵炴挷澶焑name闂傚倷娴囬褏鈧稈鏅犻、娆撳冀椤撶偟鐛ュ┑掳鍊愰崑鎾绘偂閵堝棛绡€濠电姴鍊绘晶鏇㈡煛鐎ｂ晝绐旈柡灞炬礋瀹曠厧鈹戦幇顓夛箓姊洪崫鍕仴闁稿海鏁诲濠氭晲婢跺﹦鐫勯梺鍓插亞閸犳劙寮抽悩缁樷拺缂佸灏呴弨璇测攽閻愯韬€殿喖顭烽弫鎰緞婵炩懇鏅犻弻鏇熷緞閸繂濮堕梺鍝勬鐢じnature
                    String baseName = area.getBaseName();
                    if (baseName == null) return false;
                    AreaData baseArea = areas.stream()
                        .filter(a -> a.getName().equals(baseName))
                        .findFirst().orElse(null);
                    return baseArea != null && playerName.equals(baseArea.getSignature());
                })
                .map(AreaData::getName)
                .toList();
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to get shrinkable area names", e);
            return List.of();
        }
    }
    
    /**
     * 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画濡炪倖鐗滈崑娑㈠垂閸岀偞鈷戞い鎺嗗亾缂佸鎸冲鍛婄瑹閳ь剟寮诲☉銏℃櫆閻犲洦褰冪粻鑽ょ磼閹冪稏缂侇喗鐟╁璇差吋閸℃ê顫￠梺鐟板槻閼活垶宕㈤敓鐘斥拺闁告稑顭悞浠嬫煛娴ｅ壊鐓肩€殿喛顕ч埥澶婎潨閸℃ê鍏婃俊鐐€栫敮鎺楀磹閹版澘围闁糕剝鐟ч鏇㈡⒑閸撴彃浜為柛鐘查叄閹﹢濡烽敂杞扮盎闂佸搫娲ㄩ崰鎾诲箠閸曨垱鐓忛柛顐墰閻ｉ亶鏌嶇拠鏌ュ弰妤犵偛妫滈ˇ鎶芥煛閸℃劕鍔︽慨濠勭帛閹峰懘宕ㄦ繝鍛攨缂傚倷绶￠崳顕€宕圭捄铏规殾婵犻潧顑呯粻鎶芥煛閸愶絽浜惧銈嗗姃缁瑩寮婚敐澶婎潊闁靛繆鏅濋崝鎼佹⒒?
     */
    private static final SuggestionProvider<ServerCommandSource> SHRINKABLE_AREA_SUGGESTIONS = 
        (context, builder) -> {
            ServerCommandSource source = context.getSource();
            String dimension = getDimensionFromSource(source);
            List<String> shrinkableAreas = getShrinkableAreaNames(source, dimension);
            
            // 濠电姷鏁告慨鐑藉极閹间礁纾块柟瀵稿Х缁€濠囨煃瑜滈崜姘跺Φ閸曨垰鍗抽柛鈩冾殔椤忣亪鏌涘▎蹇曠闁哄矉缍侀獮鍥敆娴ｇ懓鍓电紓鍌欒兌婵即宕曢悽绋胯摕鐎广儱鐗滃銊╂⒑閸涘﹥灏甸柛鐘查叄椤㈡岸鏁愭径濠傜€銈嗗姂閸ㄨ崵绮ｉ悙瀵哥瘈闁汇垽娼у瓭闂佺锕ｇ划娆撱€佸▎鎰窞闁归偊鍘搁幏娲煟閻愬鈻撻柍褜鍓欓崢鏍ㄧ珶閺囥垺鈷掑ù锝呮啞閸熺偤鏌熼崫銉ュ幋闁硅櫕顨婂畷婊勬媴鐠団剝缍楁繝娈垮枟椤牓宕洪弽顓熷亗闁绘柨鍚嬮悡蹇撯攽閻愯尙浠㈤柛鏃€纰嶉妵鍕疀婵犲啯鐝氶梺鍝勭焿缂嶄線鐛Ο灏栧亾闂堟稒鍟為柛锝勫嵆閹鎲撮崟顒傦紭闂佺閰ｆ禍鍫曘€佸Ο鑽ら檮缂佸鐏濈粣娑橆渻閵堝棙灏靛┑顔碱嚟閺侇噣骞樼紒妯煎幗闁硅壈鎻徊鑺ョ椤栫偞鐓曞┑鐘插暟缁犳绱掓潏銊﹀碍妞ゎ偅绻冮敍鎰攽閸ャ劍鐝﹂梻鍌欑閹诧紕绮欓幒鎴劷婵炲棙鍨归惌鍡涙煕閳╁厾鑲╂崲閸℃稒鐓忛柛顐ｇ箖閸ｈ銇勮箛瀣姦闁?
            for (String areaName : shrinkableAreas) {
                if (areaName.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                    builder.suggest(areaName);
                }
            }

            return builder.buildFuture();
        };

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔color select闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛妞ゆ挸銈稿娲川婵犲懎顥濋梺绋匡工濞尖€愁嚕婵犳碍鍋勯柣鎾虫捣椤旀帒顪冮妶鍡樼闁瑰啿绻樿棢闁哄诞灞惧瘜闂侀潧鐗嗛幊蹇曠矉鐎ｎ喗鐓曢柟鎹愭硾閺嬫稓鈧鍠栭…宄扮暦閵娾晩鏁囩憸搴ㄥ磽閻㈠憡鍊甸悷娆忓缁€鈧┑鐐茬湴閸旀垿骞冩ィ鍐╂優闁革富鍘鹃敍婵嬫⒑缁嬫寧婀伴柤褰掔畺瀵娊鎮欓悜妯煎幈闂侀潧顭堥崕鑼嫻閿熺姵鐓曢柟鐑樻尭濞搭喚鈧娲樼划宥夊箯閸涘瓨鍋￠梺顓ㄧ細閹綁姊婚崒娆戭槮闁硅绻濆畷婵嬫晜閻ｅ矈娲稿┑鐘绘涧椤戝懎效閺屻儳鍙撻柛銉ｅ妿閳洟鏌?
     */
    private static int executeRecolorSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();

        // 缂傚倸鍊搁崐鎼佸磹妞嬪海鐭嗗〒姘ｅ亾閽樻繃銇勯弽顐汗闁逞屽墾缁犳垿鎮鹃敓鐘茬闁惧浚鍋嗛埀顒佹そ閺岀喖鎳栭埡鍕婂淇婇幓鎺撳殗鐎规洜鏁诲鎾閿涘嫬骞愰梻浣规偠閸庮噣寮插☉娆戭浄婵犲﹤鍟犻弨浠嬫煟閹邦剙绾ч柛鐘筹耿閺岋紕浠︾拠鎻掝瀳闂佸疇妫勯ˇ顖濈亽闂佺粯鎸哥€垫帡寮抽锝勭箚闁绘劦浜滈埀顒佸姍瀵偊顢旈崼婵堝€為悷婊冪Ч瀹曠敻骞嬪┑鍐╂杸闂佺粯顭堥婊冾啅閵夆晜鐓曢柣鏃囨硾瀹撳棙顨ラ悙鎼疁闁诡喒鏅濈槐鎺懳熼悡搴＄疄闂傚倷鐒︾€笛兾涙担鍝ユ殾闁汇垻顭堥崒銊︾節婵犲倹鍣界痪?
        if (areaName.startsWith("\"") && areaName.endsWith("\"") && areaName.length() > 1) {
            areaName = areaName.substring(1, areaName.length() - 1);
        }

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:recolor_select:" + areaName);
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔color color闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛妞ゆ挸銈稿娲川婵犲懎顥濋梺绋匡工濞尖€愁嚕婵犳碍鍋勯柣鎾虫捣椤旀帒顪冮妶鍡樼闁瑰啿绻樿棢闁哄诞灞惧瘜闂侀潧鐗嗛幊蹇曠矉鐎ｎ喗鐓曢柟鎹愭硾閺嬫稓鈧鍠栭…宄扮暦閵娾晩鏁囩憸搴ㄥ磽閻㈠憡鍊甸悷娆忓缁€鈧┑鐐茬湴閸旀垿骞冩ィ鍐╂優闁革富鍘鹃敍婵嬫⒑缁嬫寧婀伴柤褰掔畺瀵娊鎮欓悜妯煎幈闂侀潧顭堥崕鑼嫻閿熺姵鐓曢柟鐑樻尭濞搭喚鈧娲樼划宥夊箯閸涘瓨鍋￠梺顓ㄧ細閹綁姊婚崒娆戭槮闁硅绻濆畷婵嬫晜閻ｅ矈娲稿┑鐘绘涧椤戝懎效閺屻儳鍙撻柛銉ｅ妿閳洟鏌?
     */
    private static int executeRecolorColor(CommandContext<ServerCommandSource> context, String colorValue) {
        ServerCommandSource source = context.getSource();

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:recolor_color:" + colorValue);
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔color confirm闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛妞ゆ挸銈稿娲川婵犲懎顥濋梺绋匡工濞尖€愁嚕婵犳碍鍋勯柣鎾虫捣椤旀帒顪冮妶鍡樼闁瑰啿绻樿棢闁哄诞灞惧瘜闂侀潧鐗嗛幊蹇曠矉鐎ｎ喗鐓曢柟鎹愭硾閺嬫稓鈧鍠栭…宄扮暦閵娾晩鏁囩憸搴ㄥ磽閻㈠憡鍊甸悷娆忓缁€鈧┑鐐茬湴閸旀垿骞冩ィ鍐╂優闁革富鍘鹃敍婵嬫⒑缁嬫寧婀伴柤褰掔畺瀵娊鎮欓悜妯煎幈闂侀潧顭堥崕鑼嫻閿熺姵鐓曢柟鐑樻尭濞搭喚鈧娲樼划宥夊箯閸涘瓨鍋￠梺顓ㄧ細閹綁姊婚崒娆戭槮闁硅绻濆畷婵嬫晜閻ｅ矈娲稿┑鐘绘涧椤戝懎效閺屻儳鍙撻柛銉ｅ妿閳洟鏌?
     */
    private static int executeRecolorConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:recolor_confirm");
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔color cancel闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛妞ゆ挸銈稿娲川婵犲懎顥濋梺绋匡工濞尖€愁嚕婵犳碍鍋勯柣鎾虫捣椤旀帒顪冮妶鍡樼闁瑰啿绻樿棢闁哄诞灞惧瘜闂侀潧鐗嗛幊蹇曠矉鐎ｎ喗鐓曢柟鎹愭硾閺嬫稓鈧鍠栭…宄扮暦閵娾晩鏁囩憸搴ㄥ磽閻㈠憡鍊甸悷娆忓缁€鈧┑鐐茬湴閸旀垿骞冩ィ鍐╂優闁革富鍘鹃敍婵嬫⒑缁嬫寧婀伴柤褰掔畺瀵娊鎮欓悜妯煎幈闂侀潧顭堥崕鑼嫻閿熺姵鐓曢柟鐑樻尭濞搭喚鈧娲樼划宥夊箯閸涘瓨鍋￠梺顓ㄧ細閹綁姊婚崒娆戭槮闁硅绻濆畷婵嬫晜閻ｅ矈娲稿┑鐘绘涧椤戝懎效閺屻儳鍙撻柛銉ｅ妿閳洟鏌?
     */
    private static int executeRecolorCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:recolor_cancel");
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔placebutton闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙綀鍋愮槐鎺楁倷椤掆偓缁€鍐┿亜閺囧棗娲ら悡姗€鏌熸潏鎯х槣闁轰礁锕﹂惀顏堝级閸喛鍩為梺鍛婃崌娴滃爼寮婚敐鍡樺劅闁挎稑瀚划鐢电磽娓氬洤鏋熼柟鐟版搐椤曪絾绻濆顑┾晠鏌嶉崫鍕舵敾闁哄應鏅犲娲礂闂傜鍩呴梺绋垮婵炲﹤鐣峰▎鎰浄閻庯綆鍋€閹锋椽姊洪懡銈呮瀾濠㈢懓妫濋幃楣冾敇閵忥紕鍘遍梺闈涱焾閸斿秹顢旈悩鐢电＜妞ゆ梻銆嬮煬顒勬煛娴ｇ鈧灝鐣峰鍡╂Ь闂佺粯鎸婚幑鍥蓟閿濆棙鍎熸い鏍ㄧ矊閸╁本绻濋埛鈧崘鈺傚闯缂?
     */
    private static int executeReplaceButtonStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:replacebutton_start");
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔placebutton confirm闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ｉ弮鍌ょ劸婵炲懏宀稿娲焻閻愯尪瀚板褜鍨遍妵鍕敇閻愭潙鏋犲Δ鐘靛仜缁绘帡鍩€椤掑﹦绉甸柛鐘崇墪濞插潡姊绘担铏广€婇柛鎾寸箞閳ワ箓宕堕鈧崒銊╂煙閹殿喖顣奸柍閿嬪灴閺屾盯鏁傜拠鎻掑闂佺粯甯為崑鎾诲Φ閸曨垱鏅滈柣鎰靛墯濮ｅ牓鎮楀▓鍨珮闁稿锕妴浣割潨閳ь剚鎱ㄩ埀顒勬煃閳轰礁鏆為柣?
     */
    private static int executeReplaceButtonConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:replacebutton_confirm");
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏃戞櫔placebutton cancel闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁哥槐鎾存媴缁嬭法楠囬柣搴ｇ懗閸涱喖搴婂┑鐐村灟閸ㄥ湱绮婚敐鍡欑瘈闂傚牊绋撴晶鏇熴亜閵堝倸浜鹃梻鍌欐祰椤曆勵殽閹间焦鍊舵慨妯哄船閸ㄦ繈鏌熼幑鎰靛殭闁藉啰鍠栭弻鏇熺箾閻愵剚鐝﹂梺杞扮閿曨亪寮婚妶澶婄畳闁圭儤鍨垫慨銏ゆ⒑閸涘﹤鐏ｇ紒顔界懃椤繘鎼圭憴鍕幑闂佸憡绮堢粈浣糕枔閹邦兘鏀介柣娆屽亾婵犫懇鍋撻梺鐓庣枃濞夋稑宓?
     */
    private static int executeReplaceButtonCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        if (CommandSourceCompat.isExecutedByPlayer(source)) {
            ServerNetworking.sendCommandToClient((net.minecraft.server.network.ServerPlayerEntity) source.getEntity(),
                "areahint:replacebutton_cancel");
        }

        return 1;
    }

    /**
     * 闂傚倸鍊搁崐椋庣矆娴ｉ潻鑰块梺顒€绉甸崑锟犳煙閹増顥夋鐐灲閺屽秹宕崟顐熷亾瑜版帒绾х紒鏂挎湞undviz闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁嶉崟顓犵厯闂佸湱鍎ら〃鍛村垂閸屾稓绡€闂傚牊渚楅崕蹇曠磼閻欌偓閸ｏ綁寮婚弴銏犻唶婵犲灚鍔栨晥闂備礁鎼幏瀣礈閻旂厧钃熸繛鎴欏焺閺佸啴鏌ㄥ┑鍡樺窛闁伙絿鏁诲铏规嫚閺屻儺鈧鏌曢崼鐔稿€愮€殿喖顭烽幃銏ゆ嚃閳轰胶銈﹂梻浣稿閻撳牓宕板顓烆嚤闁搞儮鏅濈壕钘壝归敐鍫綈闁抽攱甯￠弻娑㈡偐瀹曞洤鈷屽Δ鐘靛仜閸熸潙鐣峰鍡╂Ш濠电偛鐭堟禍顏堝蓟閿曗偓铻ｅ〒姘煎灡姝囨俊鐐€栭弻銊┧囬悽绋胯摕闁挎繂顦伴崑鍕煕濠靛嫬鍔滈柛锛卞啠鏀介柍钘夋娴滄繃銇勯妸銉уⅵ闁糕斁鍋撳銈嗗坊閸嬫捇鏌ｈ箛鏂垮摵闁诡噯绻濋弫鎾绘偐閸欏鈧?
     */
    private static int executeLanguageStart(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:language_start");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.start.language").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeLanguageSelect(CommandContext<ServerCommandSource> context, String langCode) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:language_select:" + langCode);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.language").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeLanguageCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }
        try {
            sendClientCommand(source, "areahint:language_cancel");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel.language").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    private static int executeServerLanguage(CommandContext<ServerCommandSource> context, String langCode) {
        ServerCommandSource source = context.getSource();
        ServerI18nManager.loadLanguage(langCode);
        CommandSourceCompat.sendMessage(source, TextCompat.literal(ServerI18nManager.translate("command.success.serverlanguage", langCode)));
        Areashint.LOGGER.info(ServerI18nManager.translate("command.success.serverlanguage", langCode));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeBoundViz(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 濠电姷鏁告慨鐑姐€傞挊澹╋綁宕ㄩ弶鎴狅紱闂侀€炲苯澧撮柡灞剧〒閳ь剨缍嗛崑鍛暦瀹€鍕厸鐎光偓鐎ｎ剛锛熸繛瀵稿婵″洭骞忛悩璇茬闁圭儤鍩堝銉モ攽閻樻鏆柍褜鍓欓崯璺ㄧ棯瑜旈弻鐔碱敊閻撳簶鍋撻幖浣瑰仼闁绘垼妫勫敮闂佸啿鎼崐鐟扳枍閸℃稒鈷戦柛蹇曞帶婢ь垶鏌涢妸锕€鈻曢挊婵喢归悡搴ｆ憼闁绘挾鍠栭獮鏍ㄦ綇閸撗咃紵闂佷紮缍嗛崣鍐蓟濞戙垹惟闁挎柨顫曟禒銏ゆ倵鐟欏嫭绀€闁圭⒈鍋婇崺銉﹀緞婵犲孩鍍甸梺鍛婎殘閸嬫稓鎲撮敂鎴掔箚闁绘劦浜滈埀顒佺墵閹兾旈崘銊︾€抽悗骞垮劚椤︻垶宕ヨぐ鎺撶厵闁绘垶锚濞?
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        // 闂傚倸鍊搁崐椋庣矆娓氣偓楠炲鏁撻悩鍐蹭画闂佹寧娲栭崐鎼佸垂閸岀偞鐓曠憸搴ㄣ€冮崨瀛樺€块柛顭戝亖娴滄粓鏌熸潏鍓хɑ缁绢叀鍩栭妵鍕晜閼测晝鏆ら梺鍝勭灱閸犳牕顕ｉ鍕ㄩ柕澶堝劗閹枫倗绱撻崒娆戭槮闁稿﹤婀遍弫顕€鍩勯崘褏绠氶梺鍓插亝濞叉牕顔忓┑鍥ヤ簻闁规崘娉涘瓭闁汇埄鍨遍幑鍥ь潖閾忚瀚氶柡灞诲労閳ь剚顨堢槐鎺楁偐閼碱儷褏鈧娲滈幊鎾跺弲濡炪倕绻愮€氼厼危椤掆偓閳规垿鎮欓弶鎴犱桓濠殿喗菧閸旀垵鐣?
        try {
            sendClientCommand(source, "areahint:boundviz_toggle");
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.boundary.visualization").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }
}


