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
require './scripts/packages/base/mobiles/Merchant'
class Blacksmith < Merchant
  include MobileBehavior

  def setupMerchant(me)
    me.setSuffix("the Blacksmith")
    $api.createItemInBackpack(me, 0x0293) # great axe
    $api.createItemInBackpack(me, 0x0294) # battle axe
    $api.createItemInBackpack(me, 0x0296) # hand axe
    $api.createItemInBackpack(me, 0x0298) # battle axe
    $api.createItemInBackpack(me, 0x029A) # war axe
    $api.createItemInBackpack(me, 0x02A1) # dagger
    $api.createItemInBackpack(me, 0x02A2) # mace
    $api.createItemInBackpack(me, 0x02A4) # short sword
    $api.createItemInBackpack(me, 0x02A6) # broad sword
    $api.createItemInBackpack(me, 0x02EC) # vambraces
    $api.createItemInBackpack(me, 0x02ED) # breastplate
    $api.createItemInBackpack(me, 0x02EE) # gorget
    $api.createItemInBackpack(me, 0x02EF) # helmet
    $api.createItemInBackpack(me, 0x02F0) # greaves
    $api.createItemInBackpack(me, 0x3D58) # heater
  end
end
