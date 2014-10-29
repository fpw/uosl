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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.solhost.folko.uosl.libuosl.data.SLData;

public class ViewSL extends JFrame {
    private static final long serialVersionUID = 2010783699646344066L;
    private final SLData data;
    private MapView mapView;
    private ArtView landView;
    private ArtView staticView;
    private SoundView soundView;
    private GumpView gumpView;
    private AnimationView animationView;
    private GameView gameView;

    public ViewSL(SLData data) {
        super("ViewSL: Folko's UOSL Data Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.data = data;

        setMinimumSize(new Dimension(800, 600));

        setupTabs();
        pack();
        setLocationByPlatform(true);
        setVisible(true);
    }

    private void setupTabs() {
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                tabs.getSelectedComponent().requestFocusInWindow();
            }
        });

        gameView = new GameView(data);
        tabs.add("Game", gameView);

        mapView = new MapView(data.getMap());
        tabs.add("Map", mapView);

        landView = new ArtView(true, data.getArt(), data.getTiles());
        tabs.add("Land Tiles", landView);

        staticView = new ArtView(false, data.getArt(), data.getTiles());
        tabs.add("Static Tiles", staticView);

        animationView = new AnimationView(data.getArt());
        tabs.add("Animations", animationView);

        gumpView = new GumpView(data.getGumps());
        tabs.add("Gumps", gumpView);

        soundView = new SoundView(data.getSound());
        tabs.add("Sound", soundView);

        add(tabs);
    }

    private static boolean initData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose UOSL Data Directory");
        String dir = "data";
        boolean success = false;
        while(!success) {
            try {
                SLData.init(dir);
                success = true;
            } catch(Exception e) {
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    dir = chooser.getSelectedFile().getAbsolutePath();
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            if(!initData()) {
                return;
            }
            new ViewSL(SLData.get());
        });
    }
}
