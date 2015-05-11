import java.util.ArrayList;

public class TimeSeriesPlotter {
    
    // Array of arrays of time points.  That is, each time series is an array, 
    // and this is an array of time series.  
    private ArrayList<ArrayList<Double>> timeSeries;
    
    private int nSeries;    // number of time series
    private double dt;      // time step
    private String title;   // plot title
    
    public TimeSeriesPlotter(int nSeries, String title, double dt) {
        this.nSeries = nSeries;
        this.title   = title;
        this.dt      = dt;
        
        timeSeries = new ArrayList<ArrayList<Double>>(nSeries);
        for (int i = 0; i < nSeries; i++) 
            timeSeries.add(new ArrayList<Double>());
    }
    
    /* 
     * Add data at a single time point to the array.  If the provided data has
     * higher dimension than the plotter, only the first nSeries points are 
     * stored.
     */
    public void addTimePoint(double[] x) {
        if (x.length < nSeries) 
            throw new IllegalArgumentException("not enough inputs");
        for (int i = 0; i < nSeries; i++) 
            timeSeries.get(i).add(x[i]);
    }
    
    public void plot() {
        Plotting.PlotTimeSeries(timeSeries, dt, title);
    }
}