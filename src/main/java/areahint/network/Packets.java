package areahint.network;

import areahint.Areashint;
import net.minecraft.util.Identifier;

/**
 * 网络数据包定义类
 * 定义客户端和服务端之间通信的数据包
 */
public class Packets {
    // 区域数据传输通道标识符
    public static final Identifier AREA_DATA_CHANNEL = new Identifier(Areashint.MOD_ID, "area_data");
    
    // 客户端命令通道标识符
    public static final Identifier CLIENT_COMMAND_CHANNEL = new Identifier(Areashint.MOD_ID, "client_command");
    
    // 维度类型常量
    public static final String DIMENSION_OVERWORLD = "overworld";
    public static final String DIMENSION_NETHER = "the_nether";
    public static final String DIMENSION_END = "the_end";
    
    // 服务端到客户端的包ID
    public static final String S2C_AREA_DATA = Areashint.MOD_ID + ":s2c_area_data";
    public static final String S2C_CLIENT_COMMAND = Areashint.MOD_ID + ":s2c_client_command";
    public static final String S2C_DEBUG_COMMAND = Areashint.MOD_ID + ":s2c_debug_command";
    
    // 客户端到服务端的包ID
    public static final String C2S_REQUEST_AREA_DATA = Areashint.MOD_ID + ":c2s_request_area_data";
    
    /**
     * 将维度路径转换为维度类型字符串
     * @param dimensionPath 维度路径
     * @return 维度类型字符串，如果未知则返回null
     */
    public static String convertDimensionPathToType(String dimensionPath) {
        if (dimensionPath == null) {
            return null;
        }
        
        switch (dimensionPath) {
            case "overworld":
                return DIMENSION_OVERWORLD;
            case "the_nether":
                return DIMENSION_NETHER;
            case "the_end":
                return DIMENSION_END;
            default:
                return null;
        }
    }
    
    /**
     * 获取维度对应的文件名
     * @param dimensionType 维度类型
     * @return 文件名，如果未知则返回null
     */
    public static String getFileNameForDimension(String dimensionType) {
        if (dimensionType == null) {
            return null;
        }
        
        switch (dimensionType) {
            case DIMENSION_OVERWORLD:
                return Areashint.OVERWORLD_FILE;
            case DIMENSION_NETHER:
                return Areashint.NETHER_FILE;
            case DIMENSION_END:
                return Areashint.END_FILE;
            default:
                return null;
        }
    }
} 