import java.awt.Canvas;
import java.util.Random;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
class DynamicGraph1 extends Canvas
{
 	 public LinkedList list = new LinkedList();
 	 int count = 0;
 	 private Color backColor,lineColor,warningColor;
 	 private int incX=0,decY=0;
 	 private int x=0,y=0;
 	 private Point[] points;
 	 private int orientation,direction;
 	 private int numValues;
 	 private int warningLevel;
 	 public int graphType;
 	 public static final int RIGHT_LEFT = -1;
 	 public static final int LEFT_RIGHT =  1;
 	 public static final int TOP_BOTTOM =  1;
 	 public static final int BOTTOM_TOP = -1;
 	 public static final int LINE       =  1;
 	 public static final int BAR        =  2;
 	 public static final int LEVEL      =  3;
 	 private int prevLevel;
 	 private int initX;
 	
 	 
 	 
 	 public DynamicGraph1(int type,int values,int ornt,int direct,int warning) throws IllegalArgumentException
 	 {
 	 	
 	 	graphType=type;
 	 	numValues=values;
 	 	orientation=ornt;
 	 	direction=direct;
 	 	backColor=Color.BLACK;
 	 	lineColor=Color.GREEN;
 	 	warningColor=Color.RED;
 	 	prevLevel=0;
 	 	initX=0;
 	 	if(orientation != TOP_BOTTOM && orientation != BOTTOM_TOP)
 	 	{
 	 	
 	 		throw new IllegalArgumentException();
 	 		
 	 	}
 	 	if(direction != LEFT_RIGHT && direction != RIGHT_LEFT)
 	 	{
 	 		throw new IllegalArgumentException();
 	 		
 	 	}
 	 	if(warningLevel>100||warningLevel<0)
 	 	{
 	 		throw new IllegalArgumentException();
 	 	 	
 	 	}
 	 	warningLevel=warning;
 	 		
 	 	if(graphType != BAR && graphType != LINE && graphType!=LEVEL)
 	 	{
 	 			throw new IllegalArgumentException();
 	 	}
 	 	if(graphType==LEVEL)
 	 	{
 	 		if(orientation==-1)
 	 		orientation=3;
 	 		else
 	 		orientation=4;
 	 		list.addFirst(new Integer(0));
 	 	}
 	 	else
 	 	{
 	 		for(int i=0;i<values-1;i++)
 	 		{
 	 			list.add(new Integer(0));
 	 		}	
 	 	}
 	 	 	 	
 	 }
 	 public DynamicGraph1(int type,int values,int ornt,int direct,int warning,Color back,Color line,Color warn)
 	 {
 	 	
 	 	graphType=type;
 	 	numValues=values;
 	 	orientation=ornt;
 	 	direction=direct;
 	 	backColor=back;
 	 	lineColor=line;
 	 	warningColor=warn;
 	 	prevLevel=0;	
 	 
 	 	if(orientation != TOP_BOTTOM && orientation != BOTTOM_TOP)
 	 	{
 	 	
 	 		throw new IllegalArgumentException();
 	 		
 	 	}
 	 	if(direction != LEFT_RIGHT && direction != RIGHT_LEFT)
 	 	{
 	 		throw new IllegalArgumentException();
 	 		
 	 	}
 	 	if(warningLevel>100||warningLevel<0)
 	 	{
 	 		throw new IllegalArgumentException();
 	 	
 	 	}
 	 	warningLevel=warning;
 	 		 	  	
 	 	if(graphType != BAR && graphType != LINE && graphType!=LEVEL)
 	 	{
 	 			throw new IllegalArgumentException();
 	 		
 	 	}
 	 	if(graphType==LEVEL)
 	 	{
 	 		if(orientation==-1)
 	 		orientation=3;
 	 		else
 	 		orientation=4;
 	 		list.addFirst(new Integer(0));
 	 	}
 	 	else
 	 	{
 	 		for(int i=0;i<values-1;i++)
 	 		{
 	 			list.add(new Integer(0));
 	 		}	
 	 	}
 	 	
 	 }
 	 public void paint(Graphics g) 
 	 {     
 	    	   	
 	   	Rectangle rect = new Rectangle();
 	    rect = getBounds();
 	    if(direction == 1)
 	    x=0;
 	    else
 	    x=rect.width;
 	    	    
 	    if(orientation==-1)
 	    y=rect.height;
 	    else
 	    y=0;
 	    incX=rect.width/numValues;
 	    decY=rect.height/numValues;
 	    
 	    setBackground(backColor);
 	   	   	
 	   			
 	   	//paintColor(Color.GREEN);
        Graphics2D g2 = (Graphics2D) g;
        //g2.setPaintMode();
        
        int index=0;
        int size = list.size();
        Iterator it = list.iterator();
        if(graphType==BAR)
        {
			while(it.hasNext())
        	{
        		g2.setColor(lineColor);
        		Integer val = (Integer)it.next();
        		int value = val.intValue();
        		if(((value*100)/numValues)>=warningLevel)
        		g2.setColor(warningColor);
        		g2.drawLine(x+(incX*index*direction),y,x+(incX*index*direction),y+(value*decY*orientation));
        		index++;
        		
        		
        	}        	
        	if(count++>numValues)
 	   		{
				list.removeLast();
				count=0;
			}
        }
        else if(graphType==LINE)
        {
        	g2.setColor(lineColor);
        	points = new Point[size];
        	int i=0;
        	initX = initX>incX?0:++initX;
        	for(int j=initX;j<25*incX;j+=incX)
        	{
        		g2.drawLine(j,0,j,rect.height);        	
        	}	
       		while(it.hasNext())
        	{
        		Point p = new Point();
        		if(i==0)
        		{
        			p.x=x;
           		}
           		else
        		p.x=x+(i*incX*direction);
        		p.y=y+((((Integer)it.next()).intValue())*decY*orientation);
        		points[i]=p;
        		i++;
        		
        	}
        	if(size>1)
       		{
        		for(i=0;i<size-1;i++)
        		{
					g2.drawLine(points[i].x,points[i].y,points[i+1].x,points[i+1].y);        	
					g2.setColor(lineColor);
				}
       	 	}
       	 	if(count++>numValues)
 	   		{
				list.removeLast();
				count=0;
			}
       		 
   	  	}
   	  	else 
   	  	{
   	  		
   	  		int value=0;
   	  		if(size>0)
   	  		{
   	  			value = ((Integer)list.removeFirst()).intValue();
   	  		}
   	  		
   	  		if(orientation==1)  //Left to Right
   	  		{
	  			y=rect.height;
   	  		 	  			  	  			
   	  			g2.setColor(lineColor);
   	  			if(value>=warningLevel)
   	  			g2.setColor(warningColor);
   	  		
   	  			for(int i=0;i<value;i++)
   	  			{
   	  				g2.drawLine(i*incX,0,i*incX,y);
   	  			}
   	  		}
   	  		else if(orientation==2)  //Right to Left
   	  		{
   	  			x=rect.width;
   	  			y=rect.height;
   	  			for(int i=0;i<value;i++)
   	  			{
   	  				g2.drawLine(x-(i*incX),0,x-(i*incX),y);
   	  			}
   	  		}
   	  		else if(orientation==3)  //Bottom to Top
   	  		{
   	  			x=rect.width;
   	  			y=rect.height;
   	  			g2.setColor(lineColor);
   	  			if(value>=warningLevel)
   	  			g2.setColor(warningColor);
   	  		
   	  			for(int i=0;i<value;i++)
   	  			{
   	  				g2.drawLine(0,y-(i*decY),x,y-(i*decY));
   	  			}
   	  		}
   	  		else if(orientation==4)  //Top to Bottom
   	  		{
   	  			x=rect.width;
   	  			
   	  			g2.setColor(lineColor);
   	  			if(value>=warningLevel)
   	  			g2.setColor(warningColor);
   	  		
   	  			for(int i=0;i<value;i++)
   	  			{
   	  				g2.drawLine(0,i*decY,x,i*decY);
   	  			}
   	  		}
  	  	}
        
    }
    public void setLineColor(Color color)
    {
    	lineColor=color;
    }
    public void setBackColor(Color color)
    {
    	backColor=color;
    }
    public void setWarningLevelColor(Color color)
    {
    	warningColor = color;
    }
    public void setDirection(int value)
    {
    	direction = value;
    }
    public void setOrientation(int value)
    {
    	orientation=value;
    }
    public void setGraphType(int value)
    {
    	
    	graphType = value;
       	
    }
    public Color getLineColor()
    {
    	return lineColor;
    }
    public Color getBackColor()
    {
    	return backColor;
    }
    public Color getWarningLevelColor()
    {
    	return warningColor;
    }
    public int getDirection()
    {
    	return direction;
    }
    public int getOrientation()
    {
    	return orientation;
    }
    public int getGraphType()
    {
    	return graphType;
    }
}
