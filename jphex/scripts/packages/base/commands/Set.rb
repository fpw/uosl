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
class Set < TextCommand
  def invoke(player, line)
    what, value = line.split(" ")
    if what == nil || value == nil
      $api.sendSysMessage(player, "Usage: #set <property> <value>")
      return
    end
    $api.targetObject(player) do |obj|
      case what
        when "amount"   then obj.setAmount(Integer(value))
        when "hue"      then obj.setHue(Integer(value))
        when "graphic"  then obj.setGraphic(Integer(value))
        when "light"    then obj.setLightLevel(Integer(value))
        when "x"        then $api.moveObject(obj,  Integer(value), obj.getLocation().getY()) 
        when "y"        then $api.moveObject(obj, obj.getLocation().getX(), Integer(value))
        when "z"        then $api.moveObject(obj, obj.getLocation().getX(), obj.getLocation().getY(), Integer(value))
        when "magic"    then obj.setAttribute(Attribute::MAGIC, Integer(value))
        when "melee"    then obj.setAttribute(Attribute::MELEE, Integer(value))
        when "peek"     then obj.setAttribute(Attribute::PEEK, Integer(value))
        when "stealing" then obj.setAttribute(Attribute::STEALING, Integer(value))
        when "int"      then obj.setAttribute(Attribute::INTELLIGENCE, Integer(value))
        when "mana"     then obj.setAttribute(Attribute::MANA, Integer(value))
        when "hits"     then obj.setAttribute(Attribute::HITS, Integer(value))
        when "fatigue"  then obj.setAttribute(Attribute::FATIGUE, Integer(value))
        when "exp"      then obj.rewardAttribute(Attribute::EXPERIENCE, Integer(value))
        when "locked"   then if value == "1" then obj.lock() else obj.unlock() end
      end
    end
  end
end
