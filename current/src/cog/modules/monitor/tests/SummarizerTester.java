package org.globus.cog.monitor.guss;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestSuite;


public class SummarizerTester extends TestCase {

    /*AllTester will initialize the database connection pool to point at test
      database before this gets called.*/
 
    private Summarizer theSummarizer;
    private Connection con;
    private Calendar cal;

    public SummarizerTester(String name) {
	super(name);

	try {
	    theSummarizer = new Summarizer(AllTester.recordsTableName,
				       AllTester.summaryTableName);

	    con = AllTester.con; //it's already open, just use this one...
	}
	catch (SQLException e) {
	    Assert.fail("Can't startup summarizer!");
	}

    }
    
    public void finalize() {
    }

    private void writeOnePacket(int waitSeconds, int sendSeconds, String loggingHost, boolean isStore, long bytes) {
	GFTPRecord oneRecord;
	Date startDate, endDate;
	cal.add(Calendar.SECOND, waitSeconds);
	startDate = cal.getTime();
	cal.add(Calendar.SECOND, sendSeconds);
	endDate = cal.getTime();
	oneRecord = new GFTPRecord(startDate, endDate, loggingHost, isStore?GFTPRecord.STOR:GFTPRecord.RETR, bytes);
	oneRecord.insertIntoDatabase(con, AllTester.recordsTableName);
    }

    protected void setUp() {

	AllTester.prepareTestDatabase();
	
	//clear the summary table and put some stuff in the packets table to summarize

	//start an hour before the last even hour
	cal = theSummarizer.getLastEvenTimestep(Calendar.HOUR,
						Calendar.getInstance().getTime());
	cal.add(Calendar.HOUR, -1);


	//these ought to get matched up:
	writeOnePacket(10, 2, "foovax@miskatonic.edu", true, 4200);
	writeOnePacket(1, 2, "barvax@losemoney.com", false, 4200);

	//these ought to get matched up:
	writeOnePacket(60, 8, "nonexist@mcs.anl.gov", false, 58099);
	writeOnePacket(-4, 5, "nowhere@mcs.anl.gov", true, 58099);

	writeOnePacket(10, 5, "foovax@miskatonic.edu", true, 7777);
	writeOnePacket(8, 3, "foovax@miskatonic.edu", true, 6416);
	writeOnePacket(12, 1, "foovax@miskatonic.edu", false, 162);

	writeOnePacket(50, 1, "barvax@losemoney.com", false, 29);
	writeOnePacket(20, 2, "nowhere@mcs.anl.gov", false, 10000);
	writeOnePacket(-23, 1, "nowhere@mcs.anl.gov", true, 29);

    }

    protected void tearDown() {
    }

    public void testSummaryExists() {
	PreparedStatement storeSt;
	Date startDate, endDate;

	cal = theSummarizer.getLastEvenTimestep(Calendar.HOUR,
						Calendar.getInstance().getTime());
	endDate = cal.getTime();
	cal.add(Calendar.HOUR, -1);
	startDate = cal.getTime();
	
	try {
	    Assert.assertFalse("Should not be a summary here",
			       theSummarizer.summaryExists(AllTester.summaryTableName,
							   startDate, endDate, ""));

	    //write a summary and then assert that summaryExists returns true
	    storeSt = con.prepareStatement("INSERT INTO " + AllTester.summaryTableName + " (start_time, end_time, num_transfers, total_bytes, num_hosts, avg_size, avg_time, avg_speed, size_stddev, time_stddev, speed_stddev, granularity, src_host) VALUES(?, ?, 42, 42, 42, 42, 42, 42, 42.0, 42.0, 42.0, 1, ?);");
	    storeSt.setLong(1, startDate.getTime());
	    storeSt.setLong(2, endDate.getTime());
	    storeSt.setString(3, "");
	    storeSt.executeUpdate();
	    storeSt.close();

	    Assert.assertTrue("Should now be a summary here",
			      theSummarizer.summaryExists(AllTester.summaryTableName,
							  startDate, endDate, ""));
	    
	}
	catch (SQLException e) {
	    Assert.fail("SQL error in testSummaryExists: "+e.getMessage());
	}			       
    }

    public void testCorrectness() {
	PreparedStatement querySt = null;
	Date startDate = null, endDate = null;
	ResultSet rs = null;

	try {
	    querySt = con.prepareStatement("SELECT num_transfers, num_hosts, total_bytes, avg_size, avg_time, avg_speed, end_time FROM "+AllTester.summaryTableName + ";");

	    cal = theSummarizer.getLastEvenTimestep(Calendar.HOUR,
						Calendar.getInstance().getTime());
	    endDate = cal.getTime();
	    cal.add(Calendar.HOUR, -1);
	    startDate = cal.getTime();

	    theSummarizer.summarizeOneTimeUnit(startDate, endDate, Calendar.HOUR, "", false);
	    rs = querySt.executeQuery();

	    //check that there is one summary entry containing the right data
	    Assert.assertTrue("Should be one summary in db.", rs.next());
	    Assert.assertEquals("Should be ten transfers", rs.getLong("num_transfers"), 10);
	    Assert.assertEquals("Should be four hosts", rs.getLong("num_hosts"), 4);
	    Assert.assertEquals("Total bytes should be ", rs.getLong("total_bytes"), 149011);
	    Assert.assertFalse("Should be ONLY one summary in db.", rs.next());

	    //summarize again to make sure it's idempotent
	    theSummarizer.summarizeOneTimeUnit(startDate, endDate, Calendar.HOUR, "", false);
	    rs = querySt.executeQuery();
	    //check that there is still only one summary entry...
	    Assert.assertTrue("Should STILL be one summary in db.", rs.next());
	    Assert.assertEquals("Should STILL be ten transfers", rs.getLong("num_transfers"), 10);
	    Assert.assertEquals("Should STILL be four hosts", rs.getLong("num_hosts"), 4);
	    Assert.assertEquals("Total bytes should STILL be ", rs.getLong("total_bytes"), 149011);
	    Assert.assertFalse("Should still be only one summary in db.", rs.next());
	    
	    rs.close();
	    querySt.close();
	}
	catch (SQLException e) {
	    Assert.fail("SQL Error in testCorrectness "+e.getMessage());
	}
    }

    public void testSummarize() {
	Date startDate, endDate;
	cal = theSummarizer.getLastEvenTimestep(Calendar.HOUR,
						Calendar.getInstance().getTime());
	endDate = cal.getTime();
	cal.add(Calendar.HOUR, -1);
	startDate = cal.getTime();
	
	try {
	    theSummarizer.summarize(startDate, endDate, Calendar.HOUR, false);
	    Assert.assertTrue("Should now be a summary for general category",
			      theSummarizer.summaryExists(AllTester.summaryTableName,
							  startDate, endDate, ""));
	    Assert.assertTrue("Should now be a summary for mcs-only.",
			      theSummarizer.summaryExists(AllTester.summaryTableName,
					    startDate, endDate, "mcs.anl.gov"));


	    //Now summarize by minute over an hour and asser that there are 60 summaries
	    
	    theSummarizer.summarize(startDate, endDate, Calendar.MINUTE, false);
	    PreparedStatement getSumSt;
	    ResultSet rs;
	    int q = 0;

	    getSumSt = con.prepareStatement("SELECT id FROM "
					    +AllTester.summaryTableName+
					    " WHERE granularity = ?;");
	    getSumSt.setInt(1, Calendar.MINUTE);
	    rs = getSumSt.executeQuery();
	    while (rs.next()) {
		q++;
	    }
	    Assert.assertEquals("Should be 120 by-minute summaries", q, 120);
	}
	catch (SQLException e) {
	    Assert.fail("SQL Error in testSummarize "+e.getMessage());
	}

    }
    
    private Date makeDate(int year, int month, int day, int hour, int minute, int second) {
	Calendar tempCal;
	tempCal = Calendar.getInstance();
	tempCal.set(year, month, day, hour, minute, second);
	tempCal.set(Calendar.MILLISECOND, 0);
	return tempCal.getTime();
    }

    public void testLastEvenTimestep() {
	/*Make sure that no matter what start and end dates you call summarize
	  with, it will break on even timestep boundaries; and that it will not
	  duplicate work between subsequent calls even if the seconds are different.
	  Because crunching up the summaries is by far the most time-consuming part.
	  So we'll test this by starting with some crazy uneven dates...

	  While we're at it, make sure summarizer stops at the end of the last
	complete timestep.*/
	
	Date crazyStartDate1, crazyEndDate1, crazyStartDate2, crazyEndDate2;
	PreparedStatement getSumSt;
	ResultSet rs;
	Calendar roundedCalendar;
	int q = 0;

	crazyStartDate1 = makeDate(2005, 2, 21, 10, 55, 26);

	roundedCalendar = theSummarizer.getLastEvenTimestep(Calendar.MINUTE,
							    crazyStartDate1);
	Assert.assertTrue("Date rounded off to minute",
		  roundedCalendar.getTime().equals(makeDate(2005, 2, 21, 10, 55, 0)));
	roundedCalendar = theSummarizer.getLastEvenTimestep(Calendar.DAY_OF_WEEK, 
							    crazyStartDate1);
	Assert.assertTrue("Date rounded off to day", 
		  roundedCalendar.getTime().equals(makeDate(2005, 2, 21, 0, 0, 0)));
	roundedCalendar = theSummarizer.getLastEvenTimestep(Calendar.WEEK_OF_YEAR, 
							    crazyStartDate1);
	Assert.assertTrue("Date rounded off to week",
                  roundedCalendar.getTime().equals(makeDate(2005, 2, 20, 0, 0, 0)));
	roundedCalendar = theSummarizer.getLastEvenTimestep(Calendar.HOUR, 
							    crazyStartDate1);
	Assert.assertTrue("Date rounded off to hour", 
		    roundedCalendar.getTime().equals(makeDate(2005, 2, 21, 10, 0, 0)));


	cal = Calendar.getInstance(); //now
	crazyEndDate1 = cal.getTime();

	cal.add(Calendar.DAY_OF_WEEK, -1);
	crazyStartDate1 = cal.getTime();
	
	cal.add(Calendar.MINUTE, 20);
	crazyStartDate2 = cal.getTime();

	cal.add(Calendar.DAY_OF_WEEK, 1);
	crazyEndDate2 = cal.getTime();

	try {
	    theSummarizer.summarize(crazyStartDate1, crazyEndDate1,
				    Calendar.HOUR, false);
	    getSumSt = con.prepareStatement("SELECT id FROM "
					    +AllTester.summaryTableName+
					    " WHERE granularity = ?;");
	    getSumSt.setInt(1, Calendar.HOUR);
	    rs = getSumSt.executeQuery();
	    while (rs.next()) {
		q++;
	    }
	    Assert.assertEquals("Should be 48 by-hour summaries", q, 48);
	    
	    
	    theSummarizer.summarize(crazyStartDate1, crazyEndDate1,
				    Calendar.HOUR, false);	
	    rs = getSumSt.executeQuery();
	    q=0;
	    while (rs.next()) {
		q++;
	    }
	    Assert.assertEquals("Should STILL be 48 by-hour summaries", q, 48);
	}
	catch (SQLException e) {
	    Assert.fail("SQL Error in testLastEvenTimestep "+e.getMessage());
	}

    }

    //Now we need tests for aligning send packets with receive packets;
    //and a test that very large numbers of rows returned from a request
    //are all accounted for.
}
