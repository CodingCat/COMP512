/*
 * Author: Navjot Singh
 */
package serverplusTM.ResImpl;

import org.jgroups.Message;
import serverplusTM.ResInterface.HotelInterface;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HotelResourceManager extends TransGCResourceManager implements HotelInterface
{
    class HotelMessageHandler extends GenericMessageHandler {
        public HotelMessageHandler(String groupName) {
            super(groupName);
        }

        @Override
        public void receive(Message msg) {
            super.receive(msg);
            String msgString = (String) msg.getObject();
            String [] arr = msgString.split(";");
            String type = arr[0];
            if (type.equals("write")) {
                String [] parameters = arr[1].split(",");
                Hotel f = new Hotel(parameters[2],
                        Integer.parseInt(parameters[3]),
                        Integer.parseInt(parameters[4]));
                f.setReserved(Integer.parseInt(parameters[5]));
                realizeWriteMessage(Integer.parseInt(parameters[0]), Integer.parseInt(parameters[1]),
                        f.getKey(), f);
            }
        }
    }

    public HotelResourceManager(String groupName, String xmlPath) {
        super(groupName, xmlPath);
        mh = new HotelMessageHandler(groupName);
        mh.castMessage("join", null);
        sendSyncRequest();
    }

    public RMItem readData( int id, String key )
    {
        return readDatafromRM(id, key);
    }

    public boolean deleteReservation(int id, int opID, String key,int reservedItemCount)
    {
        return deleteReservationfromRM(id, opID, key, reservedItemCount);
    }

    @Override
    public synchronized boolean addRooms(int id, int opID, String location, int count, int price)
            throws RemoteException {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if ( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            writeData( id, opID, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " +
                    location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            Hotel newObj = curObj.clone();
            newObj.setCount( newObj.getCount() + count );
            if ( price > 0 ) {
                newObj.setPrice( price );
            } // if
            writeData( id, opID, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") modified existing location " +
                    location + ", count=" + newObj.getCount() + ", price=$" + price );
        } // else
        return true;
    }

    @Override
    public synchronized boolean deleteRooms(int id, int opID, String location) throws RemoteException
    {
        return deleteItem(id, opID, Hotel.getKey(location));
    }

    @Override
    public int queryRooms(int id, int opID, String location) throws RemoteException
    {
        return queryNum(id, opID, Hotel.getKey(location));
    }

    @Override
    public int queryRoomsPrice(int id, int opID, String location) throws RemoteException
    {
        return queryPrice(id, opID, Hotel.getKey(location));
    }

    @Override
    public boolean reserveRoom(int id, int opID, String key)
            throws RemoteException
    {
        return reserveItem(id, opID, key);
    }


    public boolean shutdown() {
        return true;
    }

    public boolean commit(int txid) {
        return super.commit(txid);
    }

    public boolean abort(int txid)  {
        return super.abort(txid);
    }

    @Override
    protected void realizeSyncResponse(String syncStr) {
        String [] objsyncArray = syncStr.split(";");
        for (int i = 0; i < objsyncArray.length; i++) {
            String [] objarr = objsyncArray[i].split(",");
            Hotel c = new Hotel(objarr[0],
                    Integer.parseInt(objarr[1]),
                    Integer.parseInt(objarr[2]));
            c.setReserved(Integer.parseInt(objarr[3]));
            saveData(c.getKey(), c);
        }
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
            System.out.println("Usage: java ResImpl.CarResourceManager [port] groupName objname");
            System.exit(1);
        }

        try {
            // create a new Server object
            HotelResourceManager obj = new HotelResourceManager(args[1], args[2]);
            obj.m_itemHT = new RMHashtable();
            // dynamically generate the stub (client proxy)
            HotelInterface rm = (HotelInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind(args[2], rm);

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

}
