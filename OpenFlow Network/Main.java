package cs.tcd.ie;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

import tcdIO.Terminal;

public class Main {
	
	static final int DEFAULT_PORT = 40000;
	static final String DEFAULT_HOST = "localhost";
	static final int MAX_LATENCY = 1000;
	static final Random latencyGenerator = new Random();
	
	public static void main(String[] args) {
		int portCount = 0;
		
		//Ports
		int controllerSendPort = DEFAULT_PORT + portCount++;
		int controllerReceivePort = DEFAULT_PORT + portCount++;

		int router1SendPort = DEFAULT_PORT + portCount++;
		int router1ReceivePort = DEFAULT_PORT + portCount++;
		int router2SendPort = DEFAULT_PORT + portCount++;
		int router2ReceivePort = DEFAULT_PORT + portCount++;
		int router3SendPort = DEFAULT_PORT + portCount++;
		int router3ReceivePort = DEFAULT_PORT + portCount++;
		int router4SendPort = DEFAULT_PORT + portCount++;
		int router4ReceivePort = DEFAULT_PORT + portCount++;
		int router5SendPort = DEFAULT_PORT + portCount++;
		int router5ReceivePort = DEFAULT_PORT + portCount++;
		int router6SendPort = DEFAULT_PORT + portCount++;
		int router6ReceivePort = DEFAULT_PORT + portCount++;
		int router7SendPort = DEFAULT_PORT + portCount++;
		int router7ReceivePort = DEFAULT_PORT + portCount++;
		int router8SendPort = DEFAULT_PORT + portCount++;
		int router8ReceivePort = DEFAULT_PORT + portCount++;
		
		int user1SendPort = DEFAULT_PORT + portCount++;
		int user1ReceivePort = DEFAULT_PORT + portCount++;
		int user2SendPort = DEFAULT_PORT + portCount++;
		int user2ReceivePort = DEFAULT_PORT + portCount++;
		
		//Controller
		Terminal controllerTerminal = new Terminal("Controller");
		Thread Controller = new Thread(new Controller(controllerTerminal, controllerSendPort, controllerReceivePort));
		Controller.start();
		
		//Router1
		ArrayList<InetSocketAddress> router1RouterConnections = new ArrayList<InetSocketAddress>();
		ArrayList<InetSocketAddress> router1EndUserConnections = new ArrayList<InetSocketAddress>();
		router1EndUserConnections.add(new InetSocketAddress(DEFAULT_HOST, user1ReceivePort));
		router1RouterConnections.add(new InetSocketAddress(DEFAULT_HOST, router2ReceivePort));
		router1RouterConnections.add(new InetSocketAddress(DEFAULT_HOST, router3ReceivePort));
		ArrayList<Integer> router1RouterLatencies = new ArrayList<Integer>();
		ArrayList<Integer> router1EndUserLatencies = new ArrayList<Integer>();
		router1EndUserLatencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		router1RouterLatencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		router1RouterLatencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router1Terminal = new Terminal("Router 1");
		Thread Router1 = new Thread(new Router(router1Terminal, router1SendPort, router1ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router1RouterConnections, router1RouterLatencies, router1EndUserConnections, router1EndUserLatencies));
		Router1.start();
		
		//Router2
		ArrayList<InetSocketAddress> router2Connections = new ArrayList<InetSocketAddress>();
		router2Connections.add(new InetSocketAddress(DEFAULT_HOST, router1ReceivePort));
		router2Connections.add(new InetSocketAddress(DEFAULT_HOST, router5ReceivePort));
		ArrayList<Integer> router2Latencies = new ArrayList<Integer>();
		router2Latencies.add(router1RouterLatencies.get(0));
		router2Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router2Terminal = new Terminal("Router 2");
		Thread Router2 = new Thread(new Router(router2Terminal, router2SendPort, router2ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router2Connections, router2Latencies, null, null));
		Router2.start();
		
		//Router3
		ArrayList<InetSocketAddress> router3Connections = new ArrayList<InetSocketAddress>();
		router3Connections.add(new InetSocketAddress(DEFAULT_HOST, router1ReceivePort));
		router3Connections.add(new InetSocketAddress(DEFAULT_HOST, router4ReceivePort));
		router3Connections.add(new InetSocketAddress(DEFAULT_HOST, router6ReceivePort));
		ArrayList<Integer> router3Latencies = new ArrayList<Integer>();
		router3Latencies.add(router1RouterLatencies.get(1));
		router3Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		router3Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router3Terminal = new Terminal("Router 3");
		Thread Router3 = new Thread(new Router(router3Terminal, router3SendPort, router3ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router3Connections, router3Latencies, null, null));
		Router3.start();
		
		//Router4
		ArrayList<InetSocketAddress> router4Connections = new ArrayList<InetSocketAddress>();
		router4Connections.add(new InetSocketAddress(DEFAULT_HOST, router3ReceivePort));
		router4Connections.add(new InetSocketAddress(DEFAULT_HOST, router5ReceivePort));
		router4Connections.add(new InetSocketAddress(DEFAULT_HOST, router7ReceivePort));
		ArrayList<Integer> router4Latencies = new ArrayList<Integer>();
		router4Latencies.add(router3Latencies.get(1));
		router4Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		router4Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router4Terminal = new Terminal("Router 4");
		Thread Router4 = new Thread(new Router(router4Terminal, router4SendPort, router4ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router4Connections, router4Latencies, null, null));
		Router4.start();
		
		//Router5
		ArrayList<InetSocketAddress> router5Connections = new ArrayList<InetSocketAddress>();
		router5Connections.add(new InetSocketAddress(DEFAULT_HOST, router2ReceivePort));
		router5Connections.add(new InetSocketAddress(DEFAULT_HOST, router4ReceivePort));
		router5Connections.add(new InetSocketAddress(DEFAULT_HOST, router7ReceivePort));
		ArrayList<Integer> router5Latencies = new ArrayList<Integer>();
		router5Latencies.add(router2Latencies.get(1));
		router5Latencies.add(router4Latencies.get(1));
		router5Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router5Terminal = new Terminal("Router 5");
		Thread Router5 = new Thread(new Router(router5Terminal, router5SendPort, router5ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router5Connections, router5Latencies, null, null));
		Router5.start();
		
		//Router6
		ArrayList<InetSocketAddress> router6Connections = new ArrayList<InetSocketAddress>();
		router6Connections.add(new InetSocketAddress(DEFAULT_HOST, router3ReceivePort));
		router6Connections.add(new InetSocketAddress(DEFAULT_HOST, router8ReceivePort));
		ArrayList<Integer> router6Latencies = new ArrayList<Integer>();
		router6Latencies.add(router3Latencies.get(2));
		router6Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router6Terminal = new Terminal("Router 6");
		Thread Router6 = new Thread(new Router(router6Terminal, router6SendPort, router6ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router6Connections, router6Latencies, null, null));
		Router6.start();
		
		//Router7
		ArrayList<InetSocketAddress> router7Connections = new ArrayList<InetSocketAddress>();
		router7Connections.add(new InetSocketAddress(DEFAULT_HOST, router4ReceivePort));
		router7Connections.add(new InetSocketAddress(DEFAULT_HOST, router5ReceivePort));
		router7Connections.add(new InetSocketAddress(DEFAULT_HOST, router8ReceivePort));
		ArrayList<Integer> router7Latencies = new ArrayList<Integer>();
		router7Latencies.add(router4Latencies.get(2));
		router7Latencies.add(router5Latencies.get(2));
		router7Latencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		Terminal router7Terminal = new Terminal("Router 7");
		Thread Router7 = new Thread(new Router(router7Terminal, router7SendPort, router7ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router7Connections, router7Latencies, null, null));
		Router7.start();
		
		//Router8
		ArrayList<InetSocketAddress> router8RouterConnections = new ArrayList<InetSocketAddress>();
		ArrayList<InetSocketAddress> router8EndUserConnections = new ArrayList<InetSocketAddress>();
		router8EndUserConnections.add(new InetSocketAddress(DEFAULT_HOST, user2ReceivePort));
		router8RouterConnections.add(new InetSocketAddress(DEFAULT_HOST, router6ReceivePort));
		router8RouterConnections.add(new InetSocketAddress(DEFAULT_HOST, router7ReceivePort));
		ArrayList<Integer> router8RouterLatencies = new ArrayList<Integer>();
		ArrayList<Integer> router8EndUserLatencies = new ArrayList<Integer>();
		router8EndUserLatencies.add(latencyGenerator.nextInt(MAX_LATENCY));
		router8RouterLatencies.add(router6Latencies.get(1));
		router8RouterLatencies.add(router7Latencies.get(2));
		Terminal router8Terminal = new Terminal("Router 8");
		Thread Router8 = new Thread(new Router(router8Terminal, router8SendPort, router8ReceivePort,
				new InetSocketAddress(DEFAULT_HOST, controllerSendPort), new InetSocketAddress(DEFAULT_HOST, controllerReceivePort),
				router8RouterConnections, router8RouterLatencies, router8EndUserConnections, router8EndUserLatencies));
		Router8.start();
		
		//User1
		Terminal user1Terminal = new Terminal("User1");
		Thread User1 = new Thread(new EndUser(user1Terminal, user1SendPort, user1ReceivePort, 
				new InetSocketAddress(DEFAULT_HOST, user2ReceivePort), new InetSocketAddress(DEFAULT_HOST, router1ReceivePort)));
		User1.start();
		
		//User2
		Terminal user2Terminal = new Terminal("User2");
		Thread User2 = new Thread(new EndUser(user2Terminal, user2SendPort, user2ReceivePort, 
				new InetSocketAddress(DEFAULT_HOST, user1ReceivePort), new InetSocketAddress(DEFAULT_HOST, router8ReceivePort)));
		User2.start();
	}

}
