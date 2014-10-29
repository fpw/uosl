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
class Cook < Merchant
  include MobileBehavior

  def setupMerchant(me)
    me.setSuffix("the Cook")
    $api.createItemInBackpack(me, 0x0011, "food") # apple
    $api.createItemInBackpack(me, 0x0039, "food") # peach
    $api.createItemInBackpack(me, 0x012C, "food") # ham
    $api.createItemInBackpack(me, 0x0136, "food") # bread
    $api.createItemInBackpack(me, 0x0137, "food") # pie
    $api.createItemInBackpack(me, 0x02FF, "food") # grapes
  end
end
