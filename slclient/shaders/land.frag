#version 330 core

uniform sampler2D landTexture;
uniform sampler2D staticTexture;

in vec2 texCoords;
in float texType;
out vec4 color;

void main() {
    if(texType == 0.0f) {
        color = texture(landTexture, texCoords);
    } else {
        color = texture(staticTexture, texCoords);
    }
}
