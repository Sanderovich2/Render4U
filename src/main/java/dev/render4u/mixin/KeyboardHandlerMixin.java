package dev.render4u.mixin;

import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventChar;
import dev.render4u.core.event.impl.EventKey;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void render4u$onKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            EventKey keyEvent = new EventKey(event.key(), event.keycode(), action, event.modifiers());
            Render4U.getEventBus().post(keyEvent);
            if (keyEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void render4u$onCharTyped(long window, CharacterEvent event, CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            EventChar charEvent = new EventChar(event.codepointAsString());
            Render4U.getEventBus().post(charEvent);
            if (charEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
