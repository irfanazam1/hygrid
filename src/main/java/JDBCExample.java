import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.*;
import java.sql.*;
import java.util.Date;
public class JDBCExample
{
    public static void main(String args[])
	{	
		Connection con = null;
    	Statement stmt=null;
    	Random r = new Random();
    	try
    	{
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
      		con = DriverManager.getConnection("jdbc:mysql://192.168.0.1/grid",
        	"grid", "grid");
        	/*Driver Setup completed*/
        	System.out.println("Job Started At: "+new Date());
        	String str = "Select count(p.id) total,c.cenname cen"
        	+" from person p , centre c "
        	+" where p.cencode = c.cencode"
        	+" group by c.cenname";
        	stmt = con.createStatement();
        	ResultSet rs = stmt.executeQuery(str);
        	while(rs.next())
        	{
        		System.out.print(rs.getString("total")+" : ");
        		System.out.println(rs.getString("cen"));
        	}
        	System.out.println("Job Finished At: "+new Date());
        	/*
        	for(long l=1000001;l<100000000;l++)
        	{
				int cencode = Math.abs(r.nextInt()%8)+1; 
				int councode = Math.abs(r.nextInt()%6)+1;
				int statcode = Math.abs(r.nextInt()%4)+1;
				String insert = "insert into person values( "+l+", 'Irfan', "+councode+","+statcode+","+cencode+")";
				stmt.executeUpdate(insert);
			}
			*/
			stmt.close();
		}
		catch(ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			System.exit(0);
			
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		finally
		{
			if(con!=null)
			{
				try
				{
					con.close();
					
				}	
				catch(SQLException e)
				{
				}
			}
			
		}
		

	}
	public JDBCExample()
	{
	}
	
}