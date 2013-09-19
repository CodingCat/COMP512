package server.ResInterface;

import nio.NIOReactor;

import java.util.Vector;

public abstract class NIOResourceManager extends NIOReactor {

    public NIOResourceManager() {}

    public NIOResourceManager(String hostIP, int port)  {
        super(hostIP, port);
    }

    /* Add seats to a flight.  In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return success.
     */
     public abstract boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice);

    /* Add cars to a location.
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public abstract boolean addCars(int id, String location, int numCars, int price);

    /* Add rooms to a location.
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public abstract boolean addRooms(int id, String location, int numRooms, int price);


    /* new customer just returns a unique customer identifier */
    public abstract int newCustomer(int id);

    /* new customer with providing id */
    public abstract boolean newCustomer(int id, int cid);

    /**
     *   Delete the entire flight.
     *   deleteflight implies whole deletion of the flight.
     *   all seats, all reservations.  If there is a reservation on the flight,
     *   then the flight cannot be deleted
     *
     * @return success.
     */
    public abstract boolean deleteFlight(int id, int flightNum);

    /* Delete all Cars from a location.
     * It may not succeed if there are reservations for this location
     *
     * @return success
     */
    public abstract boolean deleteCars(int id, String location);

    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public abstract boolean deleteRooms(int id, String location);

    /* deleteCustomer removes the customer and associated reservations */
    public abstract boolean deleteCustomer(int id,int customer);

    /* queryFlight returns the number of empty seats. */
    public abstract int queryFlight(int id, int flightNumber);

    /* return the number of cars available at a location */
    public abstract int queryCars(int id, String location);

    /* return the number of rooms available at a location */
    public abstract int queryRooms(int id, String location);

    /* return a bill */
    public abstract String queryCustomerInfo(int id,int customer);

    /* queryFlightPrice returns the price of a seat on this flight. */
    public abstract int queryFlightPrice(int id, int flightNumber);

    /* return the price of a car at a location */
    public abstract int queryCarsPrice(int id, String location);

    /* return the price of a room at a location */
    public abstract int queryRoomsPrice(int id, String location);

    /* Reserve a seat on this flight*/
    public abstract boolean reserveFlight(int id, int customer, int flightNumber);

    /* reserve a car at this location */
    public abstract boolean reserveCar(int id, int customer, String location);

    /* reserve a room certain at this location */
    public abstract boolean reserveRoom(int id, int customer, String locationd);


    /* reserve an itinerary */
    public abstract boolean itinerary(int id, int customer,
                                      Vector flightNumbers,String location, boolean Car, boolean Room);

}
