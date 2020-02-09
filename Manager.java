import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class Manager
{
	//BEACON Object
	static class BEACON
	{
		int id, startUpTime, timeInterval, cmdPort;
		char ip[];

		public BEACON() {}

		public BEACON(int a, int b, int c, char z[], int d)
		{
			id = a;
			startUpTime = b;
			timeInterval = c;
			ip = z;
			cmdPort = d;
		}
	}

	//Main method for the Manager class
	public static void main(String args[])
	{
		try
		{
			listenUDP();
		} 
		catch (Exception e)
		{
			System.out.println("Error " + e);
		}
	}

	public static void listenUDP() throws Exception
	{
		byte[] buffer = new byte[1024];
		DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

		DatagramSocket ds = new DatagramSocket(7000);
		System.out.println("Connected to port!");
		System.out.println("Application suspended, Receiving...");
		for(;;)
		{
			ds.receive(incoming);
			byte[] data = new byte[incoming.getLength()];
			System.arraycopy(incoming.getData(), 0, data, 0, data.length);
			BeaconListner processor = new BeaconListner(data);
			processor.start();
		}		
	}

	static class BeaconListner extends Thread
	{
		String data;
		
		public BeaconListner() {}

		public BeaconListner(byte[] input)
		{
			data = new String(input);
		}

		public void run()
		{
			System.out.println("Received buffer : " + data);
		}
	}
}
