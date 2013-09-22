package message;

public class AddCustomerRequest extends ReservationMessage {
    private int id;

    public AddCustomerRequest(int i) {
        type = MessageType.ADD_CUSTOMER_REQUEST;
        id = i;
    }

    public int getID() {
        return id;
    }
}
