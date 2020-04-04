import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.io.*;
import java.awt.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.sql.*;
import java.rmi.*;
public class SchedularUI extends JPanel
                      implements ActionListener,WindowListener{
	private JTree tree;
	private JTable table;
	private JButton startButton,stopButton,loadButton,exitButton,logButton,showLogButton,clearButton;
	private JMenuItem loadItem;
	private DefaultTableModel model;
    private JPanel panelj;
    private LinkedList config= new LinkedList();
	private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;
    private JMenuItem startItem,stopItem,logItem,showLogItem,clearItem;
    private DefaultMutableTreeNode top;
	private JLabel picture;
	static final private String START 	  = "Start";
    static final private String STOP  	  = "Stop";
    static final private String LOAD  	  = "Load";
	static final private String EXIT  	  = "Exit";
	static final private String SETTINGS  = "Settings";
	static final private String CONFIG 	  = "Configuration";
	static final private String LOG		  =  "Log";
	static final private String SHOWLOG	  =  "ShowLog";
	static final private String CLEAR	  =  "Clear";
	private JSplitPane splitPane;
	private JScrollPane treeView;
	private boolean isLoaded=false,isSettings=false;
	private JFileChooser fc;
	private File configFile;
	private ImageIcon leafIcon,openIcon,closeIcon;
	private DefaultTreeCellRenderer renderer;
	private SchedularSettings settings;
	private SchedularConfiguration configuration;
	private Hashtable infoTable = new Hashtable();
	private Schedular server;
	private boolean isStarted,isCleared;
	private String brokerAddress;
	private static int infoNumber=0;
	private static String securityServer;
	public SchedularUI() {
	
        {	
        	try
    		{
    			String secURL = "rmi://"+securityServer+"/SecurityManager";
        		SecurityManagerInterface security = (SecurityManagerInterface)Naming.lookup(secURL);
        		if (security != null)
        		{
        			//Remove the hardcoded username and password with command line arguments
        			boolean result = security.authenticate("Administrator","unknown");
        			if(!result)
        			{
        				System.out.println("You are not authorized to Start this Application");
        				System.exit(0);
        			}
        		}
        	}
        	catch(RemoteException re)
        	{
        		System.out.println("Error Connecting to Security Manager");
        		System.exit(0);
        	}
        	catch (NotBoundException bne)
        	{
        		System.out.println("Error Connecting to Security Manager");
        		System.exit(0);
        	}
        	catch (java.net.MalformedURLException mfue)
        	{
        		System.out.println("Malformed URL: Check Security Manager's Address");
        		System.exit(0);
        	}
		
			
		}
		
        
        brokerAddress=null;
		isCleared=false;
		isStarted=false;
		setLayout(new BorderLayout());
        GridLayout topLayout = new GridLayout(0,1);
		fc=null;
		configFile=null;
		setPreferredSize(new Dimension(800,400));
		JToolBar toolBar = new JToolBar("Schedular");
        addButtons(toolBar);
        toolBar.setPreferredSize(new Dimension(450, 45));
        
        top = new DefaultMutableTreeNode("Configurations");
		tree = new JTree(top);
		isStarted=false;
		leafIcon = createImageIcon("images/flying-v.gif");
		openIcon = createImageIcon("images/down.gif");
		closeIcon = createImageIcon("images/up.gif");
				
        if (leafIcon != null && openIcon!=null && closeIcon != null) {
            renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(leafIcon);
			renderer.setOpenIcon(openIcon);
			renderer.setClosedIcon(closeIcon);
			tree.setCellRenderer(renderer);
        } else {
            System.err.println("Icons missing; using default.");
        }
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                    
        treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(800,250));
        
		model = new DefaultTableModel();
		model.addColumn("ID");
		model.addColumn("Source");
		model.addColumn("Destination");
		model.addColumn("Submission");
		model.addColumn("Start");
		model.addColumn("Finish");
		model.addColumn("Status");
		model.addColumn("Type");
		model.addColumn("Handle");
		model.addColumn("Delivery");
		table = new JTable(model);
		table.setCellSelectionEnabled(false);
		
		TableColumn col = table.getColumnModel().getColumn(0);
		col.setPreferredWidth(50);
		col = table.getColumnModel().getColumn(1);
		col.setPreferredWidth(100);
		col = table.getColumnModel().getColumn(2);
		col.setPreferredWidth(100);
		col = table.getColumnModel().getColumn(3);
		col.setPreferredWidth(100);
		col = table.getColumnModel().getColumn(4);
		col.setPreferredWidth(100);
		col = table.getColumnModel().getColumn(5);
		col.setPreferredWidth(100);
		col = table.getColumnModel().getColumn(9);
		col.setPreferredWidth(100);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setEnabled(false);			
		JScrollPane tableView = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		
		           
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        Dimension minimumSize = new Dimension(200, 100);
       	splitPane.setDividerLocation(200); 
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(tableView);
	    splitPane.setPreferredSize(new Dimension(800, 350));
       	add(toolBar,BorderLayout.PAGE_START);
		add(splitPane,BorderLayout.PAGE_END);
		try
		{
			server = new Schedular();
		}
		catch(Exception e)
		{
		}
		
		
    }
    private void createNodes(DefaultMutableTreeNode top) {
    DefaultMutableTreeNode category = null;
	DefaultMutableTreeNode conf = null;
    String prevService=" ";
	int tot = config.size();
	HashSet addresses = new HashSet();
	Hashtable services = new Hashtable();
	for(int i=0;i<tot;i++)
	{
		SchedularConfig cnf = (SchedularConfig)config.get(i);
		addresses.add(cnf.address);
		services.put(cnf.name,new DefaultMutableTreeNode(cnf.name));
	}
	Iterator it = addresses.iterator();
	
	while(it.hasNext())
	{
		String add = (String)it.next();
		category = new DefaultMutableTreeNode(add);
		//category.setParent(null);
		top.add(category);
		Enumeration keys = services.keys();
		while(keys.hasMoreElements())
		{
			String key  = (String)keys.nextElement();
			if(findService(add,key))
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(services.get(key));
				category.add(node);
				for(int i=0;i<tot;i++)
				{
					SchedularConfig cnf = (SchedularConfig)config.get(i);
					if( cnf.name.equals(key) && cnf.address.equals(add) )
					{
				  	   node.add(new DefaultMutableTreeNode("Handle: "+cnf.handle));
					   node.add(new DefaultMutableTreeNode("Type: "+cnf.type));
					}
				}
			}
		}
			
	}
 }
    
       
	private  ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SchedularUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
    
    public void getConfiguration(File file)
	{
		ObjectInputStream in=null;
		SchedularConfig conf=null;
		config.clear();
		try
        {
			FileInputStream fis  = new  FileInputStream(file);
	 		in =  new ObjectInputStream(fis);
			String str;
			while (true)
			{
				conf = (SchedularConfig)in.readObject();
				if (conf instanceof SchedularConfig)
				{
					config.add(conf);
				}
			}
                	
        }
		catch(IOException e)
		{
			try
			{
				in.close();
			}
			catch(IOException ioe)
			{
			}
		}
		catch(Exception e)
		{
				
		}		           

	}
	private  boolean findService(String address,String service)
	{
		for(int i=0;i<config.size();i++)
		{
			SchedularConfig conf = (SchedularConfig)config.get(i);
			if(conf.address.equals(address)&&conf.name.equals(service))
			return true;
		}
		return false;
	}
	public void actionPerformed(ActionEvent e)
    {
	   
	   if (LOAD.equals(e.getActionCommand()))
	   {
	   		
			if (fc==null)
			{
				fc = new JFileChooser();
			}
			fc.addChoosableFileFilter(new SchedularFilter());
            fc.setAcceptAllFileFilterUsed(false);
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				
				configFile = fc.getSelectedFile();
				config.clear();
				getConfiguration(configFile);
				if (isLoaded)
				{
					
					top = new DefaultMutableTreeNode("Configurations");
					tree = new JTree(top);
					if (leafIcon != null && openIcon!=null && closeIcon != null) 
					{
            			renderer = new DefaultTreeCellRenderer();
            			renderer.setLeafIcon(leafIcon);
						renderer.setOpenIcon(openIcon);
						renderer.setClosedIcon(closeIcon);
						tree.setCellRenderer(renderer);
        			} 
					treeView = new JScrollPane(tree);
					treeView.setPreferredSize(new Dimension(800,300));
					splitPane.setDividerLocation(200);
					splitPane.setTopComponent(treeView);
					
									
				}
				createNodes(top);
				isLoaded=true;
				if (isLoaded && isSettings)
				{
					if (!startItem.isEnabled())
					{
						startItem.setEnabled(true);
						startButton.setEnabled(true);
					}
					if (isStarted)
					{
						
						startItem.setEnabled(false);
						startButton.setEnabled(false);
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Yes","No"};
						int answer = JOptionPane.showOptionDialog
						(
                		SwingUtilities.getWindowAncestor(this),
           	   			"Do you want change the Schedular Configurations?",
			   			"Change Settings?",
               			JOptionPane.YES_NO_OPTION,
               			JOptionPane.QUESTION_MESSAGE,
            		    null,
            			obj,
            			obj[0]
						);
						if (answer == JOptionPane.YES_OPTION)
						{
								server.setConfig(config);
						}
						
					  }
			     }
			    
		    }
			
				
	   }
	   else if (START.equals(e.getActionCommand()))
	   {
	   		try
	   		{
				server.setConfig(config);
				server.startSchedular(this);
				isStarted=true;
				startButton.setEnabled(false);
				startItem.setEnabled(false);
				stopItem.setEnabled(true);
				stopButton.setEnabled(true);
				
	   		}
			catch(Exception re)
			{
			
			}
	   }
	   else if (STOP.equals(e.getActionCommand()))
	   {	   		
			startButton.setEnabled(true);
			startItem.setEnabled(true);
			stopItem.setEnabled(false);
			stopButton.setEnabled(false);
			server.stopScheduler();
	   }
	   else if (CONFIG.equals(e.getActionCommand()))
	   {
            Point point = getLocation();
			configuration = new SchedularConfiguration();
			configuration.showConfig(point);
	   }
	   else if (SETTINGS.equals(e.getActionCommand()))
	   {
	   		Settings set = new Settings();
	   		set = server.getSettings();
	   		settings = new SchedularSettings(this,set.broker,set.security,set.mem,set.cpu,set.disk,set.wait,set.elapsed,set.man);
			Point point = getLocation();
			settings.showSettings(point);
	   }
	   
	   else if(LOG.equals(e.getActionCommand()))
	   {
	   		
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Yes","No"};
			int answer = JOptionPane.showOptionDialog
			(
            	SwingUtilities.getWindowAncestor(this),
           	   "Do you really want to write log file ?",
			   "Write Log?",
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.QUESTION_MESSAGE,
            	null,
            	obj,
            	obj[0]
			);
			if (answer == JOptionPane.YES_OPTION)
			{
				
				try
				{
					if (fc==null)
					{
						fc = new JFileChooser();
					}
					fc.addChoosableFileFilter(new LogFilter());
            		fc.setAcceptAllFileFilterUsed(false);
					int returnVal = fc.showSaveDialog(this);
           			if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						
						if (!(model.getRowCount()>0))
						{
							logButton.setEnabled(false);
							logItem.setEnabled(false);
						}
						Vector data = model.getDataVector();
						File outFile = fc.getSelectedFile();
						ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
						int rows = data.size();
						Iterator it = data.iterator();
						while (it.hasNext())
						{
							out.writeObject(it.next());
						}
						out.flush();
					}
					
				}
				catch(IOException ioe)
				{
				}
				
			}
	   }
	   else if(SHOWLOG.equals(e.getActionCommand()))
	   {
	   		 Point point = getLocation();
			 ShowLog log = new ShowLog();
			 log.showConfig(point);
	   }
	   else if (EXIT.equals(e.getActionCommand()))
	   {
	   		
			processExit();
	   }
	   else
	   {
	   		Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Yes","No"};
			int answer = JOptionPane.showOptionDialog
			(
            	SwingUtilities.getWindowAncestor(this),
           	   "Do you really want to clear log?",
			   "Clear Log?",
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.QUESTION_MESSAGE,
            	null,
            	obj,
            	obj[0]
			);
			if (answer == JOptionPane.YES_OPTION)
			{
				int rows = model.getRowCount();
				for(int i=rows-1;i>=0;i--)
				{
					model.removeRow(i);
				}
				infoNumber=0;
				clearButton.setEnabled(false);
				clearItem.setEnabled(false);
				logItem.setEnabled(false);
				logButton.setEnabled(false);
			}
			
	   }

    }
	private JMenuBar createMenuBar() {
        
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription(
                "File Options");
        menuBar.add(menu);

        ImageIcon icon = createImageIcon("images/load1.gif");
        loadItem = new JMenuItem("Load Configuration File", icon);
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        loadItem.getAccessibleContext().setAccessibleDescription("Load");
		loadItem.setMnemonic(KeyEvent.VK_L);
		loadItem.setMnemonic(KeyEvent.VK_L);
		loadItem.setActionCommand(LOAD);
        loadItem.addActionListener(this);
		menu.add(loadItem);
        menu.addSeparator();
		
       	icon = createImageIcon("images/start1.gif");
        startItem = new JMenuItem("Start",icon);
		startItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        startItem.getAccessibleContext().setAccessibleDescription("Start");
        startItem.setEnabled(false);
        startItem.setMnemonic(KeyEvent.VK_S);
        startItem.setActionCommand(START);
        startItem.addActionListener(this);
        menu.add(startItem);

        icon = createImageIcon("images/stop1.gif");
		stopItem = new JMenuItem("Stop",icon);
		stopItem.setEnabled(false);
		stopItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        stopItem.getAccessibleContext().setAccessibleDescription("Stop");
        stopItem.setMnemonic(KeyEvent.VK_T);
        stopItem.setActionCommand(STOP);
        stopItem.addActionListener(this);
        menu.add(stopItem);
     
        menu.addSeparator();
	
        icon = createImageIcon("images/exit1.gif");
		menuItem = new JMenuItem("Exit",icon);
        menuItem.setMnemonic(KeyEvent.VK_X);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Start");
		menuItem.setActionCommand(EXIT);
        menuItem.addActionListener(this);
	    menu.add(menuItem);
       
        menu = new JMenu("Management");
        menu.setMnemonic(KeyEvent.VK_M);
		
        menu.getAccessibleContext().setAccessibleDescription(
                "Management");
		icon = createImageIcon("images/settings1.gif");
		menuItem = new JMenuItem("Settings",icon);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(SETTINGS);
        menuItem.setMnemonic(KeyEvent.VK_N);
		menuItem.setActionCommand(SETTINGS);
        menuItem.addActionListener(this);
        menu.add(menuItem);
       	
		icon = createImageIcon("images/table1.gif");
		menuItem = new JMenuItem("Configurations",icon);
        menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(CONFIG);
		menuItem.setActionCommand(CONFIG);
        menuItem.addActionListener(this);
        menu.add(menuItem);
		
		menu.addSeparator();
		
		icon = createImageIcon("images/log1.gif");
		logItem = new JMenuItem("Write Log",icon);
		logItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        logItem.getAccessibleContext().setAccessibleDescription(LOG);
        logItem.setMnemonic(KeyEvent.VK_G);
		logItem.setActionCommand(LOG);
        logItem.addActionListener(this);
		logItem.setEnabled(false);
        menu.add(logItem);
		
		icon = createImageIcon("images/showlog1.gif");
		showLogItem = new JMenuItem("Open Log",icon);
		showLogItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        showLogItem.getAccessibleContext().setAccessibleDescription(SHOWLOG);
        showLogItem.setMnemonic(KeyEvent.VK_O);
		showLogItem.setActionCommand(SHOWLOG);
        showLogItem.addActionListener(this);
		menu.add(showLogItem);
		
		
		icon = createImageIcon("images/clear1.gif");
		clearItem = new JMenuItem("Clear Log",icon);
		clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        clearItem.getAccessibleContext().setAccessibleDescription(CLEAR);
        clearItem.setMnemonic(KeyEvent.VK_E);
		clearItem.setActionCommand(CLEAR);
		clearItem.setEnabled(false);
        clearItem.addActionListener(this);
		menu.add(clearItem);
		
				
        menuBar.add(menu);
		
       
	    return menuBar;
    }
	private void addButtons(JToolBar toolBar) {
        JButton button = null;

		loadButton = makeNavigationButton("/images/load.gif", LOAD,
                                      "Load Configuration File",
                                      LOAD);
        toolBar.add(loadButton);
		
        startButton = makeNavigationButton("images/start.gif", START,
                                      "Start Schedular",
                                      START);
		startButton.setEnabled(false);	  
        toolBar.add(startButton);

      
        stopButton = makeNavigationButton("images/stop.gif", STOP,
                                      "Stop Schedular",
                                      STOP);
		stopButton.setEnabled(false);	  
        toolBar.add(stopButton);

      	button = makeNavigationButton("images/settings.gif", SETTINGS,
                                      "Schedular Settings",
                                      SETTINGS);
        toolBar.add(button);
		
		button = makeNavigationButton("images/table.gif", CONFIG,
                                      "Edit Configurations",
                                      CONFIG);
								
		toolBar.add(button);
		
		logButton = makeNavigationButton("images/log.gif", LOG,
                                      "Write Log File",
                                      LOG);
		logButton.setEnabled(false);	  
								
		toolBar.add(logButton);
		
		showLogButton = makeNavigationButton("images/showlog.gif", SHOWLOG,
                                      "Open Log File",
                                      SHOWLOG);
								
		toolBar.add(showLogButton);
		
		clearButton = makeNavigationButton("images/clear.gif", CLEAR,
                                      "Clear Log File",
                                      CLEAR);
		
		clearButton.setEnabled(false);	  
								
		toolBar.add(clearButton);
		
		
		toolBar.add(new JSeparator(JSeparator.VERTICAL));
		
		button = makeNavigationButton("images/exit.gif", EXIT,
                                      "Exit",
                                      EXIT);
        toolBar.add(button);
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
            button.setIcon(createImageIcon(imageName));
        } else {                                     //no image found
            button.setText(altText);
            System.err.println("Resource not found: "
                               + imageName);
        }

        return button;
    }
	public void setSettings(String saddress,String sec,int imem,int icpu,int idisk,int iwait,int ielp,int iman)
	{
		isSettings=true;
		if (isSettings&&isLoaded)
		{
			if (!startItem.isEnabled()&&!isStarted)
			{
				startItem.setEnabled(true);
				startButton.setEnabled(true);
			}
			
		}
		if (isStarted)
		{
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Yes","No"};
			int answer = JOptionPane.showOptionDialog
			(
                SwingUtilities.getWindowAncestor(this),
           	   "Do you want change the Schedular Seetings?",
			   "Change Settings?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
            	null,
            	obj,
            	obj[0]
			);
			if (answer == JOptionPane.YES_OPTION)
			{
				server.setSettings
				(
					saddress,
					sec,
					imem,
					icpu,
					idisk,
					iwait,
					ielp,
					iman
				);
			}
			else
				return;
		}
		server.setSettings
		(
			saddress,
			sec,
			imem,
			icpu,
			idisk,
			iwait,
			ielp,
			iman
		);

		
	}
    public void addInfo(JobInformation info)
    {
		infoTable.put(" "+info.jobId,new Integer(infoNumber++));
		Object[] obj = 
		{" "+info.jobId,info.source,info.destination,info.submitDate,info.startDate,info.finishDate,info.status,info.type,info.handle,info.deliveryDate};
		model.addRow(obj);
		if (!logButton.isEnabled())
		{
			logButton.setEnabled(true);
			logItem.setEnabled(true);
			
		}
		if (!clearButton.isEnabled())
		{
			clearButton.setEnabled(true);
			clearItem.setEnabled(true);
		}
    }
	public void updateInfo(JobInformation info)
	{
		if (infoTable.containsKey(" "+info.jobId))
		{
			int rowid = ((Integer)infoTable.get(" "+info.jobId)).intValue();
			Object[] obj = 
			{" "+info.jobId,info.source,info.destination,info.submitDate,info.startDate,info.finishDate,info.status,info.type,info.handle,info.deliveryDate};
			for(int i=0;i<obj.length;i++)
			{
				model.setValueAt(obj[i],rowid,i);
			}
			
		}
	}
	
	public void windowClosing(WindowEvent e) 
	{
    	
		processExit();
    }
	
	public void windowClosed(WindowEvent e) {
        
    }

    public void windowOpened(WindowEvent e) {
        
    }

    public void windowIconified(WindowEvent e) {
        
    }

    public void windowDeiconified(WindowEvent e) {
       
    }

    public void windowActivated(WindowEvent e) {
        
    }

    public void windowDeactivated(WindowEvent e) {
        
    }
    public void processExit()
    {
		if (stopButton.isEnabled())
			{
				Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Yes","No"};
				int answer = JOptionPane.showOptionDialog
				(
            		SwingUtilities.getWindowAncestor(this),
           	   		"Schedular is still running\n Do you really want to exit?",
			   		"Exit?",
            		JOptionPane.YES_NO_OPTION,
            		JOptionPane.QUESTION_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				if (answer == JOptionPane.NO_OPTION)
				{
			
					return;
				}
			}
			System.exit(0);
    }
	private static void createAndShowGUI() {
        
       	JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SchedularUI newContentPane = new SchedularUI();
        newContentPane.setOpaque(true); //content panes must be opaque
		frame.setJMenuBar(newContentPane.createMenuBar());
        frame.setContentPane(newContentPane);
		Dimension dim = newContentPane.getToolkit().getScreenSize();
		frame.setLocation(dim.width-dim.width+100,dim.height-dim.height+100);
		frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }
	public static void main(String[] args) {
		if(args.length!=1)
		{
			System.out.println("Proper Use: java SchedularUI Security Manager's Machines Address");
			System.exit(0);
		}
		securityServer=args[0];
		
		 SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
