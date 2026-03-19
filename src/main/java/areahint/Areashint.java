package areahint;

import areahint.util.TextCompat;

import areahint.command.ServerCommands;
import areahint.i18n.ServerI18nManager;
import areahint.network.ServerNetworking;
import areahint.file.FileManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

/**
 * 闁告牕鎼悡娆撳箵閹邦喓浠涙俊顖ょ磿缁?- 闁哄牆绉存慨鐔虹博椤栨瑥鐦滅紒?
 * 閻犳劗鍠曢惌妤呭嫉瀹ュ懎顫ょ紒鏃戝灣濞堟垿宕氬┑鍡╂綏闁告牗鐗撻埀顑跨閹斥剝绂掗妶鍡樻殘闁告劕鑻幏浼村棘閸ワ附顐界紒鐙呯磿閹?
 */
public class Areashint implements ModInitializer {
	public static final String MOD_ID = "areas-hint";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	// 闁告艾瀚ǎ顔芥償閿曗偓鐏忣垶宕洪悢鍛婃闁硅鍠楅弸鍐╃鐠虹儤鍊?
	public static final String OVERWORLD_FILE = "overworld.json";
	public static final String NETHER_FILE = "the_nether.json";
	public static final String END_FILE = "the_end.json";
	
	// 闁哄牆绉存慨鐔煎闯閵娿儳鏉藉〒姘儏缁扁晠鎮?
	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("闁告牕鎼悡娆撳箵閹邦喓浠涙俊顖ょ磿缁秹寮靛鍛潳缂佹棏鍨伴崹鍨叏鐎ｎ亜顕у☉?..");

		// 闁告帗绻傞～鎰板礌閺嶃劍绠涢柛鏃撶磿椤忣剟宕舵禒瀣€庨柛?
		ServerI18nManager.init();

		// 闁告帗绻傞～鎰板礌閺嶃劍鐎ù鐘插椤撴悂鎮?
		initConfigDir();

		// 闁告帗绻傞～鎰板礌閺嶃劍绠涢柛鏃撶磿椤忣剟寮妷銉х缂佺媴绱曢幃濠囧闯?
		areahint.log.ServerLogManager.init();

		// 闁告帗绻傞～鎰板礌閺嶃劍绠涢柛鏃撶磿椤忣剟寮妷銉х缂傚啯鍨圭划鑸靛緞閸曨厽鍊?
		areahint.log.ServerLogNetworking.init();
		
		// 闁告帗绻傞～鎰板礌閺嶎偅妯婇幖杈剧畱閻撴瑩宕ュ鍥跺悁闁荤偛妫楀▍?
		areahint.dimensional.DimensionalNameManager.init();
		
		// 闁告帗绻傞～鎰板礌閺嶎偅妯婇幖杈剧畱閻撴瑩宕ュ鍥╃Ч缂備焦绮岄ˇ鈺呮偠?
		areahint.network.DimensionalNameNetworking.init();
		
		// 闁告帗绻傞～鎰板礌閺嶃劍绠涢柛鏃撶磿椤忣剚绋夐弽顐ｆ珪缂傚啯鍨圭划鑸靛緞閸曨厽鍊?
		areahint.network.ServerWorldNetworking.init();
		
		// 婵炲鍔岄崬浠嬪嫉瀹ュ懎顫ら柛锝冨妼閹酣宕濋妸銈囩殤濞寸姴澧庡ú鍐触椤掆偓濞?
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		// 婵炲鍔岄崬浠嬪嫉瀹ュ懎顫ら柛锝冨妼娴犵姴顫㈤～顓犵殤濞寸姴澧庡ú鍐触椤掆偓濞?
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
		
		// 闁告帗绻傞～鎰板礌閺嶃劍绠涢柛鏃撶磿椤忣剛绱旈幋鐘垫崟濠㈣泛瀚幃?
		ServerNetworking.init();
		
		// 闁告帗绻傞～鎰板礌閺堝穬syAdd闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.easyadd.EasyAddServerNetworking.registerServerReceivers();

		// 闁告帗绻傞～鎰板礌閺堫晪name闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.command.RenameAreaCommand.registerServerReceivers();

		// 闁告帗绻傞～鎰板礌閺堝窡pandArea闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.expandarea.ExpandAreaServerNetworking.registerServerNetworking();
		
		// 闁告帗绻傞～鎰板礌閺堫柈rinkArea闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.shrinkarea.ShrinkAreaServerNetworking.registerServerNetworking();

		// 闁告帗绻傞～鎰板礌閺堝崓dHint闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.addhint.AddHintServerNetworking.registerServerReceivers();

		// 闁告帗绻傞～鎰板礌閺堝當leteHint闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.deletehint.DeleteHintServerNetworking.registerServerReceivers();

		// 闁告帗绻傞～鎰板礌閺堝videArea闁哄牆绉存慨鐔虹博椤栨粎绉圭紓浣圭矊椤︹晠鎮?
		areahint.dividearea.DivideAreaServerNetworking.registerServerNetworking();
		
		// 婵炲鍔岄崬鐣岀磼閺夋垵顔婇柛娆惿戝ú鎸庣鐎ｂ晜顐?- 濡絾鐗楅鍏兼交濞戞ê寮抽柡鍫簻閹筹繝宕ュ鍥ㄦ▕閹艰揪闄勫鍌炲箵閹邦喓浠汷P闁告稖妫勯幃?
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			String dimId = destination.getRegistryKey().getValue().toString();
			if (player.hasPermissionLevel(2) && !areahint.dimensional.DimensionalNameManager.hasDimensionalName(dimId)) {
				// 闁煎浜滄慨鈺佲枖閵娿儱鏂€閻犲洢鍎冲ǎ顔芥償閿旇偐绀勫ù锝堟硶閺併倗绱掗弶鎴濐唺ID濞达絾绮堢拹鐔割渶濡鍚囬柛姘Ф琚ㄩ柨?
				areahint.dimensional.DimensionalNameManager.setDimensionalName(dimId, dimId);
				areahint.dimensional.DimensionalNameManager.saveDimensionalNames();
				// 闁圭粯鍔楅妵姝凱闁绘壕鏅涢宥夊川閽樺鍊?
				MutableText msg = TextCompat.translatable("message.message.dimension_3").append(TextCompat.literal(dimId));
				player.sendMessage(msg, false);
				MutableText nameBtn = TextCompat.translatable("message.button.dimension_3")
					.setStyle(Style.EMPTY
						.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
							"/areahint dimensionalityname select \"" + dimId + "\""))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							TextCompat.translatable("message.hover.dimension.setname").append(TextCompat.literal(dimId)))));
				player.sendMessage(nameBtn, false);
				// 闁告艾鏈鐐电磼濞嗗浚鍚傞柟鎾棑椤?
				areahint.network.DimensionalNameNetworking.sendDimensionalNamesToClient(player);
			}
		});

		// 婵炲鍔岄崬浠嬪川閹存帗濮?
		ServerCommands.register();
		
		LOGGER.info("闁告牕鎼悡娆撳箵閹邦喓浠涙俊顖ょ磿缁秹寮靛鍛潳缂佹棏鍨伴崹鍨叏鐎ｎ亜顕ч悗鐟版湰閸?");
	}
	
	/**
	 * 婵☆偀鍋撻柡灞诲劚閼荤喖宕氬☉妯肩处濠㈣埖鐗犻崕鎾煀瀹ュ洨鏋傞柣鈺婂枛缂?
	 */
	private void initConfigDir() {
		try {
			// 濞达綀娉曢弫顥琲leManager闁兼儳鍢茶ぐ鍥煀瀹ュ洨鏋傞柣鈺婂枛缂?
			Path configDirPath = FileManager.checkFolderExist();
			LOGGER.info("闂佹澘绉堕悿鍡涙儎椤旇偐绉块柛鎺撶箓椤劙宕犻弽褏鏆氶柟? {}", configDirPath);
			
			// 婵炲鍔嶉崜浼存晬濮樺墎甯涢悹浣靛€曠亸顖炲春閻斿憡鐎ù鐘插楠炲洭宕烽妸銉ф闁革负鍔嬬粭姗€鎮剧仦鐐€ù鐘烘硾閵囨瑩宕氬┑鍡╂綏闁告牗鐗楀鍌炲礆濞戞绱?
			
		} catch (Exception e) {
			LOGGER.error("闁告帗绻傞～鎰板礌閺嶎厼甯崇紓鍐惧枤濞叉媽銇愰弴鐐杭閻? " + e.getMessage());
		}
	}
	
	/**
	 * 闁哄牆绉存慨鐔煎闯閵娿儲鍎欓柛鏂诲妺缁ㄣ劍绂掔捄渚П闁?
	 * @param minecraftServer 闁哄牆绉存慨鐔煎闯閵娿儳鏉藉〒?
	 */
	private void onServerStarting(MinecraftServer minecraftServer) {
		server = minecraftServer;
        LOGGER.info("Server starting");
		
		// 闁告帗绻傞～鎰板礌閺嶏妇鐟柣锝呮湰閺嬪啯绂掔捄鎭掍粴缂佺媴绱曢幃濠囧闯?
		areahint.world.WorldFolderManager.initializeServerWorld(minecraftServer);

		// 濞戞挻鐗滈弲顐﹀棘閸ワ附顐藉鍓佹嚀濮樸劎绱掗鍕€甸梺鎻掔У閺屽﹪宕濋悩鐑樼グ缂備焦娼欑€规娊宕洪悢閿嬪€抽柨娑樻綗nit闁哄啯婀圭粭姗€鎮惧畝鍐唴鐎垫澘瀚惃濠氬嫉椤忓嫬鐏ュ┑顔碱儏鐎垫煡鏁?
		areahint.dimensional.DimensionalNameManager.loadDimensionalNames();
	}
	
	/**
	 * 闁哄牆绉存慨鐔煎闯閵娿儰绮绘慨婵愭線缁ㄣ劍绂掔捄渚П闁?
	 * @param minecraftServer 闁哄牆绉存慨鐔煎闯閵娿儳鏉藉〒?
	 */
	private void onServerStopped(MinecraftServer minecraftServer) {
		server = null;
        LOGGER.info("Server stopped");

		// 闁稿繑濞婂Λ鎾嫉瀹ュ懎顫ょ紒鏃戝灡濡晞绠涘Δ鍐惧悁闁荤偛妫楀▍?
		areahint.log.ServerLogManager.shutdown();
	}
	
	/**
	 * 闁兼儳鍢茶ぐ鍥嫉瀹ュ懎顫ら柛锝冨妼閻ゅ嫭绗?
	 * @return 鐟滅増鎸告晶鐘诲嫉瀹ュ懎顫ら柛锝冨妼閻ゅ嫭绗?
	 */
	public static MinecraftServer getServer() {
		return server;
	}
	
	/**
	 * 闁兼儳鍢茶ぐ鍥ㄥ緞閺嶎厼鍔ラ梺鏉跨Ф閻ゅ棝鎯勯鑲╃Э閻犱警鍨扮欢?
	 * @return 闂佹澘绉堕悿鍡涙儎椤旇偐绉块悹渚灠缁?
	 */
	public static Path getConfigDir() {
		return FileManager.getConfigFolder();
	}
}
