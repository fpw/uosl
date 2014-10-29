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
class Healing < BaseSpellHandler
  @@min_skill  = 10.0
  @@gain_until = 30.0
  @@delay      = 1500
  @@mana       = 10

  @@range = 10

  def castOn(player, scroll, target)
    if player.distanceTo(target) > @@range
      $api.sendSysMessage(player, "That is too far away.")
      return
    end

    if not $api.canSee(player, target)
      $api.sendSysMessage(player, "You can't see that.")
      return
    end    

    beginCast(player, Spell::HEALING, scroll, @@mana, @@delay, @@min_skill, @@gain_until) do
      hits = player.getAttribute(Attribute::INTELLIGENCE) / 3
      $api.playSoundNearObj(target, 0xA4)
      target.rewardAttribute(Attribute::HITS, hits)
    end
  end
end
