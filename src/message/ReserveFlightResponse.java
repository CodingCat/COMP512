package message;

public class ReserveFlightResponse extends QueryFlightResponse {

    private boolean success = false;

    public ReserveFlightResponse(int mid, int fnumber, int seatnum, int pr, boolean suc) {
        super(mid, fnumber, seatnum, pr);
        type = MessageType.RESERVE_FLIGHT_RESPONSE;
        success = suc;
    }

    public boolean isSuccess() {
        return success;
    }
}
