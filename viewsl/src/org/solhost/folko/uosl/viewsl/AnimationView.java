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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.solhost.folko.uosl.libuosl.data.SLArt;
import org.solhost.folko.uosl.libuosl.data.SLArt.ArtEntry;
import org.solhost.folko.uosl.libuosl.data.SLArt.MobileAnimation;
import org.solhost.folko.uosl.libuosl.types.Direction;

public class AnimationView extends JPanel {
    private static final long serialVersionUID = -225672106193952019L;
    private ImagePanel imagePanel;
    private JList<ArtListEntry> artList;
    private JLabel artLabel;
    private JButton animationButton;
    private SLArt art;
    private Timer timer;
    private int currentFrame;
    private MobileAnimation currentAnimation;

    private static final int ANIMATION_DELAY = 80;

    public AnimationView(SLArt art) {
        this.art = art;
        this.imagePanel = new ImagePanel(200, 200);

        setLayout(new BorderLayout());

        ArtListModel model = new ArtListModel(art);
        artList = new JList<ArtListEntry>(model);

        JPanel artInfoPanel = new JPanel();
        artInfoPanel.setLayout(new FlowLayout());
        artLabel = new JLabel("0x0000");
        animationButton = new JButton("Start");
        artInfoPanel.add(artLabel);
        artInfoPanel.add(animationButton);
        artInfoPanel.add(imagePanel);

        animationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(timer == null) {
                    animationButton.setText("Stop");
                    timer = new Timer(ANIMATION_DELAY, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            updateFrame();
                        }
                    });
                    timer.start();
                } else {
                    animationButton.setText("Start");
                    timer.stop();
                    timer = null;
                    currentFrame = 0;
                    updateFrame();
                }
            }
        });

        artList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) return;
                selectArt(artList.getSelectedIndex());
            }
        });
        artList.setSelectedIndex(0);

        add(new JScrollPane(artList), BorderLayout.WEST);
        add(artInfoPanel, BorderLayout.CENTER);
    }

    private void selectArt(int idx) {
        if(timer != null) {
            animationButton.setText("Start");
            timer.stop();
            timer = null;
        }
        ArtListEntry entry = artList.getModel().getElementAt(idx);
        currentAnimation = art.getAnimationEntry(entry.id, entry.dir, entry.fighting);
        currentFrame = 0;
        artLabel.setText(String.format("0x%04X: %d frames", currentAnimation.id, currentAnimation.frames.size()));
        updateFrame();
    }

    private void updateFrame() {
        int staticID = currentAnimation.frames.get(currentFrame);
        ArtEntry entry = art.getStaticArt(staticID, false);
        entry.mirror(currentAnimation.needMirror);
        imagePanel.setImage(entry.image);
        currentFrame++;
        if(currentFrame == currentAnimation.frames.size()) {
            currentFrame = 0;
        }
    }
}

class ArtListEntry {
    public int id;
    public Direction dir;
    public boolean fighting;

    @Override
    public String toString() {
        return String.format("%04X %s %s", id, dir.toString(), fighting ? "Fighting" : "Walking");
    }
}

class ArtListModel extends AbstractListModel<ArtListEntry> {
    private static final long serialVersionUID = 1L;
    private static final int MAX_MOBILES = 0x3F;
    private final List<ArtListEntry> entries;

    public ArtListModel(SLArt art) {
        entries = new LinkedList<ArtListEntry>();
        for(int i = 0; i < MAX_MOBILES; i++)  {
            for(short dir = 0; dir < 8; dir++) {
                for(int fighting = 0; fighting < 2; fighting++) {
                    Direction d = Direction.parse(dir);
                    boolean isFighting = fighting == 1;
                    MobileAnimation artEntry = art.getAnimationEntry(i, d, isFighting);
                    if(artEntry != null) {
                        ArtListEntry listEntry = new ArtListEntry();
                        listEntry.id = i;
                        listEntry.dir = d;
                        listEntry.fighting = isFighting;
                        entries.add(listEntry);
                    }
                }
            }
        }
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public ArtListEntry getElementAt(int index) {
        return entries.get(index);
    }
}
