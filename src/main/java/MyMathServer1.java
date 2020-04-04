import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
class ProcessingThread11 extends Thread
{
	
	GridJob gridJob;
	MyMathServer1 mathServer;
	public ProcessingThread11(MyMathServer1 server,GridJob job)
	{
		mathServer=server;
		gridJob=job;
	}
		
	public void run()
	{	
		MathJob job=(MathJob)gridJob;
		if (job.op==1)
		{
			job.result=job.a+job.b;
		}     
		else
		{
			job.result=job.a-job.b;
		}
		mathServer.addResult(job);

		
	}	

			
}
public class MyMathServer1 extends UnicastRemoteObject implements JobInterface
{
	static final String SERVER="Math1";
        MyMath math = new MyMath();
	MathJob mathJob;
	Hashtable results = new Hashtable();
	ProcessingThread11 mathThread;
	public static void main(String args[])
	{	
		//System.setSecurityManager(new RMISecurityManager());
		try
		{
			
			MyMathServer1 server = new MyMathServer1();
			Naming.rebind(SERVER,server);
			System.out.println("Server started sucessfully");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public MyMathServer1() throws RemoteException
	{
		
	}
  	synchronized public void submitJob(GridJob job)
	{
		mathThread = new ProcessingThread11(this,job);
		mathThread.start();
	}
	public GridJob getResult(int id)
	{
		if(results.size()>0)
		{
			if (results.containsKey(" "+id))
			{
				GridJob job=(GridJob)results.get(" "+id);
				results.remove(" "+id);
				return job;
			}	
		}
		return null;
	}
	public void addResult(GridJob job)
	{
		results.put(" "+job.jobId,job);		
	}
	public int getStatus(int id)
	{
		if(results.size()>0)
		{
			if(results.containsKey(" "+id))
			return 1;
			return 0;
		}
		return 0;
	}

}