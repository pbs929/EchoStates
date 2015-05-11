import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import java.util.ArrayList;

// Stores the data for learning
public class LearningModuleRegression {
        
    private int N;
    private int M;
    private TimeSeries storedR;         // stored reservoir state
    private TimeSeries storedTarget; 
    private Reservoir res;
    private ReadoutClampedFB ro;
    private Streams.InputStream target;
    
    public LearningModuleRegression(Reservoir res, ReadoutClampedFB ro, Streams.InputStream target) {
        N = res.size();
        M = ro.size();
        storedR       = new TimeSeries(N);
        storedTarget  = new TimeSeries(M); 
        this.res    = res;
        this.ro     = ro;
        this.target = target;
    }
    
    /*********************************************************************
     * Set/get
     *********************************************************************/
    public TimeSeries getStoredR() {
        return storedR;
    }
    
    public TimeSeries getStoredTarget() {
        return storedTarget;
    }
    
    /*********************************************************************
     * Learning methods
     *********************************************************************/
    public void store() {
        storedR.addTimePoint(res.getRArray());
        
        DoubleMatrix dummy = new DoubleMatrix(M);
        target.getInput(res.t(), dummy);
        storedTarget.addTimePoint(target.getInput(res.t(), dummy).toArray());
    }
    
    public void learn() {        
        // convert to matrices
        DoubleMatrix R = storedR.toMatrix();
        DoubleMatrix T = storedTarget.toMatrix();
        
        // display error prior to training
        DoubleMatrix W = new DoubleMatrix();
        ro.getWOut(W);
        StdOut.println("RMS error before learning:");
        StdOut.println(rmsError(R.mmul(W.transpose()), T)); // T'=R'W'
        
        W = Solve.solveLeastSquares(R, T); 
        
        // display error after training
        StdOut.println("RMS error after learning:");
        StdOut.println(rmsError(R.mmul(W), T));
        
        // assign to readout unit
        ro.setWOut(W.transpose());  // transpose?
    } 

    // dump the data already stored.  
    public void reset() {
        storedR       = new TimeSeries(N);
        storedTarget  = new TimeSeries(M); 
    }
    
    /*********************************************************************
     * helper methods
     *********************************************************************/
    private double rmsError(DoubleMatrix M1, DoubleMatrix M2) {
        M1.assertSameSize(M2);
        return M1.sub(M2).mul(M1.sub(M2)).sum()/M1.getLength();
    }
    
}

