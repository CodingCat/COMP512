package nio;


import java.io.*;

public class Message implements Serializable {
    //header part
    //8 byte
    protected int messageLength = 8;

    //transactionID is used to identify the sponsor of the
    //message
    public int transactionID = -1;

    public int getTransactionID() {
        return transactionID;
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
