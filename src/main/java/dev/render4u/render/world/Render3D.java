package dev.render4u.render.world;

import dev.render4u.render.display.DisplayUtil;
import dev.render4u.render.display.rounded.RoundedRect;
import dev.render4u.render.font.FontManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class Render3D {

    public record ScreenBox(float minX, float minY, float maxX, float maxY) {
        public float width() {
            return maxX - minX;
        }

        public float height() {
            return maxY - minY;
        }
    }

    private Render3D() {
    }

    public static @Nullable Vector2f project(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) {
            return null;
        }

        Camera camera = mc.gameRenderer.mainCamera();
        if (camera == null) {
            return null;
        }

        Vec3 camPos = camera.position();
        Matrix4f mat = camera.getViewRotationProjectionMatrix(new Matrix4f());

        Vector4f v = new Vector4f(
                (float) (x - camPos.x),
                (float) (y - camPos.y),
                (float) (z - camPos.z),
                1.0F
        );
        mat.transform(v);

        if (v.w <= 0.0F) {
            return null;
        }

        float ndcX = v.x / v.w;
        float ndcY = v.y / v.w;
        float ndcZ = v.z / v.w;

        if (ndcZ < -1.0F || ndcZ > 1.0F) {
            return null;
        }

        float screenX = (ndcX * 0.5F + 0.5F) * DisplayUtil.width();
        float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * DisplayUtil.height();

        return new Vector2f(screenX, screenY);
    }

    public static @Nullable Vector2f project(Vec3 pos) {
        return project(pos.x, pos.y, pos.z);
    }

    public static @Nullable ScreenBox projectBox(AABB box) {
        double[][] corners = {
                {box.minX, box.minY, box.minZ},
                {box.maxX, box.minY, box.minZ},
                {box.maxX, box.minY, box.maxZ},
                {box.minX, box.minY, box.maxZ},
                {box.minX, box.maxY, box.minZ},
                {box.maxX, box.maxY, box.minZ},
                {box.maxX, box.maxY, box.maxZ},
                {box.minX, box.maxY, box.maxZ}
        };

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        boolean anyVisible = false;

        for (double[] corner : corners) {
            Vector2f proj = project(corner[0], corner[1], corner[2]);
            if (proj != null) {
                anyVisible = true;
                if (proj.x < minX) minX = proj.x;
                if (proj.y < minY) minY = proj.y;
                if (proj.x > maxX) maxX = proj.x;
                if (proj.y > maxY) maxY = proj.y;
            }
        }

        if (!anyVisible) {
            return null;
        }

        return new ScreenBox(minX, minY, maxX, maxY);
    }

    public static void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, int color, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.001F) {
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x1, y1);
        float angle = (float) Math.atan2(dy, dx);
        graphics.pose().rotate(angle);
        RoundedRect.of(0.0F, -thickness * 0.5F, len, thickness)
                .radius(thickness * 0.5F)
                .color(color)
                .render(graphics);
        graphics.pose().popMatrix();
    }

    public static void drawTracer(GuiGraphicsExtractor graphics, double targetX, double targetY, double targetZ, int color, float thickness) {
        Vector2f target = project(targetX, targetY, targetZ);
        if (target == null) {
            return;
        }

        float originX = DisplayUtil.width() * 0.5F;
        float originY = DisplayUtil.height() * 0.5F;
        drawLine(graphics, originX, originY, target.x, target.y, color, thickness);
    }

    public static void drawTracer(GuiGraphicsExtractor graphics, Vec3 target, int color, float thickness) {
        drawTracer(graphics, target.x, target.y, target.z, color, thickness);
    }

    public static void drawBox2D(GuiGraphicsExtractor graphics, AABB box, int color, float thickness) {
        ScreenBox screen = projectBox(box);
        if (screen == null) {
            return;
        }

        drawLine(graphics, screen.minX, screen.minY, screen.maxX, screen.minY, color, thickness);
        drawLine(graphics, screen.maxX, screen.minY, screen.maxX, screen.maxY, color, thickness);
        drawLine(graphics, screen.maxX, screen.maxY, screen.minX, screen.maxY, color, thickness);
        drawLine(graphics, screen.minX, screen.maxY, screen.minX, screen.minY, color, thickness);
    }

    public static void drawCornerBox2D(GuiGraphicsExtractor graphics, AABB box, int color, float thickness, float cornerLength) {
        ScreenBox screen = projectBox(box);
        if (screen == null) {
            return;
        }

        float x1 = screen.minX;
        float y1 = screen.minY;
        float x2 = screen.maxX;
        float y2 = screen.maxY;
        float w = screen.width();
        float h = screen.height();
        float len = Math.min(cornerLength, Math.min(w * 0.5F, h * 0.5F));

        drawLine(graphics, x1, y1, x1 + len, y1, color, thickness);
        drawLine(graphics, x1, y1, x1, y1 + len, color, thickness);

        drawLine(graphics, x2, y1, x2 - len, y1, color, thickness);
        drawLine(graphics, x2, y1, x2, y1 + len, color, thickness);

        drawLine(graphics, x1, y2, x1 + len, y2, color, thickness);
        drawLine(graphics, x1, y2, x1, y2 - len, color, thickness);

        drawLine(graphics, x2, y2, x2 - len, y2, color, thickness);
        drawLine(graphics, x2, y2, x2, y2 - len, color, thickness);
    }

    public static void drawBox3D(GuiGraphicsExtractor graphics, AABB box, int color, float thickness) {
        Vector2f c0 = project(box.minX, box.minY, box.minZ);
        Vector2f c1 = project(box.maxX, box.minY, box.minZ);
        Vector2f c2 = project(box.maxX, box.minY, box.maxZ);
        Vector2f c3 = project(box.minX, box.minY, box.maxZ);
        Vector2f c4 = project(box.minX, box.maxY, box.minZ);
        Vector2f c5 = project(box.maxX, box.maxY, box.minZ);
        Vector2f c6 = project(box.maxX, box.maxY, box.maxZ);
        Vector2f c7 = project(box.minX, box.maxY, box.maxZ);

        if (c0 != null && c1 != null) drawLine(graphics, c0.x, c0.y, c1.x, c1.y, color, thickness);
        if (c1 != null && c2 != null) drawLine(graphics, c1.x, c1.y, c2.x, c2.y, color, thickness);
        if (c2 != null && c3 != null) drawLine(graphics, c2.x, c2.y, c3.x, c3.y, color, thickness);
        if (c3 != null && c0 != null) drawLine(graphics, c3.x, c3.y, c0.x, c0.y, color, thickness);

        if (c4 != null && c5 != null) drawLine(graphics, c4.x, c4.y, c5.x, c5.y, color, thickness);
        if (c5 != null && c6 != null) drawLine(graphics, c5.x, c5.y, c6.x, c6.y, color, thickness);
        if (c6 != null && c7 != null) drawLine(graphics, c6.x, c6.y, c7.x, c7.y, color, thickness);
        if (c7 != null && c4 != null) drawLine(graphics, c7.x, c7.y, c4.x, c4.y, color, thickness);

        if (c0 != null && c4 != null) drawLine(graphics, c0.x, c0.y, c4.x, c4.y, color, thickness);
        if (c1 != null && c5 != null) drawLine(graphics, c1.x, c1.y, c5.x, c5.y, color, thickness);
        if (c2 != null && c6 != null) drawLine(graphics, c2.x, c2.y, c6.x, c6.y, color, thickness);
        if (c3 != null && c7 != null) drawLine(graphics, c3.x, c3.y, c7.x, c7.y, color, thickness);
    }

    public static void drawBlockBox3D(GuiGraphicsExtractor graphics, BlockPos pos, int color, float thickness) {
        drawBox3D(graphics, new AABB(pos), color, thickness);
    }

    public static void drawNametag(GuiGraphicsExtractor graphics, String text, double x, double y, double z, int textColor, int bgColor) {
        Vector2f pos = project(x, y, z);
        if (pos == null) {
            return;
        }

        float textWidth = FontManager.ROBOTO_12.getStringWidth(text);
        float textHeight = FontManager.ROBOTO_12.getFontHeight();
        float padX = 6.0F;
        float padY = 3.0F;
        float boxW = textWidth + padX * 2.0F;
        float boxH = textHeight + padY * 2.0F;

        float boxX = pos.x - boxW * 0.5F;
        float boxY = pos.y - boxH;

        RoundedRect.of(boxX, boxY, boxW, boxH)
                .radius(4.0F)
                .color(bgColor)
                .render(graphics);

        FontManager.ROBOTO_12.drawString(graphics, text, boxX + padX, boxY + padY, textColor);
    }

    public static void drawDistanceTag(GuiGraphicsExtractor graphics, String label, double x, double y, double z, int color) {
        Vec3 cam = getCameraPos();
        double dist = cam.distanceTo(new Vec3(x, y, z));
        String formatted = String.format("%s [%.1fm]", label, dist);
        drawNametag(graphics, formatted, x, y, z, 0xFFFFFFFF, color);
    }

    public static Vec3 getCameraPos() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null) {
            Camera cam = mc.gameRenderer.mainCamera();
            if (cam != null) {
                return cam.position();
            }
        }
        return Vec3.ZERO;
    }
}
