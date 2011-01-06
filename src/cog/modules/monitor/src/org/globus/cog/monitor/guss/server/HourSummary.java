package org.globus.cog.monitor.guss;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

//use guss classes: include GUSSService.jar on classpath.
//along with sql connector jar and apache commons jar.
import org.globus.cog.monitor.guss.GFTPRecord;


public class HourSummary {
    /*Represents One Hour.
      Must contain enough statistical info to combine correctly with any other
      summaries.*/
    protected static final short SIZE = 0;
    protected static final short TIME = 1;
    protected static final short SPEED = 2;
    protected static final short STREAMS = 3;

    /*The following needs corresponding fields in database:*/
    protected Date startTime;
    protected Date endTime;

    protected int numNewHosts;
    protected long numRecords;

    protected double meanSize;
    protected double meanTime;
    protected double meanSpeed;
    protected double meanStreams;

    protected double totalTime;
    protected long totalStreams;
    protected double totalSpeed;
    protected long totalBytes;
    
    protected long minSize, maxSize;
    protected double minTime, maxTime;
    protected double minSpeed, maxSpeed;
    protected long minStreams, maxStreams;

    protected double sizeVariance;
    protected double timeVariance;
    protected double speedVariance;
    protected double streamVariance;
    /*"variance" here means sum of (x - mean)^2.  Standard
      deviation can be easily calculated from this and numRecords.*/

    protected HistogramBucketArray sizeHistogram, timeHistogram, speedHistogram, streamHistogram;

    protected HashSet uniqueHosts;


    public HourSummary(Date startTime) {
	int i;

	this.startTime = new Date(startTime.getTime());
	this.endTime = new Date(startTime.getTime() + 3600000);
	numNewHosts = 0;
	numRecords = totalStreams = totalBytes = 0;
	meanSize = meanTime = meanSpeed = meanStreams = 0;
	totalTime = totalSpeed = 0;
	sizeVariance = timeVariance = speedVariance = streamVariance = 0;
	uniqueHosts = new HashSet();
	initHistograms();
    }

    private void initHistograms() {
	sizeHistogram = new HistogramBucketArray(new double[] {0.001, 0.01, 0.1, 1, 10, 100, 1000, 10000, 100000}); //megabytes
	timeHistogram = new HistogramBucketArray(new double[] {0.01, 0.1, 1, 10, 60, 120, 180, 240, 300, 360}); //seconds
	speedHistogram = new HistogramBucketArray(new double[] {1, 10, 100, 1000, 10000, 1000000}); //???
	streamHistogram = new HistogramBucketArray(new double[] {2, 3, 4, 5, 6, 7, 8, 12, 16}); //number of streams

    }

    public HistogramBucketArray getSizeHistogram() {
	return this.sizeHistogram;
    }
    public HistogramBucketArray getTimeHistogram() {
	return this.timeHistogram;
    }
    public HistogramBucketArray getSpeedHistogram() {
	return this.speedHistogram;
    }
    public HistogramBucketArray getStreamHistogram() {
	return this.streamHistogram;
    }


    public HourSummary(ResultSet rs) throws SQLException {
	//creates new summary from open result set, which should be a query that looks like this:
	//maybe make this a string constant or something?

	//"SELECT start_time, num_new_hosts, num_records, mean_size, mean_time, mean_speed, mean_streams, total_time, total_streams, total_speed, total_bytes, min_size, max_size, min_time, max_time, min_speed, max_speed, min_streams, max_streams, size_variance, time_variance, speed_variance, stream_variance, size_buckets, time_buckets, speed_buckets, stream_buckets, hostname_list FROM gftp_summaries WHERE start_time >= ? AND start_time < ?;");
	this.startTime = new Date(rs.getTimestamp(1).getTime());
	this.endTime = new Date(startTime.getTime() + 3600000);

	this.numNewHosts = rs.getInt(2);
	this.numRecords = rs.getLong(3);
	this.meanSize = rs.getDouble(4);
	this.meanTime = rs.getDouble(5);
	this.meanSpeed = rs.getDouble(6);
	this.meanStreams = rs.getDouble(7);
	this.totalTime = rs.getDouble(8);
	this.totalStreams = rs.getLong(9);
	this.totalSpeed = rs.getDouble(10);
	this.totalBytes = rs.getLong(11);
	this.minSize = rs.getLong(12);
	this.maxSize = rs.getLong(13);
	this.minTime = rs.getDouble(14);
	this.maxTime = rs.getDouble(15);
	this.minSpeed = rs.getDouble(16);
	this.maxSpeed = rs.getDouble(17);
	this.minStreams = rs.getLong(18);
	this.maxStreams = rs.getLong(19);
	this.sizeVariance = rs.getDouble(20);
	this.timeVariance = rs.getDouble(21);
	this.speedVariance = rs.getDouble(22);
	this.streamVariance = rs.getDouble(23);

	initHistograms();
	this.sizeHistogram.setFromSQL(rs.getString(24));
	this.timeHistogram.setFromSQL(rs.getString(25));
	this.speedHistogram.setFromSQL(rs.getString(26));
	this.streamHistogram.setFromSQL(rs.getString(27));

	String[] eachName;
	this.uniqueHosts = new HashSet();
	eachName = rs.getString(28).split(",");
	for (int i=0; i<eachName.length; i++) {
	    this.uniqueHosts.add(eachName[i]);
	}
    }

    public void addRecord(GFTPRecord aRecord) {
	long timeTaken;
	double Q, R, transferSpeed;
	int i;

	if (KnownHosts.contains(aRecord.getLoggerAddress())) {
	    KnownHosts.setLastDate(aRecord.getLoggerAddress(),
				   aRecord.getEndDate());
	} else {
	    KnownHosts.setFirstDate(aRecord.getLoggerAddress(), aRecord.getEndDate());
	    numNewHosts ++;
	}

	uniqueHosts.add(aRecord.getLoggerAddress());
	//numNewHosts is never seen before ever;
	//keep hostnamehash in memory while summarizing to save DB hits
	//numHosts increments if never seen before in this summary.
	numRecords ++;

	totalBytes += aRecord.getBytes();

	timeTaken = (aRecord.getEndTime() - aRecord.getStartTime())/1000;
	//convert milliseconds to seconds

	totalTime += timeTaken;

	if (timeTaken <= 0) {
	    //in this case, calculating speed will be /0 error...
	    //round up to 1 millisecond, or leave speed out of 
	    //the calculation entirely?
	    transferSpeed = aRecord.getBytes()*1000/1024;
	} else {
	    transferSpeed = aRecord.getBytes()/timeTaken*1024;
	}
	//units are kb / second

	totalSpeed += transferSpeed;
	totalStreams += aRecord.getNumStreams();

	if (numRecords == 1) {
	    meanSize = minSize = maxSize = aRecord.getBytes();
	    meanTime = minTime = maxTime = timeTaken;
	    meanSpeed = minSpeed = maxSpeed = transferSpeed;
	    meanStreams = minStreams = maxStreams = aRecord.getNumStreams();
	} else {
	    /*Update mean and variance*/
	    Q = aRecord.getBytes() - meanSize;
	    R = Q / (double)numRecords;
	    meanSize += R;
	    sizeVariance += (numRecords - 1) * Q * R;

	    Q = timeTaken - meanTime;
	    R = Q / (double)numRecords;
	    meanTime += R;
	    timeVariance += (numRecords - 1) * Q * R;

	    Q = transferSpeed - meanSize;
	    R = Q / (double)numRecords;
	    meanSpeed += R;
	    speedVariance += (numRecords - 1) * Q * R;

	    Q = aRecord.getNumStreams() - meanStreams;
	    R = Q / (double)numRecords;
	    meanStreams += R;
	    streamVariance += (numRecords - 1) * Q * R;

	    /*Update min and max*/
	    if (aRecord.getBytes() < minSize) {
		minSize = aRecord.getBytes();
	    } else if (aRecord.getBytes() > maxSize) {
		maxSize = aRecord.getBytes();
	    }

	    if (timeTaken < minTime) {
		minTime = timeTaken;
	    } else if (timeTaken > maxTime) {
		maxTime = timeTaken;
	    }

	    if (transferSpeed < minSpeed) {
		minSpeed = transferSpeed;
	    } else if (transferSpeed > maxSpeed) {
		maxSpeed = transferSpeed;
	    }

	    if (aRecord.getNumStreams() < minStreams) {
		minStreams = aRecord.getNumStreams();
	    } else if (aRecord.getNumStreams() > maxStreams) {
		maxStreams = aRecord.getNumStreams();
	    }
	}
	//next update buckets
	//for time, size, speed, and streams
	
	sizeHistogram.sortValue(aRecord.getBytes()/ 1048576);
	//Convert bytes to megabytes

	timeHistogram.sortValue(timeTaken);
	//units are seconds

	speedHistogram.sortValue(transferSpeed);
	//units are KB / second

	streamHistogram.sortValue(aRecord.getNumStreams());
    }

    public int getNumHosts() {
	return uniqueHosts.size();
    }

    public double getStdDev(short whichVariable) {
	if (numRecords > 1) {
	    switch (whichVariable) {
	    case SIZE:
		return Math.sqrt(sizeVariance/(numRecords - 1));
	    case TIME:
		return Math.sqrt(timeVariance/(numRecords - 1));
	    case SPEED:
		return Math.sqrt(speedVariance/(numRecords - 1));
	    case STREAMS:
		return Math.sqrt(streamVariance/(numRecords - 1));
	    }
	}
	return 0;
    }

    public String getHostNameList() {
	Iterator it = uniqueHosts.iterator();
	StringBuffer output = new StringBuffer();
	String hostname;

	if (it.hasNext()) {
	    output.append((String)it.next());
	}
	while (it.hasNext()) {
	    hostname = (String)it.next();
	    output.append(",");
	    output.append(hostname);
	}
	return output.toString();
    }

    public String getCountries() {
	/*Returns list of all unique domain name suffixes found in 
	  UniqueHosts.  How bout we store the number of unique hosts in each domain name
	in another DB table?*/

	Iterator it = uniqueHosts.iterator();
	String hostname;
	HashSet suffixes = new HashSet();
	StringBuffer output = new StringBuffer();

	while (it.hasNext()) {
	    hostname = (String)it.next();
	    String[] parts = hostname.split("/");
	    //Part before the slash is domain name, after is IP.
	    hostname = parts[0];
	    if (hostname.indexOf('.') != -1) {
		parts = hostname.split("\\."); //this is a regexp so . must be escaped
		suffixes.add(parts[parts.length-1]);
	    }
	}

	it = suffixes.iterator();
	while (it.hasNext()) {
	    output.append((String)it.next());
	    output.append(", ");
	}
	return output.toString();
    }

    public void storeToDB(Connection con) {
	String tableName = "gftp_summaries";
	PreparedStatement stmt;
	StringBuffer sqlStatement = new StringBuffer();
        try {
	    sqlStatement.append("INSERT INTO gftp_summaries (start_time, num_hosts, num_new_hosts, num_records, mean_size, mean_time, mean_speed, mean_streams, total_bytes, total_time, total_speed, total_streams, min_size, max_size, min_time, max_time, min_speed, max_speed, min_streams, max_streams, size_variance, time_variance, speed_variance, stream_variance, hostname_list, size_buckets, time_buckets, speed_buckets, stream_buckets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ");


	    sqlStatement.append(sizeHistogram.toString());
	    sqlStatement.append(", ");
	    sqlStatement.append(timeHistogram.toString());
	    sqlStatement.append(", ");
	    sqlStatement.append(speedHistogram.toString());
	    sqlStatement.append(", ");
	    sqlStatement.append(streamHistogram.toString());
	    sqlStatement.append(");");
	    
	    stmt = con.prepareStatement(sqlStatement.toString());
	    stmt.setTimestamp(1, new Timestamp(startTime.getTime()));
	    stmt.setInt(2, getNumHosts());
	    stmt.setInt(3, numNewHosts);
	    stmt.setLong(4, numRecords);
	    stmt.setDouble(5, meanSize);
	    stmt.setDouble(6, meanTime);
	    stmt.setDouble(7, meanSize);
	    stmt.setDouble(8, meanStreams);
	    stmt.setLong(9, totalBytes);
	    stmt.setDouble(10, totalTime);
	    stmt.setDouble(11, totalSpeed);
	    stmt.setLong(12, totalStreams);
	    stmt.setLong(13, minSize);
	    stmt.setLong(14, maxSize);
	    stmt.setDouble(15, minTime);
	    stmt.setDouble(16, maxTime);
	    stmt.setDouble(17, minSpeed);
	    stmt.setDouble(18, maxSpeed);
	    stmt.setLong(19, minStreams);
	    stmt.setLong(20, maxStreams);
	    stmt.setDouble(21, sizeVariance);
	    stmt.setDouble(22, timeVariance);
	    stmt.setDouble(23, speedVariance);
	    stmt.setDouble(24, streamVariance);
	    /*
	    	    stmt.setArray(25, sizeHistogram);
	    	    stmt.setArray(26, timeHistogram);
	    	    stmt.setArray(27, speedHistogram);
	    	    stmt.setArray(28, streamHistogram);
	    */
	    stmt.setString(25, getHostNameList());
	    stmt.executeUpdate();
	    stmt.close();

	    KnownHosts.writeToDatabase(con);
        }
        
        catch( SQLException e ) {
	    e.printStackTrace();
	}	
    }


    public void display() {
	System.out.println("Summary of GridFTP from " + startTime + " to " + endTime);
	System.out.println(numRecords + " transfers were made by " + getNumHosts() + " hosts, " + numNewHosts + " of which have never been seen before.");

	System.out.println("         File Sizes:       Times:       Speeds:      Streams:");
	System.out.println("Mean: " + meanSize + "   " + meanTime + "    " + meanSpeed + "     " + meanStreams);
	System.out.println("Min: " + minSize + "    " + minTime + "     " + minSpeed + "      " + minStreams);
	System.out.println("Max: " + maxSize + "    " + maxTime + "      " + minSpeed + "      " + maxStreams);
	System.out.println("Total: " + totalBytes + "    " + totalTime + "    " + totalSpeed + "     " + totalBytes);
	System.out.println("Standard dev: " + getStdDev(SIZE) + "    " + getStdDev(TIME) + "    " + getStdDev(SPEED) + "     " + getStdDev(STREAMS));

	//System.out.println("Behold the histograms:");
	//sizeHistogram.screenDump();

	System.out.println("Top-level domains: " + getCountries());
    }
}


