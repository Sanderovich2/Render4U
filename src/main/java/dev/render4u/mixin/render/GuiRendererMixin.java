package dev.render4u.mixin.render;

import dev.render4u.render.display.blur.Blur;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Inject(method = "draw", at = @At("HEAD"))
    private void render4u$processBlur(CallbackInfo ci) {
        Blur.process();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void render4u$closeBlur(CallbackInfo ci) {
        Blur.close();
    }
}

