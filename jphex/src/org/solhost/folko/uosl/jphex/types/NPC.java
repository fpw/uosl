/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
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
package org.solhost.folko.uosl.jphex.types;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.jphex.scripting.MobileBehavior;
import org.solhost.folko.uosl.jphex.scripting.ScriptManager;
import org.solhost.folko.uosl.libuosl.types.Mobiles;

public class NPC extends Mobile {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger("jphex.npc");
    private String behavior, suffix;

    public NPC(long serial) {
        super(serial);
        this.suffix = "";
        this.behavior = null;
    }

    public void setBehavior(String behavior) {
        if(ScriptManager.instance().getMobileBehaviour(behavior) != null) {
            this.behavior = behavior;
        }
    }

    public String getBehavior() {
        return behavior;
    }


    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getDecoratedName() {
        if(suffix.length() > 0) {
            return getName() + " " + getSuffix();
        } else {
            return getName();
        }
    }

    public void onEnterArea(Player player) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                be.onEnterArea(this, player);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onEnterArea: " + e.getMessage(), e);
            }
        }
    }

    public void onSpeech(Player player, String line) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                be.onSpeech(this, player, line);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onEnterArea: " + e.getMessage(), e);
            }
        }
    }

    public void onHello(Player player) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                be.onHello(this, player);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onHello: " + e.getMessage(), e);
            }
        }
    }

    // returns whether to send the paperdoll to the player
    public boolean onDoubleClick(Player player) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                return be.onDoubleClick(this, player);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onEnterArea: " + e.getMessage(), e);
                return false;
            }
        } else {
            // default behavior: depend on graphic
            if(getGraphic() == Mobiles.MOBTYPE_HUMAN_FEMALE || getGraphic() == Mobiles.MOBTYPE_HUMAN_MALE) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void onAttacked(Mobile attacker) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                be.onAttacked(this, attacker);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onAttacked: " + e.getMessage(), e);
            }
        }
    }

    public void onDeath(Item corpse) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                be.onDeath(this, corpse);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onDeath: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean dealDamage(int damage, Mobile source) {
        if(source != null) {
            onAttacked(source);
        }
        return super.dealDamage(damage, source);
    }

    @Override
    public void onLoad() {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be != null) {
            try {
                be.onLoad(this);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onDeath: " + e.getMessage(), e);
            }
        }
    }
}
