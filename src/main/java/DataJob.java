import java.rmi.*;
import java.io.Serializable;
/**
*DataJob class contains information for jobs which are to be sent to the
*GRID as data intensive jobs.
*<pre>
*Class Description:
*buf: Will contain the data,file or part of the file to be sent
*sequence:The sequence no of the file, if the file is divided into more than one parts.
*maxBytes: Maximum size of the buf.
*dataJobId: ID of the datajob. Selected from the database or file.
*fileSize: size of the buffer or file
*success: Boolean value showing whether the job is successful or not
*folderName:Folder where the file is to be stored
*fileName: Name of the file
*operation: Integer value,describing the type of job. Retrievel or Storage 
*/
public class DataJob extends GridJob implements Serializable
{
	/**
	*Job is a Storage Job
	*/
	public static final int STORAGE_JOB=1;
	/**
	*Job is a retrievel job
	*/
	public static final int RETRIEVEL_JOB=2;
	/**
	*Buffer to hold data
	*/
	public byte[] buf;
	/**
	*Sequence number of the file
	*/
	public int sequence;
	/**
	*Maximum bytes that can be sent over the network in this job
	*/
	public long maxBytes;
	/**
	*DataJob Id,populated from the database
	*/
	public int dataJobId;	
	/**
	*File size
	*/
	public int fileSize;
	/**
	*Tells the status of the jobs success or failure
	*/
	public boolean success;
	/**
	*Folder name from where the file is to be retrieved
	*/
	public String folderName;
	/**
	*Filename of the file to be retrieved
	*/
	public String fileName;
	/**
	*Type of the operation,storage or retrievel
	*/
	int operation;
	public DataJob(int id,int seq,long max,int dataSize,int opr,boolean su,String file)
	{
		buf = new byte[dataSize];
		sequence = seq;
		dataJobId=id;
		fileSize=dataSize;
		maxBytes= max;
		success=su;
		operation = opr;
		folderName=" ";		
		fileName=file;
		
	}
	public DataJob(int id,int seq,long max,int dataSize,int opr, boolean su,String folder,String file)
	{
		buf = new byte[dataSize];
		sequence = seq;
		dataJobId=id;
		fileSize=dataSize;
		maxBytes= max;
		success=su;
		operation = opr;
		folderName=folder;		
		fileName=file;
		
	}
	
}