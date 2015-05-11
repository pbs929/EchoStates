import cern.colt.matrix.tdouble.algo.*;

import cern.colt.matrix.tdouble.*;          // Parallel Colt
import cern.colt.matrix.tdouble.impl.*;
import cern.colt.function.tdouble.*;
import cern.jet.math.tdouble.*;

public class test {
    
    public static void main(String[] args) {
        
        DoubleMatrix2D mat = new DenseDoubleMatrix2D(new double[][] {{1, 2}, {3, 4}});
        DoubleMatrix2D inv = new DenseDoubleMatrix2D(2, 2);
        DoubleMatrix2D prod = new DenseDoubleMatrix2D(2, 2);
        
        DenseDoubleAlgebra alg = new DenseDoubleAlgebra();
        
        inv = alg.inverse(mat);
        mat.zMult(inv, prod);
        
        StdOut.println(mat);
        StdOut.println(inv);
        StdOut.println(prod);
        
    }
    
}