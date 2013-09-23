package message;

public class ReserveCarRequest extends ReservationMessage {

    private int id;
    private int customerid;
    private String location;

    public ReserveCarRequest(int mid, int cid, String loc) {
        type = MessageType.RESERVE_CAR_REQUEST;
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
