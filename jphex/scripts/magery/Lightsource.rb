#-------------------------------------------------------------------------------
# Copyright (c) 2013 Folke Will <folke.will@gmail.com>
# 
# This file is part of JPhex.
# 
# JPhex is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# JPhex is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#-------------------------------------------------------------------------------
require './scripts/magery/BaseSpellHandler'
class Lightsource < BaseSpellHandler
  
  @@min_skill  = 20.0
  @@gain_until = 45.0
  @@delay      = 2000
  @@mana       = 15
  
  def castAt(player, scroll, target)
    if not $api.canSee(player, target)
      $api.sendSysMessage(player, "You can't see that")
      return
    end

    beginCast(player, Spell::LIGHTSOURCE, scroll, @@mana, @@delay, @@min_skill, @@gain_until) do
      lightsource = $api.createItemAtLocation(target.getX(), target.getY(), target.getZ(), 0x1B3)
      lightsource.lock()
      duration = player.getAttribute(Attribute::INTELLIGENCE) * 1000 / 3
      $api.setObjectProperty(lightsource, "duration", duration)
      lightsource.setBehavior("tempitem")
      $api.playSoundNearObj(player, 0xA0)
    end
  end
end
