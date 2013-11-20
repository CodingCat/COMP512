/*
 * Author: Navjot Singh
 */
package serverplusTM.ResImpl;

import java.rmi.RemoteException;

public abstract class GenericResourceManager
{
	RMHashtable m_itemHT = null;
	int mport;
	// Reads a data item
    public RMItem readDatafromRM( int id, String key )
    {
        synchronized(m_itemHT) {
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    public void writeData( int id,int operationID, String key, RMItem value )
    {
        synchronized(m_itemHT) {
            m_itemHT.put(key, value);
        }
    }
 // Remove the item out of storage
    protected RMItem removeData(int id,int operationID, String key) {
        synchronized(m_itemHT) {
            return (RMItem)m_itemHT.remove(key);
        }
    }
    
    public synchronized boolean deleteReservationfromRM(int id,int operationID,String key,int reservedItemCount)
    {
    	ReservableItem item;
        item = (ReservableItem) readDatafromRM(id, key);
        item.setReserved(item.getReserved()-reservedItemCount);
        item.setCount(item.getCount()+reservedItemCount);
    	return true;
    }
    
    public synchronized boolean reserveItem(int id,int operationID, String key)
			throws RemoteException
	{
		ReservableItem item = (ReservableItem)readDatafromRM(id, key);
		item.setCount(item.getCount() - 1);
        item.setReserved(item.getReserved()+1);
		return true;
	}
    
    // deletes the entire item
    protected boolean deleteItem(int id,int operationID, String key)
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key );
        // Check if there is such an item in the storage
        if ( curObj == null ) {
            Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
            return false;
        } else {
            if (curObj.getReserved()==0) {
                removeData(id,operationID, curObj.getKey());
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                return true;
            }
            else {
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                return false;
            }
        } // if
    }
    

    // query the number of available seats/rooms/cars
    protected int queryNum(int id,int operationID, String key) {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key);
        int value = 0;  
        if ( curObj != null ) {
            value = curObj.getCount();
        } // else
        Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }    
    
    // query the price of an item
    protected int queryPrice(int id,int operationID, String key) {
        Trace.info("RM::queryPrice(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readDatafromRM( id, key);
        int value = 0; 
        if ( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;        
    }

}
