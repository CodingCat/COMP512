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


    // query the number of available seats/rooms/cars
    @Override
    protected int queryNum(int id, String key) {
        Trace.info("SUBMIT TRANSACTION:" +
                "RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key);
        int value = 0;
        if ( curObj != null ) {
            value = curObj.getCount();
        } // else
        Trace.info("SUBMIT TRANSACTION:" +
                "RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }

    // query the price of an item
    @Override
    protected int queryPrice(int id, String key) {
        Trace.info("RM::queryPrice(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key);
        int value = 0;
        if ( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;
    }


    // deletes the entire item
    @Override
    protected boolean deleteItem(int id, String key)
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key );
        // Check if there is such an item in the storage
        if ( curObj == null ) {
            Trace.warn("SUBMIT TRANSACTION: " +
                    "RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
            return false;
        } else {
            if (curObj.getReserved()==0) {
                removeData(id, curObj.getKey());
                Trace.info("SUBMIT TRANSACTION:" +
                        "RM::deleteItem(" + id + ", " + key + ") item deleted" );
                return true;
            }
            else {
                Trace.info("SUBMIT TRANSACTION:" +
                        "RM::deleteItem(" + id + ", " + key + ") item can't be deleted " +
                        "because some customers reserved it" );
                return false;
            }
        } // if
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
                            if (t3.operation == 2) super.removeData(transId, t3.key);
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
