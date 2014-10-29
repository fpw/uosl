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
import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.solhost.folko.uosl.libuosl.data.SLArt;
import org.solhost.folko.uosl.libuosl.data.SLTiles;
import org.solhost.folko.uosl.libuosl.data.SLArt.ArtEntry;
import org.solhost.folko.uosl.libuosl.data.SLTiles.LandTile;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;

public class ArtView extends JPanel {
    private static final long serialVersionUID = 4594116320550647856L;
    private ImagePanel imagePanel;
    private JList<ListEntry> artList;
    private StaticInfoPanel staticPanel;
    private LandInfoPanel landPanel;
    private JTextField searchField;
    private TileModel listModel;
    private SLArt art;
    private SLTiles tiles;
    private boolean landOrStatic;

    public ArtView(boolean landOrStatic, SLArt art, SLTiles tiles) {
        this.landOrStatic = landOrStatic;
        this.tiles = tiles;
        this.art = art;
        this.imagePanel = new ImagePanel(200, 200);
        this.staticPanel = new StaticInfoPanel();
        this.landPanel = new LandInfoPanel();
        this.searchField = new JTextField();

        setLayout(new BorderLayout());

        this.listModel = new TileModel(tiles, art, landOrStatic);
        this.artList = new JList<ListEntry>(listModel);

        JPanel artInfoPanel = new JPanel();
        artInfoPanel.setLayout(new GridLayout(2, 1));
        if(landOrStatic) {
            artInfoPanel.add(landPanel);
        } else {
            artInfoPanel.add(staticPanel);
        }
        artInfoPanel.add(imagePanel);

        artList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) return;
                selectArt(artList.getSelectedIndex());
            }
        });
        artList.setSelectedIndex(0);

        JPanel listPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPanel = new JScrollPane(artList);
        listPanel.add(searchField, BorderLayout.NORTH);
        listPanel.add(scrollPanel, BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {
                onSearch(searchField.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                onSearch(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                onSearch(searchField.getText());
            }
        });

        add(listPanel, BorderLayout.WEST);
        add(artInfoPanel, BorderLayout.CENTER);
    }

    private void onSearch(String what) {
        ((TileModel) artList.getModel()).search(what);
    }

    private void selectArt(int idx) {
        if(artList.getSelectedValue() == null) {
            return;
        }
        int id = artList.getModel().getElementAt(idx).id;
        ArtEntry entry;
        String name = "";
        if(landOrStatic) {
            LandTile tile = tiles.getLandTile(id);
            landPanel.setTile(tile);
            name = tile.name;
            entry = art.getLandArt(id);
            if(entry != null) {
                String info = String.format("0x%04X: %s, Unknown: %08X", entry.id, name, (int) entry.unknown);
                landPanel.setBorder(BorderFactory.createTitledBorder(info));
            }
        } else {
            StaticTile tile = tiles.getStaticTile(id);
            staticPanel.setTile(tile);
            name = tile.name;
            entry = art.getStaticArt(id, false);
            if(entry != null) {
                String info = String.format("0x%04X: %s, Unknown: %08X", entry.id, name, (int) entry.unknown);
                staticPanel.setBorder(BorderFactory.createTitledBorder(info));
            }
        }
        if(entry != null) {
            int width = entry.image.getWidth();
            int height = entry.image.getHeight();
            imagePanel.setImage(entry.image);
            imagePanel.setBorder(BorderFactory.createTitledBorder("Dimensions: " + width + "x" + height));
        } else {
            imagePanel.setImage(null);
            imagePanel.setBorder(BorderFactory.createTitledBorder("No image"));
        }
    }
}

class ListEntry {
    public int id;
    public String name;
    public long flags;

    @Override
    public String toString() {
        return String.format("%04X: %s", id, name);
    }
}

class TileModel extends AbstractListModel<ListEntry> {
    private static final int NUM_ENTRIES = 0x4000;
    private static final long serialVersionUID = 1L;

    private final List<ListEntry> allEntries, displayEntries;

    public TileModel(SLTiles tiles, SLArt art, boolean landOrStatic) {
        allEntries = new LinkedList<ListEntry>();
        displayEntries = new LinkedList<ListEntry>();
        for(int i = 0; i < NUM_ENTRIES; i++)  {
            ArtEntry artEntry;
            ListEntry listEntry = new ListEntry();
            listEntry.id = i;
            if(landOrStatic) {
                LandTile tile = tiles.getLandTile(i);
                listEntry.name = tile.name;
                listEntry.flags = tile.flags;
                artEntry = art.getLandArt(i);
            } else {
                StaticTile tile = tiles.getStaticTile(i);
                listEntry.name = tile.name;
                listEntry.flags = tile.flags;
                artEntry = art.getStaticArt(i, false);
            }
            if(artEntry != null || listEntry.name.length() > 0 || listEntry.flags != 0) {
                allEntries.add(listEntry);
                displayEntries.add(listEntry);
            }
        }
    }

    public void search(String what) {
        String str = what.toLowerCase();
        displayEntries.clear();
        for(int i = 0; i < allEntries.size(); i++) {
            ListEntry entry = allEntries.get(i);
            if(entry.name.toLowerCase().contains(str)) {
                displayEntries.add(entry);
            }
        }
        fireContentsChanged(this, 0, allEntries.size());
    }

    @Override
    public int getSize() {
        return displayEntries.size();
    }

    @Override
    public ListEntry getElementAt(int index) {
        return displayEntries.get(index);
    }
}

class StaticInfoPanel extends JPanel {
    private static final long serialVersionUID = -6607340640593973648L;
    private final JLabel height, layer, price, weight, animation, unk1, unk2;
    private final JLabel[] flagLabels = new JLabel[32];
    private static final String[] staticFlags = {
        //  1               2               4             8
        "Background",   "Weapon",       "Transparent",  "Translucent",
        "Wall",         "Damaging",     "Impassable",   "Wet",
        "Ignored",      "Surface",      "Stairs",       "Stackable",
        "Window",       "NoShoot",      "ArticleA",     "ArticleAn",
        "Generator",    "Foliage",      "LightSource",  "Animation",
        "NoDiagonal",   "Container",    "Wearable",     "Light",
        "Animation",    "Unknown 3",    "Unknown 4",    "Armor",
        "Roof",         "Door",         "Unknown 8",    "Unknown 9" };

    public StaticInfoPanel() {
        height = new JLabel();
        layer = new JLabel();
        price = new JLabel();
        weight = new JLabel();
        animation = new JLabel();
        unk1 = new JLabel();
        unk2 = new JLabel();

        JPanel flagPanel = new JPanel();
        setLayout(new GridLayout(10, 4));
        for(int i = 0; i < 32; i++) {
            flagLabels[i] = new JLabel(staticFlags[i]);
            add(flagLabels[i]);
        }
        add(height);
        add(layer);
        add(price);
        add(weight);
        add(animation);
        add(unk1);
        add(unk2);

        add(flagPanel);
    }

    public void setTile(StaticTile tile) {
        for(int i = 0; i < 32; i++) {
            if((tile.flags & (1 << i)) != 0) {
                flagLabels[i].setEnabled(true);
                flagLabels[i].setForeground(Color.blue);
            } else {
                flagLabels[i].setEnabled(false);
                flagLabels[i].setForeground(Color.black);
            }
        }
        height.setText(String.format("Height: %d", tile.height));
        layer.setText(String.format("Layer: %d", tile.layer));
        price.setText(String.format("Price: %d", tile.price));
        weight.setText(String.format("Weight: %d", tile.weight));
        animation.setText(String.format("Animation: 0x%04X", tile.animationID));
        unk1.setText(String.format("Unknown 1: 0x%04X", tile.unknown1));
        unk2.setText(String.format("Unknown 2: 0x%04X", tile.unknown2));
    }
}

class LandInfoPanel extends JPanel {
    private static final long serialVersionUID = -6607340640593973648L;
    private final JLabel texture;
    private final JLabel[] flagLabels = new JLabel[32];
    private static final String[] staticFlags = {
        //  1               2               4             8
        "Unknown",      "Unknown",      "Unknown",      "Unknown",
        "Unknown",      "Unknown",      "Impassable",   "Unknown",
        "Unknown",      "Unknown",      "Unknown",      "Unknown",
        "Unknown",      "Unknown",      "Unknown",      "Unknown",
        "Unknown",      "Unknown",      "Unknown",      "Unknown",
        "Unknown",      "Unknown",      "Unknown",      "Unknown",
        "Unknown",      "Unknown",      "Unknown",      "Unknown",
        "Unknown",      "Unknown",      "Unknown",      "Unknown" };


    public LandInfoPanel() {
        texture = new JLabel();

        JPanel flagPanel = new JPanel();
        setLayout(new GridLayout(9, 4));
        for(int i = 0; i < 32; i++) {
            flagLabels[i] = new JLabel(staticFlags[i]);
            add(flagLabels[i]);
        }
        add(texture);
        add(flagPanel);
    }

    public void setTile(LandTile tile) {
        for(int i = 0; i < 32; i++) {
            if((tile.flags & (1 << i)) != 0) {
                flagLabels[i].setEnabled(true);
                flagLabels[i].setForeground(Color.blue);
            } else {
                flagLabels[i].setEnabled(false);
                flagLabels[i].setForeground(Color.black);
            }
        }
        texture.setText(String.format("Texture: 0x%04X", tile.textureID));
    }
}
