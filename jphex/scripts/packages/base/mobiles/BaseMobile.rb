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
class BaseMobile
  include MobileBehavior

  @@range = 15
  @@run_delay = 500

  # supported: :aggressive, :responding, :shy
  def setType(mob, type)
    $api.setObjectProperty(mob, "type", type)
    $api.setObjectProperty(mob, "state", :idle)
  end
  
  def setStats(mob, stats)
    str = stats[:str] || 1
    dex = stats[:dex] || 1
    int = stats[:int] || 1

    if str.is_a?(Range)
      str = rand(stats[:str])
    end

    if dex.is_a?(Range)
      dex = rand(stats[:dex])
    end

    if int.is_a?(Range)
      int = rand(stats[:int])
    end

    $api.setAttribute(mob, Attribute::STRENGTH, str)
    $api.setAttribute(mob, Attribute::DEXTERITY, dex)
    $api.setAttribute(mob, Attribute::INTELLIGENCE, int)
    $api.refreshStats(mob)
  end
  
  def onLoad(mob)
    $api.setObjectProperty(mob, "state", :idle)
  end

  def onSpawn(mob)
  end

  def onDoubleClick(me, player)
    return false
  end

  def onSpeech(mob, player, line)
  end

  def onHello(me, player)
  end

  def onDeath(mob, corpse)
  end

  def onEnterArea(mob, player)
    # A mob enters the area: only react if idle, delegate player selection to methods
    type = $api.getObjectProperty(mob, "type")
    case $api.getObjectProperty(mob, "state")
    when :idle
      case type
      when :shy
        $api.setObjectProperty(mob, "state", :getting_away)
        runAway(mob)
      when :aggressive
        beAggressive(mob)
      when :responding
        return
      end 
    end
  end

  def runAway(mob)
    return if $api.getObjectProperty(mob, "state") != :getting_away or mob.isDeleted()
    nearest = $api.getNearestMobile(mob)
    if nearest == nil
      # No more mobs in range, we can go idling again
      $api.setObjectProperty(mob, "state", :idle)
      return
    end
    $api.runAway(mob, nearest)
    # No matter if running away worked or not: try again after delay, opponent could change direction
    $api.addTimer(@@run_delay) { runAway(mob) }
  end

  # Fight nearest victim
  def beAggressive(mob)
    return if mob.isDeleted()
    nearest = $api.getNearestMobile(mob)
    if nearest == nil
      # No more mobs in range, we can go idling again
      $api.setObjectProperty(mob, "state", :idle)
    else
      # Found a mobile, attack them
      $api.setObjectProperty(mob, "state", :fighting)
      doFight(mob, nearest)
    end
  end

  # Fight chosen victim
  def doFight(mob, victim)
    return if $api.getObjectProperty(mob, "state") != :fighting or mob.isDeleted()
    dist = $api.getDistance(mob, victim)
    if dist > @@range
      case $api.getObjectProperty(mob, "type")
      when :shy
        $api.setObjectProperty(mob, "state", :getting_away)
        runAway(mob)
      when :aggressive
        beAggressive(mob)
      when :responding
        $api.setObjectProperty(mob, "state", :idle)
      end
      return 
    end

    if not victim.isVisible()
      # Somehow got away, check what to do now
      case $api.getObjectProperty(mob, "type")
      when :shy
        $api.setObjectProperty(mob, "state", :idle)
      when :responding
        $api.setObjectProperty(mob, "state", :idle)
      when :aggressive
        beAggressive(mob)
      end
      return
    end

    if dist > 1
      # We need to run towards them because they are not out of range but not in attack range
      $api.runToward(mob, victim)
    else
      # We are close enough to attack
      $api.attack(mob, victim)
    end
    
    # In both cases we need to check again because the victim could run away
    $api.addTimer(@@run_delay) { doFight(mob, victim) }
  end

  def onAttacked(mob, attacker)
    # A mob attacks us: only respond when not doing something else already
    type = $api.getObjectProperty(mob, "type")
    case $api.getObjectProperty(mob, "state")
    when :idle
      case type
      when :shy
        $api.setObjectProperty(mob, "state", :getting_away)
        runAway(mob)
      when :aggressive
        beAggressive(mob)
      when :responding
        $api.setObjectProperty(mob, "state", :fighting)
        doFight(mob, attacker)
      end 
    when :getting_away
      # We are taking damage even though we were trying to run - be brave and fight back
      $api.setObjectProperty(mob, "state", :fighting)
      doFight(mob, attacker)
    end
  end

  def generateLoot(corpse, table)
    for entry in table
      next if rand() > entry[:chance]
      amount = entry[:amount] || 1
      if amount.is_a?(Range)
        amount = rand(amount)
      end 
      count = entry[:count] || 1
      if count.is_a?(Range)
        count = rand(count)
      end

      1.upto(count) do
        graphic = entry[:graphic] || 0
        if entry[:behavior] != nil
          item = $api.createItemInContainer(corpse, graphic, entry[:behavior])
        else
          item = $api.createItemInContainer(corpse, graphic)
        end
        item.setAmount(amount)
      end
    end
  end 
end
