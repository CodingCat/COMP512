package test;

import nio.Message;

public class DummyMessage extends Message {
    private int additionalVariable = 100;

    public int getAdditionalVariable() {
        return additionalVariable;
    }
}
