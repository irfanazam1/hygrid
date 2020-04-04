import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
public class ShowLog extends JPanel
                      implements ActionListener{
    
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem loadItem,exitItem;
	private JButton exitButton,loadButton;
	private JTable table;
	private DefaultTableModel tableModel;
	private JFrame frame;
    
	static final private String EXIT  = "Quit";
	static final private String LOAD  = "Load";
	private boolean isLoaded;
	private JFileChooser fc;
	
	public ShowLog() {
		
		isLoaded=false;
		
		setLayout(new BorderLayout());
			
		Dimension pos = getToolkit().getScreenSize();
		setPreferredSize(new Dimension(800,400));
	
        JToolBar toolBar = new JToolBar("Log");
        
		addButtons(toolBar);
        toolBar.setPreferredSize(new Dimension(pos.width/4,45));
       	tableModel = new DefaultTableModel();
		
		tableModel.addColumn("ID");
		tableModel.addColumn("Source");
		tableModel.addColumn("Destination");
		tableModel.addColumn("Submission");
		tableModel.addColumn("Start");
		tableModel.addColumn("Finish");
		tableModel.addColumn("Status");
		tableModel.addColumn("Type");
		tableModel.addColumn("Handle");
		tableModel.addColumn("Delivery");
		table = new JTable(tableModel);
		table.setCellSelectionEnabled(false);
		
		
		JScrollPane tableView = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		
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
		
		add(toolBar,BorderLayout.PAGE_START);  
		add(tableView,BorderLayout.CENTER);
		
	}

    /** Required by TreeSelectionListener interface. */
    
	private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ShowLog.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
    
    public void actionPerformed(ActionEvent e)
    {
	   if (EXIT.equals(e.getActionCommand()))
	   {
	   		
			frame.dispose();
	   }
	   
	   else if (LOAD.equals(e.getActionCommand()))
	   {
	   		
			if (fc==null)
			{
				fc = new JFileChooser();
			}
			
			if (isLoaded)
			{
				
				Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Yes","No"};
				int answer = JOptionPane.showOptionDialog
				(
            		SwingUtilities.getWindowAncestor(this),
           	   		"A file is already loaded\n Do you really want to load a new file ?",
			   		"Open Log?",
            		JOptionPane.YES_NO_OPTION,
            		JOptionPane.QUESTION_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
			
				if (answer == JOptionPane.YES_OPTION)
				{
					clearTable();
				}
				else
				{
					
					return;
				}
			}
			
			fc.addChoosableFileFilter(new LogFilter());
            fc.setAcceptAllFileFilterUsed(false);
			int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				
			 	File file = fc.getSelectedFile(); 
				populateTable(file);
				isLoaded=true;
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
        ImageIcon icon = createImageIcon("images/load1.gif");
		
		loadItem = new JMenuItem("Load File",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        loadItem.getAccessibleContext().setAccessibleDescription("Load");
		loadItem.setMnemonic(KeyEvent.VK_L);
		loadItem.setActionCommand("Load");
        loadItem.addActionListener(this);
		menu.add(loadItem);
		
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
	private void clearTable()
	{
			
		int rows = tableModel.getRowCount();
		for(int i=rows-1;i>=0;i--)
		{
			tableModel.removeRow(i);
		}
		
	}
	private void populateTable(File file)
	{
		ObjectInputStream in=null;
		
		try
		{
			in = new ObjectInputStream(new FileInputStream(file));
			
			Vector row = new Vector();
			do
			{
				row = (Vector)in.readObject();
				tableModel.addRow(row);
				
			}while(true);
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
		catch(ClassNotFoundException ce)
		{
		}
	}
	private void createAndShowGUI(final Point p) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame = new JFrame("View Logs");
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
