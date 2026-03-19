package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.ServerI18nManager;
import areahint.network.Packets;
import areahint.network.ServerNetworking;
import areahint.network.TranslatableMessage;
import areahint.network.TranslatableMessage.Part;
import static areahint.network.TranslatableMessage.key;
import static areahint.network.TranslatableMessage.lit;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 闁糕晝鍠庨幃鏇㈡煂瀹ュ懏鍤掗柛姘Т閹斥剝绂掗妶鍜佹П闁荤偛妫涚悮?
 * 閻庡湱鍋熼獮?/areahint rename 闁圭娲ｉ幎銈夊礉閻旇鍘撮柨娑樼墔濮橈附绂嶉幒鎴犵婵炵繝鑳堕埢濂告晬?
 */
public class RenameAreaCommand {

    /**
     * 闁圭瑳鍡╂斀rename闁圭娲ｉ幎銈夋晬閸繃鍎欓柛鏂诲妺濮橈附绂嶉幒鎴犵婵炵繝鑳堕埢濂告晬?
     * @param context 闁告稒鍨濋幎銈嗙▔婵犱胶鐟撻柡?
     * @return 闁圭瑳鍡╂斀缂備焦鎸婚悘?
     */
    public static int executeRename(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        try {
            if (source.getEntity() instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
                sendRenameableAreaList(player);
            } else {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            }
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.general_22") + e.getMessage());
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_4"));
        }

        return 1;
    }

    /**
     * 濠㈣泛瀚幃濠囧春閻旈攱鍊抽梺顐㈩槹鐎氥劑宕ㄩ幋鎺撳Б
     * @param context 闁告稒鍨濋幎銈嗙▔婵犱胶鐟撻柡?
     * @param areaName 闁糕晝鍠庨幃鏇㈠触瀹ュ泦?
     * @return 闁圭瑳鍡╂斀缂備焦鎸婚悘?
     */
    public static int executeRenameSelect(CommandContext<ServerCommandSource> context, String areaName) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            // 闁告瑦鍨块埀顑跨閹斥剝绂掗妶鍛厒閻庡箍鍨洪崺娑氱博?
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:rename_select:" + areaName);
            return 1;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area_3").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 濠㈣泛瀚幃濠勬兜椤旀鍚囬柛娑欏灊閹?
     * @param context 闁告稒鍨濋幎銈嗙▔婵犱胶鐟撻柡?
     * @return 闁圭瑳鍡╂斀缂備焦鎸婚悘?
     */
    public static int executeRenameConfirm(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            // 闁告瑦鍨块埀顑跨閹斥剝绂掗妶鍛厒閻庡箍鍨洪崺娑氱博?
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:rename_confirm");
            return 1;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.confirm.rename").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 濠㈣泛瀚幃濠囧矗閺嶃劎啸闁告稒鍨濋幎?
     * @param context 闁告稒鍨濋幎銈嗙▔婵犱胶鐟撻柡?
     * @return 闁圭瑳鍡╂斀缂備焦鎸婚悘?
     */
    public static int executeRenameCancel(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!CommandSourceCompat.isExecutedByPlayer(source)) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_9"));
            return 0;
        }

        try {
            // 闁告瑦鍨块埀顑跨閹斥剝绂掗妶鍛厒閻庡箍鍨洪崺娑氱博?
            ServerNetworking.sendCommandToClient(source.getPlayer(),
                "areahint:rename_cancel");
            return 1;
        } catch (Exception e) {
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.cancel.rename").append(TextCompat.literal(e.getMessage())));
            return 0;
        }
    }

    /**
     * 闁告瑦鍨块埀顑跨瑜版煡鏌屽鍛殥闁告艾绉撮悡娆撳触瀹ュ懎鐏欓悶娑栧妼閸╁瞼鈧箍鍨洪崺娑氱博?
     * @param player 闁绘壕鏅涢?
     */
    private static void sendRenameableAreaList(ServerPlayerEntity player) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);
            RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
            String dimensionId = dimensionType.getValue().toString();

            // 闁兼儳鍢茶ぐ鍥╃磼閺夋垵顔婇柡鍌氭矗濞嗐垽宕?
            String dimensionPath = dimensionType.getValue().getPath();
            String fileName = Packets.getFileNameForDimension(
                Packets.convertDimensionPathToType(dimensionPath));

            if (fileName == null) {
                sendRenameResponse(player, false, key("command.message.dimension_2"));
                return;
            }

            // 闁兼儳鍢茶ぐ鍥春閻旈攱鍊抽柡鍌氭矗濞嗐垻鎹勯姘辩獮
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString("rename_list");
                buf.writeString(dimensionId);
                buf.writeInt(0); // 闁糕晝鍠庨幃鏇㈠极娴兼潙娅?
                ServerPlayNetworking.send(player, Packets.S2C_RENAME_RESPONSE, buf);
                return;
            }

            // 閻犲洩顕цぐ鍥春閻旈攱鍊抽柡浣哄瀹?
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            List<AreaData> editableAreas = new ArrayList<>();

            // 缂佹稒鐩埀顒€顦ぐ鑼磽閺嶎剛甯嗛柣銊ュ閻撴瑩宕?
            for (AreaData area : areas) {
                if (canRenameArea(area, playerName, isAdmin)) {
                    editableAreas.add(area);
                }
            }

            // 闁告瑦鍨块埀顑跨閸╁瞼鈧箍鍨洪崺娑氱博?
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("rename_list");
            buf.writeString(dimensionId);
            buf.writeInt(editableAreas.size());

            for (AreaData area : editableAreas) {
                buf.writeString(area.getName());
                buf.writeString(area.getSignature() != null ? area.getSignature() : "");
            }

            ServerPlayNetworking.send(player, Packets.S2C_RENAME_RESPONSE, buf);

        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.message.area.list") + e.getMessage());
            sendRenameResponse(player, false, key("command.error.area_4"));
        }
    }

    /**
     * 濠㈣泛瀚幃濠囧春閻旈攱鍊抽梺鎻掔Т閹筹繝宕ュ鍫殲婵?
     * @param player 闁绘壕鏅涢?
     * @param oldName 闁告鍠庨悡娆撳触?
     * @param newName 闁哄倹婢橀悡娆撳触?
     * @param newSurfaceName 闁哄倹濯芥禒鍫ュ触閸繄鍘甸柛姘▌缁辨瑩宕ｉ娆掔null闁瑰瓨鐗滈埞鏍偓娑欘殘椤戜焦绋夌拠褏绀?
     * @param dimension 缂備焦娼欑€圭煼D
     */
    public static void handleRenameRequest(ServerPlayerEntity player, String oldName, String newName,
                                          String newSurfaceName, String dimension) {
        try {
            String playerName = player.getName().getString();
            boolean isAdmin = player.hasPermissionLevel(2);

            // 濡ょ姴鐭侀惁澶愬棘閺夎法鍘甸柛姘У閻楃顕?
            if (newName == null || newName.trim().isEmpty()) {
                sendRenameResponse(player, false, key("command.message.area_11"));
                return;
            }

            newName = newName.trim();

            // 濠㈣泛瀚幃濠勭矚閸濆嫮鎽熺紒妤嬬細鐟曞棝鎯冮崟顔荤矒闁告艾鐗嗛悡娆撳触?
            if (newSurfaceName != null && newSurfaceName.trim().isEmpty()) {
                newSurfaceName = null;
            }

            // 闁兼儳鍢茶ぐ鍥╃磼閺夋垵顔婇柡鍌氭矗濞嗐垽宕?
            String dimensionPath = dimension.substring(dimension.lastIndexOf(":") + 1);
            String fileName = Packets.getFileNameForDimension(
                Packets.convertDimensionPathToType(dimensionPath));

            if (fileName == null) {
                sendRenameResponse(player, false, key("command.message.dimension_3"));
                return;
            }

            // 闁兼儳鍢茶ぐ鍥春閻旈攱鍊抽柡鍌氭矗濞?
            Path areaFile = areahint.world.WorldFolderManager.getWorldDimensionFile(fileName);
            if (!areaFile.toFile().exists()) {
                sendRenameResponse(player, false, key("command.message.dimension_5"));
                return;
            }

            // 閻犲洩顕цぐ鍥春閻旈攱鍊抽柡浣哄瀹?
            List<AreaData> areas = FileManager.readAreaData(areaFile);
            AreaData targetArea = null;
            boolean newNameExists = false;

            // 闁哄被鍎叉竟姗€鎯勯鐣屽灱闁糕晝鍠庨幃鏇㈠椽鐏炵虎姊鹃柡灞诲劜閺屽﹪宕ュ鍥嗙偤寮伴姘剨鐎瑰憡褰冮悺銊╁捶?
            for (AreaData area : areas) {
                if (area.getName().equals(oldName)) {
                    targetArea = area;
                }
                if (area.getName().equals(newName)) {
                    newNameExists = true;
                }
            }

            // 濡ょ姴鐭侀惁?
            if (targetArea == null) {
                sendRenameResponse(player, false, key("addhint.message.area_3"), lit(oldName));
                return;
            }

            if (newNameExists) {
                sendRenameResponse(player, false, key("command.message.area_10"), lit(newName + "\""), key("command.prompt.name"));
                return;
            }

            if (!canRenameArea(targetArea, playerName, isAdmin)) {
                sendRenameResponse(player, false, key("command.message.area.rename.permission"), lit(oldName + "\""));
                return;
            }

            // 闁圭瑳鍡╂斀闂佹彃绉撮幊锟犲触?
            targetArea.setName(newName);

            // 闁哄洤鐡ㄩ弻濠囨嚂閺傛寧鍊ら柛鈺冨枎閹?
            if (newSurfaceName != null) {
                targetArea.setSurfacename(newSurfaceName);
            }

            // 闁哄洤鐡ㄩ弻濠囧箥閳ь剟寮垫径濠勭┛闁活潿鍔忛姘跺春閻旈攱鍊抽柣銊ュ閻℃瑩宕洪悢閿嬪€抽柣?base-name
            for (AreaData area : areas) {
                if (oldName.equals(area.getBaseName())) {
                    area.setBaseName(newName);
                }
            }

            // 濞ｅ洦绻傞悺銊╁棘閸ワ附顐?
            if (FileManager.writeAreaData(areaFile, areas)) {
                player.sendMessage(TextCompat.translatable("command.success.area.rename"), false);
                player.sendMessage(TextCompat.translatable("command.success.area.rename.old").append(TextCompat.literal(oldName)), false);
                player.sendMessage(TextCompat.translatable("command.message.area_5").append(TextCompat.literal(newName)), false);
                if (newSurfaceName != null) {
                    player.sendMessage(TextCompat.translatable("command.message.area.surface_3").append(TextCompat.literal(newSurfaceName)), false);
                }
                sendRenameResponse(player, true, key("command.success.area.rename"));

                // 闁告碍鍨舵晶宥夊嫉婢跺鍚傞柟鎾棑椤忣剟宕ｉ幋锔瑰亾娴ｈ绾柡鍌涙緲閹鎯冮崟顐㈤殬闁糕晝鍠愰弳鐔煎箲?
                ServerNetworking.sendAllAreaDataToAll();

                Areashint.LOGGER.info(ServerI18nManager.translate("command.message.general_28") + playerName + ServerI18nManager.translate("command.message.area_4") + oldName + "\"" + ServerI18nManager.translate("command.message.rename") + newName + "\"");
            } else {
                sendRenameResponse(player, false, key("command.error.area.save"));
            }

        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.rename_2") + e.getMessage());
            sendRenameResponse(player, false, key("command.error.general_19"), lit(e.getMessage()));
        }
    }

    /**
     * 婵☆偀鍋撻柡灞诲劜濡叉悂宕ラ敃鈧ぐ鍙夌閵夆晛娅㈤柛娑滄閹洟宕洪悢閿嬪€?
     * @param area 闁糕晝鍠庨幃鏇㈠极閻楀牆绁?
     * @param playerName 闁绘壕鏅涢宥夊触瀹ュ泦?
     * @param isAdmin 闁哄嫷鍨伴幆浣圭▔閾忚鍚€闁荤偛妫楅幉?
     * @return 闁哄嫷鍨伴幆渚€宕ｉ娆庣鞍闂佹彃绉撮幊锟犲触?
     */
    private static boolean canRenameArea(AreaData area, String playerName, boolean isAdmin) {
        // 缂佺媴绱曢幃濠囧川濡搫璁插ù鐘劦閸ｆ悂宕ㄩ挊澶嬪€抽柟纰樺亾闁哄牆顦悡娆撳触?
        if (isAdmin) {
            return true;
        }

        // 闁哄拋鍣ｉ埀顒佹皑鐢櫣鈧娉涜ぐ褔鎳楁禒瀣闁告稖妫勯幃鏇㈡嚊椤忓嫮绠掗柛鎺撶☉缂傛捇鎯冮崟顐ゅ幍闁告艾绋勭槐妾歩gnature闁告牕缍婇崢銈夋晬?
        return area.getSignature() != null && area.getSignature().equals(playerName);
    }

    /**
     * 闁告瑦鍨块埀顑跨窔閸ｆ悂宕ㄩ挊澶嬪€抽柛婵嗙Т缁ㄦ煡宕氶弶娆惧悅闁规挳顥撻?
     * @param player 闁绘壕鏅涢?
     * @param success 闁哄嫷鍨伴幆渚€骞嬮幇顒€顫?
     * @param message 婵炴垵鐗婃导?
     */
    private static void sendRenameResponse(ServerPlayerEntity player, boolean success, Part... parts) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("rename_response");
        buf.writeBoolean(success);
        TranslatableMessage.write(buf, parts);
        ServerPlayNetworking.send(player, Packets.S2C_RENAME_RESPONSE, buf);
    }

    /**
     * 婵炲鍔岄崬浠嬪嫉瀹ュ懎顫ょ紒鏃戝灣缂嶅绱掑鍕闁衡偓鐠虹儤鐝?
     */
    public static void registerServerReceivers() {
        // 婵炲鍔岄崬浠嬫煂瀹ュ懏鍤掗柛姘Х椤曨剙效閸屾稑澶嶉柡鈧捄鐑樼彜
        ServerPlayNetworking.registerGlobalReceiver(Packets.C2S_RENAME_REQUEST,
            (server, player, handler, buf, responseSender) -> {
                try {
                    String oldName = buf.readString();
                    String newName = buf.readString();
                    String newSurfaceName = buf.readString();
                    String dimension = buf.readString();

                    server.execute(() -> {
                        handleRenameRequest(player, oldName, newName, newSurfaceName, dimension);
                    });

                } catch (Exception e) {
                    Areashint.LOGGER.error(ServerI18nManager.translate("command.error.rename"), e);
                    sendRenameResponse(player, false, key("command.error.general_23"), lit(e.getMessage()));
                }
            });
    }
}
