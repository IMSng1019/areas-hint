package areahint.shrinkarea;

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
 * ShrinkArea閻劍鍩涢悾宀勬桨缁崵绮?
 * 娴ｈ法鏁ら懕濠傘亯濞戝牊浼呴崪灞藉讲閻愮懓鍤紒鍕鐎圭偟骞囨禍銈勭鞍閿涘牏琚导绯哸syAdd閿?
 */
public class ShrinkAreaUI {
    private final ShrinkAreaManager manager;
    private final MinecraftClient client;
    
    public ShrinkAreaUI(ShrinkAreaManager manager) {
        this.manager = manager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 閺勫墽銇氶崺鐔锋倳闁瀚ㄩ悾宀勬桨閿涘牅濞囬悽銊ㄤ喊婢垛晜绉烽幁顖ょ礆
     */
    public void showAreaSelectionScreen() {
        if (client.player == null) return;
        
        List<AreaData> areas = manager.getAvailableAreas();
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.title.area.shrink_2")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.prompt.area.shrink")), false);
        
        if (manager.isAdmin()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.area.shrink_3")), false);
        } else {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.area.shrink_2")), false);
        }
        
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        if (areas.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.error.area.shrink_6")), false);
            MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea cancel"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.cancel.shrink"))))
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
                        "/areahint shrinkarea select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.shrink") + displayName + I18nManager.translate("addhint.message.general") + signature + I18nManager.translate("shrinkarea.message.level") + area.getLevel())))
                    .withColor(Formatting.GOLD));
            
            client.player.sendMessage(areaButton, false);
        }
        
        client.player.sendMessage(areahint.util.TextCompat.of(""), false);
        
        // 閺勫墽銇氶崣鏍ㄧХ閹稿鎸?
        MutableText cancelButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.cancel.shrink"))))
                .withColor(Formatting.RED));
        
        client.player.sendMessage(cancelButton, false);
    }
    
    /**
     * 閺勫墽銇氶崸鎰垼閻愮顔囪ぐ鏇炴倵閻ㄥ嫰鈧銆嶉悾宀勬桨
     */
    public void showPointRecordedOptions(int vertexCount) {
        if (client.player == null) return;
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.record") + vertexCount + I18nManager.translate("shrinkarea.message.vertex.shrink")), false);
        
        // 閺勫墽銇氶幙宥勭稊闁銆?
        MutableText continueButton = areahint.util.TextCompat.literal(I18nManager.translate("addhint.button.record.continue"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea continue"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.vertex.record.continue"))))
                .withColor(Formatting.GREEN));
        
        MutableText saveButton = areahint.util.TextCompat.literal(I18nManager.translate("expandarea.button.area.save"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint shrinkarea save"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.message.area.save.shrink"))))
                .withColor(Formatting.AQUA));
        
        if (vertexCount >= 3) {
            // 閺堝鍐绘径鐔烘畱閻愮櫢绱濋弰鍓с仛娣囨繂鐡ㄩ柅澶愩€?
            MutableText buttonRow = areahint.util.TextCompat.empty()
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
        
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("shrinkarea.error.area.cancel.shrink")), false);
    }
} 