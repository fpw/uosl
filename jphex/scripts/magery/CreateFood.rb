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
class CreateFood < BaseSpellHandler

  @@min_skill  = 0
  @@gain_until = 20.0
  @@delay      = 1500
  @@mana       = 5

  @@food = [
      {:graphic => 0x0011, :name => 'an apple'},
      {:graphic => 0x0039, :name => 'a peach'},
      {:graphic => 0x012C, :name => 'a ham'},
      {:graphic => 0x0136, :name => 'a loaf of bread'},
      {:graphic => 0x0137, :name => 'a pie'},
      {:graphic => 0x02FF, :name => 'some grapes'},
    ]

  def cast(player, scroll)
    beginCast(player, Spell::CREATEFOOD, scroll, @@mana, @@delay, @@min_skill, @@gain_until) do
      $api.playSoundNearObj(player, 0x9F)
      entry = @@food.sample
      $api.createItemInBackpack(player, entry[:graphic], "food")
      $api.sendSysMessage(player, "You magically created #{entry[:name]}")
    end
  end
end
