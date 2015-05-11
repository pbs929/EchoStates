import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.Solve;
//AX=B
//solveLeastSquares(DoubleMatrix A, DoubleMatrix B)

/** 
 *  Clamped feedback class with learning via linear regression.  
 *  The readouts and targets are stored each time step() is called.  
 *  Then, when learn() is called, the output weights are set by 
 *  linear regression.  
 */


/*********************************************************************
  * Learning
  *********************************************************************/
// Stores the data for learning
public class LearningModule {
        
    private DoubleMatrix storedR      = new DoubleMatrix(N, 0); 
    private DoubleMatrix storedTarget = new DoubleMatrix(M, 0); 
    
    /*********************************************************************
      * Public methods
      *********************************************************************/
    public void store(Reservoir res) {
        DoubleMatrix r      = new DoubleMatrix(N);
        DoubleMatrix target = new DoubleMatrix(M);
        lm.storePoint(res.getR(r), getTargetReadout(res.t(), target));
    }
    
    public void learn() {
        wOut.assign(lm.regress());
    }
    
    // Store the readouts and targets at a time point.    
    private void storePoint(DoubleMatrix r, DoubleMatrix target) {
        storedR      = F.appendColumn(storedR, r);
        storedTarget = F.appendColumn(storedTarget, target);
    }
    
    // solve W*r = target using least squares
    private DoubleMatrix regress() { 
        // add noise before learning
        //DoubleMatrix rand = F.random(storedR.rows(), storedR.columns());
        //rand.assign(DoubleMult.mult(2.0e-1));
        //rand.assign(DoubleFunctions.plus(-1.0e-1));
        //storedR.assign(rand, MatrixOps.plus);
        
        DoubleMatrix predicted = alg.mult(wOut,storedR);
        predicted.assign(storedTarget, MatrixOps.minus);
        StdOut.println("RMS error before learning:");
        StdOut.println(alg.normF(predicted)/Math.sqrt(predicted.size()));
        
        DoubleMatrix wNew = alg.transpose(alg.solveTranspose(storedR, storedTarget));
        
        predicted = alg.mult(wNew,storedR);
        predicted.assign(storedTarget, MatrixOps.minus);
        StdOut.println("RMS error after learning:");
        StdOut.println(alg.normF(predicted)/Math.sqrt(predicted.size()));
        
        reset(); // reset so added noise doesn't accumulate.
        return wNew; 
    }
    
    // dump the data already stored.  
    private void reset() {
        storedR      = new DoubleMatrix(N, 0); 
        storedTarget = new DoubleMatrix(M, 0); 
    }
}

}