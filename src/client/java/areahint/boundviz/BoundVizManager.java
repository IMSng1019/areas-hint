package areahint.boundviz;

import areahint.AreashintClient;
import areahint.config.ClientConfig;
import areahint.data.AreaData;
import areahint.file.FileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 边界可视化管理器
 * 负责管理域名边界的可视化显示
 */
public class BoundVizManager {
    private static BoundVizManager instance;
    private boolean enabled = false;
    private List<AreaData> currentDimensionAreas = new ArrayList<>();
    private String currentDimension = null;

    // 临时顶点记录（用于easyadd、expandarea、shrinkarea）
    private List<BlockPos> temporaryVertices = new ArrayList<>();
    private boolean showTemporaryVertices = false;

    private BoundVizManager() {
        // 从配置加载初始状态
        this.enabled = ClientConfig.isBoundVizEnabled();
    }

    public static BoundVizManager getInstance() {
        if (instance == null) {
            instance = new BoundVizManager();
        }
        return instance;
    }

    /**
     * 切换边界可视化状态
     */
    public void toggle() {
        enabled = !enabled;
        ClientConfig.setBoundVizEnabled(enabled);

        if (enabled) {
            loadCurrentDimensionAreas();
            AreashintClient.LOGGER.info("边界可视化已开启");
        } else {
            currentDimensionAreas.clear();
            AreashintClient.LOGGER.info("边界可视化已关闭");
        }
    }

    /**
     * 设置边界可视化状态
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            toggle();
        }
    }

    /**
     * 获取边界可视化状态
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 加载当前维度的所有域名数据
     */
    public void loadCurrentDimensionAreas() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) {
                return;
            }

            String dimension = client.world.getRegistryKey().getValue().toString();
            if (dimension.equals(currentDimension) && !currentDimensionAreas.isEmpty()) {
                return; // 已加载
            }

            currentDimension = dimension;
            String fileName = getFileNameForDimension(dimension);
            if (fileName == null) {
                AreashintClient.LOGGER.warn("无法确定维度文件名: {}", dimension);
                return;
            }

            Path areaFile = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
            currentDimensionAreas = FileManager.readAreaData(areaFile);

            AreashintClient.LOGGER.info("已加载 {} 个域名边界用于可视化", currentDimensionAreas.size());

        } catch (Exception e) {
            AreashintClient.LOGGER.error("加载域名边界数据失败", e);
            currentDimensionAreas.clear();
        }
    }

    /**
     * 获取当前维度的所有域名数据
     */
    public List<AreaData> getCurrentDimensionAreas() {
        if (enabled) {
            loadCurrentDimensionAreas();
        }
        return new ArrayList<>(currentDimensionAreas);
    }

    /**
     * 设置临时顶点（用于easyadd、expandarea、shrinkarea）
     */
    public void setTemporaryVertices(List<BlockPos> vertices, boolean show) {
        this.temporaryVertices = new ArrayList<>(vertices);
        this.showTemporaryVertices = show;
    }

    /**
     * 清除临时顶点
     */
    public void clearTemporaryVertices() {
        this.temporaryVertices.clear();
        this.showTemporaryVertices = false;
    }

    /**
     * 获取临时顶点
     */
    public List<BlockPos> getTemporaryVertices() {
        return new ArrayList<>(temporaryVertices);
    }

    /**
     * 是否显示临时顶点
     */
    public boolean shouldShowTemporaryVertices() {
        return showTemporaryVertices && !temporaryVertices.isEmpty();
    }

    /**
     * 重新加载当前维度的域名数据
     */
    public void reload() {
        currentDimensionAreas.clear();
        currentDimension = null;
        if (enabled) {
            loadCurrentDimensionAreas();
        }
    }

    /**
     * 根据维度ID获取文件名
     */
    private String getFileNameForDimension(String dimension) {
        if (dimension.contains("overworld")) {
            return areahint.Areashint.OVERWORLD_FILE;
        } else if (dimension.contains("nether")) {
            return areahint.Areashint.NETHER_FILE;
        } else if (dimension.contains("end")) {
            return areahint.Areashint.END_FILE;
        }
        return null;
    }
}
