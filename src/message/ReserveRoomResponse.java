package message;

public class ReserveRoomResponse extends QueryRoomResponse {

    public ReserveRoomResponse(int mid, String loc, int rnum, int pr) {
        super(mid, loc, rnum, pr);
        type = MessageType.RESERVE_ROOM_RESPONSE;
    }
}
