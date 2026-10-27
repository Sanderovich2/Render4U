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

void main() {
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }
    vec2 uv = clamp(localPos / (halfSize * 2.0) + 0.5, 0.0, 1.0);
    if (((radii.x >> 14) & 1) != 0) {
        uv.x = 1.0 - uv.x;
    }
    if (((radii.x >> 15) & 1) != 0) {
        uv.y = 1.0 - uv.y;
    }
    vec4 color = texture(Sampler0, uv) * vertexColor;
    color.a *= coverage;
    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
