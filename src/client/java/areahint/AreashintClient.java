package areahint;

import areahint.config.ClientConfig;
import areahint.detection.AreaDetector;
import areahint.network.ClientNetworking;
import areahint.render.RenderManager;
import areahint.file.FileManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.io.IOException;

/**
 * 区域提示模组 - 客户端主类
 * 负责客户端的初始化、配置管理和玩家位置检测
 */
public class AreashintClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Areashint.MOD_ID + "-client");
	
	// 配置文件名
	private static final String CONFIG_FILE = "config.json";
	
	// 区域检测器和渲染管理器实例
	private static AreaDetector areaDetector;
	private static RenderManager renderManager;
	
	// 当前维度标识符
	private static Identifier currentDimension;
	
	// 当前区域名称（用于比较变化）
	private static String currentAreaName = null;
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("区域提示模组客户端初始化中...");
		
		// 检查并创建配置目录
		initConfigDir();
		
		// 初始化配置
		ClientConfig.init();
		
		// 初始化区域检测器
		areaDetector = new AreaDetector();
		
		// 初始化渲染管理器
		renderManager = new RenderManager();
		
		// 初始化网络处理
		ClientNetworking.init();
		
		// 初始化EasyAdd功能
		initEasyAdd();
		
		// 注册客户端tick事件
		registerTickEvents();
		
		LOGGER.info("区域提示模组客户端初始化完成!");
	}
	
	/**
	 * 初始化EasyAdd功能
	 */
	private void initEasyAdd() {
		try {
			// 注册按键处理器
			areahint.easyadd.EasyAddKeyHandler.register();
			
			// 注册客户端网络接收器
			areahint.easyadd.EasyAddNetworking.registerClientReceivers();
			
			LOGGER.info("EasyAdd功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化EasyAdd功能时发生错误", e);
		}
	}
	
	/**
	 * 检查并创建外部配置目录
	 */
	private void initConfigDir() {
		try {
			// 使用FileManager获取配置目录
			Path configDirPath = FileManager.checkFolderExist();
			// 创建配置文件
			FileManager.createDefaultConfigFile(FileManager.getConfigFile(CONFIG_FILE));
			LOGGER.info("配置目录初始化完成：{}", configDirPath);
		} catch (IOException e) {
			LOGGER.error("创建配置文件失败: " + e.getMessage());
		}
	}
	
	/**
	 * 注册客户端tick事件，用于检测玩家位置
	 */
	private void registerTickEvents() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity player = client.player;
			if (player == null || client.world == null) {
				return;
			}
			
			// 检查当前维度，如果改变则重新加载区域数据
			Identifier dimension = client.world.getDimensionKey().getValue();
			if (currentDimension == null || !currentDimension.equals(dimension)) {
				currentDimension = dimension;
				String dimensionFileName = getDimensionFileName(currentDimension);
				LOGGER.info("检测到维度变化为：{}，加载对应的区域文件：{}", dimension.toString(), dimensionFileName);
				areaDetector.loadAreaData(dimensionFileName);
			}
			
			// 按照配置的频率检测玩家位置
			if (areaDetector.shouldDetect()) {
				double playerX = player.getX();
				double playerY = player.getY();
				double playerZ = player.getZ();
				String areaName = areaDetector.detectPlayerArea(playerX, playerY, playerZ);
				
				// 添加调试日志，无论是否有变化都记录当前检测结果
				LOGGER.debug("玩家位置检测 - 坐标：({}, {}, {})，检测到区域：{}，之前区域：{}", 
						playerX, playerY, playerZ, areaName, currentAreaName);
				
				// 如果区域发生变化，显示新的区域名称
				if ((areaName == null && currentAreaName != null) || 
					(areaName != null && !areaName.equals(currentAreaName))) {
					if (areaName == null) {
						LOGGER.info("玩家（{}, {}, {}）离开区域：{}", playerX, playerY, playerZ, currentAreaName);
					} else if (currentAreaName == null) {
						LOGGER.info("玩家（{}, {}, {}）进入区域：{}", playerX, playerY, playerZ, areaName);
					} else {
						LOGGER.info("玩家（{}, {}, {}）从区域 {} 进入区域：{}", playerX, playerY, playerZ, currentAreaName, areaName);
					}
					
					currentAreaName = areaName;
					if (areaName != null) {
						LOGGER.info("尝试显示区域名称：{}", areaName);
						renderManager.showAreaTitle(areaName);
					}
				}
			}
		});
	}
	
	/**
	 * 获取外部配置目录路径
	 * @return 配置目录路径
	 */
	public static Path getConfigDir() {
		return FileManager.getConfigFolder();
	}
	
	/**
	 * 获取配置文件路径
	 * @return 配置文件路径
	 */
	public static Path getConfigFile() {
		return FileManager.getConfigFile(CONFIG_FILE);
	}
	
	/**
	 * 获取区域检测器实例
	 * @return 区域检测器
	 */
	public static AreaDetector getAreaDetector() {
		return areaDetector;
	}
	
	/**
	 * 获取渲染管理器实例
	 * @return 渲染管理器
	 */
	public static RenderManager getRenderManager() {
		return renderManager;
	}
	
	/**
	 * 重新加载配置和区域数据
	 */
	public static void reload() {
		LOGGER.info("重新加载区域提示模组配置和区域数据...");
		ClientConfig.load();
		if (currentDimension != null) {
			String dimensionFileName = getDimensionFileName(currentDimension);
			LOGGER.info("重新加载维度{}的区域文件：{}", currentDimension.toString(), dimensionFileName);
			
			// 获取文件路径并检查是否存在
			Path areaFile = FileManager.getDimensionFile(dimensionFileName);
			LOGGER.info("[调试] 重新加载区域文件路径: {}", areaFile.toAbsolutePath());
			if (java.nio.file.Files.exists(areaFile)) {
				try {
					String content = java.nio.file.Files.readString(areaFile);
					LOGGER.info("[调试] 区域文件大小: {} 字节", content.length());
				} catch (Exception e) {
					LOGGER.error("读取区域文件失败", e);
				}
			} else {
				LOGGER.warn("[调试] 区域文件不存在: {}", areaFile.toAbsolutePath());
			}
			
			areaDetector.loadAreaData(dimensionFileName);
		}
	}
	
	/**
	 * 根据维度标识符获取对应的区域数据文件名（静态方法）
	 * @param dimension 维度标识符
	 * @return 区域数据文件名
	 */
	public static String getDimensionFileName(Identifier dimension) {
		if (dimension.getPath().equals("the_nether")) {
			return Areashint.NETHER_FILE;
		} else if (dimension.getPath().equals("the_end")) {
			return Areashint.END_FILE;
		} else {
			return Areashint.OVERWORLD_FILE;
		}
	}
}