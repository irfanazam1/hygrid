import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
class PeopleApp1JobThread extends Thread implements RemoteJobInterface
{
	private PeopleApp1 app;
	private People1Job job;
	public PeopleApp1JobThread (PeopleApp1 cls, People1Job j)
	{
		app=cls;
		job=j;
	}
	public void run()
	{
		try
		{
			UnicastRemoteObject.exportObject(this);
			SchedularInterface server = (SchedularInterface)Naming.lookup(app.scheduler);
			int result = server.submitJob(job,this);
			if(result == SchedularInterface.USER_NOT_AUTHORIZED)
			{
				System.exit(0);
			}
			else if(result == SchedularInterface.MACHINE_NOT_AUTHORIZED)
			{
				//TODO: Your code here
			}
		}
		catch(NotBoundException e)
		{
			//TODO: Your code here
		}
		catch(OutOfMemoryError err)
		{
			//TODO: Your code here
		}
	catch(Exception e)
	{
			//TODO: Your code here
	}
	}
	public void setStatus(int status,GridJob job) throws RemoteException
	{
	   if (status==SchedularInterface.ELAPSED)
	   {
			//TODO: Your code here
	   }
	}
	public void setStatus(int status) throws RemoteException
	{
			//TODO: Your code here
	}
	public void setResult(GridJob job) throws RemoteException
	{
		app.addJob(job);
		UnicastRemoteObject.unexportObject(this,true);
	}
}
public class PeopleApp1
{
	public String scheduler;
	private String address;
	public int done;
	private int total;
	public PeopleApp1 (String add)
	{
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			address= inet.getHostAddress();
			scheduler=add;
		}
		catch(java.net.UnknownHostException une)
		{
			//TODO: Your code here
		}
		catch(Exception e)
		{
			//TODO: Your code here
		}
	}
	synchronized public void addJob(GridJob job)
	{
		done++;
		People1Job qj = ( People1Job )job;
		if(done==total)
		{
			//TODO: Your code here
		}
	}
	private void createJobs()
	{
	}
	public static void main(String args[])
	{
		String sch="rmi://"+args[0]+"/schedular";
		PeopleApp1 app = new PeopleApp1(sch);
	}
}