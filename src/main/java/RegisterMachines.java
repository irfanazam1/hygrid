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
public class RegisterMachines extends JPanel
                      implements ActionListener{
    
    
    private JTextField machineNameText;
    private JTextField securityManagerText;
    private JTextField orgText;
    private JCheckBox  requestComp;
    private JCheckBox  requestStor;
    private JButton    register;
    
    public RegisterMachines() 
    {
		
		setLayout(new BorderLayout());
		
		Border 	raisedbevel, loweredbevel;
		raisedbevel = BorderFactory.createRaisedBevelBorder();
		loweredbevel = BorderFactory.createLoweredBevelBorder();


		/*Creating fields panel*/
		JPanel fields = new JPanel();
		/*Creating fields for panel*/
		fields.setLayout(new SpringLayout());
		machineNameText = new JTextField("",10);
		securityManagerText = new JTextField("",16);
		orgText = new JTextField("",14);
		requestComp = new JCheckBox();
		requestStor = new JCheckBox();
		requestComp.setSelected(true);
		requestStor.setSelected(true);
		
		fields.add(new JLabel("Machine:"));
		fields.add(machineNameText);
		
		fields.add(new JLabel("Security Mgr:"));
		fields.add(securityManagerText);
		
		fields.add(new JLabel("Organization:"));
		fields.add(orgText);
		
		fields.add(new JLabel("Request Computation:"));
		fields.add(requestComp);
		
		fields.add(new JLabel("Request Storage"));
		fields.add(requestStor);
		
		Border compound;
		compound = BorderFactory.createCompoundBorder(
			  loweredbevel, loweredbevel);
        
        fields.setBorder(compound);
		
		SpringUtilities.makeCompactGrid(fields,
                                        5,2, //rows, cols
                                        10, 10,        //initX, initY
                                        20, 20);       //xPad, yPad
		          
		JPanel operation = new JPanel();
		operation.setLayout(new GridLayout(1,3));
		register = new JButton("Register");
		operation.add(register);
		register.addActionListener(this);
		register.setActionCommand("Reg");
		JButton exit = new JButton("Exit");
		JButton unreg = new JButton("UnRegister");
		exit.addActionListener(this);
		unreg.addActionListener(this);
		operation.add(unreg);
		operation.add(exit);
				
		add(fields,BorderLayout.PAGE_START);
        add(operation,BorderLayout.PAGE_END);
        machineNameText.setEditable(false);
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			machineNameText.setText(inet.getHostAddress());
			securityManagerText.setText("rmi://"+inet.getHostAddress()+"/SecurityManager");
		}
		catch(Exception e)
		{
			
		}
				
    }

    
    public void actionPerformed(ActionEvent e)
    {
	   
	   if(e.getActionCommand().equals("Reg"))
	   {
	   		if(securityManagerText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter Security manager URL before registration..",
			   		"Registration Error !",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
				return;
	   		}
	   		else if(orgText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter Organization name before registration..",
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
					if(security.registerMachine(machineNameText.getText().trim(),orgText.getText().trim(),requestComp.isSelected(),requestStor.isSelected()))
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
        	   				SwingUtilities.getWindowAncestor(this),
           	   				"Registration pending\nRequest posted successfully",
			   				"Registration!",
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
           	   				"Machine is already registered, or request posted..",
			   				"Registration!",
            				JOptionPane.OK_OPTION,
            				JOptionPane.INFORMATION_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
						
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
			   		"Registration!",
            		JOptionPane.OK_OPTION,
            		JOptionPane.ERROR_MESSAGE,
            		null,
            		obj,
            		obj[0]
				);
			}
	   		
	   }
	   else if(e.getActionCommand().equals("UnRegister"))
	   {
	   		if(securityManagerText.getText().trim().equals(""))
	   		{
	   			Toolkit.getDefaultToolkit().beep();
				Object[] obj={"Ok"};
				int answer = JOptionPane.showOptionDialog
				(
        	   		SwingUtilities.getWindowAncestor(this),
           	   		"Enter Security manager URL before unregister..",
			   		"UnRegister Error !",
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
					if(security.unregisterMachine(machineNameText.getText().trim()))
					{
						Toolkit.getDefaultToolkit().beep();
						Object[] obj={"Ok"};
						int answer = JOptionPane.showOptionDialog
						(
        	   				SwingUtilities.getWindowAncestor(this),
           	   				"Registration cancelled successfully",
			   				"UnRegister!",
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
           	   				"Machine is not registered ..",
			   				"UnRegister!",
            				JOptionPane.OK_OPTION,
            				JOptionPane.INFORMATION_MESSAGE,
            				null,
            				obj,
            				obj[0]
						);
						
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
			   		"Registration!",
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
	   		System.exit(0);
	   }
					
    }
	
       
	private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        JFrame frame = new JFrame("Register Machine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create and set up the content pane.
        RegisterMachines contentPane = new RegisterMachines();
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

