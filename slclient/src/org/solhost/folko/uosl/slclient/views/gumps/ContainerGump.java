package org.solhost.folko.uosl.slclient.views.gumps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solhost.folko.uosl.slclient.controllers.MainController;
import org.solhost.folko.uosl.slclient.models.SLItem;
import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.models.TexturePool;

public class ContainerGump extends BaseGump {
    private final GumpPart frame;
    private final MainController controller;
    private final Map<SLItem, GumpPart> itemParts;

    public ContainerGump(MainController controller, SLObject obj, int gumpID) {
        super(obj, gumpID);
        this.controller = controller;
        this.itemParts = new HashMap<>();

        frame = new GumpPart(this, TexturePool.getGumpTexture(gumpID), new Point(0, 0));
    }

    @Override
    public List<GumpPart> render() {
        SLItem container = (SLItem) getObject();
        Collection<SLItem> content = container.getContainerContents();

        List<GumpPart> res = new ArrayList<>(1 + content.size());
        res.add(frame);
        for(SLItem itm : content) {
            GumpPart part = itemParts.get(itm);
            Point pos = new Point(itm.getLocation().getX(), itm.getLocation().getY());
            if(part == null) {
                break;
                // part = new GumpPart(this, TexturePool.getStaticTexture(itm.getGraphic()), pos);
                // part.gumpData = itm;
                // itemParts.put(itm, part);
            } else {
                // part.texture = TexturePool.getStaticTexture(itm.getGraphic());
                part.relativePosition = pos;
            }
            res.add(part);
        }
        return res;
    }

    @Override
    public boolean canDragGumpWithPart(GumpPart part) {
        return part == frame;
    }

    @Override
    public void onClick(GumpPart part) {
        part.clickCount++;
        if(part.gumpData instanceof SLItem && part.clickCount >= 2) {
            controller.onDoubleClickObject((SLItem) part.gumpData);
            part.clickCount = 0;
        }
    }
}
