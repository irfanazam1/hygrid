import java.util.Date;
public class TestSeq 
{
	public static void main(String args[])
	{	
		
		LargeNumber number=null;
		System.out.println("Start: "+new Date());
		//for(int l=0;l<20;l++)
		long ll=0;
		System.out.println(Short.MAX_VALUE);
		for(int i=0;i<20;i++)
		{
			for(int j=0;j<4;j++)
			{
				//System.out.println(ll++);
				StringBuffer num = new StringBuffer(""+1);
				number = new LargeNumber(num);
				for(int k = 1;k<=1000;k++)
				{
					num = new StringBuffer(""+k);
					LargeNumber temp = new LargeNumber(num);
					number = number.mul(temp);		
				}
				
			}
		}
		System.out.println(number.getNumber());	
		System.out.println("End: "+new Date());
	}
	
}