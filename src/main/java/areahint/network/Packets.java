package areahint.network;

import areahint.Areashint;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 网络数据包定义类
 * 定义客户端和服务端之间通信的数据包
 */
public class Packets {
    // 维度类型常量
    public static final String DIMENSION_OVERWORLD = "overworld";
    public static final String DIMENSION_NETHER = "the_nether";
    public static final String DIMENSION_END = "the_end";

    // --- S2C 包ID ---
    public static final CustomPayload.Id<BufPayload> S2C_AREA_DATA = id("s2c_area_data");
    public static final CustomPayload.Id<BufPayload> S2C_CLIENT_COMMAND = id("s2c_client_command");
    public static final CustomPayload.Id<BufPayload> S2C_DEBUG_COMMAND = id("s2c_debug_command");
    public static final CustomPayload.Id<BufPayload> S2C_RECOLOR_RESPONSE = id("s2c_recolor_response");
    public static final CustomPayload.Id<BufPayload> S2C_RENAME_RESPONSE = id("s2c_rename_response");
    public static final CustomPayload.Id<BufPayload> S2C_EASYADD_RESPONSE = id("s2c_easyadd_response");
    public static final CustomPayload.Id<BufPayload> S2C_SETHIGH_AREA_LIST = id("s2c_sethigh_area_list");
    public static final CustomPayload.Id<BufPayload> S2C_SETHIGH_AREA_SELECTION = id("s2c_sethigh_area_selection");
    public static final CustomPayload.Id<BufPayload> S2C_SETHIGH_RESPONSE = id("s2c_sethigh_response");
    public static final CustomPayload.Id<BufPayload> EXPAND_AREA_RESPONSE_CHANNEL = id("expand_area_response");
    public static final CustomPayload.Id<BufPayload> SHRINK_AREA_RESPONSE_CHANNEL = id("shrink_area_response");
    public static final CustomPayload.Id<BufPayload> S2C_DELETABLE_AREAS_LIST = id("s2c_deletable_areas_list");
    public static final CustomPayload.Id<BufPayload> S2C_DELETE_RESPONSE = id("s2c_delete_response");
    public static final CustomPayload.Id<BufPayload> ADDHINT_AREA_RESPONSE_CHANNEL = id("addhint_area_response");
    public static final CustomPayload.Id<BufPayload> DELETEHINT_AREA_RESPONSE_CHANNEL = id("deletehint_area_response");
    public static final CustomPayload.Id<BufPayload> DIVIDE_AREA_RESPONSE_CHANNEL = id("divide_area_response");
    public static final CustomPayload.Id<BufPayload> S2C_WORLD_INFO = id("world_info");
    public static final CustomPayload.Id<BufPayload> S2C_DIMENSIONAL_NAMES = id("dimensional_names");

    // --- C2S 包ID ---
    public static final CustomPayload.Id<BufPayload> C2S_RECOLOR_REQUEST = id("c2s_recolor_request");
    public static final CustomPayload.Id<BufPayload> C2S_RENAME_REQUEST = id("c2s_rename_request");
    public static final CustomPayload.Id<BufPayload> C2S_EASYADD_AREA_DATA = id("c2s_easyadd_area_data");
    public static final CustomPayload.Id<BufPayload> C2S_SETHIGH_REQUEST = id("c2s_sethigh_request");
    public static final CustomPayload.Id<BufPayload> C2S_REQUEST_DELETABLE_AREAS = id("c2s_request_deletable_areas");
    public static final CustomPayload.Id<BufPayload> C2S_DELETE_AREA = id("c2s_delete_area");
    public static final CustomPayload.Id<BufPayload> C2S_DIMNAME_REQUEST = id("c2s_dimname_request");
    public static final CustomPayload.Id<BufPayload> C2S_DIMCOLOR_REQUEST = id("c2s_dimcolor_request");
    public static final CustomPayload.Id<BufPayload> C2S_FIRST_DIMNAME = id("c2s_first_dimname");
    public static final CustomPayload.Id<BufPayload> C2S_LANGUAGE_SYNC = id("c2s_language_sync");
    public static final CustomPayload.Id<BufPayload> C2S_AREA_LOG = id("c2s_area_log");
    public static final CustomPayload.Id<BufPayload> EXPAND_AREA_CHANNEL = id("expand_area");
    public static final CustomPayload.Id<BufPayload> SHRINK_AREA_CHANNEL = id("shrink_area");
    public static final CustomPayload.Id<BufPayload> ADDHINT_AREA_CHANNEL = id("addhint_area");
    public static final CustomPayload.Id<BufPayload> DELETEHINT_AREA_CHANNEL = id("deletehint_area");
    public static final CustomPayload.Id<BufPayload> DIVIDE_AREA_CHANNEL = id("divide_area");
    public static final CustomPayload.Id<BufPayload> REPLACEBUTTON_START = id("replacebutton_start");
    public static final CustomPayload.Id<BufPayload> REPLACEBUTTON_CANCEL = id("replacebutton_cancel");
    public static final CustomPayload.Id<BufPayload> REPLACEBUTTON_CONFIRM = id("replacebutton_confirm");
    public static final CustomPayload.Id<BufPayload> C2S_REQUEST_WORLD_INFO = id("request_world_info");

    private static CustomPayload.Id<BufPayload> id(String path) {
        return new CustomPayload.Id<>(Identifier.of(Areashint.MOD_ID, path));
    }

    /** 注册所有 payload 类型（必须在网络处理器注册之前调用） */
    public static void registerAll() {
        // S2C
        s2c(S2C_AREA_DATA); s2c(S2C_CLIENT_COMMAND); s2c(S2C_DEBUG_COMMAND);
        s2c(S2C_RECOLOR_RESPONSE); s2c(S2C_RENAME_RESPONSE); s2c(S2C_EASYADD_RESPONSE);
        s2c(S2C_SETHIGH_AREA_LIST); s2c(S2C_SETHIGH_AREA_SELECTION); s2c(S2C_SETHIGH_RESPONSE);
        s2c(EXPAND_AREA_RESPONSE_CHANNEL); s2c(SHRINK_AREA_RESPONSE_CHANNEL);
        s2c(S2C_DELETABLE_AREAS_LIST); s2c(S2C_DELETE_RESPONSE);
        s2c(ADDHINT_AREA_RESPONSE_CHANNEL); s2c(DELETEHINT_AREA_RESPONSE_CHANNEL);
        s2c(DIVIDE_AREA_RESPONSE_CHANNEL);
        s2c(S2C_WORLD_INFO); s2c(S2C_DIMENSIONAL_NAMES);
        // C2S
        c2s(C2S_RECOLOR_REQUEST); c2s(C2S_RENAME_REQUEST); c2s(C2S_EASYADD_AREA_DATA);
        c2s(C2S_SETHIGH_REQUEST); c2s(C2S_REQUEST_DELETABLE_AREAS); c2s(C2S_DELETE_AREA);
        c2s(C2S_DIMNAME_REQUEST); c2s(C2S_DIMCOLOR_REQUEST); c2s(C2S_FIRST_DIMNAME);
        c2s(C2S_LANGUAGE_SYNC); c2s(C2S_AREA_LOG);
        c2s(EXPAND_AREA_CHANNEL); c2s(SHRINK_AREA_CHANNEL);
        c2s(ADDHINT_AREA_CHANNEL); c2s(DELETEHINT_AREA_CHANNEL); c2s(DIVIDE_AREA_CHANNEL);
        c2s(REPLACEBUTTON_START); c2s(REPLACEBUTTON_CANCEL); c2s(REPLACEBUTTON_CONFIRM);
        s2c(REPLACEBUTTON_START); s2c(REPLACEBUTTON_CANCEL); s2c(REPLACEBUTTON_CONFIRM);
        c2s(C2S_REQUEST_WORLD_INFO);
    }

    private static void s2c(CustomPayload.Id<BufPayload> id) {
        PayloadTypeRegistry.playS2C().register(id, BufPayload.codec(id));
    }
    private static void c2s(CustomPayload.Id<BufPayload> id) {
        PayloadTypeRegistry.playC2S().register(id, BufPayload.codec(id));
    }

    public static String convertDimensionPathToType(String dimensionPath) {
        if (dimensionPath == null) return null;
        return switch (dimensionPath) {
            case "overworld" -> DIMENSION_OVERWORLD;
            case "the_nether" -> DIMENSION_NETHER;
            case "the_end" -> DIMENSION_END;
            default -> null;
        };
    }

    public static String getFileNameForDimension(String dimensionType) {
        if (dimensionType == null) return null;
        return switch (dimensionType) {
            case DIMENSION_OVERWORLD -> Areashint.OVERWORLD_FILE;
            case DIMENSION_NETHER -> Areashint.NETHER_FILE;
            case DIMENSION_END -> Areashint.END_FILE;
            default -> null;
        };
    }
}
