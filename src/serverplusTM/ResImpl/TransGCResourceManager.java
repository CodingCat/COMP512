package serverplusTM.ResImpl;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import serverplusTM.ResImpl.messages.MessageBase;
import serverplusTM.ResImpl.messages.MessageHeaderBase;

import java.util.List;
import java.util.Random;

public class TransGCResourceManager extends TransGenericResourceManager
        implements RequestHandler {

    MessageDispatcher disp;
    JChannel channel;

    public TransGCResourceManager(String groupName, String msgFilePath) {
        try {
            RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 5000);
            channel = new JChannel(msgFilePath);
            disp = new MessageDispatcher(channel, null, null, this);
            channel.connect(groupName);
            disp.castMessage(null, new MessageBase("join"), opts);
            sendSyncRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Address chooseRandomMember() {
        Random r = new Random(System.currentTimeMillis());
        View currentView = channel.getView();
        List<Address> members = currentView.getMembers();
        Address ret = members.get(r.nextInt(members.size()));
        if (members.size() > 1) {
            while (ret == channel.getAddress()) {
                ret = members.get(r.nextInt(members.size()));
            }
        }
        return ret;
    }

    public void sendSyncRequest() {
        View currentView = channel.getView();
        MessageBase syncmsg = new MessageBase("sync");
        syncmsg.setDest(chooseRandomMember());
        try {
            disp.sendMessage(syncmsg, new RequestOptions(ResponseMode.GET_ALL, 5000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object handle(Message msg) throws Exception {
        short headerpos = 0;
        if (! (msg instanceof MessageBase)) return null;
        MessageBase mb = (MessageBase) msg;
        if (! (mb.getHeader(headerpos) instanceof MessageHeaderBase)) return null;
        MessageHeaderBase mhb = (MessageHeaderBase) mb.getHeader(headerpos);
        if (mhb.type.equals("sync")) {

        }
        return null;
    }
}
