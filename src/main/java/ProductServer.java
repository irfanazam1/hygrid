
import java.rmi.*;
import java.rmi.server.*;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.net.InetAddress;
import java.util.ArrayList;
class ProductProcessThread extends Thread
{
	private ProductServer Server;
	private String address;
	private ProductJob job;
	public ProductProcessThread(ProductServer server,GridJob j,String add)
	{
		Server=server;
		job=(ProductJob)j;
		address=add;
	}
	public void run()
	{
    	try
  	{
    		//TODO: Job processing code here
    	}
		catch(Exception e)
		{
			Server.reportError(job.jobId);
			e.printStackTrace();
		}
		Server.addResult(job);
	}
}
public class ProductServer extends UnicastRemoteObject implements JobInterface
{
	private static String SERVER;
	private static String SCHEDULAR;
 private Hashtable results = new Hashtable();
	private static String myAddress;
	private static SchedularInterface schedular;
	private GridJob job;
	public static void main(String args[])
	{
		try
		{
			if(args.length != 1)
			{
				System.out.println("Correct Use: java QueryServer Scheduler Address");
				System.exit(0);
			}
			InetAddress local = InetAddress.getLocalHost();
			myAddress=local.getHostAddress();
			SERVER="//";
			SERVER+=myAddress;
			SERVER+="/Query";
			SCHEDULAR = "rmi://"+args[0]+"/schedular";
			JobInterface server = new ProductServer();
			Naming.rebind(SERVER,server);
			schedular = (SchedularInterface)Naming.lookup(SCHEDULAR);
			if(schedular.registerService("rmi:"+SERVER))
			{
				System.out.println("Server started sucessfully");
			}
			else
			{
				System.out.println("Service is Already Registered");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	public ProductServer() throws RemoteException
	{
	}
	synchronized public void submitJob(GridJob job)
	{
		ProductProcessThread thread = new ProductProcessThread(this,job,myAddress);
		thread.start();
	}
	public GridJob getResult(int id)
	{
		if (results.containsKey(" "+id))
		{
			GridJob job = (GridJob)results.get(" "+id);
			results.remove(" "+id);
			return job;
		}
		else
		return null;
	}
	synchronized public void addResult(GridJob job)
	{
		if(schedular instanceof SchedularInterface)
		{
			try
			{
				schedular.setResult(job);
			}
			catch(RemoteException e)
			{
				e.printStackTrace();
			}
		}
		else
		results.put(" "+job.jobId,job);
	}
	public int getStatus(int id)
	{
		if(results.containsKey(" "+id))
		return 1;
		return 0;
	}
	synchronized public void reportError(int id)
	{
		try
		{
			schedular.reportError(id);
			System.out.println("Reporting Error");
			System.exit(0);
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}
	}
}