import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class VectorODETestClient {
    
    private abstract static class TestSys2D implements VectorODESolver.DynamicalEquation {
        protected double dt;
        protected double t = 0;
        protected final DoubleMatrix init = new DoubleMatrix(2);
        public final VectorODESolver.Integrator rkInt    = new VectorODESolver.RKIntegrator(2);
        public final VectorODESolver.Integrator eulerInt = new VectorODESolver.EulerIntegrator(2);
        public TestSys2D(DoubleMatrix state, double dt){
            this.dt   = dt;
            this.init.copy(state);
        }
        public void step() {
            t += dt;
        }
        public int N() {
            return 2;
        }
        public abstract DoubleMatrix exactSolution(DoubleMatrix exact);
        public abstract DoubleMatrix timeDeriv(DoubleMatrix x, double t, DoubleMatrix deriv);
    };
    
    private static class QuadraticSys extends TestSys2D {
        public QuadraticSys(DoubleMatrix state, double dt){
            super(state, dt);
        }
        public DoubleMatrix exactSolution(DoubleMatrix exact) {
            return exact.fill(t*t).addi(init);
        }
        public DoubleMatrix timeDeriv(DoubleMatrix x, double t, DoubleMatrix deriv) {
            return deriv.fill(2*t);
        }
    };
    
    private static class ExponentialSys extends TestSys2D {
        public ExponentialSys(DoubleMatrix state, double dt){
            super(state, dt);
        }
        public DoubleMatrix exactSolution(DoubleMatrix exact) {
            return exact.fill(Math.exp(t)).muli(init);
        }
        public DoubleMatrix timeDeriv(DoubleMatrix x, double t, DoubleMatrix deriv) {
            return deriv.copy(x);
        }
    };
    
    private static class OscillatorSys extends TestSys2D {
        public OscillatorSys(DoubleMatrix state, double dt){
            super(state, dt);
        }
        public DoubleMatrix exactSolution(DoubleMatrix exact) {
            exact.put(0, Math.cos(t));
            exact.put(1, Math.sin(t));
            return exact;
        }
        public DoubleMatrix timeDeriv(DoubleMatrix x, double t, DoubleMatrix deriv) {
            deriv.put(0, -1.0*x.get(1));
            deriv.put(1,      x.get(0));
            return deriv;
        }
    };
    
    public static void main(String[] args) {
        // test integration using dy/dt = t -> y(t) = t^2 + y_0
        double dt = 0.001;           // time step
        DoubleMatrix x  = new DoubleMatrix(new double[] {1.0, 2.0}); // initial conditions
        DoubleMatrix dr = new DoubleMatrix(2);
        
        ExponentialSys testSys = new ExponentialSys(x, dt); 
        //QuadraticSys testSys = new QuadraticSys(x, dt); 
        //OscillatorSys testSys = new OscillatorSys(x, dt); // be sure to use 1,0 as init conds
        
        int nStep = 1000;
        double t = 0.0;
        Stopwatch sw = new Stopwatch();
        for (int step = 0; step < nStep; step++) {
            testSys.rkInt.Step(x, t, testSys, dt);   // update x
            //testSys.eulerInt.Step(x, t, testSys, dt);
            t += dt;
            testSys.step();   // update the exact solution
            
            //StdOut.println("Step " + step);
            //StdOut.println("Computed: ");
            //StdOut.println(x);
            //StdOut.println("Correct: ");
            //DoubleMatrix exact = new DoubleMatrix(2);
            //StdOut.println(testSys.exactSolution(exact));
        }
        StdOut.println(sw.elapsedTime());
        StdOut.println("FINAL...");
        StdOut.println("Computed: ");
        StdOut.println(x);
        StdOut.println("Correct: ");
        DoubleMatrix exact = new DoubleMatrix(2);
        StdOut.println(testSys.exactSolution(exact));
    }
}