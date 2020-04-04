import java.awt.*;
import java.util.Random;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.*;

class GraphThread extends Thread
{
	private GraphFrame frame;
	private DynamicGraph can;
	private String brokerAddress;
	private String machine;
	int value;
	public GraphThread (int val,GraphFrame d,DynamicGraph c,String broker,String address)
	{
		frame=d;
		can=c;
		brokerAddress = broker;
		value=val;
		machine=address;
	}
	public void run()
	{
		while(true)
		{
			try
			{
				
				sleep(1000);
				BrokerServerInterface in = (BrokerServerInterface)Naming.lookup(brokerAddress);
				if(in != null)
				{
					int v=0;
					if(value==2)//get cpu
					{
						v = in.getCpuPercent(machine);
					}
					else
					{
						v = in.getMemPercent(machine);
					}
					if(v!=-1)
					{
						can.list.addFirst(new Integer(v));
						can.repaint();
					}
				}
			
					
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
			
			catch(Exception e)
			{
			}
		}
	}
}
public class GraphFrame extends JPanel implements ActionListener,WindowListener{
	
	private JFrame frame;
    private String brokerAddress;
    private DynamicGraph graph;
    private String machine;
    private int value;
    private long id;
    BrokerUI ui;
    public GraphFrame(BrokerUI bui ,int val,String bAddress,String mAddress,int ornt,int dir,int type,long frameID) {
    	setLayout(new BorderLayout());
    	brokerAddress=bAddress;
    	machine=mAddress;
    	value=val;
    	id=frameID;
    	ui=bui;
    	graph = new DynamicGraph(type,100,ornt,dir,75);
    	setPreferredSize(new Dimension(200,200));
    	add(graph);
             
    }
	public void closeFrame()
	{
		frame.dispose();
	}
    public void actionPerformed(ActionEvent e)
    {
	   
	   
	   	   
    }
    public void windowClosing(WindowEvent e) {
    	
		ui.clearFrame(id);
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
    /** Returns an ImageIcon, or null if the path was invalid. */
    private  void createAndShowGUI(final Point p,String title) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //Create and set up the content pane.
        this.setOpaque(true); //content panes must be opaque
		
        frame.setContentPane(this);
		//Display the window.
		frame.setLocation(p.x+100,p.y+100);
		frame.addWindowListener(this);
        frame.pack();
        frame.setVisible(true);
        
        Thread t = new GraphThread(value,this,graph,brokerAddress,machine);
    	t.setDaemon(true);
    	t.start();
    	
    }
	public void showGraph(final Point point,final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(point,title);
            }
        });
    }
}
