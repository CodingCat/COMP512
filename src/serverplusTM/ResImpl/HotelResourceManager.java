/*
 * Author: Navjot Singh
 */
package serverplusTM.ResImpl;

import serverplusTM.ResInterface.HotelInterface;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HotelResourceManager extends TransGenericResourceManager implements HotelInterface
{

	public RMItem readData( int id, String key )
    {
		return readDatafromRM(id, key);
    }

    // Writes a data item
    /*public void writeData( int id, String key, RMItem value )
    {
    	super.writeData(id, key, value);
    }*/
    public boolean deleteReservation(int id,String key,int reservedItemCount)
    {
    	return deleteReservationfromRM(id, key, reservedItemCount);
    }
	@Override
	public synchronized boolean addRooms(int id, String location, int count, int price)
			throws RemoteException
	{
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if ( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return true;
	}

	@Override
	public synchronized boolean deleteRooms(int id, String location) throws RemoteException
	{
		return deleteItem(id, Hotel.getKey(location));
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException
	{
		return queryNum(id, Hotel.getKey(location));
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException
	{
		return queryPrice(id, Hotel.getKey(location));
	}

	@Override
	public boolean reserveRoom(int id, String key)
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
            System.out.println("Usage: java ResImpl.CarResourceManager [port]");
            System.exit(1);
        }

        try {
            // create a new Server object
        	HotelResourceManager obj = new HotelResourceManager();
        	obj.m_itemHT = new RMHashtable();
        	obj.mport=port;
            // dynamically generate the stub (client proxy)
            HotelInterface rm = (HotelInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("Group28HotelRM", rm);

            System.err.println("Hotel Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
	}
	@Override
	public boolean shutdown() throws RemoteException
	{
		try
		{
		Registry registry = LocateRegistry.getRegistry(mport);
        
        // Unregister HotelRM obj
        registry.unbind("Group28HotelRM");

        // Unexport; this will also remove from the RMI runtime
        UnicastRemoteObject.unexportObject(this, true);

        System.out.println("HotelRM Server exiting.");
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

}
