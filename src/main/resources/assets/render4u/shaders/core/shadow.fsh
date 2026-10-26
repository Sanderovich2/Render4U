#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec2 localPos;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in ivec2 params;

layout(location = 0) out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0
        ? (localPos.x < 0.0 ? params.x : params.x >> 5)
        : (localPos.x < 0.0 ? params.y : params.x >> 10);
    return float(bits & 31);
}

void main() {
    float blur = float((params.y >> 5) & 127) * 0.5;
    float strength = 1.0 + float((params.y >> 12) & 15) * 0.2;
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float w = max(blur, 0.5);
    float falloff = 1.0 - smoothstep(-w, w, d);
    float a = vertexColor.a * clamp(falloff * strength, 0.0, 1.0);
    if (a <= 0.0) {
        discard;
    }
    float dither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    fragColor = vec4(vertexColor.rgb, clamp(a + dither / 255.0, 0.0, 1.0));
}
