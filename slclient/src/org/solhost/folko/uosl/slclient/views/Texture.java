package org.solhost.folko.uosl.slclient.views;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class Texture {
    private final int id, width, height;
    private final boolean useMipMapping = false;

    public Texture(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        if(image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new RuntimeException("Can only open INT_ARGB images");
        }

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                buffer.put((byte) ((argb >> 16) & 0xFF));
                buffer.put((byte) ((argb >> 8) & 0xFF));
                buffer.put((byte) ((argb >> 0) & 0xFF));
                buffer.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        buffer.rewind();

        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        if(useMipMapping) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glGenerateMipmap(GL_TEXTURE_2D);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        }
    }

    public void setTextureUnit(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTextureId() {
        return id;
    }
}
