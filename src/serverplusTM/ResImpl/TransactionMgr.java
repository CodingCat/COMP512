package serverplusTM.ResImpl;

import serverplusTM.LockManager.DeadlockException;
import serverplusTM.LockManager.InvalidTransactionException;
import serverplusTM.LockManager.LockManager;
import serverplusTM.LockManager.TransactionAbortedException;
import serverplusTM.ResInterface.CarInterface;
import serverplusTM.ResInterface.FlightInterface;
import serverplusTM.ResInterface.HotelInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransactionMgr
{

	/**
	 * @param args
	 */
	public static final int NumTransactions=1000;//Max num of trxns allowed
	private Map<Integer,Boolean> TrxnIDList;//maintains the transactions IDs
	public static final int READ = LockManager.READ;
    public static final int WRITE = LockManager.WRITE;
	static FlightInterface rmFlight = null;
	static CarInterface rmCar = null;
	static HotelInterface rmHotel = null;
	private Map<Integer,ArrayList<String>> TxnRsMgrMap;//maintains the RMs which the trxn is communicating
	Map<Integer,Long> TxnTimestampMap;//For timeout of trxns
    LockManager lm;
	//MiddlewareServer mdServer;
    TransactionMgr()
    {
    	TrxnIDList = new HashMap<Integer, Boolean>();
    	for(int i=1;i<=NumTransactions;i++)
    	{
    		TrxnIDList.put(i, false);
    	}
    	TxnRsMgrMap = new HashMap<Integer, ArrayList<String>>();
    	TxnTimestampMap = new HashMap<Integer, Long>();
    }
    public int start() throws RemoteException
	{
		int txID=-1;
		for(int i=1;i<=NumTransactions;i++)
    	{
			if(TrxnIDList.get(i)==false)
			{
				txID=i;
				TrxnIDList.put(i, true);
				break;
			}
    	}
		if(txID==-1)
			return txID;
		TxnRsMgrMap.put(txID,new ArrayList<String>());
		Date d = new Date();
		long currtime=d.getTime();
		TxnTimestampMap.put(txID, currtime);
		return txID;
	}
	boolean commit(int txID) throws RemoteException,TransactionAbortedException,InvalidTransactionException
	{
		String[] txnsStr = (String[])TxnRsMgrMap.get(txID).toArray();
		int size=txnsStr.length;
		if(size==0)
			throw new InvalidTransactionException(txID,"Please restart the transaction");
		for(int i=0;i<size;i++)
		{
			/*switch(txnsStr[i])
			{
			case "flight":
				if(rmFlight!=null)
					rmFlight.commit(txID);
				break;
			case "car":
				if(rmCar!=null)
					rmCar.commit(txID);
				break;
			case "hotel":
				if(rmHotel!=null)
					rmHotel.commit(txID);
				break;
			} */
            if(txnsStr[i].equals("flight")) {
                if(rmFlight!=null)
                    rmFlight.commit(txID);
            }
            if (txnsStr[i].equals("car")) {
                if(rmCar!=null)
                    rmCar.commit(txID);
            }
            if (txnsStr[i].equals("hotel")) {
                if(rmHotel!=null)
                    rmHotel.commit(txID);
            }

		}
		TxnRsMgrMap.remove(txID);
		TxnTimestampMap.remove(txID);
		TrxnIDList.put(txID, false);
		lm.UnlockAll(txID);
		return true;
	}
	void abort(int txID) throws RemoteException,InvalidTransactionException
	{
		String[] txnsStr = (String[])TxnRsMgrMap.get(txID).toArray();
		int size=txnsStr.length;
		if(size==0)
			throw new InvalidTransactionException(txID,"Please restart the transaction");
		for(int i=0;i<size;i++)
		{
			/*switch(txnsStr[i])
			{
			case "flight":
				if(rmFlight!=null)
					rmFlight.abort(txID);
				break;
			case "car":
				if(rmCar!=null)
					rmCar.abort(txID);
				break;
			case "hotel":
				if(rmHotel!=null)
					rmHotel.abort(txID);
				break;
			} */
            if(txnsStr[i].equals("flight")) {
                if(rmFlight!=null)
                    rmFlight.abort(txID);
            }
            if (txnsStr[i].equals("car")) {
                if(rmCar!=null)
                    rmCar.abort(txID);
            }
            if (txnsStr[i].equals("hotel")) {
                if(rmHotel!=null)
                    rmHotel.abort(txID);
            }
		}
		TxnRsMgrMap.remove(txID);
		TxnTimestampMap.remove(txID);
		TrxnIDList.put(txID, false);
		lm.UnlockAll(txID);
	}
	//param1=flightNum for flight RM
	//param1=locations for Hotel and Car RM
	public boolean Lock(int txID, String strData, String param1, int lockType) throws RemoteException,TransactionAbortedException,InvalidTransactionException
	{
		boolean found=false;
		ArrayList<String> ListStr=TxnRsMgrMap.get(txID);
		if(ListStr==null)//Transaction was not started or was aborted
			throw new InvalidTransactionException(txID,"Please restart the transaction");
		String[] txnsStr = (String[])ListStr.toArray();
		int size=txnsStr.length;
		for(int i=0;i<size;i++)
		{
			if(txnsStr[i]==strData)
				found=true;
		}
		if(!found)
		{
			TxnRsMgrMap.get(txID).add(strData);
		}
		strData=strData+"="+param1;
		Date d = new Date();
		long currtime=d.getTime();
		TxnTimestampMap.put(txID, currtime);
		try
		{
			return lm.Lock(txID, strData, lockType);
		}
		catch(DeadlockException e)
		{
			abort(txID);
			return false;
		}
	}

}

class TimeoutThread extends Thread {

	TransactionMgr tm;
	long timeoutVal=60000;//60sec
    public TimeoutThread (TransactionMgr tm) {
    	this.tm=tm;
    }

    public void run () 
    {
    	ArrayList<Integer> ListtxIDDel = new ArrayList<Integer>();
    	while(true)
    	{
    		Integer[] key_Set=(Integer[])tm.TxnTimestampMap.keySet().toArray();
    		int size=key_Set.length;
    		if(size>0)
    		{
	    		Date d = new Date();
	    		long currtime=d.getTime();
	    		for(int i=0;i<size;i++)
	    		{
	    			if(currtime-tm.TxnTimestampMap.get(key_Set[i])>timeoutVal)
	    			{
	    				ListtxIDDel.add(key_Set[i]);
                        try {
	    				    tm.abort(key_Set[i]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
	    			}
	    		}
	    		for(int i=0;i<ListtxIDDel.size();i++)
	    		{
	    			tm.TxnTimestampMap.remove(ListtxIDDel.get(i));
	    		}
	    		ListtxIDDel.clear();
    		}
	    	try 
	    	{
	    		TimeoutThread.sleep (2000);
		    }
		    catch (InterruptedException e) 
		    { 
		    }
    	}
    }
}