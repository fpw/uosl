#version 330 core

uniform mat4 mat;
uniform vec4 zOffsets;
uniform int textureType; // 0 = land, 1 = static, 2 = mobile

layout(location = 0) in vec2 position;

out vec2 texCoords;

void main() {
    if(textureType == 0) {
        // land tile
        switch(gl_VertexID) {
            case 0: texCoords = vec2(0.5, 0.0); break;
            case 1: texCoords = vec2(0.0, 0.5); break;
            case 2: texCoords = vec2(0.5, 1.0); break;
            case 3: texCoords = vec2(1.0, 0.5); break;
        }
    } else {
        // static tile
        switch(gl_VertexID) {
            case 0: texCoords = vec2(0.0, 0.0); break;
            case 1: texCoords = vec2(0.0, 1.0); break;
            case 2: texCoords = vec2(1.0, 1.0); break;
            case 3: texCoords = vec2(1.0, 0.0); break;
        }
    }
    vec4 fixedPosition = vec4(position, zOffsets[gl_VertexID], 1);
    gl_Position = mat * fixedPosition;
}
