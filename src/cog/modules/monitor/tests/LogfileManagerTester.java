package org.globus.cog.monitor.guss;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestSuite;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class LogfileManagerTester extends TestCase {

    private final String logfilePath = "file:///home/jdicarlo/src/cog/modules/monitor/tests/dummy_gridftp.log";
    private final String logfileBackupPath = "file:///home/jdicarlo/src/cog/modules/monitor/tests/dummy_gridftp.log.bak";

    private LogfileManager theLogfileManager;

    public LogfileManagerTester(String name) {
	super(name);

	try {
	    theLogfileManager = new LogfileManager(AllTester.metadataTableName);
	}
	catch (ClassNotFoundException cnfe) {
	    Assert.fail("Can't find database driver: "+cnfe.getMessage());
	}
	catch (SQLException sqle) {
	    Assert.fail("SQL error: "+sqle.getMessage());
	}

    }

    private void copyFile(String sourcePath, String destPath) {
	URL sourceURL;
	URL destURL;
	File infile; 
	File outfile;

	try {
	    sourceURL = new URL(sourcePath);
	    destURL = new URL(destPath);
	    infile = new File(sourceURL.getFile());
	    outfile = new File(destURL.getFile());
	
	    BufferedReader reader = new BufferedReader(new FileReader(infile));
	    BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		writer.write(line);
		writer.newLine();
	    }
	    writer.flush();
	    reader.close();
	    writer.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    Assert.fail("Can't copy backup logfile.");
	}
    }

    private void appendToFile(String destPath, String lineToAppend) {
	URL destURL;
	File outfile;
	BufferedWriter writer;

	try {
	    destURL = new URL(destPath);
	    outfile = new File(destURL.getFile());
	
	    writer = new BufferedWriter(new FileWriter(outfile, true));
	    //the true means open in append mode.

	    writer.write(lineToAppend);
	    writer.newLine();
	    writer.flush();
	    writer.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	    Assert.fail("Can't append to logfile.");
	}
    }

    protected void setUp() {
       	//To get to a known state, drop and recreate the tables in database 'test'.
	AllTester.prepareTestDatabase();
	/*Restore the dummy logfile from its backup.
	(by copying the other textfile line by line: not efficient, but OK for
	a test.)*/
	copyFile(logfileBackupPath, logfilePath);
    }

    public void testBasics() {
	ArrayList results;

	theLogfileManager.putFileInDatabase(logfilePath, AllTester.recordsTableName);
	//There are ten lines in that dummy logfile, so there should now be ten
	//records in the database:

	try {
	    results = theLogfileManager.getAllRecordsFromDatabase(AllTester.recordsTableName);

	    Assert.assertEquals("Should have gotten ten lines from logfile",
			    10, results.size());
	    /*TODO Put a test here about contents of first and last lines*/
	}
	catch (SQLException e) {
	    e.printStackTrace();
	    Assert.fail("SQL error!");
	}
    }

    public void testBookmarking() {
	ArrayList results;
	String logfileAddition = "DATE=20040609163425.483923 HOST=jmayor1 PROG=wuftpd NL.EVNT=FTP_INFO START=20040609163425.339678 USER=voeckler FILE=/home/voeckler/vdldemo/test.tmp BUFFER=524288 BLOCK=65536 NBYTES=47 VOLUME=/home STREAMS=1 STRIPES=1 DEST=1[128.135.152.241] TYPE=STOR CODE=227";
	ResultSet rs;
	PreparedStatement pstmt;
	LogMetaData data;
	GFTPRecord gftpFromString;

	theLogfileManager.putFileInDatabase(logfilePath, AllTester.recordsTableName);
	theLogfileManager.putFileInDatabase(logfilePath, AllTester.recordsTableName);
	/*Read in the same logfile twice; the second time it should be able to see
	  from the bookmark entry of the file in logfile_metadata table that this
	  file has already been read, and so it should not read in any more
	  records the second time.*/
	try {
	    results = theLogfileManager.getAllRecordsFromDatabase(AllTester.recordsTableName);

	    Assert.assertEquals("Reading the same file again should add no new records",10, results.size());
	    /*TODO Put a test here about contents of first and last lines*/

	    
	    /*Next we want to extend the file with one new record and then try
	     * again to read it; we should read just the one new record, for a
	     total of 11..*/	    
	    appendToFile(logfilePath, logfileAddition);
	    theLogfileManager.putFileInDatabase(logfilePath, AllTester.recordsTableName);
	    results = theLogfileManager.getAllRecordsFromDatabase(AllTester.recordsTableName);
	    Assert.assertEquals("Reading the same file after modification should get one new record.", 11, results.size());
	    //Verify the contents of that 11th record:
	    gftpFromString = new GFTPRecord(logfileAddition);
	    Assert.assertEquals("Record should match!", gftpFromString,
				results.get(10));
	    Assert.assertTrue("Optional fields should match",
		  gftpFromString.optionalFieldsAreEqual((GFTPRecord)results.get(10)));


	    //Now let's check that logfile_metadata table contains what it should:
	    pstmt= LogMetaData.prepareSQLQuery(theLogfileManager.con,
					       AllTester.metadataTableName);
	    pstmt.setString(1, logfilePath);
	    rs = pstmt.executeQuery();

	    //There should be exactly ONE entry matching the logfilepath:
	    Assert.assertTrue("Should have a metadata entry", rs.next());
	    data = new LogMetaData(rs);
	    Assert.assertFalse("Should have ONLY ONE metadata entry", rs.next());
	    Assert.assertEquals("metadata bookmark should be at 2994.",
				data.getBookmark(), 2994);
	    rs.close();
	    pstmt.close();
	}
	catch (SQLException e) {
	    e.printStackTrace();
	    Assert.fail("SQL error!");
	}
	catch (IOException ioe) {
	    ioe.printStackTrace();
	    Assert.fail("Can't make GFTPRecord from string!");
	}
    }
}
