package message;

public class QueryCustomerResponse extends ReservationMessage {

    private int id;
    private int customerid;
    private String bill;

    public QueryCustomerResponse(int mid, int cid, String b) {
        type = MessageType.QUERY_CUSTOMER_RESPONSE;
        id = mid;
        customerid = cid;
        bill = b;
    }

    public int getID() {
        return id;
    }

    public int getCustomerid() {
        return customerid;
    }

    public String getBill() {
        return bill;
    }
}
