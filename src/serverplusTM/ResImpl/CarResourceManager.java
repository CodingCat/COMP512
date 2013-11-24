/*
 * Author: Navjot Singh
 */
package serverplusTM.ResImpl;

import org.jgroups.Message;
import serverplusTM.ResInterface.CarInterface;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class CarResourceManager extends TransGCResourceManager implements CarInterface
{
    public CarResourceManager(String groupName, String xmlpath) {
        super(groupName, xmlpath);
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
    public synchronized boolean addCars(int id, int opID, String location, int count, int price)
            throws RemoteException
    {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Car curObj = (Car) readData( id, Car.getKey(location) );
        if ( curObj == null ) {
            // car location doesn't exist...add it
            Car newObj = new Car( location, count, price );
            writeData( id, opID, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") created new location " + location +
                    ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing car location and update price...
            Car newObj = curObj.clone();
            newObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                newObj.setPrice( price );
            } // if
            writeData( id, opID, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") modified existing location " +
                    location + ", count=" + newObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }

    @Override
    public synchronized boolean deleteCars(int id, int opID,
                                           String location) throws RemoteException {
        return deleteItem(id, opID, Car.getKey(location));
    }

    @Override
    public int queryCars(int id, int opID, String location) throws RemoteException
    {
        return queryNum(id, opID, Car.getKey(location));
    }

    @Override
    public int queryCarsPrice(int id, int opID, String location) throws RemoteException
    {
        return queryPrice(id, opID, Car.getKey(location));
    }

    @Override
    public boolean reserveCar(int id, int opID, String key)
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
            Car c = new Car(objarr[0],
                    Integer.parseInt(objarr[1]),
                    Integer.parseInt(objarr[2]));
            c.setReserved(Integer.parseInt(objarr[3]));
            saveData(c.getKey(), c);
        }
    }

    @Override
    public Object handle(Message msg) throws Exception {
        super.handle(msg);
        String msgString = (String) msg.getObject();
        String [] arr = msgString.split(";");
        String type = arr[0];
        if (type.equals("write")) {
            String writeString = arr[1];
            String [] parameters = arr[1].split(",");
            Car f = new Car(parameters[2],
                    Integer.parseInt(parameters[3]),
                    Integer.parseInt(parameters[4]));
            f.setReserved(Integer.parseInt(parameters[5]));
            realizeWriteMessage(Integer.parseInt(parameters[0]), Integer.parseInt(parameters[1]),
                    f.getKey(), f);
        }
        return "ACK";
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
        } else if (args.length != 0 &&  args.length != 3) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.CarResourceManager [port] groupName xmlPath");
            System.exit(1);
        }

        try {
            // create a new Server object
            CarResourceManager obj = new CarResourceManager(args[1], args[2]);
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