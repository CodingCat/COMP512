package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public abstract class Reactor implements Runnable {

    private Selector selector;
    private ServerSocketChannel serverSocket;

    public Reactor(){}

    public Reactor(int port) {
        try {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(
                    new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);
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

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set selected = selector.selectedKeys();
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
                    selected.remove(aSelected);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract void dispatch(Message msg);
}
