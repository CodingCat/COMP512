package message;

public class AddCustomerWithIDRequest extends ReservationMessage {

    private int id;
    private int customerid;

    public AddCustomerWithIDRequest(int i, int cid) {
        type = MessageType.ADD_CUSTOMER_ID_REQUEST;
        id = i;
        customerid = cid;
    }

    public int getID() {
        return id;
    }

    public int getCustomerid() {
        return customerid;
    }
}
