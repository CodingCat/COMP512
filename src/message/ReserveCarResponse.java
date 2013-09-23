package message;

public class ReserveCarResponse extends QueryCarResponse {

    public ReserveCarResponse(int mid, String loc, int cnum, int cpr) {
        super(mid, loc, cnum, cpr);
        type = MessageType.RESERVE_CAR_RESPONSE;
    }
}
