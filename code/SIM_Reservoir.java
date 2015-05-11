/*************************************************************************
 *  Compilation:  javac ChaoticESN.java
 *  Author:  Phillip B. Schafer
 *  Last update:  March 2012
 * 
 *  Client code for running an ESN simulation.  
 *  Simulates a default Reservoir and plots the dynamics over time.  
 * 
 *  Dependencies: 
 *************************************************************************/

/**
 *  Client code for running an ESN simulation.  
 *  Creates a <tt>Reservoir</tt> with no feedback and plots the dynamics 
 *  over time.  
 */
public class SIM_Reservoir {
    
    public static void main(String[] args) {
        
        // Initialize default Reservoir
        Reservoir reservoir = new Reservoir();
        
        // Track only the first nTrack neurons for plotting
        int nTrack   = 5;                       // number of neurons to plot
        String title = "Time series of selected reservoir neurons (no feedback)";
        TimeSeriesPlotter2 resData               // time series for reservoir
            = new TimeSeriesPlotter2(nTrack, title, reservoir.dt());   
        
        // Simulate and store data
        int nT = 1000;    // number of simulation time steps
        Stopwatch sw = new Stopwatch();
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Plot
        resData.plot();
    }
    
}