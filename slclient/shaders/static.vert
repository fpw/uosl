#version 330 core

layout(location = 0) in vec2 position;

uniform vec2 atlasDimensions;
uniform mat4 mat;
uniform vec4 texRect;

out vec2 texCoords;

void main() {
    float texX = texRect[0] / atlasDimensions[0];
    float texY = texRect[1] / atlasDimensions[1];
    float texW = texRect[2] / atlasDimensions[0];
    float texH = texRect[3] / atlasDimensions[1];
    switch(gl_VertexID) {
        case 0: texCoords = vec2(texX,          texY); break;
        case 1: texCoords = vec2(texX,          texY + texH); break;
        case 2: texCoords = vec2(texX + texW,   texY + texH); break;
        case 3: texCoords = vec2(texX + texW,   texY); break;
    }
    vec4 fixedPosition = vec4(position, 0, 1);
    gl_Position = mat * fixedPosition;
}
