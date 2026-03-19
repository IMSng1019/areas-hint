package areahint.addhint;

import areahint.data.AreaData;
import areahint.file.FileManager;
import areahint.i18n.I18nManager;
import areahint.util.AreaDataConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * AddHint閸旂喕鍏樼粻锛勬倞閸?
 * 娴溿倓绨板蹇撴倻瀹稿弶婀侀崺鐔锋倳濞ｈ濮炴い鍓佸仯
 */
public class AddHintManager {
    private static AddHintManager instance;
    private final MinecraftClient client;

    private boolean isActive = false;
    private boolean isRecording = false;
    private AreaData selectedArea;
    private List<Double[]> newVertices = new ArrayList<>();

    public static AddHintManager getInstance() {
        if (instance == null) {
            instance = new AddHintManager();
        }
        return instance;
    }

    private AddHintManager() {
        this.client = MinecraftClient.getInstance();
    }

    /**
     * 閸氼垰濮〢ddHint濞翠胶鈻?
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
        isRecording = true;
        newVertices.clear();

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.area") + AreaDataConverter.getDisplayName(area)), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.general_2") + areahint.keyhandler.UnifiedKeyHandler.getRecordKeyDisplayName() + I18nManager.translate("addhint.message.vertex.record")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.button.record.finish")), false);
    }

    /**
     * 鐠佹澘缍嶈ぐ鎾冲娴ｅ秶鐤?
     */
    public void recordCurrentPosition() {
        if (!isRecording || client.player == null) return;

        int x = (int) Math.round(client.player.getX());
        int z = (int) Math.round(client.player.getZ());
        newVertices.add(new Double[]{(double) x, (double) z});

        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.vertex.record_2") + newVertices.size() + ": 鎼?(" + x + ", " + z + ")"), false);

        // 閺囧瓨鏌婃潏鍦櫕閸欘垵顫嬮崠?
        List<BlockPos> blockPosList = new ArrayList<>();
        for (Double[] v : newVertices) {
            blockPosList.add(new BlockPos(v[0].intValue(), (int) client.player.getY(), v[1].intValue()));
        }
        areahint.boundviz.BoundVizManager.getInstance().setTemporaryVertices(blockPosList, true);

        showPointRecordedOptions();
    }

    /**
     * 閹绘劒姘﹀ǎ璇插閻ㄥ嫰銆婇悙?
     */
    public void submit() {
        if (!isActive || selectedArea == null || client.player == null) return;

        if (newVertices.isEmpty()) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.vertex.record")), false);
            return;
        }

        isRecording = false;

        try {
            // 閸氬牆鑻熼崢鐔告箒妞ゅ墎鍋ｉ崪灞炬煀妞ゅ墎鍋?
            List<Double[]> allVertices = new ArrayList<>();
            if (selectedArea.getVertices() != null) {
                for (AreaData.Vertex v : selectedArea.getVertices()) {
                    allVertices.add(new Double[]{v.getX(), v.getZ()});
                }
            }
            allVertices.addAll(newVertices);

            // 閹稿顫楁惔锔藉笓鎼村繘妲诲顫唉閸?
            List<Double[]> sorted = sortVerticesByAngle(allVertices);

            // 鏉烆剚宕叉稉绡rtex閸掓銆?
            List<AreaData.Vertex> vertices = new ArrayList<>();
            for (Double[] v : sorted) {
                vertices.add(new AreaData.Vertex(v[0], v[1]));
            }

            // 闁插秵鏌婄拋锛勭暬娴滃瞼楠囨い鍓佸仯(AABB)
            List<AreaData.Vertex> secondVertices = calculateBoundingBox(sorted);

            // 閺囧瓨鏌婇崺鐔锋倳閺佺増宓?
            selectedArea.setVertices(vertices);
            selectedArea.setSecondVertices(secondVertices);

            // 閼惧嘲褰囪ぐ鎾冲缂佹潙瀹?
            String dimension = client.world.getRegistryKey().getValue().toString();

            // 閸欐垿鈧礁鍩岄張宥呭缁?
            AddHintClientNetworking.sendToServer(selectedArea, dimension);

            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.area") + selectedArea.getName() + I18nManager.translate("addhint.message.vertex_2")), false);

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
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
        if (client.player != null) {
            client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.error.vertex.cancel.add")), false);
        }
        reset();
    }

    private void reset() {
        isActive = false;
        isRecording = false;
        selectedArea = null;
        newVertices.clear();
        areahint.boundviz.BoundVizManager.getInstance().clearTemporaryVertices();
    }

    /**
     * 閹稿顫楁惔锔藉笓鎼村繘銆婇悙鐧哥礉闂冨弶顒涙径姘崇珶瑜般垻鍤庡▓鍏告唉閸?
     */
    private List<Double[]> sortVerticesByAngle(List<Double[]> vertices) {
        // 鐠侊紕鐣荤拹銊ョ妇
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

    /**
     * 鐠侊紕鐣籄ABB閸栧懎娲块惄鎺嶇稊娑撹桨绨╃痪褔銆婇悙?
     */
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

    /**
     * 閼惧嘲褰囬崣顖欐叏閺€鍦畱閸╃喎鎮曢崚妤勩€?
     */
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

    // UI閺傝纭?

    private void showAreaSelection(List<AreaData> areas) {
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.title.area.vertex.add")), false);
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.area.vertex.add")), false);

        for (AreaData area : areas) {
            String displayName = AreaDataConverter.getDisplayName(area);
            MutableText btn = areahint.util.TextCompat.literal("鎼?[" + displayName + "]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/areahint addhint select \"" + area.getName() + "\""))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        areahint.util.TextCompat.of(I18nManager.translate("addhint.prompt.general") + displayName + I18nManager.translate("addhint.message.general") + area.getSignature())))
                    .withColor(Formatting.GOLD));
            client.player.sendMessage(btn, false);
        }

        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint addhint cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.message.cancel"))))
                .withColor(Formatting.RED));
        client.player.sendMessage(cancelBtn, false);
    }

    private void showPointRecordedOptions() {
        int count = newVertices.size();
        client.player.sendMessage(areahint.util.TextCompat.of(I18nManager.translate("addhint.message.record") + count + I18nManager.translate("addhint.message.vertex")), false);

        MutableText continueBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.button.record.continue"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint addhint continue"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.message.record.continue"))))
                .withColor(Formatting.GREEN));

        MutableText submitBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.button.general"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint addhint submit"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.message.vertex_3"))))
                .withColor(Formatting.AQUA));

        MutableText cancelBtn = areahint.util.TextCompat.literal(I18nManager.translate("addhint.error.cancel"))
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/areahint addhint cancel"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, areahint.util.TextCompat.of(I18nManager.translate("addhint.message.cancel"))))
                .withColor(Formatting.RED));

        MutableText row = areahint.util.TextCompat.empty()
            .append(continueBtn).append(areahint.util.TextCompat.of("  "))
            .append(submitBtn).append(areahint.util.TextCompat.of("  "))
            .append(cancelBtn);
        client.player.sendMessage(row, false);
    }

    // Getters
    public boolean isActive() { return isActive; }
    public boolean isRecording() { return isRecording; }
}
