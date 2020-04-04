import java.io.Serializable;
public class FactorialJobEx extends GridJob implements Serializable
{
	public LargeNumber number;
	public LargeNumber args1;
	public LargeNumber args2;
	public FactorialJobEx(LargeNumber a1,LargeNumber a2,LargeNumber num)
	{
		args1=a1;
		args2=a2;
		number = num;
		
	}
	
}