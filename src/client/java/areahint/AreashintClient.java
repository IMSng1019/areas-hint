package areahint;

import areahint.config.ClientConfig;
import areahint.detection.AreaDetector;
import areahint.network.ClientNetworking;
import areahint.render.RenderManager;
import areahint.file.FileManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
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
	// 当前服务器地址（用于检测服务器变化）
	private static String currentServerAddress = null;
	// 上一次tick时玩家是否为null（用于检测进入世界）
	private static boolean wasPlayerNull = true;
	private static boolean hasShownDimensionalName = false; // 是否已经显示过维度域名
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("区域提示模组客户端初始化中...");

		// 检查并创建配置目录
		initConfigDir();

		// 初始化客户端日志管理器
		areahint.log.ClientLogManager.init();

		// 初始化配置
		ClientConfig.init();
		
		// 初始化区域检测器
		areaDetector = new AreaDetector();
		
		// 初始化渲染管理器
		renderManager = new RenderManager();
		
		// 初始化维度域名管理器
		areahint.dimensional.ClientDimensionalNameManager.init();
		
		// 初始化网络处理
		ClientNetworking.init();
		
		// 初始化维度域名网络处理
		areahint.network.ClientDimensionalNameNetworking.init();
		
		// 初始化客户端世界网络处理
		areahint.network.ClientWorldNetworking.init();
		
		// 初始化SetHigh客户端命令
		areahint.command.SetHighClientCommand.init();
		
		// 初始化EasyAdd功能
		initEasyAdd();
		
		// 初始化ExpandArea功能
		initExpandArea();
		
		// 初始化ShrinkArea功能
		initShrinkArea();

		// 初始化AddHint功能
		initAddHint();

		// 初始化Delete功能
		initDelete();

		// 初始化ReplaceButton功能
		initReplaceButton();

		// 初始化BoundViz功能
		initBoundViz();

		// 注册统一的X键处理器
		areahint.keyhandler.UnifiedKeyHandler.register();

		// 注册断开连接事件监听器
		registerDisconnectListener();

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
			
			// 注册网络接收器
			areahint.easyadd.EasyAddNetworking.registerClientReceivers();
			
			LOGGER.info("EasyAdd功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化EasyAdd功能时发生错误", e);
		}
	}
	
	/**
	 * 初始化ExpandArea功能
	 */
	private void initExpandArea() {
		try {
			// 注册按键处理器
			areahint.expandarea.ExpandAreaKeyHandler.register();
			
			// 注册网络接收器
			areahint.expandarea.ExpandAreaClientNetworking.registerClientNetworking();
			
			LOGGER.info("ExpandArea功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化ExpandArea功能时发生错误", e);
		}
	}
	
	/**
	 * 初始化ShrinkArea功能
	 */
	private void initShrinkArea() {
		try {
			// 注册按键处理器
			areahint.shrinkarea.ShrinkAreaKeyHandler.register();

			// 注册网络接收器
			areahint.shrinkarea.ShrinkAreaClientNetworking.registerClientNetworking();

			LOGGER.info("ShrinkArea功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化ShrinkArea功能时发生错误", e);
		}
	}

	/**
	 * 初始化AddHint功能
	 */
	private void initAddHint() {
		try {
			areahint.addhint.AddHintClientNetworking.registerClientReceivers();
			LOGGER.info("AddHint功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化AddHint功能时发生错误", e);
		}
	}

	/**
	 * 初始化Delete功能
	 */
	private void initDelete() {
		try {
			// 注册网络接收器
			areahint.delete.DeleteNetworking.registerClientReceivers();

			LOGGER.info("Delete功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化Delete功能时发生错误", e);
		}
	}

	/**
	 * 初始化ReplaceButton功能
	 */
	private void initReplaceButton() {
		try {
			// 注册按键监听器
			areahint.replacebutton.ReplaceButtonKeyListener.register();

			// 注册网络接收器
			areahint.replacebutton.ReplaceButtonNetworking.registerClientReceivers();

			LOGGER.info("ReplaceButton功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化ReplaceButton功能时发生错误", e);
		}
	}

	/**
	 * 初始化BoundViz功能
	 */
	private void initBoundViz() {
		try {
			// 初始化BoundViz管理器
			areahint.boundviz.BoundVizManager.getInstance();

			LOGGER.info("BoundViz功能初始化完成");
		} catch (Exception e) {
			LOGGER.error("初始化BoundViz功能时发生错误", e);
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
	 * 获取当前服务器地址，用于检测服务器变化
	 * @param client Minecraft客户端实例
	 * @return 当前服务器地址
	 */
	private static String getCurrentServerAddress(MinecraftClient client) {
		try {
			if (client.isIntegratedServerRunning()) {
				// 单人游戏
				return "localhost";
			}
			
			// 多人游戏
			ServerInfo serverInfo = client.getCurrentServerEntry();
			if (serverInfo != null && serverInfo.address != null) {
				// 移除端口号（如果有的话）
				String address = serverInfo.address;
				int colonIndex = address.lastIndexOf(':');
				if (colonIndex > 0) {
					address = address.substring(0, colonIndex);
				}
				return address;
			}
			
			// 备用
			return "unknown";
			
		} catch (Exception e) {
			LOGGER.warn("获取当前服务器地址失败", e);
			return "unknown";
		}
	}

	/**
	 * 注册断开连接事件监听器
	 * 在玩家退出世界/服务器时清理状态
	 */
	private void registerDisconnectListener() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			LOGGER.info("检测到断开连接,清理世界文件夹管理器状态");

			// 重置ClientWorldFolderManager状态
			areahint.world.ClientWorldFolderManager.resetState();

			// 重置当前状态
			currentDimension = null;
			currentAreaName = null;
			currentServerAddress = null;
			wasPlayerNull = true;
			hasShownDimensionalName = false;

			// 重置区域追踪器
			areahint.log.AreaChangeTracker.reset();

			LOGGER.info("世界文件夹管理器状态已清理");
		});
	}

	/**
	 * 注册客户端tick事件，用于检测玩家位置
	 */
	private void registerTickEvents() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity player = client.player;
			
			// 检测玩家状态变化（从null变为非null = 刚进入世界）
			boolean justEnteredWorld = wasPlayerNull && (player != null && client.world != null);
			wasPlayerNull = (player == null || client.world == null);
			
			if (player == null || client.world == null) {
				return;
			}
			
			// 检查当前维度和服务器地址，如果改变则重新加载区域数据
			Identifier dimension = client.world.getDimensionKey().getValue();
			String serverAddress = getCurrentServerAddress(client);
			
			boolean dimensionChanged = currentDimension == null || !currentDimension.equals(dimension);
			boolean serverChanged = currentServerAddress == null || !currentServerAddress.equals(serverAddress);
			
			// 如果刚进入世界、维度变化或服务器变化，则执行检测和显示逻辑
			if (justEnteredWorld || dimensionChanged || serverChanged) {
				LOGGER.info("触发域名显示 - 刚进入世界: {}, 维度变化: {}, 服务器变化: {}",
					justEnteredWorld, dimensionChanged, serverChanged);

				// 如果刚进入世界，通知日志管理器
				if (justEnteredWorld) {
					areahint.log.AreaChangeTracker.onWorldEnter();
				}

				// 只有在维度或服务器变化时才重新加载数据和初始化
				if (dimensionChanged || serverChanged) {
					// 重置区域追踪器
					areahint.log.AreaChangeTracker.reset();

					// 如果服务器变化了，重置状态并重新初始化客户端世界文件夹
					if (serverChanged) {
						LOGGER.info("检测到服务器变化: '{}' -> '{}', 重新初始化世界文件夹", currentServerAddress, serverAddress);
						currentServerAddress = serverAddress;
						// 重置ClientWorldFolderManager状态
						areahint.world.ClientWorldFolderManager.resetState();
						areahint.world.ClientWorldFolderManager.initializeClientWorld();
					} else if (currentDimension == null) {
						// 如果是第一次进入世界，初始化客户端世界文件夹
						areahint.world.ClientWorldFolderManager.initializeClientWorld();
					}
					
					String dimensionFileName = getDimensionFileName(dimension);
					LOGGER.info("检测到维度变化为：{}，加载对应的区域文件：{}", dimension.toString(), dimensionFileName);
					areaDetector.loadAreaData(dimensionFileName);
				}
				
				// 更新当前状态
				currentDimension = dimension;
				
				// 每次触发时都重置状态并立即检测（包括刚进入世界的情况）
				currentAreaName = null;
				hasShownDimensionalName = false; // 重置维度域名显示标记

				// 立即检测一次当前位置
				double playerX = player.getX();
				double playerY = player.getY();
				double playerZ = player.getZ();

				// 使用AreaChangeTracker检测区域变化并发送日志
				String areaName = areahint.log.AreaChangeTracker.detectAndLogAreaChange(areaDetector, playerX, playerY, playerZ, currentDimension);

				// 立即显示结果（仅在模组启用时）
				if (ClientConfig.isEnabled()) {
					if (areaName != null) {
						currentAreaName = areaName;
						renderManager.showAreaTitle(areaName);
						if (justEnteredWorld) {
							LOGGER.info("进入世界时位于区域：{}", areaName);
						} else {
							LOGGER.info("切换维度时位于区域：{}", areaName);
						}
					} else {
						// 如果不在区域内，显示维度域名
						String dimensionId = currentDimension.toString();
						String dimensionalName = areahint.dimensional.ClientDimensionalNameManager.getDimensionalName(dimensionId);
						renderManager.showAreaTitle(dimensionalName);
						hasShownDimensionalName = true;
						if (justEnteredWorld) {
							LOGGER.info("进入世界时显示维度域名：{}", dimensionalName);
						} else {
							LOGGER.info("切换维度时显示维度域名：{}", dimensionalName);
						}
					}
				} else {
					if (justEnteredWorld) {
						LOGGER.info("进入世界时模组已禁用，不显示域名");
					} else {
						LOGGER.info("切换维度时模组已禁用，不显示域名");
					}
				}
			}
			
			// 按照配置的频率检测玩家位置（仅在模组启用时）
			if (ClientConfig.isEnabled() && areaDetector.shouldDetect()) {
				double playerX = player.getX();
				double playerY = player.getY();
				double playerZ = player.getZ();

				// 使用AreaChangeTracker检测区域变化并发送日志
				String areaName = areahint.log.AreaChangeTracker.detectAndLogAreaChange(areaDetector, playerX, playerY, playerZ, currentDimension);

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
						hasShownDimensionalName = false; // 重置维度域名显示标记
					} else {
						// 离开所有区域时，只显示一次维度域名，避免闪烁
						if (!hasShownDimensionalName) {
							String dimensionId = currentDimension != null ? currentDimension.toString() : "minecraft:overworld";
							String dimensionalName = areahint.dimensional.ClientDimensionalNameManager.getDimensionalName(dimensionId);
							LOGGER.info("离开所有区域，显示维度域名：{}", dimensionalName);
							renderManager.showAreaTitle(dimensionalName);
							hasShownDimensionalName = true;
						}
					}
				}
			} else if (!ClientConfig.isEnabled()) {
				// 模组禁用时，仍然需要记录当前状态但不显示
				currentAreaName = null;
				hasShownDimensionalName = false;
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
	 * 强制重新检测当前区域（在网络同步完成后调用）
	 */
	public static void forceRedetectCurrentArea() {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			ClientPlayerEntity player = client.player;
			
			if (player == null || client.world == null) {
				LOGGER.warn("无法进行强制重新检测：玩家或世界为null");
				return;
			}
			
			// 重新加载当前维度的区域数据
			if (currentDimension != null) {
				String dimensionFileName = getDimensionFileName(currentDimension);
				LOGGER.info("强制重新加载维度{}的区域文件：{}", currentDimension.toString(), dimensionFileName);
				areaDetector.loadAreaData(dimensionFileName);
				
				// 立即检测当前位置
				double playerX = player.getX();
				double playerY = player.getY();
				double playerZ = player.getZ();
				String areaName = areaDetector.detectPlayerArea(playerX, playerY, playerZ);
				
				// 立即显示结果
				if (areaName != null) {
					currentAreaName = areaName;
					renderManager.showAreaTitle(areaName);
					LOGGER.info("网络同步后检测到位于区域：{}", areaName);
				} else {
					// 如果不在区域内，显示维度域名
					String dimensionId = currentDimension.toString();
					String dimensionalName = areahint.dimensional.ClientDimensionalNameManager.getDimensionalName(dimensionId);
					renderManager.showAreaTitle(dimensionalName);
					hasShownDimensionalName = true;
					LOGGER.info("网络同步后显示维度域名：{}", dimensionalName);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("强制重新检测当前区域时发生错误", e);
		}
	}
	
	/**
	 * 重新加载配置和区域数据
	 */
	public static void reload() {
		LOGGER.info("重新加载区域提示模组配置和区域数据...");
		ClientConfig.load();

		// 更新渲染管理器的渲染模式（这会重新读取字幕大小配置）
		if (renderManager != null) {
			renderManager.updateRenderMode();
			LOGGER.info("渲染管理器已更新");
		}

		// 重新加载维度域名配置
		areahint.dimensional.ClientDimensionalNameManager.resetToDefaults();
		LOGGER.info("维度域名配置已重置");

		// 重新加载边界可视化数据
		areahint.boundviz.BoundVizManager.getInstance().reload();
		LOGGER.info("边界可视化数据已重新加载");

		if (currentDimension != null) {
			String dimensionFileName = getDimensionFileName(currentDimension);
			LOGGER.info("重新加载维度{}的区域文件：{}", currentDimension.toString(), dimensionFileName);

			// 获取文件路径并检查是否存在
			Path areaFile = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(dimensionFileName);
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