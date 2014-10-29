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
require './scripts/packages/base/mobiles/BaseMobile'
class Merchant < BaseMobile
  include MobileBehavior

  @@loot_table = [
      {:graphic => 0, :behavior => "food", :chance => 1.0, :count => 1..3},
      {:graphic => 0x01F8, :chance => 1.0, :amount => 25..75, :count => 1} # gold
    ]

  def onSpawn(me)
    if rand() < 0.5
      $api.setGraphic(me, 0x00)
    else
      $api.setGraphic(me, 0x01)
    end

    $api.assignRandomName(me, "")
    $api.createClothes(me)
    me.setSuffix("the Merchant")

    setType(me, :shy)
    setStats(me, :str => 40..60, :dex => 40..50, :int => 20..30)

    $api.setAttribute(me, Attribute::MELEE, 200)
    $api.setAttribute(me, Attribute::BATTLE_DEFENSE, 100)
    
    setupMerchant(me)
  end

  def setupMerchant(me)
    # deriving classes should set suffix and fill inventory
  end
  
  def onEnterArea(me, player)
    $api.lookAt(me, player)
  end
  
  def onDoubleClick(me, player)
    $api.lookAt(me, player)
    if me.distanceTo(player) < 4
      $api.say(me, "Have a look at my wares")
      $api.offerShop(me, player)
    else
      $api.say(me, player.getName() + ", please come a littler closer")
    end
    return false
  end
  
  def onHello(me, player)
    $api.lookAt(me, player)
    $api.say(me, "Greetings, " + player.getName() + "! Dost thou want to take a look at my wares?")
  end
  
  def onSpeech(me, player, line)
    $api.lookAt(me, player)
  end

  def onAttacked(mob, attacker)
    $api.say(mob, "Guards! Help!")
    super
  end

  def onDeath(me, corpse)
    generateLoot(corpse, @@loot_table)
  end
end
