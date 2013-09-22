package message;

public class DelCarRequest extends ReservationMessage {

    private int id;
    private String location;

    public DelCarRequest(int mid, String loc) {
        type = MessageType.DELETE_CAR_REQUEST;
        id = mid;
        location = loc;
    }

    public String getLocation() {
        return location;
    }

    public int getID() {
        return id;
    }
}
