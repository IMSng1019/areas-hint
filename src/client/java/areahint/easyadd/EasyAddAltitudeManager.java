package areahint.easyadd;

import areahint.data.AreaData;
import areahint.debug.ClientDebugManager;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * EasyAdd濡ゅ倹锚鐎瑰磭绮婚敍鍕€為柛?
 * 閻犳劗鍠曢惌妤佸緞閸曨厽鍊為柛鈺冨枎閹洘顨囧Ο鍝勵唺闁汇劌瀚崵婊呪偓瑙勭煯缁犵喓鎷嬮崜褏鏋?
 */
public class EasyAddAltitudeManager {
    
    /**
     * 濡ゅ倹锚鐎规娊鏌呮径瀣仴缂侇偉顕ч悗?
     */
    public enum AltitudeType {
        AUTOMATIC,  // 闁煎浜滄慨鈺冩媼閿涘嫮鏆柨娑樼墕鐢偊鏌呴弰蹇曞竼闁?
        CUSTOM,     // 闁煎浜滈悾鐐▕婢舵劗褰幖?
        UNLIMITED   // 濞戞挸绉瑰娲礆閸洜褰幖?
    }
    
    // 濡ゅ倹锚鐎规娊鏌呮径瀣仴闁绘鍩栭埀?
    public enum AltitudeInputState {
        SELECTING_TYPE,     // 闂侇偄顦扮€氥劍顨囧Ο鍝勵唺缂侇偉顕ч悗?
        INPUT_MIN_HEIGHT,   // 閺夊牊鎸搁崣鍡涘嫉閳ь剚鎷呮惔銊у蒋閹?
        INPUT_MAX_HEIGHT    // 閺夊牊鎸搁崣鍡涘嫉閳ь剚顨囧鈧悵顔芥償?
    }
    
    // 鐟滅増鎸告晶鐘虫綇閹惧啿寮抽柣妯垮煐閳?
    private static AltitudeInputState currentInputState = AltitudeInputState.SELECTING_TYPE;
    
    // 闁煎浜滈悾鐐▕婢舵劗褰幖杈剧畱閳?
    private static Double customMinHeight = null;
    private static Double customMaxHeight = null;
    
    // 闂侇偄顦扮€氥劑鎯冮崟顖滃蒋閹艰揪濡囩悮顐﹀垂?
    private static AltitudeType selectedType = AltitudeType.AUTOMATIC;
    
    /**
     * 鐎殿喒鍋撳┑顔碱儔閻濐喗鎯旈敃鍌楀亾婢跺顏ユ繛缈犺兌閳?
     * @param recordedPoints 閻犱焦婢樼紞宥夋儍閸曨偅缍忛柡宥呮川閸?
     */
    public static void startAltitudeSelection(List<BlockPos> recordedPoints) {
        reset();
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 闁哄嫬澧介妵姘殗濡搫顔婇梺顐㈩槹鐎氥劑鎮惧畝鍕〃
        EasyAddUI.showAltitudeSelectionScreen(recordedPoints);
        
        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "Started altitude selection flow");
    }
    
    /**
     * 濠㈣泛瀚幃濠冾殗濡搫顔婄紒顐ヮ嚙閻庣兘鏌呮径瀣仴
     * @param type 闂侇偄顦扮€氥劑鎯冮崟顖滃蒋閹艰揪濡囩悮顐﹀垂?
     */
    public static void handleAltitudeTypeSelection(AltitudeType type) {
        selectedType = type;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (type == AltitudeType.AUTOMATIC) {
            // 闂侇偄顦扮€氥劑鎳涢鍕楅悹渚婄磿閻ｅ鏁嶅畝鈧ú鍧楀箳閵夈劎绠婚柛蹇嬪劙缁楀懏绋夐埀顒€顫?
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.altitude_2")), false);
            EasyAddManager.getInstance().proceedWithAltitudeData(null);
        } else if (type == AltitudeType.UNLIMITED) {
            // 闂侇偄顦扮€氥劍绋夊澶嬵€欓柛鎺戠埣閻濐喗鎯旈敂鑲╃濞达綀娉曢弫顦攗ll闁稿﹨澹堥妴鍐矆閻戞ɑ锟ュΔ鍌浢€规娊姊介幇顒€鐓?
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.altitude")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.coordinate")), false);
            AreaData.AltitudeData unlimitedAltitude = new AreaData.AltitudeData(null, null);
            EasyAddManager.getInstance().proceedWithAltitudeData(unlimitedAltitude);
        } else {
            // 闂侇偄顦扮€氥劑鎳涢鍕毎濞戞柨顧€缁辨繂顕ｉ埀顒佹叏鐎ｎ厾缈婚柛蹇嬪劜缁侊妇绮?
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.altitude_3")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.altitude_5")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.altitude")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.cancel")), false);
            
            currentInputState = AltitudeInputState.INPUT_MIN_HEIGHT;
        }
        
        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "Selected altitude type: " + type);
    }
    
    /**
     * 濠㈣泛瀚幃濠冾殗濡搫顔婇柡浣规緲閳ь剝澹堢欢顓㈠礂?
     * @param input 闁活潿鍔嶉崺娑欐綇閹惧啿寮抽柣銊ュ閻⊙呯箔閿旇儻顩?
     * @return 闁哄嫷鍨伴幆渚€骞嬮幇顒€顫犲璺哄閹﹥娼忛幘鍐插汲
     */
    public static boolean handleAltitudeInput(String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        
        // 婵☆偀鍋撻柡灞诲劚瑜板洤鈽夐崼鐔告儥濞?
        if (input.trim().isEmpty() || input.contains("取消") || input.equalsIgnoreCase("cancel")) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.altitude.cancel_2")), false);
            EasyAddManager.getInstance().cancelEasyAdd();
            return true;
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            
            switch (currentInputState) {
                case INPUT_MIN_HEIGHT:
                    customMinHeight = value;
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_10") + value), false);
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.altitude_4")), false);
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.altitude_2")), false);
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.cancel")), false);
                    
                    currentInputState = AltitudeInputState.INPUT_MAX_HEIGHT;
                    return true;
                    
                case INPUT_MAX_HEIGHT:
                    if (customMinHeight != null && value <= customMinHeight) {
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.altitude") + customMinHeight), false);
                        return true;
                    }
                    
                    customMaxHeight = value;
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_11") + value), false);
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_12") + customMinHeight + " ~ " + customMaxHeight), false);
                    
                    // 闁告帗绋戠紓鎾绘嚊椤忓嫮鏆板☉鏂款樀閻濐喗鎯旈敂鑺ユ闁硅鍠栭懟鐔虹磼瑜忛悽璇裁规担琛℃煠
                    AreaData.AltitudeData customAltitude = new AreaData.AltitudeData(customMaxHeight, customMinHeight);
                    EasyAddManager.getInstance().proceedWithAltitudeData(customAltitude);
                    return true;
                    
                default:
                    return false;
            }
            
        } catch (NumberFormatException e) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.general_12")), false);
            return true;
        }
    }
    
    /**
     * 闁兼儳鍢茶ぐ鍥亹閹惧啿顤呴弶鍫熸尭閸欏棝鎮╅懜纰樺亾?
     */
    public static AltitudeInputState getCurrentInputState() {
        return currentInputState;
    }
    
    /**
     * 闁兼儳鍢茶ぐ鍥焻婢跺顏ラ柣銊ュ閻濐喗鎯旈敂鎹愵潶闁?
     */
    public static AltitudeType getSelectedType() {
        return selectedType;
    }
    
    /**
     * 闂佹彃绉堕悿鍡涘箥閳ь剟寮垫径灞叫﹂柟?
     */
    public static void reset() {
        currentInputState = AltitudeInputState.SELECTING_TYPE;
        customMinHeight = null;
        customMaxHeight = null;
        selectedType = AltitudeType.AUTOMATIC;
        
        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "Reset altitude manager state");
    }
    
    /**
     * 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敂绛嬪妧闁革负鍔忕换妯兼偘瀹€鍕蒋閹艰揪绠掔欢顓㈠礂?
     */
    public static boolean isInputtingAltitude() {
        return currentInputState == AltitudeInputState.INPUT_MIN_HEIGHT || 
               currentInputState == AltitudeInputState.INPUT_MAX_HEIGHT;
    }
} 
