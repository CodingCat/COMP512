package message;

public class QueryCarPriceRequest extends ReservationMessage {

    private int id;
    private String location;

    public QueryCarPriceRequest(int mid, String loc) {
        type = MessageType.QUERY_CARPRICE_REQUEST;
        id = mid;
        location = loc;
    }

    public int getID() {
        return id;
    }

    public String getLocation() {
        return location;
    }
}
