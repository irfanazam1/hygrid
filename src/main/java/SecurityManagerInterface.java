import java.rmi.*;
import java.util.HashSet;
/**
Interface functions for the security
*/
public interface SecurityManagerInterface extends Remote
{
	
	boolean signOn(String user,String pass) throws RemoteException;
	boolean signOff(String user,String pass)throws RemoteException;
	boolean registerUser(String user,String pass,String org,boolean computation,boolean storage)throws RemoteException;
	boolean unregisterUser(String user,String pass)throws RemoteException;
	boolean registerMachine(String address,String org,boolean computation,boolean storage)throws RemoteException;
	boolean unregisterMachine(String address)throws RemoteException;
	boolean authenticate(String userName, String password) throws RemoteException;
	HashSet getNodes()throws RemoteException;
	HashSet getUsersForComputation()throws RemoteException;
	HashSet getUsersForStorage()throws RemoteException;
	HashSet getNodesForComputation()throws RemoteException;
	HashSet getNodesForStorage()throws RemoteException;
	
}