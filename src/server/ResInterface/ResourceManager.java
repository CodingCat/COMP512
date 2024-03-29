package server.ResInterface;


import serverplusTM.LockManager.InvalidTransactionException;
import serverplusTM.LockManager.TransactionAbortedException;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;
/** 
 * Simplified version from CSE 593 Univ. of Washington
 *
 * Distributed  System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean 
 * return values.  Exceptions are used for systemy things. Return
 * values are used for operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

public interface ResourceManager extends Remote 
{
    /* Add seats to a flight.  In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return success.
     */
	public boolean isPrimary() throws RemoteException;
	//Added int operationID for not executing the same requests more than once if there is a failure
	public int start() throws RemoteException;
	public void abort(int txID) throws RemoteException,InvalidTransactionException, TransactionAbortedException;
	public boolean commit(int txID) throws RemoteException, TransactionAbortedException,
	InvalidTransactionException;
	public boolean shutdown() throws RemoteException,NotBoundException;
	public boolean addFlight(int id,int operationID, int flightNum, int flightSeats,
			int flightPrice) throws RemoteException,TransactionAbortedException,
			InvalidTransactionException; 
    
    /* Add cars to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addCars(int id,int operationID, String location, int numCars, int price) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 
   
    /* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addRooms(int id,int operationID, String location, int numRooms, int price) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 			    

			    
    /* new customer just returns a unique customer identifier */
    public int newCustomer(int id,int operationID) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 
    
    /* new customer with providing id */
    public boolean newCustomer(int id,int operationID, int cid)
    throws RemoteException,TransactionAbortedException,
	InvalidTransactionException;

    /**
     *   Delete the entire flight.
     *   deleteflight implies whole deletion of the flight.  
     *   all seats, all reservations.  If there is a reservation on the flight, 
     *   then the flight cannot be deleted
     *
     * @return success.
     */   
    public boolean deleteFlight(int id,int operationID, int flightNum) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 
    
    /* Delete all Cars from a location.
     * It may not succeed if there are reservations for this location
     *
     * @return success
     */		    
    public boolean deleteCars(int id,int operationID, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public boolean deleteRooms(int id,int operationID, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 
    
    /* deleteCustomer removes the customer and associated reservations */
    public boolean deleteCustomer(int id,int operationID,int customer) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* queryFlight returns the number of empty seats. */
    public int queryFlight(int id,int operationID, int flightNumber) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* return the number of cars available at a location */
    public int queryCars(int id,int operationID, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* return the number of rooms available at a location */
    public int queryRooms(int id,int operationID, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* return a bill */
    public String queryCustomerInfo(int id,int operationID,int customer) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 
    
    /* queryFlightPrice returns the price of a seat on this flight. */
    public int queryFlightPrice(int id,int operationID, int flightNumber) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* return the price of a car at a location */
    public int queryCarsPrice(int id,int operationID, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* return the price of a room at a location */
    public int queryRoomsPrice(int id,int operationID, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* Reserve a seat on this flight*/
    public boolean reserveFlight(int id,int operationID, int customer, int flightNumber) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* reserve a car at this location */
    public boolean reserveCar(int id,int operationID, int customer, String location) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 

    /* reserve a room certain at this location */
    public boolean reserveRoom(int id,int operationID, int customer, String locationd) 
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 


    /* reserve an itinerary */
    public boolean itinerary(int id,int operationID,int customer,Vector flightNumbers,String location, boolean Car, boolean Room)
	throws RemoteException,TransactionAbortedException,
	InvalidTransactionException; 
    			
}
