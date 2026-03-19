package areahint.command;

import areahint.i18n.I18nManager;

import areahint.chat.ClientChatCompat;
import areahint.network.ClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * SetHigh閻庡箍鍨洪崺娑氱博椤栨碍鍤掑ù鐘€曢ˇ鈺呮偠閸℃鐝?
 * 濠㈣泛瀚幃濠囧春閻旈攱鍊冲Δ鍌浢€瑰磭鎷嬮崜褏鏋傞柣銊ュ椤撳綊骞嬫搴紓濞存嚎鍊撶花?
 */
public class SetHighClientCommand {
    
    /**
     * 濡ゅ倹锚鐎硅櫕娼忛幘鍐插汲闁绘鍩栭埀顑跨劍閻忓洦绋?
     */
    public enum AltitudeInputState {
        SELECTING_TYPE,     // 闂侇偄顦扮€氥劍顨囧Ο鍝勵唺缂侇偉顕ч悗?
        INPUT_MAX_HEIGHT,   // 閺夊牊鎸搁崣鍡涘嫉閳ь剚顨囧鈧悵顔芥償?
        INPUT_MIN_HEIGHT    // 閺夊牊鎸搁崣鍡涘嫉閳ь剚鎷呮惔銊у蒋閹?
    }
    
    // 鐟滅増鎸告晶鐘虫綇閹惧啿寮抽柣妯垮煐閳?
    private static AltitudeInputState currentInputState = AltitudeInputState.SELECTING_TYPE;
    
    // 闁煎浜滈悾鐐▕婢舵劗褰幖杈剧畱閳?
    private static Double customMaxHeight = null;
    private static Double customMinHeight = null;
    
    // 鐟滅増鎸告晶鐘绘焻婢跺鍘柣銊ュ閻撴瑩宕?
    private static String currentSelectedArea = null;
    
    // 闁煎崬锕ら妵澶愭儎閹存繃鍎旈柛锝冨妽閺佺偤宕樺畝鈧慨鎼佸箑?
    private static boolean chatListenerRegistered = false;
    
    /**
     * 闁告帗绻傞～鎰板礌閺嶎兛鍠婂鍨涙櫇濞插啴宕ラ鈧▍?
     */
    public static void init() {
        if (!chatListenerRegistered) {
            ClientChatCompat.register(input -> {
                // 濠殿喖顑囩划鎾村緞閸曨厽鍊為柤鍗烇工閵囧娼忛幘鍐插汲闁挎稑鐭侀鈧?handleChatInput 闁哄鍎遍崰鍛偓瑙勭濡叉悂宕ラ敃鈧ˇ鈺呮偠?
                handleChatInput(input);
            });
            chatListenerRegistered = true;
        }
    }
    
    /**
     * 濠㈣泛瀚幃濠囧嫉瀹ュ懎顫ら柛锝冨妼瑜板倿鏌呮担鐑樼暠闁糕晝鍠庨幃鏇㈠礆濡ゅ嫨鈧?
     * @param areaNames 闁糕晝鍠庨幃鏇㈠触瀹ュ泦鐐哄礆濡ゅ嫨鈧?
     * @param dimensionType 缂備焦娼欑€瑰磭鐚剧拠鑼偓?
     */
    public static void handleAreaList(List<String> areaNames, String dimensionType) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        if (areaNames.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.area.altitude.dimension")), false);
            return;
        }
        
        // 闁哄嫬澧介妵姘跺春閻旈攱鍊抽柛鎺擃殙閵嗗啴鏁嶉崼婵堟暔闁告瑯鍨抽崑锝夊礄缂佹ê鐦婚梺绛嬪櫙缁?
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.title.area.altitude.modify")), false);
        for (int i = 0; i < areaNames.size(); i++) {
            String areaName = areaNames.get(i);
            MutableText areaButton = areahint.util.TextCompat.literal(String.format("閹间繘%d. 閹间咖%s", i + 1, areaName))
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh " + areaName))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("sethigh.prompt.select.hover") + areaName)))
                    .withColor(Formatting.AQUA));
            client.player.sendMessage(areaButton, false);
        }

        // 闁告瑦鐗楃粔鐑藉箰婢舵劖灏?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("command.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude.cancel"))))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 濠㈣泛瀚幃濠囧春閻旈攱鍊抽梺顐㈩槹鐎?
     * @param selectedArea 闂侇偄顦扮€氥劑鎯冮崟顐ゅ幍闁?
     * @param hasAltitude 闁哄嫷鍨伴幆渚€寮垫径灞界疀闁哄牆顦甸悵顔芥償閿曞倹顎欓柛?
     * @param maxHeight 鐟滅増鎸告晶鐘诲嫉閳ь剚寰勮閻濐喗鎯?
     * @param minHeight 鐟滅増鎸告晶鐘诲嫉閳ь剛浜歌箛娑氬蒋閹?
     */
    public static void handleAreaSelection(String selectedArea, boolean hasAltitude, 
                                         Double maxHeight, Double minHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 閻犱礁澧介悿鍡氥亹閹惧啿顤呴梺顐㈩槷閼垫垿鎯冮崟顐ゅ幍闁?
        currentSelectedArea = selectedArea;
        
        // 闁哄嫬澧介妵姘炽亹閹惧啿顤呭Δ鍌浢€瑰磭鎷嬮崜褏鏋?
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.title.area.altitude") + selectedArea + " ====="), false);
        
        if (hasAltitude) {
            String maxStr = maxHeight != null ? String.format("%.1f", maxHeight) : I18nManager.translate("command.message.general_10");
            String minStr = minHeight != null ? String.format("%.1f", minHeight) : I18nManager.translate("command.message.general_10");
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_8") + maxStr + I18nManager.translate("command.message.general_7") + minStr), false);
        } else {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_7")), false);
        }
        
        // 闁哄嫬澧介妵姘殗濡搫顔婇梺顐㈩槹鐎氥劑鎮惧畝鍕〃闁挎稑鑻崕姝焌syadd濞戞挴鍋撻柡宥夋敱瑜颁焦绗熷☉娆忕樆闂佺瓔鍣ｉ埀顒€顦扮€?
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.title.altitude")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.prompt.altitude_3")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.area_7") + selectedArea), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        // 闁煎浜滈悾鐐▕婢舵劗褰幖杈鹃檮鐎垫粓鏌?
        MutableText customButton = areahint.util.TextCompat.literal(I18nManager.translate("command.button.altitude"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh custom " + selectedArea))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("command.prompt.altitude_5"))))
                .withColor(Formatting.LIGHT_PURPLE));
        
        // 濞戞挸绉瑰娲礆閸洜褰幖杈鹃檮鐎垫粓鏌?
        MutableText unlimitedButton = areahint.util.TextCompat.literal(I18nManager.translate("command.button.altitude_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh unlimited " + selectedArea))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("command.message.area.altitude.coordinate"))))
                .withColor(Formatting.YELLOW));
        
        // 闁告瑦鐗楃粔鐑藉箰婢舵劖灏?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("command.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint sethigh cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude.cancel"))))
                .withColor(Formatting.RED));
        
        // 缂備礁瀚幃搴ㄥ箰婢舵劖灏﹂悶?
        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(customButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(unlimitedButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.area.altitude.boundary")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_6")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.area_8") + selectedArea + I18nManager.translate("command.message.altitude.start")), false);
    }
    
    /**
     * 濠㈣泛瀚幃濠囨嚊椤忓嫮鏆板☉鏂款樀閻濐喗鎯旈敃浣虹炕闁?
     * @param areaName 闁糕晝鍠庨幃鏇㈠触瀹ュ泦?
     * @param input 闁活潿鍔嶉崺娑欐綇閹惧啿寮抽柣銊ュ閻濐喗鎯旈敃鈧悺褏绮敂鑳洬
     */
    public static void handleCustomHeightInput(String areaName, String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 婵☆偀鍋撻柡灞诲劚瑜板洤鈽夐崼鐔告儥濞?
        if (input.trim().isEmpty() || input.contains("取消") || input.equalsIgnoreCase("cancel")) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.altitude.cancel_2")), false);
            resetCustomHeightState();
            return;
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            
            // 濡ょ姴鐭侀惁澶嬵殗濡搫顔婇柤鐓庡暙濞?
            if (value < -64 || value > 320) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.altitude_7")), false);
                return;
            }
            
            // 闁哄秷顫夊畵浣姐亹閹惧啿顤呴柣妯垮煐閳ь兛绀侀ˇ鈺呮偠閸℃氨缈婚柛?
            if (currentInputState == AltitudeInputState.INPUT_MAX_HEIGHT) {
                // 閺夊牊鎸搁崣鍡涘嫉閳ь剚顨囧鈧悵顔芥償?
                customMaxHeight = value;
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_11") + value), false);
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.prompt.altitude_2")), false);
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_9")), false);
                
                
                currentInputState = AltitudeInputState.INPUT_MIN_HEIGHT;
                
            } else if (currentInputState == AltitudeInputState.INPUT_MIN_HEIGHT) {
                // 閺夊牊鎸搁崣鍡涘嫉閳ь剚鎷呮惔銊у蒋閹?
                if (customMaxHeight != null && value >= customMaxHeight) {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.altitude_6") + customMaxHeight), false);
                    return;
                }
                
                customMinHeight = value;
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_10") + value), false);
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_12") + customMaxHeight + " ~ " + customMinHeight), false);
                
                // 闁告帗绋戠紓鎾绘嚊椤忓嫮鏆板☉鏂款樀閻濐喗鎯旈敂鑺ユ闁硅鍠栭懟鐔煎矗閹达腹鍋撴担绛嬪殲婵?
                sendHeightRequest(areaName, true, customMaxHeight, customMinHeight);
                
                // 闂佹彃绉堕悿鍡涙偐閼哥鍋?
                resetCustomHeightState();
            }
            
        } catch (NumberFormatException e) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.general_12")), false);
        }
    }
    
    /**
     * 闁告瑦鍨块埀顑跨窔閻濐喗鎯旈敃渚囧晭缂傚喚鍠涢顒€效閸屾艾鐓傞柡鍫濈Т婵喖宕?
     * @param areaName 闁糕晝鍠庨幃鏇㈠触瀹ュ泦?
     * @param hasAltitude 闁哄嫷鍨伴幆渚€寮垫径鎰蒋閹艰揪绠撳娲礆?
     * @param maxHeight 闁哄牃鍋撳鍫嗗洨褰幖?
     * @param minHeight 闁哄牃鍋撻悘蹇撶箻閻濐喗鎯?
     */
    private static void sendHeightRequest(String areaName, boolean hasAltitude, 
                                        Double maxHeight, Double minHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.prompt.altitude")), false);
        
        // 闁告瑦鍨块埀顑胯兌缂嶅绱掑鍡╁殲婵?
        ClientNetworking.sendSetHighRequest(areaName, hasAltitude, maxHeight, minHeight);
    }
    
    /**
     * 濠㈣泛瀚幃濠囧嫉瀹ュ懎顫ら柛锝冨妼閹奸攱鎯?
     * @param success 闁哄嫷鍨伴幆渚€骞嬮幇顒€顫?
     * @param message 闁告繂绉寸花鎻掆槈閸喍绱?
     */
    public static void handleServerResponse(boolean success, MutableText message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (success) {
            client.player.sendMessage(areahint.util.TextCompat.literal("閹间繘").append(message), false);
        } else {
            client.player.sendMessage(areahint.util.TextCompat.literal("閹间竣").append(message), false);
        }
    }
    
    /**
     * 闂佹彃绉堕悿鍡涙嚊椤忓嫮鏆板☉鏂款樀閻濐喗鎯旈敂鎯﹂柟?
     */
    private static void resetCustomHeightState() {
        currentInputState = AltitudeInputState.SELECTING_TYPE;
        customMaxHeight = null;
        customMinHeight = null;
        currentSelectedArea = null;
    }
    
    /**
     * 鐎殿喒鍋撳┑顔碱儓閸ゆ粎鈧鐭粻鐔割殗濡搫顔婇弶鍫熸尭閸欏棗霉娴ｈ　鏌?
     * @param areaName 闁糕晝鍠庨幃鏇㈠触瀹ュ泦?
     */
    public static void startCustomHeightInput(String areaName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 闂佹彃绉堕悿鍡涙偐閼哥鍋?
        resetCustomHeightState();
        
        // 閻犱礁澧介悿鍡氥亹閹惧啿顤呴梺顐㈩槷閼垫垿鎯冮崟顐ゅ幍闁?
        currentSelectedArea = areaName;
        
        // 鐎殿喒鍋撳┑顔碱儓缁额參宕楅妷锔戒粯濡ゅ倹锕㈤悵顔芥償?
        currentInputState = AltitudeInputState.INPUT_MAX_HEIGHT;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.title.altitude_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.prompt.altitude_4")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.altitude_9")), false);
        
    }
    
    /**
     * 濠㈣泛瀚幃濠囨嚂婵犲倶浜弶鍫熸尭閸欏棝鏁嶉崼銏℃殸闁煎崬锕ら妵澶愭儎閹存繃鍎旈柛锝冨姀閻ㄧ喖鎮介…鎺旂
     * @param input 闁活潿鍔嶉崺娑欐綇閹惧啿寮抽柣銊ュ娴滅増寰勯埡浣告暥閻?
     * @return 闁哄嫷鍨伴幆浣瑰緞閸曨厽鍊炲ù婊冩缁额參宕?
     */
    public static boolean handleChatInput(String input) {
        if (currentSelectedArea == null && currentInputState == AltitudeInputState.SELECTING_TYPE) {
            return false;
        }
        
        // 缂佸顭峰▍搴ㄦ嚂婵犲倶浜柛鎾崇Ф缁辨垿鏁嶉崼婵嗘闁?EasyAdd 闁汇劌瀚悿鍕偝鐢喚绀?
        if (input.startsWith("<") && input.contains(">")) {
            int endIndex = input.indexOf(">") + 1;
            if (endIndex < input.length()) {
                input = input.substring(endIndex).trim();
            }
        }
        
        // 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敂鑺バ﹂柛娆愮墬缁夌兘宕ㄩ幋鎺撳Б
        if (input.trim().equals("/areahint sethigh cancel")) {
            resetCustomHeightState();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.altitude.cancel")), false);
            }
            return true;
        }
        
        // 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敂鑺バ﹂柤濂変簻閻ｇ偓绋婃径鎰蒋閹艰揪绠戦幊鈩冪?
        if (input.trim().startsWith("/areahint sethigh custom ")) {
            String areaName = input.trim().substring("/areahint sethigh custom ".length());
            if (currentSelectedArea == null || areaName.equals(currentSelectedArea)) {
                startCustomHeightInput(areaName);
                return true;
            }
        }
        
        // 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敂鑺バ﹀☉鎾崇Ч濡炬椽宕氶崼鏇犲蒋閹艰揪绠戦幊鈩冪?
        if (input.trim().startsWith("/areahint sethigh unlimited ")) {
            String areaName = input.trim().substring("/areahint sethigh unlimited ".length());
            if (currentSelectedArea == null || areaName.equals(currentSelectedArea)) {
                // 闁烩晛鐡ㄧ敮鎾矗閹达腹鍋撴担椋庣憹闂傚嫭鍔曢崺妤侇殗濡搫顔婇悹鍥敱閻?
                sendHeightRequest(areaName, false, null, null);
                resetCustomHeightState();
                return true;
            }
        }
        
        // 濠碘€冲€归悘澶婎潰閿濆懏韬弶鍫熸尭閸欏棝鎳涢鍕毎濞戞柨顦甸悵顔芥償閿旇偐绀夊璺哄閹﹥顨囧Ο鍝勵唺闁轰焦婢橀埀顒冨缁额參宕?
        if (currentInputState != AltitudeInputState.SELECTING_TYPE && currentSelectedArea != null) {
            handleCustomHeightInput(currentSelectedArea, input);
            return true;
        }
        
        return false;
    }
    
    /**
     * 闁兼儳鍢茶ぐ鍥亹閹惧啿顤呴弶鍫熸尭閸欏棝鎮╅懜纰樺亾?
     */
    public static AltitudeInputState getCurrentInputState() {
        return currentInputState;
    }
    
    /**
     * 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敂绛嬪妧闁革负鍔忕欢顓㈠礂閵夆晝褰幖?
     */
    public static boolean isInputtingAltitude() {
        return currentInputState != AltitudeInputState.SELECTING_TYPE;
    }
} 
