package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.ServerI18nManager;

import areahint.network.Packets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import static areahint.network.TranslatableMessage.key;
import static areahint.network.TranslatableMessage.lit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶鈺佹珡婵炲牊顨嗘穱濠囶敍濮橆厽鍎撳銈嗘煥閿曨亪骞冪憴鍕缂佸绨遍幐鍐⒑濮瑰洤鈧洖螞濠靛棛鏆﹂柕濞炬櫅缁狅綁鏌熼悜妯诲碍鐞氭ê鈹戦悩顔肩伇婵炲绋撻埀顒佸嚬閸ｏ綁鐛崘鈺冪瘈闁搞儜鍥紬?
 * 婵犵數濮伴崹鐓庘枖濞戞埃鍋撳鐓庢珝妤?/areahint sethigh 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁?
 */
public class SetHighCommand {
    
    /**
     * 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鎼佹⒑閼姐倕小缂佲偓娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐夸緵婵炲牊顨嗘穱濠囶敍濮橆厽鍎撳銈嗘煥閿曨亪寮诲☉妯锋婵炲棗绻掓禍浼存⒑?
     * @param context 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囶棤闁崇粯鏌ㄩ埞鎴︽偐鏉堫偄鍘￠梺鑽ゅ枑閹瑰洭寮?
     * @return 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鍝ョ磽閸屾艾鈧兘鎮為敃鍌氱濠电姵纰嶉崐?
     */
    public static int executeSetHigh(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        String playerName = player.getName().getString();
        boolean isAdmin = source.hasPermissionLevel(2);

        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁稿绻堥弻宥嗘姜閹殿喛绐楅梺鍝ュ暱閸嬫捇鏌ｉ悢鍝ョ煂濠⒀勵殜楠炴劙宕ㄦ繝鍐╃彿闂佺粯顭囬弫鍝ュ婵傚憡鐓涘璺侯儏閻忋儲銇?
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        String fileName = Packets.getFileNameForDimension(dimensionType);

        if (fileName == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.dimension_3"));
            return 0;
        }

        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁活厽顨嗛妵鍕冀閵娧勫櫗闂佸憡鐟ョ换姗€寮婚敓鐘茬劦妞ゆ帒鍟ㄦ禍褰掓煙閻戞ɑ鐓ラ柦鍌氼儔閺岀喖鎳濋悧鍫濇锭婵犵鈧櫕鎼愰柍缁樻崌楠炲鏁冮埀顒勫及閵夆晜鐓熼柡鍐ㄧ墛閺侀亶鏌涙繝鍐ㄢ枙闁哄本鐩獮鎺楀箼閸愨晝鏆梻?
        List<AreaData> editableAreas = getHeightEditableAreas(fileName, playerName, isAdmin);

        if (editableAreas.isEmpty()) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.altitude.dimension"));
            return 0;
        }

        // 闂傚倷绀侀幉锟犲礄瑜版帒鍨傞柣妤€鐗婇崣蹇涙煃閸濆嫬鈧銆呴弻銉︾厪闁割偅绻傞顐︽⒒婢跺﹦肖闁逞屽墯椤旀牠宕伴弽顐ｅ床婵せ鍋撻柟顔炬暬椤㈡瑩鎳為妷銉ュΤ闂備焦瀵уú鎴犳閿熺姴鐤幖杈剧悼绾句粙鏌涢幇顓炲姢闁哄鍠撶槐鎺撶瑹婵犲偆娲紓?
        startInteractiveHeightSetting(source, editableAreas);

        return 1;
    }

    /**
     * 闂傚倷绀侀幉锟犲礄瑜版帒鍨傞柣妤€鐗婇崣蹇涙煃閸濆嫬鈧銆呴弻銉︾厪闁割偅绻傞顐︽⒒婢跺﹦肖闁逞屽墯椤旀牠宕伴弽顐ｅ床婵せ鍋撻柟顔炬暬椤㈡瑩鎳為妷銉ュΤ闂備焦瀵уú鎴犳閿熺姴鐤幖杈剧悼绾句粙鏌涢幇顓炲姢闁哄鍠撶槐鎺撶瑹婵犲偆娲紓?
     * @param source 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囧櫝闁衡偓?
     * @param editableAreas 闂傚倷绀侀幉锟犳偡椤栫偛鍨傜€规洖娲﹂～鏇熴亜韫囨挻鍣虹紓宥呮喘閺岀喓鈧數顭堟牎闂佷紮缍佹禍鍫曞蓟濞戙垹鐓涢柛鎰╁妽閻庡姊虹拠鈥虫灍婵炶尙鍠庨悾宄邦潩椤戔晜妫冨畷鐔煎煘閹傚?
     */
    private static void startInteractiveHeightSetting(ServerCommandSource source, List<AreaData> editableAreas) {
        try {
            // 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨呴悞鍨亜閹达絾纭舵い锔肩畵閺岋綁骞樼€靛憡鍣伴悗瑙勬磸閸庨亶顢樻總绋垮窛妞ゆ挾濮崇划鎾⒑閼姐倕浠︾紒瀣箻瀹曟繂鐣濋崟顐ｆК閻庡厜鍋撻柍褜鍓熼崺鈧い鎺嗗亾缂佺姴绉瑰畷鐟懊洪鍕К濠电偞鍨跺銊╁础濮橆厹浜滈柡宥冨劚閳ь剚顨堢划鍫熷緞閹邦厾鍘卞┑顔筋焽閸樠囨倶閻樻祴鏀芥い鏃囶潐濞呭棛绱掗崒姘毙у┑鈥崇埣瀹曠喖顢橀姀鈶╁亾濞差亝鍊垫繛鍫濈仢閺嬫稓绱掔紒妯虹仼缂侇喖锕弫鍌炲礈瑜忛悡?
            List<String> areaNames = editableAreas.stream()
                    .map(AreaData::getName)
                    .toList();
            
            // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃珖缁炬儳銈搁弻鈩冨緞鐎ｎ亞浠村銈嗘煥閿曪妇妲愰幒妤婃晝闁靛鍠栧▓顓㈡⒑?
            String dimension = source.getPlayer().getWorld().getRegistryKey().getValue().toString();
            String dimensionType = convertDimensionIdToType(dimension);
            
            // 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨呴悞鍨亜閹达絾纭堕柛鏂跨Ч閺岋繝宕卞▎蹇旂亪濡ょ姷鍋為崹鍧椼€佸▎鎾村亗閹艰揪绲鹃埢澶愭⒒娴ｅ憡鍟為悽顖涱殔椤灝顫滈埀顒勩€佸Ο瑁や汗闁圭儤鍨归鐑樹繆閻愬樊鍎忛悗鍨笚鐎靛ジ宕掗悙瀵稿弳濠电偞鍨惰摫婵犫偓娴煎瓨鐓熸俊銈呭枤閻掔偓銇勯弴顏嗙К缂佺姵绋掗幆鏃堟晲閸ャ劌鏋﹂梻浣筋嚙妤犲摜绮诲澶婂瀭婵炲樊浜滈弰銉︾箾閹寸偞鐨戦柛妤佺閵囧嫰寮介妸褉濮囬梺绯曟櫅閸婂潡寮婚敓鐘查唶闁绘梹浜介埀顒佸笚缁绘盯宕奸悢椋庝桓缂備礁顑呴ˇ鐢稿春閳ь剚銇勯幒鎴濐仾闁?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("sethigh_area_list"); // 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囧窛閻忓繒鏁婚幃褰掑炊椤忓嫮姣㈤梺?
            buf.writeString(dimensionType); // 缂傚倸鍊搁崐鐑芥倿閿曗偓椤洤鈻庨幋鏂夸壕闁荤喐澹嗛敍宥夋煟濮橆剙鈷旈柟椋庡█閹崇娀顢栭挊澶夊?
            buf.writeInt(editableAreas.size()); // 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶銉ョ仼闁哄绀侀湁闁稿繐鍚嬬紞鎴炵箾?
            
            // 闂傚倷绀侀幉锟犲礉閺嶎厽鍋￠柕澶嗘櫅閻鏌涢埄鍐︿簼闁圭儤娲滈悿鈧柟鍏肩暘閸斿苯鈻撳┑瀣拺缂侇垱娲樺▍鍥煕閻樻煡鍙勬鐐插暣瀵挳濮€閳ュ啿澹勯梻浣告啞濞诧附绂嶅▎鎴炲床闁糕剝绋掗悡?
            for (AreaData area : editableAreas) {
                buf.writeString(area.getName());
                
                AreaData.AltitudeData altitude = area.getAltitude();
                boolean hasAltitude = altitude != null && (altitude.getMax() != null || altitude.getMin() != null);
                buf.writeBoolean(hasAltitude);
                
                if (hasAltitude) {
                    buf.writeBoolean(altitude.getMax() != null);
                    if (altitude.getMax() != null) {
                        buf.writeDouble(altitude.getMax());
                    }
                    buf.writeBoolean(altitude.getMin() != null);
                    if (altitude.getMin() != null) {
                        buf.writeDouble(altitude.getMin());
                    }
                }
            }
            
            ServerPlayNetworking.send(source.getPlayer(), Packets.S2C_SETHIGH_AREA_LIST, buf);
            
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.prompt.area.altitude.modify"));

        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to start interactive sethigh flow", e);
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude.start"));
        }
    }

    /**
     * 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鎼佹⒑閼姐倕小缂佲偓娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐槈缂佺姳鍗抽弻娑㈠Ψ閹存繃鍣烘慨锝呴叄濮婅櫣鍖栭弴鐔哥彇闂佸摜濮撮柊锝夌嵁閸愵喖鍗抽柣鏇氱劍閻ｈ鈹戦埥鍡楃仩闁圭⒈鍋婇、娆愮節閸ャ劎鍘介梺闈涱焾閸庨亶顢旈埡鍛厵闁哄倶鍎辨晶鑼磼?
     * @param context 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囶棤闁崇粯鏌ㄩ埞鎴︽偐鏉堫偄鍘￠梺鑽ゅ枑閹瑰洭寮?
     * @param areaName 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶銉ョ仼閻熸瑱濡囬埀顒€绠嶉崕鍗炩枖?
     * @return 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鍝ョ磽閸屾艾鈧兘鎮為敃鍌氱濠电姵纰嶉崐?
     */
    public static int executeSetHighWithArea(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        String playerName = player.getName().getString();
        boolean isAdmin = source.hasPermissionLevel(2);

        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁稿绻堥弻宥嗘姜閹殿喛绐楅梺鍝ュ暱閸嬫捇鏌ｉ悢鍝ョ煂濠⒀勵殜楠炴劙宕ㄦ繝鍐╃彿闂佺粯顭囬弫鍝ュ婵傚憡鐓涘璺侯儏閻忋儲銇?
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        String fileName = Packets.getFileNameForDimension(dimensionType);

        if (fileName == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.dimension_3"));
            return 0;
        }

        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁活厽顨嗛妵鍕冀閵娧勫櫗闂佸憡鐟ョ换姗€寮婚敓鐘茬劦妞ゆ帒鍟ㄦ禍褰掓煙閻戞ɑ鐓ラ柦鍌氼儔閺岀喖鎳濋悧鍫濇锭婵犵鈧櫕鎼愰柍缁樻崌楠炲鏁冮埀顒勫及閵夆晜鐓熼柡鍐ㄧ墛閺侀亶鏌涙繝鍐ㄢ枙闁哄本鐩獮鎺楀箼閸愨晝鏆梻?
        List<AreaData> editableAreas = getHeightEditableAreas(fileName, playerName, isAdmin);

        if (editableAreas.isEmpty()) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.altitude.dimension"));
            return 0;
        }

        // 闂傚倷绀侀幖顐ゆ偖椤愶箑纾块柛娆忣槺閻濊埖鎱ㄥ璇蹭壕濡ょ姷鍋為崝娆忕暦閹偊妲鹃梺鍝勬閸楁娊寮婚敐澶娢╅柕澶堝労娴犲ジ姊虹紒妯诲鞍闁荤啿鏅涢悾?
        AreaData targetArea = editableAreas.stream()
                .filter(area -> area.getName().equals(areaName))
                .findFirst()
                .orElse(null);

        if (targetArea == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("addhint.error.area").append(TextCompat.literal(areaName)).append(TextCompat.translatable("command.message.altitude.modify.permission")));
            // 闂傚倷绀侀幉锟犲礄瑜版帒鍨傞柣妤€鐗婇崣蹇涙煃閸濆嫬鈧銆呴弻銉︾厪闁割偅绻傞顐︽⒒婢跺﹦肖闁逞屽墯椤旀牠宕伴弽顐ｅ床闁瑰濮烽惌鍫ユ煥閺傚灝鈷旈柣顓熺懇閹鏁愰崨顓ф殺缂備讲鍋撳鑸靛姈閸嬶絽霉閿濆懎绾ф繛鍛喘閺屸剝鎷呴棃娑掑亾濠靛宓侀悗锝庡枟閸婂鎮峰▎娆戝埌濞存粍鍎抽…璺ㄦ崉娓氼垰鍓板銈呯箞閸庣敻寮诲☉婊呯杸闁哄啠鍋撻柡瀣缁辨帡顢氶崨顓炩拫閻庤娲樺浠嬪箖閵忋倖鐓ラ悗锝庝簴閸?
            startInteractiveHeightSetting(source, editableAreas);
            return 0;
        }
        
        // 闂傚倷鑳堕崕鐢稿疾濞戙垺鍋ら柕濞у嫭娈伴梺鍦檸閸犳牜鎲撮敂濮愪簻闁哄秲鍔忔竟妯兼偖閻斿吋鈷戦柛锔诲弾閻掔偓绻涚仦鍌氣偓婵嬪箖閻愵兙鍋呴柛鎰╁妽濡差剟姊虹紒妯活棃闁衡偓闁秴纾归柟鎯板Г閻撶喖鏌曟繛鍨姎妞ゅ景鍕弿婵鐗撳顔剧磼閸屾氨效妤犵偞甯￠獮瀣偐濞堟寧鏁ゆ繝寰锋澘鈧洟宕愯ぐ鎺撴櫔闂佽娴烽幊鎾跺椤撱垹绠悗锝庡枛缁€鍫㈡喐韫囨稑鍑犻柛灞剧〒缁犲墽绱撻崼銏犘柛蹇撶焸閺?
        startInteractiveHeightSettingForSpecificArea(source, targetArea);
        
        return 1;
    }
    
    /**
     * 闂傚倷绀侀幉锟犲礄瑜版帒鍨傞柣妤€鐗婇崣蹇涙煃閸濆嫭鍣圭紒鐘卞嵆閺屾盯濡烽幋婵囧櫤婵絽閰ｅ铏瑰寲閺囩喐鐝栭梺鍝ュТ闁帮綁鐛崘顔煎嵆闁靛繆鈧啿澹勯梻浣告啞濞诧附绂嶅┑瀣埞濠㈣泛鈯曢悷閭︾叆闁告洦鍋嗛悷銊х磽娴ｇ懓顣抽柛鎾寸箞閵嗗啴宕堕‖顒傚枛瀹曨偊宕熼棃娑欐毌闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐炬▍閻熸瑥瀚刊鎾偠濞戞帒澧查柣?
     * @param source 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囧櫝闁衡偓?
     * @param targetArea 闂傚倷鑳堕崕鐢稿疾閳哄懎绐楁俊銈呮噺閸嬪鏌ㄥ┑鍡╂Ц闁哄嫨鍎甸弻锝夊籍閸喐鏆犻梺?
     */
    private static void startInteractiveHeightSettingForSpecificArea(ServerCommandSource source, AreaData targetArea) {
        try {
            // 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨呴悞鍨亜閹达絾纭堕柛鏂跨Ч閹宕归锝囨闂侀€炲苯澧叉い顐㈩樀閹虫繃銈ｉ崘鈺佷簵濠电偛妫欓幐鍝ユ喆閿旈敮鍋撻獮鍨姎婵炶绠撻幃楣冩焼瀹ュ棛鍘告繛杈剧到閹碱偊銆傞懖鈹惧亾鐟欏嫭灏紒鑸佃壘閻ｅ嘲顫濈捄铏归獓濠电偛妫欓崕鎶藉箖閸涘瓨鈷戦悷娆忓鐏忥附銇勯妷锔藉磳妞?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(targetArea.getName());
            
            // 闂傚倷绀侀幉锟犲礉閺嶎厽鍋￠柕澶嗘櫅閻鏌涢埄鍐ㄦ惛濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜閵堝懏鍣归摶鏍煕鐏炴崘鈧粙顢旈崱蹇撲壕缁绢厼鎳忛悵顏嗙磼瀹€鍕喚闁糕晛瀚板畷妯款槾缂佲偓閳?
            AreaData.AltitudeData altitude = targetArea.getAltitude();
            boolean hasAltitude = altitude != null && (altitude.getMax() != null || altitude.getMin() != null);
            buf.writeBoolean(hasAltitude);
            
            if (hasAltitude) {
                buf.writeBoolean(altitude.getMax() != null);
                if (altitude.getMax() != null) {
                    buf.writeDouble(altitude.getMax());
                }
                buf.writeBoolean(altitude.getMin() != null);
                if (altitude.getMin() != null) {
                    buf.writeDouble(altitude.getMin());
                }
            }
            
            ServerPlayNetworking.send(source.getPlayer(), Packets.S2C_SETHIGH_AREA_SELECTION, buf);
            
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.start").append(TextCompat.literal(targetArea.getName())).append(TextCompat.translatable("command.message.altitude_3")));

        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to start sethigh flow for selected area", e);
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude.start"));
        }
    }

    /**
     * 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁活厽顨嗛妵鍕冀閵娧呯厒濠碉紕鍋樼划娆忣嚕閸洖閱囬柣鏃堫棑娴煎矂姊虹紒妯肩闁哥姵顨婇獮鍡涘籍閸喐娅嗛梺缁樺姦閸撴盯寮抽悩缁樷拺缂侇垱娲樺▍鍥煕閻樻煡鍙勬鐐插暣瀵挳濮€閻樻妲存繝寰锋澘鈧洜鈧哎鍔戦崺鈧?
     * @param fileName 闂傚倷绀侀幖顐﹀磹缁嬫５娲晲閸涱亝鐎婚梺闈涚箞閸婃洜鎲?
     * @param playerName 闂傚倷鑳剁划顖涚珶閺囥垹鐤炬繛鎴欏焺閺佸﹦鈧箍鍎遍ˇ顖滄喆閿旈敮鍋撻獮鍨姎婵?
     * @param isAdmin 闂傚倷绀侀幖顐も偓姘卞厴瀹曡瀵奸弶鎴犵暰婵炶揪绲藉﹢閬嶅煝閺冨牊鐓ｉ煫鍥ㄥ嚬閸ゅ啴鏌涘锝呬壕闂傚倷娴囧畷鐢稿磻濞戞娑樜旈崨顓犵枀?
     * @return 闂傚倷绀侀幉锟犳偡椤栫偛鍨傜€规洖娲﹂～鏇熴亜韫囨挻鍣虹紓宥呮喘閺岀喓鈧數顭堟牎闂佷紮缍佹禍鍫曞蓟濞戙垹鐓涢柛鎰╁妽閻庡姊虹拠鈥虫灍婵炶尙鍠庨悾宄邦潩椤戔晜妫冨畷鐔煎煘閹傚?
     */
    private static List<AreaData> getHeightEditableAreas(String fileName, String playerName, boolean isAdmin) {
        try {
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                return new ArrayList<>();
            }
            
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            
            return areas.stream()
                    .filter(area -> canEditAreaHeight(area, playerName, isAdmin, areas))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to read area data for sethigh", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 濠电姷顣藉Σ鍛村磻閳ь剟鏌涚€ｎ偅宕岄柡宀嬬磿娴狅妇鎷犻幓鎺戭潥婵犵鈧啿绾ч柟顔煎€搁悾鐑藉Ψ閳哄倹娅囬梺閫炲苯澧查柕鍥ㄥ姍瀹曪絾寰勫畝濠冪カ闂備線娼ч敍蹇涘川椤旇桨铏庨梻浣告惈椤︻垶鎮ф繝鍥у瀭闁汇垹鎲￠崑鍌涚箾閸℃ɑ灏悷娆欏閳ь剙绠嶉崕閬嵥囨潏顐犱汗妞ゆ牜鍋涚粻?
     * @param area 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶銉ョ仼闁哄绶氶弻锝呂旈埀顒勬偋閸℃瑧鐭?
     * @param playerName 闂傚倷鑳剁划顖涚珶閺囥垹鐤炬繛鎴欏焺閺佸﹦鈧箍鍎遍ˇ顖滄喆閿旈敮鍋撻獮鍨姎婵?
     * @param isAdmin 闂傚倷绀侀幖顐も偓姘卞厴瀹曡瀵奸弶鎴犵暰婵炶揪绲藉﹢閬嶅煝閺冨牊鐓ｉ煫鍥ㄥ嚬閸ゅ啴鏌涘锝呬壕闂傚倷娴囧畷鐢稿磻濞戞娑樜旈崨顓犵枀?
     * @param allAreas 闂傚倷绀佸﹢閬嶃€傛禒瀣；闁瑰墽绮悡娑㈡煕椤愶絿绠ユ俊鍙夋倐閺岋綁骞樼€靛憡鍣伴悗瑙勬磸閸庨亶顢樻總绋垮耿婵☆垱妞块崯鈧梻鍌欒兌椤㈠﹪顢氶弽顓為棷妞ゆ牗绮嶉～鏇熺箾閸℃ɑ灏伴柛瀣ㄥ姂閺屾洘绻涢崹顔瑰亾濡ゅ懏鍤岄柣鎰靛厸缁诲棙銇勯幇顔兼瀻濞存粓绠栧娲传閸曨厾鍔稿┑鐐叉噺婵＄皧ename闂佽瀛╅鏍窗濡も偓鐓ゆ繝濠傜墕閺嬩線鏌曢崼婵愭Ч闁?
     * @return 闂傚倷绀侀幖顐も偓姘卞厴瀹曡瀵奸弶鎴犵暰婵炴挻鍩冮崑鎾垛偓瑙勬穿缂嶄線銆佸☉妯锋瀻閹艰揪缍侀悗铏圭磽閸屾艾鈧悂宕愯ぐ鎺撳亱闁绘劕鐏氶?
     */
    private static boolean canEditAreaHeight(AreaData area, String playerName, boolean isAdmin, List<AreaData> allAreas) {
        // 缂傚倸鍊烽懗鑸靛垔鐎靛憡顫曢柡鍥ュ灩缁犳牕鈹戦悩鍙夋悙鐎瑰憡绻冩穱濠囶敍濮橆厽鍎撻柣鐘辩劍瑜板啴婀侀梺缁橈供閸犳牠宕濋妶鍥╃＜濡插本鐗旂花鍏肩箾閻撳海绠绘鐐差儔閹瑥顔忛鑺ヮ啇闂傚倷绀侀幖顐︽偋閸℃蛋鍥敍閻愭潙浜楀┑鐐叉閹稿摜鎲撮敂閿亾楠炲灝鍔氭繛璇х畵閹箖宕￠悙宥嗘瀹曟帒霉閵忊晙绨介柍?
        if (isAdmin) {
            return true;
        }
        
        // 闂傚倷绀侀幖顐﹀箯鐎ｎ喖闂柨婵嗩槸閻掑灚銇勯幒宥囧妽闁汇劍鍨块幃浠嬵敍濠靛浂浠╅梺閫炲苯澧版い銏狅工閳绘柨鈽夊┃澶告睏闂佸憡鐟ラˇ閬嶃€呴悜鑺ョ厪濠㈣鍨伴幊蹇涘Φ濠靛鐓涘璺猴功婢ф盯鏌涢妸銉ｅ仮鐎规洟娼ч…銊╁礃閹勬珚闂備礁鎲￠〃鍫ュ磻濞戙垺鍊堕柨娑樺閸嬫捇鎮欓悷鎵冲亾濞戞ǚ鏋嶉柨婵嗘噳閺嬫棃鏌熸潏鍓х暠闁哄嫨鍎甸弻锝夊籍閸喐鏆犻梺绋匡攻婵炲﹪寮婚妸銉㈡婵☆垵宕电粣娌琯nature闂傚倷绀侀幉锟犳偋閺囩姷绀婂┑鐘叉搐閸屻劑鏌曢崼婵愭Ч闁?
        if (area.getSignature() != null && area.getSignature().equals(playerName)) {
            return true;
        }
        
        // 闂傚倷绀侀幖顐﹀箯鐎ｎ喖闂柨婵嗩槸閻掑灚銇勯幒宥囧妽闁汇劍鍨块幃浠嬵敍濠靛浂浠╅梺閫炲苯澧版い銏狅工閳绘柨鈽夊┃澶告睏闂佸憡鐟ラˇ閬嶃€呴悜鑺ョ厪濠㈣鍨伴幊蹇涘Φ濠靛鐓涘璺猴功婢ф盯鏌涢妸銉ｅ仮闁挎繄鍋ら弫鍐磼濞戞ɑ鐤佹俊鐐€曠换鎰偓姘煎枤缁骞庣€瑰窏ename闂佽瀛╅鏍窗濡も偓鐓ゆ繝濠傜墕閺嬩線鏌曢崼婵愭Ч闁稿骸绉归弻娑㈠即閵娿儰绨介梺鍛婎焽閺佸寮?
        // 闂傚倷绀侀幉锟犮€冩径濞炬瀺闁哄洠鎳炴径鎰窛闁哄鍨奸幗鏇㈡⒑闂堟侗妾ч梻鍕閻熝囨⒒娴ｅ憡鍟為柣鏃戝墴濮婁粙宕熼顒冣偓鍨€掑锝呬壕閻庤娲橀〃濠傤嚕閻㈠憡鍋ㄩ柣銏犳啞閹啴姊绘担铏广€婇柡鍛箞瀹曠増鎯旈妸銉х暫闂佸搫娲㈤崹娲磿瀹ュ鐓曢柟鐑樼箖閹茬惫eName闂傚倷绀佸﹢閬嶁€﹂崼婢濇椽濡舵径濠勭暫濠电姴锕ゅΛ搴㈢瑜版帗鐓欓柟顖嗗啯姣愬銈冨€曢幊姗€寮婚敐鍛敠闁绘劦鍓氶悵鏂款渻閵堝棙鐓ョ紒澶屾嚀閻ｅ嘲顫濋鑺ヮ潔濠碘槅鍨甸崑鎰緞閸曨垱鈷戞慨鐟版搐婵″ジ鎮楀鐓庡⒋闁诡喒鈧枼鏋庨柟鎯х枃琚?
        for (AreaData otherArea : allAreas) {
            // 婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞闁归偊鍓涢ˇ褔姊洪崫鍕ⅱ闁轰焦鎮傞弻褔宕掗悙瀵稿幈闂佸啿鎼崯顐﹀几鎼淬劍鐓欐い鏃囧Г缁€瀣煙椤栨艾鏆ｇ€规洘宀搁妴鈧い顏嗩劋Name闂傚倷绀佸﹢閬嶁€﹂崼婢濇椽濡舵径濠勭暫濠电姴锕ゅΛ搴㈢瑜版帗鐓欓柟顖嗗啯姣愬銈冨€曢幊蹇旂┍婵犲浂鏁冮柣鏃囨腹婢规洟姊绘担鍛婃儓閻炴凹鍋婂畷鏇㈠礃濞村鐏侀梺纭呮彧缁犳垿寮伴妷鈺傜厽闁哄啫鐗婇弫閬嶆煕婵犲啫鈻曢柡灞诲妼閳藉螣閸噮浼冮梻浣藉瀹曠敻宕伴幇顔剧煓濠㈣埖鍔曞Λ姗€鏌涢…鎴濇灈缂侀鍘界换娑氣偓鐢殿焾鏍￠梺缁橆殕濞茬喖骞冮垾鏂ユ瀻闁规儳鐤囪闁诲骸绠嶉崕鍗炍涘▎鎴犳噮闂傚倷鑳剁划顖涚珶閺囥垹鐤炬繛鎴欏焺閺佸﹦鈧箍鍎遍ˇ顖滅矆閸℃绠鹃柟瀵稿剱閻掔晫绱掗幉瀣暤闁?
            if (area.getName().equals(otherArea.getBaseName()) && 
                otherArea.getSignature() != null && 
                otherArea.getSignature().equals(playerName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 闂傚倷鑳堕崕鐢稿疾濞戙垺鍋ら柕濞у嫭娈伴梺鍦檸閸犳牠骞戦崼鏇熺厪濠电偛鐏濇俊娲煕閻樿宸ユい顓炴健瀹曠懓鈽夊▎鎰絾闂備胶绮悧顒勫闯閿濆拋鍤曞ù鍏兼綑缁€鍫㈡喐鐎ｎ偆顩锋繛鎴欏灪閻撴洘绻涢崱妯忣亪鎮橀悢鍏肩厵濞撴艾鐏濇慨鍌溾偓娈垮枔閸斿繘濡甸幇鏉跨闁哄倽鍎婚幏顐︽⒑绾懎顥嶉柟娲讳簼娣囧﹪宕惰閺嬫棃鏌熸潏鍓х暠闁哄嫨鍎甸弻锝夊籍閸喐鏆犻梺绋匡攻婵炲﹪寮诲☉銏犵闁瑰灝鍟悾浠嬫⒑?
     * @param source 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囧櫝闁衡偓?
     * @param editableAreas 闂傚倷绀侀幉锟犳偡椤栫偛鍨傜€规洖娲﹂～鏇熴亜韫囨挻鍣虹紓宥呮喘閺岀喓鈧數顭堟牎闂佷紮缍佹禍鍫曞蓟濞戙垹鐓涢柛鎰╁妽閻庡姊虹拠鈥虫灍婵炶尙鍠庨悾宄邦潩椤戔晜妫冨畷鐔煎煘閹傚?
     * @param isAdmin 闂傚倷绀侀幖顐も偓姘卞厴瀹曡瀵奸弶鎴犵暰婵炶揪绲藉﹢閬嶅煝閺冨牊鐓ｉ煫鍥ㄥ嚬閸ゅ啴鏌涘锝呬壕闂傚倷娴囧畷鐢稿磻濞戞娑樜旈崨顓犵枀?
     */
    private static void displayEditableAreasInChat(ServerCommandSource source, List<AreaData> editableAreas, boolean isAdmin) {
        if (editableAreas.isEmpty()) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.altitude.dimension"));
            return;
        }

        java.util.UUID playerUuid = CommandSourceCompat.isExecutedByPlayer(source) ? ((ServerPlayerEntity) source.getEntity()).getUuid() : null;
        MutableText message = TextCompat.translatable("command.message.area.altitude.modify");
        for (AreaData area : editableAreas) {
            message.append(TextCompat.literal(" - " + area.getName() + ": ")).append(getHeightDisplayText(area.getAltitude(), playerUuid)).append(TextCompat.literal("\n"));
        }
        CommandSourceCompat.sendMessage(source, message);
    }
    
    /**
     * 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔惧帥婵炲牊顨嗘穱濠囶敍濮橆厽鍎撳銈嗘煥閿曨亪寮婚敐澶娢╅柕澶堝労娴犳儳鈹戦埥鍡楃仩妞わ箒浜崚鎺楀醇閵夈儰绱堕梺鍛婃处閸樹粙骞夐懡銈囩＝濞达綀顕栧▓锝囩磽閸屾稒鐨戦柟?
     * @param altitude 婵犲痉鏉库偓鏇㈠磹瑜版帗鏅梺璇叉唉椤绻涙繝鍌ゅ殨妞ゆ劧闄勯崑瀣煕椤愶絿绠橀柕?
     * @return 闂傚倷绀侀幖顐も偓姘煎墯閺呰埖绂掔€ｎ€附鎱ㄥΟ璇差暢闁稿鎸搁埥澶娾枎濡崵鏆俊鐐€栭崹鎶芥倿閿斿墽鐭?
     */
    private static MutableText getHeightDisplayText(AreaData.AltitudeData altitude, java.util.UUID playerUuid) {
        if (altitude == null) {
            return playerUuid != null ? TextCompat.literal(ServerI18nManager.translateForPlayer(playerUuid, "command.message.general_10")) : TextCompat.translatable("command.message.general_10");
        }

        MutableText result = TextCompat.empty();
        boolean hasMax = false;
        if (altitude.getMax() != null) {
            String label = playerUuid != null ? ServerI18nManager.translateForPlayer(playerUuid, "command.error.general_8") : ServerI18nManager.translate("command.error.general_8");
            result.append(TextCompat.literal(label + altitude.getMax()));
            hasMax = true;
        }
        if (altitude.getMin() != null) {
            if (hasMax) result.append(TextCompat.literal("闂?, "));
            String label = playerUuid != null ? ServerI18nManager.translateForPlayer(playerUuid, "command.message.general_11") : ServerI18nManager.translate("command.message.general_11");
            result.append(TextCompat.literal(label + altitude.getMin()));
            hasMax = true;
        }

        if (!hasMax) {
            return playerUuid != null ? TextCompat.literal(ServerI18nManager.translateForPlayer(playerUuid, "command.message.general_10")) : TextCompat.translatable("command.message.general_10");
        }
        return result;
    }
    
    /**
     * 闂備礁鎼ˇ閬嶅磿閹版澘绀堟慨姗嗗墰閺嗭箓鏌ょ喊鍗炲缁炬儳銈搁弻鈩冨緞鐎ｎ亞浠村銈嗘煥椤ㄦ墬婵犵數鍋為崹鍫曞箲娴ｇ硶鏋嶉柨婵嗘处閸嬫牗銇勯幇鍓佺暠闁?
     * @param dimensionId 缂傚倸鍊搁崐鐑芥倿閿曗偓椤洤鈻庨幋鏂夸壕闁革富鍘鹃崥濠?
     * @return 缂傚倸鍊搁崐鐑芥倿閿曗偓椤洤鈻庨幋鏂夸壕闁荤喐澹嗛敍宥夋煟濮橆剙鈷旈柟椋庡█閹崇娀顢栭挊澶夊?
     */
    private static String convertDimensionIdToType(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return Packets.DIMENSION_OVERWORLD;
        } else if (dimensionId.contains("nether")) {
            return Packets.DIMENSION_NETHER;
        } else if (dimensionId.contains("end")) {
            return Packets.DIMENSION_END;
        }
        return Packets.DIMENSION_OVERWORLD; // 婵犳鍠楃敮妤冪矙閹烘せ鈧箓宕奸妷顔芥櫍缂傚倷鐒﹁摫濞存嚎鍊濋弻锟犲磼濞戞﹩鍤嬬紓浣插亾闁逞屽墯缁绘盯骞嬮悙鏉戠）濡炪倖鏌ㄩ敃锔惧垝椤撶儐娼ㄩ柍褜鍓熼獮?
    }
    
    /**
     * 婵犵數濮伴崹鐓庘枖濞戞埃鍋撳鐓庢珝妤犵偛鍟换婵嬪礃閻愵剛鏆繝纰樻閸ㄤ即骞栭锔绘晩濠电姴娲﹂崑锝吤归敐鍥剁劸闁抽攱妫冮弻锝夊Χ閸涱噮妫ら梺褰掝棑婵炩偓闁哄被鍔庨埀顒婄秵閸撴岸宕?
     * @param player 闂傚倷鑳剁划顖涚珶閺囥垹鐤炬繛鎴欏焺閺?
     * @param areaName 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶銉ョ仼閻熸瑱濡囬埀顒€绠嶉崕鍗炩枖?
     * @param hasCustomHeight 闂傚倷绀侀幖顐も偓姘卞厴瀹曡瀵奸弶鎴犵暰婵炶揪缍€椤鍒婇幘顔藉仯闁诡厽甯掓俊浠嬫煛閸℃绠婚柡灞诲€栫缓鑺ュ緞婢跺瞼娉块梻浣风串缁插潡宕戦幘鍓佺煓濠电姴鍟欢鐐烘煙闁箑鏋涢柦鍌氼儔閺?
     * @param maxHeight 闂傚倷绀侀幖顐︽偋閸愵喖纾婚柟鐐墯閻斿棝鏌涢銏☆棞婵炲眰鍔忛妵鎰邦敍閻愯尙顔?
     * @param minHeight 闂傚倷绀侀幖顐︽偋閸愵喖纾婚柟鍓х帛閸婂爼鐓崶銊﹀暗缂佺姴顭烽弻锝呪攽閹邦剚鐏嶉梺?
     */
    public static void handleHeightRequest(ServerPlayerEntity player, String areaName, 
                                         boolean hasCustomHeight, Double maxHeight, Double minHeight) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);
            
            // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁稿绻堥弻宥嗘姜閹殿喛绐楅梺鍝ュ暱閸嬫捇鏌ｉ悢鍝ョ煂濠⒀勵殜楠炴劙宕ㄦ繝鍐╃彿闂佺粯顭囬弫鍝ュ婵傚憡鐓涘璺侯儏閻忋儲銇?
            String dimension = player.getWorld().getRegistryKey().getValue().toString();
            String dimensionType = convertDimensionIdToType(dimension);
            String fileName = Packets.getFileNameForDimension(dimensionType);
            
            if (fileName == null) {
                sendResponse(player, false, key("command.error.dimension_3"));
                return;
            }
            
            // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁活厽顨嗛妵鍕冀閵娧勫櫗闂佸憡鐟ョ换姗€寮婚敓鐘茬劦妞ゆ帒鍟ㄦ禍褰掓煙閻戞ɑ鐓ラ柦鍌氼儔閺岀喖鎳濋悧鍫濇锭婵犵鈧櫕鎼愰柍缁樻崌楠炲鏁冮埀顒勫及閵夆晜鐓熼柡鍐ㄧ墛閺侀亶鏌涙繝鍐ㄢ枙闁哄本鐩獮鎺楀箼閸愨晝鏆梻?
            List<AreaData> editableAreas = getHeightEditableAreas(fileName, playerName, isAdmin);
            
            // 闂傚倷绀侀幖顐ゆ偖椤愶箑纾块柛娆忣槺閻濊埖鎱ㄥ璇蹭壕濡ょ姷鍋為崝娆忕暦閹偊妲鹃梺鍝勬閸楁娊寮婚敐澶娢╅柕澶堝労娴犲ジ姊虹紒妯诲鞍闁荤啿鏅涢悾?
            AreaData targetArea = editableAreas.stream()
                    .filter(area -> area.getName().equals(areaName))
                    .findFirst()
                    .orElse(null);
            
            if (targetArea == null) {
                sendResponse(player, false, key("addhint.message.area_2"), lit(areaName), key("command.message.altitude.modify.permission"));
                return;
            }
            
            // 婵犲痉鏉库偓妤佹叏閹绢喗鍎楀〒姘ｅ亾闁诡垯鐒﹀鍕偓锝呯仛閻ｈ鈹戦埥鍡楃仩闁圭⒈鍋婇、娆愮節閸ャ劎鍘告繛杈剧到閹碱偊銆傞懖鈹惧亾?
            if (hasCustomHeight) {
                if (maxHeight != null && (maxHeight < -64 || maxHeight > 320)) {
                    sendResponse(player, false, key("command.button.altitude_4"));
                    return;
                }
                if (minHeight != null && (minHeight < -64 || minHeight > 320)) {
                    sendResponse(player, false, key("command.button.altitude_3"));
                    return;
                }
                if (maxHeight != null && minHeight != null && maxHeight < minHeight) {
                    sendResponse(player, false, key("command.message.altitude_13"));
                    return;
                }
            }
            
            // 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樺弶鎼愰柡鍕╁劦閺岋綁寮崼鐔告殸闂佺锕ら崯鑳亙闂佺鏈懓浠嬵敂閸″繐浜鹃悷娆忓閳绘洜鈧鍣崑濠囧箖瑜斿畷鐓庘攽閸稑浜?
            AreaData.AltitudeData newAltitude = hasCustomHeight ? 
                    new AreaData.AltitudeData(maxHeight, minHeight) : null;
            
            targetArea.setAltitude(newAltitude);
            
            // 婵犵數鍎戠徊钘壝洪敂鐐床闁稿瞼鍋為崑銈夋煏婵炵偓娅呯紒鈧崱娑欑厽婵°倐鍋撻柣妤€锕幃妤咁敊濞?
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            List<AreaData> allAreas = FileManager.readAreaData(areaFile);
            
            // 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樺弶鎼愮紒鐘卞嵆閺屾盯濡烽幋婵囧櫤婵絽閰ｅ铏瑰寲閺囩喐鐝栭梺鍝ュТ闁帮綁鐛?
            for (int i = 0; i < allAreas.size(); i++) {
                if (allAreas.get(i).getName().equals(areaName)) {
                    allAreas.set(i, targetArea);
                    break;
                }
            }
            
            FileManager.writeAreaData(areaFile, allAreas);
            
            // 闂傚倸鍊烽悞锕併亹閸愵亞鐭撻柣銏㈩焾閽冪喎鈹戦悩鍙夋悙缂佲偓閸℃稒鐓曢柍鈺佸枤濞堟梻绱掗崣澶嬨仢闁哄备鍓濋幏鍛村传閸曞灚姣夐柣搴ゎ潐閹哥兘鎳楅崜浣诡潟闁圭儤鍨熼弸搴ㄦ煙闁箑澧婚柡鍡節濮婃椽宕崟顓烆暫濡炪倧绠掓禍顒勫极椤曗偓閹煎綊宕楁径濠佸缂佺虎鍘奸幏瀣疮閸モ晝纾奸弶鍫涘妿閹冲洭鏌熼姘伀缂侇喗鐟ч幑鍕Ω椤噮浜濈换娑氣偓娑欘焽閻﹪鎮楀☉鎺撴珕闁哄懎鐖奸幃鍓т沪閹傜钵闂佽绻掗崑鐘诲磻閹伴偊鏁傞弶鍫涘妿缁犻箖鏌涢埦鈧弲婊堝箠濮婃樄oad闂傚倷绀佸﹢閬嶁€﹂崼婢濇椽鏁愭径濠勵唴闂侀潧鐗嗛ˇ浼村疾?
            // 婵犵數鍋犻幓顏嗙礊閳ь剚绻涙径瀣鐎?sendAllAreaDataToAll() 缂傚倷鑳堕搹搴ㄥ矗鎼淬劌绐楅柡鍥╁У瀹曞弶鎱ㄥΟ鎸庣【缂佺姰鍎甸弻宥堫檨闁告挾鍠庨锝夊垂椤愩垻绐為柣蹇曞仜閳ь剛鍠撻ˇ浼存⒑鐠囧弶鎹ｉ柟铏崌閹儲绺界粙璺槯濠殿喗銇涢崑鎾绘煙閾忣偒娈滈柟宕囧仱婵＄兘鏁傞懞銉ф闂傚倷绀侀幉锛勬暜閻愬瓨娅犳俊銈傚亾閻撱倝鏌涜椤ㄥ懎螞濮椻偓閹粙顢涢敐鍛亾缂備讲鍋撳鑸靛姈閻撴盯鏌涢幇鍏哥盎闂夊顪冮妶鍡樷拹闁搞劌澧庣划瀣箳濡も偓閸愨偓闂佹寧姊婚弲顐﹀箯閵娾晜鈷戦梻鍫氭櫈缁€瀣煕閺傝法鐒告鐐搭殜婵偓闁挎稑瀚ч弸鏍ь渻閵堝懐绠版俊顐ｇ懃铻ｅ┑鐘叉搐缁?
            areahint.network.ServerNetworking.sendAllAreaDataToAll();
            
            // 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨呴悞鍨亜閹达絾纭堕柛鏂跨Ч閺屾盯濡搁敃鈧崫铏光偓瑙勬礃缁诲牓骞冮姀銈嗙叆閻庯綆鍋勯崝宀勬⒑?
            if (hasCustomHeight) {
                String maxStr = maxHeight != null ? String.valueOf(maxHeight) : "";
                String minStr = minHeight != null ? String.valueOf(minHeight) : "";
                sendResponse(player, true, key("addhint.message.area_2"), lit(areaName), key("command.message.altitude_4"),
                    maxHeight != null ? lit(maxStr) : key("command.message.general_25"), lit(" ~ "),
                    minHeight != null ? lit(minStr) : key("command.message.general_25"));
            } else {
                sendResponse(player, true, key("addhint.message.area_2"), lit(areaName), key("command.message.altitude_5"));
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to handle sethigh request", e);
            sendResponse(player, false, key("command.error.altitude_9"));
        }
    }
    
    /**
     * 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鎼佹⒒娴ｅ搫甯舵い銊ュ缁傚秴顭ㄩ崼鐔蜂患闂佺粯鍔﹂崜娑㈠煝閺囩儐鐔嗛柤鎼佹涧婵鎲搁柇锕€鐏︽鐐寸墬濞煎繘宕滆閺嗙娀姊虹憴鍕伇闁告挻绻勭划?
     * @param context 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囶棤闁崇粯鏌ㄩ埞鎴︽偐鏉堫偄鍘￠梺鑽ゅ枑閹瑰洭寮?
     * @param areaName 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶銉ョ仼閻熸瑱濡囬埀顒€绠嶉崕鍗炩枖?
     * @return 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鍝ョ磽閸屾艾鈧兘鎮為敃鍌氱濠电姵纰嶉崐?
     */
    public static int executeSetHighCustom(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        // 闂傚倸鍊风欢锟犲磻閸涱喚鈹嶉柧蹇氼潐瀹曟煡鏌涢幇闈涙灈闁告劏鍋撻梻浣规偠閸庢粓宕橀埡鍐ㄨ闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵愭Ч闁绘帞绮幈銊ノ熺紒妯荤€繝銏ｎ潐濞茬喖寮诲☉妯锋瀻闁归偊鍓涙导宀勬⒑闁偛鑻崢鍝ョ磼閳ь剚绗熼埀顒€鐣烽崫鍕殕闁逞屽墴閸┾偓妞ゆ巻鍋撶紒鐘茬Ч瀹曠懓煤椤忓嫭妲┑鐐村灦濮樸劑宕?
        areahint.network.ServerNetworking.sendCommandToClient((ServerPlayerEntity) source.getEntity(), "areahint:sethigh_custom:" + areaName);
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶绋库攽閻愭潙鐏﹂柟绋挎憸缁棃鎮烽柇锔锯偓鑸电箾瀹割喕绨荤紒鈧崱娑欑厱妞ゆ劧绲剧粈鍫㈡喐闁箑鐏︽鐐寸墬濞煎繘宕滆閺嗙娀姊虹憴鍕伇闁告挻绻勭划?
     * @param context 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囶棤闁崇粯鏌ㄩ埞鎴︽偐鏉堫偄鍘￠梺鑽ゅ枑閹瑰洭寮?
     * @param areaName 闂傚倷鑳剁涵鍫曞疾濠靛绐楅幖娣妼缁犳牠鏌￠崶銉ョ仼閻熸瑱濡囬埀顒€绠嶉崕鍗炩枖?
     * @return 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鍝ョ磽閸屾艾鈧兘鎮為敃鍌氱濠电姵纰嶉崐?
     */
    public static int executeSetHighUnlimited(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        // 闂傚倷鑳堕崕鐢稿疾濞戙垺鍋ら柕濞у嫭娈伴柣搴㈢⊕閿氱紒鍓佸仱閺屾盯寮撮妸銉ョ闂佺锕﹂崑娑㈠煡婢舵劕绠荤€规洖娉﹁娣囧﹪鎮欓浣典虎閻庤娲樺畝绋跨暦婵傜鍗抽柣妯垮蔼閹奉偊姊虹涵鍛棈闁规椿浜炵划濠氬箳濡ゅ﹥鏅銈嗗坊閸嬫捇寮?
        handleHeightRequest((ServerPlayerEntity) source.getEntity(), areaName, false, null, null);
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鎼佹⒒娴ｅ憡鍟為柣鐕傜畵閹兾旈崘顏嗙厠闂備礁鐏濋鎰枔濡や椒绻嗘い鏍ㄧ閹牊銇勯弬鍖¤含闁诡喛顫夐幏鍛矙鎼存挻瀵栭梻浣圭湽閸婃洖螞濠靛棛鏆﹂柕濞炬櫅缁狅綁鏌熼悜妯诲碍鐞?
     * @param context 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囶棤闁崇粯鏌ㄩ埞鎴︽偐鏉堫偄鍘￠梺鑽ゅ枑閹瑰洭寮?
     * @return 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶鍝ョ磽閸屾艾鈧兘鎮為敃鍌氱濠电姵纰嶉崐?
     */
    public static int executeSetHighCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.altitude.cancel"));
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨呴悞鍨亜閹达絾纭舵い锔肩畵閺岀喐绺介崨濠冩殸闂佽鍨卞Λ鍐嚕椤掑嫬鍨傛い鏃€鍎崇敮鎾绘⒑鐠囨煡顎楃紒鐘茬Ч瀹曠懓煤椤忓嫭妲┑鐐村灦濮樸劑宕?
     * @param player 闂傚倷鑳剁划顖涚珶閺囥垹鐤炬繛鎴欏焺閺?
     * @param success 闂傚倷绀侀幖顐も偓姘卞厴瀹曡瀵奸弶鎴犵暰婵炴挻鍩冮崑鎾搭殽閻愬樊鍎旀鐐叉喘椤㈡鍩€椤掑嫸缍?
     * @param message 濠电姷鏁搁崑鐐哄垂閻㈠憡鍋嬪┑鐘插暙椤?
     */
    private static void sendResponse(ServerPlayerEntity player, boolean success, Part... parts) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(success);
            TranslatableMessage.write(buf, parts);
            ServerPlayNetworking.send(player, Packets.S2C_SETHIGH_RESPONSE, buf);
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to send sethigh response", e);
        }
    }
    
    /**
     * 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨呴悞鍨亜閹达絾纭舵い锔奸檮閵囧嫰骞樺畷鍥┬ㄥΔ鐘靛仜椤戝棝藝閻楀牊鍎熸い鏃囧吹绾句粙姊绘担鍛婂暈缂佸甯″畷鐟扳攽鐎ｎ亞顔?
     * @param source 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁狀噣鏌曢崼婵囧櫝闁衡偓?
     * @param command 闂傚倷绀侀幉锛勭矙閹烘鍨傛繝闈涱儏缁?
     * @param args 闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮?
     */
    private static void sendClientCommand(ServerCommandSource source, String command, String... args) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(command);
            buf.writeInt(args.length);
            for (String arg : args) {
                buf.writeString(arg);
            }
            
            ServerPlayNetworking.send(source.getPlayer(), new Identifier(Packets.S2C_CLIENT_COMMAND), buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error("Failed to send client command for sethigh", e);
        }
    }
} 
