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

public class InitialValue extends JPanel implements ActionListener,ChangeListener,ListSelectionListener
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
	public InitialValue()
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
        titled = BorderFactory.createTitledBorder("Options"); 
        filePathText = new JTextField("",15);
        fileSizeText = new JTextField("",15);
        browseSelect = new JButton("Add");
        browseSelect.setActionCommand("Add");
        browseSelect.addActionListener(this);
        filePanel.setBorder(titled);
        filePanel.add(new JLabel("H Value:"));
        filePanel.add(filePathText);
        filePanel.add(new JLabel("K Value:"));
        filePanel.add(fileSizeText);
        filePanel.add(new JLabel(""));
        filePanel.add(browseSelect);
        SpringUtilities.makeCompactGrid(filePanel,
                                        3, 2, //rows, cols
                                        10, 20,        //initX, initY
                                        20, 40);       //xPad, yPad
		
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new SpringLayout());
        titled = BorderFactory.createTitledBorder("Values"); 
        listPanel.setBorder(titled);
        JList list1 = new JList();
        JList list2 = new JList();
        JScrollPane sp1 = new JScrollPane(list1);
        JScrollPane sp2 = new JScrollPane(list2);
        listPanel.add(sp1);
        listPanel.add(sp2);
        SpringUtilities.makeCompactGrid(listPanel,
                                        1, 2, //rows, cols
                                        10, 10,        //initX, initY
                                        10, 20);       //xPad, yPad
        
        JPanel holder = new JPanel();
        holder.setLayout(new SpringLayout());
        holder.add(filePanel);
        holder.add(listPanel);
		SpringUtilities.makeCompactGrid(holder,
                                        1, 2, //rows, cols
                                        10, 10,        //initX, initY
                                        20, 20);       //xPad, yPad
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(holder);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(700,500));
        splitPane.setBottomComponent(tabs);
        splitPane.setDividerLocation(280);
        add(splitPane);
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
	
	public void valueChanged(ListSelectionEvent e) 
	{
         
                             
    }
    
	public void actionPerformed(ActionEvent e)
    {
	   
	   
	   
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
		/*if(args.length!=2)
		{
			System.out.println("Proper Use: SplitFile Schedular Address Broker Address");
			System.exit(0);
			
		}
		*/
		final InitialValue splitFile = new InitialValue();
		//splitFile.sAddress=args[0];
		//splitFile.brokerAddress="rmi://"+args[1]+"/brokerserver";
		
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                splitFile.createAndShowGUI();
            }
        });
    }

}