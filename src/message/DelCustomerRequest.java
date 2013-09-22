package message;

public class DelCustomerRequest extends ReservationMessage {

    private int id;
    private int customerid;

    public DelCustomerRequest(int mid, int cid) {
        type = MessageType.DELETE_CUSTOMER_REQUEST;
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
