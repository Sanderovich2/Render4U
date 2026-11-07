#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec2 localPos;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in ivec2 radii;

layout(location = 0) out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0 ? radii.x : radii.y;
    int shift = localPos.x < 0.0 ? 0 : 6;
    return float((bits >> shift) & 63) * 0.5;
}

float thickness() {
    int low = (radii.x >> 12) & 15;
    int high = (radii.y >> 12) & 15;
    return float(low | high << 4) * 0.0625;
}

void main() {
    float r = cornerRadius();
    float t = max(thickness(), 0.5);
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;

    float aa = max(fwidth(d), 1e-4);
    float outer = clamp(0.5 - d / aa, 0.0, 1.0);
    float inner = clamp(0.5 - (-d - t) / aa, 0.0, 1.0);
    float coverage = outer * inner;

    float a = vertexColor.a * coverage;
    if (a <= 0.001) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, a);
}
