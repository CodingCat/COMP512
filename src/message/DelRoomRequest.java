package message;

public class DelRoomRequest extends ReservationMessage {

    private int id;
    private String location;

    public DelRoomRequest(int mid, String loc) {
        type = MessageType.DELETE_ROOM_REQUEST;
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
