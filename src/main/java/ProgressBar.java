import java.awt.*;
import javax.swing.*;

public class ProgressBar extends JPanel
{
    private JProgressBar progressBar;
    private JFrame frame;
    private int maxValue;
    String title;
    public ProgressBar(int max,String title ) 
    {
        super(new SpringLayout());
        maxValue=max;
        this.title=title;
        setPreferredSize(new Dimension(250,80));
        progressBar = new JProgressBar(0,max);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        add(progressBar);
        SpringUtilities.makeCompactGrid(this,
                                        1, 1, //rows, cols
                                        10, 20,        //initX, initY
                                        10, 20);  
    }
	public void setValue(int val)
	{
		if(val<=maxValue)
		progressBar.setValue(val);
		//frame.setTitle(title+" "+progressBar.getPercentComplete());
	}
	public void closeProgress()
	{
		frame.dispose();
	}
   
    private  void createAndShowGUI(final Point dim) {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        frame = new JFrame("Writing File to Disk...");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Create and set up the content pane.
        frame.setContentPane(this);
		frame.setLocation((dim.x-200)/2,(dim.y-100)/2);
		frame.setTitle(title);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void showProgressBar(final Point dim) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(dim);
            }
        });
    }
}
