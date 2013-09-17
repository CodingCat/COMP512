package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set selected = selector.selectedKeys();
                for (Object aSelected : selected)
                    dispatch((SelectionKey) (aSelected));
                selected.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract void dispatch(SelectionKey selKey);
}
