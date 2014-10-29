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
class SetProp < TextCommand
  def invoke(player, line)
    what, type, valueStr = line.split(" ")
    if what == nil || type == nil || valueStr == nil
      $api.sendSysMessage(player, "Usage: #setprop <script property> <str|int> <value>")
      return
    end
    
    value = case type
      when "str" then String(valueStr)
      when "int" then Integer(valueStr)
      else nil 
    end
    
    if value == nil
      $api.sendSysMessage(player, "invalid property or property type")
      return
    end
    
    $api.targetObject(player) do |obj|
      $api.setObjectProperty(obj, what, value);
      $api.sendSysMessage(player, "#{what} set to #{value}")
    end
  end
end
