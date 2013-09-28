package message;

public class DelFlightResponse extends DelFlightRequest {

    private boolean success;

    public DelFlightResponse (int mid, int fid, boolean suc) {
        super(mid, fid);
        type = MessageType.DELETE_FLIGHT_RESPONSE;
        success = suc;
    }

    public boolean isSuccess() {
        return success;
    }
}
