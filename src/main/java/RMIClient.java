import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

class MathThread extends Thread implements RemoteJobInterface
{
		
	private String URL;
	private String from;
	private boolean flag;
	static int threadId=0;	
	int id;
	Random r = new Random();
	int arg1,arg2;
	public MathThread(int a,int b,String sch,String frm) 
	{
		arg1=a;
		arg2=b;
		URL="rmi://";
		URL+=sch;
		URL+="/schedular";
		from=frm;
		id = threadId++;
		flag=true;
	}
	public void run()
	{
		
		MathJob job = new MathJob(arg1,arg2,1,0);
		int statCode=0;
		try
		{	
		  		job.source=from;
				job.serviceName="Math";
				job.intensity=GridJob.COMPUTATION_INTENSIVE;
				job.force=false;
				job.userName="irfan";
				UnicastRemoteObject.exportObject(this);
				SchedularInterface server = (SchedularInterface) Naming.lookup(URL);
				System.out.println("Before Submission: "+job.userName);
				job.jobId=server.submitJob(job,this);
				System.out.println(job.jobId);
				
		}
		catch(RemoteException remoteException)
		{
			System.out.println(remoteException);
			//System.out.println("Error Calling the remote method, Schedular might not ready");	
		}
		catch(NotBoundException notBoundException)
		{
			System.out.println("Not Connected to the Schedular");
		}
		catch( java.net.MalformedURLException URLException)
		{
			System.out.println("Malformed URL is given to connect to the Schedular");
		}
		catch(Exception e)
		{
			System.out.println("Here..Error");
			System.out.println(e);
		}

				
	}
	public void setStatus(int status,GridJob j)
	{
		if (status == SchedularInterface.NOTSUPPORTED||status==SchedularInterface.ELAPSED)
		flag=false;
		System.out.println(status);	
	}
	public void setStatus(int status)
	{}
	public void setResult(GridJob job)
	{
		MathJob math = (MathJob)job;
		System.out.println(math.result);
		try
		{
			UnicastRemoteObject.unexportObject(this,true);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		flag=false;
	}
}

public class RMIClient 
{
	
	static int[][] val={{10,20},{40,100},{100,300},{500,600},{800,900},{160,204},{140,220},{110,250},{120,220},{150,280}};
	private static String Schedular;

	public static void main (String args[])
	{
		Schedular = "rmi://";
		Schedular+=args[0];
		Schedular+="/schedular";
		try
		{	
			for(int i=0;i<9;i++)
			{
				MathThread th = new MathThread(val[i][0],val[i][1],args[0],args[1]);
				th.start();	
			}
				
		}
		catch(Exception e)
		{
			System.out.println("Here");
			System.out.println(e);
		}
	}
	
}