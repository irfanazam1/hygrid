import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;

class WindowsDiskMonitorThread extends Thread
{
	private WinBroker broker;
	public WindowsDiskMonitorThread(WinBroker bro)
	{
		broker = bro;
	}
	public void run()
	{
		while (true)
		{
			try
			{
				
				broker.getDiskInfo(broker.getDiskName());
				if (broker.getFreeSpace()<=1200)
				{
					broker.initDisk();
					broker.getDiskInfo(broker.getDiskName());
				}
				sleep(5000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}

/**
*WinBroker is used as a broker system for Windows operating System.
*It uses Three files to get system information from the OS.
<pre>
*These files are 
*1. Cpuusage.exe: An exe file written in c++ using VC++. It provides the information
*   about the CPU,Memory and Virtual Memory. This fie writes the information
*   in an ordinary text file and winbroker reads the contents of the file to get
*   the updated information.
*2. DiskInfo.exe: An exe file written in c++ using vc++. It provides the information
*	about the harddisks and other removeable storage medias attached to the system
*	ProcessInfo: An exe file written in c++ using vc++. It provides the information
	about the processes running on the machine.
*</pre>
*/

public class WinBroker implements BrokerInterface
{
	/**
	*Holds this broker address
	*/
	private static String brokerAddress;
	/**
	*Holds the broker server address
	*/
	private static String serverAddress;
	/**
	*Holds the name of the shared folder
	*/
	private String folderName;
	/**
	*Holds the name of the shared drive
	*/
	private String diskName;
	/**
	*Holds the value of total disk size in MBs
	*/
	private int dTotal;
	/**
	*Holds the value of free disk size in MBs
	*/
	private int dFree;
	/**
	*Holds the value of used disk size in MBs
	*/
	private int dUsed;
	/**
	*Process object used to open system processes
	*/
	private Process system;
	/**
	*Name of the file to open as a process
	*/
	private static String fileName;
	public static void main(String args[])
	{	
		try
		{
			if (args.length==2)
			{
				serverAddress="rmi://";
				serverAddress+=args[0];
				fileName = args[1];
				File temp = new File(fileName);
				if (temp.isDirectory()||temp==null)
				{
						System.out.println("Please provide a valid file path"); 
						System.exit(0);
				}
				
			}
			else
			{
				System.out.println("Proper Use: BrokerServer Address Filename for system information");
				System.exit(0);
			}
			InetAddress address = InetAddress.getLocalHost();
			brokerAddress = address.getHostAddress();
			BrokerInterface server = new WinBroker();
			serverAddress+="/brokerserver";
			UnicastRemoteObject.exportObject(server);
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
	public WinBroker() throws RemoteException
	{
		system=null;
		System.out.println("Calculating Free Disk space and initializing shared folder...");
		initDisk();
		getDiskInfo(diskName);
		WindowsDiskMonitorThread th = new WindowsDiskMonitorThread(this);
		th.setDaemon(true);
		th.start();
		
	}
	/**
	*Function to get the processor speed
	*/
	/*private int getProcessorSpeed()
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		try
		{
							
			p = Runtime.getRuntime().exec("ProcessorSpeed.exe ");
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			line = br.readLine();
			return Integer.parseInt(line);
			
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(0);
		
		}
		return 0;
		
	}
	*/
	/**
	*Provides the system information of the machine like 
	Memory,CPU,Disk,Virtual Memory,shared folder,Address.It
	uses Cpuusage.exe
	*/
	
	synchronized public SystemInformation getSystemInformation() throws RemoteException
	{
		
		InputStreamReader isr;
		BufferedReader br;
		SystemInformation sysInfo;
		String line="";
		int start=0,end=0,index=0;
		String[] data = new String[7];
		try
		{
			isr = new InputStreamReader(new FileInputStream(fileName));
			br =  new BufferedReader(isr);
			line = br.readLine();
			/*Wait until no data is retrieved*/
			if(line==null)
			{
				do
				{
					Thread.sleep(1000);
					line=br.readLine();
				}while(line==null);
			}
				
		}
		catch(FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
			System.exit(0);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch(InterruptedException ie)
		{}
		index=0;
		/*Parsing a comma separated line*/
		do
        {
			end=line.indexOf(',',start);
            if (end!=-1)
			{
				data[index++]=(line.substring(start,end));
			}
            else
			{
		       	data[index++]=(line.substring(start,line.length())); 
			}
    	    start=end+1;
	                    
		}while(end!=-1);
		
		sysInfo = new SystemInformation
		(
		Long.parseLong(data[0]),//total memory
		Long.parseLong(data[1]),//used memory
		Long.parseLong(data[2]),//free memory
		Long.parseLong(data[3]),//total virtual
		Long.parseLong(data[4]),//used virtual
		Long.parseLong(data[5]),//free virtual
		(Integer.parseInt(data[6])-2)>0?Integer.parseInt(data[6])-2:0,//cpu threshold
		dTotal,//total disk
		dUsed,//used disk
		dFree,//free disk
		folderName,//shared folder
		brokerAddress//address
		);
		return sysInfo;
	}
	/**
	*Provides a list of process running on the system using the ProcessInfo.exe
	*/
	synchronized public ArrayList getProcesses() throws RemoteException
	{
		Process p=null;
		InputStream ins=null;
		BufferedReader br=null;
		String line="";
		ArrayList array = new ArrayList();
		try
		{
			p = Runtime.getRuntime().exec("ProcessInfo.exe");
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			
			while((line = br.readLine())!=  null)
			{
				
				line = parseLine(line);
				array.add(line);
			}
			ins.close();
			br.close();
			p.destroy();
			return array;
			
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		
	}
	/**
	*Parsing a comma separated line
	*/
	public String parseLine(String line)
	{
		
		int index=0,start=0,end=0;
		String result="";
		String temp="";
		do
        {
			end=line.indexOf(',',start);
            if (end!=-1)
			{
				
				temp=(line.substring(start,end));
				result=result+temp+"\t";
			}
            else
			{
		       	temp=(line.substring(start,line.length())); 
		       	result=result+temp;
			}
    	    start=end+1;
	                    
		}while(end!=-1);
		return result;
	}
	/**
	*Provides a list of details of the system using the SystemInfo.exe
	*This feature is available only on windowsxp and above
	*/
	synchronized public ArrayList getDetails() throws RemoteException
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		ArrayList array = new ArrayList();
		try
		{
			p = Runtime.getRuntime().exec("Systeminfo.exe");
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			
			while((line = br.readLine())!=  null)
			{
				array.add(line);
			}
			br.close();
			p.destroy();
			return array;
			
			
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		
	}
/**
* This method will be used to intialize shared folder on a disk
whose size is greater than all the drives present on the machine
*/	
synchronized public void initDisk()
{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		int start=0,end=0;
		File file;
		long maxFree = 0;
		String[] data = new String[4];
		int index=0;
		try
		{
			p = Runtime.getRuntime().exec("diskinfo.exe");
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			while ((line = br.readLine())!=null)
			{
			    index = 0;
			    start = 0;
			    end   = 0;
				do
        		{
					end=line.indexOf(',',start);
            		if (end!=-1)
					{
						data[index++]=(line.substring(start,end));
					}
            		else
					{
		       			data[index++]=(line.substring(start,line.length())); 
					}
    	    		start=end+1;
	                    
				}while(end!=-1);
				long size = Long.parseLong(data[3].trim());
				if (size>maxFree)
				{
					maxFree=size;
					diskName=data[0];
				}	
		
			}//while
			br.close();
			p.destroy();
		}//try	
		catch(Exception e)
		{
			e.printStackTrace();
		}
		folderName=diskName+"GridShared";
		file = new File(folderName);
		file.mkdir();
		System.out.println("Shared folder is: "+folderName);
}
/**
*Provides the information of the disk whose disk letter is provided.
*/
synchronized public void getDiskInfo(String disk)
{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		int start=0,end=0;
		String[] data = new String[4];
		int index=0;
		try
		{
							
			p = Runtime.getRuntime().exec("diskinfo.exe "+disk);//+diskName);
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			while ((line = br.readLine())!=null)
			{
				do
        		{
					end=line.indexOf(',',start);
            		if (end!=-1)
					{
						data[index++]=(line.substring(start,end));
					}
            		else
					{
		       			data[index++]=(line.substring(start,line.length())); 
					}
    	    		start=end+1;
	                    
				}while(end!=-1);	
			}
			br.close();
			p.destroy();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		dTotal = Integer.parseInt(data[1]);
		dUsed  = Integer.parseInt(data[2]);
		dFree  = Integer.parseInt(data[3]);
	}
	/**
	* Provides the shared disk name
	*/
	public String getDiskName()
	{
		return diskName;
	}
	/**
	*Provides the free space of the shared disk
	*/
	public int getFreeSpace()
	{
		return dFree;
	}
	/**
	*Provides the shared folder name
	*/
	synchronized public String getSharedFolder() throws RemoteException
	{
		return folderName;
	}
	protected void finalize()
	{
		if(system!=null)
		{
			system.destroy();
			system=null;
		}
			
	}
	/**
	*Provides the %usage of CPU
	*/
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
	/**
	*Provides the %usage of Memory
	*/
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
	/**
	*Provides the %usage of Virtual Memory
	*/
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
	/**
	*Provides the %usage of Disk
	*/
	synchronized public int getDiskPercent() throws RemoteException
	{
		getDiskInfo(diskName);
		return (dUsed*100)/dTotal;
	}
	/**
	*Provides the value of Free disk in MBs
	*/
	synchronized public int getFreeDisk() throws RemoteException
	{
		getDiskInfo(diskName);
		return dFree;
	}
	/**
	*Provides the value of Used disk in MBs
	*/
	synchronized public int getUsedDisk() throws RemoteException
	{
		getDiskInfo(diskName);
		return dUsed;
	}
	/**
	*Provides the value of Total disk in MBs
	*/
	synchronized public int getTotalDisk() throws RemoteException
	{
		getDiskInfo(diskName);
		return dTotal;
	}
	/**
	*Provides the value of Free disk in MBs whose disk letter is provided
	*/
	synchronized public int getFreeDisk(String disk) throws RemoteException
	{
		Process p;
		InputStream ins;
		BufferedReader br;
		String line="";
		int start=0,end=0;
		String[] data = new String[4];
		int index=0;
		try
		{
							
			p = Runtime.getRuntime().exec("diskinfo.exe "+disk);//+diskName);
			ins = p.getInputStream();
			br =  new BufferedReader(new InputStreamReader(ins));
			while ((line = br.readLine())!=null)
			{
				do
        		{
					end=line.indexOf(',',start);
            		if (end!=-1)
					{
						data[index++]=(line.substring(start,end));
					}
            		else
					{
		       			data[index++]=(line.substring(start,line.length())); 
					}
    	    		start=end+1;
	                    
				}while(end!=-1);	
			}
			br.close();
			p.destroy();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		int free = Integer.parseInt(data[3]);
		return free;
	}
	/**
	*Provides the value of Free Memory in KBs
	*/
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
	/**
	*Provides the value of Free Virtual Memory in KBs
	*/
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
	/**
	*Provides the value of Used Memory in KBs
	*/
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
	/**
	*Provides the value of Used Virtual Memory in KBs
	*/
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
	/**
	*Provides the value of Total Memory in KBs
	*/
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
	/**
	*Provides the value of Total Virtual Memory in KBs
	*/
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