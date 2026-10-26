#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 size = vec2(textureSize(InSampler, 0));
    vec2 uv = 2.0 * gl_FragCoord.xy / size;
    vec2 texel = 1.0 / size;
    vec3 sum = texture(InSampler, uv).rgb * 4.0;
    sum += texture(InSampler, uv + vec2(-texel.x, -texel.y)).rgb;
    sum += texture(InSampler, uv + vec2(texel.x, -texel.y)).rgb;
    sum += texture(InSampler, uv + vec2(-texel.x, texel.y)).rgb;
    sum += texture(InSampler, uv + vec2(texel.x, texel.y)).rgb;
    fragColor = vec4(sum * 0.125, 1.0);
}
