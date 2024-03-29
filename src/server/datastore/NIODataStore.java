package server.datastore;

import message.*;
import nio.Message;
import nio.NIOReactor;
import server.ResImpl.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

public class NIODataStore extends NIOReactor {

    private Object flightreservelock = new Object();
    private Object flightaddlock = new Object();
    private Object carreservelock = new Object();
    private Object caraddlock = new Object();
    private Object roomreservelock = new Object();
    private Object roomaddlock = new Object();
    private Object customeraddlock = new Object();

    protected RMHashtable m_itemHT = new RMHashtable();

    public NIODataStore(String listenIp, int listenport) {
        super(listenIp, listenport);
    }

    // Reads a data item
    private RMItem readData( int id, String key ) {
        synchronized(m_itemHT){
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value ) {
        synchronized(m_itemHT){
            m_itemHT.put(key, value);
        }
    }

    // Remove the item out of storage
    protected RMItem removeData(int id, String key){
        synchronized(m_itemHT){
            return (RMItem)m_itemHT.remove(key);
        }
    }

    // reserve an item
    protected boolean reserveItem(int id, int customerID, String key, String location){
        Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " + key + ", " + location + " ) called");
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location +
                    ")  failed--customer doesn't exist" );
            return false;
        }

        // check if the item is available
        ReservableItem item = (ReservableItem)readData(id, key);
        if(item==null){
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location +
                    ") failed--item doesn't exist" );
            return false;
        }else if(item.getCount()==0){
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location +
                    ") failed--No more items" );
            return false;
        }else{
            cust.reserve( key, location, item.getPrice());
            writeData( id, cust.getKey(), cust );

            // decrease the number of available items in the storage
            item.setCount(item.getCount() - 1);
            item.setReserved(item.getReserved()+1);

            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        }
    }

    // query the price of an item
    protected int queryPrice(int id, String key){
        Trace.info("RM::queryPrice(" + id + ", " + key + ") called" );
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
        } else return -1;
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
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be " +
                        "deleted because some customers reserved it" );
                return false;
            }
        } // if
    }

    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) {
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" +
                flightPrice + ", " + flightSeats + ") called" );
        Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
        if( curObj == null ) {
            // doesn't exist...add it
            Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                    flightSeats + ", price=$" + flightPrice );
        } else {
            // add seats to existing flight and update the price...
            curObj.setCount( curObj.getCount() + flightSeats );
            if( flightPrice > 0 ) {
                curObj.setPrice( flightPrice );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum +
                    ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
        } // else
        return true;
    }

    public boolean addCars(int id, String location, int numCars, int price) {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + numCars + ", $" + price + ") called" );
        Car curObj = (Car) readData( id, Car.getKey(location) );
        if( curObj == null ) {
            // car location doesn't exist...add it
            Car newObj = new Car( location, numCars, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") created new location " +
                    location + ", count=" + numCars + ", price=$" + price );
        } else {
            // add count to existing car location and update price...
            curObj.setCount( curObj.getCount() + numCars );
            if( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addCars(" + id + ") modified existing location " +
                    location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return true;
    }

    public boolean addRooms(int id, String location, int numRooms, int price) {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + numRooms + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, numRooms, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " +
                    location + ", count=" + numRooms + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + numRooms );
            if( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addRooms(" + id + ") modified existing location " +
                    location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
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
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
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

    public boolean reserveFlight(int id, int customerID, int flightNumber) {
        return reserveItem(id, customerID, Flight.getKey(flightNumber),
                String.valueOf(flightNumber));
    }

    public boolean reserveCar(int id, int customerID, String location) {
        return reserveItem(id, customerID, Car.getKey(location), location);
    }

    public boolean reserveRoom(int id, int customerID, String location) {
        return reserveItem(id, customerID, Hotel.getKey(location), location);
    }

    public boolean itinerary(int id, int customer, Vector flightNumbers,
                             String location, boolean Car, boolean Room) {
        for (Object obj : flightNumbers) {
            reserveFlight(id, customer, (Integer) obj);
        }
        if (Car) reserveCar(id, customer, location);
        if (Room) reserveRoom(id, customer, location);
        return false;
    }

    private void reserveFlight(ReserveFlightRequest rfr) {
        ReserveFlightResponse rfres = null;
        synchronized (flightreservelock) {
            boolean successReservedFlight =
                    reserveFlight(rfr.getID(), rfr.getCustomerid(), rfr.getFlightnumber());
            int seat = queryFlight(rfr.getID(), rfr.getFlightnumber());
            int price = queryFlightPrice(rfr.getID(), rfr.getFlightnumber());
            rfres = new ReserveFlightResponse(rfr.getID(),
                    rfr.getFlightnumber(), seat, price, successReservedFlight);
        }
        rfres.transactionIDs = (ArrayList<Integer>) rfr.transactionIDs.clone();
        reply(rfres);
    }

    private void reserveCar(ReserveCarRequest rcr) {
        ReserveCarResponse rcres = null;
        synchronized (carreservelock) {
            boolean success = reserveCar(rcr.getID(), rcr.getCustomerid(),
                    rcr.getLocation());
            int carnum = queryCars(rcr.getID(), rcr.getLocation());
            int carprice = queryCarsPrice(rcr.getID(), rcr.getLocation());
            rcres = new ReserveCarResponse(rcr.getID(),
                    rcr.getLocation(), carnum, carprice, success);
        }
        rcres.transactionIDs = (ArrayList<Integer>) rcr.transactionIDs.clone();
        reply(rcres);
    }

    private void reserveRoom(ReserveRoomRequest rrr) {
        ReserveRoomResponse rrres = null;
        synchronized (roomreservelock) {
            boolean success = reserveRoom(rrr.getID(), rrr.getCustomerid(), rrr.getLocation());
            int roomnum = queryRooms(rrr.getID(), rrr.getLocation());
            int roomprice = queryRoomsPrice(rrr.getID(), rrr.getLocation());
            rrres = new ReserveRoomResponse(rrr.getID(),
                    rrr.getLocation(), roomnum, roomprice, success);
        }
        rrres.transactionIDs = (ArrayList<Integer>) rrr.transactionIDs.clone();
        reply(rrres);
    }

    private void addCar(AddCarRequest acreq) {
        AddCarResponse acr = null;
        synchronized (caraddlock) {
            boolean success = addCars(acreq.getID(), acreq.getLocation(),
                    acreq.getCarnum(), acreq.getPrice());
            int carnum = queryCars(acreq.getID(), acreq.getLocation());
            int carprice = queryCarsPrice(acreq.getID(), acreq.getLocation());
            acr = new AddCarResponse(acreq.getID(),
                    acreq.getLocation(), carnum, carprice, success);
        }
        acr.transactionIDs = (ArrayList<Integer>) acreq.transactionIDs.clone();
        reply(acr);
    }

    private void addRoom (AddRoomRequest arreq) {
        AddRoomResponse arr = null;
        synchronized (roomaddlock) {
            boolean success = addRooms(arreq.getID(), arreq.getLocation(),
                    arreq.getRoomnum(), arreq.getPrice());
            int roomnum = queryRooms(arreq.getID(), arreq.getLocation());
            int roomprice = queryRoomsPrice(arreq.getID(), arreq.getLocation());
            arr = new AddRoomResponse(arreq.getID(),
                    arreq.getLocation(), roomnum, roomprice, success);
        }
        arr.transactionIDs = (ArrayList<Integer>) arreq.transactionIDs.clone();
        reply(arr);
    }

    private void addFlight (AddFlightRequest afreq) {
        AddFlightResponse afr = null;
        synchronized (flightaddlock) {
            boolean success = addFlight(afreq.getID(), afreq.getFlightNum(),
                    afreq.getFlightSeat(), afreq.getFlightPrice());
            int seat = queryFlight(afreq.getID(), afreq.getFlightNum());
            int price = queryFlightPrice(afreq.getID(), afreq.getFlightNum());
            afr = new AddFlightResponse(afreq.getID(),
                    afreq.getFlightNum(), seat, price, success);
        }
        afr.transactionIDs = (ArrayList<Integer>) afreq.transactionIDs.clone();
        reply(afr);
    }

    private void queryflight (QueryFlightRequest qfreq) {
        int seat = queryFlight(qfreq.getID(), qfreq.getFlightNum());
        int price = queryFlightPrice(qfreq.getID(), qfreq.getFlightNum());
        QueryFlightResponse qfres = new QueryFlightResponse(qfreq.getID(), qfreq.getFlightNum(), seat, price);
        qfres.transactionIDs = (ArrayList<Integer>) qfreq.transactionIDs.clone();
        reply(qfres);
    }

    private void querycar (QueryCarRequest qcreq) {
        int carnum = queryCars(qcreq.getID(), qcreq.getLocation());
        int carprice = queryCarsPrice(qcreq.getID(), qcreq.getLocation());
        QueryCarResponse qcres = new QueryCarResponse(qcreq.getID(), qcreq.getLocation(),
                carnum, carprice);
        qcres.transactionIDs = (ArrayList<Integer>) qcreq.transactionIDs.clone();
        reply(qcres);
    }

    private void queryroom (QueryRoomRequest qrreq) {
        int roomnum = queryRooms(qrreq.getID(), qrreq.getLocation());
        int roomprice = queryRoomsPrice(qrreq.getID(), qrreq.getLocation());
        QueryRoomResponse qrres = new QueryRoomResponse(qrreq.getID(), qrreq.getLocation(),
                roomnum, roomprice);
        qrres.transactionIDs = (ArrayList<Integer>) qrreq.transactionIDs.clone();
        reply(qrres);
    }

    private void querycustomer (QueryCustomerRequest qcureq) {
        QueryCustomerResponse qcures = new QueryCustomerResponse(
                qcureq.getID(),
                qcureq.getCustomerid(), queryCustomerInfo(qcureq.getID(),
                qcureq.getCustomerid()));
        qcures.transactionIDs = (ArrayList<Integer>) qcureq.transactionIDs.clone();
        reply(qcures);
    }

    private void itinerary (ReserveItineraryRequest  rir) {
        boolean transactionSuccess = true;
        for (int i = 0; i < rir.getFlightNumbers().size(); i++) {
            transactionSuccess = transactionSuccess &&
                    reserveFlight(rir.getID(), rir.getCustomerid(),
                            Integer.parseInt((String) rir.getFlightNumbers().get(i)));
        }
        if (!transactionSuccess) return;
        if (rir.getCarflag()) {
            transactionSuccess = transactionSuccess &&
                    reserveCar(rir.getID(), rir.getCustomerid(), rir.getLocation());
            if (!transactionSuccess) {
                //recover flight, and break;
                for (int i = 0; i < rir.getFlightNumbers().size(); i++) {
                    int p = queryFlightPrice(rir.getID(),
                            Integer.parseInt((String) rir.getFlightNumbers().get(i)));
                    addFlight(rir.getID(),
                            (Integer) rir.getFlightNumbers().get(i),
                            1, p);
                }
                return;
            }
        }
        if (rir.getRoomflag()) {
            transactionSuccess = transactionSuccess &&
                    reserveRoom(rir.getID(), rir.getCustomerid(), rir.getLocation());
            if (!transactionSuccess) {
                //recover flight, and break;
                for (int i = 0; i < rir.getFlightNumbers().size(); i++) {
                    int p = queryFlightPrice(rir.getID(),
                            (Integer) rir.getFlightNumbers().get(i));
                    addFlight(rir.getID(),
                            (Integer) rir.getFlightNumbers().get(i),
                            1, p);
                }
                //recover car, and break;
                int p = queryCarsPrice(rir.getID(), rir.getLocation());
                addCars(rir.getID(), rir.getLocation(), 1, p);
                return;
            }
        }

        Vector seats = new Vector();
        Vector prices = new Vector();
        for (int i = 0; i < rir.getFlightNumbers().size(); i++) {
            seats.add(queryFlight(rir.getID(),
                    Integer.parseInt((String) rir.getFlightNumbers().get(i))));
            prices.add(queryFlightPrice(rir.getID(),
                    Integer.parseInt((String)  rir.getFlightNumbers().get(i))));
        }
        int carnum = -1;
        int carprice = -1;
        int roomnum = -1;
        int roomprice = -1;
        if (rir.getCarflag()) {
            carnum = queryCars(rir.getID(), rir.getLocation());
            carprice = queryCarsPrice(rir.getID(), rir.getLocation());
        }
        if (rir.getRoomflag()) {
            roomnum = queryRooms(rir.getID(), rir.getLocation());
            roomprice = queryRoomsPrice(rir.getID(), rir.getLocation());
        }
        ReserveItineraryResponse rires = new ReserveItineraryResponse(rir.getID(),
                rir.getFlightNumbers(), seats, prices, rir.getLocation(), carnum, carprice,
                roomnum, roomprice, rir.getCarflag(), rir.getRoomflag(), transactionSuccess);
        rires.transactionIDs = (ArrayList<Integer>) rir.transactionIDs.clone();
        replyAll(rires);
    }

    private void deleteFlights(DelFlightRequest dfreq) {
        boolean status = deleteFlight(dfreq.getID(), dfreq.getFlightNum());
        DelFlightResponse dfres = new DelFlightResponse(dfreq.getID(), dfreq.getFlightNum(), status);
        dfres.transactionIDs = (ArrayList<Integer>) dfreq.transactionIDs.clone();
        reply(dfres);
    }

    private void deleteCars(DelCarRequest dcreq) {
        boolean status = deleteCars(dcreq.getID(), dcreq.getLocation());
        DelCarResponse dfres = new DelCarResponse(dcreq.getID(), dcreq.getLocation(), status);
        dfres.transactionIDs = (ArrayList<Integer>) dcreq.transactionIDs.clone();
        reply(dfres);
    }

    private void deleteRooms(DelRoomRequest drreq) {
        boolean status = deleteRooms(drreq.getID(), drreq.getLocation());
        DelRoomResponse drres = new DelRoomResponse(drreq.getID(), drreq.getLocation(), status);
        drres.transactionIDs = (ArrayList<Integer>) drreq.transactionIDs.clone();
        reply(drres);
    }

    @Override
    public void dispatch(Message msg) {
        if (msg instanceof ReservationMessage) {
            ReservationMessage rmsg = (ReservationMessage) msg;
            switch (rmsg.getMessageType()) {
                case ADD_FLIGHT_REQUEST:
                    addFlight((AddFlightRequest) rmsg);
                    break;
                case DELETE_FLIGHT_REQUEST:
                    deleteFlights((DelFlightRequest) rmsg);
                    break;
                case QUERY_FLIGHT_REQUEST:
                case QUERY_FLIGHTPRICE_REQUEST:
                    queryflight((QueryFlightRequest) rmsg);
                    break;
                case RESERVE_FLIGHT_REQUEST:
                    reserveFlight((ReserveFlightRequest) rmsg);
                    break;
                case ADD_CAR_REQUEST:
                    addCar((AddCarRequest) rmsg);
                    break;
                case DELETE_CAR_REQUEST:
                    deleteCars((DelCarRequest) rmsg);
                    break;
                case QUERY_CAR_REQUEST:
                case QUERY_CARPRICE_REQUEST:
                    querycar((QueryCarRequest) rmsg);
                    break;
                case RESERVE_CAR_REQUEST:
                    reserveCar((ReserveCarRequest) rmsg);
                    break;
                case ADD_ROOM_REQUEST:
                    addRoom((AddRoomRequest) rmsg);
                    break;
                case DELETE_ROOM_REQUEST:
                    deleteRooms((DelRoomRequest) rmsg);
                    break;
                case QUERY_ROOM_REQUEST:
                case QUERY_ROOMPRICE_REQUEST:
                    queryroom((QueryRoomRequest) rmsg);
                    break;
                case RESERVE_ROOM_REQUEST:
                    reserveRoom((ReserveRoomRequest) rmsg);
                    break;
                case ADD_CUSTOMER_REQUEST:
                    AddCustomerRequest acureq = (AddCustomerRequest) rmsg;
                    newCustomer(acureq.getID());
                    break;
                case ADD_CUSTOMER_ID_REQUEST:
                    AddCustomerWithIDRequest acuidreq = (AddCustomerWithIDRequest) rmsg;
                    newCustomer(acuidreq.getID(), acuidreq.getCustomerid());
                    break;
                case DELETE_CUSTOMER_REQUEST:
                    DelCustomerRequest dcureq = (DelCustomerRequest) rmsg;
                    Customer cust = (Customer) readData(dcureq.getID(), Customer.getKey(dcureq.getCustomerid()));
                    if (cust != null) {
                        deleteCustomer(dcureq.getID(), dcureq.getCustomerid());
                        replyAll(getDelCustomerResponse(cust));
                    }
                    break;
                case QUERY_CUSTOMER_REQUEST:
                    querycustomer((QueryCustomerRequest) rmsg);
                    break;
                case RESERVE_ITINERARY_REQUEST:
                    itinerary((ReserveItineraryRequest) rmsg);
                    break;
                default:
                    System.out.println("unrecognizable message");
            }
        }
    }

    private DelCustomerResponse getDelCustomerResponse(Customer customer) {
        Vector<String> reservedflights = customer.getReservedCertainItems("flight-");
        Vector<Integer> reservedflightnum = new Vector<Integer>();
        Vector<Integer> reservedflightseats = new Vector<Integer>();
        Vector<Integer> reservedflightprice = new Vector<Integer>();
        Vector<String>  reservedcarlocations = customer.getReservedCertainItems("car-");
        Vector<Integer> reservedcarnumbers = new Vector<Integer>();
        Vector<Integer> reservedcarprice = new Vector<Integer>();
        Vector<String>  reservedroomlocations = customer.getReservedCertainItems("room-");
        Vector<Integer> reservedroomnumbers = new Vector<Integer>();
        Vector<Integer> reservedroomprice = new Vector<Integer>();
        //flights
        for (String flightnum : reservedflights) {
            reservedflightnum.add(Integer.parseInt(flightnum));
            reservedflightseats.add(queryFlight(0, Integer.parseInt(flightnum)));
            reservedflightprice.add(queryFlightPrice(0, Integer.parseInt(flightnum)));
        }
        //car
        for (String location : reservedcarlocations) {
            reservedcarnumbers.add(queryCars(0, location));
            reservedcarprice.add(queryCarsPrice(0, location));
        }
        //room
        for (String room : reservedroomlocations) {
            reservedroomnumbers.add(queryRooms(0, room));
            reservedroomprice.add(queryRoomsPrice(0, room));
        }
        return new DelCustomerResponse(reservedflightnum, reservedflightseats, reservedflightprice,
                reservedcarlocations, reservedcarnumbers, reservedcarprice,
                reservedroomlocations, reservedroomnumbers, reservedroomprice);
    }

    public static void main(String [] args) {
        NIODataStore ds = new NIODataStore(args[0], Integer.parseInt(args[1]));
        Thread ds_t = new Thread(ds);
        ds_t.start();
    }
}
