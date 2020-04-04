import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;

class LinuxDiskMonitorThread extends Thread
{
	private LinuxBroker broker;
	public LinuxDiskMonitorThread(LinuxBroker bro)
	{
		broker = bro;
	}
	public void run()
	{
		while (true)
		{
			try
			{
				
				broker.getDiskInfo(broker.diskName);
				if(broker.dFree<=1200)
				broker.initDisk();
				sleep(5000);
								
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
class SystemMonitorThread extends Thread
{
	private LinuxBroker broker;
	private String fileName;
	public SystemMonitorThread(LinuxBroker bro,String file)
	{
		broker = bro;
		fileName = file;
	}
	public void run()
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		SystemInformation sysInfo;
		String line="";
		int start=0,end=0,index=0;
		String[] data = new String[7];
		FileOutputStream fos=null;
		try
		{
			fos = new FileOutputStream(fileName);
		}
		catch(FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
			System.exit(0);
		}
		
		while (true)
		{
					
			try
			{
				
				System.out.println("Executing..");
				ObjectOutputStream out=null;
				p = Runtime.getRuntime().exec("atop "+1);
				
				ins = p.getInputStream();
				br =  new BufferedReader(new InputStreamReader(ins));
				boolean cpuFound=false;
				String parseString="";
				while((line = br.readLine()) != null)
				{
					if((line.indexOf("CPU |",0)>=0) && !cpuFound)			
					{
						//System.out.println(line);
						fos = new FileOutputStream(fileName);
						out=new ObjectOutputStream(fos);	
						start = line.indexOf("idle",0);
						String l = line.substring(start+4,line.indexOf('|',start+4)-2);
						l=l.trim();
						data[index++]=l;
						cpuFound=true;
					}
					else if( ( (line.indexOf("MEM",0)>=0) || (line.indexOf("SWP",0)>=0)) && cpuFound)
					{
					
						start = line.indexOf("tot",0);
						end = line.indexOf('|',start+4);
						String temp = line.substring(start+4,end-4);
						data[index++]=temp.trim();
						start = line.indexOf("free",end);
						end = line.indexOf('|',start+4);
						temp = line.substring(start+4,end-4);
						data[index++]=temp.trim();
						int used = Integer.parseInt(data[index-2])-Integer.parseInt(data[index-1]);
						data[index++]=""+used;
						if (index==7)
						{
							index=0;
							cpuFound=false;
							sysInfo = new SystemInformation(Long.parseLong(data[1])*1024,Long.parseLong(data[3])*1024,Long.parseLong(data[2])*1024,Long.parseLong(data[4])*1024,Long.parseLong(data[6])*1024,Long.parseLong(data[5])*1024,100-Integer.parseInt(data[0]),broker.dTotal,broker.dUsed,broker.dFree,broker.folderName,broker.brokerAddress);
							//LinuxBroker.writing=true;
							out.writeObject(sysInfo);
							out.flush();
							out.close();
							sleep(1000);
						}
					}
					
					
				
				}
						
			}
			catch(IOException e)
			{
				System.out.println(e);
				System.exit(0);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
		}
	
	}
}

/**
Linux Broker implements the same interface as WinBroker is Implementing.
But it uses the files "atop" and "df" to get the system related information instead
using the ProcessInfo,Cpuusage and DiskInfo.
*/

public class LinuxBroker implements BrokerInterface
{
	public static String brokerAddress;
	private static String serverAddress;
	public int dTotal,dFree,dUsed;
	public  String diskName;
	public   String folderName;
	private static String fileName;
	private SystemInformation sysInfo;
	public static boolean writing=false;
	public static void main(String args[])
	{	
		try
		{
			if (args.length==2)
			{
				serverAddress="rmi://";
				serverAddress+=args[0];
				fileName=args[1];
			}
			else
			{
				System.out.println("Proper Use: BrokerServer Address Output File Name");
				System.exit(0);
			}
			InetAddress address = InetAddress.getLocalHost();
			brokerAddress = address.getHostAddress();
			LinuxBroker server = new LinuxBroker();
			UnicastRemoteObject.exportObject(server);
			serverAddress+="/brokerserver";
			BrokerServerInterface in = (BrokerServerInterface) Naming.lookup(serverAddress);
			int result = in.register(brokerAddress,server);
			if (result==0)
			{
				System.out.println("A Broker was already running on this machine..");
				System.out.println("Now this Broker will take command");
				System.out.println("Broker service started successfully");
			}
			else if(result == 1)
			{
				System.out.println("Connected to the Broker Server...");
				System.out.println("Broker service started successfully");
				
			}
			else
			{
				System.out.println("Machine is not registered to participate on grid..");
				UnicastRemoteObject.unexportObject(server,true);
				System.exit(0);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public LinuxBroker() throws RemoteException
	{
		sysInfo=null;
		System.out.println("Calculating Free Space and Initializing Shared folder");
		initDisk();
		LinuxDiskMonitorThread diskThread = new LinuxDiskMonitorThread(this);
		diskThread.setDaemon(true);
		diskThread.start();
		SystemMonitorThread systemThread = new SystemMonitorThread(this,fileName);
		systemThread.setDaemon(true);
		systemThread.start();
	}
	synchronized public SystemInformation getSystemInformation() throws RemoteException
	{
		
		FileInputStream ins;
		ObjectInputStream in;
		
		try
		{
			
			/*do
			{
			   Thread.sleep(5);
			}
			while(writing);
			*/
			
			ins = new FileInputStream(fileName);
			in = new ObjectInputStream(ins);
			sysInfo = (SystemInformation)in.readObject();
		}
		catch(IOException e)
		{	
			e.printStackTrace();
		}
		catch(ClassNotFoundException cnfe)
		{
			
			return sysInfo;
		}
		catch(ClassCastException cce)
		{
			cce.printStackTrace();
			System.exit(0);
		}/*
		catch(InterruptedException ie)
		{
		}*/
		
		return sysInfo;
	}
	synchronized public ArrayList getProcesses() throws RemoteException
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		SystemInformation sysInfo;
		String line="";
		ArrayList array = new ArrayList();
		int start=0,end=0;
		
		try
		{
			p = Runtime.getRuntime().exec("atop");
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			int count=0,totalProcs=0;
			boolean found=false;
			while((line = br.readLine())!=null)
			{
				if( (line.indexOf("procs",0)>=0) && !found)
				{
					start = line.indexOf("procs",0);
					end   = line.indexOf('|',start+5);
					String temp = line.substring(start+5,end-1);
					
					totalProcs=Integer.parseInt(temp.trim());
				}
				
				if( ((line.indexOf("PID",0))>=0)||found)
				{
					found=true;
					line = parseLine(line);
					line+="\n";
					array.add(line);
					if( count++ == totalProcs)
					break;
				}
			
				
			}
			
		}	
		catch(IOException e)
		{
			System.out.println(e);
			return null;
		}
		catch(Exception e)
		{
			System.out.println(e);
			return null;
		}
		try
		{
			br.close();
			p.destroy();
		}
		catch(IOException ioe)
		{
		}
		return array;
	}
	synchronized public ArrayList getDetails() throws RemoteException
	{
		return null;
	}
	synchronized public void initDisk()
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		int start=0,end=0;
		String disk;
		File file;
		float maxFree = 0;
		try
		{
			p = Runtime.getRuntime().exec("df -h ");
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			while ((line = br.readLine())!=null)
			{
				if (line.indexOf("/dev/h",0)>=0)
				{
					start = line.indexOf(' ',0);
					end   = line.indexOf('G',start+1);
			
					if(end == -1)
					end   = line.indexOf('M',start+1);
					float tot = Float.parseFloat((line.substring(start+1,end)).trim());
					start=end;
					start = line.indexOf(' ',start);
					end   = line.indexOf('G',start+1);
				
					if(end==-1)
					end   = line.indexOf('M',start+1);
				
					float used = Float.parseFloat( (line.substring(start+1,end)).trim()) ;
				
					float free = tot-used;
							
					start = end;
					if(free>maxFree)
					{
						start = line.indexOf('/',start);
						diskName = line.substring(start,line.length());
						dFree=(int)(free*1024);
						dTotal=(int)(tot*1024);
						dUsed=(int)(used*1024);
						maxFree=free;
				
					}
							
				}
			
			}//while
			br.close();
			p.destroy();
					
		}//try	
		catch(Exception e)
		{
			System.out.println(e);
		}
		if(!diskName.equals("/"))
		folderName=diskName+File.separator+"GridShared";
		else
		folderName=diskName+"GridShared";
		file = new File(folderName);
		file.mkdir();
			
	}
	synchronized public void getDiskInfo(String disk)
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		int start=0,end=0;
		File file;
		
		try
		{
			p = Runtime.getRuntime().exec("df -h "+disk);
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			while ((line = br.readLine())!=null)
			{
				if (line.indexOf("/dev/h",0)>=0)
				{
					start = line.indexOf(' ',0);
					end   = line.indexOf('G',start+1);
			
					if(end == -1)
					end   = line.indexOf('M',start+1);
					float tot = Float.parseFloat((line.substring(start+1,end)).trim());
					start=end;
					start = line.indexOf(' ',start);
					end   = line.indexOf('G',start+1);
				
					if(end==-1)
					end   = line.indexOf('M',start+1);
					float used = Float.parseFloat( (line.substring(start+1,end)).trim()) ;
					float free = tot-used;
					dFree=(int)(free*1024);
					dTotal=(int)(tot*1024);
					dUsed=(int)(used*1024);
					start = end;
					start = line.indexOf('/',start);
					String dName = line.substring(start,line.length());
					if(!dName.equals(disk))
					{
						initDisk();
					}
					
				
				}
			
			}//while
			br.close();
			p.destroy();
					
		}//try	
		catch(Exception e)
		{
			System.out.println(e);
		}
		if(!diskName.equals("/"))
		folderName=diskName+File.separator+"GridShared";
		else
		folderName=diskName+"GridShared";
		file = new File(folderName);
		file.mkdir();
			
	}
	synchronized public String getSharedFolder() throws RemoteException
	{
		return folderName;
	}
	private String parseLine(String line)
	{
		int start=0;
		int end=0;
		String result="";
		String temp="";
		do
		{
			end = line.indexOf(' ',start);
			if(end!=-1)
			{
				temp = line.substring(start,end);
				result=result+temp+"  ";
			}
			else
			{
				temp = line.substring(start,line.length());
				result=result+temp;
			}
			start=end+1;
			
		}while(end!=-1);
		return result;
	}
	synchronized public int getCpuPercent() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			{
				return info.cpu;
			}
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public int getMemPercent() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return ((int)((info.usedPhysicalMem*100)/info.physicalMem));
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
		
		
	}
	synchronized public int getVMemPercent() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return ((int)((info.usedVirtualMem*100)/(info.virtualMem+1)));
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}

	synchronized public int getDiskPercent() throws RemoteException
	{
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return ((int)((info.usedDisk*100)/info.totalDisk));
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public int getFreeDisk() throws RemoteException
	{
		return dFree*1024;
	}
	synchronized public int getFreeDisk(String diskName) throws RemoteException
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		int start=0,end=0;
		String disk;
		File file;
		long maxFree = 0;
		try
		{
			p = Runtime.getRuntime().exec("df -h "+diskName);
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			while ((line = br.readLine())!=null)
			{
				if (line.indexOf("/dev/h",0)>=0)
				{
					start = line.indexOf(' ',0);
					end   = line.indexOf('G',start+1);
			
					if(end == -1)
					end   = line.indexOf('M',start+1);
					float tot = Float.parseFloat((line.substring(start+1,end)).trim());
					start=end;
					start = line.indexOf(' ',start);
					end   = line.indexOf('G',start+1);
					if(end==-1)
					end   = line.indexOf('M',start+1);
					float used = Float.parseFloat( (line.substring(start+1,end)).trim()) ;
					float free = tot-used;
					start = end;
					return (int)(free*1024);
				}
							
			}
			br.close();
			p.destroy();
		
		}
		catch(Exception e)
		{
			
		}
		return 0;
	}
	synchronized public int getUsedDisk() throws RemoteException
	{
		return dUsed;
	}
	synchronized public int getTotalDisk() throws RemoteException
	{
		return dFree;
	}
	synchronized public long getFreeMemory() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return (info.freePhysicalMem);
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public long getTotalMemory() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return (info.physicalMem);
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public long getUsedMemory() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return (info.usedPhysicalMem);
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public long getFreeVirtualMemory() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return (info.freeVirtualMem);
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public long getUsedVirtualMemory() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return (info.usedVirtualMem);
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public long getTotalVirtualMemory() throws RemoteException
	{
		
		try
		{
			SystemInformation info = getSystemInformation();
			if(info != null)
			return (info.virtualMem);
			else
			return -1;
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}

	
}
