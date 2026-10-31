package dev.render4u.render.world;

import dev.render4u.render.font.ttf.TTFFont;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EntityRenderUtil {

    private EntityRenderUtil() {
    }

    public static AABB entityBox(Entity entity, float partialTick) {
        Vec3 lerped = entity.getPosition(partialTick);
        return entity.getBoundingBox().move(lerped.x - entity.getX(), lerped.y - entity.getY(), lerped.z - entity.getZ());
    }

    public static void drawEntityBox2D(GuiGraphicsExtractor graphics, Entity entity, float partialTick, int color, float thickness) {
        AABB box = entityBox(entity, partialTick);
        Render3D.drawBox2D(graphics, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, thickness);
    }

    public static void drawEntityCornerBox2D(GuiGraphicsExtractor graphics, Entity entity, float partialTick, int color, float thickness, float cornerLength) {
        AABB box = entityBox(entity, partialTick);
        Render3D.drawCornerBox2D(graphics, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color, thickness, cornerLength);
    }

    public static void drawEntityTracer(GuiGraphicsExtractor graphics, Entity entity, float partialTick, int color, float thickness) {
        Vec3 center = entityBox(entity, partialTick).getCenter();
        Render3D.drawTracer(graphics, center.x, center.y, center.z, color, thickness);
    }

    public static void drawEntityNametag(GuiGraphicsExtractor graphics, Entity entity, float partialTick, int textColor, int bgColor) {
        AABB box = entityBox(entity, partialTick);
        Vec3 center = box.getCenter();
        Render3D.drawNametag(graphics, entity.getName().getString(), center.x, box.maxY + 0.3, center.z, textColor, bgColor);
    }

    public static void drawEntityNametag(GuiGraphicsExtractor graphics, TTFFont font, Entity entity, float partialTick, float size, int textColor, int bgColor) {
        AABB box = entityBox(entity, partialTick);
        Vec3 center = box.getCenter();
        Render3D.drawNametag(graphics, font, entity.getName().getString(), center.x, box.maxY + 0.3, center.z, size, textColor, bgColor);
    }

    public static void drawBlockBox3D(GuiGraphicsExtractor graphics, BlockPos pos, int color, float thickness) {
        Render3D.draw3DBox(graphics, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0, color, thickness);
    }
}
