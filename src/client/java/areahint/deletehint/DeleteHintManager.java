package areahint.deletehint;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.util.AreaDataConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DeleteHint功能管理器
 * 交互式从已有域名删除顶点
 */
public class DeleteHintManager {
    private static DeleteHintManager instance;
    private final MinecraftClient client;

    private boolean isActive = false;
    private AreaData selectedArea;
    private Set<Integer> markedIndices = new HashSet<>();

    public static DeleteHintManager getInstance() {
        if (instance == null) {
            instance = new DeleteHintManager();
        }
        return instance;
    }

    private DeleteHintManager() {
        this.client = MinecraftClient.getInstance();
    }

    /**
     * 启动DeleteHint流程
     */
    public void start() {
        if (client.player == null) return;

        isActive = true;
        List<AreaData> modifiableAreas = getModifiableAreas();

        if (modifiableAreas.isEmpty()) {
            client.player.sendMessage(Text.of("§c没有可修改的域名"), false);
            isActive = false;
            return;
        }

        showAreaSelection(modifiableAreas);
    }

    /**
     * 选择域名
     */
    public void selectArea(String areaName) {
        if (!isActive || client.player == null) return;

        String cleanedName = areaName.trim();
        if (cleanedName.startsWith("\"") && cleanedName.endsWith("\"") && cleanedName.length() > 1) {
            cleanedName = cleanedName.substring(1, cleanedName.length() - 1);
        }

        List<AreaData> modifiableAreas = getModifiableAreas();
        AreaData area = null;
        for (AreaData a : modifiableAreas) {
            if (a.getName().equals(cleanedName)) {
                area = a;
                break;
            }
        }

        if (area == null) {
            client.player.sendMessage(Text.of("§c域名 '" + cleanedName + "' 不存在或您没有权限"), false);
            return;
        }

        selectedArea = area;
        markedIndices.clear();

        client.player.sendMessage(Text.of("§a已选择域名: §6" + AreaDataConverter.getDisplayName(area)), false);
        showVertexButtons();
    }

    /**
     * 标记/取消标记顶点
     */
    public void toggleVertex(int index) {
        if (!isActive || selectedArea == null || client.player == null) return;

        List<AreaData.Vertex> vertices = selectedArea.getVertices();
        if (index < 0 || index >= vertices.size()) {
            client.player.sendMessage(Text.of("§c无效的顶点索引"), false);
            return;
        }

        if (markedIndices.contains(index)) {
            markedIndices.remove(index);
        } else {
            // 确保删除后至少剩余3个顶点
            int remaining = vertices.size() - markedIndices.size() - 1;
            if (remaining < 3) {
                client.player.sendMessage(Text.of("§c不能再删除更多顶点，至少需要保留3个顶点"), false);
                return;
            }
            markedIndices.add(index);
        }

        showVertexButtons();
    }

    /**
     * 提交删除
     */
    public void submit() {
        if (!isActive || selectedArea == null || client.player == null) return;

        if (markedIndices.isEmpty()) {
            client.player.sendMessage(Text.of("§c请至少选择一个要删除的顶点"), false);
            return;
        }

        try {
            // 收集未被删除的顶点
            List<Double[]> remaining = new ArrayList<>();
            List<AreaData.Vertex> vertices = selectedArea.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                if (!markedIndices.contains(i)) {
                    remaining.add(new Double[]{vertices.get(i).getX(), vertices.get(i).getZ()});
                }
            }

            if (remaining.size() < 3) {
                client.player.sendMessage(Text.of("§c删除后顶点不足3个，无法提交"), false);
                return;
            }

            // 按角度排序防止交叉
            List<Double[]> sorted = sortVerticesByAngle(remaining);

            // 转换为Vertex列表
            List<AreaData.Vertex> newVertices = new ArrayList<>();
            for (Double[] v : sorted) {
                newVertices.add(new AreaData.Vertex(v[0], v[1]));
            }

            // 重新计算二级顶点(AABB)
            List<AreaData.Vertex> secondVertices = calculateBoundingBox(sorted);

            // 更新域名数据
            selectedArea.setVertices(newVertices);
            selectedArea.setSecondVertices(secondVertices);

            // 获取当前维度
            String dimension = client.world.getRegistryKey().getValue().toString();

            // 发送到服务端
            DeleteHintClientNetworking.sendToServer(selectedArea, dimension);

            client.player.sendMessage(Text.of("§a已提交域名 §6" + selectedArea.getName() + " §a的顶点删除"), false);

        } catch (Exception e) {
            client.player.sendMessage(Text.of("§c处理顶点时发生错误: " + e.getMessage()), false);
        } finally {
            reset();
        }
    }

    /**
     * 取消流程
     */
    public void cancel() {
        if (!isActive) return;
        if (client.player != null) {
            client.player.sendMessage(Text.of("§c已取消删除顶点"), false);
        }
        reset();
    }

    private void reset() {
        isActive = false;
        selectedArea = null;
        markedIndices.clear();
    }

    // ===== UI方法 =====

    private void showAreaSelection(List<AreaData> areas) {
        client.player.sendMessage(Text.of("§6=== 删除顶点 - 选择域名 ==="), false);
        client.player.sendMessage(Text.of("§a请选择要删除顶点的域名："), false);

        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            MutableText btn = Text.literal("§6[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint deletehint select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("选择 " + displayName + "\n创建者: " + area.getSignature()
                            + "\n顶点数: " + area.getVertices().size())))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(btn, false);
        }

        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletehint cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消")))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    private void showVertexButtons() {
        List<AreaData.Vertex> vertices = selectedArea.getVertices();
        client.player.sendMessage(Text.of("§6=== 选择要删除的顶点 ==="), false);
        client.player.sendMessage(Text.of("§7点击顶点按钮标记/取消标记，标记的顶点将被删除"), false);
        client.player.sendMessage(Text.of("§7已标记 §c" + markedIndices.size() + " §7个顶点，剩余 §a"
            + (vertices.size() - markedIndices.size()) + " §7个"), false);

        for (int i = 0; i < vertices.size(); i++) {
            AreaData.Vertex v = vertices.get(i);
            boolean marked = markedIndices.contains(i);
            String prefix = marked ? "§c✗ " : "§a";
            String label = prefix + "[顶点" + (i + 1) + ": (" + (int) v.getX() + ", " + (int) v.getZ() + ")]";

            MutableText btn = Text.literal(label)
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint deletehint toggle " + i))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of(marked ? "点击取消标记" : "点击标记删除")))
                    .withColor(marked ? Formatting.RED : Formatting.GREEN));
            client.player.sendMessage(btn, false);
        }

        // 操作按钮
        MutableText submitBtn = Text.literal("§b[提交删除]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletehint submit"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("提交顶点删除")))
                .withColor(Formatting.AQUA));

        MutableText cancelBtn = Text.literal("§c[取消]")
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletehint cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("取消")))
                .withColor(Formatting.RED));

        MutableText row = Text.empty()
            .append(submitBtn).append(Text.of("  "))
            .append(cancelBtn);
        client.player.sendMessage(row, false);
    }

    // ===== 工具方法 =====

    private List<Double[]> sortVerticesByAngle(List<Double[]> vertices) {
        double cx = 0, cz = 0;
        for (Double[] v : vertices) {
            cx += v[0];
            cz += v[1];
        }
        cx /= vertices.size();
        cz /= vertices.size();

        final double fcx = cx, fcz = cz;
        List<Double[]> sorted = new ArrayList<>(vertices);
        sorted.sort((a, b) -> {
            double angleA = Math.atan2(a[1] - fcz, a[0] - fcx);
            double angleB = Math.atan2(b[1] - fcz, b[0] - fcx);
            return Double.compare(angleA, angleB);
        });
        return sorted;
    }

    private List<AreaData.Vertex> calculateBoundingBox(List<Double[]> vertices) {
        double minX = vertices.get(0)[0], maxX = vertices.get(0)[0];
        double minZ = vertices.get(0)[1], maxZ = vertices.get(0)[1];
        for (Double[] v : vertices) {
            minX = Math.min(minX, v[0]);
            maxX = Math.max(maxX, v[0]);
            minZ = Math.min(minZ, v[1]);
            maxZ = Math.max(maxZ, v[1]);
        }
        List<AreaData.Vertex> box = new ArrayList<>();
        box.add(new AreaData.Vertex(minX, minZ));
        box.add(new AreaData.Vertex(maxX, minZ));
        box.add(new AreaData.Vertex(maxX, maxZ));
        box.add(new AreaData.Vertex(minX, maxZ));
        return box;
    }

    private List<AreaData> getModifiableAreas() {
        List<AreaData> result = new ArrayList<>();
        if (client.player == null || client.world == null) return result;

        String playerName = client.player.getGameProfile().getName();
        boolean isAdmin = client.player.hasPermissionLevel(2);

        List<AreaData> allAreas = loadAllAreas();
        for (AreaData area : allAreas) {
            if (isAdmin) {
                result.add(area);
            } else if (playerName.equals(area.getSignature())) {
                result.add(area);
            } else if (area.getBaseName() != null) {
                AreaData baseArea = findAreaByName(area.getBaseName(), allAreas);
                if (baseArea != null && playerName.equals(baseArea.getSignature())) {
                    result.add(area);
                }
            }
        }
        return result;
    }

    private AreaData findAreaByName(String name, List<AreaData> areas) {
        for (AreaData a : areas) {
            if (a.getName().equals(name)) return a;
        }
        return null;
    }

    private List<AreaData> loadAllAreas() {
        try {
            String dim = client.world.getRegistryKey().getValue().toString();
            String fileName = getFileNameForDimension(dim);
            if (fileName != null) {
                Path path = areahint.world.ClientWorldFolderManager.getWorldDimensionFile(fileName);
                if (path.toFile().exists()) {
                    return FileManager.readAreaData(path);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return new ArrayList<>();
    }

    private String getFileNameForDimension(String dim) {
        if (dim.contains("overworld")) return areahint.Areashint.OVERWORLD_FILE;
        if (dim.contains("nether")) return areahint.Areashint.NETHER_FILE;
        if (dim.contains("end")) return areahint.Areashint.END_FILE;
        return null;
    }

    // Getters
    public boolean isActive() { return isActive; }
}
