import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.border.*;
import javax.swing.table.*;

class GetBrokerInformation extends Thread
{
	private SystemInformation info;
	private BrokerUI ui;
	private String brokerAddress;
	private BrokerServerInterface in;
	public static String machine;
	
	public GetBrokerInformation(BrokerUI bui,String address)
	{
		ui = bui;
		brokerAddress=address;
	}
	public void run()
	{
		while (BrokerUI.runIt)
		{
			try
			{
				
				sleep(1000);
				in = (BrokerServerInterface)Naming.lookup(brokerAddress);
				info = in.getSystemInfo(machine);
				ui.setInfo(info);
				
			}
			catch(RemoteException re)
			{
				re.printStackTrace();
			}
			catch(NotBoundException nbe)
			{
				nbe.printStackTrace();
			}
			catch(java.net.MalformedURLException mue)
			{
				mue.printStackTrace();
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
	}
}
public class BrokerUI extends JPanel implements ActionListener,WindowListener,ChangeListener{
	
	public static boolean runIt=false;
	private static JMenuBar menuBar;
	private JMenu menu,sub,memMenu,CPUMenu,vMemMenu;
    private JMenuItem selItem,exitItem,usageItem;
	private JButton selButton,exitButton,diskButton;
	static final private String SELECT  = "Sel";
	static final private String EXIT  	= "Exit";
	private JTextArea processInformation;
	private JTextArea detailInformation;
    private String address;
    private SelectMachine selectMachine;
	private JTabbedPane tabbedPane;
	private String brokerAddress;
	private JTable processTable;
	private DefaultTableModel infoModel;
	private JTable infoTable;
	private GetBrokerInformation infoThread;
	private JProgressBar memBar,cpuBar,diskBar,virtualBar;
	private MyTableModel model;
	private int selection;
	private int charts;
	private DynamicGraph memGraph,CPUGraph,vMemGraph;
	private JPanel memGraphPanel,CPUGraphPanel,vMemGraphPanel;
	private JPanel graphHolder;
	private int mOrientation,mDirection,mType;
	private int cOrientation,cDirection,cType;
	private Hashtable frames;
	private static long frameCount;
	private int graphSelected;
	private JFrame frame;
	public BrokerUI() {
	
		frameCount=0;
		graphSelected=0;
		frames = new Hashtable();
		mOrientation=DynamicGraph.BOTTOM_TOP;
		mDirection=DynamicGraph.RIGHT_LEFT;
		mType=DynamicGraph.LINE;
		cOrientation=DynamicGraph.BOTTOM_TOP;
		cDirection=DynamicGraph.RIGHT_LEFT;
		cType=DynamicGraph.LINE;
        
        selection=0;
        charts=0;
        //brokerAddress="rmi://192.168.0.1/brokerserver";
        setLayout(new BorderLayout());
		address=null;	
		Dimension pos = getToolkit().getScreenSize();
		setPreferredSize(new Dimension(550,570));
		setMinimumSize(new Dimension(550,590));
		JToolBar toolBar = new JToolBar("Broker");
        addButtons(toolBar);
        toolBar.setPreferredSize(new Dimension(450,45));
       	add(toolBar,BorderLayout.PAGE_START);  
       	tabbedPane = new JTabbedPane();
        
        memBar = new JProgressBar(0,100);
        cpuBar = new JProgressBar(0,100);
        diskBar = new JProgressBar(0,100);
        virtualBar = new JProgressBar(0,100);
        memBar.setStringPainted(true);
        cpuBar.setStringPainted(true);
        diskBar.setStringPainted(true);
        virtualBar.setStringPainted(true);
        
        graphHolder = new JPanel();
        graphHolder.setLayout(new GridLayout(3,1));
       	
       	TitledBorder titled;
        titled = BorderFactory.createTitledBorder("Memory Usage History"); 
        memGraphPanel = new JPanel();
        memGraphPanel.setLayout(new BorderLayout());
        memGraph = new DynamicGraph(DynamicGraph.LINE,100,DynamicGraph.BOTTOM_TOP,DynamicGraph.LEFT_RIGHT,75);
        memGraphPanel.add(memGraph);
        memGraphPanel.setBorder(titled);
        
        titled = BorderFactory.createTitledBorder("CPU Usage History"); 
        CPUGraphPanel = new JPanel();
        CPUGraphPanel.setLayout(new BorderLayout());
        CPUGraph = new DynamicGraph(DynamicGraph.LINE,100,DynamicGraph.BOTTOM_TOP,DynamicGraph.RIGHT_LEFT,75);
        CPUGraphPanel.add(CPUGraph);
        CPUGraphPanel.setBorder(titled);
        
        titled = BorderFactory.createTitledBorder("Virtual Memory Usage History"); 
        vMemGraphPanel = new JPanel();
        vMemGraphPanel.setLayout(new BorderLayout());
        vMemGraph = new DynamicGraph(DynamicGraph.LINE,100,DynamicGraph.BOTTOM_TOP,DynamicGraph.RIGHT_LEFT,75);
        vMemGraphPanel.add(vMemGraph);
        vMemGraphPanel.setBorder(titled);
        
        graphHolder.add(memGraphPanel);
        graphHolder.add(CPUGraphPanel);
        graphHolder.add(vMemGraphPanel);
        
        
        ImageIcon icon = createImageIcon("images/middle.gif");
        model  = new MyTableModel();      
        
        infoTable = new JTable(model);
        JScrollPane tablePane = new JScrollPane(infoTable);
        infoTable.setDefaultRenderer(JProgressBar.class,
                                 new ProgressBarRenderer(true));
                        
        infoTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        infoTable.setEnabled(false);
       
        titled = BorderFactory.createTitledBorder("Performance Details");
        tablePane.setBorder(titled);
        
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout());
        //panel3.add(memGraph);
        panel3.add(graphHolder,BorderLayout.CENTER);
        panel3.add(tablePane,BorderLayout.PAGE_END);
        tabbedPane.addTab("Performance", icon, panel3,
                          "Displays System Performance");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        
        
        processInformation = new JTextArea();
       	JScrollPane areaScrollPane1 = new JScrollPane(processInformation);
       	processInformation.setEditable(false);
       	
		areaScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane1.setPreferredSize(new Dimension(500, 500));
		
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(areaScrollPane1,BorderLayout.CENTER);
        tabbedPane.addTab("Processes", icon, panel1,
                          "Dislplays Processes Running on the System");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        

        detailInformation = new JTextArea();
		JScrollPane areaScrollPane2 = new JScrollPane(detailInformation);
		detailInformation.setEditable(false);
       	
		areaScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane2.setPreferredSize(new Dimension(500, 500));
		
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.add(areaScrollPane2);
        tabbedPane.addTab("Details", icon, panel2,
                          "Displays System's Details");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.setPreferredSize(new Dimension(500,500));
        tabbedPane.setEnabled(false);
	    add(tabbedPane,BorderLayout.CENTER);
        tabbedPane.addChangeListener(this);
        //memBar.setValue(50);
    }
	class MyTableModel extends AbstractTableModel {
        private String[] columnNames = 
        {
        	"Options",
            "Total",
            "Free",
            "Used",
            "%Used"
        };
       
        Object [][] data = 
        {
        	{"CPU","","","",cpuBar},
        	{"Memory","","","",memBar},
        	{"Swap","","","",virtualBar},
        	{"Disk","","","",diskBar}
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            
            data[row][col] = value;
            fireTableCellUpdated(row, col);

            
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }
    /** Returns an ImageIcon, or null if the path was invalid. */
    private ImageIcon createImageIcon(String path) {
        
        java.net.URL imgURL = BrokerUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if (SELECT.equals(e.getActionCommand()))
	   {
	   	 	
    		selectMachine = new SelectMachine(this,brokerAddress,1);
			Point point = getLocation();
			selectMachine.showSelect(point);
    						
	   }
	   else if("Disk".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selectMachine.showSelect(point);
			selection=1;
			charts=0;
				   		
	   }
	   else if("Virtual".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selectMachine.showSelect(point);
			selection=2;
			charts=0;
				   		
	   }
	   else if("Physical".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selectMachine.showSelect(point);
			selection=3;
			charts=0;
				   		
	   }
	   else if("CPU".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selectMachine.showSelect(point);
			selection=4;
			charts=0;
				   		
	   }
	   else if("RDisk".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selection=1;
			charts=1;
			selectMachine.showSelect(point);
			
	   }
	   else if("RVirtual".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selection=2;
			charts=1;
			selectMachine.showSelect(point);
			
	   }
	   else if("RPhysical".equals(e.getActionCommand()))
	   {
	   		 		
	   		selectMachine = new SelectMachine(this,brokerAddress,2);
			Point point = getLocation();
			selection=3;
			charts=1;
			selectMachine.showSelect(point);
			
	   }
	   else if("MBar".equals(e.getActionCommand()))
	   {
	   		mType=DynamicGraph.BAR;
	   			 		
	   }
	   else if("MLine".equals(e.getActionCommand()))
	   {
	   		mType=DynamicGraph.LINE;	 		
	   }
	   else if("MLevel".equals(e.getActionCommand()))
	   {
	   		mType=DynamicGraph.LEVEL;	 		
	   }
	   else if("CBar".equals(e.getActionCommand()))
	   {
	   		cType=DynamicGraph.BAR;	 		
	   }
	   else if("CLine".equals(e.getActionCommand()))
	   {
	   		cType=DynamicGraph.LINE;	 		
	   }
	   else if("CLevel".equals(e.getActionCommand()))
	   {
	   		cType=DynamicGraph.LEVEL;	 		
	   }
	   else if("MTop-Bottom".equals(e.getActionCommand()))
	   {
	   		mOrientation=DynamicGraph.TOP_BOTTOM;	 		
	   }
	   else if("MBottom-Top".equals(e.getActionCommand()))
	   {
	   		mOrientation=DynamicGraph.BOTTOM_TOP;	 		
	   }
	   else if("CTop-Bottom".equals(e.getActionCommand()))
	   {
	   		cOrientation=DynamicGraph.TOP_BOTTOM;	 		
	   }
	   else if("CBottom-Top".equals(e.getActionCommand()))
	   {
	   		cOrientation=DynamicGraph.BOTTOM_TOP;	 		
	   }
	   else if("MRight-Left".equals(e.getActionCommand()))
	   {
	   		mDirection=DynamicGraph.RIGHT_LEFT;	 		
	   }
	   else if("MLeft-Right".equals(e.getActionCommand()))
	   {
	   		mDirection=DynamicGraph.LEFT_RIGHT;	 		
	   }
	   else if("CRight-Left".equals(e.getActionCommand()))
	   {
	   		cDirection=DynamicGraph.RIGHT_LEFT;	 		
	   }
	   else if("CLeft-Right".equals(e.getActionCommand()))
	   {
	   		cDirection=DynamicGraph.LEFT_RIGHT;	 		
	   }
	   else if ("DynamicMemGraph".equals(e.getActionCommand())) 
	   {
	   		selectMachine = new SelectMachine(this,brokerAddress,2); //multiple chioces on
			Point point = getLocation();
			charts=2;
			selectMachine.showSelect(point);
			graphSelected=1;
	   } 
	   else if ("DynamicCpuGraph".equals(e.getActionCommand())) 
	   {
	   		selectMachine = new SelectMachine(this,brokerAddress,2); //multiple chioces on
			Point point = getLocation();
			charts=2;
			selectMachine.showSelect(point);
			graphSelected=2;
	   }
	   else if ("DynamicLevelGraph".equals(e.getActionCommand())) 
	   {
	   		selectMachine = new SelectMachine(this,brokerAddress,2); //multiple chioces on
			Point point = getLocation();
			charts=2;
			selectMachine.showSelect(point);
			graphSelected=3;//levels
	   }
	   else //(EXIT.equals(e.getActionCommand()))
	   {
	  		System.exit(0);
	   }
	  
	   
	   	   
    }
    public void windowClosing(WindowEvent e) {
    	
		if(frames.size()>0)
		{
			Enumeration enumeration = frames.keys();
			while(enumeration.hasMoreElements())
			{
				String address = (String)enumeration.nextElement();
				GraphFrame g = (GraphFrame)frames.get(address);
				g.closeFrame();
				
			}
		}		
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
	public void setInfo(SystemInformation info)
	{
		model.setValueAt("100",0,1);
		model.setValueAt((100-info.cpu)+" ",0,2);
		model.setValueAt(info.cpu+" ",0,3);
		
		
		model.setValueAt(info.physicalMem+" K ",1,1);
		model.setValueAt(info.freePhysicalMem+" K ",1,2);
		model.setValueAt(info.usedPhysicalMem+" K ",1,3);
		
		model.setValueAt(info.virtualMem+" K ",2,1);
		model.setValueAt(info.freeVirtualMem+" K ",2,2);
		model.setValueAt(info.usedVirtualMem+" K ",2,3);
		
		
		model.setValueAt(info.totalDisk+" MB ",3,1);
		model.setValueAt(info.freeDisk+" MB ",3,2);
		model.setValueAt(info.usedDisk+" MB ",3,3);
		
		memBar.setValue( (int) ((info.usedPhysicalMem*100)/(info.physicalMem)));
		model.fireTableCellUpdated(1, 4);
		cpuBar.setValue((int)info.cpu);
		model.fireTableCellUpdated(0, 4);
		virtualBar.setValue((int) ((info.usedVirtualMem*100)/(info.virtualMem)));
		model.fireTableCellUpdated(2, 4);
		diskBar.setValue((int) ((info.usedDisk*100)/(info.totalDisk)));
		model.fireTableCellUpdated(3, 4);
		memGraph.list.addFirst(new Integer((int) ((info.usedPhysicalMem*100)/(info.physicalMem))));
		CPUGraph.list.addFirst(new Integer(info.cpu));
		vMemGraph.list.addFirst(new Integer((int)((info.usedVirtualMem*100)/(info.virtualMem))));
		memGraph.repaint();
		CPUGraph.repaint();
		vMemGraph.repaint();
		
	}
	private JMenuBar createMenuBar() {
        //Create the menu bar.
        menuBar = new JMenuBar();
        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("Main");
        menuBar.add(menu);
    
        //a group of JMenuItems
        ImageIcon icon = createImageIcon("images/start1.gif");
		
		selItem = new JMenuItem("Select",icon);
        
        selItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        selItem.getAccessibleContext().setAccessibleDescription(SELECT);
		selItem.setMnemonic(KeyEvent.VK_S);
		selItem.setActionCommand(SELECT);
        selItem.addActionListener(this);
		menu.add(selItem);
		
		menu.addSeparator();
		
		icon = createImageIcon("images/exit1.gif");
        exitItem = new JMenuItem(EXIT,icon);
		exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setActionCommand(EXIT);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        exitItem.getAccessibleContext().setAccessibleDescription(EXIT);
        exitItem.addActionListener(this);
        menu.add(exitItem);
       
        menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_O);
        menu.getAccessibleContext().setAccessibleDescription("Options");
        menuBar.add(menu);
    	
    	ButtonGroup group;
    	
    	memMenu = new JMenu("Memory Graph");
    	memMenu.setMnemonic(KeyEvent.VK_M);
        memMenu.getAccessibleContext().setAccessibleDescription("Memory Graph");
        
        group = new ButtonGroup();
        sub = new JMenu("Type");
        sub.setMnemonic(KeyEvent.VK_T);
        sub.getAccessibleContext().setAccessibleDescription("Type");
        memMenu.add(sub);
        
        JRadioButtonMenuItem item = new JRadioButtonMenuItem("Bar");
        item.getAccessibleContext().setAccessibleDescription("MBar");
        item.setActionCommand("MBar");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Line");
        item.getAccessibleContext().setAccessibleDescription("MLine");
        item.setActionCommand("MLine");
        item.addActionListener(this);
        group.add(item);
        item.setSelected(true);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Level");
        item.getAccessibleContext().setAccessibleDescription("MLevel");
        item.setActionCommand("MLevel");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        sub = new JMenu("Direction");
        sub.setMnemonic(KeyEvent.VK_D);
        sub.getAccessibleContext().setAccessibleDescription("Orientation");
        memMenu.add(sub);
        
        group = new ButtonGroup();
        item = new JRadioButtonMenuItem("Left-Right");
        item.getAccessibleContext().setAccessibleDescription("MLeft-Right");
        item.setActionCommand("MLeft-Right");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Right-Left");
        item.getAccessibleContext().setAccessibleDescription("MRight-Left");
        item.setActionCommand("MRight-Left");
        item.addActionListener(this);
        group.add(item);
        item.setSelected(true);
        sub.add(item);
        
        sub = new JMenu("Orientation");
        sub.setMnemonic(KeyEvent.VK_I);
        sub.getAccessibleContext().setAccessibleDescription("Orientation");
        memMenu.add(sub);
        
        group = new ButtonGroup();
        item = new JRadioButtonMenuItem("Top-Bottom");
        item.getAccessibleContext().setAccessibleDescription("MTop-Bottom");
        item.setActionCommand("MTop-Bottom");
        item.addActionListener(this);
        group.add(item);
        item.setSelected(true);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Bottom-Top");
        item.getAccessibleContext().setAccessibleDescription("MBottom-Top");
        item.setActionCommand("MBottom-Top");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
              
        menu.add(memMenu);
        
        //memMenu.setEnabled(false);
   		
   		CPUMenu = new JMenu("CPU Graph");
    	CPUMenu.setMnemonic(KeyEvent.VK_P);
        CPUMenu.getAccessibleContext().setAccessibleDescription("CPUory Graph");
        
        group = new ButtonGroup();
        sub = new JMenu("Type");
        sub.getAccessibleContext().setAccessibleDescription("Type");
        CPUMenu.add(sub);
        
        item = new JRadioButtonMenuItem("Bar");
        item.getAccessibleContext().setAccessibleDescription("CBar");
        item.setActionCommand("CBar");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Line");
        item.getAccessibleContext().setAccessibleDescription("CLine");
        item.setActionCommand("CLine");
        item.addActionListener(this);
        item.setSelected(true);
        group.add(item);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Level");
        item.getAccessibleContext().setAccessibleDescription("CLevel");
        item.setActionCommand("CLevel");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        sub = new JMenu("Direction");
        sub.getAccessibleContext().setAccessibleDescription("Orientation");
        CPUMenu.add(sub);
        group = new ButtonGroup();
        item = new JRadioButtonMenuItem("Left-Right");
        item.getAccessibleContext().setAccessibleDescription("CLeft-Right");
        item.setActionCommand("CLeft-Right");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Right-Left");
        item.getAccessibleContext().setAccessibleDescription("CRight-Left");
        item.setActionCommand("CRight-Left");
        item.addActionListener(this);
        group.add(item);
        item.setSelected(true);
        sub.add(item);
        
        group = new ButtonGroup();
        sub = new JMenu("Orientation");
        sub.getAccessibleContext().setAccessibleDescription("Orientation");
        CPUMenu.add(sub);
        
        item = new JRadioButtonMenuItem("Top-Bottom");
        item.getAccessibleContext().setAccessibleDescription("CTop-Bottom");
        item.setActionCommand("CTop-Bottom");
        item.addActionListener(this);
        group.add(item);
        sub.add(item);
        
        item = new JRadioButtonMenuItem("Bottom-Top");
        item.getAccessibleContext().setAccessibleDescription("CBottom-Top");
        item.setActionCommand("CBottom-Top");
        item.addActionListener(this);
        item.setSelected(true);
        group.add(item);
        sub.add(item);
        
        
        menu.add(CPUMenu);
        //CPUMenu.setEnabled(false);
        
        menu = new JMenu("Dynamic Graphs");
        menu.setMnemonic(KeyEvent.VK_Y);
        menu.getAccessibleContext().setAccessibleDescription("Dynamic Graphs");
        menuBar.add(menu);
      	icon = createImageIcon("images/cpu1.gif");
        JMenuItem item1 = new JMenuItem("CPU",icon);
        item1.setMnemonic(KeyEvent.VK_P);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        item1.getAccessibleContext().setAccessibleDescription("DynamicCpuGraph");
        item1.setActionCommand("DynamicCpuGraph");
        item1.addActionListener(this);
        menu.add(item1);
       	icon = createImageIcon("images/ram1.gif");
       	item1 = new JMenuItem("Memory",icon);
        item1.setMnemonic(KeyEvent.VK_Y);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.ALT_MASK));
        item1.getAccessibleContext().setAccessibleDescription("DynamicMemGraph");
        item1.setActionCommand("DynamicMemGraph");
        item1.addActionListener(this);
        menu.add(item1);
               
        menu = new JMenu("Graphs");
        menu.setMnemonic(KeyEvent.VK_G);
        menu.getAccessibleContext().setAccessibleDescription("Graphs");
        menuBar.add(menu);
    
        sub = new JMenu("Usage Graphs");
        menu.setMnemonic(KeyEvent.VK_U);
        menu.getAccessibleContext().setAccessibleDescription("Graphs");
        menu.add(sub);
		icon = createImageIcon("images/disk1.gif");    
        item1 = new JMenuItem("Disk",icon);
        item1.setMnemonic(KeyEvent.VK_D);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        item1.getAccessibleContext().setAccessibleDescription("Disk");
        item1.setActionCommand("Disk");
        item1.addActionListener(this);
        sub.add(item1);
       	icon = createImageIcon("images/virtual1.gif");    
       	item1 = new JMenuItem("Virtual Memory",icon);
        item1.setMnemonic(KeyEvent.VK_V);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        item1.getAccessibleContext().setAccessibleDescription("Virtual");
        item1.setActionCommand("Virtual");
        item1.addActionListener(this);
        sub.add(item1);
		icon = createImageIcon("images/ram1.gif");	
        item1 = new JMenuItem("Physical Memory",icon);
        item1.setMnemonic(KeyEvent.VK_M);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        item1.getAccessibleContext().setAccessibleDescription("Physical");
        item1.setActionCommand("Physical");
        item1.addActionListener(this);
        sub.add(item1);
        icon = createImageIcon("images/cpu1.gif");
        item1 = new JMenuItem("CPU",icon);
        item1.setMnemonic(KeyEvent.VK_P);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        item1.getAccessibleContext().setAccessibleDescription("CPU");
        item1.setActionCommand("CPU");
        item1.addActionListener(this);
        sub.add(item1);
        
        sub = new JMenu("Resources");
        menu.setMnemonic(KeyEvent.VK_R);
        menu.getAccessibleContext().setAccessibleDescription("Comparision");
        menu.add(sub);
        icon = createImageIcon("images/disk1.gif");
        item1 = new JMenuItem("Disk",icon);
        item1.setMnemonic(KeyEvent.VK_I);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.SHIFT_MASK));
        item1.getAccessibleContext().setAccessibleDescription("RDisk");
        item1.setActionCommand("RDisk");
        item1.addActionListener(this);
        sub.add(item1);
       	icon = createImageIcon("images/virtual1.gif");    
       	item1 = new JMenuItem("Virtual Memory",icon);
        item1.setMnemonic(KeyEvent.VK_O);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.SHIFT_MASK));
        item1.getAccessibleContext().setAccessibleDescription("RVirtual");
        item1.setActionCommand("RVirtual");
        item1.addActionListener(this);
        sub.add(item1);
		icon = createImageIcon("images/ram1.gif");	
        item1 = new JMenuItem("Physical Memory",icon);
        item1.setMnemonic(KeyEvent.VK_S);
        item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK));
        item1.getAccessibleContext().setAccessibleDescription("RPhysical");
        item1.setActionCommand("RPhysical");
        item1.addActionListener(this);
        sub.add(item1);
        
        return menuBar;
    }
	private void addButtons(JToolBar toolBar) {
                
		
		selButton = makeNavigationButton("images/start.gif", SELECT,
                                     "Select Machine",
                                      SELECT);
									  
		toolBar.add(selButton);
		
		
		exitButton = makeNavigationButton("images/exit.gif", EXIT,
                                      "Quit",
                                      EXIT);
        toolBar.add(exitButton);
		
		
    }

    private JButton makeNavigationButton(String imageName,
                                           String actionCommand,
                                           String toolTipText,
                                           String altText) {
        //Look for the image.
       
        //Create and initialize the button.
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
    public void setAddress(String add)
    {
    	address=add;
       	if(address != null)
    	{
    		if(!tabbedPane.isEnabled())
    		{
    			tabbedPane.setEnabled(true);
    			
    		}
    		populateTab(tabbedPane.getSelectedIndex()+1);
    		String title = frame.getTitle();
    		if(title.length()<=10)
    		frame.setTitle(title+" [ "+add+" ] ");
    			
    	}
        		
    }
	
    private void populateTab(int index)
    {
    	
    	String str = " ";
        ArrayList array;
        Iterator it;
        boolean bool=false;
        String s=null;
        try
        {
        	BrokerServerInterface in = (BrokerServerInterface)Naming.lookup(brokerAddress);
        	switch(index)
        	{
        		case 2:
        			
        			str=" ";
        			array = in.getProcessInfo(address);
        			if(array == null)
        			{
        				Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
            			SwingUtilities.getWindowAncestor(this),
           	   			"This option is not available",
			   			"Not Applicable",
            			JOptionPane.YES_NO_OPTION,
            			JOptionPane.QUESTION_MESSAGE,
            			null,
            			obj,
            			obj[0]
						);	
						return;
        			}
    				it = array.iterator();
    				int count=0;
    				while(it.hasNext())
    				{
    					s = (String)it.next()+"\n";
						str+=s;
    					
    				}
    				processInformation.setText(str);
					
					runIt=false;
					infoThread=null;
        			break;
        			
        			case 3:
        			str=" ";
        			array = in.getDetailInfo(address);
        			if(array == null)
        			{
        				Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
            			SwingUtilities.getWindowAncestor(this),
           	   			"This option is not available",
			   			"Not Applicable",
            			JOptionPane.YES_NO_OPTION,
            			JOptionPane.QUESTION_MESSAGE,
            			null,
            			obj,
            			obj[0]
						);	
						return;
        			}
    				it = array.iterator();
    				
    				while(it.hasNext())
    				{
    					s = (String)it.next()+"\n";
    					str+=s;
    					
    				}
    				detailInformation.setText(str);
					runIt=false;
					infoThread=null;
        			break;
        			default:
					if (runIt)
					{
						runIt=false;
						try
						{
						Thread.sleep(2000);
						}
						catch(InterruptedException ie)
						{
						}
					}
        			infoThread = new GetBrokerInformation(this,brokerAddress);
					infoThread.setDaemon(true);
					runIt=true;
					infoThread.machine=address;
					infoThread.start();
					
					
        			break;
        		}
        	}
        	catch(RemoteException re)
        	{re.printStackTrace();}
    		catch(NotBoundException nbe)
    		{nbe.printStackTrace();}
    		catch(java.net.MalformedURLException mue)
    		{mue.printStackTrace();}
    	
    }
    public void showCharts(HashSet set)
    {
    	Point point = getLocation();
    	if(charts==0)
    	{
    	   	Charts charts = new Charts(brokerAddress,set,selection);
    	   	charts.showCharts(point);
    	}
    	else if(charts==1)
    	{
    		ResourceCharts resource = new ResourceCharts(brokerAddress,set,selection);
    		resource.showCharts(point);
    	}
    	else
    	{
    		GraphFrame graph;
    		Iterator it = set.iterator();
    		while(it.hasNext())
    		{
    			String address = (String)it.next();
    			if(graphSelected==1)
    			{
    				frameCount++;
    				graph = new GraphFrame(this,graphSelected,brokerAddress,address,mOrientation,mDirection,mType,frameCount);
    	   			graph.showGraph(point,address+" Memory Usage Hostory");
    	   			frames.put(" "+frameCount,graph);
    	   			
    	   		}
    	   		else if(graphSelected==2)
    			{
    				frameCount++;
    				graph = new GraphFrame(this,graphSelected,brokerAddress,address,cOrientation,cDirection,cType,frameCount);
    	   			graph.showGraph(point,address+" CPU Usage Hostory");
    	   			frames.put(" "+frameCount,graph);
    	   			
    	   		}
    	   		    			
    		}
    		
    	}
    		
		
    }
    public void clearFrame(long id)
    {
    	frames.remove(" "+id);
    }
    public void stateChanged(ChangeEvent e)
    {
    	JTabbedPane source = (JTabbedPane)e.getSource();
        int index = (int)source.getSelectedIndex();
        populateTab(index+1);
	}
		
    private void createAndShowGUI(BrokerUI pane) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame = new JFrame("Broker");
        
        frame.setJMenuBar(createMenuBar());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                   
        pane.setOpaque(true);
        
        frame.setContentPane(pane);
		
		Dimension dim = pane.getToolkit().getScreenSize();
		frame.setLocation((dim.width-550)/2,(dim.height-570)/2);
		//Display the window.
        frame.pack();
        frame.setVisible(true);
        
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        if(args.length!=1)
        {
        	System.out.println("Proper Use: BrokerUI Broker IP Address");
        	System.exit(0);
        }
        final String address=args[0];
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	BrokerUI pane = new BrokerUI();
            	pane.brokerAddress="rmi://"+address+"/brokerserver";
                pane.createAndShowGUI(pane);
            }
        });
    }
}
