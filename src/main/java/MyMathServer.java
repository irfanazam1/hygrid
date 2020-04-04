import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;
class ProcessingThread extends Thread
{
	
	MathJob job;
	MyMathServer mathServer;
	public ProcessingThread(MyMathServer server,GridJob gJob)
	{
		mathServer=server;
		job=(MathJob)gJob;
	}
		
	public void run()
	{	
		
		if (job.op==1)
		{
			job.result=job.a+job.b;
		}     
		else
		{
			job.result=job.a-job.b;
		}
		try
		{
		sleep(500);
		}catch(Exception e){}
		mathServer.addResult(job);

		
	}	

			
}
public class MyMathServer extends UnicastRemoteObject implements JobInterface
{
	static String SERVER="Math";
	static String SCHEDULAR="";
    private MyMath math = new MyMath();
	private MathJob mathJob;
	private Hashtable results = new Hashtable();
	private ProcessingThread mathThread;
	private static SchedularInterface schedular;
	public static void main(String args[])
	{	
		
		try
		{
			
			if(args.length != 1)
			{
				System.out.println("Correct Use: java MyMathServer Schedular Address\n Example: java MyMathServer 192.168.1.10\n");
				System.exit(0);

			}
			InetAddress inet = InetAddress.getLocalHost();
			SERVER="//";	
			SERVER+=inet.getHostAddress();
			SERVER+="/Math";
			SCHEDULAR ="rmi://"+args[0]+"/schedular";
			MyMathServer server = new MyMathServer();
			Naming.rebind(SERVER,server);
			schedular = (SchedularInterface)Naming.lookup(SCHEDULAR);
			if(schedular.registerService("rmi:"+SERVER)) 
			{
				System.out.println("Server started sucessfully");
			}
			else
			{
				System.out.println("Service is Already Registered");
				System.exit(0);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	public MyMathServer() throws RemoteException
	{
		schedular=null;
	}
  	synchronized public void submitJob(GridJob job) throws RemoteException
	{
		mathThread = new ProcessingThread(this,job);
		mathThread.start();
	}
	public GridJob getResult(int id) throws RemoteException
	{
		
		if (results.containsKey(" "+id))
		{
			GridJob job=(GridJob)results.get(" "+id);
			results.remove(" "+id);
			return job;
		}	
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

}