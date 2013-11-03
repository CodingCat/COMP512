package nio;

import java.nio.channels.SelectionKey;

public class MessageHandler implements Runnable {

    SelectionKey socketkey = null;

    public MessageHandler(SelectionKey key) {
        socketkey = key;
    }
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            throw new Exception("undefined run method");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
