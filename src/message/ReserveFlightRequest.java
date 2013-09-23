package message;

public class ReserveFlightRequest extends ReservationMessage {

    private int id;
    private int customerid;
    private int flightnumber;

    public ReserveFlightRequest(int mid, int cid, int fnum) {
        type = MessageType.RESERVE_FLIGHT_REQUEST;
        id = mid;
        customerid = cid;
        flightnumber = fnum;
    }

    public int getID() {
        return id;
    }

    public int getCustomerid() {
        return customerid;
    }

    public int getFlightnumber() {
        return flightnumber;
    }
}
