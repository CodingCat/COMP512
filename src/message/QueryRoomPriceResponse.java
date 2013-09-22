package message;

public class QueryRoomPriceResponse extends  ReservationMessage {
    private int id;
    private String location;
    private int price;

    public QueryRoomPriceResponse(int mid, String loc, int p) {
        type = MessageType.QUERY_ROOM_RESPONSE;
        id = mid;
        location = loc;
        price = p;
    }

    public int getID() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public int getPrice() {
        return price;
    }

}
