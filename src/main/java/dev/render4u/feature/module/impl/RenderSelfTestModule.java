package dev.render4u.feature.module.impl;

import dev.render4u.core.event.impl.EventDisplay;
import dev.render4u.core.event.impl.EventRender3D;
import dev.render4u.core.event.EventTarget;
import dev.render4u.feature.module.Category;
import dev.render4u.feature.module.Module;
import dev.render4u.render.display.blur.Blur;
import dev.render4u.render.display.shape.Circle;
import dev.render4u.render.display.shape.Image;
import dev.render4u.render.display.shape.Line;
import dev.render4u.render.display.shape.Outline;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.render.display.shape.Shadow;
import dev.render4u.render.font.FontManager;
import dev.render4u.render.world.Render3D;
import dev.render4u.util.color.ColorUtil;
import dev.render4u.util.color.Gradient;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderSelfTestModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4U/SelfTest");

    private boolean logged2D;
    private boolean logged3D;
    private BufferedImage demoImage;

    public RenderSelfTestModule() {
        super("Render Self Test", "Draws a demo overlay and logs once every renderer works", Category.MISC);
    }

    @Override
    protected void onEnable() {
        logged2D = false;
        logged3D = false;
    }

    @EventTarget
    public void onDisplay(EventDisplay event) {
        var graphics = event.graphics();
        float x = 16.0F;
        float y = 16.0F;
        float w = 240.0F;
        float h = 96.0F;

        Shadow.of(x, y, w, h).radius(8).blur(10).color(0x90000000).render(graphics);
        Blur.of(x, y, w, h).radius(8).strength(4).render(graphics);
        RoundedRect.of(x, y, w, h).radius(8).color(ColorUtil.rgba(16, 20, 28, 190)).render(graphics);
        Outline.of(x, y, w, h).radius(8).thickness(1.0F)
                .horizontalGradient(ColorUtil.hex("#5B86E5"), ColorUtil.hex("#36D1DC"))
                .render(graphics);

        FontManager.ROBOTO.drawStringWithShadow(graphics, "Render4U self-check", x + 12, y + 12, 14.0F, 0xFFFFFFFF);
        FontManager.ROBOTO.drawString(graphics, "Проверка рендера и кириллицы", x + 12, y + 34, 12.0F, 0xFF9FB4CC);

        Circle.of(x + 26.0F, y + 64.0F, 8.0F).color(0xFF5B86E5).render(graphics);
        Circle.of(x + 52.0F, y + 64.0F, 8.0F).innerRadius(4.0F).color(0xFF36D1DC).render(graphics);
        Circle.of(x + 78.0F, y + 64.0F, 8.0F).innerRadius(3.0F).angle(0.0F, 270.0F).color(0xFFFFB74D).render(graphics);
        Line.of(x + 96.0F, y + 70.0F, x + 136.0F, y + 58.0F).thickness(2.0F).gradient(0xFF5B86E5, 0xFF36D1DC).render(graphics);
        Image.of("textures/block/stone.png").bounds(x + 142.0F, y + 56.0F, 16.0F, 16.0F).render(graphics);
        Image.of(customImage(), "selftest_custom").bounds(x + 164.0F, y + 56.0F, 16.0F, 16.0F).radius(4.0F).render(graphics);

        Gradient gradient = Gradient.of(ColorUtil.hex("#5B86E5"), ColorUtil.hex("#36D1DC"));
        RoundedRect.of(x + 12, y + h - 16, w - 24, 4).radius(2).gradient(gradient).render(graphics);

        if (!logged2D) {
            logged2D = true;
            LOGGER.info("Self-test: 2D OK (shadow, blur, rounded rect, outline, circle, line, image, custom image, gradient, text, cyrillic)");
        }
    }

    private BufferedImage customImage() {
        if (demoImage == null) {
            demoImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            for (int py = 0; py < 32; py++) {
                for (int px = 0; px < 32; px++) {
                    float dx = (px - 15.5F) / 15.5F;
                    float dy = (py - 15.5F) / 15.5F;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float t = Math.min(dist, 1.0F);
                    int r = (int) (91 + (54 - 91) * t);
                    int g = (int) (134 + (209 - 134) * t);
                    int b = (int) (229 + (220 - 229) * t);
                    int a = dist <= 1.0F ? 255 : 0;
                    demoImage.setRGB(px, py, a << 24 | r << 16 | g << 8 | b);
                }
            }
        }
        return demoImage;
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        Vec3 target = event.cameraPos().add(minecraft.player.getLookAngle().scale(6.0));
        AABB box = new AABB(target, target).inflate(0.5);
        int color = 0xFF5B86E5;

        Render3D.drawCornerBox2D(event.graphics(), box, color, 1.5F, 8.0F);
        Render3D.drawTracer(event.graphics(), target.x, target.y, target.z, color, 1.5F);
        Render3D.drawDistanceTag(event.graphics(), "self-test", target.x, target.y + 0.6, target.z, 0x9010141C);

        Player nearest = null;
        double best = Double.MAX_VALUE;
        for (Player player : minecraft.level.players()) {
            if (player == minecraft.player || !player.isAlive()) {
                continue;
            }
            double dist = player.distanceToSqr(minecraft.player);
            if (dist < best) {
                best = dist;
                nearest = player;
            }
        }
        if (nearest != null) {
            float partialTick = event.partialTick();
            Render3D.drawEntityCornerBox2D(event.graphics(), nearest, partialTick, color, 1.5F, 8.0F);
            Render3D.drawEntityNametag(event.graphics(), nearest, partialTick, 0xFFFFFFFF, 0x9010141C);
        }

        if (!logged3D) {
            logged3D = true;
            LOGGER.info("Self-test: 3D OK (corner box, tracer, distance tag, entity helpers)");
        }
    }
}
