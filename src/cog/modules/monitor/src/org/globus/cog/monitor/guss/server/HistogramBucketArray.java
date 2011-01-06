package org.globus.cog.monitor.guss;

import java.sql.Array;
import java.sql.Types;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.Map;

class HistogramBucketArray implements java.sql.Array {
    //of doubles
    long[] contents;
    double[] divisions;
    int numBuckets;

    public HistogramBucketArray(double[] divisions) {
	this.divisions = divisions;
	this.numBuckets = divisions.length + 1;
	contents = new long[this.numBuckets];
	reset();
    }

    public HistogramBucketArray(HistogramBucketArray copyMe) {
	this.numBuckets = copyMe.numBuckets;
	this.divisions = new double[this.numBuckets - 1];
	this.contents = new long[this.numBuckets];

	for (int i=0; i<this.numBuckets-1; i++) {
	    this.divisions[i] = copyMe.divisions[i];
	    this.contents[i] = copyMe.contents[i];
	}
	this.contents[this.numBuckets-1] = copyMe.contents[this.numBuckets-1];
    }

    public long[] getContents() {
	return contents;
    }

    public double[] getDivisions() {
	return divisions;
    }

    public void setFromSQL(String inputArray) throws SQLException {
	//The array will come in the form "{1,2,3}".
	//Split it into numbers and store.
	String[] numbers = inputArray.split("\\{|,|\\}");
	int i;
	//starting the loop from 1 is intentional
	//the way java String.split works, since the input starts with {,
	//numbers[0] will be an empty string.
	for (i=1; i<numbers.length; i++) {
	    contents[i - 1] = Integer.parseInt(numbers[i]);
	}
    }

    public void combine(HistogramBucketArray moreData) {
	for (int i=0; i<this.numBuckets; i++) {
	    this.contents[i] += moreData.contents[i];
	}
    }

    public String getBaseTypeName() throws SQLException {
	return "integer";
    }

    public void reset() {
	for (int i = 0; i<this.numBuckets; i++) {
	    contents[i] = 0;
	}
    }

    public void sortValue(double value) {
	int i=0;

	while (i < divisions.length &&
	       value > divisions[i]) {
	    i++;
	}
	contents[i] ++;

	/*So if size is smaller than the first division, it goes
	  into bucket 0.  If larger than the first but smaller than the
	  second division, it goes into bucket 1, etc.  Contents has one
	more element than divisions, so values greater than the last division
	go into the last bucket.*/
    }

    public void screenDump() {
	for (int i=0; i< divisions.length; i++) {
	    System.out.println("Value < " + divisions[i] + " occurred " + contents[i] + " times.");
	}
	System.out.println("Value > " + divisions[divisions.length - 1] + " occurred " + contents[divisions.length] + " times.");
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	int i;
	buf.append("'{");
	//return e.g. "{1,2,3}"
	for (i=0; i<numBuckets-1; i++) {
	    buf.append(Long.toString(contents[i]) + ",");
	}
	buf.append(Long.toString(contents[numBuckets-1]));
	buf.append("}'");
	return buf.toString();
    }

    public int getBaseType() throws SQLException {
	return java.sql.Types.INTEGER;
    }

    public Object getArray() throws SQLException {
	return contents;
    }

    public Object getArray(Map map) throws SQLException {
	return null;
    }
    
    public Object getArray(long index, int count) throws SQLException {
	return null;
    }

    public Object getArray(long index, int count, Map map) throws SQLException {
	return null;
    }

    public ResultSet getResultSet() throws SQLException {
	return null;
    }

    public ResultSet getResultSet(Map map) throws SQLException {
	return null;
    }

    public ResultSet getResultSet(long index, int count) throws SQLException {
	return null;
    }

    public ResultSet getResultSet(long index, int count, Map map) throws SQLException {
	return null;
    }
}
