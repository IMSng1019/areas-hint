package areahint.log;

import areahint.AreashintClient;
import areahint.data.AreaData;
import areahint.detection.AreaDetector;
import net.minecraft.util.Identifier;

/**
 * 区域变化追踪器
 * 用于追踪玩家进入和离开区域，并发送日志消息
 */
public class AreaChangeTracker {
    // 当前所在的区域数据
    private static AreaData currentAreaData = null;

    /**
     * 检测区域变化并发送日志消息
     * @param areaDetector 区域检测器
     * @param x 玩家X坐标
     * @param y 玩家Y坐标
     * @param z 玩家Z坐标
     * @param currentDimension 当前维度标识符
     * @return 格式化后的区域名称（用于显示）
     */
    public static String detectAndLogAreaChange(AreaDetector areaDetector, double x, double y, double z, Identifier currentDimension) {
        // 获取当前区域数据（未格式化的原始数据）
        AreaData newAreaData = areaDetector.findAreaRaw(x, y, z);

        // 获取维度域名
        String dimensionalName = null;
        if (currentDimension != null) {
            dimensionalName = areahint.dimensional.ClientDimensionalNameManager.getDimensionalName(currentDimension.toString());
        }

        // 检测区域变化
        boolean areaChanged = false;
        AreaData oldAreaData = currentAreaData;

        if (currentAreaData == null && newAreaData != null) {
            // 进入新区域
            areaChanged = true;
            currentAreaData = newAreaData;

            // 发送进入消息
            ClientLogNetworking.sendEnterAreaMessage(
                newAreaData.getName(),
                newAreaData.getLevel(),
                newAreaData.getSurfacename(),
                dimensionalName
            );

        } else if (currentAreaData != null && newAreaData == null) {
            // 离开区域
            areaChanged = true;

            // 发送离开消息
            ClientLogNetworking.sendLeaveAreaMessage(
                currentAreaData.getName(),
                currentAreaData.getLevel(),
                currentAreaData.getSurfacename(),
                dimensionalName
            );

            currentAreaData = null;

        } else if (currentAreaData != null && newAreaData != null &&
                   !currentAreaData.getName().equals(newAreaData.getName())) {
            // 从一个区域进入另一个区域
            areaChanged = true;

            // 先发送离开旧区域的消息
            ClientLogNetworking.sendLeaveAreaMessage(
                currentAreaData.getName(),
                currentAreaData.getLevel(),
                currentAreaData.getSurfacename(),
                dimensionalName
            );

            // 再发送进入新区域的消息
            ClientLogNetworking.sendEnterAreaMessage(
                newAreaData.getName(),
                newAreaData.getLevel(),
                newAreaData.getSurfacename(),
                dimensionalName
            );

            currentAreaData = newAreaData;
        }

        // 返回格式化后的区域名称（用于显示）
        if (newAreaData != null) {
            return areaDetector.detectPlayerArea(x, y, z);
        }

        return null;
    }

    /**
     * 重置当前区域（在切换维度或世界时调用）
     */
    public static void reset() {
        currentAreaData = null;
    }

    /**
     * 玩家进入世界时调用
     */
    public static void onWorldEnter() {
        ClientLogManager.onWorldEnter();
        reset();
    }

    /**
     * 获取当前区域数据
     * @return 当前区域数据
     */
    public static AreaData getCurrentAreaData() {
        return currentAreaData;
    }
}
