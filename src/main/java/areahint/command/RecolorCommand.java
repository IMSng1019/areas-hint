package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import static areahint.network.TranslatableMessage.key;
import static areahint.network.TranslatableMessage.lit;
import areahint.util.ColorUtil;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.nio.file.Path;
import areahint.i18n.ServerI18nManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 闁糕晝鍠庨幃鏇㈡煂瀹ュ棙鐓€闁活偀鍋撻柤鐟板级鐎垫碍绂掗妶鍜佹П闁荤偛妫楀▍?
 */
public class RecolorCommand {
    
    /**
     * 闁圭瑳鍡╂斀recolor闁圭娲ｉ幎?
     * @param context 闁圭娲ｉ幎銈嗙▔婵犱胶鐟撻柡?
     * @return 闁圭瑳鍡╂斀缂備焦鎸婚悘?
     */
    public static int executeRecolor(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        String playerName = player.getName().getString();
        boolean hasOp = source.hasPermissionLevel(2);

        // 闁兼儳鍢茶ぐ鍥偝閳轰緡鍟€鐟滅増鎸告晶鐘电磼閺夋垵顔?
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        String fileName = Packets.getFileNameForDimension(dimensionType);

        if (fileName == null) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.dimension_3"));
            return 0;
        }

        // 闁兼儳鍢茶ぐ鍥矗椤栨粎妞介弶鍫熷灩濞堟垿宕洪悢閿嬪€抽柛鎺擃殙閵?
        List<AreaData> editableAreas = getEditableAreas(fileName, playerName, hasOp);

        if (editableAreas.isEmpty()) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.dimension_3"));
            return 0;
        }

        // 闁告瑦鍨块埀顑跨閻撴瑩宕ュ鍛仚閻炴稏鍔岄崺宀€鈧箍鍨洪崺娑氱博椤栥倗绀夐柛鏍ф噹閹牊绂嶉妶鍕瀺鐎殿喖绻橀·渚€鎳濋弻銉㈠亾婢跺顏ラ柣锝呯焸濞?
        sendInteractiveRecolorToClient(player, editableAreas, dimensionType);

        CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.prompt.area.list"));
        return 1;
    }
    
    /**
     * 闁圭瑳鍡╂斀閻㈩垽绠戝顒勫极閹殿喗鐣眗ecolor闁圭娲ｉ幎銈夋晬閸垺绾柟鎭掑劙閹便劑寮ㄨぐ鎺濇澒闁肩顕滅槐?
     * @param context 闁圭娲ｉ幎銈嗙▔婵犱胶鐟撻柡?
     * @param areaName 闁糕晝鍠庨幃鏇㈠触瀹ュ泦?
     * @param colorInput 濡増绮忔竟濠冩綇閹惧啿寮?
     * @return 闁圭瑳鍡╂斀缂備焦鎸婚悘?
     */
    public static int executeRecolorChange(CommandContext<ServerCommandSource> context, String areaName, String colorInput) {
        ServerCommandSource source = context.getSource();
        
        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_10"));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        String playerName = player.getName().getString();

        // 闁哄秴娲ら崳顖炲礌閺嶎収鏉归柤纭呭蔼缁额參宕?
        String normalizedColor = areahint.util.ColorUtil.normalizeColor(colorInput);
        if (!areahint.util.ColorUtil.isValidColor(normalizedColor)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.color").append(TextCompat.literal(colorInput)));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("message.message.color"));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("message.message.general_45"));
            return 0;
        }
        
        // 闁兼儳鍢茶ぐ鍥偝閳轰緡鍟€鐟滅増鎸告晶鐘电磼閺夋垵顔?
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String dimensionType = convertDimensionIdToType(dimension);
        
        // 濠㈣泛瀚幃濠囨煂瀹ュ棙鐓€闁活偀鍋撻柤纭呭蔼椤曨剙效?
        handleRecolorRequest(player, areaName, normalizedColor, dimensionType);
        
        return 1;
    }
    
    /**
     * 闁兼儳鍢茶ぐ鍥偝閳轰緡鍟€闁告瑯鍨崇槐顏呮綇閹寸姵鐣遍柛鈺冨枎閹洟宕氬Δ鍕┾偓?
     */
    private static List<AreaData> getEditableAreas(String fileName, String playerName, boolean hasOp) {
        List<AreaData> editableAreas = new ArrayList<>();
        
        try {
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (areaFile == null || !areaFile.toFile().exists()) {
                return editableAreas;
            }
            
            List<AreaData> allAreas = FileManager.readAreaData(areaFile);
            
            for (AreaData area : allAreas) {
                // 缂佺媴绱曢幃濠囧川濡搫璁插ù鐘劤缁鳖亝娼忛幋鐐差暡闁哄牆顦悡娆撳触瀹ュ繒绀夐柣婧炬櫅椤斿秹宕ｉ鍥у幋缂傚倹鐗炵欢顐︽嚊椤忓嫮绠掗柛鎺撶☉缂傛捇鎯冮崟顐ゅ幍闁?
                if (hasOp || playerName.equals(area.getSignature())) {
                    editableAreas.add(area);
                }
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.area.list_2"), e);
        }
        
        return editableAreas;
    }
    
    /**
     * 闁告瑦鍨块埀顑跨閻撴瑩宕ュ鍛仚閻炴稏鍔岄崺宀€鈧箍鍨洪崺娑氱博?
     */
    private static void sendAreaListToClient(ServerPlayerEntity player, List<AreaData> areas, String dimension) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("recolor_list");
            buf.writeString(dimension);
            buf.writeInt(areas.size());
            
            for (AreaData area : areas) {
                buf.writeString(area.getName());
                buf.writeString(area.getColor());
                buf.writeInt(area.getLevel());
                buf.writeString(area.getBaseName() != null ? area.getBaseName() : "");
            }
            
            ServerPlayNetworking.send(player, Packets.S2C_RECOLOR_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.area.list"), e);
        }
    }
    
    /**
     * 濠㈣泛瀚幃濠勨偓骞垮灪閸╂稓绮╅姘岛闂侇偂鑳跺▓鎴︽煂瀹ュ棙鐓€闁活偀鍋撻柤纭呭蔼椤曨剙效?
     */
    public static void handleRecolorRequest(ServerPlayerEntity player, String areaName, String newColor, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean hasOp = player.hasPermissionLevel(2);
            
            // 濡ょ姴鐭侀惁澶嬶紣濠婂棗顥忛柡宥囧帶缁?
            if (!ColorUtil.isValidColor(newColor)) {
                sendRecolorResponse(player, false, key("command.error.color_3"), lit(newColor));
                return;
            }
            
            // 闁兼儳鍢茶ぐ鍥╃磼閺夋垵顔婇柡鍌氭矗濞?
            String fileName = Packets.getFileNameForDimension(dimension);
            if (fileName == null) {
                sendRecolorResponse(player, false, key("addhint.error.dimension"), lit(dimension));
                return;
            }
            
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (areaFile == null || !areaFile.toFile().exists()) {
                sendRecolorResponse(player, false, key("command.message.dimension_5"));
                return;
            }
            
            // 閻犲洩顕цぐ鍥椽鐏炵偓绾柡鍌涙緲閻撴瑩宕ュ鍡樻闁?
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            boolean found = false;
            String oldColor = "";
            
            for (AreaData area : areas) {
                if (area.getName().equals(areaName)) {
                    // 婵☆偀鍋撻柡灞诲劜濞煎牓姊?
                    if (!hasOp && !playerName.equals(area.getSignature())) {
                        sendRecolorResponse(player, false, key("command.message.area.modify.permission"), lit(areaName + "\""));
                        return;
                    }
                    
                    oldColor = area.getColor();
                    area.setColor(newColor);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                sendRecolorResponse(player, false, key("addhint.message.area_3"), lit(areaName));
                return;
            }
            
            // 濞ｅ洦绻傞悺銊╁棘閸ワ附顐?
            if (FileManager.writeAreaData(areaFile, areas)) {
                sendRecolorResponse(player, true, key("command.message.area_10"), lit(areaName), key("command.message.color_4"), lit(oldColor), lit(" 闁?"), lit(newColor));
                
                // 闂佹彃绉甸弻濠囧礉閻樼儤绁版鐐舵硾瑜板倿鏌呮担铏硅埗闁圭鍋撻柡鍫濐槸椤撳綊骞嬫搴紓
                ServerNetworking.sendAllAreaDataToAll();
                
                Areashint.LOGGER.info(ServerI18nManager.translate("command.message.general_28") + playerName + ServerI18nManager.translate("command.message.area_4") + areaName + "\"" + ServerI18nManager.translate("command.message.color_3") + oldColor + ServerI18nManager.translate("command.message.general_5") + newColor);
                
            } else {
                sendRecolorResponse(player, false, key("command.error.area.save"));
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.general_20"), e);
            sendRecolorResponse(player, false, key("command.error.general_19"), lit(e.getMessage()));
        }
    }
    
    /**
     * 闁告瑦鍨块埀顑跨窔閸ｆ悂寮幍顔界祷闁艰褰冮幖閿嬫償?
     */
    private static void sendRecolorResponse(ServerPlayerEntity player, boolean success, Part... parts) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("recolor_response");
            buf.writeBoolean(success);
            TranslatableMessage.write(buf, parts);
            ServerPlayNetworking.send(player, Packets.S2C_RECOLOR_RESPONSE, buf);
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.general_18"), e);
        }
    }
    
    /**
     * 闁告瑦鍨块埀顑挎濮橈附绂嶉幒鎴犵recolor闁伙絽鐭傚浼村礆閺夋鍚傞柟鎾棑椤?
     */
    private static void sendInteractiveRecolorToClient(ServerPlayerEntity player, List<AreaData> areas, String dimension) {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("recolor_interactive");
            buf.writeString(dimension);
            buf.writeInt(areas.size());
            
            for (AreaData area : areas) {
                buf.writeString(area.getName());
                buf.writeString(area.getColor());
                buf.writeInt(area.getLevel());
                buf.writeString(area.getBaseName() != null ? area.getBaseName() : "");
            }
            
            ServerPlayNetworking.send(player, Packets.S2C_RECOLOR_RESPONSE, buf);
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.general_15"), e);
        }
    }
    
    /**
     * 閻忓繐妫涘ǎ顔芥償椤ㄧ枍閺夌儐鍓氬畷鍙夌▔缁℃竵ckets闁哄牏鍠愬﹢婊堟儍閸曨厽妯婇幖杈惧鐞氼偊宕?
     */
    private static String convertDimensionIdToType(String dimension) {
        if (dimension == null) return null;
        
        if (dimension.contains("overworld")) {
            return Packets.DIMENSION_OVERWORLD;
        } else if (dimension.contains("nether")) {
            return Packets.DIMENSION_NETHER;
        } else if (dimension.contains("end")) {
            return Packets.DIMENSION_END;
        }
        return null;
    }
} 
