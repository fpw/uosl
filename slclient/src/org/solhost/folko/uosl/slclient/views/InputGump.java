package org.solhost.folko.uosl.slclient.views;

import java.awt.Color;
import java.awt.Font;

public class InputGump {
    private static final int INPUT_FONT_SIZE = 14;
    private final FontRenderer renderer;
    private final StringBuffer inputLine;
    private Texture texture;

    public InputGump() {
        this.renderer = new FontRenderer(new Font(Font.SANS_SERIF, Font.BOLD, INPUT_FONT_SIZE));
        this.inputLine = new StringBuffer();
    }

    public void feedCharacter(char c) {
        if(c == 127) {
            // DEL
            int oldLen = inputLine.length();
            if(oldLen > 0) {
                inputLine.setLength(oldLen - 1);
            }
        } else {
            inputLine.append(c);
        }
        updateTexture();
    }

    public String getAndReset() {
        String res = inputLine.toString();
        inputLine.setLength(0);
        updateTexture();
        return res;
    }

    private void updateTexture() {
        if(texture != null) {
            texture.dispose();
        }
        texture = renderer.renderText(inputLine.toString() + "_", Color.RED);
    }

    public Texture getTexture() {
        if(texture == null) {
            updateTexture();
        }
        return texture;
    }

    public int getTextHeight() {
        return renderer.getTextHeight();
    }
}
