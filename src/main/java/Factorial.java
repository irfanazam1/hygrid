import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Random;
import java.util.Date;
import java.util.SortedMap;
import java.util.Set;

class MultiplyThread extends Thread 
{
	Factorial fact;
	
	public MultiplyThread(Factorial f)
	{
		
		fact=f;
		
	}
	public void run()
	{
		while(true)
		{
			try
			{
				sleep(300);
			}
			catch(Exception e)
			{
			}
			System.out.println(fact.done+" "+fact.max);
			if(fact.doneEx==fact.max-1)
			{
				System.out.println("Final Mul");
				FactorialJobEx onex = (FactorialJobEx)fact.factorialJobsEx.removeFirst();
				if(fact.max%2!=0)
				{
				
					FactorialJob one = (FactorialJob)fact.factorialJobs.removeFirst();
					fact.number = onex.number.mul(one.number);
					System.out.println("Here");
				}
				else
				System.out.println(onex.number.getNumber());
				fact.exit=true;
				return;
				
			}
			
			/*FactorialJob job=null;
			if(fact.factorialJobs.size()>0)
			{
				job = (FactorialJob)fact.factorialJobs.removeFirst();
			}
			if(!(job instanceof FactorialJob))
			continue; 
			fact.updating=true;
			if (fact.number.getNumber().equals("1"))
			{
				System.out.println("Got First Number");
				fact.number=job.number;
			}
			else
			{
				System.out.println("Multiplying");
				fact.number = fact.number.mul(job.number);
				
			}
			fact.done++;
			if(fact.max==fact.done)
			{
			    System.out.println("Here");
			    System.out.println(fact.number.getNumber());
		   		System.out.print(new Date());
		  		return;
				
			}
			fact.updating=false;
			*/
		}
	}
}
class FactorialJobThread extends Thread implements RemoteJobInterface
{
	private FactorialJob job;
	private Factorial fac;
	public FactorialJobThread(FactorialJob fj,Factorial f)
	{
		job=fj;
		fac=f;
	}
	public void run()
	{
		try
		{
			UnicastRemoteObject.exportObject(this);
			SchedularInterface server = (SchedularInterface)Naming.lookup(fac.scheduler);
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
	public void setStatus(int status,GridJob job) throws RemoteException
	{
						
	}
	public void setStatus(int status) throws RemoteException
	{
				
	}
	public void setResult(GridJob job) throws RemoteException
	{
		//System.out.println(facJob.number.getNumber());
		fac.addJob(job);
		UnicastRemoteObject.unexportObject(this,true);
	}
}
class FactorialJobThreadEx extends Thread implements RemoteJobInterface
{
	private FactorialJobEx job;
	private Factorial fac;
	public FactorialJobThreadEx(FactorialJobEx fj,Factorial f)
	{
		job=fj;
		fac=f;
	}
	public void run()
	{
		try
		{
			UnicastRemoteObject.exportObject(this);
			SchedularInterface server = (SchedularInterface)Naming.lookup(fac.scheduler);
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
	public void setStatus(int status,GridJob job) throws RemoteException
	{
						
	}
	public void setStatus(int status) throws RemoteException
	{
				
	}
	public void setResult(GridJob job) throws RemoteException
	{
		//System.out.println(facJob.number.getNumber());
		fac.addJobEx(job);
		UnicastRemoteObject.unexportObject(this,true);
		
		///facJob=null;
		
		
	}
}
public class Factorial implements Remote
{
	public boolean updating;
	public LargeNumber number;
	public int max;
	private int from;
	private int to;
	private int threshold;
	private int num;
	public  LinkedList factorialJobs = new LinkedList();
	public  LinkedList factorialJobsEx = new LinkedList();
	private LinkedList pendingFactorialJobs = new LinkedList();
	public String scheduler;
	private String address;
	public int done;
	public int doneEx;
	private boolean mulThreadStarted;
	public boolean exit;
	public Factorial(int n)
	{
		done=0;
		exit=false;
		doneEx=0;
		mulThreadStarted=false;
		max=0;
		from=0;
		to=0;
		threshold=0;
		scheduler="rmi://192.168.0.4/schedular";
		num=n;
		number = new LargeNumber(new StringBuffer("1"));
		updating=false;
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			address= inet.getHostAddress();
			
		}
		catch(java.net.UnknownHostException une)
		{
			une.printStackTrace();
			
		}
		catch(Exception e)
		{
		}
		
		
	}
	private void createFactorialJobs()
	{
		threshold=num/100;
		int i=0;
		int pieces = num/threshold;
		System.out.println(new Date());	
		if(num%pieces!=0)
		{
			max=pieces+1;
			for(i=0;i<max-1;i++)
			{
				from = (i*threshold)+1;
				to   = (i+1)*threshold;
				System.out.println(from+" "+to);
				FactorialJob job = new FactorialJob(new LargeNumber(new StringBuffer(""+from)),i+1,from,to);
				job.source=address;
				job.intensity=GridJob.COMPUTATION_INTENSIVE;
				job.force=false;
				job.serviceName="Factorial";
				try
				{
					Thread.sleep(500);
				}
				catch(Exception e)
				{
					
				}
				FactorialJobThread thread = new FactorialJobThread(job,this);
				//thread.setDaemon(true);
				thread.start();
					
			}
			from = (i*threshold)+1;
			to 	 = num;
			FactorialJob job = new FactorialJob(new LargeNumber(new StringBuffer(""+from)),i+1,from,to);
			job.source=address;
			job.intensity=GridJob.COMPUTATION_INTENSIVE;
			job.force=false;
			job.serviceName="Factorial";
			System.out.println(from+" "+to);
			if(from!=to)
			{			
				
				try
				{
					Thread.sleep(500);
				}
				catch(Exception e)
				{
					
				}
				FactorialJobThread thread = new FactorialJobThread(job,this);
				//thread.setDaemon(true);
				thread.start();
			}
			else
			addJob(job);
			
			
		}
		else
		{
			max=pieces;
			System.out.println("Max==Pieces");
			for(i=0;i<max;i++)
			{
				from = (i*threshold)+1;
				to   = (i+1)*threshold;
				FactorialJob job = new FactorialJob(new LargeNumber(new StringBuffer(""+from)),i+1,from,to);
				job.source=address;
				job.intensity=GridJob.COMPUTATION_INTENSIVE;
				job.force=false;
				job.serviceName="Factorial";
				System.out.println(from+" "+to);
				try
				{
					Thread.sleep(500);
				}
				catch(Exception e)
				{
					
				}
				FactorialJobThread thread = new FactorialJobThread(job,this);
				//thread.setDaemon(true);
				thread.start();
			}
		}
	}
	
	synchronized public void addJob(GridJob job)
	{
		done++;
		factorialJobs.addFirst(job);
		if(factorialJobs.size()>=2)
		{
			FactorialJob one = (FactorialJob)factorialJobs.removeFirst();
			FactorialJob two = (FactorialJob)factorialJobs.removeFirst();
			FactorialJobEx jobx = new FactorialJobEx(one.number,two.number,new LargeNumber(new StringBuffer("1")));
			jobx.source=address;
			jobx.intensity=GridJob.COMPUTATION_INTENSIVE;
			jobx.force=false;
			jobx.serviceName="Factorial";
			FactorialJobThreadEx thx = new FactorialJobThreadEx(jobx,this);
			thx.start();
			
		}
	}
	synchronized public void addJobEx(GridJob job)
	{
		factorialJobsEx.addFirst(job);
		doneEx++;
		if(factorialJobsEx.size()>=2)
		{
			FactorialJobEx one = (FactorialJobEx)factorialJobsEx.removeFirst();
			FactorialJobEx two = (FactorialJobEx)factorialJobsEx.removeFirst();
			FactorialJobEx jobx = new FactorialJobEx(one.number,two.number,new LargeNumber(new StringBuffer("1")));
			jobx.source=address;
			jobx.intensity=GridJob.COMPUTATION_INTENSIVE;
			jobx.force=false;
			jobx.serviceName="Factorial";
			FactorialJobThreadEx thx = new FactorialJobThreadEx(jobx,this);
			thx.start();
		}
		else if(doneEx == max-1)
		{
			
			MultiplyThread th = new MultiplyThread(this);
			th.start();
		}
	}
	public void addPendingJob(FactorialJob job)
	{
		pendingFactorialJobs.add(job);
	}
	public static void main(String args[])
	{
		int num = Integer.parseInt(args[0]);
		Factorial fact = new Factorial(num);
		fact.createFactorialJobs();
		while(!fact.exit)
		{
			try
			{
				Thread.sleep(1000);
				
				
			}
			catch(Exception e){}
		}
		
	}
}