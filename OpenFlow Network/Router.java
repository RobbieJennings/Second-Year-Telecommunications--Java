package cs.tcd.ie;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import tcdIO.Terminal;

public class Router extends Node implements Runnable{

	Terminal terminal;
	DatagramSocket sendSocket;
	DatagramSocket receiveSocket;
	InetSocketAddress controllerSendAddress;
	InetSocketAddress controllerReceiveAddress;
	ArrayList<InetSocketAddress> routerConnectionAddresses;
	ArrayList<InetSocketAddress> endUserConnectionAddresses;
	ArrayList<Integer> routerLatencies;
	ArrayList<Integer> endUserLatencies;
	ArrayList<InetSocketAddress> destinations;
	ArrayList<InetSocketAddress> routeAddresses;
		
	Router(Terminal terminal, int sendPort, int receivePort, 
			InetSocketAddress controllerSendAddress, InetSocketAddress controllerReceiveAddress, ArrayList<InetSocketAddress> routerConnectionAddresses, 
			ArrayList<Integer> routerLatencies, ArrayList<InetSocketAddress> endUserConnectionAddresses, ArrayList<Integer> endUserLatencies) {
		try {
				this.terminal = terminal;
				this.sendSocket = new DatagramSocket(sendPort);
				this.receiveSocket = new DatagramSocket(receivePort);
				this.socket = receiveSocket;
				this.controllerSendAddress = controllerSendAddress;
				this.controllerReceiveAddress = controllerReceiveAddress;
				this.routerConnectionAddresses = routerConnectionAddresses;
				this.routerLatencies = 	routerLatencies;
				this.endUserConnectionAddresses = endUserConnectionAddresses;
				this.endUserLatencies = endUserLatencies;
				this.destinations = new ArrayList<InetSocketAddress>();
				this.routeAddresses = new ArrayList<InetSocketAddress>();
				terminal.println("" + sendPort);
				terminal.println("" + receivePort);
				listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void onReceipt(DatagramPacket packet) {		
		byte[] packetData = packet.getData();
		byte[] destinationPortData = new byte[4];
		destinationPortData[0] = packetData[6];
		destinationPortData[1] = packetData[7];
		destinationPortData[2] = packetData[8];
		destinationPortData[3] = packetData[9];
		int destinationPort = ByteBuffer.wrap(destinationPortData).getInt();
		InetSocketAddress destination = new InetSocketAddress(DEFAULT_HOST, destinationPort);
		
		if(packet.getPort() == controllerSendAddress.getPort()) {
			if(packetData[0] == -1) {
				terminal.println("Adding route information");
				addAddressesFromPacket(packet);
			}
			else {
				terminal.println("Sending connection data to controller");
				DatagramPacket dataForController = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
				byte[] header = null;
				byte[] buffer = null;
				byte[] payload = null;
				
				header = new byte[PacketContent.HEADERLENGTH];
				header[0] = -1;
				payload = putInfoInPayload();
				buffer= new byte[header.length + payload.length];
				
				System.arraycopy(header, 0, buffer, 0, header.length);
				System.arraycopy(payload, 0, buffer, header.length, payload.length);
				dataForController = new DatagramPacket(buffer, buffer.length, controllerReceiveAddress);
				dataForController.setSocketAddress(controllerReceiveAddress);
				try {
					sendSocket.send(dataForController);
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		else {
			StringContent content = new StringContent(packet);
			terminal.println(content.toString());
			if(endUserConnectionAddresses != null && endUserConnectionAddresses.contains(destination)) {
				terminal.println("sending to destination");
				packet.setSocketAddress(destination);
				try {
					sendSocket.send(packet);
				} catch (IOException e) {e.printStackTrace();}
			}
			else {
				int nextNode = destinations.indexOf(destination);
				if(nextNode == -1) {
					DatagramPacket controllerQuery = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
					byte[] portArray = ByteBuffer.allocate(4).putInt(receiveSocket.getLocalPort()).array();
					byte[] destinationPortArray = ByteBuffer.allocate(4).putInt(destination.getPort()).array();
					controllerQuery = null;
					
					byte[] header = null;
					byte[] buffer = null;
					byte[] payload = null;
					
					payload = putInfoInPayload();
					header = putPortsInHeader(portArray, destinationPortArray, routerConnectionAddresses.size());
					header[0] = 0;
					
					buffer= new byte[header.length + payload.length];
					System.arraycopy(header, 0, buffer, 0, header.length);
					System.arraycopy(payload, 0, buffer, header.length, payload.length);
					controllerQuery = new DatagramPacket(buffer, buffer.length, controllerReceiveAddress);
					
					try {
						sendSocket.send(controllerQuery);
					} catch (IOException e1) {e1.printStackTrace();}
					nextNode = destinations.indexOf(destination);
					
					DatagramPacket response= new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
					try {
						receiveSocket.receive(response);
					} catch (IOException e) {e.printStackTrace();}
					this.onReceipt(response);
					nextNode = destinations.indexOf(destination);
				}
				terminal.println("forwarding to " + routeAddresses.get(nextNode).getPort());
				packet.setSocketAddress(routeAddresses.get(nextNode));
				try {
					sendSocket.send(packet);
				} catch (IOException e1) {e1.printStackTrace();}
			}
		}
	}
	
	public void addAddressesFromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		byte[] destinationPortData = new byte[4];
		destinationPortData[0] = packetData[2];
		destinationPortData[1] = packetData[3];
		destinationPortData[2] = packetData[4];
		destinationPortData[3] = packetData[5];
		int destinationPort = ByteBuffer.wrap(destinationPortData).getInt();
		destinations.add(new InetSocketAddress(DEFAULT_HOST, destinationPort));
		
		byte[] routePortData = new byte[4];
		routePortData[0] = packetData[6];
		routePortData[1] = packetData[7];
		routePortData[2] = packetData[8];
		routePortData[3] = packetData[9];
		int routePort = ByteBuffer.wrap(routePortData).getInt();
		routeAddresses.add(new InetSocketAddress(DEFAULT_HOST, routePort));
	}
	
	public byte[] putInfoInPayload() {
		byte[] payload = null;
		int numberOfRouterConnections = routerConnectionAddresses.size();
		int numberOfEndUserConnections = 0;
		if(endUserConnectionAddresses != null) {
			numberOfEndUserConnections = endUserConnectionAddresses.size();
		}
		payload = new byte[6 + (numberOfRouterConnections + numberOfEndUserConnections) * 8];
		
		payload[0] = (byte) routerConnectionAddresses.size();
		payload[1] = (byte) numberOfEndUserConnections;
		
		byte[] portData = ByteBuffer.allocate(4).putInt(receiveSocket.getLocalPort()).array();
		payload[2] = portData[0];		
		payload[3] = portData[1];
		payload[4] = portData[2];
		payload[5] = portData[3];

		for(int i = 0; i < numberOfRouterConnections; i++) {
			byte[] connectionPortData = ByteBuffer.allocate(4).putInt(routerConnectionAddresses.get(i).getPort()).array();
			payload[i * 8 + 6] = connectionPortData[0];
			payload[i * 8 + 7] = connectionPortData[1];
			payload[i * 8 + 8] = connectionPortData[2];
			payload[i * 8 + 9] = connectionPortData[3];
			
			byte[] latencyData = ByteBuffer.allocate(4).putInt(routerLatencies.get(i)).array();
			payload[i * 8 + 10] = latencyData[0];
			payload[i * 8 + 11] = latencyData[1];
			payload[i * 8 + 12] = latencyData[2];
			payload[i * 8 + 13] = latencyData[3];
		}
		for(int i = 0; i < numberOfEndUserConnections; i++) {
			byte[] connectionPortData = ByteBuffer.allocate(4).putInt(endUserConnectionAddresses.get(i).getPort()).array();
			payload[i * 8 + 6 + 8 * routerConnectionAddresses.size()] = connectionPortData[0];
			payload[i * 8 + 7 + 8 * routerConnectionAddresses.size()] = connectionPortData[1];
			payload[i * 8 + 8 + 8 * routerConnectionAddresses.size()] = connectionPortData[2];
			payload[i * 8 + 9 + 8 * routerConnectionAddresses.size()] = connectionPortData[3];
			
			byte[] latencyData = ByteBuffer.allocate(4).putInt(endUserLatencies.get(i)).array();
			payload[i * 8 + 10 + 8 * routerConnectionAddresses.size()] = latencyData[0];
			payload[i * 8 + 11 + 8 * routerConnectionAddresses.size()] = latencyData[1];
			payload[i * 8 + 12 + 8 * routerConnectionAddresses.size()] = latencyData[2];
			payload[i * 8 + 13 + 8 * routerConnectionAddresses.size()] = latencyData[3];
		}
		return payload;
	}
	
	public byte[] putPortsInHeader(byte[] portData, byte[] destinationPortData, int numberOfRouterConnections) {
		byte[] header = null;
		header = new byte[PacketContent.HEADERLENGTH];
		
		header[0] = 0;
		header[1] = 0;
		
		header[2] = portData[0];
		header[3] = portData[1];
		header[4] = portData[2];
		header[5] = portData[3];
		
		header[6] = destinationPortData[0];
		header[7] = destinationPortData[1];
		header[8] = destinationPortData[2];
		header[9] = destinationPortData[3];
		return header;
	}

	@Override
	public void run() {
		terminal.println("Waiting for contact");
	}

}
