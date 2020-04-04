import java.io.*;

class DatabaseConfiguration implements Serializable
{
	private String strUserName;
	private String strPassword;
	private String strDatabaseServer;
	
	public DatabaseConfiguration()
	{
		strUserName = "";
		strPassword = "";
		strDatabaseServer = "";
	}
	public DatabaseConfiguration(String username, String password, String address)
	{
		strUserName = username;
		strPassword = password;
		strDatabaseServer = address;
	}
	public String getUserName()
	{
		return strUserName;
	}
	public boolean setUserName(String username)
	{
		if(username != null && username != "")
		{
			strUserName = username;
			return true;
		}
		else
		{
			return false;
		}
	}
	public String getPassword()
	{
		return strPassword;
	}
	public boolean setPassword(String password)
	{
		if(password != null && password != "")
		{
			strPassword = password;
			return true;
		}
		else
		{
			return false;
		}
	}
	public String getDatabaseServer()
	{
		return strDatabaseServer;
	}
	public boolean setDatabaseServer(String address)
	{
		if(address != null &&  address != "")
		{
			strDatabaseServer = address;
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean saveConfiguration(String file) throws IOException
	{
		
		if(strUserName != "" && strPassword != "" && strDatabaseServer != "")
		{
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.flush();
			out.close();
			return true;
		}
		return false;
	}
	public DatabaseConfiguration getConfiguration(String file) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fis);
		DatabaseConfiguration conf = (DatabaseConfiguration)in.readObject();
		in.close();
		return conf;
	}
}