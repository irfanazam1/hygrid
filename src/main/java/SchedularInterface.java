import java.rmi.Remote;
import java.rmi.RemoteException;

/**
*Interface implemented by the Schedular and will be used to communicate
*with the schedular by its clients
*/

public interface SchedularInterface extends Remote
{
	
	/**
	*Constants describing the jobs status
	*/
	int READY=-1;
   	int PROCESSING=-2;
   	int WAITING=-3;
   	int FINISHED=-4;
   	int ELAPSED=-5;
   	int NOTSUPPORTED=-6;
   	int MACHINE_NOT_AUTHORIZED=-7;
   	int USER_NOT_AUTHORIZED=-8;
   	int UNKNOWNSTATE= -9;
	/**
	*Used to submit the job to the schedular.The must inherit the GridJob to be scheduled
	Parameters are GridJob and RemoteJobInterface, the interface implemented by the
	clients so that the schedular can send the results back to the clients
	automatically
	*/
	int submitJob(GridJob job,RemoteJobInterface callback) throws RemoteException;
	/**
	*This method can be used by the jobs to get the result from the schedular on demand at any time.
	*The result will be returned to the client if the job has done.
	*Job id will be required to get the job.
	*To use this facility client must not implement RemoteJobInterface, otherwise the jobs will be returned
	automatically.
	*/
	GridJob getResult(int id) throws RemoteException;
	/**
	*Method to get the status of the job,providing the job id
	*/
	int getJobStatus(int id) throws RemoteException;	
	/**
	*Method to be used to register the service with the schedular.
	*/
	boolean registerService(String handle) throws RemoteException;
	/**
	*Unregister the service from the schedular
	*/
	void  unRegisterService(String handle) throws RemoteException;
	/**
	*Schedular get the results from the servers processing the job. The servers
	*registered with the schedular will return theri jobs through this method
	*/ 
	void  setResult(GridJob job) throws RemoteException;
	/*Server processing the job will return errors to the schedular
	*incase of errors, so that schedular can mark them as bad targets and
	*can reschedule the jobs
	*/
	void  reportError(int id) throws RemoteException;
	/**
	*Methods to be used by the security manager to add and remove nodes and users
	*/
	void addComputationUser(String user) throws RemoteException;
	void addStorageUser(String user) throws RemoteException;
	
	void removeUser(String user) throws RemoteException;
	void removeMachine(String machine) throws RemoteException;
	
}
