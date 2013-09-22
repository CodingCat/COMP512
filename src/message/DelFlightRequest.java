package message;

public class DelFlightRequest extends ReservationMessage {

    private int id;
    private int fn;

    public DelFlightRequest(int i, int flightnum) {
        type = MessageType.DELETE_FLIGHT_REQUEST;
        id = i;
        fn = flightnum;
    }

    public int getID() {
        return id;
    }

    public int getFlightNum() {
        return fn;
    }
}
