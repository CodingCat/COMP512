package test;

import server.middleware.Middleware;
import util.XmlParser;

public class testMain
{
	public static void main(String[] args)
	{
		String dir = args[0];
		// "/home/kai/Kai/servercode/config/mw_config.xml";
		XmlParser Parser = new XmlParser(dir);
		String flight_server = Parser.getTuple2("flight").x;
		int flight_port = Parser.getTuple2("flight").y;

		String car_server = Parser.getTuple2("car").x;
		int car_port = Parser.getTuple2("car").y;

		String room_server = Parser.getTuple2("room").x;
		int room_port = Parser.getTuple2("room").y;

		String rmi_server = Parser.getTuple2("customer").x;
		int rmi_port = Parser.getTuple2("customer").y;

		Middleware middlebox = new Middleware(flight_server, flight_port, car_server, car_port, room_server, room_port);

		middlebox.connection(flight_server, flight_port, car_server, car_port, room_server, room_port, rmi_port);
	}
}
