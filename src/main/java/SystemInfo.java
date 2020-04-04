import java.io.Serializable;
class SystemInfo implements Serializable
{
	public String address;
	public int    cpu;
	public int    memory;
	public int    vMemory;
	public int    disk;
	public String folderName; 
	public SystemInfo(){}
	public SystemInfo(String ad,int cp, int mem,int vMem,int di,String folder)
	{
		address=ad;
		cpu=cp;
		memory=mem;
		vMemory=vMem;
		disk=di;
		folderName=folder;
	}
}