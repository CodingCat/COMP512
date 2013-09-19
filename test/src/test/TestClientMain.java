package test;

import nio.NIOClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestClientMain {

    public static void main(String [] args) {
        NIOClient client = new NIOClient(args[0], Integer.parseInt(args[1]));
        Thread client_t = new Thread(client);
        client_t.start();
        DummyMessage dmsg = new DummyMessage();
        while (true) {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                dmsg.setText(in.readLine());
                System.out.println("sending " + dmsg.getText());
                client.send(dmsg);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
