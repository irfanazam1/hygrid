import java.rmi.*;
import java.util.*;

interface BrokerInterface extends Remote
{
	SystemInformation getSystemInformation() throws RemoteException;
	ArrayList getProcesses() throws RemoteException;
	ArrayList getDetails() throws RemoteException;
	String getSharedFolder() throws RemoteException;
	int getCpuPercent() throws RemoteException;
	int getMemPercent() throws RemoteException;
	int getDiskPercent() throws RemoteException;
	int getVMemPercent() throws RemoteException;
	int getFreeDisk() throws RemoteException;
	int getFreeDisk(String disk) throws RemoteException;
	int getTotalDisk() throws RemoteException;
	int getUsedDisk() throws RemoteException;
	long getFreeMemory() throws RemoteException;
	long getFreeVirtualMemory() throws RemoteException;
	long getTotalMemory() throws RemoteException;
	long getTotalVirtualMemory() throws RemoteException;
	long getUsedMemory() throws RemoteException;
	long getUsedVirtualMemory() throws RemoteException;
}