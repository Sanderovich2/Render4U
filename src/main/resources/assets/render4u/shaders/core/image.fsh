#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in ivec2 params;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord);
    vec4 color = tex * vertexColor;

    float radius = float(params.x & 63);
    if (radius > 0.0) {
        vec2 size = vec2(float(params.x >> 6), float(params.y));
        vec2 p = texCoord * size;
        vec2 q = abs(p - size * 0.5) - (size * 0.5 - radius);
        float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
        float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
        color.a *= coverage;
        if (color.a <= 0.0) {
            discard;
        }
    }

    fragColor = vec4(color.rgb, color.a);
}
