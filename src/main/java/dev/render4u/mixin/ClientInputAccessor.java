package dev.render4u.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientInput.class)
public interface ClientInputAccessor {
    @Accessor("keyPresses")
    Input render4u$getKeyPresses();

    @Accessor("keyPresses")
    void render4u$setKeyPresses(Input input);

    @Accessor("moveVector")
    Vec2 render4u$getMoveVector();

    @Accessor("moveVector")
    void render4u$setMoveVector(Vec2 moveVector);
}
