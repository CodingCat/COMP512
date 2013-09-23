package message;

public class ReserveRoomRequest extends ReservationMessage {

    private int id;
    private int customerid;
    private String location;

    public ReserveRoomRequest(int mid, int cid, String loc) {
        type = MessageType.RESERVE_ROOM_REQUEST;
        id = mid;
        customerid = cid;
        location = loc;
    }

    public int getID() {
        return id;
    }

    public int getCustomerid() {
        return customerid;
    }

    public String getLocation() {
        return location;
    }
}
