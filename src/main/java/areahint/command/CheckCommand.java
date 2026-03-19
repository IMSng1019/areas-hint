package areahint.command;

import areahint.util.TextCompat;

import areahint.Areashint;
import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.ServerI18nManager;
import areahint.util.SurfaceNameHandler;
import areahint.world.WorldFolderManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Check闂佸憡绋掗崹婵嬪箮閵堝棗绶為柛鏇ㄥ幗閸婄偤鏌?
 * 婵犮垼娉涚€氼噣骞?/areahint check 闂佸憡绋掗崹婵嬪箮閵堝鏅悘鐐靛亾閳绘梻绱掗埀顒併偊婢跺摜鐭掗梺鍛婅壘閻楀棝鎮″▎鎾宠Е鐎广儱鎳忕€氭煡鏌涢埡鍐ㄦ瀻闁诡喗娲滈幏鐘绘晜閸撗呯厔婵烇絽娲犻崜婵囧?
 */
public class CheckCommand {
    
    /**
     * 濠电偛顦崝宀勫船缁叉eck闂佸憡绋掗崹婵嬪箮?
     * @param dispatcher 闂佸憡绋掗崹婵嬪箮閵堝绀嗛柛鈩冾殔缁叉椽鏌?
     * @param registryAccess 濠电偛顦崝宀勫船閻ｅ本鍋橀柕濠庣厛閸熷繘姊?
     * @param environment 闂佺粯绮犻崹浼淬€?
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        
        dispatcher.register(
            CommandManager.literal("areahint")
                .then(CommandManager.literal("check")
                    .requires(source -> source.hasPermissionLevel(0)) // 闂佸搫顦崯鏉戭瀶閾忓湱椹冲璺虹焸閻?闂佹寧绋戞總鏃傜箔婢舵劖顥嗛柍褜鍓涢幉鐗堟媴娓氼垰鎮侀梺鑽ゅ仜濡骞?
                    .executes(CheckCommand::executeCheckAll)
                    .then(CommandManager.argument("unionName", StringArgumentType.string())
                        .suggests(createUnionNameSuggestionProvider())
                        .executes(CheckCommand::executeCheckUnion)
                    )
                )
        );
    }
    
    /**
     * 闂佸湱鐟抽崱鈺傛杸 /areahint check 闂佸憡绋掗崹婵嬪箮?- 闂佸搫瀚晶浠嬪Φ濮樿泛绠ラ柍褜鍓熷鍨緞鎼存繄鐭掗梺鍛婅壘閻楀棝鎮″▎鎾宠Е?
     * @param context 闂佸憡绋掗崹婵嬪箮閵堝棛鈻斿┑鐘辫兌閻熸捇鏌?
     * @return 闂佸憡绋掗崹婵嬪箮閵堝洨纾奸柟鎯ь嚟娴?
     */
    private static int executeCheckAll(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            ServerPlayerEntity player = CommandSourceCompat.getPlayerOrThrow(source);
            RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
            String dimensionId = dimensionType.getValue().toString();
            
            // 闂佸吋鍎抽崲鑼躲亹閸ヮ亗浜归柟鎯у暱椤ゅ懐绱撴担瑙勭稇閻庤濞婇幆鍐礋椤愩倕骞嶉梺鍛婅壘缁夌敻寮搁崘鈺冾浄?
            String fileName = getDimensionFileName(dimensionId);
            Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
            
            if (areaFile == null || !areaFile.toFile().exists()) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.dimension_2").formatted(Formatting.RED));
                return 1;
            }

            // 闁荤姴娲╅褑銇愰崶顒€鏄ラ柣鏃堟敱閸婃娊鏌℃担鍝勵暭鐎?
            List<AreaData> areas = FileManager.readAreaData(areaFile);

            if (areas.isEmpty()) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.dimension").formatted(Formatting.RED));
                return 1;
            }
            
            // 闂佽　鍋撻柛顐ｆ礃閼茬娀鏌熺喊妯轰壕闂佸搫鐗嗛ˇ铏閸儱瑙﹂柛顐ｇ箘閸樼敻鏌?
            Map<String, List<AreaData>> unionNameGroups = new LinkedHashMap<>();
            
            for (AreaData area : areas) {
                String unionName = area.getSurfacename();
                if (unionName == null || unionName.trim().isEmpty()) {
                    unionName = area.getName(); // 婵犵鈧啿鈧綊鎮樻径濞炬煢闁斥晛鍟粻鎺楁煠鏉堛劍鐓ラ柟顔奸叄瀹曟椽鎮㈤柨瀣偓鎶芥煥濞戞ê顨欏┑鐐叉喘閹粙濡搁妷褎婢栭梻鍌氬閹冲酣鎮″▎鎾宠Е?
                }
                
                unionNameGroups.computeIfAbsent(unionName, k -> new ArrayList<>()).add(area);
            }
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戣法鍔嶉柣鏍电稻閿?
            CommandSourceCompat.sendMessage(source, TextCompat.literal(""));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.title.area.surface.list").formatted(Formatting.GOLD));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.surface_5").formatted(Formatting.GRAY));
            CommandSourceCompat.sendMessage(source, TextCompat.literal(""));
            
            // 闂佸搫瀚晶浠嬪Φ濮橆儷鎺曠疀鎼淬劌娈濋梺鑹伴哺閺屻劑骞冩惔銊ユ槬闁绘棃鏀遍崐?
            for (Map.Entry<String, List<AreaData>> entry : unionNameGroups.entrySet()) {
                String unionName = entry.getKey();
                List<AreaData> areasInUnion = entry.getValue();
                
                MutableText unionText = TextCompat.literal("闁归棿绻橀梺?" + unionName).formatted(Formatting.GREEN);
                
                // 濠电儑缍€椤曆勬叏閻愬搫绠栨い鎺嗗亾濞寸姷濞€楠炴捇骞囬杞扮驳
                MutableText hoverContent = TextCompat.literal(ServerI18nManager.translateForPlayer(player.getUuid(), "command.message.area.surface_4") + unionName + "\n");
                hoverContent.append(TextCompat.literal(ServerI18nManager.translateForPlayer(player.getUuid(), "command.message.general_8") + areasInUnion.size() + ServerI18nManager.translateForPlayer(player.getUuid(), "command.message.area_3")));

                for (int i = 0; i < areasInUnion.size(); i++) {
                    AreaData area = areasInUnion.get(i);
                    hoverContent.append(TextCompat.literal("闁归棿鍜? " + area.getName()));
                    if (i < areasInUnion.size() - 1) {
                        hoverContent.append(TextCompat.literal("\n"));
                    }
                }

                unionText.styled(style -> style
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint check \"" + unionName + "\""))
                );
                
                CommandSourceCompat.sendMessage(source, unionText);
            }
            
            CommandSourceCompat.sendMessage(source, TextCompat.literal(""));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.general_9").append(TextCompat.literal(String.valueOf(unionNameGroups.size()))).append(TextCompat.translatable("command.message.area.surface_2")).append(TextCompat.literal(String.valueOf(areas.size()))).append(TextCompat.translatable("command.message.area_2")).formatted(Formatting.GRAY));
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.general_21"), e);
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_2").append(TextCompat.literal(e.getMessage())).formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * 闂佸湱鐟抽崱鈺傛杸 /areahint check <闂佽壈椴搁弻銊╁箖鎼淬劌鏄ラ柣鏃堟敱閸? 闂佸憡绋掗崹婵嬪箮?- 闂佸搫瀚晶浠嬪Φ濮樿泛绠伴柛銉戝懏姣庨梺鑹伴哺閺屻劑骞冩惔銊ユ槬闁绘棃鏀遍崐鎶芥煟閵娿儱顏い鏇熺〒缁辨帡宕遍弴姘辩畾闂?
     * @param context 闂佸憡绋掗崹婵嬪箮閵堝棛鈻斿┑鐘辫兌閻熸捇鏌?
     * @return 闂佸憡绋掗崹婵嬪箮閵堝洨纾奸柟鎯ь嚟娴?
     */
    private static int executeCheckUnion(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String unionName = StringArgumentType.getString(context, "unionName");
        
        try {
            ServerPlayerEntity player = CommandSourceCompat.getPlayerOrThrow(source);
            RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
            String dimensionId = dimensionType.getValue().toString();
            
            // 闂佸吋鍎抽崲鑼躲亹閸ヮ亗浜归柟鎯у暱椤ゅ懐绱撴担瑙勭稇閻庤濞婇幆鍐礋椤愩倕骞嶉梺鍛婅壘缁夌敻寮搁崘鈺冾浄?
            String fileName = getDimensionFileName(dimensionId);
            Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
            
            if (areaFile == null || !areaFile.toFile().exists()) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.dimension_2").formatted(Formatting.RED));
                return 1;
            }

            // 闁荤姴娲╅褑銇愰崶顒€鏄ラ柣鏃堟敱閸婃娊鏌℃担鍝勵暭鐎?
            List<AreaData> areas = FileManager.readAreaData(areaFile);

            // 闂佸搫琚崕鍙夌珶濮椻偓瀹曠姾銇愰幒鎴濊祴闂佹眹鍔岀€氼剟鎮″▎鎾宠Е?
            List<AreaData> matchedAreas = new ArrayList<>();

            for (AreaData area : areas) {
                String areaUnionName = area.getSurfacename();
                if (areaUnionName == null || areaUnionName.trim().isEmpty()) {
                    areaUnionName = area.getName();
                }

                if (areaUnionName.equals(unionName)) {
                    matchedAreas.add(area);
                }
            }

            if (matchedAreas.isEmpty()) {
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.area.surface").append(TextCompat.literal(unionName)).formatted(Formatting.RED));
                return 1;
            }
            
            // 闂佸憡鐟﹂崹鍧楀焵椤戞寧绁版い鏇熺〒缁辨帡宕遍弴姘辩畾闂?
            CommandSourceCompat.sendMessage(source, TextCompat.literal(""));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.title.area.surface").formatted(Formatting.GOLD));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.surface_4").append(TextCompat.literal(unionName)).formatted(Formatting.GOLD));
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area_6").append(TextCompat.literal(String.valueOf(matchedAreas.size()))).formatted(Formatting.GRAY));
            CommandSourceCompat.sendMessage(source, TextCompat.literal(""));
            
            // 闂佸搫瀚晶浠嬪Φ濮橆儷鎺曠疀鎼淬劌娈濋梺绯曟櫇閸犲酣骞冮弴銏″剭闁告洦鍣崵濠勭磽娴ｅ憡顥欏ǎ鍥э躬楠?
            for (int i = 0; i < matchedAreas.size(); i++) {
                AreaData area = matchedAreas.get(i);
                
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area_9").append(TextCompat.literal((i + 1) + ":")).formatted(Formatting.GREEN));
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area").append(TextCompat.literal(area.getName())));
                
                if (area.getSurfacename() != null && !area.getSurfacename().trim().isEmpty()) {
                    CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.surface").append(TextCompat.literal(area.getSurfacename())));
                }
                
                CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.level").append(TextCompat.literal(String.valueOf(area.getLevel()))).append(TextCompat.translatable("command.message.general_29")).formatted(Formatting.YELLOW));
                
                if (area.getBaseName() != null) {
                    CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.area.parent").append(TextCompat.literal(area.getBaseName())));
                }
                
                if (area.getSignature() != null) {
                    CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.general").append(TextCompat.literal(area.getSignature())));
                }
                
                if (area.getColor() != null) {
                    CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.color").append(TextCompat.literal(area.getColor())));
                }
                
                // 闂佸搫瀚晶浠嬪Φ濮橆収娈楁俊顖氭惈椤斿﹤菐閸ワ絽澧插ù?
                if (area.getAltitude() != null) {
                    MutableText altitudeText = TextCompat.translatable("command.message.altitude");
                    if (area.getAltitude().getMin() != null && area.getAltitude().getMax() != null) {
                        altitudeText.append(TextCompat.literal(String.valueOf(area.getAltitude().getMin()))).append(TextCompat.translatable("command.message.general_4")).append(TextCompat.literal(String.valueOf(area.getAltitude().getMax())));
                    } else if (area.getAltitude().getMin() != null) {
                        altitudeText.append(TextCompat.literal(String.valueOf(area.getAltitude().getMin()))).append(TextCompat.translatable("command.message.general_2"));
                    } else if (area.getAltitude().getMax() != null) {
                        altitudeText.append(TextCompat.literal(String.valueOf(area.getAltitude().getMax()))).append(TextCompat.translatable("command.message.general_3"));
                    } else {
                        altitudeText.append(TextCompat.translatable("command.message.general_25"));
                    }
                    CommandSourceCompat.sendMessage(source, altitudeText);
                } else {
                    CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.altitude_2"));
                }
                
                // 闂佸搫瀚晶浠嬪Φ濮橆厹浜滈柛鎾茬娴狀垰菐閸ワ絽澧插ù?
                if (area.getVertices() != null && !area.getVertices().isEmpty()) {
                    CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.message.vertex").append(TextCompat.literal(String.valueOf(area.getVertices().size()))));
                    
                    // 闂佸搫瀚晶浠嬪Φ濮樿泛绀堢€广儱鎳庡▓銈呪槈閹垮啩閭柕鍡楋躬閹瑩鏌呭☉姘扁枙婵炴垶鎹佸▍锝夊Φ濮橆厾鐟?
                    MutableText verticesText = TextCompat.translatable("command.message.vertex.coordinate");
                    int maxShow = Math.min(3, area.getVertices().size());
                    for (int j = 0; j < maxShow; j++) {
                        AreaData.Vertex vertex = area.getVertices().get(j);
                        verticesText.append(TextCompat.literal("(" + vertex.getX() + ", " + vertex.getZ() + ")"));
                        if (j < maxShow - 1) {
                            verticesText.append(TextCompat.literal(", "));
                        }
                    }
                    if (area.getVertices().size() > 3) {
                        verticesText.append(TextCompat.literal("..."));
                    }
                    CommandSourceCompat.sendMessage(source, verticesText);
                }
                
                if (i < matchedAreas.size() - 1) {
                    CommandSourceCompat.sendMessage(source, TextCompat.literal(""));
                }
            }
            
        } catch (Exception e) {
            Areashint.LOGGER.error(ServerI18nManager.translate("command.error.general_21"), e);
            CommandSourceCompat.sendMessage(source, TextCompat.translatable("command.error.general_2").append(TextCompat.literal(e.getMessage())).formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * 闂佸憡甯楃粙鎴犵磽閹剧粯鍤傞柡鍌涘閸娿倝鏌涢埡鍐ㄦ瀻闁诡喗娲栭娆愩偊濞嗘儳鏁堕梺鍦帛閸旀帞娆㈤悽绋块棷?
     * @return 閻庣偣鍊濈紓姘额敊閸涙潙绠甸柟閭︿簽鏉╂棃鏌?
     */
    public static SuggestionProvider<ServerCommandSource> createUnionNameSuggestionProvider() {
        return (context, builder) -> {
            try {
                ServerPlayerEntity player = CommandSourceCompat.getPlayerOrThrow(context.getSource());
                RegistryKey<World> dimensionType = player.getWorld().getRegistryKey();
                String dimensionId = dimensionType.getValue().toString();
                
                // 闂佸吋鍎抽崲鑼躲亹閸ヮ亗浜归柟鎯у暱椤ゅ懐绱撴担瑙勭稇閻庤濞婇幆鍐礋椤愩倕骞嶉梺鍛婅壘缁夌敻寮搁崘鈺冾浄?
                String fileName = getDimensionFileName(dimensionId);
                Path areaFile = WorldFolderManager.getWorldDimensionFile(fileName);
                
                if (areaFile == null || !areaFile.toFile().exists()) {
                    return Suggestions.empty();
                }
                
                // 闁荤姴娲╅褑銇愰崶顒€鏄ラ柣鏃堟敱閸婃娊鏌℃担鍝勵暭鐎?
                List<AreaData> areas = FileManager.readAreaData(areaFile);
                
                // 闂佽　鍋撻柛顐ｆ礃閼茬娀鏌熺喊妯轰壕闂佸搫鐗嗛ˇ铏閸儱瑙﹂柛顐ｇ箘閸樼敻鏌?
                Set<String> unionNames = new LinkedHashSet<>();
                
                for (AreaData area : areas) {
                    String unionName = area.getSurfacename();
                    if (unionName == null || unionName.trim().isEmpty()) {
                        unionName = area.getName();
                    }
                    unionNames.add(unionName);
                }
                
                // 闁哄鏅涘ú锕傚箮閵堝宓侀柤鎼佹涧濞兼垿鏌涢弮鍌毿㈢紓鍌涙尵閹?
                String input = builder.getRemaining().toLowerCase();
                for (String unionName : unionNames) {
                    if (unionName.toLowerCase().contains(input)) {
                        builder.suggest("\"" + unionName + "\"");
                    }
                }
                
            } catch (Exception e) {
                // 闂傚倸鐗婇悷鈺冨垝椤栨稑绶為柛鏇ㄥ幗閸婄偤姊洪幐搴ｆ噯妞ゆ洏鍊濋弫宥呯暆閸愵亞顔夐梺鎼炲劤閸嬬喖鍩為弽褜鍤堝Δ锔筋儥閸?
            }
            
            return builder.buildFuture();
        };
    }
    
    /**
     * 闂佸搫绉烽～澶婄暤娴ｈ櫣纾奸柡澶嬪灥椤斿D闂佸吋鍎抽崲鑼躲亹閸ヮ剙妫橀柛銉檮椤愪粙鏌?
     * @param dimensionId 缂傚倷鐒﹀娆戔偓鍦吋D
     * @return 闂佸搫鍊稿ú锝呪枎閵忋倕瑙?
     */
    private static String getDimensionFileName(String dimensionId) {
        switch (dimensionId) {
            case "minecraft:overworld":
                return "overworld.json";
            case "minecraft:the_nether":
                return "the_nether.json";
            case "minecraft:the_end":
                return "the_end.json";
            default:
                return "overworld.json";
        }
    }
} 


