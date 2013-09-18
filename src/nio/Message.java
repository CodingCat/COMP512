package nio;


import java.io.*;

public class Message implements Serializable {
    //header part
    //8 byte
    protected int messageID = -1;
    protected int messageLength = 8;

    public int getMessageID () {
        return messageID;
    }

    public int getMessageLength () {
        return messageLength;
    }

    public static byte[] serialize(Message obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Message deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            Message ret = (Message) is.readObject();
            is.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
