package dev.render4u.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.render4u.util.WindowIconHelper;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.InputStream;
import java.util.List;

@Mixin(Window.class)
public abstract class WindowMixin {

    @ModifyVariable(
            method = "setIcon(Ljava/util/List;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private List<IoSupplier<InputStream>> render4u$replaceWindowIcons(List<IoSupplier<InputStream>> original) {
        return WindowIconHelper.getCustomIconSuppliers();
    }
}
