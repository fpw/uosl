package org.solhost.folko.uosl.slclient.views.util;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

public class InputManager implements MouseListener, MouseMotionListener, KeyListener {
    private static final int MOUSE_DOUBLE_CLICK_MS = 300;

    private final boolean[] pressedKeys;
    private final StringBuilder typedKeys;

    private long lastMouseLeftClickTime;
    private Point lastLeftClick, lastDoubleClick;
    private MouseEvent curDragEvent;

    private boolean rightMouseButtonDown;
    private boolean mouseInsideWindow;


    public InputManager() {
        pressedKeys = new boolean[256];
        mouseInsideWindow = true;
        typedKeys = new StringBuilder();
    }

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            rightMouseButtonDown = true;
        }
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            rightMouseButtonDown = false;
        } else if(SwingUtilities.isLeftMouseButton(e)) {
            curDragEvent = null;
        }
    }

    @Override
    public synchronized void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
            long msDiff = e.getWhen() - lastMouseLeftClickTime;
            if(msDiff < MOUSE_DOUBLE_CLICK_MS) {
                lastMouseLeftClickTime = 0;
                lastDoubleClick = e.getPoint();
            } else {
                // potential single click
                lastLeftClick = e.getPoint();
                lastMouseLeftClickTime = System.currentTimeMillis();
            }
        }
    }

    public synchronized Point pollLastDoubleClick() {
        Point res = lastDoubleClick;
        lastDoubleClick = null;
        return res;
    }

    public synchronized Point peekNextSingleClick() {
        return lastLeftClick;
    }

    public synchronized void abortNextSingleClick() {
        lastMouseLeftClickTime = 0;
        lastLeftClick = null;
    }

    public synchronized Point pollLastSingleClick() {
        if(lastMouseLeftClickTime != 0 && System.currentTimeMillis() - lastMouseLeftClickTime > MOUSE_DOUBLE_CLICK_MS) {
            lastMouseLeftClickTime = 0;
            return lastLeftClick;
        } else {
            return null;
        }
    }

    @Override
    public synchronized void mouseDragged(MouseEvent e) {
        curDragEvent = e;
    }

    public synchronized Point peekLastDragEvent() {
        if(curDragEvent != null) {
            return curDragEvent.getPoint();
        } else {
            return null;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseInsideWindow = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseInsideWindow = false;
    }

    public boolean isMouseInsideWindow() {
        return mouseInsideWindow;
    }

    public boolean isRightMouseButtonDown() {
        return rightMouseButtonDown;
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if(e.getKeyCode() < pressedKeys.length) {
            pressedKeys[e.getKeyCode()] = true;
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        if(e.getKeyCode() < pressedKeys.length) {
            pressedKeys[e.getKeyCode()] = false;
        }
    }

    public synchronized boolean isKeyDown(int code) {
        return pressedKeys[code];
    }

    @Override
    public synchronized void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        typedKeys.append(c);
    }

    public synchronized String pollTypedKeys() {
        String res = typedKeys.toString();
        typedKeys.setLength(0);
        return res;
    }
}
