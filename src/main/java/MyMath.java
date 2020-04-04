import java.io.Serializable;
class MyMath extends GridJob implements Serializable
{
        String mathName;
       	public MyMath(String name)
   	{
		mathName=name;
   	}	
	public MyMath()
	{
		mathName="My Math Class";
	}
	public MyMath getMath(MyMath math)
	{
		return new MyMath (math.getName());
	}
        public String getName()
	{
		return mathName;
	}
	public int Add(int a,int b)
	{
		return a+b;
	}
	public int Subtract(int a, int b)
	{
		return a-b;
	}
	public int Multiply(int a,int b)
	{
	   	return a*b;	
	}
}