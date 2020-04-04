import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.rmi.*;
import java.util.HashSet;
import java.util.Iterator;

public class SelectMachine extends JPanel
                      implements ListSelectionListener,ActionListener {
    private JList list;
    private DefaultListModel listModel;
    private JButton okButton,selAllButton;;
    private JFrame frame;
    private BrokerUI broker;
    private int option;
    private HashSet set;
    public SelectMachine(BrokerUI b,String URL,int sel) {
        super(new BorderLayout());
		option=sel;
		broker = b;
        try
        {
        	
        	BrokerServerInterface in = (BrokerServerInterface)Naming.lookup(URL);
        	set = in.getNodes();
        	Iterator it = set.iterator();
        	listModel = new DefaultListModel();
        	while(it.hasNext())
        	{
        		listModel.addElement((String)it.next());
        	}
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        list = new JList(listModel);
        if(sel == 1)
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        else
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);

        okButton = new JButton("ok");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        
        selAllButton = new JButton("Select All");
        selAllButton.setActionCommand("Select All");
        selAllButton.addActionListener(this);
        
        if(sel==1)
        selAllButton.setEnabled(false);
		
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        buttonPane.add(okButton);
        buttonPane.add(selAllButton);
        JButton close = new JButton("Close");
        close.setActionCommand("Close");
        close.addActionListener(this);
        buttonPane.add(close);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e)
    {
    	if("ok".equals(e.getActionCommand()))
    	{
    		if(option==1)
    		{
    			broker.setAddress((String)list.getSelectedValue());
    		}
    		else
    		{
    			Object[] values = list.getSelectedValues();
    			HashSet set = new HashSet();
    			for(int i=0;i<values.length;i++)
    			set.add(values[i]);
    			broker.showCharts(set);
    		
    		}
    		broker=null;
    		frame.dispose();
    	}
    	else if("Select All".equals(e.getActionCommand()))
    	{
    		int[] indices = new int[set.size()];
    	 	for(int i=0;i<set.size();i++)
    	 	{
    	 		indices[i]=i;
    	 	}
    	 	list.setSelectedIndices(indices);
    	}
    	else
    	{
    		frame.dispose();
    	}
    }
    //This method is required by ListSelectionListener.
    
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) 
        {

            
        }
    }
    private void createAndShowGUI(final Point p) {
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame = new JFrame("Select Machine");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Create and set up the content pane.
        this.setOpaque(true); //content panes must be opaque
		frame.setContentPane(this);
        //Display the window.
		frame.setLocation(p.x+100,p.y+100);
        frame.pack();
        frame.setVisible(true);
    }

    public void showSelect(final Point p) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(p);
            }
        });
    }
}
