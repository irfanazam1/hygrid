import java.rmi.*;
/**
*Interface Implemented by the schedular clients which want to 
*enable the callback mechanism. If this interface will be impleneted
*by the client then the schedular will be able to send the results 
*and job status automatically back to the client.
*/
public interface RemoteJobInterface extends Remote
{
	/**
	*Used by the schedular to send the status and return the job back
	*to the client.
	*/
	void setStatus(int status,GridJob job) throws RemoteException;
	/**
	*Used by the schedular to send the status of the job back to the client.
	*/
	void setStatus(int status) throws RemoteException;
	/**
	*Used by the schedular to send the result back to the client.
	*/
	void setResult(GridJob result) throws RemoteException;
}
