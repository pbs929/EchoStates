import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

/** 
 *  Readout units with clamped feedback.  
 *  The readout is the actual readout using the random weights.  
 *  The feedback, however, is computed using a target readout via a 
 *  <tt>Streams.InputStream</tt> object.  
 *  The target readout can be gotten by a different method.  
 */
public class ReadoutClampedFB extends Readout {
    
    protected Streams.InputStream target;
    protected boolean isClamped = true;
    
    /**
     * Initialize a feedback/readout set of M neurons that feed back on 
     * N network units.  M is given by the size of the target stream.
     * @param target the target readout
     * @param N the number of feedbacks
     * @param p the probability of a feedback connection (sparsity parameter)
     * @param p the probability of a readout connection (sparsity parameter)
     * @param g weighting factor for gaussian FEEDBACK connectivity  
     *   (output scaled to 1)
     */
    public ReadoutClampedFB(Streams.InputStream target, int N, double p, double p_z, double g){
        super(target.size(), N, p, p_z, g);
        this.target = target;
    }
    
    /**
     * Initialize a network with default parameters.
     */
    public ReadoutClampedFB(Streams.InputStream target, int N){
        super(target.size(), N);
        this.target = target;
    }
    
    /**********************************************************************
     * set/get
     **********************************************************************/
    public void setTarget (Streams.InputStream target) {
        if (target.size() != this.target.size())
            throw new IllegalArgumentException("New target stream is incorrect size");
        this.target = target;
    }
    
    /**********************************************************************
     * Clamping
     **********************************************************************/
    /**
     * Clamp the feedback.
     */
    public void clamp() { 
        isClamped = true; 
    }
    
    /**
     * Unclamp the feedback.
     */
    public void unclamp() { 
        isClamped = false; 
    }
    
    /**********************************************************************
     * Get readouts and feedback
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
        return target.getInput(t, readout);  
    }   
    
    /**
     * Get target readout from the target stream. 
     * @param t the current time
     * @param the readout
     * @returns the readout, for convenience only
     */
    public double[] getTargetReadoutArray(double t) {
        DoubleMatrix readoutVec = new DoubleMatrix(M);
        target.getInput(t, readoutVec);
        return readoutVec.toArray();
    }
    
    /**
     * Get the readout and feedback, given the current state of the network.  
     * Here, the readout is the real readout, but the feedback is computed 
     * using the TARGET readout.  
     * @param r the network state
     * @param t the current time
     * @param readout the readout
     * @param feedback the feedback
     * @returns feedback, for convenience only
     */
    @Override
    public DoubleMatrix getReadoutAndFeedback(DoubleMatrix r, double t, DoubleMatrix readout, DoubleMatrix feedback) {
        if (!isClamped) 
            return super.getReadoutAndFeedback(r, t, readout, feedback);
        getReadout(r, t, readout);
        DoubleMatrix target = new DoubleMatrix(M);
        getTargetReadout(t, target);
        wBack.mmuli(target, feedback);
        return feedback;
    }
    
    /**
     * Get the feedback, given the current state of the network.  
     * @param r the network state
     * @param t the current time
     * @param feedback the feedback
     * @returns feedback, for convenience only
     */
    @Override
    public DoubleMatrix getFeedback(DoubleMatrix r, double t, DoubleMatrix feedback) {
        if (!isClamped) return super.getFeedback(r, t, feedback);
        DoubleMatrix target = new DoubleMatrix(M);
        getTargetReadout(t, target);
        wBack.mmuli(target, feedback);
        return feedback;
    }
    
}