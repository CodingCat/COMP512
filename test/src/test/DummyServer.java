package test;


import nio.Message;
import nio.NIOReactor;

public class DummyServer extends NIOReactor {

    public DummyServer (String ip, int port) {
        super(ip, port);
    }

    public void dispatch (Message msg) {
        if (msg instanceof DummyMessage) {
            System.out.println(((DummyMessage) msg).getText());
        } else {
            System.out.println(Message.serialize(msg));
        }
    }

}
