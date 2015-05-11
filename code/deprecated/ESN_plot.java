/*************************************************************************
 * This include plotting code from jMathPlot.  The documentation is poor
 * and some of the stuff (e.g. adding titles) doesn't seem to work.  
 * Also, the compiled classes get dumped in the directory up, for some 
 * reason.  The supposedly most recent version wouldn't compile correctly.
 * The API is not available.   
 * Despite all this, it's the only one I've found so far that easily 
 * and simply plots several time series with automatically contrasting 
 * colors.  Oy vey...
 *************************************************************************/

import java.util.ArrayList;
import javax.swing.JFrame;       // for graphics...
import org.math.plot.*;

public class ESN_plot {
    
    private static void Draw(ArrayList<ArrayList<Double>> timeSeries, double dt) {
        if (timeSeries.size() == 0) return;
        assert (checkTimeSeries(timeSeries));
        int nT = timeSeries.get(1).size();   // number of time points to plot
        // construct time array
        double[] t = new double[nT];
        for (int i = 0; i < nT; i++)
            t[i] = i*dt;
        // make the plot
        Stopwatch watch = new Stopwatch();
        Plot2DPanel plot = new Plot2DPanel();
        Double[] series = new Double[nT];
        for (int i = 0; i < timeSeries.size(); i++) {
            timeSeries.get(i).toArray(series);  // get the series array
            plot.addLinePlot(null, t, unbox(series));
        }
        // set axis labels, etc.
        //BaseLabel title = new BaseLabel("Network of 1000 neurons, 10 sampled", Color.RED, 0.5, 1.1);
        //title.setFont(new Font("Courier", Font.BOLD, 20));
        //plot.addPlotable(title);
        //plot.setAxesLabels("time (s)", "firing rate (a.u.)");  // appears to be broken..
        JFrame frame = new JFrame("a plot panel");
        frame.setSize(600, 600);
        frame.setContentPane(plot);
        frame.setVisible(true);
    }
    
    private static double[] unbox(Double[] d) {
        double[] out = new double[d.length];
        for (int i = 0; i < d.length; i++)
            out[i] = d[i];
        return out;
    }
    
    private static boolean checkTimeSeries(ArrayList<ArrayList<Double>> timeSeries) {
        if (timeSeries.size() == 0) {
            StdOut.println("time series array is empty");
            return false;
        }
        // Check that all time series are the same length
        int nT = timeSeries.get(1).size();  // number of time points
        for (int i = 1; i < timeSeries.size(); i++)
            if (timeSeries.get(i).size() != nT) return false;
        return true;
    }
    
    // append the data from the first n neurons at a given time point
    private static void addTimePoint(ArrayList<ArrayList<Double>> timeSeries, double[] r) {
        assert (timeSeries.size() < r.length); // {;}  // **** THROW EXCEPTION
        for (int i = 0; i < timeSeries.size(); i++) 
            timeSeries.get(i).add(r[i]);
    }

    public static void main(String[] args) {
        
        // Track only the first nTrack neurons for plotting
        int nTrack = 10;                         // number of neurons to plot
        ArrayList<ArrayList<Double>> timeSeries  // time series for each neuron
            = new ArrayList<ArrayList<Double>>(nTrack); 
        for (int i = 0; i < nTrack; i++)         // initialize
            timeSeries.add(new ArrayList<Double>());
         
        // Initialize default Reservoir
        Reservoir reservoir = new Reservoir();
        
        // Simulate and store data
        int nT = 100;    // number of simulation time steps
        double[] r = new double[reservoir.size()];
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            addTimePoint(timeSeries, reservoir.toArray(r));
        }
        
        // Plot
        Draw(timeSeries, reservoir.dt());
    }
    
}