package org.solhost.folko.uosl.slclient.views.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLMap;
import org.solhost.folko.uosl.libuosl.data.SLTiles.LandTile;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.slclient.models.TextureAtlas;
import org.solhost.folko.uosl.slclient.models.TexturePool;

public class LandRenderer {
    private static final int SIZEOF_FLOAT = 4;
    private final ShaderProgram shader;
    private TextureAtlas landAtlas, staticAtlas;
    private Point3D center;
    private int radius;
    private Integer vaoID, eboID, vboID, dataID;
    private int locProjectionMatrix, locViewMatrix, locLandTexture, locStaticTexture;

    public LandRenderer() {
        shader = new ShaderProgram();
    }

    public void init() throws IOException {
        landAtlas = TexturePool.getLandAtlas();
        staticAtlas = TexturePool.getStaticAtlas();

        shader.setVertexShader(Paths.get("shaders", "land.vert"));
        shader.setFragmentShader(Paths.get("shaders", "land.frag"));
        shader.link();

        shader.bind();
        shader.setUniformFloat(shader.getUniformLocation("atlasDimensions"),
                landAtlas.getTexture().getWidth(), landAtlas.getTexture().getHeight(),
                staticAtlas.getTexture().getWidth(), staticAtlas.getTexture().getHeight());
        locProjectionMatrix = shader.getUniformLocation("projectionMatrix");
        locViewMatrix = shader.getUniformLocation("viewMatrix");
        locLandTexture = shader.getUniformLocation("landTexture");
        locStaticTexture = shader.getUniformLocation("staticTexture");
        shader.unbind();

        FloatBuffer vertices = BufferUtils.createFloatBuffer(8);
        vertices.put(new float[] {
                0, 0, // left bottom
                0, 1, // left top
                1, 1, // right top
                1, 0, // right bottom
        });
        vertices.rewind();

        ShortBuffer elements = BufferUtils.createShortBuffer(4);
        elements.put(new short[] {
                0, 1, 3, 2
        });
        elements.rewind();

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);
            vboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

            eboID = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, GL_STATIC_DRAW);
        glBindVertexArray(0);
    }

    private void updateBuffer() {
        int entrySize = 11;
        int centerX = center.getX();
        int centerY = center.getY();

        FloatBuffer data = BufferUtils.createFloatBuffer(2 * radius * 2 * radius * entrySize);
        for(int x = centerX - radius; x < centerX + radius; x++) {
            for(int y = centerY - radius; y < centerY + radius; y++) {
                Point3D point;
                Rectangle texRect;
                int selfZ = 0, eastZ = 0, southZ = 0, southEastZ = 0;
                float type;

                if(x < 0 || x >= SLMap.MAP_WIDTH || y < 0 || y >= SLMap.MAP_HEIGHT) {
                    type = 0.0f;
                    texRect = TexturePool.getLandAtlas().getEntry(1); // void like in real client
                } else {
                    boolean shouldProject = false, canProject = false;
                    point = new Point3D(x, y, getZ(x, y));
                    int landID = SLData.get().getMap().getTextureID(point);
                    LandTile landTile = SLData.get().getTiles().getLandTile(landID);
                    selfZ = point.getZ();
                    eastZ = getZ(x + 1, y);
                    southZ = getZ(x, y + 1);
                    southEastZ = getZ(x + 1, y + 1);
                    shouldProject = (selfZ != eastZ) || (selfZ != southZ) || (selfZ != southEastZ);
                    canProject = (landTile != null && landTile.textureID != 0);
                    if(shouldProject && canProject) {
                        texRect = TexturePool.getStaticAtlas().getEntry(landTile.textureID);
                        type = 1.0f;
                    } else {
                        texRect = TexturePool.getLandAtlas().getEntry(landID);
                        type = 0.0f;
                    }
                }

                if(texRect == null) {
                    // there are some land tiles that reference an invalid textureID,
                    // display the "unused" tile in such situations
                    type = 0.0f;
                    texRect = TexturePool.getLandAtlas().getEntry(0);
                }

                data.put(new float[] {
                        type,
                        x, y,
                        selfZ, southZ, southEastZ, eastZ,
                        texRect.x, texRect.y, texRect.width, texRect.height
                });
            }
        }
        data.rewind();

        if(dataID != null) {
            glDeleteBuffers(dataID);
            dataID = null;
        }

        glBindVertexArray(vaoID);
        dataID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, dataID);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 1, GL_FLOAT, false, entrySize * SIZEOF_FLOAT, 0 * SIZEOF_FLOAT);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, entrySize * SIZEOF_FLOAT, 1 * SIZEOF_FLOAT);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, entrySize * SIZEOF_FLOAT, 3 * SIZEOF_FLOAT);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, entrySize * SIZEOF_FLOAT, 7 * SIZEOF_FLOAT);

        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glVertexAttribDivisor(3, 1);
        glVertexAttribDivisor(4, 1);
        glBindVertexArray(0);
    }

    private int getZ(int x, int y) {
        return SLData.get().getMap().getTileElevation(new Point2D(x, y));
    }

    public void render(Transform projection, Transform view, Point3D pos, int radius) {
        if(center == null || !center.equals(pos) || this.radius != radius) {
            this.center = pos;
            this.radius = radius;
            updateBuffer();
        }
        shader.bind();
        shader.setUniformMatrix(locProjectionMatrix, projection);
        shader.setUniformMatrix(locViewMatrix, view);
        shader.setUniformInt(locLandTexture, 0);
        shader.setUniformInt(locStaticTexture, 1);

        TexturePool.getLandAtlas().getTexture().bind(0);
        TexturePool.getStaticAtlas().getTexture().bind(1);

        glBindVertexArray(vaoID);
        glDrawElementsInstanced(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0, 2 * radius * 2 * radius);
        glBindVertexArray(0);
        shader.unbind();
    }

    public void dispose() {
        if(dataID != null) {
            glDeleteBuffers(dataID);
            dataID = null;
        }

        if(vboID != null) {
            glDeleteBuffers(vboID);
            vboID = null;
        }

        if(eboID != null) {
            glDeleteBuffers(eboID);
            eboID = null;
        }

        if(vaoID != null) {
            glBindVertexArray(0);
            glDeleteVertexArrays(vaoID);
            vaoID = null;
        }
    }
}
