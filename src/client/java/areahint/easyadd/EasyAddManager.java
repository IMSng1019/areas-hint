package areahint.easyadd;

import areahint.data.AreaData;

import areahint.chat.ClientChatCompat;
import areahint.file.FileManager;
import areahint.file.JsonHelper;
import areahint.debug.ClientDebugManager;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyAdd鍔熻兘绠＄悊鍣?
 * 璐熻矗浜や簰寮忓煙鍚嶆坊鍔犵殑鏁翠釜娴佺▼绠＄悊
 */
public class EasyAddManager {
    
    /**
     * EasyAdd鐘舵€佹灇涓?
     */
    public enum EasyAddState {
        IDLE,           // 绌洪棽鐘舵€?
        INPUT_NAME,     // 杈撳叆鍩熷悕鍚嶇О
        INPUT_SURFACE_NAME, // 杈撳叆鑱斿悎鍩熷悕鍚嶇О
        INPUT_LEVEL,    // 杈撳叆鍩熷悕绛夌骇
        SELECT_BASE,    // 閫夋嫨涓婄骇鍩熷悕
        RECORDING_POINTS, // 璁板綍鍧愭爣鐐?
        HEIGHT_SELECTION, // 楂樺害閫夋嫨
        COLOR_SELECTION, // 棰滆壊閫夋嫨锛堟柊澧烇級
        COLOR_INPUT,    // 鑷畾涔夐鑹茶緭鍏ワ紙鏂板锛?
        CONFIRM_SAVE    // 纭淇濆瓨
    }
    
    // 鍗曚緥瀹炰緥
    private static EasyAddManager instance;
    
    // 褰撳墠鐘舵€?
    private EasyAddState currentState = EasyAddState.IDLE;
    
    // 鍩熷悕鏁版嵁鏀堕泦
    private String areaName = null;
    private String surfaceName = null;  // 鑱斿悎鍩熷悕
    private int areaLevel = 1;
    private String baseName = null;
    private List<BlockPos> recordedPoints = new ArrayList<>();
    private String currentDimension = null;
    private List<AreaData> availableParentAreas = new ArrayList<>();
    private AreaData.AltitudeData customAltitudeData = null; // 鑷畾涔夐珮搴︽暟鎹?
    private String selectedColor = "#FFFFFF"; // 閫夋嫨鐨勯鑹诧紙鏂板锛?
    
    // 鑱婂ぉ鐩戝惉鍣ㄦ敞鍐岀姸鎬?
    private boolean chatListenerRegistered = false;
    
    // 绉佹湁鏋勯€犲嚱鏁帮紙鍗曚緥妯″紡锛?
    private EasyAddManager() {}
    
    /**
     * 鑾峰彇鍗曚緥瀹炰緥
     */
    public static EasyAddManager getInstance() {
        if (instance == null) {
            instance = new EasyAddManager();
        }
        return instance;
    }
    
    /**
     * 鍚姩EasyAdd娴佺▼
     */
    public void startEasyAdd() {
        if (currentState != EasyAddState.IDLE) {
            MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.general")), false);
            return;
        }
        
        // 鑾峰彇褰撳墠缁村害淇℃伅
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            currentDimension = client.world.getRegistryKey().getValue().toString();
            
            // 娉ㄥ唽鑱婂ぉ鐩戝惉鍣?
            registerChatListener();
            
            // 璁剧疆鐘舵€佸苟鏄剧ずUI
            currentState = EasyAddState.INPUT_NAME;
            EasyAddUI.showNameInputScreen();
        }
    }
    
    /**
     * 娉ㄥ唽鑱婂ぉ鐩戝惉鍣ㄦ潵鎹曡幏鐢ㄦ埛杈撳叆
     */
    private void registerChatListener() {
        if (!chatListenerRegistered) {
            ClientChatCompat.register(input -> {
                if (currentState != EasyAddState.IDLE) {
                    handleChatInput(input);
                }
            });
            chatListenerRegistered = true;
        }
    }
    
    /**
     * 澶勭悊鐢ㄦ埛鑱婂ぉ杈撳叆
     */
    private void handleChatInput(String input) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 绉婚櫎鍓嶇紑绗﹀彿锛堝鏋滄湁鐨勮瘽锛?
        if (input.startsWith("<") && input.contains(">")) {
            int endIndex = input.indexOf(">") + 1;
            if (endIndex < input.length()) {
                input = input.substring(endIndex).trim();
            }
        }
        
        switch (currentState) {
            case INPUT_NAME:
                if (!input.trim().isEmpty()) {
                    areaName = input.trim();

                    // 妫€鏌ュ煙鍚嶅悕绉版槸鍚﹀凡瀛樺湪锛堜笉妫€鏌ヨ仈鍚堝煙鍚嶏級
                    if (checkAreaNameExists(areaName)) {
                        client.player.sendMessage(areahint.util.TextCompat.of("搂c" + I18nManager.translate("easyadd.message.area.name_4") + areaName + I18nManager.translate("easyadd.message.dimension")), false);
                        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.name")), false);
                        // 淇濇寔鍦?INPUT_NAME 鐘舵€侊紝绛夊緟鐢ㄦ埛閲嶆柊杈撳叆
                        return;
                    }

                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.name_2") + areaName), false);

                    // 杩涘叆鑱斿悎鍩熷悕杈撳叆
                    currentState = EasyAddState.INPUT_SURFACE_NAME;
                    EasyAddUI.showSurfaceNameInputScreen();
                }
                break;
                
            case INPUT_SURFACE_NAME:
                // 鑱斿悎鍩熷悕鍙互涓虹┖
                surfaceName = input.trim().isEmpty() ? null : input.trim();
                if (surfaceName != null) {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.surface") + surfaceName), false);
                } else {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.area.surface")), false);
                }
                
                // 杩涘叆绛夌骇閫夋嫨
                currentState = EasyAddState.INPUT_LEVEL;
                EasyAddUI.showLevelInputScreen();
                break;
                
            case HEIGHT_SELECTION:
                // 澶勭悊楂樺害杈撳叆
                if (EasyAddAltitudeManager.isInputtingAltitude()) {
                    EasyAddAltitudeManager.handleAltitudeInput(input);
                }
                break;
                
            case COLOR_INPUT:
                // 澶勭悊鑷畾涔夐鑹茶緭鍏?
                handleCustomColorInput(input);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * 澶勭悊绛夌骇杈撳叆锛堜粠鍛戒护璋冪敤锛?
     */
    public void handleLevelInput(int level) {
        if (currentState != EasyAddState.INPUT_LEVEL) {
            return;
        }
        
        areaLevel = level;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.level") + level), false);
        }
        
        if (level == 1) {
            // 椤剁骇鍩熷悕锛岀洿鎺ュ紑濮嬭褰曞潗鏍?
            baseName = null;
            currentState = EasyAddState.RECORDING_POINTS;
            if (client.player != null) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.coordinate.record_2") + EasyAddConfig.getRecordKey() + I18nManager.translate("easyadd.message.record_2")), false);
            }
        } else {
            // 闇€瑕侀€夋嫨涓婄骇鍩熷悕
            loadAvailableParentAreas();
            currentState = EasyAddState.SELECT_BASE;
            EasyAddUI.showBaseSelectScreen(availableParentAreas);
        }
    }
    
    /**
     * 澶勭悊涓婄骇鍩熷悕閫夋嫨锛堜粠鍛戒护璋冪敤锛?
     */
    public void handleBaseSelection(String selectedBaseName) {
        if (currentState != EasyAddState.SELECT_BASE) {
            return;
        }
        
        // 绉婚櫎寮曞彿锛堝鏋滃瓨鍦級
        baseName = selectedBaseName;
        if (baseName.startsWith("\"") && baseName.endsWith("\"") && baseName.length() > 1) {
            baseName = baseName.substring(1, baseName.length() - 1);
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.parent") + baseName), false);
        }
        
        // 寮€濮嬭褰曞潗鏍囩偣
        currentState = EasyAddState.RECORDING_POINTS;
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.coordinate.record_2") + EasyAddConfig.getRecordKey() + I18nManager.translate("easyadd.message.record_2")), false);
        }
    }

    /**
     * 鍔犺浇鍙€夌殑涓婄骇鍩熷悕
     */
    private void loadAvailableParentAreas() {
        availableParentAreas.clear();
        
        try {
            String fileName = getFileNameForCurrentDimension();
            if (fileName != null) {
                List<AreaData> allAreas = FileManager.readAreaData(areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName));
                
                // 绛涢€夊嚭绛夌骇涓哄綋鍓嶇瓑绾?1鐨勫煙鍚?
                int targetLevel = areaLevel - 1;
                for (AreaData area : allAreas) {
                    if (area.getLevel() == targetLevel) {
                        availableParentAreas.add(area);
                    }
                }
            }
        } catch (Exception e) {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD, 
                "鍔犺浇涓婄骇鍩熷悕澶辫触: " + e.getMessage());
        }
        
        if (availableParentAreas.isEmpty()) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.level") + (areaLevel - 1) + I18nManager.translate("easyadd.message.area.parent")), false);
            cancelEasyAdd();
            return;
        }
    }
    
    /**
     * 寮€濮嬪潗鏍囩偣璁板綍
     */
    private void startPointRecording() {
        recordedPoints.clear();
        MinecraftClient.getInstance().player.sendMessage(
            areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.vertex.record") + EasyAddConfig.getRecordKey() + I18nManager.translate("easyadd.message.record_2")), false);
        MinecraftClient.getInstance().player.sendMessage(
            areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.record_3")), false);
    }
    
    /**
     * 璁板綍褰撳墠鐜╁浣嶇疆浣滀负椤剁偣
     */
    public void recordCurrentPosition() {
        if (currentState != EasyAddState.RECORDING_POINTS) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        BlockPos pos = client.player.getBlockPos();
        recordedPoints.add(pos);

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.coordinate.record") + recordedPoints.size() + ": 搂6(" +
            pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"), false);

        // 鏄剧ず褰撳墠鐘舵€佸拰閫夐」
        EasyAddUI.showPointRecordedScreen(recordedPoints, pos);

        // 鏇存柊杈圭晫鍙鍖栫殑涓存椂椤剁偣
        areahint.boundviz.BoundVizManager.getInstance().setTemporaryVertices(recordedPoints, true);

        ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
            "璁板綍鍧愭爣鐐? " + pos + ", 鎬昏: " + recordedPoints.size());
    }
    
    /**
     * 瀹屾垚鍧愭爣璁板綍锛岃繘鍏ラ珮搴﹂€夋嫨闃舵
     */
    public void finishPointRecording() {
        if (currentState != EasyAddState.RECORDING_POINTS) {
            return;
        }
        
        if (recordedPoints.size() < 3) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.record")), false);
            return;
        }
        
        // 娓呴櫎涓存椂椤剁偣锛堣褰曞畬鎴愬悗涓嶅啀鏄剧ず锛?
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();

        // 杩涘叆楂樺害閫夋嫨鐘舵€?
        currentState = EasyAddState.HEIGHT_SELECTION;

        // 寮€濮嬮珮搴﹂€夋嫨娴佺▼
        EasyAddAltitudeManager.startAltitudeSelection(recordedPoints);
    }
    
    /**
     * 缁х画楂樺害閫夋嫨鍚庣殑娴佺▼
     * @param altitudeData 楂樺害鏁版嵁锛宯ull琛ㄧず浣跨敤鑷姩璁＄畻
     */
    public void proceedWithAltitudeData(AreaData.AltitudeData altitudeData) {
        if (currentState != EasyAddState.HEIGHT_SELECTION) {
            return;
        }
        
        // 淇濆瓨楂樺害鏁版嵁
        customAltitudeData = altitudeData;
        
        // 杩涘叆棰滆壊閫夋嫨鐘舵€侊紙鏂板锛?
        currentState = EasyAddState.COLOR_SELECTION;
        
        // 鏄剧ず棰滆壊閫夋嫨鐣岄潰
        EasyAddUI.showColorSelectionScreen();
    }
    
    /**
     * 澶勭悊棰滆壊閫夋嫨鍚庣殑娴佺▼
     * @param selectedColor 閫夋嫨鐨勯鑹?
     */
    public void proceedWithColorSelection(String selectedColor) {
        if (currentState != EasyAddState.COLOR_SELECTION) {
            return;
        }
        
        // 淇濆瓨閫夋嫨鐨勯鑹?
        this.selectedColor = selectedColor;
        
        // 杩涘叆纭淇濆瓨鐘舵€?
        currentState = EasyAddState.CONFIRM_SAVE;
        
        // 璁＄畻浜岀骇椤剁偣鍜屽叾浠栨暟鎹?
        try {
            AreaData areaData = buildAreaData();
            
            // 楠岃瘉鍩熷悕鏈夋晥鎬?
            if (validateAreaData(areaData)) {
                EasyAddUI.showConfirmSaveScreen(areaData);
            } else {
                MinecraftClient.getInstance().player.sendMessage(
                    areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.area.coordinate")), false);
                cancelEasyAdd();
            }
        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.area_2") + e.getMessage()), false);
                cancelEasyAdd();
        }
    }
    
    /**
     * 澶勭悊棰滆壊閫夋嫨鍛戒护
     * @param colorInput 棰滆壊杈撳叆
     */
    public void handleColorSelection(String colorInput) {
        if (currentState != EasyAddState.COLOR_SELECTION) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 澶勭悊鑷畾涔夐鑹茶緭鍏?
        if ("custom".equals(colorInput)) {
            currentState = EasyAddState.COLOR_INPUT;
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.color")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.general_9")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.error.cancel")), false);
            return;
        }
        
        // 楠岃瘉棰滆壊鏍煎紡
        String normalizedColor = areahint.util.ColorUtil.normalizeColor(colorInput);
        if (normalizedColor == null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.error.color")), false);
            return;
        }
        
        // 澶勭悊棰滆壊閫夋嫨
        proceedWithColorSelection(normalizedColor);
    }
    
    /**
     * 澶勭悊鑷畾涔夐鑹茶緭鍏?
     * @param colorInput 鐢ㄦ埛杈撳叆鐨勯鑹?
     */
    private void handleCustomColorInput(String colorInput) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 楠岃瘉棰滆壊鏍煎紡
        String normalizedColor = areahint.util.ColorUtil.normalizeColor(colorInput);
        if (normalizedColor == null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.color")), false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.general_10")), false);
            return;
        }
        
        // 澶勭悊棰滆壊閫夋嫨
        proceedWithColorSelection(normalizedColor);
    }
    
    /**
     * 鏋勫缓AreaData瀵硅薄
     */
    private AreaData buildAreaData() {
        // 璁＄畻浜岀骇椤剁偣锛圓ABB鍖呭洿鐩掞級
        List<AreaData.Vertex> secondVertices = EasyAddGeometry.calculateBoundingBox(recordedPoints);
        
        // 杞崲涓€绾ч《鐐?
        List<AreaData.Vertex> vertices = new ArrayList<>();
        for (BlockPos pos : recordedPoints) {
            vertices.add(new AreaData.Vertex(pos.getX(), pos.getZ()));
        }
        
        // 閫夋嫨楂樺害鏁版嵁锛氳嚜瀹氫箟浼樺厛锛屽惁鍒欒嚜鍔ㄨ绠?
        AreaData.AltitudeData altitude;
        if (customAltitudeData != null) {
            altitude = customAltitudeData;
        } else {
            altitude = EasyAddGeometry.calculateAltitudeRange(recordedPoints);
        }
        
        // 鑾峰彇鐜╁鍚嶅瓧浣滀负绛惧悕
        String signature = MinecraftClient.getInstance().player.getName().getString();
        
        return new AreaData(areaName, vertices, secondVertices, altitude, areaLevel, baseName, signature, selectedColor, surfaceName);
    }
    
    /**
     * 楠岃瘉鍩熷悕鏁版嵁鐨勬湁鏁堟€?
     */
    private boolean validateAreaData(AreaData areaData) {
        if (baseName != null) {
            // 鏌ユ壘涓婄骇鍩熷悕
            AreaData parentArea = findParentArea();
            if (parentArea != null) {
                return EasyAddGeometry.validateAreaInParent(areaData, parentArea);
            } else {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.area.parent") + baseName), false);
                }
                return false;
            }
        }
        return true; // 椤剁骇鍩熷悕鏃犻渶楠岃瘉
    }
    
    /**
     * 鏌ユ壘涓婄骇鍩熷悕
     */
    private AreaData findParentArea() {
        for (AreaData area : availableParentAreas) {
            if (area.getName().equals(baseName)) {
                return area;
            }
        }
        return null;
    }
    
    /**
     * 纭淇濆瓨鍩熷悕
     */
    public void confirmSave() {
        if (currentState != EasyAddState.CONFIRM_SAVE) {
            return;
        }
        
        try {
            AreaData areaData = buildAreaData();
            
            // 鍙戦€佸埌鏈嶅姟绔?
            EasyAddNetworking.sendAreaDataToServer(areaData, currentDimension);
            
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area_3") + areaName + I18nManager.translate("easyadd.message.general")), false);
            
            resetState();
            
        } catch (Exception e) {
            MinecraftClient.getInstance().player.sendMessage(
                areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.area.save") + e.getMessage()), false);
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD, 
                "淇濆瓨澶辫触: " + e.getMessage());
        }
    }
    
    /**
     * 鍙栨秷EasyAdd娴佺▼
     */
    public void cancelEasyAdd() {
        MinecraftClient.getInstance().player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel")), false);
        resetState();
    }
    
    /**
     * 閲嶇疆鐘舵€?
     */
    private void resetState() {
        currentState = EasyAddState.IDLE;
        areaName = null;
        surfaceName = null;
        areaLevel = 1;
        baseName = null;
        recordedPoints.clear();
        currentDimension = null;
        availableParentAreas.clear();
        customAltitudeData = null;
        selectedColor = "#FFFFFF"; // 閲嶇疆棰滆壊

        // 娓呴櫎杈圭晫鍙鍖栫殑涓存椂椤剁偣
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();

        // 閲嶇疆楂樺害绠＄悊鍣?
        EasyAddAltitudeManager.reset();
    }
    
    /**
     * 妫€鏌ュ煙鍚嶅悕绉版槸鍚﹀凡瀛樺湪浜庡綋鍓嶇淮搴?
     * 娉ㄦ剰锛氬彧妫€鏌ュ煙鍚嶅悕绉帮紙name瀛楁锛夛紝涓嶆鏌ヨ仈鍚堝煙鍚嶏紙surfacename瀛楁锛?
     * @param areaName 瑕佹鏌ョ殑鍩熷悕鍚嶇О
     * @return 濡傛灉鍩熷悕鍚嶇О宸插瓨鍦ㄨ繑鍥瀟rue锛屽惁鍒欒繑鍥瀎alse
     */
    private boolean checkAreaNameExists(String areaName) {
        try {
            String fileName = getFileNameForCurrentDimension();
            if (fileName == null) {
                ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                    "鏃犳硶纭畾褰撳墠缁村害鏂囦欢鍚嶏紝璺宠繃鏌ラ噸");
                return false;
            }

            // 璇诲彇褰撳墠缁村害鐨勬墍鏈夊煙鍚嶆暟鎹?
            List<AreaData> existingAreas = FileManager.readAreaData(
                areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName));

            // 妫€鏌ユ槸鍚﹀瓨鍦ㄧ浉鍚岀殑鍩熷悕鍚嶇О锛坣ame瀛楁锛?
            for (AreaData area : existingAreas) {
                if (area.getName().equals(areaName)) {
                    ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                        "鍙戠幇閲嶅鍩熷悕鍚嶇О: " + areaName);
                    return true;
                }
            }

            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                "鍩熷悕鍚嶇О \"" + areaName + "\" 鏈噸澶嶏紝鍙互浣跨敤");
            return false;

        } catch (Exception e) {
            ClientDebugManager.sendDebugInfo(ClientDebugManager.DebugCategory.EASY_ADD,
                "妫€鏌ュ煙鍚嶅悕绉版椂鍙戠敓閿欒: " + e.getMessage());
            // 鍙戠敓閿欒鏃讹紝涓轰簡瀹夊叏璧疯锛屽厑璁哥户缁紙杩斿洖false锛?
            // 鏈嶅姟绔繕浼氬啀娆℃鏌?
            return false;
        }
    }

    /**
     * 鑾峰彇褰撳墠缁村害鐨勬枃浠跺悕
     */
    private String getFileNameForCurrentDimension() {
        if (currentDimension == null) return null;

        if (currentDimension.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (currentDimension.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (currentDimension.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return null;
    }
    
    // Getters
    public EasyAddState getCurrentState() { return currentState; }
    public String getAreaName() { return areaName; }
    public int getAreaLevel() { return areaLevel; }
    public String getBaseName() { return baseName; }
    public List<BlockPos> getRecordedPoints() { return new ArrayList<>(recordedPoints); }
    public String getCurrentDimension() { return currentDimension; }
    public String getSelectedColor() { return selectedColor; }
} 
