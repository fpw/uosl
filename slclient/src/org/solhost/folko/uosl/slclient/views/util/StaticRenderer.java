package org.solhost.folko.uosl.slclient.views.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.slclient.models.TextureAtlas;
import org.solhost.folko.uosl.slclient.models.TexturePool;
import org.solhost.folko.uosl.slclient.views.GameView;

public class StaticRenderer {
    private static final Logger log = Logger.getLogger("slclient.staticrenderer");
    private final ShaderProgram shader;
    private final Transform projectionMat, modelMat;
    private Transform baseProjection, viewMat;
    private TextureAtlas staticAtlas;
    private Integer vaoID, vboID, eboID;
    private int locSelectionID, locMat, locTexRect;

    public StaticRenderer() {
        shader = new ShaderProgram();
        projectionMat = new Transform();
        modelMat = new Transform();
    }

    public void init() throws IOException {
        shader.setVertexShader(Paths.get("shaders", "static.vert"));
        shader.setFragmentShader(Paths.get("shaders", "static.frag"));
        shader.link();

        staticAtlas = TexturePool.getStaticAtlas();
        shader.bind();
        shader.setUniformInt(shader.getUniformLocation("tex"), 0);
        shader.setUniformFloat(shader.getUniformLocation("atlasDimensions"),
                staticAtlas.getTexture().getWidth(), staticAtlas.getTexture().getHeight());
        locSelectionID = shader.getUniformLocation("selectionID");
        locMat = shader.getUniformLocation("mat");
        locTexRect = shader.getUniformLocation("texRect");
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

    public void setTransformations(Transform projection, Transform view) {
        this.baseProjection = projection;
        this.viewMat = view;
    }

    public void renderStaticInGame(int staticID, Point3D location, int selectionID) {
        Rectangle texRect = staticAtlas.getEntry(staticID);
        if(texRect == null) {
            log.finer("No texture for item with graphic: " + staticID);
            return;
        }

        shader.bind();
        glBindVertexArray(vaoID);
        staticAtlas.getTexture().bind(0);
        shader.setUniformInt(locSelectionID, selectionID);
        shader.setUniformFloat(locTexRect, texRect.x, texRect.y, texRect.width, texRect.height);

        projectionMat.reset(baseProjection);
        projectionMat.translate(-texRect.width / 2.0f, GameView.GRID_DIAMETER - texRect.height, 0.0f);

        modelMat.reset();
        modelMat.translate(location.getX(), location.getY(), location.getZ());
        modelMat.rotate(0, 0, -45);
        modelMat.scale(texRect.width / GameView.GRID_EDGE, texRect.height / GameView.GRID_EDGE, 1f);

        shader.setUniformMatrix(locMat, modelMat.combine(viewMat).combine(projectionMat));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);
        glBindVertexArray(0);
        shader.unbind();
    }

    public void dispose() {
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
