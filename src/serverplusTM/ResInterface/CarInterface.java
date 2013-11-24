/*
 * Author: Navjot Singh
 */
package serverplusTM.ResInterface;

import serverplusTM.ResImpl.RMItem;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface CarInterface extends Remote
{
	RMItem readData( int id, String key )  throws RemoteException;
    //void writeData( int id, String key, RMItem value );
    boolean deleteReservation(int id, int opID, String key,int reservedItemCount)  throws RemoteException;
	/* Add cars to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addCars(int id, int opID, String location, int numCars, int price)
	throws RemoteException; 
    /* Delete all Cars from a location.
     * It may not succeed if there are reservations for this location
     *
     * @return success
     */		    
    public boolean deleteCars(int id, int opID, String location)
	throws RemoteException; 
    /* return the number of cars available at a location */
    public int queryCars(int id, int opID, String location)
	throws RemoteException; 
    /* return the price of a car at a location */
    public int queryCarsPrice(int id, int opID, String location)
	throws RemoteException; 
    /* reserve a car at this location */
    public boolean reserveCar(int id, int opID, String key)
	throws RemoteException;
    public boolean shutdown() throws RemoteException;
    public boolean commit(int txid) throws  RemoteException;
    public boolean abort(int txid) throws RemoteException;

    public boolean isPrimary();
}
