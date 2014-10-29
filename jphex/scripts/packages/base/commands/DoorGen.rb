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
class DoorGen < TextCommand

  # Static1, Static2, Door Set
  @@static_pairs = [
    [0x0001, 0x0001, 0x3EC1],
    [0x0002, 0x0002, 0x3EC1],
    [0x0003, 0x0002, 0x3EC1],
    [0x0003, 0x0001, 0x3EC1],
    [0x005E, 0x0060, 0x3EEC],
    [0x005E, 0x0063, 0x3EEC],
    [0x005E, 0x005F, 0x3EEC],
    [0x005F, 0x0063, 0x3EEC],
    [0x005F, 0x005F, 0x3EEC],
    [0x0060, 0x0176, 0x3EEC],
    [0x0062, 0x0060, 0x3EEC],
    [0x0062, 0x0062, 0x3EEC],
    [0x0063, 0x0063, 0x3EEC],
    [0x0064, 0x0062, 0x3EEC],
    [0x00C0, 0x00C1, 0x3EEC],
    [0x00C0, 0x00C2, 0x3EEC],
    [0x00C1, 0x00C1, 0x3EEC],
    [0x00C2, 0x00C2, 0x3EEC],
    [0x00C4, 0x00C5, 0x3EDC],
    [0x00C4, 0x00C6, 0x3EDC],
    [0x00C5, 0x00C5, 0x3EDC],
    [0x00C6, 0x00C6, 0x3EDC],
    [0x00C8, 0x00C9, 0x3EC1],
    [0x00C8, 0x00CA, 0x3EC1],
    [0x00C9, 0x00C9, 0x3EC1],
    [0x00CA, 0x00CA, 0x3EC1],
    [0x00CC, 0x00CD, 0x3EC1],
    [0x00CC, 0x00CE, 0x3EC1],
    [0x00CE, 0x00CE, 0x3EC1],
    [0x00D0, 0x00D1, 0x3EDC],
    [0x00D0, 0x00D2, 0x3EDC],
    [0x00D1, 0x00D1, 0x3EDC],
    [0x00D2, 0x00D2, 0x3EDC],
    [0x00D4, 0x00D5, 0x3EEC],
    [0x00D4, 0x00D6, 0x3EEC],
    [0x00D5, 0x00D5, 0x3EEC],
    [0x00D6, 0x00D6, 0x3EEC],
    [0x00D8, 0x00D9, 0x3EEC],
    [0x00D8, 0x00DA, 0x3EEC],
    [0x00D9, 0x00D9, 0x3EEC],
    [0x00DA, 0x00DA, 0x3EEC],
    [0x00DC, 0x00DD, 0x3EEC],
    [0x00DC, 0x00DE, 0x3EEC],
    [0x00DD, 0x00DD, 0x3EEC],
    [0x00DE, 0x00DE, 0x3EEC],
    [0x00E0, 0x00E1, 0x3F0C],
    [0x00E0, 0x00E2, 0x3F0C],
    [0x00E1, 0x00E1, 0x3F0C],
    [0x00E2, 0x00E2, 0x3F0C],
    [0x00E2, 0x00D9, 0x3F0C],
    [0x00E2, 0x00E2, 0x3F0C],
    [0x0104, 0x0105, 0x3EC1],
    [0x0104, 0x0106, 0x3EC1],
    [0x0105, 0x0105, 0x3EC1],
    [0x0106, 0x0106, 0x3EC1],
    [0x0108, 0x0109, 0x3EEC],
    [0x0108, 0x010A, 0x3EEC],
    [0x0109, 0x0109, 0x3EEC],
    [0x010A, 0x010A, 0x3EEC],
    [0x0140, 0x0141, 0x3EC1],
    [0x0140, 0x0142, 0x3EC1],
    [0x0141, 0x0141, 0x3EC1],
    [0x0142, 0x0142, 0x3EC1],
    [0x0175, 0x0063, 0x3EEC],
    [0x0175, 0x005E, 0x3EEC],
    [0x0174, 0x0175, 0x3EEC],
    [0x0174, 0x0176, 0x3EEC],
    [0x0175, 0x0175, 0x3EEC],
    [0x0176, 0x0176, 0x3EEC],
    [0x0199, 0x0198, 0x3F1C],
    [0x0199, 0x019A, 0x3F1C],
    [0x0198, 0x0198, 0x3F1C],
    [0x019A, 0x019A, 0x3F1C],
    [0x019A, 0x3F1C, 0x3F1C],
    [0x02BC, 0x02BD, 0x3EFC],
    [0x02BC, 0x02BE, 0x3EFC],
    [0x02BD, 0x02BD, 0x3EFC],
    [0x02BE, 0x02BE, 0x3EFC],
    [0x02C0, 0x02C0, 0x3EFC],
    [0x02C1, 0x02C1, 0x3EFC],
    [0x035C, 0x035D, 0x3EEC],
    [0x035C, 0x035E, 0x3EEC],
    [0x035C, 0x0360, 0x3EEC],
    [0x035C, 0x0361, 0x3EEC],
    [0x035C, 0x035C, 0x3EEC],
    [0x035D, 0x035D, 0x3EEC],
    [0x035E, 0x035E, 0x3EEC],
    [0x035E, 0x0361, 0x3EEC],
    [0x035E, 0x0363, 0x3EEC],
    [0x0360, 0x0360, 0x3EEC],
    [0x0360, 0x035D, 0x3EEC],
    [0x0361, 0x0361, 0x3EEC],
    [0x0362, 0x0362, 0x3EEC],
    [0x0363, 0x0363, 0x3EEC]    
  ]

  # Stuff that doesn't block door creation if between doors
  @@whitelist = [
    0x0024,
    0x0025,
    0x0026,
    0x0027,
    0x0028,
    0x0029,
    0x002A,
    0x002B,
    0x002C,
    0x002D,
    0x00E4,
    0x00E5,
    0x00E6,
    0x00E7,
    0x00FA,
    0x00FB,
    0x3FA8,
    0x3FA9
  ]

  # Coordinates where doors shouldn't be placed
  @@blacklist = [
    [34, 36, 0],
    [68, 533, 0],
    [215, 559, 0],
    [558, 624, 0],
    [426, 696, 0],
    [426, 699, 0],
    [426, 701, 0],
    [528, 575, 20],
    [536, 575, 0],
    [535, 575, 20],
    [533, 575, 20],
    [537, 575, 0],
    [539, 575, 20],
    [543, 575, 0],
    [537, 575, 20],
    [541, 575, 20],
    [564, 629, 22],
    [565, 629, 22],
    [566, 629, 22],
    [539, 575, 0],
    [389, 528, 0],
    [389, 528, 22],
    [403, 587, 0],
    [479, 625, 0],
    [577, 727, 15],
    [605, 719, 41],
    [417, 520, 0],
    [418, 520, 0],
    [419, 520, 0],
    [421, 522, 0],
    [422, 522, 0],
    [426, 697, 0],
    [426, 700, 0],
    [426, 698, 0],
    [473, 590, 7],
    [407, 521, 0],
    [407, 521, 20],
    [407, 530, 20],
    [407, 543, 20],
    [419, 543, 20],
    [420, 536, 0],
    [420, 536, 20],
    [420, 536, 40],
    [434, 536, 0],
    [434, 536, 20],
    [434, 536, 40],
    [435, 543, 20],
    [435, 543, 40],
    [435, 520, 0],
    [436, 520, 0],
    [437, 520, 0],
    [447, 521, 0],
    [447, 521, 20],
    [447, 532, 20],
    [447, 543, 40],
    [447, 543, 20],
    [477, 591, 27],
    [533, 575, 0],
    [538, 575, 20],
    [540, 575, 0],
    [541, 575, 0],
    [542, 575, 20],
    [542, 575, 0],
    [420, 522, 0],
    [543, 642, 2],
    [540, 648, 8],
    [541, 648, 7],
    [538, 648, 8],
    [593, 657, 6],
    [594, 657, 6],
    [387, 601, 0],
    [387, 614, 0],
    [393, 621, 0],
    [536, 575, 20]
  ]

  def invoke(player, line)
    count = 0
    0.upto(1023) do |x|
      0.upto(1023) do |y|
        statics =  $api.getStaticsAtLocation(x, y)
        count += visit(x, y, statics)
      end
    end
    $api.sendSysMessage(player, "Done, generated #{count} doors")
  end

  def visit(x, y, statics)
    count = 0
    for static in statics
      z = static.getLocation().getZ()
      id = static.getStaticID()
      for pair in @@static_pairs
        if pair[0] == id
          count += findOther(x, y, z, pair[1], pair[2])
        elsif pair[1] == id
          count += findOther(x, y, z, pair[0], pair[2])
        end
      end
    end
    return count
  end

  def findOther(x, y, z, id, graphic)
    if hasStatic(x + 2, y, z, id) and isFree(x + 1, y, z, id)
      puts "placed door at #{x + 1} #{y} #{z}"
      door = $api.createItemAtLocation(x + 1, y, z, graphic, "door")
      return 1
    elsif hasStatic(x, y + 2, z, id) and isFree(x, y + 1, z, id)
      puts "placed door at #{x} #{y + 1} #{z}"
      door = $api.createItemAtLocation(x, y + 1, z, graphic + 8, "door")
      return 1
    elsif hasStatic(x + 3, y, z, id) and isFree(x + 1, y, z, id) and isFree(x + 2, y, z, id)
      puts "placed double door at #{x + 1} #{y} #{z}"
      door = $api.createItemAtLocation(x + 1, y, z, graphic, "door")
      door = $api.createItemAtLocation(x + 2, y, z, graphic + 2, "door")
      return 1
    elsif hasStatic(x, y + 3, z, id) and isFree(x, y + 1, z, id) and isFree(x, y + 2, z, id)
      puts "placed double door at #{x} #{y + 1} #{z}"
      door = $api.createItemAtLocation(x, y + 1, z, graphic + 10, "door")
      door = $api.createItemAtLocation(x, y + 2, z, graphic + 8, "door")
      return 1
    end
    return 0
  end

  def isFree(x, y, z, id)
    if x < 0 or x > 1023 or y < 0 or y > 1023
      return false
    end

    return false if x >= 381 && y <= 629 && (id == 0x140 || id == 0x141 || id == 0x142)
    return false if x >= 388 && x <= 392 && y == 860
    return false if x >= 43 && x <= 49 && y <= 34
    return false if x >= 529 && x <= 533 && y == 576
    return false if x >= 363 && x <= 385 && (y == 865 || y == 861)
    return false if x >= 8 && x <= 13 && y >= 108 && y <= 112 && z == 20
    return false if x >= 250 && x <= 264 && y >= 746 && y <= 774
    return false if x >= 362 && x <= 374 && y >= 626 && y <= 644
    return false if x >= 480 && x <= 502 && y == 521
    return false if x == 484 && y >= 523 && y <= 526
    return false if x == 361 && y >= 634 && y <= 640

    if @@blacklist.include?([x, y, z])
      return false
    end

    for static in $api.getStaticsAtLocation(x, y)
      return false if static.getLocation().getZ() == z and not @@whitelist.include?(static.getStaticID())
    end

    for item in $api.getItemsAtLocation(x, y, z)
      if item.getBehavior() == "door"
        # puts "#{x} #{y} #{z} already has a door"
        return false
      end
    end

    return true
  end

  def hasStatic(x, y, z, id)
    if x < 0 or x > 1023 or y < 0 or y > 1023
      return false
    end

    statics = $api.getStaticsAtLocation(x, y)
    for static in statics
      return true if static.getLocation().getZ() == z and static.getStaticID() == id
    end
    return false
  end
end
