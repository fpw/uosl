package org.solhost.folko.uosl.slclient.models;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.solhost.folko.uosl.slclient.views.util.Texture;

public class TextureAtlas {
    private static final int DISTANCE = 5;

    private final int maxWidth, maxHeight;
    private final Map<Integer, Rectangle> idMap;
    private int curX, curY, curLineHeight;
    private BufferedImage atlasImage;
    private Texture texture;

    public TextureAtlas(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        atlasImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        idMap = new HashMap<>();
    }

    public void addImage(BufferedImage image, int id) {
        int width = image.getWidth();
        int height = image.getHeight();

        if(curX + width + DISTANCE > maxWidth) {
            if(curY + curLineHeight + DISTANCE + height > maxHeight) {
                throw new RuntimeException("out of space");
            }
            curY += DISTANCE + curLineHeight;
            curLineHeight = 0;
            curX = 0;
        }
        int[] data = image.getRaster().getPixels(0, 0, width, height, (int[] )null);
        atlasImage.getRaster().setPixels(curX, curY, width, height, data);
        idMap.put(id, new Rectangle(curX, curY, width, height));
        curX += width + DISTANCE;
        if(height > curLineHeight) {
            curLineHeight = height;
        }
    }

    public boolean hasEntry(int id) {
        return idMap.containsKey(id);
    }

    private BufferedImage getSubImage() {
        return atlasImage.getSubimage(0, 0, curY == 0 ? curX : maxWidth, curY + curLineHeight + DISTANCE);
    }

    public void write(String name) throws IOException {
        ImageIO.write(getSubImage(), "PNG", new File(name));
    }

    public void generateTextureAndFreeImage() {
        texture = new Texture(getSubImage());
        atlasImage = null;
    }

    public Texture getTexture() {
        return texture;
    }

    public Rectangle getEntry(int id) {
        return idMap.get(id);
    }
}
