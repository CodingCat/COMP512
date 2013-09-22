package server.datastore;

import nio.Message;
import nio.NIOReactor;

public class NIODataStore extends NIOReactor {

    public NIODataStore(String ip, int port) {
        super(ip, port);
    }

    @Override
    public void dispatch(Message msg) {

    }

    public static void main(String [] args) {
        NIODataStore datadaemon = new NIODataStore(args[0], Integer.parseInt(args[1]));
        Thread data_t = new Thread(datadaemon);
        data_t.run();
    }
}
