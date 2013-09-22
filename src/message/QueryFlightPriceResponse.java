package message;

public class QueryFlightPriceResponse extends ReservationMessage {
    private int id;
    private int fn;
    private int price;

    public QueryFlightPriceResponse(int i, int flightnum, int p) {
        type = MessageType.QUERY_FLIGHT_REQUEST;
        id = i;
        fn = flightnum;
        price = p;
    }

    public int getID() {
        return id;
    }

    public int getFlightNum() {
        return fn;
    }

    public int getPrice() {
        return price;
    }
}
