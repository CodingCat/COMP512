package server.ResImpl;

import message.ReservationMessage;
import nio.Message;
import nio.NIOReactor;
import util.XmlParser;

public class NIOMiddleware extends NIOReactor {

    public NIOMiddleware(String serverIP, int serverport, String confPath) {
        super(serverIP, serverport);
        XmlParser xmlParser = new XmlParser(confPath);
        //setup resource managers
        //support single resource manager for each resource for now
        setClientEndPoint("flight",  xmlParser.getTuple2("flight").x,
                xmlParser.getTuple2("flight").y);
        setClientEndPoint("car",  xmlParser.getTuple2("car").x,
                xmlParser.getTuple2("car").y);
        setClientEndPoint("room",  xmlParser.getTuple2("room").x,
                xmlParser.getTuple2("room").y);
        setClientEndPoint("customer", xmlParser.getTuple2("customer").x,
                xmlParser.getTuple2("customer").y);
    }

    @Override
    public void dispatch(Message msg) {
        if (msg instanceof ReservationMessage) {
           ReservationMessage rmsg = (ReservationMessage) msg;
            switch (rmsg.getMessageType()) {
                case ADD_FLIGHT_REQUEST:
                case DELETE_FLIGHT_REQUEST:
                case QUERY_FLIGHT_REQUEST:
                case QUERY_FLIGHTPRICE_REQUEST:
                case RESERVE_FLIGHT_REQUEST:
                    forward("flight", rmsg);
                    break;
                case ADD_CAR_REQUEST:
                case DELETE_CAR_REQUEST:
                case QUERY_CAR_REQUEST:
                case QUERY_CARPRICE_REQUEST:
                case RESERVE_CAR_REQUEST:
                    forward("car", rmsg);
                    break;
                case ADD_ROOM_REQUEST:
                case DELETE_ROOM_REQUEST:
                case QUERY_ROOMPRICE_REQUEST:
                case QUERY_ROOM_REQUEST:
                case RESERVE_ROOM_REQUEST:
                    forward("room", rmsg);
                    break;
                case ADD_CUSTOMER_REQUEST:
                case ADD_CUSTOMER_ID_REQUEST:
                case DELETE_CUSTOMER_REQUEST:
                case QUERY_CUSTOMER_REQUEST:
                    forward("customer", rmsg);
                    break;
                case QUERY_FLIGHTPRICE_RESPONSE:
                case QUERY_CARPRICE_RESPONSE:
                case QUERY_ROOMPRICE_RESPONSE:
                case QUERY_FLIGHT_RESPONSE:
                case QUERY_CAR_RESPONSE:
                case QUERY_ROOM_RESPONSE:
                case QUERY_CUSTOMER_RESPONSE:
                    reply(rmsg);
                    break;
                case RESERVE_ITINERARY_REQUEST:
                    forward("flight", rmsg);
                    forward("car", rmsg);
                    forward("room", rmsg);
                    forward("customer", rmsg);
                    break;
                default:
                    break;
            }
        }
    }



    public static void main(String [] args) {
        NIOMiddleware middleware = new NIOMiddleware(args[0],
                Integer.parseInt(args[1]),
                args[2]);
        //setup resource managers
        //load the configuration file
        Thread middleware_t = new Thread(middleware);
        middleware_t.run();
    }
}
