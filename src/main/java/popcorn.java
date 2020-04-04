import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Event;
import java.awt.Color;
import java.applet.Applet;
import java.util.Date;
import java.util.LinkedList;
import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import javax.swing.*;
import java.awt.Dimension;
class DrawThread extends Thread
{
	private popcorn pop;
	private Graphics graphics;
	PopcornJob job;
	public DrawThread(popcorn p,Graphics g)
	{
		pop = p;
		graphics = g;
	}
	public void run()
 	{
 		PopcornJob job=null;
		while(true)
		{
			if(pop.popcornJobs.size()<=0)
			{
			
				try
				{
					Thread.sleep(50);
				}
				catch(Exception e)
				{
			
				}
				continue;
				
			}
			else
			{
				job = (PopcornJob)pop.popcornJobs.removeFirst();
			}
			for(int i=0;i<job.points.length;i++)
			{
				MyPoint point = job.points[i];
				if(point instanceof MyPoint)
				{
					graphics.drawLine(point.x, point.y, point.x, point.y);
					
				}
			}
			job=null;
			pop.done++;
			if(pop.done==pop.max)
			{
				System.out.println("End: "+new Date());
				break;
			}
			
		
		}
			
	}

	
}
class PopcornJobThread extends Thread implements RemoteJobInterface
{
	private PopcornJob job;
	private popcorn pop;
	public PopcornJobThread(PopcornJob pj,popcorn p)
	{
		job=pj;
		pop=p;
	}
	public void run()
	{
		try
		{
			UnicastRemoteObject.exportObject(this);
			SchedularInterface server = (SchedularInterface)Naming.lookup(pop.scheduler);
			int result = server.submitJob(job,this);
			if(result == SchedularInterface.MACHINE_NOT_AUTHORIZED || result == SchedularInterface.USER_NOT_AUTHORIZED)
			{
				System.out.println("You are not authorized to run this program");
				return;
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
				
	}
	public void setResult(GridJob job) throws RemoteException
	{
		pop.addJob(job);
		UnicastRemoteObject.unexportObject(this,true);
	}
}
class SubmitPopcornJobsThread extends Thread
{
	private popcorn pop;
	private int threshold;
	private int width,height;
	private String address;
	public SubmitPopcornJobsThread(popcorn p,int th,int w,int h,String add)
	{
		pop=p;
		threshold=th;
		width=w;
		height=h;
		address=add;
	}
	public void run()
	{
		System.out.println("Start: "+new Date());	
		for(int i=0;i<pop.max;i++)
		{
			int from = (i*threshold)+1;
			int to   = (i+1)*threshold;
			PopcornJob job = new PopcornJob(i+1,from,to,width,height);
			job.source=address;
			job.intensity=GridJob.COMPUTATION_INTENSIVE;
			job.force=false;
			job.userName=popcorn.username;
			job.serviceName="Popcorn";
			try
			{
				Thread.sleep(20);
			}
			catch(Exception e)
			{
			}
			PopcornJobThread thread = new PopcornJobThread(job,pop);
			thread.start();
		}
	}
}
public class popcorn extends JFrame{
  Thread drawThread = null;
  public  LinkedList popcornJobs = new LinkedList();
  public int done;
  public static String scheduler;
  public static String username;
  public int max;
  private String address;
  private Graphics gcont;
  private boolean started;
  private boolean painted;
  
  public popcorn()
  {
  	
  }
  public void init()
  {
    String s;
    painted=false;
    max=0;
    started=false;
    done = 0;
    try
	{
		InetAddress inet = InetAddress.getLocalHost();
		address= inet.getHostAddress();
			
	}
	catch(java.net.UnknownHostException une)
	{
		une.printStackTrace();
			
	}
	
  }
  public void stop()
  {
    drawThread = null;
    gcont = null;
  }
  
  public boolean action(Event e)
  {
    switch (e.id) {
      case Event.WINDOW_DESTROY:
        System.exit(0);
        return true;
      default:
        return false;
    }
  }
    
  public void paint (Graphics g)
  {
    gcont = g.create();
    gcont.setColor(Color.RED);
    if(!painted)
    {
    	
    	createPopcornJobs();	
    	painted=true;
    }
    
       
  }
  public void addJob(GridJob job)
  {
  	popcornJobs.addFirst(job);
  	if(!started)
  	{
  		DrawThread th = new DrawThread(this,gcont);
  		th.start();
  		started=true;
  	}
  	
  }
  private void createPopcornJobs()
  {
		Rectangle rect = getBounds();
		int num = rect.height-(rect.height%10);
		int threshold=num/20;
		int i=0;
		int pieces = num/threshold;
		max=pieces;
		SubmitPopcornJobsThread th = new SubmitPopcornJobsThread
		(this,threshold,rect.width,rect.height,address);
		th.start();
		
		
   }
 	public static void main(String args[])
	{
		if(args.length!=2)
		{
			System.out.println("Proper Use: [Scheduler Address] [Username]");
			System.exit(0); 
		}
		scheduler="rmi://"+args[0]+"/schedular";
		username = args[1];
		popcorn pop = new popcorn();
		pop.pack();
		pop.setSize(new Dimension(1024,768));
		pop.init();
		pop.setBackground(Color.WHITE);
		pop.show();
		
	}

}
