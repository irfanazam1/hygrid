import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JTree;
import java.io.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import java.net.InetAddress;

public class DatabaseConfig extends JPanel
                      implements ActionListener,WindowListener{
    
    
    private JTextField userNameText;
    private JPasswordField passwordText;
    private JTextField databaseServerText;
    private JButton    save,exit;
    private static JFrame frame;
    
    public DatabaseConfig() 
    {
		
		setLayout(new BorderLayout());
		
		Border 	raisedbevel, loweredbevel;
		raisedbevel = BorderFactory.createRaisedBevelBorder();
		loweredbevel = BorderFactory.createLoweredBevelBorder();

        
		/*Creating fields panel*/
		JPanel fields = new JPanel();
		/*Creating fields for panel*/
		fields.setLayout(new SpringLayout());
		
		userNameText = new JTextField("",10);
		passwordText = new JPasswordField("",10);
		databaseServerText = new JTextField("",16);
		
		fields.add(new JLabel("UserName:"));
		fields.add(userNameText);
		
		fields.add(new JLabel("Password:"));
		fields.add(passwordText);
		
		fields.add(new JLabel("Database Server:"));
		fields.add(databaseServerText);
		
		
		Border compound;
		compound = BorderFactory.createCompoundBorder(
			  loweredbevel, loweredbevel);
        
        fields.setBorder(compound);
		
		SpringUtilities.makeCompactGrid(fields,
                                        3,2, //rows, cols
                                        10, 10,        //initX, initY
                                        20, 20);       //xPad, yPad
		          
		JPanel operation = new JPanel();
		operation.setLayout(new GridLayout(1,3));
		
		save = new JButton("Save");
		operation.add(save);
		save.addActionListener(this);
		save.setActionCommand("Save");
		
		exit = new JButton("Exit");
		operation.add(exit);
		exit.addActionListener(this);
		exit.setActionCommand("Exit");
		
		
				
		add(fields,BorderLayout.PAGE_START);
        add(operation,BorderLayout.PAGE_END);
        try
		{
			InetAddress inet = InetAddress.getLocalHost();
			databaseServerText.setText(inet.getHostAddress());
		}
		catch(Exception e)
		{
			
		}
				
    }

    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if(e.getActionCommand().equals("Save"))
	   {
	   		
	   		
	   		if(userNameText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter username before saving..",
			   		"Save Error !",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				return;
	   		}
	   		else if(passwordText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter password before saving..",
			   		"Save Error !",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				return;
	   		}
	   		else if(databaseServerText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter Database Server Address. before saving.",
			   		"Save Error !",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				return;
	   		}
	   		
	   		try
			{
				//TODO: Save to File goes here
				DatabaseConfiguration conf = new DatabaseConfiguration
				(
					userNameText.getText().trim(),
					passwordText.getText().trim(),
					databaseServerText.getText().trim()
				);
				if(conf.saveConfiguration("dataconf.conf"))
				{
					Toolkit.getDefaultToolkit().beep();
					Object[] obj={"Ok"};
					int answer = JOptionPane.showOptionDialog
					(
        	   			SwingUtilities.getWindowAncestor(this),
           	   			"Configurations saved ..",
			   			"Save Successdul!",
            			JOptionPane.OK_OPTION,
	            		JOptionPane.INFORMATION_MESSAGE,
    	        		null,
        	    		obj,
            			obj[0]
					);
				}
				
			}
			catch(Exception ex)
			{
				System.out.println(ex.toString());
				Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Provide valid entries..",
			   		"Database Configuration..",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				
			}
	   		
	   }
	   else
	   {
	   		frame.dispose();
	   }
					
    }
	
    private ImageIcon createImageIcon(String path) {
        
        java.net.URL imgURL = Login.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    public void windowClosing(WindowEvent e) 
	{
    	
		frame.dispose();
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
    
    public static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        frame = new JFrame("Database Configuration");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //Create and set up the content pane.
        DatabaseConfig contentPane = new DatabaseConfig();
        contentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(contentPane);
		//Display the window.
		
		frame.pack();
		Rectangle rect = frame.getBounds();
		Dimension dim = contentPane.getToolkit().getScreenSize();
		frame.setLocation((dim.width-rect.width)/2,(dim.height-rect.height)/2);
		frame.setVisible(true);
        frame.setResizable(false);
    }
	
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
}

