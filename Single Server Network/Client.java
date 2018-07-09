/**
 * 
 */
package cs.tcd.ie;

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import tcdIO.*;

/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node {
	static final int DEFAULT_PORT = 40000;
	static final int DEFAULT_DST_PORT = 50000;
	static final int DEFAULT_GATEWAY_PORT = 40500;
	static final String DEFAULT_DST_NODE = "localhost";	
	static final String DEFAULT_GATEWAY_NODE = "localhost";	
	static final byte ACK = 0;
	
	Terminal terminal;
	InetSocketAddress dstAddress;
	InetSocketAddress gatewayAddress;
	
	DatagramPacket packet;
	boolean readyToSend;
	byte frameNumber;
	
	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(Terminal terminal, String gatewayHost, int gatewayPort, String dstHost, int dstPort, int port) {
		try {
			readyToSend = true;
			frameNumber = 0;
			int count = 0;
			boolean portFound = false;
			while(!portFound) {
				try {
					this.terminal= terminal;
					gatewayAddress = new InetSocketAddress(gatewayHost, gatewayPort);
					dstAddress= new InetSocketAddress(dstHost, dstPort);
					socket= new DatagramSocket(port + count);
					socket.setSoTimeout( 1000 );
					portFound = true;
				}
				catch(java.net.BindException e) {count++;}
			}
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	
	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void onReceipt(DatagramPacket packet) {
		StringContent content= new StringContent(packet);
		terminal.println( content.toString() );
		byte[] packetContent = packet.getData();
		if(packetContent[1] == ACK && packetContent[0] != frameNumber) {
			frameNumber = packetContent[0];
			readyToSend = true;
		}
		try {
			this.start();
		} catch (java.lang.Exception e) {e.printStackTrace();}
	}

	
	/**
	 * Sender Method
	 * 
	 */
	public void start() throws Exception {
		while(readyToSend) {
			readyToSend = false;
			packet= null;

			byte[] payload= null;
			byte[] header= null;
			byte[] buffer= null;
		
			payload= (terminal.readString("String to send: ")).getBytes();
			
			header= new byte[PacketContent.HEADERLENGTH];
				
			byte[] portArray = new byte[4];
			portArray[0] = (byte) (socket.getLocalPort() >> 24); 
			portArray[1] = (byte) (socket.getLocalPort() >> 16); 
			portArray[2] = (byte) (socket.getLocalPort() >> 8); 
			portArray[3] = (byte) socket.getLocalPort(); 
				
			byte[] dstPortArray = new byte[4];
			dstPortArray[0] = (byte) (dstAddress.getPort() >> 24);
			dstPortArray[1] = (byte) (dstAddress.getPort() >> 16);
			dstPortArray[2] = (byte) (dstAddress.getPort() >> 8);
			dstPortArray[3] = (byte) dstAddress.getPort();
			
			header[0] = (byte) frameNumber;
			header[1] = (byte) ACK;
			
			header[2] = portArray[0];
			header[3] = portArray[1];
			header[4] = portArray[2];
			header[5] = portArray[3];
			
			header[6] = dstPortArray[0];
			header[7] = dstPortArray[1];
			header[8] = dstPortArray[2];
			header[9] = dstPortArray[3];
			
			buffer= new byte[header.length + payload.length];
			System.arraycopy(header, 0, buffer, 0, header.length);
			System.arraycopy(payload, 0, buffer, header.length, payload.length);
			
			packet= new DatagramPacket(buffer, buffer.length, gatewayAddress);

			while(!readyToSend) {
				DatagramPacket response= new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
				terminal.println("Sending packet...");
				boolean receivedAck = false;
				while( !receivedAck ) {
					socket.send( packet );
					try {
						socket.receive( response );
						receivedAck = true;
					}
					catch ( SocketTimeoutException e ) {terminal.println("Acknowledgement timed out, resending...");}
				}
				this.onReceipt( response );
			}
		}
	}


	/**
	 * Test method
	 * 
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Client");		
			(new Client(terminal, DEFAULT_GATEWAY_NODE, DEFAULT_GATEWAY_PORT, DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_PORT)).start();
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}