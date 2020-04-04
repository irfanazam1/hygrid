import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.*;
import java.util.Iterator;
public class ClientWizard
{
	
	private String className;
	private String jobName;
	private String threadName;
	public ClientWizard(String s1,String s2)
	{
		className = s1;
		jobName=s2;
		jobName+="Job";
		threadName = className+"JobThread";
	}
	private void createFiles()
	{
		String classFile = className+".java";
		File file = new File(classFile);
		if(file.exists())
		{
			System.out.println("This class already exists please specify another name");
			System.exit(0);
		}
		classFile = jobName+".java";
		file = new File(classFile);
		if(file.exists())
		{
			System.out.println("This class already exists please specify another name");
			System.exit(0);
		}
		String contents="import java.rmi.*;\nimport java.rmi.server.*;\nimport java.net.InetAddress;"
+"\nimport java.util.HashSet;" 
+"\nimport java.util.ArrayList;"
+"\nimport java.util.Iterator;"
+"\nclass "+threadName+" extends Thread implements RemoteJobInterface"
+"\n{"
+"\n	private "+ className+" app;"
+"\n	private "+ jobName+" job;"
+"\n	public "+ threadName+" ("+className+" cls, "+jobName+" j)"
+"\n	{"
+"\n		app=cls;"
+"\n		job=j;"
+"\n	}"
+"\n	public void run()"
+"\n	{"
+"\n		try"
+"\n		{"
+"\n			UnicastRemoteObject.exportObject(this);"
+"\n			SchedularInterface server = (SchedularInterface)Naming.lookup(app.scheduler);"
+"\n			int result = server.submitJob(job,this);"
+"\n			if(result == SchedularInterface.USER_NOT_AUTHORIZED)"
+"\n			{"
+"\n				System.exit(0);"
+"\n			}"
+"\n			else if(result == SchedularInterface.MACHINE_NOT_AUTHORIZED)"
+"\n			{"
+"\n				//TODO: Your code here"
+"\n			}"
+"\n		}"
+"\n		catch(java.rmi.NotBoundException e)"
+"\n		{"
+"\n			//TODO: Your code here"
+"\n		}"
+"\n		catch(OutOfMemoryError err)"
+"\n		{"
+"\n			//TODO: Your code here"
+"\n		}"
+"\n		catch(Exception e)"
+"\n		{"
+"\n			//TODO: Your code here"
+"\n		}"
+"\n	}"
+"\n	public void setStatus(int status,GridJob job) throws RemoteException"
+"\n	{"
+"\n	   if (status==SchedularInterface.ELAPSED)"
+"\n	   {"
+"\n			//TODO: Your code here"
+"\n	   }"
+"\n	}"
+"\n	public void setStatus(int status) throws RemoteException"
+"\n	{"
+"\n			//TODO: Your code here"				
+"\n	}"
+"\n	public void setResult(GridJob job) throws RemoteException"
+"\n	{"
+"\n		app.addJob(job);"
+"\n		UnicastRemoteObject.unexportObject(this,true);"
+"\n	}"
+"\n}"
+"\npublic class "+ className
+"\n{"
+"\n	public String scheduler;"
+"\n	private String address;"
+"\n	public int done;"
+"\n	private int total;"
+"\n	public "+ className+" (String add)"
+"\n	{"
+"\n		try"
+"\n		{"
+"\n			InetAddress inet = InetAddress.getLocalHost();"
+"\n			address= inet.getHostAddress();"
+"\n			scheduler=add;"
+"\n		}"
+"\n		catch(java.net.UnknownHostException une)"
+"\n		{"
+"\n			//TODO: Your code here"				
+"\n		}"
+"\n		catch(Exception e)"
+"\n		{"
+"\n			//TODO: Your code here"			
+"\n		}"
+"\n	}"
+"\n	synchronized public void addJob(GridJob job)"
+"\n	{"
+"\n		done++;"
+"\n		"+jobName+" qj = ( "+jobName+" )job;"
+"\n		if(done==total)"
+"\n		{"
+"\n			//TODO: Your code here"
+"\n		}"
+"\n	}"
+"\n	private void createJobs()"
+"\n	{"
+"\n	}"
+"\n	public static void main(String args[])"
+"\n	{"
+"\n		String sch=\"rmi://"+"\"+args[0]+\"/schedular\";"
+"\n		"+className+" app = new "+className+"(sch);"
+"\n	}"
+"\n}";
String jobContents = "import java.io.Serializable;"
+"\nclass "+jobName+" extends GridJob implements Serializable"
+"\n{"
+"\n}";
	try
	{
		FileOutputStream out = new FileOutputStream(new File(className+".java"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		writer.write(contents);
		writer.flush();
		out.close();
		out = new FileOutputStream(new File(jobName+".java"));
		writer = new BufferedWriter(new OutputStreamWriter(out));
		writer.write(jobContents);
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
		if(args.length!=2)
		{
			System.out.println("Proper Use: ClientWizard ClassName JobClassName");
			System.exit(0);
		}
		ClientWizard wiz = new ClientWizard(args[0],args[1]);
		wiz.createFiles();
	}
}