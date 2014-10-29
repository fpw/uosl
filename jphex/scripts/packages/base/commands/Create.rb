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
class Create < TextCommand
  def invoke(player, line)
    if line == ""
      $api.sendSysMessage(player, "Usage: #create <graphic|behavior> [bag]")
      return
    end
    
    desc, where = line.split(" ")
    # check whether first parameter is integer or string (graphic or behavior)
    begin
      what = Integer(desc)
      if where == nil
        item = $api.createItemAtMobile(player, what)
      else
        item = $api.createItemInBackpack(player, what)
      end
    rescue
      # Couldn't parse integer -> item unset
    end
    
    if item == nil
      begin
        # Couldn't parse integer -> try as behavior
        what = desc
        if where == nil
          item = $api.createItemAtMobile(player, 0, what)
        else
          item = $api.createItemInBackpack(player, 0, what)
        end
      rescue
        # Couldn't parse behavior either, item still unset
      end
    end

    if item == nil
      $api.sendSysMessage(player, "Invalid parameters")
    end
  end
end
