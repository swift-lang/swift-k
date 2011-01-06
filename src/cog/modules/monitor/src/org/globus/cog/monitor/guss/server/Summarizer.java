package org.globus.cog.monitor.guss;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Date;
import java.util.Calendar;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Day;
import org.jfree.data.time.Week;
import org.jfree.data.time.RegularTimePeriod;

/*The summarizer turns the individual packet entries in the database into
  records summarizing what happened for a certain category for a certain time
  unit.
	
  It needs to be told, when created, where to find the database and the names
  of the tables to work with.

  It can then be invoked with a range of time periods and a set of categories.
  It will produce one summary record for each category for each time period,
  and put these summaries into a new database table.  We can also ask for a
  TimeSeries (suitable for plotting) back from the Summarizer.

  If a summary record matching a given time period/category already exists,
  the summarizer will assume that the record is correct and skip that
  operation.  This is essential to avoid duplicating work in repeated calls.

  (However, an override option is also provided to force the summarizer to
  recalculate everything.  Using this should not be neccessary unless new
  records of old transfers have been manually added to the database.)

  Categories that should be supported by the summarizer:
  Components (GridFTP vs RFT vs etc)
  Hosts (seperate out the mcs.anl.gov hosts, for instance)
  File sizes (but how to specify the arbitrary distinction?)



*/

public class Summarizer {

    private Connection inputCon, outputCon;
    private String rawTable;
    private String summaryTable;
    private boolean unfinishedFlag;
    private Date lastSummaryDate;

    public Summarizer(String rawTable, String summaryTable, Connection inputCon, Connection outputCon) throws SQLException {
	this.rawTable = rawTable;
	this.summaryTable = summaryTable; 
	//this.con = OneConnectionPool.getConnection();
	this.unfinishedFlag = false;
	this.lastSummaryDate = null;
	this.inputCon = inputCon;
	this.outputCon = outputCon;
    }

    public void finalize() {
	//OneConnectionPool.closeConnection(this.con);
    }

    /*call summarizeOneTimeUnit repeatedly for each timestep from
    startDate to endDate.  If overwrite is true, it will recalculate
    summary records that already exist.  timestep is one of the
    constants in Calendar: Calendar.HOUR, Calendar.DAY, etc*/
    public void summarize(Date startDate, Date endDate, int timestep, boolean overwrite) 
	throws SQLException  {

     	Calendar cal;
	Date startStep;
	Date endStep;

	//clip endDate to last even timestep before now -- i.e. do not summarize
	//time periods that haven't ended yet, or are still in the future!
	cal = Calendar.getInstance(); //now
	cal = getLastEvenTimestep(timestep, cal.getTime()); 
	if (endDate.after(cal.getTime())) {
	    endDate = cal.getTime();
	}

	//clip startDate to the beginning of monitoring (March 1, 2005)
	cal = Calendar.getInstance();
	cal.set(2005, Calendar.MARCH, 1, 0, 0, 0);
	if (startDate.before(cal.getTime())) {
	    startDate = cal.getTime();
	}

	//last even timestep before startdate
	cal = getLastEvenTimestep(timestep, startDate);

	do {
	    startStep = cal.getTime();
	    cal.add(timestep, 1);
	    endStep = cal.getTime();
	    
	    if (endStep.getTime() <= endDate.getTime()) {
		/*		summarizeOneTimeUnit(startStep, endStep, timestep, "", overwrite);
		summarizeOneTimeUnit(startStep, endStep, timestep, "mcs.anl.gov", overwrite);
		summarizeOneTimeUnit(startStep, endStep, timestep, "isi.edu", overwrite);*/

		testSummary(startDate, endDate);
	    }
	}
	while (endStep.getTime() <= endDate.getTime());
    }

    /*If inputCon and outputCon are different, we read from one db and write to another.*/
    private void testSummary(Date startDate, Date endDate) 
	throws SQLException {

	ResultSet rs;
	HourSummary summary = new HourSummary(startDate);
	PreparedStatement bigQuery;
	long maxRecords = 10000;
	int recordsRetrievedTotal;
	int recordsRetrievedThisTime;

	bigQuery = inputCon.prepareStatement("SELECT * FROM gftp_packets WHERE end_time < ? AND end_time > ? LIMIT ? OFFSET ?");
	//TODO does this mean a packet with end_time right on the border is
	//missed completely?
	bigQuery.setLong(1, endDate.getTime());
	bigQuery.setLong(2, startDate.getTime());
	bigQuery.setLong(3, maxRecords);

	recordsRetrievedTotal = recordsRetrievedThisTime = 0;
	/*If we put a limit of x records on the query, and we get exactly
	  x records back, then it's likely there are still more records;
	  this do loop will go back and retrieve more until rows matching
	  the time limits are truly exhausted.*/
	do {
	    recordsRetrievedTotal += recordsRetrievedThisTime;
	    recordsRetrievedThisTime = 0;
	    bigQuery.setLong(4, recordsRetrievedTotal);
	    rs = bigQuery.executeQuery();
	    while (rs.next()) {
		GFTPRecord tempRecord = new GFTPRecord(rs);
		recordsRetrievedThisTime ++;
		summary.addRecord(tempRecord);
	    }
	} while (recordsRetrievedThisTime == maxRecords);

	rs.close();
	bigQuery.close();
	summary.display();

	summary.storeToDB(outputCon);
    }



    /*REWRITE ME*/
    public boolean summaryExists(String summaryTable, Date startTime, Date endTime, String hostFilter) throws SQLException {
	ResultSet rs;
	PreparedStatement existsQuery;
	boolean exists;


	existsQuery = outputCon.prepareStatement("SELECT id FROM "+summaryTable+" WHERE start_time =  ? AND end_time = ? AND src_host LIKE '"+hostFilter+"';");
	existsQuery.setLong(1, startTime.getTime());
	existsQuery.setLong(2, endTime.getTime());
//	existsQuery.setString(3, hostFilter);

	rs = existsQuery.executeQuery();
	exists = rs.next();
	try {
	    rs.close();
	    existsQuery.close();
	} catch (Exception e) {}
	return exists;
    }


    Calendar getLastEvenTimestep(int granularity, Date endDate) {
	/*returns a calendar set to the date which is the last even
	  multiple before or at endDate of the timestep expressed by granularity.
	  Always start from one of these and go backwards when summarizing.*/

	Calendar cal = Calendar.getInstance();
	cal.setTime(endDate);

	switch (granularity) {
	    case Calendar.WEEK_OF_YEAR:
		//set calendar days to first of week
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); 
		//fall through
	    case Calendar.DAY_OF_WEEK:
		//set calendar hour to first of day
		cal.set(Calendar.HOUR, 0);
		//fall through
	    case Calendar.HOUR:
		//set calendar minutes to zero
		cal.set(Calendar.MINUTE, 0);
		//fall through
	    case Calendar.MINUTE:
		//set calendar seconds and milliseconds to zero
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//fall through
	    default:
	}
	
	return cal;
    }


    private Class getTimePeriodClassFromConstant(int granularity) {
	switch (granularity) {
	    case Calendar.MINUTE:
		return Minute.class;
	    case Calendar.HOUR:
		return Hour.class;
	    case Calendar.DAY_OF_WEEK:
		return Day.class;
	    case Calendar.WEEK_OF_YEAR:
		return Week.class;
	    default:
		System.err.println("Can't happen!");
	}
	return null;
    }

/*This is the function that GUSSIF will be calling, so this function should call the
  others as neccessary...*/
    public TimeSeries timeSeriesFromSummaryTable(Date startDate, Date endDate,
						 int graphQuant, String hostFilter,
						 int granularity, String seriesName)
	throws SQLException {

	ResultSet rs;
	PreparedStatement querySt;
	TimeSeries newSeries = null;
	RegularTimePeriod myPeriod = null;
	Class timePeriodClass = null;


	//Don't summarize in response to requests: it takes too long.  Leave that for the
	//cronjob.
	//summarize(startDate, endDate, granularity, false);

	timePeriodClass = getTimePeriodClassFromConstant(granularity);

	switch (graphQuant) {
	case GUSSConstants.NUM_TRANSFERS: case GUSSConstants.NUM_HOSTS: case GUSSConstants.TRANSFER_VOLUME: case GUSSConstants.NUM_NEW_HOSTS:
	    newSeries = new TimeSeries(seriesName, timePeriodClass);
	    break;
	case GUSSConstants.AVG_SIZE: case GUSSConstants.AVG_TIME: case GUSSConstants.AVG_SPEED: case GUSSConstants.AVG_STREAMS:
	    newSeries = new StdDevSeries(seriesName, timePeriodClass);
	    break;
	default:
	    System.err.println("Can't happen!");
	    return null;
	}

	//	querySt = con.prepareStatement("SELECT num_transfers, num_hosts, total_bytes, avg_size, avg_time, avg_speed, end_time, size_stddev, time_stddev, speed_stddev FROM gftp_summaries WHERE granularity = ? AND src_host LIKE '"+hostFilter+"' AND end_time >= ? AND end_time <= ?;");
	querySt = outputCon.prepareStatement("SELECT num_records, num_hosts, total_bytes, mean_size, mean_time, mean_speed, start_time, size_variance, time_variance, speed_variance, mean_streams, stream_variance, num_new_hosts FROM gftp_summaries WHERE start_time >= ? AND start_time <= ?;");
	//	querySt.setInt(1, granularity);
	querySt.setTimestamp(1, new Timestamp(startDate.getTime()));
	querySt.setTimestamp(2, new Timestamp(endDate.getTime()));
//	querySt.setString(2, hostFilter);

	rs = querySt.executeQuery();

	while (rs.next()) {

	    TimeSeriesDataItem theItem = null;
	    double variance;
	    double stdDev;
	    long numTransfers = rs.getLong(1);
	    this.lastSummaryDate = new Date(rs.getTimestamp(7).getTime());

	    switch (granularity) {
		case Calendar.MINUTE:
		    myPeriod = new Minute(this.lastSummaryDate);
		    break;
		case Calendar.HOUR:
		    myPeriod = new Hour(this.lastSummaryDate);
		    break;
		case Calendar.DAY_OF_WEEK:
		    myPeriod = new Day(this.lastSummaryDate);
		    break;
		case Calendar.WEEK_OF_YEAR:
		    myPeriod = new Week(this.lastSummaryDate);
		    break;
		default:
		    System.err.println("Can't happen!");
		    newSeries = null;
	    }
	    switch (graphQuant) {
	    case GUSSConstants.NUM_TRANSFERS:
		theItem = new TimeSeriesDataItem(myPeriod, numTransfers);
		break;
	    case GUSSConstants.NUM_HOSTS:
		theItem = new TimeSeriesDataItem(myPeriod, rs.getInt(2));
		break;
	    case GUSSConstants.NUM_NEW_HOSTS:
		theItem = new TimeSeriesDataItem(myPeriod, rs.getInt(13));
		break;
		case GUSSConstants.TRANSFER_VOLUME:
		    long value = rs.getLong(3)/1024; //convert bytes to kb
		    if (value == 0)
			value = 1; //this is logarithmic scale -- fudge to avoid zeroes.
		    theItem = new TimeSeriesDataItem(myPeriod,value);
		    break;
		case GUSSConstants.AVG_SIZE:
		    variance = rs.getDouble(8);
		    stdDev = Math.sqrt(variance/(numTransfers-1));
		    theItem = new StdDevDataItem(myPeriod, rs.getDouble(4), stdDev);
		    break;
		case GUSSConstants.AVG_TIME:
		    variance = rs.getDouble(9);
		    stdDev = Math.sqrt(variance/(numTransfers-1));
		    theItem = new StdDevDataItem(myPeriod, rs.getDouble(5), stdDev);
		    break;
		case GUSSConstants.AVG_SPEED:
		    variance = rs.getDouble(10);
		    stdDev = Math.sqrt(variance/(numTransfers-1));
		    theItem = new StdDevDataItem(myPeriod, rs.getDouble(6), stdDev);
		    break;
	    case GUSSConstants.AVG_STREAMS:
		variance = rs.getDouble(12);
		    stdDev = Math.sqrt(variance/(numTransfers-1));
		    theItem = new StdDevDataItem(myPeriod, rs.getDouble(11), stdDev);
		    break;
		default:
		    System.err.println("Can't happen!");
		    continue;
	    }

	    try {
		newSeries.add(theItem);
	    } catch (Exception e) {
		/*An exception can happen if we try to add an item
		  that duplicates one already in the series.  Continue
		  making the rest of the series anyway.*/
	        System.err.println(e.getMessage());
	    }
	}

	/*See whether the last summary returned by the query matches
	  the end of the time we are seeking... if not, there must be
	  ongoing summary processes that have not caught up with the
	  present.  Set a flag in this case to mark the resulting
	  image as somewhat bogus -- it should not be cached, and the
	  user should get a disclaimer message.*/
	this.unfinishedFlag = (!endDate.equals(lastSummaryDate));
	
	return newSeries;
    }

    public boolean isUnfinished() {
	return this.unfinishedFlag;
    }

    public Date getLastSummaryDate() {
	return this.lastSummaryDate;
    }

}
