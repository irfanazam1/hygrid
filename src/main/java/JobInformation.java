import java.io.Serializable;
import java.util.Date;
class JobInformation implements Serializable
{
	public int jobId;		
	public String source;
	public String destination;
	public Date submitDate;
	public Date startDate;
	public Date finishDate;
	public String status;
	public Date deliveryDate;
	public String type;
	public String handle;
	public JobInformation(int id, String fr, Date sub,String stat)
	{
		jobId=id;
		source=fr;
		destination=null;
		submitDate=sub;
		status=stat;
		startDate=null;
		finishDate=null;
		deliveryDate=null;
		type=null;
		handle=null;
		
		
	}
	public JobInformation(int id, String fr,String to,Date submit,Date start,Date finish,String stat,Date deliver)
	{
		jobId=id;
		source=fr;
		destination = to;
		submitDate=submit;
		status=stat;
		startDate=start;
		finishDate=finish;
		deliveryDate=deliver;
		type=null;
		handle=null;
		
	}
	
} 
