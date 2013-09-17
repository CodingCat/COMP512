package nio;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {
    //header part
    //8 byte
    protected int messageID = -1;
    protected int messageLength = 8;

    protected byte[] payload = null;

    public static Message getMessage(byte[] rawData) {
        Message msg = new Message();
        msg.messageID = ByteBuffer.wrap(Arrays.copyOfRange(rawData, 0, 4)).getInt();
        msg.messageLength = ByteBuffer.wrap(Arrays.copyOfRange(rawData, 4, 8)).getInt();
        msg.payload = new byte[rawData.length - 8];
        msg.payload = Arrays.copyOfRange(rawData, 8, rawData.length);
        return msg;
    }
}
