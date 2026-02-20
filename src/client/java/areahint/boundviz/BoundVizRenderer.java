package areahint.boundviz;

import areahint.data.AreaData;
import areahint.util.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoundVizRenderer {

    // ========== 缓存数据结构 ==========
    private static class CachedArea {
        List<int[]> triangles;          // 预计算的三角剖分
        List<AreaData.Vertex> vertices; // 顶点引用
        double minY, maxY;
        float r, g, b;
        // AABB（用于视锥剔除）
        double aabbMinX, aabbMaxX, aabbMinZ, aabbMaxZ;
        // 方块交接线缓存
        List<float[]> blockIntersections;
        int lastPlayerBX, lastPlayerBY, lastPlayerBZ; // 上次计算时的玩家方块坐标
    }

    // ========== 静态缓存 ==========
    private static final List<CachedArea> cachedAreas = new ArrayList<>();
    private static int cachedVersion = -1;
    // 视锥平面 [6个平面][a,b,c,d]
    private static final float[][] frustumPlanes = new float[6][4];

    // ========== 主渲染方法 ==========
    public static void render(MatrixStack matrices, float tickDelta) {
        BoundVizManager manager = BoundVizManager.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        boolean hasTempVertices = manager.shouldShowTemporaryVertices();
        if (!manager.isEnabled() && !hasTempVertices) return;

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        // 提取视锥平面（每帧动态更新，跟随玩家视角）
        extractFrustumPlanes(matrices.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix());

        // 更新几何缓存（仅在数据变化时重建）
        if (manager.isEnabled()) {
            updateCache(manager, client);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

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
        RenderSystem.disableBlend();
    }

    // ========== 视锥剔除 ==========

    /**
     * 从VP矩阵提取6个视锥平面（每帧动态更新）
     * 使用Gribb/Hartmann方法，JOML的m[col][row]命名
     */
    private static void extractFrustumPlanes(Matrix4f view, Matrix4f proj) {
        Matrix4f vp = new Matrix4f(proj).mul(view);
        // Left: row3 + row0
        frustumPlanes[0][0] = vp.m03() + vp.m00();
        frustumPlanes[0][1] = vp.m13() + vp.m10();
        frustumPlanes[0][2] = vp.m23() + vp.m20();
        frustumPlanes[0][3] = vp.m33() + vp.m30();
        // Right: row3 - row0
        frustumPlanes[1][0] = vp.m03() - vp.m00();
        frustumPlanes[1][1] = vp.m13() - vp.m10();
        frustumPlanes[1][2] = vp.m23() - vp.m20();
        frustumPlanes[1][3] = vp.m33() - vp.m30();
        // Bottom: row3 + row1
        frustumPlanes[2][0] = vp.m03() + vp.m01();
        frustumPlanes[2][1] = vp.m13() + vp.m11();
        frustumPlanes[2][2] = vp.m23() + vp.m21();
        frustumPlanes[2][3] = vp.m33() + vp.m31();
        // Top: row3 - row1
        frustumPlanes[3][0] = vp.m03() - vp.m01();
        frustumPlanes[3][1] = vp.m13() - vp.m11();
        frustumPlanes[3][2] = vp.m23() - vp.m21();
        frustumPlanes[3][3] = vp.m33() - vp.m31();
        // Near: row3 + row2
        frustumPlanes[4][0] = vp.m03() + vp.m02();
        frustumPlanes[4][1] = vp.m13() + vp.m12();
        frustumPlanes[4][2] = vp.m23() + vp.m22();
        frustumPlanes[4][3] = vp.m33() + vp.m32();
        // Far: row3 - row2
        frustumPlanes[5][0] = vp.m03() - vp.m02();
        frustumPlanes[5][1] = vp.m13() - vp.m12();
        frustumPlanes[5][2] = vp.m23() - vp.m22();
        frustumPlanes[5][3] = vp.m33() - vp.m32();
        // 归一化
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

    /**
     * AABB视锥测试（相机相对坐标系）
     * 对每个平面找P-vertex，若在平面外侧则整个AABB不可见
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

    // ========== 缓存管理 ==========

    /**
     * 更新缓存：仅在版本变化时重建几何数据
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
     * 为单个域名构建缓存：三角剖分、AABB、颜色
     */
    private static CachedArea buildAreaCache(AreaData area) {
        List<AreaData.Vertex> verts = area.getVertices();
        if (verts == null || verts.size() < 3) return null;

        CachedArea ca = new CachedArea();
        ca.vertices = verts;
        ca.triangles = earClipTriangulate(verts);

        AreaData.AltitudeData alt = area.getAltitude();
        ca.minY = alt != null && alt.getMin() != null ? alt.getMin() : -64;
        ca.maxY = alt != null && alt.getMax() != null ? alt.getMax() : 320;

        int[] rgb = ColorUtil.parseColor(area.getColor());
        ca.r = rgb[0] / 255.0f;
        ca.g = rgb[1] / 255.0f;
        ca.b = rgb[2] / 255.0f;

        // 计算XZ平面AABB
        ca.aabbMinX = ca.aabbMaxX = verts.get(0).getX();
        ca.aabbMinZ = ca.aabbMaxZ = verts.get(0).getZ();
        for (int i = 1; i < verts.size(); i++) {
            double x = verts.get(i).getX(), z = verts.get(i).getZ();
            if (x < ca.aabbMinX) ca.aabbMinX = x;
            if (x > ca.aabbMaxX) ca.aabbMaxX = x;
            if (z < ca.aabbMinZ) ca.aabbMinZ = z;
            if (z > ca.aabbMaxZ) ca.aabbMaxZ = z;
        }

        // 方块交接线初始化为空，延迟计算
        ca.blockIntersections = null;
        ca.lastPlayerBX = Integer.MIN_VALUE;
        return ca;
    }

    // ========== 批量渲染（2次draw call替代数百次） ==========

    /**
     * 渲染所有可见域名：视锥剔除 + 批量提交
     * 所有三角形合并为1次draw call，所有线段合并为1次draw call
     */
    private static void renderCachedAreas(Matrix4f matrix, BufferBuilder buffer, Vec3d cam) {
        MinecraftClient client = MinecraftClient.getInstance();
        int playerBX = (int) Math.floor(cam.x);
        int playerBY = (int) Math.floor(cam.y);
        int playerBZ = (int) Math.floor(cam.z);

        // === Pass 1: 批量三角形（顶面+底面+侧面，20%透明度） ===
        boolean hasTriangles = false;
        for (CachedArea ca : cachedAreas) {
            if (!isAABBInFrustum(ca.aabbMinX, ca.minY, ca.aabbMinZ,
                                  ca.aabbMaxX, ca.maxY, ca.aabbMaxZ, cam)) continue;
            if (!hasTriangles) {
                buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
                hasTriangles = true;
            }
            emitAreaTriangles(matrix, buffer, ca);
        }
        if (hasTriangles) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        // === Pass 2: 批量线段（边界线+方块交接线，80%透明度） ===
        boolean hasLines = false;
        for (CachedArea ca : cachedAreas) {
            if (!isAABBInFrustum(ca.aabbMinX, ca.minY, ca.aabbMinZ,
                                  ca.aabbMaxX, ca.maxY, ca.aabbMaxZ, cam)) continue;
            if (!hasLines) {
                buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                hasLines = true;
            }
            emitAreaLines(matrix, buffer, ca);
            // 方块交接线（带缓存）
            updateBlockIntersectionsIfNeeded(ca, playerBX, playerBY, playerBZ, client);
            if (ca.blockIntersections != null) {
                for (float[] seg : ca.blockIntersections) {
                    buffer.vertex(matrix, seg[0], seg[1], seg[2]).color(ca.r, ca.g, ca.b, 0.8f).next();
                    buffer.vertex(matrix, seg[3], seg[4], seg[5]).color(ca.r, ca.g, ca.b, 0.8f).next();
                }
            }
        }
        if (hasLines) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

    /**
     * 将单个域名的三角形数据写入批量buffer
     * 包含底面、顶面、侧面（TRIANGLE_STRIP转为TRIANGLES以支持批量）
     */
    private static void emitAreaTriangles(Matrix4f matrix, BufferBuilder buffer, CachedArea ca) {
        List<AreaData.Vertex> verts = ca.vertices;
        float r = ca.r, g = ca.g, b = ca.b;
        float minY = (float) ca.minY, maxY = (float) ca.maxY;

        // 底面 + 顶面
        for (int[] tri : ca.triangles) {
            for (int idx : tri) {
                AreaData.Vertex v = verts.get(idx);
                buffer.vertex(matrix, (float) v.getX(), minY, (float) v.getZ()).color(r, g, b, 0.2f).next();
            }
            for (int idx : tri) {
                AreaData.Vertex v = verts.get(idx);
                buffer.vertex(matrix, (float) v.getX(), maxY, (float) v.getZ()).color(r, g, b, 0.2f).next();
            }
        }

        // 侧面：每个边的quad拆为2个三角形
        for (int i = 0; i < verts.size(); i++) {
            AreaData.Vertex v1 = verts.get(i);
            AreaData.Vertex v2 = verts.get((i + 1) % verts.size());
            float x1 = (float) v1.getX(), z1 = (float) v1.getZ();
            float x2 = (float) v2.getX(), z2 = (float) v2.getZ();
            // 三角形1: v1-bottom, v1-top, v2-bottom
            buffer.vertex(matrix, x1, minY, z1).color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, x1, maxY, z1).color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, x2, minY, z2).color(r, g, b, 0.2f).next();
            // 三角形2: v1-top, v2-top, v2-bottom
            buffer.vertex(matrix, x1, maxY, z1).color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, x2, maxY, z2).color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, x2, minY, z2).color(r, g, b, 0.2f).next();
        }
    }

    /**
     * 将单个域名的边界线数据写入批量buffer（DEBUG_LINES模式）
     * 底部线、顶部线、垂直线全部转为独立线段对
     */
    private static void emitAreaLines(Matrix4f matrix, BufferBuilder buffer, CachedArea ca) {
        List<AreaData.Vertex> verts = ca.vertices;
        float r = ca.r, g = ca.g, b = ca.b;
        float minY = (float) ca.minY, maxY = (float) ca.maxY;

        // 底部 + 顶部边界线（LINE_STRIP转为独立线段对）
        for (int i = 0; i < verts.size(); i++) {
            AreaData.Vertex v1 = verts.get(i);
            AreaData.Vertex v2 = verts.get((i + 1) % verts.size());
            float x1 = (float) v1.getX(), z1 = (float) v1.getZ();
            float x2 = (float) v2.getX(), z2 = (float) v2.getZ();
            // 底部线段
            buffer.vertex(matrix, x1, minY, z1).color(r, g, b, 0.8f).next();
            buffer.vertex(matrix, x2, minY, z2).color(r, g, b, 0.8f).next();
            // 顶部线段
            buffer.vertex(matrix, x1, maxY, z1).color(r, g, b, 0.8f).next();
            buffer.vertex(matrix, x2, maxY, z2).color(r, g, b, 0.8f).next();
        }

        // 垂直边界线
        for (AreaData.Vertex v : verts) {
            float x = (float) v.getX(), z = (float) v.getZ();
            buffer.vertex(matrix, x, minY, z).color(r, g, b, 0.8f).next();
            buffer.vertex(matrix, x, maxY, z).color(r, g, b, 0.8f).next();
        }
    }

    // ========== 方块交接线（带位置缓存） ==========

    private static final int BLOCK_REBUILD_DIST_SQ = 16; // 玩家移动4格才重算

    /**
     * 仅当玩家移动超过阈值时重新计算方块交接线
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
     * 计算域名侧面与方块的交接线段
     */
    private static List<float[]> computeBlockIntersections(CachedArea ca, MinecraftClient client) {
        World world = client.world;
        if (world == null) return null;

        Vec3d playerPos = client.player.getPos();
        int renderDist = 64;
        int yMin = Math.max((int) Math.floor(ca.minY), (int) playerPos.y - renderDist);
        int yMax = Math.min((int) Math.ceil(ca.maxY) - 1, (int) playerPos.y + renderDist);

        List<float[]> segments = new ArrayList<>();
        List<AreaData.Vertex> verts = ca.vertices;

        for (int ei = 0; ei < verts.size(); ei++) {
            AreaData.Vertex v1 = verts.get(ei);
            AreaData.Vertex v2 = verts.get((ei + 1) % verts.size());
            double ex1 = v1.getX(), ez1 = v1.getZ();
            double edx = v2.getX() - ex1, edz = v2.getZ() - ez1;
            double edgeLen = Math.sqrt(edx * edx + edz * edz);
            if (edgeLen < 0.001) continue;

            Set<Long> visited = new HashSet<>();
            double step = 0.25;
            for (double d = 0; d <= edgeLen; d += step) {
                double t = d / edgeLen;
                double px = ex1 + edx * t, pz = ez1 + edz * t;
                int bx = (int) Math.floor(px), bz = (int) Math.floor(pz);
                long key = ((long) bx << 32) | (bz & 0xFFFFFFFFL);
                if (!visited.add(key)) continue;

                double ddx = bx + 0.5 - playerPos.x, ddz = bz + 0.5 - playerPos.z;
                if (ddx * ddx + ddz * ddz > renderDist * renderDist) continue;

                collectFaceIntersections(segments, world, ex1, ez1, edx, edz,
                        bx, bz, yMin, yMax, (float) ca.minY, (float) ca.maxY);
                collectHorizontalLines(segments, world, ex1, ez1, edx, edz,
                        bx, bz, yMin, yMax, (float) ca.minY, (float) ca.maxY);
            }
        }
        return segments;
    }

    /**
     * 收集边线与方块面的交点线段（北/南/东/西面）
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
     * 收集方块顶面/底面与边界墙交接处的水平线段
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
     * 收集方块面上的垂直线段（仅对实心方块）
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

    // ========== 临时顶点渲染 ==========

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
        // 虚线连接（批量）
        for (int i = 0; i < vertices.size() - 1; i++) {
            BlockPos v1 = vertices.get(i), v2 = vertices.get(i + 1);
            emitDashedLine(matrix, buffer, v1.getX() + 0.5f, v2.getX() + 0.5f,
                    v1.getZ() + 0.5f, v2.getZ() + 0.5f, py);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
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

    // ========== 耳切法三角剖分 ==========

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
