package message;

public class QueryCarPriceResponse extends ReservationMessage {

    private int id;
    private String location;
    private int price;

    public QueryCarPriceResponse(int mid, String loc, int p) {
        type = MessageType.QUERY_CARPRICE_RESPONSE;
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
