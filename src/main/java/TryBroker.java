import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class TryBroker
{
	
	public static void main (String args[])
	{
		final String URL ="rmi://localhost/BrokerServer";
		
		try
		{	
			//while (true)
			{
				BrokerServerInterface in = (BrokerServerInterface) Naming.lookup(URL);
				ArrayList s= in.getDetailInfo("localhost");
				//System.out.println(s);
				for( Iterator i = s.iterator();i.hasNext();)
				{
					String ss = (String)i.next();
					System.out.println(ss);
				}
				//System.out.println(info.cpu);
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}