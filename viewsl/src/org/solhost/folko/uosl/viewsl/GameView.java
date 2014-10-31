/*******************************************************************************
 * Copyright (c) 2013, 2014 Folke Will <folke.will@gmail.com>
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.solhost.folko.uosl.libuosl.data.SLArt;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLMap;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.data.SLStatics;
import org.solhost.folko.uosl.libuosl.data.SLTiles;
import org.solhost.folko.uosl.libuosl.data.SLArt.ArtEntry;
import org.solhost.folko.uosl.libuosl.data.SLTiles.LandTile;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.libuosl.util.Pathfinder;

public class GameView extends JPanel {
    private static final long   serialVersionUID    = 1L;
    private static final int    TILE_SIZE           = 42;
    private static final double TARGET_FPS          = 25.0;
    private static final int    PROJECTION_CONSTANT = 4;

    private final Map<Integer, Image> mapTileCache;
    private final Map<Integer, Image> staticTileCache;
    private final Map<Point2D, Image> polygonCache;
    private final SLData data;
    private final SLMap map;
    private final SLArt art;
    private final SLTiles tiles;
    private final SLStatics statics;
    private final Timer redrawTimer;
    private Point3D sceneCenter;
    private long lastRedraw, drawDuration, lastFPSUpdate;
    private double lastFPS;
    private Pathfinder finder;
    private boolean cutOffZ = false, hackMover = true;
    private boolean drawGrid = false, drawLand = true, drawStatics = true;

    // needed for polygon rasterization
    private class RasterInfo {
        int x, y;
        double tx, ty;

        public RasterInfo(int x, int y, double tx, double ty) {
            this.x = x;
            this.y = y;
            this.tx = tx;
            this.ty = ty;
        }
    };

    class RasterQuad {
        RasterInfo[] raster;
        boolean isRegular;
        int minX, minY, maxX, maxY;
        double lightLevel;

        public RasterQuad() {
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;
            raster = new RasterInfo[4];
        }

        public Polygon toPolygon() {
            Polygon res = new Polygon();
            res.addPoint(raster[0].x, raster[0].y);
            res.addPoint(raster[1].x, raster[1].y);
            res.addPoint(raster[2].x, raster[2].y);
            res.addPoint(raster[3].x, raster[3].y);
            return res;
        }
    }

    private RasterInfo[] leftSide, rightSide;

    public GameView(final SLData data) {
        this.data = data;
        this.map = data.getMap();
        this.art = data.getArt();
        this.statics = data.getStatics();
        this.tiles = data.getTiles();
        this.mapTileCache = new HashMap<>();
        this.staticTileCache = new HashMap<>();
        this.polygonCache = new HashMap<>();
        this.sceneCenter = new Point3D(379, 607, 0);
        this.lastRedraw = System.currentTimeMillis();
        this.redrawTimer = new Timer((int) (1000.0 / TARGET_FPS), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });

        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                redrawTimer.stop();
            }

            public void focusGained(FocusEvent e) {
                redrawTimer.start();
            }
        });

        addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) { }
            public void keyReleased(KeyEvent e) { }

            public void keyPressed(KeyEvent e) {
                Direction dir = null;
                switch(e.getKeyCode()) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    dir = Direction.NORTH_WEST;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    dir = Direction.SOUTH_EAST;
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    dir = Direction.SOUTH_WEST;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    dir = Direction.NORTH_EAST;
                    break;
                case KeyEvent.VK_Q:
                case KeyEvent.VK_HOME:
                    dir = Direction.WEST;
                    break;
                case KeyEvent.VK_Y:
                case KeyEvent.VK_END:
                    dir = Direction.SOUTH;
                    break;
                case KeyEvent.VK_E:
                case KeyEvent.VK_PAGE_UP:
                    dir = Direction.NORTH;
                    break;
                case KeyEvent.VK_C:
                case KeyEvent.VK_PAGE_DOWN:
                    dir = Direction.EAST;
                    break;
                case KeyEvent.VK_I:
                    showStatics();
                    break;
                case KeyEvent.VK_R:
                    cutOffZ = !cutOffZ;
                    break;
                case KeyEvent.VK_H:
                    hackMover = !hackMover;
                    break;
                case KeyEvent.VK_1:
                    drawGrid = !drawGrid;
                    break;
                case KeyEvent.VK_2:
                    drawLand = !drawLand;
                    break;
                case KeyEvent.VK_3:
                    drawStatics = !drawStatics;
                    break;
                }
                if(dir != null) {
                    moveCenter(dir);
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Direction dir;
                double rot = e.getPreciseWheelRotation();
                int num = (int) Math.max(1, Math.abs(rot * 0.4));
                if(e.getModifiers() == 0) {
                    if(rot < 0) {
                        dir = Direction.NORTH_WEST;
                    } else {
                        dir = Direction.SOUTH_EAST;
                    }
                } else {
                    if(rot < 0) {
                        dir = Direction.SOUTH_WEST;
                    } else {
                        dir = Direction.NORTH_EAST;
                    }
                }
                for(int i = 0; i < num; i++) {
                    moveCenter(dir);
                }
            }
        });
        redrawTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        int drawDist = Math.max(getWidth() / TILE_SIZE - 1, getHeight() / TILE_SIZE - 1);

        for(int y = sceneCenter.getY() - drawDist; y <= sceneCenter.getY() + drawDist; y++) {
            for(int x = sceneCenter.getX() - drawDist; x <= sceneCenter.getX() + drawDist; x++) {
                if(x >= 0 && x < 1024 && y >= 0 && y < 1024) {
                    Point3D point = new Point3D(x, y, getZ(x, y));
                    if(drawLand) {
                        drawMapTile(g, point);
                    }
                    if(drawGrid) {
                        drawGrid(g, point, Color.blue);
                    }
                    if(drawStatics) {
                        drawStatics(g, point);
                    }
                }
            }
        }

        drawInfo(g);
        drawPath(g);
        markCenter(g);
    }

    protected void markCenter(Graphics g) {
        Point center = project(sceneCenter);
        g.fillOval(center.x - 4, center.y - 4, 8, 8);
    }

    protected void drawGrid(Graphics g, Point3D p, Color color) {
        RasterQuad r = getPointPolygon(p);
        g.setColor(color);
        g.drawPolygon(r.toPolygon());
    }

    private void drawPath(Graphics g) {
        if(finder != null && finder.hasPath()) {
            List<Direction> path = finder.getPath();
            Point3D curLoc = finder.getStart();
            Point lastPoint = project(curLoc), curPoint;
            for(Direction dir : path) {
                Point3D nextLoc = data.getElevatedPoint(curLoc, dir, (p) -> statics.getStatics(p));
                if(nextLoc == null) {
                    System.err.println("invalid path move: " + dir);
                    break;
                }
                curPoint = project(nextLoc);
                g.drawLine(lastPoint.x, lastPoint.y, curPoint.x, curPoint.y);
                lastPoint = curPoint;
                curLoc = nextLoc;
            }
        }
    }

    private void drawInfo(Graphics g) {
        drawDuration = System.currentTimeMillis() - lastRedraw;
        lastRedraw = System.currentTimeMillis();
        if(lastRedraw - lastFPSUpdate > 500) {
            lastFPSUpdate = System.currentTimeMillis();
            lastFPS = 1000.0 / drawDuration;
        }
        String info = String.format("Standing at %4d, %4d, %4d with %.2f FPS",
                sceneCenter.getX(), sceneCenter.getY(), sceneCenter.getZ(), lastFPS);
        g.setColor(Color.red);
        g.drawString(info, 10, 20);

        Point mousePoint = getMousePosition();
        if(mousePoint != null) {
            Point2D igp = projectBack(mousePoint);
            info = String.format("Mouse at %4d, %4d on screen -> %4d, %4d in game",
                    mousePoint.x, mousePoint.y, igp.getX(), igp.getY());
            g.drawString(info, 10, 35);
        }
    }

    private void showStatics() {
        System.out.println("Statics at " + sceneCenter.getX() + ", " + sceneCenter.getY());
        for(SLStatic stat : sortStatics(statics.getStatics(sceneCenter))) {
            StaticTile tile = tiles.getStaticTile(stat.getStaticID());
            String info = String.format("Z = %3d, height = %3d -> 0x%04X 0x%08X (%s)", stat.getLocation().getZ(), tile.height, stat.getStaticID(), tile.flags, tile.name);
            System.out.println(info);
        }
        int landID = map.getTextureID(sceneCenter);
        System.out.println(String.format("Land: %04X, Z: %d", landID, getZ(sceneCenter.getX(), sceneCenter.getY())));
        System.out.println("====");
    }

    private void moveCenter(Direction dir) {
        if(hackMover) {
            Point2D hackDest = sceneCenter.getTranslated(dir);
            sceneCenter = new Point3D(hackDest, 0);
        } else {
            Point3D dest = data.getElevatedPoint(sceneCenter, dir, (point) -> statics.getStatics(point));
            if(dest != null) {
                sceneCenter = dest;
            }
        }
    }

    private void drawMapTile(Graphics g, Point3D pos) {
        int landID = map.getTextureID(pos);
        LandTile landTile = tiles.getLandTile(landID);

        // check whether there is a big tile to project onto irregular polygons
        boolean canProject = landTile.textureID != 0;

        // check whether we actually need to use the projection
        RasterQuad quad = getPointPolygon(pos);
        if(!quad.isRegular && canProject) {
            Image image = getMapTilePolygon(pos, quad, landTile.textureID);
            if(image != null) {
                g.drawImage(image, quad.minX, quad.minY, null);
            }
        } else {
            Point center = project(pos);
            Image image = getMapTileImage(landID);
            if(image != null) {
                g.drawImage(image, center.x - TILE_SIZE / 2, center.y - TILE_SIZE / 2, null);
            }
        }
    }

    private List<SLStatic> sortStatics(List<SLStatic> in) {
        // sort by view order
        Collections.sort(in, new Comparator<SLStatic>() {
            public int compare(SLStatic o1, SLStatic o2) {
                StaticTile tile1 = tiles.getStaticTile(o1.getStaticID());
                StaticTile tile2 = tiles.getStaticTile(o2.getStaticID());
                int z1 = o1.getLocation().getZ();
                int z2 = o2.getLocation().getZ();

                if((tile1.flags & StaticTile.FLAG_BACKGROUND) != 0) {
                    if((tile2.flags & StaticTile.FLAG_BACKGROUND) == 0) {
                        // draw background first so it will be overdrawn by statics
                        if(z1 > z2) {
                            // but only if there is nothing below it
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
                // default
                return z1 - z2;
            }
        });
        return in;
    }

    private void drawStatics(Graphics g, Point3D pos) {
        for(SLStatic s : sortStatics(statics.getStatics(pos))) {
            if(s.getLocation().getZ() - sceneCenter.getZ() > 10 && cutOffZ) {
                continue;
            }

            Image image = getStaticTileImage(s.getStaticID());
            if(image != null) {
                Point d = project(s.getLocation());
                d.x -= image.getWidth(null) / 2;
                d.y += TILE_SIZE / 2 - image.getHeight(null);
                g.drawImage(image, d.x, d.y, null);
            }
        }
    }

    private Image getMapTilePolygon(Point2D where, RasterQuad quad, int textureId) {
        Image image;
        if(!polygonCache.containsKey(where)) {
            Image textureImage = getStaticTileImage(textureId);
            image = rasterizePolygonWithTexture(quad, (BufferedImage) textureImage);
            if(image != null) {
                polygonCache.put(where, image);
            }
        } else {
            image = polygonCache.get(where);
        }
        return image;
    }

    private Image getMapTileImage(int id) {
        Image image;
        if(!mapTileCache.containsKey(id)) {
            ArtEntry entry = art.getLandArt(id);
            if(entry != null) {
                image = entry.image;
                mapTileCache.put(id, image);
            } else {
                image = null;
            }
        } else {
            image = mapTileCache.get(id);
        }
        return image;
    }

    private Image getStaticTileImage(int id) {
        Image image;
        if(!staticTileCache.containsKey(id)) {
            StaticTile tile = tiles.getStaticTile(id);
            ArtEntry entry = art.getStaticArt(id, (tile.flags & StaticTile.FLAG_TRANSLUCENT) != 0);
            if(entry != null) {
                image = entry.image;
                staticTileCache.put(id, image);
            } else {
                image = null;
            }
        } else {
            image = staticTileCache.get(id);
        }
        return image;
    }

    private void reallocateRasterInfo(int height) {
        if(leftSide == null || leftSide.length < height) {
            leftSide = new RasterInfo[height];
            for(int i = 0; i < height; i++) {
                leftSide[i] = new RasterInfo(0, 0, 0, 0);
            }
        }
        if(rightSide == null || rightSide.length < height) {
            rightSide = new RasterInfo[height];
            for(int i = 0; i < height; i++) {
                rightSide[i] = new RasterInfo(0, 0, 0, 0);
            }
        }
    }

    private BufferedImage rasterizePolygonWithTexture(RasterQuad quad, BufferedImage textureImage) {
        // texture coordinates are in [0, 1] so we need to know the actual dimensions
        double imgMaxX = textureImage.getWidth() - 1;
        double imgMaxY = textureImage.getHeight() - 1;

        // find orientation of the polygon
        int n = 4, topIndex = 0, bottomIndex = 0;
        for(int i = 0; i < n; i++) {
            if(quad.raster[i].y > quad.raster[bottomIndex].y) {
                bottomIndex = i;
            }
            if(quad.raster[i].y < quad.raster[topIndex].y) {
                topIndex = i;
            }
        }

        // calculate result height and y offset
        int height = quad.raster[bottomIndex].y - quad.raster[topIndex].y + 1;
        int minY = quad.raster[topIndex].y;
        if(height == 0) {
            return null;
        }

        // approximate polygon texture coordinates
        reallocateRasterInfo(height);
        for(int i = topIndex; i != bottomIndex; ) {
            int j = (i == 0) ? (n - 1) : (i - 1);
            linearApprox(quad.raster[i], quad.raster[j], minY, leftSide);
            i = j;
        }

        for(int i = topIndex; i != bottomIndex; ) {
            int j = (i + 1) % n;
            linearApprox(quad.raster[i], quad.raster[j], minY, rightSide);
            i = j;
        }

        // check if left and right are swapped
        RasterInfo[] left = leftSide, right = rightSide;
        int mid = (quad.raster[bottomIndex].y + quad.raster[topIndex].y) / 2;
        if(leftSide[mid - minY].x > rightSide[mid - minY].x) {
            left = rightSide;
            right = leftSide;
        }

        // calculate output width and x offset
        int maxX = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        for(int y = quad.raster[topIndex].y; y <= quad.raster[bottomIndex].y; y++) {
            int startX = left[y - minY].x;
            int endX = right[y - minY].x;
            if(endX < startX) {
                continue;
            }
            if(startX < minX) {
                minX = startX;
            }
            if(endX > maxX) {
                maxX = endX;
            }
        }
        int width = maxX - minX + 1;

        BufferedImage rasterImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int y = quad.raster[topIndex].y; y <= quad.raster[bottomIndex].y; y++) {
            RasterInfo start = left[y - minY];
            RasterInfo end = right[y - minY];
            int len = end.x - start.x + 1;
            if(len < 0) {
                // this happens if the polygon becomes concave, ignore for now because
                // the cases where this happens are rare and these polygons are not really
                // visible anyways
                continue;
            }
            double txStep = (end.tx - start.tx) / len;
            double tyStep = (end.ty - start.ty) / len;
            double tx = start.tx;
            double ty = start.ty;
            for(int x = start.x; x <= end.x; x++) {
                int dstX = x - minX;
                int dstY = y - minY;
                int srcX = (int) (tx * imgMaxX);
                int srcY = (int) (ty * imgMaxY);
                int dstColor = applyLightLevel(textureImage.getRGB(srcX, srcY), quad.lightLevel);
                rasterImage.setRGB(dstX, dstY, dstColor);
                tx += txStep;
                ty += tyStep;
            }
        }
        return rasterImage;
    }

    private int applyLightLevel(int color, double level) {
        int a = (color & 0xFF000000) >> 24;
        int r = (color & 0x00FF0000) >> 16;
        int g = (color & 0x0000FF00) >>  8;
        int b = (color & 0x000000FF) >>  0;

        r = Math.min((int) (r * level + 0.5), 255);
        g = Math.min((int) (g * level + 0.5), 255);
        b = Math.min((int) (b * level + 0.5), 255);

        return (a << 24) | (r << 16) | (g << 8) | (b << 0);
    }

    private void linearApprox(RasterInfo start, RasterInfo end, int rasterOffset, RasterInfo[] side) {
        int height = end.y - start.y + 1;
        if(height == 0) {
            // just create a straight line starting at "start"
            side[start.y - rasterOffset].x = start.x;
            side[start.y - rasterOffset].y = start.y;
            side[start.y - rasterOffset].tx = start.tx;
            side[start.y - rasterOffset].ty = start.ty;
            return;
        }
        double xStep = (end.x - start.x) / (double) height;
        double txStep = (end.tx - start.tx) / height;
        double tyStep = (end.ty - start.ty)/ height;
        double curX = start.x;
        double curTx = start.tx;
        double curTy = start.ty;
        for(int y = start.y; y <= end.y; y++) {
            side[y - rasterOffset].x = (int) (curX + 0.5);
            side[y - rasterOffset].y = y;
            side[y - rasterOffset].tx = curTx;
            side[y - rasterOffset].ty = curTy;
            curX += xStep;
            curTx += txStep;
            curTy += tyStep;
        }
    }

    private int getZ(int x, int y) {
        return map.getTileElevation(new Point2D(x, y));
    }

    private RasterQuad getPointPolygon(Point3D point) {
        RasterQuad res = new RasterQuad();
        res.isRegular = false;

        // to calculate slopes and light, we need to check our height vs our neighbors
        int selfZ = point.getZ();
        int east = getZ(point.getX() + 1, point.getY());
        int south = getZ(point.getX(), point.getY() + 1);
        int southEast = getZ(point.getX() + 1, point.getY() + 1);

        Point top = project(point);
        top.y -= TILE_SIZE / 2;

        res.lightLevel = calculateLightLevel(selfZ, east, south, southEast);

        // calculate slopes
        int dzEast =      (selfZ - east)        * PROJECTION_CONSTANT;
        int dzSouthEast = (selfZ - southEast)   * PROJECTION_CONSTANT;
        int dzSouth =     (selfZ - south)       * PROJECTION_CONSTANT;

        if(dzEast == 0 && dzSouthEast == 0 && dzSouth == 0) {
            res.isRegular = true;
        }

        // the order in a regular tile is top, right, bottom, left
        res.raster[0] = new RasterInfo(top.x,                   top.y,                           0, 0);
        res.raster[1] = new RasterInfo(top.x + TILE_SIZE / 2,   top.y + TILE_SIZE / 2 + dzEast,  1, 0);
        res.raster[2] = new RasterInfo(top.x,                   top.y + TILE_SIZE + dzSouthEast, 1, 1);
        res.raster[3] = new RasterInfo(top.x - TILE_SIZE / 2,   top.y + TILE_SIZE / 2 + dzSouth, 0, 1);

        for(int i = 0; i < 4; i++) {
            if(res.raster[i].x < res.minX) {
                res.minX = res.raster[i].x;
            }
            if(res.raster[i].x > res.maxX) {
                res.maxX = res.raster[i].x;
            }
            if(res.raster[i].y < res.minY) {
                res.minY = res.raster[i].y;
            }
            if(res.raster[i].y > res.maxY) {
                res.maxY = res.raster[i].y;
            }
        }

        return res;
    }

    private double calculateLightLevel(int selfZ, int east, int south, int southEast) {
        // TODO: think of something better
        if(selfZ < south && selfZ < east && selfZ < southEast) {
            return 0.98;
        } else if(selfZ < south && selfZ < east && selfZ >= southEast) {
            return 1.05;
        } else if(selfZ < south && selfZ >= east && selfZ < southEast) {
            return 0.98;
        } else if(selfZ < south && selfZ >= east && selfZ >= southEast) {
            return 0.98;
        } else if(selfZ >= south && selfZ < east && selfZ < southEast) {
            return 1.08;
        } else if(selfZ >= south && selfZ < east && selfZ >= southEast) {
            return 1.08;
        } else if(selfZ >= south && selfZ >= east && selfZ < southEast) {
            return 1;
        } else if(selfZ > south && selfZ > east && selfZ > southEast) {
            return 1.08;
        } else {
            return 1;
        }
    }

    // game -> screen, returns center of tile
    private Point project(Point3D src) {
        int width = getWidth();
        int height = getHeight();

        // direction vector in game
        int dxG = src.getX() - sceneCenter.getX();
        int dyG = src.getY() - sceneCenter.getY();
        int dzG = src.getZ() - sceneCenter.getZ();

        // direction vector on screen
        int dxS = (dxG - dyG) * TILE_SIZE / 2;
        int dyS = (dxG + dyG) * TILE_SIZE / 2 - dzG * PROJECTION_CONSTANT;

        return new Point(width / 2 + dxS, height / 2 + dyS);
    }

    private Point3D projectBack(Point src) {
        int width = getWidth();
        int height = getHeight();

        // direction vector on screen
        int dxS = src.x - width / 2;
        int dyS = src.y - height / 2;

        // direction vector in game
        int dxG_ = dxS * 2 / TILE_SIZE;
        int dyG_ = dyS * 2 / TILE_SIZE;
        int dxG = (dxG_ + dyG_) / 2;
        int dyG = (dyG_ - dxG_) / 2;

        int resX = sceneCenter.getX() + dxG;
        int resY = sceneCenter.getY() + dyG;
        if(resX < 0) resX = 0;
        if(resY < 0) resY = 0;
        if(resX >= 1024) resX = 1023;
        if(resY >= 1024) resY = 1023;

        return new Point3D(resX, resY, getZ(resX, resY));
    }
}
