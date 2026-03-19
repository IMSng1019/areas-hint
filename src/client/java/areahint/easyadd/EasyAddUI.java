package areahint.easyadd;

import areahint.data.AreaData;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * EasyAdd闁活潿鍔嶉崺娑㈡偩瀹€鍕〃缂侇垵宕电划?
 * 濞达綀娉曢弫銈夋嚂婵犲倶浜繛鎴濈墛娴煎懘宕仦钘夎闁绘劗鎳撻崵顔剧磼閸曨亝顐介悗鍦仧楠炲洦绂嶉妶鍕瀺
 */
public class EasyAddUI {
    
    /**
     * 闁哄嫬澧介妵姘跺春閻旈攱鍊抽柛姘Ф琚ㄩ弶鍫熸尭閸欏棝鎮惧畝鍕〃
     */
    public static void showNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.title.area.add")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.name_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.general_8")), false);
        
        // 闁哄嫬澧介妵姘跺矗閺嶃劎啸闁圭顦甸幐?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 闁哄嫬澧介妵姘舵嚂閺傛寧鍊ら柛鈺冨枎閹洘娼忛幘鍐插汲闁伙絽鐭傚?
     */
    public static void showSurfaceNameInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.title.area.surface")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.surface")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.area.surface.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.name")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.hint.area.surface")), false);
        
        // 闁哄嫬澧介妵姘跺矗閺嶃劎啸闁圭顦甸幐?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 闁哄嫬澧介妵姘跺春閻旈攱鍊崇紒娑橆槺妤犲洦娼忛幘鍐插汲闁伙絽鐭傚?
     */
    public static void showLevelInputScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.level")), false);

        // 闁告帗绋戠紓鎾剁驳婢跺矂鐛撻梺顐㈩槹鐎氥劑骞愭径鎰唉
        MutableText level1Button = areahint.util.TextCompat.literal(I18nManager.translate("dividearea.button.area"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd level 1"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area_2"))))
                .withColor(Formatting.AQUA));

        MutableText level2Button = areahint.util.TextCompat.literal(I18nManager.translate("dividearea.button.area_3"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd level 2"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area_3"))))
                .withColor(Formatting.YELLOW));

        MutableText level3Button = areahint.util.TextCompat.literal(I18nManager.translate("dividearea.button.area_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd level 3"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area_4"))))
                .withColor(Formatting.LIGHT_PURPLE));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));
        
        // 缂備礁瀚幃搴ㄥ箰婢舵劖灏﹂柡鍕⒔閵?
        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(level1Button)
            .append(areahint.util.TextCompat.of("  "))
            .append(level2Button)
            .append(areahint.util.TextCompat.of("  "))
            .append(level3Button)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.level.description")), false);
    }
    
    /**
     * 闁哄嫬澧介妵姘▔婵犲嫰鐛撻柛鈺冨枎閹洟鏌呮径瀣仴闁伙絽鐭傚?
     */
    public static void showBaseSelectScreen(List<AreaData> availableParentAreas) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.area.parent_2")), false);

        for (AreaData area : availableParentAreas) {
            String displayName = areahint.util.AreaDataConverter.getDisplayName(area);
            MutableText areaButton = areahint.util.TextCompat.literal("閹?[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint easyadd base \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName + I18nManager.translate("easyadd.message.area.parent"))))
                    .withColor(Formatting.GOLD));
            
            client.player.sendMessage(areaButton, false);
        }
        
        // 闁哄嫬澧介妵姘跺矗閺嶃劎啸闁圭顦甸幐?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(cancelButton, false);
    }

    /**
     * 闁哄嫬澧介妵姘跺锤閹邦厾鍨奸柣鎰攰椤斿洩銇愰弴鐐村€甸柣銊ュ閺咁偊妫?
     */
    public static void showPointRecordedScreen(List<BlockPos> recordedPoints, BlockPos lastPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.record") + recordedPoints.size() + I18nManager.translate("easyadd.message.record.point")), false);

        // 闁哄嫬澧介妵姘跺箼瀹ュ嫮绋婇梺顐㈩樀閵?
        MutableText continueButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.button.record.continue"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd continue"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.coordinate.record.continue"))))
                .withColor(Formatting.GREEN));

        MutableText finishButton = areahint.util.TextCompat.literal(I18nManager.translate("easyadd.button.record.finish"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd finish"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.coordinate.record.confirm"))))
                .withColor(Formatting.AQUA));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));
        
        if (recordedPoints.size() >= 3) {
            // 闁哄牆顦抽崘缁樺緞閻旂儤鐣遍柣鎰缁辨繈寮伴崜褋浠涢悗鐟版湰閸ㄦ岸鏌呮径鎰┾偓?
            MutableText buttonRow = areahint.util.TextCompat.empty()
                .append(continueButton)
                .append(areahint.util.TextCompat.of("  "))
                .append(finishButton)
                .append(areahint.util.TextCompat.of("  "))
                .append(cancelButton);
            
            client.player.sendMessage(buttonRow, false);
        } else {
            // 闁绘劘顫夐弳鐔哥▔瀹ュ拋妾柨娑樿嫰瑜把囧及閸撗佷粵缂備綀鍛暰闁告粌鑻ぐ鍥р槈?
            MutableText buttonRow = areahint.util.TextCompat.empty()
                .append(continueButton)
                .append(areahint.util.TextCompat.of("  "))
                .append(cancelButton);
            
            client.player.sendMessage(buttonRow, false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.record.finish")), false);
        }
    }
    
    /**
     * 闁哄嫬澧介妵姘辨兜椤旀鍚囧ǎ鍥ㄧ箓閻°劑鎮惧畝鍕〃
     */
    public static void showConfirmSaveScreen(AreaData areaData) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.title.area.confirm")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.name_2") + areaData.getName()), false);
        if (areaData.getSurfacename() != null && !areaData.getSurfacename().trim().isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.surface_2") + areaData.getSurfacename()), false);
        }
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.level") + areaData.getLevel()), false);
        
        if (areaData.getBaseName() != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.parent_2") + areaData.getBaseName()), false);
        } else {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.area.parent_3")), false);
        }
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.vertex") + areaData.getVertices().size() + I18nManager.translate("gui.message.general")), false);
        
        if (areaData.getAltitude() != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.altitude_12") +
                areaData.getAltitude().getMin() + " ~ " + areaData.getAltitude().getMax()), false);
        }
        
        // 闁哄嫬澧介妵姘紣濠婂棗顥忓ǎ鍥ｅ墲娴煎懘鏁嶉崼鐔哥厐濠⒀呭剳缁?
        String colorHex = areaData.getColor();
        String colorDisplay = colorHex != null ? colorHex : "#FFFFFF";
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.color_2") + colorDisplay), false);

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_6") + areaData.getSignature()), false);
        
        // 闁哄嫬澧介妵姘辨兜椤旀鍚囬柛婊冭嫰瑜板洤鈽夐崼鐔风樆闂?
        MutableText saveButton = areahint.util.TextCompat.literal(I18nManager.translate("easyadd.button.area.save"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd save"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.save.confirm"))))
                .withColor(Formatting.GREEN));

        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));
        
        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(saveButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.save.confirm")), false);
    }
    
    /**
     * 闁哄嫬澧介妵姘殗濡搫顔婇梺顐㈩槹鐎氥劑鎮惧畝鍕〃
     */
    public static void showAltitudeSelectionScreen(List<BlockPos> recordedPoints) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 閻犱緤绱曢悾鏄忋亹閹惧啿顤呴柛褎鍔栭悥锝夋倷閸︻厽鐣卞Δ鍌浢€规娊鎳犻崘銊︾函闁活潿鍔嬬花顒勫及閸撗佷粵
        int minY = recordedPoints.stream().mapToInt(BlockPos::getY).min().orElse(0);
        int maxY = recordedPoints.stream().mapToInt(BlockPos::getY).max().orElse(0);
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.title.altitude")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.prompt.altitude_3")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.altitude.record") + minY + " ~ " + maxY), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        // 闁煎浜滄慨鈺冩媼閿涘嫮鏆柟绋款樀閹?
        MutableText autoButton = areahint.util.TextCompat.literal(I18nManager.translate("easyadd.button.altitude"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd altitude auto"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.altitude.record.expand_2"))))
                .withColor(Formatting.AQUA));
        
        // 闁煎浜滈悾鐐▕婢跺鐦婚梺?
        MutableText customButton = areahint.util.TextCompat.literal(I18nManager.translate("command.button.altitude"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd altitude custom"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("easyadd.prompt.altitude_7"))))
                .withColor(Formatting.LIGHT_PURPLE));
        
        // 濞戞挸绉瑰娲礆閸洜褰幖杈鹃檮鐎垫粓鏌?
        MutableText unlimitedButton = areahint.util.TextCompat.literal(I18nManager.translate("command.button.altitude_2"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd altitude unlimited"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    areahint.util.TextCompat.of(I18nManager.translate("command.message.area.altitude.coordinate"))))
                .withColor(Formatting.YELLOW));
        
        // 闁告瑦鐗楃粔鐑藉箰婢舵劖灏?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("command.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));
        MutableText buttonRow = areahint.util.TextCompat.empty()
            .append(autoButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(customButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(unlimitedButton)
            .append(areahint.util.TextCompat.of("  "))
            .append(cancelButton);
        
        client.player.sendMessage(buttonRow, false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.altitude.record.expand")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("command.message.area.altitude.boundary")), false);
    }
    
    /**
     * 闁哄嫬澧介妵姘紣濠婂棗顥忛梺顐㈩槹鐎氥劑鎮惧畝鍕〃
     */
    public static void showColorSelectionScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.title.area.color")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("dividearea.prompt.area.color")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.area.color")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        // 缂佹鍏涚粩瀵告偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row1 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_24"), "#FFFFFF", "§f"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_23"), "#808080", "§7"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_18"), "#555555", "§8"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_31"), "#000000", "§0"));
        
        // 缂佹鍏涚花鈺冩偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row2 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_19"), "#AA0000", "§4"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_27"), "#FF0000", "§c"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_25"), "#FF55FF", "§d"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_17"), "#FFAA00", "§6"));
        
        // 缂佹鍏涚粭浣烘偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row3 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_30"), "#FFFF55", "§e"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_28"), "#55FF55", "§a"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_20"), "#00AA00", "§2"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_13"), "#55FFFF", "§b"));
        
        // 缂佹鍓欏ú鎾舵偘瀹€鍕垫澒闁肩懓寮剁€垫粓鏌?
        MutableText row4 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_22"), "#00AAAA", "§3"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_29"), "#5555FF", "§9"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_21"), "#0000AA", "§1"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_26"), "#AA00AA", "§5"));
        // 闂傚偆浜為崕濠囧极閸喓浜柟绋款樀閹稿磭鎮?
        MutableText row5 = areahint.util.TextCompat.empty()
            .append(createColorButton(I18nManager.translate("gui.message.general_15"), "FLASH_BW_ALL", "§7"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_14"), "FLASH_RAINBOW_ALL", "§b"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_12"), "FLASH_BW_CHAR", "§8"))
            .append(areahint.util.TextCompat.of("  "))
            .append(createColorButton(I18nManager.translate("gui.message.general_11"), "FLASH_RAINBOW_CHAR", "§d"));

        // 闁告瑦鐗楃粔鐑藉箰婢舵劖灏?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("command.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint easyadd cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("easyadd.message.cancel_2"))))
                .withColor(Formatting.RED));

        client.player.sendMessage(row1, false);
        client.player.sendMessage(row2, false);
        client.player.sendMessage(row3, false);
        client.player.sendMessage(row4, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("gui.message.general_4")), false);
        client.player.sendMessage(row5, false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 闁告帗绋戠紓鎾达紣濠婂棗顥忛梺顐㈩槹鐎氥劑骞愭径鎰唉
     */
    private static MutableText createColorButton(String colorName, String colorValue, String minecraftColor) {
        String command = colorValue.equals("custom") ? 
            "/areahint easyadd color custom" : 
            "/areahint easyadd color " + colorValue;
        
        return areahint.util.TextCompat.literal(minecraftColor + "[" + colorName + "]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + colorName + I18nManager.translate("dividearea.message.area.color")))));
    }
    
    /**
     * 闁哄嫬澧介妵姘舵煥濞嗘帩鍤栨繛鎴濈墛娴?
     */
    public static void showError(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("easyadd.error.general_2") + message), false);
        }
    }
    
    /**
     * 闁哄嫬澧介妵姘跺箣閹邦剙顫犳繛鎴濈墛娴?
     */
    public static void showSuccess(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of("閹间繘" + message), false);
        }
    }
    
    /**
     * 闁哄嫬澧介妵姘┍閳╁啩绱栨繛鎴濈墛娴?
     */
    public static void showInfo(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of("閹?" + message), false);
        }
    }
} 