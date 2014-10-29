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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.solhost.folko.uosl.jphex.types.Player;
import org.solhost.folko.uosl.libuosl.network.packets.BoardPostPacket;
import org.solhost.folko.uosl.libuosl.network.packets.BoardSubjectPacket;

public class BulletinBoard implements Serializable {
    private static final long serialVersionUID = 1L;

    private class Post implements Serializable {
        private static final long serialVersionUID = 1L;
        String author, subject, message;
    }

    private final List<Post> posts;

    public BulletinBoard() {
        this.posts = new LinkedList<Post>();
    }

    public void sendList(Player player) {
        for(int i = 0; i < posts.size(); i++) {
            BoardSubjectPacket packet = new BoardSubjectPacket(i, posts.get(i).subject);
            player.sendPacket(packet);
        }
    }

    public synchronized void postMessage(Player player, String subject, String message) {
        Post post = new Post();
        post.author = player.getName();
        post.subject = subject;
        post.message = message;
        posts.add(post);

        BoardSubjectPacket packet = new BoardSubjectPacket(posts.size() - 1, post.subject);
        player.sendPacket(packet);
    }

    public void sendPost(Player player, int index) {
        if(index >= posts.size()) {
            return;
        }
        Post post = posts.get(index);
        BoardPostPacket packet = new BoardPostPacket(post.subject, post.message + "\r\n\r\n(Signed by: " + post.author + ")");
        player.sendPacket(packet);
        return;
    }
}
