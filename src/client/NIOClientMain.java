package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;

public class NIOClientMain {

    private ClientCommandHandler cmdhandler = null;

    public NIOClientMain (String serverIP, int port) {
        cmdhandler = new ClientCommandHandler(serverIP, port);
    }

    public Vector<String> parse(String command) {
        command = command.trim();
        Vector<String> arguments = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(command, ",");
        String argument ="";
        while (tokenizer.hasMoreTokens()) {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }

    private void dispatchCommand(String command) {
        Vector<String> arguments = parse(command);
        if (arguments.size() > 0) {
            String commandName = arguments.get(0);
            //reflect to the function in ClientCommandHandler
            System.out.println("user input commandName:" + commandName);
            try {
                Method method = cmdhandler.getClass().getMethod(
                            commandName.toLowerCase(), arguments.getClass());
                method.invoke(cmdhandler, arguments);
            } catch (NoSuchMethodException e) {
                System.out.println("no such a command");
            } catch (InvocationTargetException e) {
                System.out.println("invalid argument");
            } catch (IllegalAccessException e) {
                System.out.println("cannot access this method");
            }
        }
    }

    public void run() {
        Thread client_t = new Thread(cmdhandler);
        client_t.start();
    }


    public static void main(String [] args) {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        //start the main thread
        NIOClientMain client = new NIOClientMain(args[0], Integer.parseInt(args[1]));
        client.run();
        //enter into the user interface
        System.out.println("\n\n\tClient Interface");
        System.out.println("Type \"help\" for list of supported commands");
        while(true){
            System.out.print("\n>");
            try{
                //read the next command
                client.dispatchCommand(stdin.readLine());
            }
            catch (IOException io){
                System.out.println("Unable to read from standard in");
                System.exit(1);
            }
        }
    }
}
