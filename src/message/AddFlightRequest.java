package message;

import nio.Message;

public class AddFlightRequest extends Message {
    private int customerid;
    private int flightnum;
    private int flightseat;
    private int flightprice;

    public AddFlightRequest(int id, int flightNum,
                            int flightSeat, int flightPrice) {
        customerid = id;
        flightnum = flightNum;
        flightseat = flightSeat;
        flightprice = flightPrice;
    }


}
