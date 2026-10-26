#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 size = vec2(textureSize(InSampler, 0));
    vec2 uv = gl_FragCoord.xy / size;
#ifdef BLUR_VERTICAL
    vec2 dir = vec2(0.0, 1.0 / size.y);
#else
    vec2 dir = vec2(1.0 / size.x, 0.0);
#endif
#ifdef KERNEL_BOX
    vec3 color = texture(InSampler, uv).rgb * 0.2
        + (texture(InSampler, uv + dir * 1.5).rgb + texture(InSampler, uv - dir * 1.5).rgb) * 0.4;
#else
    vec3 color = texture(InSampler, uv).rgb * 0.2270270270
        + (texture(InSampler, uv + dir * 1.3846153846).rgb + texture(InSampler, uv - dir * 1.3846153846).rgb) * 0.3162162162
        + (texture(InSampler, uv + dir * 3.2307692308).rgb + texture(InSampler, uv - dir * 3.2307692308).rgb) * 0.0702702703;
#endif
    fragColor = vec4(color, 1.0);
}
