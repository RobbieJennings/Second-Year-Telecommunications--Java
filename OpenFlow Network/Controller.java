package cs.tcd.ie;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tcdIO.Terminal;

public class Controller extends Node implements Runnable {
	
	Terminal terminal;
	DatagramSocket sendSocket;
	DatagramSocket receiveSocket;
	ArrayList<InetSocketAddress> route;
	HashMap<InetSocketAddress, DijkstraNode> unsettledNodes;
	HashMap<InetSocketAddress, DijkstraNode> settledNodes;
	
	Controller(Terminal terminal, int sendPort, int receivePort) {
		try {
			this.terminal = terminal;
			this.sendSocket = new DatagramSocket(sendPort);
			this.receiveSocket = new DatagramSocket(receivePort);
			this.unsettledNodes = new HashMap<InetSocketAddress, DijkstraNode>();
			this.settledNodes = new HashMap<InetSocketAddress, DijkstraNode>();
			this.route = new ArrayList<InetSocketAddress>();
			terminal.println("" + sendPort);
			terminal.println("" + receivePort);
			socket = receiveSocket;
			listener.go();
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void onReceipt(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		if(packetData[0] == -1) {
			DijkstraNode node = new DijkstraNode(getRouterAddressFromPacket(packet), null, Integer.MAX_VALUE,
					getRouterConnectionsFromPacket(packet), getRouterLatenciesFromPacket(packet),
					getEndUserConnectionsFromPacket(packet), getEndUserLatenciesFromPacket(packet));
			unsettledNodes.put(node.routerAddress, node);
		}
		else {
			terminal.println("Determining route");
			unsettledNodes.clear();
			settledNodes.clear();
			route.clear();
			InetSocketAddress routerAddress = getRouterAddressFromPacket(packet);
			InetSocketAddress destinationAddress = getDestinationAddressFromPacket(packet);
			
			DijkstraNode source = new DijkstraNode(routerAddress, null, 0,
					getRouterConnectionsFromPacket(packet), getRouterLatenciesFromPacket(packet),
					getEndUserConnectionsFromPacket(packet), getEndUserLatenciesFromPacket(packet));
			settledNodes.put(routerAddress, source);
			
			calculateRoute(routerAddress, destinationAddress, routerAddress);
			
			for(int i = 1; i <= route.size() - 1; i++ ) {
				int nextStopPort = route.get(i-1).getPort();
				byte[] nextStopPortData = ByteBuffer.allocate(4).putInt(nextStopPort).array();
				
				DatagramPacket dataFromController = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
				byte[] header = null;
				byte[] buffer = null;
				
				header = new byte[PacketContent.HEADERLENGTH];
				header[0] = -1;
				header[1] = -1;
				
				byte[] destinationPortData = ByteBuffer.allocate(4).putInt(destinationAddress.getPort()).array();
				header[2] = destinationPortData[0];
				header[3] = destinationPortData[1];
				header[4] = destinationPortData[2];
				header[5] = destinationPortData[3];
				
				header[6] = nextStopPortData[0];
				header[7] = nextStopPortData[1];
				header[8] = nextStopPortData[2];
				header[9] = nextStopPortData[3];
				
				buffer = new byte[header.length];
				
				System.arraycopy(header, 0, buffer, 0, header.length);
				dataFromController = new DatagramPacket(buffer, buffer.length, 
						route.get(i));
				terminal.println("Sending route to " + route.get(i).getPort());
				try {
					sendSocket.send(dataFromController);
				} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
	
	public InetSocketAddress getDestinationAddressFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		byte[] destinationPortData = new byte[4];
		destinationPortData[0] = packetData[6];
		destinationPortData[1] = packetData[7];
		destinationPortData[2] = packetData[8];
		destinationPortData[3] = packetData[9];
		int destinationPort = ByteBuffer.wrap(destinationPortData).getInt();
		InetSocketAddress destinationAddress = new InetSocketAddress(DEFAULT_HOST, destinationPort);
		return destinationAddress;
	}
	
	public  InetSocketAddress getRouterAddressFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		byte[]routerPortData = new byte[4];
		routerPortData[0] = packetData[12];
		routerPortData[1] = packetData[13];
		routerPortData[2] = packetData[14];
		routerPortData[3] = packetData[15];
		int routerPort = ByteBuffer.wrap(routerPortData).getInt();
		InetSocketAddress routerAddress = new InetSocketAddress(DEFAULT_HOST, routerPort);
		return routerAddress;
	}
	
	public  ArrayList<InetSocketAddress> getRouterConnectionsFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		int numberOfRouterConnections = packetData[10];
		ArrayList<InetSocketAddress> connections = new ArrayList<InetSocketAddress>();
		for(int i = 0; i < numberOfRouterConnections; i++) {
			byte[] connectionPortData = new byte[4];
			connectionPortData[0] = packetData[i*8 + 16];
			connectionPortData[1] = packetData[i*8 + 17];
			connectionPortData[2] = packetData[i*8 + 18];
			connectionPortData[3] = packetData[i*8 + 19];
			int connectionPort = ByteBuffer.wrap(connectionPortData).getInt();
			InetSocketAddress connection = new InetSocketAddress(DEFAULT_HOST, connectionPort);
			connections.add(connection);
		}
		return connections;
	}
	
	public  ArrayList<Integer> getRouterLatenciesFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		int numberOfRouterConnections = packetData[10];
		ArrayList<Integer> latencies = new ArrayList<Integer>();
		for(int i = 0; i < numberOfRouterConnections; i++) {
			byte[] latencyData = new byte[4];
			latencyData[0] = packetData[i*8 + 20];
			latencyData[1] = packetData[i*8 + 21];
			latencyData[2] = packetData[i*8 + 22];
			latencyData[3] = packetData[i*8 + 23];
			int latency = ByteBuffer.wrap(latencyData).getInt();
			latencies.add(latency);
		}
		return latencies;
	}
	
	public  ArrayList<InetSocketAddress> getEndUserConnectionsFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		int numberOfRouterConnections = packetData[10];
		int numberOfEndUserConnections = packetData[11];
		ArrayList<InetSocketAddress> connections = new ArrayList<InetSocketAddress>();
		for(int i = numberOfRouterConnections * 8; i < (numberOfRouterConnections + numberOfEndUserConnections) * 8; i+=8) {
			byte[] connectionPortData = new byte[4];
			connectionPortData[0] = packetData[i + 16];
			connectionPortData[1] = packetData[i + 17];
			connectionPortData[2] = packetData[i + 18];
			connectionPortData[3] = packetData[i + 19];
			int connectionPort = ByteBuffer.wrap(connectionPortData).getInt();
			InetSocketAddress connection = new InetSocketAddress(DEFAULT_HOST, connectionPort);
			connections.add(connection);
		}
		return connections;
	}
	
	public  ArrayList<Integer> getEndUserLatenciesFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		int numberOfRouterConnections = packetData[10];
		int numberOfEndUserConnections = packetData[11];
		ArrayList<Integer> latencies = new ArrayList<Integer>();
		for(int i = numberOfRouterConnections * 8; i < (numberOfRouterConnections + numberOfEndUserConnections) * 8; i+=8) {
			byte[] latencyData = new byte[4];
			latencyData[0] = packetData[i + 20];
			latencyData[1] = packetData[i + 21];
			latencyData[2] = packetData[i + 22];
			latencyData[3] = packetData[i + 23];
			int latency = ByteBuffer.wrap(latencyData).getInt();
			latencies.add(latency);
		}
		return latencies;
	}
	
	public void requestInfoFromRouter(InetSocketAddress routerAddress) {
		DatagramPacket request;
		byte[] header= null;
		byte[] buffer= null;

		header= new byte[PacketContent.HEADERLENGTH];
		header[0] = 0;
		header[1] = 0;
		header[2] = 0;
		header[3] = 0;
		header[4] = 0;
		header[5] = 0;	
		header[6] = 0;
		header[7] = 0;
		header[8] = 0;
		header[9] = 0;
		
		buffer= new byte[header.length];
		System.arraycopy(header, 0, buffer, 0, header.length);

		request= new DatagramPacket(buffer, buffer.length, routerAddress);
		try {
			sendSocket.send(request);
		} catch (IOException e) {e.printStackTrace();}
		
		DatagramPacket response= new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
		try {
			receiveSocket.receive(response);
		} catch (IOException e) {e.printStackTrace();}
		this.onReceipt(response);
	}
	
	public void calculateRoute(InetSocketAddress routerAddress, InetSocketAddress destinationAddress, InetSocketAddress sourceAddress) {
//		/*
//		 * preconfigured routes
//		 */
//		if(sourceAddress.getPort() == 40003) {
//			if(destinationAddress.getPort() == 40019) {
//				
//			}
//			if(destinationAddress.getPort() == 40021) {
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40017));
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40013));
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40007));
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40003));
//			}
//		}
//		if(sourceAddress.getPort() == 40017) {
//			if(destinationAddress.getPort() == 40019) {
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40003));
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40007));
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40013));
//				route.add(new InetSocketAddress(DEFAULT_HOST, 40017));
//			}
//			if(destinationAddress.getPort() == 40021) {
//				
//			}
//		}
		
		DijkstraNode node = settledNodes.get(routerAddress);
		if(node.getEndUserConnectionAddresses().contains(destinationAddress)) {
			route.add(routerAddress);
			for(DijkstraNode i = settledNodes.get(routerAddress); i.getRouterAddress() != sourceAddress;) {
				route.add(i.getParentAddress());
				i = settledNodes.get(i.getParentAddress());
			}
		}
		else {
			for(int i = 0; i < node.getRouterConnectionAddresses().size(); i++) {
				if(!settledNodes.containsKey(node.getRouterConnectionAddresses().get(i)) && !unsettledNodes.containsKey(node.getRouterConnectionAddresses().get(i))) {
					requestInfoFromRouter(node.getRouterConnectionAddresses().get(i));
					unsettledNodes.get(node.getRouterConnectionAddresses().get(i)).setRouteTime(node.getRouteTime() + node.getRouterLatencies().get(i));
					unsettledNodes.get(node.getRouterConnectionAddresses().get(i)).setParentAddress(node.getRouterAddress());
				}
			}
			int nextMinimumDistance = Integer.MAX_VALUE;
			InetSocketAddress nextStop = null;
			Iterator<HashMap.Entry <InetSocketAddress, DijkstraNode>> entries = settledNodes.entrySet().iterator();
			while (entries.hasNext()) {
				HashMap.Entry<InetSocketAddress, DijkstraNode> entry = entries.next();
				for(int i = 0; i < entry.getValue().getRouterLatencies().size(); i++) {
					if(unsettledNodes.containsKey(entry.getValue().getRouterConnectionAddresses().get(i))) {
						DijkstraNode tentativeNode = unsettledNodes.get(entry.getValue().getRouterConnectionAddresses().get(i));
						int distance = tentativeNode.getRouteTime() + entry.getValue().getRouterLatencies().get(i);
						if(!settledNodes.containsKey(tentativeNode)) {
							if(distance < tentativeNode.getRouteTime()) {
								tentativeNode.setRouteTime(distance);
								tentativeNode.setParentAddress(entry.getKey());
							}
							if(tentativeNode.getRouteTime() < nextMinimumDistance) {
								nextMinimumDistance = tentativeNode.getRouteTime();
								nextStop = tentativeNode.getRouterAddress();
							}
						}
					}
				}
			}
			settledNodes.put(nextStop, unsettledNodes.get(nextStop));
			unsettledNodes.remove(nextStop);
			calculateRoute(nextStop, destinationAddress, sourceAddress);
		}
	}

	@Override
	public void run() {
		terminal.println("Waiting for contact from router");
	}

	private class DijkstraNode {
		
		InetSocketAddress routerAddress;
		InetSocketAddress parentAddress;
		int routeTime;
		ArrayList<InetSocketAddress> routerConnectionAddresses;
		ArrayList<Integer> routerLatencies;
		ArrayList<InetSocketAddress> endUserConnectionAddresses;
		//ArrayList<Integer> endUserLatencies;
		
		DijkstraNode(InetSocketAddress routerAddress, InetSocketAddress parentAddress, int routeTime,
				ArrayList<InetSocketAddress> routerConnectionAddresses, ArrayList<Integer> routerLatencies,
				ArrayList<InetSocketAddress> endUserConnectionAddresses, ArrayList<Integer> endUserLatencies) {
			this.routerAddress = routerAddress;
			this.parentAddress = parentAddress;
			this.routeTime = routeTime;
			this.routerConnectionAddresses = routerConnectionAddresses;
			this.routerLatencies = routerLatencies;
			this.endUserConnectionAddresses = endUserConnectionAddresses;
			//this.endUserLatencies = endUserLatencies;
		}
		
		InetSocketAddress getRouterAddress() {
			return this.routerAddress;
		}
		
		InetSocketAddress getParentAddress() {
			return this.parentAddress;
		}
		
		void setParentAddress(InetSocketAddress parentAddress) {
			this.parentAddress = parentAddress;
		}
		
		int getRouteTime() {
			return this.routeTime;
		}
		
		void setRouteTime(int routeTime) {
			this.routeTime = routeTime;
		}
		
		ArrayList<InetSocketAddress> getRouterConnectionAddresses() {
			return this.routerConnectionAddresses;
		}
		
		ArrayList<Integer> getRouterLatencies() {
			return this.routerLatencies;
		}
		
		ArrayList<InetSocketAddress> getEndUserConnectionAddresses() {
			return this.endUserConnectionAddresses;
		}
		
		//ArrayList<Integer> getEndUserLatencies() {
		//	return this.endUserLatencies;
		//}
	}
}
