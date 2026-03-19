package areahint.dividearea;

import areahint.data.AreaData;

import areahint.chat.ClientChatCompat;
import areahint.file.FileManager;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

public class DivideAreaManager {
    public enum State {
        IDLE, SELECTING_AREA, RECORDING_POINTS,
        AREA1_NAME, AREA1_SURFACE_NAME, AREA1_LEVEL, AREA1_BASE, AREA1_COLOR,
        AREA2_NAME, AREA2_SURFACE_NAME, AREA2_LEVEL, AREA2_BASE, AREA2_COLOR,
        SAVING
    }

    private static DivideAreaManager instance;
    private MinecraftClient client;
    private DivideAreaUI ui;
    private State state = State.IDLE;
    private AreaData selectedArea;
    private List<Double[]> newVertices = new ArrayList<>();
    private boolean isActive = false;
    private boolean isRecording = false;
    private boolean chatListenerRegistered = false;

    // 闂備礁鎲＄敮鎺懳涘Δ浣虹彾濠电姴娲ょ憴锕傚箹鏉堝墽绋婚柣锝呭船閳藉骞橀崡鐐╁亾濡ゅ啫鍨濈€广儱妫庢禍婊堟煕閹捐尙顦﹀ù?
    private List<Double[]> area1Vertices;
    private List<Double[]> area2Vertices;

    // 濠电偞鍨堕幐鍫曞磹閹剧粯鐓傛繝濠傜墕濡﹢鏌℃径搴㈢《闁告鏁婚弻娑橆潩椤掑倐銈嗙箾閸喎鐏撮柡灞斤工椤撳ジ宕堕埡鍌溾偓?
    private AreaData area1Config;
    private AreaData area2Config;

    public static DivideAreaManager getInstance() {
        if (instance == null) instance = new DivideAreaManager();
        return instance;
    }

    private DivideAreaManager() {
        this.client = MinecraftClient.getInstance();
        this.ui = new DivideAreaUI(this);
    }

    private void registerChatListener() {
        if (!chatListenerRegistered) {
            ClientChatCompat.register(input -> {
                if (state == State.AREA1_NAME || state == State.AREA1_SURFACE_NAME
                    || state == State.AREA2_NAME || state == State.AREA2_SURFACE_NAME) {
                    handleChatInput(input);
                }
            });
            chatListenerRegistered = true;
        }
    }

    private void handleChatInput(String input) {
        if (client.player == null) return;
        // 闂備礁鎲￠…鍥╁垝椤栨粍鏆滈柍?<闂備胶绮竟鏇㈠疾濞戙埄鏁婄€广儱顦憴? 闂備礁鎲￠幐鍝ョ矓閸撲焦顫?
        if (input.startsWith("<") && input.contains(">")) {
            int end = input.indexOf(">") + 1;
            if (end < input.length()) input = input.substring(end).trim();
        }
        if (input.trim().isEmpty()) return;

        switch (state) {
            case AREA1_NAME:
                if (checkAreaNameExists(input.trim())) {
                    sendMsg(I18nManager.translate("dividearea.error.area.name") + "\"" + input.trim() + "\"" + I18nManager.translate("dividearea.prompt.general"), Formatting.RED);
                    return;
                }
                area1Config.setName(input.trim());
                sendMsg(I18nManager.translate("dividearea.message.name") + input.trim(), Formatting.GREEN);
                state = State.AREA1_SURFACE_NAME;
                sendMsg(I18nManager.translate("dividearea.title.area.surface"), Formatting.GOLD);
                sendMsg(I18nManager.translate("dividearea.prompt.area.surface"), Formatting.GREEN);
                sendMsg(I18nManager.translate("dividearea.message.area.surface.name"), Formatting.GRAY);
                sendMsg(I18nManager.translate("dividearea.prompt.area.surface_3"), Formatting.YELLOW);
                showCancelButton();
                break;
            case AREA1_SURFACE_NAME:
                if (!"skip".equalsIgnoreCase(input.trim())) {
                    area1Config.setSurfacename(input.trim());
                    sendMsg(I18nManager.translate("dividearea.message.area.surface_2") + input.trim(), Formatting.GREEN);
                } else {
                    sendMsg(I18nManager.translate("dividearea.message.area.surface"), Formatting.GRAY);
                }
                state = State.AREA1_COLOR;
                showColorSelection(1);
                break;
            case AREA2_NAME:
                if (checkAreaNameExists(input.trim())) {
                    sendMsg(I18nManager.translate("dividearea.error.area.name") + "\"" + input.trim() + "\"" + I18nManager.translate("dividearea.prompt.general"), Formatting.RED);
                    return;
                }
                area2Config.setName(input.trim());
                sendMsg(I18nManager.translate("dividearea.message.name_2") + input.trim(), Formatting.GREEN);
                state = State.AREA2_SURFACE_NAME;
                sendMsg(I18nManager.translate("dividearea.title.area.surface"), Formatting.GOLD);
                sendMsg(I18nManager.translate("dividearea.prompt.area.surface_2"), Formatting.GREEN);
                sendMsg(I18nManager.translate("dividearea.message.area.surface.name"), Formatting.GRAY);
                sendMsg(I18nManager.translate("dividearea.prompt.area.surface_3"), Formatting.YELLOW);
                showCancelButton();
                break;
            case AREA2_SURFACE_NAME:
                if (!"skip".equalsIgnoreCase(input.trim())) {
                    area2Config.setSurfacename(input.trim());
                    sendMsg(I18nManager.translate("dividearea.message.area.surface_3") + input.trim(), Formatting.GREEN);
                } else {
                    sendMsg(I18nManager.translate("dividearea.message.area.surface"), Formatting.GRAY);
                }
                state = State.AREA2_COLOR;
                showColorSelection(2);
                break;
            default:
                break;
        }
    }

    private boolean checkAreaNameExists(String name) {
        for (AreaData a : loadAllAreas())
            if (a.getName().equals(name)) return true;
        return false;
    }

    public boolean isActive() { return isActive; }
    public boolean isRecording() { return isRecording; }
    public State getState() { return state; }

    // ===== 缂?闂傚倸鍊搁崯鎶藉春閺嶎収鏁婄€光偓閸曨剚娅栧┑顔斤供閸撴岸宕曞▎鎾寸厱闁哄倽顕ф俊濂告煙閾氬倸宓嗙€规洘妞介幃銏ゆ煥鐎ｎ亖鍋撻幎鑺モ拺妞ゆ劑鍩勫Σ褰掓倵?=====

    public void start() {
        if (client.player == null) return;
        registerChatListener();
        isActive = true;
        state = State.SELECTING_AREA;
        List<AreaData> modifiableAreas = getModifiableAreas();
        if (modifiableAreas.isEmpty()) {
            sendMsg(I18nManager.translate("dividearea.error.area.divide_3"), Formatting.RED);
            reset();
            return;
        }
        ui.showAreaSelection(modifiableAreas);
    }

    public void selectAreaByName(String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            sendMsg(I18nManager.translate("dividearea.error.area"), Formatting.RED);
            return;
        }
        String cleaned = areaName.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1)
            cleaned = cleaned.substring(1, cleaned.length() - 1);

        List<AreaData> modifiable = getModifiableAreas();
        AreaData area = null;
        for (AreaData a : modifiable) {
            if (a.getName().equals(cleaned)) { area = a; break; }
        }
        if (area == null) {
            sendMsg(I18nManager.translate("addhint.error.area") + cleaned + I18nManager.translate("addhint.message.permission"), Formatting.RED);
            return;
        }
        this.selectedArea = area;
        sendMsg(I18nManager.translate("dividearea.prompt.area") + areahint.util.AreaDataConverter.getDisplayName(area), Formatting.GREEN);
        sendMsg(I18nManager.translate("dividearea.prompt.general_3") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("dividearea.message.vertex.record.divide"), Formatting.YELLOW);
        state = State.RECORDING_POINTS;
        isRecording = true;
        newVertices.clear();
        ui.showRecordingInterface();
    }

    public void cancel() {
        if (!isActive) return;
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
        ui.showCancelMessage();
        reset();
    }

    public void continueRecording() {
        if (!isRecording || client.player == null) return;
        sendMsg(I18nManager.translate("dividearea.message.record.continue") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("dividearea.message.record"), Formatting.GREEN);
    }

    // ===== 缂?闂傚倸鍊搁崯鎶藉春閺嶎収鏁婄€光偓閸曨剚娅栧┑顔缴戦崜姘跺船閸撲焦鍠愰柣妤€鐗婄粈瀣煏閸℃韬柟?=====

    public void recordCurrentPosition() {
        if (!isRecording || client.player == null) return;
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        newVertices.add(new Double[]{x, z});
        sendMsg(I18nManager.translate("dividearea.message.record_3") + newVertices.size() + ": §6(" + (int)Math.round(x) + ", " + String.format("%.1f", y) + ", " + (int)Math.round(z) + ")", Formatting.GREEN);

        List<net.minecraft.util.math.BlockPos> bpList = new ArrayList<>();
        for (Double[] v : newVertices)
            bpList.add(new net.minecraft.util.math.BlockPos(v[0].intValue(), (int) y, v[1].intValue()));
        areahint.boundviz.BoundVizManager.getInstance().setTemporaryVertices(bpList, true);
        ui.showPointRecordedOptions(newVertices.size());
    }

    // ===== 缂?闂傚倸鍊搁崯鎶藉春閺嶎収鏁婄€光偓閸曨剚娅栧┑顔斤供閸擄箑危闁秵鐓熼柣鎰级椤ユ粓鏌涢妸銉╁弰鐎?=====

    public void finishAndSave() {
        if (!isRecording || client.player == null) return;
        if (newVertices.size() < 2) {
            sendMsg(I18nManager.translate("dividearea.error.vertex.record.divide"), Formatting.RED);
            return;
        }
        isRecording = false;
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
        try {
            processDivision();
        } catch (Exception e) {
            sendMsg(I18nManager.translate("dividearea.error.area.divide") + e.getMessage(), Formatting.RED);
            e.printStackTrace();
            reset();
        }
    }

    private void processDivision() {
        // 1. 濠德板€曢崐褰掓晪闁诲海顢婂▍鏇€冮妷銉ф殕闁告劦浜濋～?
        if (!validateAltitude()) return;

        // 2. 闂備礁婀辩划顖炲礉閺嚶颁汗闁搞儺鍓欓崒銊╂煟閺冨牜妫戦柛妯兼暬閺屾稑顫濋鍌傃囨煏閸℃韬柟?
        List<Double[]> origVerts = extractOriginalVertices();
        if (origVerts.size() < 3) {
            sendMsg(I18nManager.translate("dividearea.error.area.vertex"), Formatting.RED);
            reset(); return;
        }

        // 3. 闂佸搫顦弲娑樏洪敃鍌氱闁靛牆妫楃欢鐐烘煛瀹ュ骸骞栭柛鏂诲劚椤啴濡堕崨顓ф殺闂佺顑戠紞渚€寮鍛殕闁告劖鍎抽幃浣虹磽娴ｅ搫校妞ゃ劌顦辩划鈺呮偄閸忕厧浠奸悗鍏夊亾闁逞屽墴瀹?
        List<Double[]> processedVerts = new ArrayList<>();
        List<Double[]> boundaryPoints = new ArrayList<>();
        processVerticesAndBoundary(origVerts, processedVerts, boundaryPoints);

        if (boundaryPoints.size() < 2) {
            sendMsg(I18nManager.translate("dividearea.error.area.divide.boundary"), Formatting.RED);
            reset(); return;
        }

        // 闂備礁鎲￠悷锕傛偋閺囩喐娅犻柣妯虹－閳绘棃鏌曢崼婵嗩伃闁搞倕顑夊鍫曞醇濠靛洩纭€闂佸搫鎷嬮崑濠囧箖?
        Double[] bp1 = boundaryPoints.get(0);
        Double[] bp2 = boundaryPoints.get(1);

        // 4. 闂備胶鎳撻悘姘跺磿閹惰棄鏄ラ悘鐐靛亾缂嶅洭鏌涢敂璇插箺婵炲懏娲熼弻锝夊箛椤旇棄娈屽┑鐘亾妞ゅ繐鐗嗛崒銊╂煟閺冨牜妫戦柛妯兼暬閺屾稑顫濋鍌傘儳绱掗弮鍌氭瀾缂佸顦扮换婵嬪礋椤掑倹娈稿┑鐐舵彧缁茬晫绮旈崼鏇熷仾?
        int bp1Edge = findEdgeIndex(bp1, origVerts);
        int bp2Edge = findEdgeIndex(bp2, origVerts);

        // 5. 闂佽绻愮换鎰涘Δ鍐╂殰妞ゆ劧绠戦弰銉╂煟閺冨牊鏁遍柛濠傚暱椤啴濡堕崨顓ф殺闂佺顑戠紞浣哥暦濮樿泛骞㈡俊顖濇娴滐絾绻涢幋鐐村蔼闁稿﹥顨堥崚?
        splitOriginalVertices(origVerts, bp1, bp2, bp1Edge, bp2Edge, processedVerts);

        if (area1Vertices == null || area1Vertices.size() < 3 || area2Vertices == null || area2Vertices.size() < 3) {
            sendMsg(I18nManager.translate("dividearea.error.vertex.divide"), Formatting.RED);
            reset(); return;
        }

        sendMsg(I18nManager.translate("dividearea.message.finish.divide") + area1Vertices.size() + I18nManager.translate("dividearea.message.vertex_2") + area2Vertices.size() + I18nManager.translate("dividearea.message.vertex"), Formatting.GREEN);

        // 7. 闂備礁鎲＄敮妤冩崲閸岀儑缍栭柟鐗堟緲缁€宀勬煛瀹ュ繒鐣抽柍宄扮墕閳藉骞欓崘褏鐩庨梺缁樻尰閻熲晛鐣烽妷銉悑濠㈣泛锕ょ挧瀣磽閸屾艾鏋ら柛锝庡灣濡叉劕鈻庨幋鐘碉紲閻熸粌瀛╃粩鐔封枎閹惧啿鍋嶉梺缁樻椤ユ捇宕㈤悽鍛婄厱婵﹩鍓涘瓭闂佹椿鍋呴敃銏ょ嵁閳ь剛鎲稿┑鍫燁潟?
        area1Config = createBaseConfig(area1Vertices);
        area2Config = createBaseConfig(area2Vertices);

        // 8. 闁诲孩顔栭崰鎺楀磻閹炬枼鏀芥い鏃傗拡閸庢棃鏌涘Ο绋库偓妤冩閺冨牜鏁婇柣鎾抽叄濞堫剟姊?
        startArea1Config();
    }

    // ===== 缂?闂傚倸鍊搁崯鎶藉春閺嶎収鏁婄€光偓閸曨剚娅栧┑鐐叉閵囨唶yAdd闁诲孩顔栭崰鏍崲濮椻偓瀹曘垽濡堕崶鈺冿紴濡炪倖姊婚弲顐﹀Χ閿斿墽纾?=====

    private void startArea1Config() {
        state = State.AREA1_NAME;
        sendMsg(I18nManager.translate("dividearea.title.divide"), Formatting.GOLD);
        sendMsg(I18nManager.translate("dividearea.prompt.name"), Formatting.GREEN);
        sendMsg(I18nManager.translate("dividearea.message.general_2"), Formatting.GRAY);
        showCancelButton();
    }

    private void startArea2Config() {
        state = State.AREA2_NAME;
        sendMsg(I18nManager.translate("dividearea.title.divide_2"), Formatting.GOLD);
        sendMsg(I18nManager.translate("dividearea.prompt.name_2"), Formatting.GREEN);
        sendMsg(I18nManager.translate("dividearea.message.general_2"), Formatting.GRAY);
        showCancelButton();
    }

    private void showCancelButton() {
        if (client.player == null) return;
        net.minecraft.text.MutableText cancel = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.cancel.divide"))))
            .withColor(Formatting.RED));
        client.player.sendMessage(cancel, false);
    }

    public void handleNameInput(String name) {
        if (state == State.AREA1_NAME) {
            area1Config.setName(name);
            sendMsg(I18nManager.translate("dividearea.message.name") + name, Formatting.GREEN);
            state = State.AREA1_LEVEL;
            showLevelSelection(1);
        } else if (state == State.AREA2_NAME) {
            area2Config.setName(name);
            sendMsg(I18nManager.translate("dividearea.message.name_2") + name, Formatting.GREEN);
            state = State.AREA2_LEVEL;
            showLevelSelection(2);
        }
    }

    public void handleLevelInput(int level) {
        if (state == State.AREA1_LEVEL) {
            area1Config.setLevel(level);
            sendMsg(I18nManager.translate("dividearea.message.level") + level, Formatting.GREEN);
            if (level > 1) {
                state = State.AREA1_BASE;
                showBaseSelection(1);
            } else {
                area1Config.setBaseName(null);
                state = State.AREA1_COLOR;
                showColorSelection(1);
            }
        } else if (state == State.AREA2_LEVEL) {
            area2Config.setLevel(level);
            sendMsg(I18nManager.translate("dividearea.message.level_2") + level, Formatting.GREEN);
            if (level > 1) {
                state = State.AREA2_BASE;
                showBaseSelection(2);
            } else {
                area2Config.setBaseName(null);
                state = State.AREA2_COLOR;
                showColorSelection(2);
            }
        }
    }

    public void handleBaseInput(String baseName) {
        if (state == State.AREA1_BASE) {
            area1Config.setBaseName("none".equals(baseName) ? null : baseName);
            sendMsg(I18nManager.translate("dividearea.message.area.parent_2") + (area1Config.getBaseName() == null ? I18nManager.translate("dividearea.message.general_3") : baseName), Formatting.GREEN);
            state = State.AREA1_COLOR;
            showColorSelection(1);
        } else if (state == State.AREA2_BASE) {
            area2Config.setBaseName("none".equals(baseName) ? null : baseName);
            sendMsg(I18nManager.translate("dividearea.message.area.parent_3") + (area2Config.getBaseName() == null ? I18nManager.translate("dividearea.message.general_3") : baseName), Formatting.GREEN);
            state = State.AREA2_COLOR;
            showColorSelection(2);
        }
    }

    public void handleColorInput(String color) {
        if (state == State.AREA1_COLOR) {
            area1Config.setColor(color);
            sendMsg(I18nManager.translate("dividearea.message.color") + color, Formatting.GREEN);
            // 闂備礁鎲￠悧鏇㈠箹椤愶附鍋?闂傚倷鐒﹀妯肩矓閸洘鍋柛鈩冪憿閸嬫捇鎮烽悧鍫熸嫳闂佹悶鍔嶅畝鎼佸极瀹ュ洣娌柣锝呯灱閿涙稒绻濆▓鍨灈妞ゆ垵顦靛畷銏ゅΧ閸モ晝锛炲銈嗘⒒閸樠囨⒕濮椻偓閺?
            startArea2Config();
        } else if (state == State.AREA2_COLOR) {
            area2Config.setColor(color);
            sendMsg(I18nManager.translate("dividearea.message.color_2") + color, Formatting.GREEN);
            // 濠电偞鍨堕幐鍫曞磹閹剧粯鐓傛繝濠傜墕缁€宀勬煕濠靛棗顏柛妯兼暬濮婃椽顢欓懡銈囩厯闂佸憡菧閸婃妲愰弮鍫晩闁兼祴鏅滃▓顕€姊洪悷鎵憼闁告枮鍛潟婵犻潧顑呴惌妤呮煙鏉堟崘鍚傞柛瀣尰閹峰懐绮欑捄銊ュ笓闂備礁鎼悧鍡欑矓鐎涙ɑ鍙忛柣鏃囨閸?
            sendToServer();
        }
    }

    private void sendToServer() {
        state = State.SAVING;
        String dimension = null;
        if (client.world != null)
            dimension = client.world.getRegistryKey().getValue().toString();
        if (dimension == null) {
            sendMsg(I18nManager.translate("dividearea.error.dimension"), Formatting.RED);
            reset(); return;
        }
        DivideAreaClientNetworking.sendDividedAreasToServer(area1Config, area2Config, selectedArea.getName(), dimension);
        sendMsg(I18nManager.translate("dividearea.message.divide_3"), Formatting.GREEN);
        reset();
    }

    // ===== UI闂佸搫顦悧鍡涘箠鎼淬垺鍙忔い蹇撶墕濡﹢鎮峰▎蹇擃伀闁?=====

    private void showLevelSelection(int areaNum) {
        if (client.player == null) return;
        sendMsg(I18nManager.translate("dividearea.prompt.general_2") + areaNum + I18nManager.translate("dividearea.message.level_3"), Formatting.GREEN);

        net.minecraft.text.MutableText l1 = areahint.util.TextCompat.literal(I18nManager.translate("dividearea.button.area")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea level 1"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area_2"))))
            .withColor(Formatting.AQUA));
        net.minecraft.text.MutableText l2 = areahint.util.TextCompat.literal(I18nManager.translate("dividearea.button.area_3")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea level 2"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area_3"))))
            .withColor(Formatting.YELLOW));
        net.minecraft.text.MutableText l3 = areahint.util.TextCompat.literal(I18nManager.translate("dividearea.button.area_2")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea level 3"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area_4"))))
            .withColor(Formatting.LIGHT_PURPLE));
        net.minecraft.text.MutableText cancel = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.cancel.divide"))))
            .withColor(Formatting.RED));

        client.player.sendMessage(areahint.util.TextCompat.empty().append(l1).append(areahint.util.TextCompat.of("  ")).append(l2).append(areahint.util.TextCompat.of("  ")).append(l3).append(areahint.util.TextCompat.of("  ")).append(cancel), false);
        sendMsg("Level guide: 1 = top-level area, 2/3 = child area", Formatting.GRAY);
    }

    private void showBaseSelection(int areaNum) {
        if (client.player == null) return;
        sendMsg(I18nManager.translate("dividearea.prompt.general_2") + areaNum + I18nManager.translate("dividearea.message.area.parent_4"), Formatting.GREEN);
        List<AreaData> allAreas = loadAllAreas();
        int targetLevel = (state == State.AREA1_BASE ? area1Config.getLevel() : area2Config.getLevel()) - 1;

        for (AreaData a : allAreas) {
            if (a.getLevel() == targetLevel) {
                String dn = areahint.util.AreaDataConverter.getDisplayName(a);
                net.minecraft.text.MutableText btn = areahint.util.TextCompat.literal("§6[" + dn + "]").setStyle(net.minecraft.text.Style.EMPTY
                    .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND,
                        "/areahint dividearea base \"" + a.getName() + "\""))
                    .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + dn + I18nManager.translate("dividearea.message.area.parent"))))
                    .withColor(Formatting.GOLD));
                client.player.sendMessage(btn, false);
            }
        }

        net.minecraft.text.MutableText cancel = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.cancel.divide"))))
            .withColor(Formatting.RED));
        client.player.sendMessage(cancel, false);
    }

    private void showColorSelection(int areaNum) {
        if (client.player == null) return;
        sendMsg(I18nManager.translate("dividearea.title.general") + areaNum + I18nManager.translate("dividearea.title.color"), Formatting.GOLD);
        sendMsg(I18nManager.translate("dividearea.prompt.area.color"), Formatting.GREEN);

        net.minecraft.text.MutableText row1 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_24"), "#FFFFFF", "§f")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_23"), "#808080", "§7")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_18"), "#555555", "§8")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_31"), "#000000", "§0"));
        net.minecraft.text.MutableText row2 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_19"), "#AA0000", "§4")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_27"), "#FF0000", "§c")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_25"), "#FF55FF", "§d")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_17"), "#FFAA00", "§6"));
        net.minecraft.text.MutableText row3 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_30"), "#FFFF55", "§e")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_28"), "#55FF55", "§a")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_20"), "#00AA00", "§2")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_13"), "#55FFFF", "§b"));
        net.minecraft.text.MutableText row4 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_22"), "#00AAAA", "§3")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_29"), "#5555FF", "§9")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_21"), "#0000AA", "§1")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_26"), "#AA00AA", "§5"));
        net.minecraft.text.MutableText row5 = areahint.util.TextCompat.empty()
            .append(colorBtn(I18nManager.translate("gui.message.general_15"), "FLASH_BW_ALL", "§7")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_14"), "FLASH_RAINBOW_ALL", "§b")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_12"), "FLASH_BW_CHAR", "§8")).append(areahint.util.TextCompat.of("  "))
            .append(colorBtn(I18nManager.translate("gui.message.general_11"), "FLASH_RAINBOW_CHAR", "§d"));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(row3, false);
        client.player.sendMessage(row4, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        sendMsg(I18nManager.translate("gui.message.general_4"), Formatting.GOLD);
        client.player.sendMessage(row5, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);

        net.minecraft.text.MutableText cancel = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel")).setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea cancel"))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.cancel.divide"))))
            .withColor(Formatting.RED));
        client.player.sendMessage(cancel, false);
    }

    private net.minecraft.text.MutableText colorBtn(String name, String value, String mc) {
        return areahint.util.TextCompat.literal(mc + "[" + name + "]").setStyle(net.minecraft.text.Style.EMPTY
            .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint dividearea color " + value))
            .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + name + I18nManager.translate("dividearea.message.area.color")))));
    }

    // ===== 闂備礁鎲￠崹瑙勬叏瀹曞洨绀婄€广儱娲ㄦ稉宥嗕繆閵堝嫯鍏岄柕?=====

    private boolean validateAltitude() {
        if (client.player == null || selectedArea.getAltitude() == null) return true;
        AreaData.AltitudeData alt = selectedArea.getAltitude();
        if (alt.getMax() == null || alt.getMin() == null) return true;

        double playerY = client.player.getY();
        double newMax = playerY + 10;
        double newMin = playerY - 10;

        if (newMax < alt.getMax() && newMin > alt.getMin()) {
            return true;
        } else {
            sendMsg(I18nManager.translate("dividearea.error.area.altitude_2"), Formatting.RED);
            sendMsg(I18nManager.translate("dividearea.error.area.altitude") + alt.getMin() + " ~ " + alt.getMax(), Formatting.RED);
            reset();
            return false;
        }
    }

    private List<Double[]> extractOriginalVertices() {
        List<Double[]> verts = new ArrayList<>();
        if (selectedArea.getVertices() == null) return verts;
        for (AreaData.Vertex v : selectedArea.getVertices())
            verts.add(new Double[]{v.getX(), v.getZ()});
        return verts;
    }

    private void processVerticesAndBoundary(List<Double[]> origVerts, List<Double[]> processedVerts, List<Double[]> boundaryPoints) {
        // 闂佽崵濮崇欢銈囨閺囥垺鍋╁┑鐘宠壘缁€鍡涙煕閳╁喚娈旀い銉ョ箳缁辨帡骞嗚娴犳帞绱掑Δ鈧惌鍌氱暦閵忋倖鍋勫瀣楠炲秹姊洪崨濠呭缂佸顥撶划鈺呮偄閸忕厧浠奸悗鍏夊亾闁逞屽墮閳绘捇骞嬮敃鈧粻銉╂煃瑜滈崜鐔奉嚕閸偄绶炲璺侯儏閺€顓㈡⒑?
        for (int i = 0; i < newVertices.size() - 1; i++) {
            Double[] p1 = newVertices.get(i);
            Double[] p2 = newVertices.get(i + 1);
            for (int j = 0; j < origVerts.size(); j++) {
                int k = (j + 1) % origVerts.size();
                Double[] inter = lineIntersection(p1, p2, origVerts.get(j), origVerts.get(k));
                if (inter != null) boundaryPoints.add(inter);
            }
        }

        // 濠电儑绲藉ú锔炬崲閸曨垰姹查柍褜鍓熼弻娑㈡晜閸濆嫬顬堥柣銏╁灱閸嬪﹤鐣峰ú顏呭亜闂佸灝顑呴埀顒佸▕閺屾盯骞囬浣告畻闂佺瀛╅幐鎶藉箚閸愵喖绀嬫い鏍ㄇ氶崑鎾寸節閸ャ劌鈧?
        for (Double[] v : newVertices) {
            if (isPointInPolygon(v, origVerts)) processedVerts.add(v);
        }

        // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞鐎ｎ偆绉堕梺闈╁瘜閸樿棄鈻嶉弴銏＄厽闁圭増澹嬮崑鎾斥槈濮樿鲸鍠掗梺?濠电偞鍨堕幖鈺傜妞嬪孩顫曟繝闈涙川椤╃兘骞栭幖顓犲帥婵炲瓨娲熼幃妯跨疀閹炬枼鎸冮梺绋款儜缂嶄線鐛澶嬪€锋い鎺嶆缁垶鏌℃径鍡樻珕闁搞劏鍋愮划鈺呮偄閸濄儮鏋栨繝鐢靛Т鐎氼參鎮鹃柆宥嗙厸闁割偅鑹炬禍楣冩煛婢跺棙娅嗛柛銊ョ秺瀹曟垿鏁愰崱妯侯€撳┑鐘才堥崑鎾绘偣閹邦喖鏋戞繛鐓庣箻瀹曠兘顢橀悢鍝ュ涧闂?
        if (boundaryPoints.size() < 2) {
            Double[] first = newVertices.get(0);
            Double[] last = newVertices.get(newVertices.size() - 1);
            if (boundaryPoints.size() < 1) {
                boundaryPoints.add(findClosestPointOnPolygon(first, origVerts));
            }
            if (boundaryPoints.size() < 2) {
                boundaryPoints.add(findClosestPointOnPolygon(last, origVerts));
            }
        }
    }

    private Double[] findClosestPointOnPolygon(Double[] point, List<Double[]> polygon) {
        double minDist = Double.MAX_VALUE;
        Double[] best = null;
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            Double[] cp = closestPointOnSegment(point, polygon.get(i), polygon.get(j));
            double d = distance(point, cp);
            if (d < minDist) { minDist = d; best = cp; }
        }
        return best;
    }

    private void splitOriginalVertices(List<Double[]> origVerts, Double[] bp1, Double[] bp2,
                                        int bp1Edge, int bp2Edge, List<Double[]> innerVerts) {
        int n = origVerts.size();

        // 闂備礁鎲￠悧鏇㈠箹椤愶附鍋?: bp1 闂?闂備礁鎲￠崝鏇㈠箠濮椻偓瀹曟洟骞橀鑲╊槴闂佺硶鍓濋〃鍛此夎箛娑欑厽?闂?bp2 闂?闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎遍ˇ顓㈠焵椤掆偓閿曨亪骞?bp2Edge+1..bp1Edge) 闂?闂備焦鎮堕崕鎶藉磻濞戙垹鏄ラ悗瑙勪粻1
        area1Vertices = new ArrayList<>();
        area1Vertices.add(bp1);
        area1Vertices.addAll(innerVerts);
        area1Vertices.add(bp2);
        int idx = (bp2Edge + 1) % n;
        while (idx != (bp1Edge + 1) % n) {
            area1Vertices.add(origVerts.get(idx));
            idx = (idx + 1) % n;
        }

        // 闂備礁鎲￠悧鏇㈠箹椤愶附鍋?: bp2 闂?闂備礁鎲￠崝鏇㈠箠濮椻偓瀹曟洟骞橀鑲╊槴闂佺硶鍓濋〃鍛此夎箛娑欑厽?闂備礁鎲￠悷銉х矓瀹勬噴? 闂?bp1 闂?闂備礁鎲￠…鍥窗鎼淬劍鍋傛繛鍡樻尭鐟欙妇鈧箍鍎遍ˇ顓㈠焵椤掆偓閿曨亪骞?bp1Edge+1..bp2Edge) 闂?闂備焦鎮堕崕鎶藉磻濞戙垹鏄ラ悗瑙勪粻2
        area2Vertices = new ArrayList<>();
        area2Vertices.add(bp2);
        List<Double[]> reversedInner = new ArrayList<>(innerVerts);
        java.util.Collections.reverse(reversedInner);
        area2Vertices.addAll(reversedInner);
        area2Vertices.add(bp1);
        idx = (bp1Edge + 1) % n;
        while (idx != (bp2Edge + 1) % n) {
            area2Vertices.add(origVerts.get(idx));
            idx = (idx + 1) % n;
        }
    }

    private int findEdgeIndex(Double[] point, List<Double[]> polygon) {
        double minDist = Double.MAX_VALUE;
        int bestEdge = 0;
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            Double[] closest = closestPointOnSegment(point, polygon.get(i), polygon.get(j));
            double dist = distance(point, closest);
            if (dist < minDist) { minDist = dist; bestEdge = i; }
        }
        return bestEdge;
    }

    private AreaData createBaseConfig(List<Double[]> vertices) {
        List<AreaData.Vertex> vList = new ArrayList<>();
        for (Double[] v : vertices) vList.add(new AreaData.Vertex(Math.round(v[0]), Math.round(v[1])));

        List<AreaData.Vertex> sv = calculateAABB(vertices);

        AreaData config = new AreaData(
            "", vList, sv,
            selectedArea.getAltitude(),
            selectedArea.getLevel(),
            selectedArea.getBaseName(),
            client.player != null ? client.player.getGameProfile().getName() : selectedArea.getSignature(),
            selectedArea.getColor(),
            selectedArea.getSurfacename()
        );
        return config;
    }

    private List<AreaData.Vertex> calculateAABB(List<Double[]> vertices) {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Double[] v : vertices) {
            minX = Math.min(minX, v[0]); maxX = Math.max(maxX, v[0]);
            minZ = Math.min(minZ, v[1]); maxZ = Math.max(maxZ, v[1]);
        }
        List<AreaData.Vertex> sv = new ArrayList<>();
        sv.add(new AreaData.Vertex(Math.round(minX), Math.round(minZ)));
        sv.add(new AreaData.Vertex(Math.round(maxX), Math.round(minZ)));
        sv.add(new AreaData.Vertex(Math.round(maxX), Math.round(maxZ)));
        sv.add(new AreaData.Vertex(Math.round(minX), Math.round(maxZ)));
        return sv;
    }

    // ===== 闂備礁鎲￠崹瑙勬叏瀹曞洨绀婄€广儱鎷嬮崯鍛存煏婢跺牆鍔氱€电増妫冮弻锟犲磼濠垫劖缍堢紒?=====

    private boolean isPointInPolygon(Double[] point, List<Double[]> polygon) {
        if (polygon.size() < 3) return false;
        double x = point[0], y = point[1];
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            double xi = polygon.get(i)[0], yi = polygon.get(i)[1];
            double xj = polygon.get(j)[0], yj = polygon.get(j)[1];
            if (((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi))
                inside = !inside;
        }
        return inside;
    }

    private Double[] lineIntersection(Double[] a, Double[] b, Double[] c, Double[] d) {
        double x1=a[0],y1=a[1], x2=b[0],y2=b[1], x3=c[0],y3=c[1], x4=d[0],y4=d[1];
        double denom = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
        if (Math.abs(denom) < 1e-10) return null;
        double t = ((x1-x3)*(y3-y4) - (y1-y3)*(x3-x4)) / denom;
        double u = -((x1-x2)*(y1-y3) - (y1-y2)*(x1-x3)) / denom;
        if (t >= 0 && t <= 1 && u >= 0 && u <= 1)
            return new Double[]{(double)Math.round(x1+t*(x2-x1)), (double)Math.round(y1+t*(y2-y1))};
        return null;
    }

    private Double[] closestPointOnSegment(Double[] p, Double[] a, Double[] b) {
        double dx=b[0]-a[0], dy=b[1]-a[1];
        if (dx==0 && dy==0) return new Double[]{a[0], a[1]};
        double t = Math.max(0, Math.min(1, ((p[0]-a[0])*dx + (p[1]-a[1])*dy) / (dx*dx+dy*dy)));
        return new Double[]{a[0]+t*dx, a[1]+t*dy};
    }

    private double distance(Double[] a, Double[] b) {
        double dx=a[0]-b[0], dy=a[1]-b[1];
        return Math.sqrt(dx*dx+dy*dy);
    }

    // ===== 闂備礁鎼ˇ顖炲疮閺夋埈鐎舵繛宸簻濡炰粙鎮橀悙闈涗壕婵炲牆鐖奸弻鐔烘嫚閳ヨ櫕鐏堟繝娈垮枤閸嬬偛顭?=====

    private List<AreaData> getModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        if (client.player == null) return result;
        String playerName = client.player.getGameProfile().getName();
        boolean isAdmin = client.player.hasPermissionLevel(2);
        List<AreaData> all = loadAllAreas();
        for (AreaData area : all) {
            if (isAdmin) { result.add(area); continue; }
            if (playerName.equals(area.getSignature())) { result.add(area); continue; }
            if (area.getBaseName() != null) {
                for (AreaData base : all) {
                    if (base.getName().equals(area.getBaseName()) && playerName.equals(base.getSignature())) {
                        result.add(area); break;
                    }
                }
            }
        }
        return result;
    }

    private List<AreaData> loadAllAreas() {
        List<AreaData> areas = new ArrayList<>();
        try {
            if (client.world != null) {
                String dim = client.world.getRegistryKey().getValue().toString();
                String fn = getFileNameForDimension(dim);
                if (fn != null) {
                    Path p = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fn);
                    if (p.toFile().exists()) areas = FileManager.readAreaData(p);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return areas;
    }

    private String getFileNameForDimension(String dimId) {
        if (dimId == null) return null;
        if (dimId.contains("overworld")) return areahint.Areashint.OVERWORLD_FILE;
        if (dimId.contains("nether")) return areahint.Areashint.NETHER_FILE;
        if (dimId.contains("end")) return areahint.Areashint.END_FILE;
        return null;
    }

    private void sendMsg(String msg, Formatting fmt) {
        if (client.player != null)
            client.player.sendMessage(areahint.util.TextCompat.literal(msg).formatted(fmt), false);
    }

    public void reset() {
        state = State.IDLE;
        isActive = false;
        isRecording = false;
        selectedArea = null;
        newVertices.clear();
        area1Vertices = null;
        area2Vertices = null;
        area1Config = null;
        area2Config = null;
    }
}
