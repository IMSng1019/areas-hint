package areahint.network;

import areahint.Areashint;
import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.file.FileManager;
import areahint.i18n.I18nManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛ě鍛帓缂傚倷绀侀ˇ顖氾耿鏉堚晜顫曢柟鎯х摠婵挳鏌涢敂璇插箻闁绘挴鍋撳┑锛勫亼閸娧囨嚈瑜版帒鐤鹃柣妯款嚙缁犳牠鏌涢锝嗙妤犵偑鍨虹换娑㈠幢濡桨鍒婂┑?
 * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛绀嬮柕濞垮劙婢规洖螖閻橀潧浠滈柣蹇旂箞閹﹢顢旈崼鐔哄幈闂佸啿鎼崐濠氬Υ閹烘梻纾兼い鏃囶潐濞呭﹥銇勯姀锛勨槈闁宠棄顦灒濞撴凹鍨变簺闂傚倸鍊峰ù鍥Υ閳ь剟鏌涚€ｎ偅宕岄柡灞诲€楃划娆戞崉閵娿倗椹崇紓鍌欒兌婵敻鎯勯鐐茶摕婵炴垯鍨圭猾宥夋煙閹冾暢闁挎稓鍠庨—鍐Χ閸涱喚顩伴梺鍛婃尵閸犲酣顢氶敐澶樻晝闁挎棁妫勬禍閬嶆⒑閸撴彃浜濈紒璇插婵?
 */
public class ClientNetworking {
    /**
     * 闂傚倸鍊风粈渚€骞夐敍鍕殰婵°倕鍟畷鏌ユ煕瀹€鈧崕鎴犵礊閺嶎厽鐓欓柣妤€鐗婄欢鑼磼閳ь剙鐣濋崟顒傚幐閻庤鎼╅崰鏍嚐椤栫偛绠栭柛娑樼摠閳锋垿鎮峰▎蹇擃仾閻忓骏闄勯妵鍕Ψ閿旇棄纾冲銈冨灪濡啴銆佸Δ鍛＜婵犲﹤鎷戠槐鎾⒒娴ｇ儤鍤€妞ゆ洦鍘介幈銊╂倻閽樺顔夐梺鍝勬川閸庢劙鎮㈤崱娑欏仯闁告繂瀚幆鍫熴亜韫囷絽浜伴柡?
     */
    public static void init() {
        AreashintClient.LOGGER.info("Initializing client networking");
        
        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐寸箾鐎电孝妞ゆ垵鎳愮划鍫濈暆閸曨剛鍘卞┑鐘绘涧濡顢旈锔界厱婵☆垰鍚嬮弳顒勬煛鐏炵澧查柟宄版嚇瀹曨偊宕熼崹顐ｇ様闂佽姘﹂～澶愬箰妞嬪孩顐芥慨妯哄瀹撲線鏌熼悜姗嗘當閸烆垶姊洪幐搴㈩梿濠殿喓鍊曡闁糕剝顭囩弧鈧梺闈涢獜缁插墽娑垫ィ鍐╁€垫慨妯煎帶濞呭秵顨ラ悙鎻掓殲缂佺粯绻堝畷鎯邦樄婵?
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_AREA_DATA),
                ClientNetworking::handleAreaData
        );
        
        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐烘⒑娴兼瑧鍒板璺烘喘瀹曟垿骞橀懡銈呯ウ闂佸壊鐓堥崰姘妤ｅ啯鈷戠紒顖涙礃濞呭棛绱掔€ｎ偅灏甸柛鎺撳浮閸╋繝宕担瑙勬珫婵犳鍠楅敃鈺呭储閻撳簶鏋旈柕澶涘缁犻箖鎮楅悽娈跨劸鐞氥劑姊虹粙鍖℃敾闁绘绮撳顐︻敋閳ь剟鐛鈧、娆撴嚃閳轰焦鎲㈤梻浣藉吹婵儳顩奸妸褎濯伴柨鏇楀亾閸楅亶姊洪鈧粔鐢告偂閻斿吋鐓熼柟閭﹀墮缁狙勩亜閵夛妇鐭嬬紒?
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_CLIENT_COMMAND),
                ClientNetworking::handleClientCommand
        );
        
        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐烘⒑娴兼瑧鍒伴柡鍫墰閳ь剚鍑归崜鐔煎蓟濞戙垹绠涢梻鍫熺☉缁犺鈹戦垾鍐茬骇闁诡喖鍊垮璇测槈濡攱顫嶅┑鐐叉缁绘帗绂嶆导瀛樷拺缁绢厼鎳愰崼顏堟煕婵犲啰绠為柣娑卞櫍楠炴帒螖閳ь剙鏁梻浣瑰濡焦鎱ㄩ妶鍛呮帡宕卞Ο鑲╃槇闂侀潧楠忕徊鍓ф兜妤ｅ啯鍊垫慨妯煎帶濞呭秵顨ラ悙鎻掓殲缂佺粯绻堝畷鎯邦樄婵?
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_DEBUG_COMMAND),
                ClientNetworking::handleDebugCommand
        );
        
        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐电磽娴ｅ摜婀撮柍褜鍓欓幗绉搇or闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑缂備胶濮垫繛濠囧蓟閺囩喎绶炴繛鎴欏灪椤庡秹姊洪崫鍕紞濞存粍绮撻垾锔炬崉閵婏箑纾梺鍛婄箓鐎氶攱瀵奸埀顒勬⒒娴ｇ瓔鍤冮柛鐘崇墵閺佸啴顢旈崟顓熸?
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_RECOLOR_RESPONSE,
                ClientNetworking::handleRecolorResponse
        );

        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐电磽娴ｅ摜婀撮柍褜鍓欓弨鐮e闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑缂備胶濮垫繛濠囧蓟閺囩喎绶炴繛鎴欏灪椤庡秹姊洪崫鍕紞濞存粍绮撻垾锔炬崉閵婏箑纾梺鍛婄箓鐎氶攱瀵奸埀顒勬⒒娴ｇ瓔鍤冮柛鐘崇墵閺佸啴顢旈崟顓熸闂佽鍎艰闁逞屽墾缁犳捇鐛幒妤€绫嶉柛灞诲€栬ⅸ闂?RenameNetworking 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛鐐婇柕濠忛檮閻?
        areahint.rename.RenameNetworking.registerClientReceivers();

        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐电磽娴ｇ懓绲婚柕鍫㈢毆High闂傚倸鍊烽懗鍫曞磿閻㈢鐤炬繝闈涱儌閳ь剨绠撳畷濂稿Ψ閵壯呭幀闁荤喐绮嶅Λ鍐嵁閸℃稑绀冩い鏃囧亹椤︽澘顪冮妶鍡樺暗濠殿噣鏀遍弲銉х磽閸屾艾鈧悂宕愰悜鑺ュ€块柨鏃€鍎抽崹婵堚偓鍏夊亾闁告洖锕ゅú顓㈠春閿熺姴宸濇い鏃堟？閸濇姊绘担铏广€婇柛鎾寸箞閵嗗啴宕卞☉娆忎户?
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_SETHIGH_AREA_LIST,
                ClientNetworking::handleSetHighAreaList
        );
        
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_SETHIGH_AREA_SELECTION,
                ClientNetworking::handleSetHighAreaSelection
        );
        
        ClientPlayNetworking.registerGlobalReceiver(
                Packets.S2C_SETHIGH_RESPONSE,
                ClientNetworking::handleSetHighResponse
        );
        
        // 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗鍏夊亾闁告洦鍋夐崺鐐烘⒑娴兼瑧鍒板璺烘喘瀹曟垿骞橀懡銈呯ウ闂佸壊鐓堥崰姘妤ｅ啯鈷戠紒顖涙礃濞呭棛绱掔€ｎ偅灏甸柛鎺撳浮閸╋繝宕担瑙勬珫婵犳鍠楅敃鈺呭储閻撳簶鏋旈柕澶涘缁犻箖鎮楅悽娈跨劸鐞氥劑姊虹粙鍖℃敾婵炶尙鍠曞Λ鐔兼⒑閸愬弶鎯堥柟鍐茬箻瀹曟垿鎮╃紒妯煎幈闂佹枼鏅涢崰姘枔閺囥垺鐓?
        ClientPlayNetworking.registerGlobalReceiver(
                new Identifier(Packets.S2C_CLIENT_COMMAND),
                ClientNetworking::handleClientCommand
        );

        // 闂傚倷绀侀幖顐λ囬锕€鐤炬繝闈涱儏绾惧鏌ｉ幇顒備粵闁哄棙绮撻弻鐔虹磼閵忕姵鐏堥柣搴㈣壘椤﹂亶鍩€椤掆偓缁犲秹宕曢柆宥呯疇閹兼惌鐓夌紞鏍煏閸繍妲归柣鎾存礋閺屻劌鈹戦崱妤佹婵犲痉銈呅撳ǎ鍥э躬閹瑩骞撻幒鍡椾壕闁割煈鍠氶弳锕傛煏婢诡垰鍊归崟鍐磽閸屾瑩妾烽柛銊潐娣囧﹪宕￠悙鈺傛杸闂佸疇妫勫Λ妤呮倶閼碱剛纾奸弶鍫涘妽缁€鍫ユ煕閹烘挸绗掓い鎾炽偢瀹曞爼濡搁妷褍閰卞┑锛勫亼閸婃牠骞愰弶鎴€剁憸鏂款嚕閹间礁宸濋悗娑櫭禒顖涚箾鏉堝墽鎮兼い顓炵墦閸╂盯寮崼鐔哄幗?
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register(
            (handler, sender, client) -> sendLanguageToServer()
        );
    }

    /** 闂傚倷娴囬褏鎹㈤幇顔藉床闁归偊鍎靛☉妯滄棃宕担瑙勬珝闁荤喐绮庢晶妤冩暜閹烘梻涓嶉柨婵嗘媼濞撳鎮楅敐搴濈盎闁伙负鍔岄湁闁绘挸楠搁埀顒冾潐缁岃鲸绻濋崶鑸垫櫖闂侀潧鐗嗗ú銊╂晬閻旇櫣纾藉〒姘搐閺嬫稓绱掓径濠勫煟妤犵偛绻橀幃鈺伱虹紒妯绘珝闂備焦濞婇弫顕€宕戦幘缁樼厸閻庯綆浜濋崵鍥煛鐏炲墽鈽夋い顐ｇ箞椤㈡鎷呯憴鍕伆濠碉紕鍋戦崐鏍暜閻愬搫纾婚柣鏃傚劋椤洟鏌熼悜妯虹亶闁哄閰ｉ弻鐔兼焽閿曗偓楠炴﹢鏌ｉ妶鍌氫壕闂傚倸鍊风粈渚€骞夐垾婢勬盯骞嬮悩鎰佹綗闂佸搫娲㈤崺鍕极?*/
    public static void sendLanguageToServer() {
        try {
            if (ClientPlayNetworking.canSend(Packets.C2S_LANGUAGE_SYNC)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(I18nManager.getCurrentLanguage());
                ClientPlayNetworking.send(Packets.C2S_LANGUAGE_SYNC, buf);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("Failed to sync language: " + e.getMessage());
        }
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛鐐婄憸蹇涱敊婵犲洦鈷戦梻鍫熶緱濡狙囨⒒閸曨偄顏€殿喗濞婇、鏃堝醇閻斿搫骞堥梻浣告贡閸庛倝骞愰幖浣€澶屸偓锝庡亞缁犻箖鏌涢銈呮瀻闁诲繐顕埀顒冾潐濞叉粍绻涢埀顒傗偓瑙勬处閸嬪﹤鐣烽悢纰辨晣闁绘劘灏欓濂告⒒閸屾瑦绁版俊妞煎姂閹虫鎳滈崹顐ｇ彿濠德板€愰崑鎾绘煟?
     * @param client Minecraft闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛濡囧▓銈夋⒑閸濆嫷妲兼俊顐㈠婵?
     * @param handler 缂傚倸鍊搁崐鎼佸磹閹间礁鐤い鏍仜閸ㄥ倿鏌涢敂璇插箹闁搞劍绻堥弻銈夊箹娴ｈ閿紓浣稿閸嬨倝寮诲☉銏犲嵆闁靛鍎辩粻娲⒑缁嬪尅鍔熼柛瀣ㄥ€濆?
     * @param buf 闂傚倸鍊峰ù鍥ь浖閵娾晜鍤勯柤绋跨仛濞呯姵淇婇妶鍌氫壕闂佷紮绲介悘姘辩箔閻旂厧鐒垫い鎺嗗亾闁伙絿鍏橀幃鈺冩嫚閹绘帞鐛╂俊鐐€栭悧妤冨垝瀹€鍕婂洭鍩￠崨顔规嫼闂佸憡绋戦敃銈嗘叏閸垺鍠愰柡澶婄仢閺嗙喎霉?
     * @param responseSender 闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑闂佹椿鍘介〃濠囧蓟閻斿憡缍囬柟瑙勫姇閹懘姊虹粙娆惧剱闁圭顭烽獮蹇涘川閺夋垵绐涙繝鐢靛Т鐎氼剛绱?
     */
    private static void handleAreaData(MinecraftClient client, 
                                      ClientPlayNetworkHandler handler,
                                      PacketByteBuf buf, 
                                      PacketSender responseSender) {
        // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€為梺鍐叉惈閸熶即鏁嶅鍫熲拺闁告繂瀚峰Σ褰掓煕閵娧勬毈妞ゃ垺鐟︾换婵嬪炊閵娧冨箞婵犳鍠楅敃鈺呭礈濞戞粌鈧挳鏌ｉ悙鏉戞惛闁告梹鍨垮璇测槈濮楀棛鍙嗛梺鍛婂姀閺呮粓骞忛柆宥嗏拺闁告繂瀚烽崕蹇涙煕閻曚礁鐏ｇ紒顔碱儔楠炴帡寮埀顒€鐣锋径鎰厱婵犻潧妫楅顏堟煛閸℃劕鍔﹂柡?
        String dimensionName = buf.readString();
        String fileContent = buf.readString();
        
        // 缂傚倸鍊烽懗鍫曟惞鎼淬劌鐭楅幖娣妼缁愭鏌￠崶鈺佇ｇ€规洖寮堕幈銊ノ熼幐搴ｃ€愰梺鍦嚀閻栧ジ寮婚弴鐔虹闁割煈鍠掗崑鎾诲即閵忊€冲墾濠电姴锕ら幊鎰婵傚憡鐓欓悷娆忓婵牏鐥鐐靛煟闁哄苯绉堕幏鐘诲箵閹烘挸鈧垳绱撴担鍓插剰妞わ妇鏁婚妴浣割潨閳ь剟骞冨鍫濆耿婵☆垳鍋熼崰鏍ь潖?
        client.execute(() -> {
            try {
                // 缂傚倸鍊烽懗鍫曟惞鎼淬劌鐭楅幖娣妼缁愭鏌″搴″箺闁稿鏅涜灃闁挎繂鎳庨弳娆愪繆椤愶綇鑰块柡灞剧洴婵＄兘顢涢悙鎼偓宥咁渻閵堝棗濮冪紒顔界懇瀵?
                String fileName = Packets.getFileNameForDimension(dimensionName);
                if (fileName == null) {
                    AreashintClient.LOGGER.warn("闂傚倸鍊峰ù鍥綖婢跺顩插ù鐘差儏绾惧潡鏌＄仦璇插姎闁哄鑳堕幉鎼佹偋閸繄鐟ㄧ紓浣插亾闁糕剝绋掗悡鐔镐繆閵堝倸浜鹃梺缁橆殔閿曘倝顢氶敐澶婄缂備焦顭囬崢钘夘渻閵堝棙灏扮紒顔肩焸瀹曟繈濡堕崱娆戭啎闂佹寧绻傛鎼佸几閻斿吋鐓欐い鏍ㄧ⊕椤ュ牓鏌℃担绋库偓鍧楀箖濞嗘搩鏁嗛柛灞剧⊕椤斿繘姊婚崒娆戭槮闁硅绻濋幃褔寮撮姀鐘殿啇濡炪倖鍔ч梽鍕磻閸屾稓绠鹃柛鈩兩戠亸顓㈡煛鐎ｂ晝绐旈柡宀嬬到铻栭柍褜鍓熼幃褔宕卞▎鎴犵劸? " + dimensionName);
                    return;
                }
                
                // 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╁┑鈽嗗亝閿曘垽寮诲☉銏犖ㄦい鏍仦椤庡秴顪冮妶鍡楀缂侇喗鐟╅獮鍐喆閸曨厾鎳濆銈嗙墬濮樸劍绂掗埡鍐＝?
                Path filePath = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
                AreashintClient.LOGGER.info("[闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ噣鏌熼幆鏉啃撻柛搴★躬閺?闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛ě鍕珮缂傚倷鑳舵慨宕囧垝濞嗘挸绠栨俊銈傚亾闁宠棄顦埢宥夘敇瑜岀花钘壝瑰鍕€愭い銏＄洴閹瑩宕ｆ径瀣撴岸姊绘担铏瑰笡闁哄被鍔戦獮澶愬灳閺傘儲鐏侀梺鍓插亝濞叉﹢鎮″▎鎾村仯闁搞儻绲洪崑鎾绘惞椤愩倓閭梻鍌欒兌绾爼寮插☉銏″剹闁稿本鍑归崵鏇熴亜閹烘垵顏╅柣鎰躬閺屻劑寮崒婊勬啒闂? " + filePath.toAbsolutePath());
                
                // 缂傚倸鍊烽懗鍫曟惞鎼淬劌鐭楅幖娣妼缁愭鏌￠崶鈺佇ｇ€规洖寮堕幈銊ノ熼崹顔惧帿闂佺楠哥€涒晠濡甸崟顖氱睄闁搞儜鍌涚潖缂傚倷绀侀ˇ顖氼焽閿熺姴绠栨俊銈傚亾闁宠棄顦埢宥夘敇瑜岀花濠氭煥?
                FileManager.checkFolderExist();
                
                // 闂傚倸鍊风粈渚€骞夐敓鐘茬闁哄稁鍘介崑锟犳煏婢跺棙娅呴柣顓燁殜閺屾盯鍩勯崘顏佹濠碘槅鍋呴敃銏ゅ蓟濞戙垹唯妞ゆ牜鍋為宥咁渻?
                Files.writeString(filePath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                
                AreashintClient.LOGGER.info("Saved area data for " + dimensionName);
                AreashintClient.LOGGER.info("[debug] saved file length: " + fileContent.length());
                if (fileContent.length() < 100) {
                    AreashintClient.LOGGER.info("[闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ噣鏌熼幆鏉啃撻柛搴★躬閺?闂傚倸鍊风粈渚€骞夐敓鐘冲亱闁哄洢鍨圭粻瑙勩亜閹板爼妾柛瀣€圭换娑㈠幢濡纰嶉梺鍝勵儎缁舵岸寮婚敐鍛傛棃鍩€椤掑嫭鍋嬮柛鈩冪懅閻牓鏌ㄩ弴鐐测偓褰掓偂閺囥垺鐓欓梺顓ㄧ畱婢ь垶鏌涢敐鍫綈闁靛洤瀚板顕€鍩€椤掑嫬绀夐柡宥庡幗閺咁剟鏌熼悧鍫熺凡缂佲偓鐎ｎ偁浜滈柡鍥╁仦閸ｈ櫣绱? " + fileContent);
                } else {
                    AreashintClient.LOGGER.info("[闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ噣鏌熼幆鏉啃撻柛搴★躬閺?闂傚倸鍊风粈渚€骞夐敓鐘冲亱闁哄洢鍨圭粻瑙勩亜閹板爼妾柛瀣€圭换娑㈠幢濡纰嶉梺鍝勵儎缁舵岸寮婚敐鍛傛棃鍩€椤掑嫭鍋嬮柛鈩冪懅閻牓鏌ㄩ弴鐐测偓褰掓偂閺囥垺鐓欓梺顓ㄧ畱婢ь垶鏌涢敐鍫綈闁靛洤瀚板顕€鍩€椤掑嫬绀夐柡宥庡幗閺咁剟鏌熼悧鍫熺凡缂佲偓鐎ｎ偁浜滈柡鍥╁仦閸ｈ櫣绱? " + fileContent.substring(0, 100) + "...");
                }
                
                // 濠电姷鏁告慨鐑姐€傛禒瀣劦妞ゆ巻鍋撻柛鐔锋健閸┾偓妞ゆ巻鍋撶紓宥咃躬楠炲啫螣鐠囪尙绐為柟鐓庣摠缁嬫劕效濡ゅ懏鈷戦悷娆忓閸斻倝鏌涢悢閿嬪仴鐠侯垶鏌涘☉妯兼憼闁绘挻娲熼弻銊╁棘閹稿孩鍎撴繝娈垮枓閸嬫挻淇婇悙顏勨偓鏍哄Ο渚僵闁靛ě鍕垫綗闂佸湱鍎ら〃鍛棯瑜旈弻鐔衡偓娑欘焽缁犮儵鏌涢悙闈涘妺缂佺粯鐩獮瀣倷鐎涙ê鍓梻浣告啞閻熴儳鎹㈤幒鎳筹綁骞囬鑺ユ杸闁诲函缍嗘禍鐐核囬鈶╂斀闁绘劕寮堕ˉ鐐烘煙閸濄儺鐒鹃柍璇茬Ч婵偓闁靛牆妫涢崢鎼佹⒑缁嬫寧婀扮紒瀣笧濞嗐垽骞撻幒鍡樻杸濡炪倖姊归崕铏閿旂晫绠剧痪鏉垮綁闁垶鏌熺粙鍖℃敾鐎垫澘瀚换婵嬪炊椤垵鏁梻鍌氬€烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭鏌熼悧鍫熺凡婵鐓￠弻锝夊籍閸屾艾浠樼紓?
                if (client.world != null && 
                        dimensionName.equals(Packets.convertDimensionPathToType(client.world.getRegistryKey().getValue().getPath()))) {
                    AreashintClient.LOGGER.info("[闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ噣鏌熼幆鏉啃撻柛搴★躬閺?闂傚倸鍊搁崐鐑芥倿閿曚降浜归柛鎰典簽閻捇鏌ｉ姀銏╃劸闁藉啰鍠庨埞鎴︽偐閸欏鎮欑紓浣插亾濠㈣埖鍔栭悡鐔镐繆椤栨粌甯堕柛鏂款儑缁辨帗鎷呴悷閭︽闂佺懓寮堕幃鍌炲箖瑜斿畷濂告偄妞嬪寒鏆℃繝鐢靛Л閹峰啴宕ㄩ澶堝劤缁辨帗娼忛妸銉﹁癁閻庢鍠栭悥濂哥嵁鐎ｎ亖鏀介柟閭﹀墯閺嗩垶姊婚崒娆戝妽闁活亜缍婂畷婵嗙暆閳ь剟鍩€椤掍礁鍤柛鎾寸⊕缁傚秹骞栨笟鍥ㄦ櫖闂佺粯鍔曞璺何ｉ鍕拺缂備焦蓱閻撱儵鏌熷ù瀣у亾閺傘儲鐏侀梺鍓插亝濞叉﹢鎮? " + fileName);
                    AreashintClient.getAreaDetector().loadAreaData(fileName);
                    areahint.boundviz.BoundVizManager.getInstance().reload();
                }
                
            } catch (IOException e) {
                AreashintClient.LOGGER.error("濠电姷鏁搁崕鎴犲緤閽樺娲晜閻愵剙搴婇梺绋跨灱閸嬬偤宕戦妶澶嬬厪濠电偟鍋撳▍鍛磼閻樺啿鈻曢柡灞炬礃瀵板嫬鈽夐姀鈽嗏偓宥夋⒑閸濆嫭锛嶅ù婊庝邯瀵鈽夐姀鐘殿唺闂佸湱鍋撻崜姘涢崱妞绘斀闁绘劘灏欐晶娑㈡煕閵娿儳浠㈤柣锝囧厴閹晝鎷犻煫顓犵倞闂備礁鎲″ú锕傚储婵傚摜宓佺€广儱顦伴埛鎴﹀级閻愭潙顎滈柛蹇撹嫰椤儻顦虫い銊ユ嚇閹箖宕归銈囨嚌闂侀€炲苯澧い鏇秮椤㈡瑩鎮欓鈧壕顖炴⒑閸涘﹦绠撻悗姘煎弮閹線寮崼鐔叉嫽? " + e.getMessage());
            }
        });
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛鐐婄憸蹇涱敊婵犲洦鈷戦梻鍫熶緱濡狙囨⒒閸曨偄顏€殿喗濞婇、鏃堝醇閻斿搫骞堥梻浣告贡閸庛倝骞愰幖浣€澶屸偓锝庡亞缁犻箖鏌涢銈呮瀻闁诲繆鏅犻弻宥堫檨闁告挻鐟х划璇差吋婢跺﹤鐎┑鐐叉▕娴滄粓寮伴妷锔剧闁瑰鍋為惃鎴︽煕濡や胶顣查柕鍥у瀵粙濡歌閺嗭繝姊洪幖鐐测偓鏍暜閹烘绠?
     * @param client Minecraft闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛濡囧▓銈夋⒑閸濆嫷妲兼俊顐㈠婵?
     * @param handler 缂傚倸鍊搁崐鎼佸磹閹间礁鐤い鏍仜閸ㄥ倿鏌涢敂璇插箹闁搞劍绻堥弻銈夊箹娴ｈ閿紓浣稿閸嬨倝寮诲☉銏犲嵆闁靛鍎辩粻娲⒑缁嬪尅鍔熼柛瀣ㄥ€濆?
     * @param buf 闂傚倸鍊峰ù鍥ь浖閵娾晜鍤勯柤绋跨仛濞呯姵淇婇妶鍌氫壕闂佷紮绲介悘姘辩箔閻旂厧鐒垫い鎺嗗亾闁伙絿鍏橀幃鈺冩嫚閹绘帞鐛╂俊鐐€栭悧妤冨垝瀹€鍕婂洭鍩￠崨顔规嫼闂佸憡绋戦敃銈嗘叏閸垺鍠愰柡澶婄仢閺嗙喎霉?
     * @param responseSender 闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑闂佹椿鍘介〃濠囧蓟閻斿憡缍囬柟瑙勫姇閹懘姊虹粙娆惧剱闁圭顭烽獮蹇涘川閺夋垵绐涙繝鐢靛Т鐎氼剛绱?
     */
    private static void handleClientCommand(MinecraftClient client, 
                                          ClientPlayNetworkHandler handler,
                                          PacketByteBuf buf, 
                                          PacketSender responseSender) {
        // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈嗗坊閸嬫挾鈧懓鎲＄换鍫ュ蓟閻旇　鍋撳☉娅亞绮顑芥斀?
        String command = buf.readString();
        
        // 缂傚倸鍊烽懗鍫曟惞鎼淬劌鐭楅幖娣妼缁愭鏌￠崶鈺佇ｇ€规洖寮堕幈銊ノ熼幐搴ｃ€愰梺鍦嚀閻栧ジ寮婚弴鐔虹闁割煈鍠掗崑鎾诲即閵忊€冲墾濠电姴锕ら幊鎰婵傚憡鐓欓悷娆忓婵牏鐥鐐靛煟闁哄苯绉堕幏鐘诲箵閹烘挸鈧垳绱撴担鍓插剰妞わ妇鏁婚妴浣割潨閳ь剟骞冨鍫濆耿婵☆垳鍋熼崰鏍ь潖?
        client.execute(() -> {
            try {
                AreashintClient.LOGGER.info("闂傚倸鍊峰ù鍥綖婢跺顩插ù鐘差儏绾惧潡鏌＄仦璇插姎闁哄鑳堕幉鎼佹偋閸繄鐟ㄧ紓浣插亾闁糕剝绋掗悡鐔镐繆閵堝倸浜鹃梺缁橆殔閿曪妇鍒掔拠娴嬫闁靛骏绱曢崢閬嶆⒑閸濆嫬鏆為柟绋垮⒔婢规洟骞愭惔娑楃盎闂婎偄娴勭徊鑺ユ櫠鐎电硶鍋撶憴鍕８闁告梹鍨块悰顔锯偓锝庡枛缁犳娊鏌熼悙顒€顥? " + command);
                
                // 闂傚倷娴囧畷鐢稿窗閹扮増鍋￠弶鍫氭櫅缁躲倕螖閿濆懎鏆為柛濠囨涧闇夐柣妯烘▕閸庡繒鈧懓鎲＄换鍫ュ蓟閻旇　鍋撳☉娅亞绮顑芥斀妞ゆ柨鎼悘鏌ユ煛鐏炶濡奸柍钘夘槸铻ｉ柣鎾冲瘨閺嗩偊鏌ｉ悢鍝ョ煁濠碘剝鎮傚畷鎯邦槼鐎规挸绉瑰娲濞淬倖绋撴禒锕傛寠婢跺妫庡┑鐘垫暩閸嬬偤宕归崼鏇炵闁圭虎鍠栫壕濠氭煙閻愵剚鐏辨俊鎻掔墦閺岀喖宕归鍏兼缂備降鍔岄…鐑藉蓟濞戙垺鍋勫┑鍌氼槸閸撳爼鎮楃憴鍕┛缂佽弓绮欓幃楣冩晸閻樺磭鍔烽梺鎸庢煥瑜般劑鎮㈤崗灏栨嫼闂佸憡绋戦敃銈囩箔閹烘挷绻嗘い鎰剁秵濞堟洜绱掗弮鍌氭瀾缂佺粯绻堝畷鍫曞Ω閿旂晫褰ㄩ梻浣筋嚙缁绘帡宕戦悩璇茬；闁归偊鍠氭稉宥吤归悩宸剱闁绘挾鍠栭弻銊モ攽閸℃﹩妫ら梺鐟板暱閸熶即鍩€椤掍緡鍟忛柛锝庡櫍瀹曟粓鎮㈡總澶婃濡炪倖鍔戦崐鏍汲濠婂嫨浜滈柡鍌氱仢閳锋梹銇?
                int firstColonIndex = command.indexOf(":");
                if (firstColonIndex == -1) {
                    return;
                }
                
                String type = command.substring(0, firstColonIndex);
                String action = command.substring(firstColonIndex + 1);
                
                // 闂傚倸鍊风粈渚€骞栭銈囩煋闁绘垶鏋荤紞鏍ь熆鐠虹尨鍔熼柡鍡愬€曢妴鎺戭潩閿濆懍澹曢柣搴ゎ潐濞插繘宕濋幋锔惧祦閻庯綆鍠栫粻鎶芥煙閻愵剙顥為柟瀵稿Х绾捐棄銆掑顒佹悙濞存粠鍨遍妵鍕即閸℃澶勯柛濠勬暬濮婃椽宕归鍛壈缂備礁澧庨崑銈夊蓟濞戙垹鍗抽柕濞垮劚缁犳椽姊?
                if (type.equals("areahint")) {
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤绠炵紒娑氼潣ad闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    if (action.equals("reload")) {
                        AreashintClient.reload();
                    } 
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦閻ｅ瞼灏电紒鏃€浠ency闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("frequency")) {
                        if (action.equals("frequency_info")) {
                            displayFrequencyInfo(client);
                        } else {
                            try {
                                String[] frequencyParts = action.split(" ");
                                if (frequencyParts.length >= 2) {
                                    int value = Integer.parseInt(frequencyParts[1]);
                                    ClientConfig.setFrequency(value);
                                    AreashintClient.reload();
                                }
                            } catch (NumberFormatException e) {
                                AreashintClient.LOGGER.error("闂傚倷娴囧畷鐢稿窗閹扮増鍋￠弶鍫氭櫅缁躲倕螖閿濆懎鏆為柛濠勬暬閺屻倝骞侀幒鎴濆Х缂備焦鍞荤紞渚€寮婚悢琛″亾閻㈡鐒剧€涙繈姊洪懖鈺佸妺濠电偛锕璇测槈濞嗘垹鐦堥梺绋挎湰绾板秹宕愰姘ｆ斀闁挎稑瀚禒婊堟煕婵犲倻浠㈤柣锝呭槻閳诲骸顕ュ┑鍥ㄣ仢妞ゃ垺妫冨畷銊╊敊閻ｅ苯缍? " + e.getMessage());
                            }
                        }
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤缁╂慨濠冨敻itlerender闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("subtitlerender")) {
                        if (action.equals("subtitlerender_info")) {
                            displaySubtitleRenderInfo(client);
                        } else {
                            String[] renderParts = action.split(" ");
                            if (renderParts.length >= 2) {
                                ClientConfig.setSubtitleRender(renderParts[1]);
                                AreashintClient.reload();
                            }
                        }
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤缁╂慨濠冨敻itlestyle闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("subtitlestyle")) {
                        handleSubtitleStyleCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤缁╂慨濠冨敻itlesize闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("subtitlesize")) {
                        handleSubtitleSizeCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纭€闁哥偞娼続dd闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("easyadd")) {
                        handleEasyAddCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍌ㄥ☉鏃傜彍ndArea闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("expandarea")) {
                        handleEasyAddCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁伙絽鏀╪kArea闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("shrinkarea")) {
                        handleEasyAddCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呯deArea闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("dividearea")) {
                        handleDivideAreaCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖氼嚗閸曨厾绀婄紒铏瑰劧int闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("addhint")) {
                        handleEasyAddCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾芥い褏鐏僼eHint闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("deletehint")) {
                        handleEasyAddCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟杈╊潣lor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("recolor")) {
                        handleRecolorCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呮倯ame闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("dimname")) {
                        handleDimNameCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呭編olor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("dimcolor")) {
                        handleDimColorCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟韫箻me闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("rename")) {
                        handleRenameCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾芥い褏鐏僼e闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("delete")) {
                        handleDeleteCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟鎻掑煇aceButton闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("replacebutton")) {
                        handleReplaceButtonCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲绠电痪顓濆uage闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("language")) {
                        handleLanguageCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖氼嚗閸曨厾绱﹂柣姘卞崟dViz闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("boundviz")) {
                        handleBoundVizCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛纾婚柣锝呰嫰閺嬫盯鏌℃担鍝バх€规洜鍠栭、姗€鎮╅棃娑樼瑨闂備浇顕х€涒晠顢欓弽顓炵獥闁圭儤顨呯壕濠氭煙閸撗呭笡闁绘挻娲熼悡顐﹀炊閵婏箑纰嶉梺鍝勬娴滃爼寮婚悢鐓庢闁靛鍎遍埛澶岀磽?
                    else if (action.equals("on") || action.equals("off")) {
                        areahint.command.ModToggleCommand.handleToggleCommand(action);
                    }
                    // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
                    else if (action.startsWith("sethigh")) {
                        handleSetHighCommand(action);
                    }
                }
            } catch (Exception e) {
                AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛绀嬮柕濞垮劙婢规洖螖閻橀潧浠滈柣蹇旂箞閹﹢顢旈崼鐔哄幈闂佸啿鎼崐濠氬Υ閹烘梻纾兼い鏃囶潐濞呭﹥銇勯姀锛勨槈妞ゎ偅绻堥、姗€鎮欓幖顓炴倯濠电姷鏁搁崑娑㈩敋椤撶喐鍙忓ù鍏兼綑绾惧綊鏌″畵顔兼湰鐎靛矂姊虹粙璺ㄧ伇闁稿绋撶划鍫ュ礋椤掑倻顔曢悗鐟板閸犳洜鑺辨總鍛婄厸? " + e.getMessage());
            }
        });
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纭€闁哥偞娼続dd闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleEasyAddCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纭€闁哥偞娼続dd闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.easyadd.EasyAddManager manager = areahint.easyadd.EasyAddManager.getInstance();
            
            if (action.equals("easyadd_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_start");
                manager.startEasyAdd();
            } else if (action.equals("expandarea_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀伇pandarea_start");
                System.out.println("DEBUG: 闂備浇顕х€涒晠顢欓弽顓炵獥闁圭儤顨呯壕濠氭煙閻愵剚鐏遍柡鈧懞銉ｄ簻闁哄啫鍊甸幏锟犳煕鎼淬垹濮囨い顓″劵椤︽挳鏌￠崪浣镐喊妤?expandarea_start");
                areahint.expandarea.ExpandAreaManager.getInstance().startExpandArea();
                System.out.println("DEBUG: expandarea_start completed");
            } else if (action.startsWith("expandarea_select:")) {
                String areaName = action.substring("expandarea_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀伇pandarea_select: " + areaName);
                areahint.expandarea.ExpandAreaManager.getInstance().selectAreaByName(areaName);
            } else if (action.equals("expandarea_continue")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀伇pandarea_continue");
                areahint.expandarea.ExpandAreaManager.getInstance().continueRecording();
            } else if (action.equals("expandarea_save")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀伇pandarea_save");
                areahint.expandarea.ExpandAreaManager.getInstance().finishAndSave();
            } else if (action.equals("expandarea_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀伇pandarea_cancel");
                areahint.expandarea.ExpandAreaManager.getInstance().cancel();
            } else if (action.equals("shrinkarea_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槑rinkarea_start");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().start();
            } else if (action.startsWith("shrinkarea_select:")) {
                String areaName = action.substring("shrinkarea_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槑rinkarea_select: " + areaName);
                areahint.shrinkarea.ShrinkAreaManager.getInstance().selectAreaByName(areaName);
            } else if (action.equals("shrinkarea_continue")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槑rinkarea_continue");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().continueRecording();
            } else if (action.equals("shrinkarea_save")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槑rinkarea_save");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().finishAndSave();
            } else if (action.equals("shrinkarea_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槑rinkarea_cancel");
                areahint.shrinkarea.ShrinkAreaManager.getInstance().stop();
            } else if (action.equals("addhint_start")) {
                areahint.addhint.AddHintManager.getInstance().start();
            } else if (action.startsWith("addhint_select:")) {
                String areaName = action.substring("addhint_select:".length());
                areahint.addhint.AddHintManager.getInstance().selectArea(areaName);
            } else if (action.equals("addhint_submit")) {
                areahint.addhint.AddHintManager.getInstance().submit();
            } else if (action.equals("addhint_cancel")) {
                areahint.addhint.AddHintManager.getInstance().cancel();
            } else if (action.equals("deletehint_start")) {
                areahint.deletehint.DeleteHintManager.getInstance().start();
            } else if (action.startsWith("deletehint_select:")) {
                String areaName = action.substring("deletehint_select:".length());
                areahint.deletehint.DeleteHintManager.getInstance().selectArea(areaName);
            } else if (action.startsWith("deletehint_toggle:")) {
                int index = Integer.parseInt(action.substring("deletehint_toggle:".length()));
                areahint.deletehint.DeleteHintManager.getInstance().toggleVertex(index);
            } else if (action.equals("deletehint_submit")) {
                areahint.deletehint.DeleteHintManager.getInstance().submit();
            } else if (action.equals("deletehint_cancel")) {
                areahint.deletehint.DeleteHintManager.getInstance().cancel();
            } else if (action.equals("easyadd_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_cancel");
                manager.cancelEasyAdd();
            } else if (action.startsWith("easyadd_level:")) {
                String levelStr = action.substring("easyadd_level:".length());
                int level = Integer.parseInt(levelStr);
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_level: " + level);
                manager.handleLevelInput(level);
            } else if (action.startsWith("easyadd_base:")) {
                String baseName = action.substring("easyadd_base:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_base: " + baseName);
                manager.handleBaseSelection(baseName);
            } else if (action.equals("easyadd_continue")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_continue");
                // 缂傚倸鍊搁崐鎼佸磹妞嬪海绀婇柍褜鍓熼弻娑樷槈閸楃偟浠梺鍝勬閸楁娊寮婚敐澶嬪亹閺夊牃鏅涙俊钘夆攽閻橆喖鐏い顓炴川濡叉劙鎮欏顔兼倯婵犮垼娉涢鍛村礈椤撱垺鈷戦柛娑橈攻婢跺嫰鏌涢…鎴滈偗鐎规洘鍨块獮妯兼嫚閹绘帒鎸ら梻濠庡亜濞诧箑煤閺嶎偆鍗氬┑鍌氭啞閳锋垿姊婚崼姘珗缂佲偓鐎ｎ喗鐓曢柡鍌濇硶鑲栭梺閫炲苯澧柣蹇旀皑閸掓帡骞樼拠鑼舵憰闂侀潧艌閺呪晠寮繝鍕／闁瑰嘲鐭傞崫铏规偖閳哄懏鈷掑ù锝堟閸氬綊鏌涚€ｎ偆鈽夐摶鐐烘煛閸愩劎澧涢柛銈呯墦閺岀喐娼忔ィ鍐╊€嶉梺鍝勬媼閸撴盯鍩€椤掆偓閸樻粓宕戦幘缁樼厱闁规澘鍚€缁ㄨ姤銇勯弮鈧崝娆忣潖缂佹ɑ濯撮柣鐔稿缁侀攱绻濋棃娑樷偓鐟邦潖閼姐倕鍨濇い鎾跺枎缁剁偤鏌熼柇锕€寮炬慨濠傛健濮婃椽鏌呴悙鑼跺濠⒀傚嵆閺岀喖鎳犻鈧。濂告煙瀹勭増鍤囨慨濠傜秺楠炲洭顢氶埀顒勫煘韫囨稒鈷掑ù锝堟鐢稒銇勯妸銉█闁挎繄鍋炲鍕箛椤撶偘鎮ｉ梻浣稿暱閹碱偊宕愰崫銉︻偨闁绘劗鍎ら悡鏇㈡煛閸ャ儱濡兼鐐搭殜閺?
            } else if (action.equals("easyadd_finish")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_finish");
                manager.finishPointRecording();
                    } else if (action.equals("easyadd_save")) {
            AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_save");
            manager.confirmSave();
        } else if (action.equals("easyadd_altitude_auto")) {
            AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_altitude_auto");
            areahint.easyadd.EasyAddAltitudeManager.handleAltitudeTypeSelection(
                areahint.easyadd.EasyAddAltitudeManager.AltitudeType.AUTOMATIC);
        } else if (action.equals("easyadd_altitude_custom")) {
            AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_altitude_custom");
            areahint.easyadd.EasyAddAltitudeManager.handleAltitudeTypeSelection(
                areahint.easyadd.EasyAddAltitudeManager.AltitudeType.CUSTOM);
        } else if (action.equals("easyadd_altitude_unlimited")) {
            AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_altitude_unlimited");
            areahint.easyadd.EasyAddAltitudeManager.handleAltitudeTypeSelection(
                areahint.easyadd.EasyAddAltitudeManager.AltitudeType.UNLIMITED);
        } else if (action.startsWith("easyadd_color:")) {
            String colorHex = action.substring("easyadd_color:".length());
            try {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旀亞syadd_color: " + colorHex);
                manager.handleColorSelection(colorHex);
            } catch (Exception e) {
                AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛绀冮悹鎭掑妿閻撴垶绻濋悽闈浶㈤柨鏇樺€曡灋婵°倕鎯ゆ径搴ｇ杸婵炴垶鐟㈤幏鍝勵渻閵堝棗濮傞柛鈺佸鍗辩憸鐗堝笚閸婂灚鎱ㄥΟ绋垮姎濠殿喖鍊婚埀顒侇問閸ｎ噣宕板璺虹闁告洦鍨版导鐘绘煕閺囥劌澧版俊鍙夛耿濮? " + e.getMessage());
            }
        } else {
            AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偐閹绘帩妫僺yAdd闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
        }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纭€闁哥偞娼続dd闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟杈╊潣lor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleRecolorCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟杈╊潣lor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.recolor.RecolorManager manager = areahint.recolor.RecolorManager.getInstance();

            if (action.startsWith("recolor_select:")) {
                String areaName = action.substring("recolor_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪color_select: " + areaName);
                manager.handleAreaSelection(areaName);
            } else if (action.startsWith("recolor_color:")) {
                String colorValue = action.substring("recolor_color:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪color_color: " + colorValue);
                manager.handleColorSelection(colorValue);
            } else if (action.equals("recolor_confirm")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪color_confirm");
                manager.confirmChange();
            } else if (action.equals("recolor_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪color_cancel");
                manager.cancelRecolor();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偑閳ь剙螞娑旑湹lor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟杈╊潣lor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呮倯ame闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     */
    private static void handleDimNameCommand(String action) {
        try {
            areahint.dimensional.DimensionalNameUIManager mgr = areahint.dimensional.DimensionalNameUIManager.getInstance();
            if (action.equals("dimname_start")) {
                mgr.startDimName();
            } else if (action.startsWith("dimname_select:")) {
                mgr.handleDimNameSelect(action.substring("dimname_select:".length()));
            } else if (action.startsWith("dimname_name:")) {
                mgr.handleDimNameInput(action.substring("dimname_name:".length()));
            } else if (action.equals("dimname_confirm")) {
                mgr.confirmDimName();
            } else if (action.equals("dimname_cancel")) {
                mgr.cancel();
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呮倯ame闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呭編olor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     */
    private static void handleDimColorCommand(String action) {
        try {
            areahint.dimensional.DimensionalNameUIManager mgr = areahint.dimensional.DimensionalNameUIManager.getInstance();
            if (action.equals("dimcolor_start")) {
                mgr.startDimColor();
            } else if (action.startsWith("dimcolor_select:")) {
                mgr.handleDimColorSelect(action.substring("dimcolor_select:".length()));
            } else if (action.startsWith("dimcolor_color:")) {
                mgr.handleDimColorInput(action.substring("dimcolor_color:".length()));
            } else if (action.equals("dimcolor_confirm")) {
                mgr.confirmDimColor();
            } else if (action.equals("dimcolor_cancel")) {
                mgr.cancel();
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呭編olor闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟韫箻me闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleRenameCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟韫箻me闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.rename.RenameManager manager = areahint.rename.RenameManager.getInstance();

            if (action.startsWith("rename_select:")) {
                String areaName = action.substring("rename_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪name_select: " + areaName);
                manager.handleAreaSelection(areaName);
            } else if (action.equals("rename_confirm")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪name_confirm");
                manager.confirmRename();
            } else if (action.equals("rename_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪name_cancel");
                manager.cancelRename();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偑閳ь剙螞娑旂Ψme闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟韫箻me闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓缂侇偒娲慽tleStyle闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleSubtitleStyleCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓缂侇偒娲慽tleStyle闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.subtitlestyle.SubtitleStyleManager manager = areahint.subtitlestyle.SubtitleStyleManager.getInstance();

            if (action.equals("subtitlestyle_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槢btitlestyle_start");
                manager.startSubtitleStyleSelection();
            } else if (action.startsWith("subtitlestyle_select:")) {
                String style = action.substring("subtitlestyle_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槢btitlestyle_select: " + style);
                manager.handleStyleSelection(style);
            } else if (action.equals("subtitlestyle_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槢btitlestyle_cancel");
                manager.cancelSubtitleStyle();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偑閳ь剟鎮㈡穱鐖恑tleStyle闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓缂侇偒娲慽tleStyle闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓缂侇偒娲慽tleSize闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleSubtitleSizeCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓缂侇偒娲慽tleSize闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.subtitlesize.SubtitleSizeManager manager = areahint.subtitlesize.SubtitleSizeManager.getInstance();

            if (action.equals("subtitlesize_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槢btitlesize_start");
                manager.startSubtitleSizeSelection();
            } else if (action.startsWith("subtitlesize_select:")) {
                String size = action.substring("subtitlesize_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槢btitlesize_select: " + size);
                manager.handleSizeSelection(size);
            } else if (action.equals("subtitlesize_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑槢btitlesize_cancel");
                manager.cancelSubtitleSize();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偑閳ь剟鎮㈡穱鐖恑tleSize闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓缂侇偒娲慽tleSize闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾芥い褏鐏僼e闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleDivideAreaCommand(String action) {
        try {
            areahint.dividearea.DivideAreaManager mgr = areahint.dividearea.DivideAreaManager.getInstance();
            if (action.equals("dividearea_start")) {
                mgr.start();
            } else if (action.startsWith("dividearea_select:")) {
                mgr.selectAreaByName(action.substring("dividearea_select:".length()));
            } else if (action.equals("dividearea_continue")) {
                mgr.continueRecording();
            } else if (action.equals("dividearea_save")) {
                mgr.finishAndSave();
            } else if (action.startsWith("dividearea_name:")) {
                mgr.handleNameInput(action.substring("dividearea_name:".length()));
            } else if (action.startsWith("dividearea_level:")) {
                mgr.handleLevelInput(Integer.parseInt(action.substring("dividearea_level:".length())));
            } else if (action.startsWith("dividearea_base:")) {
                mgr.handleBaseInput(action.substring("dividearea_base:".length()));
            } else if (action.startsWith("dividearea_color:")) {
                mgr.handleColorInput(action.substring("dividearea_color:".length()));
            } else if (action.equals("dividearea_cancel")) {
                mgr.cancel();
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾介柣婊呯deArea闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     */
    private static void handleSetHighCommand(String action) {
        try {
            if (action.equals("sethigh_start")) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.area.altitude.modify")), false);
                }
            } else if (action.startsWith("sethigh_custom:")) {
                String areaName = action.substring("sethigh_custom:".length());
                areahint.command.SetHighClientCommand.startCustomHeightInput(areaName);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    private static void handleDeleteCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾芥い褏鐏僼e闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.delete.DeleteManager manager = areahint.delete.DeleteManager.getInstance();

            if (action.equals("delete_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁斿坏lete_start");
                manager.startDelete();
            } else if (action.startsWith("delete_select:")) {
                String areaName = action.substring("delete_select:".length());
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁斿坏lete_select: " + areaName);
                manager.handleAreaSelection(areaName);
            } else if (action.equals("delete_confirm")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁斿坏lete_confirm");
                manager.confirmDelete();
            } else if (action.equals("delete_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁斿坏lete_cancel");
                manager.cancelDelete();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偐椤愵澀澹曢悗鐟板鐢椂e闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲纾芥い褏鐏僼e闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟鎻掑煇aceButton闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleReplaceButtonCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟鎻掑煇aceButton闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.replacebutton.ReplaceButtonManager manager = areahint.replacebutton.ReplaceButtonManager.getInstance();

            if (action.equals("replacebutton_start")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪placebutton_start");
                manager.startReplaceButton();
            } else if (action.equals("replacebutton_confirm")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪placebutton_confirm");
                manager.confirmNewKey();
            } else if (action.equals("replacebutton_cancel")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁旑晪placebutton_cancel");
                manager.cancel();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣偑閳ь剙螞娑旂珮aceButton闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鍊块柟鎻掑煇aceButton闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲绠电痪顓濆uage闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleLanguageCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲绠电痪顓濆uage闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.language.LanguageManager manager = areahint.language.LanguageManager.getInstance();

            if (action.equals("language_start")) {
                manager.startLanguageSelection();
            } else if (action.startsWith("language_select:")) {
                String langCode = action.substring("language_select:".length());
                manager.handleLanguageSelection(langCode);
            } else if (action.equals("language_cancel")) {
                manager.cancelLanguageSelection();
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣枎韫囧﹤浜剧紒娑氼嚢uage闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲绠电痪顓濆uage闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖氼嚗閸曨厾绱﹂柣姘卞崟dViz闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒?
     * @param action 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備讲鍋撳鑸靛姈閻撳繐鈹戦悙鑼虎闁逞屽墯椤ㄥ牏鍒?
     */
    private static void handleBoundVizCommand(String action) {
        try {
            AreashintClient.LOGGER.info("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖氼嚗閸曨厾绱﹂柣姘卞崟dViz闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            areahint.boundviz.BoundVizManager manager = areahint.boundviz.BoundVizManager.getInstance();

            if (action.equals("boundviz_toggle")) {
                AreashintClient.LOGGER.info("闂傚倸鍊风粈浣革耿闁秵鍋￠柟鎯版楠炪垽鏌嶉崫鍕偓褰掑级缁斿朝undviz_toggle");
                manager.toggle();

                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    if (manager.isEnabled()) {
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.boundary.visualization_2")), false);
                    } else {
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.boundary.visualization")), false);
                    }
                }
            } else {
                AreashintClient.LOGGER.warn("闂傚倸鍊风粈渚€骞栭锔藉亱婵犲﹤瀚々鏌ユ煟閹邦喖鍔嬮柛瀣€块弻銊╂偄閸濆嫅锝夋煕鎼达紕效闁哄本鐩獮瀣倻閸℃顫噓ndViz闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒? " + action);
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖氼嚗閸曨厾绱﹂柣姘卞崟dViz闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠垫劖笑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage(), e);
        }
    }

    /**
     * 闂傚倸鍊风粈渚€骞栭銈傚亾濮樼厧澧柡鍛板煐缁傛帞鈧綆鈧叏闄勯幈銊ノ熼悡搴濈紦婵炲瓨绮岀紞濠囧蓟閻旂厧绠氱憸宥夊汲鏉堛劊浜滈柕鍫濇噺閸ｅ綊鎽堕悙鐑樼厽婵°倐鍋撻柣妤€妫濋幃姗€鍩￠崘锝呬壕闁革富鍙庨悞楣冩煕濮橆剦鍎戠紒顔藉哺閺屽棗顓奸崨顖氬Ф闂備礁鎲￠崜顒勫川椤栵絾袣
     * @param client Minecraft闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛濡囧▓銈夋⒑閸濆嫷妲兼俊顐㈠婵?
     */
    private static void displayFrequencyInfo(MinecraftClient client) {
        if (client.player != null) {
            int frequency = ClientConfig.getFrequency();
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_50") + frequency + " Hz"), false);
        }
    }
    
    /**
     * 闂傚倸鍊风粈渚€骞栭銈傚亾濮樼厧澧柡鍛板煐缁傛帞鈧綆鈧叏闄勯幈銊ノ熼悡搴濈紦婵炲瓨绮岀紞濠囧蓟閻旂厧绠氱憸宥夊汲鏉堛劊浜滈柕鍫濇噺閸ｈ櫣绱掔紒妯兼创妤犵偞锕㈠畷姗€鎳犻鍕礈闂傚倷鑳剁划顖炲箰閹间緡鏁勫璺侯煬閸ゆ洘銇勯幒鎴濐仾闁稿绻濋弻鐔封枔閸喗鐏撳銈冨灩閹冲繒鎹㈠☉銏℃櫜闁告侗鍠氶ˇ閬嶆⒑閸涘﹥灏扮紒璇插暟閳?
     * @param client Minecraft闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛濡囧▓銈夋⒑閸濆嫷妲兼俊顐㈠婵?
     */
    private static void displaySubtitleRenderInfo(MinecraftClient client) {
        if (client.player != null) {
            String renderMode = ClientConfig.getSubtitleRender();
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_49") + renderMode), false);
        }
    }
    
    /**
     * 闂傚倸鍊风粈渚€骞栭銈傚亾濮樼厧澧柡鍛板煐缁傛帞鈧綆鈧叏闄勯幈銊ノ熼悡搴濈紦婵炲瓨绮岀紞濠囧蓟閻旂厧绠氱憸宥夊汲鏉堛劊浜滈柕鍫濇噺閸ゅ洭鏌熼绛嬫當闁宠棄顦埢搴∥熼懡銈嗘缂傚倸鍊烽懗鑸垫叏閻戣棄绀傛俊顖欒閸ゆ洟鏌﹀Ο渚Ш闁哄棗顑夐弻鐔封枔閸喗鐏撳銈冨灩閹冲繒鎹㈠☉銏℃櫜闁告侗鍠氶ˇ閬嶆⒑閸涘﹥灏扮紒璇插暟閳?
     * @param client Minecraft闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛濡囧▓銈夋⒑閸濆嫷妲兼俊顐㈠婵?
     */
    private static void displaySubtitleStyleInfo(MinecraftClient client) {
        if (client.player != null) {
            String style = ClientConfig.getSubtitleStyle();
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.general_48") + style), false);
        }
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛鐐婄憸蹇涱敊婵犲洦鈷戦梻鍫熶緱濡狙囨⒒閸曨偄顏€殿喗濞婇、鏃堝醇閻斿搫骞堥梻浣告贡閸庛倝骞愰幖浣€澶屸偓锝庡亞缁犻箖鏌涢銈呮瀻闁诲繆鏅犻弻鐔哥附婢跺﹣鍠婇悗瑙勬礃閿曘垽銆佸鈧幃鈺呭箥閸栵紕鐣垫慨濠冩そ瀹曘劍绻濋崟銊ヮ潓闂備焦鎮堕崝宥囨崲閸儳宓?
     * @param client Minecraft闂傚倷娴囬褎顨ラ崫銉т笉鐎广儱顦崹鍌涚箾瀹割喕绨婚柡鍕╁劜缁绘盯骞嬮悙瀵告闂佸憡顨嗙喊宥夊Φ閸曨垰鍐€闁靛濡囧▓銈夋⒑閸濆嫷妲兼俊顐㈠婵?
     * @param handler 缂傚倸鍊搁崐鎼佸磹閹间礁鐤い鏍仜閸ㄥ倿鏌涢敂璇插箹闁搞劍绻堥弻銈夊箹娴ｈ閿紓浣稿閸嬨倝寮诲☉銏犲嵆闁靛鍎辩粻娲⒑缁嬪尅鍔熼柛瀣ㄥ€濆?
     * @param buf 闂傚倸鍊峰ù鍥ь浖閵娾晜鍤勯柤绋跨仛濞呯姵淇婇妶鍌氫壕闂佷紮绲介悘姘辩箔閻旂厧鐒垫い鎺嗗亾闁伙絿鍏橀幃鈺冩嫚閹绘帞鐛╂俊鐐€栭悧妤冨垝瀹€鍕婂洭鍩￠崨顔规嫼闂佸憡绋戦敃銈嗘叏閸垺鍠愰柡澶婄仢閺嗙喎霉?
     * @param responseSender 闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑闂佹椿鍘介〃濠囧蓟閻斿憡缍囬柟瑙勫姇閹懘姊虹粙娆惧剱闁圭顭烽獮蹇涘川閺夋垵绐涙繝鐢靛Т鐎氼剛绱?
     */
    private static void handleDebugCommand(MinecraftClient client, 
                                          ClientPlayNetworkHandler handler,
                                          PacketByteBuf buf, 
                                          PacketSender responseSender) {
        // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€為梺鍐叉惈閸婃悂顢旈埡鍛拺闁告稑锕ゆ慨锕傛煕濡鍔ら崡閬嶆煙闂傚鍔嶉柣鎾跺枑娣囧﹪顢涘鍙樿檸闂佺粯鎸婚崝娆撳蓟?
        boolean enabled = buf.readBoolean();
        
        // 缂傚倸鍊烽懗鍫曟惞鎼淬劌鐭楅幖娣妼缁愭鏌￠崶鈺佇ｇ€规洖寮堕幈銊ノ熼幐搴ｃ€愰梺鍦嚀閻栧ジ寮婚弴鐔虹闁割煈鍠掗崑鎾诲即閵忊€冲墾濠电姴锕ら幊鎰婵傚憡鐓欓悷娆忓婵牏鐥鐐靛煟闁哄苯绉堕幏鐘诲箵閹烘挸鈧垳绱撴担鍓插剰妞わ妇鏁婚妴浣割潨閳ь剟骞冨鍫濆耿婵☆垳鍋熼崰鏍ь潖?
        client.execute(() -> {
            try {
                AreashintClient.LOGGER.info("Client debug mode: " + (enabled ? "enabled" : "disabled"));
                
                // 闂傚倷娴囧畷鍨叏瀹曞洨鐭嗗ù锝堫潐濞呯姴霉閻樺樊鍎愰柛瀣典邯閺屾盯鍩勯崘顏呭櫗闁诲孩鍑归崜鐔煎蓟濞戙垹绠涢梻鍫熺☉缁犺鈹戦垾鍐茬骇闁诡喖鍊垮濠氭晲閸涘倻鍠栧畷褰掝敊閹冪細闂?
                if (enabled) {
                    areahint.debug.ClientDebugManager.enableDebug();
                } else {
                    areahint.debug.ClientDebugManager.disableDebug();
                }
            } catch (Exception e) {
                AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛绀嬫い鎺嗗亾妞ゅ繆鏅犲娲川婵犲倸袝闂佸憡蓱閸庡啿宓勯梺褰掓？閻掞箓鎮￠弴鐔虹闁瑰鍋熼幊鍐煟韫囨搫韬柡宀€鍠栭悡顒勫箵閹哄棗浜炬繝闈涙－閸ゆ洘銇勯弮鍌氫壕鐎规洖顦甸弻娑樷攽閸℃浠鹃梺浼欓檮濡啫顫? " + e.getMessage());
            }
        });
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤绠炵紒娑崇lor闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑?
     */
    private static void handleRecolorResponse(MinecraftClient client, ClientPlayNetworkHandler handler, 
                                            PacketByteBuf buf, PacketSender responseSender) {
        try {
            String action = buf.readString();
            
            if ("recolor_list".equals(action)) {
                // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛鐐婄憸搴∥ｉ鍕拺缂備焦蓱椤ュ牓鏌￠埀顒勬焼瀹ュ懐锛欓梺瑙勫劶婵倝鎮￠弴銏＄厵闁绘垶蓱閹嫭绻涢崼鐔烘噰闁哄本娲熷畷鍗炍旈埀顒勫汲閿濆洠鍋撶憴鍕闁稿瀚伴、妯荤附缁嬪灝绐涘銈嗙墬濮樸劑寮?
                String dimension = buf.readString();
                int count = buf.readInt();
                
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.message.area.list")), false);
                        
                        for (int i = 0; i < count; i++) {
                            try {
                                String areaName = buf.readString();
                                String currentColor = buf.readString();
                                int level = buf.readInt();
                                String baseName = buf.readString();
                                
                                client.player.sendMessage(areahint.util.TextCompat.of(
                                    String.format(I18nManager.translate("message.message.color.level"),
                                        i + 1, areaName, level, currentColor)
                                ), false);
                            } catch (Exception e) {
                                AreashintClient.LOGGER.error("Failed to read area info", e);
                            }
                        }
                        
                        client.player.sendMessage(areahint.util.TextCompat.of(
                            I18nManager.translate("message.message.color_2")
                        ), false);
                    }
                });
                
            } else if ("recolor_interactive".equals(action)) {
                // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛绀冩い蹇撶У闁裤倝姊绘笟鈧褔鎮ч崱娆屽亾濮樼厧娅嶆俊顐㈠椤撳ジ宕堕敐鍛濠电偛鐗嗛悘婵嬪几濞戞氨纾煎Σ灞剧墬閻掔抱olor闂傚倸鍊峰鎺旀椤旀儳绶ゅù鐘差儐閸庢鏌涚仦鎯у毈闁?
                String dimension = buf.readString();
                int count = buf.readInt();

                // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈嗗坊閸嬫捇鏌￠崟鈺佸姦闁哄矉缍佸顕€宕奸悢鍛婃闂備胶顭堥敃銉︾箾婵犲洤钃熸繛鎴欏灩缁犳娊鏌熼崹顔兼殲闁伙絼绮欏?
                java.util.List<areahint.data.AreaData> areas = new java.util.ArrayList<>();
                for (int i = 0; i < count; i++) {
                    try {
                        String areaName = buf.readString();
                        String currentColor = buf.readString();
                        int level = buf.readInt();
                        String baseName = buf.readString();

                        // 闂傚倸鍊风粈渚€骞夐敍鍕殰婵°倕鍟伴惌娆撴煙鐎电啸缁惧彞绮欓弻鐔煎箚瑜嶉。鍐测槈閹惧磭效闁哄苯绉烽¨渚€鏌涢幘璺烘瀻闁伙絿鍏橀幃鈺冩嫚閹绘帒鏁ゆ俊鐐€栭崝褏寰婇崸妤€姹查柣鏇熲叧eaData闂傚倷娴囬褍霉閻戣棄鏋侀柟闂撮檷閳ь兛鐒︾换婵嬪炊閵娿儳妯侀梻浣告啞濞诧箓宕归柆宥呯柧妞ゆ帒瀚悡鏇熴亜閹板墎鎮肩紒鐘虫尰缁绘盯骞婂畡鐗堝闁绘挻娲熼弻鈥崇暤椤旂厧顏╂繛鍜冪秮濮婅櫣鎷犻垾宕囦哗闂佺粯顨嗛崝妤冨垝鐠囨祴妲堥柕蹇曞Т缁愭盯姊绘笟鍥у伎闂傚嫬瀚伴幆灞轿旀担铏诡啎闂佹寧绻傞幊蹇曠矆瀹€鍕厱闁规儳顕粻鏍倵闂堟稏鍋㈢€规洏鍔庨埀顒佺⊕钃辨繛?
                        areahint.data.AreaData area = new areahint.data.AreaData();
                        area.setName(areaName);
                        area.setColor(currentColor);
                        area.setLevel(level);
                        area.setBaseName(baseName.isEmpty() ? null : baseName);

                        areas.add(area);
                    } catch (Exception e) {
                        AreashintClient.LOGGER.error("Failed to read area info", e);
                    }
                }

                client.execute(() -> {
                    if (client.player != null) {
                        // 闂傚倸鍊风粈渚€骞夐敓鐘茬鐟滅増甯掗崹鍌炴煟濡も偓閻楀﹪宕ｈ箛娑欑厓闁告繂瀚埀顒€顭烽妴鍛村蓟閵夛妇鍘梺鍓插亝缁诲倿顢旈锔解拻濠㈣泛锕﹁倴闂侀€炲苯澧い鏃€鐗犲畷浼村冀椤愩垹鐏婄紓渚婄磿椤掝晻lor婵犵數濮烽弫鎼佸磻閻旂儤宕叉繝闈涱儐閸ゅ嫰鏌涢锝嗙闁?
                        areahint.recolor.RecolorManager.getInstance().startRecolor(areas, dimension);
                    }
                });
            } else if ("recolor_response".equals(action)) {
                // 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滄崲濠靛绀冩い蹇庣劍閺佸灚淇婇悙顏勨偓鏍涙担瑙勫弿闁靛牆顦Ч鍙夈亜閹捐泛鈧絽鈽夐姀鈥充罕闂佸壊鍋侀崹濠氭偂閸曨剛绡€闁靛繈鍨洪崵鈧梺鎸庢处娴滄粓鎮鹃悜绛嬫晝闁靛繆鏅涢幃鎴︽⒑閸涘﹣绶遍柛銊ゅ嵆閹?
                boolean success = buf.readBoolean();
                net.minecraft.text.MutableText message = areahint.network.TranslatableMessage.read(buf);
                
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(message, false);
                    }
                });
            }
             
        } catch (Exception e) {
            AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤绠炵紒娑崇lor闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage());
        }
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖涚┍婵犲洤绠炵紒娑氼嚛me闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑?
     * 婵犵數濮烽弫鎼佸磻濞戔懞鍥敇閵忕姷顦悗骞垮劚椤︻垳绮堥崼婢濆綊鎮℃惔锝嗘喖闂佸搫鎷嬮崜姘跺箞閵娿儺娼ㄩ柛鈩冾殔鐎涳綁姊洪崨濠冪叆闁哥喐鎸冲濠氭晸閻樿尙锛滃┑鐘茬仛閸旀牜绱為崼銏㈢＜缂備降鍨瑰銊︾箾婢跺娲存い銏＄懆缁犳稑鈽夊▎蹇旑吇濠碘槅鍋嗘晶妤冩崲閸愵喗瀚?RenameNetworking 闂傚倸鍊峰ù鍥綖婢跺顩插ù鐘差儏绾惧潡鏌熼崜浣烘憘闁轰礁娲弻鐔兼⒒鐎电濡介梺鍝勬媼閸撶喖骞冨鈧幃娆戞崉鏉炵増鐫忕紓鍌欒兌婵磭鍒掑▎鎾宠摕闁挎繂顦伴弲鏌ユ煕閳╁啰鎳勯柛锝庡幖椤啴濡堕崱妤冪懖缂備浇顕ч悧鍡涱敋閿濆鏁嗛柛灞剧☉閺嬪倿姊洪崨濠冨闁告ê缍婇崺鈧い鎺嗗亾闁挎洏鍊濆﹢渚€姊虹紒妯忣亜顕ｉ崼鏇為棷闁归棿鐒﹂崑锝夋煙闁箑鏋涢柡瀣〒閳ь剝顫夊ú姗€銆冩繝鍥х畺闁斥晛鍟崕鐔搞亜閺傚灝鎮戞繛鍛Ч濮?
     */
    private static void handleRenameResponse(MinecraftClient client, ClientPlayNetworkHandler handler,
                                           PacketByteBuf buf, PacketSender responseSender) {
        // 婵犵數濮甸鏍窗濡ゅ啰绱﹂柛褎顨呯壕褰掓煛瀹ュ骸骞栭柦鍐枛閺岋綁寮捄銊︻唸闂佸搫顑嗛悧鐘诲蓟閿熺姴鐐婇柍杞扮悼閿濆鐓熼柨婵嗩槷閹查箖鏌＄仦鍓ф创鐎殿噮鍣ｅ畷鎺戭潩椤撶姳绨介梻鍌欒兌椤牏鎹㈤幋锔芥櫔婵＄偑鍊栭弻銊ф崲濮椻偓楠炲﹪鎮╁ú缁樻櫈濡炪倖鍔楅崰鎰枍婵犲洦鐓?areahint.rename.RenameNetworking
        // 濠电姷鏁搁崕鎴犲緤閽樺娲晜閻愵剙搴婇梺鍛婃处閸ㄦ澘效閺屻儲鐓冪憸婊堝礈濮樿泛绠為柕濞垮劗閺€浠嬫煕閺囥劌浜愰柛瀣崌瀵粙顢橀悙鑸垫啺闂備胶绮弻銊╂儍濠靛鍑犻柡宥庡幗閻撴盯鏌涢妷锝呭闁汇劍鍨块弻锝夘敇閻愯泛顏梺瀹犳椤︾敻鐛Ο灏栧亾濞戞婊勭閵忥紕绠鹃柡澶嬪灩濮ｇ偤鏌￠崨顖毿㈤柣锝呭槻铻栭柍褜鍓熼、妯荤附缁嬭法鍊為梺鍐叉惈閸婅埖绂掗姀銈嗏拺闁煎鍊曞瓭濠电偠顕滅粻鎾崇暦瑜版帒閿ゆ俊銈傚亾闁哄绀侀埞鎴︽偐閹绘帩浠鹃柣?
        AreashintClient.LOGGER.warn("handleRenameResponse 闂傚倷娴囧畷鐢稿磻閻愬搫绀勭憸鐗堝笒绾捐顭块懜闈涘闁稿骸绉归弻锝夊棘閸喗鍊梺绋款儍閸斿秹濡甸崟顖ｆ晣闁绘棃顥撻鍌滅磽娴ｆ彃浜炬繝銏ｆ硾閳洝銇愰幒鎾存珳闂佸憡渚楅崳顔嘉涢悙瀵哥瘈婵炲牆鐏濋弸鐔兼煟閳哄﹤鐏︽鐐诧工铻栭柛娑卞枛閻у嫭绻濋姀锝嗙【闁挎洏鍊曢埢鎾诲Ψ瑜忕壕钘壝归敐鍛棌婵¤尪顫夋穱濠囧矗婢跺﹥璇為悗?RenameNetworking");
    }

    /**
     * 闂傚倸鍊风粈渚€骞栭銈傚亾濮樼厧澧柡鍛板煐缁傛帞鈧綆鈧叏闄勯幈銊ヮ潨閸℃绠诲銈傛櫆閻擄繝寮婚弴銏犻唶婵犻潧妫导鈧梻鍌氬€搁ˇ顖滅矓閸洖鐒垫い鎺嶇贰閸熷繘鏌涢悩宕囧⒈婵″弶鍔欒矾闁绘粎鈧Γor闂傚倸鍊峰鎺旀椤旀儳绶ゅù鐘差儐閸庢鏌涚仦鎯у毈闁?
     */
    private static void showInteractiveRecolorScreen(int count, PacketByteBuf buf) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.title.area.color.modify")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("message.prompt.area.color.modify")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        for (int i = 0; i < count; i++) {
            try {
                String areaName = buf.readString();
                String currentColor = buf.readString();
                int level = buf.readInt();
                String baseName = buf.readString();
                
                // 闂傚倸鍊风粈渚€骞夐敍鍕殰婵°倕鍟伴惌娆撴煙鐎电啸缁惧彞绮欓弻鐔煎箲閹伴潧娈梺鍝勫閸庣敻寮婚敐澶婄睄闁割偆鍠愰弳鐘绘⒑缁嬪尅鏀诲┑鐐诧工椤繐煤椤忓秵鏅濋梺闈涚箚閺呮粍鏅ラ梻浣筋嚙鐎涒晠宕惔銊ョ闁硅揪濡囧畵渚€鏌熼柇锕€骞栫紒鍓佸仱閺岀喖鏌囬敃鈧獮鏍煕?
                net.minecraft.text.MutableText areaButton = areahint.util.TextCompat.literal(
                    String.format(I18nManager.translate("message.button.color.level"), areaName, level, currentColor)
                ).setStyle(net.minecraft.text.Style.EMPTY
                    .withClickEvent(new net.minecraft.text.ClickEvent(
                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND, 
                        "/areahint recolor " + areaName))
                    .withHoverEvent(new net.minecraft.text.HoverEvent(
                        net.minecraft.text.HoverEvent.Action.SHOW_TEXT, 
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + areaName + I18nManager.translate("message.message.color.modify")))));
                
                client.player.sendMessage(areaButton, false);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("Failed to read area info", e);
            }
        }
        
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(
            I18nManager.translate("message.message.color_2")
        ), false);
        client.player.sendMessage(areahint.util.TextCompat.of(
            I18nManager.translate("message.message.general_45")
        ), false);
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т患缂備讲鍋撻柛鈩兠肩换鍡涙煏閸繃鍣规い蹇嬪劦閺?
     */
    private static void handleSetHighAreaList(MinecraftClient client, 
                                            ClientPlayNetworkHandler handler,
                                            PacketByteBuf buf, 
                                            PacketSender responseSender) {
        client.execute(() -> {
            try {
                // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈嗗坊閸嬫捇鏌￠崟鈺佸姦闁哄矉缍佸顕€宕奸悢鍛婃闂備胶顭堥敃銉︾箾婵犲洤钃熸繛鎴欏灩缁犳娊鏌熼崹顔兼殲闁伙絼绮欏娲濞戞瑯妫為梺鍝ュ枑濞兼瑩顢氶敐澶樻晝闁挎棁妫勬禍閬嶆⒑閸撴彃浜濈紒璇插婵?
                List<String> areaNames = new ArrayList<>();
                List<Boolean> hasAltitudeList = new ArrayList<>();
                List<Double> maxHeightList = new ArrayList<>();
                List<Double> minHeightList = new ArrayList<>();
                
                // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈嗗坊閸嬫捇鎮楀顒夋Ш闁逞屽墮缁犲秹宕曢柆宥呯疇閹兼惌鐓夌紞鏍煏閸繃宸濈痪鎯с偢閺岋繝宕橀埡浣轰淮闂佽绻掓慨鐑藉箟閹间礁绾ч柛顭戝枓閸嬫捇宕稿Δ鈧拑鐔兼煕濞戝崬鏋熷┑顖涙尦閺岋綁骞嬮悜鍡欏姺闂佸搫妫涙繛鈧慨濠勫劋濞碱亪骞忕仦钘夊腐濠电姭鎷冮崟鍨暥闂佷紮绲介悘姘辩箔閻旂厧鐒垫い鎺嗗亾妞ゆ洩绲剧粋鎺斺偓锝庡亜娴滄鏌熼懝鐗堝涧缂佹彃娼￠妴?
                String commandType = buf.readString(); // "sethigh_area_list"
                String dimensionType = buf.readString(); // 缂傚倸鍊搁崐鎼佸磹閻戣姤鍊块柨鏇楀亾妞ゎ厼娲ら埢搴ㄥ箣閺傚じ澹曢梺鑽ゅ枑婢瑰棝鏁嶅澶嬬厽婵﹩鍓欓埛鏃堟煙妞嬪骸鈻堥柟宕囧█椤㈡牠鎸婃径澶婎棜?
                int areaCount = buf.readInt(); // 闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т患闂佸搫顑勭粈渚€婀侀梺绋跨箰閸氬绱為幋鐐电?
                
                AreashintClient.LOGGER.info("闂傚倸鍊峰ù鍥綖婢跺顩插ù鐘差儏绾惧潡鏌＄仦璇插姎闁哄鑳堕幉鎼佹偋閸繄鐟ㄧ紓浣插亾闁糕剝绋掗悡鐔烘喐鎼达絾顫曠痪鐐偓igh闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т患缂備讲鍋撻柛鈩兠肩换鍡涙煏閸繃鍣规い蹇嬪劦閺? 闂傚倸鍊风粈渚€骞夐敍鍕煓闁圭儤顨呴崹鍌涚節闂堟侗鍎忕紒鐙€鍣ｉ弻鏇㈠醇濠靛洤绐涢柣蹇撶箳閺佸骞冭ぐ鎺戠倞妞ゅ繐瀚В銏ゆ⒑?{}, 缂傚倸鍊搁崐鎼佸磹閻戣姤鍊块柨鏇楀亾妞ゎ厼娲ら埢搴ㄥ箣閺傚じ澹?{}, 闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т患闂佸搫顑勭粈渚€婀侀梺绋跨箰閸氬绱為幋鐐电?{}", 
                    commandType, dimensionType, areaCount);
                
                // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈庡墻閸犳鏁悢铏逛笉婵炴垶鐟﹂崕鐔兼煏婵炲灝鍔滈柣銈呭€垮缁樻媴閾忓箍鈧﹪鏌￠崨顔剧疄鐎规洜澧楅幆鏃堝Ω閵壯呮毇闂備礁鎼ú銏ゅ垂濞差亜纾跨€广儱顦伴悡鏇㈡煛閸ャ儱濡虹紒銊╊棑缁辨帞鎷犻崣澶樻＆闂?
                for (int i = 0; i < areaCount; i++) {
                    String areaName = buf.readString();
                    areaNames.add(areaName);
                    
                    boolean hasAltitude = buf.readBoolean();
                    hasAltitudeList.add(hasAltitude);
                    
                    if (hasAltitude) {
                        boolean hasMax = buf.readBoolean();
                        Double maxHeight = hasMax ? buf.readDouble() : null;
                        maxHeightList.add(maxHeight);
                        
                        boolean hasMin = buf.readBoolean();
                        Double minHeight = hasMin ? buf.readDouble() : null;
                        minHeightList.add(minHeight);
                    } else {
                        maxHeightList.add(null);
                        minHeightList.add(null);
                    }
                }
                
                AreashintClient.LOGGER.info("闂傚倸鍊烽懗鍫曞箠閹剧粯鍋ら柕濞炬櫅缁€澶愭煛閸モ晛鏋戦柛娆忕箲缁绘盯骞嬮悙娈挎殹闂佸摜濮村Λ婵嬪蓟閵娿儮鏀介柛鈩冿供濡倝姊洪柅鐐茶嫰閸樺摜绱掗鐣屾噧闁伙絽鍢茬叅妞ゅ繐鎳庢禍婊堟⒒娴ｅ摜浠㈡い鎴濇嚇閸┾偓妞ゆ帊绀侀埢鏇㈡煛鐏炲墽娲存鐐村浮楠炴瑩宕橀埡鍐╂瘜闂? {}", areaNames);
                
                // 闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ喖霉閸忓吋缍戦柡瀣╃劍閵囧嫰濡疯閸斿HighClientCommand濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡?
                areahint.command.SetHighClientCommand.handleAreaList(areaNames, dimensionType);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т患缂備讲鍋撻柛鈩兠肩换鍡涙煏閸繃鍣规い蹇嬪劦閺屽秷顧侀柛鎾寸懇瀹曨垶寮堕幋顓炴濡炪倖姊婚崑鎾崇暦婢舵劖鐓曟繝闈涙椤忣亪鏌ｉ敂鑺ヮ棃婵? " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т痪闂佺粯甯熼褔鈥︾捄銊﹀磯濞撴凹鍨伴崜鏉款渻?
     */
    private static void handleSetHighAreaSelection(MinecraftClient client, 
                                                 ClientPlayNetworkHandler handler,
                                                 PacketByteBuf buf, 
                                                 PacketSender responseSender) {
        client.execute(() -> {
            try {
                // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈嗗坊閸嬫挾绱掗悩鍗炲祮闁哄本鐩俊鐑藉箣濠靛洤娅ゅ┑顕嗙到闁帮絽顫忛搹鐟板闁哄洨鍠愰悵鏍⒑閸濄儱孝闂佸府缍侀悰顕€宕橀鐓庡祮闂侀潧绻嗛埀顒€鍟挎竟鍕⒒娴ｅ憡鍟炴繛璇х畵瀹曟粌顫濋鑺ョ亖闂佸壊鍋呭ú姗€鎮?
                String areaName = buf.readString();
                
                // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈呯箰閸婂嘲效濡ゅ懏鈷戦悷娆忓閸斻倝鏌涢悢閿嬪仴鐠侯垶鏌涘☉鍗炲福闁哄啫鐗嗙壕鍏肩箾濞ｎ剙鐒哄ù婊呭仱閸┾偓妞ゆ帒鍋嗛弨鐗堢箾閸涱喗绀堢紒顔藉哺閺屽棗顓奸崨顖氬Ф闂備礁鎲￠崜顒勫川椤栵絾袣
                boolean hasAltitude = buf.readBoolean();
                Double maxHeight = null;
                Double minHeight = null;
                
                if (hasAltitude) {
                    boolean hasMax = buf.readBoolean();
                    if (hasMax) {
                        maxHeight = buf.readDouble();
                    }
                    boolean hasMin = buf.readBoolean();
                    if (hasMin) {
                        minHeight = buf.readDouble();
                    }
                }
                
                AreashintClient.LOGGER.info("闂傚倸鍊峰ù鍥綖婢跺顩插ù鐘差儏绾惧潡鏌＄仦璇插姎闁哄鑳堕幉鎼佹偋閸繄鐟ㄧ紓浣插亾闁糕剝绋掗悡鐔烘喐鎼达絾顫曠痪鐐偓igh闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т痪闂佺粯甯熼褔鈥︾捄銊﹀磯濞撴凹鍨伴崜鏉款渻? 闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒?{}, 闂傚倸鍊风粈渚€骞栭锔藉亱闁糕剝铔嬮崶顒佸仺闁告稑锕ゆ禒褍顪冮妶鍡橆梿濠殿喓鍊濆畷鏇㈡偄閸忚偐鍙嗛梺鍝勬川閸嬫盯鍩€椤掆偓缂嶅﹪銆侀幘鏂ユ瀻闁规儳顕崢?{}, 闂傚倸鍊风粈渚€骞栭锔藉亱闁告劦鍠栫壕濠氭煙閻愵剙澧柣鏂挎閺屾盯顢曢姀鈽嗘濠电偛鐪伴崝蹇涘Φ閹伴偊鏁嶉柣鎰皺椤?{}, 闂傚倸鍊风粈渚€骞栭锔藉亱闁告劦鍠栫壕濠氭煙閸撗呭笡闁稿﹤鐖奸悡顐﹀炊閵婏箑鏆楃紓浣哄Т椤兘寮婚敐鍛斀闁归偊鍓氶悘宥夋⒑?{}", 
                    areaName, hasAltitude, maxHeight, minHeight);
                
                // 闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ喖霉閸忓吋缍戦柡瀣╃劍閵囧嫰濡疯閸斿HighClientCommand濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡?
                areahint.command.SetHighClientCommand.handleAreaSelection(areaName, hasAltitude, maxHeight, minHeight);
                
            } catch (Exception e) {
                AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т痪闂佺粯甯熼褔鈥︾捄銊﹀磯濞撴凹鍨伴崜鏉款渻閵堝懐绠為柛搴ｆ暬瀵鏁撻悩鑼槰濡炪倖鎸鹃崰鎾诲Υ閹扮増鈷戦柟鑲╁仜閳ь兙鍊濆畷锝夊礃椤旇壈鎽? " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑?
     */
    private static void handleSetHighResponse(MinecraftClient client, 
                                            ClientPlayNetworkHandler handler,
                                            PacketByteBuf buf, 
                                            PacketSender responseSender) {
        client.execute(() -> {
            try {
                boolean success = buf.readBoolean();
                net.minecraft.text.MutableText message = TranslatableMessage.read(buf);
                areahint.command.SetHighClientCommand.handleServerResponse(success, message);
            } catch (Exception e) {
                AreashintClient.LOGGER.error("濠电姷鏁告慨浼村垂閻撳簶鏋栨繛鎴炲焹閸嬫挸顫濋悡搴㈢彎濡ょ姷鍋涢崯顖滅紦娴犲鈧偓闁绘粎顑杋gh闂傚倸鍊风粈渚€骞夐敍鍕床闁稿本绮庨惌鎾绘倵閸偆鎽冨┑顔藉▕閺岋紕浠︾拠鎻掑缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挸鈹戦鍡欏埌妞わ箓娼ч～? " + e.getMessage());
            }
        });
    }
    
    /**
     * 闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊搁崹鍌炴煕瑜庨〃鍛存倿閸偁浜滈柟杈剧到閸斿灚銇勯幘棰濆殸tHigh濠电姴鐥夐弶搴撳亾閺囥垹纾圭憸鐗堝笚閺咁亪姊虹拠鍙夋崳闁硅櫕鎸炬竟鏇㈩敇閵忕姷顔嗛柣搴秵閸犳牜绮堥崼銏″枑闊洦绋戦崙鐘绘煕瀹€鈧崑鐐哄磻閿濆鐓曢柕澶樺枛婢ь垶鏌℃笟鈧禍鍫曞蓟閿濆鏁傞柛鎰╁妼缁侇噣鎮楃憴鍕鐎光偓閹间礁绠栫憸鏂跨暦婵傚憡鍋勯柧蹇氼潐濠㈡垿姊婚崒娆戭槮闁硅绻濆濠氭晸閻樿尙鍔﹀銈嗗笒閸燁垶鎮甸鍫熺厽?
     * @param areaName 闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒鐘崇墵閺岋繝宕堕妷銉т患闁荤喐鐟辨俊鍥焵椤掆偓缁犲秹宕曢崡鐐╂灃?
     * @param hasAltitude 闂傚倸鍊风粈渚€骞栭銈傚亾濮樺崬鍘寸€规洝顫夌€靛ジ寮堕幋鐘垫毎濠电偞鎸婚崺鍐磻閹惧灈鍋撳▓鍨灈闁诲繑鑹鹃銉╁礋椤栨氨鐤€濡炪倖妫佹竟鍫ュ箯椤愶附鈷戠痪顓炴噹椤ュ秹鏌熷ú璁崇敖缂侇喖顭烽獮姗€骞囨担琛″亾閼哥數绠剧€瑰壊鍠曠花鑽ょ磼閳?
     * @param maxHeight 闂傚倸鍊风粈渚€骞栭锔藉亱闁告劦鍠栫壕濠氭煙閻愵剙澧柣鏂挎閺屾盯顢曢姀鈽嗘濠电偛鐪伴崝蹇涘Φ閹伴偊鏁嶉柣鎰皺椤?
     * @param minHeight 闂傚倸鍊风粈渚€骞栭锔藉亱闁告劦鍠栫壕濠氭煙閸撗呭笡闁稿﹤鐖奸悡顐﹀炊閵婏箑鏆楃紓浣哄Т椤兘寮婚敐鍛斀闁归偊鍓氶悘宥夋⒑?
     */
    public static void sendSetHighRequest(String areaName, boolean hasAltitude, 
                                        Double maxHeight, Double minHeight) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(areaName);
            buf.writeBoolean(hasAltitude);
            
            if (hasAltitude) {
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
            
            AreashintClient.LOGGER.info("闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊搁崹鍌炴煕瑜庨〃鍛存倿閸偁浜滈柟杈剧到閸斿灚銇勯幘棰濆殸tHigh闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕? 闂傚倸鍊烽懗鍓佹兜閸洖鐤炬繝闈涱儏缁愭骞栧ǎ顒€濡肩紒?{}, 闂傚倸鍊风粈渚€骞栭锔藉亱闁糕剝铔嬮崶顒佸仺闁告稑锕ゆ禒褍顪冮妶鍡橆梿濠殿喓鍊濆畷鏇㈡偄閸忚偐鍙嗛梺鍝勬川閸嬫盯鍩€椤掆偓缂嶅﹪銆侀幘鏂ユ瀻闁规儳顕崢?{}, 闂傚倸鍊风粈渚€骞栭锔藉亱闁告劦鍠栫壕濠氭煙閻愵剙澧柣鏂挎閺屾盯顢曢姀鈽嗘濠电偛鐪伴崝蹇涘Φ閹伴偊鏁嶉柣鎰皺椤?{}, 闂傚倸鍊风粈渚€骞栭锔藉亱闁告劦鍠栫壕濠氭煙閸撗呭笡闁稿﹤鐖奸悡顐﹀炊閵婏箑鏆楃紓浣哄Т椤兘寮婚敐鍛斀闁归偊鍓氶悘宥夋⒑?{}", 
                areaName, hasAltitude, maxHeight, minHeight);
                
        } catch (Exception e) {
            AreashintClient.LOGGER.error("闂傚倸鍊风粈渚€骞夐敓鐘冲仭闁挎洖鍊搁崹鍌炴煕瑜庨〃鍛存倿閸偁浜滈柟杈剧到閸斿灚銇勯幘棰濆殸tHigh闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕曢幋婢濆綊宕楅崗鑲╃▏缂備緡鍋勭粔褰掑箖瑜版帒浼犻柛鏇ㄥ墯閸庢挾绱撴担绋跨骇婵＄偘绮欏濠氭晲婢跺娼婇梺闈涚箳婵敻鎮橀崼銉︹拺閺夌偞澹嗛ˇ锕傛煥閺囶亞鐣垫? " + e.getMessage(), e);
        }
    }
} 
