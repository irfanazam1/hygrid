import java.io.Serializable;

class SchedularConfig implements Serializable
{
	public String address;
	public String name;
	public String handle;
	public String type;
	public SchedularConfig(){}
	public SchedularConfig(String sAddress,String sName,String sHandle,String sType)
	{
		address  =  sAddress;
		name	 =  sName;
		handle	 =  sHandle;
		type	 =  sType;
	}
} 
