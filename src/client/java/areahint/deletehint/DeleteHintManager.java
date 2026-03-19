package areahint.deletehint;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.I18nManager;
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
 * DeleteHint閸旂喕鍏樼粻锛勬倞閸?
 * 娴溿倓绨板蹇庣矤瀹稿弶婀侀崺鐔锋倳閸掔娀娅庢い鍓佸仯
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
     * 閸氼垰濮〥eleteHint濞翠胶鈻?
     */
    public void start() {
        if (client.player == null) return;

        isActive = true;
        List<AreaData> modifiableAreas = getModifiableAreas();

        if (modifiableAreas.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.area.modify")), false);
            isActive = false;
            return;
        }

        showAreaSelection(modifiableAreas);
    }

    /**
     * 闁瀚ㄩ崺鐔锋倳
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
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.area") + cleanedName + I18nManager.translate("addhint.message.permission")), false);
            return;
        }

        selectedArea = area;
        markedIndices.clear();

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.area") + AreaDataConverter.getDisplayName(area)), false);
        showVertexButtons();
    }

    /**
     * 閺嶅洩顔?閸欐牗绉烽弽鍥唶妞ゅ墎鍋?
     */
    public void toggleVertex(int index) {
        if (!isActive || selectedArea == null || client.player == null) return;

        List<AreaData.Vertex> vertices = selectedArea.getVertices();
        if (index < 0 || index >= vertices.size()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.error.vertex")), false);
            return;
        }

        if (markedIndices.contains(index)) {
            markedIndices.remove(index);
        } else {
            // 绾喕绻氶崚鐘绘珟閸氬氦鍤︾亸鎴濆⒖娴?娑擃亪銆婇悙?
            int remaining = vertices.size() - markedIndices.size() - 1;
            if (remaining < 3) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.error.vertex.delete")), false);
                return;
            }
            markedIndices.add(index);
        }

        showVertexButtons();
    }

    /**
     * 閹绘劒姘﹂崚鐘绘珟
     */
    public void submit() {
        if (!isActive || selectedArea == null || client.player == null) return;

        if (markedIndices.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.error.vertex.delete_3")), false);
            return;
        }

        try {
            // 閺€鍫曟肠閺堫亣顫﹂崚鐘绘珟閻ㄥ嫰銆婇悙?
            List<Double[]> remaining = new ArrayList<>();
            List<AreaData.Vertex> vertices = selectedArea.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                if (!markedIndices.contains(i)) {
                    remaining.add(new Double[]{vertices.get(i).getX(), vertices.get(i).getZ()});
                }
            }

            if (remaining.size() < 3) {
                client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.error.vertex.delete_2")), false);
                return;
            }

            // 閹稿顫楁惔锔藉笓鎼村繘妲诲顫唉閸?
            List<Double[]> sorted = sortVerticesByAngle(remaining);

            // 鏉烆剚宕叉稉绡rtex閸掓銆?
            List<AreaData.Vertex> newVertices = new ArrayList<>();
            for (Double[] v : sorted) {
                newVertices.add(new AreaData.Vertex(v[0], v[1]));
            }

            // 闁插秵鏌婄拋锛勭暬娴滃瞼楠囨い鍓佸仯(AABB)
            List<AreaData.Vertex> secondVertices = calculateBoundingBox(sorted);

            // 閺囧瓨鏌婇崺鐔锋倳閺佺増宓?
            selectedArea.setVertices(newVertices);
            selectedArea.setSecondVertices(secondVertices);

            // 閼惧嘲褰囪ぐ鎾冲缂佹潙瀹?
            String dimension = client.world.getRegistryKey().getValue().toString();

            // 閸欐垿鈧礁鍩岄張宥呭缁?
            DeleteHintClientNetworking.sendToServer(selectedArea, dimension);

            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.area") + selectedArea.getName() + I18nManager.translate("deletehint.message.vertex.delete")), false);

        } catch (Exception e) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.vertex") + e.getMessage()), false);
        } finally {
            reset();
        }
    }

    /**
     * 閸欐牗绉峰ù浣衡柤
     */
    public void cancel() {
        if (!isActive) return;
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.error.vertex.cancel.delete")), false);
        }
        reset();
    }

    private void reset() {
        isActive = false;
        selectedArea = null;
        markedIndices.clear();
    }

    // ===== UI閺傝纭?=====

    private void showAreaSelection(List<AreaData> areas) {
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.title.area.vertex.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.prompt.area.vertex.delete")), false);

        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            MutableText btn = areahint.util.TextCompat.literal("鎼?[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint deletehint select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName + I18nManager.translate("addhint.message.general") + area.getSignature()
                            + I18nManager.translate("deletehint.message.vertex_3") + area.getVertices().size())))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(btn, false);
        }

        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletehint cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.message.cancel"))))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    private void showVertexButtons() {
        List<AreaData.Vertex> vertices = selectedArea.getVertices();
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.title.vertex.delete")), false);
        client.player.sendMessage(areahint.util.TextCompat.of("鎼?" + I18nManager.translate("deletehint.message.delete") + "/" + I18nManager.translate("deletehint.message.cancel")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("deletehint.message.general_2") + markedIndices.size() + I18nManager.translate("deletehint.message.vertex")
            + (vertices.size() - markedIndices.size()) + I18nManager.translate("deletehint.message.general")), false);

        for (int i = 0; i < vertices.size(); i++) {
            AreaData.Vertex v = vertices.get(i);
            boolean marked = markedIndices.contains(i);
            String prefix = marked ? "鎼俢閴?" : "鎼俛";
            String label = prefix + I18nManager.translate("deletehint.message.vertex_2") + (i + 1) + ": (" + (int) v.getX() + ", " + (int) v.getZ() + ")]";

            MutableText btn = areahint.util.TextCompat.literal(label)
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint deletehint toggle " + i))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(marked ? I18nManager.translate("deletehint.message.cancel") : I18nManager.translate("deletehint.message.delete"))))
                    .withColor(marked ? Formatting.RED : Formatting.GREEN));
            client.player.sendMessage(btn, false);
        }

        // 閹垮秳缍旈幐澶愭尦
        MutableText submitBtn = areahint.util.TextCompat.literal(I18nManager.translate("deletehint.button.delete"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletehint submit"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("deletehint.message.vertex.delete_2"))))
                .withColor(Formatting.AQUA));

        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint deletehint cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.message.cancel"))))
                .withColor(Formatting.RED));

        MutableText row = areahint.util.TextCompat.empty()
            .append(submitBtn).append(areahint.util.TextCompat.of("  "))
            .append(cancelBtn);
        client.player.sendMessage(row, false);
    }

    // ===== 瀹搞儱鍙块弬瑙勭《 =====

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
