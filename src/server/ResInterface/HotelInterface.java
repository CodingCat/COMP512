/*
 * Author: Navjot Singh
 */
package server.ResInterface;

import server.ResImpl.RMItem;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HotelInterface extends Remote
{
	RMItem readData( int id, String key )  throws RemoteException;
    //void writeData( int id, String key, RMItem value );
    boolean deleteReservation(int id,String key,int reservedItemCount)  throws RemoteException;
	/* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addRooms(int id, String location, int numRooms, int price) 
	throws RemoteException;
    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public boolean deleteRooms(int id, String location) 
	throws RemoteException;
    /* return the number of rooms available at a location */
    public int queryRooms(int id, String location) 
	throws RemoteException;
    /* return the price of a room at a location */
    public int queryRoomsPrice(int id, String location) 
	throws RemoteException;
    /* reserve a room certain at this location */
    public boolean reserveRoom(int id, String key) 
	throws RemoteException;
}
