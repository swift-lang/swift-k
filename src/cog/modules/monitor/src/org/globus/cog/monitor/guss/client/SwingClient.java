/*Quick-and-dirty Swing client for GUSS service.
  Gets numbers through web service interface and draws graphs locally.*/

package org.globus.cog.monitor.guss;

import org.globus.cog.monitor.guss.subcreated.GUSSSoapBindingStub;
import org.globus.cog.monitor.guss.subcreated.GUSSIF;
import org.globus.cog.monitor.guss.subcreated.GUSSIFServiceLocator;
import java.net.URL;
import java.util.LinkedList;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import org.jfree.chart.JFreeChart;
import org.jfree.data.XYDataset;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Hour;
import org.jfree.chart.ChartPanel;


import javax.swing.*;
import java.awt.Dimension;
import java.awt.*;

public class SwingClient extends JFrame {
    /*These constants copied (bad) from GUSSIF because, due to a namespace
    conflict, GUSSClient can't resolve these constants in GUSSIF.*/
    public static final int TIMESERIES = 1;
    public static final int CATEGORY_PLOT = 2;
    public static final int GRID_SUMMARY = 3;
    public static final int HISTOGRAM = 4;
    
    public static final int NUM_HOSTS = 1;
    public static final int NUM_USERS = 2;
    public static final int NUM_TRANSFERS = 3;
    public static final int TRANSFER_VOLUME = 4;
    public static final int AVG_SPEED = 5;
    public static final int AVG_SIZE = 6;
    public static final int AVG_TIME = 7;

    private JLabel label;
    private GUSSIF theStub;
    private String serviceHostName;
    private int servicePort;
    private ChartMaker chartMaker;

    public SwingClient() {
	super("Grid Usage Sensor Services -- Client");
	try {
	    Properties p = new Properties();
	    InputStream fis=GUSSClient.class.getResourceAsStream("/guss.properties");
	    p.load(fis);
	    serviceHostName = p.getProperty("service-hostname");
	    servicePort = Integer.parseInt(p.getProperty("service-port"));
	    contactTheService(serviceHostName, servicePort);
	} catch (FileNotFoundException e) {
	    theStub = null;
	} catch (IOException e) {
	    theStub = null;
	}
    }
    
    public SwingClient(String hostname, int portNum) {
	super("Grid Usage Sensor Services -- Client");
	serviceHostName = hostname;
	servicePort = portNum;
	contactTheService(serviceHostName, servicePort);
    }

    private void contactTheService(String hostname, int portNum) {
	URL serviceLoc;
	GUSSIFServiceLocator gopher = null;

	try {
	    gopher = new GUSSIFServiceLocator();
	    if (gopher == null) {
		System.err.println("Couldn't create GUSSIFServiceLocator.");
		theStub = null;
		return;
	    }
	    serviceLoc = new URL("http://" + hostname + ":"+portNum
				 +"/axis/services/GUSS");
	    theStub= gopher.getGUSS(serviceLoc);
	    if (theStub == null) {
		System.err.println("Couldn't getGUSS.");
		theStub = null;
		return;
		}
	}
	catch (Exception e) {
	    System.err.println(e.toString());
	    theStub = null;
	    return;
	}
    }

    private void setupGUI() {
	getContentPane().setLayout(new FlowLayout());
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	if (theStub == null) {
	    label = new JLabel("Failed to contact GUSS service.  ("+
			       serviceHostName+", port "+servicePort);
	    System.out.println("Failed to contact GUSS service.  ("+
			       serviceHostName+", port "+servicePort);
	}
	else {
	    label = new JLabel("Contacted GUSS service on "+serviceHostName+
			       ", port "+servicePort);
	    System.out.println("Contacted GUSS service on "+serviceHostName+
			       ", port "+servicePort);
	
	    chartMaker = new ChartMaker(0, 0, "doesn't matter");
	    TimeSeriesCollection dataset = new TimeSeriesCollection();
	    dataset.addSeries(fetchTimeSeries(""));
	    System.out.println("Got first time series from service...");
	    dataset.addSeries(fetchTimeSeries("mcs.anl.gov"));
	    System.out.println("Got second time series from service...");
	    final JFreeChart chart = chartMaker.volumeVsTimeChart(dataset, "Foovax", "Barvax", "day", NUM_TRANSFERS);
	    System.out.println("Created chart...");
	    final ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new Dimension(500, 270));
	    chartPanel.setEnforceFileExtensions(false);
        
	    getContentPane().add(chartPanel);
	}

	getContentPane().add(label);
	pack();
	setVisible(true);
    }


    private TimeSeries fetchTimeSeries(String hostFilter) {
	long[] rawData;
	int i = 0;
	TimeSeries newSeries;
	RegularTimePeriod myPeriod;
	Calendar cal = Calendar.getInstance();
	Date start, end;
	int granularity = Calendar.HOUR;
	int graphType = TIMESERIES;
	int graphQuant = NUM_TRANSFERS;
	String options[] = new String[4];

	end = cal.getTime();
	cal.add(Calendar.DATE, -21);
	start = cal.getTime();

	options[0] = Long.toString(start.getTime());
	options[1] = Long.toString(end.getTime());
	options[2] = "hour";
	options[3] = hostFilter;
	
	if (theStub == null)
	    return null;

	try {
	    /*theStub.getNumbers just returns a bunch of numbers; have to recreate
	    the time series from that, put it in a graph*/
	    rawData = theStub.getNumbers(graphType, graphQuant, options);
	    
	    switch (granularity) {
		case Calendar.MINUTE:
		    newSeries = new TimeSeries("Minutely data", Minute.class);
		    break;
		case Calendar.HOUR:
		    newSeries = new TimeSeries("Hourly data", Hour.class);
		    break;
		case Calendar.DAY_OF_WEEK:
		    newSeries = new TimeSeries("Daily data", Day.class);
		    break;
		case Calendar.WEEK_OF_YEAR:
		    newSeries = new TimeSeries("Weekly data", Week.class);
		    break;
		default:
		    return null;
	    }

	    for (i=0; i<rawData.length; i+=2) {
		switch (granularity) {
		    case Calendar.MINUTE:
			myPeriod = new Minute(new Date(rawData[i]));
			break;
		    case Calendar.HOUR:
			myPeriod = new Hour(new Date(rawData[i]));
			break;
		    case Calendar.DAY_OF_WEEK:
			myPeriod = new Day(new Date(rawData[i]));
			break;
		    case Calendar.WEEK_OF_YEAR:
			myPeriod = new Week(new Date(rawData[i]));
			break;
		    default:
			return null;
		}

		newSeries.add(myPeriod, rawData[i+1]);
	    }
	    
	}
	catch (RemoteException re) {
	    re.printStackTrace();
	    return null;
	}
	return newSeries;
    }


    public static void main(String[] args) {
	
	SwingClient me;

	System.out.println("Starting up GUSS Swing client");
	if (args.length >=2) {
	    int port = Integer.parseInt(args[1]);
	    me = new SwingClient(args[0], port);
	}
	else
	    me = new SwingClient();

	System.out.println("Made connection to GUSS web service!");
	me.setupGUI();
	System.out.println("Setup complete!");
	while (true) {}
    }
	
}

/*What I'm going to need:
Web service needs a new method that just returns a bunchload of numbers;
generate graphs locally.
Then I need to make the web service actually running on wiggum so I can get at it from outside...
Once that works, start adding features...*/
