package dev.render4u.feature.module.impl;

import dev.render4u.Render4U;
import dev.render4u.core.event.EventTarget;
import dev.render4u.core.event.impl.EventDisplay;
import dev.render4u.feature.module.Category;
import dev.render4u.feature.module.Module;
import dev.render4u.render.display.DisplayUtil;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.render.font.FontManager;
import dev.render4u.render.vulkan.VulkanHelper;
import net.minecraft.client.Minecraft;

public class DebugHudModule extends Module {
    public DebugHudModule() {
        super("Debug HUD", "Shows renderer statistics overlay", Category.MISC);
    }

    @EventTarget
    public void onDisplay(EventDisplay event) {
        var graphics = event.graphics();
        Minecraft minecraft = Minecraft.getInstance();

        String line1 = "Render4U " + Render4U.CLIENT_VERSION + " | " + VulkanHelper.getDeviceInfo();
        String line2 = "FPS: " + minecraft.getFps() + " | frame: " + String.format("%.2f", DisplayUtil.lastFrameMs()) + " ms";
        String line3 = "batches: " + DisplayBatcher.lastFrameBatches() + " | elements: " + DisplayBatcher.lastFrameElements();

        float textHeight = FontManager.ROBOTO.getFontHeight(11.0F);
        float width = Math.max(FontManager.ROBOTO.getStringWidth(line1, 11.0F),
                Math.max(FontManager.ROBOTO.getStringWidth(line2, 11.0F), FontManager.ROBOTO.getStringWidth(line3, 11.0F))) + 16.0F;
        float height = textHeight * 3.0F + 14.0F;

        RoundedRect.of(8.0F, 8.0F, width, height).radius(6.0F).color(0x9010141C).render(graphics);
        FontManager.ROBOTO.drawStringWithShadow(graphics, line1, 16.0F, 14.0F, 11.0F, 0xFFFFFFFF);
        FontManager.ROBOTO.drawStringWithShadow(graphics, line2, 16.0F, 14.0F + textHeight + 2.0F, 11.0F, 0xFF9FB4CC);
        FontManager.ROBOTO.drawStringWithShadow(graphics, line3, 16.0F, 14.0F + (textHeight + 2.0F) * 2.0F, 11.0F, 0xFF9FB4CC);
    }
}
