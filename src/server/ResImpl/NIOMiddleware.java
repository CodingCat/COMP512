package server.ResImpl;

import message.ReservationMessage;
import nio.Message;
import nio.NIOReactor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;

public class NIOMiddleware extends NIOReactor {

    private class Tuple2<X, Y> {
        public final X x;
        public final Y y;
        public Tuple2(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    private class XmlParser {
        private HashMap<String, Tuple2<String, Integer>> propertyMap = null;

        public XmlParser(String confPath) {
            propertyMap = new HashMap<String, Tuple2<String, Integer>>();
            parse(confPath);
        }

        public Tuple2<String, Integer> getTuple2(String key) {
            return propertyMap.get(key);
        }

        /**
         * parsing operation
         */
        private void parse(String confPath) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace(true);
                DocumentBuilder builder;
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(confPath);
                Element configurationRoot = doc.getDocumentElement();
                NodeList propertyList = configurationRoot.getChildNodes();
                for (int i = 0; i < propertyList.getLength(); i++) {
                    Node property = propertyList.item(i);
                    Element eleProperty = (Element) property;
                    propertyMap.put(getTagValue("name", eleProperty),
                            new Tuple2<String, Integer>(getTagValue("ip", eleProperty),
                                    Integer.parseInt(getTagValue("port", eleProperty))));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * get the value with String type
         * @param tagName, the property name
         * @param ele, the element
         * @return the value
         */
        private String getTagValue(String tagName, Element ele){
            NodeList nlList = ele.getElementsByTagName(tagName).item(0).getChildNodes();
            Node nValue = nlList.item(0);
            if (nValue == null){
                return null;
            }
            return nValue.getNodeValue();
        }
    }

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
    }

    @Override
    public void dispatch(Message msg) {
        if (msg instanceof ReservationMessage) {
           ReservationMessage rmsg = (ReservationMessage) msg;
            switch (rmsg.getMessageType()) {
                case ADD_FLIGHT_REQUEST:
                    forward("flight", rmsg);
                    break;
                case ADD_CAR_REQUEST:
                    forward("car", rmsg);
                    break;
                case ADD_ROOM_REQUEST:
                    forward("room", rmsg);
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
