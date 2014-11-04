package org.solhost.folko.uosl.slclient.views;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import org.solhost.folko.uosl.common.RandUtil;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLSound;
import org.solhost.folko.uosl.libuosl.data.SLSound.SoundEntry;
import org.solhost.folko.uosl.slclient.models.GameState;
import org.solhost.folko.uosl.slclient.models.GameState.State;

public class SoundManager {
    private static final Logger log = Logger.getLogger("slclient.sound");
    private final SLSound sound;
    private final GameState game;
    private Sequencer sequencer;
    private Sequence songs[];
    private int currentSongId;

    public SoundManager(GameState gameState) {
        this.game = gameState;
        this.sound = SLData.get().getSound();
    }

    public void init() {
        try {
            log.fine("Initializing MIDI system");
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addMetaEventListener(new MetaEventListener() {
                @Override
                public void meta(MetaMessage meta) {
                    if(meta.getType() == 0x2F && sequencer.isRunning()) {
                        // stop sequencer at end of songs so we can start a different song
                        log.finer("Song ended, stopping sequencer");
                        sequencer.stop();
                    }
                }
            });
        } catch (MidiUnavailableException e) {
            log.warning("No MIDI support -> no music");
        }
        songs = new Sequence[25];
        loadSongs();
    }

    public void update(long elapsedMillis) {
        if(sequencer != null) {
            updateSong();
        }
    }

    private void updateSong() {
        // this is implemented exactly like in the client, including unreachable entries

        // Strategy: Search nearest location center in the following table.
        //           Since centers can overlap, all centers have to be scanned.
        //           The closest location index is then used as index into the songTable.

        // @434528h in client
        final int[][] locationTable = {
                // centerx, centery, size
                {320, 592, 75},
                {464, 592, 16},
                {536, 568, 0},
                {480, 680, 16},
                {387, 868, 50},
                {64,  560, 40},
                {48,  704, 20},
                {80,  832, 40},
                {512, 640, 128},
                {0,     0,  0},
                {0,     0,  0},
        };

        // @434580h in client
        final int[] songTable = {
                10, 16, 16, 16, 2, 24, 24, 24, 7, 4, 6, 7, 8, 9, 20, 1
        };

        if(game.getState() != State.LOGGED_IN) {
            // no music prior to login
            return;
        }

        if(game.isPlayerInWarMode() && currentSongId != songTable[9]) {
            // in war mode, always play war song immediately
            playSongNow(songTable[9]);
        } else if(!sequencer.isRunning()) {
            // otherwise, only choose new song when required
            int index = 10;
            int x = game.getPlayerLocation().getX();
            int y = game.getPlayerLocation().getY();
            int minDist = Integer.MAX_VALUE;
            for(int i = 0; i < locationTable.length; i++) {
                int distX = Math.abs(x - locationTable[i][0]);
                int distY = Math.abs(y - locationTable[i][1]);
                int dist = Math.max(distX, distY);
                if(dist <= locationTable[i][2] && dist <= minDist) {
                    minDist = dist;
                    index = i;
                }
            }
            if(index == 8) {
                // in town, use random music
                index = RandUtil.random(11,  15);
            }
            int songId = songTable[index];
            playSongNow(songId);
        }
    }

    private void playSongNow(int id) {
        if(sequencer == null) {
            return;
        }
        if(id < 0 || id >= songs.length || songs[id] == null) {
            log.warning("Couldn't find song " + id);
            return;
        }
        try {
            log.finer("Playing song " + id);
            sequencer.stop();
            currentSongId = id;
            sequencer.setSequence(songs[id]);
            sequencer.start();
        } catch (Exception e) {
            log.log(Level.WARNING, "Couldn't play song " + id + ": " + e.getMessage(), e);
        }
    }

    public void playSound(int id) {
        try {
            SoundEntry sfx = sound.getEntry(id);
            AudioFormat format = new AudioFormat(22050, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(format, sfx.pcmData, 0, sfx.pcmData.length);
            clip.start();
        } catch(Exception e) {
            log.log(Level.WARNING, "Couldn't play sound " + id + ": " + e.getMessage(), e);
        }
    }

    private Sequence[] loadSongs() {
        if(sequencer == null) {
            return null;
        }

        for(int i = 0; i < songs.length; i++) {
            try {
                Path path = Paths.get(SLData.get().getDataPath(), "MUSIC", String.format("ULTIMA%02d.MID", i));
                songs[i] = MidiSystem.getSequence(path.toFile());
            } catch (Exception e) {
                continue;
            }
        }

        return new Sequence[0];
    }

    public void stop() {
        if(sequencer != null && sequencer.isOpen()) {
            sequencer.stop();
            sequencer.close();
        }
    }
}
