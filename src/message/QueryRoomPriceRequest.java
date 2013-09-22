package message;

public class QueryRoomPriceRequest extends ReservationMessage {
    private int id;
    private String location;

    public QueryRoomPriceRequest(int mid, String loc) {
        type = MessageType.QUERY_ROOMPRICE_REQUEST;
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
