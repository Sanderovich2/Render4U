#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec2 localPos;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in ivec2 params;

layout(location = 0) out vec4 fragColor;

const float PADDING = 1.0;

void main() {
    float radius = float(params.x) * 0.25;
    float extent = 2.0 * (halfSize.x - radius - PADDING);
    float t = localPos.x + halfSize.x - radius - PADDING;
    vec2 dv = vec2(max(max(-t, t - extent), 0.0), localPos.y);
    float d = length(dv) - radius;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    float a = vertexColor.a * coverage;
    if (a <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, a);
}
