package cs.tcd.ie;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import tcdIO.Terminal;

public class Server extends Node {
	static final int DEFAULT_PORT = 50000;
	static final int ACK = 0;

	Terminal terminal;
	
	/*
	 * 
	 */
	Server(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			StringContent content= new StringContent(packet);
			terminal.println(content.toString());
			
			byte[] packetContent = packet.getData();
			byte frameNumber = packetContent[0];
			byte ackNumber;
			if( frameNumber == 0 )
			{
				ackNumber = 1;
			}
			else
			{
				ackNumber = 0;
			}

			DatagramPacket response;
			byte[] payload= null;
			byte[] header= null;
			byte[] buffer= null;
			
			payload = new String( "ACKNOWLEDGEMENT " + ackNumber ).getBytes();

			header= new byte[PacketContent.HEADERLENGTH];
			
			header[0] = ackNumber;
			header[1] = ACK;
			
			header[2] = packetContent[6];
			header[3] = packetContent[7];
			header[4] = packetContent[8];
			header[5] = packetContent[9];
			
			header[6] = packetContent[2];
			header[7] = packetContent[3];
			header[8] = packetContent[4];
			header[9] = packetContent[5];
			
			buffer= new byte[header.length + payload.length];
			System.arraycopy(header, 0, buffer, 0, header.length);
			System.arraycopy(payload, 0, buffer, header.length, payload.length);

			response= new DatagramPacket(buffer, buffer.length, packet.getSocketAddress());
			socket.send(response);
		}
		catch(Exception e) {e.printStackTrace();}
	}

	
	public synchronized void start() throws Exception {
		terminal.println("Waiting for contact");
		this.wait();
	}
	
	/*
	 * 
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Server");
			(new Server(terminal, DEFAULT_PORT)).start();
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}