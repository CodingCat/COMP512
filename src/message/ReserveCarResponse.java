package message;

public class ReserveCarResponse extends QueryCarResponse {

    private boolean success = false;

    public ReserveCarResponse(int mid, String loc, int cnum, int cpr, boolean suc) {
        super(mid, loc, cnum, cpr);
        type = MessageType.RESERVE_CAR_RESPONSE;
        success = suc;
    }

    public boolean isSuccess() {
        return success;
    }
}
