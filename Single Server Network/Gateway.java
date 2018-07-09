package cs.tcd.ie;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import tcdIO.Terminal;

public class Gateway extends Node {
	
	static final int DEFAULT_PORT = 40500;

	Terminal terminal;
	
	Gateway(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			this.socket = new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	@Override
	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			StringContent content= new StringContent(packet);
			terminal.println(content.toString());
			
			byte[] packetData = packet.getData();
			byte[] dstPortData = new byte[4];
			dstPortData[0] = packetData[6];
			dstPortData[1] = packetData[7];
			dstPortData[2] = packetData[8];
			dstPortData[3] = packetData[9];
			int dstPort = ByteBuffer.wrap(dstPortData).getInt();
			
			terminal.println("Forwarding packet...");
			packet.setPort(dstPort);;
			socket.send(packet);
			terminal.println("Packet forwarded");
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	public synchronized void start() throws Exception {
		terminal.println("Waiting for contact");
		this.wait();
	}
	
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Gateway");
			(new Gateway(terminal, DEFAULT_PORT)).start();
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
