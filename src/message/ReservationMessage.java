package message;

import nio.Message;

public class ReservationMessage extends Message {

    protected MessageType type;

    public MessageType getMessageType () {
        return type;
    }
}
