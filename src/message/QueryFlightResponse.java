package message;

public class QueryFlightResponse extends ReservationMessage {

    private int id;
    private int flightnumber;
    private int seat;
    private int price;

    public QueryFlightResponse(int mid, int fnumber, int seatnum, int pr) {
        type = MessageType.QUERY_FLIGHT_RESPONSE;
        id = mid;
        flightnumber = fnumber;
        seat = seatnum;
        price = pr;
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

    public int getPrice() {
        return price;
    }
}
