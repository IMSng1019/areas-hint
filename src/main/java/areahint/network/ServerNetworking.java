package areahint.network;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.file.FileManager;
import areahint.i18n.ServerI18nManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import static areahint.network.TranslatableMessage.key;
import static areahint.network.TranslatableMessage.lit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;

/**
 * 闂佸搫鐗嗙粔瀛樻叏閻旇櫣鍗氭い鏍ㄧ矌缁夊湱绱撴担鍦煀妞わ腹鏅犻幃鍫曞幢濞嗘帩娼?
 * 婵犮垼娉涚€氼噣骞冩繝鍥у珘鐎广儱鎳庨～銈囩磼閺冩垵鐏熺紒妤€鐬奸埀顒€绠嶉崹娲春濞戞氨鍗氭い鏍ㄧ懅椤撴椽姊婚崒姘煎殶婵炲牊鍨块弻鍛潩椤愶紕绠?
 */
public class ServerNetworking {
    /**
     * 闂佸憡甯楃换鍌烇綖閹版澘绀岄柡宥庡亞缁夊湱绱撴担鍦煀妞わ腹鏅犻幃?
     */
    public static void init() {
        // 濠电偛顦崝宀勫船閻ｅ瞼纾鹃柟瀵稿Х閹规洟姊洪锝呭摵濞存粓绠栧畷顏嗕沪缁涘袩闂佽崵鍋涘Λ妤€鈻?
        Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_67"));
        
        // 濠电偛顦崝宀勫船閻ｅ瞼纾鹃柟瀵稿Х閹规洟鎮归崶顒佹暠闁活亞澧楀鍕礋椤撶喎鈧偤鏌?
        registerNetworkHandlers();
        
        // 濠电偛顦崝宀勫船娴犲鍋濋柍杞扮贰閸熲偓闁哄鏅濋崑鐐垫暜鐎涙顩查悗锝傛櫆椤愪粙鏌ㄥ☉妯垮缂傚秴顑夐幃鎶藉煛娓氬洤鏅欓梺鍛婃⒒婵挳宕ｉ崱娑樺珘鐎广儱鎳庨～銈夋煕閿濆啫濡芥俊鐐插€垮畷锝夊箣閿旂懓浜惧ù锝囶焾闂呮﹢鏌涢埡鍐ㄦ瀾闁哄棛鍠栭獮鎴︻敊閸忕厧顏紓鍌欑劍濞兼瑧鈧濞婂畷娲偄闁垮鈧娊鏌℃担鍝勵暭鐎?
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_28") + player.getName().getString() + ServerI18nManager.translate("message.message.area.dimension"));
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶅褍绉瑰鍨緞鐏炵偓鈻曢柟鑹版彧婵″洤鈻撻幋锕€绀岄柛婵嗗閸樼敻鏌℃担鍝勵暭鐎?
            sendAllAreaDataToClient(player);
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戣儻鍏屾繛锝庡枟閹棃鏁冮埀顒勬偂濞嗘挸瑙︾€广儱顦敮宕囩磽?
            areahint.network.DimensionalNameNetworking.sendDimensionalNamesToClient(player);
            
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_123") + player.getName().getString() + ServerI18nManager.translate("message.message.general_10"));
        });

        // 闂佺粯澹曢弲娑㈩敊瀹ュ妫樻い鎾跺仧绾惧寮堕埡鍐ㄤ沪閻㈩垱鎸冲顕€鎳滈悽娈夸紩闂佽崵鍋涘Λ婊堫敋閵忋垺鍤婇柍褜鍓熷畷鎴ｇ疀閹惧崬浠?
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerI18nManager.removePlayer(handler.getPlayer().getUuid());
        });
    }
    
    /**
     * 闂佸憡纰嶉崹鐢割敇瑜版帒绠ｅ瀣瘨娴煎倿鏌涘▎鎰伌闁逞屽厸缁€浣轰焊椤栫偛鏄ラ柣鏂挎啞濞堝爼鏌?
     * @param player 闂佺儵鏅╅崰妤呮偉閿濆鍋濋柍杞扮贰閸熲偓
     * @param dimensionName 缂傚倷鐒﹀娆戔偓瑙勫▕瀹曘儱顓奸崶鍡欏仱閺佸秹宕搁¨绯磖world闂侀潧妫旀潏娓塭_nether闂侀潧妫旀潏娓塭_end闂?
     */
    public static void sendAreaDataToClient(ServerPlayerEntity player, String dimensionName) {
        try {
            String fileName = Packets.getFileNameForDimension(dimensionName);
            if (fileName == null) {
                Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.dimension.name") + dimensionName);
                return;
            }
            
            // 婵炶揪缍€濞夋洟寮妶鍡欌枖闁哄稁鍋呭▍鏇㈡煛閸屾碍鐭楁繛鍡愬灪瀵板嫰宕烽褍鎮侀梺鑽ゅ仜濡鈻嶉幒妤佸殧鐎瑰嫭婢樼徊鍧楁煛閸屾碍鐭楁繛鍡愬灮閹瑰嫰顢涘杈╃嵁
            Path filePath = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            Areashint.LOGGER.info(ServerI18nManager.translate("message.button.general_15") + filePath.toAbsolutePath());
            
            if (!Files.exists(filePath)) {
                Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.general_69") + filePath);
                // 闁诲繐绻戠换鍡涙儊椤栫偛绀嗘繛鎴烆焽缁憋妇绱掑畝鈧崕銈夊几閸愨晝顩?
                try {
                    FileManager.createEmptyAreaFile(filePath);
                    Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_117") + filePath);
                } catch (IOException e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.general_12") + e.getMessage());
                }
                return;
            }
            
            // 闁荤姴娲╅褑銇愰崶顒€妫橀柛銉檮椤愪粙鏌涢幇顒佸櫣妞?
            String fileContent = Files.readString(filePath);
            Areashint.LOGGER.info(ServerI18nManager.translate("message.button.general_12") + fileContent.length() + ServerI18nManager.translate("message.message.general_12"));
            if (fileContent.length() < 100) {
                Areashint.LOGGER.info(ServerI18nManager.translate("message.button.general_13") + fileContent);
            } else {
                Areashint.LOGGER.info(ServerI18nManager.translate("message.button.general_13") + fileContent.substring(0, 100) + "...");
            }
            
            // 闂佸憡甯楃粙鎴犵磽閹捐鏋侀柣妤€鐗嗙粊锕傛煕?
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeString(dimensionName);
            buffer.writeString(fileContent);
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶉柡鍡欏枛楠炴垿顢欓悡搴ｆ▉
            ServerPlayNetworking.send(player, new Identifier(Packets.S2C_AREA_DATA), buffer);
            
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_123") + player.getName().getString() + ServerI18nManager.translate("message.message.general_8") + dimensionName + ServerI18nManager.translate("message.message.general_19"));
            
        } catch (IOException e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.message.general_70") + e.getMessage());
        }
    }
    
    /**
     * 闂佸憡纰嶉崹鑸垫櫠瀹ュ瀚夊璺侯煬閸氬倿鏌熼幘顕呮妞ゅ浚鍓熷畷锝夊箣閿旂懓浜惧ù锝囶焾闂呮﹢鏌涢埡鍐ㄦ瀾闁哄棛鍠栭獮?
     * @param dimensionName 缂傚倷鐒﹀娆戔偓瑙勫▕瀹曘儱顓奸崶鍡欏仱閺佸秹宕搁¨绯磖world闂侀潧妫旀潏娓塭_nether闂侀潧妫旀潏娓塭_end闂?
     */
    public static void sendAreaDataToAll(String dimensionName) {
        if (Areashint.getServer() == null) {
            return;
        }
        
        List<ServerPlayerEntity> players = Areashint.getServer().getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            sendAreaDataToClient(player, dimensionName);
        }
    }
    
    /**
     * 闂佸憡纰嶉崹鑸垫櫠瀹ュ瀚夊璺侯煬閸氬倿鏌熼幘顕呮妞ゅ浚鍓熷畷锝夊箣閿旂懓浜惧ù锝囨嚀椤ｆ煡鏌￠崼婵愭Ш婵烇綆鍠楅幆鏃堟晜閻愵剛鏆犻梺鍛婄墪閹碱偊鎮″▎鎾虫瀬闁绘鐗嗙粊?
     */
    public static void sendAllAreaDataToAll() {
        Areashint.LOGGER.info(ServerI18nManager.translate("message.button.dimension"));
        sendAreaDataToAll(Packets.DIMENSION_OVERWORLD);
        sendAreaDataToAll(Packets.DIMENSION_NETHER);
        sendAreaDataToAll(Packets.DIMENSION_END);
        Areashint.LOGGER.info(ServerI18nManager.translate("message.button.dimension.finish"));
    }
    
    /**
     * 闂佸憡纰嶉崹鍓佲偓鍨皑閳ь剝顫夊銊ф暜閾忓厜鍋撶涵鍛【鐟滄澘鍊块弻鍛媴閻熼偊鏆￠梺鍝勭墕椤﹀崬菐椤旇姤鍎熼柨鏃傚亾閻ｉ亶鏌涢弽褎鎯堥柣鎾寸懇瀵偊鎮ч崼婵堛偊
     * @param player 闂佺儵鏅╅崰妤呮偉閿濆鍋濋柍杞扮贰閸熲偓
     */
    public static void sendAllAreaDataToClient(ServerPlayerEntity player) {
        sendAreaDataToClient(player, Packets.DIMENSION_OVERWORLD);
        sendAreaDataToClient(player, Packets.DIMENSION_NETHER);
        sendAreaDataToClient(player, Packets.DIMENSION_END);
    }
    
    /**
     * 闂佸憡纰嶉崹鐢割敇瑜版帒绠ｅ瀣瘨娴煎倿鏌涘▎鎰伌闁逞屽厸缁€渚€骞婇埄鍐浄?
     * @param player 闂佺儵鏅╅崰妤呮偉閿濆鍋濋柍杞扮贰閸熲偓
     * @param command 闂佸憡绋掗崹婵嬪箮閵堝洠鍋撳☉娆樻畼妞ゆ垳鐒︾粙?
     */
    public static void sendCommandToClient(ServerPlayerEntity player, String command) {
        try {
            // 闂佸憡甯楃粙鎴犵磽閹捐鏋侀柣妤€鐗嗙粊锕傛煕?
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeString(command);
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶉柡鍡欏枛楠炴垿顢欓悡搴ｆ▉
            ServerPlayNetworking.send(player, new Identifier(Packets.S2C_CLIENT_COMMAND), buffer);
            
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_123") + player.getName().getString() + ServerI18nManager.translate("message.message.general_9") + command);
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.message.general_71") + e.getMessage());
        }
    }
    
    /**
     * 闂佸憡纰嶉崹鑸垫櫠瀹ュ瀚夊璺猴攻闊剛绱掗幆褏浠㈡い鎾崇秺楠炲顦版惔顔荤磽闂佸憡鐟﹂崹鍧楀焵椤戣法顦﹂柟鏂ュ墲缁?
     */
    public static void sendCommandToAllClients(MinecraftServer server, String command) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendCommandToClient(player, command);
        }
    }

    /**
     * 闂佸憡纰嶉崹鐢割敇瑜版帒绠ｅ瀣瘨娴煎倿鏌涘▎鎰伌闁逞屽厸濞村洭鎯冮悢鐑樺珰闁哄洨鍋為崵鎺懨?
     * @param player 闂佺儵鏅╅崰妤呮偉閿濆鍋濋柍杞扮贰閸熲偓
     * @param enabled 闂佸搫瀚烽崹浼村箚娓氣偓瀹曘儵顢涘鍕闁荤姴顑呴崯鎶芥儊?
     */
    public static void sendDebugCommandToClient(ServerPlayerEntity player, boolean enabled) {
        try {
            // 闂佸憡甯楃粙鎴犵磽閹捐鏋侀柣妤€鐗嗙粊锕傛煕?
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeBoolean(enabled);
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶉柡鍡欏枛楠炴垿顢欓悡搴ｆ▉
            ServerPlayNetworking.send(player, new Identifier(Packets.S2C_DEBUG_COMMAND), buffer);
            
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_123") + player.getName().getString() + ServerI18nManager.translate("message.message.general_11") + (enabled ? ServerI18nManager.translate("message.message.general_74") : ServerI18nManager.translate("message.message.general_213")));
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.message.general_72") + e.getMessage());
        }
    }
    
    /**
     * 闂佸憡纰嶉崹鑸垫櫠瀹ュ瀚夊璺猴攻闊剛绱掗幑鎰《閻㈩垵娅ｉ埀顒傤攰濞夋稖銇愰崒鐐寸劵濞达絿鎳撻惁褰掓倵鐟欏嫭鐨戞繛锝庡枟閹棃鏁傞悙顒傛殸闂佸憡鐗曢幖顐︽偂濞嗘挸鏋侀柣妤€鐗嗙粊?
     * @param dimensionType 缂傚倷鐒﹀娆戔偓鐟扮－閻氬墽鎷犻懠顑藉亾?
     */
    public static void sendAreaDataToAllPlayers(String dimensionType) {
        try {
            // 闂佸吋鍎抽崲鑼躲亹閸ヮ剙绠ラ柍褜鍓熷鍨緞婵犲啳鍚紓浣瑰礃濞夋洜鏁搹鍏夊亾?
            List<ServerPlayerEntity> players = Areashint.getServer().getPlayerManager().getPlayerList();
            
            for (ServerPlayerEntity player : players) {
                // 濠碘槅鍋€閸嬫捇鏌＄仦璇插姢閻㈩垵娅ｉ埀顒傤攰閸╂牕危閹间礁瑙﹂柨鏇楀亾婵犫偓椤忓牆绠伴柛銉戝懏姣庣紓鍌欑劍濞兼瑧鈧娅曠粙?
                String playerDimension = player.getWorld().getRegistryKey().getValue().toString();
                String playerDimensionType = Packets.convertDimensionPathToType(playerDimension);
                
                if (playerDimensionType != null && playerDimensionType.equals(dimensionType)) {
                    // 闂佸憡鐟﹂崹鍧楀焵椤戣法顦﹂悘蹇ｅ灦瀹曟椽鎮㈤崨濠冾啀闂佺顕栭崰鏍春瀹€鈧幏鐘诲Ψ瑜嶇拹鐔兼倵?
                    sendAreaDataToClient(player, dimensionType);
                }
            }
            
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.dimension_6"), dimensionType);
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.error.general_34") + e.getMessage(), e);
        }
    }
    
    /**
     * 濠电偛顦崝宀勫船閻ｅ瞼纾鹃柟瀵稿Х閹规洟鎮归崶顒佹暠闁活亞澧楀鍕礋椤撶喎鈧偤鏌?
     */
    private static void registerNetworkHandlers() {
        // 濠电偛顦崝宀勫船閻ｅ本瀚氭い鎾閺嬪懘鏌涘顒佹拱妞ゆ帞鍋炲鍕礋椤撶喎鈧偤鏌?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_LANGUAGE_SYNC,
            (server, player, handler, buf, responseSender) -> {
                String lang = buf.readString();
                server.execute(() -> ServerI18nManager.setPlayerLanguage(player.getUuid(), lang));
            });

        // 濠电偛顦崝宀勫船缁层€哻olor闁荤姴娲弨閬嶆儑閻楀牆绶為柛鏇ㄥ幗閸婄偤鏌?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_RECOLOR_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String areaName = buf.readString();
                    String color = buf.readString();
                    String dimension = buf.readString();

                    server.execute(() -> {
                        areahint.command.RecolorCommand.handleRecolorRequest(player, areaName, color, dimension);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.general_37"), e);
                }
            });

        // 濠电偛顦崝宀勫船缁插ズtHigh闁荤姴娲弨閬嶆儑閻楀牆绶為柛鏇ㄥ幗閸婄偤鏌?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_SETHIGH_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String areaName = buf.readString();
                    final boolean hasCustomHeight = buf.readBoolean();
                    final Double maxHeight;
                    final Double minHeight;

                    if (hasCustomHeight) {
                        boolean hasMax = buf.readBoolean();
                        maxHeight = hasMax ? buf.readDouble() : null;
                        boolean hasMin = buf.readBoolean();
                        minHeight = hasMin ? buf.readDouble() : null;
                    } else {
                        maxHeight = null;
                        minHeight = null;
                    }

                    server.execute(() -> {
                        areahint.command.SetHighCommand.handleHeightRequest(player, areaName, hasCustomHeight, maxHeight, minHeight);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.general_36"), e);
                }
            });

        // 濠电偛顦崝宀勫船閻ｅ本瀚氶梺鍨儑濠€鎾煕濞嗘ê鐏犻柛銊ョ秺濮婁粙濡堕崨顖氬箥闂佸憡鑹剧粔鎾垂椤忓棙鍋橀柕濞垮労濡查亶鏌ｉ悙鍙夘棞婵?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_REQUEST_DELETABLE_AREAS,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimension = buf.readString();

                    server.execute(() -> {
                        sendDeletableAreasList(player, dimension);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.area.delete.list_5"), e);
                }
            });

        // 濠电偛顦崝宀勫船缁辩暴lete闁荤姴娲弨閬嶆儑閻楀牆绶為柛鏇ㄥ幗閸婄偤鏌?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DELETE_AREA,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String areaName = buf.readString();
                    final String dimension = buf.readString();

                    server.execute(() -> {
                        handleDeleteRequest(player, areaName, dimension);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.general_35"), e);
                }
            });

        // 濠电偛顦崝宀勫船閻ｅ瞼纾奸柡澶嬪灥椤斿﹪鏌涢埡鍐ㄦ瀻闁诡喗娲樼粚閬嶎敊閼恒儲姣夐柣鐘叉喘閺€閬嶆儑閻楀牆绶為柛鏇ㄥ幗閸婄偤鏌?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DIMNAME_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimensionId = buf.readString();
                    final String newName = buf.readString();
                    server.execute(() -> {
                        if (!player.hasPermissionLevel(2)) {
                            player.sendMessage(TextCompat.translatable("message.error.permission"), false);
                            return;
                        }
                        areahint.command.DimensionalNameCommands.handleDimNameChange(
                            player.getCommandSource(), dimensionId, newName);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.area.dimension.modify_2"), e);
                }
            });

        // 濠电偛顦崝宀勫船閻ｅ瞼纾奸柡澶嬪灥椤斿﹪鏌涢埡鍐ㄦ瀻闁诡喗娲橀敍鎰煥閸℃妫屾繛锝呮祩閸犳寮懖鈺傚珰闂佸灝顑囧﹢鏉戭熆鐠哄搫顏柟顔硷躬瀹?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_DIMCOLOR_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimensionId = buf.readString();
                    final String newColor = buf.readString();
                    server.execute(() -> {
                        if (!player.hasPermissionLevel(2)) {
                            player.sendMessage(TextCompat.translatable("message.error.permission"), false);
                            return;
                        }
                        areahint.command.DimensionalNameCommands.handleDimColorChange(
                            player.getCommandSource(), dimensionId, newColor);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.area.dimension.modify_2"), e);
                }
            });

        // 濠电偛顦崝宀勫船閼恒儻绱旈柡宥庣厛閸嬔呯磽娴ｈ缍戦悗瑙勫▕瀹曘劑鎸婃径瀣偓鍐差熆鐠哄搫顏柟顔硷躬瀹曟娊鈥﹂幒鏃傤槱闂佸搫鍟版慨闈涱焽閸儲鈷旈柟閭﹀灱濞诧絾鎱ㄩ悷鏉夸喊缂佽鲸绻冪粋鎺楀川鐎涙﹩鈧瑩鏌￠崼顐＄盎闁圭绻濆畷銉ヮ吋閸ャ劍鈻曢柟鑹版彧鐠侊絿妲?
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_FIRST_DIMNAME,
            (server, player, handler, buf, responseSender) -> {
                try {
                    final String dimensionId = buf.readString();
                    final String newName = buf.readString();
                    server.execute(() -> {
                        String currentName = areahint.dimensional.DimensionalNameManager.getDimensionalName(dimensionId);
                        // 婵炲濮撮幊搴ｇ礊鐎ｎ剛纾奸柡澶嬪灥椤斿﹪鏌涘顒傂ょ悮銊х磼濞戞﹩妲风紒顭戝墰缁辨帡寮堕幋婵愬敽ID闂佸搫鍟抽鎰濞嗘挸瀚夋い蹇撴祩濞煎爼鏌涘☉婊勵棄闁诡喗娲熼弫宥嗗緞鐎ｎ亶鏋€闂佺绻嬪ù鍥敊?
                        if (!currentName.equals(dimensionId)) {
                            player.sendMessage(TextCompat.translatable("message.error.dimension_2"), false);
                            return;
                        }
                        areahint.command.DimensionalNameCommands.handleDimNameChange(
                            player.getCommandSource(), dimensionId, newName);
                    });
                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("message.error.dimension_4"), e);
                }
            });
    }

    /**
     * 闂佸憡鐟﹂崹鍧楀焵椤戣法顦︾憸鐗堢叀瀹曟岸鎮╃紒妯煎綉闂佺硶鏅濋崰搴ㄥ箖閺囥垹绀嗘俊銈呭閳ь剙鍟村畷姘跺级濞嗘儳鎮呴梺瑙勬尦椤ユ捇顢?
     * @param player 闁荤姴娲弨閬嶆儑娴煎瓨鍎嶉柛鏇ㄥ幖鐠愮喖鎮?
     * @param dimension 缂傚倷鐒﹀娆戔偓瑙勫▕瀵粙宕堕鍡橆潥
     */
    private static void sendDeletableAreasList(ServerPlayerEntity player, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean hasOp = player.hasPermissionLevel(2);

            Areashint.LOGGER.info(ServerI18nManager.translate("message.prompt.area.delete.list") + playerName + ServerI18nManager.translate("message.message.dimension_2") + dimension + ServerI18nManager.translate("message.message.general_21") + hasOp);

            // 闂佸吋鍎抽崲鑼躲亹閸ヮ剙妫橀柛銉檮椤愪粙鏌?
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.dimension_11") + dimension);
                // 闂佸憡鐟﹂崹鍧楀焵椤戣儻鍏岄柍瑙勭墵瀹曟艾螖閸曗斁鍋?
                sendEmptyDeletableAreasList(player);
                return;
            }

            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);

            // 闁荤姴娲╅褑銇愰崶顒€绀岄柛婵嗗閸樼敻鏌℃担鍝勵暭鐎?
            java.util.List<areahint.data.AreaData> allAreas = FileManager.readAreaData(areaFile);
            java.util.List<areahint.data.AreaData> deletableAreas = new java.util.ArrayList<>();

            // 缂備焦绋掗惄顖炲焵椤掆偓椤︻垵銇愰弻銉ョ闁绘绮悵鐔兼煟閵娿儱顏╅柣鎾寸懇瀹?
            for (areahint.data.AreaData area : allAreas) {
                String signature = area.getSignature();

                // 濠碘槅鍋€閸嬫捇鏌＄仦璇插姕婵炵厧鐗撳?
                boolean canDelete = false;
                if (signature == null) {
                    // 濠电偛澶囬崜婵嗭耿娴ｈ櫣椹抽柟顖嗗嫬鈧娊鏌ｉ妸銉ヮ仾婵☆偒鍋婂畷娲偄闁垮鈧娊鏌涘▎妯圭凹婵犫偓娴ｈ櫣涓嶉柨娑樺閸婄偤鏌涘☉娅亣銇愰崣澶岊浄闁靛鍎遍悘鈺呮⒒?
                    canDelete = hasOp;
                } else {
                    // 闂佸搫鐗嗛ˇ閬嶎敆閻戣棄瑙︾€广儱娲﹂悾閬嶆煛閸屾稒绶查柣鎾寸懇瀹曘儱顓艰箛鏇犵崶闂佸憡甯楃粙鎴犵磽閹剧粯鍤€闁告侗鍠栭悘妤冪磼閻欏懐纾块柟顔硷躬瀹曘劌螣閸濆嫯顔夋繛瀵稿О閸庨亶宕硅ぐ鎺撯挃?
                    canDelete = signature.equals(playerName) || hasOp;
                }

                if (canDelete) {
                    // 濠碘槅鍋€閸嬫捇鏌＄仦璇插姕婵″弶鎮傚畷銉╂晜閼恒儳鐣冲┑鐐叉４缁辨洘顨ラ崶顒€鏄ラ柣鏃堟敱閸婂磭鈧鍠楀ú婊堝极閵堝拋娼伴柕鍫濇噽閸樼敻鏌?
                    boolean hasChildren = false;
                    for (areahint.data.AreaData childArea : allAreas) {
                        if (area.getName().equals(childArea.getBaseName())) {
                            hasChildren = true;
                            break;
                        }
                    }

                    // 闂佸憡鐟禍婵嗭耿娴ｇ硶鏌﹂柍鈺佸暞缁犳帡鎮楀☉娆忓闁绘挻鐟╁畷銉ヮ吋閸ャ劎鏆犻梺褰掓涧缁夌兘宕楀鈧畷姘舵偐缂佹褰?
                    if (!hasChildren) {
                        deletableAreas.add(area);
                    }
                }
            }

            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_175") + deletableAreas.size() + ServerI18nManager.translate("message.message.area.delete_2"));

            // 闂佸憡甯楃粙鎴犵磽閹捐鏋侀柣妤€鐗嗙粊锕傛煕?
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(deletableAreas.size());

            for (areahint.data.AreaData area : deletableAreas) {
                String json = areahint.file.JsonHelper.toJsonSingle(area);
                buffer.writeString(json);
            }

            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶉柡鍡欏枛楠炴垿顢欓悡搴ｆ▉
            ServerPlayNetworking.send(player, Packets.S2C_DELETABLE_AREAS_LIST, buffer);

            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_123") + playerName + ServerI18nManager.translate("message.message.area.delete.list"));

        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.error.area.delete.list_2"), e);
            sendEmptyDeletableAreasList(player);
        }
    }

    /**
     * 闂佸憡鐟﹂崹鍧楀焵椤戣儻鍏岄柍瑙勭墵閹啴宕熼銏ｎ唹闂佸憡甯炴繛鈧繛鍛叄瀹曟椽鎮㈤柨瀣偓鎶芥煕閹烘搩娈欓柕?
     * @param player 闂佺儵鏅╅崰妤呮偉閿濆鍋濋柍杞扮贰閸熲偓
     */
    private static void sendEmptyDeletableAreasList(ServerPlayerEntity player) {
        try {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(0);
            ServerPlayNetworking.send(player, Packets.S2C_DELETABLE_AREAS_LIST, buffer);
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.error.area.delete.list_3"), e);
        }
    }

    /**
     * 婵犮垼娉涚€氼噣骞冩繝鍥х闁绘绮悵鐔兼煕閳哄啫鏋庨柟顔芥礈閹风娀鏌ㄧ€ｎ剚鍕?
     * @param player 闁荤姴娲弨閬嶆儑娴煎瓨鍎嶉柛鏇ㄥ幖鐠愮喖鎮?
     * @param areaName 闁荤喐娲戠粈渚€宕硅ぐ鎺撯挃闁靛牆娲﹂悾閬嶆煕閳哄啫鏋庨柟顔芥礋瀹曘儱顓奸崶?
     * @param dimension 缂傚倷鐒﹀娆戔偓瑙勫▕瀵粙宕堕鍡橆潥
     */
    private static void handleDeleteRequest(ServerPlayerEntity player, String areaName, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean hasOp = player.hasPermissionLevel(2);

            Areashint.LOGGER.info(ServerI18nManager.translate("message.prompt.delete_3") + playerName + ServerI18nManager.translate("message.message.area_5") + areaName + ServerI18nManager.translate("message.message.dimension_2") + dimension);

            // 闂佸吋鍎抽崲鑼躲亹閸ヮ剙妫橀柛銉檮椤愪粙鏌?
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.dimension_11") + dimension);
                sendDeleteResponse(player, false, key("message.message.dimension_12"), lit(dimension));
                return;
            }

            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.dimension_16") + fileName);

            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.area_9") + areaFile.toAbsolutePath());

            // 闁荤姴娲╅褑銇愰崶顒€绀岄柛婵嗗閸樼敻鏌℃担鍝勵暭鐎?
            java.util.List<areahint.data.AreaData> areas = FileManager.readAreaData(areaFile);
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_224") + areas.size() + ServerI18nManager.translate("message.message.area_3"));

            // 闂佸搫琚崕鍙夌珶濡吋鍟哄ù锝囶焾閻忊晠姊婚崟鈺佲偓妤€鈻撻幋锕€鏄ラ柣鏃堟敱閸?
            areahint.data.AreaData targetArea = null;
            for (areahint.data.AreaData area : areas) {
                if (area.getName().equals(areaName)) {
                    targetArea = area;
                    break;
                }
            }

            if (targetArea == null) {
                Areashint.LOGGER.warn(ServerI18nManager.translate("addhint.message.area_3") + areaName);
                sendDeleteResponse(player, false, key("addhint.message.area_3"), lit(areaName));
                return;
            }

            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.area_11") + areaName + ServerI18nManager.translate("message.message.general_3") + targetArea.getSignature());

            // 濠碘槅鍋€閸嬫捇鏌＄仦璇插姢妞ゆ帞鍏樺畷銉ヮ吋閸℃绉梻?
            String signature = targetArea.getSignature();
            if (signature == null) {
                // 濠电偛澶囬崜婵嗭耿娴ｈ櫣椹抽柟顖嗗嫬鈧娊鏌ｉ妸銉ヮ仾婵☆偒鍋婂畷娲偄闁垮鈧娊鏌ㄥ☉娆掑鐟滄妸鍥у珘濠㈣泛鐭堥崥鈧梺鑽ゅ仜濡骞夐幎钘夌煑妞ゆ牗鐟ょ花浼存煕閹烘柨鈻堟繛?
                if (!hasOp) {
                    Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.general_28") + playerName + ServerI18nManager.translate("message.message.area.delete") + ServerI18nManager.translate("message.message.area.delete_4"));
                    sendDeleteResponse(player, false, key("message.message.area.delete_7"));
                    return;
                }
                // 缂備胶濯寸槐鏇㈠箖婵犲洤宸濇俊顖氭惈鐠佹彃霉閻橆喖鍔ら柟鎾棑缁辨帡顢橀悙鑼患闂?
                Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_216") + playerName + ServerI18nManager.translate("message.message.area.delete_4"));
            } else {
                // 闂佸搫鐗嗛ˇ閬嶎敆閻戣棄瑙︾€广儱娲﹂悾閬嶆煛閸屾稒绶查柣鎾寸懇瀹曘儱顓艰箛鏇犵崶闂佸憡甯楃粙鎴犵磽閹剧粯鍤€闁告侗鍠栭悘妤冪磼閻欏懐纾块柟顔硷躬瀹曘劌螣閸濆嫯顔夋繛瀵稿О閸庨亶宕硅ぐ鎺撯挃?
                if (!signature.equals(playerName) && !hasOp) {
                    Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.general_28") + playerName + ServerI18nManager.translate("message.message.area_2") + signature);
                    sendDeleteResponse(player, false, key("message.message.area.delete_6"));
                    return;
                }
            }

            // 濠碘槅鍋€閸嬫捇鏌＄仦璇插姕婵″弶鎮傚畷銉╂晜閼恒儳鐣冲┑鐐叉４缁辨洘顨ラ崶顒€鏄ラ柣鏃堟敱閸婂磭鈧鍠楀ú婊堝极閵堝拋娼伴柕鍫濇噽閸樼敻鏌?
            for (areahint.data.AreaData area : areas) {
                if (areaName.equals(area.getBaseName())) {
                    Areashint.LOGGER.warn(ServerI18nManager.translate("message.message.area_8") + areaName + ServerI18nManager.translate("message.message.area_4") + area.getName() + ServerI18nManager.translate("message.message.general_15"));
                    sendDeleteResponse(player, false, key("message.message.area.delete_5"), lit(area.getName()), key("message.message.general_16"));
                    return;
                }
            }

            // 闂佸湱鐟抽崱鈺傛杸闂佸憡甯炴繛鈧繛?
            areas.remove(targetArea);
            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.area.list_2") + areaName);

            // 婵烇絽娲︾换鍌炴偤閵娾晛妫橀柛銉檮椤?
            FileManager.writeAreaData(areaFile, areas);

            // 闂佸憡纰嶉崹鑸垫櫠瀹ュ瀚夊璺侯煬閸氬倿鏌熼幘顕呮妞ゅ浚鍓熷畷锝夊箣閿旂懓浜惧ù锝堫潐缁绢垶鏌￠崒娑欑凡闁诡喗顨婇幆鍐礋椤愩垽娈梺绯曟櫇閸犳劙寮抽悢鐓庣?
            sendAllAreaDataToAll();

            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶉柛銊﹀哺瀹曟繈鎮㈤柨瀣劌闁?
            sendDeleteResponse(player, true, lit(areaName));

            Areashint.LOGGER.info(ServerI18nManager.translate("message.message.general_28") + playerName + ServerI18nManager.translate("message.message.area.delete_3") + areaName);

        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.error.delete_5"), e);
            sendDeleteResponse(player, false, key("message.error.area.delete_3"), lit(e.getMessage()));
        }
    }

    /**
     * 闂佸憡鐟﹂崹鍧楀焵椤戣法顦﹂柛銊ョ秺濮婁粙濡堕崨顔藉劌闁圭厧鐡ㄩ弻銊╁春瀹€鈧埀顒€绠嶉崹娲春濞戞氨鍗?
     * @param player 闂佺儵鏅╅崰妤呮偉閿濆鍋濋柍杞扮贰閸熲偓
     * @param success 闂佸搫瀚烽崹浼村箚娓氣偓楠炲骞囬鈧～?
     * @param message 闂佸憡绻傜粔瀵歌姳閹绘巻妲堥柛顐ゅ枍缁?
     */
    private static void sendDeleteResponse(ServerPlayerEntity player, boolean success, Part... parts) {
        try {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeBoolean(success);
            TranslatableMessage.write(buffer, parts);
            ServerPlayNetworking.send(player, Packets.S2C_DELETE_RESPONSE, buffer);
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("message.message.delete_3") + e.getMessage());
        }
    }
} 
