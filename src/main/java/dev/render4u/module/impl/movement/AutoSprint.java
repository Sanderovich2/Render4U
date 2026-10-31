package dev.render4u.module.impl.movement;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.core.event.EventTarget;
import dev.render4u.core.event.impl.EventTick;
import dev.render4u.module.Category;
import dev.render4u.module.Module;

public final class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", "Автоматический бег при движении вперед", Category.MOVEMENT, InputConstants.KEY_V);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        boolean canSprint = mc.options.keyUp.isDown()
                && !mc.player.isShiftKeyDown()
                && !mc.player.horizontalCollision
                && mc.player.getFoodData().getFoodLevel() > 6;

        if (canSprint) {
            mc.options.keySprint.setDown(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && mc.options.keySprint != null) {
            mc.options.keySprint.setDown(false);
        }
    }
}
