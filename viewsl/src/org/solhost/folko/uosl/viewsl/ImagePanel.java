/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.uosl.viewsl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JComponent;

public class ImagePanel extends JComponent {
    private static final long serialVersionUID = -594060983665117884L;
    private Image image;

    public ImagePanel(Image image) {
        setImage(image);
    }

    public ImagePanel(int width, int height) {
        setMinimumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
    }

    public void setImage(Image image) {
        this.image = image;
        if(image != null) {
            setMinimumSize(new Dimension(image.getWidth(null), image.getHeight(null)));
            setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null) {
            g.drawImage(image, getWidth() / 2 - image.getWidth(null) / 2,
                    getHeight() / 2 - image.getHeight(null) / 2, null);
        }
    }

    public Point getMouseImagePosition() {
        Point p = getMousePosition();
        if(p == null) {
            return null;
        }
        p.x -= getWidth() / 2 - image.getWidth(null) / 2;
        p.y -= getHeight() / 2 - image.getHeight(null) / 2;
        return p;
    }
}
