import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class TabbedPaneDemo extends JPanel implements ActionListener,WindowListener{
	
	private static JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem selItem,exitItem;
	private JButton selButton,exitButton;
	static final private String SELECT  = "Sel";
	static final private String EXIT  = "Exit";
    private JTextArea processInformation;
	private JTextArea detailInformation;
	public TabbedPaneDemo() {
        
        setLayout(new BorderLayout());
			
		Dimension pos = getToolkit().getScreenSize();
		//setPreferredSize(new Dimension(pos.width/2,pos.height/3));
		JToolBar toolBar = new JToolBar("Broker");
        addButtons(toolBar);
        toolBar.setPreferredSize(new Dimension(450,45));
       	add(toolBar,BorderLayout.PAGE_START);  
       	processInformation = new JTextArea();
       	JScrollPane areaScrollPane = new JScrollPane(processInformation);
       	processInformation.setEditable(false);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(600, 500));
		detailInformation = new JTextArea();
		
		areaScrollPane = new JScrollPane(detailInformation);
       	detailInformation.setEditable(false);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(600, 500));
		
		JTabbedPane tabbedPane = new JTabbedPane();
        ImageIcon icon = createImageIcon("images/middle.gif");
        
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(areaScrollPane,BorderLayout.CENTER);
        tabbedPane.addTab("Processes", icon, panel1,
                          "Dislplays Processes Running on the System");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JPanel panel2 = new JPanel();
        tabbedPane.addTab("Details", icon, panel2,
                          "Displays System's Details");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        JPanel panel3 = new JPanel();
        tabbedPane.addTab("Performance", icon, panel3,
                          "Displays System Performance");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
        tabbedPane.setPreferredSize(new Dimension(600,500));
        tabbedPane.setEnabled(false);
        add(tabbedPane,BorderLayout.CENTER);
        
        
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    private ImageIcon createImageIcon(String path) {
        
        if (path != null) {
            return new ImageIcon(path);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    } 
    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if (SELECT.equals(e.getActionCommand()))
	   {
	   					
	   }
	   else //(EXIT.equals(e.getActionCommand()))
	   {
	  	
	   }
	   
	   	   
    }
    public void windowClosing(WindowEvent e) {
    	
		
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
		
		selItem = new JMenuItem("Save",icon);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        selItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        selItem.getAccessibleContext().setAccessibleDescription(SELECT);
		selItem.setMnemonic(KeyEvent.VK_S);
		selItem.setActionCommand(SELECT);
        selItem.addActionListener(this);
		menu.add(selItem);
		
		menu.addSeparator();
		
		icon = createImageIcon("images/exit1.gif");
        exitItem = new JMenuItem(EXIT,icon);
		exitItem.setMnemonic(KeyEvent.VK_Q);
        //menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.getAccessibleContext().setAccessibleDescription(EXIT);
        exitItem.addActionListener(this);
        menu.add(exitItem);

        menuBar.add(menu);

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
            button.setIcon(new ImageIcon(imageName, altText));
        } else {                                     //no image found
            button.setText(altText);
            System.err.println("Resource not found: "
                               + imageName);
        }

        return button;
    }
	

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI(TabbedPaneDemo pane) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("TabbedPaneDemo");
        frame.setJMenuBar(createMenuBar());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = pane;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.getContentPane().add(new TabbedPaneDemo(),
                                 BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	TabbedPaneDemo pane = new TabbedPaneDemo();
                pane.createAndShowGUI(pane);
            }
        });
    }
}
