package test;

import nio.Message;

public class DummyMessage extends Message {

    private String text = "HelloWorld";

    public void setText(String t) {
        text = t;
    }

    public String getText() {
        return text;
    }
}
