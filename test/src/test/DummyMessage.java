package test;

import nio.Message;

public class DummyMessage extends Message {

    private String text = "HelloWorld";

    public void setText(String t) {
        text = t;
        messageLength = 8 + text.length();
    }

    public String getText() {
        return text;
    }
}
