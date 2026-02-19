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

/**
 * 边界可视化渲染器
 * 负责渲染域名的3D边界
 */
public class BoundVizRenderer {

    /**
     * 渲染所有域名边界
     */
    public static void render(MatrixStack matrices, float tickDelta) {
        BoundVizManager manager = BoundVizManager.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        boolean hasTempVertices = manager.shouldShowTemporaryVertices();
        if (!manager.isEnabled() && !hasTempVertices) return;

        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (manager.isEnabled()) {
            List<AreaData> areas = manager.getCurrentDimensionAreas();
            for (AreaData area : areas) {
                renderAreaBoundary(matrices, buffer, area, client);
            }
        }

        if (hasTempVertices) {
            renderTemporaryVertices(matrices, buffer, manager.getTemporaryVertices(), client);
        }

        matrices.pop();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /**
     * 渲染单个域名的边界
     */
    private static void renderAreaBoundary(MatrixStack matrices, BufferBuilder buffer, AreaData area, MinecraftClient client) {
        List<AreaData.Vertex> vertices = area.getVertices();
        if (vertices == null || vertices.size() < 3) {
            return;
        }

        // 获取高度范围
        AreaData.AltitudeData altitude = area.getAltitude();
        double minY = altitude != null && altitude.getMin() != null ? altitude.getMin() : -64;
        double maxY = altitude != null && altitude.getMax() != null ? altitude.getMax() : 320;

        // 解析颜色
        int[] rgb = ColorUtil.parseColor(area.getColor());
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 耳切法三角剖分（解决凹多边形TRIANGLE_FAN多层叠加问题）
        List<int[]> triangles = earClipTriangulate(vertices);

        // 渲染底面（20%透明度）
        if (!triangles.isEmpty()) {
            buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int[] tri : triangles) {
                for (int idx : tri) {
                    AreaData.Vertex v = vertices.get(idx);
                    buffer.vertex(matrix, (float) v.getX(), (float) minY, (float) v.getZ())
                            .color(r, g, b, 0.2f).next();
                }
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // 渲染顶面（20%透明度）
            buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int[] tri : triangles) {
                for (int idx : tri) {
                    AreaData.Vertex v = vertices.get(idx);
                    buffer.vertex(matrix, (float) v.getX(), (float) maxY, (float) v.getZ())
                            .color(r, g, b, 0.2f).next();
                }
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        // 渲染侧面（20%透明度）
        for (int i = 0; i < vertices.size(); i++) {
            AreaData.Vertex v1 = vertices.get(i);
            AreaData.Vertex v2 = vertices.get((i + 1) % vertices.size());

            buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, (float) v1.getX(), (float) minY, (float) v1.getZ())
                    .color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, (float) v1.getX(), (float) maxY, (float) v1.getZ())
                    .color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, (float) v2.getX(), (float) minY, (float) v2.getZ())
                    .color(r, g, b, 0.2f).next();
            buffer.vertex(matrix, (float) v2.getX(), (float) maxY, (float) v2.getZ())
                    .color(r, g, b, 0.2f).next();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        // 渲染边界线（80%透明度）
        // 底部边界线
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (AreaData.Vertex vertex : vertices) {
            buffer.vertex(matrix, (float) vertex.getX(), (float) minY, (float) vertex.getZ())
                    .color(r, g, b, 0.8f).next();
        }
        // 闭合线条
        AreaData.Vertex firstVertex = vertices.get(0);
        buffer.vertex(matrix, (float) firstVertex.getX(), (float) minY, (float) firstVertex.getZ())
                .color(r, g, b, 0.8f).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // 顶部边界线
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (AreaData.Vertex vertex : vertices) {
            buffer.vertex(matrix, (float) vertex.getX(), (float) maxY, (float) vertex.getZ())
                    .color(r, g, b, 0.8f).next();
        }
        // 闭合线条
        buffer.vertex(matrix, (float) firstVertex.getX(), (float) maxY, (float) firstVertex.getZ())
                .color(r, g, b, 0.8f).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // 垂直边界线
        for (AreaData.Vertex vertex : vertices) {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, (float) vertex.getX(), (float) minY, (float) vertex.getZ())
                    .color(r, g, b, 0.8f).next();
            buffer.vertex(matrix, (float) vertex.getX(), (float) maxY, (float) vertex.getZ())
                    .color(r, g, b, 0.8f).next();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        // 渲染侧面与方块交接处的边界线（80%透明度）
        renderSideWallBlockIntersections(matrix, buffer, vertices, minY, maxY, r, g, b, client);
    }

    /**
     * 渲染侧面与方块交接处的边界线
     * 当域名的侧面（垂直墙面）穿过实心方块时，在方块表面渲染边界线（80%透明度）
     */
    private static void renderSideWallBlockIntersections(Matrix4f matrix, BufferBuilder buffer,
            List<AreaData.Vertex> vertices, double minY, double maxY,
            float r, float g, float b, MinecraftClient client) {

        World world = client.world;
        if (world == null) return;

        Vec3d playerPos = client.player.getPos();
        int renderDist = 64;
        int yMin = (int) Math.floor(minY);
        int yMax = (int) Math.ceil(maxY) - 1;

        // 限制Y范围到合理值
        yMin = Math.max(yMin, (int) playerPos.y - renderDist);
        yMax = Math.min(yMax, (int) playerPos.y + renderDist);

        // 先收集所有线段端点，避免空buffer问题
        List<float[]> lineSegments = new ArrayList<>();

        for (int ei = 0; ei < vertices.size(); ei++) {
            AreaData.Vertex v1 = vertices.get(ei);
            AreaData.Vertex v2 = vertices.get((ei + 1) % vertices.size());

            double ex1 = v1.getX(), ez1 = v1.getZ();
            double ex2 = v2.getX(), ez2 = v2.getZ();
            double edx = ex2 - ex1, edz = ez2 - ez1;
            double edgeLen = Math.sqrt(edx * edx + edz * edz);
            if (edgeLen < 0.001) continue;

            Set<Long> visited = new HashSet<>();
            double step = 0.25;

            for (double d = 0; d <= edgeLen; d += step) {
                double t = d / edgeLen;
                double px = ex1 + edx * t;
                double pz = ez1 + edz * t;
                int bx = (int) Math.floor(px);
                int bz = (int) Math.floor(pz);

                long key = ((long) bx << 32) | (bz & 0xFFFFFFFFL);
                if (!visited.add(key)) continue;

                double ddx = bx + 0.5 - playerPos.x, ddz = bz + 0.5 - playerPos.z;
                if (ddx * ddx + ddz * ddz > renderDist * renderDist) continue;

                collectFaceIntersections(lineSegments, world,
                        ex1, ez1, edx, edz,
                        bx, bz, yMin, yMax, (float) minY, (float) maxY, r, g, b);

                // 方块顶面和底面与边界墙的交接线（水平线段）
                collectHorizontalLines(lineSegments, world,
                        ex1, ez1, edx, edz,
                        bx, bz, yMin, yMax, (float) minY, (float) maxY);
            }
        }

        if (!lineSegments.isEmpty()) {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (float[] seg : lineSegments) {
                buffer.vertex(matrix, seg[0], seg[1], seg[2]).color(r, g, b, 0.8f).next();
                buffer.vertex(matrix, seg[3], seg[4], seg[5]).color(r, g, b, 0.8f).next();
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

    private static final float FACE_OFFSET = 0.002f;

    /**
     * 收集边线与方块面的交点线段
     * 对每个面检查两侧方块，并加偏移避免z-fighting
     */
    private static void collectFaceIntersections(List<float[]> segments, World world,
            double ex1, double ez1, double edx, double edz,
            int bx, int bz, int yMin, int yMax, float fMinY, float fMaxY,
            float r, float g, float b) {

        // 北面 z=bz：检查(bx,bz)和(bx,bz-1)
        if (Math.abs(edz) > 0.001) {
            double t = (bz - ez1) / edz;
            if (t >= 0 && t <= 1) {
                double ix = ex1 + edx * t;
                if (ix >= bx && ix <= bx + 1) {
                    // 偏移向z-方向（北侧外）
                    collectVerticalLines(segments, world, (float) ix, (float) bz - FACE_OFFSET, bx, bz, yMin, yMax, fMinY, fMaxY);
                    // 偏移向z+方向（南侧外），检查北侧方块
                    collectVerticalLines(segments, world, (float) ix, (float) bz + FACE_OFFSET, bx, bz - 1, yMin, yMax, fMinY, fMaxY);
                }
            }
            // 南面 z=bz+1：检查(bx,bz)和(bx,bz+1)
            t = (bz + 1 - ez1) / edz;
            if (t >= 0 && t <= 1) {
                double ix = ex1 + edx * t;
                if (ix >= bx && ix <= bx + 1) {
                    collectVerticalLines(segments, world, (float) ix, (float) (bz + 1) + FACE_OFFSET, bx, bz, yMin, yMax, fMinY, fMaxY);
                    collectVerticalLines(segments, world, (float) ix, (float) (bz + 1) - FACE_OFFSET, bx, bz + 1, yMin, yMax, fMinY, fMaxY);
                }
            }
        }

        // 西面 x=bx：检查(bx,bz)和(bx-1,bz)
        if (Math.abs(edx) > 0.001) {
            double t = (bx - ex1) / edx;
            if (t >= 0 && t <= 1) {
                double iz = ez1 + edz * t;
                if (iz >= bz && iz <= bz + 1) {
                    collectVerticalLines(segments, world, (float) bx - FACE_OFFSET, (float) iz, bx, bz, yMin, yMax, fMinY, fMaxY);
                    collectVerticalLines(segments, world, (float) bx + FACE_OFFSET, (float) iz, bx - 1, bz, yMin, yMax, fMinY, fMaxY);
                }
            }
            // 东面 x=bx+1：检查(bx,bz)和(bx+1,bz)
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
     * 遍历所有Y层级，在实心方块的暴露面上画线
     */
    private static void collectHorizontalLines(List<float[]> segments, World world,
            double ex1, double ez1, double edx, double edz,
            int bx, int bz, int yMin, int yMax, float fMinY, float fMaxY) {

        // 计算边线在该方块XZ范围内的t区间
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

            // 顶面：上方方块非实心时，线往上偏（到空气中）
            pos.set(bx, by + 1, bz);
            if (top == by + 1 && !world.getBlockState(pos).isOpaqueFullCube(world, pos)) {
                segments.add(new float[]{x1, top + FACE_OFFSET, z1, x2, top + FACE_OFFSET, z2});
            }

            // 底面：下方方块非实心时，线往下偏（到空气中）
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
            BlockState state = world.getBlockState(pos);
            if (!state.isOpaqueFullCube(world, pos)) continue;

            float lineBottom = Math.max(by, fMinY);
            float lineTop = Math.min(by + 1, fMaxY);
            if (lineTop <= lineBottom) continue;

            segments.add(new float[]{fx, lineBottom, fz, fx, lineTop, fz});
        }
    }

    /**
     * 渲染临时顶点（白色虚线，100%不透明度）
     * 记录过程中实时显示，不闭合多边形
     */
    private static void renderTemporaryVertices(MatrixStack matrices, BufferBuilder buffer, List<BlockPos> vertices, MinecraftClient client) {
        if (vertices.isEmpty()) return;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        double playerY = client.player.getY();

        // 每个顶点渲染十字标记
        for (BlockPos pos : vertices) {
            float px = pos.getX() + 0.5f;
            float pz = pos.getZ() + 0.5f;
            float py = (float) playerY;
            float size = 0.4f;
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, px - size, py, pz).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, px + size, py, pz).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, px, py, pz - size).color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, px, py, pz + size).color(1f, 1f, 1f, 1f).next();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        // 顺序连线（不闭合），虚线效果
        for (int i = 0; i < vertices.size() - 1; i++) {
            BlockPos v1 = vertices.get(i);
            BlockPos v2 = vertices.get(i + 1);
            renderDashedLine(matrix, buffer, v1.getX() + 0.5f, v2.getX() + 0.5f,
                    v1.getZ() + 0.5f, v2.getZ() + 0.5f, (float) playerY);
        }
    }

    /**
     * 渲染一条白色虚线段
     */
    private static void renderDashedLine(Matrix4f matrix, BufferBuilder buffer,
                                          float x1, float x2, float z1, float z2, float y) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 0.01) return;

        double segLen = 0.5, gapLen = 0.3, total = segLen + gapLen;
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (double d = 0; d < distance; d += total) {
            double t1 = d / distance;
            double t2 = Math.min((d + segLen) / distance, 1.0);
            buffer.vertex(matrix, (float)(x1 + dx * t1), y, (float)(z1 + dz * t1))
                    .color(1f, 1f, 1f, 1f).next();
            buffer.vertex(matrix, (float)(x1 + dx * t2), y, (float)(z1 + dz * t2))
                    .color(1f, 1f, 1f, 1f).next();
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    /**
     * 耳切法三角剖分（支持凹多边形）
     */
    private static List<int[]> earClipTriangulate(List<AreaData.Vertex> polygon) {
        int n = polygon.size();
        List<int[]> triangles = new ArrayList<>();
        if (n < 3) return triangles;

        // 计算有向面积判断绕向
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
