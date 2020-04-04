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
import java.rmi.*;
public class Login extends JPanel
                      implements ActionListener,WindowListener{
    
    
    private JTextField userNameText;
    private JPasswordField passwordText;
    private JTextField securityManagerText;
    private JButton    register,logoff;
    private static JFrame frame;
    
    public Login() 
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
		securityManagerText = new JTextField("",16);
		
		fields.add(new JLabel("UserName:"));
		fields.add(userNameText);
		
		fields.add(new JLabel("Password:"));
		fields.add(passwordText);
		
		fields.add(new JLabel("Security Mgr:"));
		fields.add(securityManagerText);
		
		
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
		register = new JButton("Login");
		operation.add(register);
		register.addActionListener(this);
		register.setActionCommand("Login");
		
		logoff = new JButton("Logoff");
		operation.add(logoff);
		logoff.addActionListener(this);
		logoff.setActionCommand("Logoff");
		
		
		JButton exit = new JButton("Exit");
		exit.addActionListener(this);
		operation.add(exit);
				
		add(fields,BorderLayout.PAGE_START);
        add(operation,BorderLayout.PAGE_END);
 
        
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			securityManagerText.setText("rmi://"+inet.getHostAddress()+"/SecurityManager");
		}
		catch(Exception e)
		{
			
		}
				
    }

    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if(e.getActionCommand().equals("Login"))
	   {
	   		
	   		
	   		if(userNameText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter username before signing in..",
			   		"Registration Error !",
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
           	   		"Enter password before signing in..",
			   		"Registration Error !",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				return;
	   		}
	   			   		
	   		else if(securityManagerText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter Security manager URL before signing in..",
			   		"Registration Error !",
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
				SecurityManagerInterface security = (SecurityManagerInterface)Naming.lookup(securityManagerText.getText().trim());
				if(security!=null)
				{
					if(security.signOn(userNameText.getText().trim(),passwordText.getText().trim()))
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
        	   				SwingUtilities.getWindowAncestor(this),
           	   				"Signed In successfully..",
			   				"Login",
            				JOptionPane.OK_OPTION,
            				JOptionPane.INFORMATION_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
						
					}
					else
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
        	   				SwingUtilities.getWindowAncestor(this),
           	   				"User is already signed in, or not registered, or password is not correct..",
			   				"Login",
            				JOptionPane.OK_OPTION,
            				JOptionPane.INFORMATION_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
						System.exit(0);
						
				   }
			    }
			}
			catch(Exception ex)
			{
				Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Provide valid entries..",
			   		"Login",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				
			}
	   		
	   }
	   else if(e.getActionCommand().equals("Logoff"))
	   {
	   		
	   		
	   		if(userNameText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter username before logging off..",
			   		"Logoff Error !",
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
           	   		"Enter password before Logging off..",
			   		"Logoff Error !",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				return;
	   		}
	   			   		
	   		else if(securityManagerText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter Security manager URL before Logging off..",
			   		"Logoff Error !",
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
				SecurityManagerInterface security = (SecurityManagerInterface)Naming.lookup(securityManagerText.getText().trim());
				if(security!=null)
				{
					if(security.signOff(userNameText.getText().trim(),passwordText.getText().trim()))
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
        	   				SwingUtilities.getWindowAncestor(this),
           	   				"Signed Off successfully..",
			   				"Login",
            				JOptionPane.OK_OPTION,
            				JOptionPane.INFORMATION_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
						
					}
					else
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
        	   				SwingUtilities.getWindowAncestor(this),
           	   				"Not Signed off..",
			   				"Login",
            				JOptionPane.OK_OPTION,
            				JOptionPane.INFORMATION_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
						System.exit(0);
						
				   }
			    }
			}
			catch(Exception ex)
			{
				Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Provide valid entries..",
			   		"Login",
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
        frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //Create and set up the content pane.
        Login contentPane = new Login();
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

