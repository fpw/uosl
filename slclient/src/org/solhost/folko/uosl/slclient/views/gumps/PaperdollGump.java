package org.solhost.folko.uosl.slclient.views.gumps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solhost.folko.uosl.libuosl.types.Gumps;
import org.solhost.folko.uosl.slclient.controllers.MainController;
import org.solhost.folko.uosl.slclient.models.Player;
import org.solhost.folko.uosl.slclient.models.SLItem;
import org.solhost.folko.uosl.slclient.models.SLMobile;
import org.solhost.folko.uosl.slclient.models.TexturePool;

public class PaperdollGump extends BaseGump {
    private final MainController controller;
    private final GumpPart frame, charachter;
    private final GumpPart backscrollButton, skillsButton, statusButton, peaceButton, warButton, closeButton;
    private final boolean forPlayer;
    private final Map<Short, GumpPart> equipParts;

    public PaperdollGump(MainController controller, SLMobile mob, boolean forPlayer) {
        super(mob, Gumps.ID_PAPERDOLL);
        this.controller = controller;
        this.forPlayer = forPlayer;
        this.equipParts = new HashMap<>();

        frame            = new GumpPart(this, TexturePool.getGumpTexture(forPlayer ? 0x0A : 0x0B), new Point(0, 0));
        charachter       = new GumpPart(this, TexturePool.getGumpTexture(mob.getGraphic() + 0x0C), new Point(0, 0));
        backscrollButton = new GumpPart(this, TexturePool.getGumpTexture(0x10), new Point(166, 95));
        skillsButton     = new GumpPart(this, TexturePool.getGumpTexture(0x12), new Point(166, 132));
        statusButton     = new GumpPart(this, TexturePool.getGumpTexture(0x0E), new Point(166, 169));
        peaceButton      = new GumpPart(this, TexturePool.getGumpTexture(0x17), new Point(166, 207));
        warButton        = new GumpPart(this, TexturePool.getGumpTexture(0x18), new Point(166, 207));
        closeButton      = new GumpPart(this, TexturePool.getGumpTexture(0x19), new Point(229, 207));
    }

    @Override
    public List<GumpPart> render() {
        SLMobile mob = (SLMobile) getObject();
        Collection<SLItem> equipment = mob.getEquipment();

        // the client draws the equipment in a specific order, maybe this is important:
        // layer order: 3, 4, 11, 6, 5, 10, 12, 13, 14, 7, 15, 8, 9, 1, 2

        List<GumpPart> res = new ArrayList<>(7 + equipment.size());
        res.add(frame);
        res.add(charachter);
        if(forPlayer) {
            res.add(backscrollButton);
            res.add(skillsButton);
        }
        if(mob instanceof Player && ((Player) mob).isInWarMode()) {
            res.add(warButton);
        } else {
            res.add(peaceButton);
        }
        res.add(statusButton);
        res.add(closeButton);
        for(SLItem itm : equipment) {
            int animID = itm.getTileInfo().animationID;
            if(animID == 0) {
                continue;
            }
            int gID = animID + 0x64;

            GumpPart curPart = equipParts.get(itm.getLayer());
            if(curPart == null || curPart.gumpData != itm) {
                curPart = new GumpPart(this, TexturePool.getGumpTexture(gID), new Point(0, 0));
                curPart.gumpData = itm;
                equipParts.put(itm.getLayer(), curPart);
            }
            curPart.texture = TexturePool.getGumpTexture(gID);
            res.add(curPart);
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
        if(part == closeButton) {
            close();
        } else if(part == backscrollButton) {
            controller.onOpenBackscroll();
        } else if(part == skillsButton) {
            controller.onOpenSkillWindow();
        } else if(part == statusButton) {
            controller.onOpenStatus((SLMobile) getObject(), true);
        } else if(part == peaceButton || part == warButton) {
            controller.onToggleWarMode((SLMobile) getObject());
        } else if(part.gumpData instanceof SLItem && part.clickCount >= 2) {
            // the real client does it in the same way,
            // i.e. there can be lots of time between the two clicks
            controller.onDoubleClickObject((SLItem) part.gumpData);
            part.clickCount = 0;
        }
    }
}
