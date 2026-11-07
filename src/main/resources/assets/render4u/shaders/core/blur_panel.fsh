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
    int shift = localPos.x < 0.0 ? 0 : 6;
    return float((bits >> shift) & 63) * 0.5;
}

int blurLevel() {
    return (radii.x >> 12) & 3;
}

bool isGlass() {
    return ((radii.x >> 14) & 1) == 1;
}

float opacity() {
    int steps = ((radii.x >> 15) & 1) | (((radii.y >> 12) & 15) << 1);
    return float(steps) / 31.0;
}

vec3 sampleBlur(sampler2D tex, vec2 uv) {
    vec2 texel = 1.0 / vec2(textureSize(tex, 0));
    vec2 halfTexel = 0.5 * texel;
    uv = clamp(uv, halfTexel, 1.0 - halfTexel);
    vec2 h = texel * 0.85;

    vec3 sum = texture(tex, uv + vec2(-h.x * 2.0, 0.0)).rgb;
    sum += texture(tex, uv + vec2(h.x * 2.0, 0.0)).rgb;
    sum += texture(tex, uv + vec2(0.0, h.y * 2.0)).rgb;
    sum += texture(tex, uv + vec2(0.0, -h.y * 2.0)).rgb;
    sum += texture(tex, uv + vec2(-h.x, h.y)).rgb * 2.0;
    sum += texture(tex, uv + vec2(h.x, h.y)).rgb * 2.0;
    sum += texture(tex, uv + vec2(h.x, -h.y)).rgb * 2.0;
    sum += texture(tex, uv + vec2(-h.x, -h.y)).rgb * 2.0;
    return sum / 12.0;
}

void main() {
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float aa = max(fwidth(d), 1e-4);
    float coverage = clamp(0.5 - d / aa, 0.0, 1.0);
    float a = coverage * opacity();
    if (a <= 0.0) {
        discard;
    }

    vec2 screenSize = vec2(textureSize(Sampler0, 0)) * float(1 << blurLevel());
    vec2 screenUV = gl_FragCoord.xy / screenSize;

    vec3 color = sampleBlur(Sampler0, screenUV);

    if (isGlass()) {
        float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
        color = clamp(mix(vec3(luma), color, 1.20), 0.0, 1.0);
    }

    if (vertexColor.a > 0.0) {
        color = mix(color, vertexColor.rgb, vertexColor.a);
    }

    fragColor = vec4(color, a);
}
