package test;

import nio.Message;
import nio.Reactor;

public class DummyServerClient extends Reactor {

    public DummyServerClient (String ip, int serverport) {
        super(ip, serverport);
    }

    @Override
    public void dispatch(Message msg) {
        if (msg instanceof DummyMessage) {
            send("dummyserver", Message.serialize(msg));
        }
    }

}
