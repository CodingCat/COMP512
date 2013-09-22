package message;

public class QueryFlightRequest extends ReservationMessage {
    private int id;
    private int fn;

    public QueryFlightRequest(int i, int flightnum) {
        type = MessageType.QUERY_FLIGHT_REQUEST;
        id = i;
        fn = flightnum;
    }

    public int getID() {
        return id;
    }

    public int getFlightNum() {
        return fn;
    }

}
