package dev.render4u.render.display;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public final class Pipelines {
    public static final RenderPipeline ROUNDED_RECT = gui("rounded_rect");
    public static final RenderPipeline OUTLINE = gui("outline");
    public static final RenderPipeline SHADOW = gui("shadow");
    public static final RenderPipeline LINE = gui("line");
    public static final RenderPipeline CIRCLE = gui("circle");
    public static final RenderPipeline IMAGE = guiTextured("image");
    public static final RenderPipeline FONT = guiTextured("font");
    public static final RenderPipeline BLUR_PANEL = guiTextured("blur_panel");
    public static final RenderPipeline BLUR_DOWNSAMPLE = fullscreen("blur_downsample", "blur_down", false, false);
    public static final RenderPipeline BLUR_UPSAMPLE = fullscreen("blur_upsample", "blur_up", false, false);
    public static final RenderPipeline BLUR_GAUSSIAN_HORIZONTAL = fullscreen("blur_gaussian_h", "blur_separable", false, false);
    public static final RenderPipeline BLUR_GAUSSIAN_VERTICAL = fullscreen("blur_gaussian_v", "blur_separable", true, false);
    public static final RenderPipeline BLUR_BOX_HORIZONTAL = fullscreen("blur_box_h", "blur_separable", false, true);
    public static final RenderPipeline BLUR_BOX_VERTICAL = fullscreen("blur_box_v", "blur_separable", true, true);

    private Pipelines() {
    }

    public static void init() {
        Objects.requireNonNull(ROUNDED_RECT);
    }

    private static RenderPipeline gui(String name) {
        return register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET), name);
    }

    private static RenderPipeline guiTextured(String name) {
        return register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET), name);
    }

    private static RenderPipeline register(RenderPipeline.Builder builder, String name) {
        return RenderPipelines.register(builder
                .withLocation(id("pipeline/" + name))
                .withVertexShader(id("core/" + name))
                .withFragmentShader(id("core/" + name))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR)
                .build());
    }

    private static RenderPipeline fullscreen(String name, String fragment, boolean vertical, boolean box) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(id("pipeline/" + name))
                .withVertexShader(Identifier.withDefaultNamespace("core/screenquad"))
                .withFragmentShader(id("core/" + fragment))
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withColorTargetState(ColorTargetState.DEFAULT);
        RenderPipelines.TRACY_BLIT.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
        if (vertical) {
            builder.withShaderDefine("BLUR_VERTICAL");
        }
        if (box) {
            builder.withShaderDefine("KERNEL_BOX");
        }
        return RenderPipelines.register(builder.build());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("render4u", path);
    }
}
