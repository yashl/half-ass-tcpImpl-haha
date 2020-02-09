import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import java.util.concurrent.ArrayBlockingQueue;

public class Manager
{
	//BEACON Object
	static class BEACON
	{
		int id, startUpTime, timeInterval, cmdPort, last;
		String ip;

		public BEACON() {}

		public BEACON(int a, int b, int c, String z, int d, int e)
		{
			id = a;
			startUpTime = b;
			timeInterval = c;
			ip = z;
			cmdPort = d;
			last = e;
		}

		public int getBeaconID()
		{
			return id;
		}

		public int getBeaconInterval()
		{
			return timeInterval;
		}

		public int getlast()
		{
			return last;
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
		ArrayBlockingQueue<BEACON> list = new ArrayBlockingQueue<BEACON>(80);
		AgentMonitor monitor = new AgentMonitor(list);
		monitor.start();

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
			BeaconListner processor = new BeaconListner(data, list);
			processor.start();
		}		
	}

	static class AgentMonitor extends Thread
	{
		ArrayBlockingQueue<BEACON> list;

		public AgentMonitor() {}
		public AgentMonitor(ArrayBlockingQueue<BEACON> beacons)
		{
			list = beacons;
		}
		public void run()
		{
			int i = 0;
			for(;;)
			{
				if(list.size() > i)
				{
					i++;
					System.out.println("New Beacon Detected!");
				}
			}
			
		}

	}

	static class BeaconListner extends Thread
	{
		String data;
		ArrayBlockingQueue<BEACON> list;

		public BeaconListner() {}

		public BeaconListner(byte[] input, ArrayBlockingQueue<BEACON> beacons)
		{
			data = new String(input);
			list = beacons;
		}

		public void run()
		{
			BEACON b = createData();

			BEACON[] array = list.toArray(new BEACON[0]);

			int found = 0;
			for(BEACON i : array)
			{
				if(i.id == b.id)
				{
					i.last = b.last;
					found = 1;
				}
			}

			if(found == 0)
				list.add(b);

		}

		public BEACON createData()
		{
			String[] beacon_s = data.split(",");

			int id = Integer.parseInt(beacon_s[0]);
			int startTime = Integer.parseInt(beacon_s[1]);
			int timeInterval = Integer.parseInt(beacon_s[2]);
			int cmdPort = Integer.parseInt(beacon_s[4].trim());
			int last = (int)System.currentTimeMillis()/1000;

			System.out.println("Last Received: " + last);

			BEACON b = new BEACON(id,startTime,timeInterval,beacon_s[3],cmdPort,last);
			return b;
		}
	}
}
