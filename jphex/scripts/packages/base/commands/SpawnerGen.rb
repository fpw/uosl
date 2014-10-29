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
class SpawnerGen < TextCommand
  @@spawners = [
      # Blacksmith
      {:x => 393, :y => 532, :z => 0, :type => 0x044B, :count => 1, :duration => 10, :range => 0},
      {:x => 448, :y => 647, :z => 0, :type => 0x044B, :count => 1, :duration => 10, :range => 0},
      # Provisioner
      {:x => 478, :y => 654, :z => 0, :type => 0x0455, :count => 1, :duration => 10, :range => 0},
      {:x => 407, :y => 587, :z => 0, :type => 0x0455, :count => 1, :duration => 10, :range => 0},
      # Mage
      {:x => 462, :y => 528, :z => 0, :type => 0x0451, :count => 1, :duration => 10, :range => 0},
      # Cook
      {:x => 443, :y => 599, :z => 0, :type => 0x044D, :count => 1, :duration => 10, :range => 0},

      # Rabbit
      {:x => 169, :y => 855, :z => 0, :type => 0x043F, :count => 5, :duration => 3, :range => 10},
      {:x => 266, :y => 512, :z => 0, :type => 0x043F, :count => 5, :duration => 3, :range => 10},
      {:x => 48,  :y => 606, :z => 0, :type => 0x043F, :count => 5, :duration => 3, :range => 10},
      {:x => 581, :y => 701, :z => 7, :type => 0x043F, :count => 5, :duration => 3, :range => 10},
      {:x => 356, :y => 686, :z => 0, :type => 0x043F, :count => 3, :duration => 3, :range => 10},
      # Wolf
      {:x => 419, :y => 507, :z => 0, :type => 0x043C, :count => 2, :duration => 3, :range => 10},
      {:x => 504, :y => 508, :z => 0, :type => 0x043C, :count => 2, :duration => 3, :range => 10},
      {:x => 231, :y => 653, :z => 0, :type => 0x043C, :count => 2, :duration => 3, :range => 10},
      # Deer
      {:x => 182, :y => 762, :z => 0, :type => 0x043E, :count => 3, :duration => 3, :range => 10},
      {:x => 546, :y => 516, :z => 0, :type => 0x043E, :count => 2, :duration => 3, :range => 10},
      {:x => 315, :y => 511, :z => 0, :type => 0x043E, :count => 3, :duration => 3, :range => 10},
      {:x => 81,  :y => 635, :z => 0, :type => 0x043E, :count => 3, :duration => 3, :range => 10},
      # Orc
      {:x => 58,  :y => 532, :z => 0, :type => 0x0439, :count => 4, :duration => 4, :range => 10},
      {:x => 40,  :y => 672, :z => 0, :type => 0x0439, :count => 4, :duration => 4, :range => 10},
      {:x => 58,  :y => 810, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 59,  :y => 827, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 59,  :y => 843, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 75,  :y => 835, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 83,  :y => 851, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 82,  :y => 804, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 107, :y => 819, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      {:x => 107, :y => 835, :z => 0, :type => 0x0439, :count => 1, :duration => 4, :range => 3},
      # Orc Captain
      {:x => 55,  :y => 551, :z => 0, :type => 0x043A, :count => 2, :duration => 5, :range => 4},
      {:x => 54,  :y => 662, :z => 0, :type => 0x043A, :count => 2, :duration => 5, :range => 5},
      {:x => 74,  :y => 818, :z => 0, :type => 0x043A, :count => 1, :duration => 5, :range => 3},
      # Skeleton
      {:x => 363, :y => 857, :z => 0, :type => 0x043B, :count => 4, :duration => 3, :range => 3},
      {:x => 372, :y => 874, :z =>   0, :type => 0x043B, :count => 4, :duration => 3, :range => 3},
      {:x => 363, :y => 867, :z => -22, :type => 0x043B, :count => 2, :duration => 3, :range => 3},
      {:x => 380, :y => 860, :z => -22, :type => 0x043B, :count => 2, :duration => 3, :range => 3},
      {:x => 400, :y => 866, :z => -22, :type => 0x043B, :count => 5, :duration => 3, :range => 3},
      {:x => 419, :y => 867, :z => -22, :type => 0x043B, :count => 2, :duration => 3, :range => 3},
      {:x => 439, :y => 859, :z => -22, :type => 0x043B, :count => 3, :duration => 3, :range => 3},
      {:x => 413, :y => 855, :z => 0, :type => 0x043B, :count => 4, :duration => 3, :range => 3}

    ]
  
  def invoke(player, line)
    for entry in @@spawners
      next if exists?(entry)
      x, y, z, = entry[:x], entry[:y], entry[:z]
      puts "Creating spawner at #{x} #{y} #{z}"
      spawner = $api.createItemAtLocation(x, y, z, entry[:type])
      $api.setObjectProperty(spawner, "count", entry[:count])
      $api.setObjectProperty(spawner, "duration", entry[:duration])
      $api.setObjectProperty(spawner, "range", entry[:range])
      spawner.setBehavior("spawner")
    end
  end
  
  def exists?(entry)
    for item in $api.getItemsAtLocation(entry[:x], entry[:y], entry[:z])
      if item.getGraphic() == entry[:type]
        return true
      end
    end
    return false
  end
end
