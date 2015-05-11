/*************************************************************************
 *  Plotting using the jChart2D package.  The plots are not very 
 *  customizable but they do the job quickly.  Also has good options
 *  for realtime plotting.  (Intended for engineering applications...)
 *  http://jchart2d.sourceforge.net/usage.shtml
 *************************************************************************/
import java.util.ArrayList;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.JFrame;
import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.traces.*;

public class Plotting_jChart2D {
    /**
     * Make a plot containing several time series.  
     * Input is an N-dimensional ArrayList of nT-dimensional ArrayLists, 
     * where N is the number of series to be plotted and nT is the number 
     * of time points in a series.  
     * The t-values are computed for each series using the input dt.   
     * @param timeSeries the ArrayList of ArrayLists.
     * 
     */
    public static void PlotTimeSeries(ArrayList<ArrayList<Double>> timeSeries, double dt) {
        if (timeSeries.size() == 0) return;
        
        Chart2D chart = new Chart2D();             // create chart
        IAxis xaxis = chart.getAxisX();                  // http://jchart2d.sourceforge.net/docs/javadoc/info/monitorenter/gui/chart/IAxis.html
        IAxis.AxisTitle xtitle = xaxis.getAxisTitle();   // http://jchart2d.sourceforge.net/docs/javadoc/info/monitorenter/gui/chart/IAxis.AxisTitle.html
        xtitle.setTitle("time (s)");
        chart.getAxisY().getAxisTitle().setTitle("Firing rate (a.u.)");
        for (int i = 0; i < timeSeries.size(); i++) {
            ITrace2D trace = new Trace2DSimple(Integer.toString(i));  // create trace and 
            chart.addTrace(trace);                   // add to chart
            trace.setColor(Color.BLACK);                  // http://docs.oracle.com/javase/7/docs/api/java/awt/Color.html
            for(int j = 0; j < timeSeries.get(i).size(); j++){
                trace.addPoint(j*dt, timeSeries.get(i).get(j)); // add points
            }
        }
        JFrame frame = new JFrame("MinimalStaticChart");  
        frame.getContentPane().add(chart);
        frame.setSize(400,300);
        frame.addWindowListener(new WindowAdapter(){        // 'close' button
            public void windowClosing(WindowEvent e){ System.exit(0);} });
        frame.setVisible(true);
    }
    
}