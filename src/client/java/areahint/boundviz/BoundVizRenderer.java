package areahint.boundviz;

import areahint.data.AreaData;
import areahint.render.FlashColorHelper;
import areahint.util.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.nio.FloatBuffer;
import java.util.List;

public class BoundVizRenderer {

    // ========== 缂撳瓨鏁版嵁缁撴瀯 ==========
    private static class CachedArea {
        List<int[]> triangles;          // 棰勮绠楃殑涓夎鍓栧垎
        List<AreaData.Vertex> vertices; // 椤剁偣寮曠敤锛堟柟鍧椾氦鎺ョ嚎璁＄畻鐢級
        float[] vx, vz;                // 棰勮绠楃殑float椤剁偣鍧愭爣
        float minY, maxY;
        float r, g, b;
        String colorMode; // 闂儊棰滆壊妯″紡锛坣ull琛ㄧず闈欐€侀鑹诧級
        float[] vr, vg, vb; // 鍗曞瓧妯″紡閫愰《鐐归鑹?
        // AABB锛堢敤浜庤閿ュ墧闄わ級
        double aabbMinX, aabbMaxX, aabbMinZ, aabbMaxZ;
        // 鏂瑰潡浜ゆ帴绾跨紦瀛?
        List<float[]> blockIntersections;
        int lastPlayerBX, lastPlayerBY, lastPlayerBZ;
    }

    // ========== 闈欐€佺紦瀛?==========
    private static final List<CachedArea> cachedAreas = new ArrayList<>();
    private static int cachedVersion = -1;
    // 瑙嗛敟骞抽潰 [6涓钩闈[a,b,c,d]
    private static final float[][] frustumPlanes = new float[6][4];
    // 澶嶇敤鐭╅樀锛岄伩鍏嶆瘡甯у垎閰?
    private static final Matrix4f vpMatrix = new Matrix4f();
    // 鍙鎬х紦瀛橈紝閬垮厤涓や釜pass閲嶅瑙嗛敟妫€娴?
    private static boolean[] visibleFlags = new boolean[0];

    // ========== 涓绘覆鏌撴柟娉?==========
    public static void render(MatrixStack matrices, float tickDelta) {
        BoundVizManager manager = BoundVizManager.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        boolean hasTempVertices = manager.shouldShowTemporaryVertices();
        if (!manager.isEnabled() && !hasTempVertices) return;

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        // 鎻愬彇瑙嗛敟骞抽潰锛堟瘡甯у姩鎬佹洿鏂帮紝璺熼殢鐜╁瑙嗚锛?
        extractFrustumPlanes(matrices.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix());

        // 鏇存柊鍑犱綍缂撳瓨锛堜粎鍦ㄦ暟鎹彉鍖栨椂閲嶅缓锛?
        if (manager.isEnabled()) {
            updateCache(manager, client);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (manager.isEnabled()) {
            renderCachedAreas(matrix, buffer, cameraPos);
        }

        if (hasTempVertices) {
            renderTemporaryVertices(matrices, buffer, manager.getTemporaryVerticesDirect(), client);
        }

        matrices.pop();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // ========== 瑙嗛敟鍓旈櫎 ==========

    /**
     * 浠嶸P鐭╅樀鎻愬彇6涓閿ュ钩闈紙姣忓抚鍔ㄦ€佹洿鏂帮級
     * 浣跨敤Gribb/Hartmann鏂规硶锛孞OML鐨刴[col][row]鍛藉悕
     */
    private static void extractFrustumPlanes(Matrix4f view, Matrix4f proj) {
        vpMatrix.load(proj);
        vpMatrix.multiply(view);
        float[] vp = getRowMajor(vpMatrix);
        // Left: row3 + row0
        frustumPlanes[0][0] = vp[12] + vp[0];
        frustumPlanes[0][1] = vp[13] + vp[1];
        frustumPlanes[0][2] = vp[14] + vp[2];
        frustumPlanes[0][3] = vp[15] + vp[3];
        // Right: row3 - row0
        frustumPlanes[1][0] = vp[12] - vp[0];
        frustumPlanes[1][1] = vp[13] - vp[1];
        frustumPlanes[1][2] = vp[14] - vp[2];
        frustumPlanes[1][3] = vp[15] - vp[3];
        // Bottom: row3 + row1
        frustumPlanes[2][0] = vp[12] + vp[4];
        frustumPlanes[2][1] = vp[13] + vp[5];
        frustumPlanes[2][2] = vp[14] + vp[6];
        frustumPlanes[2][3] = vp[15] + vp[7];
        // Top: row3 - row1
        frustumPlanes[3][0] = vp[12] - vp[4];
        frustumPlanes[3][1] = vp[13] - vp[5];
        frustumPlanes[3][2] = vp[14] - vp[6];
        frustumPlanes[3][3] = vp[15] - vp[7];
        // Near: row3 + row2
        frustumPlanes[4][0] = vp[12] + vp[8];
        frustumPlanes[4][1] = vp[13] + vp[9];
        frustumPlanes[4][2] = vp[14] + vp[10];
        frustumPlanes[4][3] = vp[15] + vp[11];
        // Far: row3 - row2
        frustumPlanes[5][0] = vp[12] - vp[8];
        frustumPlanes[5][1] = vp[13] - vp[9];
        frustumPlanes[5][2] = vp[14] - vp[10];
        frustumPlanes[5][3] = vp[15] - vp[11];
        // 褰掍竴鍖?
        for (int i = 0; i < 6; i++) {
            float len = (float) Math.sqrt(
                frustumPlanes[i][0] * frustumPlanes[i][0] +
                frustumPlanes[i][1] * frustumPlanes[i][1] +
                frustumPlanes[i][2] * frustumPlanes[i][2]);
            if (len > 0) {
                frustumPlanes[i][0] /= len;
                frustumPlanes[i][1] /= len;
                frustumPlanes[i][2] /= len;
                frustumPlanes[i][3] /= len;
            }
        }
    }

    private static float[] getRowMajor(Matrix4f matrix) {
        FloatBuffer buffer = FloatBuffer.allocate(16);
        matrix.writeRowMajor(buffer);
        return buffer.array();
    }

    /**
     * AABB瑙嗛敟娴嬭瘯锛堢浉鏈虹浉瀵瑰潗鏍囩郴锛?
     * 瀵规瘡涓钩闈㈡壘P-vertex锛岃嫢鍦ㄥ钩闈㈠渚у垯鏁翠釜AABB涓嶅彲瑙?
     */
    private static boolean isAABBInFrustum(double minX, double minY, double minZ,
                                            double maxX, double maxY, double maxZ,
                                            Vec3d cam) {
        double rMinX = minX - cam.x, rMaxX = maxX - cam.x;
        double rMinY = minY - cam.y, rMaxY = maxY - cam.y;
        double rMinZ = minZ - cam.z, rMaxZ = maxZ - cam.z;
        for (int i = 0; i < 6; i++) {
            float a = frustumPlanes[i][0], b = frustumPlanes[i][1];
            float c = frustumPlanes[i][2], d = frustumPlanes[i][3];
            double px = a > 0 ? rMaxX : rMinX;
            double py = b > 0 ? rMaxY : rMinY;
            double pz = c > 0 ? rMaxZ : rMinZ;
            if (a * px + b * py + c * pz + d < 0) return false;
        }
        return true;
    }

    // ========== 缂撳瓨绠＄悊 ==========

    /**
     * 鏇存柊缂撳瓨锛氫粎鍦ㄧ増鏈彉鍖栨椂閲嶅缓鍑犱綍鏁版嵁
     */
    private static void updateCache(BoundVizManager manager, MinecraftClient client) {
        int ver = manager.getVersion();
        if (ver == cachedVersion) return;
        cachedVersion = ver;

        List<AreaData> areas = manager.getCurrentDimensionAreasDirect();
        cachedAreas.clear();
        for (AreaData area : areas) {
            CachedArea ca = buildAreaCache(area);
            if (ca != null) cachedAreas.add(ca);
        }
    }

    /**
     * 涓哄崟涓煙鍚嶆瀯寤虹紦瀛橈細涓夎鍓栧垎銆丄ABB銆侀鑹?
     */
    private static CachedArea buildAreaCache(AreaData area) {
        List<AreaData.Vertex> verts = area.getVertices();
        if (verts == null || verts.size() < 3) return null;

        CachedArea ca = new CachedArea();
        ca.vertices = verts;
        ca.triangles = earClipTriangulate(verts);

        // 棰勮绠梖loat椤剁偣鍧愭爣锛岄伩鍏嶆瘡甯ouble鈫抐loat杞崲
        int n = verts.size();
        ca.vx = new float[n];
        ca.vz = new float[n];
        ca.aabbMinX = ca.aabbMaxX = verts.get(0).getX();
        ca.aabbMinZ = ca.aabbMaxZ = verts.get(0).getZ();
        for (int i = 0; i < n; i++) {
            double x = verts.get(i).getX(), z = verts.get(i).getZ();
            ca.vx[i] = (float) x;
            ca.vz[i] = (float) z;
            if (x < ca.aabbMinX) ca.aabbMinX = x;
            if (x > ca.aabbMaxX) ca.aabbMaxX = x;
            if (z < ca.aabbMinZ) ca.aabbMinZ = z;
            if (z > ca.aabbMaxZ) ca.aabbMaxZ = z;
        }

        AreaData.AltitudeData alt = area.getAltitude();
        ca.minY = (float) (alt != null && alt.getMin() != null ? alt.getMin() : -64);
        ca.maxY = (float) (alt != null && alt.getMax() != null ? alt.getMax() : 320);

        String color = area.getColor();
        if (ColorUtil.isFlashColor(color)) {
            ca.colorMode = color;
            ca.r = 1f; ca.g = 1f; ca.b = 1f;
            if (FlashColorHelper.isPerCharMode(color)) {
                ca.vr = new float[n];
                ca.vg = new float[n];
                ca.vb = new float[n];
            }
        } else {
            ca.colorMode = null;
            int[] rgb = ColorUtil.parseColor(color);
            ca.r = rgb[0] / 255.0f;
            ca.g = rgb[1] / 255.0f;
            ca.b = rgb[2] / 255.0f;
        }

        // 鏂瑰潡浜ゆ帴绾垮垵濮嬪寲涓虹┖锛屽欢杩熻绠?
        ca.blockIntersections = null;
        ca.lastPlayerBX = Integer.MIN_VALUE;
        return ca;
    }

    // ========== 鎵归噺娓叉煋锛?娆raw call鏇夸唬鏁扮櫨娆★級 ==========

    /**
     * 娓叉煋鎵€鏈夊彲瑙佸煙鍚嶏細瑙嗛敟鍓旈櫎 + 鎵归噺鎻愪氦
     * 鎵€鏈変笁瑙掑舰鍚堝苟涓?娆raw call锛屾墍鏈夌嚎娈靛悎骞朵负1娆raw call
     */
    private static void renderCachedAreas(Matrix4f matrix, BufferBuilder buffer, Vec3d cam) {
        MinecraftClient client = MinecraftClient.getInstance();
        int playerBX = (int) Math.floor(cam.x);
        int playerBY = (int) Math.floor(cam.y);
        int playerBZ = (int) Math.floor(cam.z);
        int size = cachedAreas.size();

        // 涓€娆℃€ц绠楀彲瑙佹€э紝涓や釜pass澶嶇敤
        if (visibleFlags.length < size) visibleFlags = new boolean[size];
        long now = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            CachedArea ca = cachedAreas.get(i);
            visibleFlags[i] = isAABBInFrustum(ca.aabbMinX, ca.minY, ca.aabbMinZ,
                                               ca.aabbMaxX, ca.maxY, ca.aabbMaxZ, cam);
            // 鍔ㄦ€佹洿鏂伴棯鐑侀鑹?
            if (ca.colorMode != null && visibleFlags[i]) {
                if (ca.vr != null) {
                    // 鍗曞瓧妯″紡锛氶€愰《鐐逛笉鍚岀浉浣?
                    for (int vi = 0; vi < ca.vr.length; vi++) {
                        int rgb = FlashColorHelper.getCharColor(ca.colorMode, now, vi);
                        ca.vr[vi] = ((rgb >> 16) & 0xFF) / 255.0f;
                        ca.vg[vi] = ((rgb >> 8) & 0xFF) / 255.0f;
                        ca.vb[vi] = (rgb & 0xFF) / 255.0f;
                    }
                    // r,g,b鐢ㄤ簬鏂瑰潡浜ゆ帴绾匡紙鍙栭《鐐?鐨勯鑹诧級
                    ca.r = ca.vr[0]; ca.g = ca.vg[0]; ca.b = ca.vb[0];
                } else {
                    // 鏁翠綋妯″紡
                    int rgb = FlashColorHelper.getWholeColor(ca.colorMode, now);
                    ca.r = ((rgb >> 16) & 0xFF) / 255.0f;
                    ca.g = ((rgb >> 8) & 0xFF) / 255.0f;
                    ca.b = (rgb & 0xFF) / 255.0f;
                }
            }
        }

        // === Pass 1: 鎵归噺涓夎褰?===
        boolean hasTriangles = false;
        for (int i = 0; i < size; i++) {
            if (!visibleFlags[i]) continue;
            if (!hasTriangles) {
                buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
                hasTriangles = true;
            }
            emitAreaTriangles(matrix, buffer, cachedAreas.get(i));
        }
        if (hasTriangles) {
            buffer.end();
            BufferRenderer.draw(buffer);
        }

        // === Pass 2: 鎵归噺绾挎 ===
        boolean hasLines = false;
        for (int i = 0; i < size; i++) {
            if (!visibleFlags[i]) continue;
            CachedArea ca = cachedAreas.get(i);
            if (!hasLines) {
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                hasLines = true;
            }
            emitAreaLines(matrix, buffer, ca);
            updateBlockIntersectionsIfNeeded(ca, playerBX, playerBY, playerBZ, client);
            if (ca.blockIntersections != null) {
                for (float[] seg : ca.blockIntersections) {
                    buffer.vertex(matrix, seg[0], seg[1], seg[2]).color(ca.r, ca.g, ca.b, 0.8f).next();
                    buffer.vertex(matrix, seg[3], seg[4], seg[5]).color(ca.r, ca.g, ca.b, 0.8f).next();
                }
            }
        }
        if (hasLines) {
            buffer.end();
            BufferRenderer.draw(buffer);
        }
    }

    /**
     * 灏嗗崟涓煙鍚嶇殑涓夎褰㈡暟鎹啓鍏ユ壒閲廱uffer
     * 鍖呭惈搴曢潰銆侀《闈€佷晶闈紙TRIANGLE_STRIP杞负TRIANGLES浠ユ敮鎸佹壒閲忥級
     */
    private static void emitAreaTriangles(Matrix4f matrix, BufferBuilder buffer, CachedArea ca) {
        float[] vx = ca.vx, vz = ca.vz;
        float r = ca.r, g = ca.g, b = ca.b;
        float[] vr = ca.vr, vg = ca.vg, vb = ca.vb;
        boolean perVertex = vr != null;
        float minY = ca.minY, maxY = ca.maxY;

        // 搴曢潰 + 椤堕潰
        for (int[] tri : ca.triangles) {
            for (int idx : tri) {
                float cr = perVertex ? vr[idx] : r, cg = perVertex ? vg[idx] : g, cb = perVertex ? vb[idx] : b;
                buffer.vertex(matrix, vx[idx], minY, vz[idx]).color(cr, cg, cb, 0.2f).next();
            }
            for (int idx : tri) {
                float cr = perVertex ? vr[idx] : r, cg = perVertex ? vg[idx] : g, cb = perVertex ? vb[idx] : b;
                buffer.vertex(matrix, vx[idx], maxY, vz[idx]).color(cr, cg, cb, 0.2f).next();
            }
        }

        // 渚ч潰锛氭瘡涓竟鐨剄uad鎷嗕负2涓笁瑙掑舰
        int n = vx.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float ri = perVertex ? vr[i] : r, gi = perVertex ? vg[i] : g, bi = perVertex ? vb[i] : b;
            float rj = perVertex ? vr[j] : r, gj = perVertex ? vg[j] : g, bj = perVertex ? vb[j] : b;
            buffer.vertex(matrix, vx[i], minY, vz[i]).color(ri, gi, bi, 0.2f).next();
            buffer.vertex(matrix, vx[i], maxY, vz[i]).color(ri, gi, bi, 0.2f).next();
            buffer.vertex(matrix, vx[j], minY, vz[j]).color(rj, gj, bj, 0.2f).next();
            buffer.vertex(matrix, vx[i], maxY, vz[i]).color(ri, gi, bi, 0.2f).next();
            buffer.vertex(matrix, vx[j], maxY, vz[j]).color(rj, gj, bj, 0.2f).next();
            buffer.vertex(matrix, vx[j], minY, vz[j]).color(rj, gj, bj, 0.2f).next();
        }
    }

    /**
     * 灏嗗崟涓煙鍚嶇殑杈圭晫绾挎暟鎹啓鍏ユ壒閲廱uffer锛圖EBUG_LINES妯″紡锛?
     * 搴曢儴绾裤€侀《閮ㄧ嚎銆佸瀭鐩寸嚎鍏ㄩ儴杞负鐙珛绾挎瀵?
     */
    private static void emitAreaLines(Matrix4f matrix, BufferBuilder buffer, CachedArea ca) {
        float[] vx = ca.vx, vz = ca.vz;
        float r = ca.r, g = ca.g, b = ca.b;
        float[] vr = ca.vr, vg = ca.vg, vb = ca.vb;
        boolean perVertex = vr != null;
        float minY = ca.minY, maxY = ca.maxY;
        int n = vx.length;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float ri = perVertex ? vr[i] : r, gi = perVertex ? vg[i] : g, bi = perVertex ? vb[i] : b;
            float rj = perVertex ? vr[j] : r, gj = perVertex ? vg[j] : g, bj = perVertex ? vb[j] : b;
            buffer.vertex(matrix, vx[i], minY, vz[i]).color(ri, gi, bi, 0.8f).next();
            buffer.vertex(matrix, vx[j], minY, vz[j]).color(rj, gj, bj, 0.8f).next();
            buffer.vertex(matrix, vx[i], maxY, vz[i]).color(ri, gi, bi, 0.8f).next();
            buffer.vertex(matrix, vx[j], maxY, vz[j]).color(rj, gj, bj, 0.8f).next();
        }

        for (int i = 0; i < n; i++) {
            float ri = perVertex ? vr[i] : r, gi = perVertex ? vg[i] : g, bi = perVertex ? vb[i] : b;
            buffer.vertex(matrix, vx[i], minY, vz[i]).color(ri, gi, bi, 0.8f).next();
            buffer.vertex(matrix, vx[i], maxY, vz[i]).color(ri, gi, bi, 0.8f).next();
        }
    }

    // ========== 鏂瑰潡浜ゆ帴绾匡紙甯︿綅缃紦瀛橈級 ==========

    private static final int BLOCK_REBUILD_DIST_SQ = 16; // 鐜╁绉诲姩4鏍兼墠閲嶇畻

    /**
     * 浠呭綋鐜╁绉诲姩瓒呰繃闃堝€兼椂閲嶆柊璁＄畻鏂瑰潡浜ゆ帴绾?
     */
    private static void updateBlockIntersectionsIfNeeded(CachedArea ca, int pbx, int pby, int pbz, MinecraftClient client) {
        int dx = pbx - ca.lastPlayerBX, dy = pby - ca.lastPlayerBY, dz = pbz - ca.lastPlayerBZ;
        if (ca.blockIntersections != null && dx * dx + dy * dy + dz * dz < BLOCK_REBUILD_DIST_SQ) return;

        ca.lastPlayerBX = pbx;
        ca.lastPlayerBY = pby;
        ca.lastPlayerBZ = pbz;
        ca.blockIntersections = computeBlockIntersections(ca, client);
    }

    private static final float FACE_OFFSET = 0.002f;

    /**
     * 璁＄畻鍩熷悕渚ч潰涓庢柟鍧楃殑浜ゆ帴绾挎
     */
    private static List<float[]> computeBlockIntersections(CachedArea ca, MinecraftClient client) {
        World world = client.world;
        if (world == null) return null;

        Vec3d playerPos = client.player.getPos();
        int renderDist = 64;
        int renderDistSq = renderDist * renderDist;
        int yMin = Math.max((int) Math.floor(ca.minY), (int) playerPos.y - renderDist);
        int yMax = Math.min((int) Math.ceil(ca.maxY) - 1, (int) playerPos.y + renderDist);

        List<float[]> segments = new ArrayList<>();
        List<AreaData.Vertex> verts = ca.vertices;

        for (int ei = 0; ei < verts.size(); ei++) {
            AreaData.Vertex v1 = verts.get(ei);
            AreaData.Vertex v2 = verts.get((ei + 1) % verts.size());
            double ex1 = v1.getX(), ez1 = v1.getZ();
            double edx = v2.getX() - ex1, edz = v2.getZ() - ez1;
            if (Math.abs(edx) < 0.001 && Math.abs(edz) < 0.001) continue;

            // DDA鍏夋爡鍖栵細绮剧‘閬嶅巻杈圭嚎缁忚繃鐨勬瘡涓柟鍧楁牸瀛?
            int bx = (int) Math.floor(ex1), bz = (int) Math.floor(ez1);
            int endBx = (int) Math.floor(v2.getX()), endBz = (int) Math.floor(v2.getZ());
            int stepX = edx > 0 ? 1 : edx < 0 ? -1 : 0;
            int stepZ = edz > 0 ? 1 : edz < 0 ? -1 : 0;

            // tMaxX/Z: 鍒拌揪涓嬩竴涓猉/Z鏍肩嚎鐨則鍊? tDeltaX/Z: 璺ㄨ秺涓€涓牸瀛愮殑t澧為噺
            double tMaxX = Math.abs(edx) > 0.001 ? ((stepX > 0 ? bx + 1 : bx) - ex1) / edx : Double.MAX_VALUE;
            double tMaxZ = Math.abs(edz) > 0.001 ? ((stepZ > 0 ? bz + 1 : bz) - ez1) / edz : Double.MAX_VALUE;
            double tDeltaX = Math.abs(edx) > 0.001 ? Math.abs(1.0 / edx) : Double.MAX_VALUE;
            double tDeltaZ = Math.abs(edz) > 0.001 ? Math.abs(1.0 / edz) : Double.MAX_VALUE;

            int maxSteps = Math.abs(endBx - bx) + Math.abs(endBz - bz) + 2;
            for (int s = 0; s < maxSteps; s++) {
                double ddx = bx + 0.5 - playerPos.x, ddz = bz + 0.5 - playerPos.z;
                if (ddx * ddx + ddz * ddz <= renderDistSq) {
                    collectFaceIntersections(segments, world, ex1, ez1, edx, edz,
                            bx, bz, yMin, yMax, ca.minY, ca.maxY);
                    collectHorizontalLines(segments, world, ex1, ez1, edx, edz,
                            bx, bz, yMin, yMax, ca.minY, ca.maxY);
                }
                if (bx == endBx && bz == endBz) break;
                if (tMaxX < tMaxZ) { bx += stepX; tMaxX += tDeltaX; }
                else { bz += stepZ; tMaxZ += tDeltaZ; }
            }
        }
        return segments;
    }

    /**
     * 鏀堕泦杈圭嚎涓庢柟鍧楅潰鐨勪氦鐐圭嚎娈碉紙鍖?鍗?涓?瑗块潰锛?
     */
    private static void collectFaceIntersections(List<float[]> segments, World world,
            double ex1, double ez1, double edx, double edz,
            int bx, int bz, int yMin, int yMax, float fMinY, float fMaxY) {

        if (Math.abs(edz) > 0.001) {
            double t = (bz - ez1) / edz;
            if (t >= 0 && t <= 1) {
                double ix = ex1 + edx * t;
                if (ix >= bx && ix <= bx + 1) {
                    collectVerticalLines(segments, world, (float) ix, (float) bz - FACE_OFFSET, bx, bz, yMin, yMax, fMinY, fMaxY);
                    collectVerticalLines(segments, world, (float) ix, (float) bz + FACE_OFFSET, bx, bz - 1, yMin, yMax, fMinY, fMaxY);
                }
            }
            t = (bz + 1 - ez1) / edz;
            if (t >= 0 && t <= 1) {
                double ix = ex1 + edx * t;
                if (ix >= bx && ix <= bx + 1) {
                    collectVerticalLines(segments, world, (float) ix, (float) (bz + 1) + FACE_OFFSET, bx, bz, yMin, yMax, fMinY, fMaxY);
                    collectVerticalLines(segments, world, (float) ix, (float) (bz + 1) - FACE_OFFSET, bx, bz + 1, yMin, yMax, fMinY, fMaxY);
                }
            }
        }

        if (Math.abs(edx) > 0.001) {
            double t = (bx - ex1) / edx;
            if (t >= 0 && t <= 1) {
                double iz = ez1 + edz * t;
                if (iz >= bz && iz <= bz + 1) {
                    collectVerticalLines(segments, world, (float) bx - FACE_OFFSET, (float) iz, bx, bz, yMin, yMax, fMinY, fMaxY);
                    collectVerticalLines(segments, world, (float) bx + FACE_OFFSET, (float) iz, bx - 1, bz, yMin, yMax, fMinY, fMaxY);
                }
            }
            t = (bx + 1 - ex1) / edx;
            if (t >= 0 && t <= 1) {
                double iz = ez1 + edz * t;
                if (iz >= bz && iz <= bz + 1) {
                    collectVerticalLines(segments, world, (float) (bx + 1) + FACE_OFFSET, (float) iz, bx, bz, yMin, yMax, fMinY, fMaxY);
                    collectVerticalLines(segments, world, (float) (bx + 1) - FACE_OFFSET, (float) iz, bx + 1, bz, yMin, yMax, fMinY, fMaxY);
                }
            }
        }
    }

    /**
     * 鏀堕泦鏂瑰潡椤堕潰/搴曢潰涓庤竟鐣屽浜ゆ帴澶勭殑姘村钩绾挎
     */
    private static void collectHorizontalLines(List<float[]> segments, World world,
            double ex1, double ez1, double edx, double edz,
            int bx, int bz, int yMin, int yMax, float fMinY, float fMaxY) {

        double tMin = 0, tMax = 1;
        if (Math.abs(edx) > 0.001) {
            double t1 = (bx - ex1) / edx, t2 = (bx + 1 - ex1) / edx;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
        } else if (ex1 < bx || ex1 > bx + 1) return;

        if (Math.abs(edz) > 0.001) {
            double t1 = (bz - ez1) / edz, t2 = (bz + 1 - ez1) / edz;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
        } else if (ez1 < bz || ez1 > bz + 1) return;

        if (tMin >= tMax) return;

        float x1 = (float)(ex1 + edx * tMin), z1 = (float)(ez1 + edz * tMin);
        float x2 = (float)(ex1 + edx * tMax), z2 = (float)(ez1 + edz * tMax);
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int by = yMin; by <= yMax; by++) {
            pos.set(bx, by, bz);
            if (!world.getBlockState(pos).isOpaqueFullCube(world, pos)) continue;

            float bottom = Math.max(by, fMinY);
            float top = Math.min(by + 1, fMaxY);
            if (top <= bottom) continue;

            pos.set(bx, by + 1, bz);
            if (top == by + 1 && !world.getBlockState(pos).isOpaqueFullCube(world, pos)) {
                segments.add(new float[]{x1, top + FACE_OFFSET, z1, x2, top + FACE_OFFSET, z2});
            }
            pos.set(bx, by - 1, bz);
            if (bottom == by && !world.getBlockState(pos).isOpaqueFullCube(world, pos)) {
                segments.add(new float[]{x1, bottom - FACE_OFFSET, z1, x2, bottom - FACE_OFFSET, z2});
            }
        }
    }

    /**
     * 鏀堕泦鏂瑰潡闈笂鐨勫瀭鐩寸嚎娈碉紙浠呭瀹炲績鏂瑰潡锛?
     */
    private static void collectVerticalLines(List<float[]> segments, World world,
            float fx, float fz, int bx, int bz,
            int yMin, int yMax, float fMinY, float fMaxY) {

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int by = yMin; by <= yMax; by++) {
            pos.set(bx, by, bz);
            if (!world.getBlockState(pos).isOpaqueFullCube(world, pos)) continue;

            float lineBottom = Math.max(by, fMinY);
            float lineTop = Math.min(by + 1, fMaxY);
            if (lineTop <= lineBottom) continue;

            segments.add(new float[]{fx, lineBottom, fz, fx, lineTop, fz});
        }
    }

    // ========== 涓存椂椤剁偣娓叉煋 ==========

    private static void renderTemporaryVertices(MatrixStack matrices, BufferBuilder buffer, List<BlockPos> vertices, MinecraftClient client) {
        if (vertices.isEmpty()) return;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float py = (float) client.player.getY();

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (BlockPos pos : vertices) {
            float px = pos.getX() + 0.5f, pz = pos.getZ() + 0.5f;
            float size = 0.4f;
            buffer.vertex(matrix, px - size, py, pz).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, px + size, py, pz).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, px, py, pz - size).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, px, py, pz + size).color(1f, 1f, 1f, 1f).next();
        }
        // 铏氱嚎杩炴帴锛堟壒閲忥級
        for (int i = 0; i < vertices.size() - 1; i++) {
            BlockPos v1 = vertices.get(i), v2 = vertices.get(i + 1);
            emitDashedLine(matrix, buffer, v1.getX() + 0.5f, v2.getX() + 0.5f,
                    v1.getZ() + 0.5f, v2.getZ() + 0.5f, py);
        }
        buffer.end();
        BufferRenderer.draw(buffer);
    }

    private static void emitDashedLine(Matrix4f matrix, BufferBuilder buffer,
                                        float x1, float x2, float z1, float z2, float y) {
        double dx = x2 - x1, dz = z2 - z1;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 0.01) return;

        double segLen = 0.5, total = 0.8; // seg + gap
        for (double d = 0; d < distance; d += total) {
            double t1 = d / distance;
            double t2 = Math.min((d + segLen) / distance, 1.0);
            buffer.vertex(matrix, (float)(x1 + dx * t1), y, (float)(z1 + dz * t1)).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, (float)(x1 + dx * t2), y, (float)(z1 + dz * t2)).color(1f, 1f, 1f, 1f).next();
        }
    }

    // ========== 鑰冲垏娉曚笁瑙掑墫鍒?==========

    private static List<int[]> earClipTriangulate(List<AreaData.Vertex> polygon) {
        int n = polygon.size();
        List<int[]> triangles = new ArrayList<>();
        if (n < 3) return triangles;

        double area = 0;
        for (int i = 0; i < n; i++) {
            AreaData.Vertex c = polygon.get(i);
            AreaData.Vertex nx = polygon.get((i + 1) % n);
            area += c.getX() * nx.getZ() - nx.getX() * c.getZ();
        }
        boolean ccw = area > 0;

        List<Integer> rem = new ArrayList<>();
        for (int i = 0; i < n; i++) rem.add(i);

        int safe = n * n;
        while (rem.size() > 2 && safe-- > 0) {
            boolean found = false;
            for (int i = 0; i < rem.size(); i++) {
                int pi = rem.get((i - 1 + rem.size()) % rem.size());
                int ci = rem.get(i);
                int ni = rem.get((i + 1) % rem.size());
                if (!isEar(polygon, rem, pi, ci, ni, ccw)) continue;
                triangles.add(new int[]{pi, ci, ni});
                rem.remove(i);
                found = true;
                break;
            }
            if (!found) break;
        }
        return triangles;
    }

    private static boolean isEar(List<AreaData.Vertex> poly, List<Integer> rem,
                                  int pi, int ci, int ni, boolean ccw) {
        AreaData.Vertex a = poly.get(pi), b = poly.get(ci), c = poly.get(ni);
        double cross = (b.getX() - a.getX()) * (c.getZ() - a.getZ())
                     - (b.getZ() - a.getZ()) * (c.getX() - a.getX());
        if (ccw ? cross <= 0 : cross >= 0) return false;
        for (int idx : rem) {
            if (idx == pi || idx == ci || idx == ni) continue;
            if (pointInTriangle(poly.get(idx), a, b, c)) return false;
        }
        return true;
    }

    private static boolean pointInTriangle(AreaData.Vertex p,
                                            AreaData.Vertex a, AreaData.Vertex b, AreaData.Vertex c) {
        double d1 = sign(p, a, b), d2 = sign(p, b, c), d3 = sign(p, c, a);
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }

    private static double sign(AreaData.Vertex p1, AreaData.Vertex p2, AreaData.Vertex p3) {
        return (p1.getX() - p3.getX()) * (p2.getZ() - p3.getZ())
             - (p2.getX() - p3.getX()) * (p1.getZ() - p3.getZ());
    }
}
