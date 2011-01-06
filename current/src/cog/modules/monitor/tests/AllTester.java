package org.globus.cog.monitor.guss;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestSuite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


public class AllTester {

    /*These constants define how to connect to the test database that will be
      used for all tests.  Pointing it at database "test" ensures we won't
      accidentally destroy any real data.*/
    static final String dburl = "jdbc:postgresql://mayed.mcs.anl.gov/test";
    static final String driverClass = "org.postgresql.Driver";
    static final String username = "jdicarlo";
    static final String password = "abcdefg";

    static final String recordsTableName = "gftp_packets";
    static final String metadataTableName = "logfile_metadata";
    static final String summaryTableName = "gftp_summaries";
    //there are also unknown_packets, rft_packets, etc.

    static Connection con = null;

    private static TestSuite suite() {
	TestSuite suite= new TestSuite(); 
	suite.addTest(new TestSuite(SummarizerTester.class));
//	suite.addTest(new TestSuite(GFTPRecordTester.class)); 
//	suite.addTest(new TestSuite(LogfileManagerTester.class)); 
//	suite.addTest(new TestSuite(FilterTester.class));
//	suite.addTest(new TestSuite(GUSSImplTester.class));
//	suite.addTest(new TestSuite(TimeSorterTester.class));
	return suite;
    }

    /*The starting point for the whole GUSS test suite:*/
    public static void main(String args[]) {
	try {
	    //Initialize database connection:
	    OneConnectionPool.create(driverClass, dburl, username, password);
	    con = OneConnectionPool.getConnection();
	    //Run all tests:
	    junit.textui.TestRunner.run(suite());
	    con.close();
	    OneConnectionPool.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /*Utility function that can be used in the setup of any test to ensure that
      database tables in test are empty:*/
    static void prepareTestDatabase() {
	Statement stmt;
	try {  
	    stmt = con.createStatement();
	    //stmt.executeUpdate(clearDBCmd);  //why doesn't this work???
	    //stmt.executeUpdate("USE test;");
	    /*Should put double and triple checks in here to make sure that
	      the next lines cannot run unless we're in a database called test:
	      I'm not dropping the real database tables... that would be
	      a tragedy.*/

	    stmt.executeUpdate("DROP TABLE "+recordsTableName +";");
	    stmt.executeUpdate("DROP SEQUENCE "+recordsTableName +"_id_seq;");
//	    stmt.executeUpdate("DROP TABLE "+metadataTableName+";");
//	    stmt.executeUpdate("DROP TABLE "+metadataTableName+"_id_seq;");
	    stmt.executeUpdate("DROP TABLE "+summaryTableName+";");
	    stmt.executeUpdate("DROP SEQUENCE "+summaryTableName+"_id_seq;");


	    stmt.executeUpdate(
		"CREATE TABLE "+recordsTableName+"(id SERIAL, component_code SMALLINT NOT NULL, version_code SMALLINT NOT NULL, send_time DATETIME, ip_version SMALLINT, ip_address VARCHAR(64) NOT NULL, gftp_version VARCHAR(64), stor_or_retr SMALLINT, start_time BIGINT NOT NULL, end_time BIGINT NOT NULL, num_bytes BIGINT, num_stripes INT, num_streams INT, buffer_size INT, block_size  INT, ftp_return_code INT, sequence_number BIGINT, src_id BIGINT, dest_id BIGINT, reserved BIGINT, PRIMARY KEY (id));"
		);

	    stmt.executeUpdate(
		"CREATE TABLE "+summaryTableName+"(id SERIAL, start_time BIGINT NOT NULL, end_time BIGINT NOT NULL, granularity INT, src_host VARCHAR(64), dest_host VARCHAR(64), num_transfers BIGINT, total_bytes BIGINT, num_hosts INT, avg_size BIGINT, avg_time BIGINT, avg_speed BIGINT, size_stddev DOUBLE PRECISION, time_stddev DOUBLE PRECISION, speed_stddev DOUBLE PRECISION, PRIMARY KEY (id));"
		);

/*	    stmt.executeUpdate(
		"CREATE TABLE "+metaDataTableName+"(host_id SERIAL, host_name VARCHAR(20), ip_address VARCHAR(20), log_path TINYTEXT, log_bookmark BIGINT, log_last_read TIMESTAMP, PRIMARY KEY (host_id));"
		);*/

	    stmt.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	} 

    }

}
