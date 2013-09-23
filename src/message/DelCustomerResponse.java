package message;

import java.util.Vector;

public class DelCustomerResponse extends ReservationMessage {
    private Vector<Integer> flightnumbers;


    private Vector<Integer> seatnumber;
    private Vector<Integer> flightprice;



    private Vector<String> carlocation;
    private Vector<Integer> carnum;
    private Vector<Integer> carprice;

    private Vector<String> roomlocation;
    private Vector<Integer> roomnum;
    private Vector<Integer> roomprice;


    public DelCustomerResponse(Vector<Integer> flightnumbers,
                               Vector<Integer> seatnumber,
                               Vector<Integer> flightprice,
                               Vector<String> carlocation,
                               Vector<Integer> carnum,
                               Vector<Integer> carprice,
                               Vector<String> roomlocation,
                               Vector<Integer> roomnum,
                               Vector<Integer> roomprice) {
        type = MessageType.DELETE_CUSTOMER_RESPONSE;
        this.flightnumbers = flightnumbers;
        this.seatnumber = seatnumber;
        this.flightprice = flightprice;
        this.carlocation = carlocation;
        this.carnum = carnum;
        this.carprice = carprice;
        this.roomlocation = roomlocation;
        this.roomnum = roomnum;
        this.roomprice = roomprice;
    }

    public Vector<Integer> getFlightnumbers() {
        return flightnumbers;
    }

    public Vector<Integer> getSeatnumber() {
        return seatnumber;
    }

    public Vector<Integer> getFlightprice() {
        return flightprice;
    }

    public Vector<String> getCarlocation() {
        return carlocation;
    }

    public Vector<Integer> getCarnum() {
        return carnum;
    }

    public Vector<Integer> getCarprice() {
        return carprice;
    }

    public Vector<String> getRoomlocation() {
        return roomlocation;
    }

    public Vector<Integer> getRoomnum() {
        return roomnum;
    }

    public Vector<Integer> getRoomprice() {
        return roomprice;
    }



}
