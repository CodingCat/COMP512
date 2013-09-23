package message;

import nio.Message;

public class ReservationMessage extends Message {

    protected MessageType type = MessageType.UNKNOW;

    public MessageType getMessageType () {
        return type;
    }
}
