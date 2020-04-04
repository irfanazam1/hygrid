import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.net.InetAddress;
import java.sql.*;

public class Broker extends UnicastRemoteObject implements BrokerServerInterface
{
	private static String SERVER;
	private Hashtable terminals = new Hashtable();
	static private boolean available;
	private Hashtable infos = new Hashtable();
	private static SecurityManagerInterface security;
	private static String securityAddress;
	private HashSet nodes = new HashSet();
	private Hashtable processorLoads = new Hashtable();
	public static void main(String args[])
	{
		if(args.length!=1)
		{
			System.out.println("Proper Use: Broker SecurityManager Address");
			System.exit(0);
		}
		else if(args[0].trim().equals(""))
		{
			System.out.println("Proper Use: Broker SecurityManager Address");
			System.exit(0);
		}
		securityAddress = args[0];
		{	
        	
        	try
    		{
    			String secURL = "rmi://"+securityAddress+"/SecurityManager";
        		SecurityManagerInterface security = (SecurityManagerInterface)Naming.lookup(secURL);
        		if (security != null)
        		{
        			//Remove the hardcoded username and password with command line arguments
        			boolean result = security.authenticate("Administrator","unknown");
        			if(!result)
        			{
        				System.out.println("You are not authorized to Start this Application");
        				System.exit(0);
        			}
        		}
        	}
        	catch(RemoteException re)
        	{
        		System.out.println("Error Connecting to Security Manager");
        		System.exit(0);
        	}
        	catch (NotBoundException bne)
        	{
        		System.out.println("Error Connecting to Security Manager");
        		System.exit(0);
        	}
        	catch (java.net.MalformedURLException mfue)
        	{
        		System.out.println("Malformed URL: Check Security Manager's Address");
        		System.exit(0);
        	}
        	
        	    	
    			
		}			
		
		
		try
		{
			
			available=true;
			InetAddress inet = InetAddress.getLocalHost();
			SERVER="//";	
			SERVER+=inet.getHostAddress();
			SERVER+="/brokerserver";
			System.out.println(SERVER);
			BrokerServerInterface server = new Broker();
			Naming.rebind(SERVER,server);
			System.out.println("Broker Started Successfully");
			security = (SecurityManagerInterface)Naming.lookup("rmi://"+securityAddress+"/SecurityManager");
			
		}
		catch(java.net.UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
			System.exit(0);
		}
		catch(java.net.MalformedURLException mfe)
		{
			mfe.printStackTrace();
			System.exit(0);
		}
		catch(NotBoundException nbe)
		{
			nbe.printStackTrace();
			System.exit(0);
		}
		
	}
	public Broker() throws RemoteException
	{
				
	}
  	synchronized public int register(String address,/*int processorSpeed,*/BrokerInterface broker) throws RemoteException
	{
		if(terminals.containsKey(address))
		{
			terminals.remove(address);
			terminals.put(address,broker);
			/*processorLoads.remove(address);
			processorLoads.put(address,new Integer(processorSpeed));
			*/
			return 0;
		}
		else
		{
			if(nodes.contains(address))
			{
				terminals.put(address,broker);
				//processorLoads.put(address,new Integer(processorSpeed));			
				return 1;
			}
			else
			{
				nodes = security.getNodes();
				if(nodes.contains(address))
				{
					terminals.put(address,broker);
					//processorLoads.put(address,new Integer(processorSpeed));			
					return 1;
				}
				else
				return -1;
			}
			
		}
		
				
	}
	synchronized public void unregister(String address)
	{
		terminals.remove(address);
	}
	synchronized public Hashtable getSysInfo() throws RemoteException
	{
	
		if (terminals.size()<=0)
		{
			return null;
		}
		else
		{
			Enumeration elements = terminals.keys();
			Hashtable sysInfos = new Hashtable();
			SystemInfo sysInfo;
			while (elements.hasMoreElements())
			{
				
				String address = (String)elements.nextElement();
				BrokerInterface broker = (BrokerInterface)terminals.get(address);
				try
				{
					SystemInformation info = broker.getSystemInformation();
					int percentMem = (int)((info.usedPhysicalMem*100)/info.physicalMem+1);
					int percentVMem = (int)((info.usedVirtualMem*100)/info.virtualMem+1);
					int percentDisk = (int)((info.usedDisk*100)/info.totalDisk+1);
					sysInfo = new SystemInfo(address,info.cpu,percentMem,percentVMem,percentDisk,info.folderName);
					sysInfos.put(address,sysInfo);
					
				}
				catch(RemoteException re)
				{
					unregister(address);
					re.printStackTrace();
					continue;
				}
				
			}
			return sysInfos;
		}
	}
	public SystemInformation getSystemInfo(String address) throws RemoteException
	{
       try
	   {
			if(!terminals.containsKey(address))
			return null;
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			SystemInformation info = in.getSystemInformation();
			if(!infos.containsKey(address))
			infos.put(address,info);
			return info;
	   }
	   catch(RemoteException re)
	   {
			re.printStackTrace();
			terminals.remove(address);
			return null;
	   }
	   
	}
	public ArrayList getProcessInfo(String address) throws RemoteException
	{
	   
	   ArrayList processes;
	   try
	   {
			if(!terminals.containsKey(address))
				return null;
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			processes = in.getProcesses();
			return processes;
	   }
	   catch(RemoteException re)
	   {
			re.printStackTrace();
			terminals.remove(address);
			return null;
	   }
	   
	   
		
	}
	
	public ArrayList getDetailInfo(String address) throws RemoteException
	{
	   
	   ArrayList info;
	   try
	   {
			if(!terminals.containsKey(address))
				return null;
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			info = in.getDetails();
			return info;
	   }
	   catch(RemoteException re)
	   {
			re.printStackTrace();
			terminals.remove(address);
			return null;
	   }
	   
	   
		
	}
	synchronized public String getSharedFolder(String address) throws RemoteException
	{
	   try
	   {
			if(!terminals.containsKey(address))
				return null;
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			return  in.getSharedFolder();
	   }
	   catch(RemoteException re)
	   {
			re.printStackTrace();
			terminals.remove(address);
			return null;
	   }
	}
	synchronized public HashSet getNodes() throws RemoteException
	{
		HashSet nodes; 
		Enumeration keys = terminals.keys();
		nodes = new HashSet();	
		while(keys.hasMoreElements())
		{
			nodes.add((String)keys.nextElement());
		}
		return nodes;
	}
	synchronized public int getCpuPercent(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int cpu = in.getCpuPercent();
				return cpu;
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
	synchronized public int getMemPercent(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int mem = in.getMemPercent();
				return mem;
			}
			else
			return -1;
		}catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
		
		
	}
	synchronized public long getTotalMemory(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				long mem = in.getTotalMemory();
				return mem;
			}
			else
			return -1;
		}catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
		
		
	}
	synchronized public long getUsedMemory(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				long mem = in.getUsedMemory();
				return mem;
			}
			else
			return -1;
		}catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
		
		
	}
	synchronized public long getFreeMemory(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				long mem = in.getFreeMemory();
				return mem;
			}
			else
			return -1;
		}catch(RemoteException re)
		{
			re.printStackTrace();
			return -1;
		}
		
	}
	synchronized public int getVMemPercent(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int vmem = in.getVMemPercent();
				return vmem;
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
	
	synchronized public long getTotalVirtualMemory(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				long vmem = in.getTotalVirtualMemory();
				return vmem;
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
	
	synchronized public long getUsedVirtualMemory(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				long vmem = in.getUsedVirtualMemory();
				return vmem;
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
	
	synchronized public long getFreeVirtualMemory(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				long vmem = in.getFreeVirtualMemory();
				return vmem;
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



	synchronized public int getDiskPercent(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int disk = in.getDiskPercent();
				return disk;
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
	synchronized public int getTotalDisk(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int disk = in.getTotalDisk();
				return disk;
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
	synchronized public int getUsedDisk(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int disk = in.getUsedDisk();
				return disk;
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
	synchronized public int getFreeDisk(String address) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int disk = in.getFreeDisk();
				return disk;
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
	synchronized public int getFreeDisk(String address,String diskName) throws RemoteException
	{
		if(!terminals.containsKey(address))
		return -1;
		try
		{
			BrokerInterface in = (BrokerInterface)terminals.get(address);
			if(in !=null)
			{
				int disk = in.getFreeDisk(diskName);
				return disk;
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
	public void removeNode(String machine) throws RemoteException
	{
		nodes.remove(machine);
		terminals.remove(machine); 
	}
	


}
