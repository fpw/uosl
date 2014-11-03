package org.solhost.folko.uosl.slclient.views.util;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

public class InputManager implements MouseListener, KeyListener {
    private static final int MOUSE_DOUBLE_CLICK_MS = 300;

    private long lastMouseLeftClickTime;
    private Point lastLeftClick, lastDoubleClick;
    private boolean rightMouseButtonDown;
    private boolean mouseInsideWindow;
    private final boolean[] pressedKeys;
    private final StringBuilder typedKeys;

    public InputManager() {
        pressedKeys = new boolean[256];
        mouseInsideWindow = true;
        typedKeys = new StringBuilder();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            rightMouseButtonDown = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            rightMouseButtonDown = false;
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

    public synchronized Point pollLastSingleClick() {
        if(lastMouseLeftClickTime != 0 && System.currentTimeMillis() - lastMouseLeftClickTime > MOUSE_DOUBLE_CLICK_MS) {
            lastMouseLeftClickTime = 0;
            return lastLeftClick;
        } else {
            return null;
        }
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
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() < pressedKeys.length) {
            pressedKeys[e.getKeyCode()] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() < pressedKeys.length) {
            pressedKeys[e.getKeyCode()] = false;
        }
    }

    public boolean isKeyDown(int code) {
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
