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
        // 保持深度测试启用，使方块正确遮挡面，并避免顶面/底面叠加
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

        // 渲染底面（20%透明度）
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (AreaData.Vertex vertex : vertices) {
            buffer.vertex(matrix, (float) vertex.getX(), (float) minY, (float) vertex.getZ())
                    .color(r, g, b, 0.2f).next();
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // 渲染顶面（20%透明度）
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (AreaData.Vertex vertex : vertices) {
            buffer.vertex(matrix, (float) vertex.getX(), (float) maxY, (float) vertex.getZ())
                    .color(r, g, b, 0.2f).next();
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

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
     */
    private static void renderTemporaryVertices(MatrixStack matrices, BufferBuilder buffer, List<BlockPos> vertices, MinecraftClient client) {
        if (vertices.size() < 2) {
            return;
        }

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 获取玩家当前Y坐标作为渲染高度
        double playerY = client.player.getY();

        // 渲染白色虚线（通过间隔渲染实现虚线效果）
        for (int i = 0; i < vertices.size(); i++) {
            BlockPos v1 = vertices.get(i);
            BlockPos v2 = vertices.get((i + 1) % vertices.size());

            // 计算两点之间的距离
            double dx = v2.getX() - v1.getX();
            double dz = v2.getZ() - v1.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            // 虚线参数：每段长度0.5，间隔0.3
            double segmentLength = 0.5;
            double gapLength = 0.3;
            double totalSegment = segmentLength + gapLength;

            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            for (double d = 0; d < distance; d += totalSegment) {
                double t1 = d / distance;
                double t2 = Math.min((d + segmentLength) / distance, 1.0);

                float x1 = (float) (v1.getX() + dx * t1);
                float z1 = (float) (v1.getZ() + dz * t1);
                float x2 = (float) (v1.getX() + dx * t2);
                float z2 = (float) (v1.getZ() + dz * t2);

                buffer.vertex(matrix, x1, (float) playerY, z1)
                        .color(1.0f, 1.0f, 1.0f, 1.0f).next();
                buffer.vertex(matrix, x2, (float) playerY, z2)
                        .color(1.0f, 1.0f, 1.0f, 1.0f).next();
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }
}
