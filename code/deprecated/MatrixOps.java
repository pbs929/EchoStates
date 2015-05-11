import cern.colt.matrix.tdouble.*;          // Parallel Colt
import cern.colt.matrix.tdouble.impl.*;
import cern.colt.function.tdouble.*;
import cern.jet.math.tdouble.*;

public class MatrixOps{
    // implement sum operation for matrix addition
    public static DoubleDoubleFunction plus = new DoubleDoubleFunction() {
        public double apply(double a, double b) { return a + b; }
    };       
    
    // implement minus operation for matrix subtraction
    public static DoubleDoubleFunction minus = new DoubleDoubleFunction() {
        public double apply(double a, double b) { return a - b; }
    };    
    
    // implement tanh operation for matrices
    public static DoubleFunction tanh = new DoubleFunction() {
        public double apply(double a) { return Math.tanh(a); }
    };    
}