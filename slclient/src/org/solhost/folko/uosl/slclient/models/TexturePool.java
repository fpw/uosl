package org.solhost.folko.uosl.slclient.models;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.lwjgl.opengl.GL11;
import org.solhost.folko.uosl.libuosl.data.SLArt;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLArt.ArtEntry;
import org.solhost.folko.uosl.libuosl.data.SLMap;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.slclient.views.util.Texture;

public class TexturePool {
    private static final Logger log = Logger.getLogger("slclient.texturepool");
    private static TextureAtlas landAtlas, staticAtlas;
    private static Texture[] animationFrames;
    private static Map<Integer, Texture> gumps;

    private TexturePool() {
    }

    private static void createLandAtlas() {
        SLArt art = SLData.get().getArt();
        SLMap map = SLData.get().getMap();

        // we need tile 0 to display invalid tiles, so always load it
        landAtlas.addImage(art.getLandArt(0).image, 0);

        // but otherweise only put tiles into the atlas that are actually in use by the map
        for(int x = 0; x < SLMap.MAP_WIDTH; x++) {
            for(int y = 0; y < SLMap.MAP_HEIGHT; y++) {
                int tileId = map.getTextureID(new Point2D(x, y));
                if(landAtlas.hasEntry(tileId)) {
                    continue;
                }
                ArtEntry entry = art.getLandArt(tileId);
                if(entry != null) {
                    landAtlas.addImage(entry.image, tileId);
                }
            }
        }
        landAtlas.generateTextureAndFreeImage();
    }

    private static void createStaticAtlas() {
        SLArt art = SLData.get().getArt();

        for(int i = 0; i < SLArt.NUM_STATIC_ARTS; i++) {
            StaticTile tile = SLData.get().getTiles().getStaticTile(i);
            if(tile == null) {
                continue;
            }
            boolean translucent = (tile.flags & StaticTile.FLAG_TRANSLUCENT) != 0;
            ArtEntry entry = art.getStaticArt(i, translucent);
            if(entry != null && entry.image != null) {
                staticAtlas.addImage(entry.image, i);
            }
        }
        staticAtlas.generateTextureAndFreeImage();
    }

    public static void load() {
        SLArt art = SLData.get().getArt();
        animationFrames = new Texture[SLArt.NUM_ANIMATION_ARTS];
        gumps = new HashMap<>();

        int maxWidth = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        int maxHeight = maxWidth;

        log.fine(String.format("Loading textures into GPU, max atlas size %dx%d...", maxWidth, maxHeight));

        landAtlas = new TextureAtlas(maxWidth, maxHeight);
        staticAtlas = new TextureAtlas(maxWidth, maxHeight);

        createLandAtlas();
        createStaticAtlas();

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

    public static TextureAtlas getLandAtlas() {
        return landAtlas;
    }

    public static TextureAtlas getStaticAtlas() {
        return staticAtlas;
    }

    public static Texture getAnimationFrame(int id) {
        return animationFrames[id - 0x4000];
    }

    public static Texture getGumpTexture(int id) {
        return gumps.get(id);
    }
}
