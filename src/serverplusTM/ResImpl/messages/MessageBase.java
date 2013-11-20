package serverplusTM.ResImpl.messages;

import org.jgroups.Header;
import org.jgroups.Message;

public class MessageBase extends Message {

    public MessageBase(String type) {
        short headerpos = 0;
        super.putHeader(headerpos, (Header) (new MessageHeaderBase(type)));
    }

}
