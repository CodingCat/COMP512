package serverplusTM.ResImpl.messages;

import org.jgroups.Header;

import java.io.DataInput;
import java.io.DataOutput;

public class MessageHeaderBase extends Header {

    public String type = null;

    public MessageHeaderBase(String t) {
        type = t;
    }

    @Override
    public int size() {
        return type.length();
    }

    @Override
    public void writeTo(DataOutput dataOutput) throws Exception {
        dataOutput.writeChars(type);
    }

    @Override
    public void readFrom(DataInput dataInput) throws Exception {
        type = dataInput.readLine();
    }
}
