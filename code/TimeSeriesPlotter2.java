public class TimeSeriesPlotter2 extends TimeSeries {
    
    private double dt;      // time step
    private String title;   // plot title
    
    public TimeSeriesPlotter2(int nSeries, String title, double dt) {
        super(nSeries);
        this.title   = title;
        this.dt      = dt;        
    }
    
    public void plot() {
        Plotting2.PlotTimeSeries(timeSeries, dt, title);
    }
}