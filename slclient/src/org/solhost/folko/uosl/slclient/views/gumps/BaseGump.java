package org.solhost.folko.uosl.slclient.views.gumps;

import java.awt.Point;
import java.util.List;

import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.views.util.Texture;

public abstract class BaseGump {
    private final int gumpID;
    private final SLObject object;
    private boolean wantsClose;
    private Point position;

    public class GumpPart {
        public BaseGump owner;
        public Texture texture;
        public Point relativePosition;
        public Object gumpData;
        public int clickCount;

        public GumpPart(BaseGump owner, Texture texture, Point relPos) {
            this.owner = owner;
            this.texture = texture;
            this.relativePosition = relPos;
        }
    }

    public BaseGump(SLObject obj, int gumpID) {
        this.object = obj;
        this.gumpID = gumpID;
        this.position = new Point(0, 0);
    }

    public int getGumpID() {
        return gumpID;
    }

    public SLObject getObject() {
        return object;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public void close() {
        wantsClose = true;
    }

    public boolean wantsClose() {
        return wantsClose;
    }

    public abstract List<GumpPart> render();

    public void onClick(GumpPart part) {
    }

    public abstract boolean canDragGumpWithPart(GumpPart part);
}
