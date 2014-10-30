#version 330 core

uniform sampler2D tex;
uniform int selectionID;

in vec2 texCoords;
out vec4 color;

void main() {
    vec4 texColor = texture(tex, texCoords);
    if(selectionID == 0) {
        // normal rendering
        color = texColor;
    } else {
        // selection rendering, i.e. use selectionID as color
        if(texColor.a != 0) {
            ivec3 intColor = ivec3(
                        (selectionID >> 0) & 0xFF,
                        (selectionID >> 8) & 0xFF,
                        (selectionID >> 16) & 0xFF
                    );
            color = vec4(intColor.x / 255.0, intColor.y / 255.0, intColor.z / 255.0, 1);
        } else {
            color = vec4(0, 0, 0, 0);
        }
    }
}
