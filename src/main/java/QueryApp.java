import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.*;

class QueryAppJobThread extends Thread implements RemoteJobInterface
{
	private QueryApp app;
	private QueryJob job;
	public QueryAppJobThread(QueryApp Query,QueryJob j)
	{
		app=Query;
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
	   if (status==SchedularInterface.ELAPSED)
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
	public void setStatus(int status) throws RemoteException
	{
				
	}
	public void setResult(GridJob job) throws RemoteException
	{
		app.addJob(job);
		UnicastRemoteObject.unexportObject(this,true);
	}
}
public class QueryApp
{
	public String scheduler;
	private String address;
	public int done;
	private int total;
	public QueryApp(String add)
	{
		
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
		QueryJob qj = (QueryJob)job;
		Iterator rowIt = qj.rows.iterator();
		while(rowIt.hasNext())
		{
			ArrayList set  =(ArrayList)rowIt.next();
			Iterator it = set.iterator();
			while(it.hasNext())
			{
				System.out.print((String)it.next()+" : ");
			}
			System.out.println();
		}
		if(done==total)
		{
			System.out.println("Job Finished At: "+new Date());
		}
		
	}
	private void createJobs()
	{
		Connection con = null;
    	Statement stmt=null;
    
    	try
    	{
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
      		con = DriverManager.getConnection("jdbc:mysql://10.0.0.187/grid",
        	"grid", "grid");
        	/*Driver Setup completed*/
        	String query = "Select count(*) tot from centre";
        	stmt = con.createStatement();
        	ResultSet rs=stmt.executeQuery(query);
        	if(rs.next())
        	{
        		total=rs.getInt("tot");
        	}
        	query = "Select cencode from centre";
        	rs = stmt.executeQuery(query);
        	int count=0;
        	while(rs.next())
        	{
        		count++;
        		String str = "Select count(p.id) total,c.cenname cen"
        		+" from person p , centre c "
        		+" where p.cencode = c.cencode"
        		+" and c.cencode = "+rs.getInt("cencode")
        		+" group by c.cenname";
        		QueryJob job;
        		/*if(count<total/2)
        		{job = new QueryJob(str,"jdbc:mysql://192.168.0.1/grid","grid","grid");}
        		else
        		*/
        		job = new QueryJob(str,"jdbc:mysql://10.0.0.187/grid","grid","grid");
        		job.columns.add("total");
        		job.columns.add("cen");
        		job.source=address;
				job.userName="irfan";
				job.intensity=GridJob.COMPUTATION_INTENSIVE;
				job.serviceName="Query";
				job.force=false;
				QueryAppJobThread th = new QueryAppJobThread(this,job);
				th.start();
        	}
        	stmt.close();
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			System.exit(0);
			
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		finally
		{
			if(con!=null)
			{
				try
				{
					con.close();
					
				}	
				catch(SQLException e)
				{
				}
			}
			
		}
		
	}
	public static void main(String args[])
	{
		String sch="rmi://"+args[0]+"/schedular";
		QueryApp Query = new QueryApp(sch);
		System.out.println("Job Started At: "+new Date());
		Query.createJobs();
		
		
				
	}
}