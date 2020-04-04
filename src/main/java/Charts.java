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
import java.awt.Color;
public class Charts extends JPanel {

  private JFrame frame = null;
  private String brokerAddress;
  private int tot;
  private short graphOption;
  private JPanel pieCharts,barCharts,barTrendLine,barStacked,barRegion;
  private HashSet nodes;
  private Hashtable systemInfos;
  private BrokerServerInterface in;
  private int option;
  
  public Charts(String broker,HashSet set,int op)
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
    	
    	barTrendLine = new JPanel();
    	barTrendLine.setLayout(new BorderLayout());
    	getBarChartTrendLine(barTrendLine);
    	tabs.addTab("Bars with TrendLine",barTrendLine);
    	
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
  		Enumeration enumeration  = systemInfos.keys();
  		Iterator it = nodes.iterator();
  		
  		long total,used,free;
    	while(enumeration.hasMoreElements())
    	{
    		
    		
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		Object2DProperties object2DProps = new Object2DProperties();
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			used  = sysInfo.usedDisk;
    			free  = sysInfo.freeDisk;
    			object2DProps.setObjectTitleText (address+" "+sysInfo.folderName+" "+total+" - (MB)");
    		}
  	 	 	else if(option==2) //Virtual Memory
  	 	 	{
  	 	 		total = sysInfo.virtualMem;
    			used  = sysInfo.usedVirtualMem;
    			free  = sysInfo.freeVirtualMem;
    			object2DProps.setObjectTitleText (address+" "+total+" - (K)");	
  	 	 	}
  	 	 	else if(option == 3) //Physical Memory
  	 	 	{
  	 	 		total = sysInfo.physicalMem;
    			used  = sysInfo.usedPhysicalMem;
    			free  = sysInfo.freePhysicalMem;
    			object2DProps.setObjectTitleText (address+" "+total+" - (K)");
  	 	 	}
  	 	 	else //CPU
  	 	 	{
  	 	 		used  = sysInfo.cpu;
    			free  = 100-sysInfo.cpu;
    			object2DProps.setObjectTitleText (address);
  	 	 	}
  	 	 	//Configure chart properties
    		Chart2DProperties chart2DProps = new Chart2DProperties();
    		chart2DProps.setChartDataLabelsPrecision (0);

    		//Configure legend properties
    		LegendProperties legendProps = new LegendProperties();
    		String[] legendLabels =
      		{"Used", "Free"};
    		legendProps.setLegendLabelsTexts (legendLabels);

    		//Configure dataset
    		int numSets = 2, numCats = 1, numItems = 1;
    		Dataset dataset = new Dataset (numSets, numCats, numItems);
    		dataset.set (0, 0, 0, used);
    		dataset.set (1, 0, 0, free);
    	
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
  }
  private void getBarChart(JPanel panel) //comparison
  {
	   	
	   	long total=0,used=0,free=0;
	   	Object2DProperties object2DProps = new Object2DProperties();
    	object2DProps.setObjectTitleText ("Disk Usage");

    
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);

    
    	LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = {"Used", "Free"};
    	legendProps.setLegendLabelsTexts (legendLabels);

	   	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    	String[] labelsAxisLabels = new String[nodes.size()];
    	
    	int index=0;
    	
    	//Object2DProperties object2DProps = new Object2DProperties();
    	if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Usage - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Usage - (K)");	
  	 	}
  	 	else if(option == 3) //Physical Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Physical Memory Usage - (K)");
  	 	}
  	 	else //CPU
  	 	{
  	 		object2DProps.setObjectTitleText ("CPU Usage - (%)");
  	 	}
    	graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
    	graphChart2DProps.setLabelsAxisTitleText ("Grid Machines");
    	graphChart2DProps.setNumbersAxisTitleText (" ");
    	graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    	graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    	graphChart2DProps.setChartDatasetCustomLeastValue (0);

    	//Configure graph properties
   		GraphProperties graphProps = new GraphProperties();

		Enumeration enumeration  = systemInfos.keys();
		Dataset dataset = new Dataset (2, nodes.size(), 1);
		index=0;
		int cat=0;
		int labelIndex=0;
		long max=0;
  		while(enumeration.hasMoreElements())
    	{
    		
    		
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			used  = sysInfo.usedDisk;
    			free  = sysInfo.freeDisk;
    			object2DProps.setObjectTitleText ("Disk Usage - (MB)");
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
    			used  = sysInfo.usedVirtualMem;
    			free  = sysInfo.freeVirtualMem;
    			object2DProps.setObjectTitleText ("Virtual Memory Usage - (K)");	
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
    			used  = sysInfo.usedPhysicalMem;
    			free  = sysInfo.freePhysicalMem;
    			object2DProps.setObjectTitleText ("Physical Memory Usage - (K)");
  	 		}
  	 		else //CPU
  	 		{
  	 			used  = sysInfo.cpu;
    			free  = 100-sysInfo.cpu;
    			object2DProps.setObjectTitleText ("CPU Usage - (%)");
  	 		}
  	 		if(total>max)
  	 		max=total;
    		labelsAxisLabels[labelIndex++]=sysInfo.address+" "+total;
  	 	    dataset.set (index,cat,0, used);
    		dataset.set (++index,cat++,0,free);
    		index=0;
    		
         }
  	   graphChart2DProps.setChartDatasetCustomGreatestValue (max);
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
    	object2DProps.setObjectTitleText ("Disk Usage");

    
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);

    
    	LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = {"Used", "Free"};
    	legendProps.setLegendLabelsTexts (legendLabels);

	   	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    	String[] labelsAxisLabels = new String[nodes.size()];
    	
    	int index=0;
    	
    	//Object2DProperties object2DProps = new Object2DProperties();
    	if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Usage - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Usage - (K)");	
  	 	}
  	 	else if(option == 3) //Physical Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Physical Memory Usage - (K)");
  	 	}
  	 	else //CPU
  	 	{
  	 		object2DProps.setObjectTitleText ("CPU Usage - (%)");
  	 	}
    	graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
    	graphChart2DProps.setLabelsAxisTitleText ("Grid Machines");
    	graphChart2DProps.setNumbersAxisTitleText ("Usage");
    	graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    	graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    	graphChart2DProps.setChartDatasetCustomLeastValue (0);


    	//Configure graph properties
   		GraphProperties graphProps = new GraphProperties();
    	graphProps.setGraphOutlineComponentsExistence (true);

		Enumeration enumeration  = systemInfos.keys();
		Dataset dataset = new Dataset (2, nodes.size(), 1);
		index=0;
		int cat=0;
		int labelIndex=0;
		long max=0;
  		while(enumeration.hasMoreElements())
    	{
    		
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			used  = sysInfo.usedDisk;
    			free  = sysInfo.freeDisk;
    			object2DProps.setObjectTitleText ("Disk Usage - (MB)");
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
    			used  = sysInfo.usedVirtualMem;
    			free  = sysInfo.freeVirtualMem;
    			object2DProps.setObjectTitleText ("Virtual Memory Usage - (K)");	
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
    			used  = sysInfo.usedPhysicalMem;
    			free  = sysInfo.freePhysicalMem;
    			object2DProps.setObjectTitleText ("Physical Memory Usage - (K)");
  	 		}
  	 		else //CPU
  	 		{
  	 			used  = sysInfo.cpu;
    			free  = 100-sysInfo.cpu;
    			object2DProps.setObjectTitleText ("CPU Usage - (%)");
  	 		}
  	 		if(total>max)
  	 		max=total;
    		labelsAxisLabels[labelIndex++]=sysInfo.address+" "+total;
  	 	    dataset.set (index,cat,0, used);
    		dataset.set (++index,cat++,0,free);
    		index=0;
    		
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
  private void getBarChartTrendLine(JPanel panel) //comparison
  {
	   	
	   	long total=0,used=0,free=0;
	   	Object2DProperties object2DProps = new Object2DProperties();
    	object2DProps.setObjectTitleText ("Disk Usage");

    
    	Chart2DProperties chart2DProps = new Chart2DProperties();
    	chart2DProps.setChartDataLabelsPrecision (0);
    
    	LegendProperties legendProps = new LegendProperties();
    	String[] legendLabels = {"Avg.","Used","Free"};
    	legendProps.setLegendLabelsTexts (legendLabels);

	   	GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
	   	graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    	graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    	graphChart2DProps.setChartDatasetCustomLeastValue (0);
    	String[] labelsAxisLabels = new String[nodes.size()];
    	int index=0;
    	
    	//Object2DProperties object2DProps = new Object2DProperties();
    	if(option==1)//disk usage chart
    	{
    		object2DProps.setObjectTitleText ("Disk Usage - (MB)");
    	}
  	 	else if(option==2) //Virtual Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Virtual Memory Usage - (K)");	
  	 	}
  	 	else if(option == 3) //Physical Memory
  	 	{
  	 		object2DProps.setObjectTitleText ("Physical Memory Usage - (K)");
  	 	}
  	 	else //CPU
  	 	{
  	 		object2DProps.setObjectTitleText ("CPU Usage - (%)");
  	 	}
    	
    	graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
    	graphChart2DProps.setLabelsAxisTitleText ("Grid Machines");
    	graphChart2DProps.setNumbersAxisTitleText (" ");
		
		GraphProperties graphProps = new GraphProperties();
    	graphProps.setGraphComponentsAlphaComposite (graphProps.ALPHA_COMPOSITE_MILD);
    	
    	//Configure graph properties
   		GraphProperties graphPropsTrend = new GraphProperties();
    	graphPropsTrend.setGraphBarsExistence (false);
    	graphPropsTrend.setGraphLinesExistence (true);

		Enumeration enumeration  = systemInfos.keys();
		Dataset dataset = new Dataset (2, nodes.size(), 1);
		index=0;
		int cat=0;
		int labelIndex=0;
		long max=0;
  		while(enumeration.hasMoreElements())
    	{
    		
    		
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			used  = sysInfo.usedDisk;
    			free  = sysInfo.freeDisk;
    			object2DProps.setObjectTitleText ("Disk Usage - (MB)");
    		}
  	 		else if(option==2) //Virtual Memory
  	 		{
  	 			total = sysInfo.virtualMem;
    			used  = sysInfo.usedVirtualMem;
    			free  = sysInfo.freeVirtualMem;
    			object2DProps.setObjectTitleText ("Virtual Memory Usage - (K)");	
  	 		}
  	 		else if(option == 3) //Physical Memory
  	 		{
  	 			total = sysInfo.physicalMem;
    			used  = sysInfo.usedPhysicalMem;
    			free  = sysInfo.freePhysicalMem;
    			object2DProps.setObjectTitleText ("Physical Memory Usage - (K)");
  	 		}
  	 		else //CPU
  	 		{
  	 			used  = sysInfo.cpu;
    			free  = 100-sysInfo.cpu;
    			object2DProps.setObjectTitleText ("CPU Usage - (%)");
  	 		}
  	 		if(total>max)
  	 		max=total;
    		labelsAxisLabels[labelIndex++]=sysInfo.address+" "+total;
  	 	    dataset.set (index,cat,0, used);
    		dataset.set (++index,cat++,0,free);
    		index=0;
    		
         }
        graphChart2DProps.setChartDatasetCustomGreatestValue (max);
  		Dataset datasetTrend = new Dataset();
   		datasetTrend.addMovingAverage (dataset,3);
    	
    	MultiColorsProperties multiColorsProps = new MultiColorsProperties();
		MultiColorsProperties multiColorsPropsTrend = new MultiColorsProperties();
    	multiColorsPropsTrend.setColorsCustomize (true);
   		multiColorsPropsTrend.setColorsCustom (new Color[] {new Color (193, 183, 0)});
   
    	LBChart2D chart2D = new LBChart2D();
   		chart2D.setObject2DProperties (object2DProps);
    	chart2D.setChart2DProperties (chart2DProps);
    	chart2D.setLegendProperties (legendProps);
   		chart2D.setGraphChart2DProperties (graphChart2DProps);
    	chart2D.addGraphProperties (graphPropsTrend);
    	chart2D.addDataset (datasetTrend);
    	chart2D.addMultiColorsProperties (multiColorsPropsTrend);
    	chart2D.addGraphProperties (graphProps);
    	chart2D.addDataset (dataset);
    	chart2D.addMultiColorsProperties (multiColorsProps);
   		
   		panel.add(chart2D);    

    
  }
  
  private void getStackedBarChart(JPanel panel)
  {
  		Enumeration enumeration  = systemInfos.keys();
  		Iterator it = nodes.iterator();
  		long total=0,used=0,free=0;
    	while(enumeration.hasMoreElements())
    	{
    		SystemInformation sysInfo;
    		String address = (String)enumeration.nextElement();
    		sysInfo = (SystemInformation)systemInfos.get(address);
  	 	 	
    		Object2DProperties object2DProps = new Object2DProperties();
    			
    		if(option==1)//disk usage chart
    		{
    			total = sysInfo.totalDisk;
    			used  = sysInfo.usedDisk;
    			free  = sysInfo.freeDisk;
    			object2DProps.setObjectTitleText (address+" "+sysInfo.folderName+" "+total+" - (MB)");
    		}
  	 	 	else if(option==2) //Virtual Memory
  	 	 	{
  	 	 		total = sysInfo.virtualMem;
    			used  = sysInfo.usedVirtualMem;
    			free  = sysInfo.freeVirtualMem;
    			object2DProps.setObjectTitleText (address+" "+total+" - (K)");	
  	 	 	}
  	 	 	else if(option == 3) //Physical Memory
  	 	 	{
  	 	 		total = sysInfo.physicalMem;
    			used  = sysInfo.usedPhysicalMem;
    			free  = sysInfo.freePhysicalMem;
    			object2DProps.setObjectTitleText (address+" "+total+" - (K)");
  	 	 	}
  	 	 	else //CPU
  	 	 	{
  	 	 		used  = sysInfo.cpu;
    			free  = 100-sysInfo.cpu;
    			object2DProps.setObjectTitleText (address);
  	 	 	}
  	 	 	    		
    		//Configure chart properties
    		Chart2DProperties chart2DProps = new Chart2DProperties();
    		chart2DProps.setChartDataLabelsPrecision (0);

    		//Configure legend properties
    		LegendProperties legendProps = new LegendProperties();
    		String[] legendLabels =
      		{"Used", "Free"};
    		legendProps.setLegendLabelsTexts (legendLabels);

    		//Configure dataset
    		GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    		String[] labelsAxisLabels = {address};
   		    graphChart2DProps.setLabelsAxisLabelsTexts (labelsAxisLabels);
   		    graphChart2DProps.setChartDatasetCustomizeGreatestValue (true);
    		graphChart2DProps.setChartDatasetCustomizeLeastValue (true);
    		graphChart2DProps.setChartDatasetCustomLeastValue (0);
    		graphChart2DProps.setChartDatasetCustomGreatestValue (total);
    		
    		if(option != 4)
    		graphChart2DProps.setNumbersAxisTitleText ("Size: "+total);
    		   		
    		graphChart2DProps.setLabelsAxisTitleText ("Machine");
    		
    		//Configure graph properties
    		GraphProperties graphProps = new GraphProperties();
    		graphProps.setGraphAllowComponentAlignment (true);
    		graphProps.setGraphBarsRoundingRatio (0f);
   			graphProps.setGraphOutlineComponentsExistence (true);
   			
    	

    		int numSets = 2, numCats = 1, numItems = 1;
    		Dataset dataset = new Dataset (numSets, numCats, numItems);
    		dataset.set (0, 0, 0, total);
    		dataset.set (1, 0, 0, free);
 			dataset.doConvertToStacked();

    //Configure graph component colors
   			 MultiColorsProperties multiColorsProps = new MultiColorsProperties();

    //Configure chart
    //Configure chart
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
    			frame.setTitle("Disk Usage Charts");
    		break;
    		case 2:
    			frame.setTitle("Virtual Memory Usage Charts");
    		break;
    		case 3:
    			frame.setTitle("Physical Memory Usage Charts");
    		break;
    		default:
    			frame.setTitle("CPU Usage Charts");
    		
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