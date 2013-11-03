export CLASSPATH=/home/zhunan/code/COMP512/out/flightresourcemanager.jar:/home/zhunan/code/COMP512/out/carresourcemanager.jar:/home/zhunan/code/COMP512/out/roomresourcemanager.jar:/home/zhunan/code/COMP512/out/middleware.jar
rmiregistry 8228 &
java -classpath /home/zhunan/code/COMP512/out/flightresourcemanager.jar -Djava.rmi.server.codebase=file:///home/zhunan/code/COMP512/out/flightresourcemanager.jar/ -Djava.security.policy=file:///server.policy server.ResImpl.FlightResourceManager 8228 &
java -classpath /home/zhunan/code/COMP512/out/carresourcemanager.jar -Djava.rmi.server.codebase=file:///home/zhunan/code/COMP512/out/carresourcemanager.jar/ -Djava.security.policy=file:///server.policy server.ResImpl.CarResourceManager 8228 &
java -classpath /home/zhunan/code/COMP512/out/hotelresourcemanager.jar -Djava.rmi.server.codebase=file:///home/zhunan/code/COMP512/out/hotelresourcemanager.jar/ -Djava.security.policy=file:///server.policy server.ResImpl.HotelResourceManager 8228 &
java -classpath /home/zhunan/code/COMP512/out/middleware.jar -Djava.rmi.server.codebase=file:///home/zhunan/code/COMP512/out/middleware.jar/ -Djava.security.policy=file:///server.policy server.middleware.MiddlewareServer 8228 &

