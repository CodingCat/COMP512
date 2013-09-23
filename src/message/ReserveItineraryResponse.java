package message;

import java.util.Vector;

public class ReserveItineraryResponse extends ReservationMessage {

    private int id;

    private Vector flightnumbers;
    private Vector seatnumber;
    private Vector flightprice;

    private String location;
    private int carnum;
    private int carprice;

    private int roomnum;
    private int roomprice;



    private boolean carflag;
    private boolean roomflag;

    public ReserveItineraryResponse(int mid, Vector fnum, Vector snum, Vector fpr,
                                    String  loc, int cnum, int cpr, int rnum, int rpr,
                                    boolean car, boolean room) {
        id = mid;
        flightnumbers = fnum;
        seatnumber = snum;
        flightprice = fpr;
        location = loc;
        carnum = cnum;
        carprice = cpr;
        roomnum = rnum;
        roomprice = rpr;
        carflag = car;
        roomflag = room;
    }


    public int getId() {
        return id;
    }


    public Vector getFlightnumbers() {
        return flightnumbers;
    }

    public Vector getSeatnumber() {
        return seatnumber;
    }

    public Vector getFlightprice() {
        return flightprice;
    }

    public String getLocation() {
        return location;
    }

    public int getCarnum() {
        return carnum;
    }

    public int getCarprice() {
        return carprice;
    }

    public int getRoomnum() {
        return roomnum;
    }

    public int getRoomprice() {
        return roomprice;
    }

    public boolean isCarflag() {
        return carflag;
    }

    public boolean isRoomflag() {
        return roomflag;
    }
}
