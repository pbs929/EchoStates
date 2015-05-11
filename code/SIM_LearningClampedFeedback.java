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
public class SIM_LearningClampedFeedback {
    
    /*
     * Simulate a default <tt>Reservoir</tt> with readout feedback of type
     * <tt>RandReadoutFeedback</tt> and plot the results.
     */
    public static void main(String[] args) {
        
        // Initialize Reservoir with clamped ClampedReadoutFeedback
        Reservoir           reservoir  = new Reservoir();
        Streams.InputStream targetWave = new Streams.SineWave(0.1); // period = 100ms
        ReadoutClampedFB    readout    = new ReadoutClampedFB(targetWave, reservoir.size());
        LearningModuleRegression lm    = new LearningModuleRegression(reservoir, readout, targetWave);
        reservoir.setFeedback(readout);
        
        // Track the first nTrack neurons, the readout, and the targetWave for plotting
        int nTrack   = 5;                       // number of neurons to plot
        int nReadout = readout.size();          // number of readouts to plot
        String title = "Time series of selected reservoir neurons";
        TimeSeriesPlotter2 resData              // time series for reservoir
            = new TimeSeriesPlotter2(nTrack, title, reservoir.dt());
        title = "Time series of readout neurons";
        TimeSeriesPlotter2 roData               // time series for readout
            = new TimeSeriesPlotter2(nReadout, title, reservoir.dt()); 
        title = "Target readout";
        TimeSeriesPlotter2 tgData               // time series for readout
            = new TimeSeriesPlotter2(nReadout, title, reservoir.dt()); 
        
        // Acclimation ------------------------------------------------------
        int nT = 500;    // number of simulation time steps for this segment
        Stopwatch sw = new Stopwatch();
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            //resData.addTimePoint(reservoir.getRArray());
            //roData.addTimePoint(readout.getReadoutArray(reservoir));
            //tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Clamped learning -------------------------------------------------
        nT = 1000;  
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            lm.store();
            //resData.addTimePoint(reservoir.getRArray());
            //roData.addTimePoint(readout.getReadoutArray(reservoir));
            //tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        lm.learn();
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Clamped trial ----------------------------------------------------
        TimeSeries sampleSeg = new TimeSeries(readout.size());
        
        nT = 1500;  
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            //resData.addTimePoint(reservoir.getRArray());
            //roData.addTimePoint(readout.getReadoutArray(reservoir));
            //tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
            sampleSeg.addTimePoint(readout.getReadoutArray(reservoir));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Clamped learning 2 -----------------------------------------------
        TimeSeriesStream storedResponse 
            = new TimeSeriesStream(sampleSeg, reservoir.dt(), reservoir.t());
        readout.setTarget(storedResponse);  // new clamped feedback
        
        lm.reset();
                
        nT = 1000;  
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            lm.store();
            resData.addTimePoint(reservoir.getRArray());
            roData.addTimePoint(readout.getReadoutArray(reservoir));
            tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
            storedResponse.nextFrame();
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        lm.learn();
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Clamped Testing --------------------------------------------------
        nT = 500;  
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            roData.addTimePoint(readout.getReadoutArray(reservoir));
            tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
            storedResponse.nextFrame();
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Acclimation ------------------------------------------------------
//        readout.setTarget(targetWave);  
//        
//        nT = 500;    // number of simulation time steps for this segment
//        for (int iStep = 0; iStep < nT; iStep++) {
//            reservoir.step();
//            resData.addTimePoint(reservoir.getRArray());
//            roData.addTimePoint(readout.getReadoutArray(reservoir));
//            tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
//        }
//        StdOut.println("Simulation time:");
//        StdOut.println(sw.elapsedTime());
//        
        // Unclamped Testing -------------------------------------------------
        readout.unclamp();
        
        nT = 500;  
        for (int iStep = 0; iStep < nT; iStep++) {
            reservoir.step();
            resData.addTimePoint(reservoir.getRArray());
            roData.addTimePoint(readout.getReadoutArray(reservoir));
            //tgData.addTimePoint(readout.getTargetReadoutArray(reservoir.t()));
        }
        StdOut.println("Simulation time:");
        StdOut.println(sw.elapsedTime());
        
        // Plot
        resData.plot();
        roData.plot();
        tgData.plot();
    }
    
}