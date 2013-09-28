package server.ResImpl;

import message.*;
import nio.Message;
import nio.NIOReactor;
import util.XmlParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

public class NIOResourceManager extends NIOReactor{

    protected RMHashtable cache_store = new RMHashtable();

    public NIOResourceManager(String serverIP, int serverPort, String confPath) {
        super(serverIP, serverPort);

        XmlParser xmlParser = new XmlParser(confPath);
        //setup resource managers
        //support single resource manager for each resource for now
        setClientEndPoint(xmlParser.getPropertyMap());
    }

    // Reads a data item
    private RMItem readData( int id, String key ) {
        synchronized(cache_store){
            return (RMItem) cache_store.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value ) {
        synchronized(cache_store){
            cache_store.put(key, value);
        }
    }

    // Remove the item out of storage
    protected RMItem removeData(int id, String key){
        synchronized(cache_store){
            return (RMItem) cache_store.remove(key);
        }
    }

    // query the price of an item
    protected int queryPrice(int id, String key){
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;
        if( curObj != null ) {
            value = curObj.getPrice();
        } else {
            return -1;
        }
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;
    }

    protected int queryNum(int id, String key) {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;
        if( curObj != null ) {
            value = curObj.getCount();
        } else {
            return -1;
        }
        Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }

    protected boolean deleteItem(int id, String key) {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key );
        // Check if there is such an item in the storage
        if( curObj == null ) {
            Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
            return false;
        } else {
            if(curObj.getReserved()==0){
                removeData(id, curObj.getKey());
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                return true;
            }
            else{
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                return false;
            }
        } // if
    }

    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) {
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" +
                flightPrice + ", " + flightSeats + ") called" );
        Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
        writeData(id, newObj.getKey(), newObj);
        Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                flightSeats + ", price=$" + flightPrice);
        return true;
    }

    public boolean addCars(int id, String location, int numCars, int price) {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars + ", $" + price + ") called" );
        Car curObj = (Car) readData( id, Car.getKey(location) );
        // car location doesn't exist...add it
        Car newObj = new Car(location, numCars, price);
        writeData(id, newObj.getKey(), newObj);
        Trace.info("RM::addCars(" + id + ") created new location " +
                location + ", count=" + numCars + ", price=$" + price);
        return true;
    }

    public boolean addRooms(int id, String location, int numRooms, int price) {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        // doesn't exist...add it
        Hotel newObj = new Hotel(location, numRooms, price);
        writeData(id, newObj.getKey(), newObj);
        Trace.info("RM::addRooms(" + id + ") created new room location " +
                location + ", count=" + numRooms + ", price=$" + price);
        return(true);
    }

    public int newCustomer(int id) {
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

    public boolean newCustomer(int id, int customerID) {
        Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            cust = new Customer(customerID);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
            return false;
        } // else
    }

    /**
     * Delete the entire flight.
     * deleteflight implies whole deletion of the flight.
     * all seats, all reservations.  If there is a reservation on the flight,
     * then the flight cannot be deleted
     *
     * @return success.
     */
    public boolean deleteFlight(int id, int flightNum) {
        return deleteItem(id, Flight.getKey(flightNum));
    }

    public boolean deleteCars(int id, String location) {
        return deleteItem(id, Car.getKey(location));
    }

    public boolean deleteRooms(int id, String location) {
        return deleteItem(id, Hotel.getKey(location));
    }

    public boolean deleteCustomer(int id, int customerID) {
        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
            // Increase the reserved numbers of all reservable items which the customer reserved.
            RMHashtable reservationHT = cust.getReservations();
            for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){
                String reservedkey = (String) (e.nextElement());
                ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " +
                        reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
                ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " +
                        reserveditem.getKey() + "which is reserved" +  item.getReserved() +
                        " times and is still available " + item.getCount() + " times"  );
                item.setReserved(item.getReserved()-reserveditem.getCount());
                item.setCount(item.getCount()+reserveditem.getCount());
            }

            // remove the customer from the storage
            removeData(id, cust.getKey());
            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
            return true;
        } // if
    }

    public int queryFlight(int id, int flightNumber) {
        return queryNum(id, Flight.getKey(flightNumber));
    }

    public int queryCars(int id, String location) {
        return queryNum(id, Car.getKey(location));
    }

    public int queryRooms(int id, String location) {
        return queryNum(id, Hotel.getKey(location));
    }

    public String queryCustomerInfo(int id, int customerID) {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return null;   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
            String s = cust.printBill();
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
            System.out.println( s );
            return s;
        } // if
    }

    public int queryFlightPrice(int id, int flightNumber) {
        return queryPrice(id, Flight.getKey(flightNumber));
    }

    public int queryCarsPrice(int id, String location) {
        return queryPrice(id, Car.getKey(location));
    }

    public int queryRoomsPrice(int id, String location) {
        return queryPrice(id, Hotel.getKey(location));
    }


    @Override
    public void dispatch(Message msg) {
        if (msg instanceof ReservationMessage) {
            ReservationMessage rmsg = (ReservationMessage) msg;
            switch (rmsg.getMessageType()) {
                case ADD_FLIGHT_REQUEST:
                    forward("data", rmsg);
                    break;
                case DELETE_FLIGHT_REQUEST:
                    DelFlightRequest dfreq = (DelFlightRequest) rmsg;
                    deleteFlight(dfreq.getID(), dfreq.getFlightNum());
                    forward("data", rmsg);
                    break;
                case QUERY_FLIGHT_REQUEST:
                case QUERY_FLIGHTPRICE_REQUEST:
                    QueryFlightRequest qfreq = (QueryFlightRequest) rmsg;
                    int seat = queryFlight(qfreq.getID(), qfreq.getFlightNum());
                    int price = queryFlightPrice(qfreq.getID(), qfreq.getFlightNum());
                    if (seat != -1 && price != -1) {
                        QueryFlightResponse qfres = new QueryFlightResponse(qfreq.getID(),
                                qfreq.getFlightNum(), seat, price);
                        qfres.transactionIDs = (ArrayList<Integer>) qfreq.transactionIDs.clone();
                        reply(qfres);
                    } else {
                        //cache miss
                        forward("data", qfreq);
                    }
                    break;
                case ADD_CAR_REQUEST:
                    AddCarRequest acreq = (AddCarRequest) rmsg;
                    addCars(acreq.getID(), acreq.getLocation(),
                            acreq.getCarnum(), acreq.getPrice());
                    forward("data", acreq);
                    break;
                case DELETE_CAR_REQUEST:
                    DelCarRequest dcreq = (DelCarRequest) rmsg;
                    deleteCars(dcreq.getID(), dcreq.getLocation());
                    forward("data", dcreq);
                    break;
                case QUERY_CAR_REQUEST:
                case QUERY_CARPRICE_REQUEST:
                    QueryCarRequest qcreq = (QueryCarRequest) rmsg;
                    int carnumber = queryCars(qcreq.getID(), qcreq.getLocation());
                    int carprice = queryCarsPrice(qcreq.getID(), qcreq.getLocation());
                    if (carnumber != -1 && carprice != -1) {
                        QueryCarResponse qcres = new QueryCarResponse(qcreq.getID(), qcreq.getLocation(),
                                carnumber, carprice);
                        qcres.transactionIDs = (ArrayList<Integer>) qcreq.transactionIDs.clone();
                        reply(qcres);
                    } else {
                        forward("data", rmsg);
                    }
                    break;
                case ADD_ROOM_REQUEST:
                    AddRoomRequest arreq = (AddRoomRequest) rmsg;
                    addRooms(arreq.getID(), arreq.getLocation(), arreq.getRoomnum(), arreq.getPrice());
                    forward("data", rmsg);
                    break;
                case DELETE_ROOM_REQUEST:
                    DelRoomRequest drreq = (DelRoomRequest) rmsg;
                    deleteRooms(drreq.getID(), drreq.getLocation());
                    forward("data", rmsg);
                    break;
                case QUERY_ROOM_REQUEST:
                case QUERY_ROOMPRICE_REQUEST:
                    QueryRoomRequest qrreq = (QueryRoomRequest) rmsg;
                    int roomnum = queryRooms(qrreq.getID(), qrreq.getLocation());
                    int roomprice = queryRoomsPrice(qrreq.getID(), qrreq.getLocation());
                    if (roomnum != -1 && roomprice != -1) {
                        QueryRoomResponse qrres = new QueryRoomResponse(qrreq.getID(), qrreq.getLocation(),
                                roomnum, roomprice);
                        qrres.transactionIDs = (ArrayList<Integer>) qrreq.transactionIDs.clone();
                        reply(qrres);
                    } else {
                        forward("data", rmsg);
                    }
                    break;
                case ADD_CUSTOMER_REQUEST:
                case ADD_CUSTOMER_ID_REQUEST:
                case DELETE_CUSTOMER_REQUEST:
                    forward("data", rmsg);
                    break;
                case QUERY_CUSTOMER_REQUEST:
                    QueryCustomerRequest qcureq = (QueryCustomerRequest) rmsg;
                    String bill = queryCustomerInfo(qcureq.getID(), qcureq.getCustomerid());
                    if (bill != null) {
                        QueryCustomerResponse qcures = new QueryCustomerResponse(qcureq.getID(),
                                qcureq.getCustomerid(), bill);
                        qcures.transactionIDs = (ArrayList<Integer>) qcureq.transactionIDs.clone();
                        reply(qcures);
                    } else {
                        forward("data", rmsg);
                    }
                    break;
                case ADD_CAR_RESPONSE:
                case ADD_FLIGHT_RESPONSE:
                case ADD_ROOM_RESPONSE:
                case QUERY_FLIGHT_RESPONSE:
                case QUERY_FLIGHTPRICE_RESPONSE:
                case QUERY_CAR_RESPONSE:
                case QUERY_CARPRICE_RESPONSE:
                case QUERY_ROOM_RESPONSE:
                case QUERY_ROOMPRICE_RESPONSE:
                case RESERVE_FLIGHT_RESPONSE:
                case RESERVE_CAR_RESPONSE:
                case RESERVE_ROOM_RESPONSE:
                case RESERVE_ITINERARY_RESPONSE:
                    //write cache
                    addCacheEntry(rmsg);
                    //return back to middleware
                    reply(rmsg);
                    break;
                case DELETE_CUSTOMER_RESPONSE:
                    DelCustomerResponse dcr = (DelCustomerResponse) rmsg;
                    for (int i = 0; i < dcr.getFlightnumbers().size(); i++) {
                        addFlight(0, dcr.getFlightnumbers().get(i), dcr.getSeatnumber().get(i),
                                dcr.getFlightprice().get(i));
                    }
                    for (int i = 0; i < dcr.getCarlocation().size(); i++) {
                        addCars(0, dcr.getCarlocation().get(i), dcr.getCarnum().get(i),
                                dcr.getCarprice().get(i));
                    }
                    for (int i = 0; i < dcr.getRoomlocation().size(); i++) {
                        addRooms(0, dcr.getRoomlocation().get(i), dcr.getRoomnum().get(i),
                                dcr.getRoomprice().get(i));
                    }
                    break;
                case QUERY_CUSTOMER_RESPONSE:
                    //do not cache bill
                    reply(rmsg);
                    break;
                case RESERVE_FLIGHT_REQUEST:
                case RESERVE_CAR_REQUEST:
                case RESERVE_ROOM_REQUEST:
                case RESERVE_ITINERARY_REQUEST:
                    forward("data", rmsg);
                    break;
                default:
                    System.out.println("unrecognizable message");
            }
        }
    }

    private void addCacheEntry(ReservationMessage rmsg) {
        try {
            switch (rmsg.getMessageType()) {
                case ADD_FLIGHT_RESPONSE:
                    AddFlightResponse afr = (AddFlightResponse) rmsg;
                    if (afr.isSuccess())
                        addFlight(afr.getID(), afr.getFlightnum(), afr.getSeatnum(), afr.getPrice());
                    break;
                case QUERY_FLIGHT_RESPONSE:
                case QUERY_FLIGHTPRICE_RESPONSE:
                    QueryFlightResponse qfq = (QueryFlightResponse) rmsg;
                    addFlight(qfq.getID(), qfq.getFlightnumber(), qfq.getSeat(), qfq.getPrice());
                    break;
                case ADD_CAR_RESPONSE:
                    AddCarResponse acr = (AddCarResponse) rmsg;
                    if (acr.isSuccess())
                        addCars(acr.getID(), acr.getLocation(), acr.getCarnum(), acr.getCarprice());
                    break;
                case QUERY_CAR_RESPONSE:
                case QUERY_CARPRICE_RESPONSE:
                    QueryCarResponse qcr = (QueryCarResponse) rmsg;
                    addCars(qcr.getID(), qcr.getLocation(), qcr.getCarNum(), qcr.getPrice());
                    break;
                case ADD_ROOM_RESPONSE:
                    AddRoomResponse arr = (AddRoomResponse) rmsg;
                    if (arr.isSuccess())
                        addRooms(arr.getID(), arr.getLocation(), arr.getRoomnum(), arr.getRoomprice());
                    break;
                case QUERY_ROOM_RESPONSE:
                case QUERY_ROOMPRICE_RESPONSE:
                    QueryRoomResponse qrr = (QueryRoomResponse) rmsg;
                    addRooms(qrr.getID(), qrr.getLocation(), qrr.getRoomnum(), qrr.getRoomprice());
                    break;
                case RESERVE_FLIGHT_RESPONSE:
                    ReserveFlightResponse rfr = (ReserveFlightResponse) rmsg;
                    if (rfr.isSuccess())
                        addFlight(rfr.getID(), rfr.getFlightnumber(), rfr.getSeat(), rfr.getPrice());
                    break;
                case RESERVE_CAR_RESPONSE:
                    ReserveCarResponse rcr = (ReserveCarResponse) rmsg;
                    if (rcr.isSuccess())
                        addCars(rcr.getID(), rcr.getLocation(), rcr.getCarNum(), rcr.getPrice());
                    break;
                case RESERVE_ROOM_RESPONSE:
                    ReserveRoomResponse rrr = (ReserveRoomResponse) rmsg;
                    if (rrr.isSuccess())
                        addRooms(rrr.getID(), rrr.getLocation(), rrr.getRoomnum(), rrr.getRoomprice());
                    break;
                case RESERVE_ITINERARY_RESPONSE:
                    ReserveItineraryResponse rires = (ReserveItineraryResponse) rmsg;
                    if (rires.isSuccess()) {
                        //flights
                        for (int i = 0; i < rires.getFlightnumbers().size(); i++) {
                            addFlight(rires.getId(),
                                    Integer.parseInt((String) rires.getFlightnumbers().get(i)),
                                    (Integer) rires.getSeatnumber().get(i),
                                    (Integer) rires.getFlightprice().get(i));
                        }
                        //cars
                        if (rires.isCarflag())
                            addCars(rires.getId(), rires.getLocation(), rires.getCarnum(), rires.getCarprice());
                        //room
                        if (rires.isRoomflag())
                            addRooms(rires.getId(), rires.getLocation(), rires.getRoomnum(), rires.getRoomprice());
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        NIOResourceManager rm = new NIOResourceManager(args[0], Integer.parseInt(args[1]), args[2]);
        Thread rm_server_t = new Thread(rm);
        rm_server_t.start();
    }
}
