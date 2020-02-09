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
		boolean alive;
		String ip;

		public BEACON() {}

		public BEACON(int a, int b, int c, String z, int d, int e, boolean f)
		{
			id = a;
			startUpTime = b;
			timeInterval = c;
			ip = z;
			cmdPort = d;
			last = e;
			alive = f;
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

		public void setAlive(boolean bool)
		{
			alive = bool;
		}

		public boolean isAlive()
		{
			return alive;
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
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e) { }
				contains();
			}
		}

		public void contains()
		{
			BEACON[] array = list.toArray(new BEACON[0]);
			
			int found = 0;
			for(BEACON i : array)
			{
				int currentTime = (int)System.currentTimeMillis()/1000;
				int cond = (currentTime - i.last) * 2;
				if(cond > 8  && i.isAlive())
				{
					System.out.println(i.id + ": agent Died!");
					i.setAlive(false);
				}
				if(cond < 8 && !i.isAlive())
				{
					System.out.println(i.id + ": agent resurrected!");
					i.setAlive(true);
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
			{
				list.add(b);
				System.out.println(b.id + ": new agent detected!");
			}
		}

		public BEACON createData()
		{
			String[] beacon_s = data.split(",");

			int id = Integer.parseInt(beacon_s[0]);
			int startTime = Integer.parseInt(beacon_s[1]);
			int timeInterval = Integer.parseInt(beacon_s[2]);
			int cmdPort = Integer.parseInt(beacon_s[4].trim());
			int last = (int)System.currentTimeMillis()/1000;

			BEACON b = new BEACON(id,startTime,timeInterval,beacon_s[3],cmdPort,last, true);
			return b;
		}
	}
}
