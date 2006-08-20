package org.globus.cog.monitor.guss;

import java.util.Iterator;
import java.util.Date;
import java.util.HashSet;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;

public class AggregateSummary extends HourSummary {
    /*Represents any time longer than One Hour;
      made by combining several GoodSummaries.*/

    //protected Date startTime;  //inherited
    private int numHoursAdded;
    private HistogramBucketArray transfersPerHourHistogram;

    public AggregateSummary(Date startTime) {
	//FIXME the difference between startTime and endTime is variable.
	//do we specify in advance the time covered and only accept
	//summaries within that time?
	//or do we add a bunch of summaries and calculate the time covered
	//from that?
	super(startTime);
	this.startTime = new Date(startTime.getTime());
	this.endTime = new Date(startTime.getTime() + 3600000);
	this.numHoursAdded = 0;
	this.numRecords = 0;
	this.numNewHosts = 0;
	this.totalBytes =0; 
	this.totalTime =0; 
	this.totalSpeed =0; 
	this.totalStreams =0; 

	transfersPerHourHistogram = new HistogramBucketArray(new double[] {50, 100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200});
    }

    public void addSummary(HourSummary aSummary) {
	double newTotalMeanSize, newTotalMeanTime, newTotalMeanSpeed, newTotalMeanStreams;

	if (numHoursAdded == 0) {
	    //first hour being added

	    this.meanSize = aSummary.meanSize;
	    this.meanTime = aSummary.meanTime;
	    this.meanSpeed = aSummary.meanSpeed;
	    this.meanStreams = aSummary.meanStreams;
	    this.sizeVariance = aSummary.sizeVariance;
	    this.timeVariance = aSummary.timeVariance;
	    this.speedVariance = aSummary.speedVariance;
	    this.streamVariance = aSummary.streamVariance;

	    this.minSize = aSummary.minSize;
	    this.maxSize = aSummary.maxSize;
	    this.minTime = aSummary.minTime;
	    this.maxTime = aSummary.maxTime;
	    this.minSpeed = aSummary.minSpeed;
	    this.maxSpeed = aSummary.maxSpeed;
	    this.minStreams = aSummary.minStreams;
	    this.maxStreams = aSummary.maxStreams;
	    
	    this.uniqueHosts = new HashSet(aSummary.uniqueHosts);
	    this.sizeHistogram = new HistogramBucketArray(aSummary.sizeHistogram);
	    this.timeHistogram = new HistogramBucketArray(aSummary.timeHistogram);
	    this.speedHistogram = new HistogramBucketArray(aSummary.speedHistogram);
	    this.streamHistogram = new HistogramBucketArray(aSummary.streamHistogram);
	} else {
	    //addding the second, etc. summary: combine means and variances
	    //in the statistically correct way:
	    newTotalMeanSize = combineMeans(this.meanSize, aSummary.meanSize, this.numRecords, aSummary.numRecords);
	    newTotalMeanTime = combineMeans(this.meanTime, aSummary.meanTime, this.numRecords, aSummary.numRecords);
	    newTotalMeanSpeed = combineMeans(this.meanSpeed, aSummary.meanSpeed, this.numRecords, aSummary.numRecords);
	    newTotalMeanStreams = combineMeans(this.meanStreams, aSummary.meanStreams, this.numRecords, aSummary.numRecords);

	    this.sizeVariance = combineVariances(this.sizeVariance,
						 aSummary.sizeVariance,
						 this.meanSize,
						 aSummary.meanSize,
						 this.numRecords,
						 aSummary.numRecords,
						 this.totalBytes,
						 aSummary.totalBytes,
						 newTotalMeanSize);
	    this.timeVariance = combineVariances(this.timeVariance,
						 aSummary.timeVariance,
						 this.meanTime,
						 aSummary.meanTime,
						 this.numRecords,
						 aSummary.numRecords,
						 this.totalTime,
						 aSummary.totalTime,
						 newTotalMeanTime);
	    this.speedVariance = combineVariances(this.speedVariance,
						 aSummary.speedVariance,
						 this.meanSpeed,
						 aSummary.meanSpeed,
						 this.numRecords,
						 aSummary.numRecords,
						 this.totalSpeed,
						 aSummary.totalSpeed,
						 newTotalMeanSpeed);
	    this.streamVariance = combineVariances(this.streamVariance,
						 aSummary.streamVariance,
						 this.meanStreams,
						 aSummary.meanStreams,
						 this.numRecords,
						 aSummary.numRecords,
						 this.totalStreams,
						 aSummary.totalStreams,
						 newTotalMeanStreams);

	    this.meanSize = newTotalMeanSize;
	    this.meanTime = newTotalMeanTime;
	    this.meanSpeed = newTotalMeanSpeed;
	    this.meanStreams = newTotalMeanStreams;
	    
	    Iterator it = aSummary.uniqueHosts.iterator();
	    while (it.hasNext()) {
		this.uniqueHosts.add(it.next());
	    }

	    this.sizeHistogram.combine(aSummary.sizeHistogram);
	    this.timeHistogram.combine(aSummary.timeHistogram);
	    this.speedHistogram.combine(aSummary.speedHistogram);
	    this.streamHistogram.combine(aSummary.streamHistogram);
	    
	    /*Update min and max*/
	    if (aSummary.minSize < this.minSize) {
		this.minSize = aSummary.minSize;
	    }
	    if (aSummary.maxSize > this.maxSize) {
		this.maxSize = aSummary.maxSize;
	    }
	    if (aSummary.minTime < this.minTime) {
		this.minTime = aSummary.minTime;
	    }
	    if (aSummary.maxTime > this.maxTime) {
		this.maxTime = aSummary.maxTime;
	    }
	    if (aSummary.minSpeed < this.minSpeed) {
		this.minSpeed = aSummary.minSpeed;
	    }
	    if (aSummary.maxSpeed > this.maxSpeed) {
		this.maxSpeed = aSummary.maxSpeed;
	    }
	    if (aSummary.minStreams < this.minStreams) {
		this.minStreams = aSummary.minStreams;
	    }
	    if (aSummary.maxStreams > this.maxStreams) {
		this.maxStreams = aSummary.maxStreams;
	    }
	}

	this.numRecords += aSummary.numRecords;
	this.numNewHosts += aSummary.numNewHosts;
	this.totalBytes += aSummary.totalBytes;
	this.totalTime += aSummary.totalTime;
	this.totalSpeed += aSummary.totalSpeed;
	this.totalStreams += aSummary.totalStreams;

	this.numHoursAdded ++;

	transfersPerHourHistogram.sortValue(aSummary.numRecords);
    }

    public HistogramBucketArray getTransfersPerHourHistogram() {
	return transfersPerHourHistogram;
    }


    private double combineMeans(double mean1, double mean2, long number1, long number2) {
	return (mean1*number1 + mean2*number2) / (number1 * number1);
    }

    private double combineVariances(double variance1, double variance2,
				    double mean1, double mean2,
				    long number1, long number2,
				    double total1, double total2,
				    double meanAll) {

	return (variance1 + variance2 + 
		2*(mean1 - meanAll)*total1 + 
		number1*(mean1*mean1 + meanAll*meanAll) +
		2*(mean2 - meanAll)*total2 + 
		number2*(mean2*mean2 + meanAll*meanAll));
	//double-check my math here...
    }


    public void display() {
	super.display();

	System.out.println("Here is the histogram of packets per hour within this aggregate");
	transfersPerHourHistogram.screenDump();
    }

    public void fillFromDB(Connection con, Date startDate, Date endDate) throws SQLException {
	//Gets every hourly record from the DB between the dates,
	//aggregates them all.

	PreparedStatement stmt = con.prepareStatement("SELECT start_time, num_new_hosts, num_records, mean_size, mean_time, mean_speed, mean_streams, total_time, total_streams, total_speed, total_bytes, min_size, max_size, min_time, max_time, min_speed, max_speed, min_streams, max_streams, size_variance, time_variance, speed_variance, stream_variance, size_buckets, time_buckets, speed_buckets, stream_buckets, hostname_list FROM gftp_summaries WHERE start_time >= ? AND start_time < ?;");
	
	stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
	stmt.setTimestamp(2, new Timestamp(endDate.getTime()));

	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    HourSummary aSummary = new HourSummary(rs);
	    this.addSummary(aSummary);
	}

	rs.close();
	stmt.close();
	this.startTime = startDate;
	this.endTime = endDate;
    }
}
