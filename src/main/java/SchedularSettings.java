import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JTree;
import java.io.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.*;


public class SchedularSettings extends JPanel
                      implements ActionListener,ChangeListener,WindowListener{
    private JEditorPane htmlPane;
    private JPanel paneli,panelj;
    private static boolean DEBUG = false;
	private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem exitItem,applyItem,resetItem,saveItem,loadItem;
	private JButton applyButton,resetButton,saveButton,loadButton;
    private JSlider sl1,sl2,sl3,sl4,sl5,sl6;
	private JSpinner sp1,sp2,sp3,sp4,sp5,sp6;
	private int min_mem,max_mem,def_mem;
	private int temp_def_mem;
	private int min_cpu,max_cpu,def_cpu;
	private int temp_def_cpu;
	private int min_disk,max_disk,def_disk;
	private int temp_def_disk;
	private int min_waiting,max_waiting,def_waiting;
	private int temp_def_waiting;
	private int min_elapsed,max_elapsed,def_elapsed;
	private int temp_def_elapsed;
	private int min_manage,max_manage,def_manage,temp_def_manage;
	private SpinnerNumberModel memModel,cpuModel,diskModel,waitingModel,elapseModel,manageModel;
	static final private String APPLY = "Apply";
    static final private String RESET = "Reset";
    static final private String EXIT = "Quit";
	static final private String DEFAULT ="Default";
	private static final String LOAD = "Load";
	private static final String SAVE = "Save";
	private static String lineStyle = "Horizontal";
    private static boolean useSystemLookAndFeel = false;
	private SchedularUI parent;
	private JTextField broker,security;
	private JFrame frame;
	private String brokerAddress;
	private JFileChooser fc;
	private File sFile;
	private Settings settings;
	public SchedularSettings(SchedularUI ui,String bro,String sec,int mem,int cpu,int disk,int wait,int elapsed,int man) {
		
		parent= ui;
		brokerAddress = bro;
		
		min_mem=1;
		max_mem=100;
		def_mem=80;
		
		temp_def_mem=mem;
		
		min_disk=1;
		max_disk=100;
		def_disk=80;
		
		temp_def_disk=disk;
		
		min_cpu=1;
		max_cpu=100;
		def_cpu=80;
		
		temp_def_cpu=cpu;
		
		min_waiting=1000;
		max_waiting=1000*120;
		def_waiting=1000;
		
		temp_def_waiting=wait;
		
		min_elapsed=1;
		max_elapsed=25;
		def_elapsed=5;
		
		temp_def_elapsed=elapsed;
		
		min_manage=1000;
		max_manage=1000*120;
		def_manage=2000;
		
		temp_def_manage=man;
        
        setLayout(new BorderLayout());
        JToolBar toolBar = new JToolBar("Scheduler");
        addButtons(toolBar);
        toolBar.setPreferredSize(new Dimension(350,45));
       	
		JPanel p = new JPanel();
		p.setLayout(new SpringLayout());
				
		setPreferredSize(new Dimension(450,420));
				
		p.setPreferredSize(new Dimension(450,260));
		
		JLabel l = new JLabel("Broker:");
        p.add(l);
		broker = new JTextField(20);
        l.setLabelFor(broker);
        p.add(broker);
		p.add(new JLabel(""));
		broker.setText(brokerAddress);
		security = new JTextField(20);
		l = new JLabel("Security Manager:");
		l.setLabelFor(security);
		security.setText(sec);
		p.add(l);
		p.add(security);
		p.add(new Label(""));
		String[] labels = 
		{"Memory: ","CPU: ","Disk: ","Waiting: ","Elapse: "
		};
	    
		JLabel l1 = new JLabel(labels[0]);
        p.add(l1);
        sl1 = new JSlider(min_mem,max_mem,temp_def_mem);
		sl1.setMajorTickSpacing(10);
		sl1.setPaintTicks(true);
		p.add(sl1);
		memModel = new SpinnerNumberModel(temp_def_mem,min_mem,max_mem,1);
		sp1 = new JSpinner(memModel);
		p.add(sp1);
		sl1.addChangeListener(this);
		sp1.addChangeListener(this);
		
		JLabel l2 = new JLabel(labels[1]);
		sl2 = new JSlider(min_cpu,max_cpu,temp_def_cpu);
		l2.setLabelFor(sl2);
		p.add(l2);
		sl2.setMajorTickSpacing(10);
		sl2.setPaintTicks(true);
		p.add(sl2);
		cpuModel = new SpinnerNumberModel(temp_def_cpu,min_cpu,max_cpu,1);
		sp2 = new JSpinner(cpuModel);
		p.add(sp2);
		sl2.addChangeListener(this);
		sp2.addChangeListener(this);
			
		JLabel l3 = new JLabel(labels[2]);
        p.add(l3);
        sl3 = new JSlider(min_disk,max_disk,temp_def_disk);
		l3.setLabelFor(sl3);
		sl3.setMajorTickSpacing(10);
		sl3.setPaintTicks(true);
		p.add(sl3);
		diskModel = new SpinnerNumberModel(temp_def_disk,min_disk,max_disk,1);
		sp3 = new JSpinner(diskModel);
		p.add(sp3);
		sl3.addChangeListener(this);
		sp3.addChangeListener(this);
		
		JLabel l4 = new JLabel(labels[3]);
        p.add(l4);
        sl4 = new JSlider(min_waiting/1000,max_waiting/1000,temp_def_waiting/1000);
		l4.setLabelFor(sl4);
		sl4.setMajorTickSpacing(15000/1000);
		sl4.setPaintTicks(true);
		p.add(sl4);
		waitingModel = new SpinnerNumberModel(temp_def_waiting,min_waiting,max_waiting,1000);
		sp4 = new JSpinner(waitingModel);
		p.add(sp4);
		sl4.addChangeListener(this);
		sp4.addChangeListener(this);
		
		
		JLabel l5 = new JLabel(labels[4]);
        p.add(l5);
        sl5 = new JSlider(min_elapsed,max_elapsed,temp_def_elapsed);
		l5.setLabelFor(sl5);
		sl5.setMajorTickSpacing(3);
		sl5.setPaintTicks(true);
		p.add(sl5);
		elapseModel = new SpinnerNumberModel(temp_def_elapsed,min_elapsed,max_elapsed,1);
		sp5 = new JSpinner(elapseModel);
		p.add(sp5);
		sl5.addChangeListener(this);
		sp5.addChangeListener(this);
		
		JLabel l6 = new JLabel("Management:");
        p.add(l6);
        sl6 = new JSlider(min_manage/1000,max_manage/1000,temp_def_manage/1000);
		l6.setLabelFor(sl6);
		sl6.setMajorTickSpacing(15);
		sl6.setPaintTicks(true);
		p.add(sl6);
		manageModel = new SpinnerNumberModel(temp_def_manage,min_manage,max_manage,1000);
		sp6 = new JSpinner(manageModel);
		p.add(sp6);
		sl6.addChangeListener(this);
		sp6.addChangeListener(this);
		
		SpringUtilities.makeCompactGrid(p,
                                        8, 3, //rows, cols
                                        10, 10,        //initX, initY
                                        20, 20);       //xPad, yPad
		
        add(toolBar,BorderLayout.PAGE_START);  
		add(p,BorderLayout.CENTER);
		
			
	
		
    }

    /** Required by TreeSelectionListener interface. */
    
	private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SchedularSettings.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if (APPLY.equals(e.getActionCommand()))
	   {
	   		
			
			applyItem.setEnabled(false);
			resetItem.setEnabled(false);
			temp_def_mem=(int)sl1.getValue();
			temp_def_cpu=(int)sl2.getValue();
			temp_def_disk=(int)sl3.getValue();
			temp_def_waiting=(int)sl4.getValue()*1000;
			temp_def_elapsed=(int)sl5.getValue();
			temp_def_manage=(int)sl6.getValue()*1000;
			brokerAddress = broker.getText();
			applyItem.setEnabled(false);
			resetItem.setEnabled(false);
			resetButton.setEnabled(false);
			applyButton.setEnabled(false);
			parent.setSettings(brokerAddress,security.getText(),temp_def_mem,temp_def_cpu,temp_def_disk,temp_def_waiting,temp_def_elapsed,temp_def_manage);
			
		}
		else if (RESET.equals(e.getActionCommand()))
	   	{
	   		sl1.setValue(temp_def_mem);
			sl2.setValue(temp_def_cpu);
			sl3.setValue(temp_def_disk);
			sl4.setValue(temp_def_waiting/1000);
			sl5.setValue(temp_def_elapsed);
			sl6.setValue(temp_def_manage/1000);
			applyItem.setEnabled(false);
			resetItem.setEnabled(false);
			resetButton.setEnabled(false);
			applyButton.setEnabled(false);
	   }
	   else if (DEFAULT.equals(e.getActionCommand()))
	   {
	   		sl1.setValue(def_mem);
			sl2.setValue(def_cpu);
			sl3.setValue(def_disk);
			sl4.setValue(def_waiting/1000);
			sl5.setValue(def_elapsed);
			sl6.setValue(def_manage/1000);
			applyItem.setEnabled(true);
			resetItem.setEnabled(false);
			resetButton.setEnabled(false);
			applyButton.setEnabled(true);
	   }
	   else if(LOAD.equals(e.getActionCommand()))
	   {
			if (fc==null)
			{
				fc = new JFileChooser();
				fc.addChoosableFileFilter(new SettingsFilter());
			}
			fc.setAcceptAllFileFilterUsed(false);
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					sFile = fc.getSelectedFile();
					FileInputStream fis = new FileInputStream(sFile);
					ObjectInputStream in = new ObjectInputStream(fis);
					settings = new Settings();
					settings = (Settings)in.readObject();
					sl1.setValue(settings.mem);
					sl2.setValue(settings.cpu);
					sl3.setValue(settings.disk);
					sl4.setValue(settings.wait);
					sl5.setValue(settings.elapsed);
					sl6.setValue(settings.man);
					broker.setText(settings.broker);
					security.setText(settings.security);
					fis.close();
					in.close();
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
				catch(ClassNotFoundException cne)
				{
					System.out.println("Invalid File format...");
					return;
				}
				
			}		   		   
	   }
	   else if (SAVE.equals(e.getActionCommand()))
	   {
	   		if (sFile == null)
			{
				if (fc == null)
				{
					fc = new JFileChooser();
					fc.addChoosableFileFilter(new SettingsFilter());
				}
				fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					sFile = fc.getSelectedFile();
				}
				else
				return;
			}
			try
			{
				FileOutputStream fos = new FileOutputStream(sFile);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				settings = new Settings();
				settings.mem = sl1.getValue();
				settings.cpu = sl2.getValue();
				settings.disk = sl3.getValue();
				settings.wait = sl4.getValue();
				settings.elapsed = sl5.getValue();
				settings.man = sl6.getValue();
				settings.broker=broker.getText();
				settings.security=security.getText();
				out.writeObject(settings);
				out.flush();
				out.close();
				
			}
			catch(IOException ioe)
			{
			}
			
			
					
			
	   }
	   else //if (EXIT.equals(e.getActionCommand()))
	   {
	   		processExit();	
       }
					
    }
	public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
		if (source instanceof JSlider)
		{
			JSlider slider = (JSlider)source;
			applyItem.setEnabled(true);
			resetItem.setEnabled(true);
			applyButton.setEnabled(true);
			resetButton.setEnabled(true);
			slider.setToolTipText(" "+(int)slider.getValue());
			if (slider.hashCode()==sl1.hashCode())
			{
				
				memModel.setValue(new Integer((int)slider.getValue()));
			}
			else if (slider.hashCode() == sl2.hashCode())
			{
				
				cpuModel.setValue(new Integer((int)slider.getValue()));
			}
			else if (slider.hashCode() == sl3.hashCode())
			{
				
				diskModel.setValue(new Integer((int)slider.getValue()));
			}
			else if (slider.hashCode() == sl4.hashCode())
			{
				int value = 0;
				value = (int)slider.getValue();
				
				waitingModel.setValue(new Integer((int)slider.getValue()*1000));
			}
			else if (slider.hashCode() == sl5.hashCode())
			{
				
				elapseModel.setValue(new Integer((int)slider.getValue()));
			}
			else if (slider.hashCode() == sl6.hashCode())
			{
				
				manageModel.setValue(new Integer((int)slider.getValue()*1000));
			}
			
		}
		else if (e.getSource() instanceof JSpinner)
		{
			JSpinner spinner = (JSpinner)source;
			applyItem.setEnabled(true);
			resetItem.setEnabled(true);
			applyButton.setEnabled(true);
			resetButton.setEnabled(true);
			int num=0;
			if (sp1.hashCode()==spinner.hashCode())
			{
				num = memModel.getNumber().intValue();
				sl1.setValue(num);
				
			}
			else if (sp2.hashCode()==spinner.hashCode())
			{
				num = cpuModel.getNumber().intValue();
				sl2.setValue(num);
				
			}
			else if (sp3.hashCode()==spinner.hashCode())
			{
				num = diskModel.getNumber().intValue();
				sl3.setValue(num);
				
			}
			else if (sp4.hashCode()==spinner.hashCode())
			{
				num = waitingModel.getNumber().intValue();
				sl4.setValue(num/1000);
				
			}
			else if(sp5.hashCode()==spinner.hashCode())
			{
				num = elapseModel.getNumber().intValue();
				sl5.setValue(num);
				
			}
			else if(sp6.hashCode()==spinner.hashCode())
			{
				num = manageModel.getNumber().intValue();
				sl6.setValue(num/1000);
				
			}
			
			
			
		}
		
		        
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
        ImageIcon icon = createImageIcon("images/save1.gif");
		applyItem = new JMenuItem("Apply",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        applyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        applyItem.getAccessibleContext().setAccessibleDescription("Apply");
		applyItem.setMnemonic(KeyEvent.VK_A);
        applyItem.addActionListener(this);
		applyItem.setEnabled(false);
        menu.add(applyItem);

        icon = createImageIcon("images/reset1.gif");
        resetItem = new JMenuItem("Reset",icon);
		resetItem.setMnemonic(KeyEvent.VK_R);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        resetItem.getAccessibleContext().setAccessibleDescription("Reset");
        resetItem.addActionListener(this);
		resetItem.setEnabled(false);
        menu.add(resetItem);
		
		icon = createImageIcon("images/default1.gif");
        JMenuItem item = new JMenuItem("Default",icon);
		item.setMnemonic(KeyEvent.VK_D);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        item.getAccessibleContext().setAccessibleDescription("Default");
        item.addActionListener(this);
		menu.add(item);

        //a group of radio button menu items
        menu.addSeparator();
		
		icon = createImageIcon("images/savefile1.gif");
        saveItem = new JMenuItem("Save settings",icon);
		saveItem.setMnemonic(KeyEvent.VK_F);
		saveItem.setActionCommand(SAVE);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        saveItem.getAccessibleContext().setAccessibleDescription("Save");
        saveItem.addActionListener(this);
        menu.add(saveItem);
		
		icon = createImageIcon("images/load1.gif");
        loadItem = new JMenuItem("Load settings",icon);
		loadItem.setMnemonic(KeyEvent.VK_L);
		loadItem.setActionCommand(LOAD);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        loadItem.getAccessibleContext().setAccessibleDescription("Load");
		loadItem.setActionCommand(LOAD);
		loadItem.addActionListener(this);
        menu.add(loadItem);
		
		menu.addSeparator();
		
		icon = createImageIcon("images/exit1.gif");
        exitItem = new JMenuItem("Quit",icon);
		exitItem.setMnemonic(KeyEvent.VK_Q);
		exitItem.setActionCommand(LOAD);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.getAccessibleContext().setAccessibleDescription("Quit");
		exitItem.setActionCommand(EXIT);
		exitItem.addActionListener(this);
        menu.add(exitItem);
	    menuBar.add(menu);

        return menuBar;
    }
	private void addButtons(JToolBar toolBar) {
        JButton button = null;
        
        applyButton = makeNavigationButton("images/save.gif", APPLY,
                                     "Apply Changes",
                                      APPLY);
		applyButton.setEnabled(false);	  
        toolBar.add(applyButton);
		
        resetButton = makeNavigationButton("images/reset.gif", RESET,
                                      "Reset Changes",
                                      RESET);
		resetButton.setEnabled(false);	  
        toolBar.add(resetButton);

        
		button = makeNavigationButton("images/default.gif", DEFAULT,
                                      "Default Settings",
                                      DEFAULT);
        toolBar.add(button);
		
		button = makeNavigationButton("images/savefile.gif", SAVE,
                                      "Save Settings",
                                      SAVE);
        toolBar.add(button);
		
		button = makeNavigationButton("images/load.gif", LOAD,
                                      "Load Settings",
                                      LOAD);
        toolBar.add(button);
		
		
		toolBar.add(new JSeparator(JSeparator.VERTICAL));
        button = makeNavigationButton("images/exit.gif", EXIT,
                                      "Quit",
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
	private void createAndShowGUI(final Point p) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        frame = new JFrame("Scheduler Settings");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //Create and set up the content pane.
        this.setOpaque(true); //content panes must be opaque
		frame.setJMenuBar(this.createMenuBar());
        frame.setContentPane(this);
		//Display the window.
		frame.setLocation(p.x+100,p.y+100);
		frame.addWindowListener(this);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }
	public void windowClosing(WindowEvent e) 
	{
    	
		processExit();
	}
    	
	public void windowClosed(WindowEvent e) 
	{
        
    }

    public void windowOpened(WindowEvent e) 
	{
        
    }

    public void windowIconified(WindowEvent e) 
	{
        
    }

    public void windowDeiconified(WindowEvent e) 
	{
       
    }

    public void windowActivated(WindowEvent e) 
	{
        
    }

    public void windowDeactivated(WindowEvent e) 
	{
        
    }
	private void processExit()
	{
		if (applyButton.isEnabled())
		{
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Yes","No"};
			int answer = JOptionPane.showOptionDialog
			(
        	   	SwingUtilities.getWindowAncestor(this),
           	   "Do you want to apply changes before exit?",
			   "Save changes?",
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.QUESTION_MESSAGE,
            	null,
            	obj,
            	obj[0]
			);
				if (answer == JOptionPane.YES_OPTION)
				{
					temp_def_mem=(int)sl1.getValue();
					temp_def_cpu=(int)sl2.getValue();
					temp_def_disk=(int)sl3.getValue();
					temp_def_waiting=(int)sl4.getValue()*1000;
					temp_def_elapsed=(int)sl5.getValue();
					temp_def_manage=(int)sl6.getValue()*1000;
					brokerAddress = broker.getText();
					parent.setSettings(brokerAddress,security.getText(),temp_def_mem,temp_def_cpu,temp_def_disk,temp_def_waiting,temp_def_elapsed,temp_def_manage);
			
				}
				
			
	   		}
	  		 frame.dispose();
	}
    public void showSettings(final Point point) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(point);
            }
        });
    }
	
}

