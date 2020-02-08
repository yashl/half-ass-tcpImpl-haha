import java.net.*;
import java.io.*;

public class Manager
{
	static class BEACON
	{
		int id, startUpTime, timeInterval, cmdPort;
		char ip[];

		public BEACON()
		{
			//default constructor
		}

		public BEACON(int a, int b, int c, char z[], int d)
		{
			id = a;
			startUpTime = b;
			timeInterval = c;
			ip = z;
			cmdPort = d;
		}
	}


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
			DataProcessor processor = new DataProcessor(data);
			processor.start();
		}		
	}

	static class DataProcessor extends Thread
	{
		byte[] data;
		
		public DataProcessor() {}

		public DataProcessor(byte[] input)
		{
			data = input;
		}

		public void run()
		{
			String s = new String(data);
			System.out.println(s);
		}

		public void test()
		{

		}
	}
}
