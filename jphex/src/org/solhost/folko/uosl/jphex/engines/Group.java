/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.uosl.jphex.engines;

import java.util.LinkedList;
import java.util.List;

import org.solhost.folko.uosl.jphex.types.Player;
import org.solhost.folko.uosl.libuosl.network.packets.GroupPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SendTextPacket;
import org.solhost.folko.uosl.libuosl.types.Attribute;

// a group of players that shares experience points
public class Group {
    private final Player leader;
    private final List<Player> members;
    private final List<Player> pendingByLeader; // leader must confirm join
    private final List<Player> pendingByMember; // member must confirm join

    public Group(Player leader) {
        this.leader = leader;
        this.members = new LinkedList<Player>();
        this.pendingByLeader = new LinkedList<Player>();
        this.pendingByMember = new LinkedList<Player>();

        members.add(leader);
    }

    public static void onGroupRequest(Player player, Player addingPlayer, Player addedPlayer) {
        if(player != addingPlayer) {
            player.sendSysMessage("Invalid group request");
            return;
        }

        if(player == addedPlayer) {
            leaveGroup(addedPlayer);
            return;
        }

        Group group = addingPlayer.getGroup();
        if(group == null) {
            if(addedPlayer.getGroup() == null) {
                // addingPlayer wants to create a new group with addedPlayer
                group = new Group(addingPlayer);
                addingPlayer.setGroup(group);
                group.pendingByMember.add(addedPlayer);
                addedPlayer.sendSysMessage(addingPlayer.getName() + " wants to add you to his group. Hold ALT and doubleclick them to confirm.");
            } else {
                group = addedPlayer.getGroup();
                if(group.leader == addedPlayer) {
                    // addingPlayer wants to join addedPlayer's group, check if pending
                    if(group.pendingByMember.contains(addingPlayer)) {
                        // player joined
                        group.members.add(addingPlayer);
                        addingPlayer.setGroup(group);
                        group.leader.sendSysMessage(addingPlayer.getName() + " joined your group");
                        GroupPacket packet = new GroupPacket(addingPlayer, group.leader);
                        addingPlayer.sendPacket(packet);
                        group.pendingByMember.remove(addingPlayer);
                    } else {
                        // player requests
                        group.leader.sendSysMessage(addingPlayer.getName() + " wants to join your group. Hold ALT and doubleclick them to confirm");
                        group.pendingByLeader.add(addingPlayer);
                    }
                } else {
                    // addingPlayer wants to join a non-leader
                    addingPlayer.sendSysMessage("Please ask " + group.leader.getName() + " to join their group.");
                }
            }
        } else if(addingPlayer == group.leader) {
            // leader wants to add new member to his group
            if(group.pendingByLeader.contains(addedPlayer)) {
                // confirmed join request
                group.members.add(addedPlayer);
                addedPlayer.setGroup(group);
                group.leader.sendSysMessage(addingPlayer.getName() + " joined your group");
                GroupPacket packet = new GroupPacket(addedPlayer, group.leader);
                addedPlayer.sendPacket(packet);
                group.pendingByLeader.remove(addedPlayer);
            } else if(group.pendingByMember.contains(addedPlayer)) {
                addingPlayer.sendSysMessage(addedPlayer.getName() + " was already invited to your group and can accept the invitation.");
            } else if(group.members.contains(addedPlayer)){
               addingPlayer.sendSysMessage(addedPlayer.getName() + " is already in your group.");
            } else {
                // want to ask them to join
                group.pendingByMember.add(addedPlayer);
                addedPlayer.sendSysMessage(addingPlayer.getName() + " wants to add you to his group. Hold ALT and doubleclick them to confirm.");
            }
        } else {
            // non-leader wants to add a member
            player.sendSysMessage("Only the group's leader may add members. If you wish to leave the group, hold ALT and doubleclick yourself");
        }
    }

    public static void leaveGroup(Player player) {
        Group group = player.getGroup();
        if(group == null) {
            return;
        }

        if(group.leader == player) {
            for(Player member : group.members) {
                GroupPacket packet = new GroupPacket(null, member);
                member.sendPacket(packet);
                member.sendSysMessage("Your group has been disbanded");
                member.setGroup(null);
            }
            group.members.clear();
        } else {
            group.members.remove(player);
            player.setGroup(null);
            player.sendSysMessage("You have left the group");

            for(Player member : group.members) {
                GroupPacket packet = new GroupPacket(null, member);
                member.sendPacket(packet);
                member.sendSysMessage(player.getName() + " left your group");
            }
        }
    }

    public static void onGroupChat(Player player, String line, long color) {
        Group group = player.getGroup();
        if(group == null) {
            player.sendSysMessage("You are not in a group. Switch back to normal speech mode using the ' key");
            return;
        }
        SendTextPacket packet = new SendTextPacket(player, SendTextPacket.MODE_SYSMSG, color, line);
        for(Player member : group.members) {
            member.sendPacket(packet);
        }
    }

    public void rewardExperience(long points) {
        for(Player member : members) {
            member.rewardAttribute(Attribute.EXPERIENCE, points / members.size());
        }
    }
}
