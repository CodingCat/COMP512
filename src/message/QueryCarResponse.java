package message;

public class QueryCarResponse extends ReservationMessage {

    private int id;
    private String location;
    private int carNum;

    public QueryCarResponse(int mid, String loc, int numofCars) {
        type = MessageType.QUERY_CAR_RESPONSE;
        id = mid;
        location = loc;
        carNum = numofCars;
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
}
