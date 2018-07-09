package cs.tcd.ie;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import tcdIO.Terminal;

public class EndUser extends Node implements Runnable{

	Terminal terminal;
	DatagramSocket sendSocket;
	DatagramSocket receiveSocket;
	InetSocketAddress destinationAddress;
	InetSocketAddress routerAddress;
	DatagramPacket packet;

	EndUser(Terminal terminal, int sendPort, int receivePort,
			InetSocketAddress destinationAddress, InetSocketAddress routerAddress ) {
		try {
				this.terminal = terminal;
				this.sendSocket = new DatagramSocket(sendPort);
				this.receiveSocket = new DatagramSocket(receivePort);
				this.socket = receiveSocket;
				this.destinationAddress = destinationAddress;
				this.routerAddress = routerAddress;
				terminal.println("" + sendPort);
				terminal.println("" + receivePort);
				listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	@Override
	public void onReceipt(DatagramPacket packet) {
		StringContent content = new StringContent(packet);
		terminal.print("\n<- " + content.toString() + "\n-> ");
	}

	@Override
	public void run() {
		boolean finished = false;
		while(!finished)
		{
			packet= null;
	
			byte[] payload= null;
			byte[] header= null;
			byte[] buffer= null;
		
			payload= (terminal.readString("-> ")).getBytes();
			
			header= new byte[PacketContent.HEADERLENGTH];
				
			byte[] portData = ByteBuffer.allocate(4).putInt(receiveSocket.getLocalPort()).array();
			byte[] destinationportData = ByteBuffer.allocate(4).putInt(destinationAddress.getPort()).array();
			
			header[0] = 0;
			header[1] = 0;
			
			header[2] = portData[0];
			header[3] = portData[1];
			header[4] = portData[2];
			header[5] = portData[3];
			
			header[6] = destinationportData[0];
			header[7] = destinationportData[1];
			header[8] = destinationportData[2];
			header[9] = destinationportData[3];
			
			buffer= new byte[header.length + payload.length];
			System.arraycopy(header, 0, buffer, 0, header.length);
			System.arraycopy(payload, 0, buffer, header.length, payload.length);
			
			packet = new DatagramPacket(buffer, buffer.length, routerAddress);
			try {
				socket.send( packet );
			} catch (IOException e) {e.printStackTrace();}
		}		
	}

}
