import java.io.Serializable;
public class FactorialJob extends GridJob implements Serializable
{
	public int sequence;
	public int from;
	public int to;
	public LargeNumber number;
	public FactorialJob(LargeNumber num,int seq,int f,int t)
	{
		sequence=seq;
		number = num;
		from=f;
		to=t;
	}
}