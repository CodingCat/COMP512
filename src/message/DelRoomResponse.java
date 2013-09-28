package message;

public class DelRoomResponse extends DelRoomRequest {

    private boolean success;

    public DelRoomResponse (int mid, String loc, boolean suc) {
        super(mid, loc);
        type = MessageType.DELETE_ROOM_RESPONSE;
        success = suc;
    }

    public boolean isSuccess() {
        return success;
    }
}
