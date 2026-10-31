package dev.render4u.mixin;

import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventMouseButton;
import dev.render4u.core.event.impl.EventMouseScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow public abstract double xpos();
    @Shadow public abstract double ypos();

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void render4u$onButton(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            Minecraft mc = Minecraft.getInstance();
            float mx = (float) MouseHandler.getScaledXPos(mc.getWindow(), xpos());
            float my = (float) MouseHandler.getScaledYPos(mc.getWindow(), ypos());
            EventMouseButton event = new EventMouseButton(button.button(), action, mx, my);
            Render4U.getEventBus().post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void render4u$onScroll(long window, double scrollX, double scrollY, CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            EventMouseScroll event = new EventMouseScroll(scrollX, scrollY);
            Render4U.getEventBus().post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
