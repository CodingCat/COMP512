/*
 * Author: Navjot Singh
 */
package server.ResImpl;

import server.ResInterface.CarInterface;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class CarResourceManager extends TransGenericResourceManager implements CarInterface
{
	public RMItem readData( int id, String key )
    {
		return readDatafromRM(id, key);
    }


    public boolean deleteReservation(int id,String key,int reservedItemCount)
    {
    	return deleteReservationfromRM(id, key, reservedItemCount);
    }

	@Override
	public synchronized boolean addCars(int id, String location, int count, int price)
			throws RemoteException
	{
		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Car curObj = (Car) readData( id, Car.getKey(location) );
        if ( curObj == null ) {
            // car location doesn't exist...add it
            Car newObj = new Car( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") created new location " + location +
                    ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing car location and update price...
            Car newObj = curObj.clone();
            newObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                newObj.setPrice( price );
            } // if
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") modified existing location " +
                    location + ", count=" + newObj.getCount() + ", price=$" + price );
        } // else
        return(true);
	}

	@Override
	public synchronized boolean deleteCars(int id, String location) throws RemoteException
	{
		return deleteItem(id, Car.getKey(location));
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException
	{
		return queryNum(id, Car.getKey(location));
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException
	{
		return queryPrice(id, Car.getKey(location));
	}

	@Override
	public boolean reserveCar(int id, String key)
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
        	CarResourceManager obj = new CarResourceManager();
        	obj.m_itemHT = new RMHashtable();
            // dynamically generate the stub (client proxy)
            CarInterface rm = (CarInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("Group28CarRM", rm);

            System.err.println("Car Server ready");
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
