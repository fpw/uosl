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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.network.packets.SLPacket;

public class Server {
    public interface ErrorHandler {
        public void onError(String message);
    }

    private static final Logger log = Logger.getLogger("jphex.server");
    private final IPacketHandler handler;
    private ServerSocketChannel serverSocket;
    private Selector selector;
    private final Map<SocketChannel, Client> clients;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    private final int port;
    private final Set<Client> writeRequestsOn, writeRequestsOff;
    private final Object selectLock;

    public Server(int port, IPacketHandler handler) {
        this.handler = handler;
        this.port = port;
        this.clients = new HashMap<SocketChannel, Client>();
        this.writeRequestsOn = new HashSet<Client>();
        this.writeRequestsOff = new HashSet<Client>();
        this.selectLock = new Object();
    }

    public void listen() throws IOException {
        selector = SelectorProvider.provider().openSelector();
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void eventLoop(ErrorHandler errorHandler) {
        while(!Thread.interrupted()) {
            try {
                selector.select();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Couldn't select() on server socket: " + e.getMessage(), e);
                errorHandler.onError("select failed");
                break;
            }

            for(SelectionKey key : selector.selectedKeys()) {
                // keys can become invalid at any time, so always check validity before checking something else
                if(key.isValid() && key.isAcceptable()) {
                    // pending connection
                    onAcceptable((ServerSocketChannel) key.channel());
                }

                if(key.isValid() && key.isReadable()) {
                    // pending bytes to be read
                    boolean success = onReadable((SocketChannel) key.channel());
                    if(!success) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException e) {
                            // doesn't matter because we were closing it anyways
                        }
                        continue;
                    }
                }

                if(key.isValid() && key.isWritable()) {
                    // ready to write pending data
                    boolean success = onWritable((SocketChannel) key.channel());
                    if(!success) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException e) {
                            // doesn't matter because we were closing it anyways
                        }
                        continue;
                    }
                }
            }

            selector.selectedKeys().clear();

            synchronized(selectLock) {
                for(Client client : writeRequestsOff) {
                    if(writeRequestsOn.contains(client)) {
                        // this fixes an ugly race condition
                        continue;
                    }

                    SelectionKey key = client.getChannel().keyFor(selector);
                    if(key == null || !key.isValid()) continue;
                    key.interestOps(SelectionKey.OP_READ);
                }
                writeRequestsOff.clear();

                for(Client client : writeRequestsOn) {
                    SelectionKey key = client.getChannel().keyFor(selector);
                    if(key == null || !key.isValid()) continue;
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }
                writeRequestsOn.clear();
            }
        }
    }

    public void disconnect(Client client) {
        SocketChannel channel = client.getChannel();
        if(channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // doesn't matter as we're disconnecting them anyways
            }
        }
        clients.remove(client);
        handler.onDisconnect(client);
    }

    // client should call this when it wants to write
    public void writeRequest(Client client) {
        synchronized(selectLock) {
            writeRequestsOn.add(client);
            selector.wakeup();
        }
    }

    // disable client write ready notification
    public void stopWriteRequest(Client client) {
        synchronized(selectLock) {
            writeRequestsOff.add(client);
            selector.wakeup();
        }
    }

    private void onAcceptable(ServerSocketChannel channel) {
        SocketChannel clientChannel;
        Client client;
        try {
            clientChannel = channel.accept();
            if(clientChannel == null) {
                return;
            }
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            client = new Client(clientChannel, this);
        } catch (IOException e) {
            log.warning("Error accepting client: '" + e.getMessage() + "', ignoring");
            return;
        }
        clients.put(clientChannel, client);
        handler.onNewConnection(client);
    }

    private boolean onReadable(SocketChannel channel) {
        Client client = clients.get(channel);
        if(client == null) {
            return false;
        }

        readBuffer.clear();
        List<SLPacket> packets;

        try {
            int bytesRead = channel.read(readBuffer);
            if(bytesRead == -1) {
                // normal shutdown of the client
                disconnect(client);
                return false;
            }

            packets = client.processReadData(readBuffer.array(), bytesRead);
        } catch (IOException e) {
            log.warning("Read error from client " + client.getRemoteAddress() + ": " + e.getMessage());
            disconnect(client);
            return false;
        }

        // add all received packets to handler
        for(SLPacket packet : packets) {
            try {
                handler.onIncomingPacket(client, packet);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Exception when handling incoming packet: " + e.getMessage(), e);
            }
        }

        return true;
    }

    private boolean onWritable(SocketChannel channel) {
        Client client = clients.get(channel);
        if(client == null) {
            return false;
        }

        try {
            client.writeNow();
        } catch(IOException e) {
            log.warning("Write error from client " + client.getRemoteAddress() + ": " + e.getMessage());
            disconnect(client);
            return false;
        }

        return true;
    }
}
