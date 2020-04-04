import java.rmi.*;
import java.rmi.server.*;
import java.util.Hashtable;
import java.net.InetAddress;
class PopcornProcessThread extends Thread
{
	private PopcornServer Server;
	private PopcornJob job;
	private String address;
	int h, w;
	double t = 1.5, b = -1.5, l = -2.0, r = 2.0;
	public PopcornProcessThread(PopcornServer server,GridJob j,String add)
	{
		Server=server;
		job=(PopcornJob)j;
		address=add;
	
	}
	public void run()
	{
		/*Interfaces required for database connection and insertion*/
		try
		{
		   
		   
		   	double x, y, xnew, ynew;
    		int i, j, k;
    		h = job.height; w = job.width;
    		job.points = new MyPoint[4*(w/10+1)*500];
    		int count=0;
    		for (j=job.from; j<=job.to; j+= 10)
    		{
      			for (i = 0; i<=w; i += 10) 
      			{
        			x = (double)i / w * (r - l) + l;
        			y = (double)j / h * (b - t) + t;
        			for (k = 0; k < 500; k++) 
        			{
          				xnew = newX(x, y);
          				ynew = newY(x, y);
          				x = xnew; y = ynew;
          				MyPoint point = new MyPoint((short)transX(x),(short)transY(y));
          				job.points[count++]=point;
          			}
        
      			}
      		}
    
		   job.type="RMI";
		   job.handle="rmi://"+address+"/Popcorn";
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
		job=null;
	}
	public double newX(double x, double y)
  	{
    	return x - 0.05 * Math.sin(y + Math.tan(3 * y));
  	}
  
  	public double newY(double x, double y)
  	{	
    	return y - 0.05 * Math.sin(x + Math.tan(3 * x));
  	}
  	
  	int transX(double x)
  	{
    	return (int)((double)(x - l) / (r - l) * w);
  	}
   	int transY(double y)
   	{
    	return (int)((double)(y - t) / (b - t) * h);
  	}
  
}

public class PopcornServer extends UnicastRemoteObject implements JobInterface
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
			SERVER+="/Popcorn";
			SCHEDULAR = "rmi://"+args[0]+"/schedular";
			JobInterface server = new PopcornServer();
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
	public PopcornServer() throws RemoteException
	{
		
	}
  	synchronized public void submitJob(GridJob job)
	{
			
		PopcornProcessThread thread = new PopcornProcessThread(this,job,myAddress);
		thread.setDaemon(true);
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
				job=null;
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