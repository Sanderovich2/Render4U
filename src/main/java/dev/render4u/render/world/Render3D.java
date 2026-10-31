package dev.render4u.render.world;

import dev.render4u.render.display.DisplayUtil;
import dev.render4u.render.display.element.DrawElement;
import dev.render4u.render.display.element.DrawRect;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.render.font.ttf.TTFFont;
import dev.render4u.render.font.ttf.TTFFontManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
    private static final Vector4f CLIP = new Vector4f();
    private static final Vector2f SCRATCH_A = new Vector2f();
    private static final Vector2f SCRATCH_B = new Vector2f();

    private static final int[][] BOX_EDGES = {
            {0, 1}, {1, 3}, {3, 2}, {2, 0},
            {4, 5}, {5, 7}, {7, 6}, {6, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };
    private static final Vector4f[] CLIP_CORNERS = new Vector4f[8];
    static {
        for (int i = 0; i < 8; i++) {
            CLIP_CORNERS[i] = new Vector4f();
        }
    }

    private static Vec3 cameraPos = Vec3.ZERO;
    private static boolean frameCacheValid;

    private Render3D() {
    }

    public static void beginFrame(Minecraft minecraft) {
        if (prepare(minecraft)) {
            frameCacheValid = true;
        }
    }

    public static void endFrame() {
        frameCacheValid = false;
    }

    public static Vec3 getCameraPos() {
        if (!frameCacheValid) {
            prepare(Minecraft.getInstance());
        }
        return cameraPos;
    }

    public static @Nullable Vector2f project(double x, double y, double z) {
        if (!prepare(Minecraft.getInstance())) {
            return null;
        }
        Vector2f dest = new Vector2f();
        return projectInto(x, y, z, dest) ? dest : null;
    }

    public static @Nullable Vector2f project(Vec3 pos) {
        return project(pos.x, pos.y, pos.z);
    }

    public static @Nullable ScreenBox projectBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!prepare(Minecraft.getInstance())) {
            return null;
        }

        float nearW = 0.05f;
        boolean anyInFront = false;

        for (int i = 0; i < 8; i++) {
            double cx = (i & 1) == 0 ? minX : maxX;
            double cy = (i & 4) == 0 ? minY : maxY;
            double cz = (i & 2) == 0 ? minZ : maxZ;

            Vector4f c = CLIP_CORNERS[i];
            c.set((float) (cx - cameraPos.x), (float) (cy - cameraPos.y), (float) (cz - cameraPos.z), 1.0f);
            VIEW_PROJECTION.transform(c);
            if (c.w > nearW) {
                anyInFront = true;
            }
        }

        if (!anyInFront) {
            return null;
        }

        float sMinX = Float.MAX_VALUE;
        float sMinY = Float.MAX_VALUE;
        float sMaxX = -Float.MAX_VALUE;
        float sMaxY = -Float.MAX_VALUE;
        boolean anyPoint = false;

        float dispW = DisplayUtil.width();
        float dispH = DisplayUtil.height();

        for (int i = 0; i < 8; i++) {
            Vector4f c = CLIP_CORNERS[i];
            if (c.w > nearW) {
                float invW = 1.0f / c.w;
                float sx = (c.x * invW * 0.5f + 0.5f) * dispW;
                float sy = (1.0f - (c.y * invW * 0.5f + 0.5f)) * dispH;
                sMinX = Math.min(sMinX, sx);
                sMinY = Math.min(sMinY, sy);
                sMaxX = Math.max(sMaxX, sx);
                sMaxY = Math.max(sMaxY, sy);
                anyPoint = true;
            }
        }

        for (int[] edge : BOX_EDGES) {
            Vector4f a = CLIP_CORNERS[edge[0]];
            Vector4f b = CLIP_CORNERS[edge[1]];

            boolean aIn = a.w > nearW;
            boolean bIn = b.w > nearW;

            if (aIn ^ bIn) {
                float t = (nearW - a.w) / (b.w - a.w);
                float ix = a.x + t * (b.x - a.x);
                float iy = a.y + t * (b.y - a.y);
                float invW = 1.0f / nearW;
                float sx = (ix * invW * 0.5f + 0.5f) * dispW;
                float sy = (1.0f - (iy * invW * 0.5f + 0.5f)) * dispH;

                sMinX = Math.min(sMinX, sx);
                sMinY = Math.min(sMinY, sy);
                sMaxX = Math.max(sMaxX, sx);
                sMaxY = Math.max(sMaxY, sy);
                anyPoint = true;
            }
        }

        return anyPoint ? new ScreenBox(sMinX, sMinY, sMaxX, sMaxY) : null;
    }

    public static @Nullable ScreenBox projectBox(AABB box) {
        return projectBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
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

    public static void draw2DLine(GuiGraphicsExtractor graphics, double pos3D_X, double pos3D_Y, double pos3D_Z, float screen2D_X, float screen2D_Y, int color, float thickness) {
        Vector2f target = project(pos3D_X, pos3D_Y, pos3D_Z);
        if (target == null) {
            return;
        }
        drawLine(graphics, screen2D_X, screen2D_Y, target.x, target.y, color, thickness);
    }

    public static void draw2DLine(GuiGraphicsExtractor graphics, float pos3D_X, float pos3D_Y, float pos3D_Z, float screen2D_X, float screen2D_Y, int color, float thickness) {
        draw2DLine(graphics, (double) pos3D_X, (double) pos3D_Y, (double) pos3D_Z, screen2D_X, screen2D_Y, color, thickness);
    }

    public static void draw3DLine(GuiGraphicsExtractor graphics, double x1, double y1, double z1, double x2, double y2, double z2, int color, float thickness) {
        Vector2f p1 = project(x1, y1, z1);
        Vector2f p2 = project(x2, y2, z2);
        if (p1 == null || p2 == null) {
            return;
        }
        drawLine(graphics, p1.x, p1.y, p2.x, p2.y, color, thickness);
    }

    public static void draw3DLine(GuiGraphicsExtractor graphics, float x1, float y1, float z1, float x2, float y2, float z2, int color, float thickness) {
        draw3DLine(graphics, (double) x1, (double) y1, (double) z1, (double) x2, (double) y2, (double) z2, color, thickness);
    }

    public static void draw2DElement(GuiGraphicsExtractor graphics, double pos3D_X, double pos3D_Y, double pos3D_Z, DrawElement element, float scale2D_X, float scale2D_Y) {
        if (element == null) {
            return;
        }
        Vector2f screen = project(pos3D_X, pos3D_Y, pos3D_Z);
        if (screen == null) {
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(screen.x, screen.y);
        if (scale2D_X != 1.0F || scale2D_Y != 1.0F) {
            graphics.pose().scale(scale2D_X, scale2D_Y);
        }
        if (element instanceof DrawRect rect) {
            rect.render(graphics, -rect.width * 0.5F, -rect.height * 0.5F);
        } else {
            element.render(graphics, 0.0F, 0.0F);
        }
        graphics.pose().popMatrix();
    }

    public static void draw2DElement(GuiGraphicsExtractor graphics, float pos3D_X, float pos3D_Y, float pos3D_Z, DrawElement element, float scale2D_X, float scale2D_Y) {
        draw2DElement(graphics, (double) pos3D_X, (double) pos3D_Y, (double) pos3D_Z, element, scale2D_X, scale2D_Y);
    }

    public static void draw2DElement(GuiGraphicsExtractor graphics, double pos3D_X, double pos3D_Y, double pos3D_Z, DrawElement element) {
        draw2DElement(graphics, pos3D_X, pos3D_Y, pos3D_Z, element, 1.0F, 1.0F);
    }

    public static void draw3DBox(GuiGraphicsExtractor graphics, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float thickness) {
        Vector2f[] points = new Vector2f[8];
        for (int i = 0; i < 8; i++) {
            double cx = (i & 1) == 0 ? minX : maxX;
            double cy = (i & 4) == 0 ? minY : maxY;
            double cz = (i & 2) == 0 ? minZ : maxZ;
            points[i] = project(cx, cy, cz);
        }

        for (int i = 0; i < 4; i++) {
            int next = (i + 1) & 3;
            drawProjectedEdge(graphics, points[i], points[next], color, thickness);
            drawProjectedEdge(graphics, points[i + 4], points[next + 4], color, thickness);
            drawProjectedEdge(graphics, points[i], points[i + 4], color, thickness);
        }
    }

    public static void draw3DBox(GuiGraphicsExtractor graphics, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, float thickness) {
        draw3DBox(graphics, (double) minX, (double) minY, (double) minZ, (double) maxX, (double) maxY, (double) maxZ, color, thickness);
    }

    public static void draw3DBoxBySize(GuiGraphicsExtractor graphics, double x, double y, double z, double sizeX, double sizeY, double sizeZ, int color, float thickness) {
        draw3DBox(graphics, x, y, z, x + sizeX, y + sizeY, z + sizeZ, color, thickness);
    }

    public static void draw3DBox(GuiGraphicsExtractor graphics, Matrix4f transform, int color, float thickness) {
        Vector2f[] points = new Vector2f[8];
        Vector4f v = new Vector4f();
        for (int i = 0; i < 8; i++) {
            float x = (i & 1) == 0 ? -0.5F : 0.5F;
            float y = (i & 4) == 0 ? -0.5F : 0.5F;
            float z = (i & 2) == 0 ? -0.5F : 0.5F;
            v.set(x, y, z, 1.0F);
            transform.transform(v);
            points[i] = project(v.x, v.y, v.z);
        }
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) & 3;
            drawProjectedEdge(graphics, points[i], points[next], color, thickness);
            drawProjectedEdge(graphics, points[i + 4], points[next + 4], color, thickness);
            drawProjectedEdge(graphics, points[i], points[i + 4], color, thickness);
        }
    }

    private static void drawProjectedEdge(GuiGraphicsExtractor graphics, @Nullable Vector2f p1, @Nullable Vector2f p2, int color, float thickness) {
        if (p1 != null && p2 != null) {
            drawLine(graphics, p1.x, p1.y, p2.x, p2.y, color, thickness);
        }
    }

    public static void drawBox2D(GuiGraphicsExtractor graphics, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float thickness) {
        ScreenBox screen = projectBox(minX, minY, minZ, maxX, maxY, maxZ);
        if (screen == null) {
            return;
        }

        drawLine(graphics, screen.minX, screen.minY, screen.maxX, screen.minY, color, thickness);
        drawLine(graphics, screen.maxX, screen.minY, screen.maxX, screen.maxY, color, thickness);
        drawLine(graphics, screen.maxX, screen.maxY, screen.minX, screen.maxY, color, thickness);
        drawLine(graphics, screen.minX, screen.maxY, screen.minX, screen.minY, color, thickness);
    }

    public static void drawCornerBox2D(GuiGraphicsExtractor graphics, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float thickness, float cornerLength) {
        ScreenBox screen = projectBox(minX, minY, minZ, maxX, maxY, maxZ);
        if (screen == null) {
            return;
        }

        float x1 = screen.minX;
        float y1 = screen.minY;
        float x2 = screen.maxX;
        float y2 = screen.maxY;
        float len = Math.min(cornerLength, Math.min(screen.width() * 0.5F, screen.height() * 0.5F));

        drawLine(graphics, x1, y1, x1 + len, y1, color, thickness);
        drawLine(graphics, x1, y1, x1, y1 + len, color, thickness);

        drawLine(graphics, x2, y1, x2 - len, y1, color, thickness);
        drawLine(graphics, x2, y1, x2, y1 + len, color, thickness);

        drawLine(graphics, x1, y2, x1 + len, y2, color, thickness);
        drawLine(graphics, x1, y2, x1, y2 - len, color, thickness);

        drawLine(graphics, x2, y2, x2 - len, y2, color, thickness);
        drawLine(graphics, x2, y2, x2, y2 - len, color, thickness);
    }

    public static void drawTracer(GuiGraphicsExtractor graphics, double targetX, double targetY, double targetZ, int color, float thickness) {
        float originX = DisplayUtil.width() * 0.5F;
        float originY = DisplayUtil.height() * 0.5F;
        draw2DLine(graphics, targetX, targetY, targetZ, originX, originY, color, thickness);
    }

    public static void drawTracer(GuiGraphicsExtractor graphics, Vec3 target, int color, float thickness) {
        drawTracer(graphics, target.x, target.y, target.z, color, thickness);
    }

    public static void drawTracer(GuiGraphicsExtractor graphics, double targetX, double targetY, double targetZ, float originScreenX, float originScreenY, int color, float thickness) {
        draw2DLine(graphics, targetX, targetY, targetZ, originScreenX, originScreenY, color, thickness);
    }

    public static void drawBox2D(GuiGraphicsExtractor graphics, AABB box, int color, float thickness) {
        drawBox2D(graphics, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, thickness);
    }

    public static void drawCornerBox2D(GuiGraphicsExtractor graphics, AABB box, int color, float thickness, float cornerLength) {
        drawCornerBox2D(graphics, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, thickness, cornerLength);
    }

    public static void drawBox3D(GuiGraphicsExtractor graphics, AABB box, int color, float thickness) {
        draw3DBox(graphics, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, thickness);
    }

    public static void drawNametag(GuiGraphicsExtractor graphics, TTFFont font, String text, double x, double y, double z, float size, int textColor, int bgColor) {
        Vector2f pos = project(x, y, z);
        if (pos == null) {
            return;
        }

        TTFFont f = font != null ? font : TTFFontManager.ROBOTO;
        float textWidth = f.getStringWidth(text, size);
        float textHeight = f.getFontHeight(size);
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

        f.drawString(graphics, text, boxX + padX, boxY + padY, size, textColor);
    }

    public static void drawNametag(GuiGraphicsExtractor graphics, String text, double x, double y, double z, int textColor, int bgColor) {
        drawNametag(graphics, null, text, x, y, z, 12.0F, textColor, bgColor);
    }

    public static void drawDistanceTag(GuiGraphicsExtractor graphics, String label, double x, double y, double z, int color) {
        double dist = getCameraPos().distanceTo(new Vec3(x, y, z));
        String formatted = String.format("%s [%.1fm]", label, dist);
        drawNametag(graphics, formatted, x, y, z, 0xFFFFFFFF, color);
    }

    private static boolean prepare(Minecraft minecraft) {
        if (frameCacheValid) {
            return true;
        }
        if (minecraft == null || minecraft.gameRenderer == null) {
            return false;
        }
        Camera camera = minecraft.gameRenderer.mainCamera();
        if (camera == null) {
            return false;
        }
        Matrix4f result = camera.getViewRotationProjectionMatrix(VIEW_PROJECTION);
        if (result != VIEW_PROJECTION) {
            VIEW_PROJECTION.set(result);
        }
        cameraPos = camera.position();
        return true;
    }

    private static boolean projectInto(double x, double y, double z, Vector2f dest) {
        CLIP.set((float) (x - cameraPos.x), (float) (y - cameraPos.y), (float) (z - cameraPos.z), 1.0F);
        VIEW_PROJECTION.transform(CLIP);

        float w = CLIP.w;
        if (w <= 0.0F) {
            return false;
        }
        float invW = 1.0F / w;
        float ndcZ = CLIP.z * invW;
        if (ndcZ < -1.0F || ndcZ > 1.0F) {
            return false;
        }

        dest.set((CLIP.x * invW * 0.5F + 0.5F) * DisplayUtil.width(),
                (1.0F - (CLIP.y * invW * 0.5F + 0.5F)) * DisplayUtil.height());
        return true;
    }
}
