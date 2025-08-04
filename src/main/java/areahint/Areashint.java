package areahint;

import areahint.command.ServerCommands;
import areahint.network.ServerNetworking;
import areahint.file.FileManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

/**
 * 区域提示模组 - 服务端主类
 * 负责服务端的初始化、命令注册和文件管理
 */
public class Areashint implements ModInitializer {
	public static final String MOD_ID = "areas-hint";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	// 各维度区域数据文件名
	public static final String OVERWORLD_FILE = "overworld.json";
	public static final String NETHER_FILE = "the_nether.json";
	public static final String END_FILE = "the_end.json";
	
	// 服务器实例引用
	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("区域提示模组服务端初始化中...");
		
		// 初始化文件管理
		initConfigDir();
		
		// 初始化维度域名管理器
		areahint.dimensional.DimensionalNameManager.init();
		
		// 初始化维度域名网络处理
		areahint.network.DimensionalNameNetworking.init();
		
		// 注册服务器启动事件监听器
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		// 注册服务器停止事件监听器
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
		
		// 初始化服务端网络处理
		ServerNetworking.init();
		
		// 初始化EasyAdd服务端网络处理
		areahint.easyadd.EasyAddServerNetworking.registerServerReceivers();
		
		// 注册命令
		ServerCommands.register();
		
		LOGGER.info("区域提示模组服务端初始化完成!");
	}
	
	/**
	 * 检查并创建外部配置目录
	 */
	private void initConfigDir() {
		try {
			// 使用FileManager获取配置目录
			Path configDirPath = FileManager.checkFolderExist();
			
			// 在目录中创建默认的空区域文件
			FileManager.createEmptyAreaFile(FileManager.getDimensionFile(OVERWORLD_FILE));
			FileManager.createEmptyAreaFile(FileManager.getDimensionFile(NETHER_FILE));
			FileManager.createEmptyAreaFile(FileManager.getDimensionFile(END_FILE));
		} catch (IOException e) {
			LOGGER.error("创建区域文件失败: " + e.getMessage());
		}
	}
	
	/**
	 * 服务器启动事件处理
	 * @param minecraftServer 服务器实例
	 */
	private void onServerStarting(MinecraftServer minecraftServer) {
		server = minecraftServer;
		LOGGER.info("区域提示模组: 服务器启动中");
	}
	
	/**
	 * 服务器停止事件处理
	 * @param minecraftServer 服务器实例
	 */
	private void onServerStopped(MinecraftServer minecraftServer) {
		server = null;
		LOGGER.info("区域提示模组: 服务器已停止");
	}
	
	/**
	 * 获取服务器实例
	 * @return 当前服务器实例
	 */
	public static MinecraftServer getServer() {
		return server;
	}
	
	/**
	 * 获取外部配置目录路径
	 * @return 配置目录路径
	 */
	public static Path getConfigDir() {
		return FileManager.getConfigFolder();
	}
}