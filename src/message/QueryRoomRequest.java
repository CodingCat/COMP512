package message;

public class QueryRoomRequest extends ReservationMessage {

    private int id;
    private String location;

    public QueryRoomRequest(int mid, String loc) {
        type = MessageType.QUERY_ROOM_REQUEST;
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
