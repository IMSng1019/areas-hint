package areahint.render;

import areahint.config.ClientConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 标题和副字幕渲染辅助类。
 * <p>
 * 三套渲染路径（CPU/OpenGL/Vulkan）都要保持同样的字号、居中、换行和闪烁颜色规则，
 * 所以把纯计算和绘制细节集中到这里，避免以后只改了一套渲染器导致表现不一致。
 */
public final class TitleRenderHelper {
    private static final String[] SIZE_ORDER = {
        "extra_large", "large", "medium_large", "medium", "medium_small", "small", "extra_small"
    };

    private TitleRenderHelper() {
    }

    /**
     * 获取主标题缩放比例。
     */
    public static float getTitleScale() {
        return getScaleForSize(ClientConfig.getTitleSize());
    }

    /**
     * 获取副字幕缩放比例。
     * <p>
     * subtitleSize 为 auto 时，副字幕始终使用当前标题大小的下一级；当标题已经是最小值时，
     * 副字幕保持最小值，避免继续缩小到难以阅读。
     */
    public static float getSubtitleScale() {
        String subtitleSize = ClientConfig.getSubtitleSize();
        if ("auto".equals(subtitleSize)) {
            return getScaleForSize(getOneStepSmallerSize(ClientConfig.getTitleSize()));
        }
        return getScaleForSize(subtitleSize);
    }

    /**
     * 判断副字幕是否有可显示内容。
     */
    public static boolean hasSubtitle(String subtitle) {
        return normalizeSubtitleText(subtitle) != null;
    }

    /**
     * 统一副字幕换行写法。
     * <p>
     * 玩家按需求输入的 /n 会转换成真正换行；同时兼容 \n，便于 JSON 手动编辑。
     */
    public static String normalizeSubtitleText(String subtitle) {
        if (subtitle == null) {
            return null;
        }
        String normalized = subtitle.replace("/n", "\n").replace("\\n", "\n").trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 计算副字幕行。
     * <p>
     * 如果文本包含手动换行，就只按手动换行切分，不再自动换行；否则按屏幕宽度自动分行，
     * 并保持每一行独立居中。
     */
    public static List<String> buildSubtitleLines(String subtitle, TextRenderer textRenderer, int screenWidth, float subtitleScale) {
        List<String> lines = new ArrayList<>();
        String normalized = normalizeSubtitleText(subtitle);
        if (normalized == null) {
            return lines;
        }

        if (normalized.contains("\n")) {
            for (String line : normalized.split("\\R", -1)) {
                String cleanedLine = line.trim();
                if (!cleanedLine.isEmpty()) {
                    lines.add(cleanedLine);
                }
            }
            return lines;
        }

        int maxUnscaledWidth = Math.max(80, (int) (screenWidth * 0.70f / Math.max(subtitleScale, 0.1f)));
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            String candidate = currentLine.toString() + ch;

            if (currentLine.length() > 0 && textRenderer.getWidth(candidate) > maxUnscaledWidth) {
                lines.add(currentLine.toString().trim());
                currentLine.setLength(0);
            }

            currentLine.append(ch);
        }

        String tail = currentLine.toString().trim();
        if (!tail.isEmpty()) {
            lines.add(tail);
        }

        return lines;
    }

    /**
     * 副字幕第一行距离主标题顶部的像素偏移。
     */
    public static int getSubtitleStartY(TextRenderer textRenderer, float titleScale) {
        return Math.round(textRenderer.fontHeight * titleScale) + 4;
    }

    /**
     * 副字幕每一行之间的像素距离。
     */
    public static int getLineHeight(TextRenderer textRenderer, float scale) {
        return Math.max(1, Math.round((textRenderer.fontHeight + 2) * scale));
    }

    /**
     * 绘制一行居中文本。
     * @param drawBackground 是否绘制半透明背景，CPU 渲染模式沿用原有背景框效果
     */
    public static void drawCenteredLine(DrawContext drawContext, TextRenderer textRenderer, MatrixStack matrixStack,
                                        String line, String color, float alpha, float scale, int y, boolean drawBackground) {
        if (line == null || line.isEmpty()) {
            return;
        }

        matrixStack.push();
        matrixStack.translate(0, y, 0);
        matrixStack.scale(scale, scale, 1.0f);

        Text text = Text.of(line);
        int textWidth = textRenderer.getWidth(text);
        int finalX = -textWidth / 2;
        int finalY = 0;

        if (drawBackground) {
            int bgColor = getAlphaColor(0x000000, alpha * 0.5f);
            drawContext.fill(finalX - 4, finalY - 2, finalX + textWidth + 4, finalY + textRenderer.fontHeight + 2, bgColor);
        }

        drawTextWithColor(drawContext, textRenderer, text, line, finalX, finalY, color, alpha);
        matrixStack.pop();
    }

    private static void drawTextWithColor(DrawContext drawContext, TextRenderer textRenderer, Text text,
                                          String rawText, int x, int y, String color, float alpha) {
        long now = System.currentTimeMillis();
        String safeColor = color != null ? color : "#FFFFFF";

        if (FlashColorHelper.isFlashMode(safeColor)) {
            if (FlashColorHelper.isPerCharMode(safeColor)) {
                int xOff = x;
                for (int i = 0; i < rawText.length(); i++) {
                    String ch = String.valueOf(rawText.charAt(i));
                    int charRgb = FlashColorHelper.getCharColor(safeColor, now, i);
                    drawContext.drawTextWithShadow(textRenderer, Text.of(ch), xOff, y, getAlphaColor(charRgb, alpha));
                    xOff += textRenderer.getWidth(ch);
                }
            } else {
                int rgb = FlashColorHelper.getWholeColor(safeColor, now);
                drawContext.drawTextWithShadow(textRenderer, text, x, y, getAlphaColor(rgb, alpha));
            }
        } else {
            drawContext.drawTextWithShadow(textRenderer, text, x, y, getAlphaColor(parseHexColor(safeColor), alpha));
        }
    }

    private static String getOneStepSmallerSize(String size) {
        String normalizedSize = size != null ? size : "medium";
        for (int i = 0; i < SIZE_ORDER.length; i++) {
            if (SIZE_ORDER[i].equals(normalizedSize)) {
                return SIZE_ORDER[Math.min(i + 1, SIZE_ORDER.length - 1)];
            }
        }
        return "medium_small";
    }

    private static float getScaleForSize(String size) {
        switch (size) {
            case "extra_large":
                return 3.0f;
            case "large":
                return 2.5f;
            case "medium_large":
                return 2.0f;
            case "medium":
                return 1.5f;
            case "medium_small":
                return 1.2f;
            case "small":
                return 1.0f;
            case "extra_small":
                return 0.8f;
            default:
                return 1.5f;
        }
    }

    private static int parseHexColor(String hex) {
        try {
            if (hex != null && hex.startsWith("#") && hex.length() == 7) {
                return Integer.parseInt(hex.substring(1), 16);
            }
        } catch (Exception ignored) {
        }
        return 0xFFFFFF;
    }

    private static int getAlphaColor(int rgb, float alpha) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        return a | (rgb & 0x00FFFFFF);
    }
}
