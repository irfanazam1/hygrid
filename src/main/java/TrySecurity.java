import java.rmi.*;
import java.util.HashSet;
public class TrySecurity
{
	private static SecurityManagerInterface security=null;
	public TrySecurity()
	{
		
	}
	public static void main(String args[])
	{
		
		try
		{
			String address= "rmi://192.168.0.1/SecurityManager";
			security = (SecurityManagerInterface)Naming.lookup(address);
			if(security!=null)
			{
				HashSet set = security.getNodesForStorage();
				if(set != null)
				{
					System.out.println(set);
				}
				else
				System.out.println("No Registered Machines");
			}
		}
		catch(Exception e)
		{
			
		}
	}
}