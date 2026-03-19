package areahint.expandarea;

import areahint.data.AreaData;
import areahint.i18n.I18nManager;
import areahint.util.AreaDataConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * ExpandArea閻劍鍩涢悾宀勬桨缁崵绮?
 * 娴ｈ法鏁ら懕濠傘亯濞戝牊浼呴崪灞藉讲閻愮懓鍤紒鍕鐎圭偟骞囨禍銈勭鞍閿涘牏琚导绯哸syAdd閿?
 */
public class ExpandAreaUI {
    private final ExpandAreaManager manager;
    private final MinecraftClient client;
    
    public ExpandAreaUI(ExpandAreaManager manager) {
        this.manager = manager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 閺勫墽銇氶崺鐔锋倳闁瀚ㄩ悾宀勬桨閿涘牅濞囬悽銊ㄤ喊婢垛晜绉烽幁顖ょ礆
     */
    public void showAreaSelection(List<AreaData> areas) {
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.title.area.expand")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.prompt.area.expand")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        if (areas.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.error.area.expand_4")), false);
            MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint expandarea cancel"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.cancel.expand"))))
                    .withColor(Formatting.RED));
            client.player.sendMessage(cancelButton, false);
            return;
        }
        
        // 閺勫墽銇氶崺鐔锋倳閸掓銆冮幐澶愭尦
        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            String signature = area.getSignature();
            
            MutableText areaButton = areahint.util.TextCompat.literal("鎼?[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
                        "/areahint expandarea select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.expand") + displayName + I18nManager.translate("addhint.message.general") + signature)))
                    .withColor(Formatting.GOLD));
            
            client.player.sendMessage(areaButton, false);
        }
        
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        // 閺勫墽銇氶崣鏍ㄧХ閹稿鎸?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint expandarea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.cancel.expand"))))
                .withColor(Formatting.RED));
        
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 閺勫墽銇氱拋鏉跨秿閻ｅ矂娼伴敍鍫熸▔缁€鐑樺絹缁€鐑樼Х閹垽绱?
     */
    public void showRecordingInterface() {
        if (client.player == null) return;

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.vertex.record_3")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.general_2") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("easyadd.message.record_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.vertex.record_2")), false);
    }
    
    /**
     * 閺勫墽銇氶崸鎰垼閻愮顔囪ぐ鏇炴倵閻ㄥ嫰鈧銆嶉悾宀勬桨
     */
    public void showPointRecordedOptions(int vertexCount) {
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.record") + vertexCount + I18nManager.translate("dividearea.message.coordinate")), false);
        
        // 閺勫墽銇氶幙宥勭稊闁銆?
        net.minecraft.text.MutableText continueButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.button.record.continue"))
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint expandarea continue"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("dividearea.message.coordinate.record.continue"))))
                .withColor(net.minecraft.util.Formatting.GREEN));
        
        net.minecraft.text.MutableText saveButton = areahint.util.TextCompat.literal(I18nManager.translate("expandarea.button.area.save"))
            .setStyle(net.minecraft.text.Style.EMPTY
                .withClickEvent(new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/areahint expandarea save"))
                .withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.area.save.expand"))))
                .withColor(net.minecraft.util.Formatting.AQUA));
        
        if (vertexCount >= 3) {
            // 閺堝鍐绘径鐔烘畱閻愮櫢绱濋弰鍓с仛娣囨繂鐡ㄩ柅澶愩€?
            net.minecraft.text.MutableText buttonRow = areahint.util.TextCompat.empty()
                .append(continueButton)
                .append(areahint.util.TextCompat.of("  "))
                .append(saveButton);
            
            client.player.sendMessage(buttonRow, false);
        } else {
            // 閻愯鏆熸稉宥咁檮閿涘苯褰ч弰鍓с仛缂佈呯敾
            client.player.sendMessage(continueButton, false);
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.message.area.save")), false);
        }
    }
    
    
    /**
     * 閺勫墽銇氶崣鏍ㄧХ绾喛顓诲☉鍫熶紖
     */
    public void showCancelMessage() {
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("expandarea.error.area.cancel.expand")), false);
    }
} 