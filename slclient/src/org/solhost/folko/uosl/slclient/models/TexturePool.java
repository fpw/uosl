package org.solhost.folko.uosl.slclient.models;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.data.SLArt;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLArt.ArtEntry;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.slclient.views.util.Texture;

public class TexturePool {
    private static final Logger log = Logger.getLogger("slclient.texturepool");
    private static Texture[] landTextures;
    private static Texture[] staticTextures;
    private static Texture[] animationFrames;
    private static Map<Integer, Texture> gumps;

    private TexturePool() {
    }

    public static void load() {
        landTextures = new Texture[SLArt.NUM_LAND_ARTS];
        staticTextures = new Texture[SLArt.NUM_STATIC_ARTS];
        animationFrames = new Texture[SLArt.NUM_ANIMATION_ARTS];
        gumps = new HashMap<>();

        SLArt art = SLData.get().getArt();
        log.fine("Loading textures into GPU...");

        for(int i = 0; i < SLArt.NUM_LAND_ARTS; i++) {
            ArtEntry entry = art.getLandArt(i);
            if(entry != null && entry.image != null) {
                landTextures[i] = new Texture(entry.image);
            }
        }

        for(int i = 0; i < SLArt.NUM_STATIC_ARTS; i++) {
            StaticTile tile = SLData.get().getTiles().getStaticTile(i);
            if(tile == null) {
                continue;
            }
            boolean translucent = (tile.flags & StaticTile.FLAG_TRANSLUCENT) != 0;
            ArtEntry entry = art.getStaticArt(i, translucent);
            if(entry != null && entry.image != null) {
                staticTextures[i] = new Texture(entry.image);
            }
        }

        for(int i = 0x4000; i < 0x4000 + SLArt.NUM_ANIMATION_ARTS; i++) {
            ArtEntry entry = art.getStaticArt(i, false);
            if(entry != null && entry.image != null) {
                animationFrames[i - 0x4000] = new Texture(entry.image);
            }
        }

        for(int id : SLData.get().getGumps().getAllGumpIDs()) {
            gumps.put(id, new Texture(SLData.get().getGumps().getGump(id).image));
        }
        log.fine("Done loading textures");
    }

    public static Texture getLandTexture(int id) {
        return landTextures[id];
    }

    public static Texture getStaticTexture(int id) {
        return staticTextures[id];
    }

    public static Texture getAnimationFrame(int id) {
        return animationFrames[id - 0x4000];
    }

    public static Texture getGumpTexture(int id) {
        return gumps.get(id);
    }
}
