#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in ivec2 params;

layout(location = 0) out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec3 msd = texture(Sampler0, texCoord).rgb;
    float sd = median(msd.r, msd.g, msd.b);
    float range = float(params.x & 255) * 0.5;
    vec2 unitRange = vec2(range) / vec2(textureSize(Sampler0, 0));
    vec2 screenTexSize = vec2(1.0) / max(fwidth(texCoord), vec2(1e-6));
    float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);

    float outline = float(params.y & 255) * 0.00390625;
    float blur = float((params.y >> 8) & 255) * 0.05;

    float d = sd - 0.5 + outline;
    float edge = blur > 0.0 ? (screenPxRange / (1.0 + blur * screenPxRange)) : screenPxRange;
    float a = vertexColor.a * clamp(d * edge + 0.5, 0.0, 1.0);
    if (a <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, a);
}
