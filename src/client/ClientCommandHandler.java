package client;

import message.*;
import nio.Message;
import nio.NIOClient;

import java.util.HashMap;
import java.util.Vector;

public class ClientCommandHandler extends NIOClient {

    private HashMap<String, String> supportCommands = new HashMap<String, String>();

    public ClientCommandHandler(String serverip, int serverport) {
        super(serverip, serverport);
        supportCommands.put("help", "Help\nTyping help on the prompt gives a list of all the commands available.\n" +
                "Typing help, <commandname> gives details on how to use the particular command.");
        supportCommands.put("newflight", "Adding a new Flight.\nPurpose:\n\tAdd information about a new flight.\n" +
                "Usage:\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>");
        supportCommands.put("newcar", "Adding a new Car.\nPurpose:\n\tAdd information about a new car location.\n" +
                "Usage:\tnewcar,<id>,<location>,<numberofcars>,<pricepercar>");
        supportCommands.put("newroom", "Adding a new Room.\nPurpose:\n\tAdd information about a new room location.\n" +
                "Usage:\tnewroom,<id>,<location>,<numberofrooms>,<priceperroom>");
        supportCommands.put("newcustomer", "Adding a new Customer.\nPurpose:\n\tGet the system to provide a new customer id. " +
                "(same as adding a new customer)\n" +
                "Usage:\tnewcustomer,<id>");
        supportCommands.put("deleteflight", "Deleting a flight's information.\nPurpose:\n\tDelete a flight's information. " +
                "Usage:\tdeleteflight,<id>,<flightnumber>");
        supportCommands.put("deletecar", "Delete a Car.\nPurpose:\nDelete all cars from a location.\nUsage:\n" +
                "Usage:\tdeletecar,<id>, <location>, <numCars>");
        supportCommands.put("deleteroom", "Deleting a Room\nPurpose:\nDelete all rooms from a location.\n" +
                "Usage:\tdeleteroom,<id>,<location>,<numRooms>");
        supportCommands.put("deletecustomer", "Removing a customer\nPurpose:\nRemoving a customer from the database.\n" +
                "Usage:\tdeletecustomer,<id>,<customerid>");
        supportCommands.put("queryflight", "Querying flight.\nPurpose:\nObtain Seat information about a certain flight.\n" +
                "Usage:\tqueryflight,<id>,<flightnumber>");
        supportCommands.put("querycar", "Query a Car location.\nPurpose:\nObtain number of cars at a certain car location.\n" +
                "Usage:\tquerycar,<id>,<location>");
        supportCommands.put("queryroom", "Querying a Room Location.\nPurpose:\nObtain number of rooms at a certain room location.\n" +
                "Usage:\tqueryroom,<id>,<location>");
        supportCommands.put("qeurycustomer", "Querying Customer Information,\nPurpose:\nObtain information" +
                " about a customer.\n" +
                "Usage:\tquerycustomer,<id>,<customerid>");
        supportCommands.put("queryflightprice", "Querying flight price.\nPurpose:\nObtain price information" +
                " about a certain flight.\n" +
                "Usage:\tqueryflightprice, <id>, <flightnumber>");
        //UnsupportedOperationException
        supportCommands.put("querycarprice", "Querying car price.\nPurpose:\nObtain price information" +
                " about a certain car location.\n" +
                "Usage:\tquerycarprice,<id>,<location>");
        supportCommands.put("queryroomprice", "Querying Room price.\nPurpose:\nObtain price information" +
                " about a certain room location.\n" +
                "Usage:\tqueryroomprice,<id>,<location>");
        supportCommands.put("reserveflight", "Reserve flight for a customer.\nPurpose:\nReserve a flight " +
                "for a customer.\n" +
                "Usage:\treserveflight,<id>,<customerid>,<flightnumber>");
        supportCommands.put("reservecar", "Reserving a Car.\nPurpose:\nReserve a given number of cars " +
                "for a customer at a particular location.\n" +
                "Usage:\treservecar,<id>,<customerid>,<location>,<numberofCars>");
        supportCommands.put("reserveroom", "Reserving a Room.\nPurpose:\nReserve a given number of rooms" +
                " for a customer at a particular location.\n" +
                "Usage:\treserveroom, <id>, <customerid>, <location>, <numberofRooms>");
        supportCommands.put("itinerary", "Reserving an Itinerary\nPurpose:\nBook one or more flights. " +
                "Also book zero or more cars/rooms at a location.\n" +
                "Usage:\titinerary,<id>,<customerid>,<flightnumber1>....<flightnumberN>," +
                "<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>");
        supportCommands.put("quit", "Quitting client.\nPurpose:\nExit the client\nUsage:\tquit");
        supportCommands.put("newcustomerid", "Creating new customer providing an id\tPurpose:\nCreates a " +
                " new customer with the id provided\tUsage:\tnewcustomerid, <id>, <customerid>");
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

    public void newcustomer(Vector<String> arguments) {
        if(arguments.size() != 2){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("newcustomer"));
            return;
        }
        System.out.println("Adding a new Customer using id:"+arguments.elementAt(1));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            send(new AddCustomerRequest(id));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void newcustomerID(Vector<String> arguments) {
        if(arguments.size() != 3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("newcustomerid"));
            return;
        }
        System.out.println("Adding a new Customer using id:"+arguments.elementAt(1) +
                " and cid " +arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            int cid = Integer.parseInt(arguments.elementAt(2));
            send(new AddCustomerWithIDRequest(id, cid));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deletecustomer(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("deletecustomer"));
            return;
        }
        System.out.println("Deleting a customer from the database using id: "+arguments.elementAt(1));
        System.out.println("Customer id: "+arguments.elementAt(2));
        try{
            int Id = Integer.parseInt(arguments.elementAt(1));
            int customer = Integer.parseInt(arguments.elementAt(2));
            send(new DelCustomerRequest(Id, customer));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void querycustomer(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("querycustomer"));
            return;
        }
        System.out.println("Querying Customer information using id: "+arguments.elementAt(1));
        System.out.println("Customer id: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            int customerid = Integer.parseInt(arguments.elementAt(2));
            send(new QueryCustomerRequest(id, customerid));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void newroom(Vector<String> arguments) {
        if(arguments.size() != 5){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("newroom"));
            return;
        }
        System.out.println("Adding a new Room using id: "+arguments.elementAt(1));
        System.out.println("Room Location: "+arguments.elementAt(2));
        System.out.println("Add Number of Rooms: "+arguments.elementAt(3));
        System.out.println("Set Price: "+arguments.elementAt(4));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            int numRooms = Integer.parseInt(arguments.elementAt(3));
            int price = Integer.parseInt(arguments.elementAt(4));
            //send addroomrequest
            send(new AddRoomRequest(id, location, numRooms, price));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteroom(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("deleteroom"));
            return;
        }
        System.out.println("Deleting all rooms from a particular location  using id: "+arguments.elementAt(1));
        System.out.println("Room Location: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            send(new DelRoomRequest(id, location));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void queryroom(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("queryroom"));
            return;
        }
        System.out.println("Querying a room location using id: "+arguments.elementAt(1));
        System.out.println("Room location: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            send(new QueryRoomRequest(id, location));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void queryroomprice(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("queryroomprice"));
            return;
        }
        System.out.println("Querying a room location using id: "+arguments.elementAt(1));
        System.out.println("Room location: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            send(new QueryRoomPriceRequest(id, location));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void newcar(Vector<String> arguments) {
        if(arguments.size() != 5){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("newcar"));
            return;
        }
        System.out.println("Adding a new Car using id: "+arguments.elementAt(1));
        System.out.println("Car Location: "+arguments.elementAt(2));
        System.out.println("Add Number of Cars: "+arguments.elementAt(3));
        System.out.println("Set Price: "+arguments.elementAt(4));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            int numCars = Integer.parseInt(arguments.elementAt(3));
            int price = Integer.parseInt(arguments.elementAt(4));
            //send addcar request
            send(new AddCarRequest(id, location, numCars, price));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deletecar(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("deletecar"));
            return;
        }
        System.out.println("Deleting the cars from a particular location  using id: "+arguments.elementAt(1));
        System.out.println("Car Location: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            send(new DelCarRequest(id, location));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void querycar(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("querycar"));
            return;
        }
        System.out.println("Querying a car location using id: "+arguments.elementAt(1));
        System.out.println("Car location: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            send(new QueryCarRequest(id, location));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void querycarprice(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("querycarprice"));
            return;
        }
        System.out.println("Querying a car price using id: "+arguments.elementAt(1));
        System.out.println("Car location: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            String location = arguments.elementAt(2);
            send(new QueryCarPriceRequest(id, location));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void newflight (Vector<String> arguments) {
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

    public void deleteflight(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("deleteflight"));
            return;
        }
        System.out.println("Deleting a flight using id: "+arguments.elementAt(1));
        System.out.println("Flight Number: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            int flightNum = Integer.parseInt(arguments.elementAt(2));
            send(new DelFlightRequest(id, flightNum));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void queryflight(Vector<String> arguments) {
        if(arguments.size() != 3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("queryflight"));
            return;
        }
        System.out.println("Querying a flight using id: "+arguments.elementAt(1));
        System.out.println("Flight number: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            int flightNum = Integer.parseInt(arguments.elementAt(2));
            send(new QueryFlightRequest(id, flightNum));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void queryflightprice(Vector<String> arguments) {
        if(arguments.size()!=3){
            System.out.println("Wrong Argument List");
            System.out.println(supportCommands.get("queryflightprice"));
            return;
        }
        System.out.println("Querying a flight Price using id: "+arguments.elementAt(1));
        System.out.println("Flight number: "+arguments.elementAt(2));
        try{
            int id = Integer.parseInt(arguments.elementAt(1));
            int flightNum = Integer.parseInt(arguments.elementAt(2));
            send(new QueryFlightPriceRequest(id, flightNum));
        }
        catch(Exception e){
            System.out.println("EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void help (Vector<String> arguments) {
        if(arguments.size()==1)   //command was "help"
            listCommands();
        else if (arguments.size()==2)  //command was "help <commandname>"
            listSpecific(arguments.elementAt(1));
        else  //wrong use of help command
            System.out.println("Improper use of help command. Type help or help, <commandname>");
    }

    public void quit(Vector<String> arguments) {
        System.out.println("Quitting client.");
        System.exit(1);
    }

    @Override
    public void dispatch(Message msg) {
        if (msg instanceof ReservationMessage) {
            ReservationMessage rmsg = (ReservationMessage) msg;
            switch (rmsg.getMessageType()) {
                case QUERY_FLIGHT_RESPONSE:
                    QueryFlightResponse qfresponse = (QueryFlightResponse) rmsg;
                    System.out.println("flight id:" + qfresponse.getFlightnumber() +
                        " seat num:" + qfresponse.getSeat());
                    break;
                case QUERY_FLIGHTPRICE_RESPONSE:
                    QueryFlightPriceResponse qfpresponse = (QueryFlightPriceResponse) rmsg;
                    System.out.println("flight id:" + qfpresponse.getFlightNum() +
                            " price:" + qfpresponse.getPrice());
                    break;
                case QUERY_CAR_RESPONSE:
                    QueryCarResponse qcresponse = (QueryCarResponse) rmsg;
                    System.out.println("car  location:" +
                         qcresponse.getLocation() + " car_num:" + qcresponse.getCarNum());
                    break;
                case QUERY_CARPRICE_RESPONSE:
                    QueryCarPriceResponse qcpresponse = (QueryCarPriceResponse) rmsg;
                    System.out.println("car location:" + qcpresponse.getLocation() +
                            " price:" + qcpresponse.getPrice());
                    break;
                case QUERY_ROOM_RESPONSE:
                    QueryRoomResponse qrresponse = (QueryRoomResponse) rmsg;
                    System.out.println("room  location:" +
                            qrresponse.getLocation() + " room_num:" + qrresponse.getRoomnum());
                    break;
                case QUERY_ROOMPRICE_RESPONSE:
                    QueryRoomPriceResponse qrpresponse = (QueryRoomPriceResponse) rmsg;
                    System.out.println("room  location:" +
                            qrpresponse.getLocation() + " room_price:" + qrpresponse.getPrice());
                    break;
                case QUERY_CUSTOMER_RESPONSE:
                    QueryCustomerResponse qcuresponse = (QueryCustomerResponse) rmsg;
                    System.out.println("customerID:" +
                            qcuresponse.getCustomerid() + " bill:" + qcuresponse.getBill());
                    break;
                default:
                    System.out.println("unrecognizable message");
            }
        }
    }
}
