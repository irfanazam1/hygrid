import java.io.Serializable;

public class PopcornJob extends GridJob implements Serializable
{
	public int sequence;
	public int from;
	public int to;
	public int width;
	public int height;
	public MyPoint[] points;
	public PopcornJob(int seq,int f,int t,int w,int h)
	{
		sequence=seq;
		from=f;
		to=t;
		width=w;
		height=h;
	}
}