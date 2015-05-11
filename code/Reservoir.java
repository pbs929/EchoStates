/*************************************************************************
 *  Compilation:  javac Reservoir.java
 *  Author:  Phillip B. Schafer
 *  Last update:  March 2012
 * 
 *  Implements a sparsely connected reservoir of N rate-model neurons. 
 *  For use in the ESN project.  Updated 3-24-14 to use jBlas.
 * 
 *  Add options for using a different integrator?
 * 
 *  Dependencies: 
 *   JBlas library
 *   StdRandom, Stopwatch (Algs-4)
 *************************************************************************/
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

/**
 *  Implements a sparsely connected reservoir of N rate-model neurons.  
 *  Connection weights are sparse and drawn from a normal distribution; 
 *  they are represented by a 2D matrix (JBlas library) and scaled
 *  by g/sqrt(N*p) (Sussillo & Abbott 2009, p. 556).
 *  Activity is represented as a 1D matrix of firing rates.
 *  <p>
 *  Dynamics are those of a leaky integrator.  
 *  Feedback into the network is optionally provided by a 
 *  <nn>Streams.Feedback</nn> object.  
 *  Input is optionally provided by a <nn>Streams.Input</nn> object.  
 */
public class Reservoir {
    protected int    N;          // number of neurons
    protected double dt;         // integration step size
    protected double t;          // the current time
    protected double tau;        // neural time constant
    
    protected DoubleMatrix x;    // subthreshold states of the neurons
    protected DoubleMatrix r;    // firing rates of the neurons
    protected DoubleMatrix W;    // connection matrix
    protected boolean rIsSet;    // for caching - current r was computed?
    
    protected NetworkEq dynEq;   // nested dynamical equation class
    protected VectorODESolver.Integrator integrator;
    protected Streams.InputStream input;
    protected Streams.Feedback feedback;
    
    /**
     * Initialize a network of N neurons with integration step size dt 
     * and with small, random initial activity.
     * @param N the number of neurons
     * @param p the probability of a pairwise connection (sparsity parameter)
     * @param g weighting factor for connectivity
     * @param tau the time constant of the neurons
     * @param dt the integration time step
     * @throws IllegalArgumentException if <tt>N</tt> is less than one
     * @throws IllegalArgumentException if <tt>p</tt> is not between 0 and 1
     * @throws IllegalArgumentException if <tt>g</tt> or <tt>dt</tt> is infinite or NaN
     * @throws IllegalArgumentException if <tt>tau</tt> is nonpositive, infinite, or NaN
     */
    public Reservoir(int N, double p, double g, double tau, double dt) {
        if (N <= 0)
            throw new IllegalArgumentException("N must be greater than 0");
        if (!(p >= 0 && p <= 1))
            throw new IllegalArgumentException("p must be in [0,1]");
        if (Double.isInfinite(g) || Double.isNaN(g))
            throw new IllegalArgumentException("dt must be a finite number");
        if (Double.isInfinite(tau) || Double.isNaN(tau) || tau <=0)
            throw new IllegalArgumentException("tau must be a positive finite number");
        if (Double.isInfinite(dt) || Double.isNaN(dt))
            throw new IllegalArgumentException("dt must be a finite number");
        
        this.N   = N;
        this.dt  = dt;
        this.tau = tau;
        this.t   = 0.0;
        integrator = new VectorODESolver.RKIntegrator(N);
        dynEq      = new NetworkEq(N);
        
        // set initial conditions - small initial activity
        x = DoubleMatrix.randn(N).muli(0.1);
        r = new DoubleMatrix(N); // will set this only when necessary 
        rIsSet = false;
        
        // set sparse connection weights
        W = DoubleMatrix.randn(N, N).muli(g/Math.sqrt(N*p));
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (StdRandom.uniform(0.0, 1.0) > p) 
                    W.put(i, j, 0.0);
            }
        }
    }   
    
    /**
     * Initialize a network with default parameters: 
     * N = 1000; p = 0.1; g = 1.5; tau = 0.01; dt = 0.001
     */
    public Reservoir() {
        this(1000);  
    }
    
    public Reservoir(int N) {
        this(N, 0.1);
    }
    
    public Reservoir(int N, double p) {
        this(N, p, 1.5);
    }
    
    public Reservoir(int N, double p, double g) {
        this(N, p, g, 0.01, 0.001);
    }
    
    /**********************************************************************
     *  Set/Get
     **********************************************************************/
    /** 
     * Get the number of neurons of the network.
     * @return the number of neurons
     */
    public int size() {
        return N;
    }
    
    /** 
     * Get the step size.
     * @return the step size
     */
    public double dt() {
        return dt;
    }
    
    /** 
     * Set the step size.
     * @return the step size
     */
    public void set_dt(double dt) {
        this.dt = dt;
    }
    
    /** 
     * Get the current simulation time. 
     * @return the simulation time
     */
    public double t() {
        return t;
    }
       
    /** 
     * Get the weight matrix
     * @param container for the weight matrix (to be overwritten)
     * @return the weight matrix (for convenience)
     */
    public DoubleMatrix getW(DoubleMatrix Wout) {
        Wout.copy(W);
        return Wout;
    }
    
    /** 
     * Set the weight matrix
     * @param the weight matrix
     */
    public void setW(DoubleMatrix Win) {
        W.assertSameSize(Win);
        W.copy(Win);
    }
    
    /** 
     * Set the network state
     * @param the new network state
     */
    public void setX(DoubleMatrix x) {
        this.x.assertSameSize(x);
        this.x.copy(x);
        rIsSet = false;
    }
    
    /**********************************************************************
     *  Readout methods
     **********************************************************************/
    /** 
     * Display the state of the network.
     */
    public String toString() { 
        setR(); 
        return r.toString();
    }
    
    /** 
     * Return a copy of the state r.  
     * @param rOut the vector to be written to
     * @returns rOut, for convenience only
     */
    public DoubleMatrix getR(DoubleMatrix rOut) {
        setR();  
        rOut.copy(r);
        return rOut; 
    }
    
    /** 
     * Return the state r as a double array.  
     * @returns the state r
     */
    public double[] getRArray() {
        setR();
        return r.toArray();
    }
    
    // cache the value of r if necessary
    private void setR() {
        if (!rIsSet) {
            applyNonlin(x, r); 
            rIsSet = true; 
        }
    }
    
    /**********************************************************************
     *  Set input and feedback streams
     **********************************************************************/
    /** 
     * Set the (optional) input stream for the network.  
     * An input of <tt>null</tt> turns off the input stream.  
     * @param in the input stream
     * @throws IllegalArgumentException if stream is the wrong size
     */
    public void setInput(Streams.InputStream in) {
        if (in == null) {
            dynEq.inputOn = false;
            return;
        }
        if (in.size() != N)
            throw new IllegalArgumentException("size of input stream must = N");
        input = in;
        dynEq.inputOn = true;
    }
    
    /** 
     * Set the (optional) input stream for the network.  
     * An input of <tt>null</tt> turns off the input stream.  
     * @param in the input stream
     * @throws IllegalArgumentException if stream is the wrong size
     */
    public void setFeedback(Streams.Feedback fb) {
        if (fb == null) {
            dynEq.feedbackOn = false;
            return;
        }
        if (fb.fb_size() != N)
            throw new IllegalArgumentException("size of feedback stream must = N");
        feedback = fb;
        dynEq.feedbackOn = true;
    }
    
    /**********************************************************************
     *  Stepping the network
     **********************************************************************/
    /** 
     * Perform one integration time-step.
     */
    public void step() {
        integrator.Step(x, t, dynEq, dt);  // x += dx
        rIsSet = false;
        t += dt;
    }   
    
    /** 
     * Perform one or more integration time-steps.
     * @param nSteps the number of steps to take
     */
    public void step(int nSteps) {
        for (int i = 0; i < nSteps; i++) 
            step();
    }
    
    /**********************************************************************
     *  Dynamical equation for network:
     *  x'(x,t) = (-x + W*tanh(x) + input(t) + feedback(x,t))/tau
     **********************************************************************/
    private class NetworkEq implements VectorODESolver.DynamicalEquation {
        private boolean inputOn    = false;
        private boolean feedbackOn = false;
        private DoubleMatrix rr;   // pre-allocate for speed
        private int N;
        public NetworkEq(int N) {
            rr = new DoubleMatrix(N);
            this.N = N;
        }
        public int N() {
            return N;
        }
        // Return the time derivative of the subthreshold state
        public DoubleMatrix timeDeriv(DoubleMatrix xx, double tt, DoubleMatrix deriv) {
            rr.assertSameSize(xx);
            rr.assertSameSize(deriv);
            applyNonlin(xx, rr);                        // get firing rates
            W.mmuli(rr, deriv);                         // get recurrent input
            if (inputOn) {                              // add external input
                assert (input != null) : "input was not defined";
                DoubleMatrix ro = new DoubleMatrix(N);
                input.getInput(tt, ro);
                deriv.addi(ro);    
            }
            if (feedbackOn){                            // add feedback
                assert (feedback != null) : "feedback was not defined";
                DoubleMatrix fb = new DoubleMatrix(N);
                feedback.getFeedback(rr, tt, fb);
                deriv.addi(fb); 
            }
            deriv.subi(x);                               // add leak term
            return deriv.divi(tau);                      
        }
    };
    
    /**********************************************************************
     *  Static nonlinearity
     **********************************************************************/
    // input x, output r
    private void applyNonlin(DoubleMatrix x, DoubleMatrix r) {
        r.copy(MatrixFunctions.tanh(x));
    }
    
    /********************************************************************** 
     *  Unit testing
     **********************************************************************/
    public static void main(String[] args) {
        
        StdOut.println("******************** Testing constructor *********************");
                
        System.out.println("Test variance of connection weights");
        int N = 100; 
        double p = 0.1;
        double g = 1.5;
        Reservoir res = new Reservoir(N, p, g);
        //StdOut.println(res.W);
        StdOut.println("Estimated variance - should be g^2/(Np)= ");
        StdOut.println(g*g/(N*p));
        StdOut.println("Is actually:");
        StdOut.println(res.W.mul(res.W).sum()/(N*N*p));
        
        
        System.out.println("\nTest reservoir with N=5, sparsity=0.5 ...");
        res = new Reservoir(5, 0.5);
        StdOut.println("W = ");
        StdOut.println(res.W);
        StdOut.println("x = ");
        StdOut.println(res.x);
        
        StdOut.println("******************** Testing set and get *********************");
        res = new Reservoir(3, 0.5);
        DoubleMatrix testW = new DoubleMatrix();
        res.getW(testW);              // test getW method
        assert(testW.equals(res.W));
        testW = new DoubleMatrix();   // check that internal W is protected
        assert(!testW.equals(res.W));
        
        testW = new DoubleMatrix(new double[][] {{1,0,1},{0,1,0},{1,0,1}});
        res.setW(testW);              // test setW method
        assert(testW.equals(res.W));
        testW = new DoubleMatrix();   // check that internal W is protected
        assert(!testW.equals(res.W));
        
        DoubleMatrix testX = new DoubleMatrix(new double[] {1,0,1});
        res.setX(testX);              // test setX method
        assert(testX.equals(res.x));
        testX = new DoubleMatrix();   // check that internal x is protected
        assert(!testX.equals(res.x));
        
        StdOut.println("******************** Testing readout methods *********************");
        res = new Reservoir(3, 0.5);
        res.setR();                   // private setR method
        assert(res.r.equals(MatrixFunctions.tanh(res.x))); // test nonlinearity
        
        StdOut.println(res.r);
        StdOut.println(res);          // toString() method
        res = new Reservoir(3, 0.5);
        
        DoubleMatrix testR = new DoubleMatrix();
        assert(!res.rIsSet);
        res.getR(testR);              // test getR method
        assert(res.rIsSet);
        assert(testR.equals(res.r));
        testR = new DoubleMatrix();   // check that internal r is protected
        assert(!testR.equals(res.r));
        
        StdOut.println("******************** Testing stepping methods *********************");
        
        res = new Reservoir(3, 0.5);
        testR = new DoubleMatrix();
        testX = new DoubleMatrix();
        testX.copy(res.x);
        res.getR(testR);
        res.step();                    // perform step
        assert(!testX.equals(res.x));  // x updated
        assert(testR.equals(res.r));   // r not yet updated
        testX.copy(res.x);
        res.setR();                    // update r
        assert(testX.equals(res.x));   // x not updated
        assert(!testR.equals(res.r));  // r updated
        
        StdOut.println("******************** Testing run times *********************");
        
        int n = 100; 
        double sparsity = 0.1; 
        
        // Test constructor speed
        StdOut.println("Testing run time for matrix construction...");
        Stopwatch watch = new Stopwatch();
        res = new Reservoir(n, sparsity); 
        StdOut.println("time: " + watch.elapsedTime());
        
        // test multiplication speed
        StdOut.println("Testing run time for matrix multiplication...");
        Reservoir res1 = new Reservoir(n, sparsity); 
        Reservoir res2 = new Reservoir(n, sparsity); 
        watch = new Stopwatch();
        res1.W.mmuli(res2.W, new DoubleMatrix(res1.N, res1.N));
        StdOut.println("time: " + watch.elapsedTime());
        
        // try some steps
        StdOut.println("Testing run time for 100 steps...");
        watch = new Stopwatch();
        int nSteps = 100;
        for (int i = 0; i < nSteps; i++)
            res.step();
        StdOut.println("time: " + watch.elapsedTime());
        
        StdOut.println("******************** Testing network dynamics *********************");
        
        // test unconnected network 
        StdOut.println("Testing unconnected network (g=0) - activity should die");
        res = new Reservoir(4, 0.5, 0.0, 0.1, 0.01); // N, sparsity, g, tau, dt
        StdOut.println("Connection matrix:");
        StdOut.println(res.W);
        StdOut.println("Initial state:");
        StdOut.println(res);
        nSteps = 100;
        for (int i = 0; i < nSteps; i++)
            res.step();
        StdOut.println("Final state:");
        StdOut.println(res);
        
        // test positively connected network
        StdOut.println("\nTesting positively connected network - activity should grow");
        res = new Reservoir(3); // N, sparsity, g, tau, dt
        res.setW(new DoubleMatrix(new double[][] {{1,1,1},{1,1,1},{1,1,1}}));
        StdOut.println("Initial state:");
        StdOut.println(res);
        nSteps = 100;
        for (int i = 0; i < nSteps; i++)
            res.step();
        StdOut.println("Final state:");
        StdOut.println(res);        
        
        // test chaotic network
        // Given small initial conditions the chaotic activity should grow 
        // to fill the range [-1,1].  (Need at least ~100 neurons)
        StdOut.println("\nTesting strongly connected network (g = 1.5, default) - activity should grow chaotically");
        res = new Reservoir(100, 0.1, 1.5, 0.1, 0.01); // N, sparsity, g, tau, dt
        StdOut.println("Initial state:");
        StdOut.println(res);
        nSteps = 100;
        for (int i = 0; i < nSteps; i++)
            res.step();
        StdOut.println("Final state:");
        StdOut.println(res);        
        
        // Test non-random network
        StdOut.println("\nTesting non-random network");
        res = new Reservoir(3, 0.5); // N, p
        DoubleMatrix ww = new DoubleMatrix(0);
        DoubleMatrix xx = new DoubleMatrix(0);
        ww = new DoubleMatrix(new double[][] {{0,1.5,0},{0,0,1.5},{1.5,0,0}});
        xx = new DoubleMatrix(new double[] {1,0,0});
        res.setW(ww);
        res.setX(xx);
        StdOut.println("Initial state:");
        StdOut.println(res);
        nSteps = 100;
        for (int i = 0; i < nSteps; i++) {
            res.step();
            //StdOut.println(res);        
        }
        StdOut.println("Final state:");
        StdOut.println(res);        
        
    }
    
}