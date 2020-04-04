import java.io.Serializable;
import java.util.HashSet;
import java.util.ArrayList;
class QueryJob extends GridJob implements Serializable
{
	public String handle;
	public String user;
	public String pass;
	public ArrayList columns;
	public HashSet rows;
	public String query;
	public QueryJob(String qu,String han,String us,String ps)
	{
		query=qu;
		handle=han;
		pass=ps;
		user=us;
		columns=new ArrayList();
		rows = new HashSet();
	}		
}