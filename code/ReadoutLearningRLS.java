import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

/**
 *  Implements a set of M readout/feedback units that interact with a 
 *  reservoir of size N rate-model neurons.  
 *  Unit responses are weighted sum (no nonlinearity).  
 *  Connection weights to the units are optionally sparse (parameter p_z)
 *  and drawn from a normal distribution; 
 *  they are represented by a 2D dense matrix (Colt library) 
 *  and scaled by 1/sqrt(N*p_z) (Sussillo & Abbott 2009, p. 556).
 *  Feedback weights are optionally sparse (parameter p) and drawn from a 
 *  uniform distribution; they are represented by a 2D dense matrix 
 *  and scaled by g (Sussillo & Abbott 2009, p. 556). 
 */
public class ReadoutLearningRLS extends Readout implements Streams.Feedback {
    
    protected double alpha;
    protected Streams.InputStream targetStream;
    DoubleMatrix P;  // the correlation estimate
    
    /**
     * Initialize a feedback/readout set of M neurons that feed back on 
     * N network units.  
     * @param target the target readout
     * @param N the number of feedbacks
     * @param p the probability of a feedback connection (sparsity parameter)
     * @param p_z the probability of a readout connection (sparsity parameter)
     * @param g weighting factor for gaussian FEEDBACK connectivity  
     *   (output scaled to 1)
     * @param alpha the learning rate
     * @throws IllegalArgumentException if <tt>N</tt> or <tt>M</tt> is less 
     *   than one
     * @throws IllegalArgumentException if <tt>p</tt> is not between 0 and 1
     * @throws IllegalArgumentException if <tt>g</tt> is infinite or NaN
     */
    public ReadoutLearningRLS(Streams.InputStream target, int N, double p, double p_z, double g, double alpha) {
        super(target.size(), N, p, p_z, g);
        this.alpha = alpha;
        this.targetStream = target;
        setP(alpha);
    }
    
    /**
     * Initialize a network with default parameters: 
     * p = 1.0; p_z = 1.0; g = 1.0
     */
    public ReadoutLearningRLS(Streams.InputStream target, int N, double alpha) {
        super(target.size(), N);  
        this.alpha = alpha;
        this.targetStream = target;
        setP(alpha);
    }
    
    private void setP(double alpha) {
        P = DoubleMatrix.eye(N).divi(alpha);
    }
    
    /**********************************************************************
     * Get readouts and feedback 
     * (these methods borrowed from ReadoutClampedFB)
     **********************************************************************/
    /**
     * Get target readout from the target stream. 
     * @param t the current time
     * @param the readout
     * @returns the readout, for convenience only
     */
    public DoubleMatrix getTargetReadout(double t, DoubleMatrix readout) {
        if (readout.rows != M || readout.columns != 1)
            throw new IllegalArgumentException("readout matrix is wrong size");
        return targetStream.getInput(t, readout);  
    }   
    
    /**
     * Get target readout from the target stream. 
     * @param t the current time
     * @param the readout
     * @returns the readout, for convenience only
     */
    public double[] getTargetReadoutArray(double t) {
        DoubleMatrix readoutVec = new DoubleMatrix(M);
        targetStream.getInput(t, readoutVec);
        return readoutVec.toArray();
    }
    
    public double[] getErrorArray(DoubleMatrix r, double t) {
        DoubleMatrix readout = new DoubleMatrix(M);
        getReadout(r, t, readout);
        
        DoubleMatrix target = new DoubleMatrix(M);
        targetStream.getInput(t, target);
        
        return target.subi(readout).muli(target).toArray();
    }
    
    public double[] getErrorArray(Reservoir res) {
        DoubleMatrix readout = new DoubleMatrix(M);
        DoubleMatrix r = new DoubleMatrix(N);
        getReadout(res.getR(r), res.t(), readout);
        
        DoubleMatrix target = new DoubleMatrix(M);
        targetStream.getInput(res.t(), target);
        
        return target.subi(readout).muli(target).toArray();
    }
        
    /**********************************************************************
     * Learning
     **********************************************************************/
    public void learn(Reservoir res) {
        DoubleMatrix r = new DoubleMatrix(N);
        res.getR(r);
        
        DoubleMatrix readout = new DoubleMatrix(M);
        getReadout(r, res.t(), readout);
        
        DoubleMatrix target = new DoubleMatrix(M);
        targetStream.getInput(res.t(), target);  
        
        DoubleMatrix error = readout.sub(target);
        
        DoubleMatrix norm = r.transpose().mmul(P).mmul(r).addi(1.0);
        P.subi( P.mmul(r).mmul(r.transpose()).mmul(P).divi(norm) );
        
        wOut.subi( error.mmul(r.transpose().mmul(P)) );
    }
    
}