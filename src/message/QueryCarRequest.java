package message;

public class QueryCarRequest extends ReservationMessage {

    private int id;
    private String location;

    public QueryCarRequest(int mid, String loc) {
        type = MessageType.QUERY_CAR_REQUEST;
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
