import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class Streams {
    
    public interface InputStream {
        int size();
        DoubleMatrix getInput(double t, DoubleMatrix input);
    }
    
    public interface Feedback {
        int fb_size();  // number of feedback streams (= N_reservoir)
        DoubleMatrix getFeedback(DoubleMatrix r, double t, DoubleMatrix feedback);
    }
    
    /** Sine wave input stream with range [-1, 1] and period T.  */ 
    public static class SineWave implements InputStream {
        double T;
        /** @param T the period of the wave */
        public SineWave(double T) { this.T = T; }
        public int size() { return 1; }
        public DoubleMatrix getInput(double t, DoubleMatrix input){
            return input.fill( Math.sin(2*Math.PI*t/T) ); } 
    }
    
    /** Triangle wave input stream with range [-1, 1] and period T.  */ 
    public static class TriangleWave implements InputStream {
        double T;
        /** @param T the period of the wave */
        public TriangleWave(double T) { this.T = T; }
        public int size() { return 1; }
        public DoubleMatrix getInput(double t, DoubleMatrix input){
            return input.fill( Math.abs(4*(t/T % 1) - 2) - 1 ); } 
    }
    
    /** Square wave input stream with range [-1, 1] and period T.  */ 
    public static class SquareWave implements InputStream {
        double T;
        /** @param T the period of the wave */
        public SquareWave(double T) { this.T = T; }
        public int size() { return 1; }
        public DoubleMatrix getInput(double t, DoubleMatrix input){
            return input.fill( 2*Math.floor(2*(t/T % 1)) - 1 ); } 
        
    }
    /** Saw tooth input stream with range [-1, 1] and period T.  */ 
    public static class SawTooth implements InputStream {
        double T;
        /** @param T the period of the wave */
        public SawTooth(double T) { this.T = T; }
        public int size() { return 1; }
        public DoubleMatrix getInput(double t, DoubleMatrix input){
            return input.fill( (2*(t/T % 1)) - 1 ); } 
    }

    /** Sum of sines */ 
    public static class DoubleSineWave implements InputStream {
        double T;
        /** @param T the period of the wave */
        public DoubleSineWave(double T) { this.T = T; }
        public int size() { return 1; }
        public DoubleMatrix getInput(double t, DoubleMatrix input){
            return input.fill( Math.sin(2*Math.PI*t/T) + Math.sin(Math.PI*t/T) ); 
        } 
    }
    
    public static void main(String[] args) {
        Streams.InputStream stream = new Streams.SineWave(1.5);
        DoubleMatrix vals = new DoubleMatrix(stream.size());
        for (double t = 0; t < 6; t += 0.1)
            StdOut.println(stream.getInput(t, vals));
    }

}