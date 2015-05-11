/*************************************************************************
 *  Plotting using the jFreeChart library.  
 *************************************************************************/
import java.util.ArrayList;
import java.awt.Color;
import java.awt.BasicStroke;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import java.io.IOException;
import java.io.File; 

import org.jfree.ui.*;
import org.jfree.chart.title.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.renderer.category.*;

public class Plotting_optionsDemo {
    /**
     * Make a plot containing several time series.  
     * Input is an N-dimensional ArrayList of nT-dimensional ArrayLists, 
     * where N is the number of series to be plotted and nT is the number 
     * of time points in a series.  
     * The t-values are computed for each series using the input dt.   
     * @param timeSeries the ArrayList of ArrayLists.
     * 
     */
    public static void PlotTimeSeries(ArrayList<ArrayList<Double>> timeSeries, double dt) {
        if (timeSeries.size() == 0) return;
        
        // create the dataset ---------------------------------------------  
        XYSeriesCollection dataset = new XYSeriesCollection(); 
        for (int i = 0; i < timeSeries.size(); i++) {
            XYSeries series = new XYSeries(Integer.toString(i));
            dataset.addSeries(series); 
            for(int j = 0; j < timeSeries.get(i).size(); j++){
                series.add(j*dt, timeSeries.get(i).get(j)); // add points
            }
        }
        
        // create a chart and set its properties --------------------------
        JFreeChart chart = ChartFactory.createXYLineChart(
            "XY Chart", // Title
            "x-axis",   // x-axis Label
            "y-axis",   // y-axis Label
            dataset,    // Dataset
            PlotOrientation.VERTICAL, // Plot Orientation
            true,       // Show Legend
            true,       // Use tooltips
            false       // Configure chart to generate URLs?
            ); 
        chart.setBackgroundPaint(Color.white);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        //plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0)); plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        //plot.setDomainCrosshairVisible(true);
        //plot.setRangeCrosshairVisible(true);
        
        plot.setDomainPannable(true); // for the GUI
        plot.setDomainPannable(true);
        
        //final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        //rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());   // use only integer tick marks
        //rangeAxis.setAutoRangeIncludesZero(true);
        //rangeAxis.setUpperMargin(0.12);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            //renderer.setBaseShapesVisible(true);
            //renderer.setBaseShapesFilled(true);
            //renderer.setDrawSeriesLineAsPath(true);
        }
        
        //LegendTitle legend = chart.getLegend();
        //legend.setPosition(RectangleEdge.LEFT);
        
        //To adjust the colors used for the series: 
        //CategoryItemRenderer renderer = plot.getRenderer();
        //renderer.setSeriesPaint(0, Color.green);
        //renderer.setSeriesPaint(1, Color.red);
        
        // Print to file --------------------------------------------------
        //try {
        //    ChartUtilities.saveChartAsJPEG(new File(".\\chart2.jpg"), chart, 500, 300);
        //} catch (IOException e) {
        //    System.err.println("Problem occurred creating chart.");
        //} 
        
        // Display to screen ----------------------------------------------
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        
        ApplicationFrame frame = new ApplicationFrame("this is a title");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
        
    }
    
}

/* advanced stuff: customizing the shapes
        final Shape[] shapes = new Shape[3];
        int[] xpoints;
        int[] ypoints;

        // right-pointing triangle
        xpoints = new int[] {-3, 3, -3};
        ypoints = new int[] {-3, 0, 3};
        shapes[0] = new Polygon(xpoints, ypoints, 3);

        // vertical rectangle
        shapes[1] = new Rectangle2D.Double(-2, -3, 3, 6);

        // left-pointing triangle
        xpoints = new int[] {-3, 3, 3};
        ypoints = new int[] {0, -3, 3};
        shapes[2] = new Polygon(xpoints, ypoints, 3);

        final DrawingSupplier supplier = new DefaultDrawingSupplier(
            DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
            shapes
        );
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setDrawingSupplier(supplier);

 */