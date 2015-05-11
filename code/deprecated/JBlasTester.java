import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class JBlasTester {
    
    public static void main(String[] args) {
        Stopwatch sw;
        
        DoubleMatrix m1 = new DoubleMatrix(2, 2, 1.0, 2.0, 0.0, 3.0);
        DoubleMatrix m2 = new DoubleMatrix(2, 2, 1.0, 0.0, 0.0, 2.0);
        
        StdOut.println(m1);
        StdOut.println(m2);
        
        sw = new Stopwatch();
        StdOut.println(m1.mmul(m2));
        StdOut.println(sw.elapsedTime());
        
        StdOut.println(m1);
        
        sw = new Stopwatch();
        StdOut.println(m1.mmuli(m2));
        StdOut.println(sw.elapsedTime());
                
        StdOut.println(m1);
        
        /////////////////
        
        m1 = new DoubleMatrix(2, 2, 1.0, 2.0, 0.0, 3.0);
        m2 = new DoubleMatrix(2, 2, 1.0, 0.0, 0.0, 2.0);
        
        StdOut.println();
        StdOut.println(m1);
        StdOut.println(m2);
        StdOut.println(m1.fill(0.0).addi(0.01));  //x_est.copy(x).addi(k.muli(0.5*dt))
        StdOut.println(m1);
        StdOut.println(m2);
        
        /////////////////
        
        m1 = new DoubleMatrix(2, 2, 1.0, 2.0, 0.0, 3.0);
        
        StdOut.println();
        StdOut.println(m1);
        m2 = MatrixFunctions.tanh(m1);
        StdOut.println(MatrixFunctions.tanh(m1));        
        StdOut.println(m1);
        StdOut.println(m2);
    }
    
}

/*
 *  1 2    1 0 
 *  0 3    0 2
 */