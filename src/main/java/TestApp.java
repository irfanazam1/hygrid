import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.Date;

class TestAppJobThread extends Thread implements RemoteJobInterface
{
	private TestApp app;
	private TestJob job;
	public TestAppJobThread(TestApp test,TestJob j)
	{
		app=test;
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
				System.out.println("User is not Allowed to submit a job");
				System.exit(0);
			}
			else if(result == SchedularInterface.MACHINE_NOT_AUTHORIZED)
			{
				System.out.println("Machine is not Allowed to submit a job");
				System.exit(0);
			}
		}
		catch(NotBoundException e)
		{
			System.out.println("Error Connecting Schedular\nSchedular might not ready..");
		}
		catch(OutOfMemoryError err)
		{
			System.out.println("Virtual Machine gone out of Memory");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
	public void setStatus(int status,GridJob job) throws RemoteException
	{
						
	}
	public void setStatus(int status) throws RemoteException
	{
		if(status==SchedularInterface.ELAPSED)
		{
			try
			{	
				UnicastRemoteObject.unexportObject(this,true);
				UnicastRemoteObject.exportObject(this);
				SchedularInterface server = (SchedularInterface)Naming.lookup(app.scheduler);
				server.submitJob(job,this);
			}
			catch(NotBoundException e)
			{
				System.out.println("Error Connecting Schedular\nSchedular might not ready..");
			}
			catch(OutOfMemoryError err)
			{
				System.out.println("Virtual Machine gone out of Memory");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}	
		}			
	}
	public void setResult(GridJob job) throws RemoteException
	{
		app.addJob(job);
		UnicastRemoteObject.unexportObject(this,true);
	}
}
public class TestApp
{
	public String scheduler;
	private String address;
	private String userName;
	public int done;
	public TestApp(String add,String user)
	{
		
		userName=user;
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			address= inet.getHostAddress();
			scheduler=add;
			
		}
		catch(java.net.UnknownHostException une)
		{
			une.printStackTrace();
			
		}
		catch(Exception e)
		{
		}
		
		
	}
	
	synchronized public void addJob(GridJob job)
	{
		done++;
		if(done==20)
		{
			TestJob t = (TestJob)job;
			System.out.println(t.number.getNumber());
			System.out.println("Job Finished At: "+new Date());
		}
		
	}
	private void createJobs()
	{
		System.out.println("Job Started At: "+new Date());
		for(int i=0;i<20;i++)
		{
			TestJob job = new TestJob();
			job.source=address;
			job.userName=userName;
			job.intensity=GridJob.COMPUTATION_INTENSIVE;
			job.serviceName="Test";
			job.force=false;
			TestAppJobThread th = new TestAppJobThread(this,job);
			th.start();
			
		}
	}
	public static void main(String args[])
	{
		if(args.length!=2)
		{
			System.out.println("Proper Use: [Schedular Address] [User Name]\nExample TestApp 192.168.0.1 irfan");
		}
		String sch="rmi://"+args[0]+"/schedular";
		TestApp test = new TestApp(sch,args[1]);
		test.createJobs();
		
		
		
	}
}