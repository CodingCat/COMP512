package message;

public class AddRoomResponse extends ReservationMessage {

    private int id = -1;
    private String location;
    private int roomnum;
    private int roomprice;

    private boolean success = false;

    public AddRoomResponse (int i, String loc, int rnum, int rprice, boolean status) {
        type = MessageType.ADD_ROOM_RESPONSE;
        id = i;
        location = loc;
        roomnum = rnum;
        roomprice = rprice;
        success = status;
    }

    public int getID() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getRoomprice() {
        return roomprice;
    }

    public String getLocation() {
        return location;
    }

    public int getRoomnum() {
        return roomnum;
    }
}
