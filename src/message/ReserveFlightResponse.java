package message;

public class ReserveFlightResponse extends QueryFlightResponse {

    public ReserveFlightResponse(int mid, int fnumber, int seatnum, int pr) {
        super(mid, fnumber, seatnum, pr);
        type = MessageType.RESERVE_FLIGHT_RESPONSE;
    }
}
