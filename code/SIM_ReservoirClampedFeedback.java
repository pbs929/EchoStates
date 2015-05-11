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

/**
 *  Client code for running an ESN simulation.  
 *  Creates a <tt>Reservoir</tt> and <tt>RandReadoutFeedback</tt> 
 *  and plots the dynamics over time.  
 */
public class SIM_ReservoirClampedFeedback {
    
    /*
     * Simulate a default <tt>Reservoir</tt> with readout feedback of type
     * <tt>RandReadoutFeedback</tt> and plot the results.
     */
    public static void main(String[] args) {
        
        // Initialize Reservoir with clamped ClampedReadoutFeedback
        Reservoir           reservoir = new Reservoir();
        Streams.InputStream target    = new Streams.SquareWave(0.1); // period = 100ms
        ReadoutClampedFB    readout   = new ReadoutClampedFB(target, reservoir.size());
        reservoir.setFeedback(readout);
        
        // Track the first nTrack neurons, the readout, and the target for plotting
        int nTrack   = 5;                       // number of neurons to plot
        int nReadout = readout.size();          // number of readouts to plot
        String title = "Time series of selected reservoir neurons";
        TimeSeriesPlotter resData               // time series for reservoir
            = new TimeSeriesPlotter(nTrack, title, reservoir.dt());
        title = "Time series of readout neurons";
        TimeSeriesPlotter roData                // time series for readout
            = new TimeSeriesPlotter(nReadout, title, reservoir.dt()); 
        title = "Target readout";
        TimeSeriesPlotter tgData            // time series for readout
            = new TimeSeriesPlotter(nReadout, title, reservoir.dt()); 
        
        // Simulate and store data
        int nT = 5000;    // number of simulation time steps
        Stopwatch sw = new Stopwatch();
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            roData.addTimePoint (readout.getReadoutArray(reservoir));
            tgData.addTimePoint (readout.getTargetReadoutArray(reservoir.t()));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Plot
        resData.plot();
        roData.plot();
        tgData.plot();
    }
    
}