import java.rmi.*;
import java.rmi.server.*;
import java.util.Hashtable;
import java.net.InetAddress;

class TestAppProcessThread extends Thread
{
	private TestAppServer Server;
	private String address;
	private TestJob job;
	public TestAppProcessThread(TestAppServer server,GridJob j,String add)
	{
		Server=server;
		job=(TestJob)j;
		address=add;
	
	}
	public void run()
	{
		
		LargeNumber number=null;
		try
		{
		   	
			for(int j=0;j<4;j++)
			{
				StringBuffer num = new StringBuffer(""+1);
				number = new LargeNumber(num);
				for(int k = 1;k<=1000;k++)
				{
					num = new StringBuffer(""+k);
					LargeNumber temp = new LargeNumber(num);
					number = number.mul(temp);		
				}
				
			}
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
		job.number=number;
		Server.addResult(job);
	}
}

public class TestAppServer extends UnicastRemoteObject implements JobInterface
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
				System.out.println("Correct Use: java TestAppServer Scheduler Address");
				System.exit(0);
			}
			InetAddress local = InetAddress.getLocalHost();
			myAddress=local.getHostAddress();
			SERVER="//";
			SERVER+=myAddress;
			SERVER+="/Test";
			SCHEDULAR = "rmi://"+args[0]+"/schedular";
			JobInterface server = new TestAppServer();
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
	public TestAppServer() throws RemoteException
	{
		
	}
  	synchronized public void submitJob(GridJob job)
	{
			
		
			TestAppProcessThread thread = new TestAppProcessThread(this,job,myAddress);
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