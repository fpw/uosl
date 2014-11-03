package org.solhost.folko.uosl.slclient.views;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.solhost.folko.uosl.libuosl.data.SLArt;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLMap;
import org.solhost.folko.uosl.libuosl.data.SLTiles;
import org.solhost.folko.uosl.libuosl.data.SLArt.ArtEntry;
import org.solhost.folko.uosl.libuosl.data.SLArt.MobileAnimation;
import org.solhost.folko.uosl.libuosl.data.SLTiles.LandTile;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.slclient.controllers.MainController;
import org.solhost.folko.uosl.slclient.models.GameState;
import org.solhost.folko.uosl.slclient.models.SLItem;
import org.solhost.folko.uosl.slclient.models.SLMobile;
import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.models.TexturePool;
import org.solhost.folko.uosl.slclient.models.GameState.State;
import org.solhost.folko.uosl.slclient.views.util.InputGump;
import org.solhost.folko.uosl.slclient.views.util.InputManager;
import org.solhost.folko.uosl.slclient.views.util.PickList;
import org.solhost.folko.uosl.slclient.views.util.ShaderProgram;
import org.solhost.folko.uosl.slclient.views.util.TextLog;
import org.solhost.folko.uosl.slclient.views.util.Texture;
import org.solhost.folko.uosl.slclient.views.util.Transform;
import org.solhost.folko.uosl.slclient.views.util.TextLog.TextEntry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GameView extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger("slclient.gameview");
    private static final float GRID_DIAMETER = 42.0f;
    private static final float GRID_EDGE     = GRID_DIAMETER / (float) Math.sqrt(2);
    private static final float PROJECTION_CONSTANT = 4.0f;
    private static final int FPS = 20;

    private final SLMap map;
    private final SLTiles tiles;
    private final SLArt art;

    private final InputGump inputGump;
    private final TextLog textLog;
    private final Object sysMessageEntry;

    private final MainController mainController;
    private final GameState game;

    private Transform projection, view, model;
    private ShaderProgram shader;
    private Integer vaoID, vboID, eboID;
    private int texLocation, zOffsetLocation, matLocation, texTypeLocation, selectionIDLocation;

    private final InputManager input;
    private final PickList pickList;
    private final IntBuffer pickBuffer;

    private float zoom = 1.0f;

    private int animFrameCounter;
    private final int animDelay = 100;
    private long nextAnimFrameIncrease = animDelay;

    private long fpsCounter, lastFPSReport;

    private final Canvas glCanvas;

    public GameView(MainController mainController) {
        this.mainController = mainController;
        this.game = mainController.getGameState();
        this.map = SLData.get().getMap();
        this.art = SLData.get().getArt();
        this.tiles = SLData.get().getTiles();
        this.inputGump = new InputGump();
        this.textLog = new TextLog();
        this.sysMessageEntry = new Object();
        this.pickBuffer = BufferUtils.createIntBuffer(1);
        this.pickList = new PickList();
        this.glCanvas = new Canvas();
        this.input = new InputManager();

        setLayout(new BorderLayout());
        add(glCanvas, BorderLayout.CENTER);

        glCanvas.addMouseListener(input);
        glCanvas.addKeyListener(input);

        try {
            Display.setParent(glCanvas);
        } catch (LWJGLException e) {
            log.log(Level.SEVERE, "Couldn't set display mode: " + e.getMessage(), e);
            mainController.onGameError("Couldn't set display mode: " + e.getMessage());
        }

        projection = new Transform();
    }

    public void init() throws Exception {
        try {
            PixelFormat pixFormat = new PixelFormat();
            ContextAttribs contextAttribs = new ContextAttribs(3, 2)
                .withForwardCompatible(true)
                .withProfileCore(true);
            Display.create(pixFormat, contextAttribs);
            initGL();
            glCanvas.requestFocus();
        } catch (LWJGLException e) {
            log.log(Level.SEVERE, "Couldn't create display: " + e.getMessage(), e);
            throw e;
        }

        lastFPSReport = game.getTimeMillis();
    }

    public void render() {
        // rendering will fill the back buffer, thus invalidating the select-frame
        pickList.setValid(false);

        if(game.getState() == State.LOGGED_IN) {
            // only render when logged in
            renderGameScene(false);
        }

        updateFPS();
        Display.update();

        if(Display.wasResized()) {
            onResize();
        }
    }

    public void pause() {
        Display.sync(FPS);
    }

    private void updateFPS() {
        // update FPS stats each second
        if(game.getTimeMillis() - lastFPSReport > 1000) {
            mainController.onReportFPS(fpsCounter);
            fpsCounter = 0;
            lastFPSReport += 1000;
        }
        fpsCounter++;
    }

    public void update(long elapsedMillis) {
        nextAnimFrameIncrease -= elapsedMillis;
        if(nextAnimFrameIncrease < 0) {
            animFrameCounter++;
            nextAnimFrameIncrease = animDelay;
        }
        textLog.update(elapsedMillis);
        handleInput();
    }

    private void handleInput() {
        handleAsyncInput();
        handleSyncInput();
    }

    private void handleAsyncInput() {
        String typed = input.pollTypedKeys();
        for(char c : typed.toCharArray()) {
            if((c >= 32 && c < 128) || c == '\b') {
                // printable ASCII and DEL (127)
                inputGump.feedCharacter(c);
            } else if(c == '\n' || c == '\r') {
                String text = inputGump.getAndReset();
                mainController.onTextEntered(text);
            }
        }

        Point doubleClick = input.pollLastDoubleClick();
        if(doubleClick != null) {
            SLObject obj = getMouseObject(doubleClick.x, getHeight() - doubleClick.y);
            if(obj != null) {
                handleDoubleClick(obj);
            }
        }

        Point singleClick = input.pollLastSingleClick();
        if(singleClick != null) {
            SLObject obj = getMouseObject(singleClick.x, getHeight() - singleClick.y);
            if(obj != null) {
                handleSingleClick(obj);
            }
        }

    }

    private void handleSingleClick(SLObject obj) {
        if(obj instanceof SLItem) {
            String name = obj.getName();
            if(name.length() > 0) {
                textLog.addEntry(obj, name, Color.WHITE);
            }
        } else if(obj instanceof SLMobile) {
            mainController.onSingleClickMobile((SLMobile) obj);
        }
    }

    private void handleDoubleClick(SLObject obj) {
        mainController.onDoubleClickObject(obj);
    }

    private void handleSyncInput() {
        if(input.isKeyDown(KeyEvent.VK_DOWN)) {
            mainController.onRequestMove(Direction.SOUTH_EAST);
        } else if(input.isKeyDown(KeyEvent.VK_UP)) {
            mainController.onRequestMove(Direction.NORTH_WEST);
        } else if(input.isKeyDown(KeyEvent.VK_LEFT)) {
            mainController.onRequestMove(Direction.SOUTH_WEST);
        } else if(input.isKeyDown(KeyEvent.VK_RIGHT)) {
            mainController.onRequestMove(Direction.NORTH_EAST);
        }

        Point mousePos = glCanvas.getMousePosition();
        if(input.isRightMouseButtonDown() && mousePos != null) {
            double midX = Display.getWidth() / 2.0;
            double midY = Display.getHeight() / 2.0;

            double angle = Math.toDegrees(Math.atan2(mousePos.x - midX, getHeight() - mousePos.y - midY));
            if(angle < 0) {
                angle = 360 + angle;
            }

            mainController.onRequestMove(Direction.fromAngle(angle));
        }
    }

    private void initGL() throws Exception {
        glClear(GL_COLOR_BUFFER_BIT);

        shader = new ShaderProgram();
        try {
            shader.setVertexShader(Paths.get("shaders", "tile.vert"));
            shader.setFragmentShader(Paths.get("shaders", "tile.frag"));
            shader.link();
            matLocation = shader.getUniformLocation("mat");
            zOffsetLocation = shader.getUniformLocation("zOffsets");
            texLocation = shader.getUniformLocation("tex");
            texTypeLocation = shader.getUniformLocation("textureType");
            selectionIDLocation = shader.getUniformLocation("selectionID");
        } catch (Exception e) {
            shader.dispose();
            log.log(Level.SEVERE, "Couldn't load shader: " + e.getMessage(), e);
            throw e;
        }

        // glDepthFunc(GL_LEQUAL);
        // glEnable(GL_DEPTH_TEST);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);

        model = new Transform();

        onResize();

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
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

            eboID = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, GL_STATIC_DRAW);
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void onResize() {
        int width = Display.getWidth();
        int height = Display.getHeight();
        glViewport(0, 0, width, height);
        projection = Transform.orthographic(-width / 2.0f, -height / 2.0f, width / 2.0f, height / 2.0f, 128f, -128f);
        projection.scale(zoom, zoom, 1);
        onZoom(1.0f);
    }

    private void onZoom(float f) {
        int width = Display.getWidth();
        int height = Display.getHeight();

        projection.scale(f, f, 1);
        zoom *= f;
        float radiusX = (width / zoom / GRID_DIAMETER);
        float radiusY = (height / zoom / GRID_DIAMETER);
        int radius = (int) (Math.max(radiusX, radiusY) + 0.5);
        mainController.onUpdateRangeChange(radius);
    }

    private int getZ(int x, int y) {
        return map.getTileElevation(new Point2D(x, y));
    }

    private void renderGameScene(boolean selectMode) {
        int centerX = game.getPlayerLocation().getX();
        int centerY = game.getPlayerLocation().getY();
        int centerZ = game.getPlayerLocation().getZ();
        int radius = game.getUpdateRange();

        glClear(GL_COLOR_BUFFER_BIT /* | GL_DEPTH_BUFFER_BIT */);

        shader.bind();
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);

        view = Transform.UO(GRID_DIAMETER, PROJECTION_CONSTANT);
        view.translate(-centerX, -centerY, -centerZ);

        if(!selectMode) {
            shader.setUniformInt(selectionIDLocation, 0);
        }
        shader.setUniformFloat(texLocation, 0);

        for(int y = centerY - radius; y < centerY + radius; y++) {
            for(int x = centerX - radius; x < centerX + radius; x++) {
                // draw land even at invalid locations: will draw void like real client
                if(!selectMode) {
                    // but only draw when doing real-rendering and not select-rendering
                    // because land tiles will always be in background and are never
                    // selectable
                    drawLand(x, y);
                }

                // but don't attempt to draw anything else at invalid locations
                if(x < 0 || x >= SLMap.MAP_WIDTH || y < 0 || y >= SLMap.MAP_HEIGHT) {
                    continue;
                }

                // now draw items and mobiles so that they cover the land
                Point2D point = new Point2D(x, y);
                game.getObjectsAt(point)
                    .sorted(this::staticPaintOrderCompare)
                    .forEach((obj) ->
                {
                    if(selectMode) {
                        shader.setUniformInt(selectionIDLocation, pickList.enter(obj));
                    }
                    if(obj instanceof SLMobile) {
                        drawMobile((SLMobile) obj);
                    } else if(obj instanceof SLItem) {
                        drawItem((SLItem) obj);
                    }
                 });
            }
        }

        if(!selectMode) {
            // text input line
            drawTextAtScreenPosition(inputGump.getTexture(), 5, Display.getHeight() - inputGump.getTextHeight() - 5, false);

            // draw all other visible text
            textLog.visitEntries((aboveWhom, entries) -> {
                int yOff = 0;
                for(TextEntry entry : entries) {
                    if(entry.texture != null) {
                        if(aboveWhom instanceof SLObject) {
                            int graphicHeight = getGraphicHeight((SLObject) aboveWhom);
                            int yPos = graphicHeight + (entries.size() - 1) * textLog.getLineHeight() - yOff;
                            drawTextAtGamePosition(entry.texture, ((SLObject) aboveWhom).getLocation(), yPos, false);
                        } else if(aboveWhom == sysMessageEntry) {
                            drawTextAtScreenPosition(entry.texture, 5, Display.getHeight() - inputGump.getTextHeight() * 5 - yOff, false);
                        }
                        yOff += textLog.getLineHeight();
                    }
                }
            });
        }

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.unbind();
    }

    private int getGraphicHeight(SLObject obj) {
        int staticId = obj.getGraphic();
        if(obj instanceof SLMobile) {
            MobileAnimation anim = art.getAnimationEntry(obj.getGraphic(), ((SLMobile) obj).getFacing(), false);
            if(anim != null && anim.frames.size() > 0) {
                staticId = anim.frames.get(0);
            }
        }
        ArtEntry entry = art.getStaticArt(staticId, false);
        if(entry != null) {
            return entry.image.getHeight();
        } else {
            return 0;
        }
    }

    private SLObject getMouseObject(int x, int y) {
        if(!pickList.isValid()) {
            // there is no select-frame for the current frame yet, so render one
            pickList.clear();
            renderGameScene(true);
            pickList.setValid(true);
        }

        glReadPixels(x, y, 1, 1, GL_RGB, GL_UNSIGNED_BYTE, pickBuffer);
        int pickId = pickBuffer.get(0);
        return pickList.get(pickId);
    }

    private void drawLand(int x, int y) {
        Point3D point;
        boolean shouldProject = false, canProject = false;
        int selfZ = 0, eastZ = 0, southZ = 0, southEastZ = 0;
        Texture texture;

        if(x < 0 || x >= SLMap.MAP_WIDTH || y < 0 || y >= SLMap.MAP_HEIGHT) {
            point = null;
            texture = TexturePool.getLandTexture(1); // VOID texture like in real client
            shader.setUniformInt(texTypeLocation, 0);
        } else {
            point = new Point3D(x, y, getZ(x, y));
            int landID = map.getTextureID(point);
            LandTile landTile = tiles.getLandTile(landID);
            selfZ = point.getZ();
            eastZ = getZ(x + 1, y);
            southZ = getZ(x, y + 1);
            southEastZ = getZ(x + 1, y + 1);
            shouldProject = (selfZ != eastZ) || (selfZ != southZ) || (selfZ != southEastZ);
            canProject = (landTile != null && landTile.textureID != 0);
            if(shouldProject && canProject) {
                texture = TexturePool.getStaticTexture(landTile.textureID);
                shader.setUniformInt(texTypeLocation, 1);
            } else {
                texture = TexturePool.getLandTexture(landID);
                shader.setUniformInt(texTypeLocation, 0);
            }
            if(texture == null) {
                texture = TexturePool.getLandTexture(0);
            }
        }
        texture.bind();
        shader.setUniformFloat(zOffsetLocation, selfZ, southZ, southEastZ, eastZ);

        model.reset();
        model.translate(x, y, 0);
        shader.setUniformMatrix(matLocation, model.combine(view).combine(projection));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);
    }

    private void drawItem(SLItem item) {
        int x = item.getLocation().getX();
        int y = item.getLocation().getY();
        int z = item.getLocation().getZ();

        shader.setUniformInt(texTypeLocation, 1);
        shader.setUniformFloat(zOffsetLocation, 0, 0, 0, 0);
        Texture texture = TexturePool.getStaticTexture(item.getGraphic());
        if(texture == null) {
            log.warning("No texture for item with graphic: " + item.getGraphic());
            return;
        }
        texture.bind();

        Transform textureProjection = new Transform(projection);
        textureProjection.translate(-texture.getWidth() / 2.0f, GRID_DIAMETER - texture.getHeight(), 0);

        model.reset();
        model.translate(x, y, z);
        model.rotate(0, 0, -45);
        model.scale(texture.getWidth() / GRID_EDGE, texture.getHeight() / GRID_EDGE, 1f);
        shader.setUniformMatrix(matLocation, model.combine(view).combine(textureProjection));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);
    }

    private void drawTextAtGamePosition(Texture text, Point3D where, int yOffset, boolean centered) {
        int x = where.getX();
        int y = where.getY();
        int z = where.getZ();

        shader.setUniformInt(texTypeLocation, 1);
        shader.setUniformFloat(zOffsetLocation, 0, 0, 0, 0);
        text.bind();

        Transform textureProjection = new Transform(projection);
        textureProjection.translate(-text.getWidth() / 2.0f, GRID_DIAMETER - text.getHeight() - yOffset, 0);

        model.reset();
        model.translate(x, y, z);
        model.rotate(0, 0, -45);
        model.scale(text.getWidth() / GRID_EDGE, text.getHeight() / GRID_EDGE, 1f);
        shader.setUniformMatrix(matLocation, model.combine(view).combine(textureProjection));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);

    }

    private void drawTextAtScreenPosition(Texture text, int x, int y, boolean centered) {
        shader.setUniformInt(texTypeLocation, 1);
        shader.setUniformFloat(zOffsetLocation, 0, 0, 0, 0);
        text.bind();

        Transform textureProjection = new Transform(projection);
        textureProjection.scale(1 / zoom, 1 / zoom, 1);
        if(centered) {
            textureProjection.translate(-text.getWidth() / 2.0f, 0, 0);
        }
        Transform view = new Transform();
        view.translate(-Display.getWidth() / 2.0f, -Display.getHeight() / 2.0f, 0);

        model.reset();
        model.translate(x, y, 0);
        model.scale(text.getWidth(), text.getHeight(), 1f);
        shader.setUniformMatrix(matLocation, model.combine(view).combine(textureProjection));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);
    }

    private void drawMobile(SLMobile mobile) {
        boolean fighting = false;
        int x = mobile.getLocation().getX();
        int y = mobile.getLocation().getY();
        int z = mobile.getLocation().getZ();
        int graphic = mobile.getGraphic();
        Direction facing = mobile.getFacing();

        // draw character first
        MobileAnimation animation = art.getAnimationEntry(graphic, facing, fighting);
        if(animation == null) {
            log.warning("No animation for mobile " + graphic + " with facing " + facing + ", fight: " + fighting);
            return;
        }
        drawAnimationFrame(animation, x, y, z);

        // then its equipment
        for(SLItem equipItem : mobile.getEquipment()) {
            int id = equipItem.getTileInfo().animationID;
            if(id == 0) {
                // no animation for this equipment, possible for vendors as they
                // carry invisible bags for their wares
                continue;
            }
            animation = art.getAnimationEntry(id, facing, fighting);
            if(animation == null) {
                log.warning("No animation for equipment " + graphic + " with facing " + facing + ", fight: " + fighting);
                continue;
            }
            drawAnimationFrame(animation, x, y, z);
        }
    }

    private void drawAnimationFrame(MobileAnimation animation, int x, int y, int z) {
        int numFrames = animation.frames.size();
        Texture texture = TexturePool.getAnimationFrame(animation.frames.get(animFrameCounter % numFrames));
        texture.bind();
        shader.setUniformInt(texTypeLocation, 1);
        shader.setUniformFloat(zOffsetLocation, 0, 0, 0, 0);
        shader.setUniformInt(texTypeLocation, 2);
        Transform textureProjection = new Transform(projection);
        textureProjection.translate(-texture.getWidth() / 2.0f, GRID_DIAMETER - texture.getHeight(), 0);
        model.reset();
        model.translate(x, y, z);
        model.rotate(0, 0, -45);
        if(animation.needMirror) {
            model.translate(texture.getWidth() / GRID_EDGE / 2.0f, 0, 0);
            model.rotate(0, 180, 0);
            model.translate(-texture.getWidth() / GRID_EDGE / 2.0f, 0, 0);
        }
        model.scale(texture.getWidth() / GRID_EDGE, texture.getHeight() / GRID_EDGE, 1f);
        shader.setUniformMatrix(matLocation, model.combine(view).combine(textureProjection));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);
    }

    public void close() {
        if(shader != null) {
            shader.dispose();
            shader = null;
        }

        if(vaoID != null) {
            glBindVertexArray(0);
            glDeleteVertexArrays(vaoID);
            vaoID = null;
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        if(vboID != null) {
            glDeleteBuffers(vboID);
            vboID = null;
        }

        if(eboID != null) {
            glDeleteBuffers(eboID);
            eboID = null;
        }

        Display.destroy();
    }

    private int staticPaintOrderCompare(SLObject o1, SLObject o2) {
        final int drawO1overO2 = 1, drawO2overO1 = -1;
        int z1 = o1.getLocation().getZ();
        int z2 = o2.getLocation().getZ();

        // Special case:
        //  let background always be overdrawn by non-background
        //  unless the background is above the non-background (in which case the non-background gets overdrawn)
        //  Mobiles don't have a static tile and are never background
        boolean o1IsBackground = !(o1 instanceof SLMobile) && tiles.getStaticTile(o1.getGraphic()).isBackground();
        boolean o2IsBackground = !(o2 instanceof SLMobile) && tiles.getStaticTile(o2.getGraphic()).isBackground();

        if(o1IsBackground && !o2IsBackground) {
            return (z1 > z2) ? drawO1overO2 : drawO2overO1;
        } else if(!o1IsBackground && o2IsBackground) {
            return (z2 > z1) ? drawO2overO1 : drawO1overO2;
        }

        // No background tiles involved, normal z comparison
        // Some cases are still unknown, let serial decide then so there is no z-fight
        if(z1 == z2 && !o1.equals(o2)) {
            return Long.compare(o1.getSerial(), o2.getSerial());
        } else {
            return Integer.compare(z1, z2);
        }
    }

    public void showSysMessage(String text, Color color) {
        textLog.addEntry(sysMessageEntry, text, color);
    }

    public void showTextAbove(SLObject obj, String text, Color color) {
        textLog.addEntry(obj, text, color);
    }
}
