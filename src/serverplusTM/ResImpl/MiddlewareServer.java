/*
 * Author: Navjot Singh
 */
package serverplusTM.ResImpl;

import serverplusTM.ResInterface.CarInterface;
import serverplusTM.ResInterface.FlightInterface;
import serverplusTM.LockManager.InvalidTransactionException;
import serverplusTM.LockManager.TransactionAbortedException;
import serverplusTM.ResInterface.HotelInterface;
import serverplusTM.ResInterface.ResourceManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

//import java.util.Vector;

public class MiddlewareServer implements ResourceManager {

	protected RMHashtable m_customerHT = new RMHashtable();
	private Map<String,Boolean> m_RMState;
	static FlightInterface rmFlight = null;
	static CarInterface rmCar = null;
	static HotelInterface rmHotel = null;
	private TransactionMgr trxnMgr;
	private int mport;
	
	public int start() throws RemoteException
	{
		return trxnMgr.start();
	}
	public void abort(int txID) throws RemoteException,InvalidTransactionException
	{
		trxnMgr.abort(txID);
	}
	public boolean commit(int txID) throws RemoteException, TransactionAbortedException,
	InvalidTransactionException
	{
		return trxnMgr.commit(txID);
	}
	public boolean shutdown() throws RemoteException, NotBoundException
	{
		boolean bCar;
		boolean bHotel;
		boolean bFlight;
		bFlight=bHotel=bCar=true;
		
		if(rmFlight!=null)
		{
			bFlight=rmFlight.shutdown();
		}
		if(rmCar!=null)
		{
			bCar=rmCar.shutdown();
		}
		if(rmHotel!=null)
		{
			bHotel=rmHotel.shutdown();
		}
        Registry registry = LocateRegistry.getRegistry(mport);
        
        // Unregister middleware obj
        registry.unbind("Group28ResourceManager");

        // Unexport; this will also remove us from the RMI runtime
        UnicastRemoteObject.unexportObject(this, true);

        System.out.println("Middleware Server exiting.");
    
		if(bFlight && bCar && bHotel)
			return true;
		else
			return false;
	}
	// Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized(m_customerHT) {
            return (RMItem) m_customerHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
        synchronized(m_customerHT) {
        	m_customerHT.put(key, value);
        }
    }
    
 // Remove the item out of storage
    protected RMItem removeData(int id, String key) {
        synchronized(m_customerHT) {
            return (RMItem)m_customerHT.remove(key);
        }
    }
    
	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats,
			int flightPrice) throws RemoteException,TransactionAbortedException,
			InvalidTransactionException {
		
		if(rmFlight==null || m_RMState.isEmpty() || !m_RMState.get("flight"))
		{
			Trace.info("RM::addFlight called. Cannot add flight as RM is down.");
			return false;
		}
		String param1=Integer.toString(flightNum);
		if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
			return false;
		
		if(rmFlight.addFlight(id, flightNum, flightSeats, flightPrice))
			return true;
		else
			return false;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException,TransactionAbortedException,
			InvalidTransactionException 
	{
		if(rmCar==null || m_RMState.isEmpty() || !m_RMState.get("car"))
		{
			Trace.info("RM::addcar called. Cannot add car as RM is down.");
			return false;
		}
		if(trxnMgr.Lock(id, "car",location, TransactionMgr.WRITE)==false)
			return false;
		
		if(rmCar.addCars(id, location, numCars, price))
			return true;
		else
			return false;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		if(rmHotel==null || m_RMState.isEmpty() || !m_RMState.get("hotel"))
		{
			Trace.info("RM::addRooms called. Cannot add room as RM is down.");
			return false;
		}
		if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
			return false;
		
		if(rmHotel.addRooms(id, location, numRooms, price))
			return true;
		else
			return false;
	}

	@Override
	public int newCustomer(int id) throws RemoteException ,TransactionAbortedException,
	InvalidTransactionException
	{
		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer( cid );
        writeData( id, cust.getKey(), cust );
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
        return cid;
	}

	@Override
	public boolean newCustomer(int id, int customerID) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            cust = new Customer(customerID);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
            return false;
        } // else
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmFlight!=null)
		{
			String param1=Integer.toString(flightNum);
			if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
				return false;
			return rmFlight.deleteFlight(id, flightNum);
		}
		return false;
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmCar!=null)
		{
			if(trxnMgr.Lock(id, "car",location, TransactionMgr.WRITE)==false)
				return false;
			return rmCar.deleteCars(id, location);
		}
		return false;
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmHotel!=null)
		{
			if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
				return false;
			return rmHotel.deleteRooms(id, location);
		}
		return false;
	}

	@Override
	public boolean deleteCustomer(int id, int customerID) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else 
        {            
            // Increase the reserved numbers of all reservable items which the customer reserved. 
            RMHashtable reservationHT = cust.getReservations();
            for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) 
            {        
                String reservedkey = (String) (e.nextElement());
                ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
                
                ReservableItem item;
                if(rmFlight!=null)
                {
		            item = (ReservableItem) rmFlight.readData(id, reserveditem.getKey());
		            if(item!=null)//Item was a flight
		            {
		            	Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
		            	rmFlight.deleteReservation(id,reserveditem.getKey(),reserveditem.getCount());
		            }
                }
                if(rmCar!=null)
                {
	                item = (ReservableItem) rmCar.readData(id, reserveditem.getKey());
	                if(item!=null)//Item was a flight
	                {
	                	Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
	                	rmCar.deleteReservation(id,reserveditem.getKey(),reserveditem.getCount());
	                }
                }
                if(rmHotel!=null)
                {
	                item = (ReservableItem) rmHotel.readData(id, reserveditem.getKey());
	                if(item!=null)//Item was a flight
	                {
	                	Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
	                	rmHotel.deleteReservation(id,reserveditem.getKey(),reserveditem.getCount());
	                }
                }
            }
            
            // remove the customer from the storage
            removeData(id, cust.getKey());
            
            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
            return true;
        } // if
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmFlight!=null)
		{
			String param1=Integer.toString(flightNumber);
			if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.READ)==false)
				return -1;
			return rmFlight.queryFlight(id, flightNumber);
		}
		return -1;
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmCar!=null)
		{
			if(trxnMgr.Lock(id, "car",location, TransactionMgr.READ)==false)
				return -1;
			return rmCar.queryCars(id, location);
		}
		return -1;
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmHotel!=null)
		{
			if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.READ)==false)
				return -1;
			return rmHotel.queryRooms(id, location);
		}
		return -1;
	}

	@Override
	public String queryCustomerInfo(int id, int customerID)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                System.out.println( s );
                return s;
        } // if
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		if(rmFlight!=null)
		{
			String param1=Integer.toString(flightNumber);
			if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.READ)==false)
				return -1;
			return rmFlight.queryFlightPrice(id, flightNumber);
		}
		return -1;
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		if(rmCar!=null)
		{
			if(trxnMgr.Lock(id, "car",location, TransactionMgr.READ)==false)
				return -1;
			return rmCar.queryCarsPrice(id, location);
		}
		return -1;
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException
	{
		if(rmHotel!=null)
		{
			if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.READ)==false)
				return -1;
			return rmHotel.queryRoomsPrice(id, location);
		}
		return -1;
	}

	@Override
	public boolean reserveFlight(int id, int customerID, int flightNumber)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		if(rmFlight==null)
		{
			System.out.println("Flight RM is down.");
			return false;
		}
		String key = Flight.getKey(flightNumber);
		String location = String.valueOf(flightNumber);
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
        if ( cust == null ) {
            Trace.warn("RM::reserveFlight( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
            return false;
        } 
        String param1=Integer.toString(flightNumber);
		if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.READ)==false)
			return false;
        // check if the item is available
        ReservableItem item = (ReservableItem)rmFlight.readData(id, key);
        if ( item == null ) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
            return false;
        } else if (item.getCount()==0) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
            return false;
        } else 
        {   
    		if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
    			return false;
            cust.reserve( key, location, item.getPrice());
            writeData( id, cust.getKey(), cust );
            
            // decrease the number of available items in the storage
            rmFlight.reserveFlight(id, key);
            
            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        } 
	}

	@Override
	public boolean reserveCar(int id, int customerID, String location)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		if(rmCar==null)
		{
			System.out.println("Car RM is down.");
			return false;
		}
		String key = Car.getKey(location);
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
        if ( cust == null ) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
            return false;
        } 
        
		if(trxnMgr.Lock(id, "car",location, TransactionMgr.READ)==false)
			return false;
        // check if the item is available
        ReservableItem item = (ReservableItem)rmCar.readData(id, key);
        if ( item == null ) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
            return false;
        } else if (item.getCount()==0) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
            return false;
        } else 
        {            
        	if(trxnMgr.Lock(id, "car",location, TransactionMgr.WRITE)==false)
    			return false;
            cust.reserve( key, location, item.getPrice());
            writeData( id, cust.getKey(), cust );
            
            // decrease the number of available items in the storage
            rmCar.reserveCar(id,key);
            
            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        } 

	}

	@Override
	public boolean reserveRoom(int id, int customerID, String location)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		if(rmHotel==null)
		{
			System.out.println("Hotel RM is down.");
			return false;
		}
		String key = Hotel.getKey(location);
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
        if ( cust == null ) {
            Trace.warn("RM::reserveRoom( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
            return false;
        } 
        
		if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.READ)==false)
			return false;
        // check if the item is available
        ReservableItem item = (ReservableItem)rmHotel.readData(id, key);
        if ( item == null ) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
            return false;
        } else if (item.getCount()==0) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
            return false;
        } else 
        {            
        	if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
    			return false;
            cust.reserve( key, location, item.getPrice());
            writeData( id, cust.getKey(), cust );
            
            // decrease the number of available items in the storage
            rmHotel.reserveRoom(id,key);
            
            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        } 
	}

	@Override
	public boolean itinerary(int id, int customerID, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) 
        {
            Trace.warn("RM::reserveItinerary (" + id + ", " + customerID + ") failed--customer doesn't exist" );   
            return false;
        }
		boolean bItineraryReservable = true;
		int flightNumber;
		for(int i=0;bItineraryReservable && i<flightNumbers.size();i++)
		{
			flightNumber = Integer.valueOf((String) flightNumbers.get(i));
			String param1=Integer.toString(flightNumber);
			if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.READ)==false)
				bItineraryReservable = false;
			if(bItineraryReservable && queryFlight(customerID, flightNumber)<=0)
			{
				bItineraryReservable = false;
			}
		}
		if(bItineraryReservable && Car)
		{
			if(trxnMgr.Lock(id, "car",location, TransactionMgr.READ)==false)
				bItineraryReservable = false;
			if(bItineraryReservable && queryCars(customerID, location)<=0)
			{
				bItineraryReservable = false;
			}
		}
		if(bItineraryReservable && Room)
		{
			if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.READ)==false)
				bItineraryReservable = false;
			if(bItineraryReservable && queryRooms(customerID, location)<=0)
			{
				bItineraryReservable = false;
			}
		}
		//If everything reservable, then reserve the itinerary
		if(bItineraryReservable)
		{
			for(int i=0;i<flightNumbers.size();i++)
			{
				flightNumber = Integer.valueOf((String) flightNumbers.get(i));
				String param1=Integer.toString(flightNumber);
				if(trxnMgr.Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
				{
					bItineraryReservable = false;
				}
				if(!reserveFlight(id, customerID, flightNumber))
				{
					//Could have inconsistent data
					Trace.info("RM::reserveFlight( " + id + ", " + customerID + ", " +flightNumber+") failed" );
					
					bItineraryReservable = false;
				}
			}
			if(Car)
			{
				if(trxnMgr.Lock(id, "car",location, TransactionMgr.WRITE)==false)
				{
					bItineraryReservable = false;
				}
				if(!reserveCar(id, customerID, location))
				{
					//Could have inconsistent data
					Trace.info("RM::reserveCar( " + id + ", " + customerID + ", " +location+") failed" );
					
					bItineraryReservable = false;
				}
			}
			if(Room)
			{
				if(trxnMgr.Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
				{
					bItineraryReservable = false;
				}
				if(!reserveRoom(id, customerID, location))
				{
					//Could have inconsistent data
					Trace.info("RM::reserveRoom( " + id + ", " + customerID + ", " +location+") failed" );
					
					bItineraryReservable = false;
				}
			}
		}
		else//if not reservable
		{
			//trxnMgr.abort(id);
		}
		return bItineraryReservable;// actually send a string saying transaction aborted
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// Figure out where server is running
        String server = "localhost";
        String flightServer = "localhost";
        String carServer = "localhost";
        String hotelServer = "localhost";
        int port = 1099;

        if(args.length==0)
        {
        	System.err.println ("Please enter arguments 1) port \nand then any RMs which" +
        			" are running, as Strings followed by the machines on which they are running." +
        			"\nExample flight teaching,car localhost\n");
        	System.exit(1);
        }
        
        MiddlewareServer obj = new MiddlewareServer();
        TransactionMgr trxnMgr = new TransactionMgr();
        obj.trxnMgr=trxnMgr;
        //trxnMgr.mdServer=obj;
        TimeoutThread t1 = new TimeoutThread(trxnMgr);
        t1.start();
        obj.m_RMState = new HashMap<String, Boolean>();
        // Initialize frequency table from command line
        port = Integer.parseInt(args[0]);
        obj.mport=port;
        for(int i=0; i < args.length-1; i++)//for (String a : args)
        {
        	if (args[i].equals("flight")) {
                flightServer = args[i+1];
                obj.m_RMState.put(args[i], true);
            }
            if (args[i].equals("car")) {
                carServer = args[i+1];
                obj.m_RMState.put(args[i], true);
            }
            if (args[i].equals("hotel")) {
                hotelServer = args[i+1];
                obj.m_RMState.put(args[i], true);
            }
        }
        //Check for remote objects of RMs. Acts as Client to RMs.
        try 
        {
        	if(!obj.m_RMState.isEmpty())
        	{
        		if(obj.m_RMState.get("flight")!=null)
	        	{
		            // get a reference to the rmiregistry
		            Registry registry = LocateRegistry.getRegistry(flightServer, port);
		            // get the proxy and the remote reference by rmiregistry lookup
		            rmFlight = (FlightInterface) registry.lookup("Group28FlightRM");
		            if(rmFlight!=null)
		            {
		                System.out.println("\nConnected to FlightRM\n");
		                TransactionMgr.rmFlight = rmFlight;
		            }
		            else
		            {
		                System.out.println("\nConnection unsuccessful to Flight RM\n");
		            }
	        	}
        		if(obj.m_RMState.get("car")!=null)
	        	{
		            // get a reference to the rmiregistry
		            Registry registry = LocateRegistry.getRegistry(carServer, port);
		            // get the proxy and the remote reference by rmiregistry lookup
		            rmCar = (CarInterface) registry.lookup("Group28CarRM");
		            if(rmCar!=null)
		            {
		                System.out.println("\nConnected to CarRM\n");
		                TransactionMgr.rmCar=rmCar;
		            }
		            else
		            {
		                System.out.println("\nConnection unsuccessful to Car RM\n");
		            }
	        	}
        		if(obj.m_RMState.get("hotel")!=null)
	        	{
		            // get a reference to the rmiregistry
		            Registry registry = LocateRegistry.getRegistry(hotelServer, port);
		            // get the proxy and the remote reference by rmiregistry lookup
		            rmHotel = (HotelInterface) registry.lookup("Group28HotelRM");
		            if(rmHotel!=null)
		            {
		                System.out.println("\nConnected to HotelRM\n");
		                TransactionMgr.rmHotel=rmHotel;
		            }
		            else
		            {
		                System.out.println("\nConnection unsuccessful to Hotel RM\n");
		            }
	        	}
        	}
        } 
        catch (Exception e) 
        {    
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        
        //Register MiddlewareServer object. Acts as server for Clients.
        server = server + ":" + args[0];
        try
        {
        	ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("Group28ResourceManager", rm);

            System.err.println("Server ready");
        }
        catch (Exception e) 
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }


        

	}

}
