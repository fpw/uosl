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

# An item that destroys itself after the property "duration" run up (milliseconds of existance)
# Usage: create item without behavior, setup properties, then set behavior
class TempItem
  include ItemBehavior

  def onCreate(item)
  end

  def onBehaviorChange(item)
    duration = $api.getObjectProperty(item, "duration")
    $api.addTimer(duration) do
      $api.deleteObject(item)
    end
  end

  def onLoad(item)
    $api.deleteObject(item)
  end
    
  def onUse(player, item)
  end
end
