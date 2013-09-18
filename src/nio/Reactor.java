package nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * the class representing the reactor in NIO model,
 */
public abstract class Reactor implements Runnable {

    private Selector serverSelector, clientSelector;
    private boolean clientRole = false;
    private ServerSocketChannel serverSocket;
    private HashMap<String, SocketChannel> serverChannelMap = null;  //for client use
    private HashMap<SocketChannel, ArrayList<ByteBuffer>> clientWriteBuffer = null;//for client use


    public Reactor(){}

    public Reactor(InetAddress hostAddress, int port) {
        try {
            //initialize serverSelector
            serverSelector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(hostAddress, port));
            serverSocket.configureBlocking(false);
            serverSocket.register(serverSelector, SelectionKey.OP_ACCEPT);
            //initialize clientSelector
            clientSelector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * send data to the server which is identified with its logical name
     * @param serverName
     * @param data
     */
    protected void send(String serverName, byte[] data) {
        assert(serverChannelMap.containsKey(serverName));
        SocketChannel socket = serverChannelMap.get(serverName);
        assert(socket != null);
        clientWriteBuffer.get(socket).add(ByteBuffer.wrap(data));
    }

    /**
     * this function is optinally called, when you want to make the reactor in both of the role of
     * client and server, you can call this
     * @param serverAddress
     * @param port
     */
    protected void setClientEndPoint(String socketID, InetAddress serverAddress, int port) {
        try {
            if (!clientRole) {
                clientRole = true;
                serverChannelMap = new HashMap<String, SocketChannel>();
                clientWriteBuffer = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();
            }
            // Create a non-blocking socket channel
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverAddress, port));
            socketChannel.register(clientSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            serverChannelMap.put(socketID, socketChannel);
            clientWriteBuffer.put(socketChannel, new ArrayList<ByteBuffer>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(serverSelector, SelectionKey.OP_READ);
    }

    /**
     * write the buffer to the channel
     * @param key channelkey
     */
    private void write(SelectionKey key) {
        SocketChannel socket = (SocketChannel) key.channel();
        ArrayList<ByteBuffer> bytebufferlist = clientWriteBuffer.get(socket);
        try {
            while (!bytebufferlist.isEmpty()) {
                socket.write(bytebufferlist.get(0));
                if (bytebufferlist.get(0).remaining() > 0) {
                    //tcp buffer is full
                    break;
                }
                bytebufferlist.remove(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Message read(SelectionKey key) {
        try {
            //assuming that the message is never larger than 8192 bytes
            ByteBuffer readBuffer = ByteBuffer.allocate(8192);
            SocketChannel socketChannel = (SocketChannel) key.channel();
            readBuffer.clear();
            int readbytes = socketChannel.read(readBuffer);
            if (readbytes == -1) {
                key.channel().close();
                key.cancel();
                return null;
            }
            ByteBuffer ret = ByteBuffer.allocate(readbytes);
            ret.put(readBuffer.array(), 0, readbytes);
            return Message.deserialize(ret.array());
        } catch (IOException e) {
            //key.cancel();
            //((SocketChannel) key.channel()).close();
            e.printStackTrace();
            return null;
        }
    }



    private class serverGo implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    serverSelector.select();
                    Set selected = serverSelector.selectedKeys();
                    for (Object aSelected : selected) {
                        //accept the connection
                        SelectionKey selectkey = (SelectionKey) aSelected;
                        if (selectkey.isAcceptable()) {
                            accept(selectkey);
                        } else {
                            if (selectkey.isReadable()) {
                                dispatch(read(selectkey));
                            }
                        }
                    }
                    selected.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class clientGo implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    clientSelector.select();
                    Set selected = clientSelector.selectedKeys();
                    for (Object aSelected : selected) {
                        //accept the connection
                        SelectionKey selectkey = (SelectionKey) aSelected;
                        if (selectkey.isWritable()) {
                            write(selectkey);
                        } else {
                            if (selectkey.isReadable()) {
                                dispatch(read(selectkey));
                            }
                        }
                    }
                    selected.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        Thread t_server = new Thread(new serverGo());
        t_server.start();
        if (clientRole) {
            Thread t_client = new Thread(new clientGo());
            t_client.start();
        }
    }


    abstract void dispatch(Message msg);
}
