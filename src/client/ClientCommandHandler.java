package client;

import message.AddFlightRequest;
import nio.Message;
import nio.NIOClient;

import java.util.HashMap;
import java.util.Vector;

public class ClientCommandHandler extends NIOClient {

    private HashMap<String, String> supportCommands = new HashMap<String, String>();

    public ClientCommandHandler(String serverip, int serverport) {
        super(serverip, serverport);
        supportCommands.put("helper", "Help\nTyping help on the prompt gives a list of all the commands available.\n" +
            "Typing help, <commandname> gives details on how to use the particular command.");
        supportCommands.put("newflight", "Adding a new Flight.\nPurpose:\tAdd information about a new flight.\n" +
                "Usage:\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>");
    }

    private void listCommands() {
        System.out.println("\nWelcome to the client interface provided to test your project.");
        System.out.println("Commands accepted by the interface are:");
        for (String command : supportCommands.keySet()) System.out.println(command);
        System.out.println("\ntype help, <commandname> for detailed info(NOTE the use of comma).");
    }

    private void listSpecific(String commandName) {
        if (!supportCommands.containsKey(commandName))
            System.out.println("the interface does not support this command:" + commandName);
        else
            System.out.println(supportCommands.get(commandName));
    }

    void newflight (Vector<String> arguments) {
        if(arguments.size()!=5){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("newflight"));
            return;
        }
        System.out.println("Adding a new Flight using id: "+arguments.elementAt(1));
        System.out.println("Flight number: "+arguments.elementAt(2));
        System.out.println("Add Flight Seats: "+arguments.elementAt(3));
        System.out.println("Set Flight Price: "+arguments.elementAt(4));
        try{
            int Id = Integer.parseInt(arguments.elementAt(1));
            int flightNum = Integer.parseInt(arguments.elementAt(2));
            int flightSeats = Integer.parseInt(arguments.elementAt(3));
            int flightPrice = Integer.parseInt(arguments.elementAt(4));
            //send addflight message
            send(new AddFlightRequest(Id, flightNum, flightSeats, flightPrice));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    void help (Vector arguments) {
        if(arguments.size()==1)   //command was "help"
            listCommands();
        else if (arguments.size()==2)  //command was "help <commandname>"
            listSpecific((String)arguments.elementAt(1));
        else  //wrong use of help command
            System.out.println("Improper use of help command. Type help or help, <commandname>");
    }

    @Override
    public void dispatch(Message msg) {

    }
}
