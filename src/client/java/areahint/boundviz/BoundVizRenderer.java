package areahint.boundviz;

import areahint.data.AreaData;
import areahint.util.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

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
