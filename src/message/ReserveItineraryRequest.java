package message;

import java.util.Vector;

public class ReserveItineraryRequest extends ReservationMessage {

    private int id;
    private int customerid;
    private Vector flightnumbers;
    private String location;
    boolean carflag = false;
    boolean roomflag = false;

    public ReserveItineraryRequest(int mid, int cid, Vector fnumbers, String loc,
                                   boolean car, boolean room) {
        type = MessageType.RESERVE_ITINERARY_REQUEST;
        id = mid;
        customerid = cid;
        flightnumbers = fnumbers;
        location = loc;
        carflag = car;
        roomflag = room;
    }

    public int getID() {
        return id;
    }

    public int getCustomerid() {
        return customerid;
    }

    public Vector getFlightNumbers() {
        return flightnumbers;
    }

    public String getLocation() {
        return location;
    }

    public boolean getCarflag() {
        return carflag;
    }

    public boolean getRoomflag() {
        return roomflag;
    }

}
