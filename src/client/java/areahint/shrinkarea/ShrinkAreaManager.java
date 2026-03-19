package areahint.shrinkarea;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.I18nManager;
import areahint.util.AreaDataConverter;
import areahint.shrinkarea.ShrinkAreaClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 闂備胶纭堕弲婵嬪窗鎼淬劌绠栭柡鍥ュ灩缂佲晠鏌涢幘鑼跺厡缂佽鲸姘ㄧ槐鎺楁偑閸涱垳锛熼梺璇″枛閿曨亜鐣?
 *
 * 闂佽崵濮甸崝妤呭窗閺囥垺鍎楁俊銈呮噹閺嬩胶绱撻崼銏犫枅闁搞倕顑夐弻娑㈠煛閸愩劍鐎婚梺璇″枟濞茬喎顕ｉ妸鈺佺濞达綀娅ｇ粈鍡椻攽閻愮數绡€闁煎啿鐖奸崺銏＄附閸涘﹤鍓梺鍛婃处閸欏酣宕ラ埀顒勬⒑閼姐倕浠滄俊顐ｎ殜楠炲繑瀵奸弶鎴狀攨闂婎偄娲﹀銊モ枔閸洘鐓ユ繛鎴炆戝﹢鐗堜繆?ExpandAreaManager 闂備焦鐪归崝宀€鈧凹鍓欓锝夋嚍閵壯€鏋欐繝銏ｆ硾閼活垶銆傛總鍛婄厸?
 *
 * === 闂備礁鎲″濠氬窗閺囥垹绀傛俊顖氱毞閸嬫挻鎷呴崘鐐秷闂?===
 *
 * ExpandAreaManager闂備焦瀵х粙鎴︽偋婵犲啯娅犻悹鎭掑妺濞岊亪鏌￠崶鏈电敖缂佲偓?
 * - 闂備線娼荤拹鐔煎礉瀹€鈧弫顕€顢曢敃鈧弰銉╂煟閺冨牊鏁遍柛濠傚暱椤潡鎳滈悽闈涒拰闂佺瀛╁娆掑絹闁荤姴娲﹁ぐ鍐綖閿濆鐓涢柛灞剧⊕缁舵煡鎮樿箛锝呯仸鐎规洘妞介幃銈夊磼濠婂拋妲烽梻浣告贡椤ｄ粙寮插☉銏犖﹂柛銉墮閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婇弻銈夋偂鎼粹剝娈┑?
 * - 闂備礁鎲＄敮鐐寸箾閳ь剚绻涢崨顓㈠弰鐎规洘锕㈠畷銊╁级閹存繂袚濠碉紕鍋戦崐鏇熸櫠鎼淬劌纾婚柨婵嗩槹閺咁剟鎮橀悙璺轰汗缂佺姳绮欓弻锝夋晲閸℃瑦鍣у銈忕畱椤兘寮婚崼銉ノ╅柍鐟扮氨閸嬫挻绻濋崶銊モ偓?
 *
 * ShrinkAreaManager闂備焦瀵х粙鎴︽偋婵犲洤鏋侀柛锔诲幘绾句粙鏌嶉崫鍕靛剳缂佲偓?
 * - 闂備線娼荤拹鐔煎礉瀹€鈧弫顕€顢曢敃鈧弰銉╂煟閺冨牊鏁遍柛濠冨▕閺屾盯骞囬浣告畻闂佺瀛╅幐鍐差嚕娴犲鐐婃い蹇撶Т閺佸綊姊虹悰鈥充壕闂佸憡鎸烽懗鍓佹濮樿埖鐓曢柡宓嫭鍎撻梺缁樻尰閻熲晠寮鍛殕闁逞屽墰缁辨挻寰勭仦鑲╁墾闂婎偄娲﹂幐楣冨储閻㈠憡鐓曟慨姗嗗墰鍟搁柣搴＄仛閻楃姴鐣?
 * - 闂備礁鎲＄敮鐐寸箾閳ь剚绻涢崨顓烆劉鐎垫澘瀚板浠嬵敇閻愭彃袚濠碉紕鍋戦崐鏇熸櫠鎼淬劌纾婚柨婵嗩槹閺咁剟鎮橀悙璺轰汗缂佺姳绮欓弻锝夋晲閸℃瑦鍣ч梺鍝ュ仦閹告娊寮婚崼銉ノ╅柍鐟扮氨閸嬫挻绻濋崶銊モ偓?
 *
 * === 闂備礁鎼ˇ顖炲疮閺夋埈鐎堕柧蹇氼潐閸犲棝鏌涢弴銊ヤ簻闁诲繒鍠栭弻銊モ槈濡厧顣圭紓?expandarea 濠电偞鍨堕幐鎾磻閹剧粯鐓犻柣銈庡灡瑜把呯磼?==
 *
 * 1. 缂傚倷鑳舵刊瀵告閺囥垹绠栧┑鐘叉搐瀹告繃鎱ㄥΔ鈧悧蹇曠矆閸曨垱鐓涘璺猴工閺嗭絾淇婇幑鎰仩缂侇喖鐏氬鍕暆閸曨厼绀?闂備焦瀵х粙鎴βㄩ埀顒傜磼閸欐ê宓嗙€规洩缍侀、娑樷枎鎼达綁鐎洪梻浣姐€€閸嬫捇鏌涢幘鑼跺厡缂佽鲸宀搁弻鐔哄枈濡桨澹曢梻浣告惈閻楀棝藝椤栫偞鍋傛繛鍡樻尭鐟?
 * 2. 闂備礁鎼幏瀣闯閿濆鐒垫い鎺嶇劍閻ㄦ垿鎮介婵囶仩闁逞屽墰椤㈠﹪顢欓幇顔筋潟?
 *    - 闂備礁鎲￠悷顖炲垂椤栨稓顩查柟鐑橆殔缂佲晠鏌涢幘鑼跺厡缂佽鲸宀搁弻銈嗙附婢跺鐩庣紓浣筋唺缁舵艾鐣峰顑芥婵☆垵鍋愭径鍕⒑濮瑰洤濡奸悗姘煎墴閹€斥枎閹惧疇袝閻庡箍鍎辩换鎺旂矆閸撳攨gnature 缂傚倷鐒︾粙鎴λ囬鎹愬С妞ゆ帒瀚崑婵嬫煃鏉炴壆璐伴柛鐔插亾闂備礁鎲￠懝鍓х矙閸曨厽顫?
 *    - 闂備礁鎲￠悷顖炲垂椤栨稓顩查柟鐑橆殔缂佲晠鏌涢幘鑼跺厡缂?basename 闁诲孩顔栭崰妤€煤濠婂牆鏋侀柕鍫濐槹閸ゅ﹥銇勮箛鎾愁伀缂佺姵甯￠弻锝夊Ω閵夈儺浠奸梺缁樻尰閻熲晛鐣烽妷銉悑闊洦娲滈ˇ銈渁sename 闂佽娴烽弫鎼佸箠閹炬儼濮抽柡灞诲劚閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婇弻?signature 缂傚倷鐒︾粙鎴λ囬鎹愬С妞ゆ帒瀚崑婵嬫煃鏉炴壆璐伴柛鐔插亾闂備礁鎲￠懝鍓х矙閸曨厽顫?
 *
 * === 闁诲氦顫夐幃鍫曞磿閹殿喚绀婇柡鍌涳紩鐟欏嫭濯撮悶娑掑墲閻?===
 *
 * 1. 闂備礁鎲￠崙褰掑垂閻楀牊鍙忛柍鍝勬噹缂佲晠鏌涢幘鑼跺厡缂佽鲸纰嶇换娑氱礄閻樺搫鍘￠梺?(start)
 * 2. 闂備礁鎲″缁樻叏閹灐褰掑炊椤掆偓閻銇勯弽銊р姇闁哄棭浜炵槐鎾诲磼濡や焦鐝栧┑鐐茬墛閸ㄥ灝鐣峰ú顏呭亜闂佸灝顑呴埀顒佸▕閺屾盯骞掗幙鍐╃暯闂?
 * 3. 闂備胶绮竟鏇㈠疾濞戙埄鏁婄€广儱顦伴悞璇差熆鐠轰警鍎忔い蹇嬪劦閹兘寮村鍗炲闂佽桨绀佸﹢杈╂閹捐鐓涢柛鎰ㄦ櫇閺嗙娀姊虹化鏇熸珖闁哥姴閰ｉ獮?
 * 4. 闂佽崵濮抽悞锕€顭垮Ο鑲╃鐎广儱顦紒鈺呮煕閹捐尪鍏岀紒杈ㄥ哺閺屾盯寮借閹牓鏌ｉ幘瀵告噰闁诡垰鍟村畷鐔碱敍濡嘲浜惧┑鐘叉处閸婄兘鏌ｈ閹芥粎绮堥崟顖涚厵缂佸顑欏Σ鍏笺亜閺傛寧绌块柕鍥ㄥ姍瀵挳濮€閳╁啯顔嗛梻?
 * 5. 闂佽娴烽幊鎾诲嫉椤掑嫬鍨傛慨妯块哺婵ジ鏌℃径搴㈢《缂佸瀵ч〃銉╂倷閺夋垵濮稿銈嗘尭閹芥粎鍒掔拠鍙傛梹鎷呯憴鍕槕缂傚倸鍊搁崐鐢稿疾濞戙垹绠栨俊銈呮噺閸庡秹鏌涢弴銊ヤ簽闁告鏁婚弻?
 * 6. 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶椤︼箓鏌涢埡浣虹劯鐎殿噣娼ч濂稿川椤撗勫尃缂傚倷鐒﹀Λ蹇涘垂椤栨粍宕叉慨妯诲閸嬫挸鈽夊▍顓т邯閹崇喖鎮㈤悡搴ば曢柣蹇曞仧閾忓酣宕?
 */
public class ShrinkAreaManager {
    private static ShrinkAreaManager instance;
    
    // 闂備胶绮…鍫ュ春閺嶎厼鐒垫い鎴ｅ劵閸忓本銇勯幘瀛樺€愰柟?
    private boolean isActive = false;
    private ShrinkState currentState = ShrinkState.IDLE;
    
    // 闂備胶纭堕弲婵嬪窗鎼淬劌绠栭柡鍥ュ灪閸庡海绱掔€ｎ亞浠㈢憸?
    private List<AreaData> availableAreas = new ArrayList<>();
    private AreaData selectedArea = null;
    private String playerName = "";
    private boolean isAdmin = false;
    
    // 闂備浇銆€閸嬫捇鏌涢幘鑼跺厡缂佽鲸宀搁弻娑㈠冀瑜庨幆鍫ユ煟閹惧鎳勯柕鍥у瀹曟粍鎷呴梹鎰
    private List<AreaData.Vertex> shrinkVertices = new ArrayList<>();
    private boolean isRecording = false;
    
    // UI缂傚倷鑳舵刊瀵告閺囥垹绠?
    private ShrinkAreaUI ui;
    
    /**
     * 闂備胶绮…鍫ュ春閺嶎厼鐒垫い鎴ｆ硶閸斿秹鏌ｈ箛鎾村缂?
     */
    public enum ShrinkState {
        IDLE,           // 缂傚倷绀侀張顒€顪冮挊澹╂稒绂掔€ｎ偄浜归梺鐓庢憸椤ｄ粙宕?
        SELECTING_AREA, // 闂傚倷绶￠崑鍕囬幍顔瑰亾濮樸儱濮傜€规洘妞介幃銏ゆ煥鐎ｎ亖鍋撻幎鑺ョ厽婵☆垰鐏濋悡鎰版煃?
        RECORDING,      // 闂佽崵濮抽悞锕€顭垮Ο鑲╃鐎广儱妫庢禍婊堟煕閹捐尙顦﹀ù鐙€鍨堕弻锝呂熼崹顔惧帿闂?
        CALCULATING,    // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閸嬫劙鏌ら崫銉毌闁?
        CONFIRMING      // 缂備胶铏庨崣搴ㄥ窗濞戙埄鏁囧┑鐘崇閸嬫劙鏌ら崫銉毌闁?
    }
    
    private ShrinkAreaManager() {
        this.ui = new ShrinkAreaUI(this);
    }
    
    public static ShrinkAreaManager getInstance() {
        if (instance == null) {
            instance = new ShrinkAreaManager();
        }
        return instance;
    }
    
    /**
     * 闂備礁鎲￠崙褰掑垂閻楀牊鍙忛柍鍝勬噹閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婇弻锟犲焵椤掑嫬绀堝ù锝堟缁€鍡椻攽閻愮數绡€闁煎啿鐖奸崺?
     */
    public void start() {
        if (isActive) {
            sendMessage(I18nManager.translate("shrinkarea.error.area.shrink"), Formatting.RED);
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        
        if (player == null) {
            sendMessage(I18nManager.translate("shrinkarea.error.general"), Formatting.RED);
            return;
        }
        
        playerName = player.getGameProfile().getName();
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闁汇埄鍨靛▍锝夊焵椤掑偆鏀伴柛鈺傜墪椤洭宕奸妷锔规寖闂佽婢樻晶搴ｇ矆閸曨厾纾奸柣姗嗗亜娴滈箖姊洪崨濠庢畷婵炲弶锕㈣棢闁告稒娼欓拑鐔兼煏婢舵盯妾粭鎴︽⒑閸涘﹦鍟茬紓鍌涙皑閹峰綊鎮滅粈鐔兼⒑閹稿海鈽夐柣顒傚帶閿曘垽鏁嶉崟銊ヤ壕闁革富鍘兼禒锔界箾閻撳海澧︽慨濠佺矙楠炲洭顢欓懞銉︻啅闂備礁鎼悧鍡欑矓鐎涙ɑ鍙忛柣鏃囨閸楁碍銇勯弽顐户鐎规洩缍侀幃褰掑炊閿斿墽鐡樼紓?
        isAdmin = client.player.hasPermissionLevel(2);
        
        isActive = true;
        currentState = ShrinkState.SELECTING_AREA;
        
        sendMessage(I18nManager.translate("shrinkarea.message.area.shrink.start"), Formatting.GREEN);
        sendMessage(I18nManager.translate("shrinkarea.message.area"), Formatting.YELLOW);
        
        // 闂備礁鎲″缁樻叏閹灐褰掑炊椤掆偓閻銇勯弽銊х煁闁哄棗绻橀弻娑㈠煛閸愩劍鐎婚梺?
        loadAvailableAreas();
        
        // 闂備礁鎼€氼剚鏅舵禒瀣︽慨妯挎硾閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婂娲敃閵忊晜效闁诲孩鍝庨崝鎴﹀箖閹呮殕闁告洦鍓氶妴?
        ui.showAreaSelectionScreen();
    }
    
    /**
     * 闂備胶顭堥鍡欏垝瀹ュ鏁嗘繛鎴欏灩閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婇弻锟犲焵椤掑嫬绀堝ù锝堟缁€鍡椻攽閻愮數绡€闁煎啿鐖奸崺?
     */
    public void stop() {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        currentState = ShrinkState.IDLE;
        
        // 婵犵數鍋為幐鎼佸箠閹版澘绠栧┑鐘叉处閸嬫劙鏌ら崫銉毌闁?
        reset();
        
        ui.showCancelMessage();
    }
    
    /**
     * 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅ〒姘ｅ亾鐎规洘妞介幃銏ゆ煥鐎ｎ亖鍋撻幎鑺ョ厱婵﹩鍓涜倴閻炴哎鍔戝娲敃閵忊晜效闁诲孩鍝庨崝鎴濈暦濞差亝鍋勯梺鍨儏閳?
     */
    public void selectAreaByName(String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            sendMessage(I18nManager.translate("dividearea.error.area"), Formatting.RED);
            return;
        }

        // 缂傚倷绀侀ˇ顖炩€﹀畡鎵虫瀺閹肩补妲呴崵鏇㈡煛閸モ晛浠滃┑鐐叉喘閺屻劌鈽夊Ο鐓庮暫濡炪倐鏁崶褍鍤戝┑鐘诧工閸熶即骞楅悩缁樼厱闁挎棁宕甸崢婊呯磼?
        String cleanedName = areaName.trim();
        if (cleanedName.startsWith("\"") && cleanedName.endsWith("\"") && cleanedName.length() > 1) {
            cleanedName = cleanedName.substring(1, cleanedName.length() - 1);
        }

        // 濠电偛顕慨瀛橆殽閹间礁绀勯柨鐔哄Т缁€澶愭煟濡厧鍔嬬紒浣峰嵆閺岋綁濡搁妷銉患閻熸粎澧楅悡锟犲箖娴犲惟闁靛／鍐ㄧ闂備礁鎲￠懝鍓х矓閹绢喖鍨傛い蹇撴閸嬫﹢鏌曟繛鍨偓娑㈠储椤掑嫭鐓涢悘鐐额嚙閸旀粌顭跨憴鍕惰€跨€规洘妞介幃銏ゆ煥鐎ｎ亖鍋撻幎鑺ョ叆婵炴垶顭囨晶锝夋煃瑜滈崜娆愮附閺冨倻绠斿鑸靛姇閸欏﹥銇勯弽顐粶婵炲懌鍨介弻锟犲磼閻戝棙娈ョ紓渚囧枤婵炩偓鐎殿噮鍓熷畷鍫曟晜缁涘浠洪梻浣告啞濮婄粯鎱ㄩ幆顬″綊宕堕浣规珫?
        AreaData area = null;
        for (AreaData a : availableAreas) {
            if (a.getName().equals(cleanedName)) {
                area = a;
                break;
            }
        }

        if (area == null) {
            sendMessage(I18nManager.translate("addhint.message.area_2") + cleanedName + I18nManager.translate("shrinkarea.message.shrink.permission"), Formatting.RED);
            sendMessage(I18nManager.translate("shrinkarea.prompt.area.shrink.permission"), Formatting.GRAY);
            return;
        }

        // 闂傚倷绶￠崑鍕囬幍顔瑰亾濮樸儱濡块柟椋庡█婵＄兘濡疯楠炲秹姊洪崨濠呭缂佸瀚Σ鎰枎閹惧磭楠囬梺鍛婂姦閸犳顢撳▎鎴斿亾閻熺増鍟炵憸鏉垮暙閿曘垽鎮惀鐢AvailableAreas濠电偞鍨堕幖鈺呭储娴犲瑤澶愬川閺夋垼鎽曢梺闈涱樈閸ㄥ磭绮?
        handleAreaSelection(area);
    }

    /**
     * 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崶褎妲梺缁樻閺€閬嶅磹閹惰姤鈷戞い鎰╁焺濡插綊鎮?
     */
    public void handleAreaSelection(AreaData selectedArea) {
        this.selectedArea = selectedArea;

        sendMessage(I18nManager.translate("dividearea.prompt.area") + AreaDataConverter.getDisplayName(selectedArea), Formatting.GREEN);
        sendMessage(I18nManager.translate("addhint.message.general_2") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("shrinkarea.message.vertex.record.shrink"), Formatting.YELLOW);
        sendMessage(I18nManager.translate("shrinkarea.button.area.record.save"), Formatting.GRAY);

        // 闁诲孩顔栭崰鎺楀磻閹炬枼鏀芥い鏃傗拡閸庢挻銇勯弬鎸庣┛闁靛洦鍔欏鎾偄鐏炶偐顦︽い?
        startRecording();
    }

    /**
     * 闁诲孩顔栭崰鎺楀磻閹炬枼鏀芥い鏃傗拡閸庢挻銇勯弬鎸庣┛闁靛洦鍔欏鎾綖椤戣棄浜惧┑鐘叉处閸?
     */
    private void startRecording() {
        this.isActive = true;
        this.isRecording = true;
        this.shrinkVertices.clear();
    }

    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闁汇埄鍨靛▍锝夊焵椤掑偆鏀伴柛鈺傜墪鍗遍柟闂寸鐟欙箓鏌ㄩ弮鍥撶紒鐘冲浮閺屸剝寰勬繝鍌涙濠碘€冲⒔閸庛倗绮氶柆宥庢晩闁兼亽鍎插В澶愭⒑閸︻収鐒炬繛灞傚€濋幃鐐偅閸愩劍妲梺缁樻閺€閬嶅磹?
     */
    private boolean checkPermission(AreaData area) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        String playerName = client.player.getGameProfile().getName();

        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠碘€冲级閹倸鐣烽妷鈺傛櫆闁兼剚鍨抽ˇ鎵磽娴ｅ搫顎撶紒杈ㄦ礋楠炲啯绻濋崶褍绐涘┑顔筋殔閻楀繒绮堥崟顖涚厸濠㈣泛锕ら弳锝嗕繆閹规劖顏犵紒顔肩仛瀵板嫬鐣濋崟顓炵2闂?
        if (client.player.hasPermissionLevel(2)) {
            return true;
        }

        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠碘€冲级閹倸鐣烽妷鈺傛櫆闁兼剚鍨抽ˇ浼存⒑缁洘娅囬柛鐘查叄楠炲啴寮撮姀鈥冲壄闂佸憡甯╅幗鐒name闁诲孩顔栭崰妤€煤濠婂牆鏋侀柕鍫濐槹閸嬫繈鏌嶆潪鎵窗闁哥啿鍋?
        if (area.getBaseName() != null) {
            AreaData baseArea = findAreaByName(area.getBaseName());
            if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                return true;
            }
        }

        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠碘€冲级閹倸鐣烽妷鈺傛櫆闁兼剚鍨抽ˇ浼存⒑缁洘娅囬柛鐘查叄楠炲啴寮撮姀鐘殿槴濠电偞鍨堕…鍥╂閿曞倹鐓?
        return playerName.equals(area.getSignature());
    }

    /**
     * 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅ〒姘ｅ亾鐎规洏鍎遍濂稿炊閸℃瑥浠辩€殿喖顭锋俊鐑芥晝閳ь剟藟濠靛鐓曢柍鍝勫暙閺嬪酣鏌?
     */
    private AreaData findAreaByName(String name) {
        List<AreaData> areas = loadAllAreas();
        for (AreaData area : areas) {
            if (area.getName().equals(name)) {
                return area;
            }
        }
        return null;
    }

    /**
     * 闂備礁鎲″缁樻叏閹灐褰掑炊椤忓倷姹楅梺瑙勫劤閸熷潡路閸涱垳纾藉ù锝堫潐缂嶆垿鎮楃憴鍕枙闁诡垰鍟村畷鐔碱敆閳ь剟銆傞弻銉︾厸闁割偅绻嶅Σ鎼佹煟閹惧鎳囩€?
     */
    private List<AreaData> loadAllAreas() {
        List<AreaData> areas = new ArrayList<>();
        try {
            // 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉簵娴滃綊鏌熼幆褍鏆辨い銈呮噽缁辨挻鎷呯憴鍕▏闁?
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
                String currentDimension = client.world.getRegistryKey().getValue().toString();
                String fileName = getFileNameForCurrentDimension(currentDimension);

                if (fileName != null) {
                    java.nio.file.Path areaPath = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
                    System.out.println("DEBUG: 闂佽绻愮换鎴犳崲閸℃稒鍎婃い鏍仜缁€澶愭煟濡厧鍔嬬紒浣峰嵆閺屾盯鍩￠崘銊︾€婚梺璇″枟濞茬喎顕ｉ鍕倞闁挎梻鐡旈崑? " + areaPath);

                    if (areaPath.toFile().exists()) {
                        areas = FileManager.readAreaData(areaPath);
                        System.out.println("DEBUG: loaded " + areas.size() + " areas from " + fileName);
                    } else {
                        System.out.println("DEBUG: 闂備礁鎼崐绋棵洪敐鍛瀻闁靛骏绱曢埢鏃傗偓骞垮劚閹虫劙骞楅悩缁樼厱? " + fileName);
                    }
                } else {
                    System.out.println("DEBUG: file does not exist for dimension");
                }
            } else {
                System.out.println("DEBUG: no dimension id available");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: 闂備礁鎲″缁樻叏閹灐褰掑炊椤掆偓閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婇弻锟犲磼濮橆厾鐓戝┑鐐叉閸ㄥ搫顕ラ崟顒佺秶妞ゆ劑鍎? " + e.getMessage());
            e.printStackTrace();
        }
        return areas;
    }

    /**
     * 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅù锝堟绾惧ジ鏌℃径瀣仴妞ゆ柨顨扗闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉㈡杹閸嬫捇鎮介崹顐㈡畬缂備降鍔嶉悡锟犲箚閸愵喖绀嬫い鎺嶈兌閳ь剦鍠栭湁闁绘鍎ょ涵楣冩煙?
     */
    private String getFileNameForCurrentDimension(String dimensionId) {
        if (dimensionId == null) return null;

        if (dimensionId.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (dimensionId.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (dimensionId.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return null;
    }
    
    /**
     * 闂傚倷鐒﹁ぐ鍐矓閸洘鍋柛鈩冪懅娑撳秹鏌ㄥ☉妯侯仾闁稿﹦鍋ら弻娑㈡晲閸愩劌顬嬫繝娈垮枟閹倿鐛埀?
     */
    public void reset() {
        this.selectedArea = null;
        this.shrinkVertices.clear();
        this.isRecording = false;
        this.isActive = false;  // 闂傚倷鐒﹁ぐ鍐矓閸洘鍋柛鈩冾樅閾忚瀚氶柟缁樺醇濡ゅ懏鐓熸俊顖氱仢閻撴劙鏌?
        this.availableAreas.clear();

        // 婵犵數鍋為幐鎼佸箠濡　鏋嶉幖娣灪缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻娑樷枎濡櫣浠村銈庡亜椤戝鐣烽悩璇插唨妞ゆ劧绲块弳鐘崇箾閹寸偞灏柣掳鍔岄—鍐磼濞戞牔绨婚梺鍛婃尫缁€浣圭?
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
    }
    
    /**
     * 闂備礁鎲″缁樻叏閹灐褰掑炊椤掆偓閻銇勯弽銊х煁闁哄棗绻橀弻娑㈠煛閸愩劍鐎婚梺?
     * 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰閳诲瞼绱?
     * - 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮峰銈嗙墬缁娀宕ラ埀顒勬⒑閼姐倕浠滄俊顐ｎ殜楠炲骞庨懞銉︽珫闂佸壊鍋嗛崰鎰礊閳ь剟姊婚崒姘仾闁告梹顨婇幃娲箣閻樺灚锛忛悷婊冪灱閹?闂備焦瀵х粙鎴βㄩ埀顒傜磼鏉堛劎绠炵€规洘鑹捐灃闁逞屽墴瀹曠敻顢橀姀鐘殿吋闂侀€炲苯澧寸€殿喖鐏氬鍕節閸曨偄绠ラ梻?
     * - 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮峰銈嗙墬缁嬫帡鎮甸鈧娲敃閿濆棛娈ら柣銏╁灥濞咃綁鍩€椤掑偆鏀版い鏃€鍔楀Σ鎰攽鐎ｎ亞顦┑掳鍊愰崑鎾绘煕閵堝骸骞橀柟顕呭櫍閹啿顭冲纾俷ame闁诲孩顔栭崰妤€煤濠婂牆鏋侀柕鍫濐槹閸庡秹鏌涢弴銊ュ箹閻犳劗鍠栭幃妤呮偡閺夋鏆㈠┑鐐茬墛閸ㄥ灝鐣峰ú顏呭亜闂佸灝顑呴埀顒佸▕閺屻劌鈽夊Ο鐓庮暫閻庤鎸搁弫鍒琯nature缂傚倷鐒︾粙鎴λ囬鎹愬С妞ゆ帒瀚崑婵嬫煃鏉炴壆璐伴柛鐔插亾闂備礁鎲￠懝鍓х矓鐠鸿　鏋旈柟杈剧畱閺勩儵鏌ｉ弮鍫熸暠闁稿﹥濞婇弻?
     */
    private void loadAvailableAreas() {
        try {
            // 濠电偠鎻紞鈧繛澶嬫礋瀵?loadAllAreas 闂備礁鎼崐浠嬶綖婢跺本鍏滈柛顐ｆ礀缁€澶愭煟濡厧鍔嬬紒浣峰嵆閺岀喓鍠婂Ο杞板闂備礁鎼悧鍡浰囬鐐村亗婵炲棙鎸哥憴?
            List<AreaData> allAreas = loadAllAreas();

            availableAreas.clear();

            for (AreaData area : allAreas) {
                // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮峰銈嗙墬缁娀宕ラ埀顒勬⒑閼姐倕浠滄俊顐ｎ殜楠炲骞庨懞銉︽珫闁诲繒鍋熼崑鎾诲煝閺冨倻纾奸柍褜鍓熼幃婊兾熸笟顖涙闂備礁鎼悧鍡浰囬鐐村亗婵炲棙鎸哥憴?
                if (isAdmin) {
                    availableAreas.add(area);
                } else {
                    // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮峰銈嗙墬缁嬫帡鎮甸鈧娲敃閿濆棛娈ら柣銏╁灥濞咃綁鍩€椤掑偆鏀版い鏃€鍔楀Σ鎰攽鐎ｎ亞鐓戝銈呯箰閹冲繘鍩㈤弮鍌滅＜闁逞屽墯閵堬箓宕归绛嬪敹闁诲骸婀遍…鍫濈暦椤掑嫬鍨傞柛妤冨剱閸ゅ牓鏌ц箛姘兼綈闁伙綁浜堕弻娑㈠煛閸愩劍鐎婚梺璇″枟濞茬喖寮澶婇敜闁谎咁煭nature缂傚倷鐒︾粙鎴λ囬鎹愬С妞ゆ帒瀚崑婵嬫煃鏉炴壆璐伴柛鐔插亾闂備礁鎲￠懝鍓х矙閸曨厽顫?
                    // 闂備胶鎳撻悺銊╂偋閻愬搫鐒垫い鎺嗗亾鐎规洘鍙ename闁诲孩顔栭崰妤€煤濠婂牆鏋侀柕鍫濐槹閸ゅ﹥銇勮箛鎾愁伀缂佺姵甯￠弻锝夊Ω閵夈儺浠奸梺缁樻尰閻熲晛鐣?
                    if (playerName.equals(area.getSignature())) {
                        availableAreas.add(area);
                    } else if (area.getBaseName() != null) {
                        // 闂備礁鎼悮顐﹀磿閸欏鐝舵慨鎺撴簜sename闂佽娴烽弫鎼佸箠閹炬儼濮抽柡灞诲劜閸庡秹鏌涢弴銊ヤ簽闁告鏁婚弻娑橆潩椤掑倵鏋岀紓浣介哺缁诲倽鐏嬮梺閫炲苯澧寸€殿喖顭锋俊鐑藉Ψ閵夈儳缈籹ignature闂備礁鎼€氱兘宕规导鏉戠畾濞达絽婀遍埢鏃堟煕濠靛棗顏紒澶娿偢閺屾盯骞樺畷鍥嗐倝鎮介婵囶仩闁?
                        AreaData baseArea = findAreaByNameInList(area.getBaseName(), allAreas);
                        if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                            availableAreas.add(area);
                        }
                    }
                }
            }

            if (availableAreas.isEmpty()) {
                sendMessage(I18nManager.translate("shrinkarea.error.area.shrink_7"), Formatting.RED);
                sendMessage(I18nManager.translate("shrinkarea.message.area.shrink_4"), Formatting.YELLOW);
                stop();
                return;
            }

            sendMessage(I18nManager.translate("shrinkarea.message.general_2") + availableAreas.size() + I18nManager.translate("shrinkarea.message.area.shrink"), Formatting.GREEN);

        } catch (Exception e) {
            sendMessage(I18nManager.translate("shrinkarea.error.area") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
            stop();
        }
    }

    /**
     * 闂備線娼荤拹鐔煎礉瀹ュ洠鍋撻崹顐ｇ殤闁逞屽墲椤鎳濇ィ鍐ㄥ瀭妞ゅ繐妫欓崑姗€鏌曟繛鍨偓娑㈠储椤掑嫭鐓涢悘鐐额嚙閸旀粌顭跨憴鍕惰€跨€规洘妞介幃銏ゆ煥鐎ｎ亖鍋撻幎鑺ョ叆婵炴垶顭囨晶锝囩磼閸撲礁鏋涚€规洘绻堥崺锟犲磼濞戞艾骞嶆繝鐢靛仜椤︽澘煤閳哄啯顫?
     */
    private AreaData findAreaByNameInList(String name, List<AreaData> areas) {
        for (AreaData area : areas) {
            if (area.getName().equals(name)) {
                return area;
            }
        }
        return null;
    }


    /**
     * 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绺介妸鈺傗拺婵炲棙鍎抽弸鐔兼倵閸偆鐭嬬紒瀣槺閳ь剨绲洪弲婵嬫儍閹存緷?
     */
    public void handleXKeyPress() {
        if (!isActive || !isRecording) {
            return;
        }

        // 闂備胶鍎甸弲娑㈡偤閵娧勬殰閻庨潧鎲℃刊濂告煛婢跺孩纭剁紒澶庢硶閹叉悂鎮ч崼鐔风婵犫拃鍛ｉ柟宄版噹椤撳ジ宕堕埡鍌溾偓?
        recordCurrentPosition();
    }

    /**
     * 闂佽崵濮抽悞锕€顭垮Ο鑲╃鐎广儱鐗勬禍褰掓煙閹冩毐妞ゃ倕鎳庨湁闁挎繂鎳愯倴闂佹眹鍊曞Λ娆戠矙婢跺⊕娲敂閸屾浜惧┑鐘叉处閸?
     */
    public void recordCurrentPosition() {
        if (!isRecording) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        BlockPos pos = player.getBlockPos();
        AreaData.Vertex vertex = new AreaData.Vertex(pos.getX(), pos.getZ());
        shrinkVertices.add(vertex);

        sendMessage(I18nManager.translate("shrinkarea.message.vertex.record") + shrinkVertices.size() + ": 闂?(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")", Formatting.GREEN);

        // 闂備礁鎼ú銈夋偤閵娾晛钃熷┑鐘插暞缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻娑樷枎濡櫣浠村銈庡亜椤戝鐣烽悩璇插唨妞ゆ劧绲块弳鐘崇箾閹寸偞灏柣掳鍔岄—鍐磼濞戞牔绨婚梺鍛婃尫缁€浣圭?
        List<BlockPos> blockPosList = new java.util.ArrayList<>();
        for (AreaData.Vertex v : shrinkVertices) {
            blockPosList.add(new BlockPos((int)v.getX(), pos.getY(), (int)v.getZ()));
        }
        areahint.boundviz.BoundVizManager.getInstance().setTemporaryVertices(blockPosList, true);

        // 闂備礁鎼€氼剚鏅舵禒瀣︽慨妯垮煐閻掕顭块懜闈涘闁逞屽墮缁夊綊鐛幇顓炵窞闁归偊鍘奸崬澶愭⒑閹稿海鈽夐柣妤€妫楅锝夘敆閸曨剙娈岄梺绋跨С缁绘ΞyAdd闂備焦鐪归崝宀€鈧凹鍓熼幃鍧楀礋椤栨稑浠㈤柣銏╁灱閸犳氨绮?
        ui.showPointRecordedOptions(shrinkVertices.size());
    }
    
    /**
     * 缂傚倸鍊风紞鈧柛娑卞灡閺嗕即鏌ｉ悩杈╁妽婵犮垺顭囩槐鐐差吋婢跺﹤宓嗛梺鍝勵槹閸╁牆螣鐎ｎ亶娓婚柕鍫濇噹椤ｆ娊鏌?
     */
    public void continueRecording() {
        if (!isRecording) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        sendMessage(I18nManager.translate("expandarea.message.vertex.record.continue") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("expandarea.message.record"), Formatting.GREEN);
    }
    
    /**
     * 闂佽娴烽幊鎾诲嫉椤掑嫬鍨傛慨妯块哺婵ジ鏌℃径搴㈢《缂佸瀵ч〃銉╂倷鐎涙ɑ鐎紓浣虹帛濮樸劑鍩€椤掍胶鈯曢柨姘舵煟閹惧鎳囩€规洏鍎遍濂稿幢濡粯鐦戠紓?
     */
    public void finishAndSave() {
        if (!isRecording || !isActive) {
            return;
        }

        if (shrinkVertices.size() < 3) {
            sendMessage(I18nManager.translate("expandarea.error.vertex.record"), Formatting.RED);
            return;
        }

        // 闂備胶顭堥鍡欏垝瀹ュ鏁嗘繛鎴炵婵ジ鏌℃径搴㈢《缂佸瀵ф穱濠囶敍濡炶浜剧€规洖娲ㄩ、?
        this.isRecording = false;

        // 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡舵径瀣虎闂佺粯顨呴悧濠勭不閹烘鐓熼柕濞垮劚椤忊晜銇勯敂瑙勬珚闁诡喖鐖煎畷鍗炩槈閹烘垳澹曢梺鍝勫缁绘帞鏁?
        try {
            processAreaShrinking();
        } catch (Exception e) {
            sendMessage(I18nManager.translate("shrinkarea.error.area.shrink_4") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }
    
    /**
     * 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崶褎妲梺缁樻閺€閬嶅磹閹惰姤鐓涢柍褜鍓熷畷婊勬媴閾忓湱顦梻浣圭湽閸斿瞼鈧凹鍙冮幃褏鈧湱濮烽悿鈧梺鍛婂姦閸犳帡宕戦幘璇插強闊洦娲栫粩?
     */
    private void processAreaShrinking() {
        sendMessage(I18nManager.translate("shrinkarea.message.area.shrink_5"), Formatting.YELLOW);

        try {
            // 1. 濠德板€曢崐褰掓晪闁诲海顢婂▍鏇€冮妷銉ф殕闁告劦浜濋～?
            if (!validateHeights()) {
                return;
            }

            // 2. 闂備礁鎲￠崹瑙勬叏瀹曞洨绀婄€广儱娲︽刊濂告煥濞戞ê顏柡鍡╁墴閺屾稑顭ㄩ崘顏嗗姱闂侀潧妫楅敃顏堝箖濞嗘挻鍋￠梺顓ㄩ檮琚╅梻?
            ShrinkGeometryCalculator calculator = new ShrinkGeometryCalculator(selectedArea, shrinkVertices);
            AreaData shrunkArea = calculator.shrinkArea();

            if (shrunkArea == null) {
                sendMessage(I18nManager.translate("shrinkarea.error.area.shrink_5"), Formatting.RED);
                return;
            }

            // 3. 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉簵娴滃綊鏌熼幆褍鏆辨い銈呮噽缁辨挻鎷呯憴鍕▏闁诲海顢婂▍鏇犵矚闁秴鐒洪柛鎰屽懐顦?
            MinecraftClient client = MinecraftClient.getInstance();
            String currentDimension = null;
            if (client.world != null) {
                currentDimension = client.world.getRegistryKey().getValue().toString();
            }

            if (currentDimension == null) {
                sendMessage(I18nManager.translate("dividearea.error.dimension"), Formatting.RED);
                return;
            }

            // 4. 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｅ劵閸忓瞼绱掗璇插祮鐎殿噣娼ч濂稿川椤撗勫尃缂?
            ShrinkAreaClientNetworking.sendShrunkAreaToServer(shrunkArea, currentDimension);

            sendMessage(I18nManager.translate("shrinkarea.message.area.finish.shrink"), Formatting.GREEN);

        } catch (Exception e) {
            sendMessage(I18nManager.translate("shrinkarea.error.area.shrink_3") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        } finally {
            // 闂傚倷鐒﹁ぐ鍐矓閸洘鍋柛鈩冪⊕閸嬫劙鏌ら崫銉毌闁?
            reset();
        }
    }
    
    /**
     * 濠德板€楁慨鎾儗娓氣偓閹焦寰勯幇顒侇棟闂佺粯顨呴悧濠囧汲椤忓棛纾介柛灞绢殕濞呭懘鎮樿箛锝呯仸鐎规洘妞介幃銏ゆ倻濡吋娈稿┑掳鍊曢崐褰掓晪闁?
     */
    private boolean validateHeights() {
        if (selectedArea == null || selectedArea.getAltitude() == null) {
            sendMessage(I18nManager.translate("shrinkarea.error.area.altitude"), Formatting.RED);
            return false;
        }
        
        // 闂備浇銆€閸嬫捇鏌涢幘鑼跺厡缂佽鲸宀搁弻娑㈠冀瑜庨幆鍫ユ煟閹惧鎳呯紒杈ㄥ笩椤﹀啿顭跨憴鍕噰鐎规洏鍨介幃銏☆槹鎼粹€崇闂備礁鎲￠懝鍓х矓鐠鸿　鏋旈柟瀵稿剱濞堟淇婇姘儓妞ゆ柨锕幃瑙勬媴缁嬪簱鎸冮梺?
        return true;
    }
    
    /**
     * 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑橆煥閸涙澘鐓戦梻浣虹帛婢规洟寮插☉銏╂晩?
     */
    private void sendMessage(String message, Formatting color) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.literal(message).formatted(color), false);
        }
    }

    // Getters
    public boolean isActive() { return isActive; }
    public ShrinkState getCurrentState() { return currentState; }
    public List<AreaData> getAvailableAreas() { return availableAreas; }
    public AreaData getSelectedArea() { return selectedArea; }
    public List<AreaData.Vertex> getShrinkVertices() { return shrinkVertices; }
    public boolean isRecording() { return isRecording; }
    public boolean isAdmin() { return isAdmin; }
    public String getPlayerName() { return playerName; }
} 
