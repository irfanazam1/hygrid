import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

class FileWriterThread extends Thread
{
	private DataJobServer Server;
	private GridJob job;
	private String address;
	private DataJob dataJob; 
	public FileWriterThread(DataJobServer server,GridJob j,String myAddress)
	{
		Server=server;
		job=j;
		address=myAddress;	
	}
	public void run()
	{
		/*Interfaces required for database connection and insertion*/
		Connection con = null;
    	Statement stmt=null;
    		
		try
		{
			//sleep(200);
			File outFile;
			FileOutputStream fos;
			
			dataJob = (DataJob)job;
			
							
			String file = dataJob.dataJobId+"_"+dataJob.sequence+".grd";
			String fileName=Server.getFolder()+"/"+file;
			
			/*File Writting process*/
			outFile = new File(fileName);
			fos = new FileOutputStream(outFile);
			fos.write(dataJob.buf,0,dataJob.fileSize);
			fos.flush();
			fos.close();
			/*File Writing Completed*/
			
			/*Setting up the return message of success*/
			dataJob.destination=address;
			dataJob.success=true;
			dataJob.type="RMI";
			dataJob.handle="rmi://"+address+"/DataStorage";
			dataJob.buf=null;
			dataJob.fileName=fileName;
			/*Setting up message finishied*/
			
			/*Setting up the mysql jdbc driver*/
    		
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
      		con = DriverManager.getConnection("jdbc:mysql://192.168.0.1/grid",
        	"grid", "grid");
               	
        	/*Driver Setup completed*/
        	String select = "select count(*) tot from datajob_desc where jobid= "+dataJob.dataJobId+" and seqno = "+dataJob.sequence;
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				int val = rs.getInt("tot");
				if(val!=1)
				{
					/*Adding a result in the table*/
					String insert = "insert into datajob_desc values ( "+
					dataJob.dataJobId+","+dataJob.sequence
					+","+"'"+fileName+"'"+","
					+"'"+address+"'"+","
					+"'RMI'"+","+"'"+dataJob.handle+"' )";
					Statement st = con.createStatement();
      				st.executeUpdate(insert);
      				st.close();		
				}
			}
		  stmt.close();
		  /*Returning the job to schedular*/
		  
      	  Server.addResult(dataJob);
			
		}
		catch(SQLException sqe)
		{
			System.out.println("Sql Exception");
			dataJob.destination=address;
			dataJob.success=false;
			Server.reportError(dataJob.jobId);
			sqe.printStackTrace();
			
			
		}
		catch(RemoteException e)
		{
			System.out.println("Remote Exception");
			dataJob.destination=address;
			dataJob.success=false;
			Server.reportError(dataJob.jobId);
			e.printStackTrace();
			
			
		}
		catch(ClassNotFoundException cnfe)
		{
			System.out.println("Class not found..");
			dataJob.destination=address;
			dataJob.success=false;
			Server.reportError(dataJob.jobId);
			cnfe.printStackTrace();
			
		}
		catch(IOException ioe)
		{
			System.out.println("Jobid= "+dataJob.jobId);
			System.out.println("IO Exception");
			dataJob.destination=address;
			dataJob.success=false;
			Server.reportError(dataJob.jobId);
			ioe.printStackTrace();
			
		}
		catch(Exception e)
		{
			System.out.println("Exception Occurred");
			dataJob.destination=address;
			dataJob.success=false;
			Server.reportError(dataJob.jobId);
			e.printStackTrace();
		}
		finally
		{
			try 
			{
        		if(con != null)
        		con.close();
        		
        		
      		} 
      		catch(SQLException e) {}
		}
	}

}

class FileReaderThread extends Thread
{
	private DataJobServer Server;
	private String brokerAddress;
	private String address;
	private DataJob dataJob; 
	public FileReaderThread(DataJobServer server,GridJob j,String bAddress,String myAddress)
	{
		Server=server;
		dataJob=(DataJob)j;
		brokerAddress="rmi://";
		brokerAddress+=bAddress;
		brokerAddress+="/brokerserver";
		address=myAddress;	
	}
	public void run()
	{
		try
		{
			int size=0;
			File inFile;
			FileInputStream fis;
			inFile = new File(dataJob.fileName);
			fis = new FileInputStream(inFile);
			size = fis.available();
			dataJob.buf = new byte[size];
			fis.read(dataJob.buf,0,size);
			dataJob.fileSize=size;
			dataJob.success=true;
			fis.close();
			Server.addResult(dataJob);
			
		}
		catch(Exception e)
		{
			
			Server.reportError(dataJob.jobId);
			e.printStackTrace();
			
		}
	}

}
class GetSharedFolderThread extends Thread
{
	private String broker;
	private DataJobServer dataServer;
	private String serverAddress;
	public GetSharedFolderThread(DataJobServer server,String bro,String address)
	{
		dataServer=server;
		broker="rmi://";
		broker+=bro;
		broker+="/brokerserver";
		serverAddress=address;
	}
	public void run()
	{
		while (true)
		{
			try
			{
				
				BrokerServerInterface in = (BrokerServerInterface)Naming.lookup(broker);
				dataServer.setFolder(in.getSharedFolder(serverAddress));
				sleep(5000);
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
public class DataJobServer extends UnicastRemoteObject implements JobInterface
{
	private static String SERVER=" ";
	private static String SCHEDULAR=" ";
    private DataJob dataJob;
	private Hashtable results = new Hashtable();
	private static String brokerAddress;
	private static String myAddress;
	private String folderName;
	private static SchedularInterface schedular;
	private static int count=0;
	public static void main(String args[])
	{	
		try
		{
			if(args.length != 2)
			{
				System.out.println("Correct Use: java MyMathServer Schedular IP Address Broker IP Adderss \n Example: java MyMathServer 192.168.1.10 192.168.1.200\n");
				System.exit(0);
			}
			InetAddress local = InetAddress.getLocalHost();
			brokerAddress=args[1];
			myAddress=local.getHostAddress();
			SERVER="//";
			SERVER+=myAddress;
			SERVER+="/DataStorage";
			SCHEDULAR = "rmi://"+args[0]+"/schedular";
			JobInterface server = new DataJobServer();
			Naming.rebind(SERVER,server);
			schedular = (SchedularInterface)Naming.lookup(SCHEDULAR);
			if(schedular.registerService("rmi:"+SERVER)) 
			{
				System.out.println("Server started sucessfully");
			}
			else
			{
				System.out.println("Service is Already Registered");
				//System.exit(0);
			}
			System.out.println("Server started sucessfully");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	public DataJobServer() throws RemoteException
	{
		GetSharedFolderThread th = new GetSharedFolderThread(this,brokerAddress,myAddress);
		th.setDaemon(true);
		th.start();
	}
  	synchronized public void submitJob(GridJob job)
	{
		DataJob dJob=(DataJob)job;
		if (dJob.operation==1)
		{
			FileWriterThread wr = new FileWriterThread(this,job,myAddress);
			wr.setDaemon(true);
			wr.start();
			//job=null;
		}
		else
		{
			Thread r = new FileReaderThread(this,dJob,brokerAddress,myAddress);
			r.setDaemon(true);
			r.start();
			//job=null;
		}
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
	public String getFolder()
	{
		return folderName;
	}
	public void setFolder(String address)
	{
	
		folderName=address;
	}
	synchronized public void reportError(int id)
	{
		try
		{
			schedular.reportError(id);
			System.out.println("Reporting Error");
			this.unexportObject(this,true);
			System.exit(0);
			
		}
		catch(RemoteException re)
		{
			re.printStackTrace();
		}
	}

	
}
