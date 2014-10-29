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
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.solhost.folko.uosl.libuosl.data.SLSound;
import org.solhost.folko.uosl.libuosl.data.SLSound.SoundEntry;

public class SoundView extends JPanel {
    private static final long serialVersionUID = 534689691557745487L;
    private SLSound sound;
    private JList<SoundListEntry> soundList;
    private JLabel soundLabel;
    private JButton soundButton;

    public SoundView(SLSound sound) {
        this.sound = sound;

        setLayout(new BorderLayout());

        SoundListModel model = new SoundListModel(sound);
        soundList = new JList<SoundListEntry>(model);

        JPanel soundInfoPanel = new JPanel();
        soundInfoPanel.setLayout(new FlowLayout());
        soundLabel = new JLabel("0x0000");
        soundButton = new JButton("Play");
        soundInfoPanel.add(soundLabel);
        soundInfoPanel.add(soundButton);

        soundButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playSound(soundList.getSelectedIndex());
            }
        });

        soundList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        soundList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(e.getValueIsAdjusting()) return;
                selectSound(soundList.getSelectedIndex());
            }
        });
        soundList.setSelectedIndex(0);

        add(new JScrollPane(soundList), BorderLayout.WEST);
        add(soundInfoPanel, BorderLayout.CENTER);
    }

    private void selectSound(int listIndex) {
        int id = soundList.getModel().getElementAt(listIndex).id;
        SoundEntry entry = sound.getEntry(id);
        soundLabel.setText(String.format("0x%02X", entry.id));
    }

    private void playSound(int listIndex) {
        int id = soundList.getModel().getElementAt(listIndex).id;
        SoundEntry entry = sound.getEntry(id);
        AudioFormat format = new AudioFormat(22050, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        try {
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(format, entry.pcmData, 0, entry.pcmData.length);
            clip.start();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }
}

class SoundListEntry {
    int id;
    String name;

    @Override
    public String toString() {
        return String.format("%02X: %s", id, name);
    }
}

class SoundListModel extends AbstractListModel<SoundListEntry> {
    private static final long serialVersionUID = 1L;
    private final List<SoundListEntry> entries;

    public SoundListModel(SLSound sound) {
        entries = new ArrayList<SoundListEntry>(sound.getNumEntries());
        for(int i = 0; i < sound.getNumEntries(); i++) {
            SoundListEntry listEntry = new SoundListEntry();
            SoundEntry soundEntry = sound.getEntry(i);
            if(soundEntry != null) {
                listEntry.id = (int) soundEntry.id;
                listEntry.name = soundEntry.fileName;
                entries.add(listEntry);
            }
        }
    }


    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public SoundListEntry getElementAt(int index) {
        return entries.get(index);
    }

}