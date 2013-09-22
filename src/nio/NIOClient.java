package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Set;

/**
 * the class representing the NIO client
 * does not consider connection pool now
 */
public abstract class NIOClient implements Runnable {

    private Selector clientSelector;
    private SocketChannel toServerChannel;

    private ArrayList<Message> writebuffer;

    public NIOClient(String serverIP, int serverPort) {
        try {
            writebuffer = new ArrayList<Message>();
            clientSelector = Selector.open();
            InetSocketAddress serverAddress = new InetSocketAddress(serverIP, serverPort);
            toServerChannel = SocketChannel.open();
            toServerChannel.configureBlocking(false);
            toServerChannel.connect(serverAddress);
            toServerChannel.register(clientSelector, SelectionKey.OP_CONNECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * public interface to users
     * @param msg the message to be sent
     */
    public void send(Message msg) {
        writebuffer.add(msg);
        try {
            String localIP = ((InetSocketAddress) toServerChannel.getLocalAddress()).
                    getAddress().getHostAddress();
            int localPort = ((InetSocketAddress) toServerChannel.getLocalAddress()).getPort();
            msg.transactionIDs.add((localIP + ":" + localPort).hashCode());
            toServerChannel.keyFor(clientSelector).interestOps(SelectionKey.OP_WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * read the message from the channel
     * @param selectedKey, the selectionKey indexed the channel
     * @return the pased message
     */
    private Message read(SelectionKey selectedKey) {
        try {
            //assuming that the message is never larger than 8192 bytes
            ByteBuffer readBuffer = ByteBuffer.allocate(8192);
            SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
            readBuffer.clear();
            int readbytes = socketChannel.read(readBuffer);
            if (readbytes == -1) return null;
            ByteBuffer ret = ByteBuffer.allocate(readbytes);
            ret.put(readBuffer.array(), 0, readbytes);
            return Message.deserialize(ret.array());
        } catch (IOException e) {
            selectedKey.cancel();
            e.printStackTrace();
            return null;
        }
    }

    private void write() {
        try {
            while (!writebuffer.isEmpty()) {
                ByteBuffer out = ByteBuffer.wrap(Message.serialize(writebuffer.get(0)));
                toServerChannel.write(out);
                if (out.remaining() > 0) break;
                writebuffer.remove(0);
            }
            if (writebuffer.isEmpty())
                toServerChannel.keyFor(clientSelector).interestOps(SelectionKey.OP_READ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishConnection(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending()) channel.finishConnect();
            channel.configureBlocking(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run () {
        try {
            while (true) {
                clientSelector.select();
                Set selected = clientSelector.selectedKeys();
                for (Object aSelected : selected) {
                    //accept the connection
                    SelectionKey selectkey = (SelectionKey) aSelected;
                    if (selectkey.isConnectable()) {
                        finishConnection(selectkey);
                    } else {
                        if (selectkey.isReadable()) {
                          //  System.out.println("reading response from server");
                            dispatch(read(selectkey));
                        } else {
                            if (selectkey.isWritable()) {
                                write();
                            }
                        }
                    }
                }
                selected.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void dispatch(Message msg);
}
