package org.globus.cog.monitor.guss;

import java.awt.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Date;

import java.sql.SQLException;
import java.sql.Connection;

import org.jfree.chart.JFreeChart;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DefaultCategoryDataset;
import org.jfree.data.XYDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.chart.ChartUtilities;
import org.jfree.data.DatasetUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.renderer.BarRenderer3D;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.ui.Spacer;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.CandlestickRenderer;
import org.jfree.chart.renderer.XYAreaRenderer;


public class ChartMaker {
    private int width;
    private int height;
    private String tempFileDir;

    public ChartMaker(int width, int height, String tempFileDir) {
	this.width = width;
	this.height = height;
	this.tempFileDir = tempFileDir;
    }

    private String chartLabel(int graphQuant, int granularity) {
	switch (granularity) {

	case Calendar.HOUR:
	    return "Hourly";

	case Calendar.DAY_OF_WEEK:
	    return "Daily";

	case Calendar.WEEK_OF_YEAR:
	    return "Weekly";

	}
	return "";
    }
	
    public String saveTimeSeriesChart(TimeSeriesCollection theDataset,
				    int graphQuant, int granularity)
	throws IOException {

	JFreeChart theChart;
	String description;
	description = chartLabel(graphQuant, granularity);
	theChart = volumeVsTimeChart(theDataset, "All hosts", "All hosts",
				     description, graphQuant);	
	return saveImageFile(theChart);
    }

    public String saveImageFile(JFreeChart theChart) throws IOException {
	File tempPngFile;

	tempPngFile = File.createTempFile("GUSS", ".png", new File(tempFileDir));
	ChartUtilities.writeChartAsPNG(new FileOutputStream(tempPngFile),
				       theChart, width, height);
	return tempPngFile.getName();
    }

    //saveImageFile(

    /**
     *Makes a timeSeries chart suitable for displaying, or encoding to .png file, from an XYDataset
     *@param dataset a (possibly multi-series) value-vs-time XYDataset such as the one returned by {@link #timeSeriesDataset}.  (The class XYDataset is from package org.jfree.data)
     *@return a timeSeries of the value over time, displaying one line for each source-destination pair.  (The class JFreeChart is defined in the package {@link <A HREF="http://www.jfree.org">org.jfree.chart</A>})
     */
    JFreeChart volumeVsTimeChart(XYDataset dataset, String srcHost, String destHost,
				 String granularity, int graphQuant) {
	JFreeChart theChart;
	String yAxisName="";
	
	switch (graphQuant) {
	    case GUSSConstants.NUM_HOSTS:
		yAxisName = "Number of Hosts";
		break;
	    case GUSSConstants.NUM_TRANSFERS:
		yAxisName = "Number of Transfers";
		break;
	    case GUSSConstants.TRANSFER_VOLUME:
		yAxisName = "Total Transfer Volume (KB)";
		break;
	    case GUSSConstants.AVG_SPEED:
		yAxisName = "Average Throughput (Bytes/sec)";
		break;
	    case GUSSConstants.AVG_SIZE:
		yAxisName = "Average File Size (Bytes)";
		break;
	    case GUSSConstants.AVG_TIME:
		yAxisName = "Average Transfer Time (seconds)";
		break;
	    case GUSSConstants.AVG_STREAMS:
		yAxisName = "Average Number of Streams Used in Transfers";
		break;
	    case GUSSConstants.NUM_NEW_HOSTS:
		yAxisName = "Number of hosts which began using GridFTP";
		break;

	}
        theChart = ChartFactory.createTimeSeriesChart(
            yAxisName+", " + granularity,
            "Date", yAxisName,
            dataset,
            true,
            true,
            false
        );

        theChart.setBackgroundPaint(Color.white);

        final StandardLegend sl = (StandardLegend) theChart.getLegend();
        sl.setDisplaySeriesShapes(true);

        final XYPlot plot = theChart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
	/*	if (graphQuant == GUSSConstants.TRANSFER_VOLUME) {
	    //make y-axis logarithmic, for transfer volumes only:
	    plot.setRangeAxis(new LogarithmicAxis(yAxisName));
	    }*/
	//If this is a plot of an average, substitute in a candlestick
	//renderer.


	if (graphQuant == GUSSConstants.AVG_SPEED || graphQuant == GUSSConstants.AVG_TIME ||
	    graphQuant == GUSSConstants.AVG_SIZE || graphQuant == GUSSConstants.AVG_STREAMS) {
	    final CandlestickRenderer candles = new CandlestickRenderer();
	    candles.setDrawVolume(false);
	    plot.setRenderer(candles);
	    }
        else {

	    plot.setRenderer(new XYAreaRenderer());
	}
    
//        final DateAxis axis = (DateAxis) plot.getDomainAxis();
//        axis.setDateFormatOverride(new SimpleDateFormat("MM/dd"));
        
        return theChart;	
    }

    /*Create a new TimeSeries by adding the values of each item in a with the
      matching item in b.*/
    TimeSeries sumSeries(TimeSeries a, TimeSeries b) {
	Class timePeriodClass = b.getTimePeriodClass();
	if (!a.getTimePeriodClass().equals(timePeriodClass)) {
	    return null;
	}
	TimeSeries sumSeries = new TimeSeries(b.getName(), timePeriodClass);

	Iterator it;
	for (it = a.getTimePeriods().iterator(); it.hasNext(); ) {

	    RegularTimePeriod x = (RegularTimePeriod)it.next();
	    sumSeries.add(x, a.getValue(x).doubleValue() + b.getValue(x).doubleValue());
	}
	return sumSeries;
    }


    public String saveHistogram(Connection con, Date startDate, Date endDate, int graphQuant) throws SQLException, IOException {
	AggregateSummary as = new AggregateSummary(startDate);
	as.fillFromDB(con, startDate, endDate);
	//OneConnectionPool.closeConnection(con);
	as.display();

	HistogramBucketArray bucketArray = null;
	String caption;
	switch (graphQuant) {
	case GUSSConstants.NUM_TRANSFERS:
	    bucketArray = as.getTransfersPerHourHistogram();
	    caption = "Packets Per Hour";
	    break;
	case GUSSConstants.AVG_STREAMS:
	    bucketArray = as.getStreamHistogram();
	    caption = "Number of Streams Used";
	    break;
	case GUSSConstants.AVG_TIME:
	    bucketArray = as.getTimeHistogram();
	    caption = "Transfer Duration (seconds)";
	    break;
	case GUSSConstants.AVG_SIZE:
	    bucketArray = as.getSizeHistogram();
	    caption = "File Size (MB)";
	    break;
	case GUSSConstants.AVG_SPEED:
	    bucketArray = as.getSpeedHistogram();
	    caption = "Transfer Speed (KB/sec)";
	    break;
	default:
	    throw new IOException("Can't make a histogram of quantity " + graphQuant);
	}

	DefaultCategoryDataset dataset = makeHistogramDataset(bucketArray);	
	JFreeChart theChart = ChartFactory.createBarChart("Histogram of " + caption, caption, "Number of Occurrences", dataset, PlotOrientation.VERTICAL, false, false, false);
	return saveImageFile(theChart);
    }


    /*Create a category plot from a histogram...*/
    DefaultCategoryDataset makeHistogramDataset(HistogramBucketArray theData) {
	DefaultCategoryDataset dcd = new DefaultCategoryDataset();
	String columnKey = "whatever";
	long[] counts = theData.getContents();
	double[] thresholds = theData.getDivisions();

	int i;
	for (i=0; i<thresholds.length; i++) {
	    String rowKey = "<" + thresholds[i];
	    dcd.addValue(counts[i], columnKey, rowKey);
	}
	dcd.addValue(counts[thresholds.length], columnKey, new String(">" + thresholds[thresholds.length-1]));

	return dcd;
    }
    
}
