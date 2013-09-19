package test;

import nio.NIOReactor;

public class TestServerClientMain {

    public static void main(String [] args) {
        NIOReactor runnable = new DummyServerClient(args[0], Integer.parseInt(args[1]));
        runnable.setClientEndPoint("dummyserver", "127.0.0.1", 5001);
        Thread main_thread = new Thread(runnable);
        main_thread.start();
    }
}
