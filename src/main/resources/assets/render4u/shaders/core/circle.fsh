#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec2 localPos;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in ivec2 params;

layout(location = 0) out vec4 fragColor;

const float TAU = 6.28318530718;

float innerRadius() {
    return float(params.x & 2047) * 0.5;
}

float startAngle() {
    return float((params.x >> 11) & 1023) * (TAU / 1023.0);
}

float endAngle() {
    return float(params.y & 1023) * (TAU / 1023.0);
}

float segmentDistance(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

void main() {
    float r = halfSize.x;
    float ri = innerRadius();
    float dist = length(localPos);
    float d = dist - r;
    if (ri > 0.0) {
        d = max(d, ri - dist);
    }
    float a0 = startAngle();
    float a1 = endAngle();
    if (a1 - a0 < TAU - 0.01) {
        float ang = atan(localPos.y, localPos.x);
        float rel = mod(ang - a0, TAU);
        if (rel > a1 - a0) {
            vec2 e0 = vec2(cos(a0), sin(a0));
            vec2 e1 = vec2(cos(a1), sin(a1));
            d = min(segmentDistance(localPos, e0 * ri, e0 * r), segmentDistance(localPos, e1 * ri, e1 * r));
        }
    }
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    float a = vertexColor.a * coverage;
    if (a <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, a);
}
