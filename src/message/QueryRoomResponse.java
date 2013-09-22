package message;

public class QueryRoomResponse extends ReservationMessage {

    private int id;
    private String location;
    private int roomnum;

    public QueryRoomResponse(int mid, String loc, int rn) {
        type = MessageType.QUERY_ROOM_RESPONSE;
        id = mid;
        location = loc;
        roomnum = rn;
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

}
