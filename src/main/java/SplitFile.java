import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Random;

/*GUI Packages*/
import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;

class SplitThread extends Thread implements RemoteJobInterface
{
	private File fileName;
	private int sequence;
	private long fromBytes;
	private long toBytes;
	private long maxBytes;
	private String Schedular;
	private String address;
	private long jobId;
	private DataJob job;
	private byte[] buf;
	private SplitFile split;
	private boolean addRow;
	public SplitThread(File file,long job,int seq,long from,long to,long max,SplitFile ser,String sch,String ad,boolean row)
	{
		fileName=file;
		sequence=seq;
		fromBytes=from;
		toBytes=to;
		split=ser;
		maxBytes=max;
		Schedular="rmi://";
		Schedular+=sch;
		Schedular+="/schedular";
		address=ad;
		jobId = job;
		addRow=row;
	}	
	public void run()
	{
		
		try
		{
			while(split.getBytesSent()>(10*split.ONEMB) )
			{
				
				sleep(50);
			}
			while (split.updating)
			{
				sleep(50);
			}
			split.updating=true;
			split.incrementBytes();
			split.updating=false;
			job = new DataJob((int)jobId,sequence,maxBytes,(int)(toBytes-fromBytes),1,false," ");
			job.force=false;
			job.serviceName="DataStorage";
			job.intensity=GridJob.DATA_INTENSIVE;
			job.source=address;
			job.userName = split.username;
         	FileInputStream fis = new FileInputStream(fileName);
			fis.skip(sequence*maxBytes);
			fis.read(job.buf);
			SchedularInterface server = (SchedularInterface)Naming.lookup(Schedular);
			
			if(addRow)
         	split.addRowToStoreTable(job);			
         	else
         	split.updateStoreTableStatus(job.sequence,"Ready");
			
			UnicastRemoteObject.exportObject(this);
			int result=server.submitJob(job,this);
			if(result == SchedularInterface.MACHINE_NOT_AUTHORIZED || result == SchedularInterface.USER_NOT_AUTHORIZED)
			{
				System.out.println("Either you or your machine are not authorized on grid");
				split.decrementBytes();
				return ;
				
			}
			job.buf=null;
			fis.close();
			
			
		}
		catch(NotBoundException e)
		{
			split.addStoragePendingJob(job);
			System.out.println("Error Connecting Schedular\nSchedular might not ready..");
			
		}
		catch(OutOfMemoryError err)
		{
			split.addStoragePendingJob(job);
			System.out.println("Virtual Machine gone out of Memory");
			
				
		}
		catch(Exception e)
		{
			split.addStoragePendingJob(job);
			e.printStackTrace();
			
		}
	
	}
	public void setStatus(int status,GridJob job) throws RemoteException
	{
		DataJob dJob = (DataJob)job;
		dJob.buf=null;
		if (status==SchedularInterface.ELAPSED)
		{
			try
			{
				split.updateStoreTableStatus(dJob.sequence,"Waiting Time Eplapsed");
				split.addStoragePendingJob(dJob);
				while (split.updating)
				{	
					try
					{
						sleep(50);
					}
					catch(Exception e)
					{
					}
				}
				split.updating=true;
				split.decrementBytes();
				split.updating=false;
				UnicastRemoteObject.unexportObject(this,true);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}

		}
		else if (status == SchedularInterface.NOTSUPPORTED)
		{
			try
			{
				split.updateStoreTableStatus(dJob.sequence,"Service Not Supported");
				split.addStoragePendingJob(dJob);
				while (split.updating)
				{	
					try
					{
						sleep(50);
					}
					catch(Exception e)
					{
					}
				}
				split.updating=true;
				split.decrementBytes();
				split.updating=false;
				UnicastRemoteObject.unexportObject(this,true);
				
				
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		
		}
		
	}
	public void setStatus(int status) throws RemoteException
	{
		if(status==SchedularInterface.READY)
		{
			split.updateStoreTableStatus(job.sequence,"Ready");
			
		}
		else if (status == SchedularInterface.PROCESSING)
		{
			
			split.updateStoreTableStatus(job.sequence,"Processing");
		}
		else if (status == SchedularInterface.WAITING)
		{
			
			split.updateStoreTableStatus(job.sequence,"Waiting");
		}
			
		
	}
	public void setResult(GridJob job) throws RemoteException
	{
		DataJob dJob = (DataJob)job;
		dJob.buf=null;
		while (split.updating)
		{	try
			{
				sleep(50);
			}
			catch(Exception e)
			{
			}
		}
		split.updating=true;
		split.decrementBytes();
		split.updating=false;
		try
		{
			
			split.updateStoreTable(dJob,"Done");
			split.isStorageCompleted(dJob.sequence);
			UnicastRemoteObject.unexportObject(this,true);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	}
}
class ReaderThread extends Thread implements RemoteJobInterface
{
	private String fileName;
	private String Schedular;
	private String address;
	private SplitFile split;
	private DataJob job;
	private boolean addRow;
	private String extension;
	public ReaderThread(SplitFile sp,String sch,DataJob jb,String file,String ext,boolean bool)
	{
		split = sp;
		Schedular="rmi://";
		Schedular+=sch;
		Schedular+="/schedular";
		job=jb;
		fileName=file;
		addRow=bool;
		extension="."+ext;
	}
	public void run()
	{
			try
			{
			    
				sleep(250);
				UnicastRemoteObject.exportObject(this);
				SchedularInterface server = (SchedularInterface)Naming.lookup(Schedular);
				if(addRow)
				{
         			split.addRowToRetrievelTable(job);			
         		}
         		else
         		split.updateRetrievelTableStatus(job.sequence,"Ready");
				int result = server.submitJob(job,this);
				if(result == SchedularInterface.MACHINE_NOT_AUTHORIZED || result == SchedularInterface.USER_NOT_AUTHORIZED)
				{
					System.out.println("Either you or your machine are not authorized on grid");
					return ;
				}
			
			}
			catch(NotBoundException e)
			{	
				split.addRetrievelPendingJob(job);
				System.out.println("Error Connecting Schedular\nSchedular might not ready..");
				
			}
			catch(OutOfMemoryError err)
			{
				split.addRetrievelPendingJob(job);
				System.out.println("Virtual Machine gone out of Memory");
				
				
			}
			catch(Exception e)
			{
				split.addRetrievelPendingJob(job);
				e.printStackTrace();
				
			}
			
			
	}
	public void setStatus(int status,GridJob g) throws RemoteException
	{
		DataJob dJob = (DataJob)g;
		dJob.buf=null;
		if (status==SchedularInterface.ELAPSED)
		{
			try
			{
				split.updateRetrievelTableStatus(dJob.sequence,"Waiting Time Eplapsed");
				split.addRetrievelPendingJob(dJob);
				UnicastRemoteObject.unexportObject(this,true);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (status == SchedularInterface.NOTSUPPORTED)
		{
			try
			{
				split.updateRetrievelTableStatus(job.sequence,"Service Not Supported");
				split.addRetrievelPendingJob(job);
				UnicastRemoteObject.unexportObject(this,true);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		}
		
	}
	public void setStatus(int status) throws RemoteException
	{
		if(status==SchedularInterface.READY)
		{
			split.updateRetrievelTableStatus(job.sequence,"Ready");
			
		}
		else if (status == SchedularInterface.PROCESSING)
		{
			
			split.updateRetrievelTableStatus(job.sequence,"Processing");
		}
		else if (status == SchedularInterface.WAITING)
		{
			
			split.updateRetrievelTableStatus(job.sequence,"Waiting");
		}
			
		
	}
	public void setResult(GridJob job)
	{
		DataJob dJob = (DataJob)job;
		
		if (dJob.success)
		{
			try
			{
				FileOutputStream fos;
				String folder = split.getTempFolderName();
				fos = new FileOutputStream(folder+"/"+dJob.dataJobId+"_"+dJob.sequence+extension);
				fos.write(dJob.buf,0,dJob.fileSize);
				fos.close();
				fos.flush();
				dJob.buf=null;
				split.updateRetrievelTable(dJob,"Done");
				split.isRetrievelJobsCompleted(dJob.sequence);
			}
			catch(Exception e)
			{
			}
		}
				
		try
		{
			UnicastRemoteObject.unexportObject(this,true);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	}
	
}
class DataJobDesc
{
	public int jobId;
	public int sequence;
	public String fileName;
	public String address;
	public String service;
	public String handle;
	public DataJobDesc(int id, int seq, String file,String add,String ser,String han)
	{
		jobId=id;
		sequence=seq;
		fileName=file;
		address=add;
		service=ser;
		handle=han;
	}
}
	
	
class SubmitStorageJobsThread extends Thread
{
	private int jobId;
	private int max;
	private long maxBytes;
	private File storeFile;
	private long fileSize;
	private String address;
	private String sAddress;
	private SplitFile split;
		
	public SubmitStorageJobsThread(int id,File file,long size,int m,long mbytes,String add,String s,SplitFile f)
	{
		jobId=id;
		storeFile=file;
		fileSize=size;
		max=m;
		maxBytes=mbytes;
		address=add;
		sAddress=s;
		split=f;
	}
	public void run()
	{
		int i=0;
		for(i=0;i<max-1;i++)
		{
			long from = i*maxBytes;
			long to   = i*maxBytes+maxBytes;	
			SplitThread th = new SplitThread
			(
			storeFile,
			jobId,
			i,
			from,
			to,
			maxBytes,
			split,
			sAddress,
			address,
			true
			);
			th.start();
			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		/*
		the remaining bytes will be wrtten to this
		job.
		*/
		 try
		 {
			Thread.sleep(100);
		 }
		 catch(InterruptedException e)
		 {
		 	e.printStackTrace();
		 }
		 long from = i*maxBytes;
		 long to   = fileSize;
		 SplitThread th = new SplitThread
		 (
		 storeFile,
		 jobId,
		 i,
		 from,
		 to,
		 maxBytes,
		 split,
		 sAddress,
		 address,
		 true
		 );
		 th.start();		
		
	
	}
}
class SubmitRetrievelJobsThread extends Thread
{
	private HashSet set;
	private int max;
	private SplitFile split;
	private String extension;
	private String sAddress;
	private String address;
	public SubmitRetrievelJobsThread(HashSet s,int m,String ext,String add,String sadd,SplitFile f)
	{
		set = s;
		max=m;
		split=f;
		extension=ext;
		sAddress=sadd;
		address=add;
	}
	public void run()
	{
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			
			DataJobDesc desc = (DataJobDesc)it.next();
			DataJob job = new DataJob
			(
			desc.jobId,
			desc.sequence,
			max,
			0,
			2,
			false,
			desc.fileName
			);
			job.handle=desc.handle;
			job.type = desc.service;
			job.destination=desc.address;
			job.source=address;
			job.force=true;
			job.intensity=GridJob.DATA_INTENSIVE;
			job.userName=split.username;
			job.serviceName="DataStorage";
			ReaderThread th = new ReaderThread(split,sAddress,job," ",extension,true);
			th.start();
			try
			{
				Thread.sleep(200);
			}
			catch(Exception e)
			{}
		}
			
	}
	
}
public class SplitFile extends JPanel implements ActionListener,ChangeListener,ListSelectionListener
{
	/*Constant contains the size of One MegaBytes*/
	public static final long ONEMB=1048576;
	/*Checks whether the bytes sent varibale is being updated*/
	public boolean updating;
	/*Maximum bytes allocated to the each job for the current file*/
	private long maxBytes; //1 MB

	/*Schedular Address*/
	private String sAddress; 
	/*Broker Address*/
	private String brokerAddress;
	/*Computer Address*/
	private String address;
	/*FileName to read from Disk*/
	private String fileName;
	/*File Extension*/
	private String ext;
	/*JobId for the new DataJob*/
	private int jobId;
	/*Total Bytes Sent over the netowrk during the current job*/
	private long bytesSent;
	/*The size of the file to be stored*/
	private long fileSize;
	/*Total Number of jobs calculated from the file*/
	private int totalJobs;
	/*DataStructure to store the file description to create jobs for
	retrievel, This will be populated from the database based on
	the jobid also selected from the database*/
	private DataJobDesc desc;
	/*Frame to create the main frame*/
	private JFrame frame;
	/*Tabbed Pane to hold the tab pages for storage and retrievel*/
	private JTabbedPane tabs;
	/*Table and Model to store the information for storage jobs*/
	private JTable storeJobsTable;
	private DefaultTableModel storeJobsModel;
	/*Button to select file for storage*/
	private JButton browseSelect;
	/*Fields to store the file information for storage jobs*/
	/*********************************/
	private JTextField filePathText;
	private JTextField fileSizeText;
	private JTextField totalJobsText;
	private JTextField partSizeText;
	/**********************************/
	/*File to be stored on the network*/
	private File storeFile;
	/*Tables and their Models for Retrievel and Datajobs information*/
	/***********************************************/
	private JTable retrievelJobsTable;
	private JTable dataJobsTable;
	private JTable retrievelTotalJobsTable;
	private DefaultTableModel retrievelJobsModel;
	private DefaultTableModel retrievelTotalJobsModel;
	private DefaultTableModel dataJobsModel;
	/***********************************************/
	/*Button to select a file to store the retrieved data*/
	private JButton browseSave;
	/*Text Fields to store information about the retrived file*/
	/**********************************************/
	private JTextField fileRetrievelPathText;
	private JTextField fileRetrievelSizeText;
	private JTextField partRetrievelSizeText;
	/***********************************************/
	/*Button to get files information stored on the network from Database*/
	private JButton retrievelJobsButton;
	/*MenuItems for Data Storage and Retrievel*/
	private JMenuItem storeMenuItem,retrieveMenuItem;
	/*Menu Item for Pending jobs*/
	private JMenuItem pendingMenuItem;
	/*File Chooser*/
	private JFileChooser fc;
	
	/*Hash Table to store the information about the storage jobs
	This will be used while popualting and updating the
	tables storing information about the storage jobs
	*/
	private Hashtable storageJobs = new Hashtable();
	
	/*Hash Table to store the information about the retrievel jobs
	This will be used while popualting and updating the
	tables storing information about the retrievel jobs
	*/
	private Hashtable retrievelJobs = new Hashtable();
	/*Hash Table to store the jobs which are pending due to some errors*/
	private HashSet storagePendingJobs = new HashSet();
	/*Conatains the count for the storage jobs being processed*/
	private int jobCount;
	/*HashTable to store information about the retrievel jobs.
	This will be used in creating the retrievel jobs and will
	be populated from the database
	*/
	private HashSet retrievelTotalJobs = new HashSet();
	
	/*HashTable to store information about the retrievel pendding jobs.
	This will be used in creating the retrievel jobs and will
	be populated due to errors while retrieving data from the network
	*/
	private HashSet retrievelPendingJobs = new HashSet();
	
	private int retrievelJobCount;
	/*Total number of retrievel jobs*/
	private int totalretrievelJobs;
	/*Max bytes for the current retrievel job*/
	private int retrievelMaxBytes;
	/*Count to check whether all the storage jobs are done*/
	private static int storageJobsCompleteCount=0;
	/*Count to check whether all the retrievel jobs are done*/
	private static int retrievelJobsCompleteCount=0;
	/*Store the files retrieved from the netwrok temporarily
	and will be deleted auotmatically after combining all the files
	*/
	private long retrievelFileSize;
	private String tempFolderName;
	/*File to be stored after combining all the retrieved files*/
	private File combinedFile;
	/*Extension of the file to be stored after retrievel*/
	private String extension;
	/*JobId of the retrievel jobs to be used to make file names for
	retrieved files*/
	private int retJobId;
	/*File Object attached to the temporary folder*/
	private File tempFolder;
	/*Progress Bars*/
	private ProgressBar retrieveBar,storageBar;
	public static String username;
	public SplitFile()
	{
		retrievelFileSize=0;
		retJobId=0;
		tempFolderName="";
		combinedFile=null;
		extension="";
		totalretrievelJobs=0;	
		retrievelMaxBytes=0;
		jobCount=0;
		retrievelJobCount=0;
		totalJobs=0;
		fc=null;
		storeFile=null;
		maxBytes=0;
		bytesSent=0;
		updating=false;
		jobId=0;
		sAddress="";
		address="";
		fileName="";
		ext="";
		desc=null;
		sAddress="";
		brokerAddress="";
		
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			address= inet.getHostAddress();
		}
		catch(java.net.UnknownHostException une)
		{
			une.printStackTrace();
			
		}
		init();
		
	}
	private void init()
	{
		setLayout(new BorderLayout());	
		
		/*Panels and Componentsfor the store file tab*/
		
		/*Panel and Components for File Selection*/
		JPanel filePanel = new JPanel();
		filePanel.setLayout(new SpringLayout());
		TitledBorder titled;
        titled = BorderFactory.createTitledBorder("Select File"); 
        filePathText = new JTextField("",40);
        browseSelect = new JButton("Browse");
        browseSelect.addActionListener(this);
        filePanel.setBorder(titled);
        filePanel.add(new JLabel("File Name"));
        filePanel.add(filePathText);
        filePanel.add(browseSelect);
        browseSelect.setActionCommand("SelectFile");
        SpringUtilities.makeCompactGrid(filePanel,
                                        1, 3, //rows, cols
                                        10, 20,        //initX, initY
                                        20, 40);       //xPad, yPad
		
        /*Panel and Components for file information*/
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new SpringLayout());
        
        fileSizeText = new JTextField();
        totalJobsText = new JTextField();
        partSizeText = new JTextField();
        infoPanel.add(new JLabel("Size"));
        infoPanel.add(fileSizeText);
        infoPanel.add(new JLabel("TotalJobs"));
        infoPanel.add(totalJobsText);
        infoPanel.add(new JLabel("JobSize"));
        infoPanel.add(partSizeText);
        titled = BorderFactory.createTitledBorder("File Information"); 
        infoPanel.setBorder(titled);
        SpringUtilities.makeCompactGrid(infoPanel,
                                        1,6, //rows, cols
                                        10, 20,        //initX, initY
                                        20, 30);       //xPad, yPad
		          
        /* Panel and Components for store jobs Table*/
        storeJobsModel = new DefaultTableModel();
        storeJobsModel.addColumn("ID");
        storeJobsModel.addColumn("Sequence");
        storeJobsModel.addColumn("Status");
        storeJobsModel.addColumn("FileName");
        storeJobsModel.addColumn("Source");
        storeJobsModel.addColumn("Target");
        storeJobsModel.addColumn("Service");
        storeJobsModel.addColumn("Handle");
        storeJobsTable = new JTable(storeJobsModel);
        JPanel donePanel = new JPanel();
        donePanel.setLayout(new BorderLayout());
        titled = BorderFactory.createTitledBorder("Jobs Information"); 
        JScrollPane donePane = new JScrollPane(storeJobsTable);
        donePane.setBorder(titled);
        donePanel.add(donePane);
        
        /*Split pane to add jobs table,file and info panels*/
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel holder = new JPanel();
        holder.setLayout(new GridLayout(2,1));
        holder.add(filePanel);
        holder.add(infoPanel);
        /*Addinng panels to splitpane*/
        splitPane.setTopComponent(holder);
        splitPane.setBottomComponent(donePanel);
        splitPane.setDividerLocation(170);
              
        /*Tabded pane to hold pages*/
        tabs = new JTabbedPane();
        tabs.addTab("Store File",splitPane);
        
        /*Controls and Panels for Retrievel Jobs*/
       
       /*Panel and Components for File Selection*/
        filePanel = new JPanel();
		filePanel.setLayout(new SpringLayout());
		
        titled = BorderFactory.createTitledBorder("Get Files"); 
        retrievelJobsButton = new JButton("Get");
        retrievelJobsButton.setActionCommand("Get");
        retrievelJobsButton.addActionListener(this);
        filePanel.setBorder(titled);
        
        dataJobsModel = new DefaultTableModel();
        dataJobsModel.addColumn("ID");
        dataJobsModel.addColumn("FileName");
        dataJobsModel.addColumn("Ext");
        dataJobsModel.addColumn("Size");
        dataJobsModel.addColumn("Max Bytes");
        dataJobsTable = new JTable(dataJobsModel);
        dataJobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = dataJobsTable.getSelectionModel();
        rowSM.addListSelectionListener(this);
        JScrollPane dataPane = new JScrollPane(dataJobsTable);
       
        filePanel.add(new JLabel("Files"));
        filePanel.add(dataPane);
        filePanel.add(retrievelJobsButton);
        SpringUtilities.makeCompactGrid(filePanel,
                                        1, 3, //rows, cols
                                        10, 10,        //initX, initY
                                        20, 10);       //xPad, yPad
		
       	
       	/*Components for file info panel*/
       	
       	infoPanel = new JPanel();
        infoPanel.setLayout(new SpringLayout());
        
        fileRetrievelSizeText = new JTextField();
        partRetrievelSizeText = new JTextField();
        fileRetrievelPathText = new JTextField("",15);
        browseSave = new JButton("SaveAs");
        browseSave.setActionCommand("SaveAs");
        browseSave.addActionListener(this);
        browseSave.setEnabled(false);
        infoPanel.add(browseSave);
        
        infoPanel.add(fileRetrievelPathText);
        infoPanel.add(new JLabel("size"));
        infoPanel.add(fileRetrievelSizeText);
        infoPanel.add(new JLabel("JobSize"));
        infoPanel.add(partRetrievelSizeText);
        titled = BorderFactory.createTitledBorder("File Information"); 
        infoPanel.setBorder(titled);
        SpringUtilities.makeCompactGrid(infoPanel,
                                        1,6, //rows, cols
                                        10, 20,        //initX, initY
                                        20, 30);       //xPad, yPad
		       
        /* Panel and Components for jobs Table*/
        JPanel retrievelTotalJobsPanel = new JPanel();
        retrievelTotalJobsPanel.setLayout(new BorderLayout());
        titled = BorderFactory.createTitledBorder("Total Jobs"); 
        
        
        retrievelTotalJobsModel = new DefaultTableModel();
        
        retrievelTotalJobsModel.addColumn("ID");
        retrievelTotalJobsModel.addColumn("Sequence");
        retrievelTotalJobsModel.addColumn("FileName");
        retrievelTotalJobsModel.addColumn("Source");
        retrievelTotalJobsModel.addColumn("Service");
        retrievelTotalJobsModel.addColumn("Handle");
        retrievelTotalJobsTable = new JTable(retrievelTotalJobsModel);
        JScrollPane retrievelTotalJobsPane = new JScrollPane(retrievelTotalJobsTable);
        retrievelTotalJobsPane.setBorder(titled);
        
        
        
        retrievelJobsModel = new DefaultTableModel();
        retrievelJobsModel.addColumn("ID");
        retrievelJobsModel.addColumn("Sequence");
        retrievelJobsModel.addColumn("Status");
        retrievelJobsModel.addColumn("FileName");
        retrievelJobsModel.addColumn("Source");
        retrievelJobsModel.addColumn("Target");
        retrievelJobsModel.addColumn("Service");
        retrievelJobsModel.addColumn("Handle");
        retrievelJobsTable = new JTable(retrievelJobsModel);
        
        donePanel = new JPanel();
        donePanel.setLayout(new BorderLayout());
        titled = BorderFactory.createTitledBorder("Jobs Done"); 
        donePane = new JScrollPane(retrievelJobsTable);
        donePane.setBorder(titled);
        donePanel.add(donePane);
        
        
        holder = new JPanel();
        holder.setLayout(new GridLayout(3,1));
        holder.add(filePanel);
        holder.add(infoPanel);
        holder.add(retrievelTotalJobsPane);
        
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(holder);
        splitPane.setBottomComponent(donePanel);
        
        splitPane.setDividerLocation(300);
        tabs.addTab("Retrieve File",splitPane);        
        
        add(tabs);
        tabs.addChangeListener(this);
        setPreferredSize(new Dimension(700,500));
        
    }
    public void stateChanged(ChangeEvent e)
    {
       	JTabbedPane source = (JTabbedPane)e.getSource();
        int index = (int)source.getSelectedIndex();
        if(index==0)
        {
        	if(storagePendingJobs.size()>0)
        	pendingMenuItem.setEnabled(true);
        }
        else
        {
        	
        	if(retrievelPendingJobs.size()>0)
        	pendingMenuItem.setEnabled(true);
        }
        
	}
	public void storeFile()
	{
		long from=0;
		long to=0;
		int max=0;
		int  seq=0;
		int pieces=0;
		/*This Code segment in the try block selects the maximum
		Datajob id stored in the Mysql Database named grid*/
		Connection con=null;
		Statement stmt=null;
		try
		{
				
			/*Loading the mysql jdbc driver*/
			Class.forName("com.mysql.jdbc.Driver").newInstance();
      		/*Getting the database connection*/
      		con = DriverManager.getConnection("jdbc:mysql://192.168.0.1/grid",
        	"grid", "grid");
        	/*Select statement to get the max jobid*/
        	String query = "Select max(jobid) jobid from datajob";
        	if(!con.isClosed())
        	{
        		stmt = con.createStatement();
				/*Getting the result set*/
      			ResultSet result = stmt.executeQuery(query);
      			max=0;
      			while(result.next())
      			{
      				max = result.getInt("jobid"); 
      			}
      			if(max==0)
      			{
      		    	jobId=1;
      			}
      			else
      			{
      				/*Next job id*/
      				jobId=max+1; 
      			}
      			result=null;
      			  				
      			stmt.close();
      		}
			
		}
			
		catch(SQLException sqe)
		{
			sqe.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		/*Closing the connection after completion or incase of error*/
		finally
		{
			try
			{
				if(!con.isClosed())
				con.close();
				con=null;	
			}
			catch(Exception e)
			{}
		}
		
		/*This code segment is written to seperate the extension of
		the file name selected for storage, the block is used becuase
		the variable index and string name are not required after this
		block, so they will be destroyed after leaving this block
		*/
		{
			int index=0;
			String name = storeFile.getName();
			index = name.indexOf(".",0);
			ext = name.substring(index+1);
		}
		/*This code segment calculates the max bytes size to be sent
		over the network, from the file size
		*/
		if (fileSize<=(5*ONEMB))
		{
			System.out.println("No Need to transfer the file");
		}
		else if(fileSize<=(500*ONEMB))
		maxBytes=5*ONEMB;
		else
		maxBytes = 7*ONEMB;	
		/*This segment stores the information in the database
		about the newly seleted file for storage
		*/
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
      			/*Getting the database connection*/
      		con = DriverManager.getConnection("jdbc:mysql://192.168.0.1/grid",
        	"grid", "grid");	
        	if(!con.isClosed())
        	{
        		stmt = con.createStatement();
        		String insert = "insert into datajob values( "
        		+jobId+","+"'"+fileName+"'"+","
        		+"'"+ext+"'"+","+fileSize+","
        		+maxBytes+" )";
        		stmt.executeUpdate(insert);
        		stmt.close();
        	}
        	
        		
         }
        catch(Exception e)
        {
        }
        finally
        {
        	try
        	{
        		if(!con.isClosed())
        		{
        			con.close();
        			con=null;
        		}
        	}
        	catch(Exception e)
        	{}
        }
        	
		partSizeText.setText(""+maxBytes+" Bytes");	
		/*determine the pieces of the file
		by dividing the filesize with the max bytes
		Pieces will be used to read a fixed amount of data 
		from the file
		*/
		pieces = (int)(fileSize/maxBytes);
		/*Now to calculate maximum jobs.
		Max jobs will be equal to the pieces if the filesize
		is completely divided by the maxbytes for a job.
		Max will be used to read data for the last file
		If max is greator than the pieces then the last file
		will contain data less than the max bytes
		*/
		if(fileSize%maxBytes==0)
		{
			max=pieces;	
		}
		/*Else the max jobs will be one more than pieces,
		because the remaining bytes will be stored in the
		last job which are less the maxbyes
		*/
		else
		max=pieces+1;
		
		/*Set the total jobs equal to max*/
		totalJobs=max;
		totalJobsText.setText(""+totalJobs);
		int i=0;
		/*Creating the Progress bar to display storage progress*/
		Rectangle rect = getBounds();
		storageBar = new ProgressBar(totalJobs," Storing File ");
		storageBar.showProgressBar(new Point(rect.width,rect.height));
		storageBar.setValue(0);
		/*Submitting the storage jobs*/
		SubmitStorageJobsThread th = new SubmitStorageJobsThread
		(jobId,storeFile,fileSize,max,maxBytes,address,sAddress,this);
		th.start();
		
	   	
	
				
	}
	
	public void addRowToStoreTable(DataJob job)
	{
		Object[] row = 
		{
			new Integer(job.dataJobId),
			new Integer(job.sequence),
			"",
			"",
			address,
			"",
			job.serviceName,
			""
		};
		storageJobs.put(" "+job.sequence," "+jobCount);
		storeJobsModel.addRow(row);
		jobCount++;
		
					
	}
	public void updateStoreTableStatus(int row,String status)
	{
		String str = (String)storageJobs.get(" "+row);
		int rowid = Integer.parseInt(str.trim());
		storeJobsModel.setValueAt(status,rowid,2);
	}
	public void updateStoreTable(DataJob job,String status)
	{
		String str = (String)storageJobs.get(" "+job.sequence);
		int rowid = Integer.parseInt(str.trim());
		storeJobsModel.setValueAt(status,rowid,2);
		storeJobsModel.setValueAt(job.fileName,rowid,3);
		storeJobsModel.setValueAt(job.destination,rowid,5);
		storeJobsModel.setValueAt(job.handle,rowid,7);
		
	}
	public void addStoragePendingJob(DataJob job)
	{
		storagePendingJobs.add(job);
		pendingMenuItem.setEnabled(true);
		if(storageBar!=null)
		storageBar.setValue(storageJobsCompleteCount+storagePendingJobs.size());
	}
	public void isStorageCompleted(int sequence)
	{
		if(storageJobsCompleteCount<totalJobs)
		storageJobsCompleteCount++;
		
		if(storageBar!=null)
		storageBar.setValue(storageJobsCompleteCount+storagePendingJobs.size());
		
		if(storageJobsCompleteCount==totalJobs)
		{
			storageBar.closeProgress();
			storageBar=null;
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Ok"};
			int answer = JOptionPane.showOptionDialog
			(
       			SwingUtilities.getWindowAncestor(this),
       			"File Storage Completed..",
			   	"Completion",
           		JOptionPane.OK_OPTION,
           		JOptionPane.INFORMATION_MESSAGE,
           		null,
           		obj,
           		obj[0]
			);
		
			storeMenuItem.setEnabled(false);
			browseSelect.setEnabled(true);
			jobCount=0;
			storageJobsCompleteCount=0;
		}
		else if(storageJobsCompleteCount+storagePendingJobs.size()==totalJobs)
		{
			storageBar.closeProgress();
			storageBar=null;
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Ok"};
			int answer = JOptionPane.showOptionDialog
			(
       			SwingUtilities.getWindowAncestor(this),
       			"File Storage Status\nJobs Completed= "+storageJobsCompleteCount+" Jobs Pending= "+storagePendingJobs.size(),
		   		"Storage Status",
           		JOptionPane.OK_OPTION,
           		JOptionPane.INFORMATION_MESSAGE,
           		null,
           		obj,
           		obj[0]
			);
			
		}
		
	}
	synchronized public void isRetrievelJobsCompleted(int sequence)
	{
		
		if(retrievelJobsCompleteCount<retrievelTotalJobs.size())
		retrievelJobsCompleteCount++;
		if(retrieveBar!=null)
		retrieveBar.setValue(retrievelJobsCompleteCount+retrievelPendingJobs.size());
		
		if(retrievelJobsCompleteCount==retrievelTotalJobs.size())
		{
			retrieveBar.closeProgress();
			retrieveBar=null;
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Ok"};
			int answer = JOptionPane.showOptionDialog
			(
       			SwingUtilities.getWindowAncestor(this),
       			"File Retreivel Completed..",
		   		"Completion",
           		JOptionPane.OK_OPTION,
           		JOptionPane.INFORMATION_MESSAGE,
           		null,
           		obj,
           		obj[0]
			);
			retrievelJobsCompleteCount=0;
			retrievelJobCount=0;
			browseSave.setEnabled(true);
			dataJobsTable.setEnabled(true);
			
			writeFiles();
			
			
		}
		else if(retrievelJobsCompleteCount+retrievelPendingJobs.size()==retrievelTotalJobs.size())
		{
			retrieveBar.closeProgress();
			retrieveBar=null;
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Ok"};
			int answer = JOptionPane.showOptionDialog
			(
       			SwingUtilities.getWindowAncestor(this),
       			"File Retrievel Status\nJobs Completed= "+retrievelJobsCompleteCount+" Jobs Pending= "+retrievelPendingJobs.size(),
		   		"Retrievel Status",
           		JOptionPane.OK_OPTION,
           		JOptionPane.INFORMATION_MESSAGE,
           		null,
           		obj,
           		obj[0]
			);
			pendingMenuItem.setEnabled(true);
			
		}
		
		
			
	}
	private void populateDataJobsTable()
	{
		Connection con=null;
		Statement stmt = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
      		/*Getting the database connection*/
      		con = DriverManager.getConnection("jdbc:mysql://192.168.0.1/grid",
        	"grid", "grid");	
        	if(!con.isClosed())
        	{
        		stmt = con.createStatement();
        		String select = "Select * from datajob";
        		stmt = con.createStatement();
        		ResultSet result = stmt.executeQuery(select);
        		if(dataJobsModel.getRowCount()>0)
        		{
        			int rows = dataJobsModel.getRowCount();
					for(int i=rows-1;i>=0;i--)
					{
						dataJobsModel.removeRow(i);
					}
					
        		}
        		while(result.next())
        		{
        			
        			Object[] obj = 
        			{
        				new Integer(result.getInt("jobid")),result.getString("filename"),
        				result.getString("ext"),new Long(result.getLong("size")),
        				new Integer(result.getInt("maxbytes"))
        			};
        			dataJobsModel.addRow(obj);
        			
        		}
        		result.close();
        		stmt.close();
        		
        	}
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	try
        	{
        		if(!con.isClosed())
        		{
        			con.close();
        			con=null;
        		}
        	}
        	catch(Exception e)
        	{}
        }
        
        	
	}
	public void valueChanged(ListSelectionEvent e) 
	{
         if (e.getValueIsAdjusting()) return;
	     ListSelectionModel lsm = (ListSelectionModel)e.getSource();
         if (!lsm.isSelectionEmpty()) 
         {
           int index=lsm.getMinSelectionIndex();
           Integer val  = (Integer)dataJobsModel.getValueAt(index,0);
           retJobId=val.intValue();
           populateretrievelTotalJobsTable(retJobId);
           Long size = (Long)dataJobsModel.getValueAt(index,3);
           retrievelFileSize = size.longValue();
           Integer retrievelMaxBytes = (Integer)dataJobsModel.getValueAt(index,4);
           fileRetrievelSizeText.setText(size+" (Bytes)");
           partRetrievelSizeText.setText(retrievelMaxBytes+ "(Bytes)");
           extension=(String)dataJobsModel.getValueAt(index,2);
           browseSave.setEnabled(true);
           
         } 
                             
    }
    private void populateretrievelTotalJobsTable(int id)
    {
    	Connection con=null;
		Statement stmt = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
      		/*Getting the database connection*/
      		con = DriverManager.getConnection("jdbc:mysql://192.168.0.1/grid",
        	"grid", "grid");	
        	if(!con.isClosed())
        	{
        		
        		stmt = con.createStatement();
        		String select = "Select * from datajob_desc where jobid = "+id+" order by seqno";
        		stmt = con.createStatement();
        		ResultSet result = stmt.executeQuery(select);
        		if(retrievelTotalJobsModel.getRowCount()>0)
        		{
        			int rows = retrievelTotalJobsModel.getRowCount();
					for(int i=rows-1;i>=0;i--)
					{
						retrievelTotalJobsModel.removeRow(i);
					}
					retrievelTotalJobs.clear();
					
        		}
        		while(result.next())
        		{
        			
        			Object[] obj = 
        			{
        				new Integer(result.getInt("jobid")),new Integer(result.getInt("seqno")),
        				result.getString("filename"),result.getString("Address"),
        				result.getString("Servicetype"),result.getString("handle")
        				
        			};
        			DataJobDesc desc = new DataJobDesc
        			( 
        			( (Integer)(obj[0]) ).intValue(),
        			( (Integer)(obj[1]) ).intValue(),
        			  (String)obj[2],(String)obj[3],
        			  (String)obj[4],(String)obj[5]
        			);
        			retrievelTotalJobs.add(desc);
        			retrievelTotalJobsModel.addRow(obj);
        			
        		}
        		result.close();
        		stmt.close();
        		
        	}
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	try
        	{
        		if(!con.isClosed())
        		{
        			con.close();
        			con=null;
        		}
        	}
        	catch(Exception e)
        	{}
        }
        
    }
    public void addRowToRetrievelTable(DataJob job)
	{
		Object[] row = 
		{
			new Integer(job.dataJobId),
			new Integer(job.sequence),
			"",
			"",
			address,
			"",
			job.serviceName,
			""
		};
		retrievelJobs.put(" "+job.sequence," "+retrievelJobCount);
		retrievelJobsModel.addRow(row);
		retrievelJobCount++;
		
					
	}
	
    public void updateRetrievelTableStatus(int row,String status)
	{
		String str = (String)retrievelJobs.get(" "+row);
		int rowid = Integer.parseInt(str.trim());
		retrievelJobsModel.setValueAt(status,rowid,2);
	}
	public void updateRetrievelTable(DataJob job,String status)
	{
		String str = (String)retrievelJobs.get(" "+job.sequence);
		int rowid = Integer.parseInt(str.trim());
		retrievelJobsModel.setValueAt(status,rowid,2);
		retrievelJobsModel.setValueAt(job.fileName,rowid,3);
		retrievelJobsModel.setValueAt(job.destination,rowid,5);
		retrievelJobsModel.setValueAt(job.handle,rowid,7);
		
	}
	public void addRetrievelPendingJob(DataJob job)
	{
		retrievelPendingJobs.add(job);
		if(retrieveBar!=null)
		retrieveBar.setValue(retrievelJobsCompleteCount+retrievelPendingJobs.size());
	}  
	private void getFileToStore()
	{
		if (fc==null)
		{
			fc = new JFileChooser();
		}
		fc.setAcceptAllFileFilterUsed(true);
		int returnVal = fc.showSaveDialog(this);
		
        if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			if(retrievelTotalJobs.size()>0)
			{
				try
				{
					
					int totalFree=0;
					BrokerServerInterface iFace = (BrokerServerInterface)Naming.lookup(brokerAddress);
					String str = fc.getSelectedFile().getParent();
					String root = "";
					{
						int index = 0;
						if((index=str.indexOf(":"))>=0)
						{
							root = str.substring(0,index+1);									
							totalFree = iFace.getFreeDisk(address,root);
						}
						else
						totalFree = iFace.getFreeDisk(address,str);
					}
					if(totalFree==-1)
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
       					SwingUtilities.getWindowAncestor(this),
       					"Broker Is not ready",
		   				"Broker Error",
           				JOptionPane.OK_OPTION,
           				JOptionPane.ERROR_MESSAGE,
           				null,
           				obj,
           				obj[0]
						);
					}
					else
					{
						if(totalFree > ((retrievelFileSize*2)/1024)/1024)
						{
							combinedFile=fc.getSelectedFile();
							fileRetrievelPathText.setText(combinedFile.getPath());
							retrieveMenuItem.setEnabled(true);
			
						}
						else
						{
							Toolkit.getDefaultToolkit().beep();
							Object[] obj={"Ok"};
							int answer = JOptionPane.showOptionDialog
							(
       						SwingUtilities.getWindowAncestor(this),
       						"Insufficient Disk Space to Store the file",
		   					"Disk Space Error",
           					JOptionPane.OK_OPTION,
           					JOptionPane.ERROR_MESSAGE,
           					null,
           					obj,
           					obj[0]
							);		
						}
					}
				}
				catch(Exception e)
				{
					Toolkit.getDefaultToolkit().beep();
					Object[] obj={"Ok"};
					int answer = JOptionPane.showOptionDialog
					(
       				SwingUtilities.getWindowAncestor(this),
       				"Broker Is not ready,Error",
		   			"Broker Error",
           			JOptionPane.OK_OPTION,
           			JOptionPane.ERROR_MESSAGE,
           			null,
           			obj,
           			obj[0]
					);
					e.printStackTrace();
				}
				createTempFolderToStore();
			}
			
			
		}
			
	}
	private void createTempFolderToStore()
	{
		Random random = new Random();
		tempFolderName=combinedFile.getParent()+"Temp";
		tempFolderName.replace(' ','_');
		tempFolderName+=random.nextInt();
		tempFolder = new File(tempFolderName);
		tempFolder.mkdir();
	}
	public String getTempFolderName()
	{
		return tempFolderName;
	}
    public void getData(int max)
	{
		
		SubmitRetrievelJobsThread th = new SubmitRetrievelJobsThread
		(retrievelTotalJobs,max,extension,address,sAddress,this);
		th.start();
	
	}
	
	private void writeFiles()
	{
		FileOutputStream out=null;
		FileInputStream in=null;
		try
		{
			out = new FileOutputStream(combinedFile);
		}
		catch(IOException ioe)
		{}
		
		int size=retrievelTotalJobs.size();
		ProgressBar bar = new ProgressBar(size,"Writing File ");
		Rectangle rect = getBounds();
	   	bar.showProgressBar(new Point(rect.width,rect.height));
		
		for(int i=0;i<size;i++)
		{
			try
			{
				String file = tempFolderName+"/"+retJobId+"_"+i+"."+extension;
				File inFile = new File(file);
				in = new FileInputStream(inFile);
				byte[] buf = new byte[in.available()];
				in.read(buf);
				out.write(buf);
				in.close();
				inFile.delete();
				buf=null;
				bar.setValue(i+1);
				Thread.sleep(200);
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			catch(InterruptedException ie)
			{
			}
			
		}
		try
		{
		
			
			out.flush();
			out.close();
			bar.closeProgress();
			tempFolder.delete();
			
			
		
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	synchronized public void incrementBytes()
	{
		bytesSent+=maxBytes;
	}
	synchronized public  void decrementBytes()
	{
		bytesSent-=maxBytes;
	}
	synchronized long getBytesSent()
	{
		return bytesSent;
	}
	public void actionPerformed(ActionEvent e)
    {
	   
	   if ("SelectFile".equals(e.getActionCommand()))
	   {
	   		
			if (fc==null)
			{
				fc = new JFileChooser();
			}
			fc.setAcceptAllFileFilterUsed(true);
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				storeFile=fc.getSelectedFile();
				filePathText.setText(storeFile.getPath());
				fileSize=storeFile.length();
				fileSizeText.setText(""+fileSize+" (Bytes)");
				fileName=storeFile.getName();
				fileName.replace('\\','/');
				if(fileSize<=5*ONEMB)
				{
					Toolkit.getDefaultToolkit().beep();
					Object[] obj={"Ok"};
					int answer = JOptionPane.showOptionDialog
					(
        	   			SwingUtilities.getWindowAncestor(this),
           	   			"File size must be greater than 5 MB..",
			   			"Small File!",
            			JOptionPane.OK_OPTION,
            			JOptionPane.INFORMATION_MESSAGE,
            			null,
            			obj,
            			obj[0]
					);
					
				}
				else
				{
					
					storeMenuItem.setEnabled(true);
				}
				
			}
						
	   }
	   else if ("Store".equals(e.getActionCommand()))
	   {
	   		
	   		if(storeMenuItem.isEnabled())
	   		{
	   			storeMenuItem.setEnabled(false);
	   			int rows = storeJobsModel.getRowCount();
				for(int i=rows-1;i>=0;i--)
				{
					storeJobsModel.removeRow(i);
				}
				jobCount=0;
				browseSelect.setEnabled(false);
				storeFile();
	   		}
	   		
	   			
	   }
	   else if ("Pending".equals(e.getActionCommand()))
	   {
	   		if(tabs.getSelectedIndex()==0)
	   		{
	   			processPendingStorageJobs();
	   			Rectangle rect = getBounds();
				storageBar = new ProgressBar(totalJobs," Storing Pending Jobs ");
				storageBar.showProgressBar(new Point(rect.width,rect.height));
				storageBar.setValue(0);
	   			storagePendingJobs.clear();
				pendingMenuItem.setEnabled(false);
				browseSelect.setEnabled(false);
			}
			else
			{
				Rectangle rect = getBounds();
				retrieveBar = new ProgressBar(retrievelTotalJobs.size()," Retrieving Pending Jobs ");
				retrieveBar.showProgressBar(new Point(rect.width,rect.height));
				retrieveBar.setValue(0);
				processPendingretrievelJobs(retrievelMaxBytes);
				retrievelPendingJobs.clear();
				pendingMenuItem.setEnabled(false);
				browseSave.setEnabled(false);
			}
	   		
	   }
	   else if ("Get".equals(e.getActionCommand()))
	   {
	   		populateDataJobsTable();
	   }
	   else if ("Retrieve".equals(e.getActionCommand()))
	   {
	   		int rows = retrievelJobsModel.getRowCount();
			for(int i=rows-1;i>=0;i--)
			{
				retrievelJobsModel.removeRow(i);
			}
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException ie)
			{}
			retrievelJobCount=0;
			browseSave.setEnabled(false);
			dataJobsTable.setEnabled(false);
			retrieveMenuItem.setEnabled(false);
			Rectangle rect = getBounds();
			retrieveBar = new ProgressBar(retrievelTotalJobs.size()," Retrieving File ");
			retrieveBar.showProgressBar(new Point(rect.width,rect.height));
			retrieveBar.setValue(0);
			try
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ie)
			{}
			getData(retrievelMaxBytes);
	   }
	   else if ("SaveAs".equals(e.getActionCommand()))
	   {
	   	    
	   	    getFileToStore();	
	   }
	   
	}
	
    private void processPendingStorageJobs()
    {
    	long from=0;
		long to=0;
		Iterator it = storagePendingJobs.iterator();
		while(it.hasNext())
		{
			DataJob job = (DataJob)it.next();
			if(job.sequence<(totalJobs-1))
			{
				from = job.sequence*maxBytes;
				to   = from+maxBytes;	
				SplitThread th = new SplitThread
				(
				storeFile,
				job.dataJobId,
				job.sequence,
				from,
				to,
				maxBytes,
				this,
				sAddress,
				address,
				false
				);
				th.start();
				try
				{
					Thread.sleep(100);
				}
				catch(Exception e)
				{}
			}
			else
			{
				from = job.sequence*maxBytes;
			  	to   = fileSize;
			  	SplitThread th = new SplitThread
			 	(
			  	storeFile,
			  	job.dataJobId,
			    job.sequence,
			  	from,
			  	to,
			  	maxBytes,
			  	this,
			  	sAddress,
			  	address,
			  	false
			  	);
				th.start();		
				try
				{
					Thread.sleep(100);
				}
				catch(Exception e)
				{}
			 }
		}
		
    }
    private void processPendingretrievelJobs(int max)
    {
    	Iterator it = retrievelPendingJobs.iterator();
		while(it.hasNext())
		{
			DataJob job = (DataJob)it.next();
			ReaderThread th = new ReaderThread(this,sAddress,job," ",extension,false);
			th.start();
			try
			{
				Thread.sleep(200);
			}
			catch(Exception e)
			{}
		 }
		 
    }
	private JMenuBar createMenuBar() {
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Actions");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "File Options");
        menuBar.add(menu);
		
        //ImageIcon icon = createImageIcon("images/load1.gif");
        storeMenuItem = new JMenuItem("Store File");
		storeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        storeMenuItem.getAccessibleContext().setAccessibleDescription("Store");
		storeMenuItem.setMnemonic(KeyEvent.VK_S);
		storeMenuItem.setActionCommand("Store");
        storeMenuItem.addActionListener(this);
        storeMenuItem.setEnabled(false);
		menu.add(storeMenuItem);
        
       	
        retrieveMenuItem = new JMenuItem("Retrieve File");
		retrieveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        retrieveMenuItem.getAccessibleContext().setAccessibleDescription("Retrieve");
        retrieveMenuItem.setEnabled(false);
        retrieveMenuItem.setMnemonic(KeyEvent.VK_R);
        retrieveMenuItem.setActionCommand("Retrieve");
        retrieveMenuItem.addActionListener(this);
        menu.add(retrieveMenuItem);

		pendingMenuItem = new JMenuItem("Precess Pending");
		pendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        pendingMenuItem.getAccessibleContext().setAccessibleDescription("Process Pending");
        pendingMenuItem.setEnabled(false);
        pendingMenuItem.setMnemonic(KeyEvent.VK_P);
        pendingMenuItem.setActionCommand("Pending");
        pendingMenuItem.addActionListener(this);
        menu.add(pendingMenuItem);
        pendingMenuItem.setEnabled(false);

        menuBar.add(menu);
		
       
	    return menuBar;
    }
	private void addButtons(JToolBar toolBar) {
        JButton loadButton = null;

		loadButton = makeNavigationButton("images/load.gif", "Load",
                                      "Load Configuration File",
                                      "Load");
        toolBar.add(loadButton);
		
              
    }
    private JButton makeNavigationButton(String imageName,
                                           String actionCommand,
                                           String toolTipText,
                                           String altText) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        if (imageName != null) {                      //image found
            button.setIcon(new ImageIcon(imageName, altText));
        } else {                                     //no image found
            button.setText(altText);
            System.err.println("Resource not found: "
                               + imageName);
        }

        return button;
    }
    private  ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SplitFile.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    private void createAndShowGUI() {
        
       	JFrame.setDefaultLookAndFeelDecorated(true);

        JFrame frame = new JFrame("File Sharing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        this.setOpaque(true); //content panes must be opaque
		frame.setJMenuBar(this.createMenuBar());
        frame.setContentPane(this);
		Dimension dim = this.getToolkit().getScreenSize();
		frame.setLocation((dim.width-700)/2,(dim.height-500)/2);
		frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }
	public static void main(String[] args) {
		if(args.length!=3)
		{
			System.out.println("Proper Use: SplitFile [Schedular Address] [Broker Address] [UserName]");
			System.exit(0);
			
		}
		final SplitFile splitFile = new SplitFile();
		splitFile.sAddress=args[0];
		username=args[2];
		splitFile.brokerAddress="rmi://"+args[1]+"/brokerserver";
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                splitFile.createAndShowGUI();
            }
        });
    }

}