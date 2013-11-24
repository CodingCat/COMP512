package serverplusTM.ResImpl.messages;

import org.jgroups.Message;

public class MessageFactory {

    public static Message getMessage(String type) {
        Message m = new Message();
        m.setObject(type);
        return m;
    }
}
