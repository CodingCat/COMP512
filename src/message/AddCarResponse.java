package message;

public class AddCarResponse extends  ReservationMessage {

    private int id = -1;
    private String location;
    private int carnum;


    private int carprice;

    private boolean success = false;

    public AddCarResponse (int i, String loc, int cnum, int cprice, boolean status) {
        type = MessageType.ADD_CAR_RESPONSE;
        id = i;
        location = loc;
        carnum = cnum;
        carprice = cprice;
        success = status;
    }

    public int getID() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCarprice() {
        return carprice;
    }

    public String getLocation() {
        return location;
    }

    public int getCarnum() {
        return carnum;
    }
}
