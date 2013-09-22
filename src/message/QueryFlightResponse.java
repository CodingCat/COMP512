package message;

public class QueryFlightResponse extends ReservationMessage {

    private int id;
    private int flightnumber;
    private int seat;

    public QueryFlightResponse(int mid, int fnumber, int seatnum) {
        type = MessageType.QUERY_FLIGHT_RESPONSE;
        id = mid;
        flightnumber = fnumber;
        seat = seatnum;
    }

    public int getID() {
        return id;
    }

    public int getFlightnumber() {
        return flightnumber;
    }

    public int getSeat() {
        return seat;
    }
}
