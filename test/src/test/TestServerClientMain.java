package test;

import nio.Reactor;

public class TestServerClientMain {

    public static void main(String [] args) {
        Reactor runnable = new DummyServerClient(args[0], Integer.parseInt(args[1]));
        runnable.setClientEndPoint("dummyserver", "127.0.0.1", 5001);
        Thread main_thread = new Thread(runnable);
        main_thread.start();
    }
}
