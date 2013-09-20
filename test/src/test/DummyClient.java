package test;

import nio.Message;
import nio.NIOClient;

public class DummyClient extends NIOClient {

    public DummyClient(String serverIP, int serverPort) {
        super(serverIP, serverPort);
    }

    @Override
    public void dispatch(Message msg) {
        if (msg instanceof DummyMessage) {
            System.out.println(((DummyMessage) msg).getText());
        }
    }
}
