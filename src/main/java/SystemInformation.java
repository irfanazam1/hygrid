import java.io.Serializable;
class SystemInformation implements Serializable
{
	public long physicalMem;
	public long freePhysicalMem;
	public long usedPhysicalMem;
	public long virtualMem;
	public long freeVirtualMem;
	public long usedVirtualMem;
	public int cpu;
	public String address;
	public String folderName;
	public int totalDisk;
	public int freeDisk;
	public int usedDisk;
	public SystemInformation(long pm,long upm,long fpm,long vm,long uvm,long fvm,int cp,int tdisk,int udisk,int fdisk,String di,String ad)
	{
		physicalMem=pm;
		freePhysicalMem=fpm;
		usedPhysicalMem=upm;
		virtualMem=vm;
		freeVirtualMem=fvm;
		usedVirtualMem=uvm;
		cpu=cp;
		totalDisk=tdisk;
		freeDisk=fdisk;
		usedDisk=udisk;
		folderName=di;
		address=ad;
	}
}

