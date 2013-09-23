package message;

public class QueryCarResponse extends ReservationMessage {

    private int id;
    private String location;
    private int carNum;
    private int price;

    public QueryCarResponse(int mid, String loc, int numofCars, int pr) {
        type = MessageType.QUERY_CAR_RESPONSE;
        id = mid;
        location = loc;
        carNum = numofCars;
        price = pr;
    }

    public int getID() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public int getCarNum() {
        return carNum;
    }

    public int getPrice() {
        return price;
    }
}
