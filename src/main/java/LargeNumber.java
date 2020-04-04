import java.util.Date;
import java.io.Serializable;
public class LargeNumber implements Serializable
{
	private StringBuffer lNum;
	//private static final int size=50000;
	private int len;
	public LargeNumber(){}
	public LargeNumber(StringBuffer num)
	{
		lNum = num;
		len = lNum.length();
		lNum=lNum.reverse();
		
	}
	public LargeNumber(StringBuffer num,int i)
	{
		lNum = num;
		len=lNum.length();
		
	}
	public LargeNumber(int si)
	{
		len=si;
		lNum = new StringBuffer(si);
	
	}
	public StringBuffer getNumber()
	{
		StringBuffer buf = new StringBuffer(lNum.toString());
		return buf.reverse();
	}
	public void setNumber(StringBuffer str)
	{
		lNum = str;
		len = str.length();
		lNum = lNum.reverse();
	}
	public LargeNumber mul(final LargeNumber number)
	{
		LargeNumber prod = new LargeNumber(0);
		LargeNumber tempsum = new LargeNumber(0);
		for(int i=0;i<number.len;i++)
		{
			int dig = number.lNum.charAt(i)-'0';
			prod = muldigit(dig);
			for(int j=0;j<i;j++)
			{
				prod = mul10(prod);
			}
			tempsum = tempsum.plus(prod);
		}
		return tempsum;
	}
	private LargeNumber muldigit(final int digit)
	{
		StringBuffer temp = new StringBuffer(len+1);
		int j,carry=0;
		for(j=0;j<len;j++)
		{
			int d1 = lNum.charAt(j)-'0';
			int dig = d1*digit;
			dig+=carry;
			if(dig >=10)
			{
				carry=dig/10;
				dig-=carry*10;
			}
			else
			carry=0;
			try
			{
				temp=temp.append((char)(dig+'0'));
			}
			catch(Exception e)
			{
				System.out.println("Exception at index= "+j);
			}
		}
		if(carry!=0)
		{
			temp=temp.append((char)(carry+'0'));
			j++;
		}
		temp.setLength(j);
		return new LargeNumber(temp,0);
	}
	private LargeNumber mul10(final LargeNumber number)
	{
	   char[] arr =new char[number.len+1];
	   for(int i=number.len-1;i>=0;i--)
	   {
	   		arr[i+1]=number.lNum.charAt(i);	   		
	   }
	   arr[0]='0';
	   StringBuffer temp = new StringBuffer(number.len+1);
	   temp=temp.append(arr);
	   //temp.setLength(number.len+1);
	   return new LargeNumber(temp,0);	
	}
	
	public LargeNumber plus(final LargeNumber number)
	{
		StringBuffer temp = new StringBuffer(len+1);
		int j=0;
		int maxLen = (number.len>len)?number.len:len;
		int carry=0;
		for(j=0;j<maxLen;j++)
		{
			int d1 = (j>len-1)?0:lNum.charAt(j)-'0';
			int d2 = (j>number.len-1)?0:number.lNum.charAt(j)-'0';
			int dig =  d1+d2+carry;
			if(dig>=10)
			{
				dig-=10;
				carry=1;
			}
			else
			carry=0;
			temp=temp.append((char)(dig+'0'));
		}
		if(carry==1)
		{
			temp=temp.append('1');
			j++;
		}
		temp.setLength(j);
		return new LargeNumber(temp,0);			
	}
	public static void main(String args[])
	{
		
	}
}