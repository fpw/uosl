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

# For closed doors, the layer attribute of the tiles has values 0 to 7:
# 0: hinges west, opens clockwise
# 1: hinges east, opens counter-clockwise
# 2: hinges west, opens counter-clockwise
# 3: hinges east, opens clockwise
# 4: hinges south, opens clockwise
# 5: hinges north, opens counter-clockwise
# 6: hinges south, opens counter-clockwise
# 7: hinges north, opens clockwise

# doors are stored as whole sets, so a base id is enough to derive
# all information: they are stored in the above order with the closed
# graphic between the entries, so a full set has 16 entries

class Door
  include ItemBehavior

  @@auto_close_time = 4000

  @@door_sets = [0x3EC1, # iron door
                 0x3EDC, # wooden door tall
                 0x3EEC, # wooden door small
                 0x3EFC, # wooden door normal
                 0x3F0C, # wooden gate small
                 0x3F1C, # wrought iron gate
                 0x3F2C] # secret door
  
  @@door_offsets = [
          [-1,  1],
          [ 1,  1],
          [-1,  0],
          [ 1, -1],
          [ 1,  1],
          [ 1, -1],
          [ 0,  1],
          [ 0, -1]
      ]
      
  def onCreate(door)
    if door.getGraphic() == 0
      $api.setGraphic(door, 0x3EEC)
    end
    # locking means "can't be dragged" in this context, actually locking doesn't exist because there are no keys
    door.lock()
  end

  def onBehaviorChange(door)
    door.lock()
  end

  def onLoad(door)
    # Close doors on server load
    close(door)
  end
  
  def getSet(door)
    graphic = door.getGraphic()
    for set in @@door_sets
      return set if graphic - set >= 0 and graphic - set <= 15
    end
  end
  
  def isOpen(door)
    return (door.getGraphic() - getSet(door)) % 2 == 1
  end
  
  def getFacing(door)
    if isOpen(door)
      return (door.getGraphic() - getSet(door) - 1) / 2
    else
      return (door.getGraphic() - getSet(door)) / 2
    end
  end

  
  def onUse(player, door)
    if isOpen(door)
      close(door)
    else
      open(door)
    end
  end
  
  def open(door)
    return if isOpen(door)
  
    facing = getFacing(door)
    offset = @@door_offsets[facing]

    door.setGraphic(door.getGraphic() + 1)
    $api.moveObject(door, door.getLocation().getX() + offset[0], door.getLocation().getY() + offset[1], door.getLocation().getZ())
    playOpenSound(door)

    # closeAt is used when people close and open the door while the timer is active
    $api.setObjectProperty(door, "closeAt", $api.getTimerTicks() + @@auto_close_time)
    $api.addTimer(@@auto_close_time) do
      close(door) if $api.getObjectProperty(door, "closeAt") <= $api.getTimerTicks()
    end
  end
  
  def close(door)
    return if !isOpen(door)

    facing = getFacing(door)
    offset = @@door_offsets[facing]
    
    door.setGraphic(door.getGraphic() - 1)
    $api.moveObject(door, door.getLocation().getX() - offset[0], door.getLocation().getY() - offset[1], door.getLocation().getZ())
    playCloseSound(door)
  end
  
  def playOpenSound(door)
    sound = case getSet(door)
      when 0x3EC1 then 0x44
      when 0x3EDC then 0x42
      when 0x3EEC then 0x42
      when 0x3EFC then 0x42
      when 0x3F0C then 0x43
      when 0x3F1C then 0x44
      when 0x3F2C then 0x45
      else 0x42
    end
    $api.playSoundNearObj(door, sound)
  end

  def playCloseSound(door)
    sound = case getSet(door)
      when 0x3EC1 then 0x4A
      when 0x3EDC then 0x49
      when 0x3EEC then 0x48
      when 0x3EFC then 0x48
      when 0x3F0C then 0x49
      when 0x3F1C then 0x4A
      when 0x3F2C then 0x4B
      else 0x42
    end
    $api.playSoundNearObj(door, sound)
  end
end
