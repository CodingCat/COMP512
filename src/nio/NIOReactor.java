package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * the class representing the reactor in NIO model,
 */
public abstract class NIOReactor implements Runnable {

    private Selector serverSelector, clientSelector;
    private boolean clientRole = false;
    private HashMap<Integer, SocketChannel> clientConnections = null;
    private HashMap<SocketChannel, ArrayList<ByteBuffer>> clientWriteBuffer = null;
    private ServerSocketChannel serverSocket;
    private HashMap<String, SocketChannel> channelMap = null;  //for client use
    private HashMap<SocketChannel, ArrayList<ByteBuffer>> forwardBuffer = null;//for client use


    public NIOReactor(String hostIP, int port) {
        try {
            //initialize serverSelector
            clientConnections = new HashMap<Integer, SocketChannel>();
            clientWriteBuffer = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();
            serverSelector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(hostIP, port));
            serverSocket.configureBlocking(false);
            serverSocket.register(serverSelector, SelectionKey.OP_ACCEPT);
            //initialize clientSelector
            clientSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void reply(Message msg) {
        try {
            int key = msg.transactionIDs.get(msg.transactionIDs.size() - 1);
            System.out.println(key);
            SocketChannel socketChannel = clientConnections.get(key);
            synchronized (clientWriteBuffer) {
                clientWriteBuffer.get(socketChannel).add(ByteBuffer.wrap(Message.serialize(msg)));
            }
            socketChannel.keyFor(serverSelector).interestOps(SelectionKey.OP_WRITE);
            serverSelector.wakeup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replyInternal(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ArrayList<ByteBuffer> bytebufferlist = clientWriteBuffer.get(channel);
        sendOutBuffer(bytebufferlist, channel);
        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * forward data to the server which is identified with its logical name
     * @param serverName
     * @param message
     */
    protected void forward(String serverName, Message message) {
        try {
            assert (channelMap.containsKey(serverName));
            SocketChannel socketChannel = channelMap.get(serverName);
            assert (socketChannel != null);
            synchronized (forwardBuffer) {
                forwardBuffer.get(socketChannel).add(ByteBuffer.wrap(Message.serialize(message)));
            }
            socketChannel.keyFor(clientSelector).interestOps(SelectionKey.OP_WRITE);
        //    forwardInternal(socketChannel.keyFor(clientSelector));
            clientSelector.wakeup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * forwardInternal the buffer to the channel
     * @param key channelkey
     */
    private void forwardInternal(SelectionKey key) {
        System.out.println("forward message to the channel");
        SocketChannel socket = (SocketChannel) key.channel();
        ArrayList<ByteBuffer> bytebufferlist = forwardBuffer.get(socket);
        sendOutBuffer(bytebufferlist, socket);
        if (bytebufferlist.isEmpty())
        //key.channel().register(clientSelector, SelectionKey.OP_READ);
        key.interestOps(SelectionKey.OP_READ);
    }

    private void sendOutBuffer(ArrayList<ByteBuffer> buffer, SocketChannel socket) {
        try {
            while (!buffer.isEmpty()) {
                socket.write(buffer.get(0));
                if (buffer.get(0).remaining() > 0) {
                    //tcp buffer is full
                    return;
                }
                buffer.remove(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * this function is optinally called, when you want to make the reactor in both of the role of
     * client and server, you can call this
     * @param serverIP
     * @param port
     */
    public void setClientEndPoint(String serverName, String serverIP, int port) {
        try {
            if (!clientRole) {
                clientRole = true;
                channelMap = new HashMap<String, SocketChannel>();
                forwardBuffer = new HashMap<SocketChannel, ArrayList<ByteBuffer>>();
            }
            assert(!channelMap.containsKey(serverName));
            // Create a non-blocking socket channel
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(serverIP, port));
            socketChannel.register(clientSelector, SelectionKey.OP_CONNECT);
            channelMap.put(serverName, socketChannel);
            forwardBuffer.put(socketChannel, new ArrayList<ByteBuffer>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        String clientIP = ((InetSocketAddress) socketChannel.getRemoteAddress()).getAddress().getHostAddress();
        int clientPort = ((InetSocketAddress) socketChannel.getRemoteAddress()).getPort();
        socketChannel.configureBlocking(false);
        socketChannel.register(serverSelector, SelectionKey.OP_READ);
        System.out.println("registered incoming Channel:" + clientIP + "," + clientPort);
        //track client connection
        System.out.println("tracking " + (clientIP+ ":" + clientPort).hashCode());
        clientConnections.put((clientIP+ ":" + clientPort).hashCode(), socketChannel);
        clientWriteBuffer.put(socketChannel, new ArrayList<ByteBuffer>());
    }

    private void finishConnection(SelectionKey selectionKey) {
        try {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            if (channel.isConnectionPending()) channel.finishConnect();
            channel.configureBlocking(false);
        }
        catch (Exception e) {
            selectionKey.cancel();
            e.printStackTrace();
        }
    }

   /**
     * read data from the socket
     * @param key
     * @return the socket
     */
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
            Message inMsg =  Message.deserialize(ret.array());

            String clientIP = ((InetSocketAddress) socketChannel.getRemoteAddress()).
                        getAddress().getHostAddress();
            int clientPort = ((InetSocketAddress) socketChannel.getRemoteAddress()).getPort();
            inMsg.transactionIDs.add((clientIP + ":" + clientPort).hashCode());
            return inMsg;
        } catch (IOException e) {
            //key.cancel();
            //((SocketChannel) key.channel()).close();
            e.printStackTrace();
            return null;
        }
    }



    private class ServerGo implements Runnable {

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
                            } else {
                                if (selectkey.isWritable()) {
                                    replyInternal(selectkey);
                                }
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

    private class ClientGo implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    clientSelector.select();
                    Set selected = clientSelector.selectedKeys();
                    for (Object aSelected : selected) {
                        //accept the connection
                        SelectionKey selectkey = (SelectionKey) aSelected;
                        if (selectkey.isConnectable()) {
                            finishConnection(selectkey);
                        } else {
                            if (selectkey.isWritable()) {
                                forwardInternal(selectkey);
                            } else {
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
        Thread t_server = new Thread(new ServerGo());
        t_server.start();
        if (clientRole) {
            Thread t_client = new Thread(new ClientGo());
            t_client.start();
        }
    }


    public abstract void dispatch(Message msg);
}
