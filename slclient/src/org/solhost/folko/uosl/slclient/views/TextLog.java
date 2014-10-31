package org.solhost.folko.uosl.slclient.views;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class TextLog {
    private static final int TEXT_SIZE = 14;
    private static final int MIN_DURATION = 2000;
    private static final int DURATION_PER_CHAR = 75;
    private final Map<Object, List<TextEntry>> entries;
    private final FontRenderer renderer;

    public class TextEntry {
        private final long expireAt;
        public Texture texture;
        public String text;
        public Color color;

        public TextEntry(String text, Color color) {
            this.expireAt = System.currentTimeMillis() + MIN_DURATION + DURATION_PER_CHAR * text.length();
            this.text = text;
            this.color = color;
        }
    }

    public TextLog() {
        renderer = new FontRenderer(new Font(Font.SANS_SERIF, Font.BOLD, TEXT_SIZE));
        entries = new HashMap<>();
    }

    public void update(long elapsedMillis) {
        long now = System.currentTimeMillis();
        for(Iterator<Object> it = entries.keySet().iterator(); it.hasNext(); ) {
            Object where = it.next();
            List<TextEntry> textEntries = entries.get(where);
            for(Iterator<TextEntry> textIt = textEntries.iterator(); textIt.hasNext(); ) {
                TextEntry entry = textIt.next();
                if(now > entry.expireAt) {
                    if(entry.texture != null) {
                        entry.texture.dispose();
                    }
                    textIt.remove();
                } else if(entry.texture == null) {
                    entry.texture = renderer.renderText(entry.text, entry.color);
                }
            }
            if(textEntries.isEmpty()) {
                it.remove();
            }
        }
    }

    public void addEntry(Object where, String text, Color color) {
        List<TextEntry> list = entries.get(where);
        if(list == null) {
            list = new LinkedList<>();
            entries.put(where, list);
        }
        list.add(new TextEntry(text, color));
    }

    public void visitEntries(BiConsumer<? super Object, ? super List<TextEntry>> c) {
        entries.forEach(c);
    }

    public int getLineHeight() {
        return renderer.getTextHeight();
    }
}
