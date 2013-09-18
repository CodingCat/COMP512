package nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * the class representing the NIO client
 * does not consider connection pool now
 */
public class ASyncClient {

    private Selector clientSelector;
    private InetSocketAddress serverAddress;

    public ASyncClient(String serverIP, int serverPort) {
        try {
            clientSelector = Selector.open();
            serverAddress = new InetSocketAddress(serverIP, serverPort);
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(serverAddress);
            channel.register(clientSelector, SelectionKey.OP_CONNECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run (Message outMsg) {
        try {
            while (true) {
                clientSelector.select();
                Set selected = clientSelector.selectedKeys();
                for (Object aSelected : selected) {
                    //accept the connection
                    SelectionKey selectkey = (SelectionKey) aSelected;
                    if (selectkey.isConnectable()) {
                        SocketChannel channel = (SocketChannel) selectkey.channel();
                        if (channel.isConnectionPending()) channel.finishConnect();
                        channel.configureBlocking(false);
                        channel.write(ByteBuffer.wrap(Message.serialize(outMsg)));
                        channel.register(clientSelector, SelectionKey.OP_READ);
                    } else {
                        if (selectkey.isReadable()) {
                            System.out.println("reading response from server");
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
