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

class Sextant
  include ItemBehavior

  def onCreate(sextant)
    sextant.setGraphic(0x0461)
  end

  def onBehaviorChange(sextant)
  end

  def onLoad(sextant)
  end

  def onUse(player, sextant)
    if(player.tryAccess(sextant))
      x = player.getLocation().getX()
      y = player.getLocation().getY()
      centerX = 308
      centerY = 634
      
      longitude = (x - centerX) * 360.0 / 1024
      latitude = (y - centerY) * 360.0 / 1024
      
      if longitude > 180.0
        longitude = (longitude % 180) - 180
      end
      
      if latitude > 180
        latitude = (latitude % 180) - 180
      end

      if longitude < 0
        longDir = "W"
        longitude = -longitude
      else
        longDir = "E"
      end
      
      if latitude < 0
        latDir = "N"
        latitude = -latitude
      else
        latDir = "S"
        latitude = -latitude
      end
      
      str = "I am standing at #{latitude.to_i}\260 #{((latitude % 1.0) * 60).to_i}'#{latDir}, #{longitude.to_i}\260 #{((longitude % 1.0) * 60).to_i}'#{longDir}"
      $api.say(player, str)
    end
  end
end
