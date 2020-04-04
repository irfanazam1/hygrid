import chart2d.*;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Point;
import java.rmi.*;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.BorderLayout;
public class DiskCharts extends JPanel {

  private JFrame frame = null;
  private String brokerAddress;
  private int tot;
  private JPanel pieCharts,barCharts,barTrendLines,barOverlay,barStacked;
  HashSet nodes;
  Hashtable systemInfos;
  BrokerServerInterface in;
  public DiskCharts(String broker)
  {
  		systemInfos = new Hashtable();
  	 try
  	 {
  	 	in = (BrokerServerInterface)Naming.lookup(broker);
  	 	if (in != null)
  	 	{
  	 		nodes = in.getNodes();
  	 		//setLayout(new GridLayout(nodes.size(),2));
  	 		
  	 	}
  	 	
  	 	Iterator it = nodes.iterator();
    	while(it.hasNext())
    	{
    		//Thread.sleep(2000);
    		SystemInformation sysInfo;
    		String address = (String)it.next();
    		sysInfo = in.getSystemInfo(address);
    		systemInfos.put(address,sysInfo);
    	 }
  	 }
  	 catch(NotBoundException nbe)
  	 {destroy();}
  	 catch(java.net.MalformedURLException mfe)
  	 {destroy();}
  	 catch(RemoteException re)
  	 {destroy();}
  	 init();
  }
  private void init() 
  {
    
    	
    	setPreferredSize(new Dimension(600,600));
      	setLayout(new BorderLayout());
    	JTabbedPane tabs = new JTabbedPane();
       	
		pieCharts = new JPanel();
    	pieCharts.setLayout(new GridLayout(nodes.size(),2));
    	getPieChart(pieCharts);
    	tabs.addTab("Pie",pieCharts);
    	
    	
    	barCharts = new JPanel();
    	barCharts.setLayout(new BorderLayout());
    	getBarChart(barCharts);
    	tabs.addTab("Bars",barCharts);
    	
    	barStacked = new JPanel();
    	barStacked.setLayout(new GridLayout(nodes.size(),2));
    	getStackedBarChart(barStacked);
    	tabs.addTab("Stacked Bars",barStacked);
    	
    	add(tabs);
    	
    	        
  }

 
  public void destroy() {

    if (frame != null) frame.dispose();
    
  }
  private void getPieChart(JPanel panel)
  {
  		Enumeration enumeration  = systemInfos.keys();
  		Iterator it = nodes.iterator();
    	while(enumeration.hasMoreElements())
    	{
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
  	 	 	
    		Object2DProperties object2DProps = new Object2DProperties();
    		object2DProps.setObjectTitleText (address+" "+sysInfo.folderName+" "+sysInfo.totalDisk+"(MB)");

    		//Configure chart properties
    		Chart2DProperties chart2DProps = new Chart2DProperties();
    		chart2DProps.setChartDataLabelsPrecision (0);

    		//Configure legend properties
    		LegendProperties legendProps = new LegendProperties();
    		String[] legendLabels =
      		{"Used(MB)", "Free(MB)"};// "LLChart2D", "GraphChart2D", "Chart2D", "Object2D"};
    		legendProps.setLegendLabelsTexts (legendLabels);

    		//Configure dataset
    		int numSets = 2, numCats = 1, numItems = 1;
    		Dataset dataset = new Dataset (numSets, numCats, numItems);
    		dataset.set (0, 0, 0, sysInfo.usedDisk);
    		dataset.set (1, 0, 0, sysInfo.freeDisk);
    	
    		//Configure graph component colors
    		MultiColorsProperties multiColorsProps = new MultiColorsProperties();
			
			//Configure pie area
   		 	PieChart2DProperties pieChart2DProps = new PieChart2DProperties();

   		 	//Configure chart
    		PieChart2D chart2D = new PieChart2D();
    		chart2D.setObject2DProperties (object2DProps);
    		chart2D.setChart2DProperties (chart2DProps);
   			chart2D.setLegendProperties (legendProps);
   			chart2D.setDataset (dataset);
    		chart2D.setMultiColorsProperties (multiColorsProps);
    		chart2D.setPieChart2DProperties (pieChart2DProps);

    		if (!chart2D.validate (false)) chart2D.validate (true);
			panel.add(chart2D);
      }
  }
  private void getBarChart(JPanel panel) //comparison
  {
	   	Object2DProperties object2DProps = new Object2DProperties();
    	object2DProps.setObjectTitleText ("Disk Usage");

    
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);

    
    	LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = {"Used(MB)", "Free(MB)"};
    	legendProps.setLegendLabelsTexts (legendLabels);

	   	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    	String[] labelsAxisLabels = new String[nodes.size()];
    	Iterator it = nodes.iterator();
    	int index=0;
    	/*while(it.hasNext())
   	 	{
    		String label=(String)it.next();
    		labelsAxisLabels[index++]=label+" "+sysInfo.totalDisk+"(MB)";
    	}*/
    
    	graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
    	graphChart2DProps.setLabelsAxisTitleText ("Grid Machines");
    	graphChart2DProps.setNumbersAxisTitleText ("Disk Usage");

    	//Configure graph properties
   		GraphProperties graphProps = new GraphProperties();

		Enumeration enumeration  = systemInfos.keys();
		Dataset dataset = new Dataset (2, nodes.size(), 1);
		index=0;
		int cat=0;
		int labelIndex=0;
  		while(enumeration.hasMoreElements())
    	{
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		labelsAxisLabels[labelIndex++]=sysInfo.address+" "+sysInfo.totalDisk+"(MB)";
  	 	    dataset.set (index,cat,0, sysInfo.usedDisk);
    		dataset.set (++index,cat++,0, sysInfo.freeDisk);
    		index=0;
    		
         }
  
   
    	MultiColorsProperties multiColorsProps = new MultiColorsProperties();

   
    	LBChart2D chart2D = new LBChart2D();
    	chart2D.setObject2DProperties (object2DProps);
   		chart2D.setChart2DProperties (chart2DProps);
    	chart2D.setLegendProperties (legendProps);
    	chart2D.setGraphChart2DProperties (graphChart2DProps);
    	chart2D.addGraphProperties (graphProps);
    	chart2D.addDataset (dataset);
    	chart2D.addMultiColorsProperties (multiColorsProps);
   	
   		 if (!chart2D.validate (false)) chart2D.validate (true);
		panel.add(chart2D);    

    
  }
  private void getStackedBarChart(JPanel panel)
  {
  		Enumeration enumeration  = systemInfos.keys();
  		Iterator it = nodes.iterator();
    	while(enumeration.hasMoreElements())
    	{
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
  	 	 	
    		Object2DProperties object2DProps = new Object2DProperties();
    		object2DProps.setObjectTitleText (address+" "+sysInfo.folderName);

    		//Configure chart properties
    		Chart2DProperties chart2DProps = new Chart2DProperties();
    		chart2DProps.setChartDataLabelsPrecision (0);

    		//Configure legend properties
    		LegendProperties legendProps = new LegendProperties();
    		String[] legendLabels =
      		{"Used(MB)", "Free(MB)"};// "LLChart2D", "GraphChart2D", "Chart2D", "Object2D"};
    		legendProps.setLegendLabelsTexts (legendLabels);

    		//Configure dataset
    		GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    		String[] labelsAxisLabels = {address};
   		    graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
    		graphChart2DProps.setNumbersAxisTitleText ("Machine");
    		graphChart2DProps.setLabelsAxisTitleText ("Size: "+sysInfo.totalDisk);
    		//Configure graph properties
    		GraphProperties graphProps = new GraphProperties();
    		graphProps.setGraphAllowComponentAlignment (true);
    		graphProps.setGraphBarsRoundingRatio (0f);
   			graphProps.setGraphOutlineComponentsExistence (true);

    		int numSets = 2, numCats = 1, numItems = 1;
    		Dataset dataset = new Dataset (numSets, numCats, numItems);
    		dataset.set (0, 0, 0, sysInfo.usedDisk);
    		dataset.set (1, 0, 0, sysInfo.freeDisk);
 			dataset.doConvertToStacked();

    //Configure graph component colors
   			 MultiColorsProperties multiColorsProps = new MultiColorsProperties();

    //Configure chart
   			 LBChart2D chart2D = new LBChart2D();
    		chart2D.setObject2DProperties (object2DProps);
    		chart2D.setChart2DProperties (chart2DProps);
    		chart2D.setLegendProperties (legendProps);
    		chart2D.setGraphChart2DProperties (graphChart2DProps);
    		chart2D.addGraphProperties (graphProps);
    		chart2D.addDataset (dataset);
    		chart2D.addMultiColorsProperties (multiColorsProps);

    		if (!chart2D.validate (false)) chart2D.validate (true);
			panel.add(chart2D);
      }
  }	

  private void createAndShowGUI(final Point p) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
       
        frame = new JFrame("Disk Information");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //Create and set up the content pane.
        this.setOpaque(true); //content panes must be opaque
		//frame.setJMenuBar(this.createMenuBar());
        frame.setContentPane(this);
        //Display the window.
		frame.setLocation(p.x+100,p.y+100);
        frame.pack();
        frame.setVisible(true);
		
    }

    public void showCharts(final Point p) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(p);
            }
        });
    }
}