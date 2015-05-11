import java.util.ArrayList;
import org.jblas.DoubleMatrix;

public class TimeSeries {
    
    // Arraylist of arrays of time points.  That is, each time point is an 
    // array, and this is an arraylist of time points.  
    protected ArrayList<double[]> timeSeries;
    protected int nSeries;
    protected int nT = 0;     // number of time points
    
    public TimeSeries(int nSeries) {
        this.nSeries = nSeries;
        timeSeries = new ArrayList<double[]>();
    }
        
    public int nT() {
        return nT; 
    }
    
    public int nSeries() {
        return nSeries;
    }
    
    /** 
     * Add data at a single time point to the array.  If the provided data has
     * higher dimension than the time series, only the first nSeries points are 
     * stored.
     */
    public void addTimePoint(double[] x) {
        if (x.length < nSeries) 
            throw new IllegalArgumentException("not enough inputs");
        if (x.length == nSeries) {
            timeSeries.add(x);
            nT++;
            return;
        }
        double[] reduced = new double[nSeries];
        for (int i = 0; i < nSeries; i++) 
            reduced[i] = x[i];
        timeSeries.add(reduced);
        nT++;
    }
    
    /** 
     * Get the data from a single time point as a 1D matrix  
     */
    public DoubleMatrix getTimePoint(int i) {
        if (i > nT || i < 0)
            throw new IndexOutOfBoundsException();
        return new DoubleMatrix(timeSeries.get(i));
    }
    
    /**
     * Get a nTxnR matrix version of all the data
     */
    public DoubleMatrix toMatrix() {
        double[][] array = new double[timeSeries.size()][];
        for (int i = 0; i < timeSeries.size(); i++) 
            array[i] = timeSeries.get(i);
        return new DoubleMatrix(array);
    }

    
}