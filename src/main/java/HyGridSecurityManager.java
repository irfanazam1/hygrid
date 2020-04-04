import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.util.*;
import java.sql.*;

public class HyGridSecurityManager extends UnicastRemoteObject implements SecurityManagerInterface
{
    private static String SERVER=" ";
	private static String myAddress=null;
	private static String brokerAddress=null;
	private static String schedularAddress=null;
	private static BrokerServerInterface broker;
	private static SchedularInterface schedular;
	public static void main(String args[])
	{	
		try
		{
			if(args.length!=2)
			{
				System.out.println("Proper Usage: HyGridSecurityManager [Scheduler Address] [Broker Address] \nExample: HyGridSecurityManager 192.168.0.1 192.168.0.1.192");
				System.exit(0);
			}
			else if((!hostLookup(args[0]))||(!hostLookup(args[1])))
			{
				System.out.println("Please provide valid Addresses");
				System.exit(0);
			}
			InetAddress local = InetAddress.getLocalHost();
			myAddress=local.getHostAddress();
			SERVER="//";
			SERVER+=myAddress;
			SERVER+="/SecurityManager";
			schedularAddress = "rmi://"+args[0]+"/schedular";
			brokerAddress = "rmi://"+args[1]+"/brokerserver";
			SecurityManagerInterface server = new HyGridSecurityManager();
			Naming.rebind(SERVER,server);
			
			System.out.println("Security Manager started sucessfully");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	public HyGridSecurityManager() throws RemoteException
	{
		Connection con = null;
    	Statement stmt=null;
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String update = "update user set signin = 'N' where signin = 'Y'";
			stmt = con.createStatement();
			stmt.executeUpdate(update);
			stmt.close();
						
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
	public boolean signOn(String user,String pass) throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select signin,registered from user where username= '"+user.toLowerCase()+"'"+" and password= '"+pass+"'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			if(rs.next())
			{
				if(rs.getString("registered").equals("N"))
				return false;
				if(rs.getString("signin").equals("Y"))
				return false;
				rs.close();
				String update = "update user set signin = 'Y' where username= '"+user.toLowerCase()+"'"+" and password = '"+pass+"'";
				stmt.executeUpdate(update);
				stmt.close();
				
				select = "select username from user where registered='Y' and computation='Y' and signin='Y'";
				stmt = con.createStatement();
				rs = stmt.executeQuery(select);
				if(rs.next())
				{
					try
					{
						schedular = (SchedularInterface)Naming.lookup(schedularAddress);
						schedular.addComputationUser(user.toLowerCase());
					}
					catch(NotBoundException nbe)
					{
						return true;
					}				
			 	}
			 	stmt.close();
			 	rs.close();
			 	select = "select username from user where registered='Y' and storage='Y' and signin='Y'";
				stmt = con.createStatement();
				rs = stmt.executeQuery(select);
				if(rs.next())
				{
					try
					{
						schedular = (SchedularInterface)Naming.lookup(schedularAddress);
						schedular.addStorageUser(user.toLowerCase());
					}
					catch(NotBoundException nbe)
					{
						return true;
					}				
			 	}
			 	stmt.close();
			 	rs.close();
				return true;
			}
			else
			return false;
			
	
		}
		
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
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
		
		return false;
	}
	public boolean signOff(String user,String pass) throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select signin from user where username= '"+user.toLowerCase()+"'"+" and signin = 'Y' and password= '"+pass+"'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			if(rs.next())
			{
				String update = "update user set signin = 'N' where username= '"+user.toLowerCase()+"'"+" and password = '"+pass+"'";
				stmt.executeUpdate(update);
				try
				{
					schedular = (SchedularInterface)Naming.lookup(schedularAddress);
					schedular.removeUser(user.toLowerCase());
				}
				catch(NotBoundException nbe)
				{
						return true;
				}				
				return true;
			}
			else
			return false;
			
	
		}
		
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
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
		
		
		return false;
	}
	public boolean registerUser(String user,String pass,String org,boolean computation,boolean storage) throws RemoteException
	{
		Connection con = null;
    	Statement stmt=null;
    	char comp='N';
    	char stor='N';
    	try
    	{
    		
      		con = OpenConnection();
        	
        	/*Driver Setup completed*/
        	String select = "select username from user where username= '"+user.toLowerCase()+"'"+" and password= '"+pass+"'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				rs.close();
				stmt.close();
				return false;
			}
			stmt.close();
			comp = computation==true?'Y':'N';
			stor = storage==true?'Y':'N';
			String insert = "insert into user values( "
			+"'"+user.toLowerCase()+"' , "
			+"'"+pass+"' , "
			+"'"+org+"' , "
			+"'"+comp+"' , "
			+"'"+stor+"' , "
			+"'N' , "
			+"'N' )";
			stmt = con.createStatement();
			stmt.executeUpdate(insert);
			stmt.close();
			return true;
			
	
		}
		catch(Exception e)
		{
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
			
		return false;
	}
	public boolean unregisterUser(String user,String pass) throws RemoteException
	{
		Connection con = null;
    	Statement stmt=null;
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select username from user where username= '"+user.toLowerCase()+"'"+" and password= '"+pass+"'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			if(rs.next())
			{
				
				stmt.close();
				stmt = con.createStatement();
				String del = "delete from user where username= '"+user.toLowerCase()+"'";		
				stmt.executeUpdate(del);
				stmt.close();
				try
				{
					schedular = (SchedularInterface)Naming.lookup(schedularAddress);
					schedular.removeUser(user.toLowerCase());
				}
				catch(NotBoundException nbe)
				{
					return true;
				}
				return true;
			}
					
			return false;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
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
			
		return false;
	}
	public boolean registerMachine(String address,String org,boolean computation,boolean storage) throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	char comp='N';
    	char stor='N';
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select address from machine where address= '"+address+"'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				rs.close();
				stmt.close();
				return false;
			}
			stmt.close();
			comp = computation==true?'Y':'N';
			stor = storage==true?'Y':'N';
			String insert = "insert into machine values( "
			+"'"+address+"' , "
			+"'"+org+"' , "
			+"'N' , "
			+"'"+comp+"' , "
			+"'"+stor+"' )";
			
			stmt = con.createStatement();
			stmt.executeUpdate(insert);
			stmt.close();
			return true;
		
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
			return false;	
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
	public boolean unregisterMachine(String address) throws RemoteException
	{
		Connection con = null;
    	Statement stmt=null;
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select address from machine where address= '"+address+"'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			if(rs.next())
			{
				stmt.close();
				stmt = con.createStatement();
				String del = "delete from machine where address= '"+address+"'";		
				stmt.executeUpdate(del);
				stmt.close();
				
				try
				{
					broker = (BrokerServerInterface)Naming.lookup(brokerAddress);
					broker.removeNode(address);
				}
				catch(NotBoundException nbe)
				{
					
				}
				try
				{
					schedular = (SchedularInterface)Naming.lookup(schedularAddress);
					schedular.removeMachine(address);
				}
				catch(NotBoundException nbe)
				{
					
				}
				
				return true;
			}
					
			return false;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
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
			
		return false;
	}
	public HashSet getNodes() throws RemoteException
	{
		
		
		Connection con = null;
    	Statement stmt=null;
    	HashSet tempSet = new HashSet();
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select address from machine where registered='Y'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				tempSet.add(rs.getString("address"));			
			}
					
			if(tempSet.size()>0)		
			return tempSet;
			else
			return null;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
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
					return null;
				}
			}
			
		}
	
		
		return null;
	}
	public HashSet getNodesForComputation() throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	HashSet tempSet = new HashSet();
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select address from machine where registered='Y' and computation='Y'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				tempSet.add(rs.getString("address"));			
			}
					
			if(tempSet.size()>0)		
			return tempSet;
			else
			return null;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
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
					return null;
				}
			}
			
		}
	

		return null;
	}
	public HashSet getNodesForStorage() throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	HashSet tempSet = new HashSet();
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select address from machine where registered='Y' and storage='Y'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				tempSet.add(rs.getString("address"));			
			}
					
			if(tempSet.size()>0)		
			return tempSet;
			else
			return null;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
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
					return null;
				}
			}
			
		}
	
		return null;
	}
	public HashSet getUsersForComputation() throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	HashSet tempSet = new HashSet();
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select username from user where registered='Y' and computation='Y' and signin='Y'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				tempSet.add(rs.getString("username"));			
			}
					
			if(tempSet.size()>0)		
			return tempSet;
			else
			return null;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
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
					return null;
				}
			}
			
		}
	

		return null;
	}
	public HashSet getUsersForStorage() throws RemoteException
	{
		
		Connection con = null;
    	Statement stmt=null;
    	HashSet tempSet = new HashSet();
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select username from user where registered='Y' and storage='Y' and signin='Y'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			while(rs.next())
			{
				tempSet.add(rs.getString("username"));			
			}
			if(tempSet.size()>0)		
			return tempSet;
			else
			return null;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
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
					return null;
				}
			}
			
		}

		return null;
	}
	private static boolean hostLookup(String host)
	{
		InetAddress computer;
		try
		{
			computer = InetAddress.getByName(host);
			String address = computer.getHostName();
			return true;
		}
		catch(java.net.UnknownHostException uhe)
		{
			return false;
		}
	}
	public boolean authenticate(String userName,String password)
	{
		Connection con = null;
    	Statement stmt=null;
    	try
    	{
    		
      		con = OpenConnection();
        	/*Driver Setup completed*/
        	String select = "select password from user where username= '"+userName+"' and password= '"+password+"' and signin = 'Y'";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select);
			if(rs.next())
			{
				
				stmt.close();
				return true;
			}
					
			return false;
				
		}
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
			return false;
		}
		catch(Exception e)
		{
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
			
		return false;
	}
	private Connection OpenConnection()
	{
		try
		{
			String strUser="";
			String strPass="";
			String strAddress="";
			DatabaseConfiguration conf = new DatabaseConfiguration();
			conf = conf.getConfiguration("dataconf.conf");
			strUser = conf.getUserName();
			strPass = conf.getPassword();
			strAddress = conf.getDatabaseServer();
			Class.forName("com.mysql.jdbc.Driver").newInstance();
	      	Connection con = DriverManager.getConnection("jdbc:mysql://"+strAddress+"/grid",
	        strUser,strPass);
	        return con;
        }
        catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
