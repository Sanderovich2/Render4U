#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 size = vec2(textureSize(InSampler, 0));
    vec2 uv = gl_FragCoord.xy / (2.0 * size);
    vec2 h = 0.5 / size;
    vec3 sum = texture(InSampler, uv + vec2(-2.0 * h.x, 0.0)).rgb;
    sum += texture(InSampler, uv + vec2(-h.x, h.y)).rgb * 2.0;
    sum += texture(InSampler, uv + vec2(0.0, 2.0 * h.y)).rgb;
    sum += texture(InSampler, uv + vec2(h.x, h.y)).rgb * 2.0;
    sum += texture(InSampler, uv + vec2(2.0 * h.x, 0.0)).rgb;
    sum += texture(InSampler, uv + vec2(h.x, -h.y)).rgb * 2.0;
    sum += texture(InSampler, uv + vec2(0.0, -2.0 * h.y)).rgb;
    sum += texture(InSampler, uv + vec2(-h.x, -h.y)).rgb * 2.0;
    fragColor = vec4(sum / 12.0, 1.0);
}
