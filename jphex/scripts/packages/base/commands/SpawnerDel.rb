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

# deletes spawner at player position
class SpawnerDel < TextCommand
  def invoke(player, line)
    for item in $api.getItemsAtLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ())
      if item.getBehavior() == "spawner"
        item.delete()
        $api.sendSysMessage(player, "Spawner deleted")
        return
      end
    end
    $api.sendSysMessage(player, "No spawner found")
  end
end
