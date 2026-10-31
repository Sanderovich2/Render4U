package dev.render4u.mixin;

import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void render4u$onTick(CallbackInfo ci) {
        if (Render4U.getEventBus() == null) {
            return;
        }

        ClientInputAccessor accessor = (ClientInputAccessor) this;
        Input keyPresses = accessor.render4u$getKeyPresses();
        Vec2 moveVector = accessor.render4u$getMoveVector();

        EventInput event = new EventInput(
            keyPresses.forward(),
            keyPresses.backward(),
            keyPresses.left(),
            keyPresses.right(),
            keyPresses.jump(),
            keyPresses.shift(),
            keyPresses.sprint(),
            moveVector.x,
            moveVector.y
        );

        Render4U.getEventBus().post(event);

        accessor.render4u$setKeyPresses(new Input(
            event.isForward(),
            event.isBackward(),
            event.isLeft(),
            event.isRight(),
            event.isJump(),
            event.isSneak(),
            event.isSprint()
        ));
        accessor.render4u$setMoveVector(new Vec2(event.getStrafeImpulse(), event.getForwardImpulse()));
    }
}
