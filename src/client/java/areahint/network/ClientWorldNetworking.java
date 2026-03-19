package areahint.network;

import areahint.AreashintClient;
import areahint.world.ClientWorldFolderManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * 闁诲骸绠嶉崹娲春濞戞氨鍗氭い鏍ㄧ懅閻燁垶鏌ｉ敐鍛伇缂傚秴顦辩槐鎺戭煥閸愮偓袩闂?
 * 闁荤姵鍔楅崰鏇㈡儗濡ゅ啯瀚氶梺鍨儑濠€鎾煕濠婂啯婀伴悽顖涙尦瀵劑鎳滈崹顐ょ杸闂佸憡妫戠槐鏇㈩敂椤掑嫭鍎嶉柛鏇ㄤ簽閻燁垶鏌ｉ敐鍛殭濞ｅ洤锕獮?
 */
public class ClientWorldNetworking {
    
    // 缂傚倸鍟崹鍦垝閸洖绀岄柛娑卞枤閸ㄥジ鎮归崶褎顥犳い?
    public static final String C2S_REQUEST_WORLD_INFO = "areashint:request_world_info";
    public static final String S2C_WORLD_INFO = "areashint:world_info";
    
    /**
     * 闂佸憡甯楃换鍌烇綖閹版澘绀岄柡宓苯鎮呴梺瑙勬尦椤ユ捇顢旈浣衡枖闁哄稁鍋呭▍鏇犵磽閸愨晛鐏╃紒顔垮煐瀵板嫰宕熼鐔封偓?
     */
    public static void init() {
        // 濠电偛顦崝宀勫船閼恒儳鈻旈柡宥庡亝濞呮洖菐閸ワ絽澧插ù鐓庢嚇楠炴帡濡烽敂鑺ユ瘑婵犮垼娉涚€氼噣骞冩繝鍥ч棷?
        ClientPlayNetworking.registerGlobalReceiver(
            new Identifier(S2C_WORLD_INFO),
            ClientWorldNetworking::handleWorldInfo
        );
        
        AreashintClient.LOGGER.info("Starting client world networking sync");
    }
    
    /**
     * 闂佸憡纰嶉崹璺猴耿閸ヮ剙绀夐柨娑樺娴煎倿鎮归崶顒佹暠闁活亞澧楃粙澶愬冀椤愶絾鐝繛锝呮礌閸撴繃瀵?
     */
    public static void requestWorldInfo() {
        try {
            if (ClientPlayNetworking.canSend(new Identifier(C2S_REQUEST_WORLD_INFO))) {
                PacketByteBuf buf = PacketByteBufs.create();
                // 闁荤姴娲弨閬嶆儑娴兼潙绀岄柛娑卞墰閻熸繈姊婚崶锝呬壕闁荤喐娲戝鎺楀箲閵忊剝濯撮柡鍥╁枑濞堝爼鏌?
                ClientPlayNetworking.send(new Identifier(C2S_REQUEST_WORLD_INFO), buf);
                
                AreashintClient.LOGGER.info("Requested world info from server");
            } else {
                AreashintClient.LOGGER.warn("Cannot request world info packet; server may not support it");
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("Failed to request world info", e);
        }
    }
    
    /**
     * 婵犮垼娉涚€氼噣骞冩繝鍥х闁靛闄勯弳顏堟煕閹烘挸顥嬫繛鍫熷灦缁嬪寮介锝嗙彧婵烇絽娲犻崜婵囧?
     * @param client Minecraft闁诲骸绠嶉崹娲春濞戞氨鍗氭い鏍ㄦ皑閺夎棄銆?
     * @param handler 缂傚倸鍟崹鍦垝閼搁潧绶為柛鏇ㄥ幗閸婄偤鏌?
     * @param buf 闂佽桨鑳舵晶妤€鐣垫笟鈧畷鐘诲川椤栨粌顦╅梺鍛婂姈瑜板啰浜?
     * @param responseSender 闂佸憡绻傜粔瀵歌姳閺屻儱鐭楅柟杈捐吂閸嬫挻鎷呴崨濠勫綔
     */
    private static void handleWorldInfo(MinecraftClient client,
                                       ClientPlayNetworkHandler handler,
                                       PacketByteBuf buf,
                                       PacketSender responseSender) {
        try {
            String worldName = buf.readString();
            
            // 闂侀潻璐熼崝瀣偓闈涚灱閻ヮ亞鎹勯妸銏＄厾婵炴垶鎼╅崢濂杆囬埡鍛仩闁糕剝娲滈悷顖炴煟閿濆懎顨欏ǎ鍥э躬楠?
            client.execute(() -> {
                try {
                    AreashintClient.LOGGER.info("闂佽　鍋撻悹鍝勬惈閻撳倿鏌￠崼婵埿㈠┑顔惧枔缁晠顢涘▎鎴犳噽闂佷紮绲芥總鏃€绌辨繝鍥х畳? '{}'", worldName);
                    AreashintClient.LOGGER.info("閻庢鍠掗崑鎾斥攽椤旂⒈鍎忛柣锝庡墴楠炲骞囬鍡欐噽闂佷紮绲介張顒勫几閸愨晝顩烽悹鐑樹航娴犳岸鏌涢幒鎾剁畵妞ゎ偅鍔欏畷?..");
                    
                    // 闁诲海鎳撻張顒勫垂濮橆厾鈻旈柡宥庡亝濞呮洟鏌￠崒姘煑婵炲棎鍨哄鍕偡閺夎法浠存繝娈垮枛椤戝懐鈧?
                    ClientWorldFolderManager.finalizeWorldInitialization(worldName);
                    
                    // 闂備焦褰冪粔鐢稿蓟婵犲洤绀夐柣妯煎劋缁佹壆鎲搁悧鍫熷碍濠⒀呭█瀹曠娀宕崟顓炲箥闂佽桨鑳舵晶妤€鐣?
                    reloadCurrentAreaData();
                    
                } catch (Exception e) {
                    AreashintClient.LOGGER.error("Failed to handle world info", e);
                }
            });
            
        } catch (Exception e) {
            AreashintClient.LOGGER.error("Failed to request current world info", e);
        }
    }
    
    /**
     * 闂備焦褰冪粔鐢稿蓟婵犲洤绀夐柣妯煎劋缁佹壆鎲搁悧鍫熷碍濠⒀呭█瀹曠娀宕崟顓炲箥闂佽桨鑳舵晶妤€鐣?
     * 闂侀潻璐熼崝瀣箔濮椻偓閹墽浠﹂悙顒傗偓顔济归悩鐑樼【闁靛洦鐟╁畷姘攽閸♀晜缍忛梺鍛婄墬閻楁洟鎮鹃鍕闁归偊鍓氶崐鐢告偣鐎ｎ亜鏆熼柡?
     */
    private static void reloadCurrentAreaData() {
        try {
            // 闂佸吋鍎抽崲鑼躲亹閸ヮ亗浜归柟鎯у暱椤ゅ懘鎮楅獮鍨仾闁糕晜绋撶划鈺咁敍濮樿鲸婢栨繛?
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
                // 閻庢鍠栭幖顐﹀春濡ゅ懏鐓傜€广儱妫欓悡鈧梺鍛婃⒒婵儳霉閸ヮ剙绀岄柛婵嗗閸樼敻鏌℃担鍝勵暭鐎?
                String dimensionId = client.world.getRegistryKey().getValue().toString();
                String dimensionFileName = getDimensionFileName(dimensionId);
                
                AreashintClient.LOGGER.info("缂傚倸鍟崹鍦垝閸洖瑙﹂悘鐐佃檸閸斿嫰鎮楅悷鐗堟拱闁搞劍宀搁弫宥呯暆閸曨偅顏熼梺鍝勫€瑰妯绘叏閻愬瓨濮滈柦妯侯槸闂呮﹢鏌涢埡鍐ㄦ瀾闁哄棛鍠栭獮? {}", dimensionFileName);
                
                // 闁荤姴顑呴崯浼村极椤ヮ湼eashintClient闂佹眹鍔岀€氫即宕抽幖浣告闁绘鐗滃鐐箾閺夋埈鍎愰柡宀€鍠庨埢?
                AreashintClient.forceRedetectCurrentArea();
                
            }
        } catch (Exception e) {
            AreashintClient.LOGGER.error("Failed to reload current area data", e);
        }
    }
    
    /**
     * 闂佸搫绉烽～澶婄暤娴ｈ櫣纾奸柡澶嬪灥椤斿D闂佸吋鍎抽崲鑼躲亹閸ヮ剙妫橀柛銉檮椤愪粙鏌?
     * @param dimensionId 缂傚倷鐒﹀娆戔偓鍦吋D
     * @return 闂佸搫鍊稿ú锝呪枎閵忋倕瑙?
     */
    private static String getDimensionFileName(String dimensionId) {
        if (dimensionId.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (dimensionId.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (dimensionId.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return areahint.Areashint.OVERWORLD_FILE; // 婵帗绋掗…鍫ヮ敇缂佹ɑ浜ら柡鍌涘缁€鈧繛鎴炴崄椤斿﹦绮鈧幃?
    }
} 
