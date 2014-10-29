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
package org.solhost.folko.uosl.jphex.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.network.packets.SLPacket;

public class Client {
    private static final Logger log = Logger.getLogger("jphex.client");
    private static final int BUFFER_SIZE = 65536;
    private final Server server;
    private final SocketChannel channel;
    private InetSocketAddress remoteAddress;
    private final ByteBuffer recvBuffer, sendBuffer;

    public Client(SocketChannel channel, Server server) throws IOException {
        this.channel = channel;
        this.server = server;
        SocketAddress sockAddr = channel.getRemoteAddress();

        if(sockAddr instanceof InetSocketAddress) {
            remoteAddress = (InetSocketAddress) sockAddr;
        } else {
            throw new IOException("invalid socket address");
        }

        this.recvBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.recvBuffer.order(ByteOrder.BIG_ENDIAN);

        this.sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.sendBuffer.order(ByteOrder.BIG_ENDIAN);
    }

    public List<SLPacket> processReadData(byte[] data, int len) throws IOException {
        List<SLPacket> packets = new ArrayList<SLPacket>(5);
        try {
            recvBuffer.put(data, 0, len);
        } catch(BufferOverflowException e) {
            throw new IOException("buffer overflow when reading packet");
        }

        SLPacket lastPacket = null;
        do {
            recvBuffer.flip();
            lastPacket = SLPacket.readPacket(recvBuffer);
            if(lastPacket != null) {
                // got a full packet, add to queue
                log.finest(String.format("Got from %s: %02X (%s)", getRemoteAddress(), lastPacket.getID(), lastPacket.getClass().getSimpleName()));
                packets.add(lastPacket);
                recvBuffer.compact();
            } else {
                // didn't get a full packet, try again next time
                recvBuffer.position(recvBuffer.limit());
                recvBuffer.limit(recvBuffer.capacity());
            }
        } while(lastPacket != null);

        // no more packets, return received ones
        return packets;
    }

    public void disconnect() {
        server.disconnect(this);
    }

    public void send(SLPacket packet) {
        log.finest(String.format("Sending to %s: %s (%s)", getRemoteAddress(), packet.getClass().getSimpleName(), packet.toString()));
        boolean needEnable = false;

        synchronized (sendBuffer) {
            if(sendBuffer.position() == 0) {
                // there was nothing to send before, enable select notification for write-ready
                needEnable = true;
            }
            try {
                // append packet to sendBuffer
                packet.writeTo(sendBuffer);
            } catch (IOException e) {
                server.disconnect(this);
                needEnable = false;
            }
        }
        if(needEnable) {
            server.writeRequest(this);
        }
    }

    public void writeNow() throws IOException {
        boolean needDisable = false;
        synchronized(sendBuffer) {
            sendBuffer.flip();
            channel.write(sendBuffer);
            sendBuffer.compact();
            if(sendBuffer.position() == 0) {
                // buffer empty again -> disable write notification
                needDisable = true;
            }
        }
        if(needDisable) {
            server.stopWriteRequest(this);
        }
    }

    public String getRemoteAddress() {
        return remoteAddress.toString();
    }

    SocketChannel getChannel() {
        return channel;
    }
}
