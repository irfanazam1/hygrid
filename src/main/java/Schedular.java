import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;

class TargetSystem
{
	/**
	*IP address of the target system
	*/
	public String address;
	/**
	*Service handle of the service running on the system
	*/
	public String handle;
	/**
	*Type of the service
	*/
	public String type;
	
	public TargetSystem(){}
	public TargetSystem(String sAddress,String sHandle,String sType)
	{
		address  =  sAddress;
		handle	 =  sHandle;
		type	 =  sType;
	}	
}
/*Submit Thread will be used to submit a job and tests whether the remote
system is working or not. It changes the status of the job according to the
situations
*/

class SubmitThread extends Thread
{
	
	private GridJob gridJob;
	/*Used for getting the instance of remote server ready for processing jobs*/
	private JobInterface gface;
	/*The schedular object, required for using schedular facilities*/
	private Schedular Server;
	/*Target system to be filled by the schedular*/
	private TargetSystem target;
	public SubmitThread(Schedular server)	
	{
		Server = server;
	}
	public void run()
	{
		/*Thread will run untill the schedular needs its running*/
		while (Server.submit)
		{
				
			try
			{
				sleep(100); //Sleep is used for proper scheduling of threads
			}// End Try
			catch(Exception e)
			{
				e.printStackTrace();
			}// End Catch
			//Getting a job from the ready queue
			gridJob = Server.getReadyJob();
			//If there is a job in the queue 
			if (gridJob instanceof GridJob) 
			{ 
               	Server.jobThreadBusy=true;  //Now the submission thread is busy
				try 
				{
					/*Checking whether force is on. If yes the job
					 will be automatically transferred to the given 
					 target machine
					*/
					if(gridJob.force)  
					{
					   target = new TargetSystem(gridJob.destination,gridJob.handle,gridJob.type);
					}
					/*Else get the target machine by cheking the 
					available resources on the network
					*/
					else
					{
						target = Server.getTarget(gridJob.serviceName,gridJob.intensity);
					}
					
					if ( target instanceof TargetSystem)
					{
                  		
						if(target.type.equals("RMI"))
						{
							gface = (JobInterface) Naming.lookup(target.handle);//Get the Job processing server interface 
							gface.submitJob(gridJob); //The job submission function of jobs processing server 						
							/*Incrementing load for the machine
							it will be incremented each time when
							a job is submitted to a machine
							*/
							Server.incrementTargetLoad(target.address);
							/*Now the job is process*/
							Server.jobsProcessed.put(" "+gridJob.jobId,gridJob); 
							/*Jobs inprocess is a hashtable
							It could be skipped for proper working but
							it is only used due to memory problems.
							A job could contain large amounts of data 
							so for later use the ids of the processed
							job are seperately stored to get information
							about the processing jobs
							*/
							Server.jobsInProcess.put(" "+gridJob.jobId," "+gridJob.jobId);
							/*Checking whether the thread is started or not*/
							if (!Server.isCheckDestinationThreadStarted)
							{
								Server.startCheckDestinationThread();
								Server.isCheckDestinationThreadStarted=true;
							}// if block 
							
						} //if block
						//else if code for invoking a grid service
						/*Updating the job information*/
						JobInformation info = Server.getJobInformation(gridJob.jobId);
						info.startDate = new Date();
						info.status="Processing"; 
						info.destination=target.address;
						info.handle=target.handle;
						info.type=target.type;
						Server.updateJobInformation(info);
				
					}//if block
					else
					{
                      	processWaitingJobs(gridJob, " ");
						
					}//else block
					
				}//try block
			  
				catch(RemoteException remoteException)
				{
					System.out.println("Error Calling the remote method, remote server might not be ready");	
					processWaitingJobs(gridJob,target.handle);
				}//catch block
				catch(NotBoundException notBoundException)
				{
					System.out.println("Error Connecting  the remote host");
					processWaitingJobs(gridJob,target.handle);					
				}//catch block
				catch( java.net.MalformedURLException URLException)
				{
					System.out.println("Malformed URL is given to connect to the remote host");
					processWaitingJobs(gridJob,target.handle);
				}//catch block
				catch(Exception e)
				{
					e.printStackTrace();
					processWaitingJobs(gridJob,target.handle);
									
			    }//catch block
				Server.jobThreadBusy=false;
			}//if block
			
			
		}//while block
		 
	}//run method block	
	private void processWaitingJobs(GridJob gridJob, String handle)
	{
		/*Incase of errors as described in the first section
		job will be included in the waiting queue
		and removed from the processing queues
		*/
		Server.addJobToWaitingQueue(gridJob);
		
		if(!Server.isWaitingThreadStarted)
		{
			Server.startWaitingThread();
		}//if block
		JobInformation info = Server.getJobInformation(gridJob.jobId);
		info.startDate = new Date();
		info.status="Waiting"; 
		info.destination="Unknown";
		info.type="Unknown";
		info.handle=handle;
		Server.updateJobInformation(info);
		Server.jobsProcessed.remove(" "+gridJob.jobId);
		Server.jobsInProcess.remove(" "+gridJob.jobId);
		Server.addBadTarget(handle);
                
	}
}//class 

/*This Thread checks whether the target machines processing
the jobs are still processing or now down due to some failures.
If a machine processing the job is no more working then it will
put the job into waiting queue by makring the target as a bad target
*/
class CheckDestinationThread extends Thread
{
	private Schedular Server;
	private GridJob job;
	private JobInterface gface;
	private JobInformation info;
	private int value;
	public CheckDestinationThread(Schedular server)
	{
		Server = server;
		value=0;
	}
	public void run()
	{
		
		while (true)
		{
		    
			 if (Server.jobsInProcess.size()>0)
			 {
				try
				{
					sleep(2000);
					Enumeration elements = Server.jobsInProcess.keys();
					while (elements.hasMoreElements())
					{
							String key=(String)elements.nextElement();
							value = Integer.parseInt(key.trim());
							info = Server.getJobInformation(value);
							if(! (info instanceof JobInformation))
							continue;
							
							if(info.type.equals("RMI"))
							{
								gface = (JobInterface)Naming.lookup(info.handle);
								if(!(gface instanceof JobInterface))
								{
									System.out.println("Destination is not Ready");
								}
								int status = gface.getStatus(value);
								
							}
						}
							
			
					}
					catch(RemoteException remoteException)
					{
						System.out.println("Error Calling the remote method, remote server might not be ready,In the result thread");	
						job = (GridJob)Server.jobsProcessed.get(" "+value);
						Server.addJobToWaitingQueue(job);
						
						if(!Server.isWaitingThreadStarted)
						{
							Server.startWaitingThread();
							
						}
						
						info.startDate = new Date();
						info.status="Waiting";
						Server.updateJobInformation(info);
						Server.jobsProcessed.remove(" "+job.jobId);
						Server.jobsInProcess.remove(" "+job.jobId);
						/*Adding target to bad targets*/
						Server.addBadTarget(info.handle);
					
					}
					catch (NotBoundException nbe)
					{
						System.out.println("Error Calling the remote method, remote server might not be ready,In the result thread");	
						job = (GridJob)Server.jobsProcessed.get(" "+value);
						Server.addJobToWaitingQueue(job);
						
						if(!Server.isWaitingThreadStarted)
						{
							Server.startWaitingThread();
							
						}
						
						info.startDate = new Date();
						info.status="Waiting";
						Server.updateJobInformation(info);
						Server.jobsProcessed.remove(" "+job.jobId);
						Server.jobsInProcess.remove(" "+job.jobId);
						Server.addBadTarget(info.handle);
					}
					
					catch(Exception e)
					{
						e.printStackTrace();
						job = (GridJob)Server.jobsProcessed.get(" "+value);
						Server.addJobToWaitingQueue(job);
						
						if(!Server.isWaitingThreadStarted)
						{
							Server.startWaitingThread();
							
						}
						
						info.startDate = new Date();
						info.status="Waiting";
						Server.updateJobInformation(info);
						Server.jobsProcessed.remove(" "+job.jobId);
						Server.jobsInProcess.remove(" "+job.jobId);
						Server.addBadTarget(info.handle);
					
					}
		 		}
				else
				{
					  Server.isCheckDestinationThreadStarted=false;
					  return;
				}
				
			}
			
	 }
	
}
/*This Thread will take care of the jobs in the waiting queue.
Puts the job into the ready queue after a set time and icreases 
the waiting count for the job so that the time could be elapsed after
a the set elapsed time
*/
class WaitingJobsThread extends Thread
{
	private Schedular Server;
	private GridJob job;
	private Integer val;
	public WaitingJobsThread(Schedular server)	
	{
		Server=server;
		
	}
	public void run() 
	{
		try
		{
			/*This will make the thread dependent upon the schedular
			which can make the thread inactive when it is no more needed
			*/
			Loop: while(Server.waiting)
			{
			    /*Wait for a set time*/
				sleep(Server.wait);
				/*Get a job from the waiting queue*/
				job=Server.getWaitingJobs();
				
				if( job instanceof GridJob)
				{
					/*Now the waiting thread is busy and can't be terminated*/
					Server.waitingThreadBusy=true;
					/*Check whether the job was already in the waiting queue*/
					if (Server.wJobs.containsKey(" "+job.jobId))
					{
						val = (Integer)Server.wJobs.get(" "+job.jobId);
						int count=val.intValue();
						/*
						If the elapse count is less than the set count then
						increase count
						*/
						if(count<=Server.elapsed)
						{
							
							Server.wJobs.put(" "+job.jobId,new Integer(count+1));
							JobInformation info = Server.getJobInformation(job.jobId);
							info.startDate = new Date();
							info.status="Waiting"; 
							Server.updateJobInformation(info);
							
					
						}//end if block
						/*The elapse count is finished so end the waiting status 
						and mark the status as elapsed. Remove the jobe
						from the waiting queue and return the job to the
						source of the job
						*/
						else
						{
							/*This method will return the job to the source*/
							Server.addWaitingTimeElapsed(job.jobId,job);
							/*Remove the job to be checked for waiting*/
							Server.wJobs.remove(" "+job.jobId);
							JobInformation info = Server.getJobInformation(job.jobId);
							info.finishDate = new Date();
							info.status="Waiting Time Elapsed"; 
							Server.updateJobInformation(info);
							continue Loop;
						}//else
						
					}//if
					/*If the job was not waiting before now put the 
					job in wJobs table so that i could be checked for
					next time
					*/
					else
					{
						Server.wJobs.put(" "+job.jobId,new Integer(1));
					}//else
				    	
					/*Now test the job for resubmission so that submission 
					thread could restart the job if there is any target system
					is available, othewise the job will be returned back 
					to the waiting queue and waiting thread will process
					that job agaib
					*/
					Server.addJobReadyQueue(job);
				    Server.waitingThreadBusy=false;   	
				}//if block				
				
			}//while

		}//try
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
/*Management thread will manage the threads runnuing and the 
tables containing the information about the network machines.
It will communicate with the Broker to get the machines information
participating on GROD
*/
class ManagementThread extends Thread
{
	private Schedular Server;
	private int waitingThreadCount;
	private int jobThreadCount;
	private int jobSize;
	private int waitingSize;
    public ManagementThread(Schedular server)
	{
		Server=server;
		waitingThreadCount=0;
		jobThreadCount=0;
		jobSize=0;
		waitingSize=0;
        
	}
	public void run() 
	{
		/*Run till the schedular itself is running.*/
		while (true)
		{
			try
			{
		         /*Wait for a set amount of time*/
		         sleep(Server.management);
				 /*Get the system informations from the broker*/
				 BrokerServerInterface bsi = (BrokerServerInterface)Naming.lookup(Server.broker);
	 		 	 Hashtable ht = bsi.getSysInfo();
				 /*Check whether any machine is participating on GRID*/
				 if(ht instanceof Hashtable)
				 /*Updating the schedular information about the systems*/
				 Server.updateSystemInfo(ht);
				 jobSize=Server.readyQueueSize();
				 waitingSize=Server.waitingQueueSize(); 
				 /*Checking whether the submission thread should run or not
				 if the conditions are met then stop the thread
				 The conditions are 
				 1. The ready queue must not have any job
				 2. Submission thread must not be busy
				 3. And the thread must be started to be stopped
				 */
				 if(jobSize<=0 && !Server.jobThreadBusy && Server.isJobThreadStarted)
				 {
				 	
					if( jobThreadCount > 5)
				 	{
				 		Server.submit=false;
						Server.isJobThreadStarted=false;
						jobThreadCount=0;
						System.out.println("Job Thread Terminated...");
				 	}
			         	++jobThreadCount;
				 }
				 /*Same descriptions as for the submission thread*/
				 if(waitingSize<=0 && ! Server.waitingThreadBusy && Server.isWaitingThreadStarted)
				 {
				 	if( waitingThreadCount > 5)
				 	{
				 		Server.waiting=false;
						Server.isWaitingThreadStarted=false;
						waitingThreadCount=0;
						
				 	}
			         	++waitingThreadCount;
				 }
				 
				 /*Starting the submission thread after termination if the 
				 ready queue size is greater than zero
				 */
				 if (!Server.isJobThreadStarted && jobSize>0)
				 {
				 	Server.startJobThread();
				 }
				 /*Same as above*/
				 if (!Server.isWaitingThreadStarted&&waitingSize>0)
				 {
				 	Server.startWaitingThread();
				 }
                 
				  
	        }
			catch(RemoteException re)
			{
				re.printStackTrace();
			}
			catch(NotBoundException nbe)
			{
				
				nbe.printStackTrace();
			}
			catch(java.net.MalformedURLException mfe)
			{
				mfe.printStackTrace();
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
			
	}
}

/**
*Schedular is the job submission and retrievel system which takes job through 
*the scheudlar interface and submits the job to machines participating their resources
*on the GRID. It selects the machines based on three criterion defined in the
*GridJob class i.e. The intensity of the job. It implements various states
*for a job which are 
*<pre>
 *1. READY: The status of the job recently submitted and ready to be piecked up
     by the schedular for submission.
 *2. PROCESSING: The status of the job inprocess.
 *3. WAITING: The status of a job which is in a waiting state.
 * 	 This state could be due to these conditions.
 *	 i) Their is no machine participating on the GRID.
 *   ii) Their is no machine which has resources according to the set
 * 	 	 Thresholds, set through the schedular settings utility.
 * 	 iii) The target machine is down or the  particular 
 *	 	  service running on that machine is marked as a bad target.
 * 	 iv) The target machine was processing the job but returned error due to some
 *	     problem at its end.
 *4. ELAPSED: Job is checked for a specific time in waiting state.
 *   If the waiting time becomes greater than the set elapsed time then job
 *   will be marked as elapsed and will be returned to the source.
 *5. FINISHED: The job is done and will be returned or ready to be retunred to
 *   the source. Job will be retunred automatically if the callback facility is
 *   used otherwise the source will get the job by calling a method of schedular.
 *6. UNKNOWNSTATE: Some error occured in the scheudular.
 *7. NOTSUPPORTED: The service requested is not supported by the schedular.

*Scheduling Algorithm: The scheduling algorithm is FIFO both for sumbission and getting results
*Hybrid Scheduling: This schedular provides the mechanism of scheduling jobs
*which can be data or computation intensive, at the same time. For this purpose
*"intensity" field is provided in the GridJob class which tells the nature of ths job.
*Forcing Jobs: The force field in the GridJob class can be used to force the job
*to be sent to a specific machine without checking the vailable resources.
*Note: This system will accept only the objects derived from the GridJob class.
*</pre>	     
*/
/* This class stores the information about the target machine, will be
filled by the schedular after selecting a machine as a target for the 
current job
*/
public class Schedular extends UnicastRemoteObject implements SchedularInterface
{
	
	
	/**
	*Various status for the jobs
	*/
	
	
	/**
	*Boolean value to check whether the submission thread is started or not
	*/
	public  boolean   isJobThreadStarted;
	/**
	*Boolean value to check whether the waiting thread is started or not
	*/
	public  boolean   isWaitingThreadStarted;
	/**
	*Boolean value to check whether the check destination thread is started or not
	*/
	public  boolean   isCheckDestinationThreadStarted;
	/**
	*Collects the ids and waiting counts of the jobs waiting
	*/
	public  Hashtable wJobs = new Hashtable();
	/**
	*Boolean value stop submission thread
	*/
	public  boolean   submit;
	/**
	*Boolean value stop waiting thread
	*/
	public  boolean   waiting;
	/**
	*Boolean value to check whether or not submission thread is busy?
	*/
	public  boolean   jobThreadBusy;
	/**
	*Boolean value to check whether or not waiting thread is busy?
	*/
	public  boolean   waitingThreadBusy;
	/**
	*String holding the broker address
	*/
	public  String broker;
	/**
	*String holding the security manager address
	*/
	public String securityAddress;
	/**
	*Integer value holding the Memory usage threshold. Range 0 to 100
	*/
	private int mem;
	/**
	*Integer value holding the CPU usage threshold. Range 0 to 100
	*/
	private int cpu;
	/**
	*Integer value holding the disk %usage threshold. Range 0 to 100
	*/
	private int disk;
	/**
	*Integer value holding the time for the waiting thread to recheck 
	*jobs for waiting
	*/
	public int wait;
	/**
	*Integer value holding the count for the waiting thread to recheck 
	*jobs waiting and mark the status ELAPSED if the count is increased
	*from the set threshold
	*/
	public int elapsed;
	/**
	*Integer value holding the time for the management thread to remanage 
	*thread states and system informations
	*/
	public int management;
	/**
	*Schedular service name
	*/
	private static String SERVER="schedular";
	/**
	*Value to assign an ID to jobs
	*/
	private static int count = 0; 
	/**
	*Thread to submit jobs
	*/
	private SubmitThread jobThread;
	/**
	*Thread to check the status of the machines processing the jobs
	*/
	private CheckDestinationThread CheckDestinationThread;
	/**
	*Thread to check the waiting jobs
	*/
	private WaitingJobsThread waitingThread; 
	/**
	*Thread to manage other threads and system inforamtions
	*/
	private ManagementThread managementThread;
	
	/**
	*Table holding the jobs in progress.
	*/
	public  Hashtable jobsProcessed = new Hashtable();
	/**
	*Table holding the ids of the jobs in progress.
	*/
	public  Hashtable jobsInProcess = new Hashtable();
	/**
	*Linked list serving as ready jobs queue
	*/
	private LinkedList readyQueue       =   new LinkedList(); 
	/**
	*Linked list serving as waiting jobs queue
	*/
	private LinkedList waitingQueue     =   new LinkedList(); 
	/**
	*Set hodling the ids of the jobs which are ready 
	*/
	private HashSet  jobsReady          =	new HashSet();
	/**
	*Set hodling the ids of the jobs which are waiting
	*/
	//private HashSet  jobsWaiting        = 	new HashSet();
	/**
	*Set hodling the ids of the jobs whom waiting count is elapsed
	*/
	private HashSet	 waitingTimeElapsed = 	new HashSet();
	/**
	*Set Hodling the ids of the jobs which provided the services not 
	*supported by th schedular
	*/
	private HashSet   serviceNotSupported = new HashSet();
	/**
	*Table hodling the jobs ready to be returned to the source
	*if the callback mechnism is not used
	*/
	private Hashtable results           = 	new Hashtable(); 
	/**
	*List hodling the information about the machines and service
	*participating on GRID
	*/
	private LinkedList config	        =   new LinkedList();
	/**
	*Table hodling the system informations of the machines and service
	*participating on GRID
	*/
	private Hashtable  sysInfo	        =   new Hashtable();
	/**
	*Table hodling the information about the jobs status
	*/
	private Hashtable jobInformation    =	new Hashtable();
	/**
	*Table hodling the information about target load
	*/
	private Hashtable targetLoad 	    =	new Hashtable();
	/**
	*Set hodling the targets marked as bad
	*/
    private HashSet   badTargets        =   new HashSet();
    /**
	*Table hodling the objects imported by the schedular
	*so that the results will be returned automatically using the
	callback mechanism
	*/
	private Hashtable objectsImported   =   new Hashtable();
	/**
	*Set hodling the information of the services registered with the
	schedular. This will also be used to remove a target from bad tagrgets
	list
	*/
    private HashSet  registeredServices =   new HashSet();
	
	/**
	*Set hodling the information of the machines registered with the
	*grid and can exploit the computational capabilities of grid.
	*/
	private HashSet  computationMachines = new HashSet();
	
	/**
	*Set hodling the information of the machines registered with the
	*grid and can exploit the data oriented capabilities of grid.
	*/
	private HashSet storageMachines = new HashSet();
	
	/**
	*Set hodling the information of the users registered with the
	*grid and can exploit the computational capabilities of grid.
	*/
	private HashSet computationUsers = new HashSet();
	
	/**
	*Set hodling the information of the users registered with the
	*grid and can exploit the data oriented capabilities of grid.
	*/
	private HashSet storageUsers = new HashSet();
	
	/**
	*Security Manager Instance
	*/
	private SecurityManagerInterface security;
	/**
	*Schedular User Interface
	*/
	private SchedularUI UI;
	public void startSchedular(SchedularUI ui)
	{	
		
		try
		{
			
			InetAddress inet = InetAddress.getLocalHost();
			SERVER="//";
			SERVER+=inet.getHostAddress();
			SERVER+="/schedular";
			Naming.rebind(SERVER,this);
			managementThread = new ManagementThread(this);
			managementThread.setDaemon(true);
			managementThread.start();
			isJobThreadStarted=false;
			isWaitingThreadStarted=false;
			isCheckDestinationThreadStarted=false;
			waiting=true;
			submit=true;
			waitingThreadBusy=false;
			jobThreadBusy=false;
			UI = ui;
			System.out.println("Server started sucessfully");
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void stopScheduler()
	{
		try
		{
			Naming.unbind(SERVER);
		}
		catch(Exception re)
		{}
	}
	public Schedular() throws RemoteException
	{
		mem=80;
		cpu=80;
		disk=80;
		wait=1000;
		elapsed=5;
		management=2000;
		broker = "rmi://127.0.0.1/brokerserver";
		securityAddress = "rmi://127.0.0.1/SecurityManager";
			
	}
	//Scheduler Interface Implementation
	synchronized public int submitJob(GridJob job,RemoteJobInterface objImported) throws RemoteException
	{
		boolean supported=false;
		if(job.intensity==GridJob.COMPUTATION_INTENSIVE)
		{
			if(computationMachines==null)
			{
				computationMachines = security.getNodesForComputation();
				if(computationMachines==null)
				return MACHINE_NOT_AUTHORIZED;
			}
			else if(!computationMachines.contains(job.source))
			{
				computationMachines = security.getNodesForComputation();
				if(!computationMachines.contains(job.source))
				return MACHINE_NOT_AUTHORIZED;
			}
			if(computationUsers==null)
			{
				computationUsers = security.getUsersForComputation();
				if(computationUsers==null)
				return USER_NOT_AUTHORIZED;
			}
			else if(!computationUsers.contains(job.userName))
			{
				computationUsers = security.getUsersForComputation();
				if(!computationUsers.contains(job.userName))
				{
					return USER_NOT_AUTHORIZED;
				}
			}
		}
		else if(job.intensity==GridJob.DATA_INTENSIVE)
		{
			if(storageMachines==null)
			{
				storageMachines = security.getNodesForStorage();
				if(storageMachines==null)
				return MACHINE_NOT_AUTHORIZED;
			}
			else if(!storageMachines.contains(job.source))
			{
				storageMachines = security.getNodesForStorage();
				if(!storageMachines.contains(job.source))
				return MACHINE_NOT_AUTHORIZED;
			}
			if(storageUsers==null)
			{
				storageUsers = security.getUsersForStorage();
				if(storageUsers==null)
				return USER_NOT_AUTHORIZED;
			}
			else if(!storageUsers.contains(job.userName))
			{
				storageUsers = security.getUsersForStorage();
				if(!storageUsers.contains(job.userName))
				{
					return USER_NOT_AUTHORIZED;
				}
			}
		}
		else
		{
			if(computationMachines==null||storageMachines==null)
			{
				storageMachines = security.getNodesForStorage();
				computationMachines = security.getNodesForComputation();
				if(computationMachines==null||storageMachines==null)
				return MACHINE_NOT_AUTHORIZED;
			}
			else if(!(storageMachines.contains(job.source) && computationMachines.contains(job.source)))
			{
				storageMachines = security.getNodesForStorage();
				computationMachines = security.getNodesForComputation();
				if(!(storageMachines.contains(job.source) && computationMachines.contains(job.source)))
				return MACHINE_NOT_AUTHORIZED;
			}
			if(storageUsers==null||computationUsers==null)
			{
				storageUsers = security.getUsersForStorage();
				computationUsers = security.getUsersForComputation();
				if(storageUsers==null||computationUsers==null)
				return USER_NOT_AUTHORIZED;
			}
			else if(!(storageUsers.contains(job.userName) && computationUsers.contains(job.userName)))
			{
				storageUsers = security.getUsersForStorage();
				computationUsers = security.getUsersForComputation();
				if(!(storageUsers.contains(job.userName) && computationUsers.contains(job.userName)))
				{
					return USER_NOT_AUTHORIZED;
				}
			}
		}
				
		int tot = config.size();
		for(int i=0;i<tot;i++)
		{
			SchedularConfig cnf = (SchedularConfig)config.get(i);
			if ( cnf.name.equals(job.serviceName) )
			{
				supported=true;
							
			}
		}
		if(count==Integer.MAX_VALUE)
		count=0; //Reintializig count so that to avoid negative values
		job.jobId=count++;

		if(objImported instanceof RemoteJobInterface)
		objectsImported.put(" "+job.jobId,objImported);

		if (!supported )
		{
			serviceNotSupported.add(" "+job.jobId);
			
			try
			{
				((RemoteJobInterface)objectsImported.get(" "+job.jobId)).setStatus(NOTSUPPORTED,job);
				 objectsImported.remove(" "+job.jobId);
			}
			catch(RemoteException e)
			{
			
				System.out.println("Source of the job is not ready");
			}
			return job.jobId;
		}
		
		readyQueue.addFirst(job);
		jobsReady.add(" "+job.jobId);
		
		if(!isJobThreadStarted)
		{
			jobThread = new SubmitThread(this);
			jobThread.setDaemon(true);
			isJobThreadStarted=true;
			submit=true;
			waiting=true;
			jobThread.start();
		}
		
		JobInformation info = new JobInformation(job.jobId,job.source,new Date(),"Ready");
		jobInformation.put(" "+job.jobId,info);
		UI.addInfo(info);
		
		try
		{
			if(objectsImported.containsKey(" "+job.jobId))
			((RemoteJobInterface)objectsImported.get(" "+job.jobId)).setStatus(READY);
		}
		catch(RemoteException e)
		{
			
			System.out.println("Source of the job is not ready");
		}
		return job.jobId;
		
	}
	public void addComputationUser(String user) throws RemoteException
	{
		if(computationUsers!=null)
		{
			computationUsers.add(user);
		}
	}
	public void addStorageUser(String user) throws RemoteException
	{
		if(storageUsers!=null)
		{
			storageUsers.add(user);
		}
	}
	public void removeUser(String user) throws RemoteException
	{
		if(computationUsers!=null)
		{
			computationUsers.remove(user);
			storageUsers.remove(user);
		}
	}
	public void removeMachine(String machine) throws RemoteException
	{
		if(computationMachines!=null)
		{
			computationMachines.remove(machine);
			storageMachines.remove(machine);
		}
	}
	
	public GridJob getReadyJob()
	{
		if ( readyQueue.size() > 0)
		{
			
			GridJob job=(GridJob)readyQueue.removeLast();	
			jobsReady.remove(" "+job.jobId);
			return job;
		}
		return null;
	}
	
	public GridJob getWaitingJobs()
	{
		if ( waitingQueue.size() > 0)
		{
			
			GridJob job=(GridJob)waitingQueue.removeLast();	
			return job;
		}
		return null;
	}
	//Scheduler Interface Implementation
	public GridJob getResult(int id) throws RemoteException
	{
		
		
		if( results.containsKey(" "+id))
		{
		
			GridJob job=(GridJob)results.get(" "+id);
			if( job instanceof GridJob)
			{
						
				JobInformation info = getJobInformation(id);
				info.deliveryDate = new Date();
				updateJobInformation(info);
				jobInformation.remove(" "+info.jobId);
                                
				if(objectsImported.containsKey(" "+id))
				objectsImported.remove(" "+id);
				
				results.remove(" "+id);
				return job;
			}
				
		}
		return null;
	}
	
	//Scheduler Interface Implementation
	public int getJobStatus(int id) throws RemoteException
	{
		GridJob temp;
		String tempStr="";
		
		if (id < 0 || id > count)	
		return UNKNOWNSTATE; //Error Invalid Job Id
		
		if(jobsReady.contains(" "+id))
			return READY; //Job is in the ready queue		
		
		if(jobsProcessed.containsKey(" "+id))
                    return PROCESSING; //Job is being processed		
				
		if(waitingQueue.contains(" "+id))
			return WAITING; //Job is in the waiting queue		
		
       	if (serviceNotSupported.contains(" "+id))
			return NOTSUPPORTED; //Service Not Suppported
		
		if(waitingTimeElapsed.contains(" "+id))
			return ELAPSED; //Job's waiting time has elapsed 		
		
				
		if(results.containsKey(" "+id))
		{
			temp = (GridJob)results.get(" "+id);
			if(temp instanceof GridJob)
			return 4; //Job is done			
		}
		

		return -1;		

	}
	//SchedulerInterface Implementaiton
	public boolean registerService(String handle) throws RemoteException
	{
		registeredServices.add(handle);
		badTargets.remove(handle);
		return true;
	}
	//SchedulerInterface Implementaiton
	public void unRegisterService(String handle) throws RemoteException
	{
		registeredServices.remove(handle);
	}
	//SchedulerInterface Implementaiton
	synchronized public void setResult(GridJob job) throws RemoteException
	{
		
		JobInformation info = getJobInformation(job.jobId);
		/*Remove the job from the processing jobs table because the job is done*/
		jobsProcessed.remove(" "+job.jobId);
		jobsInProcess.remove(" "+job.jobId);
		/*Update the jos status, now its done*/
		info.finishDate = new Date();
		info.status="Done"; 
		updateJobInformation(info);
		/*Decrementing the target load*/
		decrementTargetLoad(info.destination);
		/*Add the result to queue or return the result automatically through this function*/
		addResult(job);
		
		
	}
	//SchedulerInterface Implementaiton
	synchronized public void reportError(int id) throws RemoteException
	{
		if(jobsProcessed.containsKey(" "+id))
		{
			GridJob job = (GridJob)jobsProcessed.get(" "+id);
			jobsProcessed.remove(" "+id);
			jobsInProcess.remove(" "+id);
			JobInformation info = getJobInformation(job.jobId);
			addJobToWaitingQueue(job);
			//addJobWaiting(job.jobId);
			if(!isWaitingThreadStarted)
			{
				startWaitingThread();
			}
			info.startDate = new Date();
			info.status="Waiting";
			updateJobInformation(info);
			addBadTarget(info.handle);
		}
			
	}
	
	public void addWaitingTimeElapsed(int id,GridJob job)
	{
		
		try
		{
			if (objectsImported.containsKey(" "+id))
			((RemoteJobInterface)objectsImported.get(" "+id)).setStatus(ELAPSED,job);
			
			else
			{
				results.put(" "+id,job);	
				waitingTimeElapsed.add(" "+id);
			}
		}
		catch(RemoteException e)
		{
			
			System.out.println("Source of the job is not ready");
		}
	}
	public void clearWaitingTimeElapsed()
	{
		waitingTimeElapsed.clear();
		
	}
	public void removeJobReady(int id)
	{
		jobsReady.remove(" "+id);
	}
	
	public void addJobToWaitingQueue(GridJob job)
	{
		waitingQueue.addFirst(job);
		try
		{
			if (objectsImported.containsKey(" "+job.jobId))
			((RemoteJobInterface)objectsImported.get(" "+job.jobId)).setStatus(WAITING);
			
		}
		catch(RemoteException e)
		{
			
			System.out.println("Source of the job is not ready");
		}
	}
	synchronized public void addResult(GridJob job)
	{
		try
		{		
			JobInformation info = getJobInformation(job.jobId);
			info.deliveryDate = new Date();
			updateJobInformation(info);
			if(objectsImported.containsKey(" "+job.jobId))
			{
				
				((RemoteJobInterface)objectsImported.get(" "+job.jobId)).setResult(job);
				objectsImported.remove(" "+job.jobId);
			}
			else
			results.put(" "+job.jobId,job);
			
			jobInformation.remove(" "+info.jobId);
		}
		catch(RemoteException e)
		{
			results.put(" "+job.jobId,job);
			System.out.println("Source of the job is not ready...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public int waitingQueueSize()
	{
		return waitingQueue.size();
	}
	public int readyQueueSize()
	{
		return readyQueue.size();
	}
	
	public void updateSystemInfo(Hashtable ht)
	{
		
		sysInfo=ht;
	}
	public TargetSystem getTarget(String service,int val)
	{
		/*Target System to be returned*/
		TargetSystem target = new TargetSystem();
		
		/*If a system is found*/
		boolean found=false;
		
		/*Service configuratoin*/
		SchedularConfig c; 
		
		/*Intializing variables for minimum valus*/
		int minCompute=mem+cpu; 
		int minDisk=disk;
		int minDiskCompute=mem+disk+cpu;
		
		/*Total services available*/
		int total = config.size();
		
		/* Loop through all the services the schedular is set to 
		handle
		*/
		for(int i=0;i<total;i++)
		{
			/* Getting the computer and services information from the list*/
			c = (SchedularConfig)config.get(i);
			/*Checking whether the service running on the computer 
			has load not greater than four jobs.If the load is more than
			four jobs then the cpomputer will be excluded for this job*/
			System.out.println(c.address + " = "+ getTargetLoad(c.address));
			if(getTargetLoad(c.address)>2)
			continue;
			/*Checking whether the service is available in the list*/ 
			if(c.name.equals(service))
			{
				/* If the service is available then checking whether that
				the computer containing the service is available and if not then
				exclude the computer for this service and job.
				*/
				if(badTargets.contains(c.handle))
                continue;
                /* Checking whether the computer's information is available for
                submission
                */
				if(sysInfo.containsKey(c.address))
				{
					/*Getting the System information for the computer*/
					SystemInfo info = (SystemInfo)sysInfo.get(c.address);
					
					if (val==1) //Computation Intensive Job
					{
						/* Checking whether the target system meets the
						Settings set for the computation intensive jobs.
						The criteria suggests that the memory and cpu
						both must be less than the set memory and cpu levels
						If the values would individually less than the 
						cpu and memory settings then both memory and cpu 
						will be added to check which computer has less usage
						of these resources. Otherwise the computer 
						will be rejected for job submission
						*/
						if(info.cpu>cpu||info.memory>mem)
						continue; 
						if ((info.cpu+info.memory)<minCompute)
						{
							target.address=c.address;
							target.handle=c.handle;
							target.type=c.type;
							minCompute=info.cpu+info.memory;
						}
					}
					/* Checking whether the target system meets the
					Settings set for the data intensive jobs.
					The criteria suggests that the disk usage
					must be less than the set disk usage value. If the
					value will be less than disk settings then the disk usage 
					of the computer will be checked	for minimum utlization of disk.
					Otherwise the computer will be rejected for job submission
					*/
					else if (val==2) //Data Intensive Job
					{
						if(info.disk > disk)
						continue;
						
						if (info.disk<minDisk)
						{
							target.address=c.address;
							target.handle=c.handle;
							target.type=c.type;
							minDisk=info.disk;
						}
					}
					/* Checking whether the target system meets the
					Settings set for the data and computation intensive jobs.
					The criteria suggests that the disk usage and compuational resources
					must be less than the set disk usage and computation resources(memeory,cpu) values. 
					If the value will be less the settings then both the resources  
					of the computer will be checked	for minimum utlization of these resources.
					Otherwise the computer will be rejected for job submission
					*/
					else //Computation and Data Intensive
					{
						if(info.memory>mem||info.cpu>cpu||info.disk>disk)
						continue;
						if ((info.disk+info.cpu+info.memory)<minDiskCompute)
						{
							target.address=c.address;
							target.handle=c.handle;
							target.type=c.type;
							minDiskCompute=info.disk+info.cpu+info.memory;
						}
					}
					found=true;	
				}
					
			}
					
		}
		if (found)
		{
			return target;	
		}
		else
		{
		 	 return null;
		}
	
		
	} 
	
	public void updateJobInformation(JobInformation jobInfo)
	{
		jobInformation.put(" "+jobInfo.jobId,jobInfo);
		UI.updateInfo(jobInfo);	
	}
	public JobInformation getJobInformation(int id)
	{
		return (JobInformation)jobInformation.get(" "+id);
	}
	
    public void addJobNotSupported(int id)
    {
		serviceNotSupported.add(" "+id);
    }
	public int getConfigSize()
	{
		return config.size();
	}
	public LinkedList getConfigList()
	{
		return config;
	}
	
	public void incrementTargetLoad(String address)
	{
		/*Incrment the target load if the target address is available*/
		if (targetLoad.containsKey(address))
		{
			int count = Integer.parseInt(((String)targetLoad.get(address)).trim());
			System.out.println("Setting Target Load of "+ address +" = "+(count+1));
			targetLoad.put(address," "+(count+1));
			
			
		}
		/*Else Add new load for the target specified by it address*/
		else
		{
			targetLoad.put(address," "+1);
			System.out.println("Setting Target Load of "+ address +" = 1");
		}
		
		
	}
	public void decrementTargetLoad(String address)
	{
		if (targetLoad.containsKey(address))
		{
			int count = Integer.parseInt(((String)targetLoad.get(address)).trim());
			if (count > 0)
			targetLoad.put(address," "+(count-1));
					
		}
	}
	public int getTargetLoad(String address)
	{
		if (targetLoad.containsKey(address))
		{
			return Integer.parseInt(((String)targetLoad.get(address)).trim());
			
		}
		return 0;
	}
	public void addJobReadyQueue(GridJob job)
	{
		readyQueue.addFirst(job);
	}
    public void startJobThread()
	{
		jobThread = new SubmitThread(this);
		jobThread.setDaemon(true);
		isJobThreadStarted=true;
		submit=true;
		jobThread.start();
	}
	public void startWaitingThread()
	{
		waitingThread = new WaitingJobsThread(this);
		waitingThread.setDaemon(true);
		isWaitingThreadStarted=true;
		waiting=true;
		waitingThread.start();
	}
	public void startCheckDestinationThread()
	{
		CheckDestinationThread = new CheckDestinationThread(this);
		CheckDestinationThread.setDaemon(true);
		isCheckDestinationThreadStarted=true;
		CheckDestinationThread.start();
	}
	public boolean isWaitingThread()
	{
		return waitingThread.isAlive();
	}
	public boolean isJobThread()
	{
		return jobThread.isAlive();
	}
	public boolean isCheckDestinationThread()
	{
		return CheckDestinationThread.isAlive();
	}
    public void addBadTarget(String handle)
    {
    
        badTargets.add(handle);
    }
    public void removeBadTarget()
    {
        badTargets.clear();
           
    }
	public void setConfig(LinkedList list)
	{
		config=list;
	}
	public void setSettings(String saddress,String sec,int imem,int icpu,int idisk,int iwait,int ielp,int iman)
	{
		broker=saddress;
		securityAddress=sec;
		cpu=icpu;
		mem=imem;
		disk=idisk;
		wait=iwait;
		elapsed=ielp;
		management=iman;
		try
		{
			security = (SecurityManagerInterface)Naming.lookup(sec);
			if(security != null)
			{
				computationMachines=security.getNodesForComputation();
				storageMachines=security.getNodesForStorage();
				computationUsers=security.getUsersForComputation();
				storageUsers=security.getUsersForStorage();
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
				
	}
	public void setSettings(Settings settings)
	{
		broker=settings.broker;
		cpu=settings.mem;
		mem=settings.cpu;
		disk=settings.disk;
		wait=settings.wait;
		elapsed=settings.elapsed;
		management=settings.man;
		securityAddress=settings.security;
		try
		{
			security = (SecurityManagerInterface)Naming.lookup(securityAddress);
			if(security != null)
			{
				computationMachines=security.getNodesForComputation();
				storageMachines=security.getNodesForStorage();
				computationUsers=security.getUsersForComputation();
				storageUsers=security.getUsersForStorage();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	public Settings getSettings()
	{
		return new Settings(broker,securityAddress,mem,cpu,disk,wait,elapsed,management);
	}
	
	
}
