package test;


import nio.Message;
import nio.Reactor;

public class DummyServer extends Reactor {

    public DummyServer (String ip, int port) {
        super(ip, port);
    }

    public void dispatch (Message msg) {
        if (msg instanceof DummyMessage) {
            System.out.println(((DummyMessage) msg).getAdditionalVariable());
        } else {
            System.out.println("unrecognized message id:" + msg.getMessageID());
        }
    }

}
