#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord);
    float a = tex.a * vertexColor.a;
    if (a <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb * tex.rgb, a);
}
