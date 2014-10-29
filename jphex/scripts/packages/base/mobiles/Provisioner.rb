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
class Provisioner < Merchant
  include MobileBehavior

  def setupMerchant(me)
    me.setSuffix("the Provisioner")
    $api.createItemInBackpack(me, 0x00B3) # candle stick
    $api.createItemInBackpack(me, 0x0348) # backpack
    $api.createItemInBackpack(me, 0x02F1) # tunic
    $api.createItemInBackpack(me, 0x02F2) # pants
    $api.createItemInBackpack(me, 0x0442) # bracers 
    $api.createItemInBackpack(me, 0x0441) # jerkin
    $api.createItemInBackpack(me, 0x0443) # leggings
    $api.createItemInBackpack(me, 0x044A) # leather cap
  end
end
