package areahint.expandarea;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.I18nManager;
import areahint.expandarea.ExpandAreaClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

/**
 * 闂備胶纭堕弲婵嬪窗鎼淬劌绠栭柡鍥ュ灩缁犮儵鏌嶆潪鎷屽厡婵炲吋姊荤槐鎺楁偑閸涱垳锛熼梺璇″枛閿曨亜鐣?
 * 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰瀛濋梺姹囧€曠€氫即骞冮幎钘夌骇婵炲棛鍋撲簺闂備礁鎼ˇ顖炲磹鐠囨祴鏋旈柟杈剧畱缁€鍕煟濡じ鍚紒澶庢硶缁辨帡鎮╅幇浣圭暦缂佺虎鍘奸悥鐓庮嚕閸洖唯闁靛鍠楄ⅸ闂備浇宕甸崑娑樜涘Δ鍛亗婵炲棙鎸哥憴锔锯偓骞垮劚濡鍩涢弽顓熷€?
 */
public class ExpandAreaManager {
    private static ExpandAreaManager instance;
    private MinecraftClient client;
    private String selectedAreaName;
    private AreaData selectedArea;
    private List<Double[]> newVertices;
    private boolean isRecording = false;
    private boolean isActive = false;
    private ExpandAreaUI ui;
    
    public static ExpandAreaManager getInstance() {
        if (instance == null) {
            instance = new ExpandAreaManager();
        }
        return instance;
    }
    
    private ExpandAreaManager() {
        this.client = MinecraftClient.getInstance();
        this.newVertices = new ArrayList<>();
        this.ui = new ExpandAreaUI(this);
    }
    
    /**
     * 闁诲孩顔栭崰鎺楀磻閹炬枼鏀芥い鏃傗拡閸庡繘鏌ｉ幘瀵告噰鐎规洏鍎遍濂稿幢濡炵粯瀵橀梺璇茬箳閸嬬偛煤濡偐鐭堥柨鏂垮⒔閻?
     */
    public void startExpandArea() {
        System.out.println("DEBUG: startExpandArea()");
        if (client.player == null) {
            System.out.println("DEBUG: client.player 濠?null");
            return;
        }
        
        System.out.println("DEBUG: set isActive = true");
        isActive = true;  // 闂佽崵濮崇粈浣规櫠娴犲鍋柛鈩冾焽閳绘棃鏌ｉ幋鐐嗘垿濡甸悢鍏肩厱闁哄倽顕ф俊鍨攽椤旇姤鍊愭鐐╁亾?
        
        // 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墮閻銇勯弽銊ф噭闁告瑥绻橀弻锟犲焵椤掑嫬鎹舵い鎾跺枔閺嗙娀姊虹化鏇熸珖闁哥姴閰ｉ獮鍐即閵忕姷顦┑掳鍊曠€氥劑鍩€?
        System.out.println("DEBUG: loading modifiable areas");
        List<AreaData> modifiableAreas = getModifiableAreas();
        System.out.println("DEBUG: found " + modifiableAreas.size() + " modifiable areas");
        
        if (modifiableAreas.isEmpty()) {
            System.out.println("DEBUG: no modifiable areas found");
            sendMessage(I18nManager.translate("expandarea.error.area.expand_4"), Formatting.RED);
            sendMessage(I18nManager.translate("expandarea.message.area.expand_2"), Formatting.GRAY);
            isActive = false;
            return;
        }
        
        // 闂備胶鍎甸弲娑㈡偤閵娧勬殰闁圭虎鍠栭崣濠囨煕閹炬ぞ妞掔划鐢告⒑缁洘娅囬柛鐘查叄楠炲啴寮撮姀锛勫姷婵犮垼娉涢鍛搭敂閵夆晜鐓熼柨婵嗘噽閻掑憡绻?
        System.out.println("DEBUG: showing expandable area selection UI");
        ui.showAreaSelection(modifiableAreas);
        System.out.println("DEBUG: startExpandArea() completed");
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闁汇埄鍨靛▍锝夊焵椤掑偆鏀伴柛鈺傜墪鍗遍柟闂寸鐟欙箓鏌ㄩ弮鍥撶紒鐘冲浮閺屸剝寰勬繝鍌涙濠碘€冲⒔閸庛倗绮氶柆宥庢晩闁兼亽鍎插В澶愭⒑閸︻収鐒炬繛灞傚€濋幃鐐偅閸愩劍妲梺缁樻閺€閬嶅磹?
     */
    private boolean checkPermission(AreaData area) {
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

        // 濠电偛顕慨瀛橆殽閹间礁绀勯柨鐔哄Т缁€澶愭煟濡厧鍔嬬紒浣峰嵆閺岋綁濡搁妷銉患閻熸粍婢樺鍓佺矚闁稁鏁婇柤鎭掑劜濮ｅ姊虹化鏇熸珖闁哥姴閰ｉ獮鍐即閵忕姷顦┑掳鍊曠€氥劑鍩€椤掆偓閸燁垳绮欐径鎰垫晣闁绘柨鎼銊╂⒑閻熸壆浠涢柛搴㈠▕閹€斥枎閹惧疇袝閻庡箍鍎辩换鎺旂矆閸曨垱鐓犻柡澶婄仢椤ㄦ瑧绱掑Δ鈧ˇ闈涱嚕娴兼惌鏁嶉柣鎰级椤忕喖姊洪崫鍕偓鍧椼€傞敂鎯у灊闁绘顕уΛ姗€鏌涢妷顖炴妞ゆ劒绮欓弻娑㈠籍閸屾顒佺箾閺夋垶鍠橀柡?
        List<AreaData> modifiableAreas = getModifiableAreas();
        AreaData area = null;
        for (AreaData a : modifiableAreas) {
            if (a.getName().equals(cleanedName)) {
                area = a;
                break;
            }
        }

        if (area == null) {
            sendMessage(I18nManager.translate("addhint.message.area_2") + cleanedName + I18nManager.translate("expandarea.message.expand.permission"), Formatting.RED);
            sendMessage(I18nManager.translate("expandarea.prompt.area.expand.permission"), Formatting.GRAY);
            return;
        }

        // 闂傚倷绶￠崑鍕囬幍顔瑰亾濮樸儱濡块柟椋庡█婵＄兘濡疯楠炲秹姊洪崨濠呭缂佸瀚Σ鎰枎閹惧磭楠囬梺鍛婂姦閸犳顢撳▎鎴斿亾閻熺増鍟炵憸鏉垮暙閿曘垽鎮ˇ濯odifiableAreas濠电偞鍨堕幖鈺呭储娴犲瑤澶愬川閺夋垼鎽曢梺闈涱樈閸ㄥ磭绮?
        handleAreaSelection(area);
    }
    
    /**
     * 闂備礁鎲￠悷锕傛偋濡ゅ啰鐭撻柣鎴ｆ缁犮儵鏌嶆潪鎷屽厡婵炲吋姊圭换娑氱礄閻樺搫鍘￠梺?
     */
    public void cancel() {
        if (!isActive) {
            return;
        }

        isActive = false;
        isRecording = false;
        selectedArea = null;
        selectedAreaName = null;
        newVertices.clear();

        // 婵犵數鍋為幐鎼佸箠濡　鏋嶉幖娣灪缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻娑樷枎濡櫣浠村銈庡亜椤戝鐣烽悩璇插唨妞ゆ劧绲块弳鐘崇箾閹寸偞灏柣掳鍔岄—鍐磼濞戞牔绨婚梺鍛婃尫缁€浣圭?
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();

        ui.showCancelMessage();
    }
    
    /**
     * 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墮閻銇勯弽銊ф噭闁告瑥绻橀弻锟犲焵椤掑嫬鎹舵い鎾跺枔閺嗙娀姊虹化鏇熸珖闁哥姴閰ｉ獮鍐即閵忕姷顦┑掳鍊曠€氥劑鍩€?
     */
    private List<AreaData> getModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        String playerName = client.player.getGameProfile().getName();
        boolean isAdmin = client.player.hasPermissionLevel(2);
        
        System.out.println("DEBUG: 闂備胶绮竟鏇㈠疾濞戙埄鏁婄€广儱顦憴锔锯偓骞垮劚濞? " + playerName + ", 闂備礁鎼€氱兘宕规导鏉戠畾濞达綀娅ｆ稉宥夋煥濞戞ê顏柛濠勫仱閺? " + isAdmin);
        
        // 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墮缁犮儵鏌嶈閸撶喎顕ｉ崹顐㈢窞濠电姴瀚獮宥夋⒑?
        List<AreaData> allAreas = loadAllAreas();
        
        for (AreaData area : allAreas) {
            System.out.println("DEBUG: 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑闂佺粯鎸婚悷鈺佺暦? " + area.getName() + ", 缂傚倷鐒︾粙鎺楀磿閹惰棄绠? " + area.getSignature() + ", 闂備胶纭堕弲鐐测枍閿濆鈧線宕ㄩ弶鎴炴К闂佺粯妫冮弨閬嶅磹? " + area.getBaseName());
            if (isAdmin) {
                // 缂傚倷鑳舵刊瀵告閺囥垹绠栧┑鐘叉搐瀹告繃淇婇姘儓閻犱焦褰冮湁闁绘﹩鍠栭崝娆撴煙娓氬灝濮傜€殿喓鍔庨幉鎾礋椤愵偅娈归梻浣告惈閻楀棝藝椤栫偞鍋傛繛鍡樻尭鐟?
                result.add(area);
                System.out.println("DEBUG: 缂傚倷鑳舵刊瀵告閺囥垹绠栧┑鐘叉搐瀹告繃淇婇婊呭笡缂傚秮鍋撻梻鍌氬€哥€氼參宕濊濡叉劕鈹戦崰锕€娴烽幏鐘诲箵閹烘繃濂栭梻浣虹《閺呮繈宕版惔銊ョ畺? " + area.getName());
            } else {
                // 闂備礁鎼幏瀣闯閿濆鐒垫い鎺嶇劍閻ㄦ垿鎮介婵囶仩闁逞屽墰椤㈠﹤鈻斿☉婧夸汗鐟滄棃骞婂Δ鍛闁圭儤鎸稿鍧楁⒑鐞涒€充壕缂佺虎鍘奸幊搴♀枔閹殿喒鍋撻崷顓х劸鐎规洦鍓熷畷褰掑础閻戝棗娈橀梺鎯х箺椤鎮鹃柆宥嗙厵閻庢稒蓱缁晣sename闁诲孩顔栭崰妤€煤濠婂牆鏋侀柕鍫濐槹閸ゅ﹥銇勮箛鎾愁伀缂佺姵甯￠弻锝夊Ω閵夈儺浠奸梺缁樻尰閻熲晛鐣?
                if (playerName.equals(area.getSignature())) {
                    result.add(area);
                    System.out.println("DEBUG: 闂備胶绮竟鏇㈠疾濞戙埄鏁婄€广儱顦粈鍡樼箾閹寸儐鐒界紒鎲嬬畵閺岋綁濡搁妷銉患闂佺粯鎸婚悷鈺佺暦閵夈儺鍚嬮煫鍥ㄦ礈椤︻喖鈹戦悜鍥╃К妞ゆ泦鍕弿? " + area.getName());
                } else if (area.getBaseName() != null) {
                    AreaData baseArea = findAreaByName(area.getBaseName());
                    if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                        result.add(area);
                        System.out.println("DEBUG: 闂備胶纭堕弲鐐差浖閵娧嗗С妞ゆ帒瀚崑婵嬫煃鏉炴壆璐伴柛鐔插亾闂備胶纭堕弲婵嬪窗鎼淬劌绠栭柡鍥ュ灪閸庡秹鏌涢弴銊ュ闁抽攱鐗犻幃姗€鎮欑€涙鈹涚紓浣介哺缁诲倽褰侀柣鐘叉处瑜板啴锝? " + area.getName());
                    }
                }
            }
        }
        
        System.out.println("DEBUG: found " + result.size() + " modifiable areas");
        return result;
    }
    
    /**
     * 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崶褎妲梺缁樻閺€閬嶅磹閹惰姤鈷戞い鎰╁焺濡插綊鎮?
     */
    public void handleAreaSelection(AreaData selectedArea) {
        this.selectedArea = selectedArea;
        this.selectedAreaName = selectedArea.getName();
        
        sendMessage(I18nManager.translate("dividearea.prompt.area") + areahint.util.AreaDataConverter.getDisplayName(selectedArea), Formatting.GREEN);
        sendMessage(I18nManager.translate("expandarea.button.area.record.save"), Formatting.GRAY);
        
        // 闁诲孩顔栭崰鎺楀磻閹炬枼鏀芥い鏃傗拡閸庢挻銇勯弬鎸庣┛闁靛洦鍔欏鎾偄鐏炶偐顦︽い?
        startRecording();
    }
    
    /**
     * 闁诲孩顔栭崰鎺楀磻閹炬枼鏀芥い鏃傗拡閸庢挻銇勯弬鎸庣┛闁靛洦鍔欏鎾偄閸濄儱甯庡┑锛勫亼閸婃洘鏅舵惔銊ョ；?
     */
    private void startRecording() {
        this.isActive = true;
        this.isRecording = true;
        this.newVertices.clear();
        ui.showRecordingInterface();
    }
    
    /**
     * 闂佽崵濮抽悞锕€顭垮Ο鑲╃鐎广儱鐗勬禍褰掓煙閹冩毐妞ゃ倕鎳庨湁闁挎繂鎳愯倴闂佹眹鍊曞Λ娆撳箯閸涱収鐓ラ柍褜鍓涢幏褰掓偄閻撳孩顥濇繛鏉戝悑閻ｎ亪鍩€椤掆偓閿曨亪骞?
     * 闂備胶顫嬮崟顐㈩潔闂佺粯鐗徊鍓р偓鐢靛帶椤繈骞囨担纭呮櫑闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
     */
    public void recordCurrentPosition() {
        if (!isRecording || client.player == null) {
            return;
        }

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        // 闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
        int roundedX = (int) Math.round(x);
        int roundedZ = (int) Math.round(z);
        newVertices.add(new Double[]{(double) roundedX, (double) roundedZ});

        sendMessage(I18nManager.translate("dividearea.message.record_3") + newVertices.size() + ": 闂?(" +
                   roundedX + ", " + String.format("%.1f", y) + ", " + roundedZ + ")",
                   Formatting.GREEN);

        // 闂備礁鎼ú銈夋偤閵娾晛钃熷┑鐘插暞缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻娑樷枎濡櫣浠村銈庡亜椤戝鐣烽悩璇插唨妞ゆ劧绲块弳鐘崇箾閹寸偞灏柣掳鍔岄—鍐磼濞戞牔绨婚梺鍛婃尫缁€浣圭?
        List<net.minecraft.util.math.BlockPos> blockPosList = new java.util.ArrayList<>();
        for (Double[] vertex : newVertices) {
            blockPosList.add(new net.minecraft.util.math.BlockPos(vertex[0].intValue(), (int) y, vertex[1].intValue()));
        }
        areahint.boundviz.BoundVizManager.getInstance().setTemporaryVertices(blockPosList, true);

        // 闂備礁鎼€氼剚鏅舵禒瀣︽慨妯垮煐閻掕顭块懜闈涘闁逞屽墮缁夊綊鐛幇顓炵窞闁归偊鍘奸崬澶愭⒑閹稿海鈽夐柣妤€妫楅锝夘敆閸曨剙娈岄梺绋跨С缁绘ΞyAdd闂備焦鐪归崝宀€鈧凹鍓熼幃鍧楀礋椤栨稑浠㈤柣銏╁灱閸犳氨绮?
        ui.showPointRecordedOptions(newVertices.size());
    }
    
    /**
     * 闂佽娴烽幊鎾诲嫉椤掑嫬鍨傛慨妯块哺婵ジ鏌℃径搴㈢《缂佸瀵ч〃銉╂倷閼哥數銆愬銈忚吂閺呯娀骞冮崼鏇炲耿婵☆垵銆€閹稿懘鏌?
     */
    public void finishRecording() {
        if (!isRecording || newVertices.size() < 3) {
            sendMessage(I18nManager.translate("expandarea.error.vertex.record"), Formatting.RED);
            return;
        }
        
        this.isRecording = false;
        
        try {
            // 闂佸搫顦弲婊呯矙閺嶎厹鈧線骞嬮敃鈧粈鍕煟濡じ鍚紒澶婃健閹绗熸繝鍕紵闂佷紮绲介…宄扮暦椤忓棔娌柣锝呰嫰楠炲秹姊洪崨濠呭缂佸鐏氶弲鍓佹崉閵娿倖鍕?
            processAreaExpansion();
        } catch (Exception e) {
            sendMessage(I18nManager.translate("expandarea.error.area.expand_3") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }
    
    /**
     * 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崶褎妲梺缁樻閺€閬嶅磹閹惰姤鐓欑痪鐗埳戝▍鍛存煟椤愩垻效闁诡垰鍟村畷鐔碱敆閸屾凹妫庨梻濠庡亜濞层倝宕幘顔肩劦妞ゆ帊鐒﹂～妤冪磼?
     * 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰瀛濋梺姹囧€曠€氫即骞冮幎钘夌骇婵炲棛鍋撲簺闂備礁鎼ˇ顖炲磹鐠囨祴鏋旈柟杈剧畱缁€鍕煟濡じ鍚紒澶庢硶缁辨帡鎮╅幇浣圭暦缂?
     */
    private void processAreaExpansion() {
        sendMessage(I18nManager.translate("expandarea.message.area.expand_3"), Formatting.YELLOW);
        
        try {
            // 1. 濠德板€曢崐褰掓晪闁诲海顢婂▍鏇€冮妷銉ф殕闁告劦浜濋～?- 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰鏁堥梺閫炲苯澧俊鍙夊笧濞?
            if (!validateHeightsAccordingToPrompt()) {
                return;
            }
            
            // 2. 闂備礁婀辩划顖炲礉閺嚶颁汗闁搞儺鍓欓崒銊╂煟閺冨牜妫戦柛妯兼暬閺屾稑顫濋鍌傃囨煏閸℃韬柟?
            List<Double[]> originalVertices = extractOriginalVertices();
            
            // 3. 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘宠壘濡﹢鏌ｅΔ鈧悧鍡楊焽閹达附鐓曢柡鍐ㄥ€稿瓭闁诲繐娴氶崹璺虹暦濞差亝鍋勯柣鎴烆焽閺嗙姵淇婇妶鍛偓褰掓晪闁诲氦顫夋繛濠囧箠閻樿绀冮柕濠忓閸?
            double[] newAreaHeightRange = calculateNewAreaHeightRange();
            
            // 4. 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑闂佸搫鑻敃銈堝絹闁荤姴娲﹁ぐ鍐綖閿濆拋娓婚柕鍫濇噹椤ｆ娊鏌涚€ｅ墎绉€殿喕鍗抽、娑橆潩椤撶偛澧鹃梻渚€娼荤拹鐔煎礉瀹€鈧弫顕€顢曢敃鈧弰銉╂煟閺冨牊鏁遍柛濠冨▕閺屾盯骞囬浣哥彅缂備浇椴哥换鍫濈暦濮樿埖鍋愮紓浣诡焽瑜版垿姊洪崨濠傚闁硅櫕锕㈠畷鏇犫偓娑櫱滄禍婊堟煕閹捐尙顦﹀ù?
            List<Double[]> externalVertices = filterExternalVertices(originalVertices);
            
            if (externalVertices.isEmpty()) {
                sendMessage(I18nManager.translate("expandarea.error.area.vertex.add"), Formatting.RED);
                return;
            }
            
            // 5. 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣股戠紞鍥煕閿旇骞楁繛鍛礋閺?- 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰鑲栧┑鐐茬墛閸ㄨ泛顕ラ崟顐悑闁糕剝顭囬崚鎵磽娴ｅ搫校妞ゃ劌妫涘☉?
            // List<Double[]> boundaryPoints = calculateBoundaryPoints(originalVertices, externalVertices);
            
            // 6. 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘梻鐥幆褜鍎戠紒鐙€鍣ｉ弻锝夊箛椤旇棄娈岄梺褰掝棑婢ф顭囪箛娑樻嵍妞ゆ挾鍠愰悵顏堟⒑?
            List<Double[]> adjacentPoints = calculateAdjacentPoints(originalVertices, externalVertices);
            List<Double[]> finalBoundaryPoints = calculateFinalBoundaryPoints(adjacentPoints, originalVertices);
            
            // 7. 闂備礁鎲￠懝楣冩偋閸℃稒鍤愰柣鏃€鎮舵禍婊堟煕閹捐尙顦﹀ù鐙€鍨遍〃銉╂倷鐠鸿櫣鍘柣銏╁灡閹稿骞?
            List<Double[]> combinedVertices = combineVertices(originalVertices, externalVertices, finalBoundaryPoints);
            
            // 8. 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壈闂佽壈宕甸崰鎰矚闁稁鏁婄痪鎷岄哺浜涘┑鐐茬摠閸ゅ酣宕愰弴鐑嗗殨?
            List<Double[]> fixedVertices = fixCrossings(combinedVertices);
            
            // 9. 闂傚倷鐒﹁ぐ鍐矓閻㈢钃熷┑鐘插婵ジ鏌ㄥ☉妯侯伀闁哄棭鍓欓湁婵犲﹤鍟伴悘鍗炍旈悩鍙夊闁靛洤瀚板畷婊勬媴闂€鎰
            List<Double[]> secondVertices = calculateSecondVertices(fixedVertices);
            
            // 10. 闂備礁鎼ú銈夋偤閵娾晛钃熷┑鐘插枤濞堟淇婇姘儓妞ゆ柨锕よ彁闁搞儻绲芥晶鎻捗?
            AreaData.AltitudeData updatedAltitude = updateAltitudeData(newAreaHeightRange);
            
            // 11. 闂備礁鎲＄敮妤冪矙閹寸姷纾介柟鎹愵嚙缁犮儵鏌嶆潪鎷屽厡婵炲吋妫冮弻娑橆潩椤掍焦宕冲┑鐐茬墛閸ㄥ灝鐣峰ú顏呭亜闂佸灝顑呴埀?
            AreaData expandedArea = createExpandedArea(fixedVertices, secondVertices, updatedAltitude);

            // 12. 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉簵娴滃綊鏌熼幆褍鏆辨い銈呮噽缁辨挻鎷呯憴鍕▏闁诲海顢婂▍鏇犵矚闁秴鐒洪柛鎰屽懐顦?
            String currentDimension = null;
            if (client.world != null) {
                currentDimension = client.world.getRegistryKey().getValue().toString();
            }

            if (currentDimension == null) {
                sendMessage(I18nManager.translate("dividearea.error.dimension"), Formatting.RED);
                return;
            }

            // 13. 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｅ劵閸忓瞼绱掗璇插祮鐎殿噣娼ч濂稿川椤撗勫尃缂?
            ExpandAreaClientNetworking.sendExpandedAreaToServer(expandedArea, currentDimension);

            sendMessage(I18nManager.translate("expandarea.message.area.finish.expand"), Formatting.GREEN);
            
        } catch (Exception e) {
            sendMessage(I18nManager.translate("expandarea.error.area.expand_2") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        } finally {
            // 闂傚倷鐒﹁ぐ鍐矓閸洘鍋柛鈩冪⊕閸嬫劙鏌ら崫銉毌闁?
            reset();
        }
    }
    
    /**
     * 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰鏁堥梺缁樼⊕閻熝囧箯閻樿櫕濯撮悷娆忓閸樻劕鈹戦悜鍥╃К妞ゆ泦鍕弿闁绘劕鎼粈宀勬煕濠靛棗顏柛妯兼暬閺岋綁濡搁妷銉純闂佺粯绻嶉崰妤呭箚?
     * 闂備礁鍚嬮惇褰掑磿閺屻儱钃熷┑鐘蹭紜鎼达絾瀚氶柟缁樺俯濞奸亶姊哄Ч鍥у閻庢凹鍓涙禍鎼侇敍閻愬弶妲梺缁樻⒒閸庛倝鎮鹃柆宥嗙厸闁割偅鑹炬禍鐐繆閵堝懎鈧綊鏁冮姀銈嗗仧妞ゆ棁濮ら崕鐔兼煥閺冨倹娅曟俊鑼厴閺屾稑螣閻撳孩鐎婚梺缁樻尰閻熲晛鐣烽妷銉悑闁搞儯鍔庨弳鐘绘⒑閸濆嫮澧愰柛瀣尭铻栭柛灞惧喕閼板潡鏌ｅ┑鎰灍闁诡垱妫冮弫鍐焵椤掑嫭鍎嶆い鎺戝鐎氬鏌嶈閸撴岸骞忛崨顔藉劅闁靛ě鍐嫬闂佺澹堥幓顏堟⒔閸曨垱鍎婃い鏇楀亾鐎规洏鍨介幃銏☆槹鎼粹€崇闂備礁鎲￠懝鍓х矓鐠鸿　鏋旈柟杈剧畱鐎氬鏌嶈閸撴岸骞忛崨顔藉劅闁靛ě鍐嫬闂佺澹堥幓顏嗙不閹剧粯鍋熸い鏇楀亾鐎规洘鑹鹃埢搴㈡償閿濆洦鍠掗梺鑽ゅТ濞层垽宕曟潏顐犱汗濠㈣埖鍔曢弰銉╂煟閺冨牊鏁遍柛濠冨▕閺岋綁濡搁妷銉純闂佺粯绻嶉崰妤呭箚?
     * 闂備礁鎲￠悷銉х矓瀹曞洨涓嶉柨婵嗩槸缁€鍡樼箾閸℃绠虫繛鍫熸そ閺岀喖鐓幓鎺戝Е闂?
     */
    private boolean validateHeightsAccordingToPrompt() {
        if (client.player == null || selectedArea.getAltitude() == null) {
            return true; // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞婵炵偓鐓㈤梺鏂ユ櫅閸燁垳绮婚幒鎳虫棃宕掑ù銏╀簽閳ь剝顫夋繛濠傤潖娴犲绠涙い鎺嗗亾闁绘挻鍨块弻娑㈠箳閹捐埖鍣伴梺閫炲苯澧ǎ鍥閹?
        }
        
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘宠壘濡﹢鏌ｅΔ鈧悧鍡楊焽閹达附鐓曢柡鍐ㄥ€稿瓭闁诲繐娴氶崹璺虹暦濞差亝鍋勯柣鎴烆焽閺嗙姵淇婇妶鍛偓褰掓晪闁诲氦顫夋繛濠囧箠閻樿绀冮柕濠忓閸?
        double[] newAreaHeightRange = calculateNewAreaHeightRange();
        double newMinHeight = newAreaHeightRange[0];
        double newMaxHeight = newAreaHeightRange[1];
        
        AreaData.AltitudeData altitude = selectedArea.getAltitude();
        if (altitude != null && altitude.getMax() != null && altitude.getMin() != null) {
            double originalMaxHeight = altitude.getMax();
            double originalMinHeight = altitude.getMin();
            
            // 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰鏁堥梺閫炲苯澧俊鍙夊笧濞嗐垽顢曢敂鑺ユ珫濠殿喗锕╅崢浠嬫偂閳ь剟姊洪崨濠勫ⅹ闁圭⒈鍋婇幃鈥斥枎閹惧啿鐝橀梺閫炲苯澧い銊ユ搐椤粓鍩€椤掑嫭鍋熸い鏃囧Г閸?< 闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎卞Λ娆愮濡綍鏃堝磼濞戝疇鈧潡鏌ｅ┑鎰灍闁?濠?闂備礁鎼崐鐟邦熆濡棿鐒婃い鏍仜閺勩儵鏌ｉ弬鎸庡暈濞寸姵锚闇夐柨婵嗘嚇閸欏嫰鏌ｅ┑鎰灍闁?> 闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎卞Λ娆愮濡ソ褰掓晲閸涙潙寮伴梺缁樼箥閸犳骞?
            if (newMaxHeight < originalMaxHeight && newMinHeight > originalMinHeight) {
                // 濠电偞鍨堕幐鍝ョ矓閻㈢鏋佸┑鍌滎焾閻淇婇婵囶仩闁告鏁婚弻娑橆潩椤掑倐銈嗙箾閸喎鐏︽い銊ユ搐铻ｉ柛婵嗗閸?
                sendMessage(I18nManager.translate("expandarea.message.area.altitude"), Formatting.GREEN);
                return true;
            } else {
                // 闂備礁鎲￠悷銉х矓瀹曞洨涓嶉柨婵嗩槸缁€鍡樼箾閸℃绠虫繛鍫熸そ閺岀喖鐓幓鎺戝Е闂?
                sendMessage(I18nManager.translate("expandarea.error.area.altitude.expand"), Formatting.RED);
                sendMessage(I18nManager.translate("expandarea.error.area.altitude") + originalMinHeight + " ~ " + originalMaxHeight, Formatting.RED);
                sendMessage(I18nManager.translate("expandarea.error.altitude") + newMinHeight + " ~ " + newMaxHeight, Formatting.RED);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘宠壘濡﹢鏌ｅΔ鈧悧鍡楊焽閹达附鐓曢柡鍐ㄥ€稿瓭闁诲繐娴氶崹璺虹暦濞差亝鍋勯柣鎴烆焽閺嗙姵淇婇妶鍛偓褰掓晪闁诲氦顫夋繛濠囧箠閻樿绀冮柕濠忓閸?
     */
    private double[] calculateNewAreaHeightRange() {
        if (client.player == null) {
            return new double[]{64.0, 64.0}; // 濠殿喗甯楃粙鎺椻€﹂崼銉晣缂備焦锕╁▓妤佷繆椤栨碍鎯堟い?
        }
        
        double playerY = client.player.getY();
        // 缂傚倸鍊烽悞锕傛偡閿曞倸钃熷┑鐘叉搐缁€宀勬煕濠靛棗顏柛姗€娼ч埥澶愬箻椤栨矮澹曞┑鐐村灦閹尖晜绂嶅鍫濈畺閹兼番鍔嶉崑鐘绘煕閳╁啰鎳勯柣锝呭船铻栭柛灞捐€介鍡忓亾鐟欏嫬鈻曢柟宕囧█瀹曟﹢濡搁敂鎯у毐闂備焦瀵х粙鎴︽偋閸涱垳绠旈柛灞剧〒閳绘棃鎮楅敐搴濈凹闁?0闂備礁鎼粔鍫曞Υ鐎ｎ剚顫?
        return new double[]{playerY - 10.0, playerY + 10.0};
    }
    
    /**
     * 闂備礁婀辩划顖炲礉閺嚶颁汗闁搞儺鍓欓崒銊╂煟閺冨牜妫戦柛妯兼暬閺屾稑顫濋鍌傘倖绻涢崼鐔风仸闁靛洤瀚板畷婊勬媴闂€鎰闂備胶顫嬮崟顐㈩潔闂?
     */
    private List<Double[]> extractOriginalVertices() {
        List<Double[]> vertices = new ArrayList<>();
        List<AreaData.Vertex> verticesList = selectedArea.getVertices();
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Е闂侀潧妫楅敃顏堝箖濞嗘挻鍋￠柡澶庢硶娴犳岸鏌ｉ悙瀵糕棨闁告柨绉村嵄闁归棿绀佺憴锕傛煥閺冨洤鍔电紒鈧刊妾渓l
        if (verticesList == null) {
            System.err.println("WARNING: selected area vertex list is null, cannot expand area");
            return vertices; // 闂佸搫顦弲婊堝蓟閵娿儍娲冀椤愩倗鐓旈梺鍛婄箓鐎氼剟鎮樺▎鎾村仩?
        }
        
        for (AreaData.Vertex vertex : verticesList) {
            vertices.add(new Double[]{vertex.getX(), vertex.getZ()});
        }
        
        return vertices;
    }
    
    /**
     * 闂佸搫顦弲娑樏洪敃鍌氱闁靛牆妫楃欢鐐烘煛瀹ュ骸骞栭柛鏂诲劚椤啴濡堕崨顓ф殺闂?- 闂備礁鎲＄敮鐐寸箾閳ь剚绻涢崨顓㈠弰鐎规洜鍏樻俊鎼佸Ψ閵夛附鐏抽梻浣虹《閺呮繈宕版惔銊ョ畺闁哄洢鍨圭粈鍐煕濞戞瑦缍戦柛鏂诲劦閺岋綁濡搁妷銉純闂侀潧妫楅敃顏堝箖?
     */
    private List<Double[]> filterExternalVertices(List<Double[]> originalVertices) {
        List<Double[]> externalVertices = new ArrayList<>();
        
        for (Double[] vertex : newVertices) {
            if (!isPointInPolygon(vertex, originalVertices)) {
                externalVertices.add(vertex);
            }
        }
        
        return externalVertices;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣股戠紞鍥煕閿旇骞楁繛鍛礋閺?- 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰鑲栧┑鐐茬墛閸ㄨ泛顕ラ崟顐悑闁糕剝顭囬崚鎵磽娴ｅ搫校妞ゃ劌妫涘☉?
     */
    private List<Double[]> calculateBoundaryPoints(List<Double[]> originalVertices, List<Double[]> externalVertices) {
        List<Double[]> boundaryPoints = new ArrayList<>();
        
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘宠壘濡﹢鏌ｅΔ鈧悧鍡楊焽閹达附鐓曢柡鍐ㄥ€搁埢鍫ユ煏閸℃韬柟顔界懇閺屽懎鈽夊杈ㄥ枦闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎遍悧蹇涙偟閸洘鐓熼柨婵嗘噽閻忚鲸绻涢崼鐔风仸缂佸倸绉规俊鍫曞川椤撶娀鐛撻梻?
        for (int i = 0; i < externalVertices.size(); i++) {
            int nextIndex = (i + 1) % externalVertices.size();
            Double[] currentVertex = externalVertices.get(i);
            Double[] nextVertex = externalVertices.get(nextIndex);
            
            // 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣股戞禍銈嗙箾閸℃ê濮堥柦鍌氼儑缁辨帡鎮欓鈧婊勩亜閺傛寧婀扮紒瀣槹閹棃鍨鹃搹顐ｇ伋闂備胶纭堕弲婵嬪窗鎼淬劌绠栭柡鍥╁枑缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊Ω閵夈儺浠瑰┑顔斤公缁犳挸鐣烽敐鍡楃窞閻忕偞鍨濋崠?
            List<Double[]> intersections = findLinePolygonIntersections(currentVertex, nextVertex, originalVertices);
            boundaryPoints.addAll(intersections);
        }
        
        return boundaryPoints;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘梻鐥幆褜鍎戠紒鐙€鍣ｉ弻?- 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰閳诲瞼绱掗崣妯哄祮鐎规洩缍侀、鏃堝炊鐠虹儤鍊庣紓鍌欒兌婵敻銆冮崨鏉戝瀭闁割煈鍋呴崣蹇涙倵閿濆骸澧ù鐙€鍨堕弻娑橆煥閸愨晜鎷卞┑鐘亾妞ゆ牗绮堥悞濠囨煕閹炬鍟╅崠鏍⒑濮瑰洤濡奸悗姘间簼瀵板嫬顓奸崱妯规唉闂佺懓顕慨瀛樼?
     * 濠电偞鍨堕幐鎾磻閹炬枼妲堥柟鍨暕缁ㄤ粙鏌涚€ｅ墎绉€规洩缍侀、鏃堝炊瑜嶉獮瀣煟鎼达絾鏆╅柟铏尵閼洪亶宕ｆ径灞告灃闂侀€炲苯澧紒瀣樀椤㈡棃宕橀…鎴炐╅梺鍝勵槴閺呮粓宕硅ぐ鎺戠；?
     * 闂備礁鎲＄敮妤冩崲閸岀儑缍栭柟鎵閸婄兘鏌ｉ悢鍝勵暭閻庢碍鐓￠弻锟犲醇椤愵澀绨婚梺缁樻閸撶喖骞冨▎鎴炲枂闁告洦鍎烽敃鍌涚厸闁割偅鑹炬禍楣冩倵濞堝灝鏋撻柛瀣尭閳规垿顢欑喊鍗炲壋闁汇埄鍨靛▍锝夊焵椤掑倹鍤€濠⒀冮閳绘捇骞嬮悩鍐茬毇婵＄偛顑呮鍛婄椤栫偞鐓曟繝濠傚暟閻忚京绱掗鑺ュ磳闁诡喗鐟╅幆鍌炲传閵壯屾П闂備礁鎲￠悷顖炲垂閻㈢绀傛慨妞诲亾鐎规洖鐖奸幃鈺傛綇椤愩値妲归梻渚€娼荤拹鐔煎礉瀹€鍕亗婵炲棙鎸哥憴锔锯偓骞垮劚閹冲酣寮崼鏇熺厾闁哄娉曢悞閿嬨亜椤愵剛鐣电€规洘宀搁幃鈺冪磼濡厧缍夐梻?
     */
    private List<Double[]> calculateAdjacentPoints(List<Double[]> originalVertices, List<Double[]> externalVertices) {
        List<Double[]> adjacentPoints = new ArrayList<>();
        
        if (externalVertices == null || externalVertices.isEmpty()) {
            return adjacentPoints;
        }
        
        // 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰閳诲瞼绱掗崣妯哄祮鐎规洩缍侀、鏃堝炊鐠虹儤鍊庣紓鍌欒兌婵敻銆冮崨鏉戝瀭闁割煈鍋呴崣蹇涙倵閿濆骸澧ù鐙€鍨堕弻娑橆煥閸愨晜鎷卞┑鐘亾妞ゆ牗绮堥悞濠囨煕閹炬鍟╅崠鏍⒑濮瑰洤濡奸悗姘间簼瀵板嫬顓奸崱妯规唉闂佺懓顕慨瀛樼?
        // 闂備礁鎲＄敮妤冩崲閸岀儑缍栭柟鎵閸婄兘鏌ｈ閹芥粎绮ｅΔ鈧…鍧楁嚋閻㈤潧鈷岄梺绋块閻°劑濡甸崟顖氱濞达綁鏅查崠鏍⒑濮瑰洤濡奸悗姘煎櫍椤㈡牠宕堕妸褉鏋栭梺閫炲苯澧紒瀣樀椤㈡棃宕卞鎯у
        Double[] initialPoint = externalVertices.get(0);
        // 闂備礁鎼悧婊堝磻閸曨垱鍋勬い鎺戝閸婄兘鏌ｈ閹芥粎绮ｅΔ鈧…鍧楁嚋閻㈤潧鈷岄梺绋块閻°劑濡甸崟顖氱濞达綁鏅查崠鏍⒑濮瑰洤濡奸悗姘煎幖閵嗘帡宕奸弴鐐缎曢柟鐓庣摠缁诲嫰顢旈崡鐐╂闁瑰灝鍟╃花浠嬫煕?
        Double[] endPoint = externalVertices.get(externalVertices.size() - 1);
        
        // 濠电偞鍨堕幐鎼佸箹椤愶箑鍨傞柛顭戝亝閸欏繘鎮楅敐搴″濞寸媭鍨堕弻鐔煎垂椤愩垹濮㈤梺绯曟櫅閻倸顕ｉ悽绋跨劦妞ゆ帊鐒︽禍銈夋煙鐎电校闁伙絽宕埥澶愬箻妫版繃顥撶紓浣筋嚙闁帮綁骞冨▎鎾村剬闁告縿鍎抽ˇ鈺呮⒑閸涘鐒介柛鐘查叄閹€斥枎閹惧疇袝閻庡箍鍎遍悧蹇涙偟閸洘鐓熼柨婵嗘噹椤ㄦ瑧绱掑Δ鈧崐鍧楀箚閸愵喖绀嬫い鎾跺仒閸栨牠姊?
        Double[] initialAdjacent = findNearestPointOnPolygon(initialPoint, originalVertices);
        if (initialAdjacent != null) {
            adjacentPoints.add(initialAdjacent);
        }
        
        // 濠电偞鍨堕幐楣冨磿閵堝拑鑰挎い鏍ㄧ矆閻掑﹪鏌涢幘妤€鍟╅崠鏍⒑閻熸壆浠涢柛搴㈠▕瀹曞搫鐣濋崟顐㈢彉闂侀€炲苯澧繛鐓庮煼楠炲鎮╁畷鍥ㄦ濠电偞鍨堕幐楣兯夐幇顔藉床閹兼番鍔嶉崐鐑芥煟瑜嶉幗婊呯矆閸曨垱鐓曟俊顖滃帶閺嬪酣鏌ｉ幘瀵告噰鐎规洏鍎遍濂稿醇閵忋垹楠勯梻浣风串缁茶姤绺介弮鍌滅當闁稿瞼鍋為崕宥夋煕閺囥劌骞栧ù鐙€鍨堕弻?
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剛顦┑鐘绘涧濡厼顭囧Δ鍛厽闁归偊鍠楅崵鈧梺纭呮腹閸楀啿顕ｉ悽鍓叉晝闁靛繈鍨归弳锟犳⒑缂佹ê濮屾い顐㈩槸鍗遍柟闂寸鐟欙箓鎮橀悙璺轰汗妞ゅ繐宕埥澶愬箼閸愌呮晼闂佺顑戠紞渚€寮鍥︽勃闁芥ê顦伴柨顓炩攽閻戝洨绉い鏇嗗嫭鍙忛柣鎰嚟閳绘棃鏌嶈閸撴瑩鍩?
        if (initialPoint != endPoint && 
            (initialPoint[0] != endPoint[0] || initialPoint[1] != endPoint[1])) {
            Double[] endAdjacent = findNearestPointOnPolygon(endPoint, originalVertices);
            if (endAdjacent != null) {
                adjacentPoints.add(endAdjacent);
            }
        }
        
        return adjacentPoints;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘宠壘鐎氬鏌嶈閸撴稓妲愰幒妤€閱囬柨婵嗘川瑜伴箖姊烘导娆戝埌闁活剙銈稿畷?- 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崘顏佹灃闂侀€炲苯澧紒瀣樀椤㈡棃宕卞鎯у闂佽娴烽弫鎼佸箠閹炬儼濮抽柛娆忣槸缁剁偞鎱ㄥ鍡椾喊闁搞倕顑夊鍫曞醇濠靛洩纭€闂佸搫鎷嬮崑濠囧箖濞嗘挸鎹舵い鎾跺枔閺嗙娀姊虹拠鈥崇仩闁瑰啿閰ｅ畷?
     * 闂備礁婀遍。浠嬪磻閹剧粯鐓涢柛顐ｇ箥濡茶偐绱掔紒妯虹伌鐎规洖鐖奸幃娆撳垂椤愩倖娈搁梺鍝勵槸閻楀棗锕㈤柆宥呯柧妞ゆ劧闄勯崐鐑芥煟閻斿搫顣肩紓宥呯箻閺屸€愁吋閸涱喖鏋犻梺绋跨箲钃遍悗鐢靛帶椤繈骞囨担纭呮櫑闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
     */
    private List<Double[]> calculateFinalBoundaryPoints(List<Double[]> adjacentPoints, List<Double[]> originalVertices) {
        List<Double[]> finalBoundaryPoints = new ArrayList<>();
        
        // 闂佽娴烽弫濠氬焵椤掍胶銆掗柤鍦亾閹便劑鏁愰崨顖滎吅闂備礁寮剁划宥囩矙婢跺苯顥氶悗锝庡墰椤斿懘姊虹紒妯哄婵炲懌鍨诲Σ鎰攽鐎ｎ亞顓奸梺璇″瀻閸愵亜甯撻梺璇叉捣閺佹悂骞婇幘鎯板С闁哄被鍎查崕宥夋煕閺囥劌鏋ら柣婵嗙埣閺岋綁鏁愰崨顖滀紘闂?
        for (Double[] adjacentPoint : adjacentPoints) {
            // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐎ｎ偂姘﹀┑鐐叉闁帮綁宕电€ｎ喗鐓熼柟閭﹀灱濞兼劖绻濋埀顒佹媴閻熸壆绐炲┑顔斤供閸嬪﹪宕电€ｎ喖绾ч柛顐ｇ箖鐠愶繝鏌￠崪浣镐喊闁诡喗鐟╅幆鍌炲传閵壯屾Х闂備礁鎲￠悷锕傛偋閺冨牊鍤堥柟瀵稿仦婵鈧箍鍎遍幊澶愬磻?
            List<Double[]> boundaryPointsForAdjacent = findBoundaryPointsForAdjacent(adjacentPoint, originalVertices);
            
            if (boundaryPointsForAdjacent.size() == 1) {
                // 闂備礁鎲￠〃鍡椕洪幋锔界厒婵犲﹤瀚紞鍥煕閿旇骞楁繛鍛礋閺岋綁骞囬幍鍐蹭壕婵炴垶姘ㄥΣ蹇涙煟閻斿憡纾婚柤鍐茬埣閸ㄦ儳螣閼姐倐鏀冲┑鐘绘涧濡寰婇崸妤佺厸濞达綀濮よぐ褏绱掓潏銊у礌etClosestPointOnSegment闁诲氦顫夐悺鏇犱焊濞嗘垵鍨濋柨鐔哄Т閻鏌″鍐ㄥ婵炲牐顕ч湁婵犲﹤鍟敍鏃傜磼?
                finalBoundaryPoints.add(boundaryPointsForAdjacent.get(0));
            } else if (boundaryPointsForAdjacent.size() > 1) {
                // 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘梹銇勯幘瀵哥畼缂佸鎸抽弻娑欑節閸曗斁鍋撶€ｎ剚顫曟繛鍡楁禋閸熷懘鏌熺紒妯虹瑨缂佹彃娼￠弻鈩冩媴閼恒儱娑х紓?
                Double[] medianPoint = calculateMedianPoint(boundaryPointsForAdjacent);
                // 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘棃骞栧ǎ顒€鈧鎮甸崼鏇熺厽闁挎繂鎳愰悘杈ㄧ箾閸喎鐏︾紒鍌氱Ч婵″爼宕惰閸栨牠姊洪幐搴ｂ槈闁绘妫濆畷娆撴晸閻樿尙鐓戦梺鍝勭Р閸斿秴鈻撴繝姘叆?
                Double[] intersectionPoint = findIntersectionWithBoundary(adjacentPoint, medianPoint, originalVertices);
                if (intersectionPoint != null) {
                    finalBoundaryPoints.add(intersectionPoint);
                }
            }
        }
        
        return finalBoundaryPoints;
    }
    
    /**
     * 闂備礁鎲￠懝楣冩偋閸℃稒鍤愰柣鏃€鎮舵禍婊堟煕閹捐尙顦﹀ù鐙€鍨遍〃銉╂倷鐠鸿櫣鍘柣銏╁灡閹稿骞?
     * 闂備胶顭堢换鎴炵箾婵犲洤鏋佹い鎾卞灪閺咁剚鎱ㄥ鍡楀缂佺姵鍨甸—鍐Χ閸偄鏁界紓浣虹帛瀹€鎼佸箖閹€鏋庨柟閭﹀墻閺嗭繝姊洪崫鍕妞わ富鍨抽弫顕€顢曢敐鍐х盎闂佸憡鎸风粈浣圭椤栫偞鐓ユ繛鎴烆焾鐎氭壆鎲搁幎濠傛搐缁€鍡涙煟濡偐甯涢柣婵堝枎閳藉骞橀崡鐐╁亾閹剧粯鐓傛繝濠傚缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊箛閹靛啿浜炬繛鎴炲嚬閸氬懘姊婚崒姘偓鎼侇敋椤撶偐鏋旈柟杈剧畱閸屻劑鏌ｉ弮鍌ゅ劆闁逞屽墮閿曨亪骞?
     */
    private List<Double[]> combineVertices(List<Double[]> originalVertices, List<Double[]> externalVertices, List<Double[]> boundaryPoints) {
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Г缂備線顤傞崣鍐ㄧ暦濡ゅ懎唯闁靛鍎查ˉ婵嬫⒑鏉炴媽鍏屽褎顨呭嵄闁归棿绀佺憴锕傛煥閺冨洤鍔电紒鈧刊妾渓l
        if (originalVertices == null || originalVertices.isEmpty()) {
            // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞婵炵偓鐓㈤梺鏂ユ櫅閸燁垳绮婚幒妤佺厱婵☆垳鍘ч弸娑㈡煏閸℃韬柟顔界懇閹倿宕妷褜妲烽梻浣告啞閻燁垱绂嶉敐鍥ㄥ床闁硅揪绠戦悙濠囨煟閹邦剛鎽犻柣鎾亾濠碉紕鍋戦崐鏇熸櫠鎼淬劌纾?
            List<Double[]> result = new ArrayList<>();
            if (externalVertices != null) {
                result.addAll(externalVertices);
            }
            if (boundaryPoints != null) {
                result.addAll(boundaryPoints);
            }
            return result;
        }
        
        // 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰閳诲瞼绱掗崣妯哄祮鐎规洏鍨介幃銏㈢箔鐞涒€充壕濠电姴娲﹂崐?闂佸搫顦悧鍡楋耿闁秴鐤炬い鎰堕檮閸?闂備礁鎼崐鐑藉础閹惰棄违濠电姴娲﹂崐?闂佸搫顦悧鍡楋耿闁秴鐤炬い鎰堕檮閸?闂備礁鎲￠…鍥窗閺嶎厼违濠电姴娲﹂崐?闂備焦鐪归崝宀€鈧矮鍗虫俊瀛樻償閵忊€冲妳闂婎偄娲﹂弻銊х箔閹剧粯鐓?
        // 闂備胶顭堢换鎴炵箾婵犲洤鏋佹い鎾卞灪閺咁剚鎱ㄥ┑鍫rtVerticesForExpansion 闂傚鍋勫ú銈夊箠濮椻偓婵＄绠涢弮鍌楁敵濠电娀娼ч悧濠傗枔瀹€鍕厵缁剧増锚娴滈箖姊洪崫鍕妞わ富鍨抽弫顕€顢曢敐鍐х盎闂佸憡鎸风粈浣圭椤栫偞鐓ユ繛鎴烆焽婢э附绻涢崨顓烆劉缂佸倸绉瑰畷鍗烆潩閸忚壈鏅ч梻浣告啞鐢偞绻涢埀顒佺箾閸涱參鍙勯柟顖氬暣瀹曠喖顢旈崶锝嗗珱濠电偞鍨堕幖鈺傜閿濆洨鍗氶柣鏂垮悑閸嬧晝鈧厜鍋撻柍褜鍓熷畷鎴︽晲閸℃瑢鏋欓柣搴秵閸犳鏁嶉悢鍏肩厽闁靛鍎遍顐︽煕鐎ｅ墎绉柡?
        return sortVerticesForExpansion(originalVertices, externalVertices, boundaryPoints);
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壈闂佽壈宕甸崰鎰矚闁稁鏁婄痪鎷岄哺浜涘┑鐐茬摠閸ゅ酣宕愰弴鐑嗗殨?
     * 婵犵數鍋涢ˇ顓㈠礉瀹ュ绀堝ù鐓庣摠閺咁剚鎱ㄥ鍡楀箺闁绘挴鍋撳┑锛勫亼閸婃洘鏅舵惔銊ョ；闁挎繂妫涢埢鏃堝箹缁厜鍋撻搹顐ｇ伋濠碉紕鍋戦崐鏇熸櫠鎼淬劌纾婚柨婵嗩槹閸庡秹鏌涢弴銊ょ凹闁哥偛顦甸弻娑樷枎韫囷絾效濡炪們鍎遍幊搴ㄦ箒闁诲函缍嗘禍婊堝吹閹烘鐓曢柨鏃囶潐濞堢灒rtVerticesForExpansion濠电偞鍨堕幖鈺呭储婵傚憡鍋╂い鎺戝缁?
     * 闂佸搫顦弲婊堟偡閳哄懎闂柣鎴ｆ閻銇勮箛鎾村婵﹦鍋撶换娑㈠级閹搭厼鍓遍梺鍝勬４缁查箖骞忛崨鏉戠婵懓娲犻崑鎾寸節閸ャ劌鈧兘鏌ｉ悢鍛婄凡闁诲繑鐟╅幃璺衡槈閺嵮冾瀷濠电偛鐗婇崹鍧楀箠濞戙埄鏁傞柛鈩冾焽閵堝弶绻涚€涙ê娈犻柛濞у懏顫曟繛鍡樺姈婵瓨绻濇繛鎯т壕闁荤姵鍔楅崰鎰嚗閸曨垰鐐婇柍鍝勫€瑰▓銏犫攽閳藉棗浜愰柛瀣崌閺岋紕浠︾拠鎻掑Х缂?
     */
    private List<Double[]> fixCrossings(List<Double[]> vertices) {
        // 闂備焦鐪归崹纭呫亹婢跺矁濮虫い鎺戝濡ê霉閸忚偐鏆橀柍褜鍓欓敃顏堝箖濞嗘挻鐒绘繛鎴炴皑閹插潡姊洪崨濠庣劷闁哥姵鐗犳俊瀛樼節閸ャ劌鈧兘鏌涢敂璇插箺闁伙絽宕湁婵犙冪仢閳ь剚娲栭锝囨崉娴ｆ洘鐩崺鈧い鎺嗗亾闂囧鎮楅敐搴濈盎闁搞倖甯￠弻娑㈡晜鐟欏嫭顔塷rtVerticesForExpansion濠电偞鍨堕幖鈺呭储婵傚憡鍋╂い鎺戝缁?
        // 闂佸搫顦弲婊堟偡閳哄懎闂柣鎴ｆ閻銇勮箛鎾村窛缂佺姵鐓￠弻娑㈠Ψ瑜嶆禒婊堟偨椤栵絽浜扮€规洘鑹捐灃闁告洍鏂侀崑鎾诲礃椤旇姤娅栭梺鍓插亝缁诲秴煤閵堝鐓涢悗锝庡亞閻帗绻涢幘鍐差暢闁硅尙澧楃粭鐔哥節閸曨収妲烽梻浣告啞閻燁垶宕归娑氼洸闁圭儤鍤炴惔锝嗗珰闁圭粯甯╁杈ㄤ繆椤愩倕顣肩紒璇插€胯棟濠电姵纰嶉崕宥夋煕閺囥劌鏋涙繛鍫滃嵆閺岋綁鍩￠崨顔界彸濠殿喗锕粻鎴ｇ亱闂侀€炲苯澧棁澶愭倵閿濆懎顣崇紒鈧?
        return new ArrayList<>(vertices);
    }
    
    /**
     * 闂傚倷鐒﹁ぐ鍐矓閻㈢钃熷┑鐘插婵ジ鏌ㄥ☉妯侯伀闁哄棭鍓欓湁婵犲﹤鍟伴悘鍗炍旈悩鍙夊闁靛洤瀚板畷婊勬媴闂€鎰闂備焦瀵х粙鎴濓耿瀵ゅ槉B闂佸搫顦悧鍡楋耿闁秴鐤炬い鎰╁焺濞存牠鏌涢埥鍡楀箻缂佲偓?
     * 闂備胶顫嬮崟顐㈩潔闂佺粯鐗徊鍓р偓鐢靛帶椤繈骞囨担纭呮櫑闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
     */
    private List<Double[]> calculateSecondVertices(List<Double[]> vertices) {
        if (vertices.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣股戠紞鍥煕閿旇骞楁繛鍛礃娣?
        double minX = vertices.get(0)[0];
        double maxX = vertices.get(0)[0];
        double minZ = vertices.get(0)[1];
        double maxZ = vertices.get(0)[1];
        
        for (Double[] vertex : vertices) {
            minX = Math.min(minX, vertex[0]);
            maxX = Math.max(maxX, vertex[0]);
            minZ = Math.min(minZ, vertex[1]);
            maxZ = Math.max(maxZ, vertex[1]);
        }
        
        // 闂備礁鎲＄敮妤冪矙閹寸姷纾介柟璁崇簿BB闂備焦鎮堕崕鑼矙閹扮増鐓傛繝濠傛噺閸犲棝鏌熼悜妯荤叆濞寸媭鍨堕弻銊モ槈濡灝顏┑鐐插悑椤ㄥ﹤顕ｆ禒瀣倞鐟滃秶寰婇崸妤佺厸濞达綀娅ｇ弧鈧柣鐘冲姉閸犳牕顕ｉ銈傚亾濞戞鎴濃枔?
        List<Double[]> secondVertices = new ArrayList<>();
        secondVertices.add(new Double[]{(double) Math.round(minX), (double) Math.round(minZ)}); // 闁诲骸缍婂鑽ょ磽濮樿京绠?
        secondVertices.add(new Double[]{(double) Math.round(maxX), (double) Math.round(minZ)}); // 闂備礁鎲￠悷銉╁储閺嶎偆绠?
        secondVertices.add(new Double[]{(double) Math.round(maxX), (double) Math.round(maxZ)}); // 闂備礁鎲￠悷銉╁储閺嶎偆绠?
        secondVertices.add(new Double[]{(double) Math.round(minX), (double) Math.round(maxZ)}); // 闁诲骸缍婂鑽ょ磽濮樿京绠?
        
        return secondVertices;
    }
    
    /**
     * 闂備礁鎼ú銈夋偤閵娾晛钃熷┑鐘插枤濞堟淇婇姘儓妞ゆ柨锕よ彁闁搞儻绲芥晶鎻捗?
     * 闂備礁鎲￠懝楣冩偋閸℃稒鍤愰柣鏃傚帶濡﹢鏌ｅΔ鈧悧鍡楊焽閹达附鐓曢柡鍐ㄥ€稿瓭闁诲繐娴氶崹璺虹暦濞差亝鍋勯柣鎴烆焽閺嗙姵淇婇妶鍛偓褰掓晪闁诲海顢婂▍鏇犵矙婢跺鍎熼柍銉ㄦ珪閺嬮箖姊洪崨濠勫ⅹ闁圭⒈鍋婇幃鈥斥枎閹炬潙鍓梺鍛婃处閸ㄥ疇銇愰鐐茬閺夊牊宕橀铏圭磼?
     * - 闂備礁鎼悧鍐磻閹炬緞鏃堝磼濞戝疇鈧潡鏌ｅ┑鎰灍闁诡垱妫冮弫鍌炴嚃閳哄啰宕堕梻浣告啞閻燂箓鎮ч弮鍌涘仏閺夊牄鍔庨埢鏃€銇勮箛鎾愁仼闂傚懏锕㈤弻娑㈠煛閸愩劍鐎洪梺鐓庣仛閸ㄥ灝顕ｉ幘顔肩妞ゆ梹鍎抽拏瀣⒑濮瑰洤濡奸悗姘嵆瀹曟垿宕ㄩ閿灃闂侀€炲苯澧紒瀣樀椤㈡梹鎯旈敐鍥舵Пnull闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵娉涚猾宥夋煟濡偐甯涙い搴㈢懇閺屾盯骞掗弴銊ユ櫍缂備浇椴哥换鍌濐暰闂佸搫鍊婚崑鎾崇暦閹绘崡褰掓晲閸℃顦ㄩ梺鍝勬閸犳牕鐣烽幇顓犻檮缂佸鐏濋獮瀣節閵忕姷鍨荤€广儱娲ㄩˇ?
     * - 闂備礁鎼悧鍐磻閹捐秮褰掓晲閸涙潙寮伴梺缁樼箥閸犳骞嗛弮鍫熸櫆闁兼祴鏅濋悰銉╂⒑閸涘﹦鎳勯柣妤佹⒒閹喗娼忛妸褉鏋栧銈呯箰鐎氼剟姊惧鈧弻娑㈠煛閸愩劍鐎洪梺鐓庣仛閸ㄥ灝顕ｇ€靛摜绀勯柣妯兼暩鏍￠梻浣圭湽閸斿瞼鈧矮鍗冲畷鎴﹀川椤旈敮鏋栭梺閫炲苯澧紒瀣樀椤㈡梹鎯旈敐鍥舵Пnull闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵娉涚猾宥夋煟濡偐甯涙い搴㈢懇閺屾盯骞掗弴銊ユ櫍缂備浇椴哥换鍌濐暰闂佸搫鍊婚崑鎾崇暦閹绘崡褰掓晲閸℃顦ㄩ梺鍝勬閸犳牕鐣烽幇顓犻檮缂佸鐏濋獮瀣煟韫囨挾绠板Δ鐘茬箳濡?
     */
    private AreaData.AltitudeData updateAltitudeData(double[] newAreaHeightRange) {
        AreaData.AltitudeData originalAltitude = selectedArea.getAltitude();
        
        if (originalAltitude == null) {
            // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙鍋嶉梺缁樻椤ユ捇宕㈤悽鍛婄厱婵﹩鍓涙牎闂佹眹鍎遍幊妯侯嚕閸偄绶為柟閭﹀枛閽傚鏌熼懝鐗堝涧缂傚倹纰嶇粚杈ㄧ節閸パ呯暢濡炪倖鐗撻崐妤冪矆婢跺⊕褰掓晲閸喓銆婇梺杞伴檷閸婃繂顕ｉ鍕骇闁割煈鍣ｅ▓顒勬⒑缁洘娅囬柛鐘虫尭閳绘捇骞嬮悜鍡樼暬濠碘槅鍨伴幖顐︻敊?
            return new AreaData.AltitudeData(newAreaHeightRange[1], newAreaHeightRange[0]);
        }
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑闁汇埄鍨奸崑鍛村箞閵娧€鍋撻敐搴℃灓鐟滈偊鍨堕獮鏍ㄦ綇妤ｅ啯顎嶉梺鍝勬閸犳牠鐛幋锔绘晩闁兼亽鍎涢敃鍌涚厱婵ê澧介悾閬嶆煟閿濆骸澧寸€殿噮鍋呭蹇涘礈瑜忛ˇ鈺傜箾閹寸偞灏紒澶婄－閹峰綊鎮為悥鎲€l闂?
        Double originalMin = originalAltitude.getMin();
        Double originalMax = originalAltitude.getMax();
        
        // 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯淇婄€碘偓ll濠德板€曢崐褰掓晪闁诲氦顫夋繛濠囧箚閸愵喖绀嬫い鎺嗗亾闁告挷鍗抽弻娑㈠箛椤曞懏娈扮紓浣介哺閻涱暃ll闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵娉涚猾宥夋煟濡偐甯涙い搴㈢懇閺屾盯骞掗弴銊ユ櫍缂?
        // null婵犳鍣徊楣冨蓟瑜旈獮鎴﹀閳╁啫顎撻梺鍝勬川閸犳劕鈻撻崼鏇熺厱濠电姴鍊堕崑銏ゆ煕韫囨枏鎴濐嚗閸曨噮妾紓浣介哺閻熲晛顕ｉ悽绋跨劦妞ゆ帊绶″▓妤佹叏濡炶浜鹃梺缁樼箥閸犳骞嗛弮鍫熸櫆闁兼祴鏅濋ˇ顐︽⒑閻熸壆鎽犻柣妤佹礋閹啴顢楅崟顒佹珫闂佸壊鍋嗛崰鎰濡ソ褰掓晲閸涙潙寮伴梺缁樼箥閸犳骞嗛弮鍫熸櫆闁兼祴鏅濋ˇ?
        Double mergedMin;
        Double mergedMax;
        
        // 闂備礁鎼悧鍐磻閹捐秮褰掓晲閸涙潙寮伴梺缁樼箥閸犳骞嗛弮鍫熸櫆闁兼祴鏅濋悰銉╂⒑閸涘﹦鎳勯柣妤佹⒒閹喗娼忛妸褉鏋栧銈呯箰鐎氼剟姊惧鈧弻娑㈠煛閸愩劍鐎洪梺鐓庣仛閸ㄥ灝顕ｇ€靛摜绀勯柣妯兼暩鏍￠梻浣圭湽閸斿瞼鈧矮鍗冲畷鎴﹀川椤旈敮鏋栭梺閫炲苯澧紒?
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐎ｎ剦娴勯柣鐘辩绾绢參顢旈柆宥嗙厱闁哄诞鍕創闂佺粯鎸婚悷锔剧矙婢跺矁濮抽柛鎾虫饯l闂備焦瀵х粙鎴︽偋婵犲伣娑㈠Χ婢跺鎸冮梺褰掑亰閸撴瑩鎮￠幋锔界叆婵炴垶锕╁褏绱掓潏銊х疄鐎规洏鍎靛畷鐓庘攽婵犲倻褰查梻浣告啞閼瑰墽鑺遍懖鈺佸К闁告劏鏅滈崕鐔兼煛閸愩劍绁╅柛銈咁樀閺岋繝宕熼鐕傜川ull闂備焦瀵х粙鎴︽偋婵犲伣娑㈠Χ婢跺鎸冮梺褰掑亰閸撴瑩鎮￠幋锔界叆?
        if (originalMin == null) {
            // 闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎卞Λ娆愮濡ソ褰掓晲閸涙潙寮伴梺缁樼箥閸犳骞嗛弮鍫熸櫆闁兼亽鍎绘蹇涙⒒閸屾艾顏柛鏃€娲熷畷鍝勎旈崨顔芥珫闁诲繒鍋犳慨銈夊磹閵堝棭娈介柣鎰煐绾箖鏌熼绛嬫畷缂佸锕幃銏☆槹鎼达紕鏆旈梺鑽ゅТ濞层垽宕曢幓鎹ㄦ盯濡舵径瀣ф寖闂佸綊鍋婇崜娆撴偂?
            mergedMin = null;
        } else {
            // 闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎卞Λ娆戠不閹烘鐓曢煫鍥ㄧ閼靛湱绱撳鍜佸剶闁诡垰鍟村畷鐔碱敆娴ｉ甯涘┑鐐舵彧缁蹭粙鏌婇敐澶嬪仧妞ゆ棁濮ら崕鐔兼煥閺冨洤浜圭紒鈧径濞炬闁规儳鍟块銏ゆ煛鐏炴枻韬€规洜濞€瀹曨偊宕熼鐐茬婵犳鍣徊鎯涙担鐑橆偨?
            // 闂備焦鐪归崹纭呫亹婢跺矁濮虫い鎺戝濡﹢鏌℃径濠勪虎闂傚懏锕㈤弻娑㈠煛閸愩劍鐎鹃梺閫炲苯澧扮紒顕呭灠鍗遍柟闂寸鐎氬顭跨捄鐚村伐鐎电増鏌ㄩ湁闁挎繂妫欑亸浼存煛閸℃瑥鏋涚€规洘鍔楀☉鍨槹鎼达綆妲卞┑鐐差嚟婵绮氶惀鎲€culateNewAreaHeightRange闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墯閺咁剙顭块懜鐢点€掔紒鈧径鎰厽闁宠桨鑳堕幗鐘绘偨椤栨稒缍戠悮娆撴煛閸愩劍宸濈紒?
            mergedMin = Math.min(originalMin, newAreaHeightRange[0]);
        }
        
        // 闂備礁鎼悧鍐磻閹炬緞鏃堝磼濞戝疇鈧潡鏌ｅ┑鎰灍闁诡垱妫冮弫鍌炴嚃閳哄啰宕堕梻浣告啞閻燂箓鎮ч弮鍌涘仏閺夊牄鍔庨埢鏃€銇勮箛鎾愁仼闂傚懏锕㈤弻娑㈠煛閸愩劍鐎洪梺鐓庣仛閸ㄥ灝顕ｉ幘顔肩妞ゆ梹鍎抽拏瀣⒑濮瑰洤濡奸悗姘嵆瀹曟垿宕ㄩ閿灃闂侀€炲苯澧紒?
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐎ｎ剦娴勯柣鐘辩绾绢參顢旈柆宥嗙厱闁哄诞鍕創闂佺粯鎸婚悷锔剧矙婢跺矁濮抽柛鎾虫饯l闂備焦瀵х粙鎴︽偋婵犲伣娑㈠Χ婢跺鎸冮梺褰掑亰閸撴瑩鎮￠幋锔界叆婵炴垶锕╁褏绱掓潏銊х疄鐎规洏鍎靛畷鐓庘攽婵犲倻褰查梻浣告啞閼瑰墽鑺遍懖鈺佸К闁告劏鏅滈崕鐔兼煛閸愩劍绁╅柛銈咁樀閺岋繝宕熼鐕傜川ull闂備焦瀵х粙鎴︽偋婵犲伣娑㈠Χ婢跺鎸冮梺褰掑亰閸撴瑩鎮￠幋锔界叆?
        if (originalMax == null) {
            // 闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎卞Λ娆愮濡綍鏃堝磼濞戝疇鈧潡鏌ｅ┑鎰灍闁诡垱妫冮弫鍌炴嚍閵夘垶鐛滈梻鍌氬€哥€氼參宕濋弴銏犳槬婵°倕鎳忛弲顒勬倶閻愯泛袚闁稿鍊栭〃銉╂倷閼哥數銆愰梺璇″枟椤ㄥ棛绮欐繝鍥ㄥ亜濡炲楠搁悾銊╂煟閻樺弶鍌ㄩ柛搴㈠絻铻為柕鍫濐槹閳锋棃鏌熼柇锕€澧柣?
            mergedMax = null;
        } else {
            // 闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎卞Λ娆戠不閹烘鐓曢煫鍥ㄧ閼靛湱绱撳鍜佸剶闁诡垰鍟村畷鐔碱敆娴ｉ甯涘┑掳鍊曢崐褰掓晝閵忋倖鍋熸い鏃囧Г閸庣喖鏌ㄩ弮鍥т汗缂佲偓婢跺ň妲堥柟鎯у暱椤掋垽鏌＄仦鏂よ含鐎规洜濞€瀹曨偊宕熼鐐茬婵犳鍣徊鎯涙担鐑橆偨?
            // 闂備焦鐪归崹纭呫亹婢跺矁濮虫い鎺戝濡﹢鏌℃径濠勪虎闂傚懏锕㈤弻娑㈠煛閸愩劍鐎鹃梺閫炲苯澧扮紒顕呭灠鍗遍柟闂寸鐎氬顭跨捄鐚村伐鐎电増鏌ㄩ湁闁挎繂妫欑亸浼存煛閸℃瑥鏋涚€规洘鍔楀☉鍨槹鎼达綆妲卞┑鐐差嚟婵绮氶惀鎲€culateNewAreaHeightRange闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉墯閺咁剙顭块懜鐢点€掔紒鈧径鎰厽闁宠桨鑳堕幗鐘绘偨椤栨稒缍戠悮娆撴煛閸愩劍宸濈紒?
            mergedMax = Math.max(originalMax, newAreaHeightRange[1]);
        }
        
        return new AreaData.AltitudeData(mergedMax, mergedMin);
    }
    
    /**
     * 闂備礁鎲＄敮妤冪矙閹寸姷纾介柟鎹愵嚙缁犮儵鏌嶆潪鎷屽厡婵炲吋妫冮弻娑橆潩椤掍焦宕冲┑鐐茬墛閸ㄥ灝鐣峰ú顏呭亜闂佸灝顑呴埀?
     */
    private AreaData createExpandedArea(List<Double[]> vertices, List<Double[]> secondVertices, AreaData.AltitudeData altitude) {
        return new AreaData(
            selectedArea.getName(),
            convertToVertexList(vertices),
            convertToVertexList(secondVertices),
            altitude,
            selectedArea.getLevel(),
            selectedArea.getBaseName(),
            selectedArea.getSignature(),
            selectedArea.getColor(),
            selectedArea.getSurfacename()
        );
    }
    
    /**
     * 闂傚倷鐒﹁ぐ鍐矓閸洘鍋柛鈩冪懅娑撳秹鏌ㄥ☉妯侯仾闁稿﹦鍋ら弻娑㈡晲閸愩劌顬嬫繝娈垮枟閹倿鐛埀?
     */
    public void reset() {
        this.selectedAreaName = null;
        this.selectedArea = null;
        this.newVertices.clear();
        this.isRecording = false;
        this.isActive = false;
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
    }
    
    /**
     * 缂傚倸鍊风紞鈧柛娑卞灡閺嗕即鏌ｉ悩杈╁妽婵犮垺顭囩槐鐐差吋婢跺﹤宓嗛梺鍝勵槹閸╁牆螣鐎ｎ亶娓婚柕鍫濇噹椤ｆ娊鏌?
     */
    public void continueRecording() {
        if (!isRecording || client.player == null) {
            return;
        }
        
        sendMessage(I18nManager.translate("expandarea.message.vertex.record.continue") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("easyadd.message.record"), Formatting.GREEN);
    }
    
    /**
     * 闂佽娴烽幊鎾诲嫉椤掑嫬鍨傛慨妯块哺婵ジ鏌℃径搴㈢《缂佸瀵ч〃銉╂倷鐎涙ɑ鐎紓浣虹帛濮樸劑鍩€椤掍胶鈯曢柨姘舵煟閹惧鎳囩€规洏鍎遍濂稿幢濡炵粯瀵橀梺?
     */
    public void finishAndSave() {
        if (!isRecording || client.player == null) {
            return;
        }
        
        if (newVertices.size() < 3) {
            sendMessage(I18nManager.translate("expandarea.error.vertex.record"), Formatting.RED);
            return;
        }
        
        // 闂備胶顭堥鍡欏垝瀹ュ鏁嗘繛鎴炵婵ジ鏌℃径搴㈢《缂佸瀵ф穱濠囶敍濡炶浜剧€规洖娲ㄩ、?
        this.isRecording = false;
        
        // 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡舵径瀣虎闂佺粯顨呴悧濠勭不閹烘鐓熼柕濞垮劚椤忊晜銇勯敂瑙勬珚闁诡喖鐖煎畷鍗炩槈閹烘垳澹曢梺鍝勫缁绘帞鏁?
        try {
            processAreaExpansion();
        } catch (Exception e) {
            sendMessage(I18nManager.translate("expandarea.error.area.expand_3") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
        }
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠碘€冲级閹倸鐣烽妷鈺傛櫇闁逞屽墴钘濋柍鍝勫€婚々鏌ュ箹濞ｎ剙鐏柕鍥╁枛閺屾盯寮拠鎻掝瀷婵犳鍠楅幃鍌炵嵁閳?
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠碘€冲级閹倸鐣烽妷鈺傛櫇闁逞屽墴钘濋柍鍝勫€婚々鏌ュ箹濞ｎ剙鈧倝宕崜浣瑰枑闁绘鐗婄粈鍫濃攽椤旇姤鍊愭鐐╁亾?
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * 闂備胶顭堥鍡欏垝瀹ュ鏁嗘繛鎴欏灩缁犮儵鏌嶆潪鎷屽厡婵炲吋姊圭换娑氱礄閻樺搫鍘￠梺?
     */
    public void stopExpand() {
        isActive = false;
        isRecording = false;
        selectedArea = null;
        selectedAreaName = null;
        newVertices.clear();
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
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
                    Path areaPath = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
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
     * 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑橆煥閸涙澘鐓戦梻浣虹帛婢规洟寮插☉銏╂晩?
     */
    private void sendMessage(String message, Formatting formatting) {
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.literal(message).formatted(formatting), false);
        }
    }
    
    // ==================== 闂佸搫顦悧鍡涘箠鎼淬垺鍙忔い蹇撶墕濡﹢鎮峰▎蹇擃伀闁靛棗锕幃妤呭捶椤撶偘鎴峰Δ?====================
    
    /**
     * 闂備礁鎲＄敮鍥磹閺嶎厼钃熼柛銉墯閸婄兘鎮峰▎蹇擃仾缁楁垿姊洪崨濠勫暡缂佺姵鍨甸敃銏ゎ敂閸涱厾绐炲┑顔矫壕顓㈡偟閸撲焦鍠愰柤鍓插墮閻忊晠鏌涢悙瀛樺唉闁哄苯鐗撻垾锕傚箳閺冨偆妲遍梺璇茬箰缁绘劗鈧凹鍣ｅ畷鍨償閳锯偓閺嬫牠鏌￠崶鏈电敖缂佲偓?
     */
    private boolean isPointInPolygon(Double[] point, List<Double[]> polygon) {
        if (polygon.size() < 3) return false;
        
        double x = point[0];
        double y = point[1];
        boolean inside = false;
        
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double xi = polygon.get(i)[0];
            double yi = polygon.get(i)[1];
            double xj = polygon.get(j)[0];
            double yj = polygon.get(j)[1];
            
            if (((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╅柤濮愬€楀Λ顖炴煙缁嬪灝鐦ㄩ柛鐐舵閳藉骞橀幎绛嬧偓妤併亜閿旇棄顕滄繛鐓庣箻閹兘骞忕仦鐣屽帓闂備焦鐪归崝宀€鈧凹浜滈～婵嬫晝閸屾稑鈧?
     */
    private List<Double[]> findLinePolygonIntersections(Double[] lineStart, Double[] lineEnd, List<Double[]> polygon) {
        List<Double[]> intersections = new ArrayList<>();
        
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            Double[] polyStart = polygon.get(i);
            Double[] polyEnd = polygon.get(j);
            
            Double[] intersection = getLineIntersection(lineStart, lineEnd, polyStart, polyEnd);
            if (intersection != null) {
                intersections.add(intersection);
            }
        }
        
        return intersections;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘棃鏌曢崼婵囶棡闁藉倸顑囩槐鎺楁倷椤掆偓椤曟粍銇勯弬璺ㄧ劯闁诡垰鍟村畷鐔碱敂閸涱厽鏁梻?
     * 闂佸搫顦弲婊堝蓟閵娿儍娲冀椤撶喎鍓梺鍛婃处閸嬪懐绱炶箛娑欑厸鐎广儱鎳忛惌妤冪磼鏉堛劌顥嬮柟顕呭櫍椤㈡洟濮€閻樺灚鍋ч梻浣芥〃閼冲墎鎹㈠┑鍫熷闁绘梻鍘ч弸渚€鎮楀☉娅虫垵鈻?
     */
    private Double[] getLineIntersection(Double[] line1Start, Double[] line1End, Double[] line2Start, Double[] line2End) {
        double x1 = line1Start[0], y1 = line1Start[1];
        double x2 = line1End[0], y2 = line1End[1];
        double x3 = line2Start[0], y3 = line2Start[1];
        double x4 = line2End[0], y4 = line2End[1];
        
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-10) {
            return null; // 婵°倗濮烽崑娑㈡偪閸ヮ兙鈧線骞嬮悩鍨紡?
        }
        
        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;
        double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom;
        
        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            double x = x1 + t * (x2 - x1);
            double z = y1 + t * (y2 - y1);
            // 闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
            return new Double[]{(double) Math.round(x), (double) Math.round(z)};
        }
        
        return null;
    }
    
    /**
     * 闂備胶鎳撻悘姘跺磿閹惰棄鏄ョ€光偓閳ь剟骞戦崟顐熸斀闁割偒鍋嗛埀顒佺叀閺岀喓绮欓幐搴㈡倷闂佷紮缍佺粻鏍箖濞嗘垶鍠嗛柛鏇ㄥ厸缁垶鏌℃径鍡樻珕闁搞劎鎳撻埢鎾诲箣閻愯尙绐炲┑顔矫壕顓㈡偟閸撲焦鍠愰柤鍓插墮閻忚京绱掗弮鍌氭灈闁诡喗鍎抽悾婵嬪焵椤掑嫬纾?
     */
    private Double[] findNearestPointOnPolygon(Double[] point, List<Double[]> polygon) {
        if (polygon.size() < 2) return null;
        
        Double[] nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            Double[] p1 = polygon.get(i);
            Double[] p2 = polygon.get(j);
            
            // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閸婄兘鏌ｉ悢鍛婄凡闁绘挸鍊荤槐鎺楁倷椤掆偓椤曟粍銇勯弬璺ㄧ劯闁诡垰鍟村畷鐔碱敆娴ｉ甯涢梺鍝勵槴閺呮粓宕硅ぐ鎺戠；?
            Double[] closest = getClosestPointOnSegment(point, p1, p2);
            double distance = calculateDistance(point, closest);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = closest;
            }
        }
        
        return nearestPoint;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閸婄兘鏌ｉ悢鍛婄凡闁绘挸鍊荤槐鎺楁倷椤掆偓椤曟粍銇勯弬璺ㄧ劯闁诡垰鍟村畷鐔碱敆娴ｉ甯涢梺鍝勵槴閺呮粓宕硅ぐ鎺戠；?
     * 闂佸搫顦弲婊堝蓟閵娿儍娲冀椤撶喎鍓梺鍛婃处閸嬪懐绱炶箛娑欑厸鐎广儱鎳忛惌妤冪磼鏉堛劌顥嬮柟顕呭櫍椤㈡洟濮€閻樺灚鍋ч梻浣芥〃閼冲墎鎹㈠┑鍫熷闁绘梻鍘ч弸渚€鎮楀☉娅虫垵鈻?
     */
    private Double[] getClosestPointOnSegment(Double[] point, Double[] segStart, Double[] segEnd) {
        double px = point[0], py = point[1];
        double ax = segStart[0], ay = segStart[1];
        double bx = segEnd[0], by = segEnd[1];
        
        double dx = bx - ax;
        double dy = by - ay;
        
        if (dx == 0 && dy == 0) {
            // 闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
            return new Double[]{(double) Math.round(ax), (double) Math.round(ay)};
        }
        
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        
        double x = ax + t * dx;
        double z = ay + t * dy;
        // 闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
        return new Double[]{(double) Math.round(x), (double) Math.round(z)};
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘棃鏌曢崼婵囨悙濞寸媭鍨跺濠氬磼濠婂孩顥撶紓浣瑰姈缁嬫挾鍒?
     */
    private double calculateDistance(Double[] p1, Double[] p2) {
        double dx = p1[0] - p2[0];
        double dy = p1[1] - p2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 濠电偞鍨堕幑浣割浖閵婏箑绶炵€广儱妫欐禍銈夋煙鐎电孝濞寸媭鍨堕弻鐔煎垂椤愩垹濮㈤梺绯曟櫅閻ジ鍩€椤掑倹鏆╅柟铏尵閼洪亶寮婚妷锕€鍓梺鍛婃处閸犳岸鎮甸崼鏇熺厽闁挎繂鎳愰悘閬嶆煕?
     */
    private List<Double[]> findBoundaryPointsForAdjacent(Double[] adjacentPoint, List<Double[]> originalVertices) {
        List<Double[]> boundaryPoints = new ArrayList<>();
        
        // 缂傚倷鑳舵慨顓㈠磻閹剧粯鐓曢柡宥冨妿婢ь亪鏌ｉ妶鍛伃闁诡喖纾弫顕€顢欓崜褏宕堕梻浣烘嚀閻忔岸宕曢幎钘夋槬鐎光偓閳ь剟骞戦崟顐熸斀闁割偒鍋嗛埀顒佸絻閳藉骞樻０婵囶棑缂備浇顕ч柊锝夊箖濞嗘垶鍠嗛柛鏇ㄥ厸缁垶鏌℃径鍡樻珕闁搞劎鎳撻埢鎾诲箣閿曗偓缁€鍕煟濮椻偓娴滃爼宕电€ｎ喖绾ч柛顐ｇ箖鐠愶繝鏌￠崪浣镐喊闁?
        for (int i = 0; i < originalVertices.size(); i++) {
            int j = (i + 1) % originalVertices.size();
            Double[] p1 = originalVertices.get(i);
            Double[] p2 = originalVertices.get(j);
            
            Double[] closest = getClosestPointOnSegment(adjacentPoint, p1, p2);
            if (calculateDistance(adjacentPoint, closest) < 50.0) { // 50闂備礁鎼粔鍫曞储瑜斿畷顒勫箻椤旇棄鍓梺鍛婃处閸犳岸鎮甸崼鏇熺厽闁挎繂鎳愰悘閬嶆煕?
                boundaryPoints.add(closest);
            }
        }
        
        return boundaryPoints;
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣姑欢鐐存叏濮楀棗浜伴柛銈咁儔閺岋綁骞囬浣界濠电偛鐗婇崹鍓佺矙婢舵劦鏁囨繝闈涚墢瀛濋梻?
     * 闂佸搫顦弲婊堝蓟閵娿儍娲冀椤撶喎鍓梺鍛婃处閸嬪懐绱炶箛娑欑厸鐎广儱鎳忛惌妤冪磼鏉堛劌顥嬮柟顕呭櫍椤㈡洟濮€閻樺灚鍋ч梻浣芥〃閼冲墎鎹㈠┑鍫熷闁绘梻鍘ч弸渚€鎮楀☉娅虫垵鈻?
     */
    private Double[] calculateMedianPoint(List<Double[]> points) {
        if (points.isEmpty()) return null;
        if (points.size() == 1) {
            // 闂備礁鎲￠〃鍡椕洪幋锔界厒婵犲﹤鐗婇崐鐑芥⒑椤愶絿銆掗柣锕€绉归幃鐑藉即濮橀硸妲悷婊勬緲濞差厼顕?
            Double[] point = points.get(0);
            return new Double[]{(double) Math.round(point[0]), (double) Math.round(point[1])};
        }
        
        double sumX = 0, sumY = 0;
        for (Double[] point : points) {
            sumX += point[0];
            sumY += point[1];
        }
        
        double x = sumX / points.size();
        double z = sumY / points.size();
        // 闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
        return new Double[]{(double) Math.round(x), (double) Math.round(z)};
    }
    
    /**
     * 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘棃骞栧ǎ顒€鈧鎮甸崼鏇熺厽闁挎繂鎳愰悘杈ㄧ箾閸喎鐏︾紒鍌氱Ч婵″爼宕惰閸?
     * 闂佸搫顦弲婊堝蓟閵娿儍娲冀椤撶喎鍓梺鍛婃处閸嬪懐绱炶箛娑欑厸鐎广儱鎳忛惌妤冪磼鏉堛劌顥嬮柟顕呭櫍椤㈡洟濮€閻樺灚鍋ч梻浣芥〃閼冲墎鎹㈠┑鍫熷闁绘梻鍘ч弸渚€鎮楀☉娅虫垵鈻?
     */
    private Double[] findIntersectionWithBoundary(Double[] point1, Double[] point2, List<Double[]> originalVertices) {
        // 缂傚倷鑳舵慨顓㈠磻閹剧粯鐓曢柡宥冨妿婢ь亪鏌ｉ妶鍛伃闁诡喖纾弫顕€顢欓崜褏宕堕梺鍝勵槴閺呮粓寮婚妸銉冩椽寮介妸褉鏋栭梺闈涚墕濞层倖绂掗鐐茬骇闁冲搫鍟禒褔鏌涢妶鍛村弰闁诡垰鍟村畷鐔碱敂閸パ冪畱闂?
        double x = (point1[0] + point2[0]) / 2;
        double z = (point1[1] + point2[1]) / 2;
        // 闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
        return new Double[]{(double) Math.round(x), (double) Math.round(z)};
    }
    
    /**
     * 缂傚倷绀侀ˇ顖炩€﹀畡鎵虫瀺閹兼番鍔嶉悡鍌溾偓骞垮劚閹峰危婵犳碍鐓?
     */
    private List<Double[]> removeDuplicatePoints(List<Double[]> vertices) {
        List<Double[]> unique = new ArrayList<>();
        final double EPSILON = 0.001;
        
        for (Double[] vertex : vertices) {
            boolean isDuplicate = false;
            for (Double[] existing : unique) {
                if (calculateDistance(vertex, existing) < EPSILON) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                unique.add(vertex);
            }
        }
        
        return unique;
    }
    
    /**
     * 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰鏍￠柣銏╁灡閹稿骞嗛弮鍥╃杸婵炴垶鐗戦崑鎾寸節閸ャ劌鈧兘鏌ｈ閹芥粎绮ｅΔ鍛厱婵☆垳鍘ч弸娑㈡煏閸℃韬柟?闂佸搫顦悧鍡楋耿闁秴鐤炬い鎰堕檮閸?闂備礁鎼崐鐑藉础閹惰棄违濠电姴娲﹂崐?闂佸搫顦悧鍡楋耿闁秴鐤炬い鎰堕檮閸?闂備礁鎲￠…鍥窗閺嶎厼违濠电姴娲﹂崐?
     * 婵犳鍠楃换鎰緤閸ф鐏虫俊顖氱毞閸嬫捇宕烽鐐版埛濡ょ姷鍋涘ú顓㈠极瀹ュ拋娼╂い鎾跺剱濡喖姊洪崨濠傜瑲濠殿垳鏅划鈺呮偄閸忕厧浠奸悗鍏夊亾闁逞屽墴瀹曟垿鏁愰崱妯哄妳闂佸搫鍟ù鍌炲吹婢舵劖鐓欑紓浣姑粭鎺楁煕濞嗗繑顥㈤柟顖氬暣瀹曠喖顢旈崱娅恒垻绱撻崒姘灓闁革綆鍨冲Σ鎰攽鐎ｎ偄鈧潡鎮归搹鐟板妺闁稿﹦鏁婚弻鐔虹矙濞嗙偓楔闂佸憡鐟ョ€涒晠濡甸崟顖氫紶闁告洦鍘鹃。鏌ユ⒑閸︻厾甯涢悽顖涘浮瀹?
     * 闂備胶顭堢换鎴炵箾婵犲洤鏋佹い鎾卞灪閺咁剚鎱ㄥ鍡楀缂佺姵鍨甸—鍐Χ閸偄鏁界紓浣虹帛瀹€鎼佸箖閹€鏋庨柟閭﹀墻閺嗭繝姊洪崫鍕妞わ富鍨抽弫顕€顢曢敐鍐х盎闂佸憡鎸风粈浣圭椤栫偞鐓ユ繛鎴烆焾鐎氭壆鎲搁幎濠傛搐缁€鍡涙煟濡偐甯涢柣婵堝枎閳藉骞橀崡鐐╁亾閹剧粯鐓傛繝濠傚缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊箛閹靛啿浜炬繛鎴炲嚬閸氬懘姊婚崒姘偓鎼侇敋椤撶偐鏋旈柟杈剧畱閸屻劑鏌ｉ弮鍌ゅ劆闁逞屽墮閿曨亪骞?
     */
    private List<Double[]> sortVerticesForExpansion(List<Double[]> originalVertices, List<Double[]> externalVertices, List<Double[]> boundaryPoints) {
        if (originalVertices == null || originalVertices.isEmpty()) {
            return externalVertices != null ? new ArrayList<>(externalVertices) : new ArrayList<>();
        }
        
        if (externalVertices == null || externalVertices.isEmpty()) {
            return new ArrayList<>(originalVertices);
        }
        
        if (boundaryPoints == null || boundaryPoints.isEmpty()) {
            // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞婵炵偓鐓㈤梺鏂ユ櫅閸燁垳绮婚幒妤€绾ч柛顐ｇ箖鐠愶繝鏌￠崪浣镐喊闁诡喗鐟╅幆鍌炲传閵壯屾Х濠电儑绲藉ú锔炬崲閸曨垰姹查柍褜鍓熼弻鐔哄枈濡桨澹曢梻浣告惈閻楀棝藝椤栨粍鏆滄い鎰剁祷娴滄粓鏌涢幘鑼槮濞寸媭鍨堕弻銊モ槈濡偐浼囬梺鍛婃灝閸パ喰曢柟鍏肩暘閸ㄧ懓顭囬幋锔界厱闁哄啫鍊告牎闂佸搫鑻敃銉╁Φ閸曨垰绀堝ù锝夋櫜閸栨牠姊洪幐搴ｂ槈闁哄牜鍓欓妴鎺楀醇閺囩偠袝闁瑰吋鐣崹鍦箔閹捐绠归柡澶嬪灩缁犵粯淇婇懠棰濆殭妞ゎ偁鍨介敐鐐侯敇閻旈攱鏁梻?
            List<Double[]> allVertices = new ArrayList<>(originalVertices);
            allVertices.addAll(externalVertices);
            return sortVerticesWithoutCrossing(allVertices);
        }
        
        // 闂備胶鎳撻悘姘跺磿閹惰棄鏄ラ悘鐐插⒔閳绘棃鏌曢崼婵嗩伃闁搞倕顑呴埥澶愬箻鐎涙﹩娼″銈傛櫔缁犳捇骞嗛崘顔肩妞ゆ柨澧借ぐ楣冩⒑娴兼瑧鍒伴柣顒€銈稿畷鎴︽晲婢跺娅栭梺鍓插亝缁诲秹宕甸悩璇茬閻庢稒蓱閳锋劙鏌＄仦鏂ゆ敾闁靛洤瀚板畷婊勬媴闂€鎰闂備焦鐪归崝宀€鈧凹鍨堕獮鎴︽晲婢跺鈧兘鏌ｉ悢鍝勵暭閻庢俺灏欑槐鎾存媴閸濄儱鈪遍梺绋款儜缂嶄線寮?
        Double[] startBoundaryPoint = null;
        Double[] endBoundaryPoint = null;
        
        if (boundaryPoints.size() >= 2) {
            // 闂備胶鎳撻悘姘跺磿閹惰棄鏄ョ€光偓閳ь剟骞戦崟顐熸斀闁割偒鍋嗛埀顒佺叀閺岋繝宕掑▎蹇撶ギ闂侀潧妫楅敃顏堝箖濞嗘垶鍏滈柛娑卞枛闂傤垶姊虹紒妯哄闁硅櫕鎹囬獮蹇涘箥椤斿墽锛滈梺鍓插亖閸ㄨ绂掗鐐寸厸闁割偅鑹炬禍楣冩煛婢跺棙娅嗛柛銊ф嚀閳绘捇骞嬮悙瀵哥Ф闂侀潻瀵岄崢钘夆枍閺囥垺鐓?
            Double[] newStart = externalVertices.get(0);
            Double[] newEnd = externalVertices.get(externalVertices.size() - 1);
            
            double minDistToStart = Double.MAX_VALUE;
            double minDistToEnd = Double.MAX_VALUE;
            
            for (Double[] bp : boundaryPoints) {
                double distToStart = calculateDistance(newStart, bp);
                double distToEnd = calculateDistance(newEnd, bp);
                
                if (distToStart < minDistToStart) {
                    minDistToStart = distToStart;
                    startBoundaryPoint = bp;
                }
                if (distToEnd < minDistToEnd) {
                    minDistToEnd = distToEnd;
                    endBoundaryPoint = bp;
                }
            }
        } else if (boundaryPoints.size() == 1) {
            // 闂備礁鎲￠悷顖涚濠靛棴鑰垮ù锝呮贡閳绘棃鏌嶈閸撴氨绮欐径鎰垫晜闁搞儮鏅濊ぐ楣冩⒑娴兼瑧鍒伴柣顒€銈稿畷鎴︽晲婢跺娅栭柣蹇曞仧閸樠囧煝韫囨稒鐓熸い顐墮婵″ジ鏌熼鑺ュ鞍缂佸顦甸崺鈧い鎺嶈兌閳绘梹銇勮箛鎾搭棞濞寸媭鍨伴湁闁挎繂妫涢惌鍡涙偣閹邦喖鏋旈柟绉嗗嫷妲归幖绮光偓鎻掑闂備礁鎲＄划宀勬儔閼测晛鍨濋柟鍓х帛閸?
            startBoundaryPoint = boundaryPoints.get(0);
            endBoundaryPoint = boundaryPoints.get(0);
        }
        
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剛顓奸梺鍛婄懀閸庢娊鎮峰┑瀣厱闁圭儤鎸搁婊呯磼閺冨倸鏋涢柟顔藉劤閻ｆ繈鍩€椤掑嫬纾婚柨婵嗩槹閺咁剟鎮橀悙璺轰汗缂佺姳绮欓弻锝夋晲閸℃瑦鍣繝鈷€鍐х€殿喖鐏氬鍕節閸屾稒鐏冲┑锛勫亼閸婃洘鏅舵惔銊ョ；闁挎繂顦伴弲顒傗偓鍏夊亾闁逞屽墴瀹曟繆顦圭€规洏鍎查幆鏃堝閻樺磭绉归梻浣告啞濮婄粯鎱ㄩ悽绋胯摕濠电姴鍟ㄦ禍婊堟煕閹捐尙顦﹀ù?
        if (startBoundaryPoint == null || endBoundaryPoint == null) {
            List<Double[]> allVertices = new ArrayList<>(originalVertices);
            allVertices.addAll(externalVertices);
            if (boundaryPoints != null) {
                allVertices.addAll(boundaryPoints);
            }
            // 闂佽绨肩徊濠氾綖婢跺娅犵€广儱顦€氬顭块懜闈涘闁逞屽墮閿曨亪骞冨▎鎺嬩汗闁圭儤鍨堕鐔兼⒑閸濆嫬鈧粯鏅跺Δ鍐╂殰闁规儳鐡ㄩ崕鐔肩叓閸ャ儱鍔ょ紒鈧径鎰拻闁告劑鍔岀痪褎銇勯幒鎾垛姇缂佸倸绉规俊鍫曞川椤撶娀鐛?
            return sortVerticesWithoutCrossing(allVertices);
        }
        
        // 闂備胶鎳撻悘姘跺磿閹惰棄鏄ラ悘鐐靛亾缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊箛椤旇棄娈岀紓浣靛姀鐏忔瑩骞忛悩璇参ㄩ柨鏇楀亾缂佹彃顭烽弻娑滅疀鐎ｎ亜濮㈠┑鐐茬墛閸ㄥ爼骞忛崨顓у悑闁搞儮鏅滈悗顓㈡⒑閹稿海鈽夐柣妤€妫楅敃銏ゎ敂閸繂鍋嶉梺缁樻椤ユ捇宕㈤悽鍛婄厱婵﹩鍓涜倴濠电偛鐗婇崹鍨暦椤愶富鏁傞柛鎰靛幐閹封剝绻涢幋鐐存儎濞存粠浜俊瀛樼節閸ャ劌鈧兘姊洪锝囥€掓い鎾存そ濮婂宕掑┑鍥ф锭缂?
        int startBoundaryIndex = findBoundaryPointInsertIndex(startBoundaryPoint, originalVertices);
        int endBoundaryIndex = findBoundaryPointInsertIndex(endBoundaryPoint, originalVertices);
        
        // 濠电偞鍨堕幐鍝ョ矓妞嬪孩宕叉俊顖氬悑閸嬫鈧厜鍋撻柍褜鍓熼幃鎯р攽鐎ｎ亞顢呴梺鍝勬川婵敻鎮炬潏鈺冪＜濞撴艾鐏濋悘鈺冪磼閹典焦娅嗙紒鍌氱Ч婵″爼宕卞Δ鈧花銉╂⒑閹稿海鈽夊┑鍌涙⒐缁绘盯宕堕浣镐缓闂侀潧谩缂堢姷鍔嶆繛鐓庮煼楠炲洭顢欓悙顒佺槗闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣诡焽閳绘棃鏌曢崼婵囷紞闁哥偟鏅埀顒侇問閸燁偊宕掑鍛暟闂備焦鐪归崝宀€鈧凹鍓涢弫顕€顢曢敐鍐х盎闂佸憡鎸风粈浣圭椤栫偞鐓涘ù锝呭槻瀹撳棝鏌涢敐鍡樸仢闁绘侗鍠氶幑鍕传閸曨厺鎮ｉ梻鍌氬€哥€氥劑宕愬☉姘偨婵犻潧顑嗛崕妤併亜閹捐泛校闁伙絽宕埥澶愬箻椤栨矮澹曟繝?
        int n = originalVertices.size();

        // 闂備礁鎲＄敮鍥磹閺嶎厼钃熼柛銉墮濡ê霉閸忚偐鏆橀柍褜鍓欓敃顏堝箖濞嗘挸鎹舵い鎾跺枔閺嗙娀姊洪崷顓犲笡閻㈩垱甯″畷锝夊幢濞戞ɑ顥濋梺鑽ゅ枔婢ф宕愭繝姘叆婵炴垶顭囨晶娑欍亜閹烘挾鐭掔€规洏鍎甸獮瀣倷閼碱兛绮ｉ梻浣告啞閻熴儳绮旈幘顔肩畺婵犲﹤鐗婇弲?
        List<Double[]> orderedNewVertices = determineNewVerticesOrder(externalVertices, startBoundaryPoint, endBoundaryPoint);

        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剚顥?闂備礁鎲￠…鍥窗瀹ュ拋娼╅柨鏇炲€搁惌妤€顭块懜鐢点€掔紒鈧径鎰厱婵炲棗绻掑暩濠电偞娼欏Λ婵嗩嚕椤掍礁顕辨繛锝庡厴閸嬫挻绻濋崶銊モ偓椋庢喐閻楀牆绔鹃柍褜鍓欑€涒晠骞?
        if (hasCrossingBetweenNewAndOriginal(originalVertices, orderedNewVertices, startBoundaryPoint, endBoundaryPoint)) {
            List<Double[]> reversed = new ArrayList<>();
            for (int i = orderedNewVertices.size() - 1; i >= 0; i--) {
                reversed.add(orderedNewVertices.get(i));
            }
            orderedNewVertices = reversed;
        }

        // 闂佽崵濮崇欢銈囨閺囥垺鍋╅悹鍥ㄧゴ閺岋箓鏌熺粙鍨槰闁告柡鍋撻梻浣告惈閸婂骞婇幘璇茬畺婵犲﹤瀚々?start 闂?end 闂備焦鐪归崝宀€鈧凹鍨堕妴鍛存晝閸屾氨顦梺缁橆焽缁垶鎮甸悢鍏肩厽闁靛鍎遍鈺呮偨椤栵絽浜濋柕鍥у瀹曟粍鎷呴梹鎰闂備浇妗ㄥ鎺楀础閹惰棄闂繛宸簼閺咁剟鏌涢锝囩煂闁荤喐绻堥弻娑㈠冀瑜庨崳娲倵?start 闂備礁鎼悧婊堝礈濠靛顥婇柍鍝勬噺閺?
        int forwardDeleteCount = (endBoundaryIndex - startBoundaryIndex + n) % n;
        int backwardDeleteCount = n - forwardDeleteCount;

        // 闂備礁鎼粔鏉懨洪埡鍜佹晩闁搞儮鏅滈崯鍝劽归敐澶樻缂佺姳绮欓弻锝夋晲閸℃瑦鍣┑鐐茬墛閸ㄥ灝鐣烽姀銈嗗亜缂佹銆€閸嬫挻绻濋崶銊モ偓?
        boolean[] keep = new boolean[n];
        for (int i = 0; i < n; i++) keep[i] = true;

        if (n <= 2) {
            // 濠电姰鍨介·鍌涚濠婂牊鍎嶆い鏍ㄧ◤娴滄粓鏌涢幘鑼槮濞寸媭鍨堕弻銊モ槈濡偐浼囧┑鐐村絻濞硷繝鐛幒妤€唯闁靛绠戦埀顑惧€栭〃?
            List<Double[]> fallback = new ArrayList<>(originalVertices);
            fallback.add(startBoundaryPoint);
            fallback.addAll(orderedNewVertices);
            fallback.add(endBoundaryPoint);
            return sortVerticesWithoutCrossing(fallback);
        }

        if (forwardDeleteCount <= backwardDeleteCount) {
            // 闂備礁鎲＄敮鐐寸箾閳ь剚绻涢崨顓烆劉缂?startBoundaryIndex+1 闂?endBoundaryIndex闂備焦瀵х粙鎴︽偋閸℃瑢鍋撻崹顐€跨€?endBoundaryIndex闂?
            int idx = (startBoundaryIndex + 1) % n;
            for (int k = 0; k < forwardDeleteCount; k++) {
                keep[idx] = false;
                idx = (idx + 1) % n;
            }
        } else {
            // 闂備礁鎲＄敮鐐寸箾閳ь剚绻涢崨顓烆劉缂?endBoundaryIndex+1 闂?startBoundaryIndex闂備焦瀵х粙鎴︽偋閸℃瑢鍋撻崹顐€跨€?startBoundaryIndex闂?
            int idx = (endBoundaryIndex + 1) % n;
            for (int k = 0; k < backwardDeleteCount; k++) {
                keep[idx] = false;
                idx = (idx + 1) % n;
            }
        }

        // 缂備胶铏庨崣搴ㄥ窗閺囩姵宕叉慨妯垮煐閸ゅ﹪鏌涢幇顖氱毢婵絽鑻彁闁搞儯鍔庣粻姗€鏌￠崱蹇撲壕濠电偞鍨堕幐鎾磻閹炬枼妲堥柟鍨暕缁ㄥ鎮介锝呬簼闁靛洤瀚板畷婊勬媴闂€鎰闂備焦瀵х粙鎴︽偋閹炬緞锝囨嫚瀹割喗妗ㄩ梺闈涢獜缂嶅棝宕甸弽顓熺厱闁圭儤鏌ㄥ瓭闂佸憡鐟ョ换姗€寮婚崼銉⑩偓锕傚箳閺冨偆妲?
        boolean anyKept = false;
        for (boolean b : keep) { if (b) { anyKept = true; break; } }
        if (!anyKept) {
            // 闂備焦鎮堕崕鎶藉磻閵堝鐒垫い鎴ｆ娴滈箖姊洪崨濠傛诞妞わ綇濡囬幑銏狀潩閼搁潧浠煎┑鐐叉閸旀洟銆傞弻銉︾厸闁割偅绻嶅Σ鎼佹偨椤栵絽浜濋柕鍥у瀹曟粍鎷呴梹鎰闂備焦鐪归崝宀€鈧凹鍙冨濠氭偄婵傚绂?
            List<Double[]> fallbackVertices = new ArrayList<>(originalVertices);
            fallbackVertices.addAll(externalVertices);
            if (boundaryPoints != null) fallbackVertices.addAll(boundaryPoints);
            return sortVerticesWithoutCrossing(fallbackVertices);
        }

        // 闂備胶鎳撻悘姘跺磿閹惰棄鏄ョ€光偓閸曨偆顔岄梺鍦劋閸ㄩ潧顕ｉ幎鑺ョ厽闁归偊鍠楅鐔虹磼閸欐ê宓嗛柡灞芥噹椤繈顢楅崒姘卞幀闂?startBoundaryIndex 濠电偞鍨堕弻銊╊敄閸涙潙绠栨俊銈呮噹缁犵敻鏌熼悜妯虹仴鐎殿喗濞婇弻銊モ槈濡警娈紓浣藉皺閸嬫挻绌辨繝鍥х＜婵炴垶鐭崙?start 闂佽崵鍋為崙褰掑磻閸曨垰鍨傜憸鐗堝笚閳锋棃鏌曢崼婵堢缂佲偓婢舵劖鐓曟慨姗嗗幖閻忋儱顭胯婵炩偓妤犵偛绉归獮鍡氼槻闁绘挸鍊块弻锟犲醇濮橆兛澹曢梺鍝勵槴閺呮粓宕瑰畷鍥ㄥ床婵鍩栭崑鈺傜箾閸℃ê鐏﹂柣锝呭船椤啴濡堕崨顓ф殺闂佺顑戠徊楣冨箯閸涱収鐓ラ柍褜鍓涢幏褰掓偄閻撳海顔岄梺鍦劋閸ㄩ潧顕ｉ幎鑺ョ厽?
        int insertAfter = startBoundaryIndex;
        int safeCounter = 0;
        while (safeCounter < n && !keep[insertAfter]) {
            insertAfter = (insertAfter - 1 + n) % n;
            safeCounter++;
        }

        List<Double[]> finalVertices = new ArrayList<>();
        boolean inserted = false;

        // 濠电偛顕慨浼村磿鏉堚晜鏆滄い鎰剁稻閸欏繘鎮楅敐搴℃灁闁逞屽墮鐎涒晠骞嗛弮鍥╃杸婵炴垶顨嗛崰姘舵⒑閸涘⊕顏勎涚€靛憡顫曟繝闈涙川閳瑰秵绻濋棃娑氬婵炲牆鐭傞弻锟犲醇椤愨剝鎹ｅ銈庡亾缁犳挸鐣峰鑸靛亹缂備焦顭囪ぐ鎴︽⒑濮瑰洤濡奸悗姘煎櫍瀹曟垿鏁愭径瀣珫闁诲繒鍋涢埀顒傚枎閻濐垶姊?insertAfter 闂備礁鎲￠懝鍓р偓娑掓櫈閵囨劙宕橀鑲╊槯闂侀潧顦崝搴ㄦ偟閸洘鐓熼柨婵嗘噽閻忛亶鏌涚€ｅ墎绋荤紒瀣槹閹棃濮€閿涘嫬甯庡┑锛勫亼閸婃洘鏅舵惔銊ョ；?
        for (int i = 0; i < n; i++) {
            if (keep[i]) {
                finalVertices.add(originalVertices.get(i));
            }
            if (!inserted && i == insertAfter) {
                // 闂備礁婀辩划顖滄暜閹烘鐭楅柛鈩兩戠紞鍥煕閿旇骞楁繛鍛礋閺岋綁骞囬幍鍐蹭壕婵炴垶姘ㄩ幉鍧楁⒑閸濆嫬鈧兘宕￠幎钘壩ュ┑鐘叉处閸婄兘鏌ｈ閹芥粎绮堥崟顐勫酣宕堕妸褏鐣洪柣搴＄仛濮婂綊濡甸崟顖氫紶闁告洦鍘鹃。鏌ユ⒑閹稿海鈯曟い鎺旀崐artBoundary -> newVertices -> endBoundary闂?
                finalVertices.add(startBoundaryPoint);
                finalVertices.addAll(orderedNewVertices);
                finalVertices.add(endBoundaryPoint);
                inserted = true;
            }
        }

        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐏炶偐鍓ㄥ┑顔斤供閸樿棄顕ｉ幘缁樼厵缂備焦锚缁楁帡鏌涘▎蹇旑棦闁轰礁绉瑰畷濂告偄鐞涒€充壕妞ゆ帒鍊婚崡姘亜閺嶃劎鈽夐柛鎾插嵆閺屾盯骞囬鍛缂備浇椴稿畝鎼佸极瀹ュ懐鏆嗛柛鎰ㄦ櫅椤忓爼姊洪崨濠冣拻濠殿喗鎸冲畷鍝勭暆閸曨偄鐝樺銈嗗姂閸ㄥ宕?
        if (!inserted) {
            finalVertices.add(startBoundaryPoint);
            finalVertices.addAll(orderedNewVertices);
            finalVertices.add(endBoundaryPoint);
        }

        // 闂備礁鎼悧鍐磻閹剧粯鐓曟慨姗嗗墯閸炲绱掔紒姗嗙劷闁诡喗澹嗘禒锕傛寠婢跺矈鍞介梻鍌欑劍瑜板啰绮斿畷鍥╃當鐎光偓閸曨兘鎸€闂傚鍋掗崢浠嬪船婢舵劖鐓曟繛鍡楁禋濡插綊鎮介娑欏鞍闁?
        return sortVerticesWithoutCrossing(finalVertices);
    }
    
    /**
     * 闂備胶鎳撻悘姘跺磿閹惰棄鏄ラ悘鐐靛亾缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊箛椤旇棄娈岀紓浣靛姀鐏忔瑩骞忛悩璇参ㄩ柨鏇楀亾缂佹彃顭烽弻娑滅疀鐎ｎ亜濮㈠┑鐐茬墛閸ㄥ爼骞忛崨顓у悑闁搞儮鏅滈悗顓㈡⒑閹稿海鈽夐柣妤€妫楅敃銏ゎ敂閸繂鍋嶉梺缁樻椤ユ捇宕㈤悽鍛婄厱婵﹩鍓涜倴濠电偛鐗婇崹鍨暦椤愶富鏁傞柛鎰靛幐閹封剝绻涢幋鐐存儎濞存粠浜俊瀛樼節閸ャ劌鈧兘姊洪锝囥€掓い鎾存そ濮婂宕掑┑鍥ф锭缂?
     * 闂佸搫顦弲婊堝蓟閵娿儍娲冀閵婏妇绉堕梺闈╁瘜閸樿棄鈻嶉弴銏＄厽闁归偊鍨煎鎰熆瑜嶇粔鐟扮暦閻戣棄惟闁靛ě鍛獎闂備焦鐪归崝宀€鈧凹鍨堕獮鎴︽晲閸℃ê寮块柣搴秵閸犳捇鍩€椤掆偓閿曨亪骞冨▎鎾虫嵍妞ゆ挾鍋涙禍鍫曟倵?
     */
    private int findBoundaryPointInsertIndex(Double[] boundaryPoint, List<Double[]> originalVertices) {
        double minDistance = Double.MAX_VALUE;
        int edgeIndex = -1;
        
        for (int i = 0; i < originalVertices.size(); i++) {
            int j = (i + 1) % originalVertices.size();
            Double[] p1 = originalVertices.get(i);
            Double[] p2 = originalVertices.get(j);
            
            // 闂佽崵濮崇欢銈囨閺囥垺鍋╃紓浣股戠紞鍥煕閿旇骞楁繛鍛礋閺岋綁骞囬钘夋畬闂佺硶鏅涢惉濂告儉椤忓牆绠伴幖鎼枛閺傗偓闂備焦鐪归崝宀€鈧凹鍨崇划锝呪槈濮樿京鐒?
            Double[] closest = getClosestPointOnSegment(boundaryPoint, p1, p2);
            double distance = calculateDistance(boundaryPoint, closest);
            
            if (distance < minDistance) {
                minDistance = distance;
                edgeIndex = i;
            }
        }
        
        return edgeIndex >= 0 ? edgeIndex : 0;
    }
    
    /**
     * 闂備礁鎲＄敮鍥磹閺嶎厼钃熼柛銉墮濡ê霉閸忚偐鏆橀柍褜鍓欓敃顏堝箖濞嗘挸鎹舵い鎾跺枔閺嗙娀姊洪崷顓犲笡閻㈩垱甯″畷锝夊幢濡晲绨婚梺鍛婄箓鐎氼喚鍠婂澶嬬叆婵炴垶顭囨晶娑欍亜閹烘挾鐭掔€规洏鍎甸獮瀣倷閼碱兛绮ｉ梻浣告啞閻熴儳绮旈幘顔肩畺婵犲﹤鐗婇弲?
     */
    private List<Double[]> determineNewVerticesOrder(List<Double[]> newVertices, Double[] boundaryStart, Double[] boundaryEnd) {
        if (newVertices == null || newVertices.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б濡炪倖鍨靛ú锔剧矙婢舵劕鐒垫い鎺嶈兌閳绘梹銇勮箛鎾跺闁逞屽墮閿曨亪骞冨▎鎾虫嵍妞ゆ挆鍛亾閺屻儲鐓曟繝鍨姃缁ㄧ兘姊洪崣澶岀煁婵炵厧绻樺畷鐑筋敇閻斿摜褰ч梻浣虹帛閸旀﹢锝炴径濞掓椽骞愭惔顫唉?
        Double[] firstVertex = newVertices.get(0);
        double distToStart = calculateDistance(firstVertex, boundaryStart);
        double distToEnd = calculateDistance(firstVertex, boundaryEnd);
        
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞瀹€鈧粻鏃€銇勯幋锝嗙《妞ゅ繐宕埥澶愬箼閸愌囧仐闂侀潧妫楅敃顏堝箖濞嗘挸鎹舵い鎾楀應鍋撻弻銉﹀仯鐟滃秹宕查弻銉稏闁归偊鍘界紞鍥煕閿旇骞楁繛鍛礋閺岋綁骞囬锝嗙秷濠电偛寮剁€笛冾焽婵犳艾绠ｆい鏍ㄧ矌椤︻喗绻涢敐鍛缂佽鍟伴埀顒€鐏氶弻銊╋綖閵忋倖鏅查柛娑卞枛閳?
        if (distToStart < distToEnd) {
            return new ArrayList<>(newVertices);
        } else {
            // 闂備礁鎲￠悢顒傜不閹达箑鍨傛い鏍仜閻鈧箍鍎遍幊蹇涘磹?
            List<Double[]> reversed = new ArrayList<>();
            for (int i = newVertices.size() - 1; i >= 0; i--) {
                reversed.add(newVertices.get(i));
            }
            return reversed;
        }
    }
    
    /**
     * 闂備礁婀遍…鍫ニ囬悽绋跨劦妞ゆ巻鍋撴俊顐ｇ懃椤啴宕掗悙瀵稿弮闂佸壊鍋嗛崰鎰版偂濞嗘挻鐓曟慨姗嗗幖閻忥箓鎮介娑欏鞍闁诡垱妫佺粻娑樷槈閺嶃倕浜惧┑鐘叉处閸?
     */
    private List<Double[]> sortVerticesCounterClockwise(List<Double[]> vertices) {
        if (vertices.size() < 3) return vertices;
        
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閻撳倻鈧箍鍎遍幊鎰繆?
        double centerX = 0, centerZ = 0;
        for (Double[] vertex : vertices) {
            centerX += vertex[0];
            centerZ += vertex[1];
        }
        centerX /= vertices.size();
        centerZ /= vertices.size();
        
        final double finalCenterX = centerX;
        final double finalCenterZ = centerZ;
        
        // 闂備礁婀遍…鍫ニ囨导瀛樺€垫い鎺戝€归崰鍡涙煙閻戞ɑ灏紒妤佹崌楠?
        List<Double[]> sorted = new ArrayList<>(vertices);
        sorted.sort((v1, v2) -> {
            double angle1 = Math.atan2(v1[1] - finalCenterZ, v1[0] - finalCenterX);
            double angle2 = Math.atan2(v2[1] - finalCenterZ, v2[0] - finalCenterX);
            return Double.compare(angle1, angle2);
        });
        
        return sorted;
    }
    
    /**
     * 闂佽绨肩徊濠氾綖婢跺娅犵€广儱顦€氬顭块懜闈涘闁逞屽墮閿曨亪骞冨▎鎺嬩汗闁圭儤鍨堕鐔兼⒑閸濆嫬鈧粯鏅跺Δ鍐╂殰闁规儳鐡ㄩ崕鐔肩叓閸ャ儱鍔ょ紒鈧径鎰拻闁告劑鍔岀痪褎銇勯幒鎾躲€掗柣銉邯楠炴劖鎯旈鍏兼澑濠电偛鐡ㄩ崵搴ㄥ磹閺囩儐鍤?
     * 闂備礁婀遍…鍫ニ囬柆宥呯煑閹兼番鍔岀粻鐢告煙闁箑鏋斿ù鐘崇洴閹綊宕惰閳诲瞼绱掓潏鈺侇暭濞ｅ洤锕畷鎺戔槈濮橆偄鍤遍梻浣告惈閸婄粯鏅跺Δ鍛敜濠电姴娲ょ粈澶愭煟濡绲荤€殿喗濞婇弻锝夊Ω閵夈儺浠奸柣蹇撴禋閸ㄨ泛鐣峰ú顏呭亜闁绘垶顭囬弳鐘崇箾閹寸偞灏ㄩ柛瀣尵缁辨帡鎮▎蹇斿偍闁逞屽墮閿曨亪骞冨▎鎾寸劵婵炴垶姘ㄩ幉鍧楁⒑閸涘鐒介柛鐘冲姇閿曘垺绗熼埀顒€鐣烽悩璇蹭紶闁告洦鍘奸獮宥夋⒑濮瑰洤濡奸悗姘嵆婵″瓨绻濋崶銊モ偓鐑芥偡濞嗗繐顏╂い锝嗙叀瀵爼鍩￠崘銊ゆ勃闁汇埄鍨遍幐鎶界嵁鐎ｎ喖绠涙い鏃傛櫕閺嗙姷绱撴担鍝勪壕閻庣瑳鍥舵晩鐎光偓閸曨偄鐝樻繝銏ｆ硾椤︿即宕径鎰厱婵炲棗娴氬Σ鎼佹煕椤垵鐏ｉ柟椋庡У閹峰懐鎲撮崟鍨稑闂備礁鎼崐缁樻櫠濡ゅ懎閿ゅ┑鐘叉搐缁€澶愭煟濡绲荤€殿喗濞婇弻锝夊Ω閵夈儺浠奸柣蹇撴禋閸ㄨ泛鐣峰ú顏呭亜闁绘垶顭囬弳鐘充繆閻愵亜鈧洘鏅舵惔銊ョ；闁挎繂顦伴崕宥夋煕閺囥劌鐏涢柍褜鍓欑€涒晠骞嗛弮鍥╃杸闁规儳鍟块崬銊︾箾?
     * 闂傚倸鍊稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄦ皑缁愭棃鏌涢敐鍡樺€愮€殿噮鍓熼幃褔宕煎┑鍫㈡噰闂備礁鎲＄敮妤呫€冮崱娑樿摕濠电姴娲ょ粈澶愭煟濡绲荤€殿喗濞婇弻锝夊Ω閵夈儺浠奸柣蹇撴禋閸ㄨ泛鐣峰ú顏呭亜闁绘垶顭囬弳鐘崇箾閹寸偞灏ㄩ柛瀣尵缁辨帡鎮▎蹇斿偍闁逞屽墮閿曨亪骞冨▎鎾村剬闁告縿鍎抽ˇ顕€姊洪崨濠傚缂佸顥撻幑銏犖熼崗鐓庝粧闁诲繒鍋為崕宕囧緤婵犳碍鐓?
     */
    private List<Double[]> sortVerticesWithoutCrossing(List<Double[]> vertices) {
        if (vertices.size() < 3) return vertices;
        
        // 闂備胶顭堢换鎰版偋婵犲嫧鍋撻崹顐ょ煉闁哄苯鎳樺畷鍗炍熷ú缁樞濋梻鍌欐祰濡嫰鎮ф繝鍕殰闁规儳鐡ㄩ崕?
        List<Double[]> sorted = sortVerticesCounterClockwise(vertices);
        
        // 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壈闂佽壈宕甸崰鎰矚闁稁鏁婄痪鎷岄哺浜涘┑鐐茬摠閸ゅ酣宕愰弴鐑嗗殨?
        int maxIterations = 20; // 闂備礁鎼悧鍐磻閹炬剚鐔嗛柤绋垮悁娴溿垽鏌涢幋顓炵仭缂?0婵?
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            boolean hasCrossing = false;
            int crossingI = -1, crossingJ = -1;
            
            // 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壉婵犫拃鍐х€殿喖鐏氬鍕沪閼恒儳鏋冩繝鐢靛仜閻楁挸顭囪椤㈡岸顢氶埀顒€顕ｆ导鎼晬婵﹩鍘奸崜銊╂⒑閸濆嫮澧曟い锔诲枛椤繈鏁冮崒姘辩厬?
            for (int i = 0; i < sorted.size(); i++) {
                int nextI = (i + 1) % sorted.size();
                Double[] p1 = sorted.get(i);
                Double[] p2 = sorted.get(nextI);
                
                for (int j = i + 2; j < sorted.size(); j++) {
                    int nextJ = (j + 1) % sorted.size();
                    // 闂佽崵濮撮幖顐︽偪閸モ晜宕查柛鎰靛枟閸庡酣鏌熼梻瀵割槮濞寸姵鎮傚鍫曞醇閵忊€虫畬闂佺娓归崡鍐差潖婵犳凹鏁囬柣鎰綑閳ь兙鍊濆?
                    if (nextI == j || nextJ == i) continue;
                    if (i == 0 && nextJ == 0) continue;
                    if (nextI == 0 && j == sorted.size() - 1) continue;
                    
                    Double[] p3 = sorted.get(j);
                    Double[] p4 = sorted.get(nextJ);
                    
                    // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闂佹悶鍊曟鎼佸煝閹捐鍨傛い鎰跺強閿曞倹鐓曟慨妯哄⒔缁辩増鎱ㄥ鍫㈢暤鐎规洩绲惧鍕槈濮橀硸妲卞┑鐐村灦閹稿摜绮旂€电硶鍋撻崹顐€挎鐐茬箻椤㈡宕掗妶鍕＝闂備胶绮崝妤€鈻嶉姀銏☆潟?
                    if (doSegmentsCross(p1, p2, p3, p4)) {
                        hasCrossing = true;
                        crossingI = i;
                        crossingJ = j;
                        break;
                    }
                }
                if (hasCrossing) break;
            }
            
            // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞婵炵偓鐓㈤梺鏂ユ櫅閸燁垳绮婚幒鎳ㄧ懓鈹冮崹顔瑰亾閺囩儐鍤曢柛顐ｆ礃閺咁剟鎮橀悙鏉戝姢缂佹鎹囬獮鏍级閹寸姷顔囬梺浼欑秵閸撶喖鐛?
            if (!hasCrossing) {
                break;
            }
            
            // 濠电儑绲藉ù鍌炲窗閺嶎厔鍥矗婢跺矈娲搁梺闈涚墕閹冲海鐥閺屻劌鈽夊▎鎺戭棟閻庢鍠涙慨銈咁焽椤忓牜鏁勯柟绋块閺€顓㈡⒑閸涘﹦鎳冩い锔垮嵆椤㈡瑧浠﹂崜褉鏋欓柣搴秵閸犳鏁嶉悢鍏肩厽闁靛鍎遍顓㈡煏閸℃韬柟顔界懆閵囨劙骞掔€Ｑ冧壕閹兼番鍨洪崕?
            if (crossingI >= 0 && crossingJ >= 0) {
                // 闂備礁鎲￠悷銉х矓瀹勬噴褰掑幢濡⒈娴?crossingI+1 闂?crossingJ 濠电偞鍨堕弻銊╊敄婢跺á娑㈠锤濡や礁鍓梺鍛婃处閸剟鍩€椤掆偓閿曨亪骞?
                int start = (crossingI + 1) % sorted.size();
                int end = crossingJ;
                
                if (start < end) {
                    // 婵犳鍠楃换鎰緤娴犲鍋夐柛顐ｆ礀缁犳岸鏌涘☉鍗炲箹闁哄鐭傞弻銊モ槈濞嗘帒顥濋悗娈垮枦婵倕顭?[start, end] 闂備礁鎲￠悧妤€顪冮挊澹?
                    reverseSegment(sorted, start, end);
                } else {
                    // 闂佽崵濮垫禍浠嬪礉韫囨洜鐭撻柣鎴炆戠紞鍥煕閿旇骞楁繛鍛礋閺屻劌鈽夊▎鎺戭棟閻庢鍠涙慨銈咁焽椤忓牜鏁勯柤鎰佸灙閹封€斥攽?
                    // 闂備礁鎲￠悷銉х矓瀹勬噴?[start, size-1] 闂?[0, end]
                    reverseSegment(sorted, start, sorted.size() - 1);
                    reverseSegment(sorted, 0, end);
                }
            }
        }
        
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐎ｎ剦娴勯悗骞垮劚濞层倖鎱ㄨ閺岋繝宕煎┑鎰ュ┑顔斤公缁犳挸鐣烽敐鍡楃窞婵炴垶姘ㄩˇ顔界箾鏉堝墽绉繛澶嬫礋瀵偊濡舵径濠冩К闁哄鍋炴竟鍡涙嚌妤ｅ啯鐓曢柟瀵稿閻掗箖鎮楅崹顐€块柟顖氬暣瀹曠喖顢楅埀顒傜箔閹捐绠归柡澶嬪灩缁犳娊鏌ｉ敐鍫殭闁崇粯妫冨鏉戭潩鏉堚斁鏋欏┑鐐村灦閹搁箖宕曢妶鍛偓鎺楀醇閺囩偠袝闁瑰吋鐣崹褰捖锋担鍦?
        if (detectCrossings(sorted)) {
            sorted = sortVerticesByConvexHull(vertices);
        }
        
        return sorted;
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑闁荤偞宀告禍璺侯嚕閸洘鏅柛鏇ㄥ亝閻ゅ洤鈹戦悙鑼闁诲繑鑹惧嵄闁归棿绀佺憴锕傛煥閺冣偓鐎笛囧船婢舵劖鐓曟繛鍡楁禋濡狙呯磼鏉堛劎鎳勭紒瀣槸椤撳ジ宕ㄩ鐘斥枆闂備胶鎳撻〃搴ㄥ礈濞戙埄鏁傛い鎺戝閸婄兘鏌ｈ閹芥粎绮?
     */
    private boolean doSegmentsCross(Double[] p1, Double[] p2, Double[] p3, Double[] p4) {
        // 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡舵径瀣虎闂佺粯顨呴悧濠勭不閹烘鐓熼柕濞垮劚閻掔ざtLineIntersection闂備礁鎼崐浠嬶綖婢跺本鍏滈悹杞扮秿濞戙垹鐒垫い鎺嗗亾闂囧鎮楅敐鍌涙珕闁哥偛顦甸弻?
        Double[] intersection = getLineIntersection(p1, p2, p3, p4);
        if (intersection == null) {
            return false;
        }
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠殿喗锕粻鎾诲箖濞嗘垶鍠嗛柛鏇ㄥ劮閿曞倹鐓曟慨妯哄⒔閻ｈ鲸绻濋埀顒勵敂閸℃瑦锛忛梺鍦焾鐎垫帡宕禒瀣厱闁归偊鍓氶崵鍥煕鎼淬垺灏﹂柡浣哥Ч瀹曠厧顭ㄩ崨顖涘枓闂備礁鎲￠悧鏇㈠箠韫囨洍鍋撳鐓庡箻缂侇喒鏅犻、娑橆煥閸愌冨闂?
        double eps = 1e-6;
        boolean onSegment1 = isPointOnSegmentStrict(intersection, p1, p2, eps);
        boolean onSegment2 = isPointOnSegmentStrict(intersection, p3, p4, eps);
        
        return onSegment1 && onSegment2;
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闂佺顑戠紞浣割嚕娴兼惌鏁嶆慨姗嗗幖閸撱劑姊洪棃鈺勭闁告柨閰ｅ畷鍨償閳锯偓閺嬫棃姊婚崼鐔衡槈闁轰礁鐖煎娲敋閸涱垰甯ㄧ紓浣介哺閻燂妇绮欐径濠庡悑闁告侗鍘惧Ο渚€姊洪悷閭︽疇闁告挻鐩、鏃堫敆閸曨剙鈧兘鏌ｈ閹芥粎绮?
     */
    private boolean isPointOnSegmentStrict(Double[] point, Double[] segStart, Double[] segEnd, double epsilon) {
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闂佺顑戠紞浣割嚕娴兼惌鏁嶆慨姗嗗幖閸撱劑姊洪棃鈺勭闁告柨閰ｅ畷鍨償閳锯偓閺嬫棃鏌涢…鎴濅簻妞わ絾鐓￠弻娑㈡晜閸濆嫬顬嬪┑鐐茬墛閸ㄥ潡骞嗛崟顓涘亾閻㈡鐒炬繛鍫㈠Т閳?
        double dx = segEnd[0] - segStart[0];
        double dy = segEnd[1] - segStart[1];
        double dx1 = point[0] - segStart[0];
        double dy1 = point[1] - segStart[1];
        
        // 闂備礁鎲￠悷銉┧囨潏顐熷従婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑濠碘€冲级閹倸鐣烽妷鈺傛櫇闁逞屽墴瀹曪綁鏁嶉崟顓燂紡?
        double cross = dx * dy1 - dy * dx1;
        if (Math.abs(cross) > epsilon) {
            return false; // 濠电偞鍨堕幐鍝ョ矓閹绢喖鐭楅柨娑樺濡?
        }
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闂佺顑戠紞浣割嚕娴兼惌鏁嶆慨姗嗗幖閸撱劑姊洪棃鈺勭闁告柨閰ｅ畷鍨償閳锯偓閺嬫棃姊婚崼鐔衡槈闁轰礁鐖煎娲敋閸涱垰甯ㄧ紓浣介哺閻燂妇绮欐径濠庡悑闁告侗鍘惧Ο渚€姊洪悷閭︽疇闁告挻鐩、鏃堫敆閸曨剙鈧兘鏌ｈ閹芥粎绮?
        double dot = dx1 * dx + dy1 * dy;
        double lenSq = dx * dx + dy * dy;
        
        if (lenSq < epsilon) {
            // 缂傚倷鑳堕崑鎾垛偓绗涘浂鏁婄€光偓閸曨兘鎷归梺鍓茬厛閸犳牠顢欐繝鍌楁?闂備焦瀵х粙鎴﹀嫉椤掑媻澶愬川閺夋垼鎽曢梺闈涳紡閸愌冨闂備礁鎼€氱兘宕规导鏉戠畾濞达絽婀遍埢鏃堝箹濞ｎ剙鈧牕鐣烽幎鑺ョ厽闁归偊鍓涚粔娲煕閿濆棙鍊愮€?
            return false; // 缂傚倷鐒﹀Λ蹇涘垂閹惰棄纾婚柨婵嗘閳绘梻鈧箍鍎卞ú銊╁汲椤掆偓闇夋繝褍鐏濋埀顒佹礀椤?
        }
        
        double t = dot / lenSq;
        // t闂傚鍋勫ú銈夊箠濮椻偓婵＄绠涘☉妯诲祶?0, 1)闂備浇鍋愰崢褔宕鈷氭椽宕稿Δ鈧粈鍐煕濠婂啫鏆熺紒鈧径濞炬闁规儳纾瓭闁诲骸鐏氶敃銏ょ嵁韫囨侗鏁嗛柛灞诲€撶槐鎾绘⒑?
        return t > epsilon && t < 1.0 - epsilon;
    }
    
    
    /**
     * 闂備礁鎲￠悷銉х矓瀹勬噴褰掑幢濞戞顦┑掳鍊曠€氥劑鍩€椤掆偓閸燁垳绮欐径鎰垫晣闁绘棃顥撳Ο鍕煟鎼淬値娼愰柤鐟板⒔娴滄悂顢涢悙绮规嫽闁诲酣娼ч幉锟犳偩闁秵鐓曢煫鍥ㄦ尭閺嗙喓绱掗埀?
     */
    private void reverseSegment(List<Double[]> list, int start, int end) {
        while (start < end) {
            Double[] temp = list.get(start);
            list.set(start, list.get(end));
            list.set(end, temp);
            start++;
            end--;
        }
    }
    
    /**
     * 闂備胶纭堕弲鐐差浖閵娧嗗С妞ゆ帒瀚粈鍕磼鐎ｎ亞浠㈤柣锕€鐖奸弻锝夊Ω閵夈儺浠鹃柣銏╁灡閹稿骞嗛弮鍥╃杸闁哄洨濮靛В鍫濃攽閻愬樊妲规繛娴嬫櫇濡叉劕鈹戦崼鐔峰幑濡炪倖妫佸畷鐢电不閼恒儳绠鹃悘鐐殿焾婢у弶绻濋埀顒佹媴閸︻収娲搁梺闈涚墕閹冲海鐥?
     */
    private List<Double[]> sortVerticesByConvexHull(List<Double[]> vertices) {
        if (vertices.size() < 3) return vertices;
        
        // 闂備胶鎳撻悘姘跺磿閹惰棄鏄ョ€光偓閸曨偄鐝橀梺閫炲苯澧紒瀣槺閳ь剨缍嗛崑鍛存偂濞嗘挻鐓熼柕濞垮劚椤忣偊鏌涚€ｅ墎绉柡浣哥Ч瀹曠厧鈹戦幇顓熸倷闂備礁鎼鍡涙嚇椤栫偞鐓熼柍鍝勫枤閻掗箖鏌熼鑺ュ磳闁轰礁绉舵禒锕傛寠婢跺本鍋ч梻浣告惈閻楀啴宕戦幘鍨涘亾瑜版帗浜ょ紒鐘冲笧缁晠鎮㈤崗鐓庡壄闂佸憡娲栨晶搴ｇ矆?
        int bottomIndex = 0;
        for (int i = 1; i < vertices.size(); i++) {
            Double[] current = vertices.get(i);
            Double[] bottom = vertices.get(bottomIndex);
            if (current[1] < bottom[1] || (current[1] == bottom[1] && current[0] < bottom[0])) {
                bottomIndex = i;
            }
        }
        
        // 濠电偛顕慨浼村磿閹绘帇鈧帡鎳滈悽纰樻灃闁诲函缍嗛崑鍛存偂濞嗘挻鐓熼柕濞垮劚椤忣偊鏌涚€ｅ墎绋荤紒瀣槹閵堬箓骞愭惔锟犳７闂備胶绮崝妤€鈻嶉姀銏☆潟婵犻潧顑呯粻鏉款熆鐠轰警鍎戦柍褜鍓﹂崜娑㈠箟閹绢喖绠抽柟鎯х－閻熲晠鏌?
        Double[] bottomPoint = vertices.get(bottomIndex);
        List<Double[]> sorted = new ArrayList<>(vertices);
        
        sorted.sort((v1, v2) -> {
            // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閸庡海绱掔€ｎ偒鍎ラ柛銈囧Т闇夋繝濠傚暟閳瑰》ttomPoint闂備焦鐪归崝宀€鈧凹鍙冮幃妤咁敆閸屾稑鏋?
            double angle1 = Math.atan2(v1[1] - bottomPoint[1], v1[0] - bottomPoint[0]);
            double angle2 = Math.atan2(v2[1] - bottomPoint[1], v2[0] - bottomPoint[0]);
            
            // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐏炴儳鏋傞梺鍦劋閸ㄧ敻顢欐繝鍥ㄧ厽闁冲搫鍠氶悞楣冩煙椤旇姤宕岄柡浣哥Ф娴狅箓骞嗚濡棝鏌ｉ悩闈涘妺缂佽瀚伴垾鏍炊椤掆偓缁犳娊鏌熼悜妯虹仼缁?
            if (Math.abs(angle1 - angle2) < 1e-10) {
                double dist1 = calculateDistance(v1, bottomPoint);
                double dist2 = calculateDistance(v2, bottomPoint);
                return Double.compare(dist1, dist2);
            }
            
            return Double.compare(angle1, angle2);
        });
        
        return sorted;
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壆濠殿喗锕粻鎾崇暦?
     */
    private boolean detectCrossings(List<Double[]> vertices) {
        // 缂傚倷鑳舵慨顓㈠磻閹剧粯鐓曢柡宥冨妿婢ь亪鏌ｉ妶鍛伃闁诡喖纾弫顕€顢欓崜褏宕舵繝纰樻閸嬧偓闁稿鎸荤换娑㈠级閹搭厼鍓伴梺缁樻煛閸嬫捇姊洪崫鍕⒈闁告挻绋戦埢鎾诲箣閿旇棄娈滃銈呯箰濡瑧绮婇幓鎹?
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 2; j < vertices.size(); j++) {
                if (i == 0 && j == vertices.size() - 1) continue; // 闂佽崵濮撮幖顐︽偪閸モ晜宕查柛鎰靛枟閸庡酣鏌熼梻瀵割槮濞寸姵鎮傚?
                
                int nextI = (i + 1) % vertices.size();
                int nextJ = (j + 1) % vertices.size();
                
                Double[] intersection = getLineIntersection(
                    vertices.get(i), vertices.get(nextI),
                    vertices.get(j), vertices.get(nextJ)
                );
                
                if (intersection != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壉闂佸搫鑻敃銉╁Φ閸曨垰绀堝ù锝夋櫜閸栨牗绻涢幋鐐村碍濡ょ姴鎽滈弫顕€顢曢敐鍐х盎闂佸憡鎸风粈浣圭椤栨埃妲堥柡鍌涱儥閸庢梹淇婇銏″仴闁诡垰鍟村畷鐔碱敂閸涱厽鏁梻?
     * 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剚顥濋梺缁橆殔閻楀棗顭囬幋锔界厱闁哄啫鍊稿瓭闂佸憡鐟ュΛ婵嬪箚閸愵喖绀嬫い鎰╁灲濞堫剟姊虹化鏇熸珖闁哥姵鎸搁埢鎾诲箣閻愮鏋栭梺閫炲苯澧伴柣銉海椤﹀綊鏌曢崱妤嬭含闁诡喗鐟╅弻鍛槈濮樿鲸鍠涢梻浣告啞椤洭宕伴幇顒婅€垮〒姘ｅ亾鐎规洜濞€瀹曨偊宕熼鐐茬闂備焦鐪归崝宀€鈧矮鍗虫俊瀛樼節閸ャ劌鈧兘鎮峰▎蹇擃仼妞わ絾鐓″鍫曞煛閸愩劋娌柣銏╁灡閹告娊鐛€ｎ喖绠涙い鏃傛櫕閺嗙姷绱撴担鍝勪壕閻庣瑳鍥舵晩鐎光偓閸曨偄鐝樻繝銏ｆ硾椤︿即宕径鎰厱?
     * 闂備礁鎲＄敮妤呮偡閵娾晜鍎婂鑸靛姇閸欏﹪骞栧ǎ顒€鐏柣鎾亾婵犵數鍎戠紞鈧い鏇嗗嫭鍙忛柣鎰惈缁€鍌炴煏婢诡垰瀚弳鐘绘⒑閸涘﹦澧柟纰卞亰閹€斥枎閹炬潙鍓梺鍛婃处閸剟鍩€椤掆偓閿曨亪骞冨▎鎾虫嵍妞ゆ挾鍠撻弳鐘充繆閻愵亜鈧洟骞栭銈堝С妞ゆ挶鍨归惌妤冣偓骞垮劚鐎氼噣宕?
     */
    private boolean hasCrossingBetweenNewAndOriginal(List<Double[]> originalVertices, List<Double[]> newVertices, 
                                                      Double[] startBoundary, Double[] endBoundary) {
        if (newVertices == null || newVertices.size() < 2) {
            return false;
        }
        
        // 婵犵妲呴崑鈧柛瀣尰缁绘盯寮堕幋顓炲壉闂佸搫鑻敃銉╁Φ閸曨垰绀堝ù锝夋櫜閸栨牗绻涢幋鐐寸叆妞ゆ垵顦灋闁秆勵殕閸庡秹鏌涢弴銊ュ箹婵炲牏濮电换娑㈠醇濠靛棙鍊庣紓浣诡殔閻倸鐣烽姀銈嗗亜缂佹銆€閸嬫挻绻濋崶銊モ偓鐑芥⒑椤愶絿銆掓い鎾存そ濮婂宕掑鐓庢濠电偛鐗婇崹褰掓儉椤忓牆绠伴幖鎼枛閺傗偓闂備礁鎼€氱兘宕规导鏉戠畾濞撴埃鍋撶€殿喖鐏氬鍕緞鐎ｎ亝鏁梻?
        for (int i = 0; i < newVertices.size(); i++) {
            int nextNewIndex = (i + 1) % newVertices.size();
            Double[] newStart = newVertices.get(i);
            Double[] newEnd = newVertices.get(nextNewIndex);
            
            // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Г缂備胶绮悧鐘差嚕閸洖鐒洪柛鎰ㄦ櫇閸樻劖淇婇悙顏勨偓鏇熸櫠鎼淬劌纾婚柨婵嗘川濡垶鏌熺粙鍨槰闁哥偘绮欓弻锟犲礋闂堟稓浠╅梺璇″灟閻掞妇绮欐径瀣劅闁炽儴娅曢弸鐐繆閻愵亜鈧洘鏅舵惔銊ョ；闁挎繂娲ㄥΛ顖炴煙缁嬪灝鐦ㄩ柛鐐舵闇夋繝褍鐏濋埀顒佹礀椤?
            for (int j = 0; j < originalVertices.size(); j++) {
                int nextOriginalIndex = (j + 1) % originalVertices.size();
                Double[] originalStart = originalVertices.get(j);
                Double[] originalEnd = originalVertices.get(nextOriginalIndex);
                
                // 闂佽崵濮撮幖顐︽偪閸モ晜宕查柛鎰ㄦ櫆缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊箛椤栵絾缍堟繝鈷€鍐х€规洜鍏樻俊姝岊槼闁伙綁浜跺鍫曞醇閵忊槅浠х紓浣介哺閻熲晠寮婚崱娑樺瀭妞ゆ梻鍘х挧瀣煟閻樺弶绁╅柛銊ゅ嵆瀹曡绂掔€ｎ偅娅?
                if (isPointOnSegment(startBoundary, originalStart, originalEnd) || 
                    isPointOnSegment(endBoundary, originalStart, originalEnd)) {
                    continue;
                }
                
                // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闂佹悶鍊曟鎼佸煝閹捐鍨傛い鎰跺強閿曞倹鐓曟慨妯哄⒔缁辩増鎱ㄥ鍫㈢暤鐎?
                Double[] intersection = getLineIntersection(newStart, newEnd, originalStart, originalEnd);
                if (intersection != null) {
                    return true; // 闂備礁鎲￠悷锕傚垂瑜版帞宓侀柛銉ｅ妿椤╂煡鏌曢崼婵囧櫣缂?
                }
            }
        }
        
        return false;
    }
    
    /**
     * 闂備礁鎲＄敮鍥磹閺嶎厼钃熼柛銉墯閸婄兘鎮峰▎蹇擃仾缁楁垿姊洪崨濠勫暡缂佺姵鍨甸敃銏ゎ敂閸℃瑦锛忛梺鍦焾鐎垫帡宕挊澶嗘?
     */
    private boolean isPointOnSegment(Double[] point, Double[] segStart, Double[] segEnd) {
        if (point == null || segStart == null || segEnd == null) {
            return false;
        }
        
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閸婄兘鏌ｉ悢鍛婄凡闁绘挸鍊荤槐鎺楁倷椤掆偓椤曟粍銇勯弬璺ㄧ伇闁圭鍕垫Ч閹肩补鈧彃瀵查梻浣圭湽閸斿瞼鈧凹鍓熼獮鍐煥閸喓鍘掗棅顐㈡处閹歌崵鈧俺灏欑槐鎺楁倷椤掆偓椤曟粍銇勯弬璺ㄧ劯鐎规洏鍎甸獮瀣晝閳ь剙鈻?
        double dx1 = point[0] - segStart[0];
        double dy1 = point[1] - segStart[1];
        double dx2 = segEnd[0] - segStart[0];
        double dy2 = segEnd[1] - segStart[1];
        
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞瀹€鈧Λ顖炴煙缁嬪灝鐦ㄩ柛鐐扮矙濮婂鍩€椤掑嫬鍨傛い鏃傗拡閸炵儤绻?闂備焦瀵х粙鎴﹀嫉椤掑媻澶愬川閺夋垼鎽曢梺闈涳紡閸愌冨闂備礁鎼€氱兘宕规导鏉戠畾濞达絽婀遍埢鏃堝箹濞ｎ剙鈧牕鐣烽幎鑺ョ厽闁归偊鍓涚粔娲煕閿濆棙鍊愮€?
        if (Math.abs(dx2) < 1e-10 && Math.abs(dy2) < 1e-10) {
            return Math.abs(dx1) < 1e-10 && Math.abs(dy1) < 1e-10;
        }
        
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘崇閸婄兘鏌涢敂璇插辅婵犻潧顑呭浠嬫倶閻愯埖顥夌紒鐙欏懐纾?
        double dot = dx1 * dx2 + dy1 * dy2;
        double cross = dx1 * dy2 - dy1 * dx2;
        
        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剛鐓戞繝銏ｆ硾閻鈹戦崶鈹炬灃閻庡箍鍎辩€氼垳绮?闂備焦瀵х粙鎴︽儔婵傜纾婚柨婵嗘閳绘梻鈧箍鍎遍幊蹇涚叕椤掑倻纾奸柣鎰靛墮椤曟粍銇勯弬鎸庢拱缂?
        if (Math.abs(cross) > 1e-10) {
            return false;
        }
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Б闂佺顑戠紞浣割嚕娴兼惌鏁嶆慨姗嗗幖閸撱劑姊洪棃鈺勭闁告柨閰ｅ畷鍨償閳锯偓閺嬫柨霉閿濆懎鏆欓柣锕備憾閺屾盯濡烽敂鐣岀▏闂?
        double t = dot / (dx2 * dx2 + dy2 * dy2);
        return t >= 0 && t <= 1;
    }
    
    /**
     * 闂佽绻愮换鎰板箰缁屾獪ble[]闂備礁鎲＄敮妤呫€冩径鎰ラ柛鎰ㄦ櫆婵粍銇勯幒宥囶槮閹兼潙锕ら埥澶愬箻妫版繂娈榚rtex闂備礁鎲＄敮妤呫€冩径鎰?
     * 闂備胶顫嬮崟顐㈩潔闂佺粯鐗徊鍓р偓鐢靛帶椤繈骞囨担纭呮櫑闂備礁鎲￠悷锕傛偋濡ゅ懎姹查柨婵嗘閳绘棃鏌ｉ幋鐐嗘垵鈻撴繝姘厸?
     */
    private List<AreaData.Vertex> convertToVertexList(List<Double[]> coordinates) {
        List<AreaData.Vertex> vertices = new ArrayList<>();
        
        // 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑Г缂備線顤傞崣鍐ㄧ暦濡ゅ懎唯闁靛鍎查ˉ婵嬫⒑鏉炴媽鍏屽褎顨呭嵄闁归棿绀佺憴锕傛煥閺冨洤鍔电紒鈧刊妾渓l
        if (coordinates == null) {
            System.err.println("ERROR: convertToVertexList received null coordinates");
            return vertices; // 闂佸搫顦弲婊堝蓟閵娿儍娲冀椤愩倗鐓旈梺鍛婄箓鐎氼剟鎮樺▎鎾村仩?
        }
        
        for (Double[] coord : coordinates) {
            if (coord != null && coord.length >= 2) {
                // 闂佽绻愮换鎰涘Δ鈧…鍥旈崨顓炲敤闂佹悶鍎滈崘顏呭仹闂備浇妗ㄩ懗鍓佹崲濠靛牊瀚婚柣鏃傚帶閺嬩線鎮楀☉娅虫垵鈻?
                int x = (int) Math.round(coord[0]);
                int z = (int) Math.round(coord[1]);
                vertices.add(new AreaData.Vertex(x, z));
            }
        }
        return vertices;
    }
    
    // Getter闂備礁鎼崐浠嬶綖婢跺本鍏?
    public AreaData getSelectedArea() { return selectedArea; }
    public List<Double[]> getNewVertices() { return newVertices; }
} 
