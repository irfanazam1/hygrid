import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
interface MediatorInterface extends Remote
{
	boolean registerDatabase(DatabaseInstance database) throws RemoteException; 
	boolean unregisterDatabase(String database) throws RemoteException;
	boolean registerClient(RemoteJobInterface client) throws RemoteException;
	boolean unregisterClient(RemoteJobInterface client) throws RemoteException;
	boolean containsData(DatabaseInstance database,String selectStatement) throws RemoteException;
	String containsData(String selectStatement) throws RemoteException;
	DatabaseInstance getDatabase(String address) throws RemoteException;
	HashSet getDatabases() throws RemoteException; //returns the registered database instances
	HashSet getValidDatabses();  //returns the valid online database instances
	void manipulateData(DatabaseInstance database, String DMLQuery) throws RemoteException;
	void manipulateData(String DMLQuery) throws RemoteException; //replicate to all registered databases
	void manipulateData(DatabaseInstance database, ArrayList DMLQuerys) throws RemoteException;
	void manipulateData(ArrayList DMLQuerys) throws RemoteException; //replicate to all registered databases
	ArrayList selectData(DatabaseInstance database,String selectStatement) throws RemoteException;
	ArrayList selectData(String selectStatement) throws RemoteException;
}