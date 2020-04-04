import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;


public class SchedularConfiguration extends JPanel
    		               implements ActionListener,TableModelListener,WindowListener{
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem exitItem,applyItem,resetItem,addItem,delItem,loadItem,clearItem,saveasItem,newItem;
	private JButton applyButton,resetButton,addButton,delButton,loadButton,newButton,saveasButton,clearButton;
	private JTable table;
	private DefaultTableModel tableModel;
    private HashSet config = new HashSet();	 
	static final private String SAVE  = "Apply";
    static final private String RESET = "Reset";
    static final private String EXIT  = "Quit";
	static final private String ROW   = "Row";
	static final private String DEL   = "Del";
	static final private String LOAD  = "Load";
	static final private String NEW   = "New";
	static final private String SAVEAS= "Save As";
	static final private String CLEAR = "Clear";
	private boolean isEditing;
	private boolean isLoaded;
	private final static boolean ALLOW_ROW_SELECTION = true;
	private File configFile;
	private JFileChooser fc;
	private int rowSelected;
	private JFrame frame;
	
	public SchedularConfiguration() {
		rowSelected=-1;
		isLoaded=false;
		isEditing=false;
		configFile=null;
		setLayout(new BorderLayout());
			
		Dimension pos = getToolkit().getScreenSize();
		
		setPreferredSize(new Dimension(pos.width/2,pos.height/3));
		
        JToolBar toolBar = new JToolBar("Schedular");
        addButtons(toolBar);
        toolBar.setPreferredSize(new Dimension(450,45));
       	tableModel = new DefaultTableModel();
		tableModel.addColumn("Location");
		tableModel.addColumn("Service");
		tableModel.addColumn("Handle");
		tableModel.addColumn("Type");
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableView = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		add(toolBar,BorderLayout.PAGE_START);  
		add(tableView,BorderLayout.CENTER);
		TableColumn column = null;
		for (int i = 0; i < 4; i++) 
		{
    		column = table.getColumnModel().getColumn(i);
    		if (i == 2) 
			{
       			 column.setPreferredWidth(150); //sport column is bigger
    		}
			else
			{
        		column.setPreferredWidth(100);
    		}
		}
	
		if (ALLOW_ROW_SELECTION) { // true by default
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;

                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        rowSelected=-1;
                    } else {
                        rowSelected = lsm.getMinSelectionIndex();
						if (!delItem.isEnabled())
						{
							delButton.setEnabled(true);
							delItem.setEnabled(true);
							
						}
						if (!clearItem.isEnabled())
						{
							clearButton.setEnabled(true);
							clearItem.setEnabled(true);
						}
                        
                    }
                }
            });
        } else {
            table.setRowSelectionAllowed(false);
        }

	}

    /** Required by TreeSelectionListener interface. */
    
	private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SchedularConfiguration.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if (SAVE.equals(e.getActionCommand()))
	   {
	   		
				if (saveFile())
				{
					resetButton.setEnabled(false);
					resetItem.setEnabled(false);
					applyButton.setEnabled(false);
					applyItem.setEnabled(false);
				
				}
			
	   }
	   else if (RESET.equals(e.getActionCommand()))
	   {
	   		
			Toolkit.getDefaultToolkit().beep();
			Object[] obj={"Yes","No"};
			int answer = JOptionPane.showOptionDialog
			(
            	SwingUtilities.getWindowAncestor(this),
           	   "Do you really want to reset changes ?",
			   "Reset changes?",
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.QUESTION_MESSAGE,
            	null,
            	obj,
            	obj[0]
			);
			if (answer == JOptionPane.YES_OPTION)
			{	
				
				int count = tableModel.getRowCount();
				if (!saveasButton.isEnabled())
				{
					saveasButton.setEnabled(true);
					saveasItem.setEnabled(true);
				}
				clearTable();
				if (isLoaded)
				{
					addButton.setEnabled(true);
					addItem.setEnabled(true);
				}
					
					
				Iterator it = config.iterator();
				
				while (it.hasNext())
				{
					SchedularConfig conf = (SchedularConfig)it.next();
					Object[] data=
					{
						conf.address,
						conf.name,
						conf.handle,
						conf.type
							
					};
					tableModel.addRow(data);
				}
				resetButton.setEnabled(false);
				resetItem.setEnabled(false);
					
			}
	
	   }
	   else if (LOAD.equals(e.getActionCommand()))
	   {
	   		
			if (isLoaded)
			{
				if(processPending()==false)
					return;
				clearTable();
				config.clear();	
			}
			
			if (fc==null)
			{
				fc = new JFileChooser();
			}
			fc.addChoosableFileFilter(new SchedularFilter());
            fc.setAcceptAllFileFilterUsed(false);
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				isLoaded=true;
               	configFile = fc.getSelectedFile();
				getConfiguration(configFile);
				
				Iterator it = config.iterator();
				while (it.hasNext())
				{
					SchedularConfig conf = (SchedularConfig)it.next();
					Object[] obj=
					{
						conf.address,
						conf.name,
						conf.handle,
						conf.type
							
					};
					tableModel.addRow(obj);
				}
              addButton.setEnabled(true);
			  addItem.setEnabled(true);
			  if (tableModel.getRowCount()>0)
			  {
			 	 delButton.setEnabled(true);
			     delItem.setEnabled(true);
			 	 clearItem.setEnabled(true);
			  	 clearButton.setEnabled(true);	
			  	 saveasItem.setEnabled(true);
			  	 saveasButton.setEnabled(true);
				 
			  }
			  table.getModel().addTableModelListener(this);
			  
             
			}
			
	   }
	   else if (ROW.equals(e.getActionCommand()))
	   {
	   		
			Object[] obj=
			{"","","",""
			};
			tableModel.addRow(obj);
			if (!delButton.isEnabled())
			{
				delButton.setEnabled(true);
				delItem.setEnabled(true);
			}
			if (!clearButton.isEnabled())
			{
				clearButton.setEnabled(true);
				clearItem.setEnabled(true);
			}
					
			
	   }
	   else if (DEL.equals(e.getActionCommand()))
	   {
	   		
			if (rowSelected!=-1)
			{
				tableModel.removeRow(rowSelected);
				rowSelected=-1;
			}
			else
			{
			      Object[] obj = 
			      {
				    "Ok"
			      };
				  Toolkit.getDefaultToolkit().beep();
        	      int answer = JOptionPane.showOptionDialog
				  (
            		SwingUtilities.getWindowAncestor(this),
           		   "No Row Selected...",
				   "No Row to delete...",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
					);
	    
        	}
	   }
	   else if(CLEAR.equals(e.getActionCommand()))
	   {
	   		Object[] obj={"Yes","No"};
			int answer = JOptionPane.showOptionDialog
			(
            	SwingUtilities.getWindowAncestor(this),
           	   "Do you really want to clear all rows ?",
			   "Clear all",
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.QUESTION_MESSAGE,
            	null,
            	obj,
            	obj[0]
			);
			if (answer == JOptionPane.YES_OPTION)
			{	
				clearTable();
				if (isLoaded)
				{
					addItem.setEnabled(true);
					addButton.setEnabled(true);
					resetButton.setEnabled(true);
					resetItem.setEnabled(true);
				}
					
			}
	   }
	   else if (NEW.equals(e.getActionCommand()))
	   {
	   		if (applyButton.isEnabled())
			{
				processPending();
				clearTable();
				addButton.setEnabled(true);
				addItem.setEnabled(true);
				configFile=null;
							
			}
			else
			{
				clearTable();
				addButton.setEnabled(true);
			    addItem.setEnabled(true);
				table.getModel().addTableModelListener(this);
				//isLoaded=true;
				
			}	
	   }
	   else if(SAVEAS.equals(e.getActionCommand()))
	   {
	   		if (isLoaded || applyButton.isEnabled())
			{
				saveFile();
				
				if (fc == null)
				{
					fc = new JFileChooser();
				}
				fc.addChoosableFileFilter(new SchedularFilter());
           		fc.setAcceptAllFileFilterUsed(false);
				int returnVal = fc.showSaveDialog(this);
           		if (returnVal == JFileChooser.APPROVE_OPTION)
				{
               		configFile = fc.getSelectedFile();
						
				}
				saveFile();
			}
	    }
	   else //(EXIT.equals(e.getActionCommand()))
	   {
	  		processExit(); 		
	   }
	   
	   	   
    }
	public void tableChanged(TableModelEvent e)
	{
		if (!applyButton.isEnabled())
		{
			applyButton.setEnabled(true);	
			applyItem.setEnabled(true);
		}
		
		if (!resetButton.isEnabled())
		{
			resetButton.setEnabled(true);
			resetItem.setEnabled(true);
		}
		isLoaded=true;
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
        ImageIcon icon = createImageIcon("images/load1.gif");
		
		loadItem = new JMenuItem("Load File",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        loadItem.getAccessibleContext().setAccessibleDescription("Load");
		loadItem.setMnemonic(KeyEvent.VK_L);
		loadItem.setActionCommand("Load");
        loadItem.addActionListener(this);
		menu.add(loadItem);
		
		icon = createImageIcon("images/new1.gif");
		newItem = new JMenuItem("New File",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        newItem.getAccessibleContext().setAccessibleDescription("Load");
		newItem.setMnemonic(KeyEvent.VK_N);
		newItem.setActionCommand("New");
        newItem.addActionListener(this);
		menu.add(newItem);
		
		
		icon = createImageIcon("images/save1.gif");
		applyItem = new JMenuItem("Save",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        applyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        applyItem.getAccessibleContext().setAccessibleDescription("Apply");
		applyItem.setMnemonic(KeyEvent.VK_S);
		applyItem.setActionCommand("Apply");
        applyItem.addActionListener(this);
		applyItem.setEnabled(false);
        menu.add(applyItem);
		
		icon = createImageIcon("images/saveas1.gif");
		saveasItem = new JMenuItem("Save As...",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        saveasItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        saveasItem.getAccessibleContext().setAccessibleDescription("Apply");
		saveasItem.setMnemonic(KeyEvent.VK_S);
		saveasItem.setActionCommand("Save As");
        saveasItem.addActionListener(this);
		saveasItem.setEnabled(false);
        menu.add(saveasItem);

		
        icon = createImageIcon("images/reset1.gif");
        resetItem = new JMenuItem("Reset",icon);
		resetItem.setMnemonic(KeyEvent.VK_T);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
        resetItem.getAccessibleContext().setAccessibleDescription("Reset");
        resetItem.addActionListener(this);
		resetItem.setEnabled(false);
        menu.add(resetItem);
		
		menu.addSeparator();
		icon = createImageIcon("images/add1.gif");
        addItem = new JMenuItem("Add Row",icon);
		addItem.setMnemonic(KeyEvent.VK_R);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        addItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        addItem.getAccessibleContext().setAccessibleDescription("Row");
		addItem.setActionCommand("Row");
        addItem.addActionListener(this);
		addItem.setEnabled(false);
		menu.add(addItem);
		
		icon = createImageIcon("images/del1.gif");
        delItem = new JMenuItem("Delete Row",icon);
		delItem.setMnemonic(KeyEvent.VK_O);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        delItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        delItem.getAccessibleContext().setAccessibleDescription("Del");
		delItem.setActionCommand("Del");
        delItem.addActionListener(this);
		delItem.setEnabled(false);
		menu.add(delItem);
		
		icon = createImageIcon("images/clear1.gif");
        clearItem = new JMenuItem("Clear Rows",icon);
		clearItem.setMnemonic(KeyEvent.VK_W);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK));
        clearItem.getAccessibleContext().setAccessibleDescription("Del");
		clearItem.setActionCommand("Clear");
        clearItem.addActionListener(this);
		clearItem.setEnabled(false);
		menu.add(clearItem);


        //a group of radio button menu items
        menu.addSeparator();
		
		icon = createImageIcon("images/exit1.gif");
        exitItem = new JMenuItem("Quit",icon);
		exitItem.setMnemonic(KeyEvent.VK_Q);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        exitItem.getAccessibleContext().setAccessibleDescription("Quit");
        exitItem.addActionListener(this);
        menu.add(exitItem);

        menuBar.add(menu);

        return menuBar;
    }
	private void addButtons(JToolBar toolBar) {
                
		
		loadButton = makeNavigationButton("images/load.gif", LOAD,
                                     "Load Configuration File",
                                      LOAD);
        toolBar.add(loadButton);
		
		newButton = makeNavigationButton("images/new.gif", NEW,
                                     "New Configuration File",
                                     NEW);
        toolBar.add(newButton);
		
		
		applyButton = makeNavigationButton("images/save.gif", SAVE,
                                     "Save Changes",
                                      SAVE);
									  
		applyButton.setEnabled(false);	  
		toolBar.add(applyButton);
		
		saveasButton = makeNavigationButton("images/saveas.gif", SAVEAS,
                                     "Save File as",
                                      SAVEAS);
									  
		saveasButton.setEnabled(false);	  
		
		toolBar.add(saveasButton);
		//second button
        resetButton = makeNavigationButton("images/reset.gif", RESET,
                                      "Reset Changes",
                                      RESET);
		resetButton.setEnabled(false);	  
        toolBar.add(resetButton);

        //third button
		addButton = makeNavigationButton("images/add.gif", ROW,
                                      "Add New Row",
                                      ROW);
		addButton.setEnabled(false);	  
        toolBar.add(addButton);
		
		delButton = makeNavigationButton("images/del.gif", DEL,
                                      "Delete Selected Row",
                                      DEL);
		delButton.setEnabled(false);	  
        toolBar.add(delButton);
		
		clearButton = makeNavigationButton("images/clear.gif", CLEAR,
                                      "Clear All Rows",
                                      CLEAR);
		clearButton.setEnabled(false);	  
        toolBar.add(clearButton);
		
		
		toolBar.add(new JSeparator(JSeparator.VERTICAL));
        
		JButton button = makeNavigationButton("images/exit.gif", EXIT,
                                      "Quit",
                                      EXIT);
        toolBar.add(button);
		
		
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
	private void getConfiguration(File file)
	{
		
		
		ObjectInputStream in=null;
		SchedularConfig conf=null;
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
	private void clearTable()
	{
		table.getModel().removeTableModelListener(this);	
		int rows = tableModel.getRowCount();
		for(int i=rows-1;i>=0;i--)
		{
			tableModel.removeRow(i);
		}
		if (saveasButton.isEnabled())
		{
			saveasButton.setEnabled(false);
			saveasItem.setEnabled(false);
		}
		if (applyButton.isEnabled())
		{
			applyButton.setEnabled(false);
			applyItem.setEnabled(false);
		}
		if (resetButton.isEnabled())
		{
			resetButton.setEnabled(false);
			resetItem.setEnabled(false);
		}
		if (addButton.isEnabled())
		{
		    addButton.setEnabled(false);
			addItem.setEnabled(false);
		}
		if(delButton.isEnabled())
		{
			delButton.setEnabled(false);
			delItem.setEnabled(false);
		}
		clearButton.setEnabled(false);
		clearItem.setEnabled(false);
		table.getModel().addTableModelListener(this);
	}
	private boolean saveFile()
	{
		
		int rowCount = tableModel.getRowCount();
		int colCount = tableModel.getColumnCount();
		String[] strings = new String[colCount];
		config.clear();
		if (configFile==null)
		{
			if (fc == null)
			{
				fc = new JFileChooser();
				fc.addChoosableFileFilter(new SchedularFilter());
			}
			
        	fc.setAcceptAllFileFilterUsed(false);
			int returnVal = fc.showSaveDialog(this);
        	if (returnVal == JFileChooser.APPROVE_OPTION)
			{
         		configFile = fc.getSelectedFile();
						
			}
			else
			return false;
			
		}
		if (!saveasButton.isEnabled())
		{
				saveasButton.setEnabled(true);
				saveasItem.setEnabled(true);
		}
		
		for(int i=0;i<rowCount;i++)
		{
			for(int j =0;j<colCount;j++)
			{
				Object data = tableModel.getValueAt(i, j);
				strings[j]=(String)data;
						
			}
			SchedularConfig conf = new SchedularConfig(strings[0],strings[1],strings[2],strings[3]);
			config.add(conf);
														
		}
		
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(configFile));
			Iterator it = config.iterator();
			while (it.hasNext())
			{
				out.writeObject((SchedularConfig)it.next());
			}
			out.flush();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		
	}
	private boolean processPending()
	{
		Toolkit.getDefaultToolkit().beep();
		Object[] obj={"Yes","No"};
		int answer = JOptionPane.showOptionDialog
		(
       		SwingUtilities.getWindowAncestor(this),
       		"Do you want to load new file?",
	   		"Load New?",
       		JOptionPane.YES_NO_OPTION,
       		JOptionPane.QUESTION_MESSAGE,
       		null,
       		obj,
       		obj[0]
		);
		if (answer == JOptionPane.YES_OPTION)
		{
			if (applyButton.isEnabled())
			{
				answer = JOptionPane.showOptionDialog
				(
       				SwingUtilities.getWindowAncestor(this),
       				"Do you want to save this file?",
	  				"Save file?",
       				JOptionPane.YES_NO_OPTION,
       				JOptionPane.QUESTION_MESSAGE,
       				null,
       				obj,
       				obj[0]
				);
				if (answer == JOptionPane.YES_OPTION)
				{
					if (!saveFile())
					{
						answer = JOptionPane.showOptionDialog
						(
            				SwingUtilities.getWindowAncestor(this),
           	   				"Error Saving file...",
			   				"File save failed",
            				JOptionPane.OK_OPTION,
            				JOptionPane.ERROR_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
					}
							
				}
						
			}
			return true;
						
		}
		else
		return false;
				
	}
	public void windowClosing(WindowEvent e) {
    	
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
			if (applyButton.isEnabled())
			{
				Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Yes","No"};
				int answer = JOptionPane.showOptionDialog
				(
       			SwingUtilities.getWindowAncestor(this),
       			"Do you want to save this file before exit?",
	   			"Save before exit?",
       			JOptionPane.YES_NO_OPTION,
       			JOptionPane.QUESTION_MESSAGE,
       			null,
       			obj,
       			obj[0]
				);
				if (answer == JOptionPane.YES_OPTION)
				{
					saveFile();
				}
			}
			frame.dispose();
	}
    private void createAndShowGUI(final Point p) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame = new JFrame("Schedular Configuration");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Create and set up the content pane.
        this.setOpaque(true); //content panes must be opaque
		frame.setJMenuBar(this.createMenuBar());
        frame.setContentPane(this);
        //Display the window.
		frame.setLocation(p.x+100,p.y+100);
        frame.pack();
        frame.setVisible(true);
		
    }

    public void showConfig(final Point p) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(p);
            }
        });
    }
}
