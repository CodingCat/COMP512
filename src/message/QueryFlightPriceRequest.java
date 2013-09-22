package message;

public class QueryFlightPriceRequest extends ReservationMessage {

    private int id;
    private int fn;

    public QueryFlightPriceRequest(int i, int flightnum) {
        type = MessageType.QUERY_FLIGHTPRICE_REQUEST;
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
