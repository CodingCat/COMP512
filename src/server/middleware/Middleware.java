package ResImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;

import ResInterface.ResourceManager;

public class Middleware implements ResourceManager
{

	static final int flightRM = 0;
	static final int carRM = 1;
	static final int hotelRM = 2;
	static final int RMNum = 3;
	// static int port_server = 1990;

	protected static ArrayList<ResourceManager> RMlist = new ArrayList();

	public Middleware(String flightRM1, int flightPort, String carRM1, int carPort, String roomRM1, int roomPort)
	{

		// Figure out where server is running
		String server = "localhost";

		/*
		 * if (RM.length != 3) { System.err.println("Wrong usage"); System.out.println("Usage: Need at least 3 RMs");
		 * System.exit(1); }
		 */

		/*
		 * Initialization; Perform as RMs, wait for clients
		 */

		// Create and install a security manager
		if (System.getSecurityManager() == null)
		{
			// System.setSecurityManager(new RMISecurityManager());
		}

		/*
		 * Initialization; Perform as clients Connect to RMs
		 */

	}

	protected RMHashtable m_itemHT = new RMHashtable();

	// Reads a data item
	private RMItem readData(int id, String key)
	{
		synchronized (m_itemHT)
		{
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	private void writeData(int id, String key, RMItem value)
	{
		synchronized (m_itemHT)
		{
			m_itemHT.put(key, value);
		}
	}

	// Remove the item out of storage
	protected RMItem removeData(int id, String key)
	{
		synchronized (m_itemHT)
		{
			return (RMItem) m_itemHT.remove(key);
		}
	}

	public void connection(String flightRM1, int flightPort, String carRM1, int carPort, String roomRM1, int roomPort,
			int rmi_port)
	{
		Middleware middlebox2 = new Middleware(flightRM1, flightPort, carRM1, carPort, roomRM1, roomPort);
		String[] RM = { flightRM1, carRM1, roomRM1 };
		int[] port = { flightPort, carPort, roomPort };

		try
		{
			// create a new Server object
			// dynamically generate the stub (client proxy)
			ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(middlebox2, 0);
			// Bind the remote object's stub in the registry
			// System.out.println(rmi_port);
			Registry registry = LocateRegistry.getRegistry(rmi_port);
			registry.rebind("30Middleware", rm);

			System.err.println("Middleware ready");
		}

		catch (Exception e)
		{
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		for (int i = 0; i < RM.length; i++)
		{
			try
			{
				// get a reference to the rmiregistry
				System.out.println("RM addr " + RM[i]);

				Registry registry = LocateRegistry.getRegistry(RM[i], port[i]);
				// get the proxy and the remote reference by rmiregistry lookup
				// ResourceManager rm = (ResourceManager) registry.lookup(args[i]);
				// add a RM to the list
				RMlist.add((ResourceManager) registry.lookup("RM" + port[i]));

				if (RMlist.get(i) != null)
				{
					System.out.println("Successful");
					System.out.println("Middleware Has Been Connected to RMs");
				}
				else
				{
					System.out.println("Unsuccessful");
				}
				// make call on remote method
			}
			catch (Exception e)
			{
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{
		// Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called");
		System.out.println("flight-comp512");
		synchronized (RMlist.get(flightRM))
		{
			try
			{
				if (RMlist.get(flightRM).addFlight(id, flightNum, flightSeats, flightPrice))
					System.out.println("Flight added");
				else
					System.out.println("Flight could not be added");
			}
			catch (Exception e)
			{
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return (true);
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called");
		synchronized (RMlist.get(hotelRM))
		{
			try
			{
				if (RMlist.get(hotelRM).addRooms(id, location, count, price))
					System.out.println("Rooms added");
				else
					System.out.println("Rooms could not be added");
			}
			catch (Exception e)
			{
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return (true);
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called");
		synchronized (RMlist.get(carRM))
		{
			try
			{
				if (RMlist.get(carRM).addCars(id, location, count, price))
					System.out.println("Cars added");
				else
					System.out.println("Cars could not be added");
			}
			catch (Exception e)
			{
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return (true);
	}

	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum) throws RemoteException
	{
		synchronized (RMlist.get(flightRM))
		{
			return RMlist.get(flightRM).queryFlight(id, flightNum);
		}
	}

	// Returns the number of reservations for this flight.
	// public int queryFlightReservations(int id, int flightNum)
	// throws RemoteException
	// {
	// Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
	// RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
	// if( numReservations == null ) {
	// numReservations = new RMInteger(0);
	// } // if
	// Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
	// return numReservations.getValue();
	// }

	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum) throws RemoteException
	{
		synchronized (RMlist.get(flightRM))
		{
			return RMlist.get(flightRM).queryFlightPrice(id, flightNum);
		}
	}

	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location) throws RemoteException
	{
		synchronized (RMlist.get(hotelRM))
		{
			return RMlist.get(hotelRM).queryRooms(id, location);
		}
	}

	// Returns room price at this location
	public int queryRoomsPrice(int id, String location) throws RemoteException
	{
		synchronized (RMlist.get(hotelRM))
		{
			return RMlist.get(hotelRM).queryRoomsPrice(id, location);
		}
	}

	// Returns the number of cars available at a location
	public int queryCars(int id, String location) throws RemoteException
	{
		synchronized (RMlist.get(carRM))
		{
			return RMlist.get(carRM).queryCars(id, location);
		}
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location) throws RemoteException
	{
		synchronized (RMlist.get(carRM))
		{
			return RMlist.get(carRM).queryCarsPrice(id, location);
		}
	}

	// Returns data structure containing customer reservation info. Returns null if the
	// customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	// reservations.

	// undo
	/*
	 * public RMHashtable getCustomerReservations(int id, int customerID) throws RemoteException {
	 * Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called"); Customer cust = (Customer)
	 * readData(id, Customer.getKey(customerID)); if (cust == null) { Trace.warn("RM::getCustomerReservations failed(" +
	 * id + ", " + customerID + ") failed--customer doesn't exist"); return null; } else { return
	 * cust.getReservations(); } // if }
	 */

	// return a bill
	public String queryCustomerInfo(int id, int customerID) throws RemoteException
	{
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called");

		String a1 = RMlist.get(carRM).queryCustomerInfo(id, customerID);
		String a2 = RMlist.get(hotelRM).queryCustomerInfo(id, customerID);
		String a3 = RMlist.get(flightRM).queryCustomerInfo(id, customerID);
		return a1 + a2 + a3;

	}

	// customer functions
	// new customer just returns a unique customer identifier

	public int newCustomer(int id) throws RemoteException
	{
		Trace.info("INFO: RM::newCustomer(" + id + ") called");
		System.out.println("cpslab");
		int cid = RMlist.get(carRM).newCustomer(id);
		// Generate a globally unique ID for the new customer
		RMlist.get(flightRM).addNewCustomer(cid);
		RMlist.get(hotelRM).addNewCustomer(cid);
		return cid;
	}

	// I opted to pass in customerID instead. This makes testing easier
	public boolean newCustomer(int id, int customerID) throws RemoteException
	{
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called");

		boolean a1 = RMlist.get(carRM).newCustomer(id, customerID);
		// Generate a globally unique ID for the new customer
		boolean a2 = RMlist.get(flightRM).newCustomer(id, customerID);
		boolean a3 = RMlist.get(hotelRM).newCustomer(id, customerID);
		return a1 && a2 && a3;

	}

	public int addNewCustomer(int cid) throws RemoteException
	{
		return 0;
	}

	public boolean deleteFlight(int id, int flightNum) throws RemoteException
	{
		return RMlist.get(flightRM).deleteFlight(id, flightNum);
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location) throws RemoteException
	{
		return RMlist.get(hotelRM).deleteRooms(id, location);

	}

	// Delete cars from a location
	public boolean deleteCars(int id, String location) throws RemoteException
	{
		return RMlist.get(carRM).deleteCars(id, location);

	}

	// Deletes customer from the database.
	public boolean deleteCustomer(int id, int customerID) throws RemoteException
	{
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called");
		RMlist.get(hotelRM).deleteCustomer(id, customerID);
		RMlist.get(carRM).deleteCustomer(id, customerID);
		RMlist.get(flightRM).deleteCustomer(id, customerID);

		return true;
	}

	// Frees flight reservation record. Flight reservation records help us make sure we
	// don't delete a flight if one or more customers are holding reservations
	// public boolean freeFlightReservation(int id, int flightNum)
	// throws RemoteException
	// {
	// Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
	// RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
	// if( numReservations != null ) {
	// numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
	// } // if
	// writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
	// Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
	// + numReservations + " reservations" );
	// return true;
	// }
	//
	// Adds car reservation to this customer.
	public boolean reserveCar(int id, int customerID, String location) throws RemoteException
	{
		return RMlist.get(carRM).reserveCar(id, customerID, location);
	}

	// Adds room reservation to this customer.
	public boolean reserveRoom(int id, int customerID, String location) throws RemoteException
	{
		return RMlist.get(hotelRM).reserveRoom(id, customerID, location);
	}

	// Adds flight reservation to this customer.
	public boolean reserveFlight(int id, int customerID, int flightNum) throws RemoteException
	{
		return RMlist.get(flightRM).reserveFlight(id, customerID, flightNum);
	}

	/* reserve an itinerary */
	public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room)
			throws RemoteException
	{
		Trace.info("RM::itinerary(" + id + ", " + customer + ") called");

		// Integer[] flightNumArr = (Integer[]) flightNumbers.toArray(new Integer[flightNumbers.size()]);
		// for (int flightNum : (Integer[]) flightNumbers.toArray())
		// for (int flightNum : flightNumArr)
		boolean flight = false, car = false, hotel = false;

		for (int i = 0; i < flightNumbers.size(); i++)
		{
			flight = RMlist.get(flightRM)
					.reserveFlight(id, customer, Integer.parseInt(((String) flightNumbers.get(i))));
		}

		if (Car)
			car = RMlist.get(carRM).reserveCar(id, customer, location);

		if (Room)
			hotel = RMlist.get(hotelRM).reserveRoom(id, customer, location);

		return flight && car && Car && hotel && Room;
	}
}
