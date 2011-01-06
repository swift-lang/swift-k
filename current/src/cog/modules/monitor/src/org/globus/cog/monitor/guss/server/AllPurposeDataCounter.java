package org.globus.cog.monitor.guss;

import java.util.HashSet;

/*Create an instance of this class, then call crunch() with any number of
  GFTPRecords.  It will keep a running total of the number of unique hosts,
  number of records, total bytes, the average size and duration of a transfer,
  with standard deviations, and the average bytes transferred per second, with
  standard deviation.  These values can be read out with getFinalCount() and
  getStdDev().

  To compute mean and standard deviation cumulatively (i.e. without a second
  pass) I use West's algorithm, which I got from the paper "Computing Standard
  Deviation: Accuracy", Chan & Lewis, published by ACM 1979.
*/

class AllPurposeDataCounter {

    private double accumulator;
    private HashSet uniqueHosts;
    private int howManyRecords;
    
    private long byteTotal;
    private double speedM, speedT;
    private double sizeM, sizeT;
    private double timeM, timeT;
    

    public AllPurposeDataCounter() {
	uniqueHosts = new HashSet();
	howManyRecords = 0;
	byteTotal = 0;
    }

    private void updateMeanSpeed(double datum) {
	/*The first time this is called, mean gets set equal to the first and
	  only datum.  After that, mean and the quantity T are updated with
	  each new datum; the correct standard-deviation of the data so far
	  can be calculated from T at any time.*/
	double Q, R;

	if (howManyRecords == 1)
	    speedM = datum;
	else {
	    Q = datum - speedM;
	    R = Q / howManyRecords;
	    speedM += R;
	    speedT += (howManyRecords - 1) * Q * R;
	}
    }

    private void updateMeanTime(double datum) {
	double Q, R;

	if (howManyRecords == 1)
	    timeM = datum;
	else {
	    Q = datum - timeM;
	    R = Q / howManyRecords;
	    timeM += R;
	    timeT += (howManyRecords - 1) * Q * R;
	}
    }

    private void updateMeanSize(double datum) {
	double Q, R;

	if (howManyRecords == 1)
	    sizeM = datum;
	else {
	    Q = datum - sizeM;
	    R = Q / howManyRecords;
	    sizeM += R;
	    sizeT += (howManyRecords - 1) * Q * R;
	}
    }

    public void crunch(GFTPRecord aRecord) {
	long timeTaken;

	howManyRecords ++;
	uniqueHosts.add(aRecord.getLoggerAddress());
	byteTotal += aRecord.getBytes();

	/*Due to a bug, now fixed, in old versions of globus GFTP server,
	  a few transfers actually get logged with the end earlier
	  than the start, or the exact same start and end time.
	  We obviously can't calculate transfer speed for these!
	  Actually, anything under a second is highly suspect; count
	  only transfers that took longer than one second.*/
	timeTaken = aRecord.getEndTime() - aRecord.getStartTime();

	if (timeTaken > 1000) {
	    updateMeanSpeed(aRecord.getBytes()*1000/timeTaken);
	}
	if (timeTaken > 0) {
	    updateMeanTime(timeTaken/1000);
	}
	updateMeanSize(aRecord.getBytes());
    }

    public double getFinalCount(int whichField) {
	switch (whichField) {
/*	    case GUSSConstants.NUM_USERS:
	    return uniqueUsers.size();*/
	    case GUSSConstants.NUM_HOSTS:
		return uniqueHosts.size();
	    case GUSSConstants.NUM_TRANSFERS: 
		return howManyRecords;
	    case GUSSConstants.TRANSFER_VOLUME:
		return byteTotal;
	    case GUSSConstants.AVG_TIME:
		return timeM;
	    case GUSSConstants.AVG_SIZE:
		return sizeM;
	    case GUSSConstants.AVG_SPEED:
		return speedM;
	    default:
		return 0;
	}
    }

    public double getStdDev(int whichField) {
	/*This only makes sense if whichField is AVG_SPEED, AVG_SIZE, or
	  AVG_TIME.  Also make sure not to divide by zero!*/
	if (howManyRecords < 2)
	    return 0;

	switch (whichField) {
	    case GUSSConstants.AVG_SPEED:
		return Math.sqrt(speedT/(howManyRecords - 1));
	    case GUSSConstants.AVG_TIME:
		return Math.sqrt(timeT/(howManyRecords - 1));
	    case GUSSConstants.AVG_SIZE:
		return Math.sqrt(sizeT/(howManyRecords - 1));
	    default:
		return 0;
	}
    }
}
