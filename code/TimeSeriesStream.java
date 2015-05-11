import org.jblas.DoubleMatrix;

public class TimeSeriesStream implements Streams.InputStream {
    
    private TimeSeries timeSeries;
    private double dt;
    private double t;
    private int nSeries;
        
    private int currentFrame = 0;
    private final double tolerance = 1.0e-10; // for t
    
    public TimeSeriesStream(TimeSeries timeSeries, double dt, double t) {
        this.timeSeries = timeSeries;
        this.dt = dt;
        this.t = t;
        this.nSeries = timeSeries.nSeries();
    }
    
    public void setT(double t) {
        this.t = t;
    }
    
    public int size() {
        return nSeries;
    }
    
    public boolean hasMoreFrames() {
        return currentFrame < timeSeries.nT();
    }
    
    public void nextFrame() {
        t += dt;
        currentFrame ++;
    }
    
    public DoubleMatrix getInput(double t, DoubleMatrix input) {
        // check that t matches the internal time.  
        if (Math.abs(t - this.t) > dt + tolerance)
            throw new IllegalArgumentException("Queried time does not match internal time of module");
        if (currentFrame >= timeSeries.nT())
            throw new IllegalArgumentException("Time series is out of timeSeries points to return");
        if (input.columns != nSeries)
            throw new IllegalArgumentException("Wrong number of inputs requested");
        input.copy(timeSeries.getTimePoint(currentFrame));
        return input;
    }
    
}