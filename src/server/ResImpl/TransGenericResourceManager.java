package server.ResImpl;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class TransGenericResourceManager extends GenericResourceManager {
    class Tuple3 {
        String key;
        int operation;    // 0 - read, 1 - write, 2 - delete, 3- reserve
        RMItem newvalue;
        Tuple3(String k, int op, RMItem nv) {
            key = k;
            operation = op;
            newvalue = nv;
        }
    }

    private ConcurrentHashMap<Integer, ArrayList<Tuple3>> operationList =
            new ConcurrentHashMap<Integer, ArrayList<Tuple3>>();//transaction_id -> tuple2 list

    private void checkOperationQueue(int id) {
        if (!operationList.contains(id)) {
            operationList.put(id, new ArrayList<Tuple3>());
        }
    }

    public RMItem readDatafromRM( int id, String key ) {
        //push to the queue
        RMItem it = super.readDatafromRM(id, key);
        checkOperationQueue(id);
        operationList.get(id).add(new Tuple3(key, 0, it));
        return it;
    }

    // Writes a data item
    public void writeData( int id, String key, RMItem value ) {
        checkOperationQueue(id);
        operationList.get(id).add(new Tuple3(key, 1, value));
    }

    // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        checkOperationQueue(id);
        operationList.get(id).add(new Tuple3(key, 2, null));
        return super.readDatafromRM(id, key);
    }


    public synchronized boolean reserveItem(int id, String key)
            throws RemoteException
    {
        checkOperationQueue(id);
        operationList.get(id).add(new Tuple3(key, 3, null));
        return true;
    }




    public boolean abort(int transid) {
        if (operationList.containsKey(transid)) {
            //realize the operations
            operationList.remove(transid);
            return true;
        }
        return false;
    }

    public boolean commit(int transId) {
        try {
            if (operationList.containsKey(transId)) {
                //realize the operations
                for (Tuple3 t3 : operationList.get(transId)) {
                    if (t3.operation == 0) {
                        if (super.readDatafromRM(transId, t3.key) == null)
                            return false;
                    } else {
                        if (t3.operation == 1)
                            super.writeData(transId, t3.key, t3.newvalue);
                        else {
                            if (t3.operation == 2) super.deleteItem(transId, t3.key);
                            if (t3.operation == 3) super.reserveItem(transId, t3.key);
                        }
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
