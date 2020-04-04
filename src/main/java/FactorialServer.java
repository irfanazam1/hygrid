import java.rmi.*;
import java.rmi.server.*;
import java.util.Hashtable;
import java.net.InetAddress;
class FactorialProcessThread extends Thread
{
	private FactorialServer Server;
	private FactorialJob job;
	private String address;
	public FactorialProcessThread(FactorialServer server,GridJob j,String add)
	{
		Server=server;
		job=(FactorialJob)j;
		address=add;
	
	}
	public void run()
	{
		/*Interfaces required for database connection and insertion*/
		try
		{
		   sleep(200);
		   for(int i = job.from+1;i<=job.to;i++)
		   {
		   		StringBuffer num = new StringBuffer(""+i);
		   		LargeNumber temp = new LargeNumber(num);
		   		job.number = job.number.mul(temp);		
		   }
		   job.type="RMI";
		   job.handle="rmi://"+address+"/Factorial";
		   job.destination=address;
		}
		catch(NullPointerException pe)
		{
			System.out.println(pe);
			Server.reportError(job.jobId);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Server.reportError(job.jobId);
			
			
		}
		Server.addResult(job);
	}
}
class FactorialProcessThreadEx extends Thread
{
	private FactorialServer Server;
	private FactorialJobEx job;
	private String address;
	public FactorialProcessThreadEx(FactorialServer server,GridJob j,String add)
	{
		Server=server;
		job=(FactorialJobEx)j;
		address=add;
	
	}
	public void run()
	{
		/*Interfaces required for database connection and insertion*/
		try
		{
		   sleep(200);
		   job.number = job.args1.mul(job.args2);
		   job.type="RMI";
		   job.handle="rmi://"+address+"/Factorial";
		   job.destination=address;
		}
		catch(NullPointerException pe)
		{
			System.out.println(pe);
			Server.reportError(job.jobId);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Server.reportError(job.jobId);
			
			
		}
		Server.addResult(job);
	}
}

public class FactorialServer extends UnicastRemoteObject implements JobInterface
{
	private static String SERVER=" ";
	private static String SCHEDULAR=" ";
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
				System.out.println("Correct Use: java DataJobServer Scheduler Address");
				System.exit(0);
			}
			InetAddress local = InetAddress.getLocalHost();
			myAddress=local.getHostAddress();
			SERVER="//";
			SERVER+=myAddress;
			SERVER+="/Factorial";
			SCHEDULAR = "rmi://"+args[0]+"/schedular";
			JobInterface server = new FactorialServer();
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
			System.out.println("Server started sucessfully");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	public FactorialServer() throws RemoteException
	{
		
	}
  	synchronized public void submitJob(GridJob job)
	{
			
		if(job instanceof FactorialJob)
		{
			FactorialProcessThread thread = new FactorialProcessThread(this,job,myAddress);
			thread.setDaemon(true);
			thread.start();
		}
		else
		{
			FactorialProcessThreadEx thread = new FactorialProcessThreadEx(this,job,myAddress);
			thread.setDaemon(true);
			thread.start();
		}
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