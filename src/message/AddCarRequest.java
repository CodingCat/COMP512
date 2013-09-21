package message;

public class AddCarRequest extends ReservationMessage {
    private int id;
    private String location;
    private int carnum;
    private int price;

    public AddCarRequest(int carid, String loc, int num, int p) {
        type = MessageType.ADD_CAR_REQUEST;
        id = carid;
        location = loc;
        carnum = num;
        price = p;
    }

    public int getID() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public int getCarnum() {
        return carnum;
    }

    public int getPrice() {
        return price;
    }
}
