package test;

public class TestServerMain {

    public static void main(String [] args) {
        Thread main_thread = new Thread(new DummyServer(args[0], Integer.parseInt(args[1])));
        main_thread.start();
    }
}
