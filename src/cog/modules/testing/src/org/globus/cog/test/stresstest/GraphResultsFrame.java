package org.globus.cog.test.stresstest;


import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.text.NumberFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.XYDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.data.XYDataItem;
import org.jfree.data.Range;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;
import org.jfree.chart.plot.CombinedDomainXYPlot;


class GraphResultsFrame extends ApplicationFrame {

    /*If constantScale is true, use firstDate and lastDate to create x-axis of
      graph; when new data comes in, graph will scroll to show it.  Otherwise,
      let JFreeChart automatically rescale graph to fit all data points.*/
    boolean constantScale;
    long firstDate, dateRange;
    DateAxis timeAxis;

    private XYSeriesCollection datasets[];

    //Use this constructor for an automatically-scaled graph:
    public GraphResultsFrame(String title, String[] hostNameList) {
        super(title);       
 
	constantScale = false;
	firstDate = dateRange = 0;
	createDataset(hostNameList);
	setUpFrame(createDynamicChart(hostNameList));
    }

    //Use this constructor to specify fixed scale for graph:
    public GraphResultsFrame(String title, String[] hostNameList,
			     long firstDate, long dateRange) {
	super(title);
	
	this.constantScale = true;
	this.firstDate = firstDate;
	this.dateRange = dateRange;
	createDataset(hostNameList);
	setUpFrame(createDynamicChart(hostNameList));
    }

    /*As the test runs, call this function to add points.
      jobFinishedCode is either 0 (failure) or 1 (success).*/
    public void addDataPoint(int hostIndex, Date when, int jobFinishedCode) {
	XYSeries theSeries = datasets[hostIndex].getSeries(0);
	theSeries.add(new XYDataItem(when.getTime(), 
				     (double)jobFinishedCode), 
		      true); //The true means notify listeners

	//If fixed-scale graph, scroll to fit new point:
	if (constantScale && when.getTime() > (firstDate + dateRange))
	    scrollToInclude(when);
    }



    /****************************************
     *  Internal Utility Functions (private)
     ****************************************/

    private void setUpFrame(JFreeChart chart) {
	ChartPanel chartPanel;

	chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
	pack();
	setVisible(true);
    }
    
    private void createDataset(String[] hostNameList) {

	XYSeries aSeries;
	int t;

	/*Creates one XYSeriesCollection for each host,
	  with a single XYSeries in each Collection.
	  It would seem more reasonable to have one XYSeries for
	  each host, all in a single collection, but the way the 
	  CombinedDomanyXYChart works forces me to do it this way.*/
	datasets = new XYSeriesCollection[hostNameList.length];

	for (t=0; t<hostNameList.length; t++) {
	    datasets[t] = new XYSeriesCollection();
	    aSeries = new XYSeries(hostNameList[t]);
	    datasets[t].addSeries(aSeries);
	}
	
    }

    private void scrollToInclude(Date when) {
	int k;
	long scrollFactor;

	scrollFactor = when.getTime() - (firstDate + dateRange);

	firstDate += scrollFactor;
	timeAxis.setRange(new Range(firstDate, firstDate+dateRange));
	
    }

    /*This function does most of the work.
    It needs to be cleaned up.*/
    private JFreeChart createDynamicChart(String[] hostNameList) {

	// chart title and axis labels...
	final String title = "Remote Grid Testing";
	final String domainAxisLabel = "Time";

	int numberOfHosts = hostNameList.length;
	int i;
	
	// create common time axis
	timeAxis = new DateAxis(domainAxisLabel);
	timeAxis.setDateFormatOverride(DateFormat.getTimeInstance());
	timeAxis.setTickMarksVisible(true);
	
	
	if (constantScale)
	    timeAxis.setRange(new Range(firstDate, firstDate+dateRange));
	/*If user has given the noscale flag, call setRange on the timeAxis.
	  which also turns off the auto-range flag. */
	
	// make one vertical axis for each (vertical) chart
	// vertical axis is fixed to include 0 (failure) and 1 (success)
	final NumberAxis[] valueAxis = new NumberAxis[numberOfHosts];
	for (i = 0; i < numberOfHosts; i++) {
	    valueAxis[i] = new NumberAxis();
	    valueAxis[i].setLowerMargin(0.2);
	    valueAxis[i].setUpperMargin(0.2);
	    valueAxis[i].setRangeWithMargins(new Range(0.0, 1.0));
	    valueAxis[i].setTickUnit(new NumberTickUnit(1.0, 
				     NumberFormat.getIntegerInstance()));
	}
	
	final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(timeAxis);
	
	
	StandardXYItemRenderer[] rendererArray = new StandardXYItemRenderer[numberOfHosts];
	XYPlot[] subPlotArray = new XYPlot[numberOfHosts];
	
	for (i = 0; i<numberOfHosts; i++) {
	    rendererArray[i] = new StandardXYItemRenderer();
	    rendererArray[i].setPlotShapes(true);
	    rendererArray[i].setShapesFilled(true);

	    subPlotArray[i] = new XYPlot(datasets[i], null, valueAxis[i], rendererArray[i]);
	    plot.add(subPlotArray[i], 1); //the 1 is weight of the subplot.
	}


	final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);

	final StandardLegend sl = (StandardLegend) chart.getLegend();
        sl.setDisplaySeriesShapes(true);
 
	final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            rr.setPlotShapes(true);
            rr.setShapesFilled(true);
            rr.setItemLabelsVisible(true);
	}
	
	return chart;
	
    }
}
