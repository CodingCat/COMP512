package client;


import serverplusTM.ResInterface.ResourceManager;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

public class RepClient extends Client {

    class ServerDescriptor {
        String serveraddr;
        int serverport;
        String servername;
        public ServerDescriptor(String sa, int sp, String sn) {
            serveraddr = sa;
            serverport = sp;
            servername = sn;
        }
        Registry res;
        boolean livemark = true;
        ResourceManager rmobj;

        @Override
        public String toString() {
            return serveraddr + ":" + serverport + ":" + servername;
        }
    }

    private class RMLivenessChecker implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    for (int i = 0; i < serverList.size(); i++) {
                        if (!serverList.get(i).livemark) {
                            serverList.get(i).res = LocateRegistry.getRegistry(serverList.get(i).serveraddr,
                                    serverList.get(i).serverport);
                            serverList.get(i).rmobj = (ResourceManager)
                                    serverList.get(i).res.lookup(serverList.get(i).servername);
                            serverList.get(i).rmobj.isPrimary();
                            serverList.get(i).livemark = true;
                        }
                    }
                    Thread.sleep(1000);
                } catch (RemoteException re) {
                    re.printStackTrace();
                }  catch (InterruptedException ie) {
                    ie.printStackTrace();
                } catch (NotBoundException nbe) {
                    nbe.printStackTrace();
                }
            }
        }
    }

    private ArrayList<ServerDescriptor> serverList = new ArrayList<ServerDescriptor>();
    private int primaryIdx = 0;
    private ResourceManager primaryRM = null;
    private int opid = 0;

    private boolean getServerList(String path) {
        String [] linearr = null;
        ServerDescriptor sd = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String s;
            while (!(s = in.readLine()).equals("end")) {
                if (s.charAt(0) == '#') continue;
                linearr = s.split(" ");
                sd = new ServerDescriptor(linearr[0], Integer.parseInt(linearr[1]),
                        linearr[2]);
                serverList.add(sd);
                int tailpos = serverList.size() - 1;
                try {
                    serverList.get(tailpos).res = LocateRegistry.getRegistry(
                            linearr[0], Integer.parseInt(linearr[1]));
                    serverList.get(tailpos).rmobj = (ResourceManager) serverList.get(
                            tailpos).res.lookup(linearr[2]);
                    serverList.get(tailpos).rmobj.isPrimary();
                } catch (RemoteException e) {
                    System.out.println("failed to connect to remote object, " + sd.toString() +
                            ", adding it as dead server");
                    sd.livemark = false;
                } catch (NotBoundException nbe) {
                    System.out.println("failed to connect to remote object");
                    sd.livemark = false;
                }
            }
            in.close();
            primaryRM = serverList.get(0).rmobj;
            return true;
        } catch (FileNotFoundException filee) {
            System.out.println("failed to find " + path);
        } catch (IOException ioe) {
            System.out.println("failed to read/write on " + path);
        }
        return false;
    }

    private void electPrimary() {
        for (int i = 0; i < serverList.size(); i++) {
            try {
                if (serverList.get(i).rmobj != null &&
                        serverList.get(i).rmobj.isPrimary()) {
                    primaryIdx = i;
                    primaryRM = serverList.get(i).rmobj;
                    break;
                }
            } catch (RemoteException e) {
                serverList.get(i).livemark = false;
                serverList.get(i).res = null;
                serverList.get(i).rmobj = null;
                System.out.println(serverList.get(i) + " is dead");
            }
        }
    }

    private int getOPID() {
        return opid++;

    }
    private int getOldOpID()
    {
        return opid;
    }
    private void setOpID(int opID)
    {
        opid=opID;
    }

    private void processRequest(Vector<String> arguments) throws Exception{
        int Id, Cid;
        int flightNum;
        int flightPrice;
        int flightSeats;
        boolean Room;
        boolean Car;
        int price;
        int numRooms;
        int numCars;
        String location;

        switch (findChoice(arguments.get(0))) {
            case 1: //help section
                if (arguments.size() == 1)   //command was "help"
                    listCommands();
                else if (arguments.size() == 2)  //command was "help <commandname>"
                    listSpecific(arguments.elementAt(1));
                else  //wrong use of help command
                    System.out.println("Improper use of help command. Type help or help, <commandname>");
                break;
            case 2:  //new flight
                if (arguments.size() != 5) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Flight using id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));
                System.out.println("Add Flight Seats: " + arguments.elementAt(3));
                System.out.println("Set Flight Price: " + arguments.elementAt(4));

                Id = getInt(arguments.elementAt(1));
                flightNum = getInt(arguments.elementAt(2));
                flightSeats = getInt(arguments.elementAt(3));
                flightPrice = getInt(arguments.elementAt(4));
                if (primaryRM.addFlight(Id, getOPID(), flightNum, flightSeats, flightPrice))
                    System.out.println("Flight added");
                else
                    System.out.println("Flight could not be added");
                break;
            case 3:  //new Car
                if (arguments.size() != 5) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Car using id: " + arguments.elementAt(1));
                System.out.println("Car Location: " + arguments.elementAt(2));
                System.out.println("Add Number of Cars: " + arguments.elementAt(3));
                System.out.println("Set Price: " + arguments.elementAt(4));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                numCars = getInt(arguments.elementAt(3));
                price = getInt(arguments.elementAt(4));
                if (primaryRM.addCars(Id, getOPID(), location, numCars, price))
                    System.out.println("Cars added");
                else
                    System.out.println("Cars could not be added");
                break;

            case 4:  //new Room
                if (arguments.size() != 5) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Room using id: " + arguments.elementAt(1));
                System.out.println("Room Location: " + arguments.elementAt(2));
                System.out.println("Add Number of Rooms: " + arguments.elementAt(3));
                System.out.println("Set Price: " + arguments.elementAt(4));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                numRooms = getInt(arguments.elementAt(3));
                price = getInt(arguments.elementAt(4));
                if (primaryRM.addRooms(Id, getOPID(), location, numRooms, price))
                    System.out.println("Rooms added");
                else
                    System.out.println("Rooms could not be added");
                break;

            case 5:  //new Customer
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Customer using id:" + arguments.elementAt(1));
                Id = getInt(arguments.elementAt(1));
                int customer = primaryRM.newCustomer(Id, getOPID());
                System.out.println("new customer id:" + customer);
                break;

            case 6: //delete Flight
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting a flight using id: " + arguments.elementAt(1));
                System.out.println("Flight Number: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                flightNum = getInt(arguments.elementAt(2));
                if (primaryRM.deleteFlight(Id, getOPID(), flightNum))
                    System.out.println("Flight Deleted");
                else
                    System.out.println("Flight could not be deleted");
                break;
            case 7: //delete Car
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
                System.out.println("Car Location: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));

                if (primaryRM.deleteCars(Id, getOPID(), location))
                    System.out.println("Cars Deleted");
                else
                    System.out.println("Cars could not be deleted");
                break;

            case 8: //delete Room
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
                System.out.println("Room Location: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                if (primaryRM.deleteRooms(Id, getOPID(), location))
                    System.out.println("Rooms Deleted");
                else
                    System.out.println("Rooms could not be deleted");
                break;

            case 9: //delete Customer
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                customer = getInt(arguments.elementAt(2));
                if (primaryRM.deleteCustomer(Id, getOPID(), customer))
                    System.out.println("Customer Deleted");
                else
                    System.out.println("Customer could not be deleted");
                break;

            case 10: //querying a flight
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a flight using id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                flightNum = getInt(arguments.elementAt(2));
                int seats = primaryRM.queryFlight(Id, getOPID(), flightNum);
                System.out.println("Number of seats available:" + seats);
                break;

            case 11: //querying a Car Location
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a car location using id: " + arguments.elementAt(1));
                System.out.println("Car location: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                numCars = primaryRM.queryCars(Id, getOPID(), location);
                System.out.println("number of Cars at this location:" + numCars);
                break;

            case 12: //querying a Room location
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a room location using id: " + arguments.elementAt(1));
                System.out.println("Room location: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                numRooms = primaryRM.queryRooms(Id, getOPID(), location);
                System.out.println("number of Rooms at this location:" + numRooms);
                break;

            case 13: //querying Customer Information
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying Customer information using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                customer = getInt(arguments.elementAt(2));
                String bill = primaryRM.queryCustomerInfo(Id, getOPID(), customer);
                System.out.println("Customer info:" + bill);
                break;

            case 14: //querying a flight Price
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
                System.out.println("Flight number: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                flightNum = getInt(arguments.elementAt(2));
                price = primaryRM.queryFlightPrice(Id, getOPID(), flightNum);
                System.out.println("Price of a seat:" + price);
                break;

            case 15: //querying a Car Price
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a car price using id: " + arguments.elementAt(1));
                System.out.println("Car location: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                price = primaryRM.queryCarsPrice(Id, getOPID(), location);
                System.out.println("Price of a car at this location:" + price);
                break;

            case 16: //querying a Room price
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Querying a room price using id: " + arguments.elementAt(1));
                System.out.println("Room Location: " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                location = getString(arguments.elementAt(2));
                price = primaryRM.queryRoomsPrice(Id, getOPID(), location);
                System.out.println("Price of Rooms at this location:" + price);
                break;

            case 17:  //reserve a flight
                if (arguments.size() != 4) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                System.out.println("Flight number: " + arguments.elementAt(3));
                Id = getInt(arguments.elementAt(1));
                customer = getInt(arguments.elementAt(2));
                flightNum = getInt(arguments.elementAt(3));
                if (primaryRM.reserveFlight(Id, getOPID(), customer, flightNum))
                    System.out.println("Flight Reserved");
                else
                    System.out.println("Flight could not be reserved.");
                break;

            case 18:  //reserve a car
                if (arguments.size() != 4) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                System.out.println("Location: " + arguments.elementAt(3));

                Id = getInt(arguments.elementAt(1));
                customer = getInt(arguments.elementAt(2));
                location = getString(arguments.elementAt(3));

                if (primaryRM.reserveCar(Id, getOPID(), customer, location))
                    System.out.println("Car Reserved");
                else
                    System.out.println("Car could not be reserved.");
                break;

            case 19:  //reserve a room
                if (arguments.size() != 4) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving a room at a location using id: " + arguments.elementAt(1));
                System.out.println("Customer id: " + arguments.elementAt(2));
                System.out.println("Location: " + arguments.elementAt(3));
                Id = getInt(arguments.elementAt(1));
                customer = getInt(arguments.elementAt(2));
                location = getString(arguments.elementAt(3));

                if (primaryRM.reserveRoom(Id, getOPID(), customer, location))
                    System.out.println("Room Reserved");
                else
                    System.out.println("Room could not be reserved.");
                break;

            case 20:  //reserve an Itinerary
                if (arguments.size() < 7) {
                    wrongNumber();
                    break;
                }
                System.out.println("Reserving an Itinerary using id:" + arguments.elementAt(1));
                System.out.println("Customer id:" + arguments.elementAt(2));
                for (int i = 0; i < arguments.size() - 6; i++)
                    System.out.println("Flight number" + arguments.elementAt(3 + i));
                System.out.println("Location for Car/Room booking:" + arguments.elementAt(arguments.size() - 3));
                System.out.println("Car to book?:" + arguments.elementAt(arguments.size() - 2));
                System.out.println("Room to book?:" + arguments.elementAt(arguments.size() - 1));
                Id = getInt(arguments.elementAt(1));
                customer = getInt(arguments.elementAt(2));
                Vector flightNumbers = new Vector();
                for (int i = 0; i < arguments.size() - 6; i++)
                    flightNumbers.addElement(arguments.elementAt(3 + i));
                location = getString(arguments.elementAt(arguments.size() - 3));
                Car = getBoolean(arguments.elementAt(arguments.size() - 2));
                Room = getBoolean(arguments.elementAt(arguments.size() - 1));

                if (primaryRM.itinerary(Id, getOPID(), customer, flightNumbers, location, Car, Room)){
                    //increase for three times in total
                    for (int i = 0; i < flightNumbers.size() - 1; i++) getOPID();
                    if (Car) getOPID();
                    if (Room) getOPID();
                    System.out.println("Itinerary Reserved");
                }
                else
                    System.out.println("Itinerary could not be reserved.");
                break;

            case 21:  //quit the client
                if (arguments.size() != 1) {
                    wrongNumber();
                    break;
                }
                System.out.println("Quitting client.");
                System.exit(1);


            case 22:  //new Customer given id
                if (arguments.size() != 3) {
                    wrongNumber();
                    break;
                }
                System.out.println("Adding a new Customer using id:" + arguments.elementAt(1) + " and cid " + arguments.elementAt(2));
                Id = getInt(arguments.elementAt(1));
                Cid = getInt(arguments.elementAt(2));
                boolean customersuccess = primaryRM.newCustomer(Id, getOPID(), Cid);
                System.out.println("new customer id:" + Cid);
                break;

            case 23:  //start trxn
                if (arguments.size() != 1) {
                    wrongNumber();
                    break;
                }
                System.out.println("Starting new transaction");
                int trxnID = primaryRM.start();
                if (trxnID != -1)
                    System.out.println("new transaction id:" + trxnID);
                else
                    System.out.println("new transaction could not be started.");
                break;

            case 24:  //commit trxn
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Commiting the transaction with id:" + arguments.elementAt(1));
                trxnID = getInt(arguments.elementAt(1));
                boolean bcommited = primaryRM.commit(trxnID);
                if (bcommited)
                    System.out.println("transaction id:" + trxnID + " commited.");
                else
                    System.out.println("transaction could not be commited.");
                break;

            case 25:  //abort trxn
                if (arguments.size() != 2) {
                    wrongNumber();
                    break;
                }
                System.out.println("Aborting the transaction with id:" + arguments.elementAt(1));
                trxnID = getInt(arguments.elementAt(1));
                primaryRM.abort(trxnID);
                System.out.println("transaction id:" + trxnID + " aborted.");
                break;

            case 26:  //shutdown system
                if (arguments.size() != 1) {
                    wrongNumber();
                    break;
                }
                System.out.println("Shutting down the system");
                boolean bshutdown = primaryRM.shutdown();
                if (bshutdown)
                    System.out.println("System shutdown successful");
                else
                    System.out.println("System shutdown Unsuccessful.");
                break;

            default:
                System.out.println("The interface does not support this command.");
                break;
        }//end of switch
    }
    
    private void mainLoop() throws Exception {
        String command = "";
        Vector<String> arguments  = new Vector<String>();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\n\n\tClient Interface");
        System.out.println("Type \"help\" for list of supported commands");


        while(true){
            System.out.print("\n>");
            try{
                //read the next command
                command = stdin.readLine();
            }
            catch (IOException io){
                System.out.println("Unable to read from standard in");
                System.exit(1);
            }
            //remove heading and trailing white space
            command=command.trim();
            arguments= parse(command);
            int opID=-1;
            while (true) {
                try {
                    //decide which of the commands this was
                    opID=getOldOpID();
                    processRequest(arguments);
                    break;
                } catch (RemoteException e) {
                    System.out.println(serverList.get(primaryIdx) + " is dead");
                    System.out.println("electing new coordinator");
                    serverList.get(primaryIdx).rmobj = null;
                    serverList.get(primaryIdx).livemark = false;
                    electPrimary();
                    System.out.println("new coordinator is " + serverList.get(primaryIdx));
                    System.out.println("resending request... ");
                    setOpID(opID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }//end of while(true)
    }

    private void checkRMLiveness() {
        Thread checkerthread = new Thread(new RMLivenessChecker());
        checkerthread.start();
    }

    public void run() {
        checkRMLiveness();
        try {
            mainLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        if (args.length != 1) {
            System.out.println("Usage client server_list_path");
            System.exit(1);
        }
        Properties p = System.getProperties();
        p.put("sun.rmi.transport.tcp.responseTimeout", "5000");
        System.setProperties(p);
        RepClient rc = new RepClient();
        if (!rc.getServerList(args[0])) System.exit(1);
        rc.run();
    }
}
