package message;

public class AddRoomRequest extends ReservationMessage {
    private int id;
    private String location;
    private int roomnum;
    private int price;

    public AddRoomRequest(int roomid, String loc, int num, int p) {
        type = MessageType.ADD_ROOM_REQUEST;
        id = roomid;
        location = loc;
        roomnum = num;
        price = p;
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

    public int getPrice() {
        return price;
    }
}
