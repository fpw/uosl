#version 330 core

layout(location = 0) in vec2 position;
layout(location = 1) in float type;
layout(location = 2) in vec2 translation;
layout(location = 3) in vec4 zOffsets;
layout(location = 4) in vec4 texRect;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec4 atlasDimensions;

out vec2 texCoords;
out float texType;

void main() {
    float landWidth     = atlasDimensions[0];
    float landHeight    = atlasDimensions[1];
    float staticWidth   = atlasDimensions[2];
    float staticHeight  = atlasDimensions[3];

    if(type == 0.0f) {
        // direct land tile
        float texX = texRect[0] / landWidth;
        float texY = texRect[1] / landHeight;
        float texW = texRect[2] / landWidth;
        float texH = texRect[3] / landHeight;
        switch(gl_VertexID) {
            case 0: texCoords = vec2(texX + 0.5 * texW, texY); break;
            case 1: texCoords = vec2(texX,              texY + 0.5 * texH); break;
            case 2: texCoords = vec2(texX + 0.5 * texW, texY + texH); break;
            case 3: texCoords = vec2(texX + texW,       texY + 0.5 * texH); break;
        }
    } else {
        // texture tile
        float texX = texRect[0] / staticWidth;
        float texY = texRect[1] / staticHeight;
        float texW = texRect[2] / staticWidth;
        float texH = texRect[3] / staticHeight;
        switch(gl_VertexID) {
            case 0: texCoords = vec2(texX,          texY); break;
            case 1: texCoords = vec2(texX,          texY + texH); break;
            case 2: texCoords = vec2(texX + texW,   texY + texH); break;
            case 3: texCoords = vec2(texX + texW,   texY); break;
        }
    }
    vec4 instancePosition = vec4(position.x + translation.x, position.y + translation.y, zOffsets[gl_VertexID], 1);
    gl_Position = projectionMatrix * viewMatrix * instancePosition;
    texType = type;
}
