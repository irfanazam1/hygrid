import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.ArrayList;
/**
* Broker is used to gather the information of the machines particpating
on GRID. It collects the informations about CPU,Memory,Disk,Processes,
Virtual Memory and Detailed system information. Broker can used
through this inteface and the information can be obtained by other
classes by providing the IP address of the machine whose information
is needed. It will work as a server for broker services running on individual
machines which register with this broker server and provide information
about their systems on demand. 
*/
public interface BrokerServerInterface extends Remote
{
	/**
	*Used to register a local broker with the Broker Server
	*/
	int register(String address,BrokerInterface broker) throws RemoteException;
	
	/**
	*Used to register a local broker with the Broker Server
	*/
	
	/**
	*Gives the system information of all the machines connected to the broker
	*/
	Hashtable getSysInfo() throws RemoteException;
	/**
	*Provides the System information of the machine whose address 
	*will be provided
	*/
	SystemInformation getSystemInfo(String address) throws RemoteException;
	/**
	*Provides the Process information of the machine whose address 
	*will be provided
	*/
	ArrayList getProcessInfo(String address) throws RemoteException;
	/**
	*Provides the Detailed System information of the machine whose address 
	*will be provided
	*/
	ArrayList getDetailInfo(String address) throws RemoteException;
	/**
	*Provides the shared folder information of the machine whose address 
	*will be provided
	*/
	String getSharedFolder(String address) throws RemoteException;
	/**
	*Provides the addresses of all the nodes connected to the broker
	*/
	HashSet getNodes() throws RemoteException;
	/**
	*Provides the CPU %usage of the machine whose address 
	*will be provided
	*/
	int getCpuPercent(String address) throws RemoteException;
	/**
	*Provides the Memory %usage of the machine whose address 
	*will be provided
	*/
	int getMemPercent(String address) throws RemoteException;
	/**
	*Provides the Disk %usage of the machine whose address 
	*will be provided
	*/
	int getDiskPercent(String address) throws RemoteException;
	/**
	*Provides the Virtual Memory %usage of the machine whose address 
	*will be provided
	*/
	int getVMemPercent(String address) throws RemoteException;
	/**
	*Provides the Free Shared Disk size of the machine whose address 
	*will be provided
	*/
	int getFreeDisk(String address) throws RemoteException;
	/**
	*Provides the Free Disk size of a specified disk of the machine whose address 
	*will be provided
	*/
	int getFreeDisk(String address,String disk) throws RemoteException;
	/**
	*Provides the Used Shared Disk size of the machine whose address 
	*will be provided
	*/
	int getUsedDisk(String address) throws RemoteException;
	/**
	*Provides the Total Shared Disk size of the machine whose address 
	*will be provided
	*/
	int getTotalDisk(String address) throws RemoteException;
	
	/**
	*Provides the Free Memory of the machine whose address 
	*will be provided
	*/
	long getFreeMemory(String address) throws RemoteException;
	/**
	*Provides the Total Memory of the machine whose address 
	*will be provided
	*/
	long getTotalMemory(String address) throws RemoteException;
	/**
	*Provides the Used Memory of the machine whose address 
	*will be provided
	*/
	long getUsedMemory(String address) throws RemoteException;
	/**
	*Provides the Free Virtual Memory of the machine whose address 
	*will be provided
	*/
	long getFreeVirtualMemory(String address) throws RemoteException;
	/**
	*Provides the Total Virtual Memory of the machine whose address 
	*will be provided
	*/
	long getTotalVirtualMemory(String address) throws RemoteException;
	/**
	*Provides the Used Virtual Memory of the machine whose address 
	*will be provided
	*/
	long getUsedVirtualMemory(String address) throws RemoteException;
	/**
	*Security manager will update the information about the machines 
	*registered with the securitymanager. It will be called automatically
	*by the security manager whenever a machine is regietered or unregostered
	*/
	void removeNode(String machine) throws RemoteException;
	
}
