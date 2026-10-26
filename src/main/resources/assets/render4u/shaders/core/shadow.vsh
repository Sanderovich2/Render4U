#version 330
#extension GL_ARB_separate_shader_objects : require

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in ivec2 UV2;
layout(location = 3) in vec4 Color;

layout(location = 0) out vec2 localPos;
layout(location = 1) out vec4 vertexColor;
layout(location = 2) flat out vec2 halfSize;
layout(location = 3) flat out ivec2 params;

const float PADDING = 1.0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    params = UV2 & 0xFFFF;
    float blur = float((params.y >> 5) & 127) * 0.5;
    localPos = UV0;
    halfSize = abs(UV0) - (blur + PADDING);
    vertexColor = Color * ColorModulator;
}
