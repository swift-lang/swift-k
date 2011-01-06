package org.globus.cog.monitor.guss;

import org.jfree.data.HighLowDataset;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.SeriesException;
import java.util.Collections;


/**
 * A custom subclass of org.jfree.data.time.TimeSeriesCollection which supports multiple
 * data series, with standard-deviation values attatched to each data point, and
 * implements org.jfree.data.HighLowDataset so that it can be drawn by a CandlestickRenderer.
 * Contains one or more {@link StdDevSeries}, each containing one or more {@link StdDevDataItem}s,
 * each of which represents a mean with a standard deviation.
 * @version 1.0
 */
class StdDevDataset extends TimeSeriesCollection 
    implements HighLowDataset, IntervalXYDataset {

    /*Stuff to implement HighLowDataset:*/

    /**@param series the index of the desired {@link StdDevSeries series} within this dataset
     *@param item the index of the desired {@link StdDevDataItem item} within that series
     *@return the high value for that item (i.e. one standard-deviation above the mean)*/
    public Number getHighValue(int series, int item) {
	TimeSeries ts = getSeries(series);
	if (ts instanceof StdDevSeries) {
	    StdDevSeries sds = (StdDevSeries)ts;
	    return new Double(sds.getValue(item).doubleValue() 
			      + sds.getStdDev(item));
	}
	else return ts.getValue(item);
    }

    /**Same as {@link #getHighValue}, but returns a double primitive instead of a Number object.*/
    public double getHigh(int series, int item) {
	return getHighValue(series, item).doubleValue();
    }

    /**@param series the index of the desired {@link StdDevSeries series} within this dataset
     *@param item the index of the desired {@link StdDevDataItem item} within that series
     *@return one standard-deviation below the mean for that item*/
    public Number getLowValue(int series, int item) {
	TimeSeries ts = getSeries(series);
	if (ts instanceof StdDevSeries) {
	    StdDevSeries sds = (StdDevSeries)ts;
	    return new Double(sds.getValue(item).doubleValue() 
			      - sds.getStdDev(item));
	}
	else return ts.getValue(item);

    }
    /**Same as {@link #getLowValue}, but returns a double primitive instead of a Number object.*/
    public double getLow(int series, int item) {
	return getLowValue(series, item).doubleValue();
    }
    /**Same as getVolumeValue (redundant but needed to implement HighLowDataset.)*/
    public Number getOpenValue(int series, int item) {
	return getSeries(series).getValue(item);
    }
    /**Same as getVolume (redundant but needed to implement HighLowDataset.)*/
    public double getOpen(int series, int item) {
	return getOpenValue(series,item).doubleValue();
    }
    /**Same as getVolumeValue (redundant but needed to implement HighLowDataset.)*/
    public Number getCloseValue(int series, int item) {
	return getSeries(series).getValue(item);
    }
    /**Same as getVolume (redundant but needed to implement HighLowDataset.)*/
    public double getClose(int series, int item) {
	return getCloseValue(series, item).doubleValue();
    }
    /**@param series the index of the desired {@link StdDevSeries series} within this dataset
     *@param item the index of the desired {@link StdDevDataItem item} within that series
     *@return the mean for that item*/
    public Number getVolumeValue(int series, int item){
	return getSeries(series).getValue(item);	
    }
    /**Same as {@link getVolumeValue}, but returns a double primitive instead of a Number object.*/
    public double getVolume(int series, int item) {
	return getVolumeValue(series, item).doubleValue();
    }

}

/**
 * Represents a series of X-Y datapoints where X is a time, Y is the
 * mean of some quantity, and each mean has a standard deviation
 * associated.  My custom subclass of jfree.org.data.time.TimeSeries,
 * made to hold {@link StdDevDataItem}s and to be included in a {@link
 * StdDevDataset}.
 * @version 1.0
 */
class StdDevSeries extends TimeSeries {
    /*Made to hold StdDevDataItems instead of TimeSeriesDataItems.
     (Although it can still hold TimeSeriesDataItems... or even a mixture
     of the two).  Constructors are all the same as TimeSeries...*/

    /**
     *Creates a new empty series with the given name.
     *@param name a name for the new series.*/
    public StdDevSeries(final String name) {
	super(name);
    }

    /**
     *Creates a new empty series with the given name and with the stipulation that each data item
     *will represent not a point in time but a duration specified by the timePeriodClass.
     *The timePeriodClasses are defined in org.jfree.data.time.
     *@param name a name for the new series.
     *@param timePeriodClass one of Minute.Class, Hour.Class, Day.Class, or Week.Class*/
    public StdDevSeries(final String name, final Class timePeriodClass) {
	super(name, timePeriodClass);
    }

    /**
     *Creates a new empty series, as {@link StdDevSeries(name, Class)} but also allows a domain
     *and range to be specified.*/
    public StdDevSeries(final String name, final String domain,
		      final String range, final Class timePeriodClass) {

        super(name, domain, range, timePeriodClass);
    }

    /*All of the getItem, add, update, etc. of the superclass are unchanged--
      rather than override them, I have overloaded or made new methods which
      are specifically for StdDevDataItems.*/

    /**
     *@param index the index of the desired {@link StdDevDataItem} within this series.
     *@return the item.*/
    public StdDevDataItem getStdDevDataItem(final int index) {
        return (StdDevDataItem) this.data.get(index);
    }

    /**
     *@param period an org.jfree.data.time.RegularTimePeriod object specifying the time period in which to search for a data item
     *@return the item found, if there was one for that time period; <code>null</code> otherwise.*/
    public StdDevDataItem getStdDevDataItem(final RegularTimePeriod period) {

        // check arguments...
        if (period == null) {
            throw new IllegalArgumentException("Null 'period' argument");
        }

        // fetch the value...
        final TimeSeriesDataItem dummy = new TimeSeriesDataItem(period, new Integer(0));
        final int index = Collections.binarySearch(this.data, dummy);
        if (index >= 0) {
            return (StdDevDataItem) this.data.get(index);
        }
        else {
            return null;
        }

    }


    //use along with getValue, which still gets value
    /**
     *@param index the index of the desired {@link StdDevDataItem} within this series
     *@return the standard deviation of that item.*/
    public double getStdDev(final int index) {
	return getStdDevDataItem(index).getStdDev();
    }

    /**
     *@param period an org.jfree.data.time.RegularTimePeriod object specifying the time period in which to search for a data item
     *@return the standard deviation of that item, if one was found; 0 otherwise.*/
    public double getStdDev(final RegularTimePeriod period) {

        final int index = getIndex(period);
        if (index >= 0) {
            return getStdDev(index);
        }
        else {
            return 0;
        }

    }

    /*add(item) will work for either TimeSeriesItem or StdDevDataItem.*/

    /**
     *Creates and adds a new {@link StdDevDataItem to the series with the specified values.
     *@param period an org.jfree.data.time.RegularTimePeriod object which will determine the x-coordinate of the new item
     *@param value the y-coordinate of the new item
     *@param sigma the standard deviation of the new item*/ 
    public void add(final RegularTimePeriod period, final double value, 
		    final double sigma){ 
        final StdDevDataItem item = new StdDevDataItem(period, value, sigma);
        add(item);
    }
    /**
     *Same as {@link #add(RegularTimePeriod, double, double)} but takes a Number object instead of a 
     *double primitive.*/
    public void add(final RegularTimePeriod period, final Number value, 
		    final double sigma) {
        final StdDevDataItem item = new StdDevDataItem(period, value, sigma);
        add(item);
    }

    /**
     *Looks for a {@link StdDevDataItem} at the given time period, and updates its value and
     *standard deviation if it exists.
     *@param period an org.jfree.data.time.RegularTimePeriod object specifying the time period in which to search for the data item
     *@param value the new y-coordinate for the item
     *@param sigma the new standard deviation for the item
     *@throws org.jfree.data.SeriesException if no data item is found at that time period.*/ 
    public void update(final RegularTimePeriod period, final Number value, 
		       final double sigma) throws org.jfree.data.SeriesException {
        final StdDevDataItem temp = new StdDevDataItem(period, value, sigma);
        final int index = Collections.binarySearch(this.data, temp);
        if (index >= 0) {
            final StdDevDataItem pair = (StdDevDataItem) this.data.get(index);
            pair.setValue(value);
	    pair.setStdDev(sigma);
            fireSeriesChanged();
        }
        else {
            throw new SeriesException(
                "StdDevSeries.update(TimePeriod, Number, double):  period does not exist."
            );
        }

    }

    /**
     *Looks for a {@link StdDevDataItem} with the given index, and updates its value and
     *standard deviation if it exists.
     *@param index the index of the desired item
     *@param value the new y-coordinate for the item
     *@param sigma the new standard deviation for the item
     *@throws org.jfree.data.SeriesException if no data item is found at that index.*/ 
    public void update(final int index, final Number value, final double sigma) 
	throws org.jfree.data.SeriesException {

        final TimeSeriesDataItem item = getDataItem(index);
	if (item != null) {
	    if (item instanceof StdDevDataItem) {
		StdDevDataItem sd = (StdDevDataItem)item;
		sd.setStdDev(sigma);
	    }
	    item.setValue(value);
	    fireSeriesChanged();
	}
	else {
	    throw new SeriesException(
                "StdDevSeries.update(int, Number, double):  index does not exist."
            );
        }
    }

    /**
     *Same as {@link #addOrUpdate(RegularTimePeriod, Number, double)} but takes a double primitive
     *for value instead of a Number object*/
     public StdDevDataItem addOrUpdate(final RegularTimePeriod period, final double value, final double sigma) {
     return this.addOrUpdate(period, new Double(value), sigma);    
     }
    
    /**
     *Updates the value and standard deviation of the item if it already exists; creates it if it
     *does not exist.
     *@param period an org.jfree.data.time.RegularTimePeriod object specifying the time period in which to search for the data item
     *@param value the new y-coordinate for the item
     *@param sigma the new standard deviation for the item
     *@return a reference to the newly added/updated item
     */
    public StdDevDataItem addOrUpdate(final RegularTimePeriod period, final Number value, final double sigma) {
	
        if (period == null) {
            throw new IllegalArgumentException("Null 'period' argument.");   
        }
        StdDevDataItem overwritten = null;

        final StdDevDataItem key = new StdDevDataItem(period, value, sigma);
        final int index = Collections.binarySearch(this.data, key);
        if (index >= 0) {
            final StdDevDataItem existing = (StdDevDataItem) this.data.get(index);
            overwritten = (StdDevDataItem) existing.clone();
            existing.setValue(value);
	    existing.setStdDev(sigma);
            ageHistoryCountItems();
            fireSeriesChanged();
        }
        else {
            this.data.add(-index - 1, new StdDevDataItem(period, value, sigma));
            ageHistoryCountItems();
            fireSeriesChanged();
        }
        return overwritten;

    }




}

/**
 * A custom subclass of TimeSeriesDataItem (from org.jfree.data.time)
 * which in addition to a value and a time, stores a standard
 * deviation.  Instances of this class are included in a {@link
 * StdDevDataSeries} and each one will be plotted on a histogram as a
 * data point with a vertical bar.
 * @version 1.0
 */
class StdDevDataItem extends TimeSeriesDataItem {
    private double stdDev;

    /**
     *This constructor takes the value as a Number object.
     *@param period the time period this data item corresponds to.  Will be the x-coordinate when plotted.  (RegularTimePeriod defined in org.jfree.data.time)
     *@param value the numerical value of this data item; should be an average of something.  (If it isn't, TimeSeriesDataItem would be more appropriate).  Will be the y-coordinate when plotted.
     *@param sigma the standard-deviation associated with this average.
     */
    public StdDevDataItem(final RegularTimePeriod period, final Number value, final double sigma) {
	super(period, value);
	this.stdDev = sigma;
    }

    /**
     *This constructor takes the value as a double primitive.
     *@param period the time period this data item corresponds to.  Will be the x-coordinate when plotted.  (RegularTimePeriod defined in org.jfree.data.time)
     *@param value the numerical value of this data item; should be an average of something.  (If it isn't, TimeSeriesDataItem would be more appropriate).  Will be the y-coordinate when plotted.
     *@param sigma the standard-deviation associated with this average.
     */
    public StdDevDataItem(final RegularTimePeriod period, final double value, final double sigma) {
	super(period, value);
	this.stdDev = sigma;
    }

    /**@return the standard deviation for this data item*/
    public double getStdDev() {
	return stdDev;
    }

    /**Sets the standard deviation for this data item
     *@param sigma the new standard deviation*/
    public void setStdDev(double sigma) {
	stdDev = sigma;
    }

    /**
     *Overriding the equals method in java.lang.Object.
     *@param another object to compare this one to
     *@return true if the objects are both StdDevDataItems and both have the same value and the same standard deviation; false otherwise.*/
    public boolean equals(final Object o) {
	if (this == o)
	    return true;
	if (!super.equals(o))
	    return false;
	if (!(o instanceof StdDevDataItem)) {
            return false;
        }
        final StdDevDataItem stdDevDataItem = (StdDevDataItem) o;
        
	return (this.stdDev == stdDevDataItem.stdDev);
    }

}
