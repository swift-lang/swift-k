package org.globus.cog.monitor.guss;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Date;
import java.util.Properties;
import java.util.Calendar;
import java.util.LinkedList;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.ParseException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;

import java.rmi.RemoteException;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Main class implementing the GUSS web service.
 * @version 1.13
 * @see GUSSIF
 * @see GUSSClient
 */
public class GUSSImpl implements GUSSIF, GUSSConstants{
    
    /*Todo: Will probably want to read width and height from a config file, or even in the arguments.*/
    static final int WIDTH = 800;
    static final int HEIGHT = 500;
    static final String propertyFileName = "/guss.properties";

    /*Note that the following non-static fields last only as long as the instance of
      GUSSImpl -- which is the request lifetime.  Consider moving to GT4's WSRF to
      remember user settings over multiple invocations.*/
    String databaseDriverClass;
    String databaseUserName;
    String databasePassword;
    String databaseTablename;
    String databaseSummaryTable;
    String serviceURL;
    String tempFileDir;
    String debugOutFilePath;
    String rawDBURL;
    String summaryDBURL;

    File debugOutputFile;
    BufferedWriter debugOutput;

    Summarizer summarizer;

    Connection rawDBCon, summaryDBCon;

    public GUSSImpl() throws Exception {
	System.setProperty("java.awt.headless", "true");
	/*This is to prevent JFreeChart from trying to contact the 
	  X-server, which it can't do when running as a daemon.*/

	try {
	    readAllProperties();
	}
	catch (Exception e) {
	    throw new Exception("Can't read properties file: "+e.getMessage());
	}
	try {
	    Class.forName(databaseDriverClass);
	    rawDBCon = DriverManager.getConnection(rawDBURL, databaseUserName, databasePassword);
	    summaryDBCon = DriverManager.getConnection(summaryDBURL, databaseUserName, databasePassword);

	    //OneConnectionPool.create(databaseDriverClass, databaseUrl, databaseUserName, databasePassword);
	}
	catch (Exception e) {
	    throw new Exception("Can't start database connection pool: "+e.getMessage());
	}
	try {
	    summarizer = new Summarizer(databaseTablename, databaseSummaryTable, rawDBCon, summaryDBCon);
	} catch (Exception e) {
	    throw new Exception("Can't create summarizer: "+e.getMessage());
	}
	try {
	    debugOutputFile = new File(debugOutFilePath);
	    if (debugOutputFile.exists()) {
		debugOutputFile.delete();
	    }
	    debugOutputFile.createNewFile();
	    if (!debugOutputFile.canWrite()) {
		System.out.println("File cannot be written.");
	    }
	    debugOutput = new BufferedWriter( new FileWriter(debugOutputFile) );
	    debugOutput.flush();
	} catch (IOException e) {
	    throw new Exception("Can't open debug-output file: " + e.getMessage());
	}
    }

    public void finalize() {
	summarizer = null;
	//OneConnectionPool.close();
	try {
	    rawDBCon.close();
	    summaryDBCon.close();
	    debugOutput.close();
	} catch (SQLException e) {
	} catch (IOException e) {
	}
    }

    /**
     *@param args an array containing extra optional arguments, currently unused.
     *@return space-separated list of names of all hosts known to be active.
     *Callable through web-service interface.*/

    /*TODO redo this to just read from the KnownHosts table in the db.*/
    public String getAllHostNames(String[] args) { 
	PreparedStatement ps;
	ResultSet rs;
	StringBuffer hostList = new StringBuffer();
	String[] moreTableNames =  {"rft_packets", "java_ws_core_packets"};
	int i;

	try {
	    //Connection con = OneConnectionPool.getConnection();
	    ps = summaryDBCon.prepareStatement("SELECT DISTINCT ip_address FROM gftp_packets;");
	    rs = ps.executeQuery();
	    while (rs.next()) {
		hostList.append(rs.getString(1));
		hostList.append(", ");
	    }
	    
	    rs.close();
	    ps.close();


	    /*That's got us the hostnames from gftp_packets, but there
	    may be hosts in rft_packets, java_ws_core_packets etc
	    which are not in gftp_packets.  Find non-duplicates,
	    resolve their hostnames, add them to list:*/
	    for (i = 0; i<moreTableNames.length; i++) {
		ps = rawDBCon.prepareStatement("SELECT DISTINCT ip_address FROM " 
					  + moreTableNames[i] + ";");
		rs = ps.executeQuery();
		while (rs.next()) {
		    String newIP = rs.getString(1);
		    InetAddress inetAddress;
		    if (hostList.indexOf(newIP) == -1) {
			//add only if it's not already in the list:
			try {
			    inetAddress = InetAddress.getByName(newIP);
			    hostList.append(inetAddress.toString());
			} catch (UnknownHostException e) {
			    hostList.append(newIP);
			}
		    hostList.append(", ");
		    }
		}
		rs.close();
		ps.close();
	    }
	    //OneConnectionPool.closeConnection(con);
	    return hostList.toString();
	}
	catch (SQLException e) {
	    return "An SQL exception in GUSSImpl.getAllHostNames: "+e.getMessage();
	}
    }


    public long[] getNumbers(int graphType, int graphQuant, String[] options) 
	throws RemoteException {
	/*Decompose dataset into a flat array of numbers:
	  x1, y1, x2, y2, x3, y3....*/
	try {
	    /*Parse arguments: 
	      0 = startDate
	      1 = endDate
	      2 = granularity (string)
	    */
	    Date startDate = new Date(Long.parseLong(options[0]));
	    Date endDate = new Date(Long.parseLong(options[1]));
	    int granularity = parseGranularityArgument(options[2]);
	    String hostFilter = options[3];

	    TimeSeries timeData = getTimeSeries(startDate, endDate, granularity,
						graphQuant, hostFilter);
	    int numItems = timeData.getItemCount();
	    long[] numbers = new long[numItems * 2];
	    int i, j = 0;
	    
	    for (i=0; i<numItems; i++) {
		TimeSeriesDataItem temp = timeData.getDataItem(i);
		
		numbers[j] = temp.getPeriod().getEnd().getTime();
		j++;
		numbers[j] = temp.getValue().longValue();
		j++;
	    }
	    return numbers;

	} catch (ParseException pe) {
	    throw new RemoteException("GUSSImpl can't parse argument: ", pe);
	} catch (SQLException se) {
	    throw new RemoteException("GUSSImpl SQLexception: ", se);
	}
    }

    private TimeSeries getTimeSeries(Date startDate, Date endDate, int granularity, int graphQuant, String hostFilter) 
	throws ParseException, SQLException {


	String seriesName;
	
	if (hostFilter.equals("")) {
	    seriesName = "Total";
	} else {
	    seriesName = hostFilter;
	}
	return summarizer.timeSeriesFromSummaryTable(startDate, 
						     endDate,
						     graphQuant,
						     hostFilter,
						     granularity,
						     seriesName);
    }

    private void debugOut(String contents) {
	try {
	    debugOutput.write(contents);
	    debugOutput.newLine();
	    debugOutput.flush();
	} catch( IOException e) {
	}
    }

    /**
     *Creates a chart (.png file) of usage data.  If TIME_SERIES,
     *expects at least three more arguments in
     *<code>options</code>:<OL><LI>Space-separated list of source
     *hosts</LI><LI>Space-separated list of destination
     *hosts</LI><LI>Granularity (either "minute", "hour", "day", or
     *"week").  Any further strings in <code>options</code> will be
     *interpreted as {@link Filter} descriptions.
     *@param graphType one of the constants in {@link GUSSIF#TIMESERIES GUSSIF} telling which type of chart (time series, histogram, etc.) to make.
     *@param graphQuant one of the constants in {@link GUSSIF#NUM_HOSTS GUSSIF} telling which quantity to plot on the y-axis.
     *@param options extra arguments, passed to us through SOAP as an array of strings; interpretation depends on graphType.
     *@return HTML fragment containing link to URL of the saved image file.
     *Callable through web-service interface.*/
    public String makeGraphImage(int graphType, int graphQuant, String[] options) {

	ChartMaker chartMaker;
	String chartFileName;
	TimeSeriesCollection dataset = null;
	int granularity;
	Date startDate;
	Date endDate;
	StringBuffer outputBuf = new StringBuffer();

	/*Parse arguments: 
	 0 = startDate
	 1 = endDate
	 2 = granularity (string)
	 */
	startDate = new Date(Long.parseLong(options[0]));
	endDate = new Date(Long.parseLong(options[1]));
	granularity = parseGranularityArgument(options[2]);

	debugOut("makeGraphImage called.with startDate "+startDate+" and endDate " + endDate + " and granularity " + granularity + " and graphType " + graphType + " and graphQuant " + graphQuant);

	try {

	    chartFileName = chartAlreadyExists(startDate, endDate, granularity,
					       graphType, graphQuant, "no filter");
		
	    if (chartFileName == null) {

		chartMaker = new ChartMaker(WIDTH, HEIGHT, tempFileDir);		

    /* TimeSeries isiSeries =getTimeSeries(startDate, endDate,
			      granularity,graphQuant, "isi.edu");
       TimeSeries mcsSeries = getTimeSeries(startDate, endDate,
			      granularity, graphQuant, "mcs.anl.gov");
       TimeSeries globusSeries = chartMaker.sumSeries(isiSeries,
			      mcsSeries);*/
		switch (graphType) {
		case TIMESERIES:
		    switch (graphQuant) {
		    case AVG_TIME: case AVG_SIZE: case AVG_SPEED: case AVG_STREAMS:
			dataset = new StdDevDataset();
			break;
		    case NUM_HOSTS: case NUM_NEW_HOSTS: case NUM_TRANSFERS: case TRANSFER_VOLUME:
			dataset = new TimeSeriesCollection();
			break;
		    }	
		    dataset.addSeries(getTimeSeries(startDate, endDate, granularity,
						    graphQuant,  ""));
		    chartFileName = chartMaker.saveTimeSeriesChart(dataset, graphQuant, granularity);
		    break;

		case HISTOGRAM:
		    chartFileName = chartMaker.saveHistogram(summaryDBCon, startDate, endDate, graphQuant);
		    break;
		}		
		if (!summarizer.isUnfinished()) {
		    cacheChartFileName(startDate, endDate, granularity, graphType,
				       graphQuant, "no filter", chartFileName);
		}
	    }

	}
	catch (IOException ioe) {
	    return "Can't create image file " + ioe.getMessage();
	}
	catch (ParseException pe) {
	    return "Can't parse arguments. " + pe.getMessage();
	}
	catch (SQLException se) {
	    return "SQLException in GUSSImpl: "+se.getMessage();
	}
	catch (Exception e) {
	    return "Exception in GUSSImpl: " + e.getMessage();
	}

	outputBuf.append("<IMG SRC=\"");
	outputBuf.append(serviceURL);
	outputBuf.append(chartFileName);
	outputBuf.append("\">");

	outputBuf.append("<p>(The above is current as of ");
	outputBuf.append(summarizer.getLastSummaryDate());
	outputBuf.append(".)</p>");
	return outputBuf.toString();
    }


    private String hostByHostTable(int graphType, int graphQuant, String[] options)
	throws SQLException {

	/*Output a table -- html formatted or otherwise -- containing:
	hostname -- total number of packets for each protocol*/

	String[] hostNameList;
	String[] tableNameList = {"gftp_packets", "rft_packets",
				  "java_ws_core_packets"};
	//, "c_ws_core_packets", "gram_packets"};
	long[] totalPacketNumbers = new long[5];
	int numHosts;
	int i, j;
	StringBuffer outputBuf = new StringBuffer();
	PreparedStatement ps;
	ResultSet rs;
	long numPackets;
	Connection con;
	String temp;
	String newline = System.getProperty("line.separator");
	String horizontalSpacer = "     ";
	String numericalPart;

	temp = getAllHostNames(options);
	    
	hostNameList = temp.split(", ");
	numHosts = hostNameList.length;
	    
	outputBuf.append("FOR MCS INTERNAL USE ONLY. DO NOT PUBLISH.");
	outputBuf.append(newline);
	outputBuf.append("Host:");
	outputBuf.append(horizontalSpacer);
	for (j=0; j < tableNameList.length; j++) {
	    outputBuf.append(tableNameList[j]);
	    outputBuf.append(horizontalSpacer);
	    totalPacketNumbers[j] = 0;
	}
	outputBuf.append(newline);
	    
	//con = OneConnectionPool.getConnection();
	for (i=0; i < numHosts; i++) {
	    outputBuf.append(hostNameList[i]);
	    outputBuf.append(": ");
		
	    numericalPart = hostNameList[i].substring(hostNameList[i].indexOf('/')+1);
	    for (j=0; j < tableNameList.length; j++) {

		numPackets = getTotalNumEver(rawDBCon, tableNameList[j], numericalPart, NUM_TRANSFERS);
		outputBuf.append(Long.toString(numPackets));
		outputBuf.append(horizontalSpacer);
		totalPacketNumbers[j] += numPackets;
	    }
	    outputBuf.append(newline);
	}
	//OneConnectionPool.closeConnection(con);
	outputBuf.append("Total: ");
	    
	for (j=0; j < tableNameList.length; j++) {
	    outputBuf.append(Long.toString(totalPacketNumbers[j]));
	    outputBuf.append(horizontalSpacer);
	}
	
	return outputBuf.toString();
    }

    private long getTotalNumEver(Connection con, String tablename, String filter, int graphQuant) throws SQLException {
	PreparedStatement ps;
	ResultSet rs;
	long result;
	String quantity = "";
	StringBuffer sqlBuf = new StringBuffer();

	if (graphQuant == NUM_HOSTS)
	    quantity = "COUNT(DISTINCT ip_address)";
	else if (graphQuant == NUM_TRANSFERS)
	    quantity = "COUNT(*)";
	else if (graphQuant == TRANSFER_VOLUME)
	    quantity = "SUM(num_bytes)";
	else
	    return 0;
	
	sqlBuf.append("SELECT ").append(quantity).append(" FROM ");
	sqlBuf.append(tablename).append(" WHERE ip_address LIKE '%");
	sqlBuf.append(filter).append("%';");
	ps = con.prepareStatement(sqlBuf.toString());
	rs = ps.executeQuery();
	if (!rs.next()) {
	    result = 0;
	}
	else {
	    result = rs.getLong(1);
	}
	rs.close();
	ps.close();
	return result;
    }

   /**Creates an HTML table of numerical data based on user request.
     *@param graphType one of the constants in {@link GUSSIF#TIMESERIES GUSSIF}
     *@param graphQuant one of the constants in {@link GUSSIF#NUM_HOSTS GUSSIF} telling which quantity to display
     *@param options extra arguments, passed to us through SOAP as an array of strings.  The first optional argument is a string of one or more filter descriptions.
     *@return HTML fragment containing the table*/
    public String makeTable(int graphType, int graphQuant, String[] options) {

	final String spacer = "</td><td>";
	final String startLine = "<tr><td>";
	final String endLine = "</td></tr>";
	final String header = "<table border><tr><td>";
	final String footer = "</table>";

	final String[] filters = {"140.221", "128.9", ""};
	final String[] domains = {"mcs.anl.gov", "isi.edu", "All domains"};
	final String[] tables = {"gftp_packets", "rft_packets", "java_ws_core_packets"};

	//Connection con;
	int category, protocol;
	StringBuffer outputBuf = new StringBuffer();
	long quantity;

	try {
	    //con = OneConnectionPool.getConnection();

	    outputBuf.append(header);
	    outputBuf.append(startLine).append("Domain").append(spacer);
	    outputBuf.append("GFTP transfers").append(spacer);
	    //	    outputBuf.append("GFTP hosts").append(spacer);
	    outputBuf.append("GFTP bytes").append(spacer);
	    outputBuf.append("RFT packets").append(spacer);
	    outputBuf.append("Java Core packets").append(endLine);
	    
	    for (category = 0; category < filters.length; category ++) {
		outputBuf.append(startLine);
		outputBuf.append(domains[category]).append(spacer);

		quantity = getTotalNumEver(rawDBCon, this.databaseTablename, filters[category],
					   NUM_TRANSFERS);
		outputBuf.append(Long.toString(quantity)).append(spacer);
		/*		quantity = getTotalNumEver(con, this.databaseTablename,
					   filters[category], NUM_HOSTS);
					   outputBuf.append(Long.toString(quantity)).append(spacer);*/
		quantity = getTotalNumEver(rawDBCon, this.databaseTablename,
					   filters[category], TRANSFER_VOLUME);
		outputBuf.append(Long.toString(quantity)).append(spacer);

		quantity = getTotalNumEver(rawDBCon, tables[1], filters[category],
					   NUM_TRANSFERS);
		outputBuf.append(Long.toString(quantity)).append(spacer);
		quantity = getTotalNumEver(rawDBCon, tables[2], filters[category],
					   NUM_TRANSFERS);
		outputBuf.append(Long.toString(quantity)).append(endLine);
	    }
	    outputBuf.append(footer);
	    //	    OneConnectionPool.closeConnection(con);
	}
	catch (SQLException e) {
	    return "An SQLException in GUSSImpl.makeTable: "+e.getMessage();
	}

	return outputBuf.toString();
    }
 

    /**
     *Looks in the GUSSServer.jar for a file called guss.properties, reads the
     *properties from it, and stores them in the GUSSImpl object (which has the
     *lifetime of the request only).*/
    private void readAllProperties() throws FileNotFoundException, IOException {

	Properties p = new Properties();
	InputStream fis=GUSSImpl.class.getResourceAsStream(propertyFileName);
	p.load(fis);

	serviceURL = p.getProperty("service-url");
	tempFileDir = p.getProperty("temp-file-dir");
	debugOutFilePath = p.getProperty("debug-out-file");

	databaseDriverClass = p.getProperty("db-driver-class");
	databaseTablename = p.getProperty("db-gftptable");
	databaseSummaryTable = p.getProperty("db-summarytable");

	rawDBURL = p.getProperty("raw-db-url");
	summaryDBURL = p.getProperty("summary-db-url");
	databaseUserName = p.getProperty("db-username");
	databasePassword = p.getProperty("db-password");
    }

    /**
     *Sets all the properties to given values instead of reading them from the
     *properties file.  Useful for testing.*/
    void setAllProperties(String dbdc, String rdburl, String sdburl,
			  String user, String pswd,
			  String dbtn, String myurl, String tfd, String dof) {
	this.databaseDriverClass = dbdc;
	this.rawDBURL = rdburl;
	this.summaryDBURL = sdburl;
	this.databaseUserName = user;
	this.databasePassword = pswd;
	this.databaseTablename = dbtn;
	this.serviceURL = myurl;
	this.tempFileDir = tfd;
	this.debugOutFilePath = dof;
    }

    private int parseGranularityArgument(String arg) {
	if (arg.equals("minute")) {
	    return Calendar.MINUTE;
	} else if (arg.equals("hour")) {
	    return Calendar.HOUR;
	} else if (arg.equals("day")) {
	    return Calendar.DAY_OF_WEEK;
	} else if (arg.equals("week")) {
	    return Calendar.WEEK_OF_YEAR;
	} else {
	    return Calendar.DAY_OF_WEEK; //default
	}
    }

    private Date roundOffDate(int granularity, Date input) {
	Calendar temp;
	temp = this.summarizer.getLastEvenTimestep(granularity, input);
	return temp.getTime();
    }
    
    /*If an image file has already been produced to these specifications,
      there will be an entry for it in the graph_image_files table; return
      its url.  If it doesn't exist, return null.*/
    private String chartAlreadyExists(Date startTime, Date endTime, int granularity, int graphType, int graphQuant, String filter) throws SQLException {

	//Connection con = OneConnectionPool.getConnection();
	PreparedStatement ps;
	ResultSet rs;
	String fileURL;

	startTime = roundOffDate(granularity, startTime);
	endTime = roundOffDate(granularity, endTime);

	ps = summaryDBCon.prepareStatement("SELECT file_url FROM graph_image_files WHERE start_time = ? AND end_time = ? AND granularity = ? AND graph_type = ? AND graph_quant = ? AND filter LIKE '" + filter + "';");
	
	ps.setTimestamp(1, new Timestamp(startTime.getTime()));
	ps.setTimestamp(2, new Timestamp(endTime.getTime()));
	ps.setInt(3, granularity);
	ps.setInt(4, graphType);
	ps.setInt(5, graphQuant);

	rs = ps.executeQuery();

	if (rs.next()) {
	    fileURL = rs.getString(1);
	    System.out.println("Found existing image file matching this request: " + fileURL);
	} else {
	    fileURL = null;
	}

	rs.close();
	ps.close();
	//OneConnectionPool.closeConnection(con);
	return fileURL;
    }

    private void cacheChartFileName(Date startTime, Date endTime, int granularity, int graphType, int graphQuant, String filter, String fileURL) throws SQLException {

	//Connection con = OneConnectionPool.getConnection();
	PreparedStatement ps;

	startTime = roundOffDate(granularity, startTime);
	endTime = roundOffDate(granularity, endTime);

	ps = summaryDBCon.prepareStatement("INSERT INTO graph_image_files (start_time, end_time, granularity, graph_type, graph_quant, filter, file_url) VALUES (?, ?, ?, ?, ?, ?, ?);");
	
	ps.setTimestamp(1, new Timestamp(startTime.getTime()));
	ps.setTimestamp(2, new Timestamp(endTime.getTime()));
	ps.setInt(3, granularity);
	ps.setInt(4, graphType);
	ps.setInt(5, graphQuant);
	ps.setString(6, filter);
	ps.setString(7, fileURL);

	ps.executeUpdate();

	System.out.println("Stored " + fileURL + " into database.");

	ps.close();
	//OneConnectionPool.closeConnection(con);
    }

    /*Go through the entire database making summaries of the given category.
      This function is not to be called through the web service, but rather
      as a batch-mode command from the server side.  It takes a long long
      time to crunch the whole database.  But if we run this at midnight to
      crunch and pre-summarize the new stuff, then we avoid a long wait for
      users.*/
    void preSummarizeDatabase(String granularity) throws SQLException {
	Calendar now = Calendar.getInstance();
	Calendar beginningOfTime = Calendar.getInstance();
	beginningOfTime.set(2005, Calendar.MARCH, 1, 0, 0, 0);
	beginningOfTime.set(Calendar.MILLISECOND, 0);
	//Midnight on march 1, 2005: Earliest date of usage packets.
	
	/*main in DatabaseTool calls this; granularity is command-line arg
	"hour", "day", or "week".  Call ant with -DsummaryTime=hour,
	-DsummaryTime=day, etc. from cronjob to do regular summaries.*/
	summarizer.summarize(beginningOfTime.getTime(), now.getTime(),
			     parseGranularityArgument(granularity), false);
    }

    public static void main(String args[]) {
	String[] arguments = new String[4];
	Calendar cal = Calendar.getInstance();
	Date startDate, endDate;
	
	GUSSImpl me;

	try {    

	    me= new GUSSImpl();

	    /*	    System.out.println("Here is histogram of transfers per hour in the last month:");
	    endDate = cal.getTime();
	    cal.set(2005, 5, 18, 0, 0, 0); //june 18 2005
	    startDate = cal.getTime();*/

	    //me.summarizer.summarize(startDate, endDate, Calendar.HOUR, false);
	    //System.out.println(me.makePPHHistogram(startDate, endDate));

	
	    endDate = cal.getTime();
	    cal.add(Calendar.DATE, -30);
	    startDate = cal.getTime();
	    
	    arguments[0] = Long.toString(startDate.getTime());
	    arguments[1] = Long.toString(endDate.getTime());
	    arguments[2] = "hour";
	    
	    System.out.println("Here is graph of number of transfers over the past three weeks:");
	    System.out.println(me.makeGraphImage(HISTOGRAM, AVG_SIZE, arguments));
	    
	    //	    System.out.println("Here is whole grid summary table:");
	    //	    System.out.println(me.makeTable(1, 1, arguments));
	} catch (Exception e) {
	    e.printStackTrace();
	}


    }
}
