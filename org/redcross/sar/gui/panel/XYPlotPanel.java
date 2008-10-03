package org.redcross.sar.gui.panel;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.redcross.sar.util.Utils;

public class XYPlotPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	
	public XYPlotPanel() {
		// forward
		super();
	}
	
	public XYPlotPanel(String caption) {
		// forward
		super(caption);
	}
	
	/**
	 * This method initializes ChartPanel
	 * 	
	 * @return {@link ChartPanel}
	 */
	private ChartPanel createChartPanel(JFreeChart chart) {
		return new ChartPanel(chart);
	}
	
	/**
	 * This method initializes Chart
	 * 	
	 * @return {@link JFreeChart}
	 */
	private JFreeChart createChart(String title, XYPlot plot) {
		return new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	}

	
	/**
	 * This method create a new Plot
	 * 	
	 * @return {@link XYPlot}
	 */
	private XYPlot createXYPlot(XYDataset dataset, String xLabel, String yLabel) {
		ValueAxis xAxis = new NumberAxis(xLabel);
		ValueAxis yAxis = new NumberAxis(yLabel);
		StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES_AND_LINES);
        renderer.setSeriesItemLabelsVisible(0, true);
        Shape circle = new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0);
        renderer.setSeriesShape(0, circle);
        renderer.setAutoPopulateSeriesShape(false);
        renderer.setAutoPopulateSeriesPaint(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		return plot;
	}
	
	/**
	 * This method plots passed data 
	 * 
	 * @return True if data was plotted
	 */	
	public boolean plot(String title, String xLabel, String yLabel, double x[], double y[]) {
		
		// declare variables
		int yCount, xCount;
		
		// get dimensions
		xCount = x.length;
		yCount = y.length;
		
		// valid data?
		if(yCount>0 && xCount==yCount) {

			// create series
	        XYSeries series = new XYSeries("plot");
	        
			// loop over all y - values
			for(int i=0;i<xCount;i++) {
				series.add(x[i],y[i]);
			}
			
	        // create dataset
	        XYSeriesCollection dataset = new XYSeriesCollection(series);
	        
	        // create plot
	        XYPlot plot = createXYPlot(dataset, xLabel, yLabel);
	       
	        // add to plot dataset
	        plot.setDataset(dataset);
	        
	        // get chart
	        JFreeChart chart = createChart(title, plot);

	        // get chart panel
	        ChartPanel panel = createChartPanel(chart);
	        
	        // set as body panel
	        setBodyComponent(panel);
	        
	        // set fixed size equal available body size
	        Utils.setFixedSize(panel,getWidth(),getHeight()-getHeaderPanel().getHeight());
			
			// success
			return true;
		}
		
		// failed
		return false;
	}
	
}
