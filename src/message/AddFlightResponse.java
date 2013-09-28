package message;

public class AddFlightResponse extends  ReservationMessage {

    private int id = -1;
    private int flightnum = -1;


    private int seatnum = -1;
    private int price = -1;

    private boolean success = false;

    public AddFlightResponse (int i, int fnum, int snum, int p, boolean status) {
        type = MessageType.ADD_FLIGHT_RESPONSE;
        id = i;
        flightnum = fnum;
        seatnum = snum;
        price = p;
        success = status;
    }

    public int getID() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getFlightnum() {
        return flightnum;
    }

    public int getSeatnum() {
        return seatnum;
    }

    public int getPrice() {
        return price;
    }

}
