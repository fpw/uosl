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
class Light < BaseSpellHandler
  
  @@min_skill  = 40.0
  @@gain_until = 60.0
  @@delay      = 3000
  @@mana       = 20

  @@damage_delay = 1500
  @@radius       = 2
  
  def cast(player, scroll)
    beginCast(player, Spell::LIGHT, scroll, @@mana, @@delay, @@min_skill, @@gain_until) do
      fire = $api.createItemAtMobile(player, 0x03BC)
      fire.lock()
      duration = player.getAttribute(Attribute::INTELLIGENCE) / 4 * 1000
      damage =  player.getAttribute(Attribute::INTELLIGENCE) / 8
      $api.setObjectProperty(fire, "duration", duration)
      $api.playSoundNearObj(player, 0x9F)
      fire.setBehavior("tempitem")
      $api.addTimer(@@damage_delay) do
        damageTimer(fire, player, damage)
      end
    end
  end
  
  def damageTimer(fire, owner, damage)
    return if fire.isDeleted()

    for mob in $api.getMobilesInRange(fire, @@radius)
      next if mob == owner or not $api.canSee(fire, mob)
      if $api.checkSkill(mob, Attribute::MAGIC_DEFENSE, 0, 1100)
        $api.sendSysMessage(target, "You feel yourself resisting magical energy!")
        mob.dealDamage(damage * 0.3, owner)
      else
        mob.dealDamage(damage, owner)
      end
      $api.playSoundNearObj(mob, 0x86)
    end
    
    $api.addTimer(@@damage_delay) do
      damageTimer(fire, owner, damage)
    end
  end
end
