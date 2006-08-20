package org.globus.cog.monitor.guss;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestSuite;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class GFTPRecordTester extends TestCase {

    private Connection con;

    private GFTPRecord g1, g2, g3, g4;
	/*Before tests: Set up database connection;
	  clear out a table in test database so we have a clean
	  starting point.*/

    public GFTPRecordTester(String name) {
	super(name);

    }

    protected void setUp() {
	/*Before each test:  Get a database connection; make a bunch of
	  sample records

	*/
	Date startDate, endDate;
	Calendar tempCal = Calendar.getInstance();
	Statement stmt;
	LogfileManager theLogfileManager;

	try {
	    con = OneConnectionPool.getConnection();
	    
	} catch (SQLException sqe) {
	    sqe.printStackTrace();
	}

	tempCal.set(2004, Calendar.OCTOBER, 15, 8, 30, 0);
	startDate = tempCal.getTime();
	tempCal.set(2004, Calendar.OCTOBER, 15, 8, 30, 30);
	endDate = tempCal.getTime();

	g1 = new GFTPRecord(startDate, endDate, "127.0.0.1", GFTPRecord.STOR, 8701);

	tempCal.set(2004, Calendar.OCTOBER, 15, 8, 31, 0);
	startDate = tempCal.getTime();
	tempCal.set(2004, Calendar.OCTOBER, 15, 8, 31, 30);
	endDate = tempCal.getTime();

	g2 = new GFTPRecord(startDate, endDate, "127.0.0.1", GFTPRecord.RETR, 15086);

	g3 = new GFTPRecord(startDate, endDate, "127.0.0.2", GFTPRecord.RETR, 65000);

	tempCal.set(2003, Calendar.NOVEMBER, 15, 13, 31, 0);
	startDate = tempCal.getTime();
	tempCal.set(2003, Calendar.NOVEMBER, 15, 13, 35, 0);
	endDate = tempCal.getTime();

	g4 = new GFTPRecord(startDate, endDate, "127.0.0.1", GFTPRecord.STOR, 999);


	AllTester.prepareTestDatabase();
	
    }

    protected void tearDown() {
	try {
	    con.close();
	} catch (SQLException sqe) {}
    }

    public void testToDatabase() {
	Date startDate, endDate;
	Calendar tempCal = Calendar.getInstance();	
	InetAddress pretendServer = null;
	ArrayList results;

	tempCal.set(2004, Calendar.OCTOBER, 15, 8, 29, 0);
	startDate = tempCal.getTime();
	tempCal.set(2004, Calendar.OCTOBER, 15, 8, 32, 0);
	endDate = tempCal.getTime();

	try {
	    pretendServer = InetAddress.getByName("127.0.0.1");
	} catch (UnknownHostException une) {
	    une.printStackTrace();
	}
	try {
	    /*Database was just emptied, so this should return no records.*/
	    results = GFTPRecord.getRecordsFromDatabase(con, AllTester.recordsTableName,
							pretendServer,
							startDate, endDate);
	    Assert.assertEquals("Table should be empty", results.size(), 0);

	    /*Put in one record, get it back out, make sure it matches.*/
	    Assert.assertTrue("Insert g1 should succeed", 
			      g1.insertIntoDatabase(con, AllTester.recordsTableName));
	    results = GFTPRecord.getRecordsFromDatabase(con, AllTester.recordsTableName,
							pretendServer,
							startDate, endDate);
	    Assert.assertEquals("Table should contain 1", results.size(), 1);
	    Assert.assertTrue("Record g1 should be equal", g1.equals(results.get(0)));
	
	    Assert.assertTrue("Insert g2 should succeed",
			      g2.insertIntoDatabase(con, AllTester.recordsTableName));
	    results = GFTPRecord.getRecordsFromDatabase(con, AllTester.recordsTableName,
							pretendServer,
							startDate, endDate);
	    Assert.assertEquals("Should now be 2 records here", results.size(), 2);
	    Assert.assertTrue("g1 should still match", g1.equals(results.get(0)));
	    Assert.assertTrue("g2 should match", g2.equals(results.get(1)));
	} catch(SQLException e) {
	    Assert.fail(e.getMessage());
	}
    }


    public void testDatabaseRetrievalCriteria() {
	Date startDate, endDate;
	Calendar tempCal = Calendar.getInstance();	
	InetAddress pretendServer = null;
	ArrayList results;

	/*Make sure that we DON'T get back records outside of the times and hosts
	  we specified.*/

	try {
	    pretendServer = InetAddress.getByName("127.0.0.1");
	} catch (UnknownHostException une) {
	    une.printStackTrace();
	}
	try {
	    Assert.assertTrue("Insert should succeed",
			      g1.insertIntoDatabase(con, AllTester.recordsTableName));
	    Assert.assertTrue("Insert should succeed",
			      g2.insertIntoDatabase(con, AllTester.recordsTableName));
	    Assert.assertTrue("Insert should succeed",
			      g3.insertIntoDatabase(con, AllTester.recordsTableName));
	    Assert.assertTrue("Insert should succeed",
			      g4.insertIntoDatabase(con, AllTester.recordsTableName));

	    tempCal.set(2004, Calendar.OCTOBER, 15, 8, 29, 0);
	    startDate = tempCal.getTime();
	    tempCal.set(2004, Calendar.OCTOBER, 15, 8, 32, 0);
	    endDate = tempCal.getTime();

	    results = GFTPRecord.getRecordsFromDatabase(con, AllTester.recordsTableName,
							pretendServer,
							startDate, endDate);
	    Assert.assertEquals("Table should contain 2", results.size(), 2);
	    Assert.assertTrue("Record g1 should be equal", g1.equals(results.get(0)));
	    Assert.assertTrue("Record g2 should be equal", g2.equals(results.get(1)));
	    //this should NOT return g3, which is right time but wrong IP, or g4 which
	    //is wrong time.
	
	    tempCal.set(2003, Calendar.NOVEMBER, 1, 12, 0, 0);
	    startDate = tempCal.getTime();
	    tempCal.set(2003, Calendar.NOVEMBER, 31, 12, 0, 0);
	    endDate = tempCal.getTime();
	    results = GFTPRecord.getRecordsFromDatabase(con, AllTester.recordsTableName,
							pretendServer,
							startDate, endDate);
	    Assert.assertEquals("Should now be 1 record here", results.size(), 1);
	    Assert.assertTrue("g4 should match", g4.equals(results.get(0)));
	    //this time should return only g4, the only record in november 2003.


	    /*TODO: put tests here where stuff is right on the time boundaries and
	      see what happens; test other IP addresses*/
	} catch(SQLException e) {
	    Assert.fail(e.getMessage());
	}
    }

    public void testLogfileInput() {
	/*check that it reads correctly from logfile (GFTPRecord(lineToParse))
	  check that it gets dates correctly from logfile (dateFromLogfile)
	  put into database, get back out, check that everything is still the same.*/

	String logfileLine = "DATE=20040609163425.483923 HOST=jmayor1 PROG=wuftpd NL.EVNT=FTP_INFO START=20040609163425.339678 USER=voeckler FILE=/home/voeckler/vdldemo/test.tmp BUFFER=524288 BLOCK=65536 NBYTES=47 VOLUME=/home STREAMS=1 STRIPES=1 DEST=1[128.135.152.241] TYPE=STOR CODE=227";
	
	GFTPRecord gl1;
	Calendar tempCal = Calendar.getInstance();
	Date startDate, endDate;
	ArrayList results;

	try {
	    gl1 = new GFTPRecord(logfileLine);
	    Assert.assertEquals("Num bytes should match", gl1.getBytes(), 47);
	    tempCal.set(2004, Calendar.JUNE, 9, 16, 34, 25);
	    tempCal.set(Calendar.MILLISECOND, 340);
	    startDate = tempCal.getTime();
	    Assert.assertTrue("Start date should match", gl1.getStartDate().equals(startDate));
	    tempCal.set(Calendar.MILLISECOND, 484);
	    endDate = tempCal.getTime();
	    Assert.assertTrue("End date should match", gl1.getEndDate().equals(endDate));

	    Assert.assertTrue("Insert should succeed",
			      gl1.insertIntoDatabase(con, AllTester.recordsTableName));
	    results = GFTPRecord.getRecordsFromDatabase(con, AllTester.recordsTableName,
							"jmayor1",
							startDate, endDate);
	    Assert.assertEquals("Should get back one record", results.size(), 1);
	    Assert.assertEquals("Record should match!", gl1, results.get(0));
	    Assert.assertTrue("Optional fields should match",
			      gl1.optionalFieldsAreEqual((GFTPRecord)results.get(0)));
	} catch(SQLException e) {
	    Assert.fail(e.getMessage());
	} catch (IOException e) {
	    Assert.fail("Should be able to parse that above");
	}

    }

}
