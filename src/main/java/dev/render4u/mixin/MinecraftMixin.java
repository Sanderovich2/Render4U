package dev.render4u.mixin;

import dev.render4u.Constants;
import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventTick;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void render4u$onTick(CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            Render4U.getEventBus().post(new EventTick());
        }
    }

    @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
    private void render4u$createTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(Constants.getName() + " | " + Constants.getVersion());
    }
}
