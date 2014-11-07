package org.solhost.folko.uosl.slclient.views;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Gumps;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.slclient.controllers.MainController;
import org.solhost.folko.uosl.slclient.models.GameState;
import org.solhost.folko.uosl.slclient.models.SLItem;
import org.solhost.folko.uosl.slclient.models.SLMobile;
import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.models.TexturePool;
import org.solhost.folko.uosl.slclient.models.GameState.State;
import org.solhost.folko.uosl.slclient.views.gumps.BaseGump;
import org.solhost.folko.uosl.slclient.views.gumps.ContainerGump;
import org.solhost.folko.uosl.slclient.views.gumps.PaperdollGump;
import org.solhost.folko.uosl.slclient.views.gumps.BaseGump.GumpPart;
import org.solhost.folko.uosl.slclient.views.util.InputGump;
import org.solhost.folko.uosl.slclient.views.util.InputManager;
import org.solhost.folko.uosl.slclient.views.util.LandRenderer;
import org.solhost.folko.uosl.slclient.views.util.PickList;
import org.solhost.folko.uosl.slclient.views.util.ShaderProgram;
import org.solhost.folko.uosl.slclient.views.util.StaticRenderer;
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
    public static final float GRID_DIAMETER = 42.0f;
    public static final float GRID_EDGE     = GRID_DIAMETER / (float) Math.sqrt(2);
    public static final float PROJECTION_CONSTANT = 4.0f;

    private final Canvas glCanvas;

    private final MainController mainController;
    private final GameState game;

    // Client data
    private final SLTiles tiles;
    private final SLArt art;

    // Text stuff
    private final InputGump inputGump;
    private final TextLog textLog;
    private final Object sysMessageEntry;

    // Gumps
    private final List<BaseGump> openGumps;
    private Object dragObject;
    private Point dragOffset;

    // OpenGL stuff
    private final LandRenderer landRenderer;
    private final StaticRenderer staticRenderer;
    private int frameRateLimit;
    private Transform projection, view, model;
    private ShaderProgram shader;
    private Integer vaoID, vboID, eboID;
    private int zOffsetLocation, matLocation, texTypeLocation, selectionIDLocation;
    private float zoom = 1.0f;

    // Input helpers
    private final InputManager input;
    private final PickList pickList;
    private final IntBuffer pickBuffer;

    // Animations
    private int animFrameCounter;
    private final int animDelay = 100;
    private long nextAnimFrameIncrease = animDelay;

    private long fpsCounter, lastFPSReport;

    public GameView(MainController mainController) {
        this.mainController = mainController;
        this.glCanvas = new Canvas();
        this.game = mainController.getGameState();
        this.art = SLData.get().getArt();
        this.tiles = SLData.get().getTiles();
        this.inputGump = new InputGump();
        this.textLog = new TextLog();
        this.sysMessageEntry = new Object();
        this.pickBuffer = BufferUtils.createIntBuffer(1);
        this.pickList = new PickList();
        this.input = new InputManager();
        this.openGumps = new LinkedList<>();
        this.landRenderer = new LandRenderer();
        this.staticRenderer = new StaticRenderer();

        setLayout(new BorderLayout());
        glCanvas.enableInputMethods(true);
        add(glCanvas, BorderLayout.CENTER);

        glCanvas.addMouseListener(input);
        glCanvas.addMouseMotionListener(input);
        glCanvas.addKeyListener(input);

        projection = new Transform();
    }

    public void init() throws Exception {
        try {
            System.setProperty("org.lwjgl.opengl.Display.noinput", "true");
            PixelFormat pixFormat = new PixelFormat();
            ContextAttribs contextAttribs = new ContextAttribs(3, 2)
                .withForwardCompatible(true)
                .withProfileCore(true);
            Display.setResizable(true);
            Display.setParent(glCanvas);
            Display.create(pixFormat, contextAttribs);
            // TODO: Find the reason why this hack is necessary and fix it
            // Without this, the menu bar will be below the canvas and isMouseInsideWindow
            // will return false unless it left and re-entered the frame once
            SwingUtilities.invokeLater(() -> {
                Component root = SwingUtilities.getRoot(glCanvas);
                root.setSize(root.getWidth(), root.getHeight() + 1);
                root.setSize(root.getWidth(), root.getHeight() - 1);
            });
            initGL();
        } catch (LWJGLException e) {
            log.log(Level.SEVERE, "Couldn't create display: " + e.getMessage(), e);
            throw e;
        }

        lastFPSReport = game.getTimeMillis();
    }

    private void initGL() throws Exception {
        glClear(GL_COLOR_BUFFER_BIT);
        TexturePool.load();
        landRenderer.init();
        staticRenderer.init();

        shader = new ShaderProgram();
        try {
            shader.setVertexShader(Paths.get("shaders", "tile.vert"));
            shader.setFragmentShader(Paths.get("shaders", "tile.frag"));
            shader.link();
            shader.setUniformInt(shader.getUniformLocation("tex"), 0);
            shader.bind();
            shader.unbind();

            matLocation = shader.getUniformLocation("mat");
            zOffsetLocation = shader.getUniformLocation("zOffsets");
            texTypeLocation = shader.getUniformLocation("textureType");
            selectionIDLocation = shader.getUniformLocation("selectionID");
        } catch (Exception e) {
            shader.dispose();
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

    public void render() {
        // rendering will fill the back buffer, thus invalidating the select-frame
        pickList.setValid(false);

        glClear(GL_COLOR_BUFFER_BIT /* | GL_DEPTH_BUFFER_BIT */);

        if(game.getState() == State.LOGGED_IN) {
            // only render when logged in
            renderLand();
            renderGameScene(false);
            renderGumps(false);
            renderText();
        }

        calculateFPS();
        Display.update();

        if(Display.wasResized()) {
            onResize();
        }
    }

    private void renderLand() {
        int centerX = game.getPlayerLocation().getX();
        int centerY = game.getPlayerLocation().getY();
        int centerZ = game.getPlayerLocation().getZ();
        int radius = game.getUpdateRange();

        view = Transform.UO(GRID_DIAMETER, PROJECTION_CONSTANT);
        view.translate(-centerX, -centerY, -centerZ);
        landRenderer.render(projection, view, game.getPlayerLocation(), radius);
    }

    private void calculateFPS() {
        // update FPS stats each second
        if(game.getTimeMillis() - lastFPSReport > 1000) {
            mainController.onReportFPS(fpsCounter);
            fpsCounter = 0;
            lastFPSReport += 1000;
        }
        fpsCounter++;
    }

    private void renderGameScene(boolean selectMode) {
        int centerX = game.getPlayerLocation().getX();
        int centerY = game.getPlayerLocation().getY();
        int centerZ = game.getPlayerLocation().getZ();
        int radius = game.getUpdateRange();

        view = Transform.UO(GRID_DIAMETER, PROJECTION_CONSTANT);
        view.translate(-centerX, -centerY, -centerZ);

        staticRenderer.setTransformations(projection, view);

        // check if an item is on top of us (in which case we won't draw items >= that height)
        int playerZ = game.getPlayerLocation().getZ();
        boolean playerCovered = game.getObjectsAt(game.getPlayerLocation())
                                    .anyMatch((obj) -> obj.getLocation().getZ() > playerZ);

        for(int y = centerY - radius; y < centerY + radius; y++) {
            for(int x = centerX - radius; x < centerX + radius; x++) {
                // don't attempt to draw anything else at invalid locations
                if(x < 0 || x >= SLMap.MAP_WIDTH || y < 0 || y >= SLMap.MAP_HEIGHT) {
                    continue;
                }

                Predicate<SLObject> zFilter = (obj) -> {
                    if(playerCovered && obj instanceof SLItem) {
                        boolean isAbove = obj.getLocation().getZ() > playerZ + 2 * SLData.CHARACHTER_HEIGHT;
                        boolean isRoof = ((SLItem) obj).getTileInfo().isRoof();
                        return !isAbove && !isRoof;
                    }
                    return true;
                };

                // now draw items and mobiles so that they cover the land
                Point2D point = new Point2D(x, y);
                game.getObjectsAt(point)
                    .filter(zFilter)
                    .sorted(this::staticPaintOrderCompare)
                    .forEach((obj) ->
                {
                    int selectionID = 0;
                    if(selectMode) {
                        selectionID =  pickList.enter(obj);
                    }
                    if(obj instanceof SLMobile) {
                        drawMobile((SLMobile) obj, selectionID);
                    } else if(obj instanceof SLItem) {
                        staticRenderer.renderStaticInGame(obj.getGraphic(), obj.getLocation(), selectionID);
                    }
                 });
            }
        }
    }

    private void renderText() {
        shader.bind();
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);

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
                        int yPos = (entries.size() + 1) * inputGump.getTextHeight() - yOff;
                        drawTextAtScreenPosition(entry.texture, 5, getHeight() - yPos - 5, false);
                    }
                    yOff += textLog.getLineHeight();
                }
            }
        });
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.unbind();
    }

    private void drawTextAtGamePosition(Texture text, Point3D where, int yOffset, boolean centered) {
        int x = where.getX();
        int y = where.getY();
        int z = where.getZ();

        shader.setUniformInt(texTypeLocation, 1);
        shader.setUniformFloat(zOffsetLocation, 0, 0, 0, 0);
        text.bind(0);

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
        text.bind(0);

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

    private void drawMobile(SLMobile mobile, int selectionId) {
        boolean fighting = false;
        int x = mobile.getLocation().getX();
        int y = mobile.getLocation().getY();
        int z = mobile.getLocation().getZ();
        int graphic = mobile.getGraphic();
        Direction facing = mobile.getFacing();

        shader.bind();
        shader.setUniformInt(selectionIDLocation, selectionId);

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

    private void drawGumpPart(GumpPart part) {
        part.texture.bind(0);
        shader.setUniformFloat(zOffsetLocation, 0, 0, 0, 0);
        shader.setUniformInt(texTypeLocation, 2);

        Transform textureProjection = new Transform(projection);
        textureProjection.scale(1 / zoom, 1 / zoom, 1);

        Transform view = new Transform();
        view.translate(-Display.getWidth() / 2.0f, -Display.getHeight() / 2.0f, 0);

        model.reset();
        model.translate(part.owner.getPosition().x + part.relativePosition.x,
                part.owner.getPosition().y + part.relativePosition.y, 0);
        model.scale(part.texture.getWidth(), part.texture.getHeight(), 1f);

        shader.setUniformMatrix(matLocation, model.combine(view).combine(textureProjection));
        glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, 0);
    }

    private void drawAnimationFrame(MobileAnimation animation, int x, int y, int z) {
        int numFrames = animation.frames.size();
        Texture texture = TexturePool.getAnimationFrame(animation.frames.get(animFrameCounter % numFrames));
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        texture.bind(0);
        shader.bind();
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
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.unbind();
    }

    private void renderGumps(boolean selectMode) {
        shader.bind();
        if(!selectMode) {
            shader.setUniformInt(selectionIDLocation, 0);
        }
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        for(BaseGump gump : openGumps) {
            for(GumpPart part : gump.render()) {
                if(selectMode) {
                    shader.setUniformInt(selectionIDLocation, pickList.enter(part));
                }
                drawGumpPart(part);
            }
        }
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.unbind();
    }

    public void pause() {
        if(frameRateLimit > 0) {
            Display.sync(frameRateLimit);
        }
    }

    public void update(long elapsedMillis) {
        nextAnimFrameIncrease -= elapsedMillis;
        if(nextAnimFrameIncrease < 0) {
            animFrameCounter++;
            nextAnimFrameIncrease = animDelay;
        }

        for(Iterator<BaseGump> it = openGumps.iterator(); it.hasNext(); ) {
            BaseGump gump = it.next();
            if(gump.wantsClose()) {
                it.remove();
            }
        }

        textLog.update(elapsedMillis);
        handleInput();
    }

    private void handleInput() {
        handleAsyncInput();
        handleSyncInput();
        handleDragDrop();
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

        // Single clicking is usually done with a delay to distinguish them
        // from double clicks. To get immediate feedback when clicking in gumps,
        // process the click immediately and cancel potential double or single clicks.
        Point peekClick = input.peekNextSingleClick();
        if(peekClick != null) {
            Object obj = getObjectFromWindow(peekClick.x, peekClick.y);
            if(obj instanceof GumpPart) {
                GumpPart part = (GumpPart) obj;
                part.owner.onClick(part);
                input.abortNextSingleClick();
            }
        }

        Point doubleClick = input.pollLastDoubleClick();
        if(doubleClick != null) {
            Object obj = getObjectFromWindow(doubleClick.x, doubleClick.y);
            if(obj instanceof SLObject) {
                mainController.onDoubleClickObject((SLObject) obj);
            }
        }

        Point singleClick = input.pollLastSingleClick();
        if(singleClick != null) {
            Object obj = getObjectFromWindow(singleClick.x, singleClick.y);
            if(obj instanceof SLObject) {
                mainController.onSingleClickObject((SLObject) obj);
            } else if(obj instanceof GumpPart) {
                GumpPart part = (GumpPart) obj;
                part.owner.onClick(part);
            }
        }
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

        if(input.isRightMouseButtonDown()) {
            Point mousePos = getMousePosition();
            if(mousePos != null) {
                Object mouseObj = getObjectFromWindow(mousePos.x, mousePos.y);
                if(mouseObj instanceof GumpPart) {
                    // right click on gump -> close
                    ((GumpPart) mouseObj).owner.close();
                } else {
                    // right click on non-gump -> walking
                    double midX = Display.getWidth() / 2.0;
                    double midY = Display.getHeight() / 2.0;

                    double angle = Math.toDegrees(Math.atan2(mousePos.x - midX, getHeight() - mousePos.y - midY));
                    if(angle < 0) {
                        angle = 360 + angle;
                    }

                    mainController.onRequestMove(Direction.fromAngle(angle));
                }
            }
        }
    }

    private void handleDragDrop() {
        Point dragPoint = input.peekLastDragEvent();
        if(dragPoint != null) {
            if(dragObject == null) {
                // start dragging
                Object obj = getObjectFromWindow(dragPoint.x, dragPoint.y);
                if(obj instanceof SLItem) {
                    beginDragItem((SLItem) obj, dragPoint);
                } else if(obj instanceof GumpPart) {
                    beginDragGump((GumpPart) obj, dragPoint);
                }
            } else {
                // continue dragging
                if(dragObject instanceof SLItem) {
                    dragItem((SLItem) dragObject, dragPoint);
                } else if(dragObject instanceof GumpPart) {
                    dragGump((GumpPart) dragObject, dragPoint);
                }
            }
        } else if(dragObject != null) {
            // stopped dragging
            if(dragObject instanceof SLItem) {
                dropItem((SLItem) dragObject);
            } else if(dragObject instanceof GumpPart) {
                dropGump((GumpPart) dragObject);
            }
        }
    }

    private void beginDragItem(SLItem item, Point point) {
        // TODO
    }

    private void dragItem(SLItem item, Point point) {
        // TODO
    }

    private void dropItem(SLItem item) {
        // TODO
    }

    private void beginDragGump(GumpPart part, Point point) {
        if(part.owner.canDragGumpWithPart(part)) {
            dragObject = part;
            dragOffset = new Point(part.owner.getPosition().x - point.x, part.owner.getPosition().y - point.y);
        }
    }

    private void dragGump(GumpPart part, Point point) {
        part.owner.setPosition(new Point(point.x + dragOffset.x, point.y + dragOffset.y));
    }

    private void dropGump(GumpPart part) {
        dragObject = null;
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

    private Object getObjectFromWindow(int x, int y) {
        if(!pickList.isValid()) {
            // there is no select-frame for the current frame yet, so render one
            pickList.clear();
            renderGameScene(true);
            renderGumps(true);
            pickList.setValid(true);
        }

        glReadPixels(x, getHeight() - y, 1, 1, GL_RGB, GL_UNSIGNED_BYTE, pickBuffer);
        int pickId = pickBuffer.get(0);
        return pickList.get(pickId);
    }

    public void close() {
        landRenderer.dispose();
        staticRenderer.dispose();

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
        //  let background and surface always be overdrawn by non-background
        //  unless the background is above the non-background (in which case the non-background gets overdrawn)
        //  Mobiles don't have a static tile and are never background
        boolean o1IsBackground = !(o1 instanceof SLMobile) && (tiles.getStaticTile(o1.getGraphic()).isBackground() || tiles.getStaticTile(o1.getGraphic()).isSurface());
        boolean o2IsBackground = !(o2 instanceof SLMobile) && (tiles.getStaticTile(o2.getGraphic()).isBackground() || tiles.getStaticTile(o2.getGraphic()).isSurface());

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

    public void setFrameLimit(int limit) {
        frameRateLimit = limit;
    }

    public int getFrameRateLimit() {
        return frameRateLimit;
    }

    public void openGump(SLObject obj, int gumpID) {
        if(gumpID == Gumps.ID_PAPERDOLL) {
            openPaperdoll((SLMobile) obj);
        } else if(Gumps.isContainerGump(gumpID)) {
            openContainer((SLItem) obj, gumpID);
        }
    }

    private boolean isGumpOpen(int gumpID, SLObject obj) {
        for(BaseGump gump : openGumps) {
            if(gump.getObject() == obj && (gumpID == 0 || gump.getGumpID() == gumpID)) {
                return true;
            }
        }
        return false;
    }

    private void openContainer(SLItem obj, int gumpID) {
        if(!isGumpOpen(0, obj)) {
            openGumps.add(new ContainerGump(mainController, obj, gumpID));
        }
    }

    private void openPaperdoll(SLMobile mob) {
        if(!isGumpOpen(Gumps.ID_PAPERDOLL, mob)) {
            openGumps.add(new PaperdollGump(mainController, mob, game.getPlayerSerial() == mob.getSerial()));
        }
    }
}
