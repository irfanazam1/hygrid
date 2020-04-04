import java.rmi.*;
import java.rmi.server.*;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;

class QueryProcessThread extends Thread
{
	private QueryServer Server;
	private String address;
	private QueryJob job;
	public QueryProcessThread(QueryServer server,GridJob j,String add)
	{
		Server=server;
		job=(QueryJob)j;
		address=add;
	
	}
	public void run()
	{
		Connection con = null;
    	Statement stmt=null;
    	
    	try
    	{
    		System.out.println("Job Started...");
    		System.out.println(job.handle);
    		
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
      		con = DriverManager.getConnection(job.handle,
        	job.user, job.pass);
        	
        	/*Driver Setup completed*/
        	stmt = con.createStatement();
        	ResultSet rs = stmt.executeQuery(job.query);
        	while(rs.next())
        	{
        		Iterator it = job.columns.iterator();
        		ArrayList row = new ArrayList();
        		while(it.hasNext())
        		{
        			String cName = (String)it.next();
        			row.add(rs.getString(cName));
           		}
           		job.rows.add(row);
        	}
        	job.destination=address;
        	job.type="RMI";
        	stmt.close();
		}
		catch(ClassNotFoundException cnfe)
		{
			Server.reportError(job.jobId);
			cnfe.printStackTrace();
			
			
		}
		catch(SQLException sqe)
		{
			Server.reportError(job.jobId);
			sqe.printStackTrace();
			
		}
		catch(Exception e)
		{
			Server.reportError(job.jobId);
			e.printStackTrace();
			
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
		Server.addResult(job);
		
		
	}
}

public class QueryServer extends UnicastRemoteObject implements JobInterface
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
				System.out.println("Correct Use: java QueryServer Scheduler Address");
				System.exit(0);
			}
			InetAddress local = InetAddress.getLocalHost();
			myAddress=local.getHostAddress();
			SERVER="//";
			SERVER+=myAddress;
			SERVER+="/Query";
			SCHEDULAR = "rmi://"+args[0]+"/schedular";
			JobInterface server = new QueryServer();
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
			System.out.println("Server started sucessfully");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	public QueryServer() throws RemoteException
	{
		
	}
  	synchronized public void submitJob(GridJob job)
	{
		QueryProcessThread thread = new QueryProcessThread(this,job,myAddress);
		thread.start();
		System.out.println("Starting job...");
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