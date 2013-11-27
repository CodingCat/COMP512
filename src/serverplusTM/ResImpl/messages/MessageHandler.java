package serverplusTM.ResImpl.messages;

import org.jgroups.*;

public class MessageHandler extends ReceiverAdapter {

    protected JChannel iochannel;

    public MessageHandler(String groupName) {
        try {
            iochannel = new JChannel();
            iochannel.setReceiver(this);
            iochannel.connect(groupName);
        } catch (Exception e) {

        }
    }

    public View getView() {
        return iochannel.getView();
    }

    public Address getAddress() {
        return iochannel.getAddress();
    }


    public void castMessage(String type, String payload) {
        Message msg = MessageFactory.getMessage(type);
        if (payload != null)
            msg.setObject(type + ";" + payload);
        try {
            iochannel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unifastMessage(String type, String payload, Address dst) {
        Message msg = MessageFactory.getMessage(type);
        if (payload != null)
            msg.setObject(type + ";" + payload);
        msg.setDest(dst);
        try {
            iochannel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
