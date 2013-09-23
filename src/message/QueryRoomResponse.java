package message;

public class QueryRoomResponse extends ReservationMessage {

    private int id;
    private String location;
    private int roomnum;
    private int roomprice;

    public QueryRoomResponse(int mid, String loc, int rn, int pr) {
        type = MessageType.QUERY_ROOM_RESPONSE;
        id = mid;
        location = loc;
        roomnum = rn;
        roomprice = pr;
    }

    public int getID() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public int getRoomnum() {
        return roomnum;
    }

    public int getRoomprice() {
        return roomprice;
    }

}
