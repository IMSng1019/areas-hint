package areahint.debug;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.i18n.ServerI18nManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 鐠嬪啳鐦粻锛勬倞閸?
 * 閻劋绨粻锛勬倞鐠嬪啳鐦崝鐔诲厴閸滃苯鎮滈悳鈺侇啀閸欐垿鈧浇鐨熺拠鏇氫繆閹?
 */
public class DebugManager {
    // 閸氼垳鏁ょ拫鍐槸閻ㄥ嫮甯虹€圭ΚUID闂嗗棗鎮?
    private static final Set<UUID> debugEnabledPlayers = new HashSet<>();
    
    // 閺勵垰鎯侀張澶夋崲娴ｆ洜甯虹€硅泛鎯庨悽銊ょ啊鐠嬪啳鐦?
    private static boolean anyDebugEnabled = false;
    
    /**
     * 娑撶儤瀵氱€规氨甯虹€硅泛鎯庨悽銊ㄧ殶鐠囨洘膩瀵?
     * @param player 閻溾晛顔?
     * @return 閺勵垰鎯侀幋鎰閸氼垳鏁?
     */
    public static boolean enableDebug(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        boolean wasEnabled = debugEnabledPlayers.contains(playerUUID);
        
        if (!wasEnabled) {
            debugEnabledPlayers.add(playerUUID);
            anyDebugEnabled = true;
            Areashint.LOGGER.info(ServerI18nManager.translate("debug.message.general"), player.getName().getString());
            sendTranslatableDebugMessage(player, "debug.hint.general", Formatting.GREEN);
        } else {
            sendTranslatableDebugMessage(player, "debug.message.general_5", Formatting.YELLOW);
        }
        
        return !wasEnabled;
    }
    
    /**
     * 娑撶儤瀵氱€规氨甯虹€瑰墎顩﹂悽銊ㄧ殶鐠囨洘膩瀵?
     * @param player 閻溾晛顔?
     * @return 閺勵垰鎯侀幋鎰缁備胶鏁?
     */
    public static boolean disableDebug(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        boolean wasEnabled = debugEnabledPlayers.remove(playerUUID);
        
        // 閺囧瓨鏌婇崗銊ョ湰鐠嬪啳鐦悩鑸碘偓?
        anyDebugEnabled = !debugEnabledPlayers.isEmpty();
        
        if (wasEnabled) {
            Areashint.LOGGER.info(ServerI18nManager.translate("debug.message.general_2"), player.getName().getString());
            sendTranslatableDebugMessage(player, "debug.hint.general_2", Formatting.YELLOW);
        } else {
            sendTranslatableDebugMessage(player, "debug.message.general_6", Formatting.RED);
        }
        
        return wasEnabled;
    }
    
    /**
     * 濡偓閺屻儲妲搁崥锔芥箒娴犺缍嶉悳鈺侇啀閸氼垳鏁ゆ禍鍡氱殶鐠?
     * @return 閺勵垰鎯侀張澶夋崲娴ｆ洜甯虹€硅泛鎯庨悽銊ょ啊鐠嬪啳鐦?
     */
    public static boolean isAnyDebugEnabled() {
        return anyDebugEnabled;
    }
    
    /**
     * 濡偓閺屻儲瀵氱€规氨甯虹€硅埖妲搁崥锕€鎯庨悽銊ょ啊鐠嬪啳鐦?
     * @param playerUUID 閻溾晛顔峌UID
     * @return 閺勵垰鎯侀崥顖滄暏娴滃棜鐨熺拠?
     */
    public static boolean isDebugEnabled(UUID playerUUID) {
        return debugEnabledPlayers.contains(playerUUID);
    }
    
    /**
     * 閸氭垵鎯庨悽銊ㄧ殶鐠囨洜娈戦悳鈺侇啀閸欐垿鈧浇鐨熺拠鏇氫繆閹?
     * @param category 鐠嬪啳鐦猾璇插焼
     * @param message 鐠嬪啳鐦穱鈩冧紖
     */
    public static void sendDebugInfo(DebugCategory category, String message) {
        if (!anyDebugEnabled) {
            return; // 婵″倹鐏夊▽鈩冩箒閻溾晛顔嶉崥顖滄暏鐠嬪啳鐦敍宀€娲块幒銉ㄧ箲閸ョ儑绱濋柆鍨帳娑撳秴绻€鐟曚胶娈戞径鍕倞
        }
        
        // 鐠佹澘缍嶉崚鐗堟箛閸旓紕顏弮銉ョ箶
        Areashint.LOGGER.debug("[{}] {}", category.name(), message);
        
        // 闁秴宸婚幍鈧張澶婃儙閻劏鐨熺拠鏇犳畱閻溾晛顔?
        for (UUID playerUUID : debugEnabledPlayers) {
            ServerPlayerEntity player = Areashint.getServer().getPlayerManager().getPlayer(playerUUID);
            if (player != null && player.isAlive()) {
                String formattedMessage = String.format("[%s] %s", category.name(), message);
                sendDebugMessage(player, formattedMessage, category.getFormatting());
            }
        }
    }
    
    /**
     * 閸氭垹甯虹€硅泛褰傞柅浣哥敨閺嶇厧绱￠惃鍕殶鐠囨洘绉烽幁?
     * @param player 閻溾晛顔?
     * @param message 濞戝牊浼?
     * @param formatting 閺嶇厧绱?
     */
    private static void sendDebugMessage(ServerPlayerEntity player, String message, Formatting formatting) {
        Text text = TextCompat.translatable("debug.button.general").formatted(Formatting.GOLD)
                .append(TextCompat.literal(message).formatted(formatting));
        player.sendMessage(text, false);
    }

    private static void sendTranslatableDebugMessage(ServerPlayerEntity player, String key, Formatting formatting) {
        Text text = TextCompat.literal(ServerI18nManager.translateForPlayer(player.getUuid(), "debug.button.general")).formatted(Formatting.GOLD)
                .append(TextCompat.literal(ServerI18nManager.translateForPlayer(player.getUuid(), key)).formatted(formatting));
        player.sendMessage(text, false);
    }

    public static void sendTranslatableDebugInfo(DebugCategory category, String key) {
        if (!anyDebugEnabled) return;
        for (UUID playerUUID : debugEnabledPlayers) {
            ServerPlayerEntity player = Areashint.getServer().getPlayerManager().getPlayer(playerUUID);
            if (player != null && player.isAlive()) {
                Text text = TextCompat.literal(ServerI18nManager.translateForPlayer(playerUUID, "debug.button.general")).formatted(Formatting.GOLD)
                        .append(TextCompat.literal("[" + category.name() + "] ").formatted(category.getFormatting()))
                        .append(TextCompat.literal(ServerI18nManager.translateForPlayer(playerUUID, key)).formatted(category.getFormatting()));
                player.sendMessage(text, false);
            }
        }
    }
    
    /**
     * 鐠嬪啳鐦猾璇插焼閺嬫矮濡?
     */
    public enum DebugCategory {
        AREA_DETECTION(Formatting.AQUA),
        PLAYER_POSITION(Formatting.GREEN),
        CONFIG(Formatting.YELLOW),
        NETWORK(Formatting.LIGHT_PURPLE),
        RENDER(Formatting.BLUE),
        COMMAND(Formatting.WHITE),
        GENERAL(Formatting.GRAY);
        
        private final Formatting formatting;
        
        DebugCategory(Formatting formatting) {
            this.formatting = formatting;
        }
        
        public Formatting getFormatting() {
            return formatting;
        }
    }
} 