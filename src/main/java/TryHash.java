import java.util.*;

class TryHash
{	
	static GridJob a = new MathJob(10,10,1,0);
	static GridJob c = new MathJob(10,10,1,0);
	static GridJob b = new MathJob(10,20,1,0);
	public static void main(String args[])
	{
		Hashtable st = new Hashtable();
		LinkedList list = new LinkedList();
		list.add(" "+1);
		list.add(" "+2);
		list.add(" "+3);
		System.out.println((String)list.remove());
		//System.out.println(st.contains(c));
		st.remove(" "+1);
		String str = null;
		/*if(st.containsKey(str))
		{
			System.out.println("Helllo");
		}
		*/
		
		
	}
}