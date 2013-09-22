package message;

public class AddCustomerRequest extends ReservationMessage {
    private int id;

    public AddCustomerRequest(int i) {
        id = i;
    }

    public int getID() {
        return id;
    }
}
