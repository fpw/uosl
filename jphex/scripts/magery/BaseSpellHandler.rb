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
class BaseSpellHandler < SpellHandler
  def beginCast(player, spell, scroll, mana, delay, minSkill, noGainAfter)
    notBefore = $api.getObjectProperty(player, "noCastBefore") || 0
    if $api.getTimerTicks() < notBefore
      $api.sendSysMessage(player, "You are already casting a spell.")
      return
    end
    
    if player.getAttribute(Attribute::MANA) < mana
      $api.sendSysMessage(player, "You lack the required mana to cast this spell.")
      return
    end
    
    hitsBefore = player.getAttribute(Attribute::HITS)
    $api.setObjectProperty(player, "noCastBefore", $api.getTimerTicks() + delay)
    $api.speakPowerWords(player, spell)
    player.freeze()
    
    $api.addTimer(delay) do
      player.thaw()
      if !$api.checkSkill(player, Attribute::MAGIC, (minSkill * 10).to_i, (noGainAfter * 10).to_i)
        # On failure: Require half the mana and notice player
        $api.playSoundNearObj(player, 0x18)
        player.consumeAttribute(Attribute::MANA, mana / 2)
        $api.sendSysMessage(player, "You fail to cast the spell.")
      elsif player.getAttribute(Attribute::HITS) < hitsBefore
        # Interrupted casting by damage
        $api.sendSysMessage(player, "You were interrupted while casting.")
        $api.playSoundNearObj(player, 0x18)
      else
        # On success: consume mana (if he still has it), call spell and destroy scroll
        if not player.consumeAttribute(Attribute::MANA, mana)
          $api.sendSysMessage(player, "You lost the required mana to cast this spell.")
          $api.playSoundNearObj(player, 0x18)
        else
          yield()
          scroll.consume(1) if scroll != nil
        end
      end
    end
  end
end
