package areahint.command;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.i18n.I18nManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * 妯＄粍寮€鍏冲懡浠ゅ鐞嗗櫒
 * 澶勭悊瀹㈡埛绔殑 /areahint on 鍜?/areahint off 鍛戒护
 */
public class ModToggleCommand {
    
    /**
     * 澶勭悊妯＄粍寮€鍏冲懡浠?
     * @param action 鍛戒护鍙傛暟锛?on" 鎴?"off"锛?
     */
    public static void handleToggleCommand(String action) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (action == null || action.trim().isEmpty()) {
            // 濡傛灉娌℃湁鍙傛暟锛屾樉绀哄綋鍓嶇姸鎬?
            showCurrentStatus(client);
            return;
        }
        
        String command = action.trim().toLowerCase();
        
        switch (command) {
            case "on":
                enableMod(client);
                break;
            case "off":
                disableMod(client);
                break;
            default:
                showUsage(client);
                break;
        }
    }
    
    /**
     * 鍚敤妯＄粍
     * @param client Minecraft瀹㈡埛绔疄渚?
     */
    private static void enableMod(MinecraftClient client) {
        ClientConfig.setEnabled(true);
        
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.literal(I18nManager.translate("command.button.area")), false);
        }
        
        AreashintClient.LOGGER.info("妯＄粍宸查€氳繃鍛戒护鍚敤");
    }
    
    /**
     * 绂佺敤妯＄粍
     * @param client Minecraft瀹㈡埛绔疄渚?
     */
    private static void disableMod(MinecraftClient client) {
        ClientConfig.setEnabled(false);
        
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.literal(I18nManager.translate("command.error.area")), false);
        }
        
        AreashintClient.LOGGER.info("妯＄粍宸查€氳繃鍛戒护绂佺敤");
    }
    
    /**
     * 鏄剧ず褰撳墠妯＄粍鐘舵€?
     * @param client Minecraft瀹㈡埛绔疄渚?
     */
    private static void showCurrentStatus(MinecraftClient client) {
        boolean enabled = ClientConfig.isEnabled();
        String status = enabled ? I18nManager.translate("command.message.general_12") : I18nManager.translate("command.error.general_11");
        
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.literal(String.format(I18nManager.translate("command.button.general"), status)), false);
        }
    }
    
    /**
     * 鏄剧ず浣跨敤鏂规硶
     * @param client Minecraft瀹㈡埛绔疄渚?
     */
    private static void showUsage(MinecraftClient client) {
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.literal(I18nManager.translate("command.button.general_2")), false);
            client.player.sendMessage(areahint.util.TextCompat.literal("搂7/areahint on  搂f- " + I18nManager.translate("command.message.general_26")), false);
            client.player.sendMessage(areahint.util.TextCompat.literal("搂7/areahint off 搂f- " + I18nManager.translate("command.message.general_27")), false);
        }
    }
} 