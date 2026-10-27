#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;

layout(location = 0) in vec2 localPos;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in ivec2 radii;

layout(location = 0) out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0 ? radii.x : radii.y;
    int shift = localPos.x < 0.0 ? 0 : 7;
    return float((bits >> shift) & 127) * 0.5;
}

vec4 region(float originX, vec2 n, float unit, vec2 size) {
    vec2 low = vec2(originX, 8.0) * unit;
    vec2 texel = clamp(low + n * 8.0 * unit, low + 0.5, low + 8.0 * unit - 0.5);
    return texture(Sampler0, texel / size);
}

void main() {
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }
    vec2 n = clamp(localPos / (halfSize * 2.0) + 0.5, 0.0, 1.0);
    vec2 size = vec2(textureSize(Sampler0, 0));
    float unit = size.x / 64.0;
    vec4 face = region(8.0, n, unit, size);
    if (((radii.x >> 14) & 1) != 0) {
        vec4 hat = region(40.0, n, unit, size);
        float a = hat.a + face.a * (1.0 - hat.a);
        face = vec4(mix(face.rgb, hat.rgb, hat.a), a);
    }
    vec4 color = face * vertexColor;
    color.a *= coverage;
    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
