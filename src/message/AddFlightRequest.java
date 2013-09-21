package message;

public class AddFlightRequest extends ReservationMessage {

    private int customerid;
    private int flightnum;
    private int flightseat;
    private int flightprice;

    public AddFlightRequest(int id, int flightNum,
                            int flightSeat, int flightPrice) {
        type = MessageType.ADD_FLIGHT_REQUEST;
        customerid = id;
        flightnum = flightNum;
        flightseat = flightSeat;
        flightprice = flightPrice;
    }

}
