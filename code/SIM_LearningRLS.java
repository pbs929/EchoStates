/*************************************************************************
 *  Compilation:  javac ChaoticESN.java
 *  Author:  Phillip B. Schafer
 *  Last update:  March 2012
 * 
 *  Client code for running an ESN simulation.  
 *  Creates default Reservoir and Feedback objects with clamped feedback
 * 
 *  Dependencies: 
 *************************************************************************/
import org.jblas.DoubleMatrix;
/**
 *  Client code for running an ESN simulation.  
 *  Creates a <tt>Reservoir</tt> and <tt>RandReadoutFeedback</tt> 
 *  and plots the dynamics over time.  
 */
public class SIM_LearningRLS {
    
    /*
     * Simulate a default <tt>Reservoir</tt> with readout feedback of type
     * <tt>RandReadoutFeedback</tt> and plot the results.
     */
    public static void main(String[] args) {
        
        double alpha = 1.0;  // 1-100: S&A p. 548
        
        // Initialize Reservoir with clamped ClampedReadoutFeedback
        Reservoir           reservoir  = new Reservoir();
        Streams.InputStream targetWave = new Streams.TriangleWave(0.5); // period = 100ms
        ReadoutLearningRLS  readout    = new ReadoutLearningRLS(targetWave, reservoir.size(), alpha);
        reservoir.setFeedback(readout);
        
        // Track the first nTrack neurons, the readout, and the targetWave for plotting
        int nTrack   = 5;                       // number of neurons to plot
        int nReadout = readout.size();          // number of readouts to plot
        String title = "Time series of selected reservoir neurons";
        TimeSeriesPlotter2 resData              // time series for reservoir
            = new TimeSeriesPlotter2(nTrack, title, reservoir.dt());
        title = "Time series of readout neurons (pre-training)";
        TimeSeriesPlotter2 roDataPre            // time series for readout
            = new TimeSeriesPlotter2(nReadout, title, reservoir.dt()); 
        title = "Time series of readout neurons (post-training)";
        TimeSeriesPlotter2 roDataPost           // time series for readout
            = new TimeSeriesPlotter2(nReadout, title, reservoir.dt()); 
        title = "Target readout";
        TimeSeriesPlotter2 tgData               // time series for readout
            = new TimeSeriesPlotter2(nReadout, title, reservoir.dt()); 
        title = "Error";
        TimeSeriesPlotter2 errData              // time series for readout
            = new TimeSeriesPlotter2(nReadout, title, reservoir.dt()); 
        
        // Acclimation ------------------------------------------------------
        int nT = 500;    // number of simulation time steps for this segment
        Stopwatch sw = new Stopwatch();
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            roDataPost.addTimePoint(readout.getReadoutArray(reservoir));
            tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // RLS learning -----------------------------------------------------
        int learnInt = 10; // # ofintegration time steps between learning updates
        
        nT = 2000;  
        for (int iStep = 0; iStep < nT; iStep++) {
            StdOut.println(iStep);
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
            roDataPre.addTimePoint(readout.getReadoutArray(reservoir));
            errData.addTimePoint(readout.getErrorArray(reservoir));
            if (iStep % learnInt == 0)
                readout.learn(reservoir);
            roDataPost.addTimePoint(readout.getReadoutArray(reservoir));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Testing ----------------------------------------------------------
        nT = 1000;  
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            roDataPost.addTimePoint(readout.getReadoutArray(reservoir));
            tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
            errData.addTimePoint(readout.getErrorArray(reservoir));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Plot
        resData.plot();
        roDataPost.plot();
        roDataPre.plot();
        tgData.plot();
        errData.plot();
    }
    
}