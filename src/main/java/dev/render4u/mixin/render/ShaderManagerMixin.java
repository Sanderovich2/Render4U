package dev.render4u.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.render4u.render.vulkan.VulkanHelper;
import net.minecraft.client.renderer.ShaderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderManager.class)
public abstract class ShaderManagerMixin {
    @WrapOperation(
        method = "apply",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/PipelineCache;clear()V")
    )
    private void render4u$submitBeforePipelineClear(PipelineCache cache, Operation<Void> original) {
        if (VulkanHelper.isVulkan()) {
            try {
                RenderSystem.getDevice().createCommandEncoder().submit();
            } catch (Throwable ignored) {
            }
        }
        original.call(cache);
    }
}

