package org.solhost.folko.uosl.slclient.views.gumps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.uosl.slclient.controllers.MainController;
import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.models.TexturePool;

public class ContainerGump extends BaseGump {
    private final GumpPart frame;
    private final MainController controller;

    public ContainerGump(MainController controller, SLObject obj, int gumpID) {
        super(obj, gumpID);
        this.controller = controller;

        frame = new GumpPart(this, TexturePool.getGumpTexture(gumpID), new Point(0, 0));
    }

    @Override
    public List<GumpPart> render() {
        List<GumpPart> res = new ArrayList<>(1);
        res.add(frame);
        return res;
    }

    @Override
    public boolean canDragGumpWithPart(GumpPart part) {
        return part == frame;
    }

    @Override
    public void onClick(GumpPart part) {
    }
}
