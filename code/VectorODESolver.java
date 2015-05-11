/*************************************************************************
 *  Compilation:  javac VectorODESolver.java 
 *  Author:  Phillip B. Schafer
 *  Last update:  March 2012
 * 
 *  Interfaces and solvers for vector ODEs
 * 
 *  Dependencies: 
 *   JBlas library
 *************************************************************************/
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import cern.colt.matrix.tdouble.*;          // Parallel Colt
import cern.colt.matrix.tdouble.impl.*;
import cern.colt.function.tdouble.*;
import cern.jet.math.tdouble.*;

/**
 *  Interfaces and methods for solving vector ODEs.  
 *  The interface <nn>Integrator</nn> defines a solver for stepping a system 
 *  based on its dynamical equation.  
 *  The dynamical equation is defined by the interface 
 *  <nn>DynamicalEquation</nn>.
 *  Also included are two implementations of the Integrator, one using Euler's
 *  method and one using 4th-order Runge Kutta.  
 */
public class VectorODESolver {
    
    /************************************************************************
     *  Interfaces
     ************************************************************************/
    
    /**
     * Interface for the dynamical equation object used as input to the
     * numerical solvers. 
     */
    public interface DynamicalEquation {
        /**
         * Returns a time derivative based on the current state x
         * @param x the current state of the system
         * @param t the current simulation time
         * @param deriv the vector to be returned (overwritten)
         * @return deriv (for convenience only)
         */
        public DoubleMatrix timeDeriv(DoubleMatrix x, double t, DoubleMatrix deriv);
        /**
         * The dimension of the output time derivative
         * @return the dimension
         */
        public int N();
    }
    
    /**
     * Interface for a numerical solver. 
     */
    public interface Integrator {   
        /**
         * Performs a single time step of integration.  
         * @param x the current state
         * @param t the current time
         * @param dynEq the dynamical equation for computing the next state
         * @dt the length of the time step
         */
        void Step(DoubleMatrix x, double t, DynamicalEquation dynEq, double dt);
    }
    
    
    /************************************************************************
     *  Integrators
     ************************************************************************/
    /**
     * Euler solver (1st order RK), performs one time step.
     * The size of the network is specified on construction.  
     * Be sure to provide the correct dimension x and dynEq. (Param checking?)
     * @param x the current state
     * @param dynEq the dynamical equation for computing the next state
     * @dt the length of the time step
     */
    public static class EulerIntegrator implements Integrator {
        private DoubleMatrix deriv;
        private int N;
        public EulerIntegrator(int N) {
            if (N <= 0) throw new IllegalArgumentException("N <= 0");
            this.N = N;
            deriv  = new DoubleMatrix(N);
        }
        public void Step(DoubleMatrix x, double t, DynamicalEquation dynEq, double dt) {
            if (dynEq.N() != N) 
                throw new IllegalArgumentException("Dimension of dynamical equation does not match integrator");
            if (x.rows != N || x.columns != 1) 
                throw new IllegalArgumentException("Dimension of system state does not match integrator");
            dynEq.timeDeriv(x, t, deriv);      // get deriv (overwrite)
            x.addi(deriv.muli(dt));            // x += deriv*dt
        }
    }
    
    /**
     * 4th-order Runge-Kutta solver, performs one time step.  
     * The size of the network is specified on construction.  
     * Be sure to provide the correct dimension x and dynEq. (Param checking?)
     * @param x the current state, to be updated
     * @param dynEq the dynamical equation for computing the next state
     * @dt the length of the time step
     */
    public static class RKIntegrator implements Integrator {        
        private DoubleMatrix k;
        private DoubleMatrix x_est;
        private DoubleMatrix deriv;
        private int N;
        public RKIntegrator(int N) {
            if (N <= 0) throw new IllegalArgumentException("N <= 0");
            this.N = N;
            k      = new DoubleMatrix(N);
            x_est  = new DoubleMatrix(N);
            deriv  = new DoubleMatrix(N);
        }
        public void Step(DoubleMatrix x, double t, DynamicalEquation dynEq, double dt) {
            if (dynEq.N() != N) 
                throw new IllegalArgumentException("Dimension of dynamical equation does not match integrator");
            if (x.rows != N || x.columns != 1) 
                throw new IllegalArgumentException("Dimension of system state does not match integrator");
            dynEq.timeDeriv(x, t, k);              // k := k1
            deriv.copy(k);                         // deriv := k1
            
            x.addi(k.muli(0.5*dt), x_est);         // x_est := x + 0.5*k1*dt
            dynEq.timeDeriv(x_est, t + 0.5*dt, k); // k := k2
            deriv.addi(k).addi(k);                 // deriv += 2*k2
            
            x.addi(k.muli(0.5*dt), x_est);         // x_est := x + 0.5*k1*dt
            dynEq.timeDeriv(x_est, t + 0.5*dt, k); // k := k3
            deriv.addi(k).addi(k);                 // deriv += 2*k3
            
            x.addi(k.muli(dt), x_est);             // x_est := x + k3*dt
            dynEq.timeDeriv(x_est, t + dt, k);     // k := k4
            deriv.addi(k);                         // deriv += k4
            
            x.addi(deriv.muli(dt/6.0));
        }
    }
}