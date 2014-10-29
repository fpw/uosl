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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.solhost.folko.uosl.libuosl.data.SLMap;
import org.solhost.folko.uosl.libuosl.types.Point2D;

public class MapView extends JPanel {
    private static final long serialVersionUID = -9081741694620720599L;
    private BufferedImage mapImage;
    private final ImagePanel imagePanel;
    private final JLabel coordsLabel;
    private final SLMap map;

    public MapView(SLMap map) {
        this.map = map;
        generateImage();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseChanged();
            }
        });

        setLayout(new BorderLayout());
        this.imagePanel = new ImagePanel(mapImage);
        this.coordsLabel = new JLabel("Map Coordinates: ");
        add(imagePanel, BorderLayout.NORTH);
        add(coordsLabel, BorderLayout.SOUTH);
    }

    public void generateImage() {
        mapImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < 512; x += 2) {
            for(int y = 0; y < 512; y += 2) {
                int color = (int) (map.getColor(x / 4 * 128 + y / 4));
                color = (color >>> 18) & 0xFF2;

                // TODO: not quite correct I hope
                int b = (color >>> 8) & 0xF;
                int g = (color >>> 4) & 0xF;
                int r = (color >>> 0) & 0xF;
                int rgb = (r << 20) | (g << 12) | (b << 4);

                mapImage.setRGB(x, y, rgb);
                mapImage.setRGB(x + 1, y, rgb);
                mapImage.setRGB(x + 1, y + 1, rgb);
                mapImage.setRGB(x, y + 1, rgb);
            }
        }
    }

    public void mouseChanged() {
        Point p = imagePanel.getMouseImagePosition();
        if(p == null) {
            return;
        }
        int x = (p.x / 2) * 4;
        int y = (p.y / 2) * 4;
        if(x < 1024 && y < 1024 && x >=0 && y >= 0) {
            int z = map.getElevation(new Point2D(x, y));
            coordsLabel.setText(String.format("Map Coordinates: %d, %d, %d", x, y, z));
        }
    }
}
