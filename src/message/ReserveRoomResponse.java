package message;

public class ReserveRoomResponse extends QueryRoomResponse {

    private boolean success = false;

    public ReserveRoomResponse(int mid, String loc, int rnum, int pr, boolean suc) {
        super(mid, loc, rnum, pr);
        type = MessageType.RESERVE_ROOM_RESPONSE;
        success = suc;
    }

    public boolean isSuccess() {
        return success;
    }
}
