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

# Usage: Create spawner graphic (!) in your backpack, then set behavior, then doubleclick to activate
class Spawner
  include ItemBehavior

  @@spawn_types = {
    0x0439 => "orc",
    0x043A => "orccaptain",
    0x043B => "skeleton",
    0x043C => "wolf",
    0x043E => "deer",
    0x043F => "rabbit",
    0x0440 => "guard",
    0x044B => "blacksmith",
    0x044D => "cook",
    0x0451 => "mage",
    0x0455 => "provisioner"
  }
  
  def onCreate(spawner)
    spawner.lock()
  end

  def onBehaviorChange(spawner)
    behavior = @@spawn_types[spawner.getGraphic()]
    count = $api.getObjectProperty(spawner, "count")
    range = $api.getObjectProperty(spawner, "range")
    duration = $api.getObjectProperty(spawner, "duration")
    
    if behavior == nil or count == nil or range == nil or duration == nil
      # not setup yet
      return
    end
    $api.setObjectProperty(spawner, "spawned", [])
    spawner.lock()
    spawnLoop(spawner)
  end

  def onLoad(spawner)
    spawnLoop(spawner)
  end
    
  def onUse(player, spawner)
    if @@spawn_types[spawner.getGraphic()] == nil
      # Don't know what to spawn
      $api.sendSysMessage(player, "Invalid spawner type")
      return
    end
    
    if $api.getObjectProperty(spawner, "count") == nil
      $api.sendSysMessage(player, "Count not set, use #setprop count int <n>")
      return
    end
    
    if $api.getObjectProperty(spawner, "range") == nil
      $api.sendSysMessage(player, "Range not set, use #setprop range int <n>")
      return
    end
    
    if $api.getObjectProperty(spawner, "duration") == nil
      $api.sendSysMessage(player, "Duration not set, use #setprop duration int <n> (in minutes!)")
      return
    end

    $api.moveObject(spawner, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ())
    $api.setObjectProperty(spawner, "spawned", [])
    spawnLoop(spawner)
  end
  
  def spawnLoop(spawner)
    behavior = @@spawn_types[spawner.getGraphic()]
    count = $api.getObjectProperty(spawner, "count")
    range = $api.getObjectProperty(spawner, "range")
    duration = $api.getObjectProperty(spawner, "duration")
    spawnList = $api.getObjectProperty(spawner, "spawned")

    if behavior == nil or count == nil or range == nil or duration == nil or spawnList == nil
      puts "deleting invalid spawner #{spawner.getSerial()}"
      $api.deleteObject(spawner)
      return
    end

    newSpawnList = []
    for serial in spawnList
      if $api.findObject(serial) != nil
        newSpawnList << serial
      end
    end

    if newSpawnList.count < count
      location = $api.getRandomPointInRange(spawner.getLocation(), range)
      puts "spawning #{behavior} at #{location}"
      npc = $api.spawnMobileAtLocation(location.getX(), location.getY(), location.getZ(), behavior)
      newSpawnList << npc.getSerial()
    end
    $api.setObjectProperty(spawner, "spawned", newSpawnList)
    $api.addTimer(duration * 1000 * 60) do
      spawnLoop(spawner)
    end
  end
end
