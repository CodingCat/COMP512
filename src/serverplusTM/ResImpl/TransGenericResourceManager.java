package serverplusTM.ResImpl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class TransGenericResourceManager extends GenericResourceManager {
	
	//TODO: add operationIDList to check if the operation was already performed or not.
	
    class Tuple3 {
        String key;
        int operation;    // 0 - read, 1 - write, 2 - delete, 3- reserve, 4 - delete reservation
        RMItem value;
        Tuple3(String k, int op, RMItem nv) {
            key = k;
            operation = op;
            value = nv;
        }
        int oldint = 0;
    }

    private ConcurrentHashMap<Integer, ArrayList<Tuple3>> operationList =
            new ConcurrentHashMap<Integer, ArrayList<Tuple3>>();//transaction_id -> tuple2 list

    private void checkOperationQueue(int id) 
    {
        if (!operationList.containsKey(id)) 
        {
            operationList.put(id, new ArrayList<Tuple3>());
        }
    }

    public RMItem readDatafromRM( int id, String key ) {
        //push to the queue
        RMItem it = super.readDatafromRM(id, key);
        checkOperationQueue(id);
        operationList.get(id).add(0, new Tuple3(key, 0, it));
        return it;
    }

    // Writes a data item
    public void writeData( int id,int operationID, String key, RMItem value ) {
        checkOperationQueue(id);
        RMItem oldvalue = super.readDatafromRM(id, key);
        operationList.get(id).add(0, new Tuple3(key, 1, oldvalue));
        //realize the write operation
        super.writeData(id,operationID, key, value);
    }

    // Remove the item out of storage
    protected RMItem removeData(int id,int operationID, String key) {
        checkOperationQueue(id);
        //read old value
        RMItem oldvalue = super.readDatafromRM(id, key);
        operationList.get(id).add(0, new Tuple3(key, 2, oldvalue));
        super.removeData(id,operationID, key);
        return oldvalue;//super.readDatafromRM(id, key);
    }


    public synchronized boolean reserveItem(int id,int operationID, String key)
            throws RemoteException
    {
        checkOperationQueue(id);
        //read old value
        //ReservableItem oldvalue = (ReservableItem) super.readDatafromRM(id, key);
        operationList.get(id).add(0, new Tuple3(key, 3, null));
        super.reserveItem(id,operationID, key);
        return true;
    }


    // query the number of available seats/rooms/cars
    @Override
    protected int queryNum(int id,int operationID, String key) {
        Trace.info("SUBMIT TRANSACTION:" +
                "RM::queryNum(" + id + ", " + key + ") called");
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
    protected int queryPrice(int id,int operationID, String key) {
        Trace.info("RM::queryPrice(" + id + ", " + key + ") called");
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key);
        int value = 0;
        if ( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryPrice(" + id + ", " + key + ") returns cost=$" + value);
        return value;
    }


    // deletes the entire item
    @Override
    protected boolean deleteItem(int id,int operationID, String key) {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called");
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key );
        // Check if there is such an item in the storage
        if ( curObj == null ) {
            Trace.warn("SUBMIT TRANSACTION: " +
                    "RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist");
            return false;
        } else {
            if (curObj.getReserved()==0) {
                removeData(id,operationID, curObj.getKey());
                Trace.info("SUBMIT TRANSACTION:" +
                        "RM::deleteItem(" + id + ", " + key + ") item deleted");
                return true;
            }
            else {
                Trace.info("SUBMIT TRANSACTION:" +
                        "RM::deleteItem(" + id + ", " + key + ") item can't be deleted " +
                        "because some customers reserved it");
                return false;
            }
        } // if
    }

    private void deleteReservation(ReservableItem item, int reservedItemCount) 
    {
        item.setReserved(item.getReserved() - reservedItemCount);
        item.setCount(item.getCount() + reservedItemCount);
    }

    @Override
    public boolean deleteReservationfromRM(int id,int operationID, String key, int reservedItemCount) {
        ReservableItem item;
        //read old value
        item = (ReservableItem) readDatafromRM(id, key);
        checkOperationQueue(id);
        Tuple3 t3 = new Tuple3(key, 4, item);
        t3.oldint = item.getReserved();
        operationList.get(id).add(0, t3);
        deleteReservation(item, reservedItemCount);
        System.out.println("Delete reservation called.");
        return true;
    }


    public boolean commit(int transid) {
        if (operationList.containsKey(transid)) 
        {
            //realize the operations
            operationList.remove(transid);
            System.out.println("\n\nCommit called\n\n");
            return true;
        }
        return false;
    }

    public boolean abort(int transId) {
        try {
        	System.out.println("\n\nAbort called\n\n");
            if (operationList.containsKey(transId)) {
                //realize the operations
                for (Tuple3 t3 : operationList.get(transId)) {
                    if (t3.operation == 0) {
                        if (super.readDatafromRM(transId, t3.key) == null)
                            return false;
                    } else {
                        if (t3.operation == 1) {
                            //write ...
                            if (t3.value != null)
                                super.writeData(transId,-1, t3.key, t3.value);
                            else {
                                //back to the normal value
                                super.removeData(transId,-1, t3.key);
                            }
                        }
                        else {
                            if (t3.operation == 2) {
                                //remove,
                                //add that value back
                                //super.removeData(transId, t3.key);
                                super.writeData(transId,-1, t3.key, t3.value);
                            }
                            if (t3.operation == 3) {
                              // reserve, recover with 1
                                ReservableItem ri = (ReservableItem) super.readDatafromRM(transId, t3.key);
                                System.out.println("Reserve was called. Now deleting the reservation" +
                                		"Old value count= "+ri.getCount()+" reserved="+ri.getReserved());
                                ri.setCount(ri.getCount()+1);
                                ri.setReserved(ri.getReserved() - 1);
                                System.out.println("Reserve was called. Now deleting the reservation" +
                                		"New value count= "+ri.getCount()+" reserved="+ri.getReserved());
                            }
                            if (t3.operation == 4) {
                                //delete reservation, change back to the normal value
                                ReservableItem ri = (ReservableItem) super.readDatafromRM(transId, t3.key);
                                System.out.println("Delete reservation was called. Now re-reserving" +
                                		"Old value count= "+ri.getCount()+" reserved="+ri.getReserved());
                                ri.setCount(ri.getCount() - (t3.oldint - ri.getReserved()));
                                ri.setReserved(t3.oldint);
                                System.out.println("Delete reservation was called. Now re-reserving" +
                                		"New value count= "+ri.getCount()+" reserved="+ri.getReserved());
                            }
                        }
                    }
                }
                operationList.remove(transId);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
