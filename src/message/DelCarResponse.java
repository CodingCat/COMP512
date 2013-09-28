package message;

public class DelCarResponse extends DelCarRequest {

    private boolean success;

    public DelCarResponse (int mid, String location, boolean suc) {
        super(mid, location);
        type = MessageType.DELETE_CAR_RESPONSE;
        success = suc;
    }

    public boolean isSuccess() {
        return success;
    }
}
