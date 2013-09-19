package nio;

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
public class ASyncClient implements Runnable {

    private Selector clientSelector;
    private SocketChannel toServerChannel;

    private ArrayList<Message> writebuffer;

    public ASyncClient(String serverIP, int serverPort) {
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
        toServerChannel.keyFor(clientSelector).interestOps(SelectionKey.OP_WRITE);
    }

    private void write() {
        try {
            System.out.println("writing message");
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
}
