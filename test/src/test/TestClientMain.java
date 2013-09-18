package test;

import nio.ASyncClient;

public class TestClientMain {

    public static void main(String [] args) {
        ASyncClient client = new ASyncClient(args[0], Integer.parseInt(args[1]));
        DummyMessage dmsg = new DummyMessage();
        client.run(dmsg);
    }
}
