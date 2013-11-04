/*
 * Author: Navjot Singh
 */
package server.ResImpl;


import server.ResInterface.FlightInterface;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FlightResourceManager extends GenericResourceManager implements FlightInterface {

	
	public RMItem readData( int id, String key ) throws RemoteException
    {
		return readDatafromRM(id, key);
    }

    // Writes a data item
    /*public void writeData( int id, String key, RMItem value )
    {
    	super.writeData(id, key, value);
    }*/
	public boolean deleteReservation(int id,String key,int reservedItemCount) throws RemoteException
    {
    	return deleteReservationfromRM(id, key, reservedItemCount);
    }

    @Override
    public synchronized boolean addFlight(int id, int flightNum, int flightSeats,
                                          int flightPrice) throws RemoteException
    {
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
        Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
        if ( curObj == null ) {
            // doesn't exist...add it
            Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
            writeData( id, newObj.getKey(), newObj );
            Trace.info(
                    "RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                    flightSeats + ", price=$" + flightPrice );
        } else {
            // add seats to existing flight and update the price...
            Flight newobj = curObj.clone();
            newobj.setCount( curObj.getCount() + flightSeats );
            if ( flightPrice > 0 ) {
                newobj.setPrice( flightPrice );
            } // if
            writeData( id, newobj.getKey(), newobj );
            Trace.info(
                    "RM::addFlight(" + id + ") modified existing flight " +
                    flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
        } // else
        return(true);
    }

    @Override
	public synchronized boolean deleteFlight(int id, int flightNum) throws RemoteException 
	{
		return deleteItem(id, Flight.getKey(flightNum));	
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException 
	{
		return queryNum(id, Flight.getKey(flightNumber));
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException 
	{
		return queryPrice(id, Flight.getKey(flightNumber));
	}

	@Override
	public boolean reserveFlight(int id, String key)
			throws RemoteException 
	{
		return reserveItem(id,key);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String server = "localhost";
        int port = 1099;

        if (args.length == 1) {
            server = server + ":" + args[0];
            port = Integer.parseInt(args[0]);
        } else if (args.length != 0 &&  args.length != 1) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
            System.exit(1);
        }

        try {
            // create a new Server object
        	FlightResourceManager obj = new FlightResourceManager();
        	obj.m_itemHT = new RMHashtable();
            // dynamically generate the stub (client proxy)
            FlightInterface rm = (FlightInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("Group28FlightRM", rm);

            System.err.println("Flight Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }


	}

}
