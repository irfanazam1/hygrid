import java.rmi.*;
/**
*JobInterface is implemented by the services 
*which want to process jobs coming from the schedular
*/
public interface JobInterface extends Remote
{
	/**
	*Method used by the schedular to submit the jobs to this service
	*/
	void submitJob(GridJob job) throws RemoteException;
	/**
	*Method used by the schedular to get the jobs(results) from this service
	*/
	GridJob getResult(int id) throws RemoteException;
	/**
	*Method used by the schedular to get the job status
	*/
	int getStatus(int id) throws RemoteException;	
        	
}