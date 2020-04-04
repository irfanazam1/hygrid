import java.io.Serializable;
class MathJob extends GridJob implements Serializable
{
	public int a,b,op,result;
	
	public MathJob(int A, int B, int C, int res)
	{
		a=A;
		b=B;
		op=C;
		res=result;
	}
	public MathJob()
	{

	}
}