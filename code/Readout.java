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
public class Readout implements Streams.Feedback {
    protected int M;              // number of readout units
    protected int N;              // number of feedbacks (network units)
    protected DoubleMatrix wOut;  // connection matrix to the readouts
    protected DoubleMatrix wBack; // connection matrix back to the network
    
    /**
     * Initialize a feedback/readout set of M neurons that feed back on 
     * N network units.  
     * @param M the number of feedback neurons
     * @param N the number of feedbacks
     * @param p the probability of a feedback connection (sparsity parameter)
     * @param p_z the probability of a readout connection (sparsity parameter)
     * @param g weighting factor for gaussian FEEDBACK connectivity  
     *   (output scaled to 1)
     * @throws IllegalArgumentException if <tt>N</tt> or <tt>M</tt> is less 
     *   than one
     * @throws IllegalArgumentException if <tt>p</tt> is not between 0 and 1
     * @throws IllegalArgumentException if <tt>g</tt> is infinite or NaN
     */
    public Readout(int M, int N, double p, double p_z, double g) {
        if (N <= 0 || M <= 0)
            throw new IllegalArgumentException("N must be greater than 0");
        if (!(p >=0 && p <=1))
            throw new IllegalArgumentException("p must be in [0,1]");
        if (Double.isInfinite(g) || Double.isNaN(g))
            throw new IllegalArgumentException("dt must be a finite number");
        
        this.M = M;
        this.N = N;
        
        // set READOUT connection weights - gaussian distrib., scaled by 1/N
        // readout = wOut*r
        wOut  = DoubleMatrix.randn(M, N).muli(1/Math.sqrt(N*p_z));          
        for (int i = 0; i < N; i++) { 
            for (int j = 0; j < M; j++) { 
                if (StdRandom.uniform(0.0, 1.0) > p_z) {
                    wOut.put(i, j, 0.0);
                }
            }
        }
        
        // set FEEDBACK connection weights - uniform distrib., scaled by g
        // feedback = wBack*readout
        wBack = DoubleMatrix.rand(N, M).subi(0.5).muli(2.0*g); 
        for (int i = 0; i < N; i++) { 
            for (int j = 0; j < M; j++) { 
                if (StdRandom.uniform(0.0, 1.0) > p) {
                    wBack.put(i, j, 0.0);
                }
            }
        }
    }
    
    /**
     * Initialize a network with default parameters: 
     * p = 1.0; p_z = 1.0; g = 1.0
     */
    public Readout(int M, int N) {
        this(M, N, 1.0, 1.0, 1.0);  
    }
    
    public int size() {
        return M;
    }
    
    public int fb_size() {
        return N;
    }
    
    /**********************************************************************
     * Set/get
     **********************************************************************/
    public void setWOut(DoubleMatrix W) {
        W.assertSameSize(wOut);
        wOut.copy(W);
    }
    
    public DoubleMatrix getWOut(DoubleMatrix W) {
        W.copy(wOut);
        return W;
    }
    
    /**********************************************************************
     * Get readouts and feedback
     **********************************************************************/
    /**
     * Get the readout, as a vector, given the current state of the network.
     * NOTE: This is the main workhorse for computing readout; all other 
     * readout methods call it.  
     * @param r the network state
     * @param t the current time (formal convention only)
     * @param readout the readout
     * @returns readout, for convenience only
     */
    public DoubleMatrix getReadout(DoubleMatrix r, double t, DoubleMatrix readout) {
        wOut.mmuli(r, readout);
        return readout;
    }
    
    /**
     * Get the readout and feedback, given the current state of the network.
     * NOTE: This is the main workhorse for computing feedback; all other 
     * feedback methods call it.  
     * @param r the network state
     * @param t the current time
     * @param readout the readout
     * @param feedback the feedback
     * @returns feedback, for convenience only
     */
    public DoubleMatrix getReadoutAndFeedback(DoubleMatrix r, double t, DoubleMatrix readout, DoubleMatrix feedback) {
        getReadout(r, t, readout);
        wBack.mmuli(readout, feedback);
        return feedback;
    }
            
    /**
     * Get the feedback, given the current state of the network.  
     * @param r the network state
     * @param t the current time
     * @param feedback the feedback
     * @returns feedback, for convenience only
     */
    public DoubleMatrix getFeedback(DoubleMatrix r, double t, DoubleMatrix feedback) {
        DoubleMatrix readout = new DoubleMatrix(M);
        getReadoutAndFeedback(r, t, readout, feedback);
        return feedback;
    }
    
    /**
     * Get the readout as a double array, given the current state of 
     * the network. 
     * @param r the network state
     * @param t the current time
     * @param readout the readout
     * @returns readout, for convenience only
     */
    public double[] getReadoutArray(DoubleMatrix r, double t) {
        DoubleMatrix readoutVec = new DoubleMatrix(M);
        getReadout(r, t, readoutVec) ;
        return readoutVec.toArray();
    }
    
    /**
     * Get the readout as a double array, taking a reference to the 
     * reservoir as input. 
     * @param r the network state
     * @param t the current time
     * @param readout the readout
     * @returns readout, for convenience only
     */
    public double[] getReadoutArray(Reservoir res) {
        DoubleMatrix r = new DoubleMatrix(N);
        res.getR(r);
        return getReadoutArray(r, res.t());
    }
    
    /**********************************************************************
     * Static nonlinearity  *** not used in this version...
     **********************************************************************/
    // x -> tanh(x)
    private void applyNonlin(DoubleMatrix x) {
        MatrixFunctions.tanhi(x);
    }
    
    /**********************************************************************
     * Unit testing
     **********************************************************************/
    public static void main(String[] args) {
        int nRes = 10;
        int nFB  = 5;
        double resProb = 0.5;
        
        Readout ro = new Readout(nFB, nRes);
        StdOut.println("Readout weights: ");
        StdOut.println(ro.wOut);
        StdOut.println("Feedback weights: ");
        StdOut.println(ro.wBack);
        
        Reservoir res = new Reservoir(nRes, resProb);
        DoubleMatrix r  = new DoubleMatrix(nRes);
        DoubleMatrix fb = new DoubleMatrix(nFB);
        double t = 1.1;
        ro.getFeedback(res.getR(r), t, fb);
        
    }
    
}