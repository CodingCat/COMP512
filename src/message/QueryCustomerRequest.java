package message;

public class QueryCustomerRequest extends ReservationMessage {

    private int id;
    private int customerid;

    public QueryCustomerRequest(int mid, int cid) {
        type = MessageType.QUERY_CUSTOMER_REQUEST;
        id = mid;
        customerid = cid;
    }

    public int getID() {
        return id;
    }

    public int getCustomerid() {
        return customerid;
    }
}
