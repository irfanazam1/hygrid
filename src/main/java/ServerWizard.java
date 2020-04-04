import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.*;
import java.util.Iterator;
public class ServerWizard
{
	
	private String className;
	private String jobName;
	private String threadName;
	public ServerWizard(String s1)
	{
		className = s1;
		threadName = className+"ProcessThread";
	}
	private void createFiles()
	{
		String classFile = className+"Server.java";
		File file = new File(classFile);
		if(file.exists())
		{
			System.out.println("This class already exists please specify another name");
			System.exit(0);
		}
		classFile = threadName+".java";
		file = new File(classFile);
		if(file.exists())
		{
			System.out.println("This class already exists please specify another name");
			System.exit(0);
		}
		String contents="\nimport java.rmi.*;"+
		"\nimport java.rmi.server.*;"+
		"\nimport java.util.Hashtable;"+
		"\nimport java.util.HashSet;"+
		"\nimport java.util.Iterator;"+
		"\nimport java.net.InetAddress;"+
		"\nimport java.util.ArrayList;"+
		"\nclass "+ threadName+" extends Thread"+
		"\n{"+
		"\n	private "+className+"Server Server;"+
		"\n	private String address;"+
		"\n	private "+className+"Job job;"+
		"\n	public "+threadName+"("+className+"Server server,GridJob j,String add)"+
		"\n	{"+
		"\n		Server=server;"+
		"\n		job=("+className+"Job)j;"+
		"\n		address=add;"+
		"\n	}"+
		"\n	public void run()"+
		"\n	{"+
		"\n    	try"+
		"\n  	{"+
		"\n    		//TODO: Job processing code here"+
		"\n    	}"	+
		"\n		catch(Exception e)"+
		"\n		{"+
		"\n			Server.reportError(job.jobId);"+
		"\n			e.printStackTrace();"+
		"\n		}"+
		"\n		Server.addResult(job);"+
		"\n	}"+
		"\n}"+

		"\npublic class "+ className+"Server extends UnicastRemoteObject implements JobInterface"+
		"\n{"+
		"\n	private static String SERVER;"+
		"\n	private static String SCHEDULAR;"+
		"\n private Hashtable results = new Hashtable();"+
		"\n	private static String myAddress;"+
		"\n	private static SchedularInterface schedular;"+
		"\n	private GridJob job;"+
		"\n	public static void main(String args[])"+
		"\n	{"+
		"\n		try"+
		"\n		{"+
		"\n			if(args.length != 1)"+
		"\n			{"+
		"\n				System.out.println(\"Correct Use: java QueryServer Scheduler Address\");"+
		"\n				System.exit(0);"+
		"\n			}"+
		"\n			InetAddress local = InetAddress.getLocalHost();"+
		"\n			myAddress=local.getHostAddress();"+
		"\n			SERVER=\"//\";"+
		"\n			SERVER+=myAddress;"+
		"\n			SERVER+=\"/Query\";"+
		"\n			SCHEDULAR = \"rmi://\"+args[0]+\"/schedular\";"+
		"\n			JobInterface server = new "+className+"Server();"+
		"\n			Naming.rebind(SERVER,server);"+
		"\n			schedular = (SchedularInterface)Naming.lookup(SCHEDULAR);"+
		"\n			if(schedular.registerService(\"rmi:\"+SERVER))"+
		"\n			{"+
		"\n				System.out.println(\"Server started sucessfully\");"+
		"\n			}"+
		"\n			else"+
		"\n			{"+
		"\n				System.out.println(\"Service is Already Registered\");"+
		"\n			}"+
		"\n		}"+
		"\n		catch(Exception e)"+
		"\n		{"+
		"\n			e.printStackTrace();"+
		"\n			System.exit(0);"+
		"\n		}"+
		"\n	}"+
		"\n	public "+className+"Server() throws RemoteException"+
		"\n	{"+
		"\n	}"+
		"\n	synchronized public void submitJob(GridJob job)"+
		"\n	{"+
		"\n		"+threadName+" thread = new "+threadName+"(this,job,myAddress);"+
		"\n		thread.start();"+
		"\n	}"+
		"\n	public GridJob getResult(int id)"+
		"\n	{"+
		"\n		if (results.containsKey(\" \"+id))"+
		"\n		{"+
		"\n			GridJob job = (GridJob)results.get(\" \"+id);"+
		"\n			results.remove(\" \"+id);"+
		"\n			return job;"+
		"\n		}"+
		"\n		else"+
		"\n		return null;"+
		"\n	}"+
		"\n	synchronized public void addResult(GridJob job)"+
		"\n	{"+
		"\n		if(schedular instanceof SchedularInterface)"+
		"\n		{"+
		"\n			try"+
		"\n			{"+
		"\n				schedular.setResult(job);"+
		"\n			}"+
		"\n			catch(RemoteException e)"+
		"\n			{"+
		"\n				e.printStackTrace();"+
		"\n			}"+
		"\n		}"+
		"\n		else"+
		"\n		results.put(\" \"+job.jobId,job);"+
		"\n	}"+
		"\n	public int getStatus(int id)"+
		"\n	{"+
		"\n		if(results.containsKey(\" \"+id))"+
		"\n		return 1;"+
		"\n		return 0;"+
		"\n	}"+
		"\n	synchronized public void reportError(int id)"+
		"\n	{"+
		"\n		try"+
		"\n		{"+
		"\n			schedular.reportError(id);"+
		"\n			System.out.println(\"Reporting Error\");"+
		"\n			System.exit(0);"+
		"\n		}"+
		"\n		catch(RemoteException re)"+
		"\n		{"+
		"\n			re.printStackTrace();"+
		"\n		}"+
		"\n	}"+
		"\n}";
	try
	{
		FileOutputStream out = new FileOutputStream(new File(className+"Server.java"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		writer.write(contents);
		writer.flush();
		out.close();
		System.out.println("Files Created Successfully");
	}
	catch(IOException ioe)
	{
		
	}
}
public static void main(String args[])
	{
		if(args.length!=1)
		{
			System.out.println("Proper Use: ServerWizard ClassName");
			System.exit(0);
		}
		ServerWizard wiz = new ServerWizard(args[0]);
		wiz.createFiles();
	}
}