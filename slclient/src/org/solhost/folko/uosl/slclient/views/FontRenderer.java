package org.solhost.folko.uosl.slclient.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class FontRenderer {
    private Font font;
    private FontMetrics metrics;

    public FontRenderer(Font font) {
        setFont(font);
    }

    public void setFont(Font font) {
        this.font = font;
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);
        metrics = g2.getFontMetrics();
        g2.dispose();
    }

    public Texture renderText(String text, Color color) {
        BufferedImage textImage = createTextImage(text, color);
        Texture textTexture = new Texture(textImage);
        return textTexture;
    }

    private BufferedImage createTextImage(String text, Color color) {
        int width = metrics.stringWidth(text);
        int height = metrics.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setFont(font);
        g2.drawString(text, 0, metrics.getAscent());
        g2.dispose();
        return image;
    }

    public int getTextHeight() {
        return metrics.getHeight();
    }

    public Font getFont() {
        return font;
    }
}
