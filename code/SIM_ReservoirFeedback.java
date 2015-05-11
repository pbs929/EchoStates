/*************************************************************************
 *  Compilation:  javac ChaoticESN.java
 *  Author:  Phillip B. Schafer
 *  Last update:  March 2012
 * 
 *  Client code for running an ESN simulation.  
 *  Simulates a default reservoir with random feedback. 
 * 
 *  Dependencies: 
 *************************************************************************/

/**
 *  Client code for running an ESN simulation.  
 *  Creates a <tt>Reservoir</tt> with feedback of type 
 *  <tt>ReadoutFeedback</tt> and plots the dynamics over time.  
 */
public class SIM_ReservoirFeedback {
    
    public static void main(String[] args) {
        
        // Initialize default Reservoir and default ReadoutFeedback
        Reservoir reservoir = new Reservoir();
        Readout   readout   = new Readout(5, reservoir.size()); // 5 readouts
        reservoir.setFeedback(readout);
        
        // Track only the first nTrack neurons and the readout for plotting
        int nTrack   = 5;                       // number of neurons to plot
        int nReadout = readout.size();          // number of readouts to plot
        String title = "Time series of selected reservoir neurons";
        TimeSeriesPlotter resData               // time series for reservoir
            = new TimeSeriesPlotter(nTrack, title, reservoir.dt());
        title = "Time series of readout neurons";
        TimeSeriesPlotter roData                // time series for readout
            = new TimeSeriesPlotter(nReadout, title, reservoir.dt()); 
        
        // Simulate and store data
        int nT = 1000;    // number of simulation time steps
        Stopwatch sw = new Stopwatch();
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            roData.addTimePoint (readout.getReadoutArray(reservoir));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Plot
        resData.plot();
        roData.plot();
    }
    
}