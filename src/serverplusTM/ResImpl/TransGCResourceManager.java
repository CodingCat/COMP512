package serverplusTM.ResImpl;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import serverplusTM.ResImpl.messages.MessageFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TransGCResourceManager extends TransGenericResourceManager
        implements RequestHandler {

    private ConcurrentHashMap<Integer, ArrayList<Integer>> receivedOperations =
            new ConcurrentHashMap<Integer, ArrayList<Integer>>(); //txID -> list of opID

    MessageDispatcher disp;
    JChannel channel;

    public TransGCResourceManager(String groupName, String msgFilePath) {
        try {
            RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, 5000);
            channel = new JChannel();
            disp = new MessageDispatcher(channel, null, null, this);
            channel.connect(groupName);
            channel.setDiscardOwnMessages(true);
            disp.castMessage(null, MessageFactory.getMessage("join"), opts);
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
            while (ret.equals(channel.getAddress())) {
                ret = members.get(r.nextInt(members.size()));
            }
        }
        return ret;
    }

    private void sendSyncRequest() {
        Message syncmsg = MessageFactory.getMessage("syncrequest;");
        Address dstAddr = chooseRandomMember();
        System.out.println("send sync_request to " + dstAddr.toString());
        syncmsg.setDest(dstAddr);
        try {
            disp.sendMessage(syncmsg, new RequestOptions(ResponseMode.GET_NONE, 5000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSyncResponse(Address destination) {
        Message syncmsg = MessageFactory.getMessage("syncresponse");
        syncmsg.setDest(destination);
        String syncString = generateSyncString();
        System.out.println("send sync response as " + syncString +
                " to " + destination.toString());
        syncmsg.setObject(syncString);
        try {
            disp.sendMessage(syncmsg, new RequestOptions(ResponseMode.GET_NONE, 5000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPrimary() {
        return channel.getView().getMembers().get(0).
                equals(channel.getAddress());
    }

    @Override
    public Object handle(Message msg) throws Exception {

        String msgstr = (String) msg.getObject();
        String[] array = msgstr.split(";");
        if (array[0].equals("syncrequest")) {
            if (!msg.getSrc().equals(channel.getAddress())) {
                System.out.println("receive sync request");
                sendSyncResponse(msg.getSrc());
            }
        }
        if (array[0].equals("syncresponse")) {
            System.out.println("receive sync response");
            if (array.length > 1) realizeSyncResponse(array[1]);
        }
        if (array[0].equals("remove")) {
            System.out.println("receive remove message");
            String [] removeArr = array[1].split(",");
            realizeRemoveMessage(Integer.parseInt(removeArr[0]),
                    Integer.parseInt(removeArr[1]),
                    removeArr[2]);
        }
        if (array[0].equals("reserve")) {
            System.out.println("receive reserve message");
            String[] reserveArr = array[1].split(",");
            realizeReserveMessage(Integer.parseInt(reserveArr[0]),
                    Integer.parseInt(reserveArr[1]),
                    reserveArr[2]);
        }
        if (array[0].equals("delete")) {
            System.out.println("receive delete message");
            String[] deleteArr = array[1].split(",");
            realizeDeleteMessage(Integer.parseInt(deleteArr[0]),
                    Integer.parseInt(deleteArr[1]),
                    deleteArr[2]);
        }
        if (array[0].equals("deletereserve")) {
            System.out.println("receive deletereserve message");
            String[] deleteReserveArr = array[1].split(",");
            realizeDeleteReserveMessage(Integer.parseInt(deleteReserveArr[0]),
                    Integer.parseInt(deleteReserveArr[1]),
                    deleteReserveArr[2], Integer.parseInt(deleteReserveArr[3]));
        }
        if (array[0].equals("commit")) {
            System.out.println("commit");
            realizeCommitMessage(Integer.parseInt(array[1].split(",")[0]));
        }
        if (array[0].equals("abort")) {
            System.out.println("abort");
            realizeAbortMessage(Integer.parseInt(array[1].split(",")[0]));
        }
        if (array[0].equals("join")) {
            System.out.println("new member join");
        }
        return "ACK";
    }

    private void allocateTransSlot(int txid) {
        if (!receivedOperations.contains(txid)) {
            receivedOperations.put(txid, new ArrayList<Integer>());
        }
    }

    private void castMessage(String type, String payload) {
        Message msg = MessageFactory.getMessage("");
        msg.setObject(type + ";" + payload);
        try {
            disp.castMessage(null, msg, new RequestOptions(ResponseMode.GET_ALL, 5000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateWriteString(int id, int opID, RMItem item) {
        Flight f = (Flight) item;
        return id + "," + opID + "," + f.getKey() + "," + f.getLocation() + "," + f.getCount() +
                "," + f.getPrice() + "," + f.getReserved();
    }

    @Override
    public void writeData( int id,int operationID, String key, RMItem value ) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            super.writeData(id, operationID, key, value);
            castMessage("write", generateWriteString(id, operationID, value));
        }
    }

    protected void saveData(String key, RMItem value ) {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }

    protected void realizeWriteMessage(int id, int operationID, String key, RMItem value) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            super.writeData(id, operationID, key, value);
        }
    }

    private String generateRemoveString(int id, int opID, String key) {
        return id + "," + opID + "," + key;
    }

    @Override
    protected RMItem removeData(int id,int operationID, String key) {
        allocateTransSlot(id);
        RMItem item = null;
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            item = super.removeData(id, operationID, key);
            castMessage("remove", generateRemoveString(id, operationID, key));
        }
        return item;
    }

    private RMItem realizeRemoveMessage(int id, int operationID, String key) {
        allocateTransSlot(id);
        RMItem item = null;
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            item = super.removeData(id, operationID, key);
        }
        return item;
    }

    private String generateReserveString(int id, int opID, String key) {
        return id + "," + opID + "," + key;
    }

    @Override
    public synchronized boolean reserveItem(int id,int operationID, String key)
            throws RemoteException {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            boolean r = super.reserveItem(id, operationID, key);
            castMessage("reserve", generateReserveString(id, operationID, key));
            return r;
        }
        return false;
    }

    private synchronized boolean realizeReserveMessage(int id, int operationID, String key) {
        allocateTransSlot(id);
        boolean r = false;
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            try {
                r = super.reserveItem(id, operationID, key);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    @Override
    protected int queryNum(int id,int operationID, String key) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            return super.queryNum(id, operationID, key);
        }
        return -1;
    }

    // query the price of an item
    @Override
    protected int queryPrice(int id,int operationID, String key) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            return super.queryPrice(id, operationID, key);
        }
        return -1;
    }

    private String generateDeleteString(int id, int opID, String key) {
        return id + "," + opID + "," + key;
    }

    // deletes the entire item
    @Override
    protected boolean deleteItem(int id,int operationID, String key) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            boolean r = super.deleteItem(id, operationID, key);
            castMessage("delete", generateDeleteString(id, operationID, key));
            return r;
        }
        return false;
    }

    private boolean realizeDeleteMessage(int id, int operationID, String key) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            boolean r = super.deleteItem(id, operationID, key);
            return r;
        }
        return false;
    }

    private String generateDeleteReserveString(int id, int opID, String key,
                                               int reserveCount) {
        return id + "," + opID + "," + key + "," + reserveCount;
    }

    @Override
    public boolean deleteReservationfromRM(int id, int operationID, String key, int reservedItemCount) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            boolean r = super.deleteReservationfromRM(id, operationID, key, reservedItemCount);
            castMessage("deletereserve", generateDeleteReserveString(id, operationID,
                    key, reservedItemCount));
            return r;
        }
        return false;
    }

    private boolean realizeDeleteReserveMessage(int id, int operationID, String key,
                                                int reservedItemCount) {
        allocateTransSlot(id);
        if (!receivedOperations.get(id).contains(operationID)) {
            receivedOperations.get(id).add(operationID);
            boolean r = super.deleteReservationfromRM(id, operationID, key, reservedItemCount);
            return r;
        }
        return false;
    }

    @Override
    public boolean commit(int transid) {
        if (receivedOperations.contains(transid)) {
            receivedOperations.remove(transid);
            boolean r = super.commit(transid);
            castMessage("commit", String.valueOf(transid));
            return r;
        }
        return false;
    }

    private boolean realizeCommitMessage(int transid) {
        if (receivedOperations.contains(transid)) {
            receivedOperations.remove(transid);
            boolean r = super.commit(transid);
            return r;
        }
        return false;
    }

    @Override
    public boolean abort(int transId) {
        if (receivedOperations.contains(transId)) {
            receivedOperations.remove(transId);
            boolean r =super.abort(transId);
            castMessage("abort", String.valueOf(transId));
            return r;
        }
        return false;
    }

    public boolean realizeAbortMessage(int transId) {
        if (receivedOperations.contains(transId)) {
            receivedOperations.remove(transId);
            boolean r =super.abort(transId);
            return r;
        }
        return false;
    }

    protected String generateSyncString() {
        StringBuffer sb = new StringBuffer();
        sb.append("syncresponse;");
        for(Iterator itr = m_itemHT.keySet().iterator(); itr.hasNext();){
            ReservableItem f = (ReservableItem) itr.next();
            sb.append(f.getLocation() + "," + f.getCount() + "," +
                    f.getPrice() + "," + f.getReserved() + ";");
        }
        return sb.toString();
    }

    protected abstract void realizeSyncResponse(String syncStr);

}
