package org.solhost.folko.uosl.slclient.models;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.network.packets.SLPacket;

public class Connection {
    private static final Logger log = Logger.getLogger("slclient.connection");
    private static final int DEFAULT_PORT = 2590;
    private static final int BUFFER_SIZE = 65536;
    private final Selector selector;
    private final SocketChannel socketChan;
    private final Thread connectionThread;
    private final ByteBuffer recvBuffer, sendBuffer;
    private final Object selectMutex;
    private boolean enableWrite, disableWrite;
    private final ConnectionHandler handler;
    private String host;
    private int port;

    public interface ConnectionHandler {
        void onConnected();
        void onIncomingPacket(SLPacket packet);
        void onRemoteDisconnect();
        void onNetworkError(String reason);
    }


    public Connection(ConnectionHandler handler, String destination) throws IOException {
        this.handler = handler;
        this.selectMutex = new Object();

        String[] info = destination.split(":");
        if(info.length == 1) {
            host = info[0];
            port = DEFAULT_PORT;
        } else if(info.length == 2) {
            host = info[0];
            port = Integer.parseInt(info[1]);
        } else {
            throw new IllegalArgumentException("Invalid format");
        }

        recvBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        recvBuffer.order(ByteOrder.BIG_ENDIAN);
        sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        sendBuffer.order(ByteOrder.BIG_ENDIAN);

        selector = Selector.open();
        socketChan = SocketChannel.open();
        socketChan.configureBlocking(false);
        socketChan.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        connectionThread = new Thread(() -> loop());
        connectionThread.setDaemon(true);
        connectionThread.start();

        connect();
    }

    private synchronized void connect() throws IOException {
        try {
            log.info("Connecting to " + host + ", port " + port);
            InetSocketAddress addr = new InetSocketAddress(host, port);

            if(socketChan.connect(addr)) {
                // immediate success
                log.fine("Immediate success when connecting");
                handler.onConnected();
            }
        } catch(Exception e) {
            log.warning("Couldn't connect: " + e.getMessage());
            disconnect();
            throw e;
        }
    }

    public synchronized void disconnect() {
        try {
            log.fine("Disconnecting connection");
            selector.wakeup();
            connectionThread.interrupt();
            socketChan.close();
        } catch (IOException e) {
            // network error on shutdown is OK, so don't propagate
            log.fine("Exception on channel close: " + e.getMessage());
        }
    }

    private void loop() {
        log.fine("Running network loop");
        while(!Thread.interrupted()) {
            try {
                selector.select();
                for(SelectionKey key : selector.selectedKeys()) {
                    if(!key.isValid()) {
                        log.warning("Invalid key in select");
                        handler.onNetworkError("Network Error: select");
                        key.cancel();
                        continue;
                    }

                    if(key.isConnectable()) {
                        onConnect();
                    } else if(key.isReadable()) {
                        onReadable();
                    } else if(key.isWritable()) {
                        onWritable();
                    }
                }

                selector.selectedKeys().clear();

                synchronized(selectMutex) {
                    SelectionKey key = socketChan.keyFor(selector);
                    if(key == null || !key.isValid()) {
                        log.warning("Key became invalid when setting interestOps");
                        handler.onRemoteDisconnect();
                        break;
                    }

                    if(disableWrite && !enableWrite) {
                        disableWrite = false;
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    if(enableWrite) {
                        enableWrite = false;
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                }
            } catch (IOException e) {
                synchronized(this) {
                    handler.onNetworkError(e.getMessage());
                }
                break;
            }
        }
        log.fine("Left network loop");
    }

    private synchronized void onConnect() {
        try {
            socketChan.finishConnect();
        } catch(Exception e) {
            log.warning("Connect failed: " + e.getMessage());
            handler.onNetworkError(e.getMessage());
            return;
        }
        handler.onConnected();
    }

    public synchronized void sendPacket(SLPacket packet) {
        boolean needEnable = false;

        synchronized(sendBuffer) {
            if(sendBuffer.position() == 0) {
                // there was nothing to send before, enable select notification for write-ready
                needEnable = true;
            }
            try {
                // append packet to sendBuffer
                packet.writeTo(sendBuffer);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Exception when writing packet: " + e.getMessage(), e);
                handler.onRemoteDisconnect();
                needEnable = false;
            }
        }
        if(needEnable) {
            synchronized(selectMutex) {
                enableWrite = true;
                selector.wakeup();
            }
        }
    }

    private synchronized void onWritable() throws IOException {
        log.finest("Sending out write buffer");
        boolean needDisable = false;
        synchronized(sendBuffer) {
            sendBuffer.flip();
            socketChan.write(sendBuffer);
            sendBuffer.compact();
            if(sendBuffer.position() == 0) {
                // buffer empty again -> disable write notification
                needDisable = true;
            }
        }
        if(needDisable) {
            synchronized(selectMutex) {
                disableWrite = true;
                selector.wakeup();
            }
        }
    }

    private synchronized void onReadable() {
        List<SLPacket> packets = new ArrayList<>(5);

        try {
            int bytesRead = socketChan.read(recvBuffer);
            if(bytesRead == -1) {
                // normal shutdown of the client
                log.fine("Client shutdown while reading");
                handler.onRemoteDisconnect();
                return;
            }

            SLPacket lastPacket = null;
            do {
                recvBuffer.flip();
                lastPacket = SLPacket.readPacket(recvBuffer);
                if(lastPacket != null) {
                    // got a full packet, add to queue
                    packets.add(lastPacket);
                    recvBuffer.compact();
                } else {
                    // didn't get a full packet, try again next time
                    recvBuffer.position(recvBuffer.limit());
                    recvBuffer.limit(recvBuffer.capacity());
                }
            } while(lastPacket != null);
        } catch (IOException e) {
            handler.onNetworkError("Read error: " + e.getMessage());
            return;
        }

        // add all received packets to handler
        for(SLPacket packet : packets) {
            try {
                handler.onIncomingPacket(packet);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Error when handling incoming packet: " + e.getMessage(), e);
            }
        }
    }
}
