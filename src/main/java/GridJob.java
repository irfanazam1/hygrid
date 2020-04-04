import java.io.Serializable;

/**
*GridJob is the abstract base class for all the classes which are to be scheduled by the 
*schedular and sent over the network. The classes extending this class can be transferred 
*over the netwrok becuase this class is implementing Serializable interface.
*/

/**
<pre>
Legned.
+  Required, provided by the source
-  Optional
+- Required when forcing the job
++ Provided by Schedular and read

*	Class Description:
*	++ JobId : An ID which uniquely identifies the job. This will be provided by the schdular.
*	+ Source: Depicts the source of the job, provided by the source of the job. It will be the IP address of the computer.
*	+ ServiceName: Name of the service to call. Example: Math,DataStorage
*	+- Handle: Describes the handle which actually call the service. Example rmi://192.168.0.1/Math 
*	+- Type: Describes the type of the service. Example: RMI
*	+ Force: True or False value provided by the source. True value for source makes the schedular to send
*	 the job to the specific computer.
*	+ Intensity: Describes the type of the job. Possible values are
*	Data,Computation,Data & Computation
*	+- Destination: IP address of the computer hosting the service
*</pre>
*/
public abstract class GridJob implements Serializable
{
	
	/**
	*Value to be set for the intensity field if the job is computation
	*intensive. If this value is given shedular will check only the 
	*CPU and Memory resources while submitting the job to the machines
	*participating on GRID.
	*/
	public static final int COMPUTATION_INTENSIVE=1;
	/**
	*Value to be set for the intensity field if the job is Data
	*intensive. If this value is given shedular will check only the 
	*Disk resources while submitting the job to the machines
	*participating on GRID.
	*/
	public static final int DATA_INTENSIVE=2;
	/**
	*Value to be set for the intensity field if the job is cmputation and Data
	*intensive. If this value is given shedular will check only the 
	*CPU,Memory and Data resources while submitting the job to the machines
	*participating to GRID.
	*/
	public static final int DATA_AND_COMPUTATION_IENTENSIVE=3;
	/**
	*Integer value provided by the schedular
	*/
	public int jobId;
	/** 
	*IP Address of the source machine
	*/
	public String source;
	/**
	*String value provided by the source
	*/
	public String serviceName;
	/** 
	*Actual Service string
	*Example: rmi://192.168.0.1/Math
	*/
	
	public String handle;
	/**
	*Type of the service.
	*Example: RMI
	*/
	public String type;
	/**
	*True or False
	*/
	public boolean force;
	/**
	*Constants values defined in the class
	*/
	public int intensity;
	/**
	*IP Address of the Machine
	*/
	public String destination;
	
	/**
	*UserName of the user submitting the jobs
	*/
	public String userName;
	
}