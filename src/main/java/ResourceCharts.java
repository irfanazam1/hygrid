import chart2d.*;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Point;
import java.rmi.*;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JTabbedPane;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.BorderLayout;
import java.awt.Color;

public class ResourceCharts extends JPanel 
{

  private JFrame frame = null;
  private String brokerAddress;
  private int tot;
  private short graphOption;
  private JPanel pieResourceCharts,barResourceCharts,barTrendLine,barStacked,barRegion;
  private HashSet nodes;
  private Hashtable systemInfos;
  private BrokerServerInterface in;
  private int option;
  
  public ResourceCharts(String broker,HashSet set,int op)
  {
  		systemInfos = new Hashtable();
  		option = op;
  	 try
  	 {
  	 	in = (BrokerServerInterface)Naming.lookup(broker);
  	 	nodes = set;
  			 	
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
       	
		pieResourceCharts = new JPanel();
    	pieResourceCharts.setLayout(new BorderLayout());
    	getPieChart(pieResourceCharts);
    	tabs.addTab("Pie",pieResourceCharts);
    	
    	
    	barResourceCharts = new JPanel();
    	barResourceCharts.setLayout(new BorderLayout());
    	getBarChart(barResourceCharts);
    	tabs.addTab("Bars",barResourceCharts);
    	
    	barStacked = new JPanel();
    	barStacked.setLayout(new BorderLayout());
    	getStackedBarChart(barStacked);
    	tabs.addTab("Stacked Bars",barStacked);
    	    	
    	barRegion = new JPanel();
    	barRegion.setLayout(new BorderLayout());
    	getBarChartRegions(barRegion);
    	tabs.addTab("Bars with Regions",barRegion);
    	
    	
    	add(tabs);
    	
    	        
  }

 
  public void destroy() {

    if (frame != null) frame.dispose();
    
  }
  private void getPieChart(JPanel panel)
  {
  		
  		long total=0;
    	Object2DProperties object2DProps = new Object2DProperties();
    	
    	if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Resources - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Resources - (K)");	
  	 	}
  	  	else if(option == 3) //Physical Memory
  	  	{
  	  		object2DProps.setObjectTitleText ("Physical Memory Resources - (K)");
  	  	}
  	  	//Configure chart properties
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);
   		//Configure legend properties
   		LegendProperties legendProps = new LegendProperties();
   		String[] legendLabels = new String[nodes.size()];
   		Iterator it = nodes.iterator();
   		int index=0;
   		while(it.hasNext())
   		{
   			String address  = (String)it.next();
   			legendLabels[index++] = address;
   		}
   			
   		legendProps.setLegendLabelsTexts (legendLabels);
		Dataset dataset = new Dataset (nodes.size(),1, 1);
		index=0;
		Enumeration enumeration = systemInfos.keys();
    	while(enumeration.hasMoreElements())
   		{
  			SystemInformation sysInfo;
   			String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
  	 		}
  	 		dataset.set (index++,0, 0,total);
    	  		 	    
         }
    
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
    	
		panel.add(chart2D);
      
  }
  private void getBarChart(JPanel panel) //comparison
  {
	   	
	   	long total=0,used=0,free=0;
	   	Object2DProperties object2DProps = new Object2DProperties();
    	object2DProps.setObjectTitleText ("Disk Usage");

    
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);

    
    	LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = new String[nodes.size()];
    	Iterator it = nodes.iterator();
   		int index=0;
   		while(it.hasNext())
   		{
   			String address  = (String)it.next();
   			legendLabels[index++] = address;
   		}
    	
    	legendProps.setLegendLabelsTexts (legendLabels);
	   	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    	
    	if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Resources - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Resources - (K)");	
  	 	}
  	 	else if(option == 3) //Physical Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Physical Memory Resources - (K)");
  	 	}
  	 	
    	graphChart2DProps.setLabelsAxisTitleText ("Resources");
    	graphChart2DProps.setNumbersAxisTitleText ("Size");
    	graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    	graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    	graphChart2DProps.setChartDatasetCustomLeastValue (0);

    	//Configure graph properties
   		GraphProperties graphProps = new GraphProperties();

		Enumeration enumeration  = systemInfos.keys();
		Dataset dataset = new Dataset (nodes.size(),1,1);
		index=0;
		long max=0;
  		while(enumeration.hasMoreElements())
    	{
    		  		
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
    			
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
    		}
  	 		
  	 		max+=total;
    		dataset.set (index++,0,0,total);
    	}
    	//graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
  	    graphChart2DProps.setChartDatasetCustomGreatestValue(max);
        MultiColorsProperties multiColorsProps = new MultiColorsProperties();
     	LBChart2D chart2D = new LBChart2D();
     	chart2D.setObject2DProperties (object2DProps);
   		chart2D.setChart2DProperties (chart2DProps);
    	chart2D.setLegendProperties (legendProps);
    	chart2D.setGraphChart2DProperties (graphChart2DProps);
    	chart2D.addGraphProperties (graphProps);
    	chart2D.addDataset (dataset);
    	chart2D.addMultiColorsProperties (multiColorsProps);
   		
		panel.add(chart2D);    

    
  }
  private void getBarChartRegions(JPanel panel) //comparison
  {
	   	
	   	long total=0,used=0,free=0;
	   	Object2DProperties object2DProps = new Object2DProperties();
		
		LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = new String[nodes.size()];
    	Iterator it = nodes.iterator();
   		int index=0;
   		while(it.hasNext())
   		{
   			String address  = (String)it.next();
   			legendLabels[index++] = address;
   		}
    	
    	legendProps.setLegendLabelsTexts (legendLabels);
	   	    	
    
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);
 		
 		if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Resources - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Resources - (K)");	
  	 	}
  	 	else if(option == 3) //Physical Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Physical Memory Resources - (K)");
  	 	}   
    	
    	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    	
    	index=0;
    	    	
    	graphChart2DProps.setLabelsAxisTitleText ("Resources");
    	graphChart2DProps.setNumbersAxisTitleText ("Size");
    	graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    	graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    	graphChart2DProps.setChartDatasetCustomLeastValue (0);


    	//Configure graph properties
   		GraphProperties graphProps = new GraphProperties();
    	graphProps.setGraphOutlineComponentsExistence (true);
		Enumeration enumeration  = systemInfos.keys();
		index=0;
		long max=0;
		Dataset dataset = new Dataset (nodes.size(),1,1);
  		while(enumeration.hasMoreElements())
    	{
    		
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
    			
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
    			
  	 		}
  	 		max+=total;
    		
  	 	    dataset.set (index++,0,0, total);
    		
         }
  
  		graphChart2DProps.setChartDatasetCustomGreatestValue (max); 
    	MultiColorsProperties multiColorsProps = new MultiColorsProperties();
    	
    	WarningRegionProperties warningRegionProps1 = new WarningRegionProperties();
    	warningRegionProps1.setHigh (WarningRegionProperties.HIGH);
    	warningRegionProps1.setLow (max-max/3);

    //Configure warning regions for graph
    	WarningRegionProperties warningRegionProps2 = new WarningRegionProperties();
    	warningRegionProps2.setHigh (max-max/3);
    	warningRegionProps2.setLow (max/3);
    	warningRegionProps2.setComponentColor (new Color (146, 105, 0));
    	warningRegionProps2.setBackgroundColor (new Color (222, 209, 176));


      	LBChart2D chart2D = new LBChart2D();
    	chart2D.setObject2DProperties (object2DProps);
    	chart2D.setChart2DProperties (chart2DProps);
    	chart2D.setLegendProperties (legendProps);
    	chart2D.setGraphChart2DProperties (graphChart2DProps);
    	chart2D.addGraphProperties (graphProps);
    	chart2D.addDataset (dataset);
    	chart2D.addMultiColorsProperties (multiColorsProps);
    	chart2D.addWarningRegionProperties (warningRegionProps1);
    	chart2D.addWarningRegionProperties (warningRegionProps2);
		panel.add(chart2D);    

    
  }
  private void getStackedBarChart(JPanel panel)
  {
  		
  		
  		long total=0,used=0,free=0;
    	
    	Object2DProperties object2DProps = new Object2DProperties();
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);

    		//Configure legend properties
    	LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = new String[nodes.size()];
    	Iterator it = nodes.iterator();
   		int index=0;
   		while(it.hasNext())
   		{
   			String address  = (String)it.next();
   			legendLabels[index++] = address;
   		}
    	
    	legendProps.setLegendLabelsTexts (legendLabels);
	   	
	   	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    	
    	if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Resources - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Resources - (K)");	
  	 	}
  	 	else if(option == 3) //Physical Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Physical Memory Resources - (K)");
  	 	}
		graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    	graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    	graphChart2DProps.setChartDatasetCustomLeastValue (0);
    	graphChart2DProps.setLabelsAxisTitleText ("Resources");
    		
    		//Configure graph properties
    	GraphProperties graphProps = new GraphProperties();
    	graphProps.setGraphAllowComponentAlignment (true);
    	graphProps.setGraphBarsRoundingRatio (0f);
   		graphProps.setGraphOutlineComponentsExistence (true);
   			
       	
    	
    	Enumeration enumeration  = systemInfos.keys();
		Dataset dataset = new Dataset (nodes.size(),1, 1);
		index=0;
		long max=0;
  		while(enumeration.hasMoreElements())
    	{
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
    			
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
    			
  	 		}
  	 		max+=total;
    		dataset.set (index++,0,0, total);
      		
         }
    	 		
 		dataset.doConvertToStacked();
		graphChart2DProps.setChartDatasetCustomGreatestValue(max);
		MultiColorsProperties multiColorsProps = new MultiColorsProperties();
    	LBChart2D chart2D = new LBChart2D();
    	chart2D.setObject2DProperties (object2DProps);
    	chart2D.setChart2DProperties (chart2DProps);
    	chart2D.setLegendProperties (legendProps);
    	chart2D.setGraphChart2DProperties (graphChart2DProps);
    	chart2D.addGraphProperties (graphProps);
    	chart2D.addDataset (dataset);
		chart2D.addMultiColorsProperties (multiColorsProps);

		panel.add(chart2D);
    
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
        switch(option)
    	{
    		case 1:
    			frame.setTitle("Disk  Resources Charts");
    		break;
    		case 2:
    			frame.setTitle("Virtual Memory Resources Charts");
    		break;
    		case 3:
    			frame.setTitle("Physical Memory Resources Charts");
    		break;
    		
    		
    	}
		
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