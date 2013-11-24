/*
 * Author: Navjot Singh
 */
package serverplusTM.ResImpl;

//import ResImpl.TransGenericResourceManager.Tuple3;
import serverplusTM.ResInterface.CarInterface;
import serverplusTM.ResInterface.FlightInterface;
import serverplusTM.LockManager.InvalidTransactionException;
import serverplusTM.LockManager.TransactionAbortedException;
import serverplusTM.ResInterface.HotelInterface;
import serverplusTM.ResInterface.ResourceManager;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import biz.source_code.base64Coder.Base64Coder;

//import java.util.Vector;

public class MiddlewareServer extends ReceiverAdapter implements ResourceManager 
{
	
	/** Read the object from Base64 string. */
    private static Object fromString( String s ) throws IOException ,ClassNotFoundException 
    {
        byte [] data = Base64Coder.decode( s );
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    private static String toString(Serializable o) throws IOException 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String( Base64Coder.encode( baos.toByteArray() ) );
    }
    
    
	JChannel channel;
	String user_name=System.getProperty("user.name", "n/a");
	private void connectToGroup() throws Exception 
	{

		channel=new JChannel(); // use the default config, udp.xml
		channel.setReceiver(this);
		channel.connect("MiddlewareCluster");
		channel.getState(null, 0);
	}

	public void viewAccepted(View new_view) 
	{
		System.out.println("#####View changed. New View: " + new_view);
	}


	public void receive(Message msg) 
	{
		//check that this code is only executed if this is not primary MiddleWare.
		try
		{
			String command=(String)msg.getObject();
			String[] result = command.split(",");
			switch(result[0])
			{
			case "start":
				start();
				break;
			case "abort":
				abort(Integer.parseInt(result[1]));
				break;
			case "commit":
				commit(Integer.parseInt(result[1]));
				break;
			case "writeData":
				int trxnID=Integer.parseInt(result[1]);
				int operationID=Integer.parseInt(result[2]);
				String key = result[3];
				String valueStr=null;
				RMItem value=null;
				//The object in string form might get tokenized. So rejoining.
				if(result.length>5)
				{
					System.out.println("\n\n**************Special case****************\n\n");
					valueStr=result[4];
					for(int i=5;i<result.length;i++)
					{
						valueStr=valueStr+","+result[i];
					}
					value = (RMItem)fromString(valueStr);
				}
				else if(result.length==5)
				{
					value = (RMItem)fromString(result[4]);
				}
				writeData(trxnID, operationID, key, value);
				break;
			case "removeData":
				trxnID=Integer.parseInt(result[1]);
				operationID=Integer.parseInt(result[2]);
				key = result[3];
				removeData(trxnID, operationID, key);
				break;
			case "Lock":
				trxnID=Integer.parseInt(result[1]);
				Lock(trxnID, result[2], result[3], Integer.parseInt(result[4]));
				break;
			}
		}
		catch(Exception e)
		{
		}
	}
	public void getState(OutputStream output) throws Exception 
	{
		synchronized(m_customerHT) 
		{
			Util.objectToStream(m_customerHT, new DataOutputStream(output));
		}
	}
	public void setState(InputStream input) throws Exception {

		RMHashtable customerHT;

		customerHT=(RMHashtable)Util.objectFromStream(new DataInputStream(input));

		synchronized(m_customerHT) 
		{
			m_customerHT.clear();
			//TODO:check if deep copy works
			m_customerHT.putAll(customerHT);

		}
	}
	private void sendCommand(String command)
	{
		try
		{
			if(!isPrimary())
				return;
			Message msg=new Message(null, null, command);
			channel.send(msg);
		}
		catch(Exception e)
		{
			System.out.println("Exception while sending message to middleware group."+e.getMessage());
		}
	}
	@Override
	public boolean isPrimary() throws RemoteException
	{
		boolean bPrimary=false;
		Address adr1=channel.getAddress();
		Address adr2=channel.getView().getMembers().get(0);
		if(adr1.compareTo(adr2)==0)
			bPrimary=true;
		return bPrimary;
	}
	public void findPrimaryFlightRM()//here assign rmFlight to Primary flight RM
	{
		rmFlight=null;
		for(int i=0;i<rmFlightList.size();i++)
		{
			try
			{
				if(rmFlightList.get(i).isPrimary())
				{
					rmFlight=rmFlightList.get(i);
					break;
				}
			}
			catch(RemoteException e)
			{
			}
		}

	}
	public void findPrimaryCarRM()//here assign rmFlight to Primary flight RM
	{
		rmCar=null;
		for(int i=0;i<rmCarList.size();i++)
		{
			try
			{
				if(rmCarList.get(i).isPrimary())
				{
					rmCar=rmCarList.get(i);
					break;
				}
			}
			catch(RemoteException e)
			{
			}
		}

	}
	public void findPrimaryHotelRM()//here assign rmFlight to Primary flight RM
	{
		rmHotel=null;
		for(int i=0;i<rmHotelList.size();i++)
		{
			try
			{
				if(rmHotelList.get(i).isPrimary())
				{
					rmHotel=rmHotelList.get(i);
					break;
				}
			}
			catch(RemoteException e)
			{
			}
		}
	}
	//key=trxnID
	//value=List all operationIDs done.
	//This list keeps operationIDs and the results so that we do not repeat the same operations in case of failures. 
	private ConcurrentHashMap<Integer, HashMap<Integer,Object>> operationIDList=
			new ConcurrentHashMap<Integer, HashMap<Integer,Object>>();

	class Tuple3 {
		String key;
		int operation;    //  1 - write, 2 - delete
		RMItem value;
		Tuple3(String k, int op, RMItem nv) {
			key = k;
			operation = op;
			value = nv;
		}
	}

	//Undo operationList
	private ConcurrentHashMap<Integer, ArrayList<Tuple3>> operationList =
			new ConcurrentHashMap<Integer, ArrayList<Tuple3>>();//transaction_id -> tuple3 list

	private void checkOperationQueue(int id) 
	{
		if(!operationList.containsKey(id))
			//if (!operationList.contains(id)) 
		{
			System.out.println("Adding new trxn to operationList");
			operationList.put(id, new ArrayList<Tuple3>());
		}
	}
	protected RMHashtable m_customerHT = new RMHashtable();
	private Map<String,Boolean> m_RMState;
	FlightInterface rmFlight = null;
	CarInterface rmCar = null;
	HotelInterface rmHotel = null;
	//static int MaxFlightRMID=10;
	//static int MaxCarRMID=10;
	//static int MaxHotelRMID=10;
	ArrayList<FlightInterface> rmFlightList = new ArrayList<FlightInterface>();
	ArrayList<CarInterface> rmCarList = new ArrayList<CarInterface>();
	ArrayList<HotelInterface> rmHotelList = new ArrayList<HotelInterface>();
	private TransactionMgr trxnMgr;
	private int mport;
	private TimeoutThread timeout_thread;

	public int start() throws RemoteException
	{
		System.out.println("\n\nTransaction started\n\n");
		int ret=trxnMgr.start();
		String command="start";
		sendCommand(command);
		return ret;
	}
	public void abort(int txID) throws RemoteException,InvalidTransactionException, TransactionAbortedException
	{
		System.out.println("Middleware abort called");
		UndoCustomerTrxnData(txID);
		operationList.remove(txID);
		operationIDList.remove(txID);
		trxnMgr.abortFromMW(txID);
		String command="abort,"+txID;
		sendCommand(command);
		System.out.println("\n\nTransaction "+txID+" aborted\n\n");
	}
	public boolean commit(int txID) throws RemoteException, TransactionAbortedException,
	InvalidTransactionException
	{
		operationList.remove(txID);
		operationIDList.remove(txID);
		boolean bcommit=trxnMgr.commit(txID);
		String command="commit,"+txID;
		sendCommand(command);
		if(bcommit)
			System.out.println("\n\nTransaction "+txID+" commited\n\n");
		return bcommit;
	}
	public boolean Lock(int txID, String strData, String param1, int lockType) throws RemoteException,TransactionAbortedException,InvalidTransactionException
	{
		//try
		//{

		System.out.println("Trying to Lock on "+strData+"="+param1);
		boolean bLock=trxnMgr.Lock(txID, strData, param1, lockType);
		if(bLock)
		{
			String command="Lock,"+txID+","+strData+","+param1+","+lockType;
			sendCommand(command);
			System.out.println("Lock on "+strData+"="+param1+" is successful");
		}
		return bLock;
		//}
		/*catch(TransactionAbortedException e)
		{
			UndoCustomerTrxnData(txID);
			throw e;
		}*/
	}

	public boolean shutdown() throws RemoteException, NotBoundException
	{
		boolean bCar;
		boolean bHotel;
		boolean bFlight;
		bFlight=bHotel=bCar=true;

		if(rmFlight!=null)
		{
			System.out.println("Calling Flight RM shutdown");
			bFlight=rmFlight.shutdown();
		}
		if(rmCar!=null)
		{
			System.out.println("Calling Car RM shutdown");
			bCar=rmCar.shutdown();
		}
		if(rmHotel!=null)
		{
			System.out.println("Calling Hotel RM shutdown");
			bHotel=rmHotel.shutdown();
		}
		Registry registry = LocateRegistry.getRegistry(mport);

		// Unregister middleware obj
		registry.unbind("Group28ResourceManager");

		// Unexport; this will also remove us from the RMI runtime
		UnicastRemoteObject.unexportObject(this, true);

		timeout_thread.kill=true;

		System.out.println("Middleware Server exiting.");
		String command="shutdown";
		sendCommand(command);
		if(bFlight && bCar && bHotel)
			return true;
		else
			return false;
	}
	// Reads a data item
	private RMItem readData( int id, String key )
	{
		synchronized(m_customerHT) 
		{
			return (RMItem) m_customerHT.get(key);
		}
	}

	private void UndoCustomerTrxnData(int txID) throws RemoteException, TransactionAbortedException, InvalidTransactionException
	{
		ArrayList<Tuple3> list=this.operationList.get(txID);
		if(list==null)
			return;
		int size=list.size();
		System.out.println("UndoCustomerTrxnData called. Operation list contains "+size+" operations to undo");
		for(int i=0;i<size;i++)
		{
			Tuple3 obj=list.get(i);
			if(obj.operation==1)//was write operation
			{
				if(obj.value==null)
				{
					synchronized(m_customerHT)
					{
						System.out.println("New customer was called. So deleting the customer.");
						//no need to call delete customer as the cancelling of reservations 
						//are handled by each RM.
						//int custID=Customer.getCustomerID(obj.key);
						//System.out.println("Deleting customer"+custID);
						//deleteCustomer(txID, custID);

						m_customerHT.remove(obj.key);
					}
				}
				else
				{
					synchronized(m_customerHT)
					{
						System.out.println("Resetting the customer to its old value.");

						System.out.println("old Value is");
						System.out.println(((Customer)obj.value).printBill());
						System.out.println("new Value is");
						System.out.println(((Customer)readData(txID, obj.key)).printBill());
						m_customerHT.put(obj.key,obj.value);
					}
				}
			}
			else if(obj.operation==2)//was delete operation
			{
				synchronized(m_customerHT)
				{
					System.out.println("Deletecustomer was called. So Resetting the customer to its old value.");
					m_customerHT.put(obj.key,obj.value);
				}
			}
		}
	}
	// Writes a data item
	private void writeData( int id,int operationID, String key, RMItem value )
	{
		HashMap<Integer,Object> list=operationIDList.get(id);
		if(list.containsKey(operationID)) //operation already performed
			return;
		synchronized(m_customerHT) 
		{
			RMItem oldvalue = readData(id, key);
			System.out.println("********************Start**************************");
			System.out.println("Writing data in customer DB.");
			System.out.println("old Value is");
			if(oldvalue!=null)
				System.out.println(((Customer)oldvalue).printBill());
			else
				System.out.println("Old value was null");
			System.out.println("new Value is");
			System.out.println(((Customer)value).printBill());

			checkOperationQueue(id);
			this.operationList.get(id).add(0,new Tuple3(key,1,oldvalue));
			System.out.println("Operation List has "+this.operationList.get(id).size()+" operations");
			m_customerHT.put(key, value);
			String valueStr="";
			try
			{
				valueStr = toString(value);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			String command="writeData,"+id+","+operationID+","+key+","+valueStr;
			sendCommand(command);
			list.put(operationID,value);
			System.out.println("*********************End*************************");
		}
	}

	// Remove the item out of storage
	protected RMItem removeData(int id,int operationID, String key) 
	{
		HashMap<Integer,Object> list=operationIDList.get(id);
		if(list.containsKey(operationID)) //operation already performed
			return (RMItem)list.get(operationID);
		synchronized(m_customerHT) 
		{
			RMItem oldvalue = readData(id, key);
			checkOperationQueue(id);
			this.operationList.get(id).add(0,new Tuple3(key,2,oldvalue));

			RMItem ret=(RMItem)m_customerHT.remove(key);
			list.put(operationID,ret);
			String command="removeData,"+id+","+operationID+","+key;
			sendCommand(command);
			return ret;
		}
	}

	@Override
	public boolean addFlight(int id,int operationID, int flightNum, int flightSeats,
			int flightPrice) throws RemoteException,TransactionAbortedException,
			InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmFlight==null || m_RMState.isEmpty() || !m_RMState.get("flight"))
				{
					Trace.info("RM::addFlight called. Cannot add flight as RM is down.");
					return false;
				}
				String param1=Integer.toString(flightNum);
				if(Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
					return false;
				System.out.println("newFlight lock acquired.");
				if(rmFlight.addFlight(id,operationID, flightNum, flightSeats, flightPrice))
					return true;
				else
					return false;
			}
			catch(RemoteException e)
			{
				findPrimaryFlightRM();
			}
		}
	}

	@Override
	public boolean addCars(int id,int operationID, String location, int numCars, int price)
			throws RemoteException,TransactionAbortedException,
			InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmCar==null || m_RMState.isEmpty() || !m_RMState.get("car"))
				{
					Trace.info("RM::addcar called. Cannot add car as RM is down.");
					return false;
				}
				if(Lock(id, "car",location, TransactionMgr.WRITE)==false)
					return false;

				if(rmCar.addCars(id,operationID, location, numCars, price))
					return true;
				else
					return false;
			}
			catch(RemoteException e)
			{
				findPrimaryCarRM();
			}
		}
	}

	@Override
	public boolean addRooms(int id,int operationID, String location, int numRooms, int price)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		while(true)
		{
			try
			{
				if(rmHotel==null || m_RMState.isEmpty() || !m_RMState.get("hotel"))
				{
					Trace.info("RM::addRooms called. Cannot add room as RM is down.");
					return false;
				}
				if(Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
					return false;

				if(rmHotel.addRooms(id,operationID, location, numRooms, price))
					return true;
				else
					return false;
			}
			catch(RemoteException e)
			{
				findPrimaryHotelRM();
			}
		}
	}

	@Override
	public int newCustomer(int id,int operationID) throws RemoteException ,TransactionAbortedException,
	InvalidTransactionException
	{
		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt( String.valueOf(id) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		if(Lock(id, "customer",Integer.toString(cid), TransactionMgr.WRITE)==false)
			return -1;
		Customer cust = new Customer( cid );
		writeData( id,operationID, cust.getKey(), cust );
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
		return cid;
	}

	@Override
	public boolean newCustomer(int id,int operationID, int customerID) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
		if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
			return false;
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if ( cust == null ) {
			if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.WRITE)==false)
				return false;
			cust = new Customer(customerID);
			writeData( id,operationID, cust.getKey(), cust );
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
			return true;
		} else {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
			return false;
		} // else
	}

	@Override
	public boolean deleteFlight(int id,int operationID, int flightNum) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmFlight!=null)
				{
					String param1=Integer.toString(flightNum);
					if(Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
						return false;
					return rmFlight.deleteFlight(id,operationID, flightNum);
				}
				return false;
			}
			catch(RemoteException e)
			{
				findPrimaryFlightRM();
			}
		}
	}

	@Override
	public boolean deleteCars(int id,int operationID, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmCar!=null)
				{
					if(Lock(id, "car",location, TransactionMgr.WRITE)==false)
						return false;
					return rmCar.deleteCars(id,operationID, location);
				}
				return false;
			}
			catch(RemoteException e)
			{
				findPrimaryCarRM();
			}
		}
	}

	@Override
	public boolean deleteRooms(int id,int operationID, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmHotel!=null)
				{
					if(Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
						return false;
					return rmHotel.deleteRooms(id,operationID, location);
				}
				return false;
			}
			catch(RemoteException e)
			{
				findPrimaryHotelRM();
			}
		}
	}

	@Override
	public boolean deleteCustomer(int id,int operationID, int customerID) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
		if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
			return false;
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if ( cust == null ) {
			Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return false;
		} else 
		{    
			if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.WRITE)==false)
				return false;
			// Increase the reserved numbers of all reservable items which the customer reserved.
			
			RMHashtable reservationHT = cust.getReservations();
			for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) 
			{        
				String reservedkey = (String) (e.nextElement());
				ReservedItem reserveditem = cust.getReservedItem(reservedkey);
				Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
				String strData=reserveditem.getKey();
				int index=strData.indexOf("-");
				String strData1=strData.substring(0,index);
				String strData2=strData.substring(index+1);
				System.out.println("Deleting the customer type="+strData1+" number="+strData2);
				ReservableItem item;
				boolean primaryRMfound=true;
				while(primaryRMfound)
				{
					try
					{
						if(rmFlight!=null && strData1.equals("flight"))
						{
							//Could be erroneous. #Check#.
							if(Lock(id,strData1,strData2,TransactionMgr.READ)==false)
								return false;
							item = (ReservableItem) rmFlight.readData(id, reserveditem.getKey());
							if(item!=null)//Item was a flight
							{
								Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " +
                                        reserveditem.getKey() + "which is reserved" +  item.getReserved() +
                                        " times and is still available " + item.getCount() + " times"  );
								if(Lock(id,strData1,strData2,TransactionMgr.WRITE)==false)
									return false;
								boolean rt = rmFlight.deleteReservation(id,
                                        operationID,reserveditem.getKey(),reserveditem.getCount());
								if(rt)
									primaryRMfound=false;
							}
						}
					}
					catch(RemoteException ec)
					{
						primaryRMfound=true;
						findPrimaryFlightRM();
					}
				}
				primaryRMfound=true;
				while(primaryRMfound)
				{
					try
					{
						if(rmCar!=null && strData1.equals("car"))
						{
							if(Lock(id,strData1,strData2,TransactionMgr.READ)==false)
								return false;
							item = (ReservableItem) rmCar.readData(id, reserveditem.getKey());
							if(item!=null)//Item was a flight
							{
								Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
								if(Lock(id,strData1,strData2,TransactionMgr.WRITE)==false)
									return false;
								boolean rt=rmCar.deleteReservation(id,operationID,reserveditem.getKey(),reserveditem.getCount());
								if(rt)
									primaryRMfound=false;
							}
						}
					}
					catch(RemoteException ec)
					{
						primaryRMfound=true;
						findPrimaryCarRM();
					}
				}
				primaryRMfound=true;
				while(primaryRMfound)
				{
					try
					{
						if(rmHotel!=null && strData1.equals("hotel"))
						{
							if(Lock(id,strData1,strData2,TransactionMgr.READ)==false)
								return false;
							item = (ReservableItem) rmHotel.readData(id, reserveditem.getKey());
							if(item!=null)//Item was a flight
							{
								Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
								if(Lock(id,strData1,strData2,TransactionMgr.WRITE)==false)
									return false;
								boolean rt=rmHotel.deleteReservation(id,operationID,reserveditem.getKey(),reserveditem.getCount());
								if(rt)
									primaryRMfound=false;
							}
						}
					}
					catch(RemoteException ec)
					{
						primaryRMfound=true;
						findPrimaryHotelRM();
					}
				}
			}
			

			// remove the customer from the storage
			removeData(id,operationID, cust.getKey());

			Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
			return true;
		} // if
	}

	@Override
	public int queryFlight(int id,int operationID, int flightNumber) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmFlight!=null)
				{
					String param1=Integer.toString(flightNumber);
					if(Lock(id, "flight",param1, TransactionMgr.READ)==false)
						return -1;
					return rmFlight.queryFlight(id,operationID, flightNumber);
				}
				return -1;
			}
			catch(RemoteException e)
			{
				findPrimaryFlightRM();
			}
		}
	}

	@Override
	public int queryCars(int id,int operationID, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmCar!=null)
				{
					if(Lock(id, "car",location, TransactionMgr.READ)==false)
						return -1;
					return rmCar.queryCars(id,operationID, location);
				}
				return -1;
			}
			catch(RemoteException e)
			{
				findPrimaryCarRM();
			}
		}
	}

	@Override
	public int queryRooms(int id,int operationID, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmHotel!=null)
				{
					if(Lock(id, "hotel",location, TransactionMgr.READ)==false)
						return -1;
					return rmHotel.queryRooms(id,operationID, location);
				}
				return -1;
			}
			catch(RemoteException e)
			{
				findPrimaryHotelRM();
			}
		}
	}

	@Override
	public String queryCustomerInfo(int id,int operationID, int customerID)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
		if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
			return "";
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
	public int queryFlightPrice(int id,int operationID, int flightNumber)
			throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		while(true)
		{
			try
			{
				if(rmFlight!=null)
				{
					String param1=Integer.toString(flightNumber);
					if(Lock(id, "flight",param1, TransactionMgr.READ)==false)
						return -1;
					return rmFlight.queryFlightPrice(id,operationID, flightNumber);
				}
				return -1;
			}
			catch(RemoteException e)
			{
				findPrimaryFlightRM();
			}
		}
	}

	@Override
	public int queryCarsPrice(int id,int operationID, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException 
	{
		while(true)
		{
			try
			{
				if(rmCar!=null)
				{
					if(Lock(id, "car",location, TransactionMgr.READ)==false)
						return -1;
					return rmCar.queryCarsPrice(id,operationID, location);
				}
				return -1;
			}
			catch(RemoteException e)
			{
				findPrimaryCarRM();
			}
		}
	}

	@Override
	public int queryRoomsPrice(int id,int operationID, String location) throws RemoteException,TransactionAbortedException,
	InvalidTransactionException
	{
		while(true)
		{
			try
			{
				if(rmHotel!=null)
				{
					if(Lock(id, "hotel",location, TransactionMgr.READ)==false)
						return -1;
					return rmHotel.queryRoomsPrice(id,operationID, location);
				}
				return -1;
			}
			catch(RemoteException e)
			{
				findPrimaryHotelRM();
			}
		}
	}

	@Override
	public boolean reserveFlight(int id,int operationID, int customerID, int flightNumber)
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
				if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
					return false;
				Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
				if ( cust == null ) {
					Trace.warn("RM::reserveFlight( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
					return false;
				} 
				String param1=Integer.toString(flightNumber);
				if(Lock(id, "flight",param1, TransactionMgr.READ)==false)
					return false;
				// check if the item is available
				ReservableItem item=null;
				boolean bPrimaryRMnotFound=true;
				while(bPrimaryRMnotFound)
				{
					try
					{
						item = (ReservableItem)rmFlight.readData(id, key);
						bPrimaryRMnotFound=false;
					}
					catch(RemoteException e)
					{
						bPrimaryRMnotFound=true;
						findPrimaryFlightRM();
					}
				}
				if ( item == null ) {
					Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
					return false;
				} else if (item.getCount()==0) {
					Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
					return false;
				} else 
				{   
					if(Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
						return false;
					if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.WRITE)==false)
						return false;
					Customer newcustomer=cust.clone();
					newcustomer.reserve( key, location, item.getPrice());//#Check#
					writeData( id,operationID, newcustomer.getKey(), newcustomer );
					//TODO: sendMessage to all replicas "writeData( id, newcustomer.getKey(), newcustomer );"
					//TODO: Customer class needs to implement serializable interface.

					// decrease the number of available items in the storage
					//if(primary)
					bPrimaryRMnotFound=true;
					while(bPrimaryRMnotFound)
					{
						try
						{
							rmFlight.reserveFlight(id,operationID, key);
							bPrimaryRMnotFound=false;
						}
						catch( RemoteException e)
						{
							bPrimaryRMnotFound=true;
							findPrimaryFlightRM();
						}
					}

					//fails here

					//set state()
					Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
					return true;
				}
				//bExceptionOccured=false;//if everything ok would not reach this statement
			
		
	}

	@Override
	public boolean reserveCar(int id,int operationID, int customerID, String location)
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
		if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
			return false;
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
		if ( cust == null ) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return false;
		} 

		if(Lock(id, "car",location, TransactionMgr.READ)==false)
			return false;
		// check if the item is available
		ReservableItem item=null;
		boolean bPrimaryRMnotFound=true;
		while(bPrimaryRMnotFound)
		{
			try
			{
				item = (ReservableItem)rmCar.readData(id, key);
				bPrimaryRMnotFound=false;
			}
			catch(RemoteException e)
			{
				bPrimaryRMnotFound=true;
				findPrimaryCarRM();
			}
		}
		if ( item == null ) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
			return false;
		} else if (item.getCount()==0) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
			return false;
		} else 
		{            
			if(Lock(id, "car",location, TransactionMgr.WRITE)==false)
				return false;
			if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.WRITE)==false)
				return false;
			Customer newcustomer=cust.clone();
			newcustomer.reserve( key, location, item.getPrice());
			writeData( id,operationID, cust.getKey(), newcustomer );

			bPrimaryRMnotFound=true;
			while(bPrimaryRMnotFound)
			{
				try
				{
					// decrease the number of available items in the storage
					rmCar.reserveCar(id,operationID,key);
					bPrimaryRMnotFound=false;
				}
				catch(RemoteException e)
				{
					bPrimaryRMnotFound = true;
					findPrimaryCarRM();
				}
			}

			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return true;
		} 

			}

	@Override
	public boolean reserveRoom(int id,int operationID, int customerID, String location)
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
		if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
			return false;
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
		if ( cust == null ) {
			Trace.warn("RM::reserveRoom( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return false;
		} 

		if(Lock(id, "hotel",location, TransactionMgr.READ)==false)
			return false;
		ReservableItem item=null;
		boolean bPrimaryRMnotFound=true;
		while(bPrimaryRMnotFound)
		{
			try
			{
				// check if the item is available
				item = (ReservableItem)rmHotel.readData(id, key);
				bPrimaryRMnotFound=false;
			}
			catch(RemoteException e)
			{
				bPrimaryRMnotFound=true;
				findPrimaryHotelRM();
			}
		}
		if ( item == null ) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
			return false;
		} else if (item.getCount()==0) {
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
			return false;
		} else 
		{            
			if(Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
				return false;
			if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.WRITE)==false)
				return false;
			Customer newcustomer=cust.clone();
			newcustomer.reserve( key, location, item.getPrice());
			writeData( id,operationID, cust.getKey(), newcustomer );

			bPrimaryRMnotFound=true;
			while(bPrimaryRMnotFound)
			{
				try
				{
					// decrease the number of available items in the storage
					rmHotel.reserveRoom(id,operationID,key);
					bPrimaryRMnotFound=false;
				}
				catch(RemoteException e)
				{
					bPrimaryRMnotFound=true;
					findPrimaryHotelRM();
				}
			}

			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return true;
		} 
	}

	@Override
	public boolean itinerary(int id,int operationID, int customerID, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws RemoteException ,TransactionAbortedException,
			InvalidTransactionException
	{
		if(Lock(id, "customer",Integer.toString(customerID), TransactionMgr.READ)==false)
			return false;
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if ( cust == null ) 
		{
			Trace.warn("RM::reserveItinerary (" + id + ", " + customerID + ") failed--customer doesn't exist" );   
			return false;
		}
		boolean bItineraryReservable = true;
		int flightNumber;
		int opID=operationID;
		for(int i=0;bItineraryReservable && i<flightNumbers.size();i++)
		{
			flightNumber = Integer.valueOf((String) flightNumbers.get(i));
			String param1=Integer.toString(flightNumber);
			if(Lock(id, "flight",param1, TransactionMgr.READ)==false)
				bItineraryReservable = false;
			if(bItineraryReservable && queryFlight(id,opID, flightNumber)<=0)
			{
				bItineraryReservable = false;
			}
		}
		if(bItineraryReservable && Car)
		{
			if(Lock(id, "car",location, TransactionMgr.READ)==false)
				bItineraryReservable = false;
			if(bItineraryReservable && queryCars(id,opID, location)<=0)
			{
				bItineraryReservable = false;
			}
		}
		if(bItineraryReservable && Room)
		{
			if(Lock(id, "hotel",location, TransactionMgr.READ)==false)
				bItineraryReservable = false;
			if(bItineraryReservable && queryRooms(id,opID, location)<=0)
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
				if(Lock(id, "flight",param1, TransactionMgr.WRITE)==false)
				{
					bItineraryReservable = false;
				}
				if(!reserveFlight(id,opID, customerID, flightNumber))
				{
					//Could have inconsistent data
					Trace.info("RM::reserveFlight( " + id + ", " + customerID + ", " +flightNumber+") failed" );

					bItineraryReservable = false;
				}
				opID++;
			}
			opID=operationID+flightNumbers.size();
			if(Car)
			{
				if(Lock(id, "car",location, TransactionMgr.WRITE)==false)
				{
					bItineraryReservable = false;
				}
				if(!reserveCar(id,opID, customerID, location))
				{
					//Could have inconsistent data
					Trace.info("RM::reserveCar( " + id + ", " + customerID + ", " +location+") failed" );

					bItineraryReservable = false;
				}
				opID++;
			}
			if(Room)
			{
				if(Lock(id, "hotel",location, TransactionMgr.WRITE)==false)
				{
					bItineraryReservable = false;
				}
				if(!reserveRoom(id,opID, customerID, location))
				{
					//Could have inconsistent data
					Trace.info("RM::reserveRoom( " + id + ", " + customerID + ", " +location+") failed" );

					bItineraryReservable = false;
				}
				opID++;
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
		String serverLookUpName = "Group28ResourceManager";
		ArrayList<String> flightServer = new ArrayList<String>();
		ArrayList<String> carServer = new ArrayList<String>();
		ArrayList<String> hotelServer = new ArrayList<String>();
		ArrayList<String> flightRMLookUp = new ArrayList<String>();
		ArrayList<String> carRMLookUp = new ArrayList<String>();
		ArrayList<String> hotelRMLookUp = new ArrayList<String>();
		int port = 1099;

		if(args.length==0)
		{
			System.err.println ("Please enter arguments 1) <port> \n2)<Middleware Server Lookup name>" +
					"\nand then any RMs which" +
					" are running, as Strings followed by the machines on which they are running." +
					"and RMI lookup names" +
					"\nExample:\n 8228 Group28ResourceManager flight <machine name> <RMI lookup name> flight <machine name> " +
					"<RMI lookup name>" +
					" car <machine name> <RMI lookup name> " +
					"hotel <machine name> <RMI lookup name> hotel <machine name> <RMI lookup name>\n");
			System.exit(1);
		}

		MiddlewareServer obj = new MiddlewareServer();
		TransactionMgr trxnMgr = new TransactionMgr();
		obj.trxnMgr=trxnMgr;
		trxnMgr.mdServer=obj;
		TimeoutThread t1 = new TimeoutThread(trxnMgr);
		obj.timeout_thread=t1;
		t1.start();
		obj.m_RMState = new HashMap<String, Boolean>();
		// Initialize frequency table from command line
		port = Integer.parseInt(args[0]);
		serverLookUpName = args[1];
		obj.mport=port;
		for(int i=2; i < args.length-2; i++)
		{
			if (args[i].equals("flight")) {
				flightServer.add(args[i+1]);//machine name
				flightRMLookUp.add(args[i+2]);//Remote object name
				obj.m_RMState.put(args[i], true);
			}
			if (args[i].equals("car")) {
				carServer.add(args[i+1]);
				carRMLookUp.add(args[i+2]);
				obj.m_RMState.put(args[i], true);
			}
			if (args[i].equals("hotel")) {
				hotelServer.add(args[i+1]);
				hotelRMLookUp.add(args[i+2]);
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
					for(int itr=0;itr<flightServer.size();itr++)
					{
						// get a reference to the rmiregistry
						Registry registry = LocateRegistry.getRegistry(flightServer.get(itr), port);
						// get the proxy and the remote reference by rmiregistry lookup
						obj.rmFlightList.add((FlightInterface) registry.lookup(flightRMLookUp.get(itr)));
					}
					obj.findPrimaryFlightRM();
					if(obj.rmFlight!=null)
					{
						System.out.println("\nConnected to FlightRM\n");
						TransactionMgr.rmFlight = obj.rmFlight;
					}
					else
					{
						System.out.println("\nConnection unsuccessful to Flight RM\n");
					}
				}
				if(obj.m_RMState.get("car")!=null)
				{
					for(int itr=0;itr<carServer.size();itr++)
					{
						// get a reference to the rmiregistry
						Registry registry = LocateRegistry.getRegistry(carServer.get(itr), port);
						// get the proxy and the remote reference by rmiregistry lookup
						obj.rmCarList.add((CarInterface) registry.lookup(carRMLookUp.get(itr)));
					}
					obj.findPrimaryCarRM();
					if(obj.rmCar!=null)
					{
						System.out.println("\nConnected to CarRM\n");
						TransactionMgr.rmCar=obj.rmCar;
					}
					else
					{
						System.out.println("\nConnection unsuccessful to Car RM\n");
					}
				}
				if(obj.m_RMState.get("hotel")!=null)
				{
					for(int itr=0;itr<hotelServer.size();itr++)
					{
						// get a reference to the rmiregistry
						Registry registry = LocateRegistry.getRegistry(hotelServer.get(itr), port);
						// get the proxy and the remote reference by rmiregistry lookup
						obj.rmHotelList.add((HotelInterface) registry.lookup(hotelRMLookUp.get(itr)));
					}
					obj.findPrimaryHotelRM();
					if(obj.rmHotel!=null)
					{
						System.out.println("\nConnected to HotelRM\n");
						TransactionMgr.rmHotel=obj.rmHotel;
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
			obj.connectToGroup();
			ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind(serverLookUpName, rm);


			System.err.println("Server ready");
		}
		catch (Exception e) 
		{
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}




	}
}
