package test;


import nio.Message;
import nio.NIOReactor;

import java.util.ArrayList;

public class DummyServer extends NIOReactor {

    public DummyServer (String ip, int port) {
        super(ip, port);
    }

    public void dispatch (Message msg) {
        if (msg instanceof DummyMessage) {
            DummyMessage dmsg = new DummyMessage();
            dmsg.transactionIDs = (ArrayList<Integer>) msg.transactionIDs.clone();
            dmsg.setText("response for " + ((DummyMessage) msg).getText());
            reply(dmsg);
        } else {
            System.out.println(Message.serialize(msg));
        }
    }

}
