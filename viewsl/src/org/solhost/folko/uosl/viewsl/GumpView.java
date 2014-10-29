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
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.solhost.folko.uosl.libuosl.data.SLGumps;
import org.solhost.folko.uosl.libuosl.data.SLGumps.GumpEntry;

public class GumpView extends JPanel {
    private static final long serialVersionUID = 2447630322701777960L;
    private final List<Integer> gumpIDs;
    private SLGumps gumps;
    private ImagePanel imagePanel;
    private JList<String> gumpList;
    private JLabel gumpLabel, coordsLabel;

    public GumpView(SLGumps gumps) {
        this.gumps = gumps;
        this.imagePanel = new ImagePanel(200, 200);

        setLayout(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<String>();
        gumpList = new JList<String>(model);
        gumpIDs = gumps.getAllGumpIDs();
        for(int i : gumpIDs) {
            model.addElement(String.format("%02X", i));
        }

        JPanel gumpInfoPanel = new JPanel(new BorderLayout());
        gumpLabel = new JLabel("0x0000");
        this.coordsLabel = new JLabel("Gump Coordinates: ");
        gumpInfoPanel.add(gumpLabel, BorderLayout.NORTH);
        gumpInfoPanel.add(coordsLabel, BorderLayout.SOUTH);
        gumpInfoPanel.add(imagePanel, BorderLayout.CENTER);

        gumpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gumpList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) return;
                selectGump(gumpList.getSelectedIndex());
            }
        });
        gumpList.setSelectedIndex(0);

        add(new JScrollPane(gumpList), BorderLayout.WEST);
        add(gumpInfoPanel, BorderLayout.CENTER);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseChanged();
            }
        });
    }

    private void selectGump(int listIndex) {
        GumpEntry entry = gumps.getGump(gumpIDs.get(listIndex));
        gumpLabel.setText(String.format("0x%04X", entry.id));
        imagePanel.setImage(entry.image);
    }

    private void mouseChanged() {
        Point p = imagePanel.getMouseImagePosition();
        if(p == null) {
            return;
        }
        coordsLabel.setText(String.format("Gump Coordinates: %d, %d", p.x, p.y));
    }
}
